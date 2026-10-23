"""Upgrade test driver: build a world with an old release, upgrade it, and check nothing was lost.

Invoked by the upgradeTest Gradle task with the env.json that prepareUpgradeTest
writes. See docs/superpowers/specs/2026-10-23-upgrade-test-automation-design.md.
"""

import argparse
import json
import re
import shutil
import sys
import time
from pathlib import Path

from scripts.upgrade_test import compare, logcheck, processes, snapshot, versions
from scripts.upgrade_test.layers import load_layers, layers_for_source
from scripts.upgrade_test.rcon import Rcon

HERE = Path(__file__).resolve().parent
PLAYER = "Dev"
JOIN_TIMEOUT = 300
AWAIT_TIMEOUT = 120


class SetupError(Exception):
    pass


def _rcon(ports):
    return Rcon("127.0.0.1", ports[1], processes.RCON_PASSWORD)


def _run_setup_entry(rcon, entry, transcript, layer_name="?"):
    """A setup entry is a command string, or {"await": cmd, "match": regex} that polls an RCON reply."""
    if isinstance(entry, str):
        reply = rcon.command(entry)
        transcript.append(f"> {entry}\n{reply}")
        # A failed setup command (unknown id in the old release, unloaded chunk,
        # ...) often leaves the checked value empty on both sides of the upgrade.
        if compare.setup_reply_failed(reply):
            raise SetupError(f"{layer_name}: {entry!r} -> {reply!r}")
        return
    deadline = time.monotonic() + entry.get("timeout", AWAIT_TIMEOUT)
    pattern = re.compile(entry["match"])
    while True:
        reply = rcon.command(entry["await"])
        if pattern.search(reply):
            transcript.append(f"> (await) {entry['await']}\n{reply}")
            return
        if time.monotonic() > deadline:
            raise SetupError(f"{layer_name}: await timed out: {entry['await']!r} never matched "
                             f"/{entry['match']}/, last reply: {reply!r}")
        time.sleep(1)


def read_when_ready(rcon, command, timeout=AWAIT_TIMEOUT, interval=1.0):
    """Re-read until the reply no longer says "not loaded / not found".

    After a restart, forceloaded chunks and their entities load a few ticks
    after the player joins, so a first read can miss data that is there.
    """
    deadline = time.monotonic() + timeout
    while True:
        reply = rcon.command(command)
        if not compare.looks_unready(reply) or time.monotonic() >= deadline:
            return reply
        time.sleep(interval)


def _start_session(env, server_dir, client_dir, mods, ports, label, out_dir):
    server = processes.run_server(env.server, server_dir, mods, out_dir / f"{label}-server.log")
    client = None
    try:
        client = processes.run_client(env.client, client_dir, mods, ports[0], out_dir / f"{label}-client.log")
        try:
            server.wait_for_line(rf"{PLAYER} joined the game", JOIN_TIMEOUT)
        except processes.ReadyTimeout as e:
            raise processes.ReadyTimeout(f"{e} Client log: {client.log_file}") from e
    except BaseException:
        if client:
            client.kill()
        server.kill()
        raise
    return server, client


def _end_session(server, client, ports):
    # Disconnect from the server side first: killing a connected client makes
    # the server log a "Connection reset" ERROR that the log check would flag.
    with _rcon(ports) as rcon:
        rcon.command(f"kick {PLAYER} upgrade test session finished")
    server.wait_for_line(rf"{PLAYER} left the game", 60)
    client.stop(None, 60)
    with _rcon(ports) as rcon:
        rcon.command("save-all flush")
    server.stop("stop", 180)


def _offline_value(world, check):
    kind = check["kind"]
    if kind == "saved_data":
        return snapshot.read_saved_data(world, check.get("dimension", "overworld"), check["file"])
    if kind == "advancements":
        return snapshot.read_advancements(world, snapshot.offline_uuid(PLAYER))
    if kind == "file_identical":
        raw = snapshot.read_file_bytes(world.parent / check["path"])
        return None if raw is None else raw.decode("utf-8", errors="replace")
    raise ValueError(kind)


def _forward_check(rcon, forward, transcript):
    """Force-generate an area of a biome only the candidate's worldgen has seen; returns its chunk set."""
    dim = forward["dimension"]
    reply = rcon.command(f"execute in {dim} run locate biome {forward['biome']}")
    transcript.append(f"> locate biome {forward['biome']}\n{reply}")
    match = re.search(r"\[(-?\d+), [^,]+, (-?\d+)\]", reply)
    if not match:
        raise SetupError(f"forward check: locate biome failed: {reply!r}")
    x, z = int(match.group(1)), int(match.group(2))
    radius = forward.get("radius_chunks", 8)
    cx, cz = x >> 4, z >> 4
    x1, z1 = (cx - radius) * 16, (cz - radius) * 16
    x2, z2 = (cx + radius) * 16 - 1, (cz + radius) * 16 - 1
    reply = rcon.command(f"execute in {dim} run forceload add {x1} {z1} {x2} {z2}")
    transcript.append(f"> forceload add {x1} {z1} {x2} {z2}\n{reply}")
    for px, pz in ((x1, z1), (x2, z2), (x1, z2), (x2, z1)):
        _run_setup_entry(rcon, {"await": f"execute in {dim} if loaded {px} 0 {pz}", "match": "Test passed"},
                         transcript)
    return {(a, b) for a in range(cx - radius, cx + radius) for b in range(cz - radius, cz + radius)}


