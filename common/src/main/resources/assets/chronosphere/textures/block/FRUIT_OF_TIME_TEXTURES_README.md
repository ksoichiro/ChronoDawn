# Fruit of Time Block Textures

This directory contains textures for the Fruit of Time block with 3 growth stages.

## Required Textures

### fruit_of_time_stage_0.png (Small/Young)
- **Description**: Small fruit bud just starting to grow
- **Size**: Should appear small on the block model (4x5x4 block units)
- **Base texture**: Use Minecraft's `cocoa_stage0.png` as reference
- **Modifications**:
  - Use glowing orange/golden color scheme (matching time theme)
  - Add subtle glow effect or particle-like details
  - Keep it small and compact
- **Color scheme**: Orange/gold with slight purple tint (#db8813 base color)
- **Size**: 16x16 pixels (standard Minecraft block texture)

### fruit_of_time_stage_1.png (Medium/Growing)
- **Description**: Medium-sized growing fruit
- **Size**: Should appear medium on the block model (6x7x6 block units)
- **Base texture**: Use Minecraft's `cocoa_stage1.png` as reference
- **Modifications**:
  - Brighter glow effect than stage 0
  - More defined shape
  - Add time-related patterns (clock hands, spiral, gears)
- **Color scheme**: Brighter orange/gold (#db8813 base color)
- **Size**: 16x16 pixels (standard Minecraft block texture)

### fruit_of_time_stage_2.png (Large/Mature)
- **Description**: Large mature fruit ready to harvest
- **Size**: Should appear large on the block model (8x9x8 block units)
- **Base texture**: Use Minecraft's `cocoa_stage2.png` as reference
- **Modifications**:
  - Strong glow effect (emissive texture recommended)
  - Fully developed time-themed patterns
  - Add visual indicators that it's ready to harvest
  - Similar appearance to fruit_of_time.png (item texture)
- **Color scheme**: Vibrant orange/gold with glow (#db8813 base color)
- **Size**: 16x16 pixels (standard Minecraft block texture)

## How to Create Textures

1. **Extract base textures**: Get Minecraft's cocoa textures as reference
   - `cocoa_stage0.png`, `cocoa_stage1.png`, `cocoa_stage2.png`

2. **Modify for time theme**:
   - Change brown cocoa colors to orange/gold time colors
   - Add glow effects (optional emissive texture with `_e` suffix)
   - Add time-related details (clock faces, gears, spirals)

3. **Use image editor**:
   - GIMP, Photoshop, Aseprite, or Pixly (online)
   - Work with 16x16 pixel canvas
   - Use nearest-neighbor scaling (no blur)

4. **Save files**:
   - PNG format with transparency
   - Exact names: `fruit_of_time_stage_0.png`, `fruit_of_time_stage_1.png`, `fruit_of_time_stage_2.png`
   - Place in this directory

5. **Optional emissive textures**:
   - Create `fruit_of_time_stage_0_e.png`, etc. for glow-in-dark effect
   - These require OptiFine or similar mod support

## Temporary Solution (Current)

For development/testing, placeholder textures have been created by copying `fruit_of_time.png` (item texture):
- All 3 stages currently use the same texture
- This allows the mod to load without errors
- Replace with proper growth-stage textures for final version

## Visual Progression

```
Stage 0 (age=0): ●   Small bud
Stage 1 (age=1): ◐   Medium fruit
Stage 2 (age=2): ◉   Large mature fruit (harvestable)
```

## Related Files
- **Item texture**: `../item/fruit_of_time.png`
- **Block models**: `../../models/block/fruit_of_time_stage_0.json`, etc.
- **Block class**: `com.chronosphere.blocks.FruitOfTimeBlock`
