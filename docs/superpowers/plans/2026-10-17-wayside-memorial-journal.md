# Lost Adventurer's Journal Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Let the six Faded Journal Pages craft into a Lost Adventurer's Journal that is never consumed, and make a player holding it at a memorial receive a small offering instead of another page.

**Architecture:** A new `LostAdventurerJournalItem` (per version, like `JournalPageItem`) shows the closing passage. A shapeless recipe in each recipe root, plus a recipe-unlock advancement, makes it. `MemorialPlateBlock` (14 copies) gains `offeringTableFor(Player)` that picks `gameplay/lost_adventurer_memorial_blessed` when either hand holds the journal. The blessed table lives beside the existing reward table in the three loot roots.

**Tech Stack:** Java 17/21/25, Architectury, Fabric, Forge 1.20.1, NeoForge, Minecraft GameTest, ImageMagick for the item texture.

**Spec:** `docs/superpowers/specs/2026-10-14-wayside-memorial-design.md` (section "Collection and the journal")

## Global Constraints

- Supported modules: 1.20.1, 1.21.1, 1.21.2 (also serves 1.21.3), 1.21.4, 1.21.5, 1.21.6, 1.21.7, 1.21.8, 1.21.9, 1.21.10, 1.21.11, 26.1.2, 26.2, 26.3.
- Item ID `lost_adventurer_journal`, stacks to 1, never consumed by praying. Four tooltip lines, gray italic, keys `item.chronodawn.lost_adventurer_journal.line1` to `.line4`.
- Recipe `chronodawn:lost_adventurer_journal`: shapeless, `faded_journal_page_1` to `_6` plus `minecraft:book`, result one journal, category `misc`. Unlocked when the player picks up any page.
- Blessed loot table `chronodawn:gameplay/lost_adventurer_memorial_blessed`: one roll, equal weight, of clockstone 1 to 2, raw_temporal_amber 1 to 2, time_fruit_pie 1, temporal_root_stew 1, baked_temporal_root 2 to 3, then the same supplies pool as the normal table (1 to 2 rolls of bread 2 to 4, arrow 4 to 8, torch 4 to 8). No page.
- A player holding the journal in the main hand or the off hand when the prayer is answered rolls the blessed table. Otherwise the normal table.
- Code comments and docs in English. No em dashes or semicolons in docs and commit messages.
- Do not commit without the user's go-ahead. Each "Commit" step means: stage the listed files and let the controller run `git commit` (subagents cannot GPG-sign). Commit messages use Conventional Commits and end with `Co-Authored-By: Claude Opus 5.5 <noreply@anthropic.com>`. Never write issue-closing keywords.
- Never edit lang JSON with `json.load`/`json.dump`. Insert lines textually.
- Write temporary files under `$TMPDIR`.
- Gradle tasks fork a JVM and need the sandbox disabled. One Gradle invocation at a time. Redirect long output to `/Users/ksoichiro/.paseo/worktrees/2us4x7u1/bright-sheep/build/<name>.log`.

## Review Focus

1. A player holding the journal in the off hand (sword in the main hand) must get the blessed offering. Test in Task 2 (`memorial_journal_in_either_hand_blesses`).
2. A player holding the journal must never receive a page, otherwise duplicates keep piling up. Test in Task 2 (`memorial_blessed_offering_has_no_page`).
3. A player without the journal, or holding a page instead, must still receive a page. Test in Task 2 (`memorial_journal_in_either_hand_blesses` also checks an empty hand and a page in hand).
4. The recipe must load on every version despite the three recipe formats (object ingredients up to 1.21.1, string ingredients from 1.21.2). Test in Task 1 (`recipe_loaded_lost_adventurer_journal` runs on every module).
5. Crafting must consume the pages and the book but leave nothing behind (no remainder). Covered by vanilla shapeless behavior, checked in the Task 3 manual list.

---

### Task 1: The journal item and its recipe on every module

