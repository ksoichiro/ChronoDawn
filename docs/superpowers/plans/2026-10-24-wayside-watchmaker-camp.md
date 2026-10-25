# Wayside Watchmaker's Camp Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Newly generated Watchmaker's Camps get an openable tent and a per-player, take-only chest of rare materials. An emptied chest refills for that player after a cooldown, and when Lootr is installed the camp keeps a plain loot chest for Lootr to convert.

**Architecture:** The camp NBT keeps a vanilla `minecraft:chest` with the loot table. After `NbtTemplateFeature` places `watchmaker_camp`, a shared helper `WatchmakerCampChests` swaps that chest for a `watchmaker_chest` block unless Lootr is loaded. `WatchmakerChestBlock` (one per version module) extends `AbstractChestBlock` so the vanilla `ChestRenderer` draws it. Its block entity extends the part 1 `CooldownActivatorBlockEntity`, stores 27 slots per player, and is not a `Container`, so hoppers and Lootr never see it. Opening shows a take-only `WatchmakerChestMenu` that uses vanilla `MenuType.GENERIC_9x3`, so no client screen is registered.

**Tech Stack:** Java 17/21/25, Architectury (`dev.architectury.platform.Platform`), Fabric, Forge 1.20.1, NeoForge, JUnit 5, Minecraft GameTest, Python `nbtlib` for the NBT edit.

**Spec:** `docs/superpowers/specs/2026-10-22-wayside-watchmaker-camp-design.md`

## Global Constraints

- Supported modules: 1.20.1, 1.21.1, 1.21.2 (also serves 1.21.3), 1.21.4, 1.21.5, 1.21.6, 1.21.7, 1.21.8, 1.21.9, 1.21.10, 1.21.11, 26.1.2, 26.2, 26.3.
- Block ID `watchmaker_chest`, block entity ID `watchmaker_chest`. No `BlockItem`. Looks and opens like a vanilla single chest. Breaking it drops one `minecraft:chest`.
- The block must not be in `c:chests/wooden`, `forge:chests/wooden` or any `lootr:*` tag. Lootr is detected only with `Platform.isModLoaded("lootr")`, no Lootr classes.
- Per-player slots: 27. Filled from `chronodawn:chests/watchmaker_camp`, rolled with the player as `THIS_ENTITY` and their luck.
- Refill rule: a fill happens only when the player's slots are completely empty **and** `CooldownActivatorBlockEntity#tryActivate` accepts. A non-empty chest never calls `tryActivate`. The first open of a player is a fill.
- Take-only: no slot of the chest accepts an item from a player, by click, drag, number-key swap or shift-click.
- Breaking by a player drops the plain chest and that player's current slots, regardless of game mode. Removal without a player drops the plain chest only. Other players' slots are discarded.
- Hoppers, droppers and other automation cannot insert or extract (the block entity is not a `Container`).
- Fill effects, sent only to the opening player: sound `SoundEvents.LODESTONE_COMPASS_LOCK` (`minecraft:item.lodestone_compass.lock`, volume 1.0, pitch 1.0), chosen by the maintainer after the manual check, and 12 `ParticleTypes.REVERSE_PORTAL` above the chest.
- Waiting message (action bar, only when the slots are empty and the cooldown refused): key `message.chronodawn.watchmaker_chest.waiting`, en "The camp's time has not turned back yet.", ja 「キャンプの時はまだ巻き戻っていない。」. No message in any other case.
- Container title: `block.chronodawn.watchmaker_chest`, en "Watchmaker's Chest", ja 「時計職人のチェスト」.
- Cooldown default 72000 ticks, counted with `Level#getGameTime`. Config key `watchmaker_camp_cooldown_ticks` in `[gameplay.wayside]`, range 20 to 1728000, out-of-range falls back per key.
- Already-generated camps are not converted. Camp placement and rarity do not change.
- Code comments and docs in English. No em dashes or semicolons in docs and commit messages.
- Do not commit without the user's go-ahead. Each "Commit" step means: stage the listed files and ask the controller/user before running `git commit`. Commit messages use Conventional Commits and end with `Co-Authored-By: Claude Opus 5.5 <noreply@anthropic.com>`. Never write issue-closing keywords.
- Never edit lang JSON with `json.load`/`json.dump`. Insert lines textually so blank-line groupings survive.
- Write temporary files under `$TMPDIR`.
- Gradle tasks fork a JVM and need the sandbox disabled. Run one Gradle invocation at a time in this worktree. Redirect long Gradle output to `<worktree>/build/<name>.log` with the absolute worktree path.

## Review Focus

1. A player leaves one item in the chest and comes back much later: nothing is added and the item is untouched. Test `watchmaker_chest_leftover_blocks_refill` (Task 4).
2. A player tries to put their own item in, including shift-click and number-key swap: the item stays in their inventory. Tests `watchmaker_chest_is_take_only` (Task 4).
3. The swap during generation replaces the vanilla chest and no loot items spill onto the ground, in every rotation. Test `watchmaker_camp_swaps_chest_without_lootr` (Task 4).
4. A hopper under the chest: it pulls nothing, even after a fill. Test `watchmaker_chest_is_not_a_container` (Task 4).
5. Server restart between fill and refill: the slots and the cooldown survive. Test `watchmaker_chest_persists_slots_and_cooldown` (Task 4), run on every module in Task 5.

