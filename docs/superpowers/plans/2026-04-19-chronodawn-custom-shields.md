# ChronoDawn Custom Shields Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Add three tiered custom shields (Clockstone / Enhanced Clockstone / Entropy Crystal) with cumulative time-manipulation effects, independent of the existing Chrono Aegis artifact.

**Architecture:** Per-version `ShieldItem` subclasses with shared effect logic in `common/shared`. Behavior changes (debuff shortening, raise timing, block-success hooks, echo damage handling) are implemented as Mixins. Personal cooldown state lives in `PlayerProgressData`, not ItemStack NBT, so hot-swap cannot reset CDs. Reference implementation is done on MC 1.21.11 first, then ported to 1.20.1 / 1.21.1 / 1.21.2 / 1.21.4 / 1.21.5 / 1.21.6 / 1.21.7 / 1.21.8 / 1.21.9 / 1.21.10.

**Tech Stack:** Java 21, Minecraft 1.20.1 + 1.21.1 through 1.21.11, Architectury Loom 1.13-SNAPSHOT (Groovy DSL), Mojang mappings, Shadow 8.3.6, Mixin, existing `chronodawn-fabric.mixins.json` / `chronodawn-neoforge.mixins.json` dual-loader pattern.

**Spec:** `docs/superpowers/specs/2026-04-19-chronodawn-custom-shields-design.md`

---

## File Structure

### New files (shared — one copy total)

```
common/shared/src/main/java/com/chronodawn/items/shield/
  ChronoShieldTier.java                 (enum T1/T2/T3 + base stats + effect flags)
  ChronoShieldMarker.java               (marker interface; implemented by all 3 item classes)
  ChronoShieldEffectHandler.java        (static helpers: applyDebuffShortening, onBlockSuccess, tryGenerateEcho, tryConsumeEcho)

common/shared/src/main/java/com/chronodawn/client/particle/
  ChronoShieldEchoParticle.java         (custom particle, 3 stage variants via ParticleOptions)
```

### New files (per MC version — 11 copies: 1.20.1, 1.21.1, 1.21.2, 1.21.4, 1.21.5, 1.21.6, 1.21.7, 1.21.8, 1.21.9, 1.21.10, 1.21.11)

```
common/{ver}/src/main/java/com/chronodawn/items/
  ClockstoneShieldItem.java             (extends ShieldItem, implements ChronoShieldMarker, tier=T1)
  EnhancedClockstoneShieldItem.java     (tier=T2)
  EntropyCrystalShieldItem.java         (tier=T3)

common/{ver}/src/main/java/com/chronodawn/mixin/
  ChronoShieldBlockingMixin.java        (LivingEntity: #1 raise timing + block-success detection hook)
  ChronoShieldDamageMixin.java          (Player: #12 echo consumption)
  ChronoShieldEffectInterceptorMixin.java (LivingEntity: effect A MobEffect duration halving)
```

### Modified files (shared)

```
common/shared/src/main/java/com/chronodawn/registry/ModItemId.java      (+3 enum entries)
common/shared/src/main/java/com/chronodawn/registry/ModParticles.java   (+1 registration)
common/shared/src/main/java/com/chronodawn/registry/ModSounds.java      (+3 sound events: echo_generate, echo_active_pulse, echo_consume)
common/shared/src/main/java/com/chronodawn/data/PlayerProgressData.java (+3 tick fields per player)
```

### Modified files (per MC version)

```
common/{ver}/src/main/java/com/chronodawn/registry/ModItems.java  (+3 RegistrySupplier entries + creative tab adds)
fabric/{ver}/src/main/resources/chronodawn-fabric.mixins.json     (+3 mixin entries with refMap)
neoforge/{ver}/src/main/resources/chronodawn-neoforge.mixins.json (+3 mixin entries without refMap)
```

### New resources (shared — placed under `common/shared-1.21.1+` for 1.21.1+, `common/1.20.1` for 1.20.1)

```
assets/chronodawn/textures/item/
  clockstone_shield.png
  clockstone_shield_blocking.png        (3D model texture for blocking pose)
  enhanced_clockstone_shield.png
  enhanced_clockstone_shield_blocking.png
  entropy_crystal_shield.png
  entropy_crystal_shield_blocking.png
  entropy_crystal_shield_emissive.png   (T3 only, for emissive layer)
  chrono_shield_echo_particle.png       (particle texture)

assets/chronodawn/models/item/
  clockstone_shield.json
  clockstone_shield_blocking.json
  enhanced_clockstone_shield.json
  enhanced_clockstone_shield_blocking.json
  entropy_crystal_shield.json
  entropy_crystal_shield_blocking.json

assets/chronodawn/particles/
  chrono_shield_echo.json               (particle definition pointing at texture)

assets/chronodawn/lang/
  en_us.json  (add item names + tooltips)
  ja_jp.json  (add item names + tooltips)

assets/chronodawn/sounds.json          (add 3 shield echo sound events)
assets/chronodawn/sounds/item/shield/
  echo_generate.ogg
  echo_active_pulse.ogg
  echo_consume.ogg

data/chronodawn/recipe/
  clockstone_shield.json
  enhanced_clockstone_shield.json
  entropy_crystal_shield.json

data/minecraft/tags/item/              (path differs per version era)
  shields.json                         (add 3 shield IDs to existing vanilla tag file if not already forged; or create)
```

### Modified files (docs — updated at the end)

```
docs/player_guide.md
docs/developer_guide.md
docs/curseforge_description.md
docs/modrinth_description.md
CHANGELOG.md
```

---

## Cross-Version Strategy

**Reference version for full feature development: MC 1.21.11** (`-Ptarget_mc_version=1.21.11`, the default build target).

Phases 1–8 develop the reference version end-to-end. Phase 9 ports structure to other versions. Phase 10 does cross-version validation and docs.

**Per-version porting deltas to watch for** (from project memory):
- `Item.Properties.setId()` required from 1.21.2+
- `MobEffectInstance` constructor takes `Holder<MobEffect>` from 1.21.1+ (raw `MobEffect` in 1.20.1)
- `net.minecraft.Util` moved to `net.minecraft.util.Util` in 1.21.11 only
- Recipe JSON format differs: 1.20.1 uses `result.item`, 1.21.1 uses `result.id` object form, 1.21.2+ consolidates
- Tag file path `data/minecraft/tags/items/shields.json` changed to singular `item` in 1.21.1+
- ShieldItem rendering API changed at 1.21.9+ (submit() drops packedLight param — use state.lightCoords)
- Fabric mixin config needs `"refmap": "common-common-refmap.json"`; NeoForge must NOT have refMap

---

## Phase 1: Foundation Types and IDs

### Task 1: Add shield item IDs to `ModItemId`

**Files:**
- Modify: `common/shared/src/main/java/com/chronodawn/registry/ModItemId.java`
- Test: `common/shared/src/test/java/com/chronodawn/unit/ModIdEnumTest.java` (existing test auto-covers)

- [ ] **Step 1: Add three enum entries under the `// === Armor ===` or a new `// === Shields ===` section**

Insert after the `TEMPORAL_AMBER_BOOTS` line:

```java
    // === Shields ===
    CLOCKSTONE_SHIELD("clockstone_shield"),
    ENHANCED_CLOCKSTONE_SHIELD("enhanced_clockstone_shield"),
    ENTROPY_CRYSTAL_SHIELD("entropy_crystal_shield"),
```

- [ ] **Step 2: Run `ModIdEnumTest` on reference version**

Run: `./gradlew :common-1.21.11:test -Ptarget_mc_version=1.21.11 --tests "com.chronodawn.unit.ModIdEnumTest"`
Expected: PASS (enum additions are compile-time safe; test verifies ID format)

- [ ] **Step 3: Commit**

```bash
git add common/shared/src/main/java/com/chronodawn/registry/ModItemId.java
git commit -m "feat(shields): add 3 shield item IDs to ModItemId enum"
```

### Task 2: Create `ChronoShieldTier` enum

**Files:**
- Create: `common/shared/src/main/java/com/chronodawn/items/shield/ChronoShieldTier.java`

- [ ] **Step 1: Create the enum with base stats and cumulative effect flags**

```java
package com.chronodawn.items.shield;

public enum ChronoShieldTier {
    T1(400, 100, false, false),   // axe disable 5s=100ticks, no speed, no echo
    T2(600, 60, true,  false),    // axe disable 3s=60ticks, speed, no echo
    T3(800, 40, true,  true);     // axe disable 2s=40ticks, speed, echo

    public final int durability;
    public final int axeDisableTicks;
    public final boolean hasSpeedOnBlock;   // effect #7
    public final boolean hasTimeEcho;       // effect #12

    ChronoShieldTier(int durability, int axeDisableTicks, boolean hasSpeedOnBlock, boolean hasTimeEcho) {
        this.durability = durability;
        this.axeDisableTicks = axeDisableTicks;
        this.hasSpeedOnBlock = hasSpeedOnBlock;
        this.hasTimeEcho = hasTimeEcho;
    }
}
```

Effect A (debuff shortening) and effect #1 (3-tick raise) apply to all tiers, so they don't need flags here.

- [ ] **Step 2: Verify compilation**

Run: `./gradlew :common-1.21.11:compileJava -Ptarget_mc_version=1.21.11`
Expected: BUILD SUCCESSFUL

- [ ] **Step 3: Commit**

```bash
git add common/shared/src/main/java/com/chronodawn/items/shield/ChronoShieldTier.java
git commit -m "feat(shields): add ChronoShieldTier enum with tier stats"
```

### Task 3: Create `ChronoShieldMarker` interface

**Files:**
- Create: `common/shared/src/main/java/com/chronodawn/items/shield/ChronoShieldMarker.java`

- [ ] **Step 1: Create a simple marker interface so Mixins can `instanceof` check the item without needing class-name imports**

```java
package com.chronodawn.items.shield;

/**
 * Marker interface implemented by all ChronoDawn custom shield items.
 * Mixins use `instanceof ChronoShieldMarker` to detect our shields regardless of
 * which ShieldItem subclass is used per MC version.
 */
public interface ChronoShieldMarker {
    ChronoShieldTier getChronoShieldTier();
}
```

- [ ] **Step 2: Verify compilation**

Run: `./gradlew :common-1.21.11:compileJava -Ptarget_mc_version=1.21.11`
Expected: BUILD SUCCESSFUL

- [ ] **Step 3: Commit**

```bash
git add common/shared/src/main/java/com/chronodawn/items/shield/ChronoShieldMarker.java
git commit -m "feat(shields): add ChronoShieldMarker interface for Mixin detection"
```

---

## Phase 2: Reference Shield Classes (1.21.11) and Basic Registration

### Task 4: Create `ClockstoneShieldItem` class for 1.21.11

**Files:**
- Create: `common/1.21.11/src/main/java/com/chronodawn/items/ClockstoneShieldItem.java`

- [ ] **Step 1: Write the class extending `ShieldItem`**

