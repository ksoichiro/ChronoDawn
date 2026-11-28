#!/usr/bin/env python3
"""Bake weathering into structure templates.

Replaces a share of the exposed Temporal Stone Bricks blocks with cracked and
mossy variants, so large stone structures do not read as one flat material.
Only the palette-visible blocks change, never the layout, so the terrain
preserving air stripping (strip_exterior_air.py) is unaffected.

The choice for a block is a pure function of the template name and the block
position, so running the script again changes nothing: a block that was kept
the first time is kept again. Rates live in RATES below. Edit them and run the
script on the original templates (git checkout the .nbt first) to re-roll.

Only exposed blocks (at least one air neighbour) are replaced. A block buried
inside a wall is never seen, so changing it only costs a palette entry.
Stairs, slabs and walls only have a mossy variant, so they never get cracked.

Requirements: nbtlib (pip install nbtlib)

Usage:
  python3 scripts/weather_structures.py [--dry-run]
"""
import hashlib
import sys
from pathlib import Path

import nbtlib

STRUCTURE_DIR = Path("common/shared-1.21.1+/src/main/resources/data/chronodawn/structure")
AIR = "minecraft:air"
NS = "chronodawn:"

# Per template: (cracked rate, mossy rate, ground moss boost). The ground boost
# multiplies the mossy rate in the lowest GROUND_LAYERS layers, where a ruin
# meets the soil. Templates absent here are left alone on purpose: Ancient
# Ruins and the Guardian Vault are already weathered by hand, Clockwork Depths
# is a copper machine, and the small features have too few bricks to vary.
GROUND_LAYERS = 3
RATES = {
    "desert_clock_tower": (0.08, 0.0, 1),
    "forgotten_library": (0.05, 0.10, 3),
    "master_clock_surface": (0.06, 0.05, 3),
    "master_clock_corridor": (0.06, 0.04, 1),
    "master_clock_stairs": (0.06, 0.04, 1),
    "master_clock_stairs_bottom": (0.06, 0.04, 1),
    "master_clock_boss_room": (0.05, 0.03, 1),
    "phantom_catacombs_entrance": (0.08, 0.0, 1),
    "phantom_catacombs_corridor": (0.08, 0.0, 1),
    "phantom_catacombs_boss_room": (0.06, 0.04, 1),
    "time_keeper_village": (0.10, 0.10, 3),
}
for _i in range(1, 8):
    RATES[f"phantom_catacombs_room_{_i}"] = (0.08, 0.06, 1)
RATES["phantom_catacombs_room_dead_end"] = (0.08, 0.06, 1)

FULL = NS + "temporal_stone_bricks"
CRACKED = NS + "cracked_temporal_stone_bricks"
MOSSY = {
    FULL: NS + "mossy_temporal_stone_bricks",
    FULL + "_stairs": NS + "mossy_temporal_stone_bricks_stairs",
    FULL + "_slab": NS + "mossy_temporal_stone_bricks_slab",
    FULL + "_wall": NS + "mossy_temporal_stone_bricks_wall",
}

# Entropy Crypt is blackstone and has no mossy variant. Only raise its cracked
# share on the full blocks.
BLACKSTONE_RATES = {
    "entropy_crypt_main": 0.06,
    "entropy_crypt_stairs": 0.06,
    "entropy_crypt_entrance": 0.06,
}
BLACKSTONE_FULL = "minecraft:polished_blackstone_bricks"
BLACKSTONE_CRACKED = "minecraft:cracked_polished_blackstone_bricks"

# Sentinel Outpost is a clean white hull that should read as old, not as a
# ruin. Two kinds of stain are baked into its exposed light blocks:
#   - rust streaks: some columns carry a drip of copper that starts at a seeded
#     height and runs down, from Exposed through Weathered to Oxidized.
#   - grime: a low rate of Tuff, Mud and copper near the ground that fades out
#     with height, so the base looks dirtier than the nose.
# No swap target is also a swap source, so running the script again cannot
# weather a block twice. The stair forms keep their facing.
SENTINEL_TEMPLATE = "sentinel_outpost"
SENTINEL_SOURCES = {
    "minecraft:quartz_block", "minecraft:smooth_quartz", "minecraft:white_concrete",
    "minecraft:light_gray_concrete", "minecraft:quartz_stairs",
    "minecraft:smooth_quartz_stairs",
}
SENTINEL_STAIRS = {"minecraft:quartz_stairs", "minecraft:smooth_quartz_stairs"}
STREAK_COLUMN_RATE = 0.22
STREAK_KEEP = 0.6
GRIME_HEIGHT = 25
STREAK_STAGES = ("exposed_cut_copper", "weathered_cut_copper", "oxidized_cut_copper")


def sentinel_roll(*key):
    digest = hashlib.sha256((SENTINEL_TEMPLATE + ":" + ",".join(map(str, key))).encode()).digest()
    return int.from_bytes(digest[:8], "big") / 2**64


