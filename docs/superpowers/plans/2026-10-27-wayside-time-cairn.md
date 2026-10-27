# Wayside Time Cairn Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Newly generated Time Cairns get a turnable dial that gives a Cairn Stone for the current hour of the day, four stones craft into a Cairn Keystone that a cairn trades for a reward, and a midnight turn rarely summons a particle echo that leads to a buried cache.

**Architecture:** The cairn NBT's `clockwork_dial` becomes `time_cairn_dial`, a block per version module that reuses the dial model and implements `EntityBlock`. Its block entity extends the part 1 `CooldownActivatorBlockEntity` and adds only transient echo state. Hour boundaries, the echo walk and the cache search order are pure classes in `common/shared/.../core/wayside` with unit tests. Stones and the keystone are plain items with tooltips. Rewards and cache contents are loot tables, the cache ground list is a block tag, and the two tunables are config keys.

**Tech Stack:** Java 17/21/25, Architectury, Fabric, Forge 1.20.1, NeoForge, JUnit 5, Minecraft GameTest, Python `nbtlib` for the NBT edit, Pillow for texture drafts.

**Spec:** `docs/superpowers/specs/2026-10-27-wayside-time-cairn-design.md`

## Global Constraints

- Supported modules: 1.20.1, 1.21.1, 1.21.2 (also serves 1.21.3), 1.21.4, 1.21.5, 1.21.6, 1.21.7, 1.21.8, 1.21.9, 1.21.10, 1.21.11, 26.1.2, 26.2, 26.3.
- Block ID `time_cairn_dial`, block entity ID `time_cairn_dial`. No `BlockItem`. Same shape, strength, sound and tool as `clockwork_dial`. Breaking it drops one `clockwork_dial`.
- Item IDs `cairn_stone_dawn`, `cairn_stone_noon`, `cairn_stone_dusk`, `cairn_stone_midnight`, `cairn_keystone`. Stones stack to 64, keystone to 16.
- Hours by day time (`Math.floorMod(dayTime, 24000)`): DAWN 21000 to 23999 and 0 to 2999, NOON 3000 to 8999, DUSK 9000 to 14999, MIDNIGHT 15000 to 20999. Day time is read with the module's `CompatLevelTime.dayTime(level)`.
- Cooldown: per player per cairn, `Level#getGameTime`, default 24000, key `time_cairn_cooldown_ticks` in `[gameplay.wayside]`, range 20 to 1728000. Only an accepted turn consumes it. Setting a keystone never calls `tryActivate`.
- Echo chance: key `time_cairn_echo_chance`, float, default 0.2, range 0.0 to 1.0, rolled only when an accepted turn gives MIDNIGHT. Out-of-range values fall back to the default for that key alone.
- Keystone detection: `player.getMainHandItem()` or `player.getOffhandItem()` is `ModItems.CAIRN_KEYSTONE`. The block overrides only the module's no-item use method (`use` on 1.20.1, `useWithoutItem` later). A plain `Item` in hand falls through to it on every module.
- Items the cairn gives are spawned as `ItemEntity` at `(pos.x + 0.5, pos.y + 0.4, pos.z + 0.5)` with velocity `(0, 0.25, 0)` plus a random horizontal component of at most 0.1, never put into the inventory.
- Loot tables: `chronodawn:gameplay/time_cairn_keystone` (keystone reward), `chronodawn:chests/time_cairn_echo_cache` (cache), `chronodawn:blocks/time_cairn_dial` (drop). Rolled with the player as `THIS_ENTITY` and luck, following `MemorialPlateBlock.rollOffering`.
- Cache: chest at `T.below()`, marker at `T`, where `T` is the top solid block of the column from `Heightmap.Types.MOTION_BLOCKING_NO_LEAVES` after skipping a `minecraft:snow` layer. Distance 16 to 24 blocks from the dial in production (GameTests pass shorter distances), eight directions tried from a random start angle. `T` must be in block tag `chronodawn:time_cairn_cache_ground`, `T.below()` must be solid, no block entity, no fluid. No valid spot means no echo and no error.
- Echo walk speed 0.08 blocks per tick. Linger particles at the cache for 1200 ticks after arrival. Echo state is never saved.
- Messages (action bar), en / ja:
  - `message.chronodawn.time_cairn.dawn`: "The dial turns and gives up a Dawn Stone." / 「文字盤が回り、暁の石がこぼれ落ちた。」
  - `message.chronodawn.time_cairn.noon`: "The dial turns and gives up a Noon Stone." / 「文字盤が回り、正午の石がこぼれ落ちた。」
  - `message.chronodawn.time_cairn.dusk`: "The dial turns and gives up a Dusk Stone." / 「文字盤が回り、黄昏の石がこぼれ落ちた。」
  - `message.chronodawn.time_cairn.midnight`: "The dial turns and gives up a Midnight Stone." / 「文字盤が回り、真夜中の石がこぼれ落ちた。」
  - `message.chronodawn.time_cairn.cooldown`: "The dial will not turn yet. Try again in about %s min." / 「文字盤はまだ回らない。あと約%s分で再び回せる。」
  - `message.chronodawn.time_cairn.keystone`: "The cairn remembers a whole day." / 「ケルンが一日のすべてを思い出した。」
  - `message.chronodawn.time_cairn.echo`: "A pale figure rises from the cairn and walks away." / 「淡い人影がケルンから立ち上がり、歩き去っていく。」
