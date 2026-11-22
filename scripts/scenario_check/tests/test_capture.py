import tempfile
import threading
import time
import unittest
from pathlib import Path

from scripts.scenario_check import capture


class FakeAgent:
    """Answers requests the way ScenarioShotAgent does."""

    def __init__(self, request_dir: Path, save=True, message="Saved screenshot"):
        self.request_dir = request_dir
        self.save = save
        self.message = message
        self.operations = []
        self.stop = threading.Event()
        self.thread = threading.Thread(target=self._loop, daemon=True)

    def __enter__(self):
        self.thread.start()
        return self

    def __exit__(self, *exc):
        self.stop.set()
        self.thread.join(5)

    def _loop(self):
        while not self.stop.is_set():
            for request in self.request_dir.glob("*.request"):
                name = request.name[:-len(".request")]
                operation = request.read_text().strip()
                request.unlink()
                self.operations.append(operation)
                if operation != "shot":
                    (self.request_dir / f"{name}.done").write_text(f"did {operation}")
                    continue
                if self.save:
                    shots = self.request_dir / "screenshots"
                    shots.mkdir(exist_ok=True)
                    (shots / f"{name}.png").write_bytes(b"png")
                (self.request_dir / f"{name}.done").write_text(self.message)
            time.sleep(0.05)


class TakeTest(unittest.TestCase):
    def setUp(self):
        self.tmp = tempfile.TemporaryDirectory()
        self.dir = Path(self.tmp.name)
        self.request_dir = self.dir / "agent"
        self.request_dir.mkdir()

    def tearDown(self):
        self.tmp.cleanup()

    def test_moves_the_saved_screenshot(self):
        out = self.dir / "shots" / "front.png"
        with FakeAgent(self.request_dir):
            self.assertEqual(capture.take(self.request_dir, "front", out, timeout=5), out)
        self.assertEqual(out.read_bytes(), b"png")
        self.assertEqual(list(self.request_dir.glob("front.*")), [])

    def test_reports_the_message_when_nothing_was_saved(self):
        with FakeAgent(self.request_dir, save=False, message="Couldn't save screenshot"):
            with self.assertRaisesRegex(capture.CaptureError, "Couldn't save screenshot"):
                capture.take(self.request_dir, "front", self.dir / "front.png", timeout=5)

    def test_times_out_without_an_agent(self):
        with self.assertRaisesRegex(capture.CaptureError, "no screenshot"):
            capture.take(self.request_dir, "front", self.dir / "front.png", timeout=0.3)

    def test_stops_when_the_client_exits(self):
        with self.assertRaisesRegex(capture.CaptureError, "client exited"):
            capture.take(self.request_dir, "front", self.dir / "front.png", alive=lambda: False, timeout=5)


class ClientTest(unittest.TestCase):
    def setUp(self):
        self.tmp = tempfile.TemporaryDirectory()
        self.request_dir = Path(self.tmp.name)

    def tearDown(self):
        self.tmp.cleanup()

    def test_sends_operations_and_returns_answers(self):
        client = capture.Client(self.request_dir, timeout=5)
        with FakeAgent(self.request_dir) as agent:
            self.assertEqual(client("use"), "did use")
            self.assertEqual(client("screen"), "did screen")
        self.assertEqual(agent.operations, ["use", "screen"])
        self.assertEqual(list(self.request_dir.iterdir()), [])

    def test_times_out_without_an_agent(self):
        with self.assertRaisesRegex(capture.CaptureError, "answer to 'use'"):
            capture.Client(self.request_dir, timeout=0.3)("use")


class BuildAgentTest(unittest.TestCase):
    def test_rejects_a_runtime_without_javac(self):
        with tempfile.TemporaryDirectory() as tmp:
            java = Path(tmp) / "bin" / "java"
            java.parent.mkdir()
            java.write_text("")
            with self.assertRaisesRegex(capture.CaptureError, "no javac"):
                capture.build_agent(str(java), Path(tmp) / "out")


if __name__ == "__main__":
    unittest.main()
