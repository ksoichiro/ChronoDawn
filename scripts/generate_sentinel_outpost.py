#!/usr/bin/env python3
"""Generate the Sentinel Outpost structure template.

The outpost is a circular landing pad of white and grey concrete and quartz
around a tall two-storey central hall. The Clockwork Sentinels, which are the only mobs
that Time Distortion cannot slow, keep it. Four docking bays on the pad and one
spawner in the hall keep producing mobs, mostly Clockwork Sentinels with a few
Epoch Husks and Secondhand Archers mixed in. Two chests on the hall's upper floor
hold the reward.

The look is deliberately different from the Clockwork Depths, which is an old
factory of temporal stone: clean white surfaces, black trim and sea lanterns,
like something that landed from outside.

Layout (local coordinates, the template is SIZE x HEIGHT x SIZE):

  y = 0        foundation layer under the pad (the foundation filler and the
               terrain beard blend it with the ground)
  y = 1        pad floor
  y = 2..6     hall ground floor, 7 is the upper floor, 8..11 the upper hall,
               12 the roof. Bay towers reach y = 12 and the antenna y = 23.

Only the pad's disc is written. Positions outside the disc are left out of the
block list so they keep the terrain, the same as a structure_void block.

Usage:

  python3 scripts/generate_sentinel_outpost.py

The output is already free of exterior air, so strip_exterior_air.py has
nothing to remove, but StructureTemplateTest checks the result the same way.

Requirements: nbtlib (pip install nbtlib)
"""
import math
import os
import sys

import nbtlib
from nbtlib import tag

SIZE = 29
HEIGHT = 24
CENTER = 14
RADIUS = 14.3
DATA_VERSION = 3955  # Minecraft 1.21.1, the version the other templates use

OUT_PATH = os.path.join(
    os.path.dirname(os.path.abspath(__file__)), os.pardir,
    "common", "shared-1.21.1+", "src", "main", "resources", "data",
    "chronodawn", "structure", "sentinel_outpost.nbt")

AIR = "minecraft:air"
FOUNDATION = "minecraft:light_gray_concrete"
FLOOR = "minecraft:smooth_quartz"
FLOOR_ALT = "minecraft:white_concrete"
TRIM = "minecraft:black_concrete"
INLAY = "minecraft:light_blue_concrete"
WALL = "minecraft:quartz_block"
PILLAR = "minecraft:quartz_pillar"
ROOF = "minecraft:smooth_quartz"
GLASS = "minecraft:light_blue_stained_glass"
LAMP = "minecraft:sea_lantern"
ROD = "minecraft:end_rod"
SPAWNER = "minecraft:spawner"
CHEST = "minecraft:chest"

# Weights favour the Clockwork Sentinel. Epoch Husk and Secondhand Archer only
# use Mob.checkMobSpawnRules, so the spawners work in daylight too.
BAY_MOBS = (("chronodawn:clockwork_sentinel", 8),
            ("chronodawn:epoch_husk", 1),
            ("chronodawn:secondhand_archer", 1))
HALL_MOBS = (("chronodawn:clockwork_sentinel", 6),
             ("chronodawn:epoch_husk", 2),
             ("chronodawn:secondhand_archer", 2))

BAY_CENTERS = ((6, 6), (22, 6), (6, 22), (22, 22))
HALL_MIN, HALL_MAX = 10, 18  # inclusive wall coordinates, a 9x9 hall


class Template:
    def __init__(self):
        self.blocks = {}  # (x, y, z) -> (name, props or None, nbt or None)

    def set(self, x, y, z, name, props=None, nbt=None):
        self.blocks[(x, y, z)] = (name, props, nbt)

    def fill(self, x1, y1, z1, x2, y2, z2, name, props=None):
        for x in range(x1, x2 + 1):
            for y in range(y1, y2 + 1):
                for z in range(z1, z2 + 1):
                    self.set(x, y, z, name, props)


# Sentinels spawned here drop less than the ones in the open, so that the
# spawners are not a farm. Other mobs keep their normal loot tables.
OUTPOST_SENTINEL_LOOT = "chronodawn:entities/clockwork_sentinel_outpost"


def entity_nbt(mob):
    entity = tag.Compound({"id": tag.String(mob)})
    if mob == "chronodawn:clockwork_sentinel":
        entity["DeathLootTable"] = tag.String(OUTPOST_SENTINEL_LOOT)
    return entity


