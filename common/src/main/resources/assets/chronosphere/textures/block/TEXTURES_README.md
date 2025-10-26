# Block Textures for Special Blocks

This directory needs the following texture files for the special blocks:

## Required Textures

### reversing_time_sandstone.png
- **Base texture**: Use Minecraft's `sandstone.png` or `smooth_sandstone.png` as a base
- **Modifications**: Add subtle clock or time-related details (e.g., faint clock hands, spiral patterns)
- **Color scheme**: Sandy/beige tones with hints of purple or blue to indicate time magic
- **Size**: 16x16 pixels (standard Minecraft block texture)

### unstable_fungus.png
- **Base texture**: Use Minecraft's `warped_fungus.png` or `crimson_fungus.png` as a base
- **Modifications**: Add distortion or glitch effects to indicate instability
- **Color scheme**: Purple or mixed colors (purple/cyan) to represent unstable time effects
- **Size**: 16x16 pixels (standard Minecraft block texture)

## How to Create Textures

1. Extract textures from Minecraft JAR file or use a resource pack
2. Use an image editor (e.g., GIMP, Photoshop, Aseprite) to modify the base textures
3. Save the files with the exact names listed above
4. Place them in this directory

## Temporary Solution

For development/testing, you can:
1. Copy existing Minecraft textures and rename them:
   - `sandstone.png` → `reversing_time_sandstone.png`
   - `warped_fungus.png` → `unstable_fungus.png`
2. These placeholder textures will allow the mod to load without errors
3. Replace with custom textures later for the final version
