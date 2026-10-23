import gzip
import io
import json
import tempfile
import unittest
from pathlib import Path

from nbt import nbt

from scripts.upgrade_test.snapshot import (
    chunk_timestamps,
    data_dir,
    offline_uuid,
    read_advancements,
    read_saved_data,
    set_saved_data_value,
)


def _write_dat(path: Path, compound: nbt.NBTFile):
    path.parent.mkdir(parents=True, exist_ok=True)
    buf = io.BytesIO()
    compound.write_file(buffer=buf)
    path.write_bytes(gzip.compress(buf.getvalue()))


def _global_state(defeated: int) -> nbt.NBTFile:
    root = nbt.NBTFile()
    root.name = ""
    data = nbt.TAG_Compound(name="data")
    data.tags.append(nbt.TAG_Byte(name="IsTyrantDefeated", value=defeated))
    items = nbt.TAG_List(name="Players", type=nbt.TAG_String)
    items.tags.append(nbt.TAG_String(value="Dev"))
    data.tags.append(items)
    root.tags.append(data)
    root.tags.append(nbt.TAG_Int(name="DataVersion", value=3955))
    return root


class SnapshotTest(unittest.TestCase):
    def setUp(self):
        self.tmp = tempfile.TemporaryDirectory()
        self.world = Path(self.tmp.name) / "world"

    def tearDown(self):
        self.tmp.cleanup()

    def test_data_dir_per_dimension(self):
        self.assertEqual(data_dir(self.world, "overworld"), self.world / "data")
        self.assertEqual(data_dir(self.world, "chronodawn:chronodawn"),
                         self.world / "dimensions" / "chronodawn" / "chronodawn" / "data")

    def test_read_saved_data_roundtrip(self):
        _write_dat(self.world / "data" / "chronodawn_global_state.dat", _global_state(1))
        value = read_saved_data(self.world, "overworld", "chronodawn_global_state.dat")
        self.assertEqual(value["data"]["IsTyrantDefeated"], 1)
        self.assertEqual(value["data"]["Players"], ["Dev"])

    def test_read_saved_data_missing_file_is_none(self):
        self.assertIsNone(read_saved_data(self.world, "overworld", "nope.dat"))

    def test_set_saved_data_value(self):
        path = self.world / "data" / "chronodawn_global_state.dat"
        _write_dat(path, _global_state(0))
        set_saved_data_value(self.world, "overworld", "chronodawn_global_state.dat",
                             "data.IsTyrantDefeated", 1, "byte")
        self.assertEqual(read_saved_data(self.world, "overworld", "chronodawn_global_state.dat")
                         ["data"]["IsTyrantDefeated"], 1)

    def test_read_advancements_done_only(self):
        uuid = offline_uuid("Dev")
        path = self.world / "advancements" / f"{uuid}.json"
        path.parent.mkdir(parents=True)
        path.write_text(json.dumps({
            "chronodawn:root": {"criteria": {}, "done": True},
            "chronodawn:enter": {"criteria": {}, "done": False},
            "DataVersion": 3955,
        }))
        self.assertEqual(read_advancements(self.world, uuid), {"chronodawn:root": True})

    def test_chunk_timestamps_from_region_header(self):
        region = self.world / "region"
        region.mkdir(parents=True)
        header = bytearray(8192)
        # Chunk (1, 2) of region r.-1.0 is local index 1 + 2 * 32; its timestamp lives in the second 4 KiB.
        index = 1 + 2 * 32
        header[index * 4:index * 4 + 4] = (2 << 8 | 1).to_bytes(4, "big")  # sector offset 2, 1 sector
        header[4096 + index * 4:4096 + index * 4 + 4] = (1761234567).to_bytes(4, "big")
        (region / "r.-1.0.mca").write_bytes(bytes(header))
        self.assertEqual(chunk_timestamps(self.world, "overworld"), {(-32 + 1, 2): 1761234567})

    def test_offline_uuid_dev(self):
        # Value of java.util.UUID.nameUUIDFromBytes("OfflinePlayer:Dev".getBytes(UTF_8)),
        # obtained once with jshell.
        self.assertEqual(offline_uuid("Dev"), "380df991-f603-344c-a090-369bad2a924a")


if __name__ == "__main__":
    unittest.main()