**Files:**
- Modify: `common/shared/src/main/java/com/chronodawn/registry/ModItemId.java` (after `FADED_JOURNAL_PAGE_6`)
- Create: `common/<v>/src/main/java/com/chronodawn/items/LostAdventurerJournalItem.java` for all 14 modules
- Modify: `common/<v>/src/main/java/com/chronodawn/registry/ModItems.java` for all 14 modules (field after `FADED_JOURNAL_PAGE_6`, creative tab after `output.accept(FADED_JOURNAL_PAGE_6.get());`)
- Create: `common/shared/src/main/resources/assets/chronodawn/textures/item/lost_adventurer_journal.png`
- Create: `common/shared/src/main/resources/assets/chronodawn/models/item/lost_adventurer_journal.json`
- Create: `common/1.21.4/src/main/resources/assets/chronodawn/items/lost_adventurer_journal.json` and `common/shared-1.21.5+/src/main/resources/assets/chronodawn/items/lost_adventurer_journal.json`
- Create: recipe `lost_adventurer_journal.json` in `common/1.20.1/src/main/resources/data/chronodawn/recipes/`, `common/1.21.1/src/main/resources/data/chronodawn/recipe/`, `common/shared-1.21.2+/src/main/resources/data/chronodawn/recipe/`
- Create: unlock advancement `lost_adventurer_journal.json` in `common/1.20.1/src/main/resources/data/chronodawn/advancements/recipes/misc/`, `common/shared-1.21.1+/src/main/resources/data/chronodawn/advancement/recipes/misc/`, `common/26.3/src/main/resources/data/chronodawn/advancement/recipes/misc/`
- Modify: the six lang files (`en_us.json`, `ja_jp.json` in `common/1.20.1`, `common/1.21.1`, `common/shared-1.21.2+`, under `src/main/resources/assets/chronodawn/lang/`)
- Modify: `common/gametest/src/main/java/com/chronodawn/gametest/MemorialPlateTests.java`

**Interfaces:**
- Produces: `ModItemId.LOST_ADVENTURER_JOURNAL`, `ModItems.LOST_ADVENTURER_JOURNAL` (`RegistrySupplier<Item>`) in every module, `public class LostAdventurerJournalItem extends Item` with ctor `(Item.Properties)` and `public static final int LINE_COUNT = 4`.

- [ ] **Step 1: Write the failing GameTest**

In `MemorialPlateTests.java`, register after the last existing test:

```java
        tests.add(factory.create("recipe_loaded_lost_adventurer_journal",
            MemorialPlateTests::testJournalRecipeLoaded));
```

```java
    /**
     * Test: the journal recipe loads on this version. The three recipe roots use different
     * ingredient formats, so a format slip would drop the recipe silently.
     */
    private static void testJournalRecipeLoaded(GameTestHelper helper) {
        helper.runAfterDelay(1, () -> {
            var id = CompatResourceLocation.create("chronodawn", "lost_adventurer_journal");
            var recipeManager = helper.getLevel().getServer().getRecipeManager();
            if (CompatRecipeManager.isRecipeLoaded(recipeManager, id)) {
                helper.succeed();
            } else {
                CompatGameTestHelper.fail(helper, "Recipe not loaded: chronodawn:lost_adventurer_journal");
            }
        });
    }
```

Add imports `com.chronodawn.compat.CompatRecipeManager` and `com.chronodawn.compat.CompatResourceLocation` (same as `RecipeLoadTests.java`).

- [ ] **Step 2: Run to verify it fails**

Run (sandbox disabled): `./gradlew :fabric:runGameTest -Ptarget_mc_version=1.21.1 > <worktree>/build/journal-t1-red.log 2>&1`
Expected: FAIL, `Recipe not loaded: chronodawn:lost_adventurer_journal`.

- [ ] **Step 3: Item class per era**

Each module already has `items/JournalPageItem.java` in the right era. Create `items/LostAdventurerJournalItem.java` in the same module from it with these changes only:
- Class name `LostAdventurerJournalItem`, Javadoc `The Lost Adventurer's journal, bound from all six pages. Its closing passage is the tooltip. Praying while holding it is handled by MemorialPlateBlock.`
- No `page` field, no `page()` accessor, constructor `public LostAdventurerJournalItem(Properties properties) { super(properties); }`.
- Tooltip key `"item.chronodawn.lost_adventurer_journal.line" + line`.
- Keep `LINE_COUNT = 4`, the module's `appendHoverText` signature and imports, and the gray italic style.

