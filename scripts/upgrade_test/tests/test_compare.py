import unittest

from scripts.upgrade_test.compare import SetupFailed, check_before_present, compare_values, setup_reply_failed


class CompareTest(unittest.TestCase):
    def test_added_key_passes(self):
        self.assertEqual(compare_values({"a": 1}, {"a": 1, "b": 2}, "equal"), [])

    def test_removed_key_fails(self):
        diffs = compare_values({"a": 1, "b": 2}, {"a": 1}, "equal")
        self.assertEqual(len(diffs), 1)
        self.assertEqual(diffs[0].path, "b")
        self.assertEqual(diffs[0].reason, "missing")

    def test_changed_nested_value_fails(self):
        before = {"Items": [{"id": "chronodawn:clockstone", "count": 5}]}
        after = {"Items": [{"id": "chronodawn:clockstone", "count": 4}]}
        diffs = compare_values(before, after, "equal")
        self.assertEqual([d.path for d in diffs], ["Items[0].count"])
        self.assertEqual((diffs[0].before, diffs[0].after), (5, 4))

    def test_list_growth_passes(self):
        self.assertEqual(compare_values([1, 2], [1, 2, 3], "equal"), [])

    def test_list_shrink_fails(self):
        diffs = compare_values([1, 2, 3], [1, 2], "equal")
        self.assertEqual([d.path for d in diffs], ["[2]"])

    def test_scalar_strings_compared_exactly(self):
        self.assertEqual(len(compare_values("a", "b", "equal")), 1)

    def test_int_gte(self):
        self.assertEqual(compare_values("The time is 1200", "The time is 1500", "int_gte"), [])
        self.assertEqual(len(compare_values("The time is 1500", "The time is 1200", "int_gte")), 1)

    def test_monotonic_path_may_grow(self):
        before = {"data": {"IndependentTime": 336, "Fixed": 1}}
        self.assertEqual(compare_values(before, {"data": {"IndependentTime": 610, "Fixed": 1}}, "equal",
                                        monotonic=["data.IndependentTime"]), [])
        diffs = compare_values(before, {"data": {"IndependentTime": 100, "Fixed": 1}}, "equal",
                               monotonic=["data.IndependentTime"])
        self.assertEqual([(d.path, d.reason) for d in diffs], [("data.IndependentTime", "decreased")])

    def test_setup_reply_failures(self):
        for reply in ("Unknown item 'chronodawn:nope'", "Incorrect argument for command",
                      "...chronodawn:nope<--[HERE]", "Could not set the block", "No player was found",
                      "That position is not loaded", "Found no elements matching Items",
                      "The target block is not a block entity", "No entity was found"):
            self.assertTrue(setup_reply_failed(reply), reply)
        for reply in ("Changed the block at 1600, 200, 1600", "Summoned new Ticking Sheep",
                      "Replaced a slot at 1600, 200, 1600 with [Clockstone Pickaxe]",
                      "Applied 100000.0 damage to Time Guardian", "Granted 1606 advancements to Dev",
                      "Gave 32 [Clockstone] to Dev", "Gamerule randomTickSpeed is now set to: 0"):
            self.assertFalse(setup_reply_failed(reply), reply)

    def test_empty_container_is_setup_failure(self):
        with self.assertRaises(SetupFailed):
            check_before_present("chest", "1600, 200, 1620 has the following block data: []")
        with self.assertRaises(SetupFailed):
            check_before_present("be", "The target block is not a block entity")

    def test_missing_before_value_is_setup_failure(self):
        with self.assertRaises(SetupFailed):
            check_before_present("x", "No entity was found")
        for empty in (None, "", {}, []):
            with self.assertRaises(SetupFailed):
                check_before_present("x", empty)
        check_before_present("x", {"a": 1})

    def test_file_content_with_marker_words_is_present(self):
        # A config file legitimately mentions "Unknown keys"; only RCON replies carry failure markers.
        check_before_present("config", "# Unknown keys are ignored", command_reply=False)
        with self.assertRaises(SetupFailed):
            check_before_present("config", "", command_reply=False)


if __name__ == "__main__":
    unittest.main()
