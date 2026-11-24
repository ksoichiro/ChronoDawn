import unittest

from scripts.merge_tier.classify import Change, check_verification
from scripts.hooks.claude_pr_gate import check_pr_body


class VerificationTest(unittest.TestCase):
    def test_behavior_requires_test_or_reason(self):
        changes = [Change('M', 'common/shared/src/main/java/example/Item.java')]
        self.assertTrue(check_verification(changes, ''))
        self.assertFalse(check_verification(changes, 'Test-Exemption: Existing compass scenario covers this change.'))
        self.assertTrue(check_verification(changes, 'Test-Exemption: none'))
        self.assertFalse(check_verification(changes + [Change('A', 'common/gametest/example/Test.java')], ''))
        self.assertTrue(check_verification(changes + [Change('D', 'common/gametest/example/Test.java')], ''))
        self.assertFalse(check_verification(changes + [Change('M', 'scripts/scenario_check/scenarios/test.json')], ''))

    def test_hook_applies_body_check(self):
        body = "## Verification\nMerge tier: D\n"
        self.assertIsNone(check_pr_body(body))
        self.assertIn("Manual checks", check_pr_body(body + "Not verified in game"))
        self.assertIsNone(check_pr_body(body + "Manual checks: Not verified in game because special hardware is required."))

    def test_assets_data_and_non_behavior(self):
        for path in ('common/shared/src/main/resources/assets/mod/model.json',
                     'common/shared/src/main/resources/data/mod/recipe/a.json'):
            self.assertTrue(check_verification([Change('M', path)], ''), path)
        for path in ('common/shared/src/main/resources/assets/mod/lang/en_us.json',
                     'common/gametest/example/Test.java', 'build.gradle'):
            self.assertFalse(check_verification([Change('M', path)], ''), path)

    def test_unverified_requires_reason_on_manual_checks_line(self):
        for body in ('Not verified in game', '実機未確認', 'Manual checks: not verified in game',
                     'Manual checks: scenario cannot render this effect\n実機未確認'):
            self.assertTrue(check_verification([], body), body)
        self.assertFalse(check_verification([], 'Manual checks: not verified in game because this requires inaccessible hardware.'))
        self.assertFalse(check_verification([], 'Manual checks: 実機未確認。専用の外部ハードウェアが必要なため確認できませんでした。'))


if __name__ == '__main__':
    unittest.main()
