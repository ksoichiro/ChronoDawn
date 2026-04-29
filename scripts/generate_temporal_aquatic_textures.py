#!/usr/bin/env python3
"""Generate Temporal Aquatic Plant textures from vanilla baselines.

Extracts vanilla 1.21.11 kelp / kelp_plant / seagrass / tall_seagrass /
dried_kelp PNGs (and their .mcmeta animation sidecars) from the locally
cached Minecraft client jar, then hue-shifts each texture by +100 deg in
HSV space (preserving saturation, value, and alpha) into the ChronoDawn
Temporal teal palette (~210 deg). The same shift is applied to the item
icons. tall_temporal_seagrass item icon is the first 16x16 frame of the
new tall_temporal_seagrass_top texture, since vanilla has no separate
tall_seagrass item PNG.

Animation parity: the .mcmeta sidecars are copied unchanged so in-world
plants sway with the same cadence as vanilla.

Deterministic output: re-running the script with the same vanilla jar
yields bit-identical PNGs.

Prerequisites:
  - Pillow (`python3 -m pip install --user Pillow`)
  - Vanilla Minecraft 1.21.11 client jar in the local NeoForm cache
    (auto-downloaded by gradle on first build / runClient).

Usage:
  python3 scripts/generate_temporal_aquatic_textures.py
  # or specify a custom jar:
  python3 scripts/generate_temporal_aquatic_textures.py --jar /path/to/client.jar
"""
import argparse
import colorsys
import shutil
import sys
import tempfile
import zipfile
from pathlib import Path

from PIL import Image


HUE_SHIFT_DEG = 100.0
HUE_SHIFT = HUE_SHIFT_DEG / 360.0  # 0..1 fraction for colorsys

REPO_ROOT = Path(__file__).resolve().parent.parent
TEXTURE_ROOT = (
    REPO_ROOT / "common/shared/src/main/resources/assets/chronodawn/textures"
)

DEFAULT_JAR = (
    Path.home()
    / ".gradle/caches/neoformruntime/artifacts/minecraft_1.21.11_client.jar"
)

# Vanilla PNGs to hue-shift. (jar_path, dest_under_TEXTURE_ROOT)
HUE_SHIFTED = [
    # Block textures (animated; .mcmeta sidecars are copied alongside).
    ("assets/minecraft/textures/block/kelp.png",
     "block/temporal_kelp.png"),
    ("assets/minecraft/textures/block/kelp_plant.png",
     "block/temporal_kelp_plant.png"),
    ("assets/minecraft/textures/block/seagrass.png",
     "block/temporal_seagrass.png"),
    ("assets/minecraft/textures/block/tall_seagrass_top.png",
     "block/tall_temporal_seagrass_top.png"),
    ("assets/minecraft/textures/block/tall_seagrass_bottom.png",
     "block/tall_temporal_seagrass_bottom.png"),
    # Item textures (single-frame icons).
    ("assets/minecraft/textures/item/kelp.png",
     "item/temporal_kelp.png"),
    ("assets/minecraft/textures/item/seagrass.png",
     "item/temporal_seagrass.png"),
    ("assets/minecraft/textures/item/dried_kelp.png",
     "item/dried_temporal_kelp.png"),
    # Lumen Polyp: vanilla sea_pickle has no separate item PNG, so the
    # block texture is reused for the item icon. Sea pickle is not animated,
    # hence no .mcmeta entry below.
    ("assets/minecraft/textures/block/sea_pickle.png",
     "block/lumen_polyp.png"),
    ("assets/minecraft/textures/block/sea_pickle.png",
     "item/lumen_polyp.png"),
]

# .mcmeta animation sidecars to copy unchanged.
MCMETA_COPIES = [
    ("assets/minecraft/textures/block/kelp.png.mcmeta",
     "block/temporal_kelp.png.mcmeta"),
    ("assets/minecraft/textures/block/kelp_plant.png.mcmeta",
     "block/temporal_kelp_plant.png.mcmeta"),
    ("assets/minecraft/textures/block/seagrass.png.mcmeta",
     "block/temporal_seagrass.png.mcmeta"),
    ("assets/minecraft/textures/block/tall_seagrass_top.png.mcmeta",
     "block/tall_temporal_seagrass_top.png.mcmeta"),
    ("assets/minecraft/textures/block/tall_seagrass_bottom.png.mcmeta",
     "block/tall_temporal_seagrass_bottom.png.mcmeta"),
]