- Names, en / ja: block "Time Cairn Dial" / 「時のケルンの文字盤」, items "Dawn Stone" / 「暁の石」, "Noon Stone" / 「正午の石」, "Dusk Stone" / 「黄昏の石」, "Midnight Stone" / 「真夜中の石」, "Cairn Keystone" / 「ケルンの要石」. Tooltip lines (`item.chronodawn.<id>.line1`, keystone also `.line2`), en / ja: dawn "A cairn stone engraved with the first light." / 「最初の光が刻まれたケルンの石。」, noon "A cairn stone engraved with the sun at its height." / 「中天の太陽が刻まれたケルンの石。」, dusk "A cairn stone engraved with the last light." / 「最後の光が刻まれたケルンの石。」, midnight "A cairn stone engraved with a moonless hour." / 「月のない刻が刻まれたケルンの石。」, keystone line1 "The four hours of a day, bound in one stone." / 「一日の四つの刻を、ひとつの石に束ねたもの。」, line2 "Set it into a Time Cairn." / 「時のケルンにはめ込んで使う。」
- Advancement `chronodawn:exploration/time_cairn_stones`, title "A Full Day" / 「まる一日」, description "Collect a Dawn, Noon, Dusk and Midnight Stone" / 「暁、正午、黄昏、真夜中の石をすべて集める」.
- Textures are new 16x16 pixel art drawn for this feature. No tinted or copied vanilla pixels. Flag every PNG for the maintainer's review.
- Already-generated cairns are not converted. Placement and rarity do not change.
- Code comments and docs in English. No em dashes or semicolons in docs and commit messages.
- Do not commit without the user's go-ahead. Each "Commit" step means: stage the listed files and ask the controller/user before running `git commit`. Commit messages use Conventional Commits and end with `Co-Authored-By: Claude Fable 5.1 <noreply@anthropic.com>`. Never write issue-closing keywords.
- Never edit lang JSON with `json.load`/`json.dump`. Insert lines textually so blank-line groupings survive.
- Write temporary files under `$TMPDIR`.
- Gradle tasks fork a JVM and need the sandbox disabled. This worktree starts with an empty `gradle/shared`, so run `git submodule update --init gradle/shared` once before the first Gradle command. Run one Gradle invocation at a time. Redirect long Gradle output to `<worktree>/build/<name>.log` with the absolute worktree path.

## Review Focus

1. A player with a full inventory, in creative mode, turns the dial: the stone appears on the ground and is not deleted. Test `time_cairn_turn_spawns_item_with_full_inventory` (Task 5).
2. A player holds a sword in the main hand and the keystone in the off hand: the keystone is set, the sword is untouched, no stone is given. Test `time_cairn_keystone_in_either_hand` (Task 5).
3. A cairn surrounded by water or standing on a one-block pillar: a midnight turn still gives the stone, no chest is placed, no exception. Test `time_cairn_echo_finds_no_spot_over_water` (Task 6).
4. The server stops the tick after an echo starts: the chest and marker already exist. Test `time_cairn_echo_places_cache_before_walking` (Task 6) asserts the chest is present the same tick `summonEcho` returns.
5. Snowy biome: the top of the column is a `minecraft:snow` layer on a snow block. The marker replaces the snow block, the layer is removed, the chest sits under the marker. Test `time_cairn_echo_cache_under_snow_layer` (Task 6).

---

## Version eras used in this plan

Block (`TimeCairnDialBlock`). Copy the interaction method, client check, action bar call and loot lookup from the same module's `SundialGnomonBlock.java` and `MemorialPlateBlock.java`. Copy the shape and `createProperties()` from the same module's `ClockworkDialBlock.java` (`Block.box(0, 0, 0, 16, 2, 16)`, `MapColor.METAL`, strength 3.5/5.0, `requiresCorrectToolForDrops`, `SoundType.METAL`), adding `setId(...)` with path `"time_cairn_dial"` where the module's `TimeWellStoneBlock.createProperties()` does.

| Era | Modules | Interaction | Client check | Action bar | Registry key class |
|---|---|---|---|---|---|
| A | 1.20.1 | `use(...)` | `level.isClientSide` | `displayClientMessage(c, true)` | `ResourceLocation` |
| B | 1.21.1 to 1.21.8 | `useWithoutItem(...)` | `level.isClientSide` | as A | `ResourceLocation` |
| C | 1.21.9, 1.21.10 | as B | `level.isClientSide()` | as A | `ResourceLocation` |
| D | 1.21.11 | as B | as C | as A | `Identifier` |
| E | 26.1.2, 26.2, 26.3 | as B | as C | `player.sendOverlayMessage(c)` | `Identifier` |

Other per-module points, each with a precedent in that module:
- Day time: `CompatLevelTime.dayTime(level)`.
- Loot lookup: `getLootData()` on 1.20.1, `reloadableRegistries()` later, as in `MemorialPlateBlock.rollOffering`.
- Chest loot table: `RandomizableContainerBlockEntity#setLootTable(ResourceLocation, long)` on 1.20.1, `setLootTable(ResourceKey<LootTable>, long)` later, as in the module's `TimeKeeperVillagePlacer`.
- Dust particle color: `DustParticleOptions(Vector3f, float)` up to 1.21.1, `DustParticleOptions(int argb, float)` from 1.21.2, as in the module's `TeleporterChargingHandler`.
- Block entity ticker: `getTicker` as in the module's `TimeWellStoneBlock`.
- Item tooltips: `appendHoverText` signature as in the module's `JournalPageItem`.

Block entity (`TimeCairnDialBlockEntity`) adds no saved data, so one copy in `common/shared` serves every module. It must not be added to any `build.gradle` exclude list.

Data roots:
- Loot tables: `common/1.20.1/.../data/chronodawn/loot_tables/`, `common/shared-1.21.1+/.../data/chronodawn/loot_table/`, `common/26.3/.../data/chronodawn/loot_table/` (26.3 schema: `"modifier"` object in place of `"functions"` array, `"condition"` in place of `"conditions"`).
- Recipes: `common/1.20.1/.../data/chronodawn/recipes/` (object ingredients, result `"item"`), `common/1.21.1/.../data/chronodawn/recipe/` (object ingredients, result `"id"`), `common/shared-1.21.2+/.../data/chronodawn/recipe/` (string ingredients, result `"id"`).
- Advancements: `common/1.20.1/.../data/chronodawn/advancements/`, `common/shared-1.21.1+/.../data/chronodawn/advancement/`. Recipe-unlock advancements also in `common/26.3/.../data/chronodawn/advancement/recipes/misc/` with `"recipes"` in place of `"recipe"` under `recipe_unlocked`. Exploration advancements have no 26.3 copy.
- Block tags: `common/1.20.1/.../data/chronodawn/tags/blocks/`, `common/shared-1.21.1+/.../data/chronodawn/tags/block/`. Pickaxe tag: `common/1.20.1/.../data/minecraft/tags/blocks/mineable/pickaxe.json`, `common/shared-1.21.1+/.../data/minecraft/tags/block/mineable/pickaxe.json`.
- Lang: `en_us.json` and `ja_jp.json` in `common/1.20.1`, `common/1.21.1`, `common/shared-1.21.2+` (`src/main/resources/assets/chronodawn/lang/`).
- Item models: `common/shared/.../assets/chronodawn/models/item/<id>.json` (`minecraft:item/generated`, `layer0` texture). Client item JSON: `common/1.21.4/.../assets/chronodawn/items/<id>.json` and `common/shared-1.21.5+/.../assets/chronodawn/items/<id>.json`, same content as `faded_journal_page_1.json`.
- Textures: `common/shared/.../assets/chronodawn/textures/item/<id>.png`.

