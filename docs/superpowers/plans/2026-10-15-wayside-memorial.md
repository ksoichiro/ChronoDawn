# Wayside Memorial Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Make newly generated Lost Adventurer Memorials interactive: a player stepping onto the memorial's plate gets a falling light pillar, one of six journal pages and a few supplies, once per player per cooldown, and collecting all six pages grants an advancement.

**Architecture:** Version-independent logic (`StepOnTracker`) lives in `common/shared` and is unit tested. A new `memorial_plate` block subclasses each version's `TemporalStonePressurePlate`, implements `EntityBlock`, and owns a `MemorialPlateBlockEntity` that extends the part 1 `CooldownActivatorBlockEntity`. Six `faded_journal_page_<n>` items share one `JournalPageItem` class. Rewards come from the loot table `chronodawn:gameplay/lost_adventurer_memorial`. Both memorial NBTs swap their pressure plate for the new block.

**Tech Stack:** Java 17/21/25, Architectury, Fabric, Forge 1.20.1, NeoForge, JUnit 5, Minecraft GameTest, Python `nbtlib` for the NBT edit, ImageMagick for the item texture.

**Spec:** `docs/superpowers/specs/2026-10-14-wayside-memorial-design.md`

## Global Constraints

- Supported modules: 1.20.1, 1.21.1, 1.21.2 (also serves 1.21.3), 1.21.4, 1.21.5, 1.21.6, 1.21.7, 1.21.8, 1.21.9, 1.21.10, 1.21.11, 26.1.2, 26.2, 26.3.
- Block ID `memorial_plate`, block entity ID `memorial_plate`. No `BlockItem` for it. Breaking it drops `chronodawn:temporal_stone_pressure_plate`.
- Item IDs `faded_journal_page_1` to `faded_journal_page_6`, four tooltip lines each, gray italic.
- Reward loot table `chronodawn:gameplay/lost_adventurer_memorial`: one page (equal weight), then 1 to 2 rolls of bread 2 to 4, arrow 4 to 8, torch 4 to 8 (equal weight).
- Advancement `chronodawn:exploration/lost_adventurer_journal`, parent `chronodawn:story/us1/dimension_entry`, all six pages required.
- Cooldown default 24000 ticks, counted with `Level#getGameTime`. Config key `memorial_cooldown_ticks` in `[gameplay.wayside]`, range 20 to 1728000, out-of-range falls back per key.
- A player must be off the plate for more than `StepOnTracker.STEP_OFF_TICKS` (10) ticks before stepping on again counts as a new visit.
- Sound `SoundEvents.BEACON_ACTIVATE` (a plain `SoundEvent` in every version, no `.value()`).
- Reward items spawn 1.5 blocks above the plate center with zero velocity.
- Code comments and docs in English. No em dashes or semicolons in docs and commit messages.
- Do not commit without the user's go-ahead. Each "Commit" step means: stage the listed files and ask the controller/user before running `git commit`. Commit messages use Conventional Commits and end with `Co-Authored-By: Claude Opus 5.5 <noreply@anthropic.com>`. Never write issue-closing keywords.
- Never edit lang JSON with `json.load`/`json.dump`. Insert lines textually so blank-line groupings survive.
- Write temporary files under `$TMPDIR`.
- Gradle tasks fork a JVM and need the sandbox disabled. Run one Gradle invocation at a time in this worktree. Redirect long Gradle output to `<worktree>/build/<name>.log` with the absolute worktree path.

## Review Focus

1. A player standing still on the plate for minutes: no second prayer, no cooldown message spam. Test in Task 1 (`standingStillNeverRetriggers`).
2. The same player stepping off and back on during the cooldown: only the cooldown message, never a second reward. Test `memorial_plate_rewards_once_per_visit` (added in the final review fix wave).
3. A datapack that removes or empties the reward loot table: the prayer must not throw, it just drops nothing. Test in Task 5 (`memorial_offering_from_missing_table_is_empty`).
4. Two players on the plate at the same time: each gets their own visit and cooldown. Tests in Task 1 (`playersAreIndependent`) and Task 5 (`memorial_plate_refuses_same_player_within_cooldown` also accepts a second player).
5. A bad `memorial_cooldown_ticks` value must not reset the sundial keys. Test in Task 2 (`wayside_badMemorialCooldown_fallsBackAlone`).

---

## Version eras used in this plan

Block (`MemorialPlateBlock`):

| Era | Modules | `entityInside` | Client check | Action bar | Properties `noCollission` spelling | `setId` resource class | Loot table lookup |
|---|---|---|---|---|---|---|---|
| A | 1.20.1 | `public void entityInside(BlockState, Level, BlockPos, Entity)` | `level.isClientSide` | `displayClientMessage(c, true)` | `noCollission()` | none | `getLootData().getLootTable(ResourceLocation)` |
| B | 1.21.1 | as A | as A | as A | `noCollission()` | none | `reloadableRegistries().getLootTable(ResourceKey)` |
| C | 1.21.2, 1.21.4 | as A | as A | as A | `noCollission()` | `ResourceLocation` | as B |
| D | 1.21.5, 1.21.6, 1.21.7, 1.21.8 | `protected void entityInside(BlockState, Level, BlockPos, Entity, InsideBlockEffectApplier)` | `level.isClientSide` | as A | `noCollission()` | `ResourceLocation` | as B |
| E | 1.21.9 | as D | `level.isClientSide()` | as A | `noCollision()` | `ResourceLocation` | as B |
| F | 1.21.10 | `protected void entityInside(BlockState, Level, BlockPos, Entity, InsideBlockEffectApplier, boolean)` | `level.isClientSide()` | as A | `noCollision()` | `ResourceLocation` | as B |
| G | 1.21.11 | as F | as F | as A | `noCollision()` | `Identifier` | as B |
| H | 26.1.2, 26.2, 26.3 | as F | as F | `player.sendOverlayMessage(c)` | `noCollision()` | `Identifier` | as B |

`InsideBlockEffectApplier` is `net.minecraft.world.entity.InsideBlockEffectApplier`. Each module's existing `TemporalStonePressurePlate.java` is the reference for its properties chain, and each module's `blocks/UnstableFungus.java` or `blocks/ChronoDawnPortalBlock.java` for its `entityInside` signature. When in doubt, match those files.

Item (`JournalPageItem.appendHoverText`):

| Era | Modules | Signature |
|---|---|---|
| A | 1.20.1 | `appendHoverText(ItemStack, @Nullable Level, List<Component>, TooltipFlag)` |
| B | 1.21.1, 1.21.2, 1.21.4 | `appendHoverText(ItemStack, TooltipContext, List<Component>, TooltipFlag)` |
| C | 1.21.5 to 26.3 | `appendHoverText(ItemStack, TooltipContext, TooltipDisplay, Consumer<Component>, TooltipFlag)` |

Each module's `items/consumables/ClockworkCookieItem.java` is the reference.

Registration differences:
- `ModItems`, 1.20.1 and 1.21.1: `new Item.Properties()` with no `setId`.
- `ModItems`, 1.21.2 to 1.21.10: `.setId(ResourceKey.create(Registries.ITEM, ResourceLocation.fromNamespaceAndPath(ChronoDawn.MOD_ID, id)))`.
- `ModItems`, 1.21.11 to 26.3: same with `Identifier.fromNamespaceAndPath`.
- `ModBlocks`: every module uses `new MemorialPlateBlock(MemorialPlateBlock.createProperties())`. The `setId` lives in `createProperties()`.
- `ModBlockEntities`, 1.20.1 and 1.21.1: `BlockEntityType.Builder.of(Factory::new, block).build(null)`. 1.21.2 and later: `new BlockEntityType<>(Factory::new, Set.<Block>of(block))`.

Data roots:
- Loot tables: `common/1.20.1/.../data/chronodawn/loot_tables/`, `common/shared-1.21.1+/.../data/chronodawn/loot_table/`, and `common/26.3/.../data/chronodawn/loot_table/` (26.3 excludes shared loot tables and uses `"condition"` objects and `"modifier"` in place of `"conditions"`/`"functions"`).
- Advancements: `common/1.20.1/.../data/chronodawn/advancements/` (icon `"item"`) and `common/shared-1.21.1+/.../data/chronodawn/advancement/` (icon `"id"`). 26.3 reads the shared-1.21.1+ advancements, so no 26.3 copy.
- Lang: `en_us.json` and `ja_jp.json` in `common/1.20.1`, `common/1.21.1`, `common/shared-1.21.2+` (`src/main/resources/assets/chronodawn/lang/`). `validateLangParity` requires the same keys in both languages.
- Client items (1.21.4+): `common/1.21.4/src/main/resources/assets/chronodawn/items/` and `common/shared-1.21.5+/src/main/resources/assets/chronodawn/items/`, one-line JSON.

---

### Task 1: StepOnTracker

**Files:**
- Create: `common/shared/src/main/java/com/chronodawn/core/wayside/StepOnTracker.java`
- Test: `common/shared/src/test/java/com/chronodawn/unit/StepOnTrackerTest.java`

**Interfaces:**
- Produces: `public final class StepOnTracker` with `public static final long STEP_OFF_TICKS = 10`, `public boolean step(UUID player, long gameTime)`, `public int size()`.

- [ ] **Step 1: Write the failing test**

```java
package com.chronodawn.unit;

import com.chronodawn.core.wayside.StepOnTracker;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class StepOnTrackerTest {

    private static final UUID ALICE = UUID.fromString("00000000-0000-0000-0000-000000000001");
    private static final UUID BOB = UUID.fromString("00000000-0000-0000-0000-000000000002");

    @Test
    void firstStepIsNewVisit() {
        StepOnTracker tracker = new StepOnTracker();
        assertTrue(tracker.step(ALICE, 1000));
    }

    @Test
    void standingStillNeverRetriggers() {
        StepOnTracker tracker = new StepOnTracker();
        tracker.step(ALICE, 1000);
        for (long t = 1001; t <= 1000 + 20 * 60 * 5; t++) {
            assertFalse(tracker.step(ALICE, t), "retriggered at tick " + t);
        }
    }

    @Test
    void shortGapIsStillTheSameVisit() {
        StepOnTracker tracker = new StepOnTracker();
        tracker.step(ALICE, 1000);
        assertFalse(tracker.step(ALICE, 1000 + StepOnTracker.STEP_OFF_TICKS));
    }

    @Test
    void steppingBackOnAfterLeavingIsNewVisit() {
        StepOnTracker tracker = new StepOnTracker();
        tracker.step(ALICE, 1000);
        assertTrue(tracker.step(ALICE, 1000 + StepOnTracker.STEP_OFF_TICKS + 1));
    }

    @Test
    void playersAreIndependent() {
        StepOnTracker tracker = new StepOnTracker();
        assertTrue(tracker.step(ALICE, 1000));
        assertTrue(tracker.step(BOB, 1001));
        assertFalse(tracker.step(ALICE, 1002));
        assertFalse(tracker.step(BOB, 1002));
    }

    @Test
    void staleEntriesArePruned() {
        StepOnTracker tracker = new StepOnTracker();
        tracker.step(ALICE, 1000);
        tracker.step(BOB, 2000);
        assertEquals(1, tracker.size());
    }

    @Test
    void timeRunningBackwardsCountsAsNewVisit() {
        StepOnTracker tracker = new StepOnTracker();
        tracker.step(ALICE, 5000);
        assertTrue(tracker.step(ALICE, 100));
    }
}
```