---

## Version eras used in this plan

Block (`WatchmakerChestBlock`). Each module's `SundialGnomonBlock.java` and `TimeWellStoneBlock.java` already use the right interaction method, client check, action bar and loot lookup for that module, so copy those lines from them. Copy `createProperties()` from that module's vanilla `Blocks.CHEST` definition (strength 2.5, wood sound, `ignitedByLava`), adding `setId(...)` with path `"watchmaker_chest"` where the module's other blocks do.

| Era | Modules | Interaction | Client check | Action bar | Registry key class |
|---|---|---|---|---|---|
| A | 1.20.1 | `use(...)` | `level.isClientSide` | `displayClientMessage(c, true)` | `ResourceLocation` |
| B | 1.21.1 to 1.21.8 | `useWithoutItem(...)` | `level.isClientSide` | as A | `ResourceLocation` |
| C | 1.21.9, 1.21.10 | as B | `level.isClientSide()` | as A | `ResourceLocation` |
| D | 1.21.11 | as B | as C | as A | `Identifier` |
| E | 26.1.2, 26.2, 26.3 | as B | as C | `player.sendOverlayMessage(c)` | `Identifier` |

Things that differ by version and must be checked against vanilla with `javap` (memory `feedback_javap_verification_jar_paths`) or by compiling:
- `AbstractChestBlock` constructor and `combine(...)` signature, `codec()` (1.20.5+), and what vanilla `ChestBlock#getRenderShape` returns in that module. Return `DoubleBlockCombiner.Combiner::acceptNone` from `combine`.
- How `ChestRenderer` decides to draw: it needs the block to be an `AbstractChestBlock` and reads `ChestBlock.FACING` (`HorizontalDirectionalBlock.FACING`). Declare that property.
- Sending to one player: `ServerPlayer#playNotifySound` and the `ServerLevel#sendParticles(ServerPlayer, ...)` overload (gained a boolean in 1.21.4).
- `playerWillDestroy` returns `void` on 1.20.1 and `BlockState` later.
- `LootTable#fill(Container, LootParams, long)` and the loot table lookup (`getLootData()` on 1.20.1, `reloadableRegistries()` later).

Block entity (`WatchmakerChestBlockEntity`) follows the `CooldownActivatorBlockEntity` copies, but items need registries:

| Copy | Modules | Item persistence |
|---|---|---|
| `common/1.20.1` | 1.20.1 | override `saveAdditional(CompoundTag)` / `load(CompoundTag)`, `ContainerHelper.saveAllItems(tag, list)` |
| `common/shared` | 1.21.1, 1.21.2, 1.21.4 | override `saveAdditional(CompoundTag, HolderLookup.Provider)` / `loadAdditional(...)`, call `super`, `ContainerHelper.saveAllItems(tag, list, registries)` |
| `common/1.21.5` | 1.21.5 | as shared, with the 1.21.5 `CompoundTag` getters (`getListOrEmpty`, `getStringOr`) |
| `common/<v>` | 1.21.6 to 26.3 | `saveData(ValueOutput)` / `loadData(ValueInput)` with `childrenList` and `ContainerHelper.saveAllItems(ValueOutput, list)` |

Add `'WatchmakerChestBlockEntity.java'` to the shared-source exclude list in `common/1.20.1/build.gradle` and in every module from 1.21.5 to 26.3, next to `'CooldownActivatorBlockEntity.java'`.

Client renderer registration (`ChestRenderer::new` for `ModBlockEntities.WATCHMAKER_CHEST`):
- Fabric: `fabric/<v>/.../fabric/client/ChronoDawnClientFabric.java`, all 14 Fabric modules. Use the module's block entity renderer registry (`BlockEntityRenderers.register` or the Fabric API equivalent available there).
- NeoForge: `onRegisterEntityRenderers(EntityRenderersEvent.RegisterRenderers)` with `event.registerBlockEntityRenderer`. `neoforge/base/.../ChronoDawnClientNeoForge.java` serves 1.21.1 and 1.21.2. The other 11 modules have their own copy.
- Forge 1.20.1: `forge/base/.../forge/client/ChronoDawnClientForge.java`, same event.

Data roots:
- Loot tables: `common/1.20.1/.../data/chronodawn/loot_tables/`, `common/shared-1.21.1+/.../data/chronodawn/loot_table/`, and `common/26.3/.../data/chronodawn/loot_table/` (26.3 schema: `"modifier"` in place of `"functions"`, `"condition"` in place of `"conditions"`). A nested `minecraft:loot_table` entry is not needed here.
- Lang: `en_us.json` and `ja_jp.json` in `common/1.20.1`, `common/1.21.1`, `common/shared-1.21.2+` (`src/main/resources/assets/chronodawn/lang/`).
- Axe tag: `common/1.20.1/.../tags/blocks/mineable/axe.json` and `common/shared-1.21.1+/.../tags/block/mineable/axe.json`.