---

### Task 1: Config keys

**Files:**
- Modify: `common/shared/src/main/java/com/chronodawn/config/WaysideSettings.java` (two new record components)
- Modify: `common/shared/src/main/java/com/chronodawn/config/ConfigDefaults.java:80`
- Modify: `common/shared/src/main/java/com/chronodawn/config/ConfigLoader.java:115-126` (constants) and `parseWayside` (~576-640)
- Modify: `common/shared/src/main/resources/chronodawn-default-config.toml` (after `watchmaker_camp_cooldown_ticks`, line 357)
- Modify: `docs/configuration.md:540-556`
- Test: `common/shared/src/test/java/com/chronodawn/unit/ConfigLoaderTest.java:680-760`

**Interfaces:**
- Produces: `WaysideSettings#timeCairnCooldownTicks()` (`int`, default 24000) and `WaysideSettings#timeCairnEchoChance()` (`float`, default 0.2f).

- [ ] **Step 1: Write the failing tests**

Extend the existing wayside override test to set `time_cairn_cooldown_ticks = 7000` and `time_cairn_echo_chance = 0.5` and expect `new WaysideSettings(6000, 1200, 12000, 6000, 9000, 7000, 0.5f)`. Add `wayside_badTimeCairnCooldown_fallsBackAlone` (value 1728001) and `wayside_badTimeCairnEchoChance_fallsBackAlone` (value 1.5), each mirroring `wayside_badWatchmakerCampCooldown_fallsBackAlone`: the bad key takes its default while every other key keeps its configured value.

- [ ] **Step 2: Run to verify it fails**

Run (sandbox disabled): `./gradlew :common-1.21.1:test -Ptarget_mc_version=1.21.1 --tests '*ConfigLoaderTest'`
Expected: compilation FAIL, the seven-argument constructor does not exist.

- [ ] **Step 3: Add the keys**

`int timeCairnCooldownTicks, float timeCairnEchoChance` in `WaysideSettings` with Javadoc, `24000, 0.2f` in `WAYSIDE_DEFAULTS`, constants `K_WS_TIME_CAIRN_COOLDOWN_TICKS = "time_cairn_cooldown_ticks"`, `K_WS_TIME_CAIRN_ECHO_CHANCE = "time_cairn_echo_chance"`, `MAX_TIME_CAIRN_COOLDOWN_TICKS = 1_728_000`. Parse the cooldown exactly like `watchmaker_camp_cooldown_ticks`. Parse the chance as `Number::floatValue` like `K_TD_SLOW_BELOW`, falling back when outside `0.0f..1.0f` with the same warning format. Fix every other `new WaysideSettings(...)` call the compiler reports. In the default TOML and `docs/configuration.md`, add both keys after `watchmaker_camp_cooldown_ticks`. Descriptions: "Time Cairn: how long (in ticks, 20 ticks = 1 second) before the same player can turn the same cairn dial again. Counted in game time, so sleeping does not shorten it. Range: 20 to 1728000." and "Time Cairn: chance, from 0.0 to 1.0, that turning a dial at midnight summons the echo that leads to a buried cache. 0 disables the echo." Mention cairns in the section intro.

- [ ] **Step 4: Run to verify it passes**

Same command. Expected: PASS.

- [ ] **Step 5: Commit**

```bash
git add common/shared/src/main/java/com/chronodawn/config/ \
        common/shared/src/main/resources/chronodawn-default-config.toml \
        common/shared/src/test/java/com/chronodawn/unit/ConfigLoaderTest.java \
        docs/configuration.md
git commit -m "feat: add the time_cairn config keys"
```

---

### Task 2: Pure logic for hours, the echo walk and the cache search

**Files:**
- Create: `common/shared/src/main/java/com/chronodawn/core/wayside/CairnHour.java`
- Create: `common/shared/src/main/java/com/chronodawn/core/wayside/EchoWalk.java`
- Create: `common/shared/src/main/java/com/chronodawn/core/wayside/CachePlacement.java`
- Test: `common/shared/src/test/java/com/chronodawn/unit/CairnHourTest.java`, `EchoWalkTest.java`, `CachePlacementTest.java`

**Interfaces:**
- Produces:
  - `enum CairnHour { DAWN, NOON, DUSK, MIDNIGHT }` with `static CairnHour forDayTime(long dayTime)`, `String messageKey()` (`"message.chronodawn.time_cairn." + name().toLowerCase(Locale.ROOT)`), `String stonePath()` (`"cairn_stone_" + lower name`), `float soundPitch()` (DAWN 0.9f, NOON 1.1f, DUSK 0.8f, MIDNIGHT 0.6f), `int dustColorArgb()` (DAWN 0xFFE08040, NOON 0xFFFFE6A0, DUSK 0xFF9050C0, MIDNIGHT 0xFF2040A0). Constants `TICKS_PER_DAY = 24000L`, `QUARTER_TICKS = 6000L`, `DAWN_START = 21000L`.
  - `final class EchoWalk` with constructor `EchoWalk(double startX, double startZ, double endX, double endZ, long startGameTime)`, `static final double SPEED = 0.08`, `static final int LINGER_TICKS = 1200`, `double[] positionAt(long gameTime)` (clamped lerp by distance traveled), `boolean arrived(long gameTime)`, `boolean finished(long gameTime)` (arrived plus linger elapsed), `long arrivalGameTime()`.
  - `final class CachePlacement` with `static final int MIN_DISTANCE = 16`, `MAX_DISTANCE = 24`, `DIRECTIONS = 8`, and `static List<int[]> candidateOffsets(long seed, int minDistance, int maxDistance)` returning eight `{dx, dz}` offsets from a start angle drawn from `new Random(seed)`, each at a random integer distance in `[minDistance, maxDistance]`, in search order. The block passes `random.nextLong()` and the two constants; tests pass short distances so the cache lands inside the 10x10x10 GameTest template.

- [ ] **Step 1: Write the failing tests**

