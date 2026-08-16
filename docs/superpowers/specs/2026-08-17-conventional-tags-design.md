# Design: Conventional (`c:`) Tag Coverage for Materials

**Created**: 2026-08-17
**Status**: Approved — ready for implementation planning
**Pillar**: [Flagship-parity roadmap](./2026-07-05-flagship-parity-roadmap.md) P1 (ecosystem
integrations), first slice; also covers the tag half of sub-project B in the
[modpack-author readiness roadmap](./2026-05-09-modpack-author-readiness-roadmap.md)

---

## 1. Problem

Chrono Dawn exposes no conventional tags. `data/c/` does not exist in any
resource root, and the only mod-owned data namespace besides `chronodawn` is
`data/neoforge/loot_modifiers`. Every material the mod adds — ores that yield
vanilla resources, its own ingots and gems, its stone and wood sets, its foods —
is invisible to cross-mod recipes, to pack-author unification recipes, and to
any mod that consumes materials by tag.

This blocks the rest of the P1 pillar: recipe-viewer plugins, Jade, and
accessory-slot integrations all assume a mod's materials are already
tag-addressable.

## 2. Scope

In scope: item and block tags for **materials** — ores, raw materials, ingots
and gems, storage blocks, stone/sand/gravel/sandstone, wood sets, foods, crops
and seeds.

Out of scope for this initiative:

- Tools, weapons, armor, and shields (`c:tools/*`, `c:armors/*`) — a separate
  slice, since tier semantics need their own decisions.
- Mod-owned biome and structure tags — deferred to a later P1 slice.
- The `forge:` namespace. This repo has no NeoForge module for 1.20.1, and
  NeoForge reads `c:` on every version it does support, so `forge:` would be
  dead weight.

## 3. Version matrix

The conventional-tag convention changed shape between the two eras this repo
spans. This was verified against Fabric API sources rather than assumed:

| Era | Convention | Shape |
| --- | --- | --- |
| 1.20.1 | `fabric-convention-tags-v1` | Flat plural umbrella tags only (`c:ores`, `c:ingots`, `c:gems`, `c:raw_ores`, `c:dusts`, `c:foods`, `c:sandstone_blocks`). No per-material subtags. |
| 1.21.1–1.21.11 | `fabric-convention-tags-v2` (aligned with NeoForge) | Path-form subtags under umbrellas (`c:ores/coal`, `c:gems/amethyst`, `c:storage_blocks/iron`). |

Within the 1.21.1–1.21.11 range the drift is **additive only**. Comparing
`ConventionalItemTags` between the 1.21.1 and 1.21.11 branches shows new
categories appearing (`armors/*`, `natural_logs/*`, `flowers/*`, `bricks/resin`,
`nuggets/copper`, `tools/wrench`, `seeds/pitcher_plant`, `storage_blocks/resin`,
`clumps/resin`) and one removal (`foods/dough`). **Every tag this design uses
exists unchanged across all ten 1.21.x versions.**

One trap inside v2: both singular and plural food subtags appear in the source
(`foods/bread` and `foods/breads`, `foods/soup` and `foods/soups`, …). The
**plural** ones are the deprecated aliases — each is annotated `@Deprecated`
with the comment "This tag was typoed" — so this design uses the **singular**
forms only. Some categories have no plural alias at all (`foods/pie`), which
makes the singular form the only option regardless.

## 4. Tag assignments (1.21.1+)

### 4.1 Ores

Applied to both the block and its item form, and to the deepslate variant where
one exists.

| Blocks | Tag |
| --- | --- |
| `temporal_coal_ore` | `c:ores/coal` |
| `temporal_iron_ore` | `c:ores/iron` |
| `temporal_gold_ore`, `deepslate_temporal_gold_ore` | `c:ores/gold` |
| `temporal_redstone_ore`, `deepslate_temporal_redstone_ore` | `c:ores/redstone` |
| `clockstone_ore`, `deepslate_clockstone_ore` | `c:ores/clockstone` |
| `time_crystal_ore` | `c:ores/time_crystal` |
| `entropy_crystal_ore` | `c:ores/entropy_crystal` |
| `temporal_amber_ore`, `deepslate_temporal_amber_ore` | `c:ores/temporal_amber` |

All of the above additionally join the `c:ores` umbrella, plus
`c:ores_in_ground/stone` for the temporal-stone-hosted variants and
`c:ores_in_ground/deepslate` for the `deepslate_*` variants.

Host stone joins the ground tags: `temporal_stone` →
`c:ore_bearing_ground/stone`, `deepslate_temporal_stone` →
`c:ore_bearing_ground/deepslate`.

