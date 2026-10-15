# Ancient Gear Fragment-Saving Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Add a Clockwork Mechanism crafted from Ancient Gears, alternative artifact recipes that need only 1 Fragment of Stasis Core each, and raise the Time Tyrant Fragment drop to 4-6.

**Architecture:** Mostly data: one new plain `Item` registered in all 14 version modules, recipe and recipe-unlock advancement JSON in each data-format era, and a one-line loot table change in three era copies. A shared JUnit test reads the JSON from the classpath of every version module, and the existing recipe-load GameTest gets the new recipe IDs.

**Tech Stack:** Java 17/21/25, Architectury, Minecraft data packs (1.20.1, 1.21.1, 1.21.2+ and 26.3 schemas), JUnit 5, GameTest.

**Spec:** `docs/superpowers/specs/2026-10-14-ancient-gear-fragment-saving-design.md`

## Global Constraints

- Supported version modules: `1.20.1 1.21.1 1.21.2 1.21.4 1.21.5 1.21.6 1.21.7 1.21.8 1.21.9 1.21.10 1.21.11 26.1.2 26.2 26.3`
- New item ID: `clockwork_mechanism`. Enum constant `CLOCKWORK_MECHANISM`. English name "Clockwork Mechanism", Japanese name 「時計仕掛けの機構」
- Mechanism recipe: pattern `CGC / GEG / CGC`, `G` = Ancient Gear, `E` = Enhanced Clockstone, `C` = Time Crystal, yields 1, category `misc`
- Alternative recipe IDs: `<artifact>_from_clockwork_mechanism`, same category as the original artifact recipe (`equipment`)
- Alternative patterns: Chronoblade `MCM / FEM / " U "`, Time Tyrant Mail `M M / MEM / FUM`, Echoing Time Boots `F M / E E`, Unstable Pocket Watch `" F " / MUM / " E "`
- Time Tyrant Fragment pool: `uniform 4-6`. The Looting bonus stays as it is
- Existing Fragment-only recipes are not changed
- Every new file ends with a newline
- Edit lang JSON line by line (sed or Edit). Never round-trip lang files through `json.dumps`, it strips the blank grouping lines
- Before the first `./gradlew` in this worktree, run `git submodule update --init gradle/shared`
- Write long Gradle output to `<worktree>/build/<name>.log`, not `$TMPDIR`
- Commits are GPG-signed and fail inside the sandbox. Run `git commit` with the sandbox disabled. Commit only after the user has approved execution of this plan

## Review Focus

1. **1.20.1 recipe format.** 1.20.1 has no `c:` tag for these materials in the existing recipes and uses `{"item": ...}` keys and `"result": {"item": ...}`. A recipe copied from 1.21.2+ fails to load silently. Pinned by the recipe-load GameTest (Task 3) and the shared unit test, which reads the 1.20.1 classpath.
2. **26.3 advancement schema.** 26.3 has its own full copy of `advancement/` with `"recipes"` instead of `"recipe"` in `recipe_unlocked`. A file added only to `shared-1.21.1+` leaves 26.3 without an unlock. Pinned by the advancement coverage check in Task 3 Step 6.
3. **26.3 loot schema.** The 26.3 Tyrant loot table uses `"modifier"` with `"type"` keys. The unit test in Task 1 reads either shape, so editing only the shared copy fails the 26.3 run.
4. **Recipe book visibility.** Without an unlock advancement the recipe works but never appears in the recipe book. Static tests cannot see this. Covered by the manual check in Task 5.
5. **Mixed Fragment/Mechanism grids.** A player who places 2 Fragments and 2 Mechanisms should get nothing for the Chronoblade. Shaped recipes guarantee this as long as each alternative uses exactly the pattern in Global Constraints. The unit test in Task 3 pins the exact counts.

---

### Task 1: Time Tyrant drops 4-6 Fragments

**Files:**
- Create: `common/shared/src/test/java/com/chronodawn/unit/FragmentSavingTest.java`
- Modify: `common/1.20.1/src/main/resources/data/chronodawn/loot_tables/entities/time_tyrant.json:16-17`
- Modify: `common/shared-1.21.1+/src/main/resources/data/chronodawn/loot_table/entities/time_tyrant.json:16-17`
- Modify: `common/26.3/src/main/resources/data/chronodawn/loot_table/entities/time_tyrant.json:16-17`
- Modify (comments only): `common/<v>/src/main/java/com/chronodawn/items/base/FragmentOfStasisCoreItem.java` and `common/<v>/src/main/java/com/chronodawn/registry/ModItems.java` for all 14 versions

**Interfaces:**
- Produces: `FragmentSavingTest` with private helpers `loadData(String singularPath, String pluralPath)`, `ingredientId(Object keyValue)`, `countIngredient(Map recipe, String id)`, `resultId(Map recipe)`. Task 3 adds tests to this class and reuses the helpers.

- [ ] **Step 1: Write the failing test**

