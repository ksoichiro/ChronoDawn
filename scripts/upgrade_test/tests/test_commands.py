import unittest

from scripts.upgrade_test.commands import adapt


class AdaptTest(unittest.TestCase):
    def test_keeps_commands_on_1_21_1_and_unknown_versions(self):
        self.assertEqual(adapt("gamerule doDaylightCycle false", "1.21.1"), "gamerule doDaylightCycle false")
        self.assertEqual(adapt("gamerule doDaylightCycle false", None), "gamerule doDaylightCycle false")

    def test_renames_game_rules_on_26_x(self):
        self.assertEqual(adapt("gamerule doDaylightCycle false", "26.3"), "gamerule advance_time false")
        self.assertEqual(adapt("gamerule randomTickSpeed 0", "26.3"), "gamerule random_tick_speed 0")
        self.assertEqual(adapt("gamerule doMobSpawning true", "26.3"), "gamerule spawn_mobs true")
        self.assertEqual(adapt("execute in minecraft:overworld run gamerule mobGriefing false", "26.3"),
                         "execute in minecraft:overworld run gamerule mob_griefing false")

    def test_maps_values_of_merged_rules(self):
        self.assertEqual(adapt("gamerule doFireTick false", "26.3"), "gamerule fire_spread_radius_around_player 0")

    def test_leaves_other_commands_and_unknown_rules_alone(self):
        self.assertEqual(adapt("say gamerule doDaylightCycle", "26.3"), "say gamerule doDaylightCycle")
        self.assertEqual(adapt("gamerule keepInventory true", "26.3"), "gamerule keepInventory true")
        self.assertEqual(adapt("time set noon", "26.3"), "time set noon")


if __name__ == "__main__":
    unittest.main()