- [ ] **Step 2: Run test to verify it fails**

Run (sandbox disabled): `./gradlew :common-1.21.1:test -Ptarget_mc_version=1.21.1 --tests 'com.chronodawn.unit.StepOnTrackerTest'`
Expected: compilation FAIL, `StepOnTracker` not found.

- [ ] **Step 3: Implement**

`common/shared/src/main/java/com/chronodawn/core/wayside/StepOnTracker.java` (copy the license header from `PlayerCooldownTracker.java`):

```java
package com.chronodawn.core.wayside;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * Tells a player stepping onto a plate apart from a player standing on it.
 *
 * <p>Plates report every tick while an entity is inside them, so a wayside plate
 * asks this tracker before acting. Kept in memory only: after a chunk reload a
 * player already on the plate counts as a new visit, and the cooldown keeps that
 * from paying out twice.
 */
public final class StepOnTracker {

    /** Ticks a player must be off the plate before stepping on again is a new visit. */
    public static final long STEP_OFF_TICKS = 10;

    private final Map<UUID, Long> lastSeenByPlayer = new HashMap<>();

    /**
     * Records that the player is on the plate at {@code gameTime}.
     *
     * @return true when this starts a new visit
     */
    public boolean step(UUID player, long gameTime) {
        // gameTime only runs backwards when a world is restored or copied, so drop those entries too.
        lastSeenByPlayer.values().removeIf(lastSeen -> gameTime - lastSeen > STEP_OFF_TICKS || gameTime < lastSeen);
        return lastSeenByPlayer.put(player, gameTime) == null;
    }

    public int size() {
        return lastSeenByPlayer.size();
    }
}
```

- [ ] **Step 4: Run test to verify it passes**

Run (sandbox disabled): `./gradlew :common-1.21.1:test -Ptarget_mc_version=1.21.1 --tests 'com.chronodawn.unit.StepOnTrackerTest'`
Expected: PASS, 7 tests.

- [ ] **Step 5: Commit**

```bash
git add common/shared/src/main/java/com/chronodawn/core/wayside/StepOnTracker.java \
        common/shared/src/test/java/com/chronodawn/unit/StepOnTrackerTest.java
git commit -m "feat: track when a player steps onto a wayside plate"
```

---

### Task 2: Memorial cooldown config key

**Files:**
- Modify: `common/shared/src/main/java/com/chronodawn/config/WaysideSettings.java`
- Modify: `common/shared/src/main/java/com/chronodawn/config/ConfigDefaults.java:79-80`
- Modify: `common/shared/src/main/java/com/chronodawn/config/ConfigLoader.java:115-120` (constants) and `:569-599` (`parseWayside`)
- Modify: `common/shared/src/main/resources/chronodawn-default-config.toml:333-342`
- Modify: `docs/configuration.md:529-548`
- Test: `common/shared/src/test/java/com/chronodawn/unit/ConfigLoaderTest.java:672-709`

**Interfaces:**
- Produces: `public record WaysideSettings(int sundialCooldownTicks, int sundialEffectDurationTicks, int memorialCooldownTicks)`, `ConfigDefaults.WAYSIDE_DEFAULTS = new WaysideSettings(24000, 3600, 24000)`.

- [ ] **Step 1: Write the failing tests**

In `ConfigLoaderTest`, replace `wayside_validCustomValues_areReturnedVerbatim` with:

```java
    @Test
    void wayside_validCustomValues_areReturnedVerbatim(@TempDir Path tmp) throws IOException {
        Files.writeString(tmp.resolve("chronodawn.toml"),
            "[gameplay.wayside]\n" +
            "sundial_cooldown_ticks = 6000\n" +
            "sundial_effect_duration_ticks = 1200\n" +
            "memorial_cooldown_ticks = 12000\n");
        ChronoDawnConfig config = ConfigLoader.load(tmp);
        assertEquals(new WaysideSettings(6000, 1200, 12000), config.gameplay().wayside());
    }
```

and add after `wayside_tooLongEffect_fallsBack`:

```java
    @Test
    void wayside_badMemorialCooldown_fallsBackAlone(@TempDir Path tmp) throws IOException {
        Files.writeString(tmp.resolve("chronodawn.toml"),
            "[gameplay.wayside]\n" +
            "sundial_cooldown_ticks = 6000\n" +
            "memorial_cooldown_ticks = 1728001\n");
        WaysideSettings settings = ConfigLoader.load(tmp).gameplay().wayside();
        assertEquals(6000, settings.sundialCooldownTicks());
        assertEquals(ConfigDefaults.WAYSIDE_DEFAULTS.memorialCooldownTicks(), settings.memorialCooldownTicks());
    }
```

- [ ] **Step 2: Run tests to verify they fail**

Run (sandbox disabled): `./gradlew :common-1.21.1:test -Ptarget_mc_version=1.21.1 --tests 'com.chronodawn.unit.ConfigLoaderTest'`
Expected: compilation FAIL, no `WaysideSettings` constructor with three ints.

- [ ] **Step 3: Implement**

`WaysideSettings.java`, record components:

```java
public record WaysideSettings(
    int sundialCooldownTicks,
    int sundialEffectDurationTicks,
    int memorialCooldownTicks
) {}
```

Update its Javadoc to say "such as the Old Sundial and the Lost Adventurer Memorial".

`ConfigDefaults.java:79-80`:

```java
    // One Minecraft day of cooldown and a three-minute buff, from the wayside design specs.
    public static final WaysideSettings WAYSIDE_DEFAULTS = new WaysideSettings(24000, 3600, 24000);
```

`ConfigLoader.java`, after `K_WS_SUNDIAL_EFFECT_DURATION_TICKS`:

```java
    private static final String K_WS_MEMORIAL_COOLDOWN_TICKS = "memorial_cooldown_ticks";
```

and after `MAX_SUNDIAL_EFFECT_DURATION_TICKS`:

```java
    private static final int MAX_MEMORIAL_COOLDOWN_TICKS = 1_728_000;
```

In `parseWayside`, after reading `duration`:

```java
        int memorialCooldown = parsed.<Number>getOptional(path + "." + K_WS_MEMORIAL_COOLDOWN_TICKS)
            .map(Number::intValue)
            .orElse(defaults.memorialCooldownTicks());
```

after the `duration` validation block:

```java
        if (memorialCooldown < MIN_WAYSIDE_TICKS || memorialCooldown > MAX_MEMORIAL_COOLDOWN_TICKS) {
            LOGGER.error(
                "Invalid {}.{} = {} (must be in [{}, {}]); using default {}",
                path, K_WS_MEMORIAL_COOLDOWN_TICKS, memorialCooldown, MIN_WAYSIDE_TICKS,
                MAX_MEMORIAL_COOLDOWN_TICKS, defaults.memorialCooldownTicks()
            );
            memorialCooldown = defaults.memorialCooldownTicks();
        }
```

and return `new WaysideSettings(cooldown, duration, memorialCooldown)`.

`chronodawn-default-config.toml`, after `sundial_effect_duration_ticks = 3600` and its blank line, before `[gameplay.portals]`:

```toml
# Lost Adventurer Memorial: how long (in ticks) each player must wait before
# praying at the same memorial again. Counted in game time, so sleeping does
# not shorten it. Range: 20 to 1728000.
memorial_cooldown_ticks = 24000

```

`docs/configuration.md`, `[gameplay.wayside]` section: change the intro to

```markdown
Controls interactive wayside features. Old Sundials generated after this
update grant Speed in daylight (Speed II around noon) and Night Vision at
night when a player right-clicks the gnomon. Lost Adventurer Memorials
generated after this update drop a journal page and a few supplies when a
player steps onto the plate in front of the grave. Each player has a
separate cooldown for each sundial and each memorial.
```

add `memorial_cooldown_ticks = 24000` to the TOML example, and add this table row:

```markdown
| `memorial_cooldown_ticks` | integer | `24000` | `20..=1728000` | Ticks before the same player can pray at the same memorial again. Counted in game time. |
```

- [ ] **Step 4: Run tests to verify they pass**

Run (sandbox disabled): `./gradlew :common-1.21.1:test -Ptarget_mc_version=1.21.1 --tests 'com.chronodawn.unit.ConfigLoaderTest'`
Expected: PASS, including the 5 `wayside_*` tests.

Also run: `grep -rn "new WaysideSettings(" common --include='*.java' | grep -v /build/`
Expected: only `ConfigDefaults.java`, `ConfigLoader.java` and `ConfigLoaderTest.java`, all with three arguments.

- [ ] **Step 5: Commit**

```bash
git add common/shared/src/main/java/com/chronodawn/config/WaysideSettings.java \
        common/shared/src/main/java/com/chronodawn/config/ConfigDefaults.java \
        common/shared/src/main/java/com/chronodawn/config/ConfigLoader.java \
        common/shared/src/main/resources/chronodawn-default-config.toml \
        common/shared/src/test/java/com/chronodawn/unit/ConfigLoaderTest.java \
        docs/configuration.md
git commit -m "feat: add a config key for the memorial cooldown"
```

---

### Task 3: Faded journal page items on every module

All 14 modules get the items in this task, so every module keeps compiling.

**Files:**
- Modify: `common/shared/src/main/java/com/chronodawn/registry/ModItemId.java` (after `CHRONICLE_BOOK`, line ~314)
- Create: `common/<v>/src/main/java/com/chronodawn/items/JournalPageItem.java` for all 14 modules
- Modify: `common/<v>/src/main/java/com/chronodawn/registry/ModItems.java` for all 14 modules (registrations after `CHRONICLE_BOOK`, creative tab after `output.accept(CHRONICLE_BOOK.get());`)
- Create: `common/shared/src/main/resources/assets/chronodawn/textures/item/faded_journal_page.png`
- Create: `common/shared/src/main/resources/assets/chronodawn/models/item/faded_journal_page_<n>.json` (n = 1..6)
- Create: `common/1.21.4/src/main/resources/assets/chronodawn/items/faded_journal_page_<n>.json` and `common/shared-1.21.5+/src/main/resources/assets/chronodawn/items/faded_journal_page_<n>.json` (n = 1..6)
- Modify: the six lang files (see "Data roots")