# Derived item: crop the first 16x16 frame of the top block texture.
TALL_SEAGRASS_ITEM_FROM_BLOCK_TOP = (
    "block/tall_temporal_seagrass_top.png",
    "item/tall_temporal_seagrass.png",
)


def hue_shift_pixel(r: int, g: int, b: int) -> tuple[int, int, int]:
    h, s, v = colorsys.rgb_to_hsv(r / 255.0, g / 255.0, b / 255.0)
    h = (h + HUE_SHIFT) % 1.0
    nr, ng, nb = colorsys.hsv_to_rgb(h, s, v)
    return int(round(nr * 255)), int(round(ng * 255)), int(round(nb * 255))


def transform_image(src: Path, dest: Path) -> None:
    img = Image.open(src).convert("RGBA")
    pixels = img.load()
    for y in range(img.height):
        for x in range(img.width):
            r, g, b, a = pixels[x, y]
            if a == 0:
                continue  # leave fully-transparent pixels alone
            nr, ng, nb = hue_shift_pixel(r, g, b)
            pixels[x, y] = (nr, ng, nb, a)
    dest.parent.mkdir(parents=True, exist_ok=True)
    img.save(dest, "PNG")


def crop_first_frame(src: Path, dest: Path, size: int = 16) -> None:
    img = Image.open(src).convert("RGBA")
    img.crop((0, 0, size, size)).save(dest, "PNG")


def main() -> int:
    parser = argparse.ArgumentParser(description=__doc__.split("\n", 1)[0])
    parser.add_argument(
        "--jar",
        type=Path,
        default=DEFAULT_JAR,
        help=f"Path to vanilla Minecraft client jar (default: {DEFAULT_JAR})",
    )
    args = parser.parse_args()

    if not args.jar.is_file():
        print(f"ERROR: vanilla jar not found at {args.jar}", file=sys.stderr)
        print(
            "Run `./gradlew runClientFabric1_21_11` once to populate the cache, "
            "or pass --jar.",
            file=sys.stderr,
        )
        return 2

    with tempfile.TemporaryDirectory() as tmp:
        tmp_dir = Path(tmp)

        # Extract everything we need in one pass, preserving the in-jar
        # directory structure so block/kelp.png and item/kelp.png don't collide.
        wanted = {entry for entry, _ in HUE_SHIFTED} | {
            entry for entry, _ in MCMETA_COPIES
        }
        with zipfile.ZipFile(args.jar) as zf:
            for entry in wanted:
                out_path = tmp_dir / entry  # preserves block/ vs item/ subdir
                out_path.parent.mkdir(parents=True, exist_ok=True)
                with zf.open(entry) as src_in, out_path.open("wb") as out:
                    shutil.copyfileobj(src_in, out)

        ok = True
        for entry, dest_rel in HUE_SHIFTED:
            src = tmp_dir / entry
            dest = TEXTURE_ROOT / dest_rel
            if not src.is_file():
                print(f"  MISSING {entry}", file=sys.stderr)
                ok = False
                continue
            transform_image(src, dest)
            sz = Image.open(dest).size
            print(f"  hue-shift {entry} -> {dest_rel} ({sz[0]}x{sz[1]})")

        for entry, dest_rel in MCMETA_COPIES:
            src = tmp_dir / entry
            dest = TEXTURE_ROOT / dest_rel
            if not src.is_file():
                print(f"  MISSING {entry}", file=sys.stderr)
                ok = False
                continue
            dest.parent.mkdir(parents=True, exist_ok=True)
            shutil.copy2(src, dest)
            print(f"  copy mcmeta {entry} -> {dest_rel}")

        # Derived: tall_temporal_seagrass item from block top first frame.
        block_top_rel, item_rel = TALL_SEAGRASS_ITEM_FROM_BLOCK_TOP
        block_top = TEXTURE_ROOT / block_top_rel
        item_path = TEXTURE_ROOT / item_rel
        crop_first_frame(block_top, item_path)
        print(f"  crop first frame {block_top_rel} -> {item_rel}")

    return 0 if ok else 1


if __name__ == "__main__":
    sys.exit(main())