```java
package com.chronodawn.unit;

import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

/**
 * Pins the numbers that let one Time Tyrant kill cover all four ultimate
 * artifacts through the Clockwork Mechanism recipes: at least 4 Fragments
 * per kill, and exactly 1 Fragment per alternative recipe.
 */
public class FragmentSavingTest {

    private static final String FRAGMENT = "chronodawn:fragment_of_stasis_core";

    @Test
    void timeTyrantDropsFourToSixFragments() {
        Map<String, Object> table = loadData(
            "loot_table/entities/time_tyrant.json",
            "loot_tables/entities/time_tyrant.json");
        assertNotNull(table, "time_tyrant loot table is not on the classpath");

        Map<String, Object> count = findFragmentSetCount(table);
        assertNotNull(count, "No set_count function on the Fragment entry");
        assertEquals(4.0, ((Number) count.get("min")).doubleValue(), "Fragment min");
        assertEquals(6.0, ((Number) count.get("max")).doubleValue(), "Fragment max");
    }

    // 1.20.1 uses plural data directories, 1.21.1+ uses singular ones.
    static Map<String, Object> loadData(String singularPath, String pluralPath) {
        Map<String, Object> json = TestUtils.loadJsonResource("data/chronodawn/" + singularPath);
        if (json == null) {
            json = TestUtils.loadJsonResource("data/chronodawn/" + pluralPath);
        }
        return json;
    }

    // 26.3 lists entry functions under "modifier" with a "type" key. Older
    // versions use "functions" with a "function" key.
    @SuppressWarnings("unchecked")
    private static Map<String, Object> findFragmentSetCount(Map<String, Object> table) {
        for (Object poolObj : (List<Object>) table.get("pools")) {
            Map<String, Object> pool = (Map<String, Object>) poolObj;
            for (Object entryObj : (List<Object>) pool.get("entries")) {
                Map<String, Object> entry = (Map<String, Object>) entryObj;
                if (!FRAGMENT.equals(entry.get("name"))) continue;
                Object fns = entry.containsKey("functions") ? entry.get("functions") : entry.get("modifier");
                for (Object fnObj : (List<Object>) fns) {
                    Map<String, Object> fn = (Map<String, Object>) fnObj;
                    Object type = fn.containsKey("function") ? fn.get("function") : fn.get("type");
                    if ("minecraft:set_count".equals(type)) {
                        return (Map<String, Object>) fn.get("count");
                    }
                }
            }
        }
        return null;
    }

    // Recipe key values are a bare string in 1.21.2+, and {"item": ...} or
    // {"tag": ...} in 1.21.1 and 1.20.1. Tags are returned with a leading '#'.
    @SuppressWarnings("unchecked")
    static String ingredientId(Object keyValue) {
        if (keyValue instanceof String s) return s;
        Map<String, Object> m = (Map<String, Object>) keyValue;
        if (m.containsKey("item")) return (String) m.get("item");
        return "#" + m.get("tag");
    }

    @SuppressWarnings("unchecked")
    static int countIngredient(Map<String, Object> recipe, String id) {
        Map<String, Object> key = (Map<String, Object>) recipe.get("key");
        int n = 0;
        for (Object rowObj : (List<Object>) recipe.get("pattern")) {
            for (char c : ((String) rowObj).toCharArray()) {
                if (c == ' ') continue;
                if (id.equals(ingredientId(key.get(String.valueOf(c))))) n++;
            }
        }
        return n;
    }

    // 1.20.1 names the result "item", 1.21.1+ names it "id".
    @SuppressWarnings("unchecked")
    static String resultId(Map<String, Object> recipe) {
        Map<String, Object> result = (Map<String, Object>) recipe.get("result");
        return (String) (result.containsKey("id") ? result.get("id") : result.get("item"));
    }
}
```

- [ ] **Step 2: Run test to verify it fails**

```bash
git submodule update --init gradle/shared
./gradlew :common-1.21.1:test -Ptarget_mc_version=1.21.1 --tests 'com.chronodawn.unit.FragmentSavingTest' > build/task1.log 2>&1; tail -30 build/task1.log
```

Expected: FAIL, `Fragment min ==> expected: <4.0> but was: <3.0>`.

- [ ] **Step 3: Change the loot tables**

In each of the three files, line 16 is `"min": 3,` and line 17 is `"max": 5` inside the Fragment entry's `set_count`. Check with `sed -n 14,18p <file>` first, then:

```bash
for f in \
  common/1.20.1/src/main/resources/data/chronodawn/loot_tables/entities/time_tyrant.json \
  common/shared-1.21.1+/src/main/resources/data/chronodawn/loot_table/entities/time_tyrant.json \
  common/26.3/src/main/resources/data/chronodawn/loot_table/entities/time_tyrant.json; do
  sed -i '' -e '16s/"min": 3,/"min": 4,/' -e '17s/"max": 5/"max": 6/' "$f"
  sed -n 14,18p "$f"
done
```

Expected output for each file shows `"min": 4,` and `"max": 6`.

- [ ] **Step 4: Update the stale "3-5" comments**

```bash
grep -rln "3-5 per kill\|3-5 fragments" common/*/src/main/java | xargs sed -i '' -e 's/3-5 per kill/4-6 per kill/' -e 's/3-5 fragments per kill/4-6 fragments per kill/'
grep -rn "3-5 per kill\|3-5 fragments" common/*/src/main/java
```