**Interfaces:**
- Produces:
  - `ModItemId.FADED_JOURNAL_PAGE_1` .. `FADED_JOURNAL_PAGE_6`
  - `ModItems.FADED_JOURNAL_PAGE_1` .. `FADED_JOURNAL_PAGE_6` (`RegistrySupplier<Item>`) in every module
  - `public class JournalPageItem extends Item` with ctor `(Item.Properties properties, int page)`, `public int page()`, `public static final int LINE_COUNT = 4`

- [ ] **Step 1: Confirm the tests that will fail**

The existing per-version `ResourceValidationTest` (item model and translation per `ModItems` field), `CreativeTabCompletenessTest` and `CrossVersionConsistencyTest` cover this task. They only fail once fields exist without resources, so write the code and resources first, then run them. Record the current pass state: run (sandbox disabled) `./gradlew :common-1.21.1:test -Ptarget_mc_version=1.21.1` and expect PASS before any change.

- [ ] **Step 2: IDs**

`ModItemId.java`, after `CHRONICLE_BOOK("chronicle_book"),`:

```java
    FADED_JOURNAL_PAGE_1("faded_journal_page_1"),
    FADED_JOURNAL_PAGE_2("faded_journal_page_2"),
    FADED_JOURNAL_PAGE_3("faded_journal_page_3"),
    FADED_JOURNAL_PAGE_4("faded_journal_page_4"),
    FADED_JOURNAL_PAGE_5("faded_journal_page_5"),
    FADED_JOURNAL_PAGE_6("faded_journal_page_6"),
```

- [ ] **Step 3: Item class per era**

Era C (1.21.5, 1.21.6, 1.21.7, 1.21.8, 1.21.9, 1.21.10, 1.21.11, 26.1.2, 26.2, 26.3), `common/<v>/src/main/java/com/chronodawn/items/JournalPageItem.java` (license header first, copied from `ClockworkCookieItem.java`):

```java
package com.chronodawn.items;

import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.component.TooltipDisplay;

import java.util.function.Consumer;

/**
 * A page from the Lost Adventurer's journal. Its text is the tooltip.
 */
public class JournalPageItem extends Item {
    public static final int LINE_COUNT = 4;

    private final int page;

    public JournalPageItem(Properties properties, int page) {
        super(properties);
        this.page = page;
    }

    public int page() {
        return page;
    }

    @Override
    public void appendHoverText(ItemStack stack, TooltipContext context, TooltipDisplay tooltipDisplay,
                               Consumer<Component> tooltipAdder, TooltipFlag tooltipFlag) {
        super.appendHoverText(stack, context, tooltipDisplay, tooltipAdder, tooltipFlag);
        for (int line = 1; line <= LINE_COUNT; line++) {
            tooltipAdder.accept(Component.translatable("item.chronodawn.faded_journal_page_" + page + ".line" + line)
                .withStyle(ChatFormatting.GRAY, ChatFormatting.ITALIC));
        }
    }
}
```

Era B (1.21.1, 1.21.2, 1.21.4): same file, with imports `java.util.List` in place of `TooltipDisplay` and `Consumer`, and:

```java
    @Override
    public void appendHoverText(ItemStack stack, TooltipContext context, List<Component> tooltipComponents, TooltipFlag tooltipFlag) {
        super.appendHoverText(stack, context, tooltipComponents, tooltipFlag);
        for (int line = 1; line <= LINE_COUNT; line++) {
            tooltipComponents.add(Component.translatable("item.chronodawn.faded_journal_page_" + page + ".line" + line)
                .withStyle(ChatFormatting.GRAY, ChatFormatting.ITALIC));
        }
    }
```

Era A (1.20.1): era B with `import net.minecraft.world.level.Level;`, `import org.jetbrains.annotations.Nullable;` (match the `@Nullable` import used by 1.20.1 `ClockworkCookieItem.java`, or omit the annotation if that file has none) and the first parameter list `(ItemStack stack, @Nullable Level level, List<Component> tooltipComponents, TooltipFlag tooltipFlag)`, calling `super.appendHoverText(stack, level, tooltipComponents, tooltipFlag)`.

After writing each file, compare its signature with the same module's `ClockworkCookieItem.java`: `grep -n "appendHoverText" common/<v>/src/main/java/com/chronodawn/items/consumables/ClockworkCookieItem.java common/<v>/src/main/java/com/chronodawn/items/JournalPageItem.java`.

- [ ] **Step 4: Registration per module**

In every module's `ModItems.java`, directly after the `CHRONICLE_BOOK` registration, add six fields and one helper. 1.20.1 and 1.21.1:

```java
    /**
     * Faded Journal Pages - the Lost Adventurer's story, one item per page.
     * Dropped by the Lost Adventurer Memorial wayside feature.
     */
    public static final RegistrySupplier<Item> FADED_JOURNAL_PAGE_1 = registerJournalPage(ModItemId.FADED_JOURNAL_PAGE_1, 1);
    public static final RegistrySupplier<Item> FADED_JOURNAL_PAGE_2 = registerJournalPage(ModItemId.FADED_JOURNAL_PAGE_2, 2);
    public static final RegistrySupplier<Item> FADED_JOURNAL_PAGE_3 = registerJournalPage(ModItemId.FADED_JOURNAL_PAGE_3, 3);
    public static final RegistrySupplier<Item> FADED_JOURNAL_PAGE_4 = registerJournalPage(ModItemId.FADED_JOURNAL_PAGE_4, 4);
    public static final RegistrySupplier<Item> FADED_JOURNAL_PAGE_5 = registerJournalPage(ModItemId.FADED_JOURNAL_PAGE_5, 5);
    public static final RegistrySupplier<Item> FADED_JOURNAL_PAGE_6 = registerJournalPage(ModItemId.FADED_JOURNAL_PAGE_6, 6);

    private static RegistrySupplier<Item> registerJournalPage(ModItemId id, int page) {
        return ITEMS.register(id.id(), () -> new JournalPageItem(new Item.Properties().stacksTo(16), page));
    }
```

1.21.2 to 1.21.10: the helper body becomes

```java
        return ITEMS.register(id.id(), () -> new JournalPageItem(new Item.Properties()
                .stacksTo(16)
                .setId(ResourceKey.create(Registries.ITEM,
                    ResourceLocation.fromNamespaceAndPath(ChronoDawn.MOD_ID, id.id()))), page));
```

1.21.11 to 26.3: the same with `Identifier.fromNamespaceAndPath`.

Add `import com.chronodawn.items.JournalPageItem;` unless the file imports `com.chronodawn.items.*`. `ITEMS` must be declared above these fields, which it is in every module (it is the first field). Confirm with `grep -n "DeferredRegister<Item> ITEMS" common/<v>/src/main/java/com/chronodawn/registry/ModItems.java`.

In every module's `populateCreativeTab`, after `output.accept(CHRONICLE_BOOK.get());`:

```java
        output.accept(FADED_JOURNAL_PAGE_1.get());
        output.accept(FADED_JOURNAL_PAGE_2.get());
        output.accept(FADED_JOURNAL_PAGE_3.get());
        output.accept(FADED_JOURNAL_PAGE_4.get());
        output.accept(FADED_JOURNAL_PAGE_5.get());
        output.accept(FADED_JOURNAL_PAGE_6.get());
```

- [ ] **Step 5: Texture, models and client items**

Texture, an aged copy of vanilla paper (reuse with color transformation, as in the `texture-creation` skill):

```bash
mkdir -p "$TMPDIR/journal_tex" && cd "$TMPDIR/journal_tex"
unzip -o -j ~/.gradle/caches/fabric-loom/1.21.1/minecraft-client.jar 'assets/minecraft/textures/item/paper.png'
magick paper.png -modulate 92,60,100 -fill '#8a6a3a' -colorize 30% -depth 8 PNG32:faded_journal_page.png
magick faded_journal_page.png -format '%wx%h %[channels]\n' info:
cp faded_journal_page.png /Users/ksoichiro/.paseo/worktrees/2us4x7u1/bright-sheep/common/shared/src/main/resources/assets/chronodawn/textures/item/faded_journal_page.png
cd /Users/ksoichiro/.paseo/worktrees/2us4x7u1/bright-sheep
```

Expected: `16x16 srgba`. Open the PNG with the Read tool and confirm it looks like a yellowed sheet, still recognizable as paper. If the tint is too strong or too weak, adjust `-colorize` between 20% and 40% and report the value used.

Models, `common/shared/src/main/resources/assets/chronodawn/models/item/faded_journal_page_<n>.json` for n = 1..6:

```json
{
  "parent": "minecraft:item/generated",
  "textures": {
    "layer0": "chronodawn:item/faded_journal_page"
  }
}
```

Client items, one line, in both `common/1.21.4/.../items/` and `common/shared-1.21.5+/.../items/`, file `faded_journal_page_<n>.json` for n = 1..6:

```json
{"model":{"type":"minecraft:model","model":"chronodawn:item/faded_journal_page_<n>"}}
```

(with `<n>` replaced by the page number).

- [ ] **Step 6: Lang**

Use a Python script that reads lines and inserts text after the line containing `"item.chronodawn.time_compass.tooltip.no_target"` in each of the six lang files. Do not use `json.load` for writing.

en_us:

```
  "item.chronodawn.faded_journal_page_1": "Faded Journal Page (1/6)",
  "item.chronodawn.faded_journal_page_1.line1": "The portal went dark behind me.",
  "item.chronodawn.faded_journal_page_1.line2": "The ruin scholars wrote of a",
  "item.chronodawn.faded_journal_page_1.line3": "library where the way home is kept.",
  "item.chronodawn.faded_journal_page_1.line4": "I will find it.",
  "item.chronodawn.faded_journal_page_2": "Faded Journal Page (2/6)",
  "item.chronodawn.faded_journal_page_2.line1": "The sun here keeps no honest hours.",
  "item.chronodawn.faded_journal_page_2.line2": "Some days crawl, some vanish.",
  "item.chronodawn.faded_journal_page_2.line3": "I count them by the old sundials",
  "item.chronodawn.faded_journal_page_2.line4": "and their shadows instead.",
  "item.chronodawn.faded_journal_page_3": "Faded Journal Page (3/6)",
  "item.chronodawn.faded_journal_page_3.line1": "A roadside well, amber at its bottom.",
  "item.chronodawn.faded_journal_page_3.line2": "Its stones are worn smooth by hands.",
  "item.chronodawn.faded_journal_page_3.line3": "Someone made wishes here",
  "item.chronodawn.faded_journal_page_3.line4": "long before I came.",
  "item.chronodawn.faded_journal_page_4": "Faded Journal Page (4/6)",
  "item.chronodawn.faded_journal_page_4.line1": "Gears turn beneath the earth",
  "item.chronodawn.faded_journal_page_4.line2": "with no one left to wind them.",
  "item.chronodawn.faded_journal_page_4.line3": "I went down to look.",
  "item.chronodawn.faded_journal_page_4.line4": "I almost did not come back up.",
  "item.chronodawn.faded_journal_page_5": "Faded Journal Page (5/6)",
  "item.chronodawn.faded_journal_page_5.line1": "The library was real.",
  "item.chronodawn.faded_journal_page_5.line2": "I copied the stabilizer drawings,",
  "item.chronodawn.faded_journal_page_5.line3": "but my hands shake too much to build.",
  "item.chronodawn.faded_journal_page_5.line4": "The great clock calls to me instead.",
  "item.chronodawn.faded_journal_page_6": "Faded Journal Page (6/6)",
  "item.chronodawn.faded_journal_page_6.line1": "If you are reading this,",
  "item.chronodawn.faded_journal_page_6.line2": "I did not come back.",
  "item.chronodawn.faded_journal_page_6.line3": "Do not stop for the ringing you hear.",
  "item.chronodawn.faded_journal_page_6.line4": "Walk on, traveler.",
```

ja_jp:

```
  "item.chronodawn.faded_journal_page_1": "色褪せた日誌のページ (1/6)",
  "item.chronodawn.faded_journal_page_1.line1": "背後でポータルの光が消えた。",
  "item.chronodawn.faded_journal_page_1.line2": "遺跡の学者たちは、帰り道を",
  "item.chronodawn.faded_journal_page_1.line3": "記した書庫があると書いていた。",
  "item.chronodawn.faded_journal_page_1.line4": "必ず見つけてみせる。",
  "item.chronodawn.faded_journal_page_2": "色褪せた日誌のページ (2/6)",
  "item.chronodawn.faded_journal_page_2.line1": "ここの太陽は正直に時を刻まない。",
  "item.chronodawn.faded_journal_page_2.line2": "這うように長い日も、消える日もある。",
  "item.chronodawn.faded_journal_page_2.line3": "だから古い日時計の影で",
  "item.chronodawn.faded_journal_page_2.line4": "日を数えることにした。",
  "item.chronodawn.faded_journal_page_3": "色褪せた日誌のページ (3/6)",
  "item.chronodawn.faded_journal_page_3.line1": "道ばたの井戸の底に琥珀があった。",
  "item.chronodawn.faded_journal_page_3.line2": "縁の石は、人の手ですり減っていた。",
  "item.chronodawn.faded_journal_page_3.line3": "私より前に、誰かが",
  "item.chronodawn.faded_journal_page_3.line4": "ここで願いをかけたのだ。",
  "item.chronodawn.faded_journal_page_4": "色褪せた日誌のページ (4/6)",
  "item.chronodawn.faded_journal_page_4.line1": "地の底で、巻く者もいない歯車が",
  "item.chronodawn.faded_journal_page_4.line2": "回り続けている。",
  "item.chronodawn.faded_journal_page_4.line3": "降りて確かめに行った。",
  "item.chronodawn.faded_journal_page_4.line4": "危うく戻れなくなるところだった。",
  "item.chronodawn.faded_journal_page_5": "色褪せた日誌のページ (5/6)",
  "item.chronodawn.faded_journal_page_5.line1": "書庫は本当にあった。",
  "item.chronodawn.faded_journal_page_5.line2": "安定化装置の図面は写したが、",
  "item.chronodawn.faded_journal_page_5.line3": "手が震えて組み立てられない。",
  "item.chronodawn.faded_journal_page_5.line4": "代わりに、大時計が私を呼んでいる。",
  "item.chronodawn.faded_journal_page_6": "色褪せた日誌のページ (6/6)",
  "item.chronodawn.faded_journal_page_6.line1": "これを読んでいるなら、",
  "item.chronodawn.faded_journal_page_6.line2": "私は戻らなかったのだろう。",
  "item.chronodawn.faded_journal_page_6.line3": "聞こえる鐘の音に足を止めるな。",
  "item.chronodawn.faded_journal_page_6.line4": "歩き続けよ、旅人よ。",
```

Validate each edited file parses: `python3 -I -c "import json,sys; [json.load(open(f)) for f in sys.argv[1:]]" <six files>`.

- [ ] **Step 7: Build and test**

Run (sandbox disabled), one at a time:
1. `./gradlew buildAll > <worktree>/build/task3-build.log 2>&1`
   Expected: BUILD SUCCESSFUL. On a compile error, fix that module against its `ClockworkCookieItem.java` and `ENTROPY_CRYSTAL` registration.
2. `./gradlew testAll > <worktree>/build/task3-test.log 2>&1`
   Expected: PASS, including `ResourceValidationTest` (models and translations for the six items), `CreativeTabCompletenessTest`, `CrossVersionConsistencyTest`, `ModIdEnumTest`.
3. `./gradlew validateResources validateTranslations validateData validateEraTagParity validateLangParity`
   Expected: PASS. `validateData` checks the client items in both directories.

- [ ] **Step 8: Commit**

```bash
git add common/shared/src/main/java/com/chronodawn/registry/ModItemId.java \
        common/*/src/main/java/com/chronodawn/items/JournalPageItem.java \
        common/*/src/main/java/com/chronodawn/registry/ModItems.java \
        common/shared/src/main/resources/assets/chronodawn/textures/item/faded_journal_page.png \
        common/shared/src/main/resources/assets/chronodawn/models/item/faded_journal_page_*.json \
        common/1.21.4/src/main/resources/assets/chronodawn/items/faded_journal_page_*.json \
        common/shared-1.21.5+/src/main/resources/assets/chronodawn/items/faded_journal_page_*.json \
        common/1.20.1/src/main/resources/assets/chronodawn/lang/en_us.json \
        common/1.20.1/src/main/resources/assets/chronodawn/lang/ja_jp.json \
        common/1.21.1/src/main/resources/assets/chronodawn/lang/en_us.json \
        common/1.21.1/src/main/resources/assets/chronodawn/lang/ja_jp.json \
        common/shared-1.21.2+/src/main/resources/assets/chronodawn/lang/en_us.json \
        common/shared-1.21.2+/src/main/resources/assets/chronodawn/lang/ja_jp.json
git commit -m "feat: add the six Faded Journal Pages"
```

Before committing, `git status --short` must show only files from this task.

---

### Task 4: Lost Adventurer's Journal advancement

**Files:**
- Create: `common/1.20.1/src/main/resources/data/chronodawn/advancements/exploration/lost_adventurer_journal.json`
- Create: `common/shared-1.21.1+/src/main/resources/data/chronodawn/advancement/exploration/lost_adventurer_journal.json`
- Modify: `common/<v>/src/main/java/com/chronodawn/gametest/RegistryDrivenTestData.java` for all 14 modules (the `// Exploration` list in `getAdvancementSpecs()`)
- Modify: the six lang files

**Interfaces:**
- Consumes: `ModItems.FADED_JOURNAL_PAGE_1..6` (Task 3).
- Produces: advancement `chronodawn:exploration/lost_adventurer_journal`.

- [ ] **Step 1: Write the failing GameTest entry**

In every module's `RegistryDrivenTestData.java`, after the line
`new AdvancementTests.AdvancementSpec("exploration/first_time_crystal_ore", "story/us1/dimension_entry"),` add:

```java
            new AdvancementTests.AdvancementSpec("exploration/lost_adventurer_journal", "story/us1/dimension_entry"),
```

- [ ] **Step 2: Run to verify it fails**

Run (sandbox disabled): `./gradlew :fabric:runGameTest -Ptarget_mc_version=1.21.1 > <worktree>/build/task4-red.log 2>&1`
Expected: FAIL on `advancement_loaded_exploration/lost_adventurer_journal` (and its grant/parent/isolation tests). Check with `grep -n "lost_adventurer_journal" build/task4-red.log`.

- [ ] **Step 3: Advancement JSON**

`common/shared-1.21.1+/.../advancement/exploration/lost_adventurer_journal.json`:

```json
{
  "parent": "chronodawn:story/us1/dimension_entry",
  "criteria": {
    "has_page_1": {
      "trigger": "minecraft:inventory_changed",
      "conditions": { "items": [ { "items": [ "chronodawn:faded_journal_page_1" ] } ] }
    },
    "has_page_2": {
      "trigger": "minecraft:inventory_changed",
      "conditions": { "items": [ { "items": [ "chronodawn:faded_journal_page_2" ] } ] }
    },
    "has_page_3": {
      "trigger": "minecraft:inventory_changed",
      "conditions": { "items": [ { "items": [ "chronodawn:faded_journal_page_3" ] } ] }
    },
    "has_page_4": {
      "trigger": "minecraft:inventory_changed",
      "conditions": { "items": [ { "items": [ "chronodawn:faded_journal_page_4" ] } ] }
    },
    "has_page_5": {
      "trigger": "minecraft:inventory_changed",
      "conditions": { "items": [ { "items": [ "chronodawn:faded_journal_page_5" ] } ] }
    },
    "has_page_6": {
      "trigger": "minecraft:inventory_changed",
      "conditions": { "items": [ { "items": [ "chronodawn:faded_journal_page_6" ] } ] }
    }
  },
  "display": {
    "icon": {
      "id": "chronodawn:faded_journal_page_6"
    },
    "title": {
      "translate": "advancements.chronodawn.lost_adventurer_journal.title"
    },
    "description": {
      "translate": "advancements.chronodawn.lost_adventurer_journal.description"
    },
    "frame": "goal",
    "show_toast": true,
    "announce_to_chat": true,
    "hidden": false
  }
}
```

No `requirements` key, so all six criteria are required (same as `equipment/tier1_full_set.json`).

The 1.20.1 copy is identical except `"icon": { "item": "chronodawn:faded_journal_page_6" }`.

- [ ] **Step 4: Lang**

Insert after the line containing `"advancements.chronodawn.entropy_crystal_obtained.description"` in the six lang files:

