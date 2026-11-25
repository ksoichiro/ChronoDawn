import json
import os
import subprocess
import tempfile
import unittest
from pathlib import Path
from unittest import mock

from scripts.merge_tier import classify as c


def tier_of(path, status="M", base="", head=""):
    rules, default = c.load_rules()
    results = c.classify([c.Change(status, path)], rules, default, lambda p: base, lambda p: head)
    return results[0].tier


class GlobTest(unittest.TestCase):
    def test_double_star_spans_directories(self):
        r = c.glob_to_regex("**/data/*/worldgen/**")
        self.assertTrue(r.match("common/1.21.1/src/main/resources/data/chronodawn/worldgen/biome/x.json"))
        self.assertTrue(r.match("data/chronodawn/worldgen/x.json"))
        self.assertFalse(r.match("common/data/a/b/worldgen/x.json"))

    def test_single_star_stays_in_segment(self):
        r = c.glob_to_regex("scripts/*_rules.json")
        self.assertTrue(r.match("scripts/era_symbol_rules.json"))
        self.assertFalse(r.match("scripts/sub/era_symbol_rules.json"))

    def test_exact_path(self):
        r = c.glob_to_regex("gradle/shared")
        self.assertTrue(r.match("gradle/shared"))
        self.assertFalse(r.match("gradle/shared-old"))


class RuleTest(unittest.TestCase):
    """Representative paths for each tier in docs/merge_policy.md."""

    CASES = {
        "A": [
            "docs/player_guide.md",
            "CHANGELOG.md",
            "README.md",
            "common/shared/src/test/java/com/chronodawn/unit/FooTest.java",
            "common/1.21.1/src/main/java/com/chronodawn/gametest/BossTests.java",
            "fabric/1.21.1/src/gametest/java/com/chronodawn/gametest/X.java",
            "scripts/scenario_check/scenarios/master_clock.json",
            "scripts/upgrade_test/layers/v0_13_0.json",
            "scripts/upgrade_test/tests/test_compare.py",
            "scripts/weather_structures.py",
        ],
        "B": [
            "common/1.21.1/src/main/resources/data/chronodawn/recipe/clockwork_gear.json",
            "common/1.20.1/src/main/resources/data/chronodawn/loot_tables/blocks/x.json",
            "common/1.21.1/src/main/resources/data/minecraft/tags/item/planks.json",
            "common/shared/src/main/resources/assets/chronodawn/textures/block/x.png",
            "common/1.21.1/src/main/java/com/chronodawn/entities/bosses/TimeTyrant.java",
            "assets/music/theme.ogg",
        ],
        "C": [
            "common/shared/src/main/resources/data/chronodawn/worldgen/structure/outpost.json",
            "common/1.21.1/src/main/resources/data/chronodawn/structure/outpost.nbt",
            "common/1.21.1/src/main/resources/data/chronodawn/dimension/chronodawn.json",
            "common/1.21.1/src/main/resources/data/chronodawn/tags/worldgen/biome/has_outpost.json",
            "common/shared/src/main/resources/data/chronodawn/neoforge/biome_modifier/ores.json",
            "common/1.21.1/src/main/resources/data/chronodawn/advancement/story/root.json",
            "common/1.21.1/src/main/resources/data/minecraft/tags/block/needs_iron_tool.json",
            "common/1.21.1/src/main/java/com/chronodawn/registry/ModBlocks.java",
            "common/1.21.1/src/main/java/com/chronodawn/config/ChronoDawnConfig.java",
            "common/1.21.1/src/main/java/com/chronodawn/data/ChronoDawnWorldData.java",
            "common/1.21.1/src/main/java/com/chronodawn/mixin/FooMixin.java",
            "common/1.21.1/src/main/java/com/chronodawn/worldgen/processors/BossRoomProtectionProcessor.java",
            "common/1.21.1/src/main/resources/chronodawn.mixins.json",
            "fabric/1.21.1/src/main/resources/fabric.mod.json",
            "neoforge/1.21.1/src/main/resources/META-INF/neoforge.mods.toml",
            "common/1.21.1/src/main/resources/pack.mcmeta",
            "props/1.21.1.properties",
            "common/1.21.1/build.gradle",
            "something/unknown.bin",
        ],
        "D": [
            "CLAUDE.md",
            "AGENTS.md",
            ".claude/skills/scenario-check/SKILL.md",
            ".claude/settings.json",
            ".agents/skills",
            ".agents/skills/example/scripts/check.py",
            ".agents/skills/example/references/guide.md",
            ".github/workflows/ci.yml",
            "scripts/hooks/pre-commit",
            "scripts/merge_tier/rules.json",
            "docs/merge_policy.md",
            "gradle/shared",
            "gradle/chronodawn-validation.gradle",
            "gradle/version-sync.lock",
            "gradle.properties",
            "scripts/era_symbol_rules.json",
            "prod-smoke-log-ignore.txt",
            "scripts/upgrade_test/run.py",
            "scripts/scenario_check/run.py",
        ],
    }

    def test_representative_paths(self):
        for tier, paths in self.CASES.items():
            for path in paths:
                with self.subTest(path=path):
                    self.assertEqual(tier_of(path), tier)


class DeletionAndSelfTest(unittest.TestCase):
    def test_deleted_test_is_d(self):
        self.assertEqual(tier_of("common/gametest/java/com/chronodawn/gametest/BossTests.java", "D"), "D")
        self.assertEqual(tier_of("scripts/scenario_check/scenarios/master_clock.json", "D"), "D")
        self.assertEqual(tier_of("common/gametest/java/com/chronodawn/gametest/BossTests.java", "A"), "A")

    def test_rules_cannot_lower_classifier_files(self):
        rules = [c.Rule("A", "everything", ["**"])]
        default = c.Rule("C", "default", [])
        results = c.classify([c.Change("M", "scripts/merge_tier/rules.json")], rules, default)
        self.assertEqual(results[0].tier, "D")


