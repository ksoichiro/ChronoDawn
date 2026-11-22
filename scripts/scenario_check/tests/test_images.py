import tempfile
import unittest
from pathlib import Path

from PIL import Image, ImageDraw

from scripts.scenario_check import images


class ImagesTest(unittest.TestCase):
    def setUp(self):
        self.tmp = tempfile.TemporaryDirectory()
        self.dir = Path(self.tmp.name)

    def tearDown(self):
        self.tmp.cleanup()

    def _scene(self, name, magenta=False, blank=False):
        image = Image.new("RGB", (320, 180), (120, 170, 230))
        if not blank:
            draw = ImageDraw.Draw(image)
            for x in range(0, 320, 16):
                draw.rectangle([x, 100, x + 7, 179], fill=(90, 140, 60))
            if magenta:
                draw.rectangle([100, 40, 160, 100], fill=(200, 0, 200))
                draw.rectangle([100, 40, 130, 70], fill=(0, 0, 0))
        path = self.dir / f"{name}.png"
        image.save(path)
        return path

    def test_clean_scene_has_no_problems(self):
        self.assertEqual(images.inspect(self._scene("clean")), [])

    def test_missing_texture_is_reported(self):
        problems = images.inspect(self._scene("missing", magenta=True))
        self.assertTrue(any("magenta" in p for p in problems), problems)

    def test_mod_purple_is_not_missing_texture(self):
        image = Image.new("RGB", (100, 100), (110, 60, 170))
        self.assertLess(images.missing_texture_ratio(image), images.MISSING_TEXTURE_RATIO)

    def test_blank_frame_is_reported(self):
        problems = images.inspect(self._scene("blank", blank=True))
        self.assertTrue(any("single color" in p for p in problems), problems)

    def test_contact_sheet(self):
        shots = [(f"s/{i}", self._scene(f"shot{i}"), "a fairly long note " * 5) for i in range(3)]
        out = images.contact_sheet(shots, self.dir / "sheet.png", "title", thumb_width=200)
        with Image.open(out) as sheet:
            self.assertGreater(sheet.width, 400)
            self.assertGreater(sheet.height, 2 * 112)


if __name__ == "__main__":
    unittest.main()
