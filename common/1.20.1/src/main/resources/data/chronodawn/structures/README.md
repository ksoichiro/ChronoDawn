# Structure NBT Files

**Note**: The `.nbt` files for 1.20.1 are auto-generated into `build/generated/nbt-structures/` at build time
by the `convertNbtStructures` Gradle task. The master source files are in
`common/shared-1.21.1+/src/main/resources/data/chronodawn/structure/`
and are converted from 1.21.1 Data Components format to 1.20.1 NBT tag format.

## Required Files

- `ancient_ruins/main.nbt` - Ancient Ruins structure (Overworld)
- `forgotten_library/main.nbt` - Forgotten Library structure (Chrono Dawn)

## Creating Structure NBT Files

1. Launch Minecraft with the Chrono Dawn mod installed
2. Use `/give @s structure_block` to get a Structure Block
3. Build your structure in-game
4. Use the Structure Block to save the structure
5. Copy the .nbt file from `.minecraft/saves/<world>/generated/chronodawn/structures/` to this directory

## Structure Specifications

### Ancient Ruins
- **Location**: Overworld surface
- **Size**: Small to medium (up to 3 jigsaw pieces)
- **Contains**: Clockstone Ore deposits, basic loot chests
- **Theme**: Ancient, weathered stone ruins

### Forgotten Library
- **Location**: Chrono Dawn surface
- **Size**: Medium (up to 4 jigsaw pieces)
- **Contains**: Portal Stabilizer blueprint, books, loot chests
- **Theme**: Mystical library with temporal elements