def run_source(env, source, layers, forward, candidate_jar, out_root, ports):
    results = []
    out_dir = out_root / source
    if out_dir.exists():
        shutil.rmtree(out_dir)
    out_dir.mkdir(parents=True)
    server_dir = out_dir / "server"
    world = server_dir / "world"
    transcript = []
    active = layers_for_source(layers, source)

    processes.preflight_ports(list(ports))
    processes.prepare_server_dir(env.server, server_dir, ports)
    old_jar = versions.fetch_old_jar(source, env.loader, env.cache_dir)
    old_mods = [old_jar, *env.prod_mods]
    new_mods = [candidate_jar, *env.prod_mods]
    print(f"[{env.loader} {source}] old: {old_jar.name}, candidate: {candidate_jar.name}", flush=True)

    # --- Old release: build the state and record it ---
    command_before = {}
    server, client = _start_session(env, server_dir, out_dir / "old-client", old_mods, ports, "old", out_dir)
    try:
        with _rcon(ports) as rcon:
            for layer in active:
                for entry in layer.setup:
                    _run_setup_entry(rcon, entry, transcript, layer.name)
            for layer in active:
                for check in layer.checks:
                    if check["kind"] == "command":
                        command_before[check["id"]] = read_when_ready(rcon, check["command"])
        _end_session(server, client, ports)
    finally:
        client.kill()
        server.kill()
        (out_dir / "setup-transcript.txt").write_text("\n".join(transcript) + "\n")

    for layer in active:
        for patch in layer.offline_patches:
            snapshot.set_saved_data_value(world, patch.get("dimension", "overworld"), patch["file"],
                                          patch["path"], patch["value"], patch["type"])

    before = {}
    setup_ok = True
    for layer in active:
        for check in layer.checks:
            if check["kind"] == "command":
                value = command_before[check["id"]]
            elif check["kind"] == "block_counts":
                value = snapshot.block_counts(world, check["dimension"])
            else:
                value = _offline_value(world, check)
            try:
                compare.check_before_present(check["id"], value[0] if check["kind"] == "block_counts" else value,
                                             command_reply=check["kind"] == "command")
                before[check["id"]] = value
            except compare.SetupFailed as e:
                if check.get("optional"):
                    # State the old release creates on its own schedule (not by our commands).
                    results.append(("NOT COVERED", f"{source}/{check['id']}", f"absent before the upgrade: {check['optional']}"))
                    continue
                setup_ok = False
                results.append(("SETUP FAILED", f"{source}/{check['id']}", repr(e.value)[:500]))
    shutil.copytree(world, out_dir / "before-world")
    if not setup_ok:
        return results

    # --- Candidate: load the same world and read everything back ---
    command_after = {}
    forward_chunks = None
    server, client = _start_session(env, server_dir, out_dir / "new-client", new_mods, ports, "new", out_dir)
    try:
        with _rcon(ports) as rcon:
            for layer in active:
                for check in layer.checks:
                    if check["kind"] == "command":
                        command_after[check["id"]] = read_when_ready(rcon, check["command"])
            if forward:
                forward_chunks = _forward_check(rcon, forward, transcript)
        _end_session(server, client, ports)
    finally:
        client.kill()
        server.kill()
        (out_dir / "setup-transcript.txt").write_text("\n".join(transcript) + "\n")

    for layer in active:
        for check in layer.checks:
            cid = f"{source}/{check['id']}"
            if check["id"] not in before:
                continue  # optional state that was absent; already reported as NOT COVERED
            if check["kind"] == "command":
                after = command_after[check["id"]]
                diffs = compare.compare_values(before[check["id"]], after, check.get("compare", "equal"))
            elif check["kind"] == "block_counts":
                # Only chunks the candidate loaded and wrote back say anything about
                # the upgrade; the rest still hold the old release's bytes.
                _, chunks = before[check["id"]]
                dim = check["dimension"]
                before_stamps = snapshot.chunk_timestamps(out_dir / "before-world", dim)
                after_stamps = snapshot.chunk_timestamps(world, dim)
                resaved = {c for c in chunks if after_stamps.get(c) != before_stamps.get(c)}
                if not resaved:
                    results.append(("FAIL", cid, f"none of the {len(chunks)} counted chunks was re-saved by the candidate"))
                    continue
                before_counts, _ = snapshot.block_counts(out_dir / "before-world", dim, resaved)
                after_counts, _ = snapshot.block_counts(world, dim, resaved)
                ignored = set(check.get("ignore", []))
                diffs = compare.compare_values(
                    {k: v for k, v in before_counts.items() if k not in ignored},
                    {k: v for k, v in after_counts.items() if k not in ignored}, "equal")
                coverage = f"{len(resaved)} of {len(chunks)} chunks re-saved by the candidate"
                results.append(("FAIL", cid, f"{coverage}: {compare.describe(diffs)[:2000]}") if diffs
                               else ("PASS", cid, coverage))
                continue
            else:
                diffs = compare.compare_values(before[check["id"]], _offline_value(world, check), "equal",
                                               monotonic=check.get("monotonic", []))
            results.append(("FAIL", cid, compare.describe(diffs)[:2000]) if diffs else ("PASS", cid, ""))

    if forward:
        counts, _ = snapshot.block_counts(world, forward["dimension"], forward_chunks)
        found = counts.get(forward["block"], 0)
        status = "PASS" if found >= forward.get("min_count", 1) else "FAIL"
        results.append((status, f"{source}/forward:{forward['block']}", f"{found} blocks in {len(forward_chunks)} chunks"))

    server_problems = logcheck.check_server_log(out_dir / "new-server.log", env.log_ignore_file)
    results.append(("FAIL" if server_problems else "PASS", f"{source}/server-log", "\n  ".join(server_problems[:30])))
    client_problems = logcheck.check_client_log(out_dir / "new-client.log", out_dir / "new-client", env.log_ignore_file)
    results.append(("FAIL" if client_problems else "PASS", f"{source}/client-log", "\n  ".join(client_problems[:30])))

    for layer in active:
        for item in layer.not_covered:
            results.append(("NOT COVERED", f"{source}/{item['item']}", item["reason"]))
    return results


