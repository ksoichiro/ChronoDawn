# Design: `c:tools/*`, `c:armors/*`, and Shield Tag Coverage

**Created**: 2026-09-03
**Status**: Approved — ready for implementation planning
**Pillar**: [Modpack-author readiness roadmap](./2026-05-09-modpack-author-readiness-roadmap.md),
sub-project B (datapack and tag externalization)

---

## 1. Problem

The conventional-tags design
([2026-08-17-conventional-tags-design.md](./2026-08-17-conventional-tags-design.md))
deliberately scoped tools, weapons, armor, and shields out, noting "tier
semantics need their own decisions." Chrono Dawn's own tool, weapon, armor,
and shield items currently join no `c:` tag at all, so pack recipes,
recipe-viewer plugins, and cross-mod systems that select gear by convention
tag cannot see them.

## 2. Ground truth: what the convention actually offers

The premise that this needed "tier" (material) decisions turned out to be
wrong. Verified directly from the `fabric-convention-tags-v1`/`v2` jars in
the local Gradle cache (not assumed) and cross-checked against NeoForge's
`Tags.Items` source, across every MC version this repo supports:

- Conventional tags v2 (1.21.1–1.21.11) has **no per-material subtags** for
  tools, and **no per-tool-type tag** for shovels or hoes at all. The only
  categories are functional: `tools/mining_tool` (pickaxe only),
  `tools/melee_weapon` (sword + axe + mace + trident), `tools/ranged_weapon`,
  `tools/shield`, `tools/spear`, plus categories this mod has no items for
  (`bow`, `crossbow`, `brush`, `igniter`, `mace`, `shear`, `fishing_rod`,
  `wrench`).
- `armors` exists as a bare umbrella from 1.21.1 onward, resolving to vanilla
  `#minecraft:head_armor`/`chest_armor`/`leg_armor`/`foot_armor` — i.e.
  humanoid armor only. Per-category subtags (`armors/humanoid`,
  `armors/horse`, `armors/nautilus`, `armors/wolf`) were added later, first
  present at fabric-api 0.141.3 / NeoForge 21.11.38-beta (**MC 1.21.11
  only**, confirmed absent through 1.21.10 on both loaders). Before that
  version, bare `armors` already meant "humanoid armor" — there was nothing
  else it could mean.
- Conventional tags v1 (1.20.1) has no armor tag in any form. Its tool
  coverage is flat, ungrouped umbrellas: `axes`, `bows`, `hoes`, `pickaxes`,
  `shields`, `shovels`, `swords`.
- Deprecated plural aliases exist for some v2 tags (`melee_weapons`,
  `shields` under `tools/`, `mining_tools`) — same trap as the `foods/breads`
  case in the materials design. This design uses the singular canonical
  forms only.

So there is no tier decision to make. The only design work is: which
functional category does each Chrono Dawn item join, and which categories
have no home in the convention at all.

## 3. Scope

**In scope**: every real Chrono Dawn tool, weapon, armor, and shield item,
including artifact and boss-drop gear (`Chronoblade`,
`SpatiallyLinkedPickaxe`, `TimeTyrantMailItem`). These tags classify *what an
item is* (a sword, a pickaxe, a piece of armor), not *what it's made of* or
*how obtainable it is* — a boss-drop sword is still, functionally, a sword,
and excluding it from `c:tools/melee_weapon` would only break interop
(weapon-rack detection, enchant-compatibility broadening, automated
equip logic) for no benefit.

**Out of scope**:

- `c:armors/humanoid` and its 1.21.11-only siblings (`horse`, `nautilus`,
  `wolf`). See §5.
- Repair-ingredient tags (`c:gems/time_crystal` etc. as consumed by
  `Tier`/`ArmorMaterial` repair predicates) — already shipped, unrelated
  mechanism from this slice's outbound classification tags.
- Inventing new tag names for shovels/hoes on 1.21.x. The convention has no
  category for them there; adding a mod-owned tag name (e.g.
  `chronodawn:mining_tools`) would be a name no other mod reads, the same
  trap the materials design explicitly avoided.

## 4. Tag assignments

### 4.1 1.21.1–1.21.11 (v2)

| Chrono Dawn items | Tag |
| --- | --- |
| `clockstone_sword`, `enhanced_clockstone_sword`, `entropy_crystal_sword`, `chronoblade`, `clockstone_axe`, `enhanced_clockstone_axe` | `c:tools/melee_weapon` |
| `clockstone_pickaxe`, `enhanced_clockstone_pickaxe`, `spatially_linked_pickaxe` | `c:tools/mining_tool` |
| `clockstone_shield`, `enhanced_clockstone_shield`, `entropy_crystal_shield` | `c:tools/shield` |
| `clockstone_{helmet,chestplate,leggings,boots}`, `enhanced_clockstone_{helmet,chestplate,leggings,boots}`, `temporal_amber_{helmet,chestplate,leggings,boots}`, `time_tyrant_mail` | `c:armors` |
| `clockstone_shovel`, `enhanced_clockstone_shovel`, `clockstone_hoe`, `enhanced_clockstone_hoe` | none — no v2 category exists for shovels or hoes |

