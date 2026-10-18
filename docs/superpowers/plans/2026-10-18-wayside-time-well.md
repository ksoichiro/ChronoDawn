# Wayside Time Well Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Make newly generated Time Wells wishing wells: a player drops an accepted item into the well water, and after about 2 seconds one of it is exchanged through a loot table while everything else rises back out, once per player per well per cooldown.

**Architecture:** Version-independent logic (`WellQueue`, `TimeWellOutcome`) lives in `common/shared/.../core/wayside` and is unit tested. A new `time_well_stone` block subclasses each version's `MossyTemporalCobblestoneBlock`, implements `EntityBlock` with a server ticker, and owns a `TimeWellStoneBlockEntity` that extends the part 1 `CooldownActivatorBlockEntity`. The ticker watches the water block next to it, queues player-thrown items, and answers them through `TimeWellStoneBlock.answer`. Results come from `chronodawn:gameplay/time_well/<item path>` loot tables. `time_well.nbt` gets water and the stone.

**Tech Stack:** Java 17/21/25, Architectury, Fabric, Forge 1.20.1, NeoForge, JUnit 5, Minecraft GameTest, Python `nbtlib` for the NBT edit.

**Spec:** `docs/superpowers/specs/2026-10-18-wayside-time-well-design.md`

## Global Constraints

- Supported modules: 1.20.1, 1.21.1, 1.21.2 (also serves 1.21.3), 1.21.4, 1.21.5, 1.21.6, 1.21.7, 1.21.8, 1.21.9, 1.21.10, 1.21.11, 26.1.2, 26.2, 26.3.
- Block ID `time_well_stone`, block entity ID `time_well_stone`. No `BlockItem`. Looks like `mossy_temporal_cobblestone`. Breaking it drops `chronodawn:mossy_temporal_cobblestone`.
- Answer delay `ANSWER_DELAY_TICKS` = 40, measured from when the well first sees the item.
- Only one item of one stack is exchanged per answer. Everything not exchanged rises out unchanged. Nothing thrown by a player is destroyed without a replacement.
- A wish counts only when the item's thrower resolves to a `Player` in the well's level when first seen. Other items are ignored (left in place, never marked).
- Only an exchange consumes the cooldown. Cooldown default 24000 ticks, counted with `Level#getGameTime`. Config key `time_well_cooldown_ticks` in `[gameplay.wayside]`, range 20 to 1728000, out-of-range falls back per key.
- Exchange loot tables at `chronodawn:gameplay/time_well/<item path>`, looked up only for `chronodawn` items. A missing table or an empty roll means "not accepted".
- Exchange table (one pool, one roll, weighted entries):

  | Thrown | Common (weight) | Rare (weight) |
  |---|---|---|
  | `time_wheat` | `time_bread` x1 (9) | `golden_time_wheat` x1 (1) |
  | `time_bread` | `enhanced_time_bread` x1 (9) | `time_fruit_pie` x1 (1) |
  | `chrono_melon_slice` | `chrono_melon_juice` x1 (9) | `glistening_chrono_melon` x1 (1) |
  | `raw_temporal_amber` | `temporal_amber_dust` x2 to 3 (9) | `time_crystal` x1 (1) |
  | `clockstone` | `clockstone` x2 (9) | `time_crystal` x1 (1) |
  | `time_crystal` | `time_crystal` x2 (19) | `enhanced_clockstone` x1 (1) |

- Exchange sound `SoundEvents.ENCHANTMENT_TABLE_USE`, return-only sound `SoundEvents.GENERIC_SPLASH`. Exchange particles `GLOW` over the water and rising `END_ROD`. Return-only particles `BUBBLE`.
- Rising items spawn at the shaft center, 2.2 blocks above the water block's bottom (just above the rim), with no thrower, a default pickup delay, and velocity toward the thrower so they land outside the well. Without a thrower in range (16 blocks) they get a random horizontal push instead.
- Action-bar messages, at most one per player per tick, priority exchange > cooldown > not accepted.
- Code comments and docs in English. No em dashes or semicolons in docs and commit messages.
- Do not commit without the user's go-ahead. Each "Commit" step means: stage the listed files and ask the controller/user before running `git commit`. Commit messages use Conventional Commits and end with `Co-Authored-By: Claude Opus 5.5 <noreply@anthropic.com>`. Never write issue-closing keywords.
- Never edit lang JSON with `json.load`/`json.dump`. Insert lines textually so blank-line groupings survive.
- Write temporary files under `$TMPDIR`.
- Gradle tasks fork a JVM and need the sandbox disabled. Run one Gradle invocation at a time in this worktree. Redirect long Gradle output to `<worktree>/build/<name>.log` with the absolute worktree path.