Expected: the second grep prints nothing.

- [ ] **Step 5: Run the test on three schema eras**

```bash
for v in 1.20.1 1.21.1 26.3; do
  ./gradlew :common-$v:test -Ptarget_mc_version=$v --tests 'com.chronodawn.unit.FragmentSavingTest' > build/task1-$v.log 2>&1 && echo "$v PASS" || { echo "$v FAIL"; tail -30 build/task1-$v.log; }
done
```

Expected: `1.20.1 PASS`, `1.21.1 PASS`, `26.3 PASS`.

- [ ] **Step 6: Commit**

```bash
git add common/shared/src/test/java/com/chronodawn/unit/FragmentSavingTest.java common/*/src/main/resources/data/chronodawn/loot_table*/entities/time_tyrant.json common/shared-1.21.1+/src/main/resources/data/chronodawn/loot_table/entities/time_tyrant.json common/*/src/main/java/com/chronodawn/items/base/FragmentOfStasisCoreItem.java common/*/src/main/java/com/chronodawn/registry/ModItems.java
git commit -m "feat: raise the Time Tyrant Fragment drop to 4-6"
```

---

### Task 2: Clockwork Mechanism item

**Files:**
- Modify: `common/shared/src/main/java/com/chronodawn/registry/ModItemId.java:319`
- Modify: `common/<v>/src/main/java/com/chronodawn/registry/ModItems.java` for all 14 versions (field after `ANCIENT_GEAR`, creative tab entry after `output.accept(ANCIENT_GEAR.get());`)
- Create: `common/shared/src/main/resources/assets/chronodawn/textures/item/clockwork_mechanism.png`
- Create: `common/shared/src/main/resources/assets/chronodawn/models/item/clockwork_mechanism.json`
- Create: `common/1.21.4/src/main/resources/assets/chronodawn/items/clockwork_mechanism.json`
- Create: `common/shared-1.21.5+/src/main/resources/assets/chronodawn/items/clockwork_mechanism.json`
- Modify: `en_us.json` and `ja_jp.json` in `common/1.20.1/.../lang/`, `common/1.21.1/.../lang/`, `common/shared-1.21.2+/.../lang/`

**Interfaces:**
- Produces: `ModItemId.CLOCKWORK_MECHANISM` (id `"clockwork_mechanism"`) and `ModItems.CLOCKWORK_MECHANISM` (`RegistrySupplier<Item>`). Item ID `chronodawn:clockwork_mechanism` is used by Task 3 JSON.

The existing completeness tests are the failing tests for this task: `ResourceValidationTest` (item model exists), `CreativeTabCompletenessTest` (every item is in the tab) and the `TranslationKeyTests` GameTest.

- [ ] **Step 1: Add the ID**

In `ModItemId.java`, after `ANCIENT_GEAR("ancient_gear"),` add:

```java
    CLOCKWORK_MECHANISM("clockwork_mechanism"),
```

- [ ] **Step 2: Register the item in all 14 modules**

In each `common/<v>/src/main/java/com/chronodawn/registry/ModItems.java`, insert right after the `ANCIENT_GEAR` field's closing `);` a field whose lambda is a copy of the `ENTROPY_CRYSTAL` lambda **from the same file** with `ENTROPY_CRYSTAL` replaced by `CLOCKWORK_MECHANISM`. This keeps the era-specific constructor (`new Item.Properties()` in 1.20.1/1.21.1, `.setId(... ResourceLocation ...)` in 1.21.2-1.21.10, `.setId(... Identifier ...)` in 1.21.11 and 26.x). For 1.21.2 the result is:

```java

    /**
     * Clockwork Mechanism - Crafting material.
     * Crafted from 4 Ancient Gears, 1 Enhanced Clockstone and 4 Time Crystals.
     * Replaces Fragments of Stasis Core in the alternative ultimate artifact recipes.
     */
    public static final RegistrySupplier<Item> CLOCKWORK_MECHANISM = ITEMS.register(
        ModItemId.CLOCKWORK_MECHANISM.id(),
        () -> new Item(new Item.Properties()
                .setId(ResourceKey.create(Registries.ITEM,
                    ResourceLocation.fromNamespaceAndPath(ChronoDawn.MOD_ID, ModItemId.CLOCKWORK_MECHANISM.id()))))
    );
```

Then after `output.accept(ANCIENT_GEAR.get());` add:

```java
        output.accept(CLOCKWORK_MECHANISM.get());
```

Verify all 14 got both lines:

```bash
for v in 1.20.1 1.21.1 1.21.2 1.21.4 1.21.5 1.21.6 1.21.7 1.21.8 1.21.9 1.21.10 1.21.11 26.1.2 26.2 26.3; do
  echo "$v $(grep -c 'CLOCKWORK_MECHANISM' common/$v/src/main/java/com/chronodawn/registry/ModItems.java)"
done
```

Expected: every line ends with `3` (field name, `ModItemId.CLOCKWORK_MECHANISM` in `register`, creative tab). 1.21.2+ lines show `4` because `setId` repeats the ID.

- [ ] **Step 3: Item model and client items**

`common/shared/src/main/resources/assets/chronodawn/models/item/clockwork_mechanism.json`:

```json
{
  "parent": "minecraft:item/generated",
  "textures": {
    "layer0": "chronodawn:item/clockwork_mechanism"
  }
}
```

`common/1.21.4/src/main/resources/assets/chronodawn/items/clockwork_mechanism.json` and `common/shared-1.21.5+/src/main/resources/assets/chronodawn/items/clockwork_mechanism.json`, same content:

```json
{"model":{"type":"minecraft:model","model":"chronodawn:item/clockwork_mechanism"}}
```

- [ ] **Step 4: Texture**

Read the `texture-creation` skill first. Make a 16x16 texture that reads as several gears around a cyan core: take `textures/item/ancient_gear.png` as the base and recolor its center pixels with the cyan of `textures/item/time_crystal.png`. Example with Pillow, run outside the repo so nothing is left behind:

```bash
cat > "$TMPDIR/make_mechanism.py" <<'EOF'
import sys
from PIL import Image
gear = Image.open(sys.argv[1]).convert("RGBA")
crystal = Image.open(sys.argv[2]).convert("RGBA")
# Most saturated opaque crystal pixel as the core color
core = max((p for p in crystal.getdata() if p[3] > 0),
           key=lambda p: max(p[:3]) - min(p[:3]))
out = gear.copy()
for x in range(6, 10):
    for y in range(6, 10):
        if out.getpixel((x, y))[3] > 0:
            out.putpixel((x, y), core)
out.save(sys.argv[3])
EOF
T=common/shared/src/main/resources/assets/chronodawn/textures/item
uv run --with pillow python -I "$TMPDIR/make_mechanism.py" $T/ancient_gear.png $T/time_crystal.png $T/clockwork_mechanism.png
```

The user reviews the texture in game during Task 5 and may replace it.

- [ ] **Step 5: Lang entries**

Insert after the `item.chronodawn.ancient_gear` line in each file:

```bash
for d in common/1.20.1 common/1.21.1 common/shared-1.21.2+; do
  L=$d/src/main/resources/assets/chronodawn/lang
  sed -i '' '/"item.chronodawn.ancient_gear":/a\
  "item.chronodawn.clockwork_mechanism": "Clockwork Mechanism",
' $L/en_us.json
  sed -i '' '/"item.chronodawn.ancient_gear":/a\
  "item.chronodawn.clockwork_mechanism": "時計仕掛けの機構",
' $L/ja_jp.json
  grep -n "clockwork_mechanism" $L/en_us.json $L/ja_jp.json
done
```

Expected: six lines, each directly after the `ancient_gear` line number.

- [ ] **Step 6: Run the completeness tests and validators**

```bash
./gradlew validateResources validateTranslations > build/task2-validate.log 2>&1; tail -5 build/task2-validate.log
for v in 1.20.1 1.21.1 1.21.4 26.3; do
  ./gradlew :common-$v:test -Ptarget_mc_version=$v > build/task2-$v.log 2>&1 && echo "$v PASS" || { echo "$v FAIL"; grep -m5 "FAILED" build/task2-$v.log; }
done
```

Expected: `BUILD SUCCESSFUL` and four `PASS` lines.

- [ ] **Step 7: Commit**

```bash
git add common/shared/src/main/java/com/chronodawn/registry/ModItemId.java common/*/src/main/java/com/chronodawn/registry/ModItems.java common/shared/src/main/resources/assets/chronodawn/textures/item/clockwork_mechanism.png common/shared/src/main/resources/assets/chronodawn/models/item/clockwork_mechanism.json common/1.21.4/src/main/resources/assets/chronodawn/items/clockwork_mechanism.json common/shared-1.21.5+/src/main/resources/assets/chronodawn/items/clockwork_mechanism.json common/*/src/main/resources/assets/chronodawn/lang/*.json
git commit -m "feat: add the Clockwork Mechanism crafting material"
```

---

### Task 3: Mechanism and alternative artifact recipes

**Files:**
- Modify: `common/shared/src/test/java/com/chronodawn/unit/FragmentSavingTest.java`
- Modify: `common/gametest/src/main/java/com/chronodawn/gametest/RecipeLoadTests.java`
- Modify: `common/<v>/src/main/java/com/chronodawn/gametest/RegistryDrivenTestGenerator.java` for all 14 versions
- Create (5 recipes in each of 3 dirs): `common/1.20.1/src/main/resources/data/chronodawn/recipes/`, `common/1.21.1/src/main/resources/data/chronodawn/recipe/`, `common/shared-1.21.2+/src/main/resources/data/chronodawn/recipe/`: `clockwork_mechanism.json`, `chronoblade_from_clockwork_mechanism.json`, `time_tyrant_mail_from_clockwork_mechanism.json`, `echoing_time_boots_from_clockwork_mechanism.json`, `unstable_pocket_watch_from_clockwork_mechanism.json`
- Create (5 unlock advancements in each of 3 dirs): `common/1.20.1/src/main/resources/data/chronodawn/advancements/recipes/`, `common/shared-1.21.1+/src/main/resources/data/chronodawn/advancement/recipes/`, `common/26.3/src/main/resources/data/chronodawn/advancement/recipes/`: `misc/clockwork_mechanism.json`, `combat/chronoblade_from_clockwork_mechanism.json`, `combat/time_tyrant_mail_from_clockwork_mechanism.json`, `combat/echoing_time_boots_from_clockwork_mechanism.json`, `tools/unstable_pocket_watch_from_clockwork_mechanism.json`

