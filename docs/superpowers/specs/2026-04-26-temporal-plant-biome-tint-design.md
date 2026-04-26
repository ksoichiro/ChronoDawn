# Temporal Plant Biome Tint — Design

Date: 2026-04-26
Scope: `TemporalTallGrassBlock`, `TemporalFernBlock`

## Background

`TemporalTallGrassBlock` and `TemporalFernBlock` originally inherited from `minecraft:block/cross`, which has no `tintindex` on its element faces (it's the parent vanilla flowers like dandelion / poppy use precisely because they don't tint). To make a `BlockColor` provider effective the model parent must be `minecraft:block/tinted_cross` instead — the same parent vanilla `tall_grass` / `fern` / `short_grass` / `large_fern` use. This rewrite covers both halves: registering a `BlockColor` provider AND migrating the model parent so the provider's return value is actually multiplied into the texel.

The textures (`temporal_fern.png`, `temporal_tall_grass_top.png`, `temporal_tall_grass_bottom.png`) are deep teal — average around `#2C435F`–`#3D5C84`, five unique shades each. This teal happens to match Chrono Dawn's `chronodawn_plains` biome `grass_color` (`#5B8AC4`), so the plants look natural in plains. In other biomes (e.g. `chronodawn_dark_forest` with `grass_color = #44520C`), the surrounding `TemporalGrassBlock` shifts to the biome's color via `BiomeColors.getAverageGrassColor`, but the temporal plants stay teal and visually clash.

## Goal

Apply a biome-weighted blend of the texture's intended baseline (`#5B8AC4`) and the per-position biome grass color, so the plants pick up the surrounding tint while preserving their Chrono Dawn character. The blend factor was tuned to `0.8` (80% biome influence) during in-game playtesting; it started at `0.5` (even split) and was raised after dark_forest plants still felt too blue.

## Non-Goals

