# Faded Plains — Temporal Sand/Gravel Biome Tint — Design

Date: 2026-05-02
Status: Approved (pending implementation plan)
Theme: Make Temporal Sand / Temporal Gravel visually fit Faded Plains' warm, withered palette via a biome-localized multiplicative tint.

---

## 1. Overview and Scope

The Faded Plains biome (`chronodawn:chronodawn_faded_plains`, added 2026-04-30) uses a warm yellow-brown palette: `grass_color = 0xC5A65B`, `foliage_color = 0xB89345`, `temperature = 1.0`, `downfall = 0.1`, no precipitation. Custom blocks introduced for the biome (`FADED_TEMPORAL_GRASS`, `PARCHED_TEMPORAL_DIRT`, `TEMPORAL_DEAD_BUSH`) use baked-in fixed-color textures. However, **Temporal Sand and Temporal Gravel also appear in the biome** — Temporal Gravel via the `gravel_disk_placed` feature listed in layer 9, and Temporal Sand via existing surface rules — and both have blue-tinted textures (texture average ≈ `0x90BBE7`). They look out of place against the surrounding warm grass.

This design adds a Faded-Plains-localized tint to Temporal Sand / Temporal Gravel and re-tunes the existing edge-blend logic for the new biome, so that:

- Sand/Gravel adopts a warm, faded look inside Faded Plains.
- The grass↔sand boundary still blends smoothly (no sharp seam).
- Other biomes (notably `chronodawn_prairies`, currently the only biome with hand-tuned edge-blend constants) are completely unaffected.

### Scope

- Modify a single shared-module class: `common/shared/src/main/java/com/chronodawn/client/TemporalGrassEdgeTint.java`.
- Add Faded-Plains-specific tint constants and a biome-detection helper.
- Branch the existing `provide` (grass-side) and `provideForSandGravel` (sand-side) entry points on the new biome.
- No new blocks. No new models. No new textures. No per-version Java spread.

### Out of scope

- Vanilla `Blocks.SAND` / `Blocks.GRAVEL` placed by players in Faded Plains. These use vanilla's color providers, which we don't override; vanilla sand's natural light-yellow already harmonizes with the biome.
- Temporal Sandstone family (`TEMPORAL_SANDSTONE`, stairs, slabs, walls). These do not generate in worldgen; they only appear via crafting and structures.
- `PARCHED_TEMPORAL_DIRT` and `FADED_TEMPORAL_GRASS`. These were intentionally designed with baked-in fixed-color textures (per `2026-04-30-faded-plains-biome-design.md` §2). Re-evaluate later if visual feedback warrants it.
- Generalizing edge-blend tuning across arbitrary biomes (option 2-B during brainstorming). YAGNI — only two biomes have tuned edge logic so far. Reconsider if a third biome with sand/gravel is added.
- The `WET_TINT` water-proximity blend on grass. Faded Plains has no precipitation and water is rare; existing logic stays untouched.

---

## 2. Approach: Faded-Plains-Localized Tint Constants (Option 2-A)

The existing class already exposes hand-tuned constants for `chronodawn_prairies` (see the `Tuning history` comments inside `EDGE_TINT`, `SAND_NEIGHBOR_TINT`, `WET_TINT`). Replacing them with a fully data-driven scheme (option 2-B) was considered and rejected because the prairies values were arrived at through visual iteration that an automatic formula is unlikely to reproduce. Instead, this design **parallels the existing constants** with a new set keyed to the Faded Plains biome, and switches between them on a single `if` per provider call.

The cost is bounded: 3 new constants, 1 helper method, 2 small branches. The blast radius is bounded: any code path that does not enter Faded Plains is byte-for-byte identical to the current behavior.

---

## 3. New Constants and Initial Values

All values below are **initial estimates** to be confirmed by in-game visual testing (see §6). They mirror the derivation logic used for the existing prairies constants so the comments can follow the same `Tuning history` pattern.

| Constant | Value | Rationale |
|---|---|---|
| `BIOME_TINT_FADED` | `0xFFE06A` | Multiplicative tint applied to Temporal Sand / Temporal Gravel anywhere inside Faded Plains. Derived to push the texture average (`0x90BBE7`) toward a warm faded color. Effective rendered color ≈ `0x90A460` (warm olive). Multiplicative tint cannot raise channel values, so a "true" sandstone yellow (`~0xC2A560`) is unreachable without a texture rewrite — see §7. |
| `EDGE_TINT_FADED` | `0x90A460` | Color toward which `provide` (grass-side) lerps Temporal Grass when scanning detects an adjacent sand/gravel block in Faded Plains. Same value as the effective sand color above — mirrors prairies, where `EDGE_TINT = 0x90BBE7` is also the raw sand average. |
| `SAND_NEIGHBOR_TINT_FADED` | `0xFFFFFF` | Multiplicative tint applied to Temporal Sand / Temporal Gravel at Chebyshev distance 1 from a Temporal Grass Block, when in Faded Plains. Initial value `0xFFFFFF` (no further pull) on the assumption that warm sand and warm grass are already close enough at the boundary. Tune up to a small darken (e.g., `0xF8F8F0`) only if visual testing reveals a step at d=1. |