- [ ] **Step 4: Registration per module**

`ModItemId.java`, after `FADED_JOURNAL_PAGE_6("faded_journal_page_6"),`:

```java
    LOST_ADVENTURER_JOURNAL("lost_adventurer_journal"),
```

Each `ModItems.java`, after the `FADED_JOURNAL_PAGE_6` field and before `registerJournalPage`. 1.20.1 and 1.21.1:

```java
    /**
     * Lost Adventurer's Journal - all six pages bound together. Holding it at a memorial
     * swaps the page for a small offering.
     */
    public static final RegistrySupplier<Item> LOST_ADVENTURER_JOURNAL = ITEMS.register(
        ModItemId.LOST_ADVENTURER_JOURNAL.id(),
        () -> new LostAdventurerJournalItem(new Item.Properties().stacksTo(1))
    );
```

1.21.2 to 1.21.10: the properties become

```java
        () -> new LostAdventurerJournalItem(new Item.Properties()
                .stacksTo(1)
                .setId(ResourceKey.create(Registries.ITEM,
                    ResourceLocation.fromNamespaceAndPath(ChronoDawn.MOD_ID, ModItemId.LOST_ADVENTURER_JOURNAL.id()))))
```

1.21.11 to 26.3: the same with `Identifier.fromNamespaceAndPath`. Match the `registerJournalPage` helper in the same file for the exact `setId` form.

Add `import com.chronodawn.items.LostAdventurerJournalItem;` next to the `JournalPageItem` import.

In each `populateCreativeTab`, after `output.accept(FADED_JOURNAL_PAGE_6.get());`:

```java
        output.accept(LOST_ADVENTURER_JOURNAL.get());
```

- [ ] **Step 5: Texture, model, client items**

```bash
mkdir -p "$TMPDIR/journal_book" && cd "$TMPDIR/journal_book"
unzip -o -j ~/.gradle/caches/fabric-loom/1.21.1/minecraft-client.jar 'assets/minecraft/textures/item/writable_book.png'
magick writable_book.png -modulate 92,60,100 -fill '#8a6a3a' -colorize 30% -depth 8 PNG32:lost_adventurer_journal.png
magick lost_adventurer_journal.png -format '%wx%h %[channels]\n' info:
cp lost_adventurer_journal.png /Users/ksoichiro/.paseo/worktrees/2us4x7u1/bright-sheep/common/shared/src/main/resources/assets/chronodawn/textures/item/lost_adventurer_journal.png
cd /Users/ksoichiro/.paseo/worktrees/2us4x7u1/bright-sheep
```

The same transform as `faded_journal_page.png`, so the two read as a set. View the PNG with the Read tool and confirm it is still recognizable as a book.

`models/item/lost_adventurer_journal.json`:

```json
{
  "parent": "minecraft:item/generated",
  "textures": {
    "layer0": "chronodawn:item/lost_adventurer_journal"
  }
}
```

Client items, one line, in both `common/1.21.4/.../items/` and `common/shared-1.21.5+/.../items/`:

```json
{"model":{"type":"minecraft:model","model":"chronodawn:item/lost_adventurer_journal"}}
```

- [ ] **Step 6: Recipe per root**

`common/1.20.1/src/main/resources/data/chronodawn/recipes/lost_adventurer_journal.json`:

```json
{
  "type": "minecraft:crafting_shapeless",
  "category": "misc",
  "ingredients": [
    { "item": "chronodawn:faded_journal_page_1" },
    { "item": "chronodawn:faded_journal_page_2" },
    { "item": "chronodawn:faded_journal_page_3" },
    { "item": "chronodawn:faded_journal_page_4" },
    { "item": "chronodawn:faded_journal_page_5" },
    { "item": "chronodawn:faded_journal_page_6" },
    { "item": "minecraft:book" }
  ],
  "result": {
    "item": "chronodawn:lost_adventurer_journal",
    "count": 1
  }
}
```

`common/1.21.1/src/main/resources/data/chronodawn/recipe/lost_adventurer_journal.json`: the same, with `"result": { "id": "chronodawn:lost_adventurer_journal", "count": 1 }`.

