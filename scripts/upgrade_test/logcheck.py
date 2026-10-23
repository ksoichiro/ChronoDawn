"""WARN/ERROR checks for the post-upgrade server and client logs."""

import re
from pathlib import Path

_LEVEL = re.compile(r"\[[^\]]*/(WARN|ERROR|FATAL)\]")
_ANSI = re.compile(r"\x1b\[[0-9;]*m")

# Noise caused by the harness itself (offline mode, RCON, fresh install) or
# by every upgrade regardless of mod changes. Mirrors prod-run.gradle's
# PROD_SMOKE_DEFAULT_LOG_IGNORES, plus what only an upgrade produces.
DEFAULT_IGNORES = [
    r"\*\*\*\* SERVER IS RUNNING IN OFFLINE/INSECURE MODE!",
    r"The server will make no attempt to authenticate usernames",
    r"While this makes the game possible to play without internet access",
    r'To change this, set "online-mode" to "true"',
    r"LanServerPinger: No route to host",
    r"VanillaPackResourcesBuilder.*uses unexpected schema",
    r"FMLConfig.*is not correct\. Correcting",
    r"No data fixer registered for ",
    # NeoForge warns when a world was last saved with different mod versions,
    # which is exactly what an upgrade does.
    r"version differences",
    r"RCON",
    # The forward worldgen check force-generates 256 chunks at once.
    r"Can't keep up! Is the server overloaded\?",
]


def _patterns(ignore_file: Path) -> list[re.Pattern]:
    patterns = [re.compile(p) for p in DEFAULT_IGNORES]
    if ignore_file.exists():
        for i, raw in enumerate(ignore_file.read_text().splitlines(), start=1):
            entry = raw.strip()
            if not entry or entry.startswith("#"):
                continue
            try:
                patterns.append(re.compile(entry))
            except re.error as e:
                raise ValueError(f"Invalid regex at {ignore_file}:{i}: {e}") from e
    return patterns


def _unexpected(log: Path, ignore_file: Path, extra_filter=lambda line: True) -> list[str]:
    patterns = _patterns(ignore_file)
    found = []
    for raw in log.read_text(errors="replace").splitlines():
        line = _ANSI.sub("", raw)
        if _LEVEL.search(line) and extra_filter(line) and not any(p.search(line) for p in patterns):
            found.append(line)
    return found


def check_server_log(log: Path, ignore_file: Path) -> list[str]:
    return _unexpected(log, ignore_file)


def check_client_log(log: Path, game_dir: Path, ignore_file: Path) -> list[str]:
    """Client logs are noisy; only crashes and WARN/ERROR lines about this mod count."""
    problems = []
    crash_dir = game_dir / "crash-reports"
    if crash_dir.exists():
        problems += [f"crash report: {p}" for p in sorted(crash_dir.iterdir())]
    text = log.read_text(errors="replace") if log.exists() else ""
    if "---- Minecraft Crash Report ----" in text:
        problems.append(f"crash report printed in {log}")
    problems += _unexpected(log, ignore_file, lambda line: "chronodawn" in line.lower())
    return problems