**Interfaces:**
- Consumes: `FragmentSavingTest.loadData`, `ingredientId`, `countIngredient`, `resultId` (Task 1). Item `chronodawn:clockwork_mechanism` (Task 2).
- Produces: `RecipeLoadTests.generateClockworkMechanismRecipeLoadTests(MobBehaviorTests.TestFactory<T>)` and `RegistryDrivenTestGenerator.generateClockworkMechanismRecipeLoadTests()`.

- [ ] **Step 1: Write the failing unit tests**

Add to `FragmentSavingTest` (imports: `org.junit.jupiter.api.DynamicTest`, `org.junit.jupiter.api.TestFactory`, `java.util.ArrayList`, `java.util.Collection`):

```java
    private static final String MECHANISM = "chronodawn:clockwork_mechanism";

    private record Alternative(String recipe, String result, int mechanisms) {}

    private static final List<Alternative> ALTERNATIVES = List.of(
        new Alternative("chronoblade_from_clockwork_mechanism", "chronodawn:chronoblade", 3),
        new Alternative("time_tyrant_mail_from_clockwork_mechanism", "chronodawn:time_tyrant_mail", 5),
        new Alternative("echoing_time_boots_from_clockwork_mechanism", "chronodawn:echoing_time_boots", 1),
        new Alternative("unstable_pocket_watch_from_clockwork_mechanism", "chronodawn:unstable_pocket_watch", 2)
    );

    @TestFactory
    Collection<DynamicTest> alternativeRecipesUseOneFragment() {
        Collection<DynamicTest> tests = new ArrayList<>();
        for (Alternative alt : ALTERNATIVES) {
            tests.add(DynamicTest.dynamicTest(alt.recipe(), () -> {
                Map<String, Object> recipe = loadData(
                    "recipe/" + alt.recipe() + ".json", "recipes/" + alt.recipe() + ".json");
                assertNotNull(recipe, "Missing recipe " + alt.recipe());
                assertEquals(alt.result(), resultId(recipe), "result");
                assertEquals(1, countIngredient(recipe, FRAGMENT), "Fragments");
                assertEquals(alt.mechanisms(), countIngredient(recipe, MECHANISM), "Mechanisms");
            }));
        }
        return tests;
    }

    @Test
    @SuppressWarnings("unchecked")
    void mechanismRecipeUsesFourGearsOneClockstoneFourCrystals() {
        Map<String, Object> recipe = loadData("recipe/clockwork_mechanism.json", "recipes/clockwork_mechanism.json");
        assertNotNull(recipe, "Missing recipe clockwork_mechanism");
        assertEquals(MECHANISM, resultId(recipe), "result");
        assertEquals(1.0, ((Number) ((Map<String, Object>) recipe.get("result")).get("count")).doubleValue(), "count");
        assertEquals(4, countIngredient(recipe, "chronodawn:ancient_gear"), "Ancient Gears");

        // 1.20.1 names the item directly, 1.21.1+ uses the c: tag.
        int clockstone = countIngredient(recipe, "chronodawn:enhanced_clockstone")
            + countIngredient(recipe, "#c:ingots/enhanced_clockstone");
        int crystal = countIngredient(recipe, "chronodawn:time_crystal")
            + countIngredient(recipe, "#c:gems/time_crystal");
        assertEquals(1, clockstone, "Enhanced Clockstone");
        assertEquals(4, crystal, "Time Crystals");
    }
```

- [ ] **Step 2: Run to verify it fails**

```bash
./gradlew :common-1.21.1:test -Ptarget_mc_version=1.21.1 --tests 'com.chronodawn.unit.FragmentSavingTest' > build/task3.log 2>&1; grep -E "FAILED|Missing recipe" build/task3.log | head
```

Expected: FAIL with `Missing recipe ...` for all five recipes.

- [ ] **Step 3: Write the Mechanism recipes**

`common/shared-1.21.2+/src/main/resources/data/chronodawn/recipe/clockwork_mechanism.json`:

```json
{
  "type": "minecraft:crafting_shaped",
  "category": "misc",
  "pattern": [
    "CGC",
    "GEG",
    "CGC"
  ],
  "key": {
    "G": "chronodawn:ancient_gear",
    "E": "#c:ingots/enhanced_clockstone",
    "C": "#c:gems/time_crystal"
  },
  "result": {
    "id": "chronodawn:clockwork_mechanism",
    "count": 1
  }
}
```

`common/1.21.1/src/main/resources/data/chronodawn/recipe/clockwork_mechanism.json`:

```json
{
  "type": "minecraft:crafting_shaped",
  "category": "misc",
  "pattern": [
    "CGC",
    "GEG",
    "CGC"
  ],
  "key": {
    "G": {
      "item": "chronodawn:ancient_gear"
    },
    "E": {
      "tag": "c:ingots/enhanced_clockstone"
    },
    "C": {
      "tag": "c:gems/time_crystal"
    }
  },
  "result": {
    "id": "chronodawn:clockwork_mechanism",
    "count": 1
  }
}
```