The existing constants `EDGE_TINT = 0x90BBE7`, `SAND_NEIGHBOR_TINT = 0xE8EFF5`, `WET_TINT = 0x3A597F` are unchanged and remain the active values for any biome other than Faded Plains.

---

## 4. Biome Detection

`TemporalGrassEdgeTint.java` lives in `common/shared/`, which compiles for all 11 supported Minecraft versions (1.20.1 through 1.21.11). Per the memory entry `feedback_resourcelocation_identifier_rename`, Mojang renamed `ResourceLocation` to `Identifier` in 1.21.11, so any code that **imports** or **explicitly types** that class will not cross-compile.

Workaround: never name the class. Compare the biome's resource location via `toString()`:

```java
private static final String FADED_PLAINS_ID = "chronodawn:chronodawn_faded_plains";

static boolean isInFadedPlains(BlockAndTintGetter world, BlockPos pos) {
    return world.getBiome(pos).unwrapKey()
        .map(key -> key.location().toString().equals(FADED_PLAINS_ID))
        .orElse(false);
}
```

`Holder<Biome>.unwrapKey()` returns `Optional<ResourceKey<Biome>>` whose `.location()` returns the renamed class — but we only call `toString()`, never name the type. `BlockAndTintGetter.getBiome(BlockPos)` is stable across all 11 versions.

If the verification build for any version fails on this snippet, fall back to a `var` declaration or `Object` cast at the call site rather than introducing an abstraction layer.

---

## 5. Provider-Side Changes

### 5.1 `provide` (grass-side, `TemporalGrassBlock` color provider)

Add the biome check just before `blend`, and pass the appropriate edge tint into `blend`:

```java
public static int provide(BlockState state, @Nullable BlockAndTintGetter world,
                          @Nullable BlockPos pos, int tintIndex) {
    if (tintIndex != 0) return -1;
    if (world == null || pos == null
            || !(world.getBlockState(pos).getBlock() instanceof TemporalGrassBlock)) {
        return DEFAULT_FALLBACK;
    }
    int base = BiomeColors.getAverageGrassColor(world, pos);
    int edgeTint = isInFadedPlains(world, pos) ? EDGE_TINT_FADED : EDGE_TINT;
    return blend(world, pos, base, edgeTint);
}
```

`blend(...)` gains an `edgeTint` parameter (passed through to its single existing `lerpRgb(result, EDGE_TINT, t)` call site). The water-blend branch keeps using `WET_TINT` unchanged.

### 5.2 `provideForSandGravel` (sand-side, Temporal Sand / Temporal Gravel color provider)

Restructure to apply biome tint independently of the grass-neighbor scan, then layer the neighbor tint on top:

```java
public static int provideForSandGravel(BlockState state, @Nullable BlockAndTintGetter world,
                                       @Nullable BlockPos pos, int tintIndex) {
    if (tintIndex != 0) return -1;
    if (world == null || pos == null) return 0xFFFFFF;
    boolean nearGrass = scanForGrassNeighbor(world, pos);
    if (isInFadedPlains(world, pos)) {
        int result = BIOME_TINT_FADED;
        if (nearGrass) result = multiplyRgb(result, SAND_NEIGHBOR_TINT_FADED);
        return result;
    }
    return nearGrass ? SAND_NEIGHBOR_TINT : 0xFFFFFF;
}
```

`scanForGrassNeighbor(world, pos)` is a small extraction of the existing 3×3 same-Y loop body. `multiplyRgb` is a new helper that does per-channel `(a * b) / 255`.

### 5.3 What is unchanged

- `EDGE_TINT`, `SAND_NEIGHBOR_TINT`, `WET_TINT`, `RADIUS`, `WATER_RADIUS`, `DEFAULT_FALLBACK`.
- `isEdgeTrigger` (still triggers on both Temporal and vanilla sand/gravel).
- `isWater`, `lerpRgb`.
- The 8-neighbor scan radius and structure inside `provideForSandGravel`.
- All registration sites (Fabric `ColorProviderRegistry.BLOCK.register`, NeoForge `RegisterColorHandlersEvent.Block#register`) — no signature change.

---

## 6. Verification Strategy

### 6.1 Per-version build

Run sequentially (skip wrapper per `feedback_buildall_gametestall_wrapper_unreliable`):