en_us:
```
  "advancements.chronodawn.lost_adventurer_journal.title": "The Lost Adventurer's Journal",
  "advancements.chronodawn.lost_adventurer_journal.description": "Collect all six Faded Journal Pages",
```

ja_jp:
```
  "advancements.chronodawn.lost_adventurer_journal.title": "失われた冒険者の日誌",
  "advancements.chronodawn.lost_adventurer_journal.description": "色褪せた日誌のページを6枚すべて集める",
```

- [ ] **Step 5: Run to verify it passes**

Run (sandbox disabled), one at a time:
1. `./gradlew :fabric:runGameTest -Ptarget_mc_version=1.21.1 > <worktree>/build/task4-green.log 2>&1`
   Expected: PASS, `All N required tests passed`.
2. `./gradlew :fabric:runGameTest -Ptarget_mc_version=1.20.1 > <worktree>/build/task4-green-1.20.1.log 2>&1`
   Expected: PASS (proves the 1.20.1 `"item"` icon form).
3. `./gradlew :common-1.21.1:test -Ptarget_mc_version=1.21.1 --tests 'com.chronodawn.unit.AdvancementValidationTest'` and `./gradlew validateEraJsonFormat validateLangParity`
   Expected: PASS.

- [ ] **Step 6: Commit**

```bash
git add common/1.20.1/src/main/resources/data/chronodawn/advancements/exploration/lost_adventurer_journal.json \
        common/shared-1.21.1+/src/main/resources/data/chronodawn/advancement/exploration/lost_adventurer_journal.json \
        common/*/src/main/java/com/chronodawn/gametest/RegistryDrivenTestData.java \
        common/1.20.1/src/main/resources/assets/chronodawn/lang/*.json \
        common/1.21.1/src/main/resources/assets/chronodawn/lang/*.json \
        common/shared-1.21.2+/src/main/resources/assets/chronodawn/lang/*.json
git commit -m "feat: grant an advancement for collecting every journal page"
```

---

### Task 5: Memorial plate on 1.21.1 (end to end)

This task builds the block on the modpack target 1.21.1 with all shared resources. Other modules do not compile between this task and Task 6, because the shared block entity references `ModBlockEntities.MEMORIAL_PLATE`. Build and test only 1.21.1 in this task.

**Files:**
- Modify: `common/shared/src/main/java/com/chronodawn/registry/ModBlockId.java` (after `SUNDIAL_GNOMON`, line ~253)
- Modify: `common/shared/src/main/java/com/chronodawn/registry/ModBlockEntityId.java` (after `SUNDIAL_GNOMON`, line ~35)
- Create: `common/shared/src/main/java/com/chronodawn/blocks/MemorialPlateBlockEntity.java`
- Create: `common/1.21.1/src/main/java/com/chronodawn/blocks/MemorialPlateBlock.java`
- Modify: `common/1.21.1/src/main/java/com/chronodawn/registry/ModBlocks.java` (after `SUNDIAL_GNOMON`)
- Modify: `common/1.21.1/src/main/java/com/chronodawn/registry/ModBlockEntities.java` (after `SUNDIAL_GNOMON`)
- Create: `common/shared/src/main/resources/assets/chronodawn/blockstates/memorial_plate.json`
- Create: block loot table `memorial_plate.json` in the three loot roots (`blocks/`)
- Create: reward loot table `lost_adventurer_memorial.json` in the three loot roots (`gameplay/`)
- Modify: `common/1.20.1/src/main/resources/data/minecraft/tags/blocks/mineable/pickaxe.json` and `common/shared-1.21.1+/src/main/resources/data/minecraft/tags/block/mineable/pickaxe.json`
- Modify: the six lang files
- Create: `common/gametest/src/main/java/com/chronodawn/gametest/MemorialPlateTests.java`
- Modify: `common/1.21.1/src/main/java/com/chronodawn/gametest/RegistryDrivenTestGenerator.java` (after the `SundialGnomonTests` line, ~447)

**Interfaces:**
- Consumes: `StepOnTracker` (Task 1), `WaysideSettings#memorialCooldownTicks()` (Task 2), `JournalPageItem` and the six items (Task 3), `CooldownActivatorBlockEntity#tryActivate(UUID, long, long)` (part 1), `PlayerCooldownTracker.toMinutesRoundedUp(long)` (part 1).
- Produces:
  - `ModBlockId.MEMORIAL_PLATE`, `ModBlockEntityId.MEMORIAL_PLATE`
  - `ModBlocks.MEMORIAL_PLATE` (`RegistrySupplier<Block>`), `ModBlockEntities.MEMORIAL_PLATE` (`RegistrySupplier<BlockEntityType<MemorialPlateBlockEntity>>`)
  - `public class MemorialPlateBlockEntity extends CooldownActivatorBlockEntity` with ctor `(BlockPos, BlockState)` and `public boolean markStep(UUID player, long gameTime)`
  - `public class MemorialPlateBlock extends TemporalStonePressurePlate implements EntityBlock` with `public static BlockBehaviour.Properties createProperties()`, `public static List<ItemStack> rollOffering(ServerLevel level, BlockPos pos)` and `public static List<ItemStack> rollOffering(ServerLevel level, BlockPos pos, String lootTablePath)`
  - `MemorialPlateTests.generateTests(MobBehaviorTests.TestFactory<T>)`

- [ ] **Step 1: Write the failing GameTest**

`common/gametest/src/main/java/com/chronodawn/gametest/MemorialPlateTests.java`:

```java
package com.chronodawn.gametest;

import com.chronodawn.blocks.MemorialPlateBlock;
import com.chronodawn.blocks.MemorialPlateBlockEntity;
import com.chronodawn.compat.CompatGameTestHelper;
import com.chronodawn.items.JournalPageItem;
import com.chronodawn.registry.ModBlocks;
import net.minecraft.core.BlockPos;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.loot.LootParams;
import net.minecraft.world.level.storage.loot.parameters.LootContextParams;
import net.minecraft.world.phys.Vec3;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public final class MemorialPlateTests {

    private static final BlockPos PLATE_POS = new BlockPos(1, 2, 1);
    private static final long COOLDOWN = 24000;
    // Rolls per check. The supplies pool is random, so one roll could miss a bad entry.
    private static final int ROLLS = 30;

    private MemorialPlateTests() {
        // Utility class
    }

    public static <T> List<T> generateTests(MobBehaviorTests.TestFactory<T> factory) {
        List<T> tests = new ArrayList<>();
        tests.add(factory.create("memorial_plate_refuses_same_player_within_cooldown",
            MemorialPlateTests::testCooldownPerPlayer));
        tests.add(factory.create("memorial_offering_has_one_page_and_supplies",
            MemorialPlateTests::testOffering));
        tests.add(factory.create("memorial_offering_from_missing_table_is_empty",
            MemorialPlateTests::testMissingTable));
        tests.add(factory.create("memorial_plate_drops_plain_pressure_plate",
            MemorialPlateTests::testDrops));
        return tests;
    }

    /**
     * Test: the placed plate carries a block entity that refuses a second prayer by
     * the same player within the cooldown and accepts another player.
     */
    private static void testCooldownPerPlayer(GameTestHelper helper) {
        helper.setBlock(PLATE_POS, ModBlocks.MEMORIAL_PLATE.get());
        if (!(helper.getLevel().getBlockEntity(helper.absolutePos(PLATE_POS)) instanceof MemorialPlateBlockEntity plate)) {
            CompatGameTestHelper.fail(helper, "memorial_plate has no MemorialPlateBlockEntity");
            return;
        }
        UUID first = UUID.randomUUID();
        UUID second = UUID.randomUUID();
        long now = helper.getLevel().getGameTime();
        if (plate.tryActivate(first, now, COOLDOWN) != 0) {
            CompatGameTestHelper.fail(helper, "First prayer was refused");
            return;
        }
        if (plate.tryActivate(first, now + 100, COOLDOWN) <= 0) {
            CompatGameTestHelper.fail(helper, "Second prayer by the same player was accepted within the cooldown");
            return;
        }
        if (plate.tryActivate(second, now + 100, COOLDOWN) != 0) {
            CompatGameTestHelper.fail(helper, "Prayer by a different player was refused");
            return;
        }
        helper.succeed();
    }

    /**
     * Test: every offering holds exactly one journal page and one or two supply stacks.
     */
    private static void testOffering(GameTestHelper helper) {
        ServerLevel level = helper.getLevel();
        BlockPos pos = helper.absolutePos(PLATE_POS);
        for (int roll = 0; roll < ROLLS; roll++) {
            List<ItemStack> offering = MemorialPlateBlock.rollOffering(level, pos);
            int pages = 0;
            int supplies = 0;
            for (ItemStack stack : offering) {
                if (stack.getItem() instanceof JournalPageItem) {
                    pages++;
                } else if (stack.is(Items.BREAD) || stack.is(Items.ARROW) || stack.is(Items.TORCH)) {
                    supplies++;
                } else {
                    CompatGameTestHelper.fail(helper, "Unexpected offering item: " + stack);
                    return;
                }
            }
            if (pages != 1 || supplies < 1 || supplies > 2) {
                CompatGameTestHelper.fail(helper, "Expected 1 page and 1-2 supplies, got " + offering);
                return;
            }
        }
        helper.succeed();
    }

    /**
     * Test: a loot table removed by a datapack yields nothing instead of throwing.
     */
    private static void testMissingTable(GameTestHelper helper) {
        List<ItemStack> offering = MemorialPlateBlock.rollOffering(
            helper.getLevel(), helper.absolutePos(PLATE_POS), "gameplay/does_not_exist");
        if (!offering.isEmpty()) {
            CompatGameTestHelper.fail(helper, "Missing loot table produced " + offering);
            return;
        }
        helper.succeed();
    }

    /**
     * Test: breaking the plate gives the plain pressure plate, so the activator cannot be farmed.
     */
    private static void testDrops(GameTestHelper helper) {
        helper.setBlock(PLATE_POS, ModBlocks.MEMORIAL_PLATE.get());
        BlockPos pos = helper.absolutePos(PLATE_POS);
        BlockState state = helper.getLevel().getBlockState(pos);
        LootParams.Builder builder = new LootParams.Builder(helper.getLevel())
            .withParameter(LootContextParams.ORIGIN, Vec3.atCenterOf(pos))
            .withParameter(LootContextParams.TOOL, new ItemStack(Items.WOODEN_PICKAXE));
        List<ItemStack> drops = state.getDrops(builder);
        if (drops.size() == 1 && drops.get(0).is(ModBlocks.TEMPORAL_STONE_PRESSURE_PLATE.get().asItem())) {
            helper.succeed();
        } else {
            CompatGameTestHelper.fail(helper, "memorial_plate should drop one temporal_stone_pressure_plate, got " + drops);
        }
    }
}
```