`common/shared-1.21.2+/src/main/resources/data/chronodawn/recipe/lost_adventurer_journal.json`:

```json
{
  "type": "minecraft:crafting_shapeless",
  "category": "misc",
  "ingredients": [
    "chronodawn:faded_journal_page_1",
    "chronodawn:faded_journal_page_2",
    "chronodawn:faded_journal_page_3",
    "chronodawn:faded_journal_page_4",
    "chronodawn:faded_journal_page_5",
    "chronodawn:faded_journal_page_6",
    "minecraft:book"
  ],
  "result": {
    "id": "chronodawn:lost_adventurer_journal",
    "count": 1
  }
}
```

Compare each with an existing shapeless recipe in the same directory (for example `ancient_time_wood_button.json`) before saving.

- [ ] **Step 7: Recipe-unlock advancement per root**

`common/shared-1.21.1+/src/main/resources/data/chronodawn/advancement/recipes/misc/lost_adventurer_journal.json` and the 1.20.1 copy under `advancements/recipes/misc/` (identical):

```json
{
  "parent": "minecraft:recipes/root",
  "criteria": {
    "has_item": {
      "trigger": "minecraft:inventory_changed",
      "conditions": {
        "items": [
          {
            "items": [
              "chronodawn:faded_journal_page_1",
              "chronodawn:faded_journal_page_2",
              "chronodawn:faded_journal_page_3",
              "chronodawn:faded_journal_page_4",
              "chronodawn:faded_journal_page_5",
              "chronodawn:faded_journal_page_6"
            ]
          }
        ]
      }
    },
    "has_the_recipe": {
      "trigger": "minecraft:recipe_unlocked",
      "conditions": {
        "recipe": "chronodawn:lost_adventurer_journal"
      }
    }
  },
  "requirements": [
    [
      "has_item",
      "has_the_recipe"
    ]
  ],
  "rewards": {
    "recipes": [
      "chronodawn:lost_adventurer_journal"
    ]
  }
}
```

The 26.3 copy is the same except the `recipe_unlocked` condition key is `"recipes"` instead of `"recipe"`, matching `common/26.3/.../advancement/recipes/misc/ancient_time_wood_button.json`.

An item predicate with several items matches any one of them, so picking up any page unlocks the recipe.

- [ ] **Step 8: Lang**

Insert after the line containing `"item.chronodawn.faded_journal_page_6.line4"` in the six lang files.

en_us:
```
  "item.chronodawn.lost_adventurer_journal": "Lost Adventurer's Journal",
  "item.chronodawn.lost_adventurer_journal.line1": "I reached the great clock and heard it strike.",
  "item.chronodawn.lost_adventurer_journal.line2": "Time folded, and I woke at the roadside,",
  "item.chronodawn.lost_adventurer_journal.line3": "under a stone with my own name on it.",
  "item.chronodawn.lost_adventurer_journal.line4": "Pray for the next traveler, not for me.",
```

ja_jp:
```
  "item.chronodawn.lost_adventurer_journal": "失われた冒険者の日誌",
  "item.chronodawn.lost_adventurer_journal.line1": "大時計にたどり着き、その鐘を聞いた。",
  "item.chronodawn.lost_adventurer_journal.line2": "時が折り重なり、気づけば道ばたにいた。",
  "item.chronodawn.lost_adventurer_journal.line3": "自分の名が刻まれた石の下で。",
  "item.chronodawn.lost_adventurer_journal.line4": "私ではなく、次の旅人のために祈ってくれ。",
```

Validate each edited file parses with `python3 -I -c "import json,sys; [json.load(open(f)) for f in sys.argv[1:]]" <files>`.

- [ ] **Step 9: Build and test**

Run (sandbox disabled), one at a time:
1. `./gradlew buildAll > <worktree>/build/journal-t1-build.log 2>&1`: expected BUILD SUCCESSFUL.
2. `./gradlew testAll > <worktree>/build/journal-t1-test.log 2>&1`: expected PASS (`ResourceValidationTest` model and translation, `CreativeTabCompletenessTest`, `CrossVersionConsistencyTest`, `AdvancementValidationTest`).
3. `./gradlew validateResources validateTranslations validateData validateEraTagParity validateEraJsonFormat validateLangParity`: expected PASS.
4. `./gradlew :fabric:runGameTest -Ptarget_mc_version=1.21.1`, `./gradlew :fabric:runGameTest -Ptarget_mc_version=1.20.1`, `./gradlew :fabric:runGameTest -Ptarget_mc_version=26.3`: expected PASS each, including `recipe_loaded_lost_adventurer_journal`. These three cover the three recipe roots.

