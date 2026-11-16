#!/usr/bin/env python3
"""Remove the air outside the building from structure templates.

A structure block saves every position in its box, so the air around a building
is stored as explicit minecraft:air. A rigid template places that air over the
terrain, and on a slope the high side is cut away in the shape of the box.

A position missing from the template's block list is skipped on placement, the
same as a structure_void block when the template is saved. This script removes:

- the air above the topmost covering block of each column (the space outside
  the building and above its roof), and
- the air under a high overhang that opens onto a side of the box, such as the
  space under the wide top of a tower, where the covering block is more than
  LOW_COVER blocks above.

Air under a low cover stops the removal, so doorways and passages keep the
rooms behind them. A courtyard open to the sky is removed, but the removal does
not spread from it into the rooms that open onto it. Rooms and passages keep
their air, so the terrain does not fill the building. Air along a wall under a
low eave or trim is kept too.

Vines and leaves do not count as covering blocks: they hang over open ground,
and the air under them would otherwise be cut into the terrain. Neither does a
block that a structure processor replaces with air (ReplaceWith: minecraft:air,
such as the boss room boundary marker).

StructureTemplateTest checks that no air is left above the topmost covering
block for the structures listed there. Run this script again after saving such
a structure from a structure block.

Requirements: nbtlib (pip install nbtlib)

Usage:
  python3 scripts/strip_exterior_air.py \
    common/shared-1.21.1+/src/main/resources/data/chronodawn/structure/forgotten_library.nbt
"""
import sys
from collections import deque

import nbtlib

AIR = "minecraft:air"

# The tallest opening into a room, in blocks, such as the 5 high arches of the
# Desert Clock Tower. Air whose covering block is at most this far above is
# treated as inside the building.
LOW_COVER = 5

NEIGHBOURS = ((1, 0, 0), (-1, 0, 0), (0, 1, 0), (0, -1, 0), (0, 0, 1), (0, 0, -1))


def is_covering(name, block):
    if name == AIR or "vine" in name or name.endswith("_leaves"):
        return False
    data = block.get("nbt")
    return data is None or str(data.get("ReplaceWith", "")) != AIR


def strip(path):
    structure = nbtlib.load(path)
    if "palette" not in structure:
        sys.exit(f"{path}: templates with several palettes are not supported")
    names = [str(state["Name"]) for state in structure["palette"]]
    air = set()
    covers = {}
    for block in structure["blocks"]:
        pos = tuple(int(v) for v in block["pos"])
        name = names[int(block["state"])]
        if name == AIR:
            air.add(pos)
        if is_covering(name, block):
            covers.setdefault((pos[0], pos[2]), []).append(pos[1])

    def cover_height(pos):
        above = [y - pos[1] for y in covers.get((pos[0], pos[2]), ()) if y > pos[1]]
        return min(above) if above else None

    size = [int(v) for v in structure["size"]]
    above_roof = {pos for pos in air if cover_height(pos) is None}
    # Start the overhang removal only from the sides of the box. Starting from a
    # courtyard open to the sky would reach the rooms that open onto it.
    under_overhang = {
        pos for pos in air
        if pos not in above_roof and cover_height(pos) > LOW_COVER
        and (pos[0] in (0, size[0] - 1) or pos[2] in (0, size[2] - 1))
    }
    queue = deque(under_overhang)
    while queue:
        x, y, z = queue.popleft()
        for dx, dy, dz in NEIGHBOURS:
            pos = (x + dx, y + dy, z + dz)
            if pos in air and pos not in above_roof and pos not in under_overhang \
                    and cover_height(pos) > LOW_COVER:
                under_overhang.add(pos)
                queue.append(pos)

    exterior = above_roof | under_overhang

    kept = [block for block in structure["blocks"] if tuple(int(v) for v in block["pos"]) not in exterior]
    structure["blocks"] = nbtlib.tag.List[nbtlib.tag.Compound](kept)
    structure.save()
    print(f"{path}: removed {len(exterior)} air blocks")


if __name__ == "__main__":
    if len(sys.argv) < 2:
        sys.exit(__doc__)
    for arg in sys.argv[1:]:
        strip(arg)