## Review Focus

1. A player drops a full stack of 64 Clockstone: exactly one exchange and 63 Clockstone rise back out. Test `time_well_exchanges_one_and_returns_rest` (Task 3).
2. A returned item lands back in the water: it has no thrower, so it is ignored and never loops or re-triggers. Test `time_well_ignores_item_without_thrower` (Task 3).
3. One player drops two accepted stacks in the same moment: the first is exchanged, the second returns, and the action bar shows the exchange message, not the cooldown one. Tests `dueEntriesComeOutInFirstSeenOrder` and `exchangeMessageWinsOverCooldown` (Task 1), `time_well_second_wish_returns_unchanged` (Task 3).
4. The thrower logs out or changes dimension during the 2-second wait: the item is returned with a random push and no cooldown is consumed. Test `time_well_returns_when_thrower_gone` (Task 3).
5. A datapack empties or removes an exchange table: the item is returned, the cooldown is not consumed, and the same player can still exchange another item. Test `time_well_unaccepted_item_keeps_cooldown_free` (Task 3).

---

## Version eras used in this plan

Block (`TimeWellStoneBlock`). The memorial plate (`blocks/MemorialPlateBlock.java`) of each module already uses the right client check, action bar and loot lookup for that module, so copy those lines from it.

| Era | Modules | Client check | Action bar | Loot table lookup | Registry key class |
|---|---|---|---|---|---|
| A | 1.20.1 | `level.isClientSide` | `displayClientMessage(c, true)` | `getLootData().getLootTable(ResourceLocation)` | `ResourceLocation` |
| B | 1.21.1, 1.21.2, 1.21.4, 1.21.5 to 1.21.8 | `level.isClientSide` | as A | `reloadableRegistries().getLootTable(ResourceKey)` | `ResourceLocation` |
| C | 1.21.9, 1.21.10 | `level.isClientSide()` | as A | as B | `ResourceLocation` |
| D | 1.21.11 | as C | as A | as B | `Identifier` |
| E | 26.1.2, 26.2, 26.3 | as C | `player.sendOverlayMessage(c)` | as B | `Identifier` |

- `ItemEntity#getOwner()` (returns `Entity`) and `ItemEntity#setNeverPickUp()` exist with these names from 1.21.1 to 26.3 (checked with `javap`). For 1.20.1, confirm with `javap` against the Forge 1.20.1 mapped jar or by compiling; if `getOwner()` is missing there, read the thrower through the 1.20.1 API instead.
- `createProperties()`: copy the body of that module's `MossyTemporalCobblestoneBlock.createProperties()`. Where it calls `setId(...)`, use the path `"time_well_stone"`.
- `getTicker(Level, BlockState, BlockEntityType<T>)` from `EntityBlock`: return `null` on the client and when the type is not `ModBlockEntities.TIME_WELL_STONE.get()`.
- `ModBlockEntities`, 1.20.1 and 1.21.1: `BlockEntityType.Builder.of(Factory::new, block).build(null)`. 1.21.2 and later: `new BlockEntityType<>(Factory::new, Set.<Block>of(block))`. Copy each module's `MEMORIAL_PLATE` entry.

Data roots:
- Loot tables: `common/1.20.1/.../data/chronodawn/loot_tables/`, `common/shared-1.21.1+/.../data/chronodawn/loot_table/`, and `common/26.3/.../data/chronodawn/loot_table/` (26.3 schema: `"modifier"` object in place of `"functions"`, `"condition"` object in place of `"conditions"`). Compare with the three `gameplay/lost_adventurer_memorial_blessed.json` files.
- Lang: `en_us.json` and `ja_jp.json` in `common/1.20.1`, `common/1.21.1`, `common/shared-1.21.2+` (`src/main/resources/assets/chronodawn/lang/`).
- Pickaxe tag: `common/1.20.1/.../tags/blocks/mineable/pickaxe.json` and `common/shared-1.21.1+/.../tags/block/mineable/pickaxe.json`.

---

### Task 1: WellQueue and TimeWellOutcome

**Files:**
- Create: `common/shared/src/main/java/com/chronodawn/core/wayside/WellQueue.java`
- Create: `common/shared/src/main/java/com/chronodawn/core/wayside/TimeWellOutcome.java`
- Test: `common/shared/src/test/java/com/chronodawn/unit/WellQueueTest.java`