def spawner_nbt(mobs, max_nearby):
    potentials = tag.List[tag.Compound]([
        tag.Compound({
            "weight": tag.Int(weight),
            "data": tag.Compound({
                "entity": entity_nbt(mob)}),
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


def build():
    t = Template()
    cx = cz = CENTER

    # Foundation and pad floor, only inside the disc.
    for x in range(SIZE):
        for z in range(SIZE):
            r = math.hypot(x - cx, z - cz)
            if r > RADIUS:
                continue
            t.set(x, 0, z, FOUNDATION)
            if r >= 13.2:
                floor = TRIM
            elif 10.3 <= r <= 11.3:
                floor = INLAY
            else:
                floor = FLOOR
            t.set(x, 1, z, floor)

    # White axis paths from the hall doors (north, south, west) to the rim.
    for d in range(HALL_MAX - cx + 1, int(RADIUS) + 1):
        for w in (-1, 0, 1):
            for x, z in ((cx + w, cz - d), (cx + w, cz + d), (cx - d, cz + w)):
                if math.hypot(x - cx, z - cz) <= RADIUS and (x, 1, z) in t.blocks:
                    t.set(x, 1, z, FLOOR_ALT)

    # Sea lanterns set into the floor at the eight compass points.
    for k in range(8):
        a = math.radians(45 * k)
        x = round(cx + 12 * math.cos(a))
        z = round(cz + 12 * math.sin(a))
        t.set(x, 1, z, LAMP)

    # Hall: a two-storey tower. Ground floor y 2..6, upper floor at y 7, upper hall y 8..11, roof at y 12.
    ROOF_Y = 12
    for x in range(HALL_MIN, HALL_MAX + 1):
        for z in range(HALL_MIN, HALL_MAX + 1):
            inside = HALL_MIN < x < HALL_MAX and HALL_MIN < z < HALL_MAX
            if inside:
                checker = FLOOR if (x + z) % 2 == 0 else FLOOR_ALT
                t.set(x, 1, z, checker)
                for y in range(2, ROOF_Y):
                    t.set(x, y, z, AIR)
            else:
                for y in range(2, ROOF_Y):
                    t.set(x, y, z, WALL)
            t.set(x, ROOF_Y, z, ROOF)
    for corner in ((HALL_MIN, HALL_MIN), (HALL_MIN, HALL_MAX),
                   (HALL_MAX, HALL_MIN), (HALL_MAX, HALL_MAX)):
        for y in range(2, ROOF_Y):
            t.set(corner[0], y, corner[1], PILLAR, {"axis": "y"})
        t.set(corner[0], ROOF_Y + 1, corner[1], ROD, {"facing": "up"})
    span = range(HALL_MIN + 1, HALL_MAX)
    for a in span:
        # A 3 wide door in the middle of the north, south and west walls, windows 3 blocks from its centre
        # on both storeys.
        is_door = abs(a - cx) <= 1
        is_window = abs(a - cx) == 3
        for y in range(2, ROOF_Y):
            # The east wall has no door: the stair to the upper floor stands against it.
            for pos in ((a, HALL_MIN), (a, HALL_MAX), (HALL_MIN, a)):
                if is_door and y <= 4:
                    t.set(pos[0], y, pos[1], AIR)
                elif is_window and (3 <= y <= 5 or 9 <= y <= 10):
                    t.set(pos[0], y, pos[1], GLASS)
            if is_window and (3 <= y <= 5 or 9 <= y <= 10):
                t.set(HALL_MAX, y, a, GLASS)
    # The trim marks the doorway sides.
    for a in (cx - 2, cx + 2):
        for y in range(2, 5):
            for pos in ((a, HALL_MIN), (a, HALL_MAX), (HALL_MIN, a)):
                t.set(pos[0], y, pos[1], TRIM)

    # Upper floor: a 2 wide walkway around a 3x3 well, open above the stair column.
    for x in range(HALL_MIN + 1, HALL_MAX):
        for z in range(HALL_MIN + 1, HALL_MAX):
            in_well = abs(x - cx) <= 1 and abs(z - cz) <= 1
            stair_column = x == HALL_MAX - 1 and HALL_MIN + 2 <= z <= HALL_MAX - 1
            if not in_well and not stair_column:
                t.set(x, 7, z, FLOOR_ALT if (x + z) % 2 else FLOOR)
    # Stair of full blocks along the east wall, one block higher per step going north.
    for step, z in enumerate(range(HALL_MAX - 1, HALL_MIN + 1, -1), start=1):
        for y in range(2, 2 + step):
            t.set(HALL_MAX - 1, y, z, WALL)

    # Roof lamps and the antenna mast.
    for x, z in ((cx - 2, cz), (cx + 2, cz), (cx, cz - 2), (cx, cz + 2)):
        t.set(x, ROOF_Y, z, LAMP)
    for y in range(ROOF_Y + 1, HEIGHT - 1):
        t.set(cx, y, cz, PILLAR, {"axis": "y"})
    t.set(cx, HEIGHT - 1, cz, ROD, {"facing": "up"})

    # Hall interior: the hall spawner on a plinth, with four lamps in the floor.
    t.set(cx, 1, cz, TRIM)
    t.set(cx, 2, cz, SPAWNER, None, spawner_nbt(HALL_MOBS, 6))
    for dx, dz in ((-2, -2), (2, -2), (-2, 2), (2, 2)):
        t.set(cx + dx, 1, cz + dz, LAMP)
    # Two reward chests on the upper floor, in the west corners, facing the well.
    for z in (HALL_MIN + 1, HALL_MAX - 1):
        t.set(HALL_MIN + 1, 8, z, CHEST,
              {"facing": "east", "type": "single", "waterlogged": "false"},
              tag.Compound({"id": tag.String(CHEST), "Items": tag.List[tag.Compound]([])}))

    # Docking bays: a black pad with a spawner, under a canopy held by four tall pylons.
    for bx, bz in BAY_CENTERS:
        for dx in (-1, 0, 1):
            for dz in (-1, 0, 1):
                t.set(bx + dx, 1, bz + dz, TRIM)
                t.set(bx + dx, 9, bz + dz, ROOF)
                for y in range(2, 9):
                    t.set(bx + dx, y, bz + dz, AIR)
        t.set(bx, 2, bz, SPAWNER, None, spawner_nbt(BAY_MOBS, 8))
        for dx, dz in ((-1, -1), (1, -1), (-1, 1), (1, 1)):
            for y in range(2, 9):
                t.set(bx + dx, y, bz + dz, PILLAR, {"axis": "y"})
        t.set(bx, 9, bz, LAMP)
        for y in (10, 11):
            t.set(bx, y, bz, PILLAR, {"axis": "y"})
        t.set(bx, 12, bz, ROD, {"facing": "up"})

    return t


def save(t, path):
    palette_index = {}
    palette = []
    blocks = []
    for (x, y, z), (name, props, nbt) in sorted(t.blocks.items(), key=lambda kv: (kv[0][1], kv[0][2], kv[0][0])):
        key = (name, tuple(sorted((props or {}).items())))
        if key not in palette_index:
            palette_index[key] = len(palette)
            entry = tag.Compound({"Name": tag.String(name)})
            if props:
                entry["Properties"] = tag.Compound({k: tag.String(v) for k, v in props.items()})
            palette.append(entry)
        block = tag.Compound({
            "pos": tag.List[tag.Int]([tag.Int(x), tag.Int(y), tag.Int(z)]),
            "state": tag.Int(palette_index[key]),
        })
        if nbt is not None:
            block["nbt"] = nbt
        blocks.append(block)

    root = tag.Compound({
        "DataVersion": tag.Int(DATA_VERSION),
        "size": tag.List[tag.Int]([tag.Int(SIZE), tag.Int(HEIGHT), tag.Int(SIZE)]),
        "palette": tag.List[tag.Compound](palette),
        "blocks": tag.List[tag.Compound](blocks),
        "entities": tag.List[tag.Compound]([]),
    })
    path = os.path.normpath(path)
    nbtlib.File(root, gzipped=True).save(path)
    return path, len(blocks), len(palette)


if __name__ == "__main__":
    out = sys.argv[1] if len(sys.argv) > 1 else OUT_PATH
    p, n, k = save(build(), out)
    print(f"wrote {p}: {n} blocks, {k} palette entries")