```java
package com.chronodawn.items;

import com.chronodawn.items.shield.ChronoShieldMarker;
import com.chronodawn.items.shield.ChronoShieldTier;
import net.minecraft.world.item.ShieldItem;

public class ClockstoneShieldItem extends ShieldItem implements ChronoShieldMarker {
    public ClockstoneShieldItem(Properties properties) {
        super(properties.durability(ChronoShieldTier.T1.durability));
    }

    @Override
    public ChronoShieldTier getChronoShieldTier() {
        return ChronoShieldTier.T1;
    }
}
```

- [ ] **Step 2: Verify compilation**

Run: `./gradlew :common-1.21.11:compileJava -Ptarget_mc_version=1.21.11`
Expected: BUILD SUCCESSFUL

### Task 5: Create `EnhancedClockstoneShieldItem` and `EntropyCrystalShieldItem` classes for 1.21.11

**Files:**
- Create: `common/1.21.11/src/main/java/com/chronodawn/items/EnhancedClockstoneShieldItem.java`
- Create: `common/1.21.11/src/main/java/com/chronodawn/items/EntropyCrystalShieldItem.java`

- [ ] **Step 1: Write `EnhancedClockstoneShieldItem`**

```java
package com.chronodawn.items;

import com.chronodawn.items.shield.ChronoShieldMarker;
import com.chronodawn.items.shield.ChronoShieldTier;
import net.minecraft.world.item.ShieldItem;

public class EnhancedClockstoneShieldItem extends ShieldItem implements ChronoShieldMarker {
    public EnhancedClockstoneShieldItem(Properties properties) {
        super(properties.durability(ChronoShieldTier.T2.durability));
    }

    @Override
    public ChronoShieldTier getChronoShieldTier() {
        return ChronoShieldTier.T2;
    }
}
```

- [ ] **Step 2: Write `EntropyCrystalShieldItem`**

```java
package com.chronodawn.items;

import com.chronodawn.items.shield.ChronoShieldMarker;
import com.chronodawn.items.shield.ChronoShieldTier;
import net.minecraft.world.item.Rarity;
import net.minecraft.world.item.ShieldItem;

public class EntropyCrystalShieldItem extends ShieldItem implements ChronoShieldMarker {
    public EntropyCrystalShieldItem(Properties properties) {
        super(properties
            .durability(ChronoShieldTier.T3.durability)
            .rarity(Rarity.RARE)
        );
    }

    @Override
    public ChronoShieldTier getChronoShieldTier() {
        return ChronoShieldTier.T3;
    }
}
```

- [ ] **Step 3: Verify compilation**

Run: `./gradlew :common-1.21.11:compileJava -Ptarget_mc_version=1.21.11`
Expected: BUILD SUCCESSFUL

- [ ] **Step 4: Commit Tasks 4 and 5**

```bash
git add common/1.21.11/src/main/java/com/chronodawn/items/ClockstoneShieldItem.java common/1.21.11/src/main/java/com/chronodawn/items/EnhancedClockstoneShieldItem.java common/1.21.11/src/main/java/com/chronodawn/items/EntropyCrystalShieldItem.java
git commit -m "feat(shields): add 3 tiered ShieldItem subclasses for 1.21.11"
```

### Task 6: Register shields in `ModItems` for 1.21.11

**Files:**
- Modify: `common/1.21.11/src/main/java/com/chronodawn/registry/ModItems.java`

- [ ] **Step 1: Locate a good insertion point near existing armor registrations and add three `RegistrySupplier<Item>` entries**

Find the section after `TEMPORAL_AMBER_BOOTS` registration. Insert:

```java
    // ========== Shields ==========

    public static final RegistrySupplier<Item> CLOCKSTONE_SHIELD = ITEMS.register(
        ModItemId.CLOCKSTONE_SHIELD.id(),
        () -> new com.chronodawn.items.ClockstoneShieldItem(
            new Item.Properties()
                .setId(ResourceKey.create(Registries.ITEM,
                    Identifier.fromNamespaceAndPath(ChronoDawn.MOD_ID, ModItemId.CLOCKSTONE_SHIELD.id())))
        )
    );

    public static final RegistrySupplier<Item> ENHANCED_CLOCKSTONE_SHIELD = ITEMS.register(
        ModItemId.ENHANCED_CLOCKSTONE_SHIELD.id(),
        () -> new com.chronodawn.items.EnhancedClockstoneShieldItem(
            new Item.Properties()
                .setId(ResourceKey.create(Registries.ITEM,
                    Identifier.fromNamespaceAndPath(ChronoDawn.MOD_ID, ModItemId.ENHANCED_CLOCKSTONE_SHIELD.id())))
        )
    );

    public static final RegistrySupplier<Item> ENTROPY_CRYSTAL_SHIELD = ITEMS.register(
        ModItemId.ENTROPY_CRYSTAL_SHIELD.id(),
        () -> new com.chronodawn.items.EntropyCrystalShieldItem(
            new Item.Properties()
                .setId(ResourceKey.create(Registries.ITEM,
                    Identifier.fromNamespaceAndPath(ChronoDawn.MOD_ID, ModItemId.ENTROPY_CRYSTAL_SHIELD.id())))
        )
    );
```

- [ ] **Step 2: Add creative tab entries**

Find the creative tab registration block (search for `output.accept(TEMPORAL_AMBER_BOOTS.get());` or similar). Insert after the armor section:

```java
        // Shields
        output.accept(CLOCKSTONE_SHIELD.get());
        output.accept(ENHANCED_CLOCKSTONE_SHIELD.get());
        output.accept(ENTROPY_CRYSTAL_SHIELD.get());
```

- [ ] **Step 3: Verify compilation and registry**

Run: `./gradlew :common-1.21.11:build -Ptarget_mc_version=1.21.11 -x test`
Expected: BUILD SUCCESSFUL

- [ ] **Step 4: Run CrossVersionConsistency and CreativeTabCompleteness tests**

Run: `./gradlew :common-1.21.11:test -Ptarget_mc_version=1.21.11 --tests "com.chronodawn.unit.CrossVersionConsistencyTest" --tests "com.chronodawn.unit.CreativeTabCompletenessTest"`
Expected: PASS (all 3 shield IDs registered and in creative tab)

- [ ] **Step 5: Commit**

```bash
git add common/1.21.11/src/main/java/com/chronodawn/registry/ModItems.java
git commit -m "feat(shields): register 3 shields and add to creative tab (1.21.11)"
```

### Task 7: Create placeholder textures and item models for 1.21.11

At this phase we use simple placeholder textures to enable smoke-testing. Final art comes in Phase 9.

**Files:**
- Create: `common/shared/src/main/resources/assets/chronodawn/textures/item/clockstone_shield.png` (16×16, simple yellow disk)
- Create: `common/shared/src/main/resources/assets/chronodawn/textures/item/enhanced_clockstone_shield.png` (16×16, orange)
- Create: `common/shared/src/main/resources/assets/chronodawn/textures/item/entropy_crystal_shield.png` (16×16, violet)
- Create: `common/shared/src/main/resources/assets/chronodawn/models/item/clockstone_shield.json`
- Create: `common/shared/src/main/resources/assets/chronodawn/models/item/enhanced_clockstone_shield.json`
- Create: `common/shared/src/main/resources/assets/chronodawn/models/item/entropy_crystal_shield.json`

- [ ] **Step 1: Generate three placeholder 16×16 PNGs**

Use any tool (e.g., GIMP, or generate programmatically). Pure-color squares are acceptable for smoke testing. Proper art lands in Phase 9.

- [ ] **Step 2: Write three model JSONs following the vanilla `item/handheld` parent**

Clockstone shield model example:

```json
{
  "parent": "minecraft:item/handheld",
  "textures": {
    "layer0": "chronodawn:item/clockstone_shield"
  }
}
```

Replace `clockstone_shield` with the corresponding ID for the other two files.

- [ ] **Step 3: Run resource validation**

Run: `./gradlew validateResources`
Expected: PASS (JSON syntax OK, texture references exist)

- [ ] **Step 4: Manual smoke test**

Run: `./gradlew runClientFabric1_21_11`
Create a world in creative mode → search creative tab for the new shields → confirm they appear with their placeholder textures → hold one in main hand → confirm no crash.

- [ ] **Step 5: Commit**

```bash
git add common/shared/src/main/resources/assets/chronodawn/textures/item/clockstone_shield.png common/shared/src/main/resources/assets/chronodawn/textures/item/enhanced_clockstone_shield.png common/shared/src/main/resources/assets/chronodawn/textures/item/entropy_crystal_shield.png common/shared/src/main/resources/assets/chronodawn/models/item/clockstone_shield.json common/shared/src/main/resources/assets/chronodawn/models/item/enhanced_clockstone_shield.json common/shared/src/main/resources/assets/chronodawn/models/item/entropy_crystal_shield.json
git commit -m "feat(shields): add placeholder textures and item models"
```

---

## Phase 3: Effect A — Temporal Debuff Resistance

### Task 8: Create `ChronoShieldEffectHandler` with debuff shortening logic

**Files:**
- Create: `common/shared/src/main/java/com/chronodawn/items/shield/ChronoShieldEffectHandler.java`

- [ ] **Step 1: Create the handler class with effect A logic (other methods are stubs for now)**

```java
package com.chronodawn.items.shield;

import net.minecraft.core.Holder;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;

import java.util.Set;

public final class ChronoShieldEffectHandler {
    private ChronoShieldEffectHandler() {}

    private static final Set<ResourceLocation> SHORTENED_EFFECTS = Set.of(
        BuiltInRegistries.MOB_EFFECT.getKey(MobEffects.MOVEMENT_SLOWDOWN.value()),
        BuiltInRegistries.MOB_EFFECT.getKey(MobEffects.WEAKNESS.value()),
        BuiltInRegistries.MOB_EFFECT.getKey(MobEffects.DIG_SLOWDOWN.value())
    );

    /**
     * Effect A — halves duration of time-themed debuffs when entity is blocking with a ChronoDawn shield.
     * Returns the (possibly modified) instance; a new instance is returned when duration changes.
     */
    public static MobEffectInstance maybeShortenDebuff(LivingEntity target, MobEffectInstance incoming) {
        if (!isBlockingWithChronoShield(target)) return incoming;
        Holder<MobEffect> holder = incoming.getEffect();
        ResourceLocation id = BuiltInRegistries.MOB_EFFECT.getKey(holder.value());
        if (!SHORTENED_EFFECTS.contains(id)) return incoming;
        int halved = Math.max(1, incoming.getDuration() / 2);
        return new MobEffectInstance(holder, halved, incoming.getAmplifier(), incoming.isAmbient(), incoming.isVisible(), incoming.showIcon());
    }

    public static boolean isBlockingWithChronoShield(LivingEntity entity) {
        if (!entity.isBlocking()) return false;
        ItemStack useItem = entity.getUseItem();
        return useItem.getItem() instanceof ChronoShieldMarker;
    }
}
```

**Note:** In 1.20.1, `MobEffects.MOVEMENT_SLOWDOWN` returns a raw `MobEffect` rather than a `Holder`. The per-version port in Phase 9 will adjust the `BuiltInRegistries.MOB_EFFECT.getKey(...)` calls to drop the `.value()` accessor.

- [ ] **Step 2: Verify compilation**

Run: `./gradlew :common-1.21.11:compileJava -Ptarget_mc_version=1.21.11`
Expected: BUILD SUCCESSFUL

