import tempfile
import unittest
from pathlib import Path

from scripts.upgrade_test.logcheck import check_client_log, check_server_log


class LogCheckTest(unittest.TestCase):
    def setUp(self):
        self.tmp = tempfile.TemporaryDirectory()
        self.dir = Path(self.tmp.name)
        self.ignore = self.dir / "ignore.txt"
        self.ignore.write_text("# comment\n\nKnownIssue: .*\n")

    def tearDown(self):
        self.tmp.cleanup()

    def _log(self, *lines):
        path = self.dir / "latest.log"
        path.write_text("\n".join(lines) + "\n")
        return path

    def test_server_log_ignores_defaults_and_file(self):
        log = self._log(
            "[12:00:00] [Server thread/INFO]: Done (3.1s)!",
            "[12:00:00] [Server thread/WARN]: **** SERVER IS RUNNING IN OFFLINE/INSECURE MODE!",
            "[12:00:00] [Server thread/ERROR]: No data fixer registered for chronodawn:time_keeper",
            "\x1b[33m[12:00:01] [Server thread/WARN]: KnownIssue: something\x1b[0m",
            "[00:47:35] [Server thread/WARN]: Can't keep up! Is the server overloaded? Running 6185ms or 123 ticks behind",
        )
        self.assertEqual(check_server_log(log, self.ignore), [])

    def test_server_log_reports_unexpected(self):
        log = self._log("[12:00:02] [Server thread/ERROR]: Failed to load chunk")
        self.assertEqual(check_server_log(log, self.ignore), ["[12:00:02] [Server thread/ERROR]: Failed to load chunk"])

    def test_client_crash_report_dir_fails(self):
        game_dir = self.dir / "game"
        (game_dir / "crash-reports").mkdir(parents=True)
        (game_dir / "crash-reports" / "crash-2026.txt").write_text("boom\n")
        problems = check_client_log(self._log("[12:00:00] [Render thread/INFO]: ok"), game_dir, self.ignore)
        self.assertTrue(any("crash-2026.txt" in p for p in problems))

    def test_client_crash_banner_fails(self):
        log = self._log("---- Minecraft Crash Report ----")
        self.assertEqual(len(check_client_log(log, self.dir, self.ignore)), 1)

    def test_client_log_ignores_non_chronodawn_warn(self):
        log = self._log(
            "[12:00:00] [Render thread/WARN]: Missing sound for event: minecraft:item.goat_horn.play",
            "[12:00:00] [Render thread/WARN]: Missing textures in model chronodawn:block/foo#",
        )
        problems = check_client_log(log, self.dir, self.ignore)
        self.assertEqual(problems, ["[12:00:00] [Render thread/WARN]: Missing textures in model chronodawn:block/foo#"])


if __name__ == "__main__":
    unittest.main()