---

### Task 1: Watchmaker camp cooldown config key

**Files:**
- Modify: `common/shared/src/main/java/com/chronodawn/config/WaysideSettings.java` (fifth record component)
- Modify: `common/shared/src/main/java/com/chronodawn/config/ConfigDefaults.java:80`
- Modify: `common/shared/src/main/java/com/chronodawn/config/ConfigLoader.java:118-124` (constants) and `parseWayside` (~575-625)
- Modify: `common/shared/src/main/resources/chronodawn-default-config.toml` (after `time_well_cooldown_ticks`, ~351)
- Modify: `docs/configuration.md:529-552`
- Test: `common/shared/src/test/java/com/chronodawn/unit/ConfigLoaderTest.java:680-740`

**Interfaces:**
- Produces: `WaysideSettings#watchmakerCampCooldownTicks()` (`int`), default 72000.

- [ ] **Step 1: Write the failing tests**

Extend the existing wayside override test to set `watchmaker_camp_cooldown_ticks = 9000` and expect `new WaysideSettings(6000, 1200, 12000, 6000, 9000)`. Add `wayside_badWatchmakerCampCooldown_fallsBackAlone`, mirroring `wayside_badTimeWellCooldown_fallsBackAlone`: `watchmaker_camp_cooldown_ticks = 1728001` gives `ConfigDefaults.WAYSIDE_DEFAULTS.watchmakerCampCooldownTicks()` while the other four keys keep their configured values.

- [ ] **Step 2: Run to verify it fails**

Run (sandbox disabled): `./gradlew :common-1.21.1:test -Ptarget_mc_version=1.21.1 --tests '*ConfigLoaderTest'`
Expected: compilation FAIL, the five-argument constructor does not exist.

- [ ] **Step 3: Add the key**

`int watchmakerCampCooldownTicks` in `WaysideSettings` and its Javadoc, `72000` in `WAYSIDE_DEFAULTS`, `K_WS_WATCHMAKER_CAMP_COOLDOWN_TICKS = "watchmaker_camp_cooldown_ticks"` and `MAX_WATCHMAKER_CAMP_COOLDOWN_TICKS = 1_728_000`, parsed and range-checked exactly like `time_well_cooldown_ticks`. Fix every other `new WaysideSettings(...)` call the compiler reports. In the default TOML and `docs/configuration.md`, add the key after `time_well_cooldown_ticks` with the description "Watchmaker's Camp: how long (in ticks, 20 ticks = 1 second) before the camp chest can refill for the same player. It refills only once the player has emptied it. Counted in game time, so sleeping does not shorten it. Has no effect when Lootr is installed." Mention camps in the section intro.

- [ ] **Step 4: Run to verify it passes**

Same command. Expected: PASS.

- [ ] **Step 5: Commit**

```bash
git add common/shared/src/main/java/com/chronodawn/config/ \
        common/shared/src/main/resources/chronodawn-default-config.toml \
        common/shared/src/test/java/com/chronodawn/unit/ConfigLoaderTest.java \
        docs/configuration.md
git commit -m "feat: add the watchmaker_camp_cooldown_ticks config key"
```

---

### Task 2: New camp loot table

**Files:**
- Modify: `chests/watchmaker_camp.json` in the three loot roots
- Create: `common/gametest/src/main/java/com/chronodawn/gametest/WatchmakerCampLootTests.java`
- Modify: `common/<v>/src/main/java/com/chronodawn/gametest/RegistryDrivenTestGenerator.java` in all 14 modules (after the `TimeWellTests` line)

**Interfaces:**
- Produces: `WatchmakerCampLootTests.generateTests(MobBehaviorTests.TestFactory<T>)`, and in it `public static final Set<String> STOCKPILE_ITEMS` and `SUPPLY_ITEMS` (item ID strings) that Task 4 reuses.

- [ ] **Step 1: Write the failing GameTest**

Same shape as `TimeWellTests`. `watchmaker_camp_loot_rolls_one_stockpile_item`: roll the table 200 times into a fresh `SimpleContainer(27)` with no player (lookup as in `TimeWellStoneBlock.rollExchange`). For each roll: exactly one non-empty stack's item is in `STOCKPILE_ITEMS`, every other stack's item is in `SUPPLY_ITEMS`, 1 to 2 supply stacks, and counts are within the table ranges below. Across all 200 rolls, `entropy_crystal` and `dark_time_wood_log` each appear at least once.

`STOCKPILE_ITEMS`: `chronodawn:entropy_crystal`, `dark_time_wood_log`, `time_crystal`, `enhanced_clockstone`, `ancient_gear`, `dark_time_wood_sapling`, `ancient_time_wood_sapling`. `SUPPLY_ITEMS`: `minecraft:copper_ingot`, `minecraft:iron_ingot`, `minecraft:clock`, `chronodawn:clockwork_cookie`.

- [ ] **Step 2: Run to verify it fails**

