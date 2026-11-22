"""Take screenshots and send player input inside the Minecraft client through a small Java agent.

The agent (agent/ScenarioShotAgent.java) is loaded into the client JVM with
-javaagent. It saves a screenshot from the game's own framebuffer, the way F2
does, and clicks the game's key mappings for player input. Unlike macOS
screen capture or synthetic OS events, that needs no Screen Recording or
Accessibility permission and keeps working while the screen is locked or the
window is covered.

The agent is compiled with the client's own JDK on first use, so the driver
needs no build step for it.
"""

import shutil
import subprocess
import itertools
import tempfile
import time
from pathlib import Path

AGENT_SOURCE = Path(__file__).resolve().parent / "agent" / "ScenarioShotAgent.java"
SHOT_TIMEOUT = 60
REQUEST_TIMEOUT = 30
# The settle step asks for the render state several times a second.
POLL_SECONDS = 0.05


class CaptureError(Exception):
    pass


def build_agent(java: str, out_dir: Path) -> Path:
    """Compile the agent with the JDK that runs the client and return its jar."""
    bin_dir = Path(java).parent
    javac, jar = bin_dir / "javac", bin_dir / "jar"
    if not javac.exists() or not jar.exists():
        raise CaptureError(f"{bin_dir} has no javac or jar. The client must run on a JDK to load the screenshot agent")
    out_dir.mkdir(parents=True, exist_ok=True)
    agent_jar = out_dir / "scenario-shot-agent.jar"
    with tempfile.TemporaryDirectory() as tmp:
        classes = Path(tmp) / "classes"
        manifest = Path(tmp) / "MANIFEST.MF"
        manifest.write_text("Premain-Class: ScenarioShotAgent\n")
        for command in ([str(javac), "-d", str(classes), str(AGENT_SOURCE)],
                        [str(jar), "--create", "--file", str(agent_jar), "--manifest", str(manifest),
                         "-C", str(classes), "."]):
            result = subprocess.run(command, capture_output=True, text=True)
            if result.returncode != 0:
                raise CaptureError(f"{Path(command[0]).name} failed: {result.stderr.strip() or result.stdout.strip()}")
    return agent_jar


def agent_jvm_arg(agent_jar: Path, request_dir: Path) -> str:
    return f"-javaagent:{agent_jar}={request_dir}"


def request(request_dir: Path, name: str, operation: str, alive=lambda: True, timeout=REQUEST_TIMEOUT,
            waiting_for="an answer") -> str:
    """Send one operation to the agent and return its answer."""
    request_dir.mkdir(parents=True, exist_ok=True)
    done = request_dir / f"{name}.done"
    done.unlink(missing_ok=True)
    # Renamed into place so the agent never reads a half-written request.
    tmp = request_dir / f"{name}.request.tmp"
    tmp.write_text(operation)
    tmp.replace(request_dir / f"{name}.request")
    deadline = time.monotonic() + timeout
    while not done.exists():
        if not alive():
            raise CaptureError(f"the client exited before {waiting_for} came back")
        if time.monotonic() >= deadline:
            raise CaptureError(f"no {waiting_for} from the client after {timeout}s")
        time.sleep(POLL_SECONDS)
    answer = done.read_text().strip()
    done.unlink()
    return answer


def take(request_dir: Path, name: str, out_file: Path, alive=lambda: True, timeout=SHOT_TIMEOUT) -> Path:
    """Ask the agent for one screenshot and move it to out_file."""
    saved = request_dir / "screenshots" / f"{name}.png"
    saved.unlink(missing_ok=True)
    message = request(request_dir, name, "shot", alive, timeout, "screenshot")
    if not saved.exists():
        raise CaptureError(f"the client did not save the screenshot: {message}")
    out_file = Path(out_file)
    out_file.parent.mkdir(parents=True, exist_ok=True)
    shutil.move(str(saved), out_file)
    return out_file


class Client:
    """Sends input operations (use, attack, close, screen, click, slot, hover, scroll) to the agent."""

    def __init__(self, request_dir: Path, alive=lambda: True, timeout=REQUEST_TIMEOUT):
        self.request_dir = request_dir
        self.alive = alive
        self.timeout = timeout
        # Shot names are letters, digits, '.', '_' and '-', so '~' cannot clash with them.
        self._ids = itertools.count(1)

    def __call__(self, operation: str) -> str:
        # Only the operation word goes into the file name, not its arguments.
        name = f"~{operation.split()[0]}-{next(self._ids)}"
        return request(self.request_dir, name, operation, self.alive, self.timeout, f"answer to {operation!r}")