- [ ] **Step 3: Commit**

```bash
git add common/shared/src/main/java/com/chronodawn/items/shield/ChronoShieldEffectHandler.java
git commit -m "feat(shields): add ChronoShieldEffectHandler with debuff shortening"
```

### Task 9: Write GameTest for effect A

**Files:**
- Create: `common/gametest/src/main/java/com/chronodawn/gametest/ShieldDebuffReductionTest.java`

- [ ] **Step 1: Write the failing test**

```java
package com.chronodawn.gametest;

import com.chronodawn.registry.ModItems;
import net.minecraft.core.BlockPos;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.UseAnim;

public class ShieldDebuffReductionTest {

    @GameTest(template = "chronodawn:empty_platform")
    public static void slowness_halved_when_blocking_with_chrono_shield(GameTestHelper helper) {
        Player player = helper.makeMockPlayer();
        player.setItemSlot(EquipmentSlot.OFFHAND, new ItemStack(ModItems.CLOCKSTONE_SHIELD.get()));
        player.startUsingItem(net.minecraft.world.InteractionHand.OFF_HAND);
        helper.runAfterDelay(4L, () -> {
            // Apply Slowness for 200 ticks (10 seconds)
            player.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, 200, 0));
            MobEffectInstance active = player.getEffect(MobEffects.MOVEMENT_SLOWDOWN);
            helper.assertTrue(active != null, "Slowness should be applied");
            helper.assertTrue(active.getDuration() <= 100,
                "Slowness duration should be halved to <=100 ticks, was " + active.getDuration());
            helper.succeed();
        });
    }
}
```

- [ ] **Step 2: Register the test in the GameTest entrypoint**

Find the existing GameTest registry file (e.g., `ChronoDawnGameTests.java` in `common/gametest`). Add a reference to the new test class.

- [ ] **Step 3: Run the test and verify it fails (because Mixin is not yet in place)**

Run: `./gradlew :fabric:runGameTest -Ptarget_mc_version=1.21.11 --tests "*slowness_halved_when_blocking_with_chrono_shield*"`
Expected: FAIL — Slowness duration stays at 200 ticks because the Mixin isn't wired up yet.

### Task 10: Add `ChronoShieldEffectInterceptorMixin`

**Files:**
- Create: `common/1.21.11/src/main/java/com/chronodawn/mixin/ChronoShieldEffectInterceptorMixin.java`

- [ ] **Step 1: Write the Mixin targeting `LivingEntity.addEffect(MobEffectInstance, Entity)`**

```java
package com.chronodawn.mixin;

import com.chronodawn.items.shield.ChronoShieldEffectHandler;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.LivingEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;

@Mixin(LivingEntity.class)
public abstract class ChronoShieldEffectInterceptorMixin {

    @ModifyVariable(method = "addEffect(Lnet/minecraft/world/effect/MobEffectInstance;Lnet/minecraft/world/entity/Entity;)Z",
                    at = @At("HEAD"),
                    argsOnly = true,
                    ordinal = 0)
    private MobEffectInstance chronodawn$shortenShieldDebuff(MobEffectInstance incoming) {
        LivingEntity self = (LivingEntity)(Object)this;
        return ChronoShieldEffectHandler.maybeShortenDebuff(self, incoming);
    }
}
```

**Note:** If the `@ModifyVariable` approach proves fragile across versions, fall back to a `@Redirect` on the internal `MobEffectInstance` mutation path, or a `@Inject` with `@Cancellable` that re-issues a shortened instance. Document the pattern chosen at porting time.

- [ ] **Step 2: Register the Mixin in the Fabric config**

Edit `fabric/1.21.11/src/main/resources/chronodawn-fabric.mixins.json` — add `"ChronoShieldEffectInterceptorMixin"` to the `mixins` array.

- [ ] **Step 3: Register the Mixin in the NeoForge config**

Edit `neoforge/1.21.11/src/main/resources/chronodawn-neoforge.mixins.json` — add `"ChronoShieldEffectInterceptorMixin"` to the `mixins` array. **Do NOT add a `refmap` property to this file.**

- [ ] **Step 4: Run the GameTest from Task 9 and verify it now passes**

Run: `./gradlew :fabric:runGameTest -Ptarget_mc_version=1.21.11 --tests "*slowness_halved_when_blocking_with_chrono_shield*"`
Expected: PASS

- [ ] **Step 5: Commit**

```bash
git add common/1.21.11/src/main/java/com/chronodawn/mixin/ChronoShieldEffectInterceptorMixin.java fabric/1.21.11/src/main/resources/chronodawn-fabric.mixins.json neoforge/1.21.11/src/main/resources/chronodawn-neoforge.mixins.json common/gametest/src/main/java/com/chronodawn/gametest/ShieldDebuffReductionTest.java
git commit -m "feat(shields): effect A - halve time-debuff duration while blocking"
```

---

## Phase 4: Effect #1 — Faster Raise (3 ticks instead of 5)

### Task 11: Write GameTest for raise timing

**Files:**
- Create: `common/gametest/src/main/java/com/chronodawn/gametest/ShieldRaiseTimingTest.java`

- [ ] **Step 1: Write the failing test**

```java
package com.chronodawn.gametest;

import com.chronodawn.registry.ModItems;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;

public class ShieldRaiseTimingTest {

    @GameTest(template = "chronodawn:empty_platform")
    public static void chrono_shield_blocks_active_at_tick_3(GameTestHelper helper) {
        Player player = helper.makeMockPlayer();
        player.setItemSlot(EquipmentSlot.OFFHAND, new ItemStack(ModItems.CLOCKSTONE_SHIELD.get()));
        player.startUsingItem(InteractionHand.OFF_HAND);
        helper.runAfterDelay(3L, () -> {
            helper.assertTrue(player.isBlocking(),
                "ChronoDawn shield should be blocking at tick 3 (vanilla requires tick 5)");
            helper.succeed();
        });
    }

    @GameTest(template = "chronodawn:empty_platform")
    public static void vanilla_shield_still_requires_tick_5(GameTestHelper helper) {
        Player player = helper.makeMockPlayer();
        player.setItemSlot(EquipmentSlot.OFFHAND, new ItemStack(net.minecraft.world.item.Items.SHIELD));
        player.startUsingItem(InteractionHand.OFF_HAND);
        helper.runAfterDelay(3L, () -> {
            helper.assertTrue(!player.isBlocking(),
                "Vanilla shield should NOT be blocking at tick 3");
            helper.succeed();
        });
    }
}
```

- [ ] **Step 2: Register and run; confirm `chrono_shield_blocks_active_at_tick_3` fails**

Run: `./gradlew :fabric:runGameTest -Ptarget_mc_version=1.21.11 --tests "*chrono_shield_blocks_active_at_tick_3*"`
Expected: FAIL

### Task 12: Add `ChronoShieldBlockingMixin` for raise timing

**Files:**
- Create: `common/1.21.11/src/main/java/com/chronodawn/mixin/ChronoShieldBlockingMixin.java`

- [ ] **Step 1: Write the Mixin**

In 1.21.11, the vanilla check is effectively `this.getUseItemRemainingTicks() <= this.getUseItem().getUseDuration(this) - 5` inside `LivingEntity.isBlocking()`. We modify the `5` when the use item is a ChronoDawn shield.

```java
package com.chronodawn.mixin;

import com.chronodawn.items.shield.ChronoShieldMarker;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyConstant;

@Mixin(LivingEntity.class)
public abstract class ChronoShieldBlockingMixin {

    @ModifyConstant(method = "isBlocking()Z",
                    constant = @org.spongepowered.asm.mixin.injection.Constant(intValue = 5))
    private int chronodawn$fasterRaiseForChronoShield(int original) {
        LivingEntity self = (LivingEntity)(Object)this;
        ItemStack useItem = self.getUseItem();
        return useItem.getItem() instanceof ChronoShieldMarker ? 3 : original;
    }
}
```

**Note:** If `LivingEntity.isBlocking()` in your version contains the 5 constant via `Items.SHIELD.getUseDuration` return value rather than an inline literal, switch the target method — in some versions the 5 lives in `ShieldItem.getUseDuration`. Adjust the Mixin target at porting time; document the chosen approach per version.

- [ ] **Step 2: Register in both Fabric and NeoForge mixin configs for 1.21.11**

- [ ] **Step 3: Run the GameTests and confirm both pass**

Run: `./gradlew :fabric:runGameTest -Ptarget_mc_version=1.21.11 --tests "*ShieldRaiseTiming*"`
Expected: PASS (both tests)

- [ ] **Step 4: Commit**

```bash
git add common/1.21.11/src/main/java/com/chronodawn/mixin/ChronoShieldBlockingMixin.java fabric/1.21.11/src/main/resources/chronodawn-fabric.mixins.json neoforge/1.21.11/src/main/resources/chronodawn-neoforge.mixins.json common/gametest/src/main/java/com/chronodawn/gametest/ShieldRaiseTimingTest.java
git commit -m "feat(shields): effect #1 - 3-tick raise timing for ChronoDawn shields"
```

---

## Phase 5: Effect #7 — Speed on Block (T2+)

### Task 13: Extend `PlayerProgressData` with shield CD fields

**Files:**
- Modify: `common/shared/src/main/java/com/chronodawn/data/PlayerProgressData.java`

- [ ] **Step 1: Add three long fields to the inner `PlayerProgress` class**

```java
    public static class PlayerProgress {
        public boolean hasChronosEye;
        public Set<UUID> stabilizedPortals;
        public Set<String> defeatedBosses;

        // Shield state
        public long shieldSpeedCooldownEndTick;   // effect #7
        public long shieldEchoActiveUntilTick;    // effect #12
        public long shieldEchoCooldownEndTick;    // effect #12

        public PlayerProgress() {
            this.hasChronosEye = false;
            this.stabilizedPortals = new HashSet<>();
            this.defeatedBosses = new HashSet<>();
            this.shieldSpeedCooldownEndTick = 0L;
            this.shieldEchoActiveUntilTick = 0L;
            this.shieldEchoCooldownEndTick = 0L;
        }
    }
```

- [ ] **Step 2: Update the NBT serialize/deserialize methods**

Find the `save(CompoundTag, HolderLookup.Provider)` and `load(CompoundTag, HolderLookup.Provider)` methods. For each per-player record:

Save side (inside the loop that writes per-player NBT):
```java
            playerTag.putLong("shield_speed_cd", progress.shieldSpeedCooldownEndTick);
            playerTag.putLong("shield_echo_until", progress.shieldEchoActiveUntilTick);
            playerTag.putLong("shield_echo_cd", progress.shieldEchoCooldownEndTick);
```

Load side (inside the loop that reads per-player NBT):
```java
            progress.shieldSpeedCooldownEndTick = playerTag.getLong("shield_speed_cd");
            progress.shieldEchoActiveUntilTick = playerTag.getLong("shield_echo_until");
            progress.shieldEchoCooldownEndTick = playerTag.getLong("shield_echo_cd");
```

**Note:** 1.21.8+ introduced a new `Tag.Getter` / `TagValueInput` NBT API for some paths; if the existing save/load already uses the new-style API, mirror that. Follow the existing pattern exactly.

