# Implementation History

This document archives technical insights and trial-and-error records obtained during the implementation of the Chrono Dawn mod.

---

## Ancient Ruins Structure Placement - Trial and Error Log

**Implementation Date**: Early 2025
**Purpose**: Optimize Ancient Ruins placement configuration

### Requirements
- Placement distance: 700-1500 blocks (equivalent to End Stronghold, for adventure start)
- Placement count: Only 1 per world (special feeling)
- Placement biome: Forest biomes (tree-rich)
- Strange Leaves effect: Convert surrounding leaves to blue-purple color (improved visibility)
- /locate command: Should be usable
- World regeneration: Not available as a solution

### Trial 1: concentric_rings (Standard Placement)

**Configuration:**
```json
{
  "type": "minecraft:concentric_rings",
  "distance": 69,
  "spread": 25,
  "count": 128,
  "preferred_biomes": "#chronodawn:has_ancient_ruins"
}
```

**Problems:**
- Ancient Ruins spawn too far away at 2500-3000 blocks
- Cases where Strange Forest exists but no Ancient Ruins inside
- Increasing Strange Forest frequency is "putting the cart before the horse" (defeats the hint purpose)

**Conclusion:** ❌ Rejected - Insufficient distance control, weak biome coordination

---

### Trial 2: RangedSingleStructurePlacement (Custom Placement - Single Candidate)

**Configuration:**
- Custom StructurePlacement implementation
- Deterministically generate one coordinate from world seed
- min_distance=700, max_distance=1500

**Problem 1: max_distance_from_center limitation**
```
Caused by: java.lang.IllegalStateException: Value 450 outside of range [1:128]
```
- Minecraft limitation: max_distance_from_center ≤ 128 blocks
- SEARCH_RADIUS adjustment: 400→120→80 blocks

**Problem 2: Combination with terrain_adaptation**
```
Caused by: java.lang.IllegalStateException: Structure size including terrain adaptation must not exceed 128
```
- Solution: terrain_adaptation = "none"

**Problem 3: Biome constraints**
- Cases where chosen coordinates happen to be in "ocean" biome
- Structure generates but Strange Leaves effect is invisible due to no surrounding leaves

**Conclusion:** △ Partially functional - Generates but depends on biome luck

---

### Trial 3: RangedSingleStructurePlacement (Multiple Candidates System)

**Configuration:**
- Generate 500→2000 candidate coordinates
- Check biomes in order, place at first matching position
- Biomes: Forest, taiga, plains, etc.

**Problems:**
- `isPlacementChunk` is only called during chunk generation
- Candidates are not checked unless player explores
- Logs output massively, but structure placement not confirmed

**Conclusion:** ❌ Rejected - Chunk generation timing issue, low practicality

---

### Trial 4: concentric_rings Reconsidered (Standard Placement - Parameter Adjustment)

**Configuration:**
```json
{
  "type": "minecraft:concentric_rings",
  "distance": 44,
  "spread": 25,
  "count": 3,
  "preferred_biomes": "#chronodawn:has_ancient_ruins"
}
```

**Problems (Expected):**
- `count: 3` = Places in 3 locations (requirement: only 1 location)
- `preferred_biomes` is "preferred" not "required"
- Same problems as Trial 1 likely to recur

**Conclusion:** ⏸️ On hold - Trial 1 problems not resolved

---

### Trial 5: concentric_rings + StrangeLeavesProcessor (Final Implementation Candidate)

**Configuration:**
```json
{
  "type": "minecraft:concentric_rings",
  "distance": 44,
  "spread": 25,
  "count": 3,
  "preferred_biomes": "#chronodawn:has_ancient_ruins"
}
```

**Biome Tag (has_ancient_ruins):**
```json
{
  "values": [
    "#minecraft:is_forest",
    "#minecraft:is_taiga",
    "minecraft:plains",
    "minecraft:sunflower_plains"
  ]
}
```

**Important Realization: Strange Forest Biome Itself is Unnecessary**
- `StrangeLeavesProcessor` converts leaves within 80 blocks around structure to blue-purple
- "Strange Forest effect" emerges regardless of which biome it's placed in
- Biome definition only constrains placement location (tree-rich areas)

**Advantages:**
- ✓ /locate structure chronodawn:ancient_ruins is usable
- ✓ Places in forest, taiga, plains (tree-rich)
- ✓ Surrounding 80 blocks turn blue-purple (visibility)
- ✓ 700-1500 block range (3 rings: 700/1100/1500)
- ✓ Implemented using same mechanism as End Stronghold

**Constraints:**
- max_distance_from_center = 120 blocks
- SEARCH_RADIUS = 80 blocks (safety margin)
- terrain_adaptation = "none"