- [ ] **Step 10: Commit**

```bash
git add common/shared/src/main/java/com/chronodawn/registry/ModItemId.java \
        common/*/src/main/java/com/chronodawn/items/LostAdventurerJournalItem.java \
        common/*/src/main/java/com/chronodawn/registry/ModItems.java \
        common/shared/src/main/resources/assets/chronodawn/textures/item/lost_adventurer_journal.png \
        common/shared/src/main/resources/assets/chronodawn/models/item/lost_adventurer_journal.json \
        common/1.21.4/src/main/resources/assets/chronodawn/items/lost_adventurer_journal.json \
        common/shared-1.21.5+/src/main/resources/assets/chronodawn/items/lost_adventurer_journal.json \
        common/1.20.1/src/main/resources/data/chronodawn/recipes/lost_adventurer_journal.json \
        common/1.21.1/src/main/resources/data/chronodawn/recipe/lost_adventurer_journal.json \
        common/shared-1.21.2+/src/main/resources/data/chronodawn/recipe/lost_adventurer_journal.json \
        common/1.20.1/src/main/resources/data/chronodawn/advancements/recipes/misc/lost_adventurer_journal.json \
        common/shared-1.21.1+/src/main/resources/data/chronodawn/advancement/recipes/misc/lost_adventurer_journal.json \
        common/26.3/src/main/resources/data/chronodawn/advancement/recipes/misc/lost_adventurer_journal.json \
        common/1.20.1/src/main/resources/assets/chronodawn/lang/*.json \
        common/1.21.1/src/main/resources/assets/chronodawn/lang/*.json \
        common/shared-1.21.2+/src/main/resources/assets/chronodawn/lang/*.json \
        common/gametest/src/main/java/com/chronodawn/gametest/MemorialPlateTests.java
git commit -m "feat: bind the six journal pages into the Lost Adventurer's Journal"
```

---

### Task 2: Blessed offering for players holding the journal

**Files:**
- Create: `lost_adventurer_memorial_blessed.json` in `common/1.20.1/src/main/resources/data/chronodawn/loot_tables/gameplay/`, `common/shared-1.21.1+/src/main/resources/data/chronodawn/loot_table/gameplay/`, `common/26.3/src/main/resources/data/chronodawn/loot_table/gameplay/`
- Modify: `common/<v>/src/main/java/com/chronodawn/blocks/MemorialPlateBlock.java` for all 14 modules
- Modify: `common/gametest/src/main/java/com/chronodawn/gametest/MemorialPlateTests.java`

**Interfaces:**
- Consumes: `ModItems.LOST_ADVENTURER_JOURNAL`, `LostAdventurerJournalItem` (Task 1). Existing `MemorialPlateBlock.rollOffering(ServerLevel, BlockPos, String)`, `CompatGameTestHelper.makeMockPlayer(GameTestHelper)` returning `Player`.
- Produces: `public static String offeringTableFor(Player player)` on `MemorialPlateBlock` in every module, returning `"gameplay/lost_adventurer_memorial_blessed"` or `"gameplay/lost_adventurer_memorial"`.

- [ ] **Step 1: Write the failing GameTests**

In `MemorialPlateTests.java`, add constants and register two tests after the existing ones:

```java
    private static final String NORMAL_TABLE = "gameplay/lost_adventurer_memorial";
    private static final String BLESSED_TABLE = "gameplay/lost_adventurer_memorial_blessed";
```

```java
        tests.add(factory.create("memorial_blessed_offering_has_no_page",
            MemorialPlateTests::testBlessedOffering));
        tests.add(factory.create("memorial_journal_in_either_hand_blesses",
            MemorialPlateTests::testJournalInEitherHand));
```

