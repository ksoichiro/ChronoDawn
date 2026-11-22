import json
import tempfile
import unittest
from pathlib import Path

from scripts.scenario_check.scenario import (Runner, Scenario, ScenarioError, StepFailed, load_scenario,
                                             load_scenarios, substitute)

SCENARIOS_DIR = Path(__file__).resolve().parent.parent / "scenarios"


class FakeRcon:
    def __init__(self, replies):
        self.replies = replies  # command -> reply, or a list of replies used in order
        self.sent = []

    def command(self, cmd):
        self.sent.append(cmd)
        reply = self.replies.get(cmd, "ok")
        return reply.pop(0) if isinstance(reply, list) else reply


class FakeClock:
    def __init__(self, step=0.0):
        self.now = 0.0
        self.step = step

    def __call__(self):
        self.now += self.step
        return self.now

    def sleep(self, seconds):
        self.now += seconds


class FakeClient:
    def __init__(self, screens=None, errors=()):
        self.screens = screens or [""]  # titles answered to "screen", the last one repeats
        self.errors = set(errors)
        self.sent = []

    def __call__(self, operation):
        self.sent.append(operation)
        if operation in self.errors:
            return "error: a screen is open: X"
        if operation == "screen":
            return self.screens.pop(0) if len(self.screens) > 1 else self.screens[0]
        return "ok"


def _runner(rcon, clock=None, captured=None, client=None):
    clock = clock or FakeClock()
    captured = captured if captured is not None else []

    def capture(name):
        captured.append(name)
        return Path(f"/shots/{name}.png")
    return Runner(rcon, capture, "Dev", client, clock=clock, sleep=clock.sleep)


class SubstituteTest(unittest.TestCase):
    def test_replaces_known_variables_and_keeps_braces(self):
        self.assertEqual(substitute("tp ${player} ${x} 0 0 {a:1}", {"player": "Dev", "x": 5}),
                         "tp Dev 5 0 0 {a:1}")

    def test_undefined_variable_fails(self):
        with self.assertRaises(StepFailed):
            substitute("tp ${nope}", {})


class LoadTest(unittest.TestCase):
    def _write(self, raw):
        tmp = tempfile.TemporaryDirectory()
        self.addCleanup(tmp.cleanup)
        path = Path(tmp.name) / "s.json"
        path.write_text(json.dumps(raw))
        return path

    def test_bundled_scenarios_are_valid(self):
        scenarios = load_scenarios(SCENARIOS_DIR)
        self.assertTrue(scenarios)
        for scenario in scenarios:
            self.assertTrue(scenario.description, scenario.name)

    def test_rejects_unknown_key(self):
        with self.assertRaises(ScenarioError):
            load_scenario(self._write({"steps": [{"command": "x", "mach": "y"}]}))

    def test_rejects_check_without_match(self):
        with self.assertRaises(ScenarioError):
            load_scenario(self._write({"steps": [{"command": "x", "check": "c"}]}))

    def test_rejects_two_kinds(self):
        with self.assertRaises(ScenarioError):
            load_scenario(self._write({"steps": [{"command": "x", "sleep": 1}]}))

    def test_rejects_duplicate_shots(self):
        with self.assertRaises(ScenarioError):
            load_scenario(self._write({"steps": [{"shot": "a"}, {"shot": "a"}]}))

    def test_rejects_unsafe_shot_name(self):
        with self.assertRaises(ScenarioError):
            load_scenario(self._write({"steps": [{"shot": "../a"}]}))

    def test_accepts_input_and_screen_steps(self):
        scenario = load_scenario(self._write({"steps": [
            {"input": "use"}, {"input": "attack"}, {"input": "close"},
            {"screen": "Time Keeper", "check": "opened", "timeout": 5}]}))
        self.assertEqual(len(scenario.steps), 4)

    def test_rejects_unknown_input(self):
        with self.assertRaises(ScenarioError):
            load_scenario(self._write({"steps": [{"input": "jump"}]}))

    def test_rejects_input_with_extra_keys(self):
        with self.assertRaises(ScenarioError):
            load_scenario(self._write({"steps": [{"input": "use", "match": "x"}]}))

    def test_rejects_invalid_screen_regex(self):
        with self.assertRaises(ScenarioError):
            load_scenario(self._write({"steps": [{"screen": "("}]}))

    def test_unknown_scenario_name(self):
        with self.assertRaises(ScenarioError):
            load_scenarios(SCENARIOS_DIR, ["does-not-exist"])


