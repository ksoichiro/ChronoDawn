"""Offline reads of a stopped server's world: SavedData, advancements, block counts."""

import gzip
import hashlib
import io
import json
import uuid as uuid_lib
from pathlib import Path

import anvil
from nbt import nbt

_ARRAY_TAGS = (nbt.TAG_Byte_Array, nbt.TAG_Int_Array, nbt.TAG_Long_Array)
_SCALAR_TYPES = {
    "byte": nbt.TAG_Byte, "short": nbt.TAG_Short, "int": nbt.TAG_Int, "long": nbt.TAG_Long,
    "float": nbt.TAG_Float, "double": nbt.TAG_Double, "string": nbt.TAG_String,
}


def offline_uuid(name: str) -> str:
    """Same as Java's UUID.nameUUIDFromBytes("OfflinePlayer:" + name), used by offline-mode servers."""
    digest = bytearray(hashlib.md5(f"OfflinePlayer:{name}".encode("utf-8")).digest())
    digest[6] = (digest[6] & 0x0F) | 0x30
    digest[8] = (digest[8] & 0x3F) | 0x80
    return str(uuid_lib.UUID(bytes=bytes(digest)))


def dimension_dir(world: Path, dimension: str) -> Path:
    if dimension in ("overworld", "minecraft:overworld"):
        return world
    if dimension == "minecraft:the_nether":
        return world / "DIM-1"
    if dimension == "minecraft:the_end":
        return world / "DIM1"
    namespace, path = dimension.split(":", 1)
    return world / "dimensions" / namespace / path


def data_dir(world: Path, dimension: str) -> Path:
    return dimension_dir(world, dimension) / "data"


def to_plain(tag):
    if isinstance(tag, nbt.TAG_Compound):
        return {child.name: to_plain(child) for child in tag.tags}
    if isinstance(tag, nbt.TAG_List):
        return [to_plain(child) for child in tag.tags]
    if isinstance(tag, _ARRAY_TAGS):
        return list(tag.value)
    return tag.value


def _load(path: Path) -> nbt.NBTFile:
    return nbt.NBTFile(buffer=io.BytesIO(gzip.decompress(path.read_bytes())))


def read_saved_data(world: Path, dimension: str, file: str):
    path = data_dir(world, dimension) / file
    return to_plain(_load(path)) if path.exists() else None


def set_saved_data_value(world: Path, dimension: str, file: str, dotted_path: str, value, value_type: str):
    """Set (or create) a scalar inside a SavedData file; used only for states commands cannot reach."""
    path = data_dir(world, dimension) / file
    root = _load(path)
    *parents, leaf = dotted_path.split(".")
    node = root
    for key in parents:
        node = node[key]
    for child in list(node.tags):
        if child.name == leaf:
            node.tags.remove(child)
    node.tags.append(_SCALAR_TYPES[value_type](name=leaf, value=value))
    buf = io.BytesIO()
    root.write_file(buffer=buf)
    path.write_bytes(gzip.compress(buf.getvalue()))


def read_advancements(world: Path, player_uuid: str) -> dict:
    path = world / "advancements" / f"{player_uuid}.json"
    if not path.exists():
        return {}
    raw = json.loads(path.read_text())
    return {key: True for key, value in raw.items() if isinstance(value, dict) and value.get("done")}


def read_file_bytes(path: Path):
    return path.read_bytes() if path.exists() else None


def _section_counts(section, counts):
    states = section.get("block_states")
    if states is None:
        return
    palette = [entry["Name"].value for entry in states["palette"].tags]
    if "data" not in states or len(palette) == 1:
        counts[palette[0]] = counts.get(palette[0], 0) + 4096
        return
    bits = max(4, (len(palette) - 1).bit_length())
    per_long = 64 // bits
    mask = (1 << bits) - 1
    seen = 0
    local = [0] * len(palette)
    for raw in states["data"].value:
        word = raw & 0xFFFFFFFFFFFFFFFF
        for _ in range(per_long):
            if seen == 4096:
                break
            local[word & mask] += 1
            word >>= bits
            seen += 1
    for name, n in zip(palette, local):
        if n:
            counts[name] = counts.get(name, 0) + n


def block_counts(world: Path, dimension: str, chunks: set | None = None) -> tuple[dict, set]:
    """Per-block-id counts over fully generated chunks.

    With chunks=None every full chunk is counted and the set of chunks is
    returned, so the post-upgrade scan can be limited to the same chunks.
    """
    region_dir = dimension_dir(world, dimension) / "region"
    counts, counted = {}, set()
    if not region_dir.exists():
        return counts, counted
    for region_file in sorted(region_dir.glob("r.*.*.mca")):
        _, rx, rz, _ = region_file.name.split(".")
        region = anvil.Region.from_file(str(region_file))
        for local_x in range(32):
            for local_z in range(32):
                cx, cz = int(rx) * 32 + local_x, int(rz) * 32 + local_z
                if chunks is not None and (cx, cz) not in chunks:
                    continue
                try:
                    data = region.chunk_data(local_x, local_z)
                except Exception:
                    data = None
                if data is None or data.get("Status") is None or data["Status"].value != "minecraft:full":
                    continue
                counted.add((cx, cz))
                for section in data["sections"].tags:
                    _section_counts(section, counts)
    return counts, counted


def chunk_timestamps(world: Path, dimension: str) -> dict:
    """Last-save time (epoch seconds) of every stored chunk, from the region file headers."""
    region_dir = dimension_dir(world, dimension) / "region"
    stamps = {}
    if not region_dir.exists():
        return stamps
    for region_file in sorted(region_dir.glob("r.*.*.mca")):
        _, rx, rz, _ = region_file.name.split(".")
        with region_file.open("rb") as f:
            header = f.read(8192)
        if len(header) < 8192:
            continue
        for index in range(1024):
            if int.from_bytes(header[index * 4:index * 4 + 4], "big") == 0:
                continue  # chunk not stored
            stamp = int.from_bytes(header[4096 + index * 4:4096 + index * 4 + 4], "big")
            stamps[(int(rx) * 32 + index % 32, int(rz) * 32 + index // 32)] = stamp
    return stamps
