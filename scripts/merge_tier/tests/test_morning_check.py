import datetime
import io
import json
import unittest
from contextlib import redirect_stdout
from unittest import mock

from scripts.merge_tier import morning_check as m

VERIFIED = """Summary of the change.

## Verification

Merge tier: A

Independent reviewer (fresh subagent, same model, diff + task + tier only):
- `python3 -m unittest`: passes

Reproduced findings: none.

🤖 Generated with [Claude Code](https://claude.com/claude-code)
"""


def pr(number=1, body=VERIFIED):
    return {"number": number, "title": f"PR {number}", "url": f"https://example.invalid/pull/{number}",
            "body": body, "baseRefOid": "base", "headRefOid": "head", "mergedAt": "2026-11-23T00:00:00Z"}


class VerificationSectionTest(unittest.TestCase):
    def test_filled_section_passes(self):
        self.assertIsNone(m.verification_problem(VERIFIED))

    def test_missing_section(self):
        self.assertEqual(m.verification_problem("Merge tier: A\n"), "The body has no Verification section.")

    def test_section_with_only_the_tier_line_is_empty(self):
        body = "## Verification\n\nMerge tier: A\n\n## Notes\n\ntext\n"
        self.assertEqual(m.verification_problem(body), "The Verification section is empty.")

    def test_section_ends_at_same_level_heading(self):
        self.assertEqual(m.verification_section("## Verification\nran x\n## Next\ny\n"), ["ran x"])

    def test_subheadings_stay_in_section(self):
        self.assertEqual(m.verification_section("## Verification\n### Commands\nran x\n# Top\n"),
                         ["### Commands", "ran x"])

    def test_heading_level_and_case_do_not_matter(self):
        self.assertIsNotNone(m.verification_section("### verification\nran x\n"))

    def test_word_inside_a_line_is_not_a_heading(self):
        self.assertIsNone(m.verification_section("Verification was skipped.\n"))


class AssessTest(unittest.TestCase):
    def test_tier_a_with_verification_is_ok(self):
        self.assertEqual(m.assess(pr(), "A").problems, [])

    def test_higher_tier_is_reported(self):
        problems = m.assess(pr(), "C").problems
        self.assertEqual(problems, ["Declared tier A, but the diff of its final head is tier C."])

    def test_both_problems_are_reported(self):
        self.assertEqual(len(m.assess(pr(body="Merge tier: A"), "B").problems), 2)

    def test_declares_tier_a(self):
        self.assertTrue(m.declares_tier_a("**Merge tier:** A"))
        self.assertFalse(m.declares_tier_a("Merge tier: B"))
        self.assertFalse(m.declares_tier_a("Explains when to write Merge tier: A in a body."))


class MainTest(unittest.TestCase):
    def run_main(self, prs, tiers, argv=()):
        calls = []

        def fake_run(cmd, cwd):
            calls.append(cmd)
            if cmd[:2] == ["gh", "pr"]:
                return json.dumps(prs)
            return ""

        out = io.StringIO()
        with mock.patch.object(m, "run", fake_run), \
                mock.patch.object(m, "classify_pr", side_effect=lambda p: tiers[p["number"]]), \
                redirect_stdout(out):
            code = m.main(list(argv), today=datetime.date(2026, 11, 24))
        return code, out.getvalue(), calls

    def test_defaults_to_yesterday(self):
        code, out, calls = self.run_main([], {})
        self.assertEqual(code, 0)
        self.assertIn("merged:>=2026-11-23", " ".join(calls[0]))
        self.assertIn("No pull request merged", out)

    def test_misclassified_pr_fails(self):
        code, out, calls = self.run_main([pr(1), pr(2)], {1: "A", 2: "B"}, ["--since", "2026-11-20"])
        self.assertEqual(code, 1)
        self.assertIn("1 of 2 need attention.", out)
        self.assertIn(["git", "fetch", "--quiet", "origin", "pull/2/head", "base"], calls)

    def test_search_hits_without_a_declaration_are_skipped(self):
        code, out, _ = self.run_main([pr(1, body="Mentions Merge tier: A in prose.")], {})
        self.assertEqual(code, 0)
        self.assertIn("No pull request merged", out)

    def test_bad_date_is_a_usage_error(self):
        with redirect_stdout(io.StringIO()), mock.patch("sys.stderr", io.StringIO()):
            self.assertEqual(m.main(["--since", "yesterday"]), 2)

    def test_gh_error_exits_2(self):
        def failing_run(cmd, cwd):
            raise RuntimeError("gh failed")

        with mock.patch.object(m, "run", failing_run), mock.patch("sys.stderr", io.StringIO()):
            self.assertEqual(m.main([]), 2)


if __name__ == "__main__":
    unittest.main()
