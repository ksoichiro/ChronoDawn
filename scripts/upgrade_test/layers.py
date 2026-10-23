"""Setup layers: what to build in the old world and what to check after the upgrade.

Layers are keyed by the mod version that introduced their content, so a run
from an old release only sets up what that release already had.
"""

import json
from dataclasses import dataclass, field
from pathlib import Path

KINDS = {"command", "saved_data", "advancements", "file_identical", "block_counts"}
COMPARES = {"equal", "int_gte"}


class LayerError(Exception):
    pass


@dataclass
class Layer:
    name: str
    introduced: str
    setup: list = field(default_factory=list)
    offline_patches: list = field(default_factory=list)
    checks: list = field(default_factory=list)
    not_covered: list = field(default_factory=list)


def version_key(version: str) -> tuple:
    return tuple(int(part) for part in version.split("."))


def load_layers(directory: Path) -> list[Layer]:
    layers = []
    seen_ids = {}
    for path in sorted(directory.glob("*.json")):
        try:
            raw = json.loads(path.read_text())
        except json.JSONDecodeError as e:
            raise LayerError(f"{path.name}: invalid JSON: {e}") from e
        if "introduced" not in raw:
            raise LayerError(f"{path.name}: missing 'introduced'")
        try:
            version_key(raw["introduced"])
        except ValueError as e:
            raise LayerError(f"{path.name}: bad version {raw['introduced']!r}") from e
        for check in raw.get("checks", []):
            cid = check.get("id")
            if not cid:
                raise LayerError(f"{path.name}: check without 'id'")
            if check.get("kind") not in KINDS:
                raise LayerError(f"{path.name}: check {cid}: unknown kind {check.get('kind')!r}")
            if check.get("compare", "equal") not in COMPARES:
                raise LayerError(f"{path.name}: check {cid}: unknown compare {check.get('compare')!r}")
            if cid in seen_ids:
                raise LayerError(f"{path.name}: check id {cid!r} already used in {seen_ids[cid]}")
            seen_ids[cid] = path.name
        layers.append(Layer(
            name=path.stem,
            introduced=raw["introduced"],
            setup=raw.get("setup", []),
            offline_patches=raw.get("offline_patches", []),
            checks=raw.get("checks", []),
            not_covered=raw.get("not_covered", []),
        ))
    layers.sort(key=lambda layer: version_key(layer.introduced))
    return layers


def layers_for_source(layers: list[Layer], source: str) -> list[Layer]:
    limit = version_key(source)
    return [layer for layer in layers if version_key(layer.introduced) <= limit]