Putting the four vanilla-resource ores under `c:ores/coal|iron|gold|redstone` is
the highest-value single change here: it makes Chrono Dawn ores usable by every
pack recipe and processing mod that already targets those tags.

### 4.2 Material items

| Item | Tags |
| --- | --- |
| `clockstone` | `c:ingots/clockstone`, `c:ingots` |
| `enhanced_clockstone` | `c:ingots/enhanced_clockstone`, `c:ingots` |
| `time_crystal` | `c:gems/time_crystal`, `c:gems` |
| `entropy_crystal` | `c:gems/entropy_crystal`, `c:gems` |
| `raw_temporal_amber` | `c:raw_materials/temporal_amber`, `c:raw_materials` |
| `temporal_amber_dust` | `c:dusts/temporal_amber`, `c:dusts` |

### 4.3 Storage blocks

`clockstone_block` → `c:storage_blocks/clockstone`; `time_crystal_block` →
`c:storage_blocks/time_crystal`. Both also join `c:storage_blocks`.

### 4.4 Stone, sand, gravel, sandstone

| Blocks | Tag |
| --- | --- |
| `temporal_stone` | `c:stones` |
| `temporal_cobblestone` | `c:cobblestones/normal`, `c:cobblestones` |
| `mossy_temporal_cobblestone` | `c:cobblestones/mossy`, `c:cobblestones` |
| `cobbled_deepslate_temporal_stone` | `c:cobblestones/deepslate`, `c:cobblestones` |
| `temporal_sand` | `c:sands` |
| `temporal_gravel` | `c:gravels` |
| `temporal_sandstone` | `c:sandstone/blocks` |
| `temporal_sandstone_slab` | `c:sandstone/slabs` |
| `temporal_sandstone_stairs` | `c:sandstone/stairs` |

`temporal_sand` deliberately joins only the `c:sands` umbrella, not
`c:sands/colorless` or `c:sands/red` — its tint matches neither.

### 4.5 Wood sets (time_wood, dark_time_wood, ancient_time_wood)

Planks, logs, leaves, saplings, doors, trapdoors, slabs, stairs, buttons, and
pressure plates are already covered by the `minecraft:` tags this repo ships, so
only the conventional-only categories are added:

- `stripped_*_log` → `c:stripped_logs`
- `stripped_*_wood` → `c:stripped_woods`
- `*_fence` → `c:fences/wooden`, `c:fences`
- `*_fence_gate` → `c:fence_gates/wooden`, `c:fence_gates`

`c:natural_logs/overworld` is **not** used: it exists only from 1.21.11 and
would force a version-specific override for marginal benefit.

### 4.6 Foods, crops, seeds

| Items | Tag |
| --- | --- |
| `time_bread`, `enhanced_time_bread` | `c:foods/bread` |
| `time_wheat_cookie`, `clockwork_cookie` | `c:foods/cookie` |
| `time_fruit_pie` | `c:foods/pie` |
| `fruit_of_time`, `chrono_melon_slice` | `c:foods/fruit` |
| `temporal_root`, `baked_temporal_root` | `c:foods/vegetable` |
| `temporal_root_stew`, `timeless_mushroom_soup` | `c:foods/soup` |
| `chrono_bovine_meat` | `c:foods/raw_meat` |
| `cooked_chrono_bovine_meat` | `c:foods/cooked_meat` |
| `glide_fish` | `c:foods/raw_fish` |
| `cooked_glide_fish` | `c:foods/cooked_fish` |
| `golden_time_wheat`, `glistening_chrono_melon` | `c:foods/golden` |
| `chrono_melon_juice` | `c:drinks/juice`, `c:drinks` |
| `timeless_mushroom` | `c:mushrooms` |

Every food entry above also joins the `c:foods` umbrella. `chrono_melon_juice`
and `timeless_mushroom` are not foods in the tag sense and join only the tags
listed for them.

Crops and seeds: `time_wheat` → `c:crops/time_wheat`, `time_wheat_seeds` →
`c:seeds/time_wheat`, `chrono_melon` → `c:crops/chrono_melon`,
`chrono_melon_seeds` → `c:seeds/chrono_melon`, each also joining `c:crops` /
`c:seeds`.

## 5. Tag assignments (1.20.1)

v1 has no per-material subtags, so 1.20.1 gets the umbrella tags that actually
exist in that convention and nothing else:

| Tag | Members |
| --- | --- |
| `c:ores` | all eight ore families (blocks and items) |
| `c:ingots` | `clockstone`, `enhanced_clockstone` |
| `c:gems` | `time_crystal`, `entropy_crystal` |
| `c:raw_ores` | `raw_temporal_amber` |
| `c:dusts` | `temporal_amber_dust` |
| `c:foods` | every food item listed in §4.6 |
| `c:sandstone_blocks` / `c:sandstone_slabs` / `c:sandstone_stairs` | temporal sandstone set |

