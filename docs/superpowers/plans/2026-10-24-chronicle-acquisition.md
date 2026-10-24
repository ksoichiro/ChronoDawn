# Chronicle Acquisition Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Grant the Chronicle once per player through an advancement reward, and make it craftable inside Chrono Dawn (Time Wheat → paper → book → Chronicle) with its own texture.

**Architecture:** Pure data for the grant (advancement `rewards.loot` + a loot table) and the recipes (shaped paper, shapeless Chronicle, recipe-unlock advancements), across the 1.20.1, 1.21.1, shared-1.21.1+/1.21.2+ and 26.3 data eras. Java changes only delete the old per-entry grant and fix stale comments. JUnit resource tests in `common/shared/src/test` pin the data on every version.

**Tech Stack:** Minecraft data packs (advancements, loot tables, recipes), Architectury common module (Java), JUnit 5 resource tests.

**Spec:** `docs/superpowers/specs/2026-10-24-chronicle-acquisition-design.md`

## Global Constraints

- Paper recipe: three `chronodawn:time_wheat` in a **vertical** column (`["W","W","W"]`) → 3 `minecraft:paper`.
- Chronicle recipe: shapeless `minecraft:book` + one Time Crystal → 1 `chronodawn:chronicle_book`. 1.20.1 names `chronodawn:time_crystal`. 1.21.1+ uses `#c:gems/time_crystal`, like `clockwork_mechanism`.
- First grant is the `rewards.loot` of `chronodawn:story/us1/dimension_entry`, yielding exactly one `chronodawn:chronicle_book`.
- Unlock advancements: paper by obtaining Time Wheat, Chronicle by obtaining Time Crystal.
- Chronicle stack size stays 64.
- `textures/item/chronicle_book.png` is a vanilla placeholder the user is editing. Never overwrite it and never stage it. The user commits it after replacing it. Do not merge the branch to main until then.
- Data era formats (verified in this repo):
  - Directories: 1.20.1 plural (`advancements/`, `loot_tables/`, `recipes/`), 1.21.1+ singular.
  - Recipe result: 1.20.1 `"item"`, 1.21.1+ `"id"`.
  - Recipe ingredients/keys: 1.20.1 and 1.21.1 `{ "item": ... }` / `{ "tag": ... }`, 1.21.2+ bare strings (`"#c:..."` for tags).
  - `minecraft:recipe_unlocked` condition: `"recipe"` up to 26.2, `"recipes"` in 26.3. 26.3 keeps its own copy of every recipe-unlock advancement under `common/26.3/.../advancement/recipes/`.
- Commits need the user's go-ahead (global rule). GPG signing must run outside the sandbox. End messages with `Co-Authored-By: Claude Opus 5.5 <noreply@anthropic.com>`.

## Review Focus

1. **Full inventory on first entry.** The book must drop at the player's feet, not vanish. Vanilla `AdvancementRewards.grant` already does this. Covered by manual check in Task 4.
2. **Re-entry after storing the book** (chest, Ender Chest, death and respawn into the dimension) must not grant a copy. Covered by the manual check in Task 4. Registry-dependent unit tests are avoided in this project, so the Java removal has no automated test.
3. **Players who earned `dimension_entry` before the update** get nothing new and can still craft one. Manual check in Task 4 on an old save.
4. **Time Bread must stay horizontal-only.** Pinned by `timeBreadStaysHorizontal` in Task 2.
5. **Packs with advancements disabled** can still craft both recipes from the grid. Manual check in Task 4 with the Chronicle unlock advancement revoked.

---

### Task 0: Worktree setup

- [ ] **Step 1:** `pwd` must be the worktree root. Then run `git submodule update --init gradle/shared`. `gradle/shared/` is empty in this worktree.
- [ ] **Step 2:** `./gradlew :common-1.21.1:test -Ptarget_mc_version=1.21.1 --tests '*FragmentSavingTest*'` → BUILD SUCCESSFUL. This confirms the resource-test harness runs.

### Task 1: First grant via advancement reward

**Files:**
- Create: `common/shared/src/test/java/com/chronodawn/unit/ChronicleAcquisitionTest.java`
- Create: `common/shared-1.21.1+/src/main/resources/data/chronodawn/loot_table/advancement_reward/chronicle_book.json`
- Create: `common/1.20.1/src/main/resources/data/chronodawn/loot_tables/advancement_reward/chronicle_book.json`
- Modify: `common/shared-1.21.1+/.../advancement/story/us1/dimension_entry.json`, `common/1.20.1/.../advancements/story/us1/dimension_entry.json`
- Modify: every `PlayerEventHandler.java` that contains `giveChronicleBook`. Today that is `common/shared` plus the copies in 1.21.6, 1.21.7, 1.21.8, 1.21.9, 1.21.10, 1.21.11, 26.1.2, 26.2 and 26.3. Re-check with `grep -rl giveChronicleBook common --include=PlayerEventHandler.java`.
- Modify: every `ChronicleBookItem.java` (14 files), only the `stacksTo(64)` comment.
- Modify: every `ModItems.java` with `grant_chronicle_book` in the `CHRONICLE_BOOK` Javadoc (14 files).