`common/1.20.1/src/main/resources/data/chronodawn/recipes/clockwork_mechanism.json`:

```json
{
  "type": "minecraft:crafting_shaped",
  "category": "misc",
  "pattern": [
    "CGC",
    "GEG",
    "CGC"
  ],
  "key": {
    "G": {
      "item": "chronodawn:ancient_gear"
    },
    "E": {
      "item": "chronodawn:enhanced_clockstone"
    },
    "C": {
      "item": "chronodawn:time_crystal"
    }
  },
  "result": {
    "item": "chronodawn:clockwork_mechanism",
    "count": 1
  }
}
```

- [ ] **Step 4: Generate the alternative artifact recipes from the originals**

Each alternative is the original recipe in the same directory with a new pattern and an added `M` key in that era's key format. A script keeps the era format exact:

```bash
cat > "$TMPDIR/make_alt_recipes.py" <<'EOF'
import json, os, sys
PATTERNS = {
    "chronoblade": ["MCM", "FEM", " U "],
    "time_tyrant_mail": ["M M", "MEM", "FUM"],
    "echoing_time_boots": ["F M", "E E"],
    "unstable_pocket_watch": [" F ", "MUM", " E "],
}
MECH = "chronodawn:clockwork_mechanism"
for d in sys.argv[1:]:
    for name, pattern in PATTERNS.items():
        with open(os.path.join(d, name + ".json")) as f:
            r = json.load(f)
        r["pattern"] = pattern
        frag = r["key"]["F"]
        # Match the era's key shape: bare string (1.21.2+) or {"item": ...}
        r["key"]["M"] = MECH if isinstance(frag, str) else {"item": MECH}
        out = os.path.join(d, name + "_from_clockwork_mechanism.json")
        with open(out, "w") as f:
            json.dump(r, f, indent=2, ensure_ascii=False)
            f.write("\n")
        print(out, r["pattern"])
EOF
python3 -I "$TMPDIR/make_alt_recipes.py" \
  common/1.20.1/src/main/resources/data/chronodawn/recipes \
  common/1.21.1/src/main/resources/data/chronodawn/recipe \
  common/shared-1.21.2+/src/main/resources/data/chronodawn/recipe
```

Expected: 12 lines, each pattern matching Global Constraints. Spot-check one file per era with `cat`.

- [ ] **Step 5: Unlock advancements**

Each alternative unlocks on picking up a Clockwork Mechanism or a Fragment. The Mechanism recipe unlocks on picking up an Ancient Gear. Generate from the existing artifact advancement in the same directory so each era keeps its schema (26.3 uses `"recipes"` in `recipe_unlocked`):

```bash
cat > "$TMPDIR/make_alt_advancements.py" <<'EOF'
import json, os, sys
ALTS = {
    "combat/chronoblade": "combat/chronoblade",
    "combat/time_tyrant_mail": "combat/time_tyrant_mail",
    "combat/echoing_time_boots": "combat/echoing_time_boots",
    "tools/unstable_pocket_watch": "tools/unstable_pocket_watch",
}
def write(path, adv):
    with open(path, "w") as f:
        json.dump(adv, f, indent=2, ensure_ascii=False)
        f.write("\n")
    print(path)
def retarget(adv, recipe_id, items):
    adv["criteria"]["has_item"]["conditions"]["items"][0]["items"] = items
    cond = adv["criteria"]["has_the_recipe"]["conditions"]
    key = "recipes" if "recipes" in cond else "recipe"
    cond[key] = recipe_id
    adv["rewards"]["recipes"] = [recipe_id]
    return adv
for d in sys.argv[1:]:
    for src in ALTS:
        with open(os.path.join(d, src + ".json")) as f:
            adv = json.load(f)
        name = os.path.basename(src) + "_from_clockwork_mechanism"
        rid = "chronodawn:" + name
        retarget(adv, rid, ["chronodawn:clockwork_mechanism", "chronodawn:fragment_of_stasis_core"])
        write(os.path.join(d, os.path.dirname(src), name + ".json"), adv)
    with open(os.path.join(d, "combat/echoing_time_boots.json")) as f:
        adv = json.load(f)
    retarget(adv, "chronodawn:clockwork_mechanism", ["chronodawn:ancient_gear"])
    write(os.path.join(d, "misc/clockwork_mechanism.json"), adv)
EOF
python3 -I "$TMPDIR/make_alt_advancements.py" \
  common/1.20.1/src/main/resources/data/chronodawn/advancements/recipes \
  common/shared-1.21.1+/src/main/resources/data/chronodawn/advancement/recipes \
  common/26.3/src/main/resources/data/chronodawn/advancement/recipes
```

Expected: 15 paths printed.

- [ ] **Step 6: Check 26.3 advancement coverage**

