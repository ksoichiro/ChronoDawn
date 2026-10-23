import subprocess
import tempfile
import unittest
from pathlib import Path

from scripts.upgrade_test.versions import (
    VersionSelectionError,
    resolve_previous_release,
    resolve_sources,
    select_modrinth_file,
)


def _git(repo, *args):
    subprocess.run(["git", "-C", str(repo), *args], check=True, capture_output=True)


def _commit(repo, name):
    (repo / name).write_text(name + "\n")
    _git(repo, "add", name)
    _git(repo, "-c", "user.name=t", "-c", "user.email=t@example.com", "-c", "commit.gpgsign=false",
         "commit", "-q", "-m", name)


class PreviousReleaseTest(unittest.TestCase):
    def setUp(self):
        self.tmp = tempfile.TemporaryDirectory()
        self.repo = Path(self.tmp.name)
        _git(self.repo, "init", "-q")
        _commit(self.repo, "a")
        _git(self.repo, "tag", "v0.13.0")
        _commit(self.repo, "b")
        _git(self.repo, "tag", "v0.14.0")

    def tearDown(self):
        self.tmp.cleanup()

    def test_resolve_previous_release_head_untagged(self):
        _commit(self.repo, "c")
        self.assertEqual(resolve_previous_release(self.repo), "0.14.0")

    def test_resolve_previous_release_head_is_tag(self):
        self.assertEqual(resolve_previous_release(self.repo), "0.13.0")


class SourcesTest(unittest.TestCase):
    def test_resolve_sources_dedupes(self):
        self.assertEqual(resolve_sources("0.8.0", "0.8.0"), ["0.8.0"])

    def test_resolve_sources_baseline_first(self):
        self.assertEqual(resolve_sources("0.8.0", "0.14.0"), ["0.8.0", "0.14.0"])


def _version(number, loaders, filename):
    return {
        "version_number": number,
        "loaders": loaders,
        "game_versions": ["1.21.1"],
        "files": [
            {"url": "https://cdn.modrinth.com/" + filename, "filename": filename, "primary": True,
             "hashes": {"sha512": "00"}},
        ],
    }


class SelectModrinthFileTest(unittest.TestCase):
    def setUp(self):
        self.versions = [
            _version("0.8.0", ["neoforge"], "chronodawn-0.8.0+1.21.1-neoforge.jar"),
            _version("0.8.0-beta", ["neoforge"], "chronodawn-0.8.0-beta+1.21.1-neoforge.jar"),
            _version("0.13.0", ["neoforge"], "chronodawn-0.13.0+1.21.1-neoforge.jar"),
            _version("0.8.0", ["fabric"], "chronodawn-0.8.0+1.21.1-fabric.jar"),
        ]

    def test_select_version_exact(self):
        f = select_modrinth_file(self.versions, "0.8.0", "neoforge")
        self.assertEqual(f["filename"], "chronodawn-0.8.0+1.21.1-neoforge.jar")

    def test_select_version_prefix_when_no_exact(self):
        versions = [_version("0.9.0+1.21.1-fabric", ["fabric"], "c-0.9.0.jar")]
        self.assertEqual(select_modrinth_file(versions, "0.9.0", "fabric")["filename"], "c-0.9.0.jar")

    def test_select_version_none_raises(self):
        with self.assertRaises(VersionSelectionError) as ctx:
            select_modrinth_file(self.versions, "0.7.0", "neoforge")
        self.assertIn("0.13.0", str(ctx.exception))

    def test_select_version_ambiguous_raises(self):
        versions = self.versions + [_version("0.8.0", ["neoforge"], "dup.jar")]
        with self.assertRaises(VersionSelectionError) as ctx:
            select_modrinth_file(versions, "0.8.0", "neoforge")
        self.assertIn("0.8.0", str(ctx.exception))


if __name__ == "__main__":
    unittest.main()
