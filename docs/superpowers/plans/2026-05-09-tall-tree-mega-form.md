# Tall Tree Mega-Form Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Convert ChronoDawn's three `_tall` tree variants (dark, ancient, fruit) to mega-form (2x2 trunk + cone-shaped foliage covering ~60-70 % of trunk height) so tall trees stop looking like a leaf cap on a toothpick.

**Architecture:** Edit three sibling worldgen JSONs under `common/shared/src/main/resources/data/chronodawn/worldgen/configured_feature/`. Swap to vanilla `mega_jungle_trunk_placer` + `mega_pine_foliage_placer`, expand `two_layers_feature_size` for the wider footprint, and bump base heights for ancient/fruit so the mega proportions look right. The shared resource directory makes a single edit per file apply to all 11 supported MC versions; no per-version branching.

**Tech Stack:** Minecraft worldgen JSON (data-pack format), Gradle (`validateResources`, `buildAll`).

---

## Spec

See `docs/superpowers/specs/2026-05-09-tall-tree-mega-design.md`.

## File map

| File | Action | Responsibility |
| --- | --- | --- |
| `common/shared/src/main/resources/data/chronodawn/worldgen/configured_feature/dark_time_wood_tree_tall.json` | Modify | dark mega tall (16-24 trunk, 12-16 crown) |
| `common/shared/src/main/resources/data/chronodawn/worldgen/configured_feature/ancient_time_wood_tree_tall.json` | Modify | ancient mega tall (11-15 trunk, 7-10 crown), forking lost |
| `common/shared/src/main/resources/data/chronodawn/worldgen/configured_feature/fruit_of_time_tree_tall.json` | Modify | fruit mega tall (10-14 trunk, 6-9 crown), keep `chronodawn:fruit_decorator` |

No other files require edits. `random` selectors, `placed_feature` JSONs, biome JSONs, and registration code reference the configured features by ID and remain unchanged.

## Notes for the implementer

- All three files share an identical shape; only `Name` references and the four numeric ranges (trunk base/a/b + crown_height min/max) differ. The plan repeats each full JSON intentionally — read carefully, do not paste between files.
- Use the **flat** `minecraft:uniform` IntProvider format (`min_inclusive` / `max_inclusive` directly under `type`, no nested `value` object). This is the project convention; see `common/shared/src/main/resources/data/chronodawn/worldgen/configured_feature/patch_coarse_dirt.json` for a sibling example.
- Always end JSON files with a trailing newline (project rule).
- After each task, only the file(s) listed in that task should be staged. Do not bundle unrelated changes.

---

## Task 1: Convert `dark_time_wood_tree_tall.json` to mega form

**Files:**
- Modify: `common/shared/src/main/resources/data/chronodawn/worldgen/configured_feature/dark_time_wood_tree_tall.json`

- [ ] **Step 1: Replace the file contents**

Overwrite the entire file with:

```json
{
  "type": "minecraft:tree",
  "config": {
    "trunk_provider": {
      "type": "minecraft:simple_state_provider",
      "state": {
        "Name": "chronodawn:dark_time_wood_log",
        "Properties": {
          "axis": "y"
        }
      }
    },
    "trunk_placer": {
      "type": "minecraft:mega_jungle_trunk_placer",
      "base_height": 16,
      "height_rand_a": 5,
      "height_rand_b": 3
    },
    "foliage_provider": {
      "type": "minecraft:simple_state_provider",
      "state": {
        "Name": "chronodawn:dark_time_wood_leaves"
      }
    },
    "foliage_placer": {
      "type": "minecraft:mega_pine_foliage_placer",
      "radius": 0,
      "offset": 0,
      "crown_height": {
        "type": "minecraft:uniform",
        "min_inclusive": 12,
        "max_inclusive": 16
      }
    },
    "force_dirt": false,
    "ignore_vines": true,
    "minimum_size": {
      "type": "minecraft:two_layers_feature_size",
      "limit": 1,
      "lower_size": 1,
      "upper_limit": 2
    },
    "dirt_provider": {
      "type": "minecraft:simple_state_provider",
      "state": {
        "Name": "chronodawn:temporal_dirt"
      }
    },
    "decorators": []
  }
}
```

- [ ] **Step 2: Validate JSON syntax + cross-references**

Run: `./gradlew validateResources`
Expected: BUILD SUCCESSFUL with no errors mentioning `dark_time_wood_tree_tall`.

- [ ] **Step 3: Commit**

```bash
git add common/shared/src/main/resources/data/chronodawn/worldgen/configured_feature/dark_time_wood_tree_tall.json
git commit -m "feat(worldgen): convert dark_time_wood_tree_tall to mega form"
```

---

## Task 2: Convert `ancient_time_wood_tree_tall.json` to mega form

**Files:**
- Modify: `common/shared/src/main/resources/data/chronodawn/worldgen/configured_feature/ancient_time_wood_tree_tall.json`

Heights are bumped (8-13 → 11-15) so even the shortest sample stays clearly tall, and forking is intentionally dropped (mega_jungle_trunk_placer has no branching).

- [ ] **Step 1: Replace the file contents**

Overwrite the entire file with:

```json
{
  "type": "minecraft:tree",
  "config": {
    "trunk_provider": {
      "type": "minecraft:simple_state_provider",
      "state": {
        "Name": "chronodawn:ancient_time_wood_log",
        "Properties": {
          "axis": "y"
        }
      }
    },
    "trunk_placer": {
      "type": "minecraft:mega_jungle_trunk_placer",
      "base_height": 11,
      "height_rand_a": 2,
      "height_rand_b": 2
    },
    "foliage_provider": {
      "type": "minecraft:simple_state_provider",
      "state": {
        "Name": "chronodawn:ancient_time_wood_leaves"
      }
    },
    "foliage_placer": {
      "type": "minecraft:mega_pine_foliage_placer",
      "radius": 0,
      "offset": 0,
      "crown_height": {
        "type": "minecraft:uniform",
        "min_inclusive": 7,
        "max_inclusive": 10
      }
    },
    "force_dirt": false,
    "ignore_vines": true,
    "minimum_size": {
      "type": "minecraft:two_layers_feature_size",
      "limit": 1,
      "lower_size": 1,
      "upper_limit": 2
    },
    "dirt_provider": {
      "type": "minecraft:simple_state_provider",
      "state": {
        "Name": "chronodawn:temporal_dirt"
      }
    },
    "decorators": []
  }
}
```

- [ ] **Step 2: Validate JSON syntax + cross-references**

Run: `./gradlew validateResources`
Expected: BUILD SUCCESSFUL with no errors mentioning `ancient_time_wood_tree_tall`.

- [ ] **Step 3: Commit**

```bash
git add common/shared/src/main/resources/data/chronodawn/worldgen/configured_feature/ancient_time_wood_tree_tall.json
git commit -m "feat(worldgen): convert ancient_time_wood_tree_tall to mega form"
```

---

## Task 3: Convert `fruit_of_time_tree_tall.json` to mega form

**Files:**
- Modify: `common/shared/src/main/resources/data/chronodawn/worldgen/configured_feature/fruit_of_time_tree_tall.json`

Heights bumped (7-11 → 10-14). The `chronodawn:fruit_decorator` entry is preserved.

- [ ] **Step 1: Replace the file contents**

Overwrite the entire file with:

```json
{
  "type": "minecraft:tree",
  "config": {
    "trunk_provider": {
      "type": "minecraft:simple_state_provider",
      "state": {
        "Name": "chronodawn:time_wood_log",
        "Properties": {
          "axis": "y"
        }
      }
    },
    "trunk_placer": {
      "type": "minecraft:mega_jungle_trunk_placer",
      "base_height": 10,
      "height_rand_a": 3,
      "height_rand_b": 1
    },
    "foliage_provider": {
      "type": "minecraft:simple_state_provider",
      "state": {
        "Name": "chronodawn:time_wood_leaves"
      }
    },
    "foliage_placer": {
      "type": "minecraft:mega_pine_foliage_placer",
      "radius": 0,
      "offset": 0,
      "crown_height": {
        "type": "minecraft:uniform",
        "min_inclusive": 6,
        "max_inclusive": 9
      }
    },
    "force_dirt": false,
    "ignore_vines": true,
    "minimum_size": {
      "type": "minecraft:two_layers_feature_size",
      "limit": 1,
      "lower_size": 1,
      "upper_limit": 2
    },
    "dirt_provider": {
      "type": "minecraft:simple_state_provider",
      "state": {
        "Name": "chronodawn:temporal_dirt"
      }
    },
    "decorators": [
      {
        "type": "chronodawn:fruit_decorator"
      }
    ]
  }
}
```

- [ ] **Step 2: Validate JSON syntax + cross-references**

Run: `./gradlew validateResources`
Expected: BUILD SUCCESSFUL with no errors mentioning `fruit_of_time_tree_tall`.

- [ ] **Step 3: Commit**

```bash
git add common/shared/src/main/resources/data/chronodawn/worldgen/configured_feature/fruit_of_time_tree_tall.json
git commit -m "feat(worldgen): convert fruit_of_time_tree_tall to mega form"
```

---

## Task 4: End-to-end build verification

This task confirms the changes load correctly across versions. Visual smoke testing happens here too — JSON validation passes shape checks, but the only way to confirm the trees actually look right is in-game.

**Files:**
- (no edits)

- [ ] **Step 1: Run the full multi-version build**

Run: `./gradlew buildAll`
Expected: BUILD SUCCESSFUL. Worldgen JSONs are loaded as resources by every version subproject; if any version's data-fixer / parser rejects the new shape, this is where it surfaces.

- [ ] **Step 2: Visual smoke test (one version)**

Run: `./gradlew runClientFabric1_21_11`

In-game (creative or by `/locate biome chronodawn:temporal_*` then teleporting):
1. Find a biome that hosts the `_tall` trees. The dark variant lives in temporal forest biomes. Use `/locate biome` to find one quickly.
2. Confirm at least one `dark_time_wood_tree_tall` instance shows: 2x2 trunk, leaves forming a cone tapering down from the top, foliage covers ~60-70 % of trunk height.
3. Confirm at least one `ancient_time_wood_tree_tall` and one `fruit_of_time_tree_tall` show the same trunk + cone shape (heights 11-15 / 10-14, crowns 7-10 / 6-9 respectively).
4. On a `fruit_of_time_tree_tall`, confirm fruit blocks still appear in/under the leaves (decorator still works).

If any of the trees spawn as a 1-block trunk or a flat leaf cap, the JSON did not take effect — recheck the file edits and resource validation.

- [ ] **Step 3: No commit needed**

This task only verifies — nothing was changed. If the visual smoke test surfaces issues, address them as new tasks.

---

## Self-review notes

- Spec coverage: each spec section maps to a task. Tasks 1-3 cover the three target files individually; Task 4 covers spec sections "Verification" and "Risks" (visual confirmation + fruit decorator interaction).
- Placeholder scan: every step has either a complete JSON body or an exact command. No "TBD" or "similar to" references.
- Type consistency: all three files use the same set of placer / size / provider type names; numeric values match the spec table.
- Side-effect "generation density drops" is observational only — no test or task gates on it.