Run (sandbox disabled): `./gradlew :fabric:runGameTest -Ptarget_mc_version=1.21.1 > <worktree>/build/task2-red.log 2>&1`
Expected: FAIL on `watchmaker_camp_loot_rolls_one_stockpile_item` (bread and other old items are not allowed).

- [ ] **Step 3: Rewrite the table in all three roots**

Two pools, type `minecraft:chest`. Starting weights (tuning is allowed, keep the ordering):
- Pool 1, `rolls` 1: `entropy_crystal` 1-2 (weight 6), `dark_time_wood_log` 2-4 (6), `time_crystal` 1-2 (4), `enhanced_clockstone` 1 (1), `ancient_gear` 1 (1), `dark_time_wood_sapling` 1 (1), `ancient_time_wood_sapling` 1 (1).
- Pool 2, `rolls` uniform 1-2: `copper_ingot` 1-3 (3), `iron_ingot` 1-2 (2), `clock` 1 (1), `clockwork_cookie` 2-4 (2).

Use `set_count` only where the count is not 1. Validate with `python3 -I -c "import json,sys; [json.load(open(f)) for f in sys.argv[1:]]" <files>`.

- [ ] **Step 4: Run to verify it passes**

Run (sandbox disabled), one at a time: the Step 2 command, then `./gradlew :forge:runGameTestServer -Ptarget_mc_version=1.20.1 > <worktree>/build/task2-1.20.1.log 2>&1` and `./gradlew :fabric:runGameTest -Ptarget_mc_version=26.3 > <worktree>/build/task2-26.3.log 2>&1`.
Expected: PASS on all three.

- [ ] **Step 5: Commit**

```bash
git commit -m "feat: stock Watchmaker's Camp chests with rare materials"
```

---

### Task 3: Raise the camp tent so the chest opens

**Files:**
- Modify: `common/shared-1.21.1+/src/main/resources/data/chronodawn/structure/watchmaker_camp.nbt`
- Modify: `common/gametest/src/main/java/com/chronodawn/gametest/StructureTests.java:111-115`
- Modify: `common/26.3/src/main/java/com/chronodawn/gametest/StructureTests.java:111-115`

- [ ] **Step 1: Update both structure tests**

`new StructureSpec("watchmaker_camp", 5, 3, 5, ...)`, `TIME_WOOD_FENCE` count 12, and add `new BlockRequirement("white_wool", () -> Blocks.WHITE_WOOL, 3)`. Add a check in the same tests' style that the block directly above the chest is air (if `StructureSpec` cannot express it, add a dedicated test `watchmaker_camp_chest_has_headroom` in `StructureTests` that loads the template and asserts it).

- [ ] **Step 2: Run to verify it fails**

Run (sandbox disabled): `./gradlew :fabric:runGameTest -Ptarget_mc_version=1.21.1 > <worktree>/build/task3-red.log 2>&1`
Expected: FAIL on the `watchmaker_camp` structure test (size 5x2x5).

- [ ] **Step 3: Edit the NBT**

With nbtlib (memory `feedback_nbtlib_missing_venv_workaround` if the import fails): set `size` to `[5, 3, 5]`, move the three `white_wool` blocks from `y=1` to `y=2` (same x, z), and add a `time_wood_fence` block (reuse the existing fence palette entry) at `(0,2,0)`, `(0,2,4)`, `(4,2,0)`, `(4,2,4)`. The template lists air blocks explicitly, so point the vacated `(2,1,1)`, `(2,1,2)`, `(2,1,3)` at the existing `minecraft:air` palette entry, and add air blocks for every other new `y=2` position that has no block. Assert before saving that the chest is still at `(2,0,1)` with its `LootTable` NBT, and after saving that the file is still gzip and decodes.

- [ ] **Step 4: Run to verify it passes**

Run (sandbox disabled), one at a time: the Step 2 command, `./gradlew :fabric:runGameTest -Ptarget_mc_version=1.20.1 > <worktree>/build/task3-1.20.1.log 2>&1` (1.20.1 copy comes from `convertNbt`), `./gradlew :fabric:runGameTest -Ptarget_mc_version=26.3 > <worktree>/build/task3-26.3.log 2>&1`.
Expected: PASS, including the `watchmaker_camp` structure test.

- [ ] **Step 5: Commit**

```bash
git commit -m "fix: raise the Watchmaker's Camp tent so its chest can be opened"
```

---

### Task 4: Watchmaker's Chest on 1.21.1 (end to end)

Builds everything on the modpack target 1.21.1. Other modules do not compile between this task and Task 5, because shared code references `ModBlocks.WATCHMAKER_CHEST`. Build and test only 1.21.1 here.

