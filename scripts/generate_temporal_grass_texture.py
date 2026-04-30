#!/usr/bin/env python3
"""Generate the Temporal Grass block texture from vanilla short_grass.png.

The texture is baked at the Chrono Dawn plains grass color
``0x5B8AC4`` (R=91, G=138, B=196). At runtime ``TemporalPlantColorProvider``
applies a biome-weighted tint that collapses to a no-op when the biome's
grass color equals this baseline, so plains appearance matches the baked
texture exactly.

This is the same per-channel multiply that produces ``temporal_fern.png``
from vanilla ``fern.png``: empirically the average pixel ratio of the
existing ``temporal_fern.png`` to ``fern.png`` matches
``(91/255, 138/255, 196/255)``.

Prerequisites:
  - Pillow (``python3 -m pip install --user Pillow``)
  - A locally-cached Minecraft client jar from the Fabric Loom cache
    (auto-populated by gradle on first runClient).

Usage:
  python3 scripts/generate_temporal_grass_texture.py
  # or specify a custom jar (any 1.21.x client jar works since
  # short_grass.png has been stable across recent versions):
  python3 scripts/generate_temporal_grass_texture.py --jar /path/to/client.jar
"""
import argparse
import sys
import tempfile
import zipfile
from pathlib import Path

from PIL import Image


# Chrono Dawn plains grass_color baseline. Must match
# TemporalPlantColorProvider.BASELINE.
BASELINE = (0x5B, 0x8A, 0xC4)

REPO_ROOT = Path(__file__).resolve().parent.parent
TEXTURE_DEST = (
    REPO_ROOT
    / "common/shared/src/main/resources/assets/chronodawn/textures/block/temporal_grass.png"
)

VANILLA_ENTRY = "assets/minecraft/textures/block/short_grass.png"

DEFAULT_JAR_CANDIDATES = [
    Path.home() / ".gradle/caches/fabric-loom/1.21.11/minecraft-client.jar",
    Path.home() / ".gradle/caches/fabric-loom/1.21.10/minecraft-client.jar",
    Path.home() / ".gradle/caches/fabric-loom/1.21.1/minecraft-client.jar",
]


def bake_baseline(src: Path, dest: Path) -> None:
    img = Image.open(src).convert("RGBA")
    pixels = img.load()
    rb, gb, bb = BASELINE
    for y in range(img.height):
        for x in range(img.width):
            r, g, b, a = pixels[x, y]
            if a == 0:
                continue
            nr = int(round(r * rb / 255.0))
            ng = int(round(g * gb / 255.0))
            nb = int(round(b * bb / 255.0))
            pixels[x, y] = (nr, ng, nb, a)
    dest.parent.mkdir(parents=True, exist_ok=True)
    img.save(dest, "PNG")


def main() -> int:
    parser = argparse.ArgumentParser(description=__doc__.split("\n", 1)[0])
    parser.add_argument(
        "--jar",
        type=Path,
        default=None,
        help="Path to vanilla Minecraft client jar. If omitted, the first "
             "existing path among the default candidates is used.",
    )
    args = parser.parse_args()

    jar = args.jar
    if jar is None:
        for cand in DEFAULT_JAR_CANDIDATES:
            if cand.is_file():
                jar = cand
                break
    if jar is None or not jar.is_file():
        print(
            "ERROR: vanilla client jar not found. Run a runClient task once "
            "to populate the Fabric Loom cache, or pass --jar.",
            file=sys.stderr,
        )
        return 2

    with tempfile.TemporaryDirectory() as tmp:
        tmp_dir = Path(tmp)
        out = tmp_dir / "short_grass.png"
        with zipfile.ZipFile(jar) as zf, zf.open(VANILLA_ENTRY) as src_in, out.open("wb") as dst_out:
            dst_out.write(src_in.read())
        bake_baseline(out, TEXTURE_DEST)
        sz = Image.open(TEXTURE_DEST).size
        print(f"  baked {VANILLA_ENTRY} x #5B8AC4 -> {TEXTURE_DEST.relative_to(REPO_ROOT)} ({sz[0]}x{sz[1]})")

    return 0


if __name__ == "__main__":
    sys.exit(main())
