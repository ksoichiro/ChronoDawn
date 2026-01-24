# Structure NBT Files

This directory should contain the actual structure NBT files created using Minecraft's Structure Block.

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
