#!/usr/bin/env python3
"""Generate Temporal Sand / Gravel / Sandstone textures.

Produces 16x16 pale-ice-blue PNGs that are clearly distinct from Temporal
Dirt (which is a warmer blue-grey). Sand is near-white with fine grain,
Gravel is slightly darker with a coarser pebble pattern, and the Sandstone
faces share the sand tone.

Deterministic output (seeded random). Re-running the script yields bit-
identical PNGs.
"""
import random
import sys
from pathlib import Path

from PIL import Image


OUT_DIR = Path(__file__).resolve().parent.parent / (
    "common/shared/src/main/resources/assets/chronodawn/textures/block"
)


def clamp(v: int) -> int:
    return max(0, min(255, v))


def tint(base: tuple[int, int, int], delta: int) -> tuple[int, int, int, int]:
    return (clamp(base[0] + delta), clamp(base[1] + delta), clamp(base[2] + delta), 255)


def make_sand() -> Image.Image:
    """Clearly blue sand — lighter than Temporal Dirt, but no longer near-white."""
    rng = random.Random(1001)
    base = (145, 188, 232)
    img = Image.new("RGBA", (16, 16), (*base, 255))
    px = img.load()
    for y in range(16):
        for x in range(16):
            # Subtle ±6 brightness noise
            d = rng.randint(-6, 6)
            # Occasional darker speck for texture
            if rng.random() < 0.05:
                d -= rng.randint(4, 10)
            px[x, y] = tint(base, d)
    return img


def make_gravel() -> Image.Image:
    """Darker saturated blue with evenly-distributed small speckles (jittered grid)."""
    rng = random.Random(2002)
    base = (118, 158, 205)
    img = Image.new("RGBA", (16, 16), (*base, 255))
    px = img.load()
    # Jittered grid: 16 speckles on a 4x4 cell partition (each cell is 4x4 pixels).
    # One speckle per cell → uniform coverage, no clustering.
    for cy in range(4):
        for cx in range(4):
            x = cx * 4 + rng.randint(0, 3)
            y = cy * 4 + rng.randint(0, 3)
            brightness = rng.randint(-32, 26)
            radius = rng.choice([0, 0, 0, 1])
            for dy in range(-radius, radius + 1):
                for dx in range(-radius, radius + 1):
                    if abs(dx) + abs(dy) > radius:
                        continue
                    nx, ny = x + dx, y + dy
                    if 0 <= nx < 16 and 0 <= ny < 16:
                        px[nx, ny] = tint(base, brightness + rng.randint(-3, 3))
    # Fine over-noise so the background isn't flat
    for y in range(16):
        for x in range(16):
            r, g, b, a = px[x, y]
            d = rng.randint(-5, 5)
            px[x, y] = (clamp(r + d), clamp(g + d), clamp(b + d), a)
    # Very dark single-pixel cores — also jittered-grid on a 4x4 partition,
    # ~40% chance per cell, for sparse but even high-contrast specks.
    for cy in range(4):
        for cx in range(4):
            if rng.random() >= 0.4:
                continue
            x = cx * 4 + rng.randint(0, 3)
            y = cy * 4 + rng.randint(0, 3)
            r, g, b, a = px[x, y]
            px[x, y] = (clamp(r - 40), clamp(g - 40), clamp(b - 40), a)
    return img


def make_sandstone_side() -> Image.Image:
    """Sand-toned block with two subtle horizontal banding lines."""
    rng = random.Random(3003)
    base = (155, 195, 232)
    img = Image.new("RGBA", (16, 16), (*base, 255))
    px = img.load()
    # Faint horizontal bands at y=4 and y=11
    for y in range(16):
        band = -10 if y in (4, 11) else 0
        for x in range(16):
            d = rng.randint(-4, 4) + band
            px[x, y] = tint(base, d)
    return img


def make_sandstone_top() -> Image.Image:
    """Sand-toned block with a slight inset square frame."""
    rng = random.Random(4004)
    base = (165, 205, 238)
    img = Image.new("RGBA", (16, 16), (*base, 255))
    px = img.load()
    for y in range(16):
        for x in range(16):
            edge = x == 0 or y == 0 or x == 15 or y == 15
            inset = x == 2 or y == 2 or x == 13 or y == 13
            band = -8 if edge else (-5 if inset else 0)
            d = rng.randint(-3, 3) + band
            px[x, y] = tint(base, d)
    return img


def make_sandstone_bottom() -> Image.Image:
    """Uniform sand-toned base with subtle noise."""
    rng = random.Random(5005)
    base = (130, 175, 220)
    img = Image.new("RGBA", (16, 16), (*base, 255))
    px = img.load()
    for y in range(16):
        for x in range(16):
            d = rng.randint(-4, 4)
            px[x, y] = tint(base, d)
    return img


MAPPING = {
    "temporal_sand.png": make_sand,
    "temporal_gravel.png": make_gravel,
    "temporal_sandstone.png": make_sandstone_side,
    "temporal_sandstone_top.png": make_sandstone_top,
    "temporal_sandstone_bottom.png": make_sandstone_bottom,
}


def main() -> int:
    OUT_DIR.mkdir(parents=True, exist_ok=True)
    for name, fn in MAPPING.items():
        path = OUT_DIR / name
        fn().save(path)
        print(f"wrote {path}")
    return 0


if __name__ == "__main__":
    raise SystemExit(main())
