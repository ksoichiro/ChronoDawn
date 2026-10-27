#!/usr/bin/env python3
"""Draw the Cairn Stone and Cairn Keystone item textures from scratch.

Every pixel is computed here, so no vanilla or existing texture is copied or tinted.
The output is deterministic (no randomness). Requires Pillow.

Usage (from the repository root):
    python3 -I scripts/draw_cairn_stone_textures.py
"""
import math
import os

from PIL import Image

SIZE = 16
OUT_DIR = os.path.join(
    os.path.dirname(os.path.abspath(__file__)),
    "..", "common", "shared", "src", "main", "resources",
    "assets", "chronodawn", "textures", "item",
)

OUTLINE = (0x4A, 0x4A, 0x50, 255)
STONE = (0x8C, 0x8C, 0x90, 255)
STONE_LIGHT = (0x9C, 0x9C, 0xA0, 255)
STONE_DARK = (0x7A, 0x7A, 0x80, 255)

# The engraving colors are lighter than the stone so the carving reads at 16x16.
ENGRAVING = {
    "cairn_stone_dawn": (0xE8, 0xB8, 0x80, 255),
    "cairn_stone_noon": (0xF4, 0xE4, 0xA0, 255),
    "cairn_stone_dusk": (0xC0, 0x98, 0xD8, 255),
    "cairn_stone_midnight": (0x90, 0xA8, 0xE0, 255),
    "cairn_keystone": (0xF0, 0xE0, 0xA8, 255),
}

# Stone silhouette: a 14x13 block with the corners rounded off.
STONE_X0, STONE_X1 = 1, 14
STONE_Y0, STONE_Y1 = 2, 14
CORNERS = {(1, 2), (14, 2), (1, 14), (14, 14), (2, 2), (13, 2)}


def stone_mask():
    mask = set()
    for y in range(STONE_Y0, STONE_Y1 + 1):
        for x in range(STONE_X0, STONE_X1 + 1):
            if (x, y) not in CORNERS:
                mask.add((x, y))
    return mask


def draw_stone():
    mask = stone_mask()
    image = Image.new("RGBA", (SIZE, SIZE), (0, 0, 0, 0))
    for (x, y) in mask:
        edge = any((x + dx, y + dy) not in mask for dx, dy in ((1, 0), (-1, 0), (0, 1), (0, -1)))
        if edge:
            color = OUTLINE
        elif y <= 4 or x <= 3:
            color = STONE_LIGHT
        elif y >= 12 or x >= 12:
            color = STONE_DARK
        else:
            color = STONE
        image.putpixel((x, y), color)
    return image, mask


def disc(cx, cy, radius, keep=lambda x, y: True):
    """Pixels whose centers lie within radius of (cx, cy), in pixel-center coordinates."""
    return {
        (x, y)
        for y in range(SIZE)
        for x in range(SIZE)
        if math.hypot(x + 0.5 - cx, y + 0.5 - cy) <= radius and keep(x, y)
    }


def engraving(name):
    if name == "cairn_stone_dawn":
        # Half disc rising over a horizon line.
        line = {(x, 10) for x in range(4, 12)}
        return line | disc(8, 10, 3.4, keep=lambda x, y: y < 10)
    if name == "cairn_stone_noon":
        # Full disc with one two-pixel ray per side.
        rays = {(7, 4), (8, 4), (7, 11), (8, 11), (4, 7), (4, 8), (11, 7), (11, 8)}
        return rays | disc(8, 8, 2.3)
    if name == "cairn_stone_dusk":
        # Half disc sinking into the horizon line, with rays shining below it.
        line = {(x, 7) for x in range(4, 12)}
        rays = {(5, 9), (7, 9), (8, 9), (10, 9), (6, 11), (9, 11)}
        return line | rays | disc(8, 7, 3.4, keep=lambda x, y: y < 7)
    if name == "cairn_stone_midnight":
        # Crescent: a disc with a shifted disc cut out of it.
        return disc(7.5, 8.5, 3.8) - disc(9.5, 7.5, 3.2)
    if name == "cairn_keystone":
        # Small four-pointed star.
        star = {(8, y) for y in range(5, 12)} | {(x, 8) for x in range(5, 12)}
        return star | {(7, 7), (9, 7), (7, 9), (9, 9)}
    raise ValueError(name)


def main():
    os.makedirs(OUT_DIR, exist_ok=True)
    for name, color in ENGRAVING.items():
        image, mask = draw_stone()
        for (x, y) in sorted(engraving(name)):
            # Keep the engraving inside the stone's outline.
            if (x, y) in mask and image.getpixel((x, y)) != OUTLINE:
                image.putpixel((x, y), color)
        image.save(os.path.join(OUT_DIR, name + ".png"))


if __name__ == "__main__":
    main()
