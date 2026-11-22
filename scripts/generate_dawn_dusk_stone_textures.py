#!/usr/bin/env python3
"""Generate the Dawnstone and Duskstone block textures.

Both are original 16x16 textures drawn from seeded noise (no vanilla source
pixels). Stairs, slabs and walls reuse these base textures.

- Dawnstone: rose-pink stone with a granite-like speckle. The palette is
  kept clearly pinker than vanilla granite, which reads brown.
- Duskstone: cool blue-grey stone with an andesite-like fine grain.

Seamless tiling: every noise field is sampled on a 16x16 torus (lattice
coordinates wrap) and every speckle is stamped with wrap-around, so the
left edge continues the right edge and the top edge continues the bottom
edge. Placing blocks side by side therefore shows no block border.

Relief: a tileable height field is shaded with a light from the upper left
(height difference to the lower-right neighbour, also wrapped), which gives
the face soft bumps instead of a flat look.

Deterministic output (seeded random). Re-running the script yields
bit-identical PNGs.

Prerequisites: Pillow (``python3 -m pip install --user Pillow``).

Usage:
  python3 scripts/generate_dawn_dusk_stone_textures.py
  python3 scripts/generate_dawn_dusk_stone_textures.py --preview DIR
  # --preview also writes 3x3 tiled, 8x upscaled previews into DIR
"""
import argparse
import math
import random
from pathlib import Path

from PIL import Image


SIZE = 16
OUT_DIR = Path(__file__).resolve().parent.parent / (
    "common/shared/src/main/resources/assets/chronodawn/textures/block"
)

Color = tuple[int, int, int]


def lattice(rng: random.Random, period: int) -> list[list[float]]:
    return [[rng.random() for _ in range(period)] for _ in range(period)]


def smooth(t: float) -> float:
    return t * t * (3.0 - 2.0 * t)


def periodic_value_noise(grid: list[list[float]], x: float, y: float) -> float:
    """Bilinear value noise whose lattice wraps, so the field tiles."""
    period = len(grid)
    cell = SIZE / period
    gx, gy = x / cell, y / cell
    x0, y0 = int(math.floor(gx)), int(math.floor(gy))
    tx, ty = smooth(gx - x0), smooth(gy - y0)
    x0 %= period
    y0 %= period
    x1, y1 = (x0 + 1) % period, (y0 + 1) % period
    a = grid[y0][x0] + (grid[y0][x1] - grid[y0][x0]) * tx
    b = grid[y1][x0] + (grid[y1][x1] - grid[y1][x0]) * tx
    return a + (b - a) * ty


def fbm(rng: random.Random, octaves: list[tuple[int, float]]) -> list[list[float]]:
    """Sum of tileable noise octaves, normalised to 0..1.

    Each octave is (lattice period, weight). Periods must divide SIZE.
    """
    grids = [(lattice(rng, p), w) for p, w in octaves]
    field = [[0.0] * SIZE for _ in range(SIZE)]
    for y in range(SIZE):
        for x in range(SIZE):
            field[y][x] = sum(
                periodic_value_noise(g, x + 0.5, y + 0.5) * w for g, w in grids
            )
    lo = min(min(r) for r in field)
    hi = max(max(r) for r in field)
    return [[(v - lo) / (hi - lo) for v in row] for row in field]


def shade(height: list[list[float]], strength: float) -> list[list[float]]:
    """Light from the upper left: compare each pixel with its lower-right
    neighbour (wrapped). Positive means lit, negative means in shadow."""
    out = [[0.0] * SIZE for _ in range(SIZE)]
    for y in range(SIZE):
        for x in range(SIZE):
            here = height[y][x]
            lower_right = height[(y + 1) % SIZE][(x + 1) % SIZE]
            upper_left = height[(y - 1) % SIZE][(x - 1) % SIZE]
            out[y][x] = ((here - lower_right) - (upper_left - here) * 0.5) * strength
    return out


def ramp_index(value: float, size: int) -> int:
    return max(0, min(size - 1, int(round(value * (size - 1)))))


def stamp(
    px,
    rng: random.Random,
    color: Color,
    count: int,
    shapes: list[list[tuple[int, int]]],
    occupied: set[tuple[int, int]],
) -> None:
    """Place small specks with wrap-around. A jittered grid keeps them evenly
    spread, and specks never touch each other so they stay readable."""
    cells = int(math.ceil(math.sqrt(count)))
    step = SIZE / cells
    slots = [(cx, cy) for cy in range(cells) for cx in range(cells)]
    rng.shuffle(slots)
    placed = 0
    for cx, cy in slots:
        if placed >= count:
            break
        for _ in range(8):
            ox = int(cx * step + rng.random() * step) % SIZE
            oy = int(cy * step + rng.random() * step) % SIZE
            shape = rng.choice(shapes)
            pts = [((ox + dx) % SIZE, (oy + dy) % SIZE) for dx, dy in shape]
            halo = {
                ((x + hx) % SIZE, (y + hy) % SIZE)
                for x, y in pts
                for hx in (-1, 0, 1)
                for hy in (-1, 0, 1)
            }
            if halo & occupied:
                continue
            for p in pts:
                px[p] = (*color, 255)
            occupied.update(pts)
            placed += 1
            break