- Re-baking the texture PNGs (kept as-is to minimise diff and preserve plains appearance exactly).
- Modifying `TemporalGrassBlock` (the ground block already applies `BiomeColors.getAverageGrassColor` directly; behaviour unchanged).
- Applying tint to inventory icons, first-person held item, or other rendering contexts (those keep the texture's raw color).

## Approach

Selected approach: **multiplicative biome bias on the existing teal texture, no PNG changes**.

Minecraft's tint pipeline multiplies texel RGB by tint RGB / 255 per channel. We compute a tint that, when multiplied with the texture's `BASELINE = 0x5B8AC4` baseline, lands the result 80% of the way from the baseline toward the biome's grass color:

```
tint_C = lerp(255, biome_C * 255 / BASELINE_C, 0.8)        per channel
```

This collapses to `0xFFFFFF` (no tint) when the biome's grass color equals the baseline, so the plains appearance is preserved exactly. In darker or differently-hued biomes, the multiplication produces a colour about 80% of the way from the baseline teal to the biome tint.

### Architecture

Shared utility class (loader-agnostic, lives in `common/shared/`):

```
common/shared/src/main/java/com/chronodawn/client/TemporalPlantColorProvider.java
```

Mirrors the pattern of `LeafColorProvider`: pure static helpers, no Minecraft client lifecycle dependencies, callable from both Fabric and NeoForge registration sites.

Public surface:

```java
public static int blockTint(BlockAndTintGetter world, BlockPos pos, int tintIndex);
public static int itemTint(int tintIndex); // always -1 (the ColorProvider "no tint" sentinel)
```

Constants:

- `BASELINE = 0x5B8AC4` — Chrono Dawn plains `grass_color`, the texture's intended baseline tint.
- `BLEND = 0.8f` — tuned during in-game playtest (started at `0.5f`; raised after dark_forest plants still read as too blue). Tunable later without texture re-bake.

### Tint formula

```java
if (tintIndex != 0) return -1;
if (world == null || pos == null) return 0xFFFFFF;

int biome = BiomeColors.getAverageGrassColor(world, pos);
int rTint = blendChannel((biome >> 16) & 0xFF, (BASELINE >> 16) & 0xFF);
int gTint = blendChannel((biome >>  8) & 0xFF, (BASELINE >>  8) & 0xFF);
int bTint = blendChannel( biome        & 0xFF,  BASELINE        & 0xFF);
return (rTint << 16) | (gTint << 8) | bTint;

// package-private to allow direct unit testing without reflection.
static int blendChannel(int biomeC, int baseC) {
    float ratio  = baseC == 0 ? 1f : (float) biomeC / (float) baseC;
    float scaled = Math.min(ratio * 255f, 255f);
    float tint   = 255f + 0.8f * (scaled - 255f);
    return Math.max(0, Math.min(255, Math.round(tint)));
}
```

Worked examples:

| Biome (Chrono Dawn) | grass_color | Computed tint | Texel `#5B8AC4` × tint result |
|---------------------|-------------|---------------|-------------------------------|
| plains              | `#5B8AC4`   | `#FFFFFF`     | `#5B8AC4` (unchanged)         |
| dark_forest         | `#44520C`   | `~#CBAC3F`    | `~#485D30` (olive-green)      |
| snowy               | `#ABB8AB`   | `~#FFFFE5`    | `~#5B8AB0` (slightly cooler)  |
| swamp (post-modifier) | varies    | varies        | mossy green                   |

### Per-loader registration

**Fabric** — 11 files: `fabric/{1.20.1, 1.21.1, 1.21.2, 1.21.4, 1.21.5, 1.21.6, 1.21.7, 1.21.8, 1.21.9, 1.21.10, 1.21.11}/src/main/java/com/chronodawn/fabric/client/ChronoDawnClientFabric.java`

Inside existing `registerBlockColors()`:

```java
ColorProviderRegistry.BLOCK.register(
    (state, world, pos, tintIndex) ->
        TemporalPlantColorProvider.blockTint(world, pos, tintIndex),
    ModBlocks.TEMPORAL_TALL_GRASS.get(),
    ModBlocks.TEMPORAL_FERN.get()
);

ColorProviderRegistry.ITEM.register(
    (stack, tintIndex) -> TemporalPlantColorProvider.itemTint(tintIndex),
    ModItems.TEMPORAL_TALL_GRASS.get(),
    ModItems.TEMPORAL_FERN.get()
);
```

Fabric 1.21.4+ no longer uses `ColorProviderRegistry.ITEM` — item tints in those versions are declared in Client Items JSON (`assets/chronodawn/items/<id>.json`). Since our item tint returns `-1` (no tint) and the existing `temporal_tall_grass.json` / `temporal_fern.json` already render the raw texture, no Client Items JSON edits are needed and the `ITEM.register` call is added only to `fabric/1.20.1`, `fabric/1.21.1`, and `fabric/1.21.2`.

**NeoForge** — `neoforge/{base, 1.21.4, 1.21.5, 1.21.6, 1.21.7, 1.21.8, 1.21.9, 1.21.10, 1.21.11}/src/main/java/com/chronodawn/neoforge/client/ChronoDawnClientNeoForge.java`

Inside existing `onRegisterBlockColors`:

```java
event.register(
    (state, world, pos, tintIndex) ->
        TemporalPlantColorProvider.blockTint(world, pos, tintIndex),
    ModBlocks.TEMPORAL_TALL_GRASS.get(),
    ModBlocks.TEMPORAL_FERN.get()
);
```

For NeoForge `base/` only (covers 1.21.1–1.21.3, where `onRegisterItemColors` still exists), inside `onRegisterItemColors`:

```java
event.register(
    (stack, tintIndex) -> TemporalPlantColorProvider.itemTint(tintIndex),
    ModItems.TEMPORAL_TALL_GRASS.get(),
    ModItems.TEMPORAL_FERN.get()
);
```

NeoForge 1.21.4+ moved item tints to Client Items JSON. Since our item tint returns `-1` (the "no tint" sentinel, texture unchanged), no Client Items JSON edits are required for these versions.

## Edge cases

- `tintIndex != 0` → return `-1`.
- `world == null || pos == null` (inventory / first-person) → return `0xFFFFFF`, which keeps the texture's raw teal.
- `baseC == 0` defensive guard → ratio falls back to `1f`. (Plains baseline `B = 0xC4 ≠ 0`, so this branch is never taken in practice but prevents division-by-zero if BASELINE is ever changed.)
- `swamp` modifier — `BiomeColors.getAverageGrassColor` already applies `grass_color_modifier: "swamp"` internally; the formula sees the post-modifier value.
- Vanilla biomes (Overworld) — same formula applies; the tint follows whatever vanilla biome the player plants the plant in.
- `DoublePlantBlock` upper/lower halves — each block position queries its own biome color independently.

## Cross-version stability

- `BiomeColors.getAverageGrassColor(BlockAndTintGetter, BlockPos)` signature is stable across 1.20.1 → 1.21.11.
- `BlockAndTintGetter` parameter type is stable across the same range.
- Existing `TemporalGrassBlock` registration uses the same APIs in every per-version file, demonstrating compatibility.
- `tintIndex != 0` guard is required in both Fabric and NeoForge (per project memory `feedback_neoforge_tintindex_guard.md`).

## Testing

### Unit test

`common/1.21.2/src/test/java/com/chronodawn/client/TemporalPlantColorProviderTest.java` — exercises the pure-math path. The shared source is identical across versions, so a single version's test suffices for the formula correctness check.

| Case | Input | Expected |
|------|-------|----------|
| `tintIndex = 1` | any | `-1` |
| `world = null` | — | `0xFFFFFF` |
| baseline match | biome `= 0x5B8AC4` | `0xFFFFFF` |
| dark biome spot check | biome `= 0x44520C` | each channel within ±1 of expected `0xCBAC3F` |
| zero-channel guard | biome `= 0x000000` | no exception, deterministic clamp |

`BiomeColors.getAverageGrassColor` is not unit-tested; the formula is exercised by calling `blendChannel` (package-private) directly with synthetic biome and baseline channel values, and by calling `blockTint(null, null, 0)` for the inventory branch.

### Manual in-game check

1. `./gradlew runClientFabric1_21_2`
2. `/locate biome chronodawn:chronodawn_dark_forest`, teleport, `/give @s chronodawn:temporal_tall_grass 64` and `temporal_fern 64`.
3. Place blocks and confirm:
   - `chronodawn_plains`: identical to current appearance (teal, no shift).
   - `chronodawn_dark_forest`: muted teal-green, biome influence visible but blue still present.
   - `chronodawn_snowy`: still mostly teal, slightly cooler.
   - `chronodawn_swamp`: greener with swamp modifier.
4. Inventory and first-person held item icons: identical to current appearance (no tint applied, raw texture).
5. Naturally-generated Temporal Tall Grass / Fern in non-plains biomes pick up the same biome shift.

### Build verification

- `./gradlew :common-1.21.2:test -Ptarget_mc_version=1.21.2` — formula unit test.
- `./gradlew build1_21_2` — compile check.
- `./gradlew checkAll` — full multi-version verification before merge (per `feedback_check_all_before_phase_completion.md`).

## File touch summary

- New: `common/shared/src/main/java/com/chronodawn/client/TemporalPlantColorProvider.java`
- New: `common/1.21.2/src/test/java/com/chronodawn/client/TemporalPlantColorProviderTest.java`
- Modified: 11 × `fabric/<ver>/src/main/java/com/chronodawn/fabric/client/ChronoDawnClientFabric.java`
- Modified: 9 × `neoforge/<ver>/src/main/java/com/chronodawn/neoforge/client/ChronoDawnClientNeoForge.java` (`base` + `1.21.4`–`1.21.11`)

- Modified: 3 model JSONs at `common/shared/.../models/block/temporal_fern.json` and `temporal_tall_grass_{top,bottom}.json` — parent migrated from `minecraft:block/cross` to `minecraft:block/tinted_cross` so the inherited element faces declare `tintindex: 0` and the `BlockColor` provider's return is multiplied into the texel.

No texture, blockstate, datapack, or registry changes.