**Conclusion:** ✅ Adoption candidate

---

### Trial 6: random_spread (Final Implementation)

**Configuration:**
```json
{
  "type": "minecraft:random_spread",
  "spacing": 48,
  "separation": 24,
  "salt": 20005897
}
```

**Structure Definition:**
```json
{
  "terrain_adaptation": "beard_thin",
  "max_distance_from_center": 80,
  "biomes": "#chronodawn:has_ancient_ruins"
}
```

**Biome Tag (has_ancient_ruins):**
```json
{
  "values": [
    "#minecraft:is_forest",
    "#minecraft:is_taiga"
  ]
}
```

**Advantages:**
- ✓ Stable distance: Minimum 384 blocks, average 500-1000 blocks
- ✓ /locate structure chronodawn:ancient_ruins is usable
- ✓ Places only in forest/taiga (tree-rich)
- ✓ terrain_adaptation="beard_thin" blends smoothly with terrain
- ✓ Distance is stable even with biome restrictions

**Constraints:**
- spacing=48 (768 blocks), so multiple instances may generate
- Not guaranteed single placement like concentric_rings

**Conclusion:** ✅ Adopted - Balance between distance stability and biome restrictions

---

### Current Status
- **Implementation**: random_spread (spacing=48, separation=24)
- **Placement**: Forest/taiga only, minimum 384 blocks, average 500-1000 blocks
- **Terrain Adaptation**: beard_thin (smooth placement even on slopes)
- **Command**: /locate structure chronodawn:ancient_ruins functional
- **Next Steps**: Add tall tower to improve visibility

---

### Technical Constraints (Important)
- **max_distance_from_center ≤ 128 blocks** - Minecraft's hard-coded limitation
- **terrain_adaptation is included in constraint** - Structure size + terrain_adaptation total must be ≤ 128
- **SEARCH_RADIUS (StrangeLeavesProcessor) ≤ 80 blocks is safe** - Considering 128 limit
- **Biome tags are evaluated at runtime** - Pre-calculation is not possible

---

## Additional Bosses (T234-T238) - Implementation Record

**Implementation Date**: 2025-11-21～2025-11-22
**Branch**: T234-238-additional-bosses
**Purpose**: Implement 4 mini-bosses and Chrono Aegis system as preparation for Time Tyrant battle

### ✅ Completed Implementation

#### 1. Four Mini-Boss Entities

**Chronos Warden (T234a-i):**
- **HP**: 180 (90 hearts)
- **Mechanics**: Stone Stance (damage reduction), Ground Slam (knockback AoE)
- **Drops**: Guardian Stone (1-2) + Enhanced Clockstone (2-4)
- **Files**: ChronosWardenEntity.java, GuardianStoneItem.java, ChronosWardenRenderer.java
- **Visual**: Uses TimeGuardianModel with custom texture

**Clockwork Colossus (T235a-l):**
- **HP**: 200 (100 hearts)
- **Mechanics**: Gear Shot (ranged), Overcharge (attack power boost), Repair Protocol (HP recovery), Ground Slam
- **Drops**: Colossus Gear (1-2) + Enhanced Clockstone (2-4)
- **Files**: ClockworkColossusEntity.java, GearProjectileEntity.java, ColossusGearItem.java
- **Visual**: Uses TimeGuardianModel with custom texture

**Temporal Phantom (T236a-l):**
- **HP**: 150 (75 hearts)
- **Mechanics**: Phase Shift (30% evasion), Warp Bolt (ranged magic), Phantom Clone (summon), Blink Strike
- **Drops**: Phantom Essence (1-2) + Enhanced Clockstone (2-4)
- **Files**: TemporalPhantomEntity.java, PhantomEssenceItem.java, TemporalPhantomRenderer.java
- **Visual**: Uses TimeGuardianModel with custom texture

**Entropy Keeper (T237a-m):**
- **HP**: 160 (80 hearts)
- **Mechanics**: Decay Aura (Wither I), Corrosion Touch (durability damage), Temporal Rot (decay patches), Degradation (attack power +2 every 60s), Entropy Burst (one-time explosion)
- **Drops**: Entropy Core (1-2) + Enhanced Clockstone (2-4)
- **Files**: EntropyKeeperEntity.java, EntropyCoreItem.java, EntropyKeeperRenderer.java
- **Visual**: Uses TimeGuardianModel with custom texture

---

#### 2. Chrono Aegis System (T238a-l)

**Chrono Aegis Item:**
- **Type**: Consumable item (single-use)
- **Crafting**: Shapeless recipe using 4 boss drops
  ```
  Guardian Stone + Phantom Essence + Colossus Gear + Entropy Core → Chrono Aegis (1)
  ```