def render(
    seed: int,
    ramp: list[Color],
    tone_octaves: list[tuple[int, float]],
    height_octaves: list[tuple[int, float]],
    shade_strength: float,
    grain: float,
    specks: list[tuple[Color, int, list[list[tuple[int, int]]]]],
) -> Image.Image:
    rng = random.Random(seed)
    tone = fbm(rng, tone_octaves)
    height = fbm(rng, height_octaves)
    light = shade(height, shade_strength)
    img = Image.new("RGBA", (SIZE, SIZE))
    px = img.load()
    for y in range(SIZE):
        for x in range(SIZE):
            v = 0.2 + tone[y][x] * 0.55 + light[y][x] + (rng.random() - 0.5) * grain
            px[x, y] = (*ramp[ramp_index(v, len(ramp))], 255)
    occupied: set[tuple[int, int]] = set()
    for color, count, shapes in specks:
        stamp(px, rng, color, count, shapes, occupied)
    return img


DOT = [(0, 0)]
PAIR_H = [(0, 0), (1, 0)]
PAIR_V = [(0, 0), (0, 1)]
DIAG = [(0, 0), (1, 1)]
CLUSTER = [(0, 0), (1, 0), (0, 1)]


def make_dawnstone() -> Image.Image:
    # Rose-pink ramp, dark to light. Hue stays around rose (red with blue
    # slightly above green) so it does not drift into granite's brown.
    ramp = [
        (0x80, 0x4C, 0x58),
        (0x9A, 0x5E, 0x6A),
        (0xB0, 0x71, 0x7B),
        (0xC3, 0x84, 0x8B),
        (0xD1, 0x96, 0x9A),
        (0xDD, 0xA9, 0xA9),
        (0xE8, 0xBD, 0xB9),
    ]
    return render(
        seed=6006,
        ramp=ramp,
        tone_octaves=[(4, 0.6), (8, 0.7), (16, 0.35)],
        height_octaves=[(4, 0.8), (8, 1.0)],
        shade_strength=0.4,
        grain=0.14,
        specks=[
            # Pale cream feldspar flecks, the granite-like speckle.
            ((0xF2, 0xDC, 0xCF), 6, [DOT, DOT, PAIR_H, DIAG]),
            # Deep plum mafic grains.
            ((0x5E, 0x3A, 0x49), 6, [DOT, DOT, PAIR_V]),
            # A few warm amber grains for the "rose-amber" accent.
            ((0xD8, 0x9A, 0x78), 3, [DOT]),
        ],
    )


def make_duskstone() -> Image.Image:
    # Cool blue-grey ramp, matching the hue of the original placeholder.
    ramp = [
        (0x50, 0x57, 0x63),
        (0x5E, 0x66, 0x72),
        (0x6C, 0x74, 0x80),
        (0x79, 0x81, 0x8D),
        (0x86, 0x8E, 0x9A),
        (0x94, 0x9C, 0xA7),
    ]
    return render(
        seed=7007,
        ramp=ramp,
        tone_octaves=[(4, 0.4), (8, 0.6), (16, 0.55)],
        height_octaves=[(4, 0.8), (8, 1.0)],
        shade_strength=0.5,
        grain=0.2,
        specks=[
            # Fine light and dark grains, andesite-like.
            ((0xA8, 0xB0, 0xBB), 8, [DOT]),
            ((0x42, 0x48, 0x53), 8, [DOT]),
        ],
    )


MAPPING = {
    "dawnstone.png": make_dawnstone,
    "duskstone.png": make_duskstone,
}


def tiled_preview(img: Image.Image, tiles: int = 3, scale: int = 8) -> Image.Image:
    sheet = Image.new("RGBA", (SIZE * tiles, SIZE * tiles))
    for ty in range(tiles):
        for tx in range(tiles):
            sheet.paste(img, (tx * SIZE, ty * SIZE))
    return sheet.resize((SIZE * tiles * scale,) * 2, Image.NEAREST)


def main() -> int:
    parser = argparse.ArgumentParser(description=__doc__.splitlines()[0])
    parser.add_argument("--preview", type=Path, help="also write tiled previews here")
    args = parser.parse_args()
    OUT_DIR.mkdir(parents=True, exist_ok=True)
    for name, fn in MAPPING.items():
        img = fn()
        path = OUT_DIR / name
        img.save(path)
        print(f"wrote {path}")
        if args.preview:
            args.preview.mkdir(parents=True, exist_ok=True)
            prev = args.preview / name.replace(".png", "_tiled.png")
            tiled_preview(img).save(prev)
            print(f"wrote {prev}")
    return 0


if __name__ == "__main__":
    raise SystemExit(main())