**Interfaces:**
- Produces:
  - `public final class WellQueue` with `public static final long ANSWER_DELAY_TICKS = 40`, `public void offer(UUID item, UUID thrower, long gameTime)` (keeps the first-seen time and thrower if the item is already queued), `public boolean contains(UUID item)`, `public List<Entry> due(long gameTime)` (entries with `gameTime - firstSeen >= ANSWER_DELAY_TICKS`, in first-seen order, removed from the queue), `public void forget(UUID item)`, `public int size()`.
  - `public record Entry(UUID item, UUID thrower, long firstSeen)` nested in `WellQueue`.
  - `public enum TimeWellOutcome { EXCHANGED, COOLDOWN, NOT_ACCEPTED, THROWER_GONE }` with `public static TimeWellOutcome mostImportant(TimeWellOutcome a, TimeWellOutcome b)` (the earlier constant wins, `null` loses to anything) and `public String messageKey()` returning `"message.chronodawn.time_well.answered"`, `".cooldown"`, `".not_accepted"`, or `null` for `THROWER_GONE`.

- [ ] **Step 1: Write the failing tests**

`WellQueueTest` (JUnit 5, style of `StepOnTrackerTest`):
- `notDueBeforeDelay`: offer at 100, `due(139)` is empty, `due(140)` has one entry with the offered thrower.
- `dueEntriesComeOutInFirstSeenOrder`: offer A at 100, B at 101, `due(200)` returns A then B.
- `reofferKeepsFirstSeen`: offer A at 100, offer A at 130, `due(140)` contains A.
- `dueRemovesEntries`: after `due(140)` returned A, `due(500)` is empty and `contains(A)` is false.
- `forgetDropsEntry`: offer A, `forget(A)`, `size()` is 0.
- `exchangeMessageWinsOverCooldown`: `mostImportant(COOLDOWN, EXCHANGED) == EXCHANGED`, `mostImportant(NOT_ACCEPTED, COOLDOWN) == COOLDOWN`, `mostImportant(null, NOT_ACCEPTED) == NOT_ACCEPTED`.
- `throwerGoneHasNoMessage`: `THROWER_GONE.messageKey()` is null, `EXCHANGED.messageKey()` equals `"message.chronodawn.time_well.answered"`.

- [ ] **Step 2: Run to verify it fails**

Run (sandbox disabled): `./gradlew :common-1.21.1:test -Ptarget_mc_version=1.21.1 --tests '*WellQueueTest'`
Expected: compilation FAIL, `WellQueue` not found.

- [ ] **Step 3: Implement `WellQueue` and `TimeWellOutcome`**

Back the queue with a `LinkedHashMap<UUID, Entry>` so iteration order is first-seen order. License header first, as in `StepOnTracker.java`.

- [ ] **Step 4: Run to verify it passes**

Same command. Expected: PASS, 7 tests.

- [ ] **Step 5: Commit**

```bash
git add common/shared/src/main/java/com/chronodawn/core/wayside/WellQueue.java \
        common/shared/src/main/java/com/chronodawn/core/wayside/TimeWellOutcome.java \
        common/shared/src/test/java/com/chronodawn/unit/WellQueueTest.java
git commit -m "feat: add the Time Well answer queue"
```

---

### Task 2: Time Well cooldown config key

**Files:**
- Modify: `common/shared/src/main/java/com/chronodawn/config/WaysideSettings.java` (add the fourth record component)
- Modify: `common/shared/src/main/java/com/chronodawn/config/ConfigDefaults.java:80`
- Modify: `common/shared/src/main/java/com/chronodawn/config/ConfigLoader.java:118-122` (constants) and the `parseWayside` method (~575-611)
- Modify: `common/shared/src/main/resources/chronodawn-default-config.toml` (after `memorial_cooldown_ticks`, ~346)
- Modify: `docs/configuration.md:529-549`
- Test: `common/shared/src/test/java/com/chronodawn/unit/ConfigLoaderTest.java:680-720`

**Interfaces:**
- Produces: `WaysideSettings#timeWellCooldownTicks()` (`int`), default 24000.

- [ ] **Step 1: Write the failing tests**

In `ConfigLoaderTest`, extend the existing wayside override test to set `time_well_cooldown_ticks = 6000` and expect `new WaysideSettings(6000, 1200, 12000, 6000)`. Add `wayside_badTimeWellCooldown_fallsBackAlone`, mirroring `wayside_badMemorialCooldown_fallsBackAlone`: `time_well_cooldown_ticks = 1728001` gives `ConfigDefaults.WAYSIDE_DEFAULTS.timeWellCooldownTicks()` while the other three keys keep their configured values.

- [ ] **Step 2: Run to verify it fails**