`CairnHourTest`: `forDayTime` returns DAWN for 21000, 23999, 0, 2999, NOON for 3000 and 8999, DUSK for 9000 and 14999, MIDNIGHT for 15000 and 20999, DAWN for -1 (wraps to 23999) and NOON for 24000 * 1000 + 6000. `stonePath()` of MIDNIGHT is `"cairn_stone_midnight"`.
`EchoWalkTest`: a walk of 20 blocks along x starting at game time 100 is at `(0, 0)` at 100, at `(8, 0)` at 200 (100 ticks times 0.08), `arrived` false at 349 and true at 350, `finished` false at 350 and true at 1550; `positionAt` after arrival equals the end.
`CachePlacementTest`: `candidateOffsets(42L, 16, 24)` gives eight offsets, each with `16 <= hypot(dx, dz) <= 24.5` and no two with the same angle rounded to 45 degrees; `candidateOffsets(42L, 3, 4)` keeps every offset within `hypot <= 4.5`.

- [ ] **Step 2: Run to verify it fails**

Run: `./gradlew :common-1.21.1:test -Ptarget_mc_version=1.21.1 --tests '*CairnHourTest' --tests '*EchoWalkTest' --tests '*CachePlacementTest'`
Expected: compilation FAIL.

- [ ] **Step 3: Implement the three classes**

Pure Java, no Minecraft imports, following `SundialBlessing` and `WellQueue` in style and license header.

- [ ] **Step 4: Run to verify it passes**

Same command. Expected: PASS.

- [ ] **Step 5: Commit**

```bash
git add common/shared/src/main/java/com/chronodawn/core/wayside/CairnHour.java \
        common/shared/src/main/java/com/chronodawn/core/wayside/EchoWalk.java \
        common/shared/src/main/java/com/chronodawn/core/wayside/CachePlacement.java \
        common/shared/src/test/java/com/chronodawn/unit/CairnHourTest.java \
        common/shared/src/test/java/com/chronodawn/unit/EchoWalkTest.java \
        common/shared/src/test/java/com/chronodawn/unit/CachePlacementTest.java
git commit -m "feat: add the Time Cairn hour, echo walk and cache search logic"
```

---

### Task 3: Cairn Stones and Cairn Keystone items on 1.21.1

**Files:**
- Modify: `common/shared/src/main/java/com/chronodawn/registry/ModItemId.java:322` (after `LOST_ADVENTURER_JOURNAL`)
- Create: `common/1.21.1/src/main/java/com/chronodawn/items/CairnStoneItem.java`, `CairnKeystoneItem.java`
- Modify: `common/1.21.1/src/main/java/com/chronodawn/registry/ModItems.java:1167-1175` (registration) and `:2662` (creative tab, after `LOST_ADVENTURER_JOURNAL`)
- Create: item models, client item JSON (1.21.4 and shared-1.21.5+) and textures for the five items (data roots above)
- Create: recipe `cairn_keystone.json` in the three recipe roots, recipe-unlock advancement `recipes/misc/cairn_keystone.json` in the three advancement roots, exploration advancement `exploration/time_cairn_stones.json` in 1.20.1 and shared-1.21.1+
- Modify: the six lang files
- Test: `common/gametest/src/main/java/com/chronodawn/gametest/TimeCairnDialTests.java` (new, recipe test only for now), registered in `common/1.21.1/src/main/java/com/chronodawn/gametest/RegistryDrivenTestGenerator.java:457`

**Interfaces:**
- Produces: `ModItems.CAIRN_STONE_DAWN`, `CAIRN_STONE_NOON`, `CAIRN_STONE_DUSK`, `CAIRN_STONE_MIDNIGHT`, `CAIRN_KEYSTONE` (`RegistrySupplier<Item>`); `static Item ModItems.cairnStone(CairnHour hour)` returning the matching stone; `TimeCairnDialTests.generateTests(MobBehaviorTests.TestFactory<T>)`.

- [ ] **Step 1: Write the failing test**

In `TimeCairnDialTests`, `recipe_loaded_cairn_keystone` following `MemorialPlateTests.testJournalRecipeLoaded` with id `cairn_keystone`. Register the class in the 1.21.1 `RegistryDrivenTestGenerator` after `TimeWellTests`.

- [ ] **Step 2: Run to verify it fails**

Run: `./gradlew :fabric:runGameTest -Ptarget_mc_version=1.21.1 > <worktree>/build/task3-red.log 2>&1`
Expected: FAIL, `recipe_loaded_cairn_keystone` reports the recipe is not loaded (or the build fails until the items exist, which also counts).

- [ ] **Step 3: Add the items**

`CairnStoneItem(Properties, CairnHour)` and `CairnKeystoneItem(Properties)` copied from the module's `JournalPageItem`, adding `item.chronodawn.<id>.line1` (and `.line2` for the keystone) in gray italic. Register the five with `ModItemId` entries, stones `stacksTo(64)`, keystone `stacksTo(16)`, and `cairnStone(CairnHour)` as a switch over the enum. Add the five to the creative tab after the journal. Item models, client item JSON and lang lines (names, tooltips, advancement title and description from Global Constraints) in all roots.

- [ ] **Step 4: Draft the five textures**

Write `scripts/draw_cairn_stone_textures.py` (Pillow, deterministic): a 16x16 rounded gray stone with a one-pixel darker outline, and an engraving in a lighter color per item: dawn a half disc on a horizontal line, noon a full disc with four rays, dusk a half disc with rays below the line, midnight a crescent, keystone the stone with a small four-pointed star. Save the PNGs under `common/shared/.../textures/item/`. Record in the task notes that the maintainer reviews or redraws them.

- [ ] **Step 5: Add the recipe and advancements**

Shapeless `cairn_keystone` with the four stones as ingredients and result count 1 in each recipe root's format. Recipe-unlock advancement with `has_item` listing the four stones in each advancement root, 26.3 using `"recipes"`. Exploration advancement `time_cairn_stones` with four `inventory_changed` criteria, parent `chronodawn:story/us1/dimension_entry`, icon `chronodawn:cairn_keystone`, frame `goal`, following `exploration/lost_adventurer_journal.json`.

- [ ] **Step 6: Run to verify it passes**