```java
    /**
     * Test: the blessed offering never contains a page, and holds exactly one item from the
     * blessed pool plus one or two supply stacks.
     */
    private static void testBlessedOffering(GameTestHelper helper) {
        ServerLevel level = helper.getLevel();
        BlockPos pos = helper.absolutePos(PLATE_POS);
        for (int roll = 0; roll < ROLLS; roll++) {
            List<ItemStack> offering = MemorialPlateBlock.rollOffering(level, pos, BLESSED_TABLE);
            int blessed = 0;
            int supplies = 0;
            for (ItemStack stack : offering) {
                if (stack.getItem() instanceof JournalPageItem) {
                    CompatGameTestHelper.fail(helper, "Blessed offering contained a page: " + offering);
                    return;
                } else if (stack.is(ModItems.CLOCKSTONE.get()) || stack.is(ModItems.RAW_TEMPORAL_AMBER.get())
                        || stack.is(ModItems.TIME_FRUIT_PIE.get()) || stack.is(ModItems.TEMPORAL_ROOT_STEW.get())
                        || stack.is(ModItems.BAKED_TEMPORAL_ROOT.get())) {
                    blessed++;
                } else if (stack.is(Items.BREAD) || stack.is(Items.ARROW) || stack.is(Items.TORCH)) {
                    supplies++;
                } else {
                    CompatGameTestHelper.fail(helper, "Unexpected blessed offering item: " + stack);
                    return;
                }
            }
            if (blessed != 1 || supplies < 1 || supplies > 2) {
                CompatGameTestHelper.fail(helper, "Expected 1 blessed item and 1-2 supplies, got " + offering);
                return;
            }
        }
        helper.succeed();
    }

    /**
     * Test: the journal in the main hand or the off hand picks the blessed table. An empty hand
     * or a page in hand keeps the normal table, so players still missing pages keep collecting.
     */
    private static void testJournalInEitherHand(GameTestHelper helper) {
        Player player = CompatGameTestHelper.makeMockPlayer(helper);
        if (player == null) {
            CompatGameTestHelper.fail(helper, "Could not create a mock player");
            return;
        }
        ItemStack journal = new ItemStack(ModItems.LOST_ADVENTURER_JOURNAL.get());
        String empty = MemorialPlateBlock.offeringTableFor(player);
        player.setItemInHand(InteractionHand.MAIN_HAND, new ItemStack(ModItems.FADED_JOURNAL_PAGE_1.get()));
        String page = MemorialPlateBlock.offeringTableFor(player);
        player.setItemInHand(InteractionHand.MAIN_HAND, journal.copy());
        String mainHand = MemorialPlateBlock.offeringTableFor(player);
        player.setItemInHand(InteractionHand.MAIN_HAND, new ItemStack(Items.IRON_SWORD));
        player.setItemInHand(InteractionHand.OFF_HAND, journal.copy());
        String offHand = MemorialPlateBlock.offeringTableFor(player);
        if (!NORMAL_TABLE.equals(empty) || !NORMAL_TABLE.equals(page)
                || !BLESSED_TABLE.equals(mainHand) || !BLESSED_TABLE.equals(offHand)) {
            CompatGameTestHelper.fail(helper, "Table choice wrong: empty=" + empty + " page=" + page
                + " main=" + mainHand + " off=" + offHand);
            return;
        }
        helper.succeed();
    }
```

Imports to add: `com.chronodawn.registry.ModItems`, `net.minecraft.world.InteractionHand`, `net.minecraft.world.entity.player.Player`. Confirm the five blessed item fields exist with these names in `common/1.21.1/src/main/java/com/chronodawn/registry/ModItems.java` (`grep -n "CLOCKSTONE =\|RAW_TEMPORAL_AMBER =\|TIME_FRUIT_PIE =\|TEMPORAL_ROOT_STEW =\|BAKED_TEMPORAL_ROOT =" ...`) and use the real names if any differ.

- [ ] **Step 2: Run to verify it fails**

Run (sandbox disabled): `./gradlew :common-1.21.1:compileJava -Ptarget_mc_version=1.21.1`
Expected: compile FAIL, `offeringTableFor` not found. (The gametest sources compile with the module, so this is the RED.)

