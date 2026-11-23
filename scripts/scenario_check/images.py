"""First-pass image checks and the contact sheet the owner reviews remotely."""

import re
import textwrap
from pathlib import Path

from PIL import Image, ImageDraw, ImageFont, ImageStat

# Minecraft draws a missing texture as #F800F8 and black. Shading keeps red and
# blue close together and green near zero, which mod purples rarely do.
_MISSING_MIN_RB = 110
_MISSING_MAX_G = 40
_MISSING_MAX_RB_GAP = 40
MISSING_TEXTURE_RATIO = 0.005
BLANK_STDDEV = 6.0
# Mean luminance (0-255) below which a shot counts as night or unrendered.
# Scenarios set the time to noon first, so a healthy frame is far above it.
# A shot that is meant to be dark (a cave) lowers it with expect.min_brightness.
MIN_BRIGHTNESS = 15
EXPECT_KEYS = {"min_brightness", "colors"}


def missing_texture_ratio(image: Image.Image) -> float:
    small = image.convert("RGB")
    small.thumbnail((320, 320))
    pixels = list(small.getdata())
    hits = sum(1 for r, g, b in pixels
               if r >= _MISSING_MIN_RB and b >= _MISSING_MIN_RB and g <= _MISSING_MAX_G
               and abs(r - b) <= _MISSING_MAX_RB_GAP)
    return hits / len(pixels) if pixels else 0.0


def looks_blank(image: Image.Image) -> bool:
    """A single-color frame: a loading screen, a black window, or a capture that got nothing."""
    return max(ImageStat.Stat(image.convert("L")).stddev) < BLANK_STDDEV


def mean_brightness(image: Image.Image) -> float:
    """Average luminance, 0 (black) to 255 (white)."""
    return ImageStat.Stat(image.convert("L")).mean[0]


def color_ratio(image: Image.Image, rgb, tolerance: int) -> float:
    """Share of pixels within `tolerance` of `rgb` on every channel."""
    small = image.convert("RGB")
    small.thumbnail((320, 320))
    pixels = list(small.getdata())
    r0, g0, b0 = rgb
    hits = sum(1 for r, g, b in pixels
               if abs(r - r0) <= tolerance and abs(g - g0) <= tolerance and abs(b - b0) <= tolerance)
    return hits / len(pixels) if pixels else 0.0


def _parse_hex(value):
    if not (isinstance(value, str) and re.fullmatch(r"#[0-9a-fA-F]{6}", value)):
        raise ValueError(f"rgb is a '#rrggbb' string, got {value!r}")
    return tuple(int(value[i:i + 2], 16) for i in (1, 3, 5))


def _is_number(value):
    return isinstance(value, (int, float)) and not isinstance(value, bool)


def validate_expect(expect) -> None:
    """Raise ValueError when a shot's "expect" object is malformed."""
    if not isinstance(expect, dict):
        raise ValueError(f"expect is an object, got {type(expect).__name__}")
    unknown = set(expect) - EXPECT_KEYS
    if unknown:
        raise ValueError(f"unknown expect keys {sorted(unknown)}")
    if "min_brightness" in expect and not (_is_number(expect["min_brightness"])
                                           and 0 <= expect["min_brightness"] <= 255):
        raise ValueError("min_brightness is a number from 0 to 255 (0 turns the check off)")
    colors = expect.get("colors", [])
    if not isinstance(colors, list):
        raise ValueError("colors is a list of objects")
    for color in colors:
        if not isinstance(color, dict) or set(color) - {"rgb", "tolerance", "min_ratio", "max_ratio"}:
            raise ValueError("each colors entry is an object with rgb, tolerance, min_ratio, max_ratio")
        _parse_hex(color.get("rgb"))
        if "tolerance" in color and not (_is_number(color["tolerance"]) and 0 <= color["tolerance"] <= 255):
            raise ValueError("tolerance is a number from 0 to 255")
        for key in ("min_ratio", "max_ratio"):
            if key in color and not (_is_number(color[key]) and 0 <= color[key] <= 1):
                raise ValueError(f"{key} is a number from 0 to 1")
        if "min_ratio" not in color and "max_ratio" not in color:
            raise ValueError("a colors entry needs min_ratio or max_ratio")


