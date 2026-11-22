"""First-pass image checks and the contact sheet the owner reviews remotely."""

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


def inspect(file: Path) -> list[str]:
    """Problems a reviewer should not need to look for by eye."""
    with Image.open(file) as image:
        problems = []
        if looks_blank(image):
            problems.append("image is a single color (loading screen, black window, or failed capture)")
        ratio = missing_texture_ratio(image)
        if ratio >= MISSING_TEXTURE_RATIO:
            problems.append(f"{ratio:.1%} of the image is missing-texture magenta")
        return problems


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
