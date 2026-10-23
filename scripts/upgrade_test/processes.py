"""Server and client process lifecycle for the upgrade test."""

import atexit
import os
import re
import shutil
import signal
import socket
import subprocess
import sys
import threading
import time
from dataclasses import dataclass
from pathlib import Path

RCON_PASSWORD = "upgradetest"
LEVEL_SEED = "20261023"

_live = set()


class PortInUse(Exception):
    def __init__(self, port):
        super().__init__(f"Port {port} is already in use (a server from an earlier run may still be running)")
        self.port = port


class ProcessExited(Exception):
    def __init__(self, code, log_file):
        super().__init__(f"Process exited with code {code} before it was ready. See log: {log_file}")
        self.code = code
        self.log_file = log_file


class ReadyTimeout(Exception):
    pass


def preflight_ports(ports: list[int]) -> None:
    for port in ports:
        with socket.socket() as sock:
            if sock.connect_ex(("127.0.0.1", port)) == 0:
                raise PortInUse(port)


def _kill_all():
    for proc in list(_live):
        proc.kill()


def install_signal_handlers():
    """Kill every child JVM on Ctrl+C or SIGTERM so none is left holding a port."""
    def handler(signum, _frame):
        _kill_all()
        sys.exit(128 + signum)
    signal.signal(signal.SIGINT, handler)
    signal.signal(signal.SIGTERM, handler)


atexit.register(_kill_all)


class ManagedProcess:
    def __init__(self, command: list[str], cwd: Path, log_file: Path):
        self.command = [str(c) for c in command]
        self.cwd = Path(cwd)
        self.log_file = Path(log_file)
        self._proc = None
        self._lines = []
        self._cursor = 0
        self._cond = threading.Condition()
        self._reader = None

    def start(self):
        self.log_file.parent.mkdir(parents=True, exist_ok=True)
        # A new session keeps a terminal Ctrl+C from reaching the JVM directly,
        # so shutdown always goes through kill()/stop().
        self._proc = subprocess.Popen(
            self.command, cwd=self.cwd, stdin=subprocess.PIPE, stdout=subprocess.PIPE,
            stderr=subprocess.STDOUT, start_new_session=True,
        )
        _live.add(self)
        self._reader = threading.Thread(target=self._read, daemon=True)
        self._reader.start()
        return self

    def _read(self):
        with self.log_file.open("w", encoding="utf-8", errors="replace") as log:
            for raw in iter(self._proc.stdout.readline, b""):
                line = raw.decode("utf-8", errors="replace").rstrip("\r\n")
                log.write(line + "\n")
                log.flush()
                with self._cond:
                    self._lines.append(line)
                    self._cond.notify_all()
        with self._cond:
            self._cond.notify_all()

    def alive(self) -> bool:
        return self._proc is not None and self._proc.poll() is None

    @property
    def exit_code(self):
        return None if self._proc is None else self._proc.poll()

    def wait_for_line(self, pattern: str, timeout: float) -> str:
        """Wait for a line matching pattern after the previously matched one."""
        regex = re.compile(pattern)
        deadline = time.monotonic() + timeout
        with self._cond:
            while True:
                while self._cursor < len(self._lines):
                    line = self._lines[self._cursor]
                    self._cursor += 1
                    if regex.search(line):
                        return line
                if not self.alive() and not self._reader.is_alive():
                    code = self._proc.wait()
                    self._forget()
                    raise ProcessExited(code, self.log_file)
                remaining = deadline - time.monotonic()
                if remaining <= 0:
                    break
                self._cond.wait(min(remaining, 0.5))
        self.kill()
        raise ReadyTimeout(f"Timed out after {timeout}s waiting for /{pattern}/. See log: {self.log_file}")

    def send(self, line: str):
        self._proc.stdin.write((line + "\n").encode())
        self._proc.stdin.flush()

    def stop(self, graceful_command: str | None = None, timeout: float = 120):
        if not self.alive():
            self._forget()
            return
        try:
            if graceful_command:
                self.send(graceful_command)
            else:
                self._proc.terminate()
            self._proc.wait(timeout)
        except (subprocess.TimeoutExpired, BrokenPipeError, OSError):
            self.kill()
        self._forget()

    def kill(self):
        if self._proc is not None and self._proc.poll() is None:
            self._proc.kill()
            self._proc.wait()
        self._forget()

    def _forget(self):
        _live.discard(self)
        if self._proc is not None and self._proc.poll() is not None:
            if self._reader is not None:
                self._reader.join(5)
            for stream in (self._proc.stdin, self._proc.stdout):
                if stream is not None and not stream.closed:
                    stream.close()