class Env:
    def __init__(self, raw):
        self.loader = raw["loader"]
        self.repo_root = Path(raw["repoRoot"])
        self.cache_dir = Path(raw["cacheDir"])
        self.out_dir = Path(raw["outDir"])
        self.baseline = raw["baseline"]
        self.candidate_jar = Path(raw["candidateJar"])
        self.prod_mods = [Path(p) for p in raw["prodMods"]]
        self.ports = tuple(raw["ports"])
        self.log_ignore_file = Path(raw["logIgnoreFile"])
        self.server = processes.ServerEnv(Path(raw["serverTemplateDir"]), raw["serverCommand"])
        c = raw["client"]
        self.client = processes.ClientEnv(c["java"], c["jvmArgs"], c["classpath"], c["mainClass"], c["args"])


def main(argv=None) -> int:
    parser = argparse.ArgumentParser(description=__doc__)
    parser.add_argument("--env", required=True, type=Path)
    parser.add_argument("--sources", help="comma-separated versions; default: baseline + previous release")
    parser.add_argument("--candidate-jar", type=Path)
    args = parser.parse_args(argv)

    env = Env(json.loads(args.env.read_text()))
    # Never leave an earlier run's report where a reader would trust it.
    report_file = env.out_dir / "report.txt"
    report_file.unlink(missing_ok=True)
    processes.install_signal_handlers()
    layers_dir = HERE / "layers"
    layers = load_layers(layers_dir) if layers_dir.exists() else []
    forward_file = HERE / "forward.json"
    forward = json.loads(forward_file.read_text()) if forward_file.exists() else None
    if args.sources:
        sources = [s.strip() for s in args.sources.split(",") if s.strip()]
    else:
        sources = versions.resolve_sources(env.baseline, versions.resolve_previous_release(env.repo_root))
    candidate = args.candidate_jar or env.candidate_jar

    results = []
    for source in sources:
        try:
            results += run_source(env, source, layers, forward, candidate, env.out_dir, env.ports)
        except SetupError as e:
            results.append(("SETUP FAILED", f"{source}/setup", str(e)))
        except Exception as e:  # one broken source must not hide the others
            results.append(("FAIL", f"{source}/harness", f"{type(e).__name__}: {e}"))

    report = f"upgradeTest {env.loader} 1.21.1, candidate {candidate.name}\n" + compare.format_report(results)
    env.out_dir.mkdir(parents=True, exist_ok=True)
    report_file.write_text(report)
    print(report, flush=True)
    failed = [r for r in results if r[0] in ("FAIL", "SETUP FAILED")]
    print(f"{len(failed)} failure(s). Report: {env.out_dir / 'report.txt'}", flush=True)
    return 1 if failed else 0


if __name__ == "__main__":
    sys.exit(main())