- [ ] **Step 3: Add accessor helpers**

Add to `PlayerProgressData`:

```java
    public long getShieldSpeedCooldownEnd(UUID playerId) {
        return playerData.getOrDefault(playerId, new PlayerProgress()).shieldSpeedCooldownEndTick;
    }

    public void setShieldSpeedCooldownEnd(UUID playerId, long endTick) {
        playerData.computeIfAbsent(playerId, k -> new PlayerProgress()).shieldSpeedCooldownEndTick = endTick;
        setDirty();
    }

    public long getShieldEchoActiveUntil(UUID playerId) {
        return playerData.getOrDefault(playerId, new PlayerProgress()).shieldEchoActiveUntilTick;
    }

    public void setShieldEchoActiveUntil(UUID playerId, long untilTick) {
        playerData.computeIfAbsent(playerId, k -> new PlayerProgress()).shieldEchoActiveUntilTick = untilTick;
        setDirty();
    }

    public long getShieldEchoCooldownEnd(UUID playerId) {
        return playerData.getOrDefault(playerId, new PlayerProgress()).shieldEchoCooldownEndTick;
    }

    public void setShieldEchoCooldownEnd(UUID playerId, long endTick) {
        playerData.computeIfAbsent(playerId, k -> new PlayerProgress()).shieldEchoCooldownEndTick = endTick;
        setDirty();
    }
```

- [ ] **Step 4: Verify compilation on reference version**

Run: `./gradlew :common-1.21.11:compileJava -Ptarget_mc_version=1.21.11`
Expected: BUILD SUCCESSFUL

- [ ] **Step 5: Commit**

```bash
git add common/shared/src/main/java/com/chronodawn/data/PlayerProgressData.java
git commit -m "feat(shields): add 3 shield state fields to PlayerProgressData"
```

### Task 14: Add `onBlockSuccess` to `ChronoShieldEffectHandler`

**Files:**
- Modify: `common/shared/src/main/java/com/chronodawn/items/shield/ChronoShieldEffectHandler.java`

- [ ] **Step 1: Add the handler method**

```java
    private static final int SPEED_DURATION_TICKS = 20;   // 1 second
    private static final int SPEED_COOLDOWN_TICKS = 60;   // 3 seconds

    /**
     * Effect #7 — grant Speed I to the player after a successful block with T2+ shield.
     * Internal 3-second cooldown stored in PlayerProgressData.
     */
    public static void onBlockSuccess(net.minecraft.server.level.ServerPlayer player, ChronoShieldTier tier) {
        if (!tier.hasSpeedOnBlock) return;
        long now = player.level().getGameTime();
        com.chronodawn.data.PlayerProgressData data =
            com.chronodawn.data.PlayerProgressData.get((net.minecraft.server.level.ServerLevel) player.level());
        long cdEnd = data.getShieldSpeedCooldownEnd(player.getUUID());
        if (now < cdEnd) return;
        data.setShieldSpeedCooldownEnd(player.getUUID(), now + SPEED_COOLDOWN_TICKS);
        player.addEffect(new MobEffectInstance(
            BuiltInRegistries.MOB_EFFECT.wrapAsHolder(MobEffects.MOVEMENT_SPEED.value()),
            SPEED_DURATION_TICKS, 0, false, false, true));
    }
```

- [ ] **Step 2: Verify compilation**

Run: `./gradlew :common-1.21.11:compileJava -Ptarget_mc_version=1.21.11`
Expected: BUILD SUCCESSFUL

### Task 15: Write GameTest for Speed on block

**Files:**
- Create: `common/gametest/src/main/java/com/chronodawn/gametest/ShieldSpeedOnBlockTest.java`

- [ ] **Step 1: Write the failing test**

```java
package com.chronodawn.gametest;

import com.chronodawn.registry.ModItems;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.damagesource.DamageTypes;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;

public class ShieldSpeedOnBlockTest {

    @GameTest(template = "chronodawn:empty_platform")
    public static void t2_shield_grants_speed_on_block(GameTestHelper helper) {
        Player player = helper.makeMockPlayer();
        player.setItemSlot(EquipmentSlot.OFFHAND, new ItemStack(ModItems.ENHANCED_CLOCKSTONE_SHIELD.get()));
        player.startUsingItem(InteractionHand.OFF_HAND);
        helper.runAfterDelay(4L, () -> {
            // Simulate a frontal melee hit
            var source = player.damageSources().source(DamageTypes.MOB_ATTACK);
            player.hurt(source, 2.0f);
            helper.assertTrue(player.hasEffect(MobEffects.MOVEMENT_SPEED),
                "T2 shield should grant Speed on block");
            helper.succeed();
        });
    }

    @GameTest(template = "chronodawn:empty_platform")
    public static void t1_shield_does_not_grant_speed(GameTestHelper helper) {
        Player player = helper.makeMockPlayer();
        player.setItemSlot(EquipmentSlot.OFFHAND, new ItemStack(ModItems.CLOCKSTONE_SHIELD.get()));
        player.startUsingItem(InteractionHand.OFF_HAND);
        helper.runAfterDelay(4L, () -> {
            var source = player.damageSources().source(DamageTypes.MOB_ATTACK);
            player.hurt(source, 2.0f);
            helper.assertTrue(!player.hasEffect(MobEffects.MOVEMENT_SPEED),
                "T1 shield should not grant Speed");
            helper.succeed();
        });
    }
}
```

- [ ] **Step 2: Run and confirm failure (Mixin not yet in place)**

Run: `./gradlew :fabric:runGameTest -Ptarget_mc_version=1.21.11 --tests "*ShieldSpeedOnBlock*"`
Expected: `t2_shield_grants_speed_on_block` FAILS.

### Task 16: Add block-success detection to `ChronoShieldBlockingMixin`

**Files:**
- Modify: `common/1.21.11/src/main/java/com/chronodawn/mixin/ChronoShieldBlockingMixin.java`

- [ ] **Step 1: Add an `@Inject` that fires when a shield successfully absorbs damage**

In 1.21.11 the relevant method is `LivingEntity.blockUsingShield(LivingEntity attacker)` (called from the damage pipeline when a shield absorbs a hit). Add:

```java
    @Inject(method = "blockUsingShield(Lnet/minecraft/world/entity/LivingEntity;)V", at = @At("TAIL"))
    private void chronodawn$onShieldBlock(LivingEntity attacker, CallbackInfo ci) {
        LivingEntity self = (LivingEntity)(Object)this;
        if (!(self instanceof net.minecraft.server.level.ServerPlayer player)) return;
        ItemStack useItem = self.getUseItem();
        if (!(useItem.getItem() instanceof com.chronodawn.items.shield.ChronoShieldMarker marker)) return;
        com.chronodawn.items.shield.ChronoShieldEffectHandler.onBlockSuccess(player, marker.getChronoShieldTier());
    }
```

Add the needed `import` for `CallbackInfo` and `@Inject`.

**Note:** In versions where `blockUsingShield` doesn't exist or has a different signature (notably 1.20.1, which uses `hurtCurrentlyUsedShield` differently), target the block-success branch inside `Player.hurt` or `LivingEntity.getDamageAfterMagicAbsorb` instead. Verify the target per version at porting time.

- [ ] **Step 2: Run GameTests and confirm both pass**

Run: `./gradlew :fabric:runGameTest -Ptarget_mc_version=1.21.11 --tests "*ShieldSpeedOnBlock*"`
Expected: PASS (both)

- [ ] **Step 3: Commit**

```bash
git add common/1.21.11/src/main/java/com/chronodawn/mixin/ChronoShieldBlockingMixin.java common/shared/src/main/java/com/chronodawn/items/shield/ChronoShieldEffectHandler.java common/gametest/src/main/java/com/chronodawn/gametest/ShieldSpeedOnBlockTest.java
git commit -m "feat(shields): effect #7 - Speed I on successful block (T2+)"
```

### Task 17: Write GameTest for Speed cooldown

**Files:**
- Create: `common/gametest/src/main/java/com/chronodawn/gametest/ShieldSpeedCooldownTest.java`

- [ ] **Step 1: Write the failing test**

```java
package com.chronodawn.gametest;

import com.chronodawn.registry.ModItems;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.damagesource.DamageTypes;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;

public class ShieldSpeedCooldownTest {

    @GameTest(template = "chronodawn:empty_platform", timeoutTicks = 120)
    public static void speed_cooldown_respected(GameTestHelper helper) {
        Player player = helper.makeMockPlayer();
        player.setItemSlot(EquipmentSlot.OFFHAND, new ItemStack(ModItems.ENHANCED_CLOCKSTONE_SHIELD.get()));
        player.startUsingItem(InteractionHand.OFF_HAND);
        helper.runAfterDelay(4L, () -> {
            var source = player.damageSources().source(DamageTypes.MOB_ATTACK);
            player.hurt(source, 2.0f);
            int firstDuration = player.getEffect(MobEffects.MOVEMENT_SPEED).getDuration();
            // Second hit within cooldown should not re-apply / refresh
            helper.runAfterDelay(20L, () -> {
                player.hurt(source, 2.0f);
                int secondDuration = player.getEffect(MobEffects.MOVEMENT_SPEED).getDuration();
                helper.assertTrue(secondDuration < firstDuration,
                    "Within cooldown, Speed should not be refreshed (was " + secondDuration + ", first=" + firstDuration + ")");
                helper.succeed();
            });
        });
    }
}
```

- [ ] **Step 2: Run test**

Run: `./gradlew :fabric:runGameTest -Ptarget_mc_version=1.21.11 --tests "*speed_cooldown_respected*"`
Expected: PASS (the CD logic in `onBlockSuccess` already handles this)

- [ ] **Step 3: Commit**

```bash
git add common/gametest/src/main/java/com/chronodawn/gametest/ShieldSpeedCooldownTest.java
git commit -m "test(shields): verify Speed cooldown respected on repeated blocks"
```

---

## Phase 6: Effect #12 — Time Echo (T3)

### Task 18: Add echo generation to `ChronoShieldEffectHandler`

**Files:**
- Modify: `common/shared/src/main/java/com/chronodawn/items/shield/ChronoShieldEffectHandler.java`

- [ ] **Step 1: Add echo logic**