Run (sandbox disabled): `./gradlew :common-1.21.1:test -Ptarget_mc_version=1.21.1 --tests '*ConfigLoaderTest'`
Expected: compilation FAIL, the four-argument constructor does not exist.

- [ ] **Step 3: Add the key**

Add `int timeWellCooldownTicks` to `WaysideSettings` and its Javadoc, `24000` to `WAYSIDE_DEFAULTS`, `K_WS_TIME_WELL_COOLDOWN_TICKS = "time_well_cooldown_ticks"` and `MAX_TIME_WELL_COOLDOWN_TICKS = 1_728_000` to `ConfigLoader`, and parse and range-check it exactly like `memorial_cooldown_ticks`. In the default TOML and `docs/configuration.md`, add the key after `memorial_cooldown_ticks` with the comment/description "Time Well: how long (in ticks, 20 ticks = 1 second) each player must wait before the same well grants another wish. Counted in game time, so sleeping does not shorten it." Update the section intro in `docs/configuration.md` to mention Time Wells.

- [ ] **Step 4: Run to verify it passes**

Same command. Expected: PASS.

- [ ] **Step 5: Commit**

```bash
git add common/shared/src/main/java/com/chronodawn/config/WaysideSettings.java \
        common/shared/src/main/java/com/chronodawn/config/ConfigDefaults.java \
        common/shared/src/main/java/com/chronodawn/config/ConfigLoader.java \
        common/shared/src/main/resources/chronodawn-default-config.toml \
        common/shared/src/test/java/com/chronodawn/unit/ConfigLoaderTest.java \
        docs/configuration.md
git commit -m "feat: add the time_well_cooldown_ticks config key"
```

---

### Task 3: Time Well stone on 1.21.1 (end to end)

Builds the block on the modpack target 1.21.1 with all shared resources. Other modules do not compile between this task and Task 4, because the shared block entity references `ModBlockEntities.TIME_WELL_STONE`. Build and test only 1.21.1 here.

**Files:**
- Modify: `common/shared/src/main/java/com/chronodawn/registry/ModBlockId.java` (after `MEMORIAL_PLATE`, line ~254)
- Modify: `common/shared/src/main/java/com/chronodawn/registry/ModBlockEntityId.java` (after `MEMORIAL_PLATE`, line ~36)
- Create: `common/shared/src/main/java/com/chronodawn/blocks/TimeWellStoneBlockEntity.java`
- Create: `common/1.21.1/src/main/java/com/chronodawn/blocks/TimeWellStoneBlock.java`
- Modify: `common/1.21.1/src/main/java/com/chronodawn/registry/ModBlocks.java` and `ModBlockEntities.java` (after `MEMORIAL_PLATE`)
- Create: `common/shared/src/main/resources/assets/chronodawn/blockstates/time_well_stone.json`
- Create: block loot table `blocks/time_well_stone.json` in the three loot roots
- Create: six exchange tables `gameplay/time_well/<item>.json` in the three loot roots
- Modify: both `mineable/pickaxe.json` files
- Modify: the six lang files
- Create: `common/gametest/src/main/java/com/chronodawn/gametest/TimeWellTests.java`
- Modify: `common/1.21.1/src/main/java/com/chronodawn/gametest/RegistryDrivenTestGenerator.java` (after the `MemorialPlateTests` line, ~455)

**Interfaces:**
- Consumes: `WellQueue`, `TimeWellOutcome` (Task 1), `WaysideSettings#timeWellCooldownTicks()` (Task 2), `CooldownActivatorBlockEntity#tryActivate(UUID, long, long)` and `PlayerCooldownTracker.toMinutesRoundedUp(long)` (part 1).
- Produces:
  - `ModBlockId.TIME_WELL_STONE` (no `.cutout()`), `ModBlockEntityId.TIME_WELL_STONE`
  - `ModBlocks.TIME_WELL_STONE` (`RegistrySupplier<Block>`), `ModBlockEntities.TIME_WELL_STONE` (`RegistrySupplier<BlockEntityType<TimeWellStoneBlockEntity>>`)
  - `public class TimeWellStoneBlockEntity extends CooldownActivatorBlockEntity` with ctor `(BlockPos, BlockState)`, `public WellQueue queue()`, `public BlockPos cachedWater()` / `public void cacheWater(BlockPos)` (transient, may be null)
  - `public class TimeWellStoneBlock extends MossyTemporalCobblestoneBlock implements EntityBlock` with:
    - `public static BlockBehaviour.Properties createProperties()`
    - `public static BlockPos findWater(Level level, BlockPos stonePos)`: the first of the four horizontal neighbours whose fluid state is a water source, or null
    - `public static List<ItemStack> rollExchange(ServerLevel level, BlockPos pos, Item item, Player player)`: empty when the item is not a `chronodawn` item, has no table, or the roll is empty. `player` may be null.
    - `public static TimeWellOutcome answer(ServerLevel level, BlockPos stonePos, ItemEntity item, Player thrower)`: performs one answer and its effects, never sends a message. `thrower` null means the thrower is gone.
    - `public static void markWaiting(TimeWellStoneBlockEntity well, ItemEntity item, Player thrower, long gameTime)`: `setNeverPickUp()` and `queue().offer(...)`. `tick` calls it for each new player-thrown item
    - `public static void tick(ServerLevel level, BlockPos pos, TimeWellStoneBlockEntity well)` called by the ticker
  - `TimeWellTests.generateTests(MobBehaviorTests.TestFactory<T>)`

