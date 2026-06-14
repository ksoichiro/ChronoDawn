---
name: texture-creation
description: Guide for creating textures using vanilla texture reuse and color transformation
---

# Texture Creation Guide

**Purpose**: Guide for creating mod textures using vanilla texture reuse and color transformation.

**How it works**: This skill is automatically activated when you mention tasks related to:
- Creating textures for custom blocks or items
- Reusing vanilla textures with color modifications
- Using ImageMagick for texture processing
- Creating wood variant textures (doors, trapdoors, etc.)

Simply describe what you want to do, and Claude will reference the appropriate guidance from this skill.

---

## Reusing Vanilla Textures with Color Transformation (2025-11-15)

**Approach**: For custom wood variants, extract vanilla textures and apply color transformation to maintain visual consistency while ensuring rapid texture creation.

**Workflow**:

### 1. Extract Vanilla Textures

- Locate Minecraft client JAR: `~/.gradle/caches/fabric-loom/minecraftMaven/net/minecraft/minecraft/1.21.1/minecraft-1.21.1.jar`
- Extract textures using `unzip`:
  ```bash
  unzip -j minecraft-1.21.1.jar \
    "assets/minecraft/textures/block/jungle_door_top.png" \
    "assets/minecraft/textures/block/jungle_door_bottom.png" \
    "assets/minecraft/textures/block/jungle_trapdoor.png" \
    "assets/minecraft/textures/item/jungle_door.png"
  ```
- Use similar wood type as base (e.g., Jungle for yellowish woods, Oak for brown woods)

### 2. Determine Color Transformation Parameters

- Compare reference planks (e.g., Time Wood Planks vs Jungle Planks)
- Use ImageMagick histogram to analyze color distributions:
  ```bash
  magick time_wood_planks.png -format %c histogram:info:-
  magick jungle_planks.png -format %c histogram:info:-
  ```
- Calculate RGB channel multipliers by comparing dominant colors
- Example for Time Wood (yellowish-olive from brown):
  - Red: 0.95× (slight reduction)
  - Green: 1.17× (significant increase for yellow tone)
  - Blue: 0.85× (reduction for warmth)

### 3. Apply Color Transformation with ImageMagick

```bash
magick input.png \
  -channel R -evaluate multiply 0.95 +channel \
  -channel G -evaluate multiply 1.17 +channel \
  -channel B -evaluate multiply 0.85 +channel \
  output.png
```

### 4. Iterate Based on Visual Feedback

- Test textures in-game
- Adjust RGB multipliers if colors don't match:
  - Too green → reduce G multiplier, increase R/B
  - Too yellow → reduce G multiplier
  - Too dark/bright → adjust all channels proportionally

---

## Known Color Transformations

**Time Wood** (from Jungle): R×0.95, G×1.17, B×0.85 - produces yellowish-olive tone
- Applied to: Door, Trapdoor textures (T080v-T080aa)

**Temporal Stone family** (vanilla stone → temporal_stone): R×0.871, G×0.984, B×1.129 - produces the blue-gray temporal tone. Derived from mean-RGB ratio of `temporal_stone.png` (109,124,142) over vanilla grayscale `stone.png` (~125.6). Apply to any vanilla grayscale stone-family texture to make its Temporal variant. Note vanilla stone textures are single-channel grayscale, so convert to RGB first: `magick in.png -colorspace sRGB -type TrueColor -channel R -evaluate multiply 0.871 +channel -channel G -evaluate multiply 0.984 +channel -channel B -evaluate multiply 1.129 +channel -depth 8 PNG32:out.png`
- Applied to: `smooth_temporal_stone.png` (from vanilla `smooth_stone.png`)

---

## Benefits

- Rapid texture creation (batch processing with ImageMagick)
- Visual consistency across all variants
- Easy iteration and adjustment
- Reusable approach for future wood types

---

## Notes

- HSV color space transformations may not work well for fundamental color shifts (e.g., brown → yellow-green)
- RGB channel multiplication provides more control for cross-color transformations
- Always verify textures in-game before finalizing

---

**Last Updated**: 2025-12-08
**Maintained by**: Chrono Dawn Development Team