- **Effect**: 10-minute buff (12000 ticks)
- **Visual**: Epic rarity + enchantment glint
- **Files**: ChronoAegisItem.java, chrono_aegis.json (recipe)

**Chrono Aegis Buff Effect:**
- **Registry**: ModEffects.CHRONO_AEGIS_BUFF
- **Color**: Royal Blue (0x4169E1)
- **Icon**: mob_effect/chrono_aegis_buff.png
- **Files**: ChronoAegisEffect.java, ModEffects.java

**Time Tyrant Integration (T238g-j):**

1. **Time Stop Resistance** - Mitigates Time Stop debuff: Slowness V → Slowness II
2. **Dimensional Anchor** - After teleport, prevents next teleport for 3 seconds
3. **Temporal Shield** - Reduces AoE damage by 50% (12 → 6 damage)
4. **Time Reversal Disruption** - Reduces HP recovery: 10% → 5% of max HP
5. **Clarity Auto-Cleanse** - Removes Slowness/Weakness/Mining Fatigue every 2 seconds

**Multiplayer Protection:**
- Debuff flags prevent stacking from multiple Chrono Aegis players
- `hasNearbyChronoAegisPlayer()` checks 32-block radius
- State saved to NBT for save/load compatibility

**Clarity Implementation (T240):**
- Uses event-based system in EntityEventHandler
- Uses `PLAYER_POST` tick event instead of `MobEffect.applyEffectTick()`
- Cleansing every 40 ticks (2 seconds)
- Avoids ConcurrentModificationException during NBT save

---

### 🔧 Technical Decisions

#### 1. Model Reuse Strategy
**Decision**: All bosses use TimeGuardianModel with different textures
**Reason**: Rapid development, consistent size/hitbox, replaceable later
**Future**: Can create custom Blockbench models for each boss (T242)

#### 2. Holder<MobEffect> Conversion
**Problem**: Minecraft 1.21.1 requires `Holder<MobEffect>` for effect checks
**Solution**: Use `BuiltInRegistries.MOB_EFFECT.wrapAsHolder(effect)`
**Applied**: ChronoAegisItem.java, TimeTyrantEntity.java

#### 3. Effect Application Safety
**Problem**: `removeEffect()` in `applyEffectTick()` causes NBT save crash
**Solution**: Disabled Clarity feature, implemented event-based removal
**Lesson**: Don't modify effect list during iteration in applyEffectTick()

#### 4. Naming Consistency Fix
**Problem**: Temporal Phantom had `_boss` suffix in registry ID
**Fix**: Renamed `temporal_phantom_boss` → `temporal_phantom`
**Applied**: ModEntities.java, loot table filenames, translations

---

### 📊 Implementation Statistics

- **Files Created**: 30+
- **Files Modified**: 15+
- **Lines of Code**: ~3000+
- **Commits**: 6
- **Build Status**: ✅ Success
- **Test Status**: ⚠️ Manual testing only (automated tests pending)

---

### 🎮 Game Balance

#### Boss Difficulty Comparison
| Boss | HP | Attack | Defense | Speed | Difficulty |
|------|-----|--------|---------|-------|------------|
| Chronos Warden | 180 | 9 | 12 | 0.15 | Medium (Tank) |
| Clockwork Colossus | 200 | 12 | 8 | 0.18 | Medium-High (Balanced) |
| Temporal Phantom | 150 | 8 | 5 | 0.25 | Medium (Evasion) |
| Entropy Keeper | 160 | 10 (max 16) | 6 | 0.20 | High (DoT/Burst) |

#### Chrono Aegis Impact
- **Without**: Time Tyrant battle is extremely difficult (Slowness V, frequent teleports, high damage)
- **With**: Battle becomes manageable (Slowness II, limited teleports, mitigated damage)
- **Design Goal**: Achievable without Chrono Aegis, significantly easier with it

---

### 📝 Known Issues

1. **Boss Visual Similarity**
   - Cause: All bosses use TimeGuardianModel
   - Impact: Limited visual variety
   - Fix: Create custom models (future work)

2. **Boss Spawn Structures Not Implemented**
   - Cause: Not yet implemented
   - Impact: Bosses cannot spawn naturally
   - Fix: Implement in T239

---

### 🔗 Related Files

**Entity Classes:**
- `common/src/main/java/com/chronodawn/entities/bosses/ChronosWardenEntity.java`
- `common/src/main/java/com/chronodawn/entities/bosses/ClockworkColossusEntity.java`
- `common/src/main/java/com/chronodawn/entities/bosses/TemporalPhantomEntity.java`
- `common/src/main/java/com/chronodawn/entities/bosses/EntropyKeeperEntity.java`
- `common/src/main/java/com/chronodawn/entities/bosses/TimeTyrantEntity.java` (modified)

