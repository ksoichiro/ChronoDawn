import json
import os
import subprocess
import sys
import tempfile
import unittest
from pathlib import Path

from scripts.hooks import claude_pr_gate as gate

SCRIPT = Path(gate.__file__)
BODY = "Summary\n\n## Verification\n\nMerge tier: B\n\nNo reproduced findings.\n"
REVIEW = "Commands run:\n- `./gradlew build1_21_1`: passes\n\nReproduced findings:\nNo reproduced findings\n"
JAVA = "common/shared/src/main/java/com/chronodawn/items/TimeCompassItem.java"


class Repo:
    """A throwaway repository with a main branch and a feature branch checked out."""

    def __init__(self, root):
        self.root = Path(root)
        self.git("init", "-q", "-b", "main")
        self.write("scripts/merge_tier/classify.py", "")
        self.write(JAVA, "class TimeCompassItem {\n    int angle() { return 0; }\n}\n")
        self.commit("base")
        self.git("checkout", "-q", "-b", "feature")

    def git(self, *args):
        return subprocess.run(
            ["git", "-c", "user.name=t", "-c", "user.email=t@example.com", "-c", "commit.gpgsign=false", *args],
            cwd=self.root, check=True, capture_output=True, text=True,
        ).stdout

    def write(self, rel, text):
        path = self.root / rel
        path.parent.mkdir(parents=True, exist_ok=True)
        path.write_text(text, encoding="utf-8")

    def commit(self, message):
        self.git("add", "-A")
        self.git("commit", "-q", "-m", message)

    def verify(self):
        """What the independent-verification skill leaves behind."""
        vdir = self.root / "build" / "verification"
        vdir.mkdir(parents=True, exist_ok=True)
        merge_base = self.git("merge-base", "main", "HEAD").strip()
        diff = subprocess.run(["git", "diff", merge_base, "HEAD"], cwd=self.root, capture_output=True).stdout
        (vdir / "diff.patch").write_bytes(diff)
        (vdir / "review.md").write_text(REVIEW, encoding="utf-8")

    def evaluate(self, command):
        return gate.evaluate(command, str(self.root))