- [ ] **Step 1: Write the failing GameTests**

`TimeWellTests.java`, same shape as `MemorialPlateTests` (`generateTests`, `CompatGameTestHelper.fail`). Each test places `Blocks.WATER` at `WATER_POS = (1, 2, 1)` and `ModBlocks.TIME_WELL_STONE` at `STONE_POS = (1, 2, 0)`, spawns item entities into the water with `new ItemEntity(level, x, y, z, stack)` + `level.addFreshEntity`, and uses `CompatGameTestHelper.makeMockPlayer(helper)` as the thrower (two mock players for the "different thrower" case). "Rising items" means `ItemEntity`s found in an AABB around the well inflated by 3, other than the original. Tests:
- `time_well_stone_has_block_entity`: the placed stone has a `TimeWellStoneBlockEntity`, and `findWater` returns the absolute `WATER_POS`.
- `time_well_exchange_tables_roll_expected_items`: for each of the six rows of the Global Constraints table, 30 rolls of `rollExchange(..., null)` each yield exactly one stack whose item is the common or the rare result and whose count is in the stated range.
- `time_well_exchanges_one_and_returns_rest`: a stack of 64 `clockstone`, `answer` returns `EXCHANGED`, the original entity is removed, and the rising items are one exchange result (2 `clockstone` or 1 `time_crystal`) plus one stack of 63 `clockstone`.
- `time_well_second_wish_returns_unchanged`: after an exchange by player A, a second `clockstone` from A returns `COOLDOWN` and rises back as 1 `clockstone`. A `clockstone` from player B returns `EXCHANGED`.
- `time_well_unaccepted_item_keeps_cooldown_free`: `minecraft:dirt` x5 from A returns `NOT_ACCEPTED` and rises back as 5 `dirt`. `chronodawn:temporal_stone` (a chronodawn item with no table) also returns `NOT_ACCEPTED`. Then a `clockstone` from A returns `EXCHANGED`.
- `time_well_returns_when_thrower_gone`: `answer(..., null)` on a `clockstone` returns `THROWER_GONE`, it rises back as 1 `clockstone`, and a following `clockstone` answer for a mock player returns `EXCHANGED`.
- `time_well_ignores_item_without_thrower`: an item spawned with `new ItemEntity(...)` has no thrower. After one `tick` call, `well.queue().size()` is 0, the item is still alive, and `hasPickUpDelay()` is false.
- `time_well_waiting_item_does_not_merge`: `markWaiting` an item for a mock player, then spawn a second identical stack touching it. After 10 ticks (`helper.runAfterDelay`), the first item has `hasPickUpDelay()` true and both entities still exist. Shared GameTest code cannot call `setThrower` (its parameter type differs on 1.20.1), which is why the test enters through `markWaiting` instead of `getOwner()`.
- `time_well_stone_drops_mossy_cobblestone`: `BlockState#getDrops` with a wooden pickaxe yields one `mossy_temporal_cobblestone` (copy `MemorialPlateTests.testDrops`).

Register in `common/1.21.1/.../RegistryDrivenTestGenerator.java` after the `MemorialPlateTests` line:

```java
        all.addAll(TimeWellTests.generateTests(NamedTest::new));
```

- [ ] **Step 2: Run to verify it fails**

Run (sandbox disabled): `./gradlew :common-1.21.1:compileJava -Ptarget_mc_version=1.21.1`
Expected: compilation FAIL, `TimeWellStoneBlock`, `TimeWellStoneBlockEntity` and `ModBlocks.TIME_WELL_STONE` not found.

- [ ] **Step 3: IDs and shared block entity**

