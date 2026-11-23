import tempfile
import unittest
from pathlib import Path

from scripts.upgrade_test import cleanup


class CleanupTest(unittest.TestCase):
    def test_scenario_removes_instances_but_preserves_reports(self):
        with tempfile.TemporaryDirectory() as tmp:
            root = Path(tmp)
            for name in ('server', 'client', 'agent'):
                (root / name).mkdir()
            (root / 'report.txt').write_text('report')
            cleanup.scenario(root)
            for name in ('server', 'client', 'agent'):
                self.assertFalse((root / name).exists())
            self.assertTrue((root / 'report.txt').exists())

    def test_upgrade_removes_instances_for_each_source_only(self):
        with tempfile.TemporaryDirectory() as tmp:
            root = Path(tmp)
            for name in ('server', 'old-client', 'new-client', 'before-world'):
                (root / '1.0' / name).mkdir(parents=True)
            (root / '1.0' / 'old-server.log').write_text('log')
            cleanup.upgrade(root, ['1.0'])
            for name in ('server', 'old-client', 'new-client', 'before-world'):
                self.assertFalse((root / '1.0' / name).exists())
            self.assertTrue((root / '1.0' / 'old-server.log').exists())


class DriverCleanupTest(unittest.TestCase):
    def test_upgrade_cleans_only_after_overall_success_and_without_keep(self):
        import json
        from unittest import mock
        from scripts.upgrade_test import run

        for result, keep in [('PASS', False), ('FAIL', False), ('PASS', True)]:
            with self.subTest(result=result, keep=keep), tempfile.TemporaryDirectory() as tmp:
                root = Path(tmp)
                out = root / 'out'
                instance = out / '1.0' / 'server'
                instance.mkdir(parents=True)
                env = {'loader': 'fabric', 'mcVersion': '1.21.1', 'repoRoot': tmp, 'cacheDir': tmp,
                       'outDir': str(out), 'baseline': '1.0', 'candidateJar': str(root / 'candidate.jar'),
                       'prodMods': [], 'ports': [1, 2], 'logIgnoreFile': str(root / 'ignore'),
                       'serverTemplateDir': tmp, 'serverCommand': [],
                       'client': {'java': 'java', 'jvmArgs': [], 'classpath': [], 'mainClass': 'M', 'args': []}}
                env_file = root / 'env.json'
                env_file.write_text(json.dumps(env))
                args = ['--env', str(env_file), '--sources', '1.0'] + (['--keep-test-output'] if keep else [])
                with mock.patch.object(run, 'run_source', return_value=[(result, 'check', '')]), \
                     mock.patch.object(run.processes, 'install_signal_handlers'), \
                     mock.patch.object(run.processes, 'acquire_run_slot'), \
                     mock.patch.object(run.processes, 'resolve_ports', return_value=(1, 2)):
                    self.assertEqual(run.main(args), 0 if result == 'PASS' else 1)
                self.assertEqual(instance.exists(), result == 'FAIL' or keep)
                self.assertTrue((out / 'report.txt').exists())

    def test_scenario_cleans_only_after_success_and_without_keep(self):
        import json
        from unittest import mock
        from scripts.scenario_check import run

        for result, keep in [('PASS', False), ('FAIL', False), ('PASS', True)]:
            with self.subTest(result=result, keep=keep), tempfile.TemporaryDirectory() as tmp:
                root = Path(tmp)
                out = root / 'out'
                instance = out / 'server'
                instance.mkdir(parents=True)
                env = {'loader': 'fabric', 'mcVersion': '1.21.1', 'repoRoot': tmp, 'cacheDir': tmp,
                       'outDir': str(out), 'baseline': '1.0', 'candidateJar': str(root / 'candidate.jar'),
                       'prodMods': [], 'ports': [1, 2], 'logIgnoreFile': str(root / 'ignore'),
                       'serverTemplateDir': tmp, 'serverCommand': [],
                       'client': {'java': 'java', 'jvmArgs': [], 'classpath': [], 'mainClass': 'M', 'args': []}}
                env_file = root / 'env.json'
                env_file.write_text(json.dumps(env))
                args = ['--env', str(env_file), '--out-dir', str(out)] + (['--keep-test-output'] if keep else [])
                with mock.patch.object(run, 'run', return_value=([(result, 'check', '')], {}, None, {})), \
                     mock.patch.object(run, 'load_scenarios', return_value=[]), \
                     mock.patch.object(run.processes, 'install_signal_handlers'), \
                     mock.patch.object(run.processes, 'acquire_run_slot'), \
                     mock.patch.object(run.processes, 'resolve_ports', return_value=(1, 2)):
                    self.assertEqual(run.main(args), 0 if result == 'PASS' else 1)
                self.assertEqual(instance.exists(), result == 'FAIL' or keep)
                self.assertTrue((out / 'report.txt').exists())
