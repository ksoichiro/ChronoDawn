# ChronoDawn Custom Shields — Design

- **Date**: 2026-04-19
- **Status**: Approved for planning
- **Scope**: Add 3 tiered custom shields to ChronoDawn as new items (independent of existing `Chrono Aegis`)

## 1. Goals and Motivation

ChronoDawn currently lacks a true custom shield. Players can craft vanilla shields in the dimension since iron is obtainable, but vanilla shields feel thematically flat in a time-themed world. This design adds a tiered line of shields that:

- Match the ChronoDawn aesthetic (clockwork / temporal materials)
- Provide practical advantages inside the ChronoDawn dimension where time-based debuffs are common
- Offer progression parallel to existing weapon/tool tiers (Clockstone → Enhanced Clockstone → Entropy Crystal)
- Do **not** modify or replace the existing `Chrono Aegis` artifact

### Non-Goals

- Do not rework `Chrono Aegis` (currently a right-click buff artifact)
- Do not remove or change vanilla shield availability
- Do not introduce a new smithing template system
- Do not cover banner-style patterned rendering

## 2. Item Lineup

Three new items are registered in `ModItemId`:

| Tier | Item ID | Primary Material | Wood |
|------|---------|------------------|------|
| T1 | `clockstone_shield` | Clockstone | Time Wood Planks |
| T2 | `enhanced_clockstone_shield` | Enhanced Clockstone | Dark Time Wood Planks |
| T3 | `entropy_crystal_shield` | Entropy Crystal | Ancient Time Wood Planks |

All three are new items; no existing item is modified.

## 3. Effect Design (Cumulative Tiers)

All shields share a base effect A. Each higher tier adds one time-manipulation effect on top of the previous tier.

### 3.1 Effect A — Temporal Debuff Resistance (all tiers)

While the player is raising/blocking with a ChronoDawn shield, any newly-applied Slowness, Weakness, or Mining Fatigue effect has its duration reduced by **50%**.

- Trigger: during block (`LivingEntity.isBlocking` returning true and using one of our shields)
- Scope: applies to MobEffect application path
- Scaling: uniform 50% across all tiers (no per-tier scaling to keep behavior predictable)

### 3.2 Effect #1 — Faster Raise (T1 onward)

Shield activation latency is reduced from the vanilla **5 ticks to 3 ticks**.

- Trigger: `LivingEntity.isBlocking` evaluation when use-item is a ChronoDawn shield
- Applies to all three tiers

### 3.3 Effect #7 — Speed on Block (T2 onward)

On a successful shield block, the user is granted **Speed I for 1 second (20 ticks)**.

- Internal cooldown: **3 seconds** (60 ticks) to prevent spam
- Trigger point: the block-success detection (shield absorbs incoming damage)
- Tier applicability: T2 and T3

### 3.4 Effect #12 — Time Echo (T3 only)

On a successful shield block, a "ghost shield" persists on the player for **5 seconds**. During that window, the next incoming hit is auto-blocked even if the player is not raising the shield.

- Consumed by: next incoming damage event within 5s, OR expiry
- Damage handling: reduced to zero, durability cost 1 (same as a normal block)
- Blockable damage scope: **follows vanilla shield rules** (melee and projectile damage types that a raised vanilla shield can block); non-blockable sources (fall, suffocation, magic-bypassing) do not consume echo
- Cooldown after consumption or expiry: **30 seconds** (600 ticks)
- Only T3 has this effect

### 3.5 Base Stats

| Tier | Durability | Axe-disable window | Max damage per block |
|------|-----------|--------------------|-----|
| T1 | 400 | 5 s (vanilla) | vanilla |
| T2 | 600 | 3 s | vanilla |
| T3 | 800 | 2 s | vanilla |

## 4. Crafting — Tiered Upgrade in Crafting Table (Approach 2 + Wood Option b)

T1 is crafted from raw materials. T2 and T3 are upgrades that consume the previous tier shield in the center of a 3×3 pattern.

### T1 — Clockstone Shield
Standard vanilla shield layout with ChronoDawn materials:

```
W C W
W W W
. W .
```
- W = Time Wood Planks ×5
- C = Clockstone ×1

### T2 — Enhanced Clockstone Shield
Consumes a T1 shield at the center:

```
W E W
E T1 E
W E W
```
- W = Dark Time Wood Planks ×4
- E = Enhanced Clockstone ×4
- T1 = Clockstone Shield ×1 (center, consumed)

### T3 — Entropy Crystal Shield
Consumes a T2 shield at the center:

```
W X W
X T2 X
W X W
```
- W = Ancient Time Wood Planks ×4
- X = Entropy Crystal ×4
- T2 = Enhanced Clockstone Shield ×1 (center, consumed)

All three recipes live in `data/chronodawn/recipe/*.json` and follow the existing project recipe format conventions per MC version.

## 5. Visual Design

Shields use fixed per-tier art (no banner rendering). Each tier has a distinct thermal/cosmic color story:

| Tier | Primary metal color | Wood frame | Motif | Emissive |
|------|--------------------|------------|-------|----------|
| T1 | **Yellow-gold** (Clockstone) | Time Wood (light) | Single clock gear / clock hand | No |
| T2 | **Orange** (Enhanced Clockstone) | Dark Time Wood | Layered twin gears | No |
| T3 | **Blue-violet** (Entropy Crystal) | Ancient Time Wood | Fractured gear exposing crystal shards | Yes (crystal portion) |

Progression reads as "warm dawn → warm dusk → cold entropic break," creating a deliberate thematic inflection at T3 rather than a pure power climb.

Assets required per tier:
- Item icon (16×16)
- In-hand 3D model (idle + blocking variants)
- Texture atlas referenced by the 3D model
- Optional durability-damage stage textures

T3 uses the existing `RenderType.eyes()` + transparent overlay texture pattern already proven in the codebase for emissive mob parts.

## 6. Particle and Sound for Time Echo

Three-stage visualization mirrors the echo lifecycle.

### Stage 1 — Echo Generation (block success)
- Location: shield-front position of blocker
- Particle: blue-violet ring-burst (6–8 particles in a circle)
- Duration: 10 ticks
- SFX: soft clockwork click (new sound in `ModSounds`)

### Stage 2 — Echo Active (5 seconds)
- Location: player off-hand
- Particle: slow-drifting blue-violet afterimage particles, spawned every ~5 ticks
- Duration: matches echo validity
- Visual intent: ghost shield attached to player

### Stage 3 — Echo Consumed (auto-block)
- Location: point of incoming hit
- Particle: blue-violet ripple + radial "time warp" burst
- Duration: 15 ticks
- SFX: brief rewind-style echo

**Implementation**: add a custom particle `ChronoShieldEchoParticle` under `common/shared/src/main/java/com/chronodawn/client/particle/`, registered via `ModParticles`. Client visualization is driven by server-issued particle packets (one per stage transition) to avoid reliance on client-side state reconstruction. Rationale: the three stages are event-shaped rather than continuous, so a packet per event is both simpler and more reliable across the loader/version matrix than synchronizing arbitrary fields of `PlayerProgressData` for Stage 2's drifting particles — Stage 2 can be triggered by an "echo-active pulse" packet loop from the server.

## 7. Implementation Architecture

### 7.1 Class Layout

Shared (version-agnostic):
```
common/shared/src/main/java/com/chronodawn/items/shield/
  ChronoShieldTier.java             (enum: T1/T2/T3 with durability, effect flags, axe-disable window)
  ChronoShieldEffectHandler.java    (static helpers for A / #1 / #7 / #12)
```

Version-specific (one copy per supported MC version, mirroring the existing armor/weapon pattern):
```
common/{version}/src/main/java/com/chronodawn/items/
  ClockstoneShieldItem.java           (extends ShieldItem, tier=T1)
  EnhancedClockstoneShieldItem.java   (tier=T2)
  EntropyCrystalShieldItem.java       (tier=T3)
```

Per-version duplication is required because `ShieldItem` / `Item.Properties` constructor contracts differ (notably the `setId()` requirement from 1.21.2+).

### 7.2 Event Hooks

| Effect | Hook mechanism | Notes |
|--------|---------------|-------|
| A | Mixin on `LivingEntity.addEffect` | If target holds a ChronoDawn shield and is blocking, halve incoming `MobEffectInstance` duration for Slowness/Weakness/Mining Fatigue |
| #1 | Mixin on the 5-tick threshold in the `isBlocking`/useDuration path | Replace literal 5 with 3 when the active use-item is a ChronoDawn shield |
| #7 | Mixin on the shield-block branch of `Player.hurt` / `LivingEntity.blockUsingShield` | On success, call effect handler; handler checks and updates CD in `PlayerProgressData` |
| #12 | Mixin on `Player.hurt` prologue | If echo is active, auto-block and consume; otherwise pass through |

New Mixin classes:
- `ChronoShieldBlockingMixin` (LivingEntity): #1 timing + block-success detection
- `ChronoShieldDamageMixin` (Player): #12 echo consumption on damage
- `ChronoShieldEffectInterceptorMixin` (LivingEntity): A debuff shortening

All three are registered in both `chronodawn-fabric.mixins.json` (with refMap) and `chronodawn-neoforge.mixins.json` (without refMap), matching the project's existing dual-loader pattern.

### 7.3 State Storage

State is owned by the player, not the item, because:
- Cooldowns must persist across shield hot-swap to prevent exploits
- Avoids 1.20.1 NBT vs 1.21.5+ DataComponent divergence
- Durability remains on the ItemStack as normal