Run: `./gradlew validateResources validateTranslations validateData > <worktree>/build/task3-validate.log 2>&1`, then the Step 2 command to `task3-green.log`.
Expected: both PASS, including `recipe_loaded_cairn_keystone` and the existing `AdvancementTests`.

- [ ] **Step 7: Commit**

```bash
git add common/shared/src/main/java/com/chronodawn/registry/ModItemId.java \
        common/1.21.1/src/main/java/com/chronodawn/items/ common/1.21.1/src/main/java/com/chronodawn/registry/ModItems.java \
        common/1.21.1/src/main/java/com/chronodawn/gametest/RegistryDrivenTestGenerator.java \
        common/gametest/src/main/java/com/chronodawn/gametest/TimeCairnDialTests.java \
        common/shared/src/main/resources/assets/ common/1.21.4/src/main/resources/assets/chronodawn/items/ \
        common/shared-1.21.5+/src/main/resources/assets/chronodawn/items/ \
        common/1.20.1/src/main/resources/ common/1.21.1/src/main/resources/ common/shared-1.21.1+/src/main/resources/ \
        common/shared-1.21.2+/src/main/resources/ common/26.3/src/main/resources/data/chronodawn/advancement/ \
        scripts/draw_cairn_stone_textures.py
git commit -m "feat: add the Cairn Stones, the Cairn Keystone and their recipe on 1.21.1"
```

---

### Task 4: Loot tables and the cache ground tag

**Files:**
- Create: `gameplay/time_cairn_keystone.json`, `chests/time_cairn_echo_cache.json`, `blocks/time_cairn_dial.json` in the three loot table roots
- Create: `tags/blocks/time_cairn_cache_ground.json` (1.20.1) and `tags/block/time_cairn_cache_ground.json` (shared-1.21.1+)
- Modify: the two `mineable/pickaxe.json` files (add `chronodawn:time_cairn_dial` after `chronodawn:clockwork_dial`)
- Test: `WatchmakerCampLootTests`-style checks added to `TimeCairnDialTests` in Task 5

**Interfaces:**
- Produces: the three loot table ids and the tag id above.

- [ ] **Step 1: Write the loot tables**

Keystone table, type `minecraft:chest`, pool 1 rolls 1: `time_crystal` 2 to 3 weight 10, `clockstone` 3 to 5 weight 10, `entropy_crystal` 1 weight 5, `enhanced_clockstone` 1 weight 2, `ancient_gear` 1 weight 2. Pool 2 rolls 1: `clockwork_cookie` 1 to 2, `baked_temporal_root` 2 to 3, `minecraft:torch` 4 to 8, weight 1 each.
Cache table, pool 1 rolls 1: `entropy_crystal` 1 to 2 weight 8, `enhanced_clockstone` 1 weight 4, `ancient_gear` 1 weight 4, `time_crystal` 2 to 4 weight 4, `dark_time_wood_sapling` 1 weight 1, `ancient_time_wood_sapling` 1 weight 1. Pool 2 rolls 1 to 2: `minecraft:clock`, `minecraft:compass`, `dawn_lantern`, `dusk_lantern`, `clockwork_cookie` 1 to 2, weight 1 each.
Block table: `clockwork_dial` with `survives_explosion`, copied from `blocks/time_well_stone.json`.
Same content in all three roots in each root's schema.

- [ ] **Step 2: Write the tag**

Entries, both roots: `minecraft:grass_block`, `minecraft:dirt`, `minecraft:podzol`, `minecraft:coarse_dirt`, `minecraft:snow_block`, `chronodawn:temporal_grass_block`, `chronodawn:temporal_dirt`, `chronodawn:coarse_temporal_dirt`, `chronodawn:temporal_sand`. Not `minecraft:snow`.

- [ ] **Step 3: Validate**

Run: `./gradlew validateData validateEraTagParity > <worktree>/build/task4-validate.log 2>&1`
Expected: PASS. (`validateData` resolves every item id in the tables.)

- [ ] **Step 4: Commit**

```bash
git add common/1.20.1/src/main/resources/data/ common/shared-1.21.1+/src/main/resources/data/ common/26.3/src/main/resources/data/chronodawn/loot_table/
git commit -m "feat: add the Time Cairn loot tables and cache ground tag"
```

---

### Task 5: Time Cairn Dial block on 1.21.1 (turn and keystone)

**Files:**
- Modify: `common/shared/src/main/java/com/chronodawn/registry/ModBlockId.java:258`, `ModBlockEntityId.java:37`
- Create: `common/shared/src/main/java/com/chronodawn/blocks/TimeCairnDialBlockEntity.java`
- Create: `common/1.21.1/src/main/java/com/chronodawn/blocks/TimeCairnDialBlock.java`
- Modify: `common/1.21.1/src/main/java/com/chronodawn/registry/ModBlocks.java:492` and `ModBlockEntities.java:71` (after the watchmaker entries)
- Create: `common/shared/src/main/resources/assets/chronodawn/blockstates/time_cairn_dial.json` (model `chronodawn:block/clockwork_dial`)
- Modify: `common/shared-1.21.1+/src/main/resources/data/chronodawn/structure/time_cairn.nbt`
- Modify: `common/gametest/src/main/java/com/chronodawn/gametest/StructureTests.java:108-110` and `common/26.3/src/main/java/com/chronodawn/gametest/StructureTests.java:109-111`
- Test: `TimeCairnDialTests.java`

**Interfaces:**
- Consumes: `CairnHour`, `ModItems.cairnStone`, `WaysideSettings#timeCairnCooldownTicks()`, loot table `gameplay/time_cairn_keystone`.
- Produces:
  - `TimeCairnDialBlockEntity extends CooldownActivatorBlockEntity`, constructor `(BlockPos, BlockState)`, fields `@Nullable EchoWalk echo`, `@Nullable BlockPos cachePos`, methods `startEcho(EchoWalk, BlockPos)`, `boolean echoActive()`, `void clearEcho()`.
  - `TimeCairnDialBlock` with `static BlockBehaviour.Properties createProperties()`, `enum TurnResult { GIVEN, COOLDOWN, GIVEN_WITH_ECHO }`, `static TurnResult turn(ServerLevel level, BlockPos pos, Player player, long dayTime)`, `static boolean setKeystone(ServerLevel level, BlockPos pos, Player player)` (false when neither hand holds a keystone), `static boolean holdsKeystone(Player player)`, `static List<ItemStack> rollKeystoneReward(ServerLevel level, BlockPos pos, @Nullable Player player)`, `static void spawnReward(ServerLevel level, BlockPos pos, ItemStack stack)` (the spawn rule in Global Constraints). `summonEcho` is added in Task 6; in this task `turn` rolls the chance but calls a stub that returns `Optional.empty()`.