```bash
diff <(cd common/shared-1.21.1+/src/main/resources/data/chronodawn/advancement/recipes && find . -name '*.json' | sort) \
     <(cd common/26.3/src/main/resources/data/chronodawn/advancement/recipes && find . -name '*.json' | sort)
grep -l '"recipes": "chronodawn:' common/26.3/src/main/resources/data/chronodawn/advancement/recipes/*/*clockwork_mechanism*.json | wc -l
```

Expected: `diff` prints nothing, and the count is `5`.

- [ ] **Step 7: Run the unit test on three eras**

```bash
for v in 1.20.1 1.21.1 26.3; do
  ./gradlew :common-$v:test -Ptarget_mc_version=$v --tests 'com.chronodawn.unit.FragmentSavingTest' > build/task3-$v.log 2>&1 && echo "$v PASS" || { echo "$v FAIL"; grep -m5 "FAILED\|expected" build/task3-$v.log; }
done
```

Expected: three `PASS` lines.

- [ ] **Step 8: Add the recipe-load GameTest**

In `RecipeLoadTests.java`, add after `generateChroniteRecipeLoadTests`:

```java
    /**
     * Clockwork Mechanism recipes: the Mechanism itself and the alternative
     * ultimate artifact recipes that take 1 Fragment of Stasis Core.
     */
    private static final List<String> CLOCKWORK_MECHANISM_RECIPE_IDS = List.of(
        "clockwork_mechanism",
        "chronoblade_from_clockwork_mechanism",
        "time_tyrant_mail_from_clockwork_mechanism",
        "echoing_time_boots_from_clockwork_mechanism",
        "unstable_pocket_watch_from_clockwork_mechanism"
    );

    public static <T> List<T> generateClockworkMechanismRecipeLoadTests(MobBehaviorTests.TestFactory<T> factory) {
        return generateRecipeLoadTests(CLOCKWORK_MECHANISM_RECIPE_IDS, factory);
    }
```

Then move the loop body of `generateChroniteRecipeLoadTests` into a private method `generateRecipeLoadTests(List<String> recipeIds, MobBehaviorTests.TestFactory<T> factory)` (generic `<T>`, same body, iterating `recipeIds`), and make `generateChroniteRecipeLoadTests` return `generateRecipeLoadTests(CHRONITE_RECIPE_IDS, factory)`. Test names stay `"recipe_loaded_" + recipeId`.

In each of the 14 `RegistryDrivenTestGenerator.java`, add after `generateChroniteRecipeLoadTests()`:

```java
    /**
     * Generates tests verifying Clockwork Mechanism recipes are loaded by the server's RecipeManager.
     */
    public static List<NamedTest> generateClockworkMechanismRecipeLoadTests() {
        return RecipeLoadTests.generateClockworkMechanismRecipeLoadTests(NamedTest::new);
    }
```

and after `all.addAll(generateChroniteRecipeLoadTests());` add:

```java
        all.addAll(generateClockworkMechanismRecipeLoadTests());
```

Check: `grep -c "generateClockworkMechanismRecipeLoadTests" common/*/src/main/java/com/chronodawn/gametest/RegistryDrivenTestGenerator.java` prints `2` for all 14 files.

- [ ] **Step 9: Run the GameTests on the eras with distinct recipe formats**

```bash
./gradlew :fabric:runGameTest -Ptarget_mc_version=1.20.1 > build/task3-gt-1.20.1.log 2>&1; grep -E "recipe_loaded_.*clockwork|tests? (passed|failed)|FAILED" build/task3-gt-1.20.1.log | tail -10
./gradlew :neoforge:runGameTestServer -Ptarget_mc_version=1.21.1 > build/task3-gt-1.21.1.log 2>&1; grep -E "recipe_loaded_.*clockwork|tests? (passed|failed)|FAILED" build/task3-gt-1.21.1.log | tail -10
./gradlew :neoforge:runGameTestServer -Ptarget_mc_version=26.3 > build/task3-gt-26.3.log 2>&1; grep -E "recipe_loaded_.*clockwork|tests? (passed|failed)|FAILED" build/task3-gt-26.3.log | tail -10
```

Expected: all GameTests pass in each run, including the five `recipe_loaded_*clockwork_mechanism` tests.

- [ ] **Step 10: Commit**

```bash
git add common/shared/src/test/java/com/chronodawn/unit/FragmentSavingTest.java common/gametest/src/main/java/com/chronodawn/gametest/RecipeLoadTests.java common/*/src/main/java/com/chronodawn/gametest/RegistryDrivenTestGenerator.java common/1.20.1/src/main/resources/data/chronodawn/recipes common/1.21.1/src/main/resources/data/chronodawn/recipe common/shared-1.21.2+/src/main/resources/data/chronodawn/recipe common/1.20.1/src/main/resources/data/chronodawn/advancements/recipes common/shared-1.21.1+/src/main/resources/data/chronodawn/advancement/recipes common/26.3/src/main/resources/data/chronodawn/advancement/recipes
git commit -m "feat: craft ultimate artifacts with Clockwork Mechanisms and one Fragment"
```

---

### Task 4: Documentation

**Files:**
- Modify: `docs/player_guide.md` (lines near 519, 538, 697, 821-823, 959-961, 969-1007)
- Modify: `common/shared/src/main/resources/assets/chronodawn/chronicle/entries/items/ultimate_artifacts.json` (page 2)
- Modify: `CHANGELOG.md` (`## [Unreleased]` → `### Added` and `### Changed`)