class LangTest(unittest.TestCase):
    PATH = "common/1.21.1/src/main/resources/assets/chronodawn/lang/en_us.json"

    def test_value_change_is_a(self):
        self.assertEqual(tier_of(self.PATH, "M", '{"a": "x", "b": "y"}', '{"a": "z", "b": "y"}'), "A")

    def test_added_key_is_a(self):
        self.assertEqual(tier_of(self.PATH, "M", '{"a": "x"}', '{"a": "x", "b": "y"}'), "A")

    def test_removed_or_renamed_key_is_c(self):
        self.assertEqual(tier_of(self.PATH, "M", '{"a": "x", "b": "y"}', '{"a": "x", "c": "y"}'), "C")

    def test_new_and_deleted_files(self):
        self.assertEqual(tier_of(self.PATH, "A", "", '{"a": "x"}'), "A")
        self.assertEqual(tier_of(self.PATH, "D", '{"a": "x"}', ""), "C")

    def test_broken_json_is_c(self):
        self.assertEqual(tier_of(self.PATH, "M", '{"a": "x"}', '{"a": '), "C")


class DeclarationTest(unittest.TestCase):
    def test_parses_plain_and_bold(self):
        self.assertEqual(c.declared_tiers("Merge tier: A"), ["A"])
        self.assertEqual(c.declared_tiers("intro\n**Merge tier:** b\n"), ["B"])
        self.assertEqual(c.declared_tiers("- Merge tier: `C`"), ["C"])
        self.assertEqual(c.declared_tiers("The merge tier is A"), [])

    def test_lower_than_computed_fails(self):
        ok, msg = c.check_declaration("Merge tier: A", "C")
        self.assertFalse(ok)
        self.assertIn("tier C", msg)

    def test_equal_or_higher_passes(self):
        self.assertTrue(c.check_declaration("Merge tier: C", "C")[0])
        self.assertTrue(c.check_declaration("Merge tier: D", "B")[0])

    def test_missing_passes_with_hint(self):
        ok, msg = c.check_declaration("no tier here", "B")
        self.assertTrue(ok)
        self.assertIn("Merge tier: B", msg)

    def test_conflicting_declarations_fail(self):
        self.assertFalse(c.check_declaration("Merge tier: A\nMerge tier: C", "A")[0])

    def test_overall_tier(self):
        results = [c.Result("a", "M", "A", ""), c.Result("b", "M", "C", ""), c.Result("c", "M", "B", "")]
        self.assertEqual(c.overall_tier(results), "C")
        self.assertEqual(c.overall_tier([]), "A")


class DocTest(unittest.TestCase):
    def test_merge_policy_table_is_current(self):
        rules, default = c.load_rules()
        text = c.DOC_PATH.read_text(encoding="utf-8")
        self.assertEqual(
            c.replace_doc_table(text, c.render_doc_table(rules, default)), text,
            "docs/merge_policy.md is stale, run: python3 scripts/merge_tier/classify.py --write-doc",
        )


class GitTest(unittest.TestCase):
    """Runs the CLI against a throwaway repository."""

    def setUp(self):
        self.tmp = tempfile.TemporaryDirectory()
        self.repo = Path(self.tmp.name)
        env = {**os.environ, "GIT_CONFIG_GLOBAL": os.devnull, "GIT_CONFIG_NOSYSTEM": "1"}
        self.git_env = env

        def run(*args):
            subprocess.run(["git", *args], cwd=self.repo, env=env, check=True, capture_output=True)

        self.run_git = run
        run("init", "-q", "-b", "main")
        run("config", "user.email", "test@example.com")
        run("config", "user.name", "test")
        run("config", "commit.gpgsign", "false")
        lang = self.repo / "common/x/src/main/resources/assets/chronodawn/lang/en_us.json"
        lang.parent.mkdir(parents=True)
        lang.write_text(json.dumps({"a": "x", "b": "y"}))
        (self.repo / "README.md").write_text("hi\n")
        run("add", "-A")
        run("commit", "-q", "-m", "base")
        run("checkout", "-q", "-b", "topic")
        self.lang = lang

    def tearDown(self):
        self.tmp.cleanup()

    def commit(self):
        self.run_git("add", "-A")
        self.run_git("commit", "-q", "-m", "change")

    def cli(self, body):
        body_file = self.repo / "body.md"
        body_file.write_text(body)
        with mock.patch.dict(os.environ, self.git_env), mock.patch("sys.stdout"):
            return c.main(["--repo", str(self.repo), "--base", "main", "--head", "topic",
                           "--body-file", str(body_file)])

    def test_lang_value_change_is_a(self):
        self.lang.write_text(json.dumps({"a": "changed", "b": "y"}))
        (self.repo / "README.md").write_text("hello\n")
        self.commit()
        self.assertEqual(self.cli("Merge tier: A"), 0)

    def test_renamed_lang_key_fails_tier_a(self):
        self.lang.write_text(json.dumps({"a": "x", "renamed": "y"}))
        self.commit()
        self.assertEqual(self.cli("Merge tier: A"), 1)
        self.assertEqual(self.cli("Merge tier: C"), 0)

    def test_worldgen_file_mixed_into_docs_fails_tier_a(self):
        (self.repo / "README.md").write_text("hello\n")
        f = self.repo / "common/x/src/main/resources/data/chronodawn/worldgen/biome/b.json"
        f.parent.mkdir(parents=True)
        f.write_text("{}")
        self.commit()
        self.assertEqual(self.cli("Merge tier: A"), 1)


if __name__ == "__main__":
    unittest.main()
