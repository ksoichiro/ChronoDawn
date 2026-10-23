import json
import tempfile
import unittest
from pathlib import Path

from scripts.upgrade_test.layers import LayerError, layers_for_source, load_layers


def _write(dir_path, name, layer):
    (dir_path / name).write_text(json.dumps(layer))


def _layer(introduced, check_ids, kind="command"):
    checks = [{"id": cid, "kind": kind, "command": "data get block 0 64 0"} for cid in check_ids]
    return {"introduced": introduced, "setup": [], "checks": checks}


class LayersTest(unittest.TestCase):
    def setUp(self):
        self.tmp = tempfile.TemporaryDirectory()
        self.dir = Path(self.tmp.name)

    def tearDown(self):
        self.tmp.cleanup()

    def test_layers_for_source_filters_by_version(self):
        _write(self.dir, "0.8.0-base.json", _layer("0.8.0", ["base"]))
        _write(self.dir, "0.9.0-chronite.json", _layer("0.9.0", ["chronite"]))
        _write(self.dir, "0.10.0-x.json", _layer("0.10.0", ["ten"]))
        _write(self.dir, "0.14.0-time-well.json", _layer("0.14.0", ["well"]))
        layers = load_layers(self.dir)
        ids = [c["id"] for layer in layers_for_source(layers, "0.13.0") for c in layer.checks]
        # 0.10.0 sorts after 0.9.0 numerically, not as a string.
        self.assertEqual(ids, ["base", "chronite", "ten"])
        self.assertEqual([c["id"] for layer in layers_for_source(layers, "0.8.0") for c in layer.checks], ["base"])

    def test_unknown_kind_rejected(self):
        _write(self.dir, "0.8.0-base.json", _layer("0.8.0", ["a"], kind="screenshot"))
        with self.assertRaises(LayerError):
            load_layers(self.dir)

    def test_unknown_compare_rejected(self):
        layer = _layer("0.8.0", ["a"])
        layer["checks"][0]["compare"] = "fuzzy"
        _write(self.dir, "0.8.0-base.json", layer)
        with self.assertRaises(LayerError):
            load_layers(self.dir)

    def test_duplicate_check_id_rejected(self):
        _write(self.dir, "0.8.0-base.json", _layer("0.8.0", ["same"]))
        _write(self.dir, "0.9.0-other.json", _layer("0.9.0", ["same"]))
        with self.assertRaises(LayerError):
            load_layers(self.dir)

    def test_not_covered_kept(self):
        layer = _layer("0.8.0", ["a"])
        layer["not_covered"] = [{"item": "boss defeated", "reason": "needs a real fight"}]
        _write(self.dir, "0.8.0-base.json", layer)
        self.assertEqual(load_layers(self.dir)[0].not_covered[0]["item"], "boss defeated")


if __name__ == "__main__":
    unittest.main()
