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

    def test_dark_frame_is_reported(self):
        path = self.dir / "dark.png"
        image = Image.new("RGB", (320, 180), (4, 4, 6))
        ImageDraw.Draw(image).rectangle([0, 100, 319, 179], fill=(20, 22, 25))
        image.save(path)
        self.assertTrue(any("brightness" in p for p in images.inspect(path)))

    def test_min_brightness_override_and_off(self):
        path = self.dir / "dim.png"
        image = Image.new("RGB", (320, 180), (10, 10, 12))
        ImageDraw.Draw(image).rectangle([0, 100, 319, 179], fill=(30, 32, 35))
        image.save(path)
        self.assertTrue(images.inspect(path, {"min_brightness": 30}))
        self.assertEqual(images.inspect(path, {"min_brightness": 5}), [])
        self.assertEqual([n for n, _, _ in images.check(path, {"min_brightness": 0})], ["blank", "missing-texture"])

    def test_color_presence_and_absence(self):
        path = self._scene("green")  # the bottom 80 of 180 rows is half grass green
        grass = {"rgb": "#5a8c3c", "tolerance": 10}
        self.assertEqual(images.inspect(path, {"colors": [{**grass, "min_ratio": 0.2}]}), [])
        self.assertTrue(any("expected at least" in p for p in
                            images.inspect(path, {"colors": [{**grass, "min_ratio": 0.9}]})))
        self.assertTrue(any("expected at most" in p for p in
                            images.inspect(path, {"colors": [{**grass, "max_ratio": 0.01}]})))
        self.assertTrue(images.inspect(path, {"colors": [{"rgb": "#ff0000", "min_ratio": 0.05}]}))

    def test_validate_expect(self):
        images.validate_expect({"min_brightness": 0, "colors": [{"rgb": "#aabbcc", "min_ratio": 0.1}]})
        for bad in ([], {"nope": 1}, {"min_brightness": 300}, {"colors": {}}, {"colors": [{"rgb": "red", "min_ratio": 1}]},
                    {"colors": [{"rgb": "#aabbcc"}]}, {"colors": [{"rgb": "#aabbcc", "min_ratio": 2}]},
                    {"colors": [{"rgb": "#aabbcc", "min_ratio": 0.1, "x": 1}]}):
            with self.assertRaises(ValueError, msg=bad):
                images.validate_expect(bad)

    def test_contact_sheet(self):
        shots = [(f"s/{i}", self._scene(f"shot{i}"), "a fairly long note " * 5) for i in range(3)]
        out = images.contact_sheet(shots, self.dir / "sheet.png", "title", thumb_width=200)
        with Image.open(out) as sheet:
            self.assertGreater(sheet.width, 400)
            self.assertGreater(sheet.height, 2 * 112)


if __name__ == "__main__":
    unittest.main()
