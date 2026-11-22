"""Setup layers: what to build in the old world and what to check after the upgrade.

Layers are keyed by the mod version that introduced their content, so a run
from an old release only sets up what that release already had.
"""

import dataclasses
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
            versions = check.get("mc_versions")
            if versions is not None and (not isinstance(versions, list)
                                         or not all(isinstance(v, str) for v in versions)):
                raise LayerError(f"{path.name}: check {cid}: 'mc_versions' must be a list of strings")
            if cid in seen_ids:
                raise LayerError(f"{path.name}: check id {cid!r} already used in {seen_ids[cid]}")
            seen_ids[cid] = path.name
        for item in raw.get("not_covered", []):
            if not isinstance(item, dict) or not item.get("item") or not item.get("reason"):
                raise LayerError(f"{path.name}: not_covered entry needs 'item' and 'reason': {item!r}")
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


def layers_for_source(layers: list[Layer], source: str, mc_version: str | None = None) -> list[Layer]:
    """Layers the source release already had, keeping only the checks that apply to mc_version.

    A check with "mc_versions" runs only on those Minecraft versions, for state
    that a Minecraft version stores differently (e.g. 26.x keeps dimension time
    in a vanilla world clock instead of the mod's own SavedData).
    """
    limit = version_key(source)
    active = []
    for layer in layers:
        if version_key(layer.introduced) > limit:
            continue
        checks = [c for c in layer.checks if mc_version is None or mc_version in c.get("mc_versions", [mc_version])]
        active.append(dataclasses.replace(layer, checks=checks))
    return active
