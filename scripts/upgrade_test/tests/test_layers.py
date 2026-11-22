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

    def test_checks_limited_to_mc_versions(self):
        layer = _layer("0.8.0", ["everywhere", "old", "new"])
        layer["checks"][1]["mc_versions"] = ["1.21.1"]
        layer["checks"][2]["mc_versions"] = ["26.3"]
        _write(self.dir, "0.8.0-base.json", layer)
        layers = load_layers(self.dir)

        def ids(mc_version):
            return [c["id"] for layer in layers_for_source(layers, "0.8.0", mc_version) for c in layer.checks]

        self.assertEqual(ids("1.21.1"), ["everywhere", "old"])
        self.assertEqual(ids("26.3"), ["everywhere", "new"])
        self.assertEqual(ids(None), ["everywhere", "old", "new"])

    def test_mc_versions_must_be_a_list(self):
        layer = _layer("0.8.0", ["a"])
        layer["checks"][0]["mc_versions"] = "26.3"
        _write(self.dir, "0.8.0-base.json", layer)
        with self.assertRaises(LayerError):
            load_layers(self.dir)

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

    def test_not_covered_without_item_and_reason_rejected(self):
        # The report reads item["item"] and item["reason"] only after both game
        # sessions ran, so a bare string must fail when the layers load.
        layer = _layer("0.8.0", ["a"])
        layer["not_covered"] = ["The structure is not created."]
        _write(self.dir, "0.8.0-base.json", layer)
        with self.assertRaises(LayerError):
            load_layers(self.dir)


if __name__ == "__main__":
    unittest.main()
