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


READY = "chunks=81 missing=0 sections=40 queue=empty"


class FakeClient:
    def __init__(self, screens=None, errors=(), renders=None):
        self.screens = screens or [""]  # titles answered to "screen", the last one repeats
        self.renders = renders or [READY]  # answers to "render", the last one repeats
        self.errors = set(errors)
        self.sent = []

    def __call__(self, operation):
        self.sent.append(operation)
        if operation in self.errors:
            return "error: a screen is open: X"
        if operation == "screen":
            return self.screens.pop(0) if len(self.screens) > 1 else self.screens[0]
        if operation == "render":
            return self.renders.pop(0) if len(self.renders) > 1 else self.renders[0]
        return "ok"


def _runner(rcon, clock=None, captured=None, client=None):
    clock = clock or FakeClock()
    client = client if client is not None else FakeClient()
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

    def test_rejects_sleep_without_reason(self):
        with self.assertRaises(ScenarioError):
            load_scenario(self._write({"steps": [{"sleep": 5}]}))

    def test_accepts_sleep_with_reason(self):
        scenario = load_scenario(self._write({"steps": [{"sleep": 5, "reason": "no condition shows it"}]}))
        self.assertEqual(len(scenario.steps), 1)

    def test_rejects_shot_wait_without_reason(self):
        with self.assertRaises(ScenarioError):
            load_scenario(self._write({"steps": [{"shot": "a", "wait": 5}]}))

    def test_rejects_bad_settle(self):
        for step in ({"settle": 0}, {"settle": "x"}, {"settle": False}, {"settle": True}, {"settle": None},
                     {"shot": "a", "settle": -1}, {"shot": "a", "settle": None}):
            with self.subTest(step=step), self.assertRaises(ScenarioError):
                load_scenario(self._write({"steps": [step]}))

    def test_accepts_settle_and_shot_without_settle(self):
        scenario = load_scenario(self._write({"steps": [{"settle": 60}, {"shot": "a", "settle": False}]}))
        self.assertEqual(len(scenario.steps), 2)

    def test_input_until_needs_match(self):
        for step in ({"input": "use", "until": "x"}, {"input": "use", "match": "x"}, {"input": "use", "repeat": 3},
                     {"input": "use", "until": "x", "match": "y", "repeat": 0},
                     {"input": "use", "until": "x", "match": "y", "repeat": 1.5}):
            with self.subTest(step=step), self.assertRaises(ScenarioError):
                load_scenario(self._write({"steps": [step]}))

    def test_bundled_scenarios_keep_fixed_waits_short(self):
        # A guard against a scenario slipping back into long fixed sleeps.
        for scenario in load_scenarios(SCENARIOS_DIR):
            fixed = sum(s.get("sleep", 0) + s.get("wait", 0) for s in scenario.steps if isinstance(s, dict))
            self.assertLessEqual(fixed, 10, f"{scenario.name} sleeps {fixed}s in fixed waits")

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
        self.assertLessEqual(len(rcon.sent), 8)

    def test_max_seconds_fails_slow_check(self):
        rcon = FakeRcon({"locate": "at [1, ~, 2]"})
        scenario = Scenario("s", "", [{"command": "locate", "match": r"\[", "check": "fast", "max_seconds": 5}])
        outcome = _runner(rcon, clock=FakeClock(step=10)).run(scenario)
        self.assertEqual(outcome.results[0][:2], ("FAIL", "s/fast"))

    def test_shot_settles_then_records_note(self):
        clock = FakeClock()
        captured = []
        client = FakeClient()
        scenario = Scenario("s", "", [{"shot": "front", "note": "seen by ${player}"}])
        outcome = _runner(FakeRcon({}), clock=clock, captured=captured, client=client).run(scenario)
        self.assertEqual(captured, ["front"])
        self.assertIn("render", client.sent)
        self.assertGreaterEqual(clock.now, 2)
        self.assertLess(clock.now, 3)
        self.assertEqual(outcome.shots[0].note, "seen by Dev")
        self.assertEqual(outcome.results[0][0], "SHOT")

    def test_shot_extra_wait_and_no_settle(self):
        clock = FakeClock()
        client = FakeClient()
        scenario = Scenario("s", "", [{"shot": "front", "settle": False, "wait": 7, "reason": "r"}])
        _runner(FakeRcon({}), clock=clock, client=client).run(scenario)
        self.assertEqual(client.sent, [])
        self.assertEqual(clock.now, 7)

    def test_step_timings_are_recorded(self):
        clock = FakeClock()
        outcome = _runner(FakeRcon({}), clock=clock).run(Scenario("s", "", ["say a", {"sleep": 3, "reason": "r"}]))
        self.assertEqual([(n, kind) for n, kind, _ in outcome.timings], [(1, "command"), (2, "sleep")])
        self.assertEqual(outcome.seconds, 3)
        self.assertIn("# step 2 (sleep): 3.0s", outcome.transcript)

    def test_capture_error_is_fail_but_continues(self):
        rcon = FakeRcon({})

        def broken(name):
            raise RuntimeError("no window")
        clock = FakeClock()
        runner = Runner(rcon, broken, "Dev", FakeClient(), clock=clock, sleep=clock.sleep)
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
        runner = Runner(FakeRcon({}), lambda n: Path(n), "Dev", None, clock=FakeClock(), sleep=lambda s: None)
        outcome = runner.run(Scenario("s", "", [{"input": "use"}]))
        self.assertEqual(outcome.results[0][0], "SETUP FAILED")

    def test_input_until_clicks_until_the_command_matches(self):
        client = FakeClient()
        rcon = FakeRcon({"unless block": ["Test failed", "Test failed", "Test passed"]})
        outcome = _runner(rcon, client=client).run(Scenario("s", "", [
            {"input": "use", "until": "unless block", "match": "Test passed", "repeat": 10}, "say after"]))
        self.assertEqual(client.sent, ["use"] * 3)
        self.assertEqual(outcome.results, [])
        self.assertIn("say after", rcon.sent)

    def test_input_until_runs_out_of_clicks(self):
        client = FakeClient()
        rcon = FakeRcon({"unless block": "Test failed"})
        outcome = _runner(rcon, client=client).run(Scenario("s", "", [
            {"input": "use", "until": "unless block", "match": "Test passed", "repeat": 4}]))
        self.assertEqual(client.sent, ["use"] * 4)
        self.assertEqual(outcome.results[0][0], "SETUP FAILED")


