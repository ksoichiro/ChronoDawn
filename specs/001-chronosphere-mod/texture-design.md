# Texture Design Notes - Chronosphere Mod

## Spatially Linked Pickaxe (空間連結ツルハシ)

### Item Concept

**Function**: Magic pickaxe that manipulates spatial connections to duplicate block drops from another dimension
**Performance**: Diamond pickaxe equivalent
**Special Effect**: 33% chance to double drops (spatial duplication)

### Visual Design

#### Basic Shape
```
Pickaxe Shape:
   ◇◇◇  ← Pick head (Made from Enhanced Clockstone)
     |   ← Handle (Wooden stick)
     |
     |
```

#### Color Palette

1. **Main Colors**
   - **Primary**: Cyan/Light Blue (Time manipulation magic energy)
   - **Accent**: Orange/Gold (#db8813 - Matches portal theme)
   - **Handle**: Brown (Wooden stick)

2. **Decorative Elements**
   - **Pick Head**: Enhanced Clockstone crystal material texture
   - **Energy Effect**: Faint glow/aura around the head
   - **Clock Motif**: Small gears or clock hands decoration (optional)
   - **Space Distortion**: Visual effect showing space warping around pick head

### Texture Options

#### Option 1: Diamond Pickaxe Base + Time Effect
```
Based on vanilla diamond_pickaxe.png:
- Pick Head: Cyan/Light Blue (Time magic)
- Handle: Standard brown (Wooden stick)
- Glow Effect: Orange/Gold glow on pick head edges
```

#### Option 2: Enhanced Clockstone Style
```
Emphasizing Enhanced Clockstone material:
- Pick Head: Same crystal texture as Enhanced Clockstone
- Clock hand decorations
- Energy lines running down the handle
```

### Technical Specifications

- **Format**: PNG image data, 16 x 16, 8-bit/color RGBA, non-interlaced
- **Location**: `common/src/main/resources/assets/chronosphere/textures/item/spatially_linked_pickaxe.png`
- **Model**: `common/src/main/resources/assets/chronosphere/models/item/spatially_linked_pickaxe.json`

### Creation Steps

1. **Reference Vanilla Texture**
   - Check Minecraft's assets/minecraft/textures/item/diamond_pickaxe.png

2. **Modify Colors**
   - Diamond blue → Brighter cyan/light blue
   - Add orange/gold accent (#db8813)

3. **Add Decorations (Optional)**
   - Small clock hands
   - Energy glow
   - Emphasize crystal texture

### Current Status

**Placeholder**: Currently using enhanced_clockstone.png as placeholder texture
**TODO**: Create custom texture based on the design above

---

## Future Items to Design

### Time Clock (タイムクロック)
- Utility item for canceling mob AI
- Clock/pocket watch appearance
- Gold/bronze mechanical theme

### Time Guardian Mail (時の番人のメイル)
- Ultimate chestplate armor
- Time Guardian boss theme
- Crystalline armor plates with temporal energy

### Chronoblade (クロノブレード)
- Ultimate sword
- Time manipulation blade
- Blue energy blade with clock motifs

---

**Last Updated**: 2025-11-02
**Phase**: User Story 2 (T105-T109 Implementation)