In `common/1.21.1/.../gametest/RegistryDrivenTestGenerator.java`, after `all.addAll(SundialGnomonTests.generateTests(NamedTest::new));`:

```java
        all.addAll(MemorialPlateTests.generateTests(NamedTest::new));
```

- [ ] **Step 2: Run to verify it fails**

Run (sandbox disabled): `./gradlew :common-1.21.1:compileJava -Ptarget_mc_version=1.21.1`
Expected: compilation FAIL, `MemorialPlateBlock`, `MemorialPlateBlockEntity` and `ModBlocks.MEMORIAL_PLATE` not found.

- [ ] **Step 3: IDs and shared block entity**

`ModBlockId.java`, after `SUNDIAL_GNOMON(def("sundial_gnomon").cutout()),`:

```java
    MEMORIAL_PLATE(def("memorial_plate")),
```

(no `.cutout()`, matching `TEMPORAL_STONE_PRESSURE_PLATE`).

`ModBlockEntityId.java`, after `SUNDIAL_GNOMON("sundial_gnomon"),`:

```java
    MEMORIAL_PLATE("memorial_plate"),
```

`common/shared/src/main/java/com/chronodawn/blocks/MemorialPlateBlockEntity.java` (license header first):

```java
package com.chronodawn.blocks;

import com.chronodawn.core.wayside.StepOnTracker;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.state.BlockState;

import java.util.UUID;

/**
 * Holds per-player cooldowns for one Lost Adventurer Memorial plate.
 */
public class MemorialPlateBlockEntity extends CooldownActivatorBlockEntity {
    private final StepOnTracker steps = new StepOnTracker();

    public MemorialPlateBlockEntity(BlockPos pos, BlockState state) {
        super(com.chronodawn.registry.ModBlockEntities.MEMORIAL_PLATE.get(), pos, state);
    }

    /**
     * @return true when the player has just stepped onto the plate, false while they keep standing on it
     */
    public boolean markStep(UUID player, long gameTime) {
        return steps.step(player, gameTime);
    }
}
```

- [ ] **Step 4: 1.21.1 block (era B)**

`common/1.21.1/src/main/java/com/chronodawn/blocks/MemorialPlateBlock.java` (license header first):

```java
package com.chronodawn.blocks;

import com.chronodawn.ChronoDawn;
import com.chronodawn.compat.CompatResourceLocation;
import com.chronodawn.config.ChronoDawnConfig;
import com.chronodawn.core.wayside.PlayerCooldownTracker;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.MapColor;
import net.minecraft.world.level.storage.loot.LootParams;
import net.minecraft.world.level.storage.loot.LootTable;
import net.minecraft.world.level.storage.loot.parameters.LootContextParamSets;
import net.minecraft.world.level.storage.loot.parameters.LootContextParams;
import net.minecraft.world.phys.Vec3;

import java.util.List;

/**
 * The plate in front of a Lost Adventurer Memorial. Stepping onto it is a prayer at
 * the grave: light falls on the player and leaves a journal page and a few supplies,
 * once per cooldown for each player.
 */
public class MemorialPlateBlock extends TemporalStonePressurePlate implements EntityBlock {
    private static final String OFFERING_LOOT_TABLE = "gameplay/lost_adventurer_memorial";
    private static final double PILLAR_HEIGHT = 8.0;
    private static final int PILLAR_PARTICLES = 40;
    private static final double PILLAR_SPREAD = 0.4;
    private static final double FALL_SPEED = 0.3;
    // Above the player's head at the plate, so the items fall onto them with the light.
    // The plate cannot tell which way the structure was rotated, so it does not aim at the headstone.
    private static final double OFFERING_DROP_HEIGHT = 1.5;

    public MemorialPlateBlock(Properties properties) {
        super(properties);
    }

    public static BlockBehaviour.Properties createProperties() {
        return BlockBehaviour.Properties.of()
                .mapColor(MapColor.STONE)
                .noCollission()
                .strength(0.5f)
                .sound(SoundType.STONE);
    }

    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new MemorialPlateBlockEntity(pos, state);
    }

    @Override
    public void entityInside(BlockState state, Level level, BlockPos pos, Entity entity) {
        super.entityInside(state, level, pos, entity);
        if (!level.isClientSide && entity instanceof Player player && level instanceof ServerLevel serverLevel
                && serverLevel.getBlockEntity(pos) instanceof MemorialPlateBlockEntity plate
                && plate.markStep(player.getUUID(), serverLevel.getGameTime())) {
            pray(serverLevel, pos, player, plate);
        }
    }

    private static void pray(ServerLevel level, BlockPos pos, Player player, MemorialPlateBlockEntity plate) {
        long cooldown = ChronoDawnConfig.get().gameplay().wayside().memorialCooldownTicks();
        long remaining = plate.tryActivate(player.getUUID(), level.getGameTime(), cooldown);
        if (remaining > 0) {
            player.displayClientMessage(
                Component.translatable("message.chronodawn.memorial_plate.cooldown",
                    PlayerCooldownTracker.toMinutesRoundedUp(remaining)),
                true
            );
            return;
        }
        spawnLightPillar(level, pos);
        level.playSound(null, pos, SoundEvents.BEACON_ACTIVATE, SoundSource.BLOCKS, 1.0f, 1.2f);
        dropOffering(level, pos);
        player.displayClientMessage(Component.translatable("message.chronodawn.memorial_plate.answered"), true);
    }

    public static List<ItemStack> rollOffering(ServerLevel level, BlockPos pos) {
        return rollOffering(level, pos, OFFERING_LOOT_TABLE);
    }

    /**
     * Rolls a chronodawn loot table at the plate. A missing table yields an empty list.
     */
    public static List<ItemStack> rollOffering(ServerLevel level, BlockPos pos, String lootTablePath) {
        LootTable table = level.getServer().reloadableRegistries().getLootTable(
            ResourceKey.create(Registries.LOOT_TABLE, CompatResourceLocation.create(ChronoDawn.MOD_ID, lootTablePath)));
        LootParams params = new LootParams.Builder(level)
            .withParameter(LootContextParams.ORIGIN, Vec3.atCenterOf(pos))
            .create(LootContextParamSets.CHEST);
        return table.getRandomItems(params);
    }

    private static void dropOffering(ServerLevel level, BlockPos pos) {
        for (ItemStack stack : rollOffering(level, pos)) {
            if (stack.isEmpty()) {
                continue;
            }
            ItemEntity item = new ItemEntity(level,
                pos.getX() + 0.5, pos.getY() + OFFERING_DROP_HEIGHT, pos.getZ() + 0.5, stack);
            item.setDeltaMovement(Vec3.ZERO);
            item.setDefaultPickUpDelay();
            level.addFreshEntity(item);
        }
    }

    private static void spawnLightPillar(ServerLevel level, BlockPos pos) {
        double centerX = pos.getX() + 0.5;
        double centerZ = pos.getZ() + 0.5;
        for (int i = 0; i < PILLAR_PARTICLES; i++) {
            double y = pos.getY() + 1.0 + PILLAR_HEIGHT * i / PILLAR_PARTICLES;
            double x = centerX + (level.getRandom().nextDouble() - 0.5) * PILLAR_SPREAD;
            double z = centerZ + (level.getRandom().nextDouble() - 0.5) * PILLAR_SPREAD;
            // With a count of 0 the client reads the offsets as a direction scaled by the speed,
            // so each mote falls toward the plate instead of scattering.
            level.sendParticles(ParticleTypes.END_ROD, x, y, z, 0, 0.0, -1.0, 0.0, FALL_SPEED);
        }
        level.sendParticles(ParticleTypes.GLOW, centerX, pos.getY() + 0.1, centerZ, 16, 0.4, 0.05, 0.4, 0.0);
    }
}
```

Confirm `CompatResourceLocation.create(String, String)` exists in 1.21.1: `grep -n "public static" common/1.21.1/src/main/java/com/chronodawn/compat/CompatResourceLocation.java`. If the factory has another name, use that name.

- [ ] **Step 5: 1.21.1 registration**

`ModBlocks.java`, after the `SUNDIAL_GNOMON` registration:

```java
    /**
     * Memorial Plate - interactive plate of the Lost Adventurer Memorial wayside feature.
     * No item: it only appears through worldgen, and breaking it drops the plain pressure plate.
     */
    public static final RegistrySupplier<Block> MEMORIAL_PLATE = BLOCKS.register(
        ModBlockId.MEMORIAL_PLATE.id(),
        () -> new MemorialPlateBlock(MemorialPlateBlock.createProperties())
    );
```

`ModBlockEntities.java`, after `SUNDIAL_GNOMON`:

```java
    /**
     * Memorial Plate BlockEntity - stores per-player cooldowns.
     */
    public static final RegistrySupplier<BlockEntityType<MemorialPlateBlockEntity>> MEMORIAL_PLATE =
        BLOCK_ENTITIES.register(ModBlockEntityId.MEMORIAL_PLATE.id(), () ->
            BlockEntityType.Builder.of(
                MemorialPlateBlockEntity::new,
                ModBlocks.MEMORIAL_PLATE.get()
            ).build(null)
        );
```

Add imports for `MemorialPlateBlock` and `MemorialPlateBlockEntity` if the files import block classes individually.

- [ ] **Step 6: Shared resources**