**Files:**
- Modify: `common/shared/src/main/java/com/chronodawn/registry/ModBlockId.java` and `ModBlockEntityId.java` (after `TIME_WELL_STONE`)
- Create: `common/shared/src/main/java/com/chronodawn/core/wayside/CampChestOutcome.java`
- Create: `common/shared/src/main/java/com/chronodawn/blocks/WatchmakerChestBlockEntity.java`
- Create: `common/shared/src/main/java/com/chronodawn/blocks/WatchmakerChestMenu.java`
- Create: `common/shared/src/main/java/com/chronodawn/worldgen/features/WatchmakerCampChests.java`
- Modify: `common/shared/src/main/java/com/chronodawn/worldgen/features/NbtTemplateFeature.java:85`
- Create: `common/1.21.1/src/main/java/com/chronodawn/blocks/WatchmakerChestBlock.java`
- Modify: `common/1.21.1/src/main/java/com/chronodawn/registry/ModBlocks.java` and `ModBlockEntities.java` (after `TIME_WELL_STONE`)
- Modify: `fabric/1.21.1/.../ChronoDawnClientFabric.java`, `neoforge/base/.../ChronoDawnClientNeoForge.java`
- Create: `common/shared/src/main/resources/assets/chronodawn/blockstates/watchmaker_chest.json`
- Create: block loot table `blocks/watchmaker_chest.json` in the three loot roots
- Modify: both `mineable/axe.json` files and the six lang files
- Create: `common/gametest/src/main/java/com/chronodawn/gametest/WatchmakerChestTests.java`
- Modify: `common/1.21.1/src/main/java/com/chronodawn/gametest/RegistryDrivenTestGenerator.java`

**Interfaces:**
- Consumes: `WaysideSettings#watchmakerCampCooldownTicks()` (Task 1), `WatchmakerCampLootTests.STOCKPILE_ITEMS` / `SUPPLY_ITEMS` (Task 2), `CooldownActivatorBlockEntity#tryActivate(UUID, long, long)` (part 1).
- Produces:
  - `ModBlockId.WATCHMAKER_CHEST` (no `.cutout()`), `ModBlockEntityId.WATCHMAKER_CHEST`, `ModBlocks.WATCHMAKER_CHEST`, `ModBlockEntities.WATCHMAKER_CHEST`
  - `public enum CampChestOutcome { FILLED, OPENED, WAITING }` with `public String messageKey()`: `"message.chronodawn.watchmaker_chest.waiting"` for `WAITING`, `null` otherwise
  - `public class WatchmakerChestBlockEntity extends CooldownActivatorBlockEntity implements LidBlockEntity`:
    - `public static final int SLOTS = 27`
    - `public Container slotsFor(UUID player)`: a live 27-slot view of that player's stored list (created empty if absent). Writes through to the block entity and call `setChanged()`.
    - `public boolean isEmptyFor(UUID player)`
    - `public void startOpen(Player)` / `public void stopOpen(Player)`: count openers, send `level.blockEvent(pos, block, 1, count)`, play `CHEST_OPEN` / `CHEST_CLOSE` at the chest for everyone (the lid is visible to all)
    - `public boolean triggerEvent(int id, int param)`, `public float getOpenNess(float partialTicks)`, `public static void lidAnimateTick(Level, BlockPos, BlockState, WatchmakerChestBlockEntity)` (client ticker), backed by `ChestLidController`
  - `public class WatchmakerChestMenu extends AbstractContainerMenu`: ctor `(int containerId, Inventory playerInventory, Container slots, WatchmakerChestBlockEntity chest)` with `super(MenuType.GENERIC_9x3, containerId)`. Slot order and positions exactly as vanilla `ChestMenu` with 3 rows (27 chest slots, then 27 inventory, then 9 hotbar), so the vanilla client screen matches. Chest slots override `mayPlace` to return false. `quickMoveStack` moves chest to inventory only and returns `ItemStack.EMPTY` for inventory slots. `removed(Player)` calls `chest.stopOpen`. `stillValid` uses `Container.stillValidBlockEntity(chest, player)`.
  - `public final class WatchmakerCampChests`: `public static boolean lootrLoaded()` (`Platform.isModLoaded("lootr")`), and `public static int swapChests(WorldGenLevel level, StructureTemplate template, BlockPos placePos, StructurePlaceSettings settings, boolean lootrLoaded)`: returns 0 and changes nothing when `lootrLoaded`. Otherwise, for each `minecraft:chest` position from `template.filterBlocks(placePos, settings, Blocks.CHEST)`, reads the placed state from the level and sets `ModBlocks.WATCHMAKER_CHEST` with the same `FACING`, flag 2. Returns the number swapped.
  - `public class WatchmakerChestBlock extends AbstractChestBlock<WatchmakerChestBlockEntity>` with `FACING`:
    - `public static BlockBehaviour.Properties createProperties()`
    - `public static CampChestOutcome use(ServerLevel level, BlockPos pos, ServerPlayer player, long gameTime, long cooldownTicks)`: the whole open. Not empty: open the menu, `OPENED`. Every opened menu calls `chest.startOpen(player)`. Empty: `tryActivate`. Accepted: fill the player's slots with `LootTable#fill` (random seed), play the fill effects to that player, open the menu, `FILLED`. Refused: action-bar message, no menu, `WAITING`. The interaction method calls it with `level.getGameTime()` and the config value.
  - `WatchmakerChestTests.generateTests(MobBehaviorTests.TestFactory<T>)`

- [ ] **Step 1: Write the failing GameTests**

