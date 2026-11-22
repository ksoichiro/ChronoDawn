"""Take screenshots inside the Minecraft client through a small Java agent.

The agent (agent/ScenarioShotAgent.java) is loaded into the client JVM with
-javaagent and saves a screenshot from the game's own framebuffer, the way F2
does. Unlike macOS screen capture, that needs no Screen Recording permission
and keeps working while the screen is locked or the window is covered.

The agent is compiled with the client's own JDK on first use, so the driver
needs no build step for it.
"""

import shutil
import subprocess
import tempfile
import time
from pathlib import Path

AGENT_SOURCE = Path(__file__).resolve().parent / "agent" / "ScenarioShotAgent.java"
SHOT_TIMEOUT = 60


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


def take(request_dir: Path, name: str, out_file: Path, alive=lambda: True, timeout=SHOT_TIMEOUT) -> Path:
    """Ask the agent for one screenshot and move it to out_file."""
    request_dir.mkdir(parents=True, exist_ok=True)
    done = request_dir / f"{name}.done"
    saved = request_dir / "screenshots" / f"{name}.png"
    done.unlink(missing_ok=True)
    saved.unlink(missing_ok=True)
    (request_dir / f"{name}.request").write_text("")
    deadline = time.monotonic() + timeout
    while not done.exists():
        if not alive():
            raise CaptureError("the client exited before the screenshot was saved")
        if time.monotonic() >= deadline:
            raise CaptureError(f"no screenshot from the client after {timeout}s")
        time.sleep(0.2)
    message = done.read_text().strip()
    done.unlink()
    if not saved.exists():
        raise CaptureError(f"the client did not save the screenshot: {message}")
    out_file = Path(out_file)
    out_file.parent.mkdir(parents=True, exist_ok=True)
    shutil.move(str(saved), out_file)
    return out_file
