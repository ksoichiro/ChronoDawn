#!/usr/bin/env python3
"""Generate Blockbench projects for editing humanoid armor layer textures.

Writes one assets/<armor>_armor_layer_<n>.bbmodel per armor layer. Layer 1
(helmet, chestplate, sleeves, boots) uses inflate 1.0 and layer 2 (leggings)
uses inflate 0.5, matching the vanilla humanoid armor model. Each project
references a single texture, so layers are kept in separate files.

The texture is embedded as base64 and also referenced by relative path, so
saving in Blockbench overwrites the PNG under common/shared. The 1.21.2+
copies under common/shared-1.21.2+ are not touched and must be synced by hand.

Deterministic output (uuid5). Re-run after editing a texture outside
Blockbench to refresh the embedded copy.

Usage: python3 scripts/generate_armor_bbmodels.py
"""
import base64
import json
import os
import uuid
from pathlib import Path

ROOT = Path(__file__).resolve().parent.parent
TEX_DIR = ROOT / "common/shared/src/main/resources/assets/chronodawn/textures/models/armor"
OUT_DIR = ROOT / "assets"
W, H = 64, 32


_counter = 0


def new_uuid():
    global _counter
    _counter += 1
    return str(uuid.uuid5(uuid.NAMESPACE_URL, f"chronodawn-armor-bbmodel-{_counter}"))




def part(name, pivot, frm, to, uv, mirror=False):
    return dict(name=name, pivot=pivot, frm=frm, to=to, uv=uv, mirror=mirror)

# Blockbench's x axis is the reverse of the Java model space the vanilla
# armor UV layout comes from, so the right (unmirrored) parts sit at +x here.
# Placing them at -x showed the left and right feet swapped against the game.
LAYER1 = [
    ("head", [0, 24, 0], [part("helmet", [0, 24, 0], [-4, 24, -4], [4, 32, 4], [0, 0])]),
    ("body", [0, 24, 0], [part("chestplate", [0, 24, 0], [-4, 12, -2], [4, 24, 2], [16, 16])]),
    ("right_arm", [5, 22, 0], [part("right_sleeve", [5, 22, 0], [4, 12, -2], [8, 24, 2], [40, 16])]),
    ("left_arm", [-5, 22, 0], [part("left_sleeve", [-5, 22, 0], [-8, 12, -2], [-4, 24, 2], [40, 16], True)]),
    ("right_leg", [1.9, 12, 0], [part("right_boot", [1.9, 12, 0], [0, 0, -2], [4, 12, 2], [0, 16])]),
    ("left_leg", [-1.9, 12, 0], [part("left_boot", [-1.9, 12, 0], [-4, 0, -2], [0, 12, 2], [0, 16], True)]),
]
LAYER2 = [
    ("body", [0, 24, 0], [part("belt", [0, 24, 0], [-4, 12, -2], [4, 24, 2], [16, 16])]),
    ("right_leg", [1.9, 12, 0], [part("right_legging", [1.9, 12, 0], [0, 0, -2], [4, 12, 2], [0, 16])]),
    ("left_leg", [-1.9, 12, 0], [part("left_legging", [-1.9, 12, 0], [-4, 0, -2], [0, 12, 2], [0, 16], True)]),
]


def box_faces(size, uv, mirror, tex):
    w, h, d = size
    u, v = uv
    f = {
        "north": [u + d, v + d, u + d + w, v + d + h],
        "east": [u, v + d, u + d, v + d + h],
        "south": [u + 2 * d + w, v + d, u + 2 * d + 2 * w, v + d + h],
        "west": [u + d + w, v + d, u + 2 * d + w, v + d + h],
        "up": [u + d + w, v + d, u + d, v],
        "down": [u + d + 2 * w, v, u + d + w, v + d],
    }
    if mirror:
        # Blockbench swaps east/west and flips every face horizontally
        f["east"], f["west"] = f["west"], f["east"]
        for k, (x1, y1, x2, y2) in list(f.items()):
            f[k] = [x2, y1, x1, y2]
    return {k: {"uv": val, "texture": tex} for k, val in f.items()}


def texture_entry(layer_id, fname):
    path = TEX_DIR / fname
    data = base64.b64encode(path.read_bytes()).decode()
    rel = os.path.relpath(path, OUT_DIR)
    return {
        "name": fname, "relative_path": rel, "folder": "", "namespace": "", "id": str(layer_id),
        "group": "", "width": W, "height": H, "uv_width": W, "uv_height": H,
        "particle": False, "use_as_default": layer_id == 0, "layers_enabled": False,
        "sync_to_project": "", "render_mode": "default", "render_sides": "auto",
        "pbr_channel": "color", "frame_time": 1, "frame_order_type": "loop", "frame_order": "",
        "frame_interpolate": False, "visible": True, "internal": True, "saved": True,
        "uuid": str(new_uuid()), "source": "data:image/png;base64," + data,
    }


def build(armor, layer):
    parts, inflate = (LAYER1, 1.0) if layer == 1 else (LAYER2, 0.5)
    elements, groups, outliner = [], [], []
    color = 0
    for gname, pivot, cubes in parts:
        g_uuid = str(new_uuid())
        kids = []
        for c in cubes:
            e_uuid = str(new_uuid())
            size = [c["to"][i] - c["frm"][i] for i in range(3)]
            elements.append({
                "name": c["name"], "box_uv": True, "render_order": "default", "locked": False,
                "allow_mirror_modeling": True, "from": c["frm"], "to": c["to"],
                "autouv": 0, "color": color % 8, "inflate": inflate, "origin": [0, 0, 0],
                "uv_offset": c["uv"], "mirror_uv": c["mirror"],
                "faces": box_faces(size, c["uv"], c["mirror"], 0),
                "type": "cube", "uuid": e_uuid,
            })
            kids.append(e_uuid)
            color += 1
        groups.append({"uuid": g_uuid, "export": True, "locked": False, "origin": pivot,
                       "rotation": [0, 0, 0], "color": 0, "name": gname,
                       "children": [], "reset": False, "shade": True, "mirror_uv": False,
                       "selected": False, "visibility": True, "autouv": 0, "isOpen": False})
        outliner.append({"uuid": g_uuid, "isOpen": False, "children": kids})
    return {
        "meta": {"format_version": "5.0", "model_format": "modded_entity", "box_uv": True},
        "name": f"{armor}_armor_layer_{layer}", "model_identifier": "", "modded_entity_entity_class": "",
        "modded_entity_version": "1.17", "modded_entity_flip_y": True, "visible_box": [1, 1, 0],
        "variable_placeholders": "", "variable_placeholder_buttons": [], "timeline_setups": [],
        "unhandled_root_fields": {}, "resolution": {"width": W, "height": H},
        "elements": elements, "groups": groups, "outliner": outliner,
        "textures": [texture_entry(0, f"{armor}_layer_{layer}.png")],
    }


for armor, layers in [("clockstone", (1, 2)), ("enhanced_clockstone", (1, 2)),
                      ("temporal_amber", (1, 2)), ("time_tyrant", (1,))]:
    for layer in layers:
        out = OUT_DIR / f"{armor}_armor_layer_{layer}.bbmodel"
        with open(out, "w") as fh:
            json.dump(build(armor, layer), fh, indent=2)
            fh.write("\n")
        print("wrote", out.relative_to(ROOT))