`WatchmakerChestTests.java`, same shape as `TimeWellTests`. Each test places `ModBlocks.WATCHMAKER_CHEST` at `CHEST_POS = (1, 2, 1)` and uses `CompatGameTestHelper.makeMockServerPlayer(helper)` (always removed in `finally`). `T0 = 1000`, `CD = 72000`. "Stored items" means the non-empty stacks of `slotsFor(uuid)`. Tests:
- `watchmaker_chest_first_open_fills`: `use(..., T0, CD)` returns `FILLED`. Stored items are non-empty and every item is in `STOCKPILE_ITEMS` or `SUPPLY_ITEMS`. `player.containerMenu` is a `WatchmakerChestMenu`.
- `watchmaker_chest_waits_when_empty`: fill at `T0`, clear the player's slots, `use(..., T0 + CD - 1, CD)` returns `WAITING`, slots stay empty, and the player's menu is not a `WatchmakerChestMenu`.
- `watchmaker_chest_refills_when_empty_after_cooldown`: fill at `T0`, clear, `use(..., T0 + CD, CD)` returns `FILLED` with stored items.
- `watchmaker_chest_leftover_blocks_refill`: fill at `T0`, keep only the first stored stack, `use(..., T0 + 10 * CD, CD)` returns `OPENED`, and the stored items equal exactly that one stack. A following `use` after clearing at `T0 + 10 * CD` returns `FILLED`, proving the leftover did not consume the cooldown.
- `watchmaker_chest_is_per_player`: two players. A fills, B fills, clearing A leaves B's stored items unchanged, and B's `use` at `T0 + 1` returns `OPENED`.
- `watchmaker_chest_is_take_only`: after a fill, put a `minecraft:dirt` stack in the player's inventory. On the open menu: `getSlot(0).mayPlace(dirt)` is false, `quickMoveStack(player, <index of the dirt inventory slot>)` returns `ItemStack.EMPTY` and the dirt stays in the inventory, `clicked(<empty chest slot>, 0, ClickType.PICKUP, player)` with dirt on the cursor leaves the slot empty, and `quickMoveStack(player, 0)` on a filled chest slot moves that stack into the inventory.
- `watchmaker_chest_persists_slots_and_cooldown`: fill at `T0`, `CompatGameTestHelper.copyBlockEntityData` into a fresh block entity at another position, the copy has the same stored items (item and count) and its `tryActivate(uuid, T0 + 1, CD)` is refused.
- `watchmaker_chest_breaking_drops_own_slots`: players A and B both filled. A breaks the chest in survival (`gameMode.destroyBlock`). Item entities within 3 blocks: one `minecraft:chest` plus exactly A's stored items. None of B's.
- `watchmaker_chest_removal_without_player_drops_chest_only`: after a fill, `level.destroyBlock(pos, true)`. Item entities within 3 blocks: exactly one `minecraft:chest`.
- `watchmaker_chest_is_not_a_container`: after a fill, `HopperBlockEntity.getContainerAt(level, pos)` is null.
- `watchmaker_camp_swaps_chest_without_lootr`: for each `Rotation`, place `chronodawn:watchmaker_camp` with `CompatGameTestHelper.loadModStructureTemplate` and settings like `NbtTemplateFeature` (`setRotationPivot` at the size center), then `swapChests(level, template, origin, settings, false)` returns 1, the swapped position holds `WATCHMAKER_CHEST` whose `FACING` equals the vanilla chest's facing before the swap, and no `ItemEntity` exists in the template area. Repeat with `lootrLoaded = true`: returns 0 and the vanilla chest keeps a non-null loot table.

Register in `common/1.21.1/.../RegistryDrivenTestGenerator.java` after the `WatchmakerCampLootTests` line.

- [ ] **Step 2: Run to verify it fails**

Run (sandbox disabled): `./gradlew :common-1.21.1:compileJava -Ptarget_mc_version=1.21.1`
Expected: compilation FAIL, `WatchmakerChestBlock` and `ModBlocks.WATCHMAKER_CHEST` not found.

- [ ] **Step 3: IDs, outcome, block entity, menu**

`WATCHMAKER_CHEST(def("watchmaker_chest"))`, `WATCHMAKER_CHEST("watchmaker_chest")`. The block entity stores `Map<UUID, NonNullList<ItemStack>>` under key `PlayerSlots` (a list of `{Player: <uuid string>, Items: [...]}`) and drops a player's entry once it is empty and saved, so the map does not grow without bound. It calls `super` so the cooldowns keep saving through `CooldownActivatorBlockEntity`. Not a `Container`, not a `RandomizableContainerBlockEntity`.

- [ ] **Step 4: Swap hook in `NbtTemplateFeature`**

Keep the result of `template.placeInWorld(...)`. When it is true and `config.template().getPath().equals("watchmaker_camp")`, call `WatchmakerCampChests.swapChests(level, template, placePos, settings, WatchmakerCampChests.lootrLoaded())`. Comment why the swap happens after placement: Lootr converts chests only after the chunk is live, so the vanilla chest must stay in the NBT for the Lootr case.