```java
    private static final int ECHO_ACTIVE_TICKS = 100;      // 5 seconds
    private static final int ECHO_COOLDOWN_TICKS = 600;    // 30 seconds

    /**
     * Effect #12 — schedule a ghost-shield echo for the next incoming hit.
     * Respects 30s cooldown.
     */
    public static void tryGenerateEcho(net.minecraft.server.level.ServerPlayer player, ChronoShieldTier tier) {
        if (!tier.hasTimeEcho) return;
        long now = player.level().getGameTime();
        com.chronodawn.data.PlayerProgressData data =
            com.chronodawn.data.PlayerProgressData.get((net.minecraft.server.level.ServerLevel) player.level());
        long cdEnd = data.getShieldEchoCooldownEnd(player.getUUID());
        if (now < cdEnd) return;
        long activeUntil = data.getShieldEchoActiveUntil(player.getUUID());
        if (now < activeUntil) return;  // echo already active
        data.setShieldEchoActiveUntil(player.getUUID(), now + ECHO_ACTIVE_TICKS);
        // emit particle/sound — wired up in Phase 7
    }

    /**
     * Effect #12 — consume an active echo to auto-block an incoming hit.
     * Returns true if the damage was absorbed (caller should cancel the damage and decrement durability).
     */
    public static boolean tryConsumeEcho(net.minecraft.server.level.ServerPlayer player) {
        long now = player.level().getGameTime();
        com.chronodawn.data.PlayerProgressData data =
            com.chronodawn.data.PlayerProgressData.get((net.minecraft.server.level.ServerLevel) player.level());
        long activeUntil = data.getShieldEchoActiveUntil(player.getUUID());
        if (now >= activeUntil) return false;
        // Consume: clear active window and start cooldown
        data.setShieldEchoActiveUntil(player.getUUID(), 0L);
        data.setShieldEchoCooldownEnd(player.getUUID(), now + ECHO_COOLDOWN_TICKS);
        // Decrement durability of whichever ChronoShield is held
        for (var hand : net.minecraft.world.InteractionHand.values()) {
            ItemStack stack = player.getItemInHand(hand);
            if (stack.getItem() instanceof ChronoShieldMarker) {
                stack.hurtAndBreak(1, player, net.minecraft.world.entity.EquipmentSlot.OFFHAND);
                break;
            }
        }
        return true;
    }
```

- [ ] **Step 2: Call `tryGenerateEcho` from the existing `onBlockSuccess` flow**

In `onBlockSuccess`, after the Speed branch, add:

```java
        tryGenerateEcho(player, tier);
```

- [ ] **Step 3: Verify compilation**

Run: `./gradlew :common-1.21.11:compileJava -Ptarget_mc_version=1.21.11`
Expected: BUILD SUCCESSFUL

### Task 19: Write GameTest for echo generation and consumption

**Files:**
- Create: `common/gametest/src/main/java/com/chronodawn/gametest/ShieldTimeEchoTest.java`

- [ ] **Step 1: Write the failing test**

```java
package com.chronodawn.gametest;

import com.chronodawn.data.PlayerProgressData;
import com.chronodawn.registry.ModItems;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.damagesource.DamageTypes;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;

public class ShieldTimeEchoTest {

    @GameTest(template = "chronodawn:empty_platform", timeoutTicks = 160)
    public static void t3_generates_echo_and_auto_blocks_next_hit(GameTestHelper helper) {
        Player player = helper.makeMockPlayer();
        player.setItemSlot(EquipmentSlot.OFFHAND, new ItemStack(ModItems.ENTROPY_CRYSTAL_SHIELD.get()));
        player.startUsingItem(InteractionHand.OFF_HAND);
        helper.runAfterDelay(4L, () -> {
            var src = player.damageSources().source(DamageTypes.MOB_ATTACK);
            player.hurt(src, 2.0f);  // First hit: blocked; generates echo.
            var data = PlayerProgressData.get((ServerLevel) player.level());
            long activeUntil = data.getShieldEchoActiveUntil(player.getUUID());
            helper.assertTrue(activeUntil > player.level().getGameTime(),
                "Echo should be active after first successful block");

            // Stop blocking, then take another hit → should auto-block via echo
            player.stopUsingItem();
            float hpBefore = player.getHealth();
            player.hurt(src, 2.0f);
            float hpAfter = player.getHealth();
            helper.assertTrue(hpAfter == hpBefore,
                "Second hit while echo active should be fully absorbed (hpBefore=" + hpBefore + " hpAfter=" + hpAfter + ")");
            helper.assertTrue(data.getShieldEchoCooldownEnd(player.getUUID()) > player.level().getGameTime(),
                "Echo cooldown should be set after consumption");
            helper.succeed();
        });
    }

    @GameTest(template = "chronodawn:empty_platform", timeoutTicks = 160)
    public static void t2_does_not_generate_echo(GameTestHelper helper) {
        Player player = helper.makeMockPlayer();
        player.setItemSlot(EquipmentSlot.OFFHAND, new ItemStack(ModItems.ENHANCED_CLOCKSTONE_SHIELD.get()));
        player.startUsingItem(InteractionHand.OFF_HAND);
        helper.runAfterDelay(4L, () -> {
            var src = player.damageSources().source(DamageTypes.MOB_ATTACK);
            player.hurt(src, 2.0f);
            var data = PlayerProgressData.get((ServerLevel) player.level());
            helper.assertTrue(data.getShieldEchoActiveUntil(player.getUUID()) == 0,
                "T2 shield must not generate echo");
            helper.succeed();
        });
    }
}
```

- [ ] **Step 2: Run the test — `t2_does_not_generate_echo` should pass; `t3_generates_echo_and_auto_blocks_next_hit` should fail (the Player.hurt Mixin is not yet in place)**

Run: `./gradlew :fabric:runGameTest -Ptarget_mc_version=1.21.11 --tests "*ShieldTimeEcho*"`
Expected: one PASS, one FAIL.

### Task 20: Create `ChronoShieldDamageMixin` for echo consumption

**Files:**
- Create: `common/1.21.11/src/main/java/com/chronodawn/mixin/ChronoShieldDamageMixin.java`

- [ ] **Step 1: Write the Mixin targeting `Player.hurt` prologue**

```java
package com.chronodawn.mixin;

import com.chronodawn.items.shield.ChronoShieldEffectHandler;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.player.Player;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(Player.class)
public abstract class ChronoShieldDamageMixin {

    @Inject(method = "hurt(Lnet/minecraft/world/damagesource/DamageSource;F)Z",
            at = @At("HEAD"),
            cancellable = true)
    private void chronodawn$maybeConsumeEcho(DamageSource source, float amount, CallbackInfoReturnable<Boolean> cir) {
        Player self = (Player)(Object)this;
        if (!(self instanceof ServerPlayer sp)) return;
        if (self.isBlocking()) return;   // already handled by vanilla shield path
        if (!isEchoBlockable(source)) return;
        if (ChronoShieldEffectHandler.tryConsumeEcho(sp)) {
            cir.setReturnValue(true);  // damage absorbed
        }
    }

    private static boolean isEchoBlockable(DamageSource source) {
        // Follow vanilla shield blockable rules: exclude BYPASSES_ARMOR,
        // IS_PROJECTILE_ARROW_THROUGH_SHIELD (piercing), and magic/fall/suffocation.
        if (source.is(net.minecraft.tags.DamageTypeTags.BYPASSES_ARMOR)) return false;
        if (source.is(net.minecraft.tags.DamageTypeTags.BYPASSES_INVULNERABILITY)) return false;
        if (source.is(net.minecraft.tags.DamageTypeTags.IS_FIRE)) return false;
        if (source.is(net.minecraft.tags.DamageTypeTags.IS_FALL)) return false;
        return true;
    }
}
```

**Note:** Exact tag names (`BYPASSES_ARMOR` vs `BYPASSES_ARMOR_AND_SHIELD`) vary across versions. Use the tag that matches vanilla's own shield block filter in each version.

In 1.21.8+ the damage method signature changed to `hurtServer(ServerLevel, DamageSource, float)` for server-side; if that's what vanilla uses in the target version, retarget accordingly.

- [ ] **Step 2: Register in both Fabric and NeoForge mixin configs for 1.21.11**

- [ ] **Step 3: Run tests; both should pass**

Run: `./gradlew :fabric:runGameTest -Ptarget_mc_version=1.21.11 --tests "*ShieldTimeEcho*"`
Expected: PASS (both)

- [ ] **Step 4: Commit**

```bash
git add common/1.21.11/src/main/java/com/chronodawn/mixin/ChronoShieldDamageMixin.java fabric/1.21.11/src/main/resources/chronodawn-fabric.mixins.json neoforge/1.21.11/src/main/resources/chronodawn-neoforge.mixins.json common/shared/src/main/java/com/chronodawn/items/shield/ChronoShieldEffectHandler.java common/gametest/src/main/java/com/chronodawn/gametest/ShieldTimeEchoTest.java
git commit -m "feat(shields): effect #12 - Time Echo auto-block for T3 shield"
```

### Task 21: Write GameTest for echo expiry (no auto-block after 5s)

**Files:**
- Create: `common/gametest/src/main/java/com/chronodawn/gametest/ShieldEchoExpiryTest.java`

- [ ] **Step 1: Write the test**

```java
package com.chronodawn.gametest;

import com.chronodawn.registry.ModItems;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.damagesource.DamageTypes;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;

public class ShieldEchoExpiryTest {

    @GameTest(template = "chronodawn:empty_platform", timeoutTicks = 240)
    public static void echo_expires_after_5_seconds(GameTestHelper helper) {
        Player player = helper.makeMockPlayer();
        player.setItemSlot(EquipmentSlot.OFFHAND, new ItemStack(ModItems.ENTROPY_CRYSTAL_SHIELD.get()));
        player.startUsingItem(InteractionHand.OFF_HAND);
        helper.runAfterDelay(4L, () -> {
            var src = player.damageSources().source(DamageTypes.MOB_ATTACK);
            player.hurt(src, 2.0f);  // generates echo
            player.stopUsingItem();
            helper.runAfterDelay(110L, () -> {   // >100 ticks elapsed
                float hpBefore = player.getHealth();
                player.hurt(src, 2.0f);
                helper.assertTrue(player.getHealth() < hpBefore,
                    "After echo expiry, second hit should deal damage");
                helper.succeed();
            });
        });
    }
}
```

- [ ] **Step 2: Run and verify passes**

Run: `./gradlew :fabric:runGameTest -Ptarget_mc_version=1.21.11 --tests "*echo_expires_after_5_seconds*"`
Expected: PASS

- [ ] **Step 3: Commit**

```bash
git add common/gametest/src/main/java/com/chronodawn/gametest/ShieldEchoExpiryTest.java
git commit -m "test(shields): verify echo expires after 5 seconds"
```

---

## Phase 7: Custom Particle and Sounds for Time Echo

### Task 22: Add shield echo sound events to `ModSounds`

**Files:**
- Modify: `common/shared/src/main/java/com/chronodawn/registry/ModSounds.java`
- Modify: `common/shared/src/main/resources/assets/chronodawn/sounds.json`

- [ ] **Step 1: Register three new sound events**

Add to `ModSounds.java`:

```java
    public static final RegistrySupplier<SoundEvent> SHIELD_ECHO_GENERATE =
        SOUNDS.register("shield_echo_generate", () -> SoundEvent.createVariableRangeEvent(
            Identifier.fromNamespaceAndPath(ChronoDawn.MOD_ID, "shield_echo_generate")));

    public static final RegistrySupplier<SoundEvent> SHIELD_ECHO_ACTIVE_PULSE =
        SOUNDS.register("shield_echo_active_pulse", () -> SoundEvent.createVariableRangeEvent(
            Identifier.fromNamespaceAndPath(ChronoDawn.MOD_ID, "shield_echo_active_pulse")));

    public static final RegistrySupplier<SoundEvent> SHIELD_ECHO_CONSUME =
        SOUNDS.register("shield_echo_consume", () -> SoundEvent.createVariableRangeEvent(
            Identifier.fromNamespaceAndPath(ChronoDawn.MOD_ID, "shield_echo_consume")));
```

