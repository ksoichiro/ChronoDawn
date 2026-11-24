import io
import os
import subprocess
import tempfile
import unittest
from contextlib import redirect_stdout
from pathlib import Path
from unittest import mock

from scripts.merge_tier import docs_only as d


class PatternTest(unittest.TestCase):
    def setUp(self):
        self.patterns = d.docs_patterns()

    def test_patterns_are_the_tier_a_docs_rule(self):
        self.assertEqual(self.patterns, ["docs/**", "**/*.md", "LICENSE", ".gitignore"])

    def test_docs_only(self):
        paths = ["docs/merge_policy.md", "CHANGELOG.md", "AGENTS.md", ".claude/skills/x/SKILL.md",
                 "docs/images/a.png", "LICENSE", ".gitignore"]
        self.assertTrue(d.is_docs_only(paths, self.patterns))

    def test_one_other_file_runs_everything(self):
        for other in ["common/shared/src/main/java/A.java", "gradle.properties", ".github/workflows/ci.yml",
                      "scripts/merge_tier/rules.json", "common/1.21.1/src/main/resources/data/x/y.json",
                      "gradle/shared"]:
            with self.subTest(other=other):
                self.assertFalse(d.is_docs_only(["README.md", other], self.patterns))
                self.assertEqual(d.non_docs_paths(["README.md", other], self.patterns), [other])

    def test_nested_license_is_not_docs(self):
        self.assertFalse(d.is_docs_only(["sub/LICENSE"], self.patterns))

    def test_empty_diff_is_not_docs_only(self):
        self.assertFalse(d.is_docs_only([], self.patterns))


class CliTest(unittest.TestCase):
    """Runs the CLI against a throwaway repository."""

    def setUp(self):
        self.tmp = tempfile.TemporaryDirectory()
        self.repo = Path(self.tmp.name)
        self.env = {**os.environ, "GIT_CONFIG_GLOBAL": os.devnull, "GIT_CONFIG_NOSYSTEM": "1"}
        self.git("init", "-q", "-b", "main")
        self.git("config", "user.email", "test@example.com")
        self.git("config", "user.name", "test")
        self.git("config", "commit.gpgsign", "false")
        self.write("README.md", "hi\n")
        self.write("src/A.java", "class A {}\n")
        self.commit()
        self.git("checkout", "-q", "-b", "topic")

    def tearDown(self):
        self.tmp.cleanup()

    def git(self, *args):
        subprocess.run(["git", *args], cwd=self.repo, env=self.env, check=True, capture_output=True)

    def write(self, path, text):
        f = self.repo / path
        f.parent.mkdir(parents=True, exist_ok=True)
        f.write_text(text)

    def commit(self):
        self.git("add", "-A")
        self.git("commit", "-q", "--allow-empty", "-m", "change")

    def cli(self):
        output = self.repo / "github_output"
        summary = self.repo / "summary.md"
        out = io.StringIO()
        with mock.patch.dict(os.environ, self.env), redirect_stdout(out):
            code = d.main(["--repo", str(self.repo), "--base", "main", "--head", "topic",
                           "--output", str(output), "--summary", str(summary)])
        return code, out.getvalue().strip(), output.read_text().strip(), summary.read_text()

    def test_docs_change(self):
        self.write("README.md", "hello\n")
        self.write("docs/a.md", "new\n")
        self.commit()
        code, printed, output, summary = self.cli()
        self.assertEqual((code, printed, output), (0, "docs_only=true", "docs_only=true"))
        self.assertIn("skipped", summary)

    def test_code_change(self):
        self.write("README.md", "hello\n")
        self.write("src/A.java", "class A { int x; }\n")
        self.commit()
        code, printed, output, summary = self.cli()
        self.assertEqual((code, printed, output), (0, "docs_only=false", "docs_only=false"))
        self.assertIn("`src/A.java`", summary)

    def test_deleted_code_file_is_not_docs(self):
        (self.repo / "src/A.java").unlink()
        self.commit()
        self.assertEqual(self.cli()[1], "docs_only=false")

    def test_empty_diff(self):
        self.commit()
        self.assertEqual(self.cli()[1], "docs_only=false")

    def test_unknown_revision_is_an_error(self):
        with mock.patch.dict(os.environ, self.env), redirect_stdout(io.StringIO()), \
                mock.patch("sys.stderr", io.StringIO()):
            code = d.main(["--repo", str(self.repo), "--base", "nope", "--head", "topic"])
        self.assertEqual(code, 2)


if __name__ == "__main__":
    unittest.main()