- [ ] **Step 5: 1.21.1 block and registration**

`WatchmakerChestBlock` in `common/1.21.1/.../blocks/`. Behavior the tests do not fully pin:
- `getStateForPlacement` is not needed (no item). Default `FACING` north.
- Shape: the vanilla single chest shape (`Block.box(1, 0, 1, 15, 14, 15)`).
- Interaction: on the client return success. On the server call `use` with `ServerPlayer`. Ignore `ChestBlock.isChestBlockedAt` (the tent now has headroom, and a blocked camp chest has no value).
- `getTicker`: client only, `lidAnimateTick`. No server ticker.
- `playerWillDestroy`: on the server, drop `slotsFor(player)` with `Containers.dropContents` before calling `super`.
- Fill effects with the Global Constraints values, sent only to the opener. Particles at chest center, y + 1.0, spread 0.3, speed 0.05.
- Registration Javadoc: "Watchmaker's Chest - per-player, take-only chest of the Watchmaker's Camp. No item: it only appears through worldgen when Lootr is not installed, and breaking it drops a plain chest."
- Client: register `ChestRenderer::new` for `ModBlockEntities.WATCHMAKER_CHEST` in `fabric/1.21.1` and `neoforge/base`.

- [ ] **Step 6: Shared resources**

- `blockstates/watchmaker_chest.json`: one `""` variant with model `minecraft:block/chest` (particle only, like vanilla).
- `blocks/watchmaker_chest.json` in the three roots: one pool, one `minecraft:chest`, `minecraft:survives_explosion` condition (copy the shape of an existing simple block table in each root).
- Axe tags: add `"chronodawn:watchmaker_chest",` in both files.
- Lang, line-based. After `"block.chronodawn.time_well_stone"`: the Global Constraints block name. After the last `"message.chronodawn.time_well.*"` line: the waiting message.

- [ ] **Step 7: Build and run tests for 1.21.1**

Run (sandbox disabled), one at a time:
1. `./gradlew :common-1.21.1:test -Ptarget_mc_version=1.21.1`. Expected: PASS. If `CrossVersionConsistencyTest` fails only because other modules lack `WatchmakerChestBlock`, note it and continue. It must pass at the end of Task 5.
2. `./gradlew :fabric:runGameTest -Ptarget_mc_version=1.21.1 > <worktree>/build/task4-fabric.log 2>&1`. Expected: PASS, including the eleven `watchmaker_*` tests.
3. `./gradlew :neoforge:runGameTestServer -Ptarget_mc_version=1.21.1 > <worktree>/build/task4-neoforge.log 2>&1`. Expected: PASS, same tests.
4. `./gradlew validateResources validateTranslations validateData validateEraJsonFormat validateLangParity`. Expected: PASS.

- [ ] **Step 8: Commit**

Stage every file listed under **Files** above.

```bash
git commit -m "feat: add the per-player Watchmaker's Chest on 1.21.1"
```

---

### Task 5: Port the Watchmaker's Chest to the other 13 modules

**Files:**
- Create: `common/<v>/src/main/java/com/chronodawn/blocks/WatchmakerChestBlock.java` for v in 1.20.1, 1.21.2, 1.21.4, 1.21.5, 1.21.6, 1.21.7, 1.21.8, 1.21.9, 1.21.10, 1.21.11, 26.1.2, 26.2, 26.3
- Create: `common/<v>/src/main/java/com/chronodawn/blocks/WatchmakerChestBlockEntity.java` for v in 1.20.1, 1.21.5, 1.21.6 to 26.3 (block entity era table)
- Modify: `common/<v>/build.gradle` exclude lists (block entity era table)
- Modify: `common/<v>/src/main/java/com/chronodawn/registry/ModBlocks.java`, `ModBlockEntities.java` and `gametest/RegistryDrivenTestGenerator.java` for the same 13
- Modify: `NbtTemplateFeature.java` in `common/1.21.11`, `common/26.1.2`, `common/26.2`, `common/26.3` (same hook as Task 4 Step 4)
- Modify: the 13 remaining Fabric client files, the 11 NeoForge client overrides, `forge/base/.../ChronoDawnClientForge.java`

**Interfaces:**
- Consumes: everything Task 4 produces. Each copy keeps the same public signatures, so the shared tests compile on every module.

- [ ] **Step 1: Copy block and block entity into each module, applying the era tables**

Diff each new block against the same module's `TimeWellStoneBlock.java` for the interaction, client check, action bar and loot lines. Verify the `AbstractChestBlock`, renderer, per-player send and `playerWillDestroy` points from the era notes with `javap` before writing.

- [ ] **Step 2: Register, add the GameTest lines, the feature hook and the renderer in each module**

- [ ] **Step 3: Build and test every module**