class GateTest(unittest.TestCase):
    def setUp(self):
        self.tmp = tempfile.TemporaryDirectory()
        self.repo = Repo(self.tmp.name)

    def tearDown(self):
        self.tmp.cleanup()

    def change_behavior(self):
        self.repo.write(JAVA, "class TimeCompassItem {\n    int angle() { return 90; }\n}\n")
        self.repo.commit("fix: turn the needle")

    def add_scenario(self):
        self.repo.write("scripts/scenario_check/scenarios/time-compass.json", "{}\n")
        self.repo.commit("test: add a scenario")

    # Check 1: a behavior change needs a test or a scenario.

    def test_behavior_change_without_test_is_denied(self):
        self.change_behavior()
        self.repo.verify()
        message = self.repo.evaluate("git push -u origin feature")
        self.assertIn(JAVA, message)
        self.assertIn("Test-Exemption", message)

    def test_behavior_change_with_scenario_passes(self):
        self.change_behavior()
        self.add_scenario()
        self.repo.verify()
        self.assertIsNone(self.repo.evaluate("git push -u origin feature"))

    def test_behavior_change_with_unit_test_passes(self):
        self.change_behavior()
        self.repo.write("common/1.21.1/src/test/java/TimeCompassTest.java", "class TimeCompassTest {}\n")
        self.repo.commit("test: add a unit test")
        self.repo.verify()
        self.assertIsNone(self.repo.evaluate("git push"))

    def test_exemption_trailer_passes(self):
        self.repo.write(JAVA, "class TimeCompassItem {\n    int angle() { return 90; }\n}\n")
        self.repo.git("add", "-A")
        self.repo.git(
            "commit", "-q", "-m", "fix: turn the needle\n\n"
            "Test-Exemption: covered by the existing time-compass-needle scenario\n",
        )
        self.repo.verify()
        self.assertIsNone(self.repo.evaluate("git push"))

    def test_short_exemption_reason_is_not_enough(self):
        self.repo.write(JAVA, "class TimeCompassItem {\n    int angle() { return 90; }\n}\n")
        self.repo.git("add", "-A")
        self.repo.git("commit", "-q", "-m", "fix\n\nTest-Exemption: none\n")
        self.repo.verify()
        self.assertIn(JAVA, self.repo.evaluate("git push"))

    def test_comment_only_java_change_is_not_behavioral(self):
        self.repo.write(JAVA, "// Points at the target.\nclass TimeCompassItem {\n    int angle() { return 0; }\n}\n")
        self.repo.commit("docs: comment")
        self.repo.verify()
        self.assertIsNone(self.repo.evaluate("git push"))

    def test_lang_and_docs_are_not_behavioral(self):
        self.repo.write("common/shared/src/main/resources/assets/chronodawn/lang/en_us.json", "{}\n")
        self.repo.write("docs/player_guide.md", "text\n")
        self.repo.commit("docs: wording")
        self.repo.verify()
        self.assertIsNone(self.repo.evaluate("git push"))

    def test_client_asset_is_behavioral(self):
        self.repo.write("common/shared/src/main/resources/assets/chronodawn/items/time_compass.json", "{}\n")
        self.repo.commit("feat: item definition")
        self.repo.verify()
        self.assertIn("items/time_compass.json", self.repo.evaluate("git push"))

    def test_deleting_a_test_does_not_count_as_a_test(self):
        self.repo.write("common/1.21.1/src/test/java/OldTest.java", "class OldTest {}\n")
        self.repo.git("add", "-A")
        self.repo.git("commit", "-q", "-m", "test")
        self.repo.git("branch", "-f", "main", "HEAD")
        (self.repo.root / "common/1.21.1/src/test/java/OldTest.java").unlink()
        self.change_behavior()
        self.repo.verify()
        self.assertIn(JAVA, self.repo.evaluate("git push"))

    # Check 2: the independent verification ran on the current diff.

    def test_missing_verification_is_denied(self):
        self.change_behavior()
        self.add_scenario()
        self.assertIn("diff.patch is missing", self.repo.evaluate("git push"))

    def test_stale_diff_patch_is_denied(self):
        self.change_behavior()
        self.add_scenario()
        self.repo.verify()
        self.repo.write("docs/notes.md", "later\n")
        self.repo.commit("docs: later")
        self.assertIn("differs from the branch diff", self.repo.evaluate("git push"))

    def test_missing_review_is_denied(self):
        self.change_behavior()
        self.add_scenario()
        self.repo.verify()
        (self.repo.root / "build/verification/review.md").unlink()
        self.assertIn("report is missing", self.repo.evaluate("git push"))

    def test_review_older_than_patch_is_denied(self):
        self.change_behavior()
        self.add_scenario()
        self.repo.verify()
        review = self.repo.root / "build/verification/review.md"
        patch = self.repo.root / "build/verification/diff.patch"
        os.utime(review, (patch.stat().st_mtime - 60, patch.stat().st_mtime - 60))
        self.assertIn("older than diff.patch", self.repo.evaluate("git push"))

    def test_review_without_commands_is_denied(self):
        self.change_behavior()
        self.add_scenario()
        self.repo.verify()
        (self.repo.root / "build/verification/review.md").write_text("Looks good.\n", encoding="utf-8")
        self.assertIn("Commands run:", self.repo.evaluate("git push"))

    def test_docs_only_change_still_needs_verification(self):
        self.repo.write("docs/notes.md", "text\n")
        self.repo.commit("docs: notes")
        self.assertIn("diff.patch is missing", self.repo.evaluate("git push"))

    # Check 3: gh pr create needs the Verification section.

    def test_pr_create_with_inline_body_passes(self):
        self.change_behavior()
        self.add_scenario()
        self.repo.verify()
        command = f"gh pr create --title 'fix: needle' --body \"$(cat <<'EOF'\n{BODY}EOF\n)\""
        self.assertIsNone(self.repo.evaluate(command))

    def test_pr_create_with_body_file_passes(self):
        self.change_behavior()
        self.add_scenario()
        self.repo.verify()
        self.repo.write("build/pr-body.md", BODY)
        self.assertIsNone(self.repo.evaluate("gh pr create --title t --body-file build/pr-body.md"))

    def test_pr_create_without_verification_section_is_denied(self):
        self.change_behavior()
        self.add_scenario()
        self.repo.verify()
        message = self.repo.evaluate("gh pr create --title t --body 'Not verified in game.'")
        self.assertIn("`## Verification` section", message)
        self.assertIn("`Merge tier:` line", message)

    def test_pr_create_with_fill_is_denied(self):
        self.change_behavior()
        self.add_scenario()
        self.repo.verify()
        self.assertIn("Verification", self.repo.evaluate("gh pr create --fill"))

    def test_pr_create_uses_the_given_base(self):
        self.repo.git("checkout", "-q", "-b", "stack-base")
        self.change_behavior()
        self.repo.git("checkout", "-q", "-b", "stacked")
        self.repo.write("docs/notes.md", "text\n")
        self.repo.commit("docs: notes")
        vdir = self.repo.root / "build/verification"
        vdir.mkdir(parents=True)
        diff = subprocess.run(["git", "diff", "stack-base", "HEAD"], cwd=self.repo.root, capture_output=True).stdout
        (vdir / "diff.patch").write_bytes(diff)
        (vdir / "review.md").write_text(REVIEW, encoding="utf-8")
        self.repo.write("build/pr-body.md", BODY)
        self.assertIsNone(
            self.repo.evaluate("gh pr create --base stack-base --title t --body-file build/pr-body.md"))

    # Commands the gate leaves alone.

    def test_unrelated_commands_pass(self):
        self.change_behavior()
        for command in ("git status", "git log --oneline -3", "gh pr view 12", "git commit -m 'git push later'"):
            self.assertIsNone(self.repo.evaluate(command), command)

    def test_push_of_main_passes(self):
        self.change_behavior()
        self.repo.git("checkout", "-q", "main")
        self.repo.git("merge", "-q", "--ff-only", "feature")
        self.assertIsNone(self.repo.evaluate("git push origin main"))
        self.assertIsNone(self.repo.evaluate("git push"))

    def test_tag_delete_and_dry_run_pushes_pass(self):
        self.change_behavior()
        for command in ("git push origin v1.0.0 --tags", "git push origin --delete old",
                        "git push origin refs/tags/v1.0.0", "git push --dry-run"):
            self.assertIsNone(self.repo.evaluate(command), command)

    def test_branch_without_commits_passes(self):
        self.assertIsNone(self.repo.evaluate("git push -u origin feature"))

    def test_other_repository_passes(self):
        self.change_behavior()
        other = Path(self.tmp.name) / "gradle" / "shared"
        other.mkdir(parents=True)
        subprocess.run(["git", "init", "-q"], cwd=other, check=True)
        self.assertIsNone(self.repo.evaluate("git -C gradle/shared push"))

    def test_chained_push_is_found(self):
        self.change_behavior()
        self.assertIsNotNone(self.repo.evaluate("./gradlew validateAll && git push -u origin feature"))

    def test_redirects_and_push_options_do_not_hide_a_push(self):
        self.change_behavior()
        for command in ("git push -u origin feature 2>&1", "git push -u origin feature 2>&1 | tail -5",
                        "git push origin HEAD > /dev/null", "git push -o ci.skip origin feature",
                        "git push --push-option ci.skip origin feature", "(cd . && git push)",
                        "bash -c 'git push -u origin feature'"):
            self.assertIsNotNone(self.repo.evaluate(command), command)

    def test_title_with_shell_characters_keeps_the_body_file(self):
        self.change_behavior()
        self.add_scenario()
        self.repo.verify()
        self.repo.write("build/pr-body.md", BODY)
        for title in ("feat(hooks): gate", "fix: a; b", "fix: a & b", "fix: a > b"):
            command = f'gh pr create --title "{title}" --body-file build/pr-body.md 2>&1'
            self.assertIsNone(self.repo.evaluate(command), title)

    def test_heredoc_bodies_are_not_commands(self):
        self.change_behavior()
        command = "cat > build/notes.md <<'EOF'\ngh pr create --fill\ngit push\nEOF\necho done"
        self.assertIsNone(self.repo.evaluate(command))

    def test_inline_body_may_quote_the_skip_variable(self):
        self.change_behavior()
        self.add_scenario()
        self.repo.verify()
        body = BODY + 'Prefix with CHRONODAWN_PR_GATE_SKIP="<reason>" to skip.\n'
        command = f"gh pr create --title 'feat(hooks): gate' --body \"$(cat <<'EOF'\n{body}EOF\n)\""
        self.assertIsNone(self.repo.evaluate(command))

    def test_skip_applies_only_to_its_own_command(self):
        self.change_behavior()
        command = "echo CHRONODAWN_PR_GATE_SKIP=abcdefghijkl; git push -u origin feature"
        self.assertIsNotNone(self.repo.evaluate(command))
        command = "CHRONODAWN_PR_GATE_SKIP='backup of work in progress' git status && git push"
        self.assertIsNotNone(self.repo.evaluate(command))

    def test_skip_with_reason_passes(self):
        self.change_behavior()
        self.assertIsNone(self.repo.evaluate('CHRONODAWN_PR_GATE_SKIP="backup of work in progress" git push'))

    def test_skip_without_reason_is_denied(self):
        self.change_behavior()
        self.assertIn("at least 10 characters", self.repo.evaluate("CHRONODAWN_PR_GATE_SKIP=x git push"))

    # The hook protocol.

    def test_hook_prints_a_deny_decision(self):
        self.change_behavior()
        payload = {"tool_name": "Bash", "tool_input": {"command": "git push"}, "cwd": str(self.repo.root)}
        proc = subprocess.run([sys.executable, str(SCRIPT)], input=json.dumps(payload),
                              capture_output=True, text=True, check=True)
        output = json.loads(proc.stdout)["hookSpecificOutput"]
        self.assertEqual(output["hookEventName"], "PreToolUse")
        self.assertEqual(output["permissionDecision"], "deny")
        self.assertIn(JAVA, output["permissionDecisionReason"])

    def test_hook_is_silent_when_the_gate_passes(self):
        payload = {"tool_name": "Bash", "tool_input": {"command": "git status"}, "cwd": str(self.repo.root)}
        proc = subprocess.run([sys.executable, str(SCRIPT)], input=json.dumps(payload),
                              capture_output=True, text=True, check=True)
        self.assertEqual(proc.stdout, "")

    def test_hook_fails_open_without_a_base_branch(self):
        self.repo.git("branch", "-D", "main")
        self.change_behavior()
        payload = {"tool_name": "Bash", "tool_input": {"command": "git push"}, "cwd": str(self.repo.root)}
        proc = subprocess.run([sys.executable, str(SCRIPT)], input=json.dumps(payload),
                              capture_output=True, text=True, check=True)
        self.assertEqual(proc.stdout, "")
        self.assertIn("skipped after an error", proc.stderr)


if __name__ == "__main__":
    unittest.main()