`TIME_WELL_STONE(def("time_well_stone"))` in `ModBlockId`, `TIME_WELL_STONE("time_well_stone")` in `ModBlockEntityId`. `TimeWellStoneBlockEntity` follows `MemorialPlateBlockEntity`: it adds a transient `WellQueue` and a transient cached water position, and no saved data.

- [ ] **Step 4: 1.21.1 block**

`TimeWellStoneBlock` in `common/1.21.1/.../blocks/`. Behavior the tests do not fully pin:
- `tick`: every tick on the server. Resolve the water position from the cache, re-running `findWater` when it is null or no longer water. Do nothing without water. For each `ItemEntity` whose bounding box intersects the water block and that is not yet queued: if `getOwner()` is a `Player`, call `markWaiting`. Items without a player owner are skipped every tick and never touched. Then for each `due` entry, look the item up with `level.getEntity(uuid)` (wherever it drifted). If it is gone, drop the entry. Otherwise resolve the thrower with `level.getPlayerByUUID(thrower)` and call `answer`. Collect outcomes per player with `TimeWellOutcome.mostImportant` and send one action-bar message each afterwards. The cooldown message takes the remaining minutes as its argument.
- `answer`: copy the stack and discard the entity. With no thrower: return everything (random push), `THROWER_GONE`. Otherwise roll `rollExchange` with the thrower. Empty: return everything, `NOT_ACCEPTED`. Not empty: `tryActivate(thrower, gameTime, timeWellCooldownTicks)`. Refused: return everything, `COOLDOWN`. Accepted: shrink the copy by one, raise the results and the rest, play the exchange effects, `EXCHANGED`. Return-only outcomes play the splash effects.
- Raising an item: a new `ItemEntity` at the shaft center (water x/z + 0.5), `water.getY() + 2.2`, with `setDefaultPickUpDelay()`. Toward a thrower within 16 blocks: horizontal unit vector to the thrower times 0.25, y velocity 0.35. Otherwise a random horizontal unit vector times 0.25, same y velocity. The rim is one block wide, so 0.25 clears it. Check in play-testing.
- Effects: exchange plays `ENCHANTMENT_TABLE_USE` (volume 1.0, pitch 1.0) at the water, 12 `GLOW` particles spread over the water surface and 10 `END_ROD` rising with small upward speed. Return-only plays `GENERIC_SPLASH` (volume 0.5) and 6 `BUBBLE` particles.
- `rollExchange`: `BuiltInRegistries.ITEM.getKey(item)`. Namespace not `chronodawn` gives an empty list. Look up `gameplay/time_well/<path>` the way `MemorialPlateBlock.rollOffering` does, with `THIS_ENTITY` and luck when the player is not null, `LootContextParamSets.CHEST`. A missing table returns `LootTable.EMPTY`, which rolls nothing, so no special case is needed. Drop empty stacks from the result.

- [ ] **Step 5: 1.21.1 registration**

`ModBlocks.TIME_WELL_STONE` and `ModBlockEntities.TIME_WELL_STONE` after the `MEMORIAL_PLATE` entries, same Javadoc style ("Time Well Stone - activator of the Time Well wayside feature. No item: it only appears through worldgen, and breaking it drops the plain mossy temporal cobblestone.").

- [ ] **Step 6: Shared resources**

- `blockstates/time_well_stone.json`: one `""` variant with model `chronodawn:block/mossy_temporal_cobblestone`.
- Block loot table `blocks/time_well_stone.json` in the three roots: copy the three `blocks/memorial_plate.json` files and change the item to `chronodawn:mossy_temporal_cobblestone`.
- Exchange tables `gameplay/time_well/{time_wheat,time_bread,chrono_melon_slice,raw_temporal_amber,clockstone,time_crystal}.json` in the three roots, type `minecraft:chest`, one pool, `rolls` 1, two `minecraft:item` entries with the weights and counts from the Global Constraints table (`set_count` only where the count is not 1, uniform for the 2 to 3 range).
- Pickaxe tags: add `"chronodawn:time_well_stone",` after `"chronodawn:memorial_plate",` in both files.
- Lang, line-based. After `"block.chronodawn.memorial_plate"`: en `"Time Well Stone"`, ja `"時の井戸の石"`. After `"message.chronodawn.memorial_plate.cooldown"`:
  - en: `answered` "The well answers your wish.", `cooldown` "The well is still. Try again in about %s min.", `not_accepted` "The well has no use for this."
  - ja: `answered` 「井戸が願いに応えた。」, `cooldown` 「井戸は静まり返っている。あと約%s分で再び願える。」, `not_accepted` 「井戸はこれを受け取らないようだ。」