Run (sandbox disabled), one at a time, each to `<worktree>/build/<name>.log`:
1. `./gradlew buildAll`. Expected: PASS.
2. `./gradlew testAll`. Expected: PASS, including `CrossVersionConsistencyTest`.
3. `./gradlew :forge:runGameTestServer -Ptarget_mc_version=1.20.1`, `./gradlew :fabric:runGameTest -Ptarget_mc_version=1.21.5`, `./gradlew :fabric:runGameTest -Ptarget_mc_version=1.21.6` and `./gradlew :neoforge:runGameTestServer -Ptarget_mc_version=26.3`. Expected: PASS, including the `watchmaker_*` tests (one module per block entity copy). The full matrix runs in Task 6.

- [ ] **Step 4: Commit**

```bash
git commit -m "feat: port the Watchmaker's Chest to every supported version"
```

---

### Task 6: Docs, changelog and full verification

**Files:**
- Modify: `CHANGELOG.md` (`## [Unreleased]`, new `### Added` and `### Fixed` as needed)
- Modify: `docs/player_guide.md:350` (camp bullet) and `:951` (Raw Temporal Amber no longer from camps)
- Modify: `docs/superpowers/specs/2026-10-14-wayside-features-roadmap.md` (Status row for part 4, foundation notes)

- [ ] **Step 1: Changelog, player guide, roadmap**

Changelog, in the bold-lead style of the Time Well entry: newly generated Watchmaker's Camps have a taller tent and a Watchmaker's Chest. Each player gets their own contents (Entropy Crystal, Dark Time Wood, Time Crystal, rarely Enhanced Clockstone, an Ancient Gear or a Dark or Ancient Time Wood sapling, plus a few supplies). Once a player has emptied it, it refills for them after about three Minecraft days by default (`watchmaker_camp_cooldown_ticks` under `[gameplay.wayside]`). Items cannot be stored in it. With Lootr installed the camp keeps a normal loot chest that Lootr handles. Under `### Fixed`: the old camp chest could not be opened because the tent roof sat on it.

Player guide bullet in guide style (colon after the bold name, no em dash), with the same content, the story line "The watchmaker is long gone, but their clockwork keeps turning the camp's time back", and a sentence for modpack authors: with Lootr the chest follows Lootr's rules, and adding `chronodawn:chests/watchmaker_camp` to Lootr's `refresh_loot_tables` makes it refill. Remove "Watchmaker's Camp chests also contain it" at line 951. Line 915 (Clockwork Cookie) stays true.

Roadmap: part 4 status `Implemented on branch, manual check pending`, and add the post-placement swap, the per-player slots block entity and the take-only menu to "Foundation available".

- [ ] **Step 2: Full verification**

Run (sandbox disabled), one at a time, each redirected to `<worktree>/build/<name>.log`:
1. `./gradlew validateResources validateTranslations validateData validateEraTagParity validateEraJsonFormat validateLangParity`
2. `./gradlew buildAll`
3. `./gradlew testAll`
4. `./gradlew gameTestAll`, then read every `build/gametest-*.log` for `required tests passed` and `required tests failed`.

Expected: every step passes. No Mixin changed, so `prodSmokeTest` is not required.

- [ ] **Step 3: Manual check (user)**

Present to the user. Accepted runtime checks: 1.21.1 NeoForge and Fabric, 1.20.1 Forge, 26.3 NeoForge, 26.2 Fabric.
1. The maintainer compared fill sounds with `/playsound` and chose `minecraft:item.lodestone_compass.lock` (pitch 1.0), so the fill sound constant is `LODESTONE_COMPASS_LOCK`.
2. New world, enter Chrono Dawn, `/place feature chronodawn:watchmaker_camp`.
   Note that `/place feature` only exercises the live-chunk path.
3. On 1.21.1 (NeoForge and Fabric) and 26.3, travel into freshly generated Chrono Dawn forest or plains chunks until a naturally generated camp is found. Alternatively, confirm with a region-file scan that camp positions hold `chronodawn:watchmaker_chest` blocks with a matching block entity and no leftover `minecraft:chest` block entity. Open the chest, then grep the server log for "Tried to access a block entity before it was created" and invalid block entity warnings. Expected: none.
4. The tent roof is one block above the chest, and the chest looks and animates like a vanilla chest.
5. Open it: the fill sound and reverse portal particles appear, the contents include one rare material, and putting an item in (click, shift-click, number key) does nothing.
6. Take everything, close, open again: the action bar shows the waiting message and no screen opens.
7. Leave one item in the chest. `/time` changes day time, not game time, so for the refill check temporarily set `watchmaker_camp_cooldown_ticks = 200` in the config and restart. Wait 10 seconds and open: only the one item is there. Take it, wait 10 seconds, open: it refills.
8. A second account (or `/op` in LAN) sees its own contents.
9. Break the chest with an axe in survival: a plain chest drops along with your remaining items.
10. On 1.21.1 NeoForge with Lootr added, a naturally generated new camp (not `/place`) has a Lootr chest, opens, and shows no Chrono Dawn message or effect.

Expected: all of the above. GameTest servers may omit Chrono Dawn dimensions, so this needs a normal client.

- [ ] **Step 4: Commit**

```bash
git add CHANGELOG.md docs/player_guide.md docs/superpowers/specs/2026-10-14-wayside-features-roadmap.md
git commit -m "docs: describe the refilling Watchmaker's Camp chest"
```