def check(file: Path, expect: dict | None = None) -> list[tuple[str, bool, str]]:
    """Every automatic check on one screenshot: (name, passed, detail).

    blank, missing-texture and brightness run on every shot. "expect" adds
    per-shot rules: min_brightness overrides the default, colors checks that a
    color covers at least min_ratio (and at most max_ratio) of the frame.
    """
    expect = expect or {}
    with Image.open(file) as image:
        results = []
        blank = looks_blank(image)
        results.append(("blank", not blank,
                        "image is a single color (loading screen, black window, or failed capture)" if blank
                        else "not a single-color frame"))
        ratio = missing_texture_ratio(image)
        results.append(("missing-texture", ratio < MISSING_TEXTURE_RATIO,
                        f"{ratio:.1%} of the image is missing-texture magenta" if ratio >= MISSING_TEXTURE_RATIO
                        else f"{ratio:.1%} missing-texture magenta"))
        minimum = expect.get("min_brightness", MIN_BRIGHTNESS)
        if minimum:
            brightness = mean_brightness(image)
            results.append(("brightness", brightness >= minimum,
                            f"mean brightness {brightness:.0f} is below {minimum:g} (night, unrendered or very dark frame)"
                            if brightness < minimum else f"mean brightness {brightness:.0f} (min {minimum:g})"))
        for color in expect.get("colors", []):
            hex_name = color["rgb"].lower()
            found = color_ratio(image, _parse_hex(color["rgb"]), color.get("tolerance", 30))
            low, high = color.get("min_ratio", 0), color.get("max_ratio", 1)
            ok = low <= found <= high
            want = " and ".join(([f"at least {low:.1%}"] if "min_ratio" in color else [])
                                + ([f"at most {high:.1%}"] if "max_ratio" in color else []))
            results.append((f"color {hex_name}", ok, f"{found:.1%} of the image is near {hex_name}"
                            + ("" if ok else f", expected {want}")))
        return results


def inspect(file: Path, expect: dict | None = None) -> list[str]:
    """Problems a reviewer should not need to look for by eye."""
    return [detail for _, ok, detail in check(file, expect) if not ok]


def _font(size):
    try:
        return ImageFont.load_default(size=size)
    except TypeError:  # Pillow < 10.1 has no sized default font
        return ImageFont.load_default()


def contact_sheet(shots, out_file: Path, title: str, columns: int = 2, thumb_width: int = 640) -> Path:
    """shots: (label, file, note) tuples. Writes one PNG with every shot and its note."""
    font, title_font = _font(16), _font(22)
    thumbs = []
    for label, file, note in shots:
        with Image.open(file) as image:
            image = image.convert("RGB")
            height = round(image.height * thumb_width / image.width)
            thumbs.append((label, image.resize((thumb_width, height)), note))
    if not thumbs:
        raise ValueError("no shots to put on a contact sheet")
    pad, line = 12, 20
    wrap = max(20, thumb_width // 9)
    captions = [[label] + textwrap.wrap(note, wrap) for label, _, note in thumbs]
    cell_h = max(t[1].height for t in thumbs) + pad + line * max(len(c) for c in captions)
    rows = (len(thumbs) + columns - 1) // columns
    width = columns * thumb_width + (columns + 1) * pad
    header = 40
    sheet = Image.new("RGB", (width, header + rows * (cell_h + pad) + pad), "white")
    draw = ImageDraw.Draw(sheet)
    draw.text((pad, pad), title, fill="black", font=title_font)
    for index, ((_, image, _), caption) in enumerate(zip(thumbs, captions)):
        x = pad + (index % columns) * (thumb_width + pad)
        y = header + pad + (index // columns) * (cell_h + pad)
        sheet.paste(image, (x, y))
        for n, text in enumerate(caption):
            draw.text((x, y + image.height + 4 + n * line), text, fill="black" if n else "#1a4fa0", font=font)
    out_file = Path(out_file)
    out_file.parent.mkdir(parents=True, exist_ok=True)
    sheet.save(out_file)
    return out_file