class SettleTest(unittest.TestCase):
    def _settle(self, renders, timeout=60):
        clock = FakeClock()
        client = FakeClient(renders=renders)
        outcome = _runner(FakeRcon({}), clock=clock, client=client).run(
            Scenario("s", "", [{"settle": timeout}, "say after"]))
        return outcome, clock

    def test_waits_until_counts_stop_changing(self):
        busy = "chunks=50 missing=3 sections=10 queue=busy"
        growing = ["chunks=70 missing=0 sections=20 queue=empty", "chunks=81 missing=0 sections=30 queue=empty"]
        outcome, clock = self._settle(["error: no player in a world", busy, busy, *growing, READY])
        self.assertEqual(outcome.results, [])
        # 5 polls before the last state, then 2 seconds of it unchanged.
        self.assertAlmostEqual(clock.now, 5 * 0.25 + 2, places=5)

    def test_agent_error_fails_fast(self):
        outcome, clock = self._settle(["error: java.lang.NoSuchMethodException: x"], timeout=60)
        self.assertEqual(outcome.results[0][0], "SETUP FAILED")
        self.assertLess(clock.now, 1)

    def test_queued_builds_are_not_settled(self):
        outcome, clock = self._settle(["chunks=81 missing=0 sections=40 queue=busy"], timeout=5)
        self.assertEqual(outcome.results[0][:2], ("FAIL", "s/settle"))
        self.assertGreaterEqual(clock.now, 5)

    def test_missing_chunks_are_not_settled(self):
        outcome, _ = self._settle(["chunks=81 missing=1 sections=40 queue=empty"], timeout=5)
        self.assertEqual(outcome.results[0][0], "FAIL")

    def test_timeout_is_fail_but_the_scenario_goes_on(self):
        clock = FakeClock()
        rcon = FakeRcon({})
        client = FakeClient(renders=["chunks=1 missing=9 sections=0 queue=busy"])
        outcome = _runner(rcon, clock=clock, client=client).run(Scenario("s", "", [{"settle": 3}, "say after"]))
        self.assertEqual(outcome.results[0][0], "FAIL")
        self.assertIn("say after", rcon.sent)


class ScreenStepTest(unittest.TestCase):
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
        self.assertLessEqual(len(client.sent), 14)
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