New fields in `PlayerProgressData`:
- `shieldSpeedCooldownEndTick: long` (#7)
- `shieldEchoActiveUntilTick: long` (#12)
- `shieldEchoCooldownEndTick: long` (#12)

### 7.4 Version Pitfall Summary

- `MobEffectInstance` constructor: raw `MobEffect` in 1.20.1 vs `Holder<MobEffect>` from 1.21.1+ (follow `ChronoAegisItem` per-version pattern)
- `new Item.Properties()` / `ShieldItem` creation requires `.setId()` from 1.21.2+ (follow existing armor/tool registration)
- Recipe JSON format differs between 1.20.1 / 1.21.1 / 1.21.2+ (follow existing recipes; per `feedback_recipe_json_format_versions` memory)
- `net.minecraft.Util` moved to `net.minecraft.util.Util` in 1.21.11 (copy imports from same-version sibling file)
- `submit()` / rendering API breakage at 1.21.9+ for custom models — follow existing render patterns

## 8. Decision Criterion: PlayerProgressData vs ItemStack NBT

For future designers touching similar effects:

| Axis | PlayerProgressData | ItemStack NBT / DataComponent |
|------|-------------------|-------------------------------|
| Should hot-swap reset state? | No (anti-exploit) | Yes (multiple independent items) |
| Cross-version storage complexity | ◎ (single mechanism) | △ (NBT vs DataComponent split) |
| "Player skill" flavor | ◎ | - |
| Multiple items held simultaneously carrying independent state | — | ◎ |

For the shields in this design, all non-durability state is personal-cooldown skill state → PlayerProgressData.

## 9. Test Plan

### 9.1 Coverage from existing validation tests

- `RecipeValidationTest` auto-validates the three new recipes
- `LootTableValidationTest` unaffected (shields have no drops; if dropped by Entropy Keeper later, add loot-table entry and coverage)
- `TagValidationTest` checks inclusion in `minecraft:shields` tag (or equivalent per version)
- `CrossVersionConsistencyTest` asserts all three item IDs are registered in every supported MC version
- `CreativeTabCompletenessTest` asserts presence in the Combat or Equipment creative tab
- Translation validation for `item.chronodawn.*_shield` and tooltip keys in `en_us` and `ja_jp`

### 9.2 New GameTests under `common/gametest/`

- `ShieldBlockSuccessTest` — basic block reduces damage (all 3 tiers)
- `ShieldDebuffReductionTest` — A: Slowness applied mid-block gets halved duration
- `ShieldRaiseTimingTest` — #1: block becomes active on tick 3 instead of 5
- `ShieldSpeedOnBlockTest` — #7: T2/T3 grant Speed I on successful block, honor 3s CD
- `ShieldTimeEchoTest` — #12: T3 generates echo, auto-blocks within 5s, enters 30s CD
- `ShieldUpgradeRecipeTest` — crafting table upgrade chain T1→T2→T3 works

### 9.3 Manual verification per MC version via `runClient*`

- 3D model appearance (idle vs blocking)
- Particle effects at each echo stage
- Sound playback

## 10. Documentation Updates

Required:
- `docs/player_guide.md` — new "Custom Shields" section near the Armor section; describe tier upgrade path and effects
- `docs/developer_guide.md` — shield class layout, new Mixins, new `PlayerProgressData` fields
- `docs/curseforge_description.md` and `docs/modrinth_description.md` — add to Features
- `CHANGELOG.md` — entry for the next version: "Added ChronoDawn Custom Shields (3 tiers)"

Optional:
- `README.md` — single-line addition to the equipment summary if current granularity warrants it

## 11. Task Breakdown (Implementation Phases)

Each phase is independently committable.

| Phase | Content | Exit criterion |
|-------|---------|----------------|
| 1 | Add entries to `ModItemId`; create `ChronoShieldTier` enum; empty Item classes in all versions | Registration succeeds, items visible in creative tab |
| 2 | Base `ShieldItem` behavior (vanilla block + durability), per-version wiring | Shield can be held and blocks like vanilla |
| 3 | Effect A (Mixin on MobEffect application) | `ShieldDebuffReductionTest` passes on all versions |
| 4 | Effect #1 (Mixin on 5-tick activation) | `ShieldRaiseTimingTest` passes |
| 5 | Effect #7 (block-success detection + Speed + CD) | `ShieldSpeedOnBlockTest` passes for T2/T3 |
| 6 | Effect #12 (echo state + Player.hurt Mixin + CD) | `ShieldTimeEchoTest` passes for T3 |
| 7 | Custom particle `ChronoShieldEchoParticle` + three-stage visualization + sounds | Visual confirmation in `runClient` |
| 8 | Recipes (upgrade chain), tags, language files (en_us, ja_jp) | `validateResources` and `validateTranslations` pass |
| 9 | Item textures (3) + 3D models (idle + blocking for 3 tiers) | Visually complete |
| 10 | Documentation updates + CHANGELOG + full `./gradlew checkAll` | Ready for release |

## 12. Open Parameters Confirmed

- A reduction: 50%
- #1 raise: 3 ticks
- #7: Speed I, 1s, 3s CD
- #12: 5s active window, 30s CD
- Durability: 400 / 600 / 800

These values are considered tuned for the design stage; further tuning may happen during gameplay testing in Phase 10.

## 13. Out of Scope (for future work)

- A fourth, boss-drop-based ultimate shield
- Banner pattern rendering on custom shields
- Interaction with `Chrono Aegis` (no stacking logic defined)
- Other material-based shields (e.g., Temporal Amber Shield)
