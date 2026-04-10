#!/usr/bin/env python3
"""Re-base an existing ore texture from vanilla stone onto a custom base (e.g. temporal_stone).

Use case: an ore block's 16x16 texture was drawn as <vanilla stone> + <ore overlay>.
You now want the same ore to sit on a different stone (e.g. chronodawn:block/temporal_stone)
without creating a new block. This script applies a per-pixel delta from the
original stone to the new base, preserving both the ore color AND the shading /
anti-aliased edges:

  result = clamp(base + (ore - vanilla_stone))

This avoids the pitfall of threshold-based masking, where edge-of-overlay pixels
(grayish anti-aliased shading) leak the original stone's gray tone into the result.

Requirements:
  - Pillow (pip install Pillow)
  - A copy of vanilla stone.png (extract from minecraft-client.jar, e.g.
    unzip -j ~/.gradle/caches/fabric-loom/1.21.1/minecraft-client.jar \
      assets/minecraft/textures/block/stone.png -d /tmp/)

Usage:
  python3 scripts/composite_ore_on_custom_stone.py \
    --vanilla-stone /tmp/stone.png \
    --base common/shared/src/main/resources/assets/chronodawn/textures/block/temporal_stone.png \
    --ore  common/shared/src/main/resources/assets/chronodawn/textures/block/time_crystal_ore.png \
    --ore  common/shared/src/main/resources/assets/chronodawn/textures/block/clockstone_ore.png
"""
import argparse
import sys
from pathlib import Path

try:
    from PIL import Image
except ImportError:
    sys.exit("Pillow is required: pip install Pillow")


def _clamp(v: int) -> int:
    return 0 if v < 0 else 255 if v > 255 else v


def composite(ore_path: Path, vanilla_stone: Image.Image, base: Image.Image) -> int:
    ore = Image.open(ore_path).convert("RGBA")
    w, h = ore.size
    if (w, h) != vanilla_stone.size or (w, h) != base.size:
        raise SystemExit(
            f"size mismatch: ore={ore.size} vanilla_stone={vanilla_stone.size} base={base.size}"
        )

    out = Image.new("RGBA", (w, h))
    changed = 0
    for y in range(h):
        for x in range(w):
            or_, og, ob, oa = ore.getpixel((x, y))
            sr, sg, sb = vanilla_stone.getpixel((x, y))
            br, bg, bb, ba = base.getpixel((x, y))
            nr = _clamp(br + (or_ - sr))
            ng = _clamp(bg + (og - sg))
            nb = _clamp(bb + (ob - sb))
            out.putpixel((x, y), (nr, ng, nb, oa if oa else ba))
            if (or_, og, ob) != (sr, sg, sb):
                changed += 1

    out.save(ore_path)
    return changed


def main() -> None:
    ap = argparse.ArgumentParser(description=__doc__, formatter_class=argparse.RawDescriptionHelpFormatter)
    ap.add_argument("--vanilla-stone", required=True, type=Path, help="path to vanilla stone.png")
    ap.add_argument("--base", required=True, type=Path, help="path to new base texture (e.g. temporal_stone.png)")
    ap.add_argument("--ore", required=True, type=Path, action="append", help="ore texture to re-base (repeatable)")
    args = ap.parse_args()

    vanilla_stone = Image.open(args.vanilla_stone).convert("RGB")
    base = Image.open(args.base).convert("RGBA")

    for ore_path in args.ore:
        changed = composite(ore_path, vanilla_stone, base)
        total = vanilla_stone.size[0] * vanilla_stone.size[1]
        print(f"{ore_path.name}: delta_pixels={changed}/{total}")


if __name__ == "__main__":
    main()
