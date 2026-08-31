# Design: Mod-owned biome and structure tags

**Created**: 2026-08-31
**Status**: Approved — ready for implementation
**Pillar**: [Modpack-author readiness roadmap](./2026-05-09-modpack-author-readiness-roadmap.md),
sub-project B (datapack and tag externalization)

---

## 1. Problem

Pack authors can refer to individual Chrono Dawn biomes and structures, but
cannot select the mod's complete biome or structure set without copying an
ever-growing list of IDs into every datapack. Existing `has_*` biome tags are
internal world-generation placement inputs, not a stable public grouping.

## 2. Scope

Publish a small, stable set of `chronodawn:` worldgen tags. These tags only
classify existing registry entries; they do not alter generation, structure
placement, or gameplay.

Out of scope:

- Replacing existing hard-coded structure biome lists with the new tags.
- Adding biome categories whose semantics are subjective or overlap vanilla
  categories.
- Tagging configured or placed features, structure sets, entities, blocks, or
  items.

## 3. Public tags

| Tag | Members | Intended use |
| --- | --- | --- |
| `chronodawn:chronodawn_biomes` | All eleven Chrono Dawn biomes | Target or exclude the whole Chrono Dawn dimension biome set in datapack rules. |
| `chronodawn:chronodawn_structures` | All eight Chrono Dawn structures | Target or exclude every Chrono Dawn structure in structure-aware datapacks. |
| `chronodawn:boss_structures` | Desert Clock Tower, Guardian Vault, Clockwork Depths, Phantom Catacombs, Entropy Crypt, Master Clock | Apply shared quest, loot, or progression treatment to structures that contain a Chrono Dawn boss. |

`ancient_ruins` and `forgotten_library` deliberately remain outside
`boss_structures`: neither contains a boss, even though the library gates a
progression recipe. The existing `has_*` biome tags remain implementation
details for structure placement and are not included in this public contract.

## 4. Version and file layout

All supported Minecraft versions use `data/chronodawn/tags/worldgen/biome/`
and `data/chronodawn/tags/worldgen/structure/` for these registry tags. The
member IDs are stable across 1.20.1 and 1.21.1–1.21.11, so the files belong in
`common/shared/src/main/resources/` and require no version- or loader-specific
copies.

Every tag uses `"replace": false`; datapacks can extend it normally, while a
pack that needs to replace the set can still override the file with
`"replace": true` at higher datapack priority.

## 5. Compatibility contract

The three tag IDs and the meaning of existing members are public integration
API. Future Chrono Dawn biomes and structures should join the matching
inclusive tag. A structure belongs in `boss_structures` only if it contains a
Chrono Dawn boss encounter.

## 6. Verification

Run:

```bash
mise exec java@temurin-21.0.3+9.0.LTS -- ./gradlew validateResources
```

The task must succeed with no unresolved tag members. In a 1.21.11 development
world, use a datapack that references each tag and reload it successfully; the
tag contents can be inspected with Minecraft's tag commands or a compatible
datapack-debugging mod.