- Validate each edited JSON file with `python3 -I -c "import json,sys; [json.load(open(f)) for f in sys.argv[1:]]" <files>`.

- [ ] **Step 7: Build and run tests for 1.21.1**

Run (sandbox disabled), one at a time:
1. `./gradlew :common-1.21.1:test -Ptarget_mc_version=1.21.1`. Expected: PASS. If `CrossVersionConsistencyTest` fails only because other modules lack `TimeWellStoneBlock`, note it and continue. It must pass at the end of Task 4.
2. `./gradlew :fabric:runGameTest -Ptarget_mc_version=1.21.1 > <worktree>/build/task3-fabric.log 2>&1`. Expected: PASS, including the nine `time_well_*` tests.
3. `./gradlew :neoforge:runGameTestServer -Ptarget_mc_version=1.21.1 > <worktree>/build/task3-neoforge.log 2>&1`. Expected: PASS, same tests.
4. `./gradlew validateResources validateTranslations validateData validateEraJsonFormat validateLangParity`. Expected: PASS.

- [ ] **Step 8: Commit**

Stage every file listed under **Files** above.

```bash
git commit -m "feat: add the Time Well Stone that grants wishes on 1.21.1"
```

---

### Task 4: Port the Time Well stone to the other 13 modules

**Files:**
- Create: `common/<v>/src/main/java/com/chronodawn/blocks/TimeWellStoneBlock.java` for v in 1.20.1, 1.21.2, 1.21.4, 1.21.5, 1.21.6, 1.21.7, 1.21.8, 1.21.9, 1.21.10, 1.21.11, 26.1.2, 26.2, 26.3
- Modify: `common/<v>/src/main/java/com/chronodawn/registry/ModBlocks.java` and `ModBlockEntities.java` for the same 13
- Modify: `common/<v>/src/main/java/com/chronodawn/gametest/RegistryDrivenTestGenerator.java` for the same 13 (`TimeWellTests` line after `MemorialPlateTests`)

**Interfaces:**
- Consumes: everything Task 3 produces. Each copy keeps the same public static signatures, so `TimeWellTests` compiles on every module.

- [ ] **Step 1: Copy the 1.21.1 block into each module, applying that module's era column**

Use the era table. Diff each new file against the same module's `MemorialPlateBlock.java` for the client check, action bar and loot lookup lines, and against its `MossyTemporalCobblestoneBlock.java` for `createProperties()`.

- [ ] **Step 2: Register in each module and add the GameTest line**

- [ ] **Step 3: Build and test every module**

Run (sandbox disabled), one at a time, each to `<worktree>/build/<name>.log`:
1. `./gradlew buildAll`. Expected: PASS.
2. `./gradlew testAll`. Expected: PASS, including `CrossVersionConsistencyTest`.
3. `./gradlew :forge:runGameTestServer -Ptarget_mc_version=1.20.1`, `./gradlew :fabric:runGameTest -Ptarget_mc_version=26.3` and `./gradlew :neoforge:runGameTestServer -Ptarget_mc_version=26.3`. Expected: PASS, including the `time_well_*` tests. The full matrix runs in Task 6.

- [ ] **Step 4: Commit**

```bash
git commit -m "feat: port the Time Well Stone to every supported version"
```

---

### Task 5: Put water and the stone into the Time Well structure

**Files:**
- Modify: `common/shared-1.21.1+/src/main/resources/data/chronodawn/structure/time_well.nbt`
- Modify: `common/gametest/src/main/java/com/chronodawn/gametest/StructureTests.java:91-94`
- Modify: `common/26.3/src/main/java/com/chronodawn/gametest/StructureTests.java:91-94`

**Interfaces:**
- Consumes: `ModBlockId.TIME_WELL_STONE`, `ModBlocks.TIME_WELL_STONE` (Task 3).

- [ ] **Step 1: Update both structure tests first**

In the `time_well` spec of both files, add:

```java
                new BlockRequirement(ModBlockId.TIME_WELL_STONE.id(), ModBlocks.TIME_WELL_STONE, 1),
                new BlockRequirement("water", () -> Blocks.WATER, 1)
```

- [ ] **Step 2: Run to verify it fails**

Run (sandbox disabled): `./gradlew :fabric:runGameTest -Ptarget_mc_version=1.21.1 > <worktree>/build/task5-red.log 2>&1`
Expected: FAIL on the `time_well` structure test, expecting 1 `time_well_stone`.

- [ ] **Step 3: Edit the NBT**

