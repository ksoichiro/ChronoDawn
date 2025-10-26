# Time Wood Textures

## Current Status

### Log Textures (Ready for editing)
- `time_wood_log.png` - Side texture for Time Wood Log (copied from `clockstone_block.png`)
- `time_wood_log_top.png` - Top/bottom texture for Time Wood Log (copied from `clockstone_block.png`)

These are ready for you to edit with custom time-themed designs.

### Leaves Texture (Requires manual copy)
- `time_wood_leaves.png` - **NEEDS TO BE REPLACED** with vanilla Oak leaves texture

## How to Get Vanilla Oak Leaves Texture

### Step 1: Extract from Minecraft JAR

1. Locate your Minecraft client jar:
   - macOS: `~/Library/Application Support/minecraft/versions/1.21.1/1.21.1.jar`
   - Windows: `%APPDATA%\.minecraft\versions\1.21.1\1.21.1.jar`
   - Linux: `~/.minecraft/versions/1.21.1/1.21.1.jar`

2. Open the jar file with an archive tool (7-Zip, WinRAR, or unzip)

3. Navigate to: `assets/minecraft/textures/block/oak_leaves.png`

4. Copy `oak_leaves.png` to:
   `common/src/main/resources/assets/chronosphere/textures/block/time_wood_leaves.png`

### Step 2: Verify

The oak leaves texture should be:
- **Grayscale** (or tinted with a neutral color)
- 16x16 pixels
- PNG format with transparency

The color will be applied dynamically based on biome using the `BlockColorProvider` we've added.

---

## Design Requirements (For custom textures)

**Time Wood Log Side (`time_wood_log.png`)**:
- Base: Similar to oak log bark texture
- Theme: Time-related visual elements
- Suggestions:
  - Subtle clock hand patterns in the bark
  - Faint glowing blue/purple tint
  - Time-distorted/warped wood grain effect

**Time Wood Log Top (`time_wood_log_top.png`)**:
- Base: Similar to oak log top (growth rings)
- Theme: Time/clock face aesthetic
- Suggestions:
  - Growth rings arranged like clock markings
  - Optional: Roman numerals around the edge
  - Center point resembling clock center
  - Faint glow effect

### Technical Specifications
- Format: PNG with transparency support
- Size: 16x16 pixels (Minecraft standard)
- Color palette: Should complement the Chronosphere dimension aesthetic

### References
- Vanilla Oak Log: `oak_log.png`, `oak_log_top.png`
- Mod theme: Time manipulation, temporal distortion
- Related: Fruit of Time item texture (`glow_berries.png` based)

---

**Task Reference**: T080c [US1] Create Time Wood Log textures