Axes join `melee_weapon` only, matching the convention's own classification
(axes are not in `mining_tool` upstream either) — not a mining-tool
grouping as their vanilla dual-use might suggest.

### 4.2 1.20.1 (v1)

| Chrono Dawn items | Tag |
| --- | --- |
| `clockstone_sword`, `enhanced_clockstone_sword`, `entropy_crystal_sword`, `chronoblade` | `c:swords` |
| `clockstone_pickaxe`, `enhanced_clockstone_pickaxe`, `spatially_linked_pickaxe` | `c:pickaxes` |
| `clockstone_axe`, `enhanced_clockstone_axe` | `c:axes` |
| `clockstone_shovel`, `enhanced_clockstone_shovel` | `c:shovels` |
| `clockstone_hoe`, `enhanced_clockstone_hoe` | `c:hoes` |
| `clockstone_shield`, `enhanced_clockstone_shield`, `entropy_crystal_shield` | `c:shields` |
| any armor item | none — v1 has no armor tag in any form |

## 5. Deferred: `c:armors/humanoid` and 1.21.11 subtags

`armors/humanoid` (and `horse`/`nautilus`/`wolf`) exist only from 1.21.11.
Publishing it correctly would require a 1.21.11-only override directory
layered on top of the shared `c:` tree (the established pattern for
1.21.11-only content — see the biome-override precedent), plus extending
`validateConventionalTags`'s cross-root consistency check (currently
inert, written for exactly one populated `v2_roots` entry) to tolerate a
second, deliberately-partial root.

This is deferred: bare `c:armors` already resolves correctly and
identically in meaning across the whole 1.21.1–1.21.11 range (humanoid
armor, full stop), and a consumer specifically distinguishing
`c:armors/humanoid` from the bare umbrella — as opposed to just checking
membership in `c:armors`, or checking the item's actual equipment slot in
Java — is a narrow case with no known modpack demand. If that demand
surfaces, `common/1.21.11` gets its own small `data/c/tags/item/armors/humanoid.json`
addition, matching this repo's 1.21.11-biome-override precedent, at that
time.

## 6. File layout

| Era | Root | New files |
| --- | --- | --- |
| 1.21.1–1.21.11 | `common/shared-1.21.1+/src/main/resources/data/c/tags/item/` | `tools/melee_weapon.json`, `tools/mining_tool.json`, `tools/shield.json`, `armors.json` |
| 1.20.1 | `common/1.20.1/src/main/resources/data/c/tags/items/` | `swords.json`, `pickaxes.json`, `axes.json`, `shovels.json`, `hoes.json`, `shields.json` |

Every file uses `"replace": false` and lists members directly in `values`,
matching the existing `c:` tag files' shape. No loader-specific files: both
Fabric and NeoForge read `c:`.

## 7. Verification

Extend `scripts/conventional_tag_rules.json` (consumed by the existing
`validateConventionalTags` Gradle task):

- Add to `v2_allowed_tags`: `item/tools/melee_weapon`,
  `item/tools/mining_tool`, `item/tools/shield`, `item/armors`.
- Add to `v1_allowed_tags`: `items/swords`, `items/pickaxes`, `items/axes`,
  `items/shovels`, `items/hoes`, `items/shields`.
- Add coverage rules (same `ids`-list shape already used for ingots/gems/raw
  materials/dusts) for each row in §4.1 (`era` defaults to v2) and each row
  in §4.2 (`era: "v1"`). Shovels and hoes get a v1-only rule; no v2 rule
  exists for them since no v2 tag applies.
- Add the four new v2 tag names to `translation_exempt_tags` — Fabric
  supplies their translations upstream, the same reasoning already applied
  to `item/ores`, `item/foods/bread`, etc.

No `v2_roots` change, and no change to the cross-root consistency check
(§5 explains why that check's extension is out of scope for this slice).

Run after implementation:

```
mise exec java@temurin-21.0.3+9.0.LTS -- ./gradlew validateResources
mise exec java@temurin-21.0.3+9.0.LTS -- ./gradlew :common-1.21.11:test -Ptarget_mc_version=1.21.11
```

## 8. Documentation

- `docs/modpack-integration.md`: add the new tags to the published `c:` tag
  list.
- `2026-05-09-modpack-author-readiness-roadmap.md`: mark the tools/armor tag
  slice shipped, link this design doc.
- `CHANGELOG.md`: one entry.

## 9. Follow-on work

`c:armors/humanoid` and its 1.21.11 siblings (§5) are the natural
continuation if a concrete need appears. The `c:tools/*`/`c:armors/*`
consuming half — recipes or other Java code that currently hardcodes a
Chrono Dawn tool/armor `Item` reference — is a separate future slice, same
relationship the recipe-consumer slice had to the materials tag design.
