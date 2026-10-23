import unittest

from scripts.upgrade_test.run import read_when_ready


class FakeRcon:
    def __init__(self, replies):
        self.replies = list(replies)
        self.calls = 0

    def command(self, _cmd):
        self.calls += 1
        return self.replies.pop(0) if len(self.replies) > 1 else self.replies[0]


class ReadWhenReadyTest(unittest.TestCase):
    def test_retries_until_entity_is_loaded(self):
        rcon = FakeRcon(["No entity was found", "No entity was found", "Sheep has the following entity data: 1b"])
        self.assertEqual(read_when_ready(rcon, "data get entity @e[limit=1]", timeout=5, interval=0),
                         "Sheep has the following entity data: 1b")
        self.assertEqual(rcon.calls, 3)

    def test_gives_up_with_last_reply(self):
        rcon = FakeRcon(["That position is not loaded"])
        self.assertEqual(read_when_ready(rcon, "data get block 0 0 0", timeout=0.05, interval=0.01),
                         "That position is not loaded")


if __name__ == "__main__":
    unittest.main()


import json
import tempfile
from pathlib import Path
from unittest import mock

from scripts.upgrade_test import run as run_module
from scripts.upgrade_test.rcon import RconError


class SetupEntryTest(unittest.TestCase):
    def test_failed_setup_command_raises_with_layer_and_command(self):
        rcon = FakeRcon(["Unknown item 'chronodawn:nope'"])
        with self.assertRaises(run_module.SetupError) as ctx:
            run_module._run_setup_entry(rcon, "give Dev chronodawn:nope 1", [], "0.10.0-chronite")
        self.assertIn("0.10.0-chronite", str(ctx.exception))
        self.assertIn("give Dev chronodawn:nope 1", str(ctx.exception))

    def test_successful_setup_command_passes(self):
        transcript = []
        run_module._run_setup_entry(FakeRcon(["Gave 1 [Clockstone] to Dev"]), "give Dev chronodawn:clockstone 1",
                                    transcript, "0.8.0-base")
        self.assertEqual(len(transcript), 1)


class MainTest(unittest.TestCase):
    def _env_file(self, tmp):
        out = Path(tmp) / "out"
        out.mkdir()
        (out / "report.txt").write_text("PASS stale report from an earlier run\n")
        env = {
            "loader": "fabric", "repoRoot": tmp, "cacheDir": tmp, "outDir": str(out), "baseline": "0.8.0",
            "candidateJar": str(Path(tmp) / "c.jar"), "prodMods": [], "ports": [1, 2],
            "logIgnoreFile": str(Path(tmp) / "none.txt"), "serverTemplateDir": tmp, "serverCommand": [],
            "client": {"java": "java", "jvmArgs": [], "classpath": [], "mainClass": "M", "args": []},
        }
        path = Path(tmp) / "env.json"
        path.write_text(json.dumps(env))
        return path, out

    def test_unexpected_error_is_reported_and_other_sources_still_run(self):
        with tempfile.TemporaryDirectory() as tmp:
            env_file, out = self._env_file(tmp)
            calls = []

            def fake_run_source(env, source, *args):
                calls.append(source)
                if source == "0.8.0":
                    raise RconError("RCON connection closed")
                return [("PASS", f"{source}/x", "")]

            with mock.patch.object(run_module, "run_source", fake_run_source):
                code = run_module.main(["--env", str(env_file), "--sources", "0.8.0,0.13.0"])
            report = (out / "report.txt").read_text()
            self.assertEqual(code, 1)
            self.assertEqual(calls, ["0.8.0", "0.13.0"])
            self.assertIn("FAIL 0.8.0/harness: RconError: RCON connection closed", report)
            self.assertNotIn("stale", report)

    def test_report_is_removed_before_running(self):
        with tempfile.TemporaryDirectory() as tmp:
            env_file, out = self._env_file(tmp)

            def fake_run_source(*args):
                self.assertFalse((out / "report.txt").exists())
                return []

            with mock.patch.object(run_module, "run_source", fake_run_source):
                run_module.main(["--env", str(env_file), "--sources", "0.8.0"])