**Interfaces:**
- Produces: loot table ID `chronodawn:advancement_reward/chronicle_book`. Produces the test class `ChronicleAcquisitionTest`, which reuses `FragmentSavingTest.loadData`, `resultId` and `ingredientId` (package-private static). Task 2 adds to this class.

- [ ] **Step 1: Write the failing tests** in `ChronicleAcquisitionTest`:
  - `dimensionEntryRewardsChronicleLoot`: load `advancement/story/us1/dimension_entry.json` (plural fallback `advancements/...`). Assert `rewards.loot == ["chronodawn:advancement_reward/chronicle_book"]`.
  - `chronicleRewardLootTableGivesOneBook`: load `loot_table/advancement_reward/chronicle_book.json` (fallback `loot_tables/...`). Assert `type == "minecraft:advancement_reward"`, exactly one pool with `rolls == 1`, exactly one entry with `type == "minecraft:item"` and `name == "chronodawn:chronicle_book"`, and no `functions`/`modifier` key.
- [ ] **Step 2:** `./gradlew :common-1.21.1:test -Ptarget_mc_version=1.21.1 --tests '*ChronicleAcquisitionTest*'` → both FAIL (missing reward / null table).
- [ ] **Step 3: Add the data.** Add `"rewards": { "loot": ["chronodawn:advancement_reward/chronicle_book"] }` to both `dimension_entry.json` files, after `display`. The loot table is identical in both eras because it has no functions. 26.3 needs no copy for the same reason, and 26.3 has no `dimension_entry` override.
- [ ] **Step 4: Delete the Java grant.** In each listed `PlayerEventHandler`, delete the `giveChronicleBook(player);` call with its comment, plus the `giveChronicleBook` and `hasChronicleBook` methods. Remove any import that becomes unused (for example `ItemStack`, if nothing else uses it). Do not touch the rest of the entry handler.
- [ ] **Step 5: Fix stale comments.**
  - `ChronicleBookItem`: replace `// Stackable like vanilla books, since a copy is granted on each entry without one` with `// Stackable like vanilla books`.
  - `ModItems`: replace the line `Distributed via advancement grant_chronicle_book when entering ChronoDawn.` with `Granted once by the dimension_entry advancement reward, and craftable from a book and a Time Crystal.`
  - Leave the `advancements.chronodawn.grant_chronicle_book.*` lang keys alone.
- [ ] **Step 6:** Run the test from Step 2 on `1.20.1`, `1.21.1` and `26.3` (`:common-<v>:test -Ptarget_mc_version=<v>`) → PASS. Then `./gradlew build1_21_1 build26_3 build1_20_1` → BUILD SUCCESSFUL.
- [ ] **Step 7: Commit** (after user go-ahead), excluding the PNG: `feat: grant the Chronicle once through the dimension entry advancement`.

### Task 2: Paper and Chronicle recipes with unlock advancements

**Files:**
- Create recipes `paper_from_time_wheat.json` and `chronicle_book.json` in `common/1.20.1/.../recipes/`, `common/1.21.1/.../recipe/` and `common/shared-1.21.2+/.../recipe/`.
- Create unlock advancements `recipes/misc/paper_from_time_wheat.json` and `recipes/misc/chronicle_book.json` in `common/1.20.1/.../advancements/`, `common/shared-1.21.1+/.../advancement/` and `common/26.3/.../advancement/`.
- Modify: `ChronicleAcquisitionTest.java`

**Interfaces:**
- Consumes: `ChronicleAcquisitionTest` and the `FragmentSavingTest` helpers from Task 1.
- Produces: recipe IDs `chronodawn:paper_from_time_wheat` and `chronodawn:chronicle_book`.

- [ ] **Step 1: Write the failing tests:**
  - `paperFromTimeWheatIsVerticalColumn`: `pattern == ["W","W","W"]`, `ingredientId(key.W) == "chronodawn:time_wheat"`, `resultId == "minecraft:paper"`, `result.count == 3`.
  - `timeBreadStaysHorizontal`: `time_bread` `pattern == ["WWW"]`.
  - `chronicleRecipeIsBookPlusTimeCrystal`: `type == "minecraft:crafting_shapeless"`. The ingredients mapped through `ingredientId` must equal exactly `["minecraft:book", <crystal>]` as a multiset, where `<crystal>` is `chronodawn:time_crystal` or `#c:gems/time_crystal`. Also `resultId == "chronodawn:chronicle_book"` and `count == 1`.
  - `unlockAdvancementsRewardRecipes` (dynamic, for both IDs):
    - The unlock advancement at `advancement/recipes/misc/<id>.json` (plural fallback) has `rewards.recipes == ["chronodawn:<id>"]`.
    - Its `has_item` items include the trigger item (`chronodawn:time_wheat` / `chronodawn:time_crystal`).
    - Its `has_the_recipe` condition's `recipe` or `recipes` value is `chronodawn:<id>`.
