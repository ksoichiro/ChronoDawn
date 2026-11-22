# Terrain blending investigation

The terrain update leaves a visible seam where newly generated chunks meet
chunks generated before the update. The changelog already warns players about
this. Vanilla blending is not a small configuration change that can safely be
applied to existing Chrono Dawn worlds. Keep the warning and do not enable
blending without a separate implementation and world-upgrade investigation.

This is a source-level assessment, not a visual comparison of blended and
unblended worlds. It does not establish whether blending would sufficiently
reduce the cliffs or whether every supported loader and version can run an
implementation. No blending prototype or production Mixin was tested.

## Findings

In Minecraft 1.21.1, `Blender.of` first asks `WorldGenRegion.isOldChunkAround`.
`IOWorker` answers using a cached region scan of chunk NBT. For current-format
chunks, that scan checks for `blending_data`. Adding the data only when a chunk
is deserialized can therefore be too late: the cached scan may already have
reported no old chunks, causing the blender to remain empty. Persisting data
also needs a reliable way to distinguish chunks generated before the terrain
change from newly generated chunks, including partially generated chunks,
unvisited saved chunks, worlds created after the update and worlds reopened
across restarts. Minecraft's `DataVersion` identifies the game version, not the
Chrono Dawn terrain revision.

`BlendingData` can calculate its own samples from old chunks. It does more than
read a heightmap: it samples block density and biomes along old/new boundaries.
Its surface search uses a fixed list of vanilla blocks. Chrono Dawn's terrain
uses Temporal Stone and custom surface blocks, which are not in that list. A
blind use of vanilla surface detection can search past the actual surface and
return an incorrect height. The implementation would need an appropriate
surface sampling strategy and tests for water, caves, structures and modified
terrain.

In 1.21.1, height and biome samples are considered up to 27 quart cells, or
108 blocks, away. Density samples use a radius of two quart cells, or eight
blocks, with vertical distance accounted for. These are algorithmic ranges,
not a guarantee of a smooth slope. Blending also changes biome selection,
filters cave carving near old chunks and schedules border ticks. Structures,
fluids, player edits and cave entrances need separate upgrade checks. The
existing terrain relief is added to the vanilla depth after its offset, so the
height blending path alone does not remove the new relief at the boundary.

The current noise settings contain `blend_density` in all 14 version modules.
Through 26.2 it is inline. In 26.3, the settings refer to
`chronodawn:terrain/final_density`, which contains it. The latter also uses a
Chrono Dawn offset that adds relief to `minecraft:overworld/offset`. These
references show that the density function can call the blender, but do not
prove that a populated blender produces the same result across versions.
The cached NeoForge Minecraft class signatures show `BlendingData` in every
1.21.1+ target. From 1.21.2 onward, they expose a packed representation rather
than the 1.21.1 class codec. The 26.3 sampling signatures also differ, so a
1.21.1 Mixin cannot be assumed to work unchanged across versions.

The common Mixin configuration is excluded from production loader builds.
Any future common gameplay Mixin must be listed in each applicable Fabric,
NeoForge or Forge configuration. Production remapping and startup must be
verified for each affected version and loader. No Mixin or Mixin configuration
was changed in this investigation.

## Evidence and remaining validation

The detailed behavior above was inspected in the locally cached Minecraft
1.21.1 sources: `Blender`, `BlendingData`, `ChunkSerializer`, `ChunkStorage`
and `IOWorker`. The 26.3 comparison used the local Minecraft class signatures
and this repository's worldgen resources. The resource files were inspected for
all 14 supported version modules. This does not substitute for testing the
other Minecraft versions or loaders.

A conclusive visual comparison would require a disposable 0.17.1 world, copies
with the same seed and old generated chunks, and a diagnostic implementation
that handles the old-chunk scan and Chrono Dawn surface sampling. Generate the
same new area with and without it, then inspect the seams in a client. Test
multiple seeds and terrain types, including structures, caves and fluids, and
repeat after saving and reloading. Run production smoke tests for each affected
loader and version before considering a release. The upgrade test checks data
preservation and startup, but does not judge seam appearance.

The 1.21.1 upgrade test was run against the current terrain build on Fabric and
NeoForge, using 0.8.0 and 0.17.1 worlds. Both reports recorded zero failures.
They also list state that the harness does not cover, including boss-room data
and parts of Time Cairn. The run did not test a blending implementation or
visually inspect the terrain seam.