- `./gradlew build1_21_11` — confirm the biome-detection snippet compiles for the renamed-class era.
- `./gradlew build1_20_1` — confirm it still compiles for the oldest supported version (largest API delta).
- `./gradlew build1_21_5` — middle-of-the-road sanity check.

If those three pass, the cross-version risk is bounded; the remaining versions can be validated by `./gradlew checkAll` before commit.

### 6.2 Visual test (manual, per-version)

1. `./gradlew runClientFabric1_21_11`
2. `/gamemode creative`, `/locate biome chronodawn:chronodawn_faded_plains`, `/tp` to a generated patch of Faded Plains.
3. Confirm:
   - Temporal Sand and Temporal Gravel surfaces appear warm-olive, not the usual blue.
   - The boundary between Temporal Sand/Gravel and Temporal Grass Block transitions smoothly (no sharp color step at d=1).
4. `/locate biome chronodawn:chronodawn_prairies`, `/tp`. Confirm:
   - Temporal Sand and Temporal Gravel still appear with the original blue tint.
   - The grass↔sand edge gradient still looks identical to before this change.
5. Repeat 1–4 with `runClientFabric1_20_1` to catch any version-specific render quirks.

### 6.3 Tuning loop

The constants in §3 are starting points. Expected iteration:

- If the warm tint is too muted, lower `BIOME_TINT_FADED`'s blue channel further (e.g., `0xFFE040`).
- If the d=1 boundary in Faded Plains shows a visible step, set `SAND_NEIGHBOR_TINT_FADED` to a slight darken (e.g., `0xF8F8F0`).
- Append each iteration's value + reason to a `Tuning history` comment block on the corresponding constant, mirroring the prairies pattern in the existing file.

### 6.4 No automated tests

The change is purely visual-aesthetic and depends on rendering. No unit or GameTest can meaningfully validate it. Manual smoke test (§6.2) is the verification of record.

### 6.5 Documentation impact

- `CHANGELOG.md` entry under Unreleased — added only after user approval per CLAUDE.md commit policy.
- `docs/player_guide.md`, `docs/curseforge_description.md`, `docs/modrinth_description.md` — Faded Plains description (already added in 2026-04-30 work) does not need to mention internal tinting; skip.

---

## 7. Risks and Mitigations

1. **Multiplicative tint cannot raise channel values.** The blue-leaning sand texture (`R = 144`) cannot be tinted to a true sandstone yellow (`R ≥ 197`). Mitigation: aim for "warm olive" rather than "saturated yellow"; this fits the biome theme of "withered, faded land" naturally. If a fully different hue becomes a hard requirement later, switch to a per-biome texture variant (out of scope for this design).
2. **Cross-version `ResourceLocation`/`Identifier` rename.** Mitigated by string-comparison via `toString()` (§4). If any version's compiler still rejects the lambda, fall back to a `var` declaration in a per-version helper rather than spreading the class across all 11 modules.
3. **Performance of `world.getBiome(pos)` in chunk mesh bake.** Vanilla's `BiomeColors.getAverageGrassColor` already issues equivalent biome lookups on every grass block, so the marginal cost is small. No caching layer added (matches the rest of the file's "pure function" stance per its class-doc comment).
4. **Regression in prairies' tuned edge gradient.** Mitigated by exclusive `if/else` branching: any code path that is not in Faded Plains executes the byte-for-byte original logic.
5. **Other biomes that may later host sand/gravel.** Adding a third tuned biome will require extending the same branch pattern (or finally adopting option 2-B). Out of scope here; flag in `MEMORY.md` as a watch-item if it becomes recurring.

---

## 8. Implementation Phases

- **Phase A**: Edit `TemporalGrassEdgeTint.java` — add `BIOME_TINT_FADED`, `EDGE_TINT_FADED`, `SAND_NEIGHBOR_TINT_FADED`, `isInFadedPlains`, `scanForGrassNeighbor`, `multiplyRgb`, and refactor `provide` and `provideForSandGravel` per §5.
- **Phase B**: Build for 1.21.11, 1.20.1, 1.21.5 (per §6.1).
- **Phase C**: Visual smoke test on 1.21.11 (per §6.2) and tune constants if needed.
- **Phase D**: `./gradlew checkAll` before commit.

---

## 9. Open Questions Deferred to Implementation

- Whether `world.getBiome(pos).unwrapKey()` lambda compiles cleanly under all 11 versions without a `var` workaround. Verify in Phase B.
- Final tuning of `BIOME_TINT_FADED`. Record adopted value with rationale in the file's `Tuning history` comment.
- Whether `SAND_NEIGHBOR_TINT_FADED` needs any value other than `0xFFFFFF`. Decide after Phase C visual review.