With nbtlib (see memory `feedback_nbtlib_missing_venv_workaround` if the import fails): append palette entries `chronodawn:time_well_stone` (no properties) and `minecraft:water` with `Properties {level: "0"}`. Point the block at (1, 1, 1) (currently air) to the water entry and the block at (1, 1, 0) (currently `mossy_temporal_cobblestone`) to the stone entry. Assert before saving that (1, 1, 1) was air and (1, 1, 0) was `mossy_temporal_cobblestone`, and after saving that `(1, 0, 1)` is still `temporal_amber_ore` and the file is still gzip.

- [ ] **Step 4: Run to verify it passes**

Run (sandbox disabled), one at a time:
1. `./gradlew :fabric:runGameTest -Ptarget_mc_version=1.21.1 > <worktree>/build/task5-green.log 2>&1`
2. `./gradlew :fabric:runGameTest -Ptarget_mc_version=1.20.1 > <worktree>/build/task5-green-1.20.1.log 2>&1` (the 1.20.1 copy comes from `convertNbt`)
3. `./gradlew :fabric:runGameTest -Ptarget_mc_version=26.3 > <worktree>/build/task5-green-26.3.log 2>&1`

Expected: PASS, including the `time_well` structure test.

- [ ] **Step 5: Commit**

```bash
git add common/shared-1.21.1+/src/main/resources/data/chronodawn/structure/time_well.nbt \
        common/gametest/src/main/java/com/chronodawn/gametest/StructureTests.java \
        common/26.3/src/main/java/com/chronodawn/gametest/StructureTests.java
git commit -m "feat: generate Time Wells with water and a Time Well Stone"
```

---

### Task 6: Docs, changelog and full verification

**Files:**
- Modify: `CHANGELOG.md` (`## [Unreleased]` / `### Added`, after the Lost Adventurer Memorial entry)
- Modify: `docs/player_guide.md:342` (Time Well bullet)
- Modify: `docs/superpowers/specs/2026-10-14-wayside-features-roadmap.md` (Status table row for part 3, foundation notes)

- [ ] **Step 1: Changelog and player guide**

Changelog entry in the same bold-lead style as the memorial entry: Time Wells now hold water, dropping one of six listed Chrono Dawn materials or foods into the water exchanges one of it for a slightly better item with a small chance of something rarer, the rest and anything else rises back out, about once every 20 minutes of game time per player per well by default (`time_well_cooldown_ticks` under `[gameplay.wayside]`), only wells in newly generated chunks.

Replace the player guide bullet with the same content in guide style, using a colon after the bold name (no em dash), and list the six accepted items.

Roadmap: set part 3's status to `Implemented on branch, manual check pending`, and add `WellQueue`, `TimeWellOutcome` and the ticking activator pattern to "Foundation available".

- [ ] **Step 2: Full verification**

Run (sandbox disabled), one at a time, each redirected to `<worktree>/build/<name>.log`:
1. `./gradlew validateResources validateTranslations validateData validateEraTagParity validateEraJsonFormat validateLangParity`
2. `./gradlew buildAll`
3. `./gradlew testAll`
4. `./gradlew gameTestAll`, then read every `build/gametest-*.log` for `required tests passed` and `required tests failed`.

Expected: every step passes. No Mixin changed, so `prodSmokeTest` is not required.

- [ ] **Step 3: Manual check (user)**

Present to the user. Accepted runtime checks: 1.21.1 NeoForge and Fabric, 1.20.1 Forge, 26.3 NeoForge, 26.2 Fabric.
1. `./gradlew runClientNeoForge1_21_1` (and the others above).
2. New world, enter Chrono Dawn, find a newly generated well or use `/place feature chronodawn:time_well`.
3. The water sits one block below the rim, does not spread, and the amber ore is visible at the bottom. If the water is missing, stop: the spec's fallback (the block entity places the water source on its first tick) needs a follow-up task.
4. Drop one Clockstone into the water: after about 2 seconds the water glows, the sound is clearly audible, 2 Clockstone (or a Time Crystal) fly out and land outside the well near you, and the action bar shows the answered message.
5. Drop another Clockstone: it comes back with the cooldown message.
6. Drop a stack of dirt: it comes back whole with the "no use" message.
7. Drop 10 Time Wheat right after another player has used the well (or with a second account): one exchange, 9 come back.
8. Break the Time Well Stone in survival with a pickaxe: Mossy Temporal Cobblestone drops.

Expected: all of the above. GameTest servers may omit Chrono Dawn dimensions, so this needs a normal client.

- [ ] **Step 4: Commit**

```bash
git add CHANGELOG.md docs/player_guide.md docs/superpowers/specs/2026-10-14-wayside-features-roadmap.md
git commit -m "docs: describe Time Well wishes"
```
