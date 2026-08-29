# Design: Conventional Tags for Equipment Repair Materials

**Created**: 2026-08-29
**Status**: Implemented
**Initiative**: [Modpack-Author Readiness](./2026-05-09-modpack-author-readiness-roadmap.md), sub-project B

---

## Why

Chrono Dawn publishes conventional `c:` material tags, but equipment repair
materials still use either exact item ingredients or vanilla tier tags. This
prevents a modpack or unification addon from supplying another item through the
published material convention.

This slice connects Chrono Dawn's own repair-material consumers to the
per-material conventional tags that already exist in the 1.21.1+ tag set.

## Scope

The following Chrono Dawn repair materials use conventional tags on Minecraft
1.21.1 through 1.21.11:

| Equipment material | Conventional item tag |
| --- | --- |
| Clockstone tools and armor | `c:gems/time_crystal` |
| Enhanced Clockstone tools and armor | `c:gems/time_crystal` |
| Entropy Crystal tools | `c:gems/entropy_crystal` |
| Temporal Amber armor | `c:dusts/temporal_amber` |
| Chronoblade and Time Tyrant Mail | `c:gems/time_crystal` |

Minecraft 1.20.1 keeps exact item repair ingredients. Its conventional tag
format has only the flat `c:gems` umbrella, which contains both Time Crystal
and Entropy Crystal; using it would make those two distinct repair materials
interchangeable.

The Spatially Linked Pickaxe remains on its existing vanilla Diamond repair
semantics because its repair material is intentionally Diamond, not a Chrono
Dawn material. Custom anvil behavior remains a separate concern: it provides
the existing enhanced Time Crystal repair amount and is not generalized to
arbitrary conventional-tag members in this slice.

## Compatibility and implementation

The 1.21.1+ version modules create `TagKey<Item>` values in the `c` namespace
with the existing `CompatResourceLocation` helper. Older 1.21.1 equipment
APIs consume the tags through `Ingredient.of`; newer APIs receive the tags in
their material constructors.

No new tags are introduced. No recipe outputs, tool mining tiers, durability,
or repair amounts change for the items currently shipped by Chrono Dawn.

## Verification

- A source guard covers every in-scope version-specific material class and
  rejects regression to vanilla repair tags or exact-item ingredients.
- Existing conventional tag validation continues to verify the tag members.
- The user manually checks that a current-version Fabric and NeoForge anvil
  accepts the expected tagged repair item and rejects the other crystal where
  the tags are distinct.

## Out of scope

- 1.20.1 flat-tag broadening.
- Spatially Linked Pickaxe's Diamond repair behavior.
- Custom anvil amount/experience rules.
- Tool, armor, weapon, or shield category tags (`c:tools/*`, `c:armors/*`).
- Recipe ingredient conversion beyond repair materials.