**Item Classes:**
- `common/src/main/java/com/chronodawn/items/GuardianStoneItem.java`
- `common/src/main/java/com/chronodawn/items/ColossusGearItem.java`
- `common/src/main/java/com/chronodawn/items/PhantomEssenceItem.java`
- `common/src/main/java/com/chronodawn/items/EntropyCoreItem.java`
- `common/src/main/java/com/chronodawn/items/ChronoAegisItem.java`

**Registry Classes:**
- `common/src/main/java/com/chronodawn/registry/ModEntities.java`
- `common/src/main/java/com/chronodawn/registry/ModItems.java`
- `common/src/main/java/com/chronodawn/registry/ModEffects.java` (new)

**Effect Classes:**
- `common/src/main/java/com/chronodawn/effects/ChronoAegisEffect.java` (new)

**Event Handlers:**
- `common/src/main/java/com/chronodawn/events/EntityEventHandler.java` (modified for Clarity)

---

## Desert Clock Tower Implementation

**Implementation Date**: 2025-11-02
**Tasks**: T093-T095, T099
**Purpose**: Add structure to obtain Enhanced Clockstone

### Completed Tasks

**T093: Desert Clock Tower structure NBT and JSON configuration**
- Created template pool JSON: `/common/src/main/resources/data/chronodawn/worldgen/template_pool/desert_clock_tower/start_pool.json`
- Created processor list JSON: `/common/src/main/resources/data/chronodawn/worldgen/processor_list/desert_clock_tower_loot.json`
- Created placeholder NBT: `/common/src/main/resources/data/chronodawn/structure/desert_clock_tower.nbt`
  - ⚠️ **Important**: Currently using ancient_ruins.nbt as placeholder
  - **TODO**: Create actual tower structure using Structure Block in-game

**T094: Desert Clock Tower structure feature**
- Created structure JSON: `/common/src/main/resources/data/chronodawn/worldgen/structure/desert_clock_tower.json`
- Configuration:
  - Type: `minecraft:jigsaw`
  - Biome: `chronodawn:chronodawn_plains`
  - Terrain adaptation: `beard_thin`
  - Start height: `absolute: 0`

**T095: Desert Clock Tower structure set**
- Created structure set JSON: `/common/src/main/resources/data/chronodawn/worldgen/structure_set/desert_clock_tower.json`
- Placement configuration:
  - Type: `minecraft:random_spread`
  - Salt: `1663542342`
  - Spacing: `20` (rarer than ancient_ruins spacing of 16)
  - Separation: `8`

**T099: Enhanced Clockstone loot configuration**
- Created loot table: `/common/src/main/resources/data/chronodawn/loot_table/chests/desert_clock_tower.json`
- Loot contents:
  - **Pool 1** (guaranteed): Enhanced Clockstone x4-8
  - **Pool 2** (guaranteed): Clockstone x8-16
  - **Pool 3** (2-4 random items):
    - Iron Ingot x2-6 (weight: 10)
    - Gold Ingot x2-5 (weight: 8)
    - Diamond x1-3 (weight: 5)
    - Fruit of Time x4-8 (weight: 12)
    - Torch x8-16 (weight: 10)

### Design Notes

- **Placement Strategy**: Rarer than ancient_ruins (spacing 20 vs 16) to make Enhanced Clockstone more valuable
- **Loot Balance**: Guaranteed Enhanced Clockstone drops (4-8) provide sufficient materials for time manipulation items
- **Biome Restriction**: Currently chronodawn_plains only - can expand to other chronodawn biomes in future
- **Structure Adaptation**: Uses `beard_thin` for natural terrain blending

### Next Steps (TODO)

**High Priority:**
1. Create actual NBT structure (T095a)
   - Use Minecraft in-game Structure Block
   - Recommended specifications:
     - Size: 15x30x15 blocks (tall tower design)
     - Materials: Sandstone-based blocks (smooth sandstone, cut sandstone, chiseled sandstone)
     - Features:
       - Clock tower aesthetic (vertical tower with clock face decorations)
       - Multiple floors/levels
       - Strategically placed chests
       - Decorative elements (stairs, slabs, fences, etc. for detail)
     - Desert theme matching "Desert Clock Tower" name

**Medium Priority:**
2. Test structure generation in-game
3. Visual verification

**Optional Improvements:**
4. Consider adding variants (future)

---

*Last Updated: 2025-12-27*
