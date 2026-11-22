#!/usr/bin/env python3
"""Finish the Sentinel Outpost template after it is saved from a structure block.

The outpost is built and adjusted by hand in the game, and the saved NBT is the
source of truth for its shape. Some things are hard to set in the game, so this
script adds them to the saved template:

- Spawner settings. Spawners within one block of the vertical centre line of
  the template are the hall spawner (the lowest one) and the deck spawners
  (the ones above it). Every other spawner is a docking bay spawner. Each gets its SpawnPotentials, SpawnData and timing values, and
  the Clockwork Sentinels it spawns use the outpost drop table.
- Empty reward chests. The chests are filled by the sentinel_outpost_loot
  processor, so items left in them while building are removed.
- Exterior air removal, the same as scripts/strip_exterior_air.py.

It also checks the building rules and stops with an error when one is broken:

- No black concrete is left. The trim is grey concrete.
- Every spawner is enclosed on all six sides by Reversing Time Sandstone. The
  enclosure only protects the spawner itself: mobs still spawn anywhere within
  SpawnRange of it.

Run it every time the template is saved from a structure block. Running it
again on its own output changes nothing.

Usage:

  python3 scripts/finish_sentinel_outpost.py [path/to/sentinel_outpost.nbt]

Requirements: nbtlib (pip install nbtlib)
"""
import os
import sys

import nbtlib
from nbtlib import tag

sys.path.insert(0, os.path.dirname(os.path.abspath(__file__)))
import strip_exterior_air  # noqa: E402

DEFAULT_PATH = os.path.join(
    os.path.dirname(os.path.abspath(__file__)), os.pardir,
    "common", "shared-1.21.1+", "src", "main", "resources", "data",
    "chronodawn", "structure", "sentinel_outpost.nbt")

SPAWNER = "minecraft:spawner"
CHEST = "minecraft:chest"
ENCLOSURE = "chronodawn:reversing_time_sandstone"
FORBIDDEN = ("minecraft:black_concrete",)

# Weights favour the Clockwork Sentinel. Epoch Husk and Secondhand Archer only
# use Mob.checkMobSpawnRules, so the spawners work in daylight too.
BAY_MOBS = (("chronodawn:clockwork_sentinel", 8),
            ("chronodawn:epoch_husk", 1),
            ("chronodawn:secondhand_archer", 1))
HALL_MOBS = (("chronodawn:clockwork_sentinel", 6),
             ("chronodawn:epoch_husk", 2),
             ("chronodawn:secondhand_archer", 2))
# The decks are small rooms on the way up, so their spawners send out
# Sentinels only and keep fewer of them around.
DECK_MOBS = (("chronodawn:clockwork_sentinel", 1),)
BAY_MAX_NEARBY = 8
HALL_MAX_NEARBY = 6
DECK_MAX_NEARBY = 4

# Sentinels spawned here drop less than the ones in the open, so that the
# spawners are not a farm. Other mobs keep their normal loot tables.
OUTPOST_SENTINEL_LOOT = "chronodawn:entities/clockwork_sentinel_outpost"

NEIGHBOURS = ((1, 0, 0), (-1, 0, 0), (0, 1, 0), (0, -1, 0), (0, 0, 1), (0, 0, -1))


def entity_nbt(mob):
    entity = tag.Compound({"id": tag.String(mob)})
    if mob == "chronodawn:clockwork_sentinel":
        entity["DeathLootTable"] = tag.String(OUTPOST_SENTINEL_LOOT)
    return entity


def spawner_nbt(mobs, max_nearby):
    potentials = tag.List[tag.Compound]([
        tag.Compound({
            "weight": tag.Int(weight),
            "data": tag.Compound({"entity": entity_nbt(mob)}),
        }) for mob, weight in mobs])
    return tag.Compound({
        "id": tag.String(SPAWNER),
        "SpawnPotentials": potentials,
        "SpawnData": tag.Compound({"entity": entity_nbt(mobs[0][0])}),
        "Delay": tag.Short(20),
        "MinSpawnDelay": tag.Short(150),
        "MaxSpawnDelay": tag.Short(400),
        "SpawnCount": tag.Short(3),
        "MaxNearbyEntities": tag.Short(max_nearby),
        "RequiredPlayerRange": tag.Short(16),
        "SpawnRange": tag.Short(4),
    })


def finish(path):
    structure = nbtlib.load(path)
    if "palette" not in structure:
        sys.exit(f"{path}: templates with several palettes are not supported")
    names = [str(state["Name"]) for state in structure["palette"]]
    size = [int(v) for v in structure["size"]]
    by_pos = {}
    for block in structure["blocks"]:
        by_pos[tuple(int(v) for v in block["pos"])] = block

    errors = []
    for pos, block in by_pos.items():
        if names[int(block["state"])] in FORBIDDEN:
            errors.append(f"{names[int(block['state'])]} at {pos}")

    spawners = [pos for pos, block in by_pos.items() if names[int(block["state"])] == SPAWNER]
    if not spawners:
        errors.append("no spawner found")
    for pos in spawners:
        for dx, dy, dz in NEIGHBOURS:
            n = (pos[0] + dx, pos[1] + dy, pos[2] + dz)
            block = by_pos.get(n)
            if block is None or names[int(block["state"])] != ENCLOSURE:
                errors.append(f"spawner at {pos} is not enclosed by {ENCLOSURE} at {n}")
    if errors:
        sys.exit(f"{path}:\n  " + "\n  ".join(errors))

    cx, cz = (size[0] - 1) / 2, (size[2] - 1) / 2
    centre = sorted((p for p in spawners if abs(p[0] - cx) <= 1 and abs(p[2] - cz) <= 1), key=lambda p: p[1])
    if not centre:
        sys.exit(f"{path}: no hall spawner near the centre")
    hall, decks = centre[0], centre[1:]
    for pos in spawners:
        if pos == hall:
            mobs, max_nearby = HALL_MOBS, HALL_MAX_NEARBY
        elif pos in decks:
            mobs, max_nearby = DECK_MOBS, DECK_MAX_NEARBY
        else:
            mobs, max_nearby = BAY_MOBS, BAY_MAX_NEARBY
        by_pos[pos]["nbt"] = spawner_nbt(mobs, max_nearby)

    chests = 0
    for pos, block in by_pos.items():
        if names[int(block["state"])] == CHEST:
            block["nbt"] = tag.Compound({"id": tag.String(CHEST), "Items": tag.List[tag.Compound]([])})
            chests += 1

    structure.save()
    print(f"{path}: hall spawner at {hall}, {len(decks)} deck spawners, "
          f"{len(spawners) - 1 - len(decks)} bay spawners, {chests} chests")
    strip_exterior_air.strip(path)


if __name__ == "__main__":
    for arg in sys.argv[1:] or [DEFAULT_PATH]:
        finish(os.path.normpath(arg))