- [ ] **Step 3: Blessed loot table**

`common/1.20.1/.../loot_tables/gameplay/lost_adventurer_memorial_blessed.json` and `common/shared-1.21.1+/.../loot_table/gameplay/lost_adventurer_memorial_blessed.json` (identical):

```json
{
  "type": "minecraft:chest",
  "pools": [
    {
      "rolls": 1,
      "entries": [
        {
          "type": "minecraft:item",
          "name": "chronodawn:clockstone",
          "functions": [
            { "function": "minecraft:set_count", "count": { "type": "minecraft:uniform", "min": 1, "max": 2 } }
          ]
        },
        {
          "type": "minecraft:item",
          "name": "chronodawn:raw_temporal_amber",
          "functions": [
            { "function": "minecraft:set_count", "count": { "type": "minecraft:uniform", "min": 1, "max": 2 } }
          ]
        },
        { "type": "minecraft:item", "name": "chronodawn:time_fruit_pie" },
        { "type": "minecraft:item", "name": "chronodawn:temporal_root_stew" },
        {
          "type": "minecraft:item",
          "name": "chronodawn:baked_temporal_root",
          "functions": [
            { "function": "minecraft:set_count", "count": { "type": "minecraft:uniform", "min": 2, "max": 3 } }
          ]
        }
      ]
    },
    {
      "rolls": { "type": "minecraft:uniform", "min": 1, "max": 2 },
      "entries": [
        {
          "type": "minecraft:item",
          "name": "minecraft:bread",
          "functions": [
            { "function": "minecraft:set_count", "count": { "type": "minecraft:uniform", "min": 2, "max": 4 } }
          ]
        },
        {
          "type": "minecraft:item",
          "name": "minecraft:arrow",
          "functions": [
            { "function": "minecraft:set_count", "count": { "type": "minecraft:uniform", "min": 4, "max": 8 } }
          ]
        },
        {
          "type": "minecraft:item",
          "name": "minecraft:torch",
          "functions": [
            { "function": "minecraft:set_count", "count": { "type": "minecraft:uniform", "min": 4, "max": 8 } }
          ]
        }
      ]
    }
  ]
}
```

The supplies pool is the same as in `lost_adventurer_memorial.json` in the same directory. The 26.3 copy uses `"modifier": { "type": "minecraft:set_count", "count": ... }` in place of each `"functions"` array, matching `common/26.3/.../loot_table/gameplay/lost_adventurer_memorial.json`.

- [ ] **Step 4: Table choice in every MemorialPlateBlock copy**

In each of the 14 copies, after the `OFFERING_LOOT_TABLE` constant:

```java
    private static final String BLESSED_OFFERING_LOOT_TABLE = "gameplay/lost_adventurer_memorial_blessed";
```

Add after `rollOffering(ServerLevel, BlockPos, String)`:

```java
    /**
     * Holding the journal in either hand blesses the prayer: a small offering replaces the page,
     * so a player who finished the journal stops collecting duplicates. Loot tables cannot see
     * the held item, so the choice is made here.
     */
    public static String offeringTableFor(Player player) {
        Item journal = ModItems.LOST_ADVENTURER_JOURNAL.get();
        return player.getMainHandItem().is(journal) || player.getOffhandItem().is(journal)
            ? BLESSED_OFFERING_LOOT_TABLE
            : OFFERING_LOOT_TABLE;
    }
```

and in `dropOffering`, change `rollOffering(level, pos, OFFERING_LOOT_TABLE, player)` to `rollOffering(level, pos, offeringTableFor(player), player)`.

Imports: `com.chronodawn.registry.ModItems`, `net.minecraft.world.item.Item`. No era lines change. After editing, diff every copy against `common/1.21.1/.../MemorialPlateBlock.java` and confirm only the existing era lines differ.

- [ ] **Step 5: Build and test**

Run (sandbox disabled), one at a time:
1. `./gradlew buildAll`: expected BUILD SUCCESSFUL.
2. `./gradlew :fabric:runGameTest -Ptarget_mc_version=1.21.1`, `./gradlew :neoforge:runGameTestServer -Ptarget_mc_version=1.21.1`, `./gradlew :fabric:runGameTest -Ptarget_mc_version=1.20.1`, `./gradlew :fabric:runGameTest -Ptarget_mc_version=26.3`: expected PASS each, including the two new tests. The 1.20.1 and 26.3 runs cover the other two loot roots.
3. `./gradlew validateEraJsonFormat`: expected PASS.