`common/shared/src/main/resources/assets/chronodawn/blockstates/memorial_plate.json` (reuses the plain plate's models, so it looks identical):

```json
{
  "variants": {
    "powered=false": {
      "model": "chronodawn:block/temporal_stone_pressure_plate"
    },
    "powered=true": {
      "model": "chronodawn:block/temporal_stone_pressure_plate_down"
    }
  }
}
```

Block loot table `memorial_plate.json` in `common/1.20.1/.../loot_tables/blocks/` and `common/shared-1.21.1+/.../loot_table/blocks/` (identical):

```json
{
  "type": "minecraft:block",
  "pools": [
    {
      "rolls": 1,
      "entries": [
        {
          "type": "minecraft:item",
          "name": "chronodawn:temporal_stone_pressure_plate"
        }
      ],
      "conditions": [
        {
          "condition": "minecraft:survives_explosion"
        }
      ]
    }
  ]
}
```

and in `common/26.3/.../loot_table/blocks/memorial_plate.json`:

```json
{
  "type": "minecraft:block",
  "pools": [
    {
      "rolls": 1,
      "entries": [
        {
          "type": "minecraft:item",
          "name": "chronodawn:temporal_stone_pressure_plate"
        }
      ],
      "condition": {
        "type": "minecraft:survives_explosion"
      }
    }
  ]
}
```

Reward loot table `lost_adventurer_memorial.json` in `common/1.20.1/.../loot_tables/gameplay/` and `common/shared-1.21.1+/.../loot_table/gameplay/` (identical):

```json
{
  "type": "minecraft:chest",
  "pools": [
    {
      "rolls": 1,
      "entries": [
        { "type": "minecraft:item", "name": "chronodawn:faded_journal_page_1" },
        { "type": "minecraft:item", "name": "chronodawn:faded_journal_page_2" },
        { "type": "minecraft:item", "name": "chronodawn:faded_journal_page_3" },
        { "type": "minecraft:item", "name": "chronodawn:faded_journal_page_4" },
        { "type": "minecraft:item", "name": "chronodawn:faded_journal_page_5" },
        { "type": "minecraft:item", "name": "chronodawn:faded_journal_page_6" }
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

and in `common/26.3/.../loot_table/gameplay/lost_adventurer_memorial.json` the same with each `"functions": [ { "function": "minecraft:set_count", "count": ... } ]` replaced by `"modifier": { "type": "minecraft:set_count", "count": ... }`. Compare with `common/26.3/.../loot_table/chests/watchmaker_camp.json` before saving.

In both `mineable/pickaxe.json` files, add `"chronodawn:memorial_plate",` on the line after `"chronodawn:temporal_stone_pressure_plate",`.

Lang, line-based insertion in the six files.

After the line containing `"block.chronodawn.sundial_gnomon"`:
- en_us: `  "block.chronodawn.memorial_plate": "Memorial Plate",`
- ja_jp: `  "block.chronodawn.memorial_plate": "慰霊の感圧板",`

After the line containing `"message.chronodawn.sundial_gnomon.cooldown"`:

en_us:
```
  "message.chronodawn.memorial_plate.answered": "A light answers your prayer.",
  "message.chronodawn.memorial_plate.cooldown": "The grave is silent. Pray again in about %s min.",
```

ja_jp:
```
  "message.chronodawn.memorial_plate.answered": "祈りに応えて、光が降りてきた。",
  "message.chronodawn.memorial_plate.cooldown": "墓は静まり返っている。あと約%s分で再び祈れる。",
```

Validate each edited JSON file parses with `python3 -I -c "import json,sys; [json.load(open(f)) for f in sys.argv[1:]]" <files>`.

- [ ] **Step 7: Build and run tests for 1.21.1**

Run (sandbox disabled), one at a time:
1. `./gradlew :common-1.21.1:test -Ptarget_mc_version=1.21.1`
   Expected: PASS (`ResourceValidationTest` block loot table and blockstate checks, `ModIdEnumTest`, `LootTableValidationTest`). If `CrossVersionConsistencyTest` fails only because other modules lack `MemorialPlateBlock` or `MEMORIAL_PLATE`, note it and continue. Task 6 adds them, and it must pass at the end of Task 6.
2. `./gradlew :fabric:runGameTest -Ptarget_mc_version=1.21.1 > <worktree>/build/task5-fabric.log 2>&1`
   Expected: PASS, including the four `memorial_*` tests.
3. `./gradlew :neoforge:runGameTestServer -Ptarget_mc_version=1.21.1 > <worktree>/build/task5-neoforge.log 2>&1`
   Expected: PASS, same tests.
4. `./gradlew validateResources validateTranslations validateEraJsonFormat validateLangParity`
   Expected: PASS.

- [ ] **Step 8: Commit**

```bash
git add common/shared/src/main/java/com/chronodawn/registry/ModBlockId.java \
        common/shared/src/main/java/com/chronodawn/registry/ModBlockEntityId.java \
        common/shared/src/main/java/com/chronodawn/blocks/MemorialPlateBlockEntity.java \
        common/1.21.1/src/main/java/com/chronodawn/blocks/MemorialPlateBlock.java \
        common/1.21.1/src/main/java/com/chronodawn/registry/ModBlocks.java \
        common/1.21.1/src/main/java/com/chronodawn/registry/ModBlockEntities.java \
        common/1.21.1/src/main/java/com/chronodawn/gametest/RegistryDrivenTestGenerator.java \
        common/gametest/src/main/java/com/chronodawn/gametest/MemorialPlateTests.java \
        common/shared/src/main/resources/assets/chronodawn/blockstates/memorial_plate.json \
        common/1.20.1/src/main/resources/data/chronodawn/loot_tables/blocks/memorial_plate.json \
        common/1.20.1/src/main/resources/data/chronodawn/loot_tables/gameplay/lost_adventurer_memorial.json \
        common/shared-1.21.1+/src/main/resources/data/chronodawn/loot_table/blocks/memorial_plate.json \
        common/shared-1.21.1+/src/main/resources/data/chronodawn/loot_table/gameplay/lost_adventurer_memorial.json \
        common/26.3/src/main/resources/data/chronodawn/loot_table/blocks/memorial_plate.json \
        common/26.3/src/main/resources/data/chronodawn/loot_table/gameplay/lost_adventurer_memorial.json \
        common/1.20.1/src/main/resources/data/minecraft/tags/blocks/mineable/pickaxe.json \
        common/shared-1.21.1+/src/main/resources/data/minecraft/tags/block/mineable/pickaxe.json \
        common/1.20.1/src/main/resources/assets/chronodawn/lang/*.json \
        common/1.21.1/src/main/resources/assets/chronodawn/lang/*.json \
        common/shared-1.21.2+/src/main/resources/assets/chronodawn/lang/*.json
git commit -m "feat: add the Memorial Plate that answers a prayer on 1.21.1"
```

---

### Task 6: Port the memorial plate to the other 13 modules

**Files:**
- Create: `common/<v>/src/main/java/com/chronodawn/blocks/MemorialPlateBlock.java` for v in 1.20.1, 1.21.2, 1.21.4, 1.21.5, 1.21.6, 1.21.7, 1.21.8, 1.21.9, 1.21.10, 1.21.11, 26.1.2, 26.2, 26.3
- Modify: `common/<v>/src/main/java/com/chronodawn/registry/ModBlocks.java` and `ModBlockEntities.java` for the same 13
- Modify: `common/<v>/src/main/java/com/chronodawn/gametest/RegistryDrivenTestGenerator.java` for the same 13 (add `all.addAll(MemorialPlateTests.generateTests(NamedTest::new));` after the `SundialGnomonTests` line)

**Interfaces:**
- Consumes: everything Task 5 produces, with identical names and signatures.
- Produces: the same `MemorialPlateBlock`, `ModBlocks.MEMORIAL_PLATE`, `ModBlockEntities.MEMORIAL_PLATE` in every module.

- [ ] **Step 1: Block classes per era**

Start from the Task 5 file and apply the block era table at the top of this plan. Every module gets a full copy. The differences:

Era A, 1.20.1:
- `rollOffering(ServerLevel, BlockPos, String)` body:

```java
        LootTable table = level.getServer().getLootData().getLootTable(
            CompatResourceLocation.create(ChronoDawn.MOD_ID, lootTablePath));
        LootParams params = new LootParams.Builder(level)
            .withParameter(LootContextParams.ORIGIN, Vec3.atCenterOf(pos))
            .create(LootContextParamSets.CHEST);
        return table.getRandomItems(params);
```
  and drop the now unused `Registries` and `ResourceKey` imports.

Era C, 1.21.2 and 1.21.4: `createProperties()` gains, after `.sound(SoundType.STONE)`:

```java
                .setId(ResourceKey.create(Registries.BLOCK,
                    ResourceLocation.fromNamespaceAndPath(ChronoDawn.MOD_ID, "memorial_plate")));
```
  with `import net.minecraft.resources.ResourceLocation;`.

Era D, 1.21.5 to 1.21.8: era C plus this `entityInside` and `import net.minecraft.world.entity.InsideBlockEffectApplier;`:

```java
    @Override
    protected void entityInside(BlockState state, Level level, BlockPos pos, Entity entity, InsideBlockEffectApplier effectApplier) {
        super.entityInside(state, level, pos, entity, effectApplier);
        if (!level.isClientSide && entity instanceof Player player && level instanceof ServerLevel serverLevel
                && serverLevel.getBlockEntity(pos) instanceof MemorialPlateBlockEntity plate
                && plate.markStep(player.getUUID(), serverLevel.getGameTime())) {
            pray(serverLevel, pos, player, plate);
        }
    }
```

Era E, 1.21.9: era D with `level.isClientSide()` and `.noCollision()`.

Era F, 1.21.10: era E with this signature and super call:

```java
    protected void entityInside(BlockState state, Level level, BlockPos pos, Entity entity,
                                InsideBlockEffectApplier effectApplier, boolean colliding) {
        super.entityInside(state, level, pos, entity, effectApplier, colliding);
```

Era G, 1.21.11: era F with `Identifier.fromNamespaceAndPath` and `import net.minecraft.resources.Identifier;` in place of `ResourceLocation`.

Era H, 26.1.2, 26.2, 26.3: era G with both `player.displayClientMessage(X, true)` calls replaced by `player.sendOverlayMessage(X)`.

After writing each file, check it against the module's references:
`grep -n "entityInside\|isClientSide\|noCollis\|fromNamespaceAndPath" common/<v>/src/main/java/com/chronodawn/blocks/MemorialPlateBlock.java common/<v>/src/main/java/com/chronodawn/blocks/TemporalStonePressurePlate.java common/<v>/src/main/java/com/chronodawn/blocks/UnstableFungus.java`
The plate's properties chain must match `TemporalStonePressurePlate.createProperties()` except for the id, and `entityInside` must match `UnstableFungus`.

- [ ] **Step 2: Registration per module**

`ModBlocks`: the Task 5 snippet unchanged in every module (the `setId` is inside `createProperties()`).

`ModBlockEntities`, 1.20.1: as 1.21.1. 1.21.2 and later:

```java
    public static final RegistrySupplier<BlockEntityType<MemorialPlateBlockEntity>> MEMORIAL_PLATE =
        BLOCK_ENTITIES.register(ModBlockEntityId.MEMORIAL_PLATE.id(), () ->
            new BlockEntityType<>(
                MemorialPlateBlockEntity::new,
                Set.<Block>of(ModBlocks.MEMORIAL_PLATE.get())
            )
        );
```

Match how `SUNDIAL_GNOMON` is written in the same file.

Add the `MemorialPlateTests` line to each module's `RegistryDrivenTestGenerator.java`.

- [ ] **Step 3: Build all modules**

Run (sandbox disabled): `./gradlew buildAll > <worktree>/build/task6-build.log 2>&1`
Expected: BUILD SUCCESSFUL for all 14 modules. On a compile error, fix the offending module using its `TemporalStonePressurePlate.java`, `UnstableFungus.java` and `SundialGnomonBlock.java` as references.

- [ ] **Step 4: Unit tests and GameTests**

Run (sandbox disabled), one at a time:
1. `./gradlew testAll > <worktree>/build/task6-test.log 2>&1`
   Expected: PASS for all modules, including `CrossVersionConsistencyTest`.
2. `./gradlew gameTestAll > <worktree>/build/task6-gametest.log 2>&1`, then for each `build/gametest-*.log` check `grep -c "required tests passed"` and `grep -c "required tests failed"`.
   Expected: no failures. Fabric and NeoForge run in every version from 1.21.1, only Fabric in 1.20.1 (Forge 1.20.1 has no GameTest integration).

- [ ] **Step 5: Commit**

```bash
git add common/*/src/main/java/com/chronodawn/blocks/MemorialPlateBlock.java \
        common/*/src/main/java/com/chronodawn/registry/ModBlocks.java \
        common/*/src/main/java/com/chronodawn/registry/ModBlockEntities.java \
        common/*/src/main/java/com/chronodawn/gametest/RegistryDrivenTestGenerator.java
git commit -m "feat: port the Memorial Plate to every supported Minecraft version"
```

Before committing, `git status --short` must show only files from this task.

---

### Task 7: Put the plate into both memorial structures

**Files:**
- Modify: `common/shared-1.21.1+/src/main/resources/data/chronodawn/structure/lost_adventurer_memorial.nbt`
- Modify: `common/shared-1.21.1+/src/main/resources/data/chronodawn/structure/lost_adventurer_memorial_snowy.nbt`
- Modify: `common/gametest/src/main/java/com/chronodawn/gametest/StructureTests.java:95-105`
- Modify: `common/26.3/src/main/java/com/chronodawn/gametest/StructureTests.java` (same spec lines)

**Interfaces:**
- Consumes: `ModBlockId.MEMORIAL_PLATE`, `ModBlocks.MEMORIAL_PLATE` (Task 5).

- [ ] **Step 1: Update the structure tests first**

In both `StructureTests.java` files, in the `lost_adventurer_memorial` and `lost_adventurer_memorial_snowy` specs, replace

```java
                new BlockRequirement(ModBlockId.TEMPORAL_STONE_PRESSURE_PLATE.id(),
                    ModBlocks.TEMPORAL_STONE_PRESSURE_PLATE, 1)
```

with

```java
                new BlockRequirement(ModBlockId.MEMORIAL_PLATE.id(),
                    ModBlocks.MEMORIAL_PLATE, 1)
```

- [ ] **Step 2: Run to verify it fails**

Run (sandbox disabled): `./gradlew :fabric:runGameTest -Ptarget_mc_version=1.21.1 > <worktree>/build/task7-red.log 2>&1`
Expected: FAIL on the two memorial structure tests, expecting 1 `memorial_plate` and finding 0.

- [ ] **Step 3: Edit the NBT palettes**

Confirm the editor is available: `python3 -c "import nbtlib"`. If it fails, use a local venv as described in memory `feedback_nbtlib_missing_venv_workaround`.

```bash
python3 - <<'EOF'
import nbtlib
base = 'common/shared-1.21.1+/src/main/resources/data/chronodawn/structure/'
for name in ('lost_adventurer_memorial.nbt', 'lost_adventurer_memorial_snowy.nbt'):
    path = base + name
    f = nbtlib.load(path)
    hits = [p for p in f['palette'] if p['Name'] == 'chronodawn:temporal_stone_pressure_plate']
    assert len(hits) == 1, f'{name}: expected one pressure plate palette entry, found {len(hits)}'
    # Keep Properties: memorial_plate has the same "powered" property as the plain plate.
    hits[0]['Name'] = nbtlib.String('chronodawn:memorial_plate')
    f.save()
    names = [str(p['Name']) for p in nbtlib.load(path)['palette']]
    assert 'chronodawn:temporal_stone_pressure_plate' not in names, f'{name}: plain plate still present'
    print(name, names)
EOF
```

Expected: both files list `chronodawn:memorial_plate` and no `chronodawn:temporal_stone_pressure_plate`. The script's last assertion replaces the spec's "no plain pressure plate" GameTest check, because `BlockRequirement` only checks minimum counts. Verify gzip is kept: `python3 -c "import gzip;gzip.open('<path>').read(10)"` for both files.

- [ ] **Step 4: Run to verify it passes**

Run (sandbox disabled), one at a time:
1. `./gradlew :fabric:runGameTest -Ptarget_mc_version=1.21.1 > <worktree>/build/task7-green.log 2>&1`
   Expected: PASS, including both memorial structure tests.
2. `./gradlew :fabric:runGameTest -Ptarget_mc_version=1.20.1 > <worktree>/build/task7-green-1.20.1.log 2>&1`
   Expected: PASS. The 1.20.1 copies come from `convertNbt`, which does not rename palette entries, so this proves the converted files carry the new block.
3. `./gradlew :fabric:runGameTest -Ptarget_mc_version=26.3 > <worktree>/build/task7-green-26.3.log 2>&1`
   Expected: PASS (the 26.3 `StructureTests` copy).

- [ ] **Step 5: Commit**

```bash
git add common/shared-1.21.1+/src/main/resources/data/chronodawn/structure/lost_adventurer_memorial.nbt \
        common/shared-1.21.1+/src/main/resources/data/chronodawn/structure/lost_adventurer_memorial_snowy.nbt \
        common/gametest/src/main/java/com/chronodawn/gametest/StructureTests.java \
        common/26.3/src/main/java/com/chronodawn/gametest/StructureTests.java
git commit -m "feat: generate Lost Adventurer Memorials with a Memorial Plate"
```

---

### Task 8: Docs, changelog and full verification

**Files:**
- Modify: `CHANGELOG.md` (`## [Unreleased]` / `### Added`, after the Old Sundial entry)
- Modify: `docs/player_guide.md:333-345` (Small Ambient Structures)
- Modify: `README.md:46`, `docs/curseforge_description.md:89`, `docs/modrinth_description.md:89`
- Modify: `docs/superpowers/specs/2026-10-14-wayside-features-roadmap.md` (Status table row for part 2)

- [ ] **Step 1: Changelog**

Add under `## [Unreleased]` → `### Added`, after the Old Sundial entry, in the same bold-lead style:

```markdown
- **Lost Adventurer Memorial prayers**: step onto the plate in front of a
  Lost Adventurer Memorial and light falls from above, leaving one of six
  Faded Journal Pages and a little bread, arrows or torches. Each player can
  pray at each memorial about once every 20 minutes of game time by default
  (`memorial_cooldown_ticks` under `[gameplay.wayside]`). The pages tell the
  story of the adventurer buried there, and collecting all six grants an
  advancement. Breaking the plate drops a plain Temporal Stone Pressure
  Plate. Only memorials in newly generated chunks have the interactive plate.
```

- [ ] **Step 2: Player guide and store pages**

The memorial is currently documented as "Petrified Adventurer", which does not match its NBT (a grave with a headstone). Rename it to "Lost Adventurer Memorial" in all four places.

`docs/player_guide.md`:
- Line 335: replace "one of them carries usable loot:" with "a few of them reward a visit:".
- Line 340: replace the Petrified Adventurer bullet with:

```markdown
- **Lost Adventurer Memorial**: a small grave with a headstone, with a snowy variant in the Snowy biome. In newly generated chunks, stepping onto the plate in front of the grave answers a prayer: light falls from above and leaves one of six Faded Journal Pages plus a little bread, arrows or torches. Each player can pray at each memorial about once every 20 minutes of game time by default. Read the pages in order for the adventurer's story, and collect all six for an advancement.
```

The neighboring bullets use an em dash after the bold name. The new bullet uses a colon instead, because docs must not contain em dashes. Leave the other bullets as they are.

`README.md:46`, `docs/curseforge_description.md:89`, `docs/modrinth_description.md:89`: replace `Petrified Adventurer` with `Lost Adventurer Memorial` and keep the rest of the line. Use the `doc-maintenance` skill checklist for any other file it says must stay consistent.

Roadmap status table: change part 2's status to `Implemented on branch, manual check pending` (the controller updates it to "Shipped" with the merge commit).

- [ ] **Step 3: Full verification**

`checkAll` exceeds the 10-minute foreground limit, so run (sandbox disabled), one at a time, each redirected to `<worktree>/build/<name>.log`:
1. `./gradlew validateResources validateTranslations validateData validateEraTagParity validateEraJsonFormat validateLangParity`
2. `./gradlew buildAll`
3. `./gradlew testAll`
4. `./gradlew gameTestAll`, then read every `build/gametest-*.log` for `required tests passed` and `required tests failed`.

Expected: every step passes. No Mixin changed, so `prodSmokeTest` is not required.

- [ ] **Step 4: Manual check (user)**

Present to the user. The roadmap accepts these runtime checks: 1.21.1 NeoForge and Fabric, 1.20.1 Forge, 26.3 NeoForge, 26.2 Fabric.
1. `./gradlew runClientNeoForge1_21_1` (and the others above)
2. Create a new world, enter Chrono Dawn, and find a newly generated memorial (or `/place feature chronodawn:lost_adventurer_memorial`).
3. Step onto the plate: light falls from about 8 blocks up onto the plate, the beacon sound is clearly audible, one page and one or two supply stacks fall onto the player and are picked up, the action bar shows the answered message.
4. Stand still on the plate for 10 seconds: nothing more happens.
5. Step off and back on: the cooldown message with remaining minutes, no items.
6. Hover a page: four gray italic lines.
7. `/give` yourself pages 1 to 6: the advancement toast appears.
8. Break the plate in survival: a Temporal Stone Pressure Plate drops.
Expected: all of the above. GameTest servers may omit Chrono Dawn dimensions, so this has to be done in a normal client.

- [ ] **Step 5: Commit**

```bash
git add CHANGELOG.md docs/player_guide.md README.md docs/curseforge_description.md docs/modrinth_description.md \
        docs/superpowers/specs/2026-10-14-wayside-features-roadmap.md
git commit -m "docs: describe Lost Adventurer Memorial prayers"
```
