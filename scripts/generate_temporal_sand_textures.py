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
    """Near-white pale-blue, very fine pixel grain."""
    rng = random.Random(1001)
    base = (234, 240, 247)
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
    """Slightly darker pale-blue with coarse pebble clumps."""
    rng = random.Random(2002)
    base = (208, 220, 234)
    img = Image.new("RGBA", (16, 16), (*base, 255))
    px = img.load()
    # Draw ~6 pebbles: 2-3 pixel blobs with varying brightness
    for _ in range(6):
        cx = rng.randint(1, 14)
        cy = rng.randint(1, 14)
        brightness = rng.randint(-30, 28)
        radius = rng.choice([1, 1, 2])
        for dy in range(-radius, radius + 1):
            for dx in range(-radius, radius + 1):
                if dx * dx + dy * dy > radius * radius:
                    continue
                nx, ny = cx + dx, cy + dy
                if 0 <= nx < 16 and 0 <= ny < 16:
                    px[nx, ny] = tint(base, brightness + rng.randint(-4, 4))
    # Fine over-noise so the background isn't flat
    for y in range(16):
        for x in range(16):
            r, g, b, a = px[x, y]
            d = rng.randint(-5, 5)
            px[x, y] = (clamp(r + d), clamp(g + d), clamp(b + d), a)
    # A few very dark pebble cores for contrast
    for _ in range(4):
        x = rng.randint(0, 15)
        y = rng.randint(0, 15)
        r, g, b, a = px[x, y]
        px[x, y] = (clamp(r - 40), clamp(g - 40), clamp(b - 40), a)
    return img


def make_sandstone_side() -> Image.Image:
    """Sand-toned block with two subtle horizontal banding lines."""
    rng = random.Random(3003)
    base = (236, 239, 244)
    img = Image.new("RGBA", (16, 16), (*base, 255))
    px = img.load()
    # Faint horizontal bands at y=4 and y=11
    for y in range(16):
        band = -8 if y in (4, 11) else 0
        for x in range(16):
            d = rng.randint(-4, 4) + band
            px[x, y] = tint(base, d)
    return img


def make_sandstone_top() -> Image.Image:
    """Sand-toned block with a slight inset square frame."""
    rng = random.Random(4004)
    base = (240, 243, 247)
    img = Image.new("RGBA", (16, 16), (*base, 255))
    px = img.load()
    for y in range(16):
        for x in range(16):
            edge = x == 0 or y == 0 or x == 15 or y == 15
            inset = x == 2 or y == 2 or x == 13 or y == 13
            band = -6 if edge else (-4 if inset else 0)
            d = rng.randint(-3, 3) + band
            px[x, y] = tint(base, d)
    return img


def make_sandstone_bottom() -> Image.Image:
    """Uniform sand-toned base with subtle noise."""
    rng = random.Random(5005)
    base = (226, 232, 241)
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