The controller runs `gameTestAll` after the commit.

- [ ] **Step 6: Commit**

```bash
git add common/1.20.1/src/main/resources/data/chronodawn/loot_tables/gameplay/lost_adventurer_memorial_blessed.json \
        common/shared-1.21.1+/src/main/resources/data/chronodawn/loot_table/gameplay/lost_adventurer_memorial_blessed.json \
        common/26.3/src/main/resources/data/chronodawn/loot_table/gameplay/lost_adventurer_memorial_blessed.json \
        common/*/src/main/java/com/chronodawn/blocks/MemorialPlateBlock.java \
        common/gametest/src/main/java/com/chronodawn/gametest/MemorialPlateTests.java
git commit -m "feat: bless memorial prayers made while holding the journal"
```

---

### Task 3: Docs and full verification

**Files:**
- Modify: `CHANGELOG.md` (the Lost Adventurer Memorial entry under `## [Unreleased]` / `### Added`)
- Modify: `docs/player_guide.md` (the Lost Adventurer Memorial bullet in Small Ambient Structures)

- [ ] **Step 1: Changelog**

Replace the whole "Lost Adventurer Memorial prayers" entry under `## [Unreleased]` / `### Added` with:

```markdown
- **Lost Adventurer Memorial prayers**: step onto the plate in front of a
  Lost Adventurer Memorial and light falls from above, leaving one of six
  Faded Journal Pages and a little bread, arrows or torches. Each player can
  pray at each memorial about once every 20 minutes of game time by default
  (`memorial_cooldown_ticks` under `[gameplay.wayside]`). The pages tell the
  story of the adventurer buried there, and collecting all six grants an
  advancement. The six pages and a book craft into the Lost Adventurer's
  Journal, which holds the end of the story. Pray while holding it in either
  hand and the page is replaced by a small offering of Clockstone, Raw
  Temporal Amber or cooked food. The journal is never used up. Breaking the
  plate drops a plain Temporal Stone Pressure Plate. Only memorials in newly
  generated chunks have the interactive plate.
```

- [ ] **Step 2: Player guide**

In the Lost Adventurer Memorial bullet, replace "Read the pages in order for the adventurer's story, and collect all six for an advancement." with:

```markdown
Read the pages in order for the adventurer's story, and collect all six for an advancement. Craft the six pages with a book into the Lost Adventurer's Journal for the ending. Holding the journal in either hand while praying replaces the page with a small offering of Clockstone, Raw Temporal Amber or cooked food, and the journal is never used up.
```

No em dashes or semicolons in the added text.

- [ ] **Step 3: Full verification**

Run (sandbox disabled), one at a time, each redirected to `<worktree>/build/journal-final-*.log`:
1. `./gradlew validateResources validateTranslations validateData validateEraTagParity validateEraJsonFormat validateLangParity`
2. `./gradlew testAll`
3. `./gradlew gameTestAll` (controller, exceeds a subagent's 10-minute limit), then check every `build/gametest-*.log` for `required tests passed` and no `required tests failed`.

- [ ] **Step 4: Manual check (user)**

1. Collect or `/give` the six pages and a book, craft the journal in survival: the recipe appears in the recipe book after picking up any page, crafting consumes the seven inputs and leaves nothing behind, and the result is one journal.
2. Hover the journal: four gray italic lines.
3. Hold the journal in the main hand and step onto a memorial plate (cooldown shortened as before): the offering is one of Clockstone, Raw Temporal Amber, Time Fruit Pie, Temporal Root Stew or Baked Temporal Root, plus supplies, and no page.
4. Repeat with a sword in the main hand and the journal in the off hand: same blessed offering.
5. Step on without the journal: a page as before.
6. The journal is still in hand after every prayer.

- [ ] **Step 5: Commit**

```bash
git add CHANGELOG.md docs/player_guide.md
git commit -m "docs: describe the Lost Adventurer's Journal"
```