- [ ] **Step 2:** Run on 1.21.1 → the new tests FAIL. `timeBreadStaysHorizontal` passes already.
- [ ] **Step 3: Write the recipes** in each era's format (see Global Constraints). Use `"category": "misc"` and no `group`.
- [ ] **Step 4: Write the unlock advancements.** Copy the `lost_adventurer_journal` unlock advancement of the same era (the 26.3 copy uses `"recipes"`). Swap in the trigger item and recipe ID.
- [ ] **Step 5:** Run `ChronicleAcquisitionTest` on 1.20.1, 1.21.1, 1.21.2 and 26.3 → PASS. Then `./gradlew validateResources validateData` → BUILD SUCCESSFUL.
- [ ] **Step 6: Commit** (after user go-ahead): `feat: craft paper from Time Wheat and the Chronicle from a book and Time Crystal`.

### Task 3: Own item texture

**Files:**
- Modify: `common/shared/src/main/resources/assets/chronodawn/models/item/chronicle_book.json`
- Modify: `ChronicleAcquisitionTest.java`
- Existing, user-owned: `common/shared/src/main/resources/assets/chronodawn/textures/item/chronicle_book.png`

- [ ] **Step 1: Write the failing test** `chronicleModelUsesOwnTexture`. Assert the model's `textures.layer0 == "chronodawn:item/chronicle_book"`, and that the class loader finds the resource `assets/chronodawn/textures/item/chronicle_book.png`.
- [ ] **Step 2:** Run on 1.21.1 → FAIL on `layer0`.
- [ ] **Step 3:** Set `layer0` to `chronodawn:item/chronicle_book`.
- [ ] **Step 4:** Run on 1.20.1 and 26.3 → PASS. Then `./gradlew validateResources` → BUILD SUCCESSFUL.
- [ ] **Step 5: Commit** (after user go-ahead) the model and test only: `feat: give the Chronicle its own item texture`. Tell the user the PNG is still unstaged.

### Task 4: Docs and runtime verification

**Files:**
- Modify: `docs/player_guide.md`:
  - FAQ "How do I get the in-game guide book?" (around line 1408): received once, the first time you enter Chrono Dawn. If lost, craft one from a Book and a Time Crystal. Paper comes from three Time Wheat stacked vertically.
  - `#### Time Wheat` Products (around line 897): add Paper.
- Modify: `README.md:69`, `docs/curseforge_description.md:58`, `docs/modrinth_description.md:58`: change "auto-given/automatically given when entering the dimension" to "given the first time you enter the dimension, and craftable if lost".
- Modify: `CHANGELOG.md` `## [Unreleased]`:
  - Under `### Changed`: the Chronicle is given once per player through the "dimension entry" advancement instead of on every entry without one. Note for pack authors: packs that disable mod advancements should hand the book out themselves.
  - Under `### Added`: the paper and Chronicle recipes, and the new texture.
  - Follow the `doc-maintenance` skill and the CHANGELOG Unreleased convention.

- [ ] **Step 1:** Write the doc changes above. Use the `human-tone` rules (no em dashes or semicolons).
- [ ] **Step 2:** Runtime check on 1.21.1 NeoForge, 1.21.1 Fabric, 26.3 (`runClient…`), and 1.20.1 Forge. The user runs these clients, and you report the steps.
  1. New world, first entry → exactly one Chronicle.
  2. Store it in a chest, die and respawn or re-enter → no new copy.
  3. Enter with a full inventory in a fresh world, in survival mode → the book drops at your feet. In creative mode vanilla `Inventory.add` deletes the item instead, so this check does not apply there.
  4. Pick up Time Wheat and Time Crystal → recipe-unlock toasts. Crafting chain works: vertical Time Wheat → paper, book, then book + Time Crystal → Chronicle. Horizontal Time Wheat still gives Time Bread.
  5. `/advancement revoke @s only chronodawn:recipes/misc/chronicle_book` → the recipe still crafts from the grid.
  6. An old save where `dimension_entry` is already earned → no new grant on entry.
  7. The Chronicle shows the user's texture.
- [ ] **Step 3:** `./gradlew checkAll > <worktree>/build/checkall.log 2>&1` → BUILD SUCCESSFUL.
- [ ] **Step 4: Commit** (after user go-ahead): `docs: describe the one-time Chronicle grant and its recipes`.