class RunnerTest(unittest.TestCase):
    def test_capture_variables_feed_later_steps(self):
        rcon = FakeRcon({"locate": "The nearest is at [120, ~, -40] (130 blocks away)"})
        scenario = Scenario("s", "", [
            {"command": "locate", "match": r"\[(?P<x>-?\d+), [^,]+, (?P<z>-?\d+)\]", "check": "found"},
            "tp ${player} ${x} 100 ${z}",
        ])
        outcome = _runner(rcon).run(scenario)
        self.assertEqual(rcon.sent[-1], "tp Dev 120 100 -40")
        self.assertEqual(outcome.results[0][:2], ("PASS", "s/found"))

    def test_failed_setup_command_stops_and_reports_skipped_shots(self):
        rcon = FakeRcon({"setblock 0 0 0 nope:x": "Unknown block type 'nope:x'"})
        captured = []
        scenario = Scenario("s", "", ["setblock 0 0 0 nope:x", "say never", {"shot": "a"}])
        outcome = _runner(rcon, captured=captured).run(scenario)
        self.assertEqual(outcome.results[0][0], "SETUP FAILED")
        self.assertEqual(outcome.results[1][0], "NOT COVERED")
        self.assertNotIn("say never", rcon.sent)
        self.assertEqual(captured, [])

    def test_failed_check_is_fail_and_stops(self):
        rcon = FakeRcon({"execute if block 0 0 0 stone": "Test failed"})
        scenario = Scenario("s", "", [
            {"command": "execute if block 0 0 0 stone", "match": "Test passed", "check": "placed"},
            "say never",
        ])
        outcome = _runner(rcon).run(scenario)
        self.assertEqual(outcome.results[0][:2], ("FAIL", "s/placed"))
        self.assertNotIn("say never", rcon.sent)

    def test_await_polls_until_match(self):
        rcon = FakeRcon({"if loaded": ["Test failed", "Test failed", "Test passed"]})
        scenario = Scenario("s", "", [{"command": "if loaded", "match": "Test passed", "await": True, "timeout": 10}])
        outcome = _runner(rcon).run(scenario)
        self.assertEqual(rcon.sent, ["if loaded"] * 3)
        self.assertEqual(outcome.results, [])

    def test_await_times_out(self):
        rcon = FakeRcon({"if loaded": "Test failed"})
        scenario = Scenario("s", "", [{"command": "if loaded", "match": "Test passed", "await": True, "timeout": 3}])
        outcome = _runner(rcon).run(scenario)
        self.assertEqual(outcome.results[0][0], "SETUP FAILED")
        self.assertLessEqual(len(rcon.sent), 5)

    def test_max_seconds_fails_slow_check(self):
        rcon = FakeRcon({"locate": "at [1, ~, 2]"})
        scenario = Scenario("s", "", [{"command": "locate", "match": r"\[", "check": "fast", "max_seconds": 5}])
        outcome = _runner(rcon, clock=FakeClock(step=10)).run(scenario)
        self.assertEqual(outcome.results[0][:2], ("FAIL", "s/fast"))

    def test_shot_records_note_and_waits(self):
        clock = FakeClock()
        captured = []
        rcon = FakeRcon({})
        scenario = Scenario("s", "", [{"shot": "front", "wait": 7, "note": "seen by ${player}"}])
        outcome = _runner(rcon, clock=clock, captured=captured).run(scenario)
        self.assertEqual(captured, ["front"])
        self.assertEqual(clock.now, 7)
        self.assertEqual(outcome.shots[0].note, "seen by Dev")
        self.assertEqual(outcome.results[0][0], "SHOT")

    def test_capture_error_is_fail_but_continues(self):
        rcon = FakeRcon({})

        def broken(name):
            raise RuntimeError("no window")
        runner = Runner(rcon, broken, "Dev", clock=FakeClock(), sleep=lambda s: None)
        outcome = runner.run(Scenario("s", "", [{"shot": "a"}, "say after"]))
        self.assertEqual(outcome.results[0][0], "FAIL")
        self.assertIn("say after", rcon.sent)


class ClientStepTest(unittest.TestCase):
    def test_input_is_sent_to_the_client(self):
        client = FakeClient()
        outcome = _runner(FakeRcon({}), client=client).run(Scenario("s", "", [{"input": "use"}, "say after"]))
        self.assertEqual(client.sent, ["use"])
        self.assertEqual(outcome.results, [])
        self.assertIn("> (client) use\nok", outcome.transcript)

    def test_input_error_stops_the_scenario(self):
        rcon = FakeRcon({})
        client = FakeClient(errors=["use"])
        outcome = _runner(rcon, client=client).run(Scenario("s", "", [{"input": "use"}, "say never", {"shot": "a"}]))
        self.assertEqual(outcome.results[0][0], "SETUP FAILED")
        self.assertEqual(outcome.results[1][0], "NOT COVERED")
        self.assertNotIn("say never", rcon.sent)

    def test_input_without_client_is_setup_failure(self):
        outcome = _runner(FakeRcon({})).run(Scenario("s", "", [{"input": "use"}]))
        self.assertEqual(outcome.results[0][0], "SETUP FAILED")

    def test_screen_waits_until_title_matches(self):
        client = FakeClient(screens=["", "", "Time Keeper"])
        outcome = _runner(FakeRcon({}), client=client).run(
            Scenario("s", "", [{"screen": "Time Keeper", "check": "opened"}]))
        self.assertEqual(client.sent, ["screen"] * 3)
        self.assertEqual(outcome.results[0][:2], ("PASS", "s/opened"))

    def test_screen_check_times_out_as_fail(self):
        rcon = FakeRcon({})
        client = FakeClient(screens=[""])
        outcome = _runner(rcon, client=client).run(
            Scenario("s", "", [{"screen": "Time Keeper", "check": "opened", "timeout": 3}, "say never"]))
        self.assertEqual(outcome.results[0][:2], ("FAIL", "s/opened"))
        self.assertLessEqual(len(client.sent), 5)
        self.assertNotIn("say never", rcon.sent)

    def test_screen_without_check_mismatch_is_setup_failure(self):
        client = FakeClient(screens=["Chest"])
        outcome = _runner(FakeRcon({}), client=client).run(Scenario("s", "", [{"screen": "^$", "timeout": 2}]))
        self.assertEqual(outcome.results[0][0], "SETUP FAILED")

    def test_empty_pattern_matches_no_screen(self):
        client = FakeClient(screens=[""])
        outcome = _runner(FakeRcon({}), client=client).run(Scenario("s", "", [{"screen": "^$", "check": "closed"}]))
        self.assertEqual(outcome.results[0][0], "PASS")


if __name__ == "__main__":
    unittest.main()