@dataclass
class ServerEnv:
    template_dir: Path
    command: list[str]


@dataclass
class ClientEnv:
    java: str
    jvm_args: list[str]
    classpath: list[str]
    main_class: str
    args: list[str]


_TEMPLATE_SKIP = {"smoketest", "world", "mods", "logs", "crash-reports", "config", "eula.txt",
                  "server.properties", "smoke-test.log"}


def _clone_tree(src: Path, dst: Path):
    dst.mkdir(parents=True, exist_ok=True)
    for entry in src.iterdir():
        if entry.name in _TEMPLATE_SKIP:
            continue
        target = dst / entry.name
        # APFS clones keep copying the multi-hundred-MB runtime cheap on macOS.
        if subprocess.run(["cp", "-cR", str(entry), str(target)], capture_output=True).returncode != 0:
            if entry.is_dir():
                shutil.copytree(entry, target, symlinks=True)
            else:
                shutil.copy2(entry, target)


def _copy_mods(mods: list[Path], mods_dir: Path):
    if mods_dir.exists():
        shutil.rmtree(mods_dir)
    mods_dir.mkdir(parents=True)
    for mod in mods:
        shutil.copy2(mod, mods_dir / Path(mod).name)


def prepare_server_dir(env: ServerEnv, server_dir: Path, ports: tuple[int, int]):
    """Copy the runtime and write config once; the world inside persists across restarts."""
    _clone_tree(Path(env.template_dir), server_dir)
    (server_dir / "eula.txt").write_text("eula=true\n")
    (server_dir / "server.properties").write_text("\n".join([
        "level-name=world",
        f"level-seed={LEVEL_SEED}",
        "online-mode=false",
        "enable-rcon=true",
        f"rcon.password={RCON_PASSWORD}",
        f"rcon.port={ports[1]}",
        f"server-port={ports[0]}",
        "broadcast-rcon-to-ops=false",
        "view-distance=4",
        "simulation-distance=4",
        "spawn-protection=0",
        "max-tick-time=-1",
        "gamemode=creative",
        "enable-command-block=false",
        "",
    ]))


def run_server(env: ServerEnv, server_dir: Path, mods: list[Path], log_file: Path,
               timeout: float = 600) -> ManagedProcess:
    _copy_mods(mods, server_dir / "mods")
    proc = ManagedProcess(env.command, server_dir, log_file).start()
    proc.wait_for_line(r"Done \(", timeout)
    proc.wait_for_line(r"RCON running on", 60)
    return proc


_CLIENT_OPTIONS = """onboardAccessibility:false
pauseOnLostFocus:false
skipMultiplayerWarning:true
joinedFirstServer:true
tutorialStep:none
renderDistance:4
soundCategory_master:0.0
"""


def run_client(env: ClientEnv, game_dir: Path, mods: list[Path], server_port: int,
               log_file: Path) -> ManagedProcess:
    game_dir.mkdir(parents=True, exist_ok=True)
    _copy_mods(mods, game_dir / "mods")
    options = game_dir / "options.txt"
    if not options.exists():
        options.write_text(_CLIENT_OPTIONS)
    # NeoForge's early loading window probes the monitor and, when that fails,
    # blocks on an error dialog nobody will click. The game window itself does
    # not need it. Fabric ignores this file.
    fml = game_dir / "config" / "fml.toml"
    if not fml.exists():
        fml.parent.mkdir(parents=True, exist_ok=True)
        fml.write_text("earlyWindowControl = false\n")
    args = list(env.args)
    if "--gameDir" in args:
        args[args.index("--gameDir") + 1] = str(game_dir)
    else:
        args += ["--gameDir", str(game_dir)]
    args += ["--quickPlayMultiplayer", f"127.0.0.1:{server_port}"]
    command = [env.java, *env.jvm_args, "-cp", os.pathsep.join(env.classpath), env.main_class, *args]
    return ManagedProcess(command, game_dir, log_file).start()