def sentinel_target(name, pos, height):
    """The weathered replacement for one exposed block, or None to keep it."""
    if name not in SENTINEL_SOURCES:
        return None
    x, y, z = pos
    target = None
    if sentinel_roll("col", x, z) < STREAK_COLUMN_RATE and sentinel_roll("keep", x, y, z) < STREAK_KEEP:
        start = 25 + int(sentinel_roll("start", x, z) * 40)
        length = 6 + int(sentinel_roll("len", x, z) * 22)
        if start - length <= y <= start:
            t = (start - y) / length
            target = STREAK_STAGES[0] if t < 0.35 else STREAK_STAGES[1] if t < 0.7 else STREAK_STAGES[2]
    if target is None and y < GRIME_HEIGHT:
        fade = 1 - y / height
        if sentinel_roll("grime", x, y, z) < 0.04 + 0.36 * fade ** 2:
            pick = sentinel_roll("pick", x, y, z)
            if fade > 0.6:
                target = "tuff" if pick < 0.45 else "mud_bricks" if pick < 0.75 else "weathered_cut_copper"
            else:
                target = "exposed_cut_copper" if pick < 0.5 else "weathered_cut_copper"
    if target is None:
        return None
    if name in SENTINEL_STAIRS:
        # Tuff Stairs only exist from 1.20.3, so a 1.20.1 world would leave a gap in the
        # staircase. Andesite is the closest grey that every supported version has.
        target = {"mud_bricks": "mud_brick_stairs", "tuff": "andesite_stairs"}.get(target, target + "_stairs")
    return "minecraft:" + target


NEIGHBOURS = ((1, 0, 0), (-1, 0, 0), (0, 1, 0), (0, -1, 0), (0, 0, 1), (0, 0, -1))


def roll(template, pos):
    """A stable value in [0, 1) for one block of one template."""
    digest = hashlib.sha256(f"{template}:{pos[0]},{pos[1]},{pos[2]}".encode()).digest()
    return int.from_bytes(digest[:8], "big") / 2**64


def weather(path, dry_run):
    template = path.stem
    if template not in RATES and template not in BLACKSTONE_RATES and template != SENTINEL_TEMPLATE:
        return
    structure = nbtlib.load(path)
    if "palette" not in structure:
        sys.exit(f"{path}: templates with several palettes are not supported")
    palette = structure["palette"]
    names = [str(state["Name"]) for state in palette]
    air = {
        tuple(int(v) for v in b["pos"])
        for b in structure["blocks"] if names[int(b["state"])] == AIR
    }

    def entry_for(source_index, new_name):
        # Reuse an identical palette entry, or add one that keeps the properties.
        source = palette[source_index]
        for index, state in enumerate(palette):
            if str(state["Name"]) == new_name and state.get("Properties") == source.get("Properties"):
                return index
        state = nbtlib.tag.Compound({"Name": nbtlib.tag.String(new_name)})
        if "Properties" in source:
            state["Properties"] = nbtlib.tag.Compound(dict(source["Properties"]))
        palette.append(state)
        return len(palette) - 1

    cracked_rate, moss_rate, boost = RATES.get(template, (0.0, 0.0, 1))
    changed = {"cracked": 0, "mossy": 0}
    for block in structure["blocks"]:
        pos = tuple(int(v) for v in block["pos"])
        index = int(block["state"])
        name = names[index]
        if "nbt" in block:
            continue
        if not any((pos[0] + dx, pos[1] + dy, pos[2] + dz) in air for dx, dy, dz in NEIGHBOURS):
            continue
        value = roll(template, pos)
        new_name = None
        if template == SENTINEL_TEMPLATE:
            new_name = sentinel_target(name, pos, int(structure["size"][1]))
            if new_name:
                changed["cracked"] += 1
        elif template in BLACKSTONE_RATES:
            if name == BLACKSTONE_FULL and value < BLACKSTONE_RATES[template]:
                new_name = BLACKSTONE_CRACKED
                changed["cracked"] += 1
        elif name in MOSSY:
            rate = moss_rate * (boost if pos[1] < GROUND_LAYERS else 1)
            if name == FULL and value < cracked_rate:
                new_name = CRACKED
                changed["cracked"] += 1
            elif cracked_rate <= value < cracked_rate + rate:
                new_name = MOSSY[name]
                changed["mossy"] += 1
        if new_name:
            block["state"] = nbtlib.tag.Int(entry_for(index, new_name))

    print(f"{template}: cracked {changed['cracked']}, mossy {changed['mossy']}")
    if not dry_run and any(changed.values()):
        structure.save()


if __name__ == "__main__":
    dry = "--dry-run" in sys.argv
    for nbt in sorted(STRUCTURE_DIR.glob("*.nbt")):
        weather(nbt, dry)