- [ ] **Step 2: Add entries to `sounds.json`**

```json
  "shield_echo_generate": {
    "sounds": [ "chronodawn:item/shield/echo_generate" ]
  },
  "shield_echo_active_pulse": {
    "sounds": [ "chronodawn:item/shield/echo_active_pulse" ]
  },
  "shield_echo_consume": {
    "sounds": [ "chronodawn:item/shield/echo_consume" ]
  }
```

- [ ] **Step 3: Add placeholder OGG files (use short vanilla sound copies during development; final sounds in Phase 9)**

Create (or copy from vanilla `entity/zombie/attack_iron_door` etc. as temporary fillers):
- `common/shared/src/main/resources/assets/chronodawn/sounds/item/shield/echo_generate.ogg`
- `common/shared/src/main/resources/assets/chronodawn/sounds/item/shield/echo_active_pulse.ogg`
- `common/shared/src/main/resources/assets/chronodawn/sounds/item/shield/echo_consume.ogg`

- [ ] **Step 4: Verify**

Run: `./gradlew :common-1.21.11:compileJava -Ptarget_mc_version=1.21.11 && ./gradlew validateResources`
Expected: BUILD SUCCESSFUL

- [ ] **Step 5: Commit**

```bash
git add common/shared/src/main/java/com/chronodawn/registry/ModSounds.java common/shared/src/main/resources/assets/chronodawn/sounds.json common/shared/src/main/resources/assets/chronodawn/sounds/item/shield/
git commit -m "feat(shields): register 3 echo sound events with placeholder OGGs"
```

### Task 23: Create `ChronoShieldEchoParticle` class

**Files:**
- Create: `common/shared/src/main/java/com/chronodawn/client/particle/ChronoShieldEchoParticle.java`
- Create: `common/shared/src/main/resources/assets/chronodawn/particles/chrono_shield_echo.json`
- Create: `common/shared/src/main/resources/assets/chronodawn/textures/particle/chrono_shield_echo.png` (8×8 or 16×16 violet glow)

- [ ] **Step 1: Write the particle class (follow `ChronoDawnPortalParticle` as the reference pattern)**

```java
package com.chronodawn.client.particle;

import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.particle.Particle;
import net.minecraft.client.particle.ParticleProvider;
import net.minecraft.client.particle.ParticleRenderType;
import net.minecraft.client.particle.SpriteSet;
import net.minecraft.client.particle.TextureSheetParticle;
import net.minecraft.core.particles.SimpleParticleType;

public class ChronoShieldEchoParticle extends TextureSheetParticle {
    protected ChronoShieldEchoParticle(ClientLevel level, double x, double y, double z,
                                       double vx, double vy, double vz, SpriteSet sprites) {
        super(level, x, y, z, vx, vy, vz);
        this.rCol = 0.55f;
        this.gCol = 0.35f;
        this.bCol = 0.95f;   // blue-violet
        this.lifetime = 25;
        this.scale(1.4f);
        this.setSpriteFromAge(sprites);
    }

    @Override
    public ParticleRenderType getRenderType() {
        return ParticleRenderType.PARTICLE_SHEET_TRANSLUCENT;
    }

    public static class Provider implements ParticleProvider<SimpleParticleType> {
        private final SpriteSet sprites;
        public Provider(SpriteSet sprites) { this.sprites = sprites; }
        @Override
        public Particle createParticle(SimpleParticleType type, ClientLevel level,
                                       double x, double y, double z, double vx, double vy, double vz) {
            return new ChronoShieldEchoParticle(level, x, y, z, vx, vy, vz, sprites);
        }
    }
}
```

- [ ] **Step 2: Write the particle definition JSON**

```json
{
  "textures": [ "chronodawn:chrono_shield_echo" ]
}
```

- [ ] **Step 3: Register the particle type in `ModParticles`**

Add to `common/shared/src/main/java/com/chronodawn/registry/ModParticles.java`:

```java
    public static final RegistrySupplier<SimpleParticleType> CHRONO_SHIELD_ECHO =
        PARTICLES.register("chrono_shield_echo", () -> new SimpleParticleType(false));
```

- [ ] **Step 4: Register the particle provider on the client side**

Find the existing client-side particle registration (e.g., in Fabric `ChronoDawnClient` and NeoForge equivalent). Add:

```java
    particleEngine.register(ModParticles.CHRONO_SHIELD_ECHO.get(), ChronoShieldEchoParticle.Provider::new);
```

- [ ] **Step 5: Verify**

Run: `./gradlew :common-1.21.11:compileJava -Ptarget_mc_version=1.21.11 && ./gradlew validateResources`
Expected: BUILD SUCCESSFUL

- [ ] **Step 6: Commit**

```bash
git add common/shared/src/main/java/com/chronodawn/client/particle/ChronoShieldEchoParticle.java common/shared/src/main/resources/assets/chronodawn/particles/chrono_shield_echo.json common/shared/src/main/resources/assets/chronodawn/textures/particle/chrono_shield_echo.png common/shared/src/main/java/com/chronodawn/registry/ModParticles.java fabric/1.21.11/src/main/java/com/chronodawn/fabric/client/
git commit -m "feat(shields): add ChronoShieldEchoParticle custom particle"
```

### Task 24: Emit particles and sounds from handler events

**Files:**
- Modify: `common/shared/src/main/java/com/chronodawn/items/shield/ChronoShieldEffectHandler.java`

- [ ] **Step 1: Spawn stage-1 particles and play echo-generate sound inside `tryGenerateEcho`**

Inside `tryGenerateEcho`, after `data.setShieldEchoActiveUntil(...)`:

```java
        ServerLevel level = (ServerLevel) player.level();
        // Stage 1: ring burst in front of player
        for (int i = 0; i < 8; i++) {
            double angle = (Math.PI * 2.0 * i) / 8.0;
            double dx = Math.cos(angle) * 0.8;
            double dz = Math.sin(angle) * 0.8;
            level.sendParticles(com.chronodawn.registry.ModParticles.CHRONO_SHIELD_ECHO.get(),
                player.getX() + dx, player.getY() + 1.2, player.getZ() + dz,
                1, 0, 0, 0, 0);
        }
        level.playSound(null, player.blockPosition(),
            com.chronodawn.registry.ModSounds.SHIELD_ECHO_GENERATE.get(),
            net.minecraft.sounds.SoundSource.PLAYERS, 0.7f, 1.2f);
```

- [ ] **Step 2: Spawn stage-3 particles and play consume sound inside `tryConsumeEcho`** (before the `return true`)

```java
        ServerLevel level = (ServerLevel) player.level();
        for (int i = 0; i < 12; i++) {
            double dx = (level.getRandom().nextDouble() - 0.5) * 1.5;
            double dy = level.getRandom().nextDouble() * 1.0;
            double dz = (level.getRandom().nextDouble() - 0.5) * 1.5;
            level.sendParticles(com.chronodawn.registry.ModParticles.CHRONO_SHIELD_ECHO.get(),
                player.getX() + dx, player.getY() + dy, player.getZ() + dz,
                1, 0, 0.05, 0, 0.02);
        }
        level.playSound(null, player.blockPosition(),
            com.chronodawn.registry.ModSounds.SHIELD_ECHO_CONSUME.get(),
            net.minecraft.sounds.SoundSource.PLAYERS, 0.8f, 0.9f);
```

- [ ] **Step 3: Stage 2 (active pulse)** — This requires periodic emission during the 5-second window. Add a server tick listener in `ChronoDawnEvents` (or follow the existing pattern for periodic server tick logic) that iterates online players, checks `PlayerProgressData.getShieldEchoActiveUntil`, and emits one subtle particle + the active-pulse sound every 10 ticks.

In `common/1.21.11/src/main/java/com/chronodawn/events/ChronoDawnEvents.java` (or the equivalent event handler), add a server-tick hook:

```java
    public static void onServerTick(ServerLevel level) {
        long now = level.getGameTime();
        if (now % 10 != 0) return;
        PlayerProgressData data = PlayerProgressData.get(level);
        for (ServerPlayer sp : level.players()) {
            long activeUntil = data.getShieldEchoActiveUntil(sp.getUUID());
            if (activeUntil > now) {
                level.sendParticles(com.chronodawn.registry.ModParticles.CHRONO_SHIELD_ECHO.get(),
                    sp.getX(), sp.getY() + 1.0, sp.getZ(),
                    1, 0.3, 0.3, 0.3, 0.01);
            }
        }
    }
```

Wire it into the existing server tick dispatcher.

- [ ] **Step 4: Manual verification via `runClientFabric1_21_11`**

Launch client, enter world, get T3 shield, trigger block → observe stage 1 burst, stage 2 pulsing, stage 3 consume burst on auto-blocked hit.

- [ ] **Step 5: Commit**

```bash
git add common/shared/src/main/java/com/chronodawn/items/shield/ChronoShieldEffectHandler.java common/1.21.11/src/main/java/com/chronodawn/events/
git commit -m "feat(shields): 3-stage particle and sound FX for Time Echo"
```

---

## Phase 8: Recipes, Tags, Translations

### Task 25: Add recipe JSON for Clockstone Shield (reference version 1.21.11)

**Files:**
- Create: `common/shared/src/main/resources/data/chronodawn/recipe/clockstone_shield.json`

- [ ] **Step 1: Write the recipe**

```json
{
  "type": "minecraft:crafting_shaped",
  "pattern": [
    "WCW",
    "WWW",
    " W "
  ],
  "key": {
    "W": { "item": "chronodawn:time_wood_planks" },
    "C": { "item": "chronodawn:clockstone" }
  },
  "result": {
    "id": "chronodawn:clockstone_shield",
    "count": 1
  }
}
```

- [ ] **Step 2: Run `validateResources` and `RecipeValidationTest`**

Run: `./gradlew validateResources && ./gradlew :common-1.21.11:test -Ptarget_mc_version=1.21.11 --tests "com.chronodawn.unit.RecipeValidationTest"`
Expected: PASS

### Task 26: Add recipe JSON for Enhanced Clockstone Shield

**Files:**
- Create: `common/shared/src/main/resources/data/chronodawn/recipe/enhanced_clockstone_shield.json`

- [ ] **Step 1: Write the upgrade recipe**

```json
{
  "type": "minecraft:crafting_shaped",
  "pattern": [
    "WEW",
    "ETE",
    "WEW"
  ],
  "key": {
    "W": { "item": "chronodawn:dark_time_wood_planks" },
    "E": { "item": "chronodawn:enhanced_clockstone" },
    "T": { "item": "chronodawn:clockstone_shield" }
  },
  "result": {
    "id": "chronodawn:enhanced_clockstone_shield",
    "count": 1
  }
}
```

- [ ] **Step 2: Validate**

Run: `./gradlew validateResources`
Expected: PASS

### Task 27: Add recipe JSON for Entropy Crystal Shield

**Files:**
- Create: `common/shared/src/main/resources/data/chronodawn/recipe/entropy_crystal_shield.json`

- [ ] **Step 1: Write the upgrade recipe**

