import socket
import sys
import tempfile
import time
import unittest
from pathlib import Path

from scripts.upgrade_test.processes import ManagedProcess, PortInUse, ProcessExited, ReadyTimeout, preflight_ports


class ProcessesTest(unittest.TestCase):
    def setUp(self):
        self.tmp = tempfile.TemporaryDirectory()
        self.dir = Path(self.tmp.name)

    def tearDown(self):
        self.tmp.cleanup()

    def _proc(self, code):
        return ManagedProcess([sys.executable, "-u", "-c", code], self.dir, self.dir / "out.log")

    def test_port_in_use_preflight(self):
        sock = socket.socket()
        sock.bind(("127.0.0.1", 0))
        sock.listen(1)
        port = sock.getsockname()[1]
        try:
            with self.assertRaises(PortInUse) as ctx:
                preflight_ports([port])
            self.assertEqual(ctx.exception.port, port)
        finally:
            sock.close()

    def test_wait_for_line_ready(self):
        proc = self._proc("print('Done (1.0s)!'); import time; time.sleep(30)")
        proc.start()
        try:
            self.assertIn("Done (", proc.wait_for_line(r"Done \(", 10))
        finally:
            proc.kill()
        self.assertFalse(proc.alive())
        self.assertIn("Done (1.0s)!", (self.dir / "out.log").read_text())

    def test_wait_for_line_only_sees_new_lines(self):
        proc = self._proc("import time; print('joined'); time.sleep(1); print('joined'); time.sleep(30)")
        proc.start()
        try:
            proc.wait_for_line("joined", 10)
            start = time.monotonic()
            proc.wait_for_line("joined", 10)
            self.assertGreater(time.monotonic() - start, 0.5)
        finally:
            proc.kill()

    def test_process_exit_before_ready_fails(self):
        proc = self._proc("import sys; sys.exit(3)")
        proc.start()
        with self.assertRaises(ProcessExited) as ctx:
            proc.wait_for_line("never", 10)
        self.assertEqual(ctx.exception.code, 3)

    def test_timeout_kills_process(self):
        proc = self._proc("import time; time.sleep(30)")
        proc.start()
        with self.assertRaises(ReadyTimeout):
            proc.wait_for_line("never", 1)
        self.assertFalse(proc.alive())


if __name__ == "__main__":
    unittest.main()