Categories absent from v1 (`stones`, `gravels`, `cobblestones`,
`storage_blocks`, `crops`, `seeds`, `mushrooms`, `fences/wooden`,
`stripped_logs`, `stripped_woods`, `drinks`) are **not** emitted on 1.20.1.
Inventing tag names outside the convention produces files no other mod reads.

1.20.1 therefore lands at roughly a third of the 1.21.1+ coverage. That is a
limit of the convention of that era, not an incomplete implementation.

## 6. File layout

| Era | Root | Directory form |
| --- | --- | --- |
| 1.21.1–1.21.11 | `common/shared-1.21.1+/src/main/resources/data/c/tags/` | singular: `item/`, `block/` |
| 1.20.1 | `common/1.20.1/src/main/resources/data/c/tags/` | plural: `items/`, `blocks/` |

This mirrors the existing `data/minecraft/tags/` layout in the same roots,
including the plural-to-singular directory rename that landed in 1.21.1.

No version-specific overrides are created inside the 1.21.x range, because §3
establishes that every tag used is stable across it. If a future MC version
changes one, the override is introduced then, in the narrowest shared root that
covers it (`shared-1.21.5+`, and so on).

No loader-specific files: NeoForge reads the same `c:` namespace as Fabric.

## 7. Verification

A new Gradle task `validateConventionalTags` is added to
`gradle/chronodawn-validation.gradle` (the project-local validation script) and
wired into `checkAll`. The shared submodule `gradle/shared/` is not modified.

**Check 1 — coverage.** A declarative rule file,
`scripts/conventional_tag_rules.json`, maps ID naming patterns to the tag (or
set of alternative tags) that must contain them. Rules are pattern-driven rather
than an enumerated list, so a *future* material inherits the requirement:

- any block ID matching `*_ore` must appear in some `c:ores/*` tag, in `c:ores`,
  and in exactly one of `c:ores_in_ground/stone` or `c:ores_in_ground/deepslate`
- any block ID matching `deepslate_*_ore` must resolve to
  `c:ores_in_ground/deepslate` specifically
- IDs on the storage-block list must appear in some `c:storage_blocks/*`
- IDs on the food list must appear in at least one `c:foods/*` subtag

The check reuses the existing `ModItemId` / `ModBlockId` enum-extraction logic
that `validateData` already relies on, so the ID list is never hand-maintained.

This ordering matters: a mistake in this class is silent. Nothing crashes when a
material is missing from a tag — it simply stops being visible to other mods,
exactly like the "new ore added, biome feature list not updated" defect this
repo has hit before.

**Check 2 — cross-version consistency and tag-name validity.** For every
resource root containing `data/c/`, assert the tag sets agree. The 1.20.1 root
is excluded from that comparison (it follows v1) and is instead validated
against an allowlist of tag names that actually exist in conventional tags v1.
The 1.21.1+ root is validated against a v2 allowlist that also rejects the
deprecated plural food and tool aliases (§3), so an out-of-convention or
typoed-alias tag name fails loudly instead of silently going unread.

**Check 3 — reference integrity.** Already covered: `validateData`'s existing
Check A walks tag entries and asserts they resolve to registered IDs, and it
applies to `data/c/` with no change.

Beyond the new task, each slice is verified with `./gradlew checkAll`.

## 8. Documentation

- `docs/modpack-integration.md`: a new section listing the published `c:` tags
  per era. This is the page pack authors actually read.
- `docs/developer_guide.md`: add `validateConventionalTags` to the validation
  task list.
- `CHANGELOG.md`: one entry per shipped slice.

## 9. Implementation slices

Every slice adds its validation rules together with the tag files they cover, so
each slice ends green rather than leaving the guard failing across commits.

1. `validateConventionalTags` task + rule-file plumbing + the ore rules and the
   ore, material-item, and storage-block tags (1.21.1+). The task and its first
   rules ship together because a rule with no matching tag files is a red build.
2. Stone, sand, gravel, sandstone, wood sets (1.21.1+), plus their rules.
3. Foods, crops, seeds (1.21.1+), plus their rules.
4. 1.20.1 v1 tag set, plus the v1 allowlist check.
5. Documentation pass.

Each slice is one PR-sized commit set and ends green on `checkAll`.

## 10. Follow-on work unblocked

With materials tag-addressable, the next P1 items become straightforward: the
Jade/WTHIT plugin, JEI/EMI information pages, and Curios/Trinkets slots. Biome
and structure tags, and the tool/armor tag slice, are the natural continuations
of this initiative.