```json
{
  "type": "minecraft:crafting_shaped",
  "pattern": [
    "WXW",
    "XTX",
    "WXW"
  ],
  "key": {
    "W": { "item": "chronodawn:ancient_time_wood_planks" },
    "X": { "item": "chronodawn:entropy_crystal" },
    "T": { "item": "chronodawn:enhanced_clockstone_shield" }
  },
  "result": {
    "id": "chronodawn:entropy_crystal_shield",
    "count": 1
  }
}
```

- [ ] **Step 2: Validate and commit Tasks 25–27**

Run: `./gradlew validateResources`
Expected: PASS

```bash
git add common/shared/src/main/resources/data/chronodawn/recipe/clockstone_shield.json common/shared/src/main/resources/data/chronodawn/recipe/enhanced_clockstone_shield.json common/shared/src/main/resources/data/chronodawn/recipe/entropy_crystal_shield.json
git commit -m "feat(shields): add 3 crafting recipes with tier upgrade chain"
```

### Task 28: Add GameTest for recipe chain

**Files:**
- Create: `common/gametest/src/main/java/com/chronodawn/gametest/ShieldRecipeChainTest.java`

- [ ] **Step 1: Write the test asserting all 3 recipes are registered and produce the right output**

```java
package com.chronodawn.gametest;

import com.chronodawn.registry.ModItems;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.crafting.RecipeType;

public class ShieldRecipeChainTest {

    @GameTest(template = "chronodawn:empty_platform")
    public static void all_shield_recipes_registered(GameTestHelper helper) {
        var rm = helper.getLevel().getServer().getRecipeManager();
        for (String id : new String[] { "clockstone_shield", "enhanced_clockstone_shield", "entropy_crystal_shield" }) {
            var recipe = rm.byKey(ResourceLocation.fromNamespaceAndPath("chronodawn", id));
            helper.assertTrue(recipe.isPresent(), "Recipe " + id + " must be registered");
        }
        helper.succeed();
    }
}
```

- [ ] **Step 2: Run and verify**

Run: `./gradlew :fabric:runGameTest -Ptarget_mc_version=1.21.11 --tests "*all_shield_recipes_registered*"`
Expected: PASS

- [ ] **Step 3: Commit**

```bash
git add common/gametest/src/main/java/com/chronodawn/gametest/ShieldRecipeChainTest.java
git commit -m "test(shields): verify 3 recipes registered for tier upgrade chain"
```

### Task 29: Add shield tag entries

**Files:**
- Create or modify: `common/shared/src/main/resources/data/minecraft/tags/item/shields.json` (or `items/shields.json` for 1.20.1 — path differs)

- [ ] **Step 1: Add IDs to the shields tag**

```json
{
  "replace": false,
  "values": [
    "chronodawn:clockstone_shield",
    "chronodawn:enhanced_clockstone_shield",
    "chronodawn:entropy_crystal_shield"
  ]
}
```

- [ ] **Step 2: Run `TagValidationTest`**

Run: `./gradlew :common-1.21.11:test -Ptarget_mc_version=1.21.11 --tests "com.chronodawn.unit.TagValidationTest"`
Expected: PASS

- [ ] **Step 3: Commit**

```bash
git add common/shared/src/main/resources/data/minecraft/tags/
git commit -m "feat(shields): add 3 shields to minecraft:shields tag"
```

### Task 30: Add English and Japanese translations

**Files:**
- Modify: `common/shared/src/main/resources/assets/chronodawn/lang/en_us.json`
- Modify: `common/shared/src/main/resources/assets/chronodawn/lang/ja_jp.json`

- [ ] **Step 1: Add English entries using `Edit` (line-based to preserve blank-line section groupers)**

Add near the existing armor item entries:

```
  "item.chronodawn.clockstone_shield": "Clockstone Shield",
  "item.chronodawn.clockstone_shield.tooltip.1": "Reduces Slowness/Weakness/Mining Fatigue duration while blocking",
  "item.chronodawn.clockstone_shield.tooltip.2": "Raises to block in 3 ticks (vanilla: 5)",
  "item.chronodawn.enhanced_clockstone_shield": "Enhanced Clockstone Shield",
  "item.chronodawn.enhanced_clockstone_shield.tooltip.1": "All T1 effects",
  "item.chronodawn.enhanced_clockstone_shield.tooltip.2": "Speed I on successful block (3s cooldown)",
  "item.chronodawn.entropy_crystal_shield": "Entropy Crystal Shield",
  "item.chronodawn.entropy_crystal_shield.tooltip.1": "All T2 effects",
  "item.chronodawn.entropy_crystal_shield.tooltip.2": "Time Echo: auto-blocks next hit within 5s (30s cooldown)",
```

- [ ] **Step 2: Add Japanese entries**

```
  "item.chronodawn.clockstone_shield": "クロックストーンの盾",
  "item.chronodawn.clockstone_shield.tooltip.1": "盾構え中の鈍化・弱化・採掘速度低下の効果時間を短縮",
  "item.chronodawn.clockstone_shield.tooltip.2": "構えてからブロック判定まで3tick（バニラは5tick）",
  "item.chronodawn.enhanced_clockstone_shield": "強化クロックストーンの盾",
  "item.chronodawn.enhanced_clockstone_shield.tooltip.1": "T1効果すべて",
  "item.chronodawn.enhanced_clockstone_shield.tooltip.2": "ブロック成功時に移動速度上昇I（クールダウン3秒）",
  "item.chronodawn.entropy_crystal_shield": "エントロピークリスタルの盾",
  "item.chronodawn.entropy_crystal_shield.tooltip.1": "T2効果すべて",
  "item.chronodawn.entropy_crystal_shield.tooltip.2": "タイムエコー：5秒以内の次の1撃を自動でブロック（クールダウン30秒）",
```

- [ ] **Step 3: Implement the tooltip display in each shield class**

Override `appendHoverText` on the three shield items to display the tier-appropriate tooltip lines. Example on `ClockstoneShieldItem`:

```java
    @Override
    public void appendHoverText(ItemStack stack, TooltipContext ctx, TooltipDisplay tooltipDisplay, java.util.function.Consumer<Component> consumer, TooltipFlag flag) {
        consumer.accept(Component.translatable("item.chronodawn.clockstone_shield.tooltip.1"));
        consumer.accept(Component.translatable("item.chronodawn.clockstone_shield.tooltip.2"));
    }
```

**Note:** The `appendHoverText` signature varies by version. In 1.20.1 it takes `(ItemStack, Level, List<Component>, TooltipFlag)`. Adjust per version in Phase 9.

- [ ] **Step 4: Run validation**

Run: `./gradlew validateTranslations`
Expected: PASS

- [ ] **Step 5: Commit**

```bash
git add common/shared/src/main/resources/assets/chronodawn/lang/ common/1.21.11/src/main/java/com/chronodawn/items/
git commit -m "feat(shields): add en_us/ja_jp translations and tooltip rendering"
```

---

## Phase 9: Cross-Version Porting

The reference version (1.21.11) is complete. This phase ports the same structure to each other supported MC version. Before starting, review the porting delta list at the top of this plan.

### Task 31: Port to MC 1.21.10

**Files:**
- Create: `common/1.21.10/src/main/java/com/chronodawn/items/{Clockstone,EnhancedClockstone,EntropyCrystal}ShieldItem.java`
- Create: `common/1.21.10/src/main/java/com/chronodawn/mixin/{ChronoShieldBlockingMixin,ChronoShieldDamageMixin,ChronoShieldEffectInterceptorMixin}.java`
- Modify: `common/1.21.10/src/main/java/com/chronodawn/registry/ModItems.java`
- Modify: `fabric/1.21.10/src/main/resources/chronodawn-fabric.mixins.json`
- Modify: `neoforge/1.21.10/src/main/resources/chronodawn-neoforge.mixins.json`

- [ ] **Step 1: Copy the three shield item classes from `common/1.21.11/.../items/` to `common/1.21.10/.../items/`.** Verify signatures compile (API should match 1.21.11 for items).

- [ ] **Step 2: Copy the three Mixin classes from `common/1.21.11/.../mixin/` to `common/1.21.10/.../mixin/`.** Check the `submit()` render path differences mentioned in the porting-delta list.

- [ ] **Step 3: Replicate `ModItems.java` shield registration and creative tab entries (same code, different file).**

- [ ] **Step 4: Add mixin entries to `chronodawn-fabric.mixins.json` (with refMap in Fabric only) and `chronodawn-neoforge.mixins.json`.**

- [ ] **Step 5: Run per-version build and tests**

Run: `./gradlew :common-1.21.10:build -Ptarget_mc_version=1.21.10 && ./gradlew :fabric:runGameTest -Ptarget_mc_version=1.21.10`
Expected: BUILD SUCCESSFUL + GameTests pass.

- [ ] **Step 6: Commit**

```bash
git add common/1.21.10/src/main/java/com/chronodawn/items/ common/1.21.10/src/main/java/com/chronodawn/mixin/ common/1.21.10/src/main/java/com/chronodawn/registry/ModItems.java fabric/1.21.10/src/main/resources/chronodawn-fabric.mixins.json neoforge/1.21.10/src/main/resources/chronodawn-neoforge.mixins.json
git commit -m "feat(shields): port custom shields to MC 1.21.10"
```

### Task 32: Port to MC 1.21.9