- [ ] **Step 1: Write the failing tests**

Add to `TimeCairnDialTests` (dial at `new BlockPos(1, 2, 1)`, players from `makeMockServerPlayer` with `removeMockServerPlayer` in `finally`, item counts filtered to entities whose `tickCount` is below the test's elapsed ticks as in `TimeWellTests`):
- `time_cairn_dial_has_block_entity`: `helper.setBlock` then the block entity is a `TimeCairnDialBlockEntity`.
- `time_cairn_turn_gives_stone_for_each_hour`: `turn` at day times 0, 6000, 12000, 18000 with four different players spawns exactly one `ItemEntity` each, holding `cairn_stone_dawn`, `_noon`, `_dusk`, `_midnight` respectively, and returns `GIVEN`.
- `time_cairn_turn_refuses_within_cooldown`: a second `turn` by the same player returns `COOLDOWN` and spawns nothing; a different player gets `GIVEN`.
- `time_cairn_turn_spawns_item_with_full_inventory`: fill all 36 inventory slots of the player with `minecraft:stone` before turning; still exactly one `ItemEntity` appears and the inventory holds no cairn stone.
- `time_cairn_keystone_in_either_hand`: keystone in the main hand gives `setKeystone` true, consumes one keystone, spawns at least one `ItemEntity` and no cairn stone; keystone in the off hand with `minecraft:iron_sword` in the main hand likewise, and the sword count is unchanged; no keystone gives false and spawns nothing.
- `time_cairn_keystone_ignores_cooldown`: `turn` then `setKeystone` by the same player succeeds, and `tryActivate` for that player afterwards still reports a remaining time greater than 0 and not reset (compare with the value before `setKeystone`).
- `time_cairn_keystone_reward_table_rolls`: 30 rolls of `rollKeystoneReward` each yield 2 to 3 stacks, every item one of the ten allowed ids.
- `time_cairn_dial_drops_clockwork_dial`: following `memorial_plate_drops_plain_pressure_plate`.
- Update both `StructureTests` so `time_cairn` requires `ModBlockId.TIME_CAIRN_DIAL` x1 instead of `CLOCKWORK_DIAL`.

- [ ] **Step 2: Run to verify it fails**

Run: `./gradlew :fabric:runGameTest -Ptarget_mc_version=1.21.1 > <worktree>/build/task5-red.log 2>&1`
Expected: compilation FAIL.

- [ ] **Step 3: Implement the block entity and the block**

`TimeCairnDialBlockEntity` follows `SundialGnomonBlockEntity` plus the transient echo fields. `TimeCairnDialBlock` extends `BaseEntityBlock` following `SundialGnomonBlock` (`CODEC`, `codec()`, `getRenderShape` MODEL, shape from `ClockworkDialBlock`). Use method: on the server, if `holdsKeystone(player)` call `setKeystone`, else `turn(level, pos, player, CompatLevelTime.dayTime(level))`, return SUCCESS. `turn`: `tryActivate` with the config cooldown, on refusal send the cooldown message and return COOLDOWN; otherwise `CairnHour.forDayTime`, spawn the stone, send `END_ROD` x12 in a ring of radius 0.45 at `pos.y + 0.2` plus `DustParticleOptions(hour.dustColorArgb(), 1.0f)` x8 in the same ring, play `SoundEvents.STONE_BUTTON_CLICK_ON` at volume 0.8 and `hour.soundPitch()`, send the hour message, then if the hour is MIDNIGHT and `level.random.nextFloat() < settings.timeCairnEchoChance()` and the echo stub returns a position, return GIVEN_WITH_ECHO. `setKeystone`: shrink the keystone stack by one in whichever hand holds it, `spawnReward` each stack from `rollKeystoneReward`, send `END_ROD` x24 rising over 1.5 blocks and the four dust colors x6 each in sequence, play `SoundEvents.BEACON_POWER_SELECT` at 0.7 / 1.0, send the keystone message. `rollKeystoneReward` copies `MemorialPlateBlock.rollOffering` with the keystone table.

- [ ] **Step 4: Register and edit the template**

`ModBlockId.TIME_CAIRN_DIAL(def("time_cairn_dial"))`, `ModBlockEntityId.TIME_CAIRN_DIAL("time_cairn_dial")`, `ModBlocks.TIME_CAIRN_DIAL` and `ModBlockEntities.TIME_CAIRN_DIAL` following the watchmaker entries. Blockstate file. With nbtlib (memory `feedback_nbtlib_missing_venv_workaround` if the import fails): append palette entry `chronodawn:time_cairn_dial` (no properties) and point the block at (1, 2, 1) to it. Assert before saving that (1, 2, 1) was `chronodawn:clockwork_dial`, and after saving that (1, 0, 1) is still `time_crystal_ore` and the file is gzip.

- [ ] **Step 5: Run to verify it passes**

Run the Step 2 command to `task5-green.log`.
Expected: PASS for every `time_cairn_*` test and `StructureTests`.

- [ ] **Step 6: Commit**

```bash
git add common/shared/src/main/java/com/chronodawn/registry/ModBlockId.java common/shared/src/main/java/com/chronodawn/registry/ModBlockEntityId.java \
        common/shared/src/main/java/com/chronodawn/blocks/TimeCairnDialBlockEntity.java \
        common/1.21.1/src/main/java/com/chronodawn/blocks/TimeCairnDialBlock.java \
        common/1.21.1/src/main/java/com/chronodawn/registry/ModBlocks.java common/1.21.1/src/main/java/com/chronodawn/registry/ModBlockEntities.java \
        common/shared/src/main/resources/assets/chronodawn/blockstates/time_cairn_dial.json \
        common/shared-1.21.1+/src/main/resources/data/chronodawn/structure/time_cairn.nbt \
        common/gametest/src/main/java/com/chronodawn/gametest/ common/26.3/src/main/java/com/chronodawn/gametest/StructureTests.java
git commit -m "feat: add the turnable Time Cairn Dial on 1.21.1"
```

---

### Task 6: Midnight echo and buried cache on 1.21.1

**Files:**
- Modify: `common/1.21.1/src/main/java/com/chronodawn/blocks/TimeCairnDialBlock.java` (replace the stub, add the ticker)
- Modify: `common/shared/src/main/java/com/chronodawn/blocks/TimeCairnDialBlockEntity.java` if more state is needed
- Test: `TimeCairnDialTests.java`

**Interfaces:**
- Consumes: `EchoWalk`, `CachePlacement`, tag `chronodawn:time_cairn_cache_ground`, loot table `chests/time_cairn_echo_cache`.
- Produces: `static Optional<BlockPos> summonEcho(ServerLevel level, BlockPos dialPos, Player player, RandomSource random)` (production distances) and `summonEcho(ServerLevel, BlockPos, Player, RandomSource, int minDistance, int maxDistance)` returning the chest position; `static Optional<BlockPos> findCacheSpot(ServerLevel level, BlockPos dialPos, RandomSource random, int minDistance, int maxDistance)` returning `T` (the marker position); `static void placeCache(ServerLevel level, BlockPos markerPos)`; `static void tickEcho(ServerLevel level, BlockPos pos, TimeCairnDialBlockEntity dial, long gameTime)` (the ticker passes `level.getGameTime()`); `static Block markerFor(BlockState ground)`.

- [ ] **Step 1: Write the failing tests**

Every GameTest runs in the 10x10x10 `chronodawn:empty_test` template, so each test builds a 9x9 platform with `helper.setBlock` at local y = 0 (`temporal_dirt`) and y = 1 (`temporal_grass_block`, or the ground named below), puts the dial at local (4, 2, 4), and calls the six-argument `summonEcho` with distances 3 and 4:
- `time_cairn_echo_places_cache_before_walking`: `summonEcho` returns a position whose horizontal distance from the dial is 3 to 4.5; in the same tick the block there is `minecraft:chest` with the cache loot table (`CompatGameTestHelper.hasLootTable`), the block above is `coarse_temporal_dirt`, and the dial block entity reports `echoActive()`.
- `time_cairn_echo_finds_no_spot_over_water`: platform of `minecraft:water` with the dial on a single `temporal_cobblestone` pillar: `summonEcho` is empty and no chest exists anywhere in the template bounds.
- `time_cairn_echo_cache_under_snow_layer`: platform of `minecraft:snow_block` with a `minecraft:snow` layer on top: the marker is `coarse_temporal_dirt` at the snow block's position, the layer above is air, the chest is below.
- `time_cairn_echo_marker_matches_ground`: on `minecraft:grass_block` the marker is `minecraft:coarse_dirt`; on `temporal_sand` it is `temporal_gravel`.
- `time_cairn_midnight_turn_rolls_echo`: with a test-only override `TimeCairnDialBlock.forceEchoChance(Float)` (package-visible static, `null` restores the config), chance 1 and `turn` at 18000 returns `GIVEN_WITH_ECHO` and a chest exists; chance 0 returns `GIVEN` and no chest; `turn` at 6000 with chance 1 returns `GIVEN`. Reset the override in `finally`.
- `time_cairn_echo_finishes_and_clears`: after `summonEcho`, call `tickEcho` with `gameTime + 1` and assert `echoActive()` is still true, then with `gameTime + EchoWalk.LINGER_TICKS + 400` and assert it is false. No waiting, so the default test timeout is enough.

- [ ] **Step 2: Run to verify it fails**

Run: `./gradlew :fabric:runGameTest -Ptarget_mc_version=1.21.1 > <worktree>/build/task6-red.log 2>&1`
Expected: compilation FAIL on `summonEcho`.

- [ ] **Step 3: Implement the echo**

`findCacheSpot`: for each offset from `CachePlacement.candidateOffsets(random.nextLong(), minDistance, maxDistance)`, compute `x, z`, skip if `!level.hasChunkAt(pos)`, take `y = level.getHeight(MOTION_BLOCKING_NO_LEAVES, x, z) - 1`, if the block there is `minecraft:snow` move down one, require the state to be in the tag and `below` to be solid (`isSolidRender` or the module's equivalent) with no block entity and empty fluid. `MOTION_BLOCKING_NO_LEAVES` counts fluids, so a water surface yields a water block that fails the tag check. `markerFor`: `temporal_sand` gives `ModBlocks.TEMPORAL_GRAVEL`, `grass_block`, `dirt`, `podzol`, `coarse_dirt` give `Blocks.COARSE_DIRT`, everything else gives `ModBlocks.COARSE_TEMPORAL_DIRT`. `placeCache`: remove a snow layer above, set the chest at `below` with a random `ChestBlock.FACING`, set its loot table with `level.random.nextLong()`, set the marker at `T`. `summonEcho`: `findCacheSpot`, `placeCache`, `dial.startEcho(new EchoWalk(dial center, chest center, gameTime), chestPos)`, send `SOUL` x16 and `END_ROD` x4 in a column from `dial.y + 1` to `+ 2.8` at the start, play `SoundEvents.SOUL_ESCAPE` at 1.0 / 0.8, send the echo message, return the chest position. `tickEcho` (called by the server ticker with `level.getGameTime()`, returns at once when `!echoActive()`): before arrival send `SOUL` x6 and `END_ROD` x1 at `positionAt` with `y = heightmap at that column`, spread 0.25 / 0.5 / 0.25, every tick; on the arrival tick send `SOUL` x20 sinking (speed -0.1) at the cache; after arrival every 40 ticks send `SOUL` x2 at `cachePos.above(2)`; when `finished` call `clearEcho()`. `getTicker` returns the ticker only on the server. In `turn`, replace the stub with `summonEcho`.

- [ ] **Step 4: Run to verify it passes**

Run the Step 2 command to `task6-green.log`.
Expected: PASS for every `time_cairn_*` test.

- [ ] **Step 5: Commit**

```bash
git add common/1.21.1/src/main/java/com/chronodawn/blocks/TimeCairnDialBlock.java \
        common/shared/src/main/java/com/chronodawn/blocks/TimeCairnDialBlockEntity.java \
        common/gametest/src/main/java/com/chronodawn/gametest/TimeCairnDialTests.java
git commit -m "feat: add the Time Cairn midnight echo and buried cache on 1.21.1"
```

---

### Task 7: Port to the other 13 modules

**Files:**
- Create: `common/<v>/src/main/java/com/chronodawn/blocks/TimeCairnDialBlock.java` and `items/CairnStoneItem.java`, `items/CairnKeystoneItem.java` for v in 1.20.1, 1.21.2, 1.21.4, 1.21.5, 1.21.6, 1.21.7, 1.21.8, 1.21.9, 1.21.10, 1.21.11, 26.1.2, 26.2, 26.3
- Modify: `common/<v>/src/main/java/com/chronodawn/registry/ModBlocks.java`, `ModBlockEntities.java`, `ModItems.java` and `gametest/RegistryDrivenTestGenerator.java` for the same 13

**Interfaces:**
- Consumes: everything Tasks 3, 5 and 6 produce. Each copy keeps the same public signatures, so the shared tests compile on every module.

- [ ] **Step 1: Copy block and items into each module, applying the era table**

Diff each new block against the same module's `SundialGnomonBlock.java` and `MemorialPlateBlock.java` for the interaction, client check, action bar, day time and loot lines, against `TimeKeeperVillagePlacer.java` for `setLootTable`, and against `TeleporterChargingHandler.java` for `DustParticleOptions`. Items follow the module's `JournalPageItem`.

- [ ] **Step 2: Register the block, block entity, five items, creative tab entries and the GameTest class in each module**

- [ ] **Step 3: Build and test every module**

Run (sandbox disabled), one at a time, each to `<worktree>/build/<name>.log`:
1. `./gradlew buildAll`. Expected: PASS.
2. `./gradlew testAll`. Expected: PASS.
3. `./gradlew :fabric:runGameTest -Ptarget_mc_version=1.20.1`, `./gradlew :fabric:runGameTest -Ptarget_mc_version=1.21.5`, `./gradlew :neoforge:runGameTestServer -Ptarget_mc_version=26.3`. Expected: PASS including every `time_cairn_*` and `recipe_loaded_cairn_keystone`. The full matrix runs in Task 9.

- [ ] **Step 4: Commit**

```bash
git commit -m "feat: port the Time Cairn to every supported version"
```

---

### Task 8: Cairn Stones in the Time Well (optional, data only)

**Files:**
- Create: `gameplay/time_well/cairn_stone_dawn.json`, `_noon.json`, `_dusk.json`, `_midnight.json` in the three loot table roots
- Test: `TimeCairnDialTests.java`

- [ ] **Step 1: Write the failing test**

`time_cairn_stones_exchange_in_time_well`: for each of the four stones, 20 rolls of `CompatGameTestHelper.rollModLootTable` on its table each yield one stack that is either `clockstone` or one of the other three stones, never the thrown stone itself.

- [ ] **Step 2: Run to verify it fails**

Run: `./gradlew :fabric:runGameTest -Ptarget_mc_version=1.21.1 > <worktree>/build/task8-red.log 2>&1`
Expected: FAIL, the tables are missing.

- [ ] **Step 3: Write the four tables**

One pool, rolls 1: `clockstone` x1 weight 9, and the three other stones weight 1 each, following `gameplay/time_well/clockstone.json` in each root's schema.

- [ ] **Step 4: Run to verify it passes**

Same command to `task8-green.log`, plus `./gradlew validateData`. Expected: PASS.

- [ ] **Step 5: Commit**

```bash
git add common/1.20.1/src/main/resources/data/chronodawn/loot_tables/gameplay/time_well/ \
        common/shared-1.21.1+/src/main/resources/data/chronodawn/loot_table/gameplay/time_well/ \
        common/26.3/src/main/resources/data/chronodawn/loot_table/gameplay/time_well/ \
        common/gametest/src/main/java/com/chronodawn/gametest/TimeCairnDialTests.java
git commit -m "feat: let the Time Well exchange duplicate Cairn Stones"
```

---

### Task 9: Docs, changelog, roadmap and full verification

**Files:**
- Modify: `CHANGELOG.md` (`## [Unreleased]`, `### Added`)
- Modify: `docs/player_guide.md:354` (replace the Time Cairn line)
- Modify: `docs/superpowers/specs/2026-10-14-wayside-features-roadmap.md` (status table, part 5 notes, foundation, lessons)

- [ ] **Step 1: Write the docs**

Player guide: a Time Cairn paragraph in the style of the Time Well line, covering the dial, the four stones and their hours, the 2x2 keystone recipe, setting the keystone, the midnight echo and the dug-up cache, the once-a-day cooldown, that breaking the dial drops a Clockwork Dial, the Time Well exchange if Task 8 shipped, and that the cooldown and echo chance are configurable. Changelog: one `Added` entry in the same voice. Roadmap: part 5 "Shipped (date)", foundation bullets for `CairnHour`, `EchoWalk`, `CachePlacement` and the particle-only echo pattern, and any lesson learned during Tasks 1 to 8.

- [ ] **Step 2: Full verification**

Run (sandbox disabled), one at a time, each to `<worktree>/build/<name>.log`: `./gradlew validateAll`, `./gradlew buildAll`, `./gradlew testAll`, `./gradlew gameTestAll`, then `./gradlew :fabric:prodSmokeTest -Ptarget_mc_version=1.21.1` and `./gradlew :neoforge:prodSmokeTest -Ptarget_mc_version=26.3`. Read `build/gametest-*.log`.
Expected: all PASS.

- [ ] **Step 3: Manual check (maintainer, outside the sandbox)**

`./gradlew runClientNeoForge1_21_1` with `time_cairn_echo_chance = 1.0` in the config, in a fresh world: find a cairn in new chunks, turn the dial and compare the stone with the sky, confirm the particles and sound, try again for the cooldown message, `/time set 18000` and turn another cairn to watch the echo and dig up the cache, craft a keystone in the 2x2 grid and set it, break a dial and place a Clockwork Dial to confirm it does nothing. Repeat the turn and echo on Fabric 1.21.1, Forge 1.20.1, NeoForge 26.3 and Fabric 26.2. Compare sound candidates with `/playsound` and take a screenshot of the echo. Record the chosen sounds and any texture redraw in the roadmap lessons and apply them before the final commit.

- [ ] **Step 4: Commit**

```bash
git add CHANGELOG.md docs/player_guide.md docs/superpowers/specs/2026-10-14-wayside-features-roadmap.md
git commit -m "docs: describe the Time Cairn and mark wayside part 5 as shipped"
```