Read the `doc-maintenance` and `human-tone` skills first. No em dashes or semicolons in new text.

- [ ] **Step 1: player_guide.md**

1. `#### Ancient Gear` (line ~821): change **Use** to:
   `- **Use**: Carry 3 with the key to open the Master Clock boss room door (they are not consumed). Spare gears craft Clockwork Mechanisms`
2. Add after the Ancient Gear section:

```markdown
#### Clockwork Mechanism
- **Use**: Replaces Fragments of Stasis Core in the alternative ultimate artifact recipes
- **Recipe**: 4 Ancient Gears + 1 Enhanced Clockstone + 4 Time Crystals
```

3. `#### Fragments of Stasis Core` (line ~959): change **Source** to `Time Tyrant final boss (4-6, more with Looting)`.
4. Under `### Ultimate Artifacts` (line ~969), after the opening sentence add:

```markdown
Each artifact also has an alternative recipe that takes only 1 Fragment of Stasis Core and uses Clockwork Mechanisms for the rest. One Time Tyrant kill always drops enough Fragments to make all four this way.
```

5. Under each artifact's **Recipe** line add an **Alternative recipe** line:
   - Chronoblade: `1 Fragment of Stasis Core + 3 Clockwork Mechanisms + 1 Eye of Chronos + 1 Enhanced Clockstone + 1 Unstable Hourglass`
   - Time Tyrant Mail: `1 Fragment of Stasis Core + 5 Clockwork Mechanisms + 1 Enhanced Clockstone + 1 Unstable Hourglass`
   - Echoing Time Boots: `1 Fragment of Stasis Core + 1 Clockwork Mechanism + 2 Enhanced Clockstone`
   - Unstable Pocket Watch: `1 Fragment of Stasis Core + 2 Clockwork Mechanisms + 1 Unstable Hourglass + 1 Enhanced Clockstone`
6. Lines ~538 and ~697: change `(multiple pieces, used to craft ultimate artifacts)` to `(4-6, used to craft ultimate artifacts)`.

- [ ] **Step 2: Chronicle**

In `ultimate_artifacts.json` page 2, replace the last sentence of each language with:

- en_us: `Each is crafted from Time Tyrant drops, primarily Fragments of Stasis Core, combined with Enhanced Clockstone. Clockwork Mechanisms made from Ancient Gears can stand in for all but one Fragment.`
- ja_jp: `いずれも時間の暴君のドロップ（主に静止のコアの破片）と強化クロックストーンから作成されます。古代の歯車から作る時計仕掛けの機構で、破片1個を残して代用できます。`

Use the Edit tool and keep the file's existing formatting.

- [ ] **Step 3: CHANGELOG**

Under `## [Unreleased]` → `### Added` append:

```markdown
- **Clockwork Mechanism**: craft one from 4 Ancient Gears, 1 Enhanced
  Clockstone and 4 Time Crystals. Each ultimate artifact has an alternative
  recipe that takes 1 Fragment of Stasis Core plus Clockwork Mechanisms
  (Chronoblade 3, Time Tyrant Mail 5, Echoing Time Boots 1, Unstable Pocket
  Watch 2). The original recipes still work.
```

Add a `### Changed` section (before `### Fixed`) if there is none, with:

```markdown
- **Time Tyrant**: drops 4-6 Fragments of Stasis Core instead of 3-5, so a
  single kill without Looting covers all four alternative artifact recipes.
  Worlds where the Time Tyrant is already defeated keep the Fragments they got.
```

- [ ] **Step 4: Validate and commit**

```bash
./gradlew validateResources > build/task4.log 2>&1; tail -3 build/task4.log
git add docs/player_guide.md common/shared/src/main/resources/assets/chronodawn/chronicle/entries/items/ultimate_artifacts.json CHANGELOG.md
git commit -m "docs: describe Clockwork Mechanism recipes and the new Fragment drop"
```

Expected: `BUILD SUCCESSFUL`.

---

### Task 5: Full verification

- [ ] **Step 1: checkAll**

```bash
./gradlew checkAll > "$(pwd)/build/checkall.log" 2>&1; tail -20 build/checkall.log
```

Expected: `BUILD SUCCESSFUL`. On failure, read the first `FAILED` in the log and fix it in the task that owns the file.

- [ ] **Step 2: Manual check (user)**

Present these steps to the user and wait for the result. On 1.21.1 NeoForge (`./gradlew runClientNeoForge1_21_1`) and 1.21.1 Fabric (`./gradlew runClientFabric1_21_1`), in a survival world:

1. `/give @s chronodawn:ancient_gear 4`: the recipe book shows the Clockwork Mechanism recipe
2. Craft a Mechanism with 4 gears, 1 Enhanced Clockstone and 4 Time Crystals. The texture is readable and distinct from the Ancient Gear
3. With 1 Fragment of Stasis Core and the materials, each of the four alternative recipes appears in the recipe book and crafts the artifact
4. 2 Fragments + 2 Mechanisms in a Chronoblade-like grid produce nothing
