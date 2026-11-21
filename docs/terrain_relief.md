# Chrono Dawn terrain relief

Chrono Dawn keeps the vanilla Overworld terrain functions and adds a small
height offset on top of them. The offset lives in the `chronodawn:` namespace,
so the vanilla `minecraft:overworld/*` files stay untouched.

## How it works

`chronodawn:terrain/relief` is a 2D value in "depth" units. One unit of depth
is about 128 blocks of surface height (`y_clamped_gradient` goes from 1.5 to
-1.5 over 384 blocks, so 0.0078 per block). It is the sum of two parts:

- **Hills**: weighted by erosion (zero at 0.0 or below, full at 0.2 or above)
  and continentalness (zero at the coast, full inland), driven by the
  `chronodawn:relief/hills` noise (features of about 64 blocks). The noise is
  scaled by 1.2, shifted by 0.05 and clamped to a floor of -0.05, so hills rise
  but plains do not dig many new lakes.
- **Mountains**: weighted by erosion (zero at -0.1, full at -0.35) and
  continentalness (inland only). The `chronodawn:relief/massif` noise picks the
  character. Where it is positive, narrow crests around the zero line of
  `chronodawn:relief/ridge` give steep ridges. Where it is negative, a broad
  uplift gives high but gentle mountains, modulated by the hills noise so the
  top is not flat.

The offset is applied in three places so terrain, caves, aquifers and
structures agree on the surface:

| Function | Replaces |
| --- | --- |
| `chronodawn:terrain/sloped_cheese` | `minecraft:overworld/sloped_cheese` (copy that uses the shifted depth) |
| `chronodawn:terrain/depth` | `minecraft:overworld/depth` inside `initial_density_without_jaggedness` (up to 1.21.8) |
| `chronodawn:terrain/offset` | `minecraft:overworld/offset` inside `preliminary_surface_level` (1.21.9 to 26.2) |

The `depth` entry of the noise router stays vanilla, so the biome layout does
not move. Minecraft 26.3 also gets copies of `final_density`,
`preliminary_surface_level` and `chunk_surface_level` that point at the shifted
functions. Its density function schema is different (`left`/`right`/`input`),
so its files are not interchangeable with the older ones. The same applies to
noise definitions (`base_octave`, `base_amplitude`, `octave_count`,
`amplitude_modifiers` instead of `firstOctave` and `amplitudes`) and to the
2D cache type (`minecraft:cache` instead of `minecraft:cache_2d`). That is why
the three noises live in each version module and not in `common/shared`.

## Tuning

The strengths are the constants in `relief.json` (`0.2` hills, `0.5` ridges,
`0.35` uplift). All 14 versions carry the same constants, so change them together.

## Measuring

Terrain was compared against the 0.17.1 jar on the same seed with a dedicated
server (Fabric 1.21.1, seed `20261122`). The method: force-load 16x16-chunk
boxes in `chronodawn:chronodawn`, read the `OCEAN_FLOOR` heightmap from the
region files, and render a hillshaded top view. Averages over 5599 chunks:

| Biome | In-chunk height range | Mean deviation from neighbours | 95th percentile peak |
| --- | --- | --- | --- |
| Plains | 8.6 to 9.5 | 2.3 to 2.7 | 125 (unchanged) |
| Mountain | 11.0 to 12.4 | 3.1 to 3.6 | 126 to 137 |

Oceans, faded plains and forests barely change. Desert gets a bit more relief
because it shares the erosion range of plains. Minecraft 26.3 gave the same
numbers within 0.1 on the same seed, so the two schemas produce the same shape.
The first attempt with 0.09, 0.2 and 0.16 was too subtle to see.