Same structure as Task 31. Watch for `submit()` API change boundary at 1.21.9+ (armor texture concerns don't apply to shields, but confirm no render breakage). Follow the same 6-step sequence.

- [ ] Port files
- [ ] Verify `./gradlew :common-1.21.9:build -Ptarget_mc_version=1.21.9`
- [ ] Run `./gradlew :fabric:runGameTest -Ptarget_mc_version=1.21.9`
- [ ] Commit: `feat(shields): port custom shields to MC 1.21.9`

### Task 33: Port to MC 1.21.8

Same structure. The `Player.hurt` signature split at 1.21.8 may require changing the Mixin target from `hurt(DamageSource, float)` to `hurtServer(ServerLevel, DamageSource, float)`. Check `LevelGetTimeMixin` in the same version folder for prior-art of this API boundary.

- [ ] Port files, adjusting `ChronoShieldDamageMixin` target method signature as needed
- [ ] Verify build and tests for 1.21.8
- [ ] Commit: `feat(shields): port custom shields to MC 1.21.8`

### Task 34: Port to MC 1.21.7

Same structure. API should largely match 1.21.8. Quick port.

- [ ] Port, build, test, commit as `feat(shields): port custom shields to MC 1.21.7`

### Task 35: Port to MC 1.21.6

Same structure. Quick port.

- [ ] Port, build, test, commit as `feat(shields): port custom shields to MC 1.21.6`

### Task 36: Port to MC 1.21.5

Same structure. 1.21.5 is a significant API boundary (armor, equipment builder) but shields should be unaffected since they are off-hand items. Quick port.

- [ ] Port, build, test, commit as `feat(shields): port custom shields to MC 1.21.5`

### Task 37: Port to MC 1.21.4

Same structure. Spawn-egg JSON format boundary (doesn't apply here). Quick port.

- [ ] Port, build, test, commit as `feat(shields): port custom shields to MC 1.21.4`

### Task 38: Port to MC 1.21.2 (also covers 1.21.3 — shares modules)

Same structure. 1.21.2 is the `setId()` boundary, but our reference already uses it, so the code copies over. Watch recipe JSON format: 1.21.2 result uses `id`, which we already use.

- [ ] Port, build, test for both 1.21.2 and 1.21.3 runtime (`./gradlew :fabric:runGameTest -Ptarget_mc_version=1.21.3`)
- [ ] Commit: `feat(shields): port custom shields to MC 1.21.2/1.21.3`

### Task 39: Port to MC 1.21.1

Recipe JSON format: `result.id` may need to be `result.item`. Verify against an existing shield-equivalent recipe in `common/1.21.1` (e.g., armor recipes).

MobEffects API: 1.21.1 uses `Holder<MobEffect>` (1.21+). Code should port mostly unchanged.

- [ ] Port, adjust recipe JSONs if needed, build, test
- [ ] Commit: `feat(shields): port custom shields to MC 1.21.1`

### Task 40: Port to MC 1.20.1

This is the biggest porting delta:

- `Item.Properties` does NOT have `.setId()` — remove from `ModItems.java` registrations for this version
- `ShieldItem` constructor signature differs (no setId requirement)
- `MobEffectInstance` uses raw `MobEffect`, not `Holder` — adjust `maybeShortenDebuff`, `onBlockSuccess` in the shared handler by feature-gating on `MinecraftVersion.isAtLeast(MC_1_21_1)` or providing a 1.20.1-specific helper class
- Recipe JSON uses `result.item` not `result.id`
- Tag path is `data/minecraft/tags/items/shields.json` (plural)
- `appendHoverText` signature is `(ItemStack, Level, List<Component>, TooltipFlag)` — add items the old way
- Mixin `Player.hurt` signature matches pre-Server-split

- [ ] Create `common/1.20.1/.../items/ClockstoneShieldItem.java` etc. with 1.20.1 API (no setId, no rarity property-chaining if API differs)
- [ ] Create `common/1.20.1/.../mixin/` three files adjusted for 1.20.1 signatures
- [ ] Modify `common/1.20.1/.../registry/ModItems.java`
- [ ] Adjust the shared handler to handle 1.20.1 MobEffect path — consider extracting a small compat interface if branching in one class becomes messy
- [ ] Replicate recipe JSONs with `result.item` form under `common/1.20.1/src/main/resources/data/chronodawn/recipe/`
- [ ] Replicate tag JSON under `items/` (plural)
- [ ] Replicate translation lang entries if they live per version
- [ ] Update fabric/neoforge mixin configs
- [ ] Run full build + tests for 1.20.1: `./gradlew :common-1.20.1:build -Ptarget_mc_version=1.20.1 && ./gradlew :fabric:runGameTest -Ptarget_mc_version=1.20.1`
- [ ] Commit: `feat(shields): port custom shields to MC 1.20.1`

---

## Phase 10: Final Art, Docs, and Release Verification

### Task 41: Create final item textures (16×16) for all 3 shields

**Files:**
- Replace: `common/shared/src/main/resources/assets/chronodawn/textures/item/{clockstone,enhanced_clockstone,entropy_crystal}_shield.png`
- Create: `common/shared/src/main/resources/assets/chronodawn/textures/item/entropy_crystal_shield_emissive.png` (T3 emissive overlay)

- [ ] **Step 1: Design final textures**

Follow the design:
- T1 Clockstone Shield: yellow/gold face with Time Wood frame, single clock hand / gear in center
- T2 Enhanced Clockstone Shield: orange face with Dark Time Wood frame, twin stacked gears
- T3 Entropy Crystal Shield: blue-violet face with Ancient Time Wood frame, fractured gear exposing crystal; separate `_emissive.png` for the crystal portion

- [ ] **Step 2: Run manual verification**

Launch `runClientFabric1_21_11`, confirm inventory icons match the design.

- [ ] **Step 3: Commit**

```bash
git add common/shared/src/main/resources/assets/chronodawn/textures/item/
git commit -m "art(shields): finalize shield item textures for 3 tiers"
```

### Task 42: Create 3D in-hand models for all 3 shields (idle and blocking poses)

**Files:**
- Create: `common/shared/src/main/resources/assets/chronodawn/models/item/{clockstone,enhanced_clockstone,entropy_crystal}_shield_blocking.json`
- Update: `common/shared/src/main/resources/assets/chronodawn/models/item/{clockstone,enhanced_clockstone,entropy_crystal}_shield.json` (with `overrides` for blocking state)

- [ ] **Step 1: Author 3D blocking models**

Use a 3D block modeler (e.g., Blockbench) to create shield-shaped models resembling Undergarden/Blue Skies custom shields. Use the texture atlas from Task 41. One model per tier, one blocking variant.

- [ ] **Step 2: Wire up blocking override in the idle model**

Example for `clockstone_shield.json`:

```json
{
  "parent": "minecraft:item/handheld",
  "textures": { "layer0": "chronodawn:item/clockstone_shield" },
  "overrides": [
    {
      "predicate": { "blocking": 1 },
      "model": "chronodawn:item/clockstone_shield_blocking"
    }
  ]
}
```

- [ ] **Step 3: Implement emissive render layer for T3 crystal**

Follow the existing `feedback_emissive_layer_pattern` memory — use `RenderType.eyes()` + the transparent `_emissive.png` overlay. Locate the client-side item model renderer and hook into it for `EntropyCrystalShieldItem` specifically.

- [ ] **Step 4: Manual verification**

Launch client, hold each shield, right-click to block, verify 3D model and emissive glow on T3.

- [ ] **Step 5: Commit**

```bash
git add common/shared/src/main/resources/assets/chronodawn/models/item/
git commit -m "art(shields): add 3D blocking models and T3 emissive layer"
```

### Task 43: Add final sound files

- [ ] **Step 1: Produce or license 3 short OGG clips for echo_generate (clockwork click), echo_active_pulse (subtle tick), echo_consume (reverse echo).** Ensure LGPL-compatible licensing per `license-compliance` skill.

- [ ] **Step 2: Replace placeholder files in `common/shared/src/main/resources/assets/chronodawn/sounds/item/shield/`**

- [ ] **Step 3: Manual audio verification in `runClient*`**

- [ ] **Step 4: Commit**

```bash
git add common/shared/src/main/resources/assets/chronodawn/sounds/item/shield/
git commit -m "art(shields): finalize echo sound effects"
```

### Task 44: Update `docs/player_guide.md`

**Files:**
- Modify: `docs/player_guide.md`

- [ ] **Step 1: Add a new "Custom Shields" subsection after the Armor section**

```markdown
### Custom Shields

ChronoDawn adds three tiered shields optimized for the dimension's time-themed threats. All tiers raise faster (3 ticks vs vanilla's 5) and reduce Slowness/Weakness/Mining Fatigue duration by 50% while blocking.

- **Clockstone Shield** (T1): 400 durability
- **Enhanced Clockstone Shield** (T2): 600 durability. Adds Speed I on successful block (3s cooldown)
- **Entropy Crystal Shield** (T3): 800 durability. Adds Time Echo — auto-blocks the next incoming hit within 5 seconds (30s cooldown)

T2 and T3 are crafted by upgrading the previous tier in a crafting table, following a ring pattern. See the Recipes section or your in-game recipe book for details.

These custom shields are independent of the existing Chrono Aegis artifact.
```

- [ ] **Step 2: Commit**

```bash
git add docs/player_guide.md
git commit -m "docs(shields): document custom shields in player guide"
```

### Task 45: Update `docs/developer_guide.md`

**Files:**
- Modify: `docs/developer_guide.md`

- [ ] **Step 1: Add a "Custom Shields Architecture" subsection**

```markdown
### Custom Shields

3 tiered `ShieldItem` subclasses exist per MC version in `common/{ver}/src/main/java/com/chronodawn/items/`. Shared tier data and effect logic live in `common/shared/src/main/java/com/chronodawn/items/shield/` (`ChronoShieldTier`, `ChronoShieldMarker`, `ChronoShieldEffectHandler`).

Behavioral hooks are implemented as three per-version Mixins:
- `ChronoShieldBlockingMixin` — modifies the 5-tick raise constant and hooks block success
- `ChronoShieldDamageMixin` — injects into Player.hurt to consume Time Echo
- `ChronoShieldEffectInterceptorMixin` — halves time-themed debuff durations when blocking

Player-bound cooldown state lives in `PlayerProgressData`:
- `shieldSpeedCooldownEndTick` — Speed-on-block CD
- `shieldEchoActiveUntilTick` — Time Echo active window
- `shieldEchoCooldownEndTick` — Time Echo CD

State is player-bound (not ItemStack NBT) to prevent hot-swap exploits and to avoid NBT↔DataComponent cross-version complexity.
```

- [ ] **Step 2: Commit**

```bash
git add docs/developer_guide.md
git commit -m "docs(shields): document custom shield architecture in developer guide"
```

### Task 46: Update marketplace descriptions

**Files:**
- Modify: `docs/curseforge_description.md`
- Modify: `docs/modrinth_description.md`

- [ ] **Step 1: Add a bullet to the Features section in both files**

```markdown
- **ChronoDawn Custom Shields** (3 tiers): Clockstone / Enhanced Clockstone / Entropy Crystal. Each tier adds a time-manipulation effect on top of shared debuff resistance — faster raise, Speed on block, and a 5-second Time Echo auto-block.
```

- [ ] **Step 2: Commit**

```bash
git add docs/curseforge_description.md docs/modrinth_description.md
git commit -m "docs(shields): add shields to marketplace descriptions"
```

### Task 47: Update CHANGELOG.md

**Files:**
- Modify: `CHANGELOG.md`

- [ ] **Step 1: Add entry to the Unreleased (or next-version) section**

```markdown
### Added
- ChronoDawn Custom Shields: three tiered shields (Clockstone, Enhanced Clockstone, Entropy Crystal) with cumulative time-manipulation effects. Crafted via an upgrade chain from Time Wood planks and temporal materials. Independent from the existing Chrono Aegis artifact.
```

- [ ] **Step 2: Commit**

```bash
git add CHANGELOG.md
git commit -m "docs(shields): add changelog entry"
```

### Task 48: Full `./gradlew checkAll`

- [ ] **Step 1: Run full verification**

Run: `./gradlew checkAll`
Expected: all phases pass (cleanAll → validateResources → validateTranslations → buildAll → testAll → gameTestAll).

- [ ] **Step 2: Fix any regressions surfaced**

Any failures: debug per the `build-troubleshooting` and `audit-performance-thread-safety` skills. Do NOT commit the fix alongside the final checkAll — commit fixes separately.

- [ ] **Step 3: Commit any fixes as `fix(shields): <specific issue>`**

### Task 49: Final review against spec

- [ ] **Step 1: Open `docs/superpowers/specs/2026-04-19-chronodawn-custom-shields-design.md` and walk through each numbered section, verifying the corresponding implementation exists**

- [ ] **Step 2: Update `.claude/tasks.local.md` in this worktree to mark all phases complete**

- [ ] **Step 3: Prepare PR description**

The branch is ready to merge. Use `superpowers:finishing-a-development-branch` skill (or `merge-worktree` / `commit-commands:commit-push-pr`) to finalize.
