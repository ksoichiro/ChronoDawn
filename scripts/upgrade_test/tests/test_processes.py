import os
import socket
import sys
import tempfile
import time
import unittest
from pathlib import Path
from unittest import mock

from scripts.upgrade_test.processes import ManagedProcess, PortInUse, ProcessExited, ReadyTimeout, preflight_ports
from scripts.upgrade_test import processes


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

    def _isolated_locks(self):
        patcher = mock.patch.dict(os.environ, {processes.LOCK_DIR_ENV: str(self.dir / "locks")})
        patcher.start()
        self.addCleanup(patcher.stop)
        held = list(processes._held_locks)
        self.addCleanup(lambda: processes._held_locks.__setitem__(slice(None), held))

    def test_allocate_ports_gives_distinct_pairs_to_concurrent_runs(self):
        self._isolated_locks()
        first = processes.allocate_ports(range(26100, 26110))
        second = processes.allocate_ports(range(26100, 26110))
        self.assertEqual(first, (26100, 26101))
        self.assertEqual(second, (26102, 26103))

    def test_allocate_ports_skips_ports_in_use(self):
        self._isolated_locks()
        sock = socket.socket()
        sock.bind(("0.0.0.0", 0))
        busy = sock.getsockname()[1]
        try:
            server, rcon = processes.allocate_ports(range(busy, busy + 6))
            self.assertNotIn(busy, (server, rcon))
        finally:
            sock.close()

    def test_allocate_ports_fails_when_range_is_exhausted(self):
        self._isolated_locks()
        processes.allocate_ports(range(26200, 26202))
        with self.assertRaises(RuntimeError):
            processes.allocate_ports(range(26200, 26202))

    def test_resolve_ports_prefers_explicit_ports(self):
        self._isolated_locks()
        self.assertEqual(processes.resolve_ports((1, 2)), (1, 2))

    def test_max_parallel_reads_environment(self):
        with mock.patch.dict(os.environ, {processes.MAX_PARALLEL_ENV: "3"}):
            self.assertEqual(processes.max_parallel(), 3)
        with mock.patch.dict(os.environ, {}, clear=False):
            os.environ.pop(processes.MAX_PARALLEL_ENV, None)
            self.assertEqual(processes.max_parallel(), 2)
        with mock.patch.dict(os.environ, {processes.MAX_PARALLEL_ENV: "0"}):
            with self.assertRaises(ValueError):
                processes.max_parallel()

    def test_run_slot_waits_until_one_is_free(self):
        self._isolated_locks()
        with mock.patch.dict(os.environ, {processes.MAX_PARALLEL_ENV: "1"}):
            processes.acquire_run_slot()
            slept = []

            def fake_sleep(seconds):
                slept.append(seconds)
                processes._held_locks.pop().close()  # the other run finishes

            with mock.patch.object(processes.time, "sleep", fake_sleep):
                processes.acquire_run_slot(poll_seconds=0.01)
            self.assertEqual(slept, [0.01])

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
