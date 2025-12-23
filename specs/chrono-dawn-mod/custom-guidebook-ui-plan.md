# Custom Guidebook UI Implementation Plan

**Status**: Planning (Future Implementation)
**Created**: 2025-12-23
**Priority**: Low (Patchouli provides adequate functionality)
**Estimated Effort**: 10-15 hours

---

## Executive Summary

This document outlines a plan to implement a custom, dependency-free guidebook UI system for Chrono Dawn. While Patchouli currently provides adequate functionality for both Fabric and NeoForge, a custom implementation would offer:

- **Complete control** over features and user experience
- **Zero external dependencies** (no licensing concerns)
- **Multi-loader compatibility** via Architectury abstractions
- **Lightweight implementation** (only features we actually need)

**Current Status**: Patchouli is acceptable for now. This plan serves as a roadmap for future migration if needed.

---

## Background & Motivation

### Current Situation (as of 2025-12-23)

We evaluated three guidebook systems:

| System | Fabric | NeoForge | License | Issues |
|--------|--------|----------|---------|--------|
| **Patchouli** (current) | ✅ | ✅ | CC-BY-NC-SA 3.0* | API dependency acceptable |
| **Lavender** | ✅ | ❌ | MIT | No NeoForge support |
| **GuideME** | ❌ | ✅ | LGPL-3.0 | No Fabric support |
| **GuidebookAPI** | ✅ | ✅ | MIT | 1.21 version unreleased |
| **Custom** (proposed) | ✅ | ✅ | MIT (own code) | Requires implementation |

*Patchouli license note: Using as API dependency (not Jar-in-Jar bundling) is acceptable.

### Why Custom UI?

1. **Licensing Clarity**: Complete MIT ownership, no ambiguity
2. **Long-term Sustainability**: No dependency on third-party update cycles
3. **Tailored Experience**: Only implement features Chrono Dawn actually needs
4. **Learning Opportunity**: Deep understanding of Architectury GUI systems

---

## Design Goals

### Must-Have Features

1. **Category Navigation** (4-5 categories)
   - Getting Started
   - Progression
   - Structures
   - Boss Encounters

2. **Entry Display**
   - Text rendering (no Markdown required, plain text acceptable)
   - Multi-page support (prev/next buttons)
   - Bilingual support (English + Japanese)

3. **Recipe Integration**
   - Display Minecraft recipes using vanilla UI components
   - No custom recipe renderer needed

4. **Multi-Loader Compatibility**
   - Use Architectury MenuRegistry for cross-loader GUI
   - Common codebase for 90%+ of implementation

### Nice-to-Have Features (Optional)

- Search functionality
- Bookmarks/favorites
- Progress tracking
- External link support

### Non-Goals

- **Markdown rendering** (too complex, not needed)
- **3D scene rendering** (out of scope)
- **Extensibility API** (we're the only consumer)

---

## Technical Architecture

### High-Level Structure

```
common/
  └── src/main/java/com/chronodawn/
      └── gui/
          ├── ChronicleScreen.java          # Main GUI screen
          ├── ChronicleMenu.java            # Container/menu (if needed)
          ├── widgets/
          │   ├── CategoryListWidget.java   # Left sidebar
          │   ├── EntryPageWidget.java      # Right content area
          │   └── PageNavigationWidget.java # Prev/Next buttons
          └── data/
              ├── Category.java             # Data model
              ├── Entry.java                # Data model
              └── ChronicleData.java        # JSON loader

common/src/main/resources/
  └── data/chronodawn/chronicle/
      ├── categories.json
      └── entries/
          ├── getting_started/
          │   ├── welcome.json
          │   └── time_distortion.json
          └── bosses/
              ├── time_guardian.json
              └── time_tyrant.json
```

### Data Format

**categories.json**:
```json
{
  "getting_started": {
    "title": {
      "en_us": "Getting Started",
      "ja_jp": "はじめに"
    },
    "icon": "chronodawn:time_crystal",
    "ordinal": 0
  },
  "bosses": {
    "title": {
      "en_us": "Boss Encounters",
      "ja_jp": "ボス戦"
    },
    "icon": "chronodawn:time_crystal",
    "ordinal": 3
  }
}
```

**entries/bosses/time_guardian.json**:
```json
{
  "category": "bosses",
  "title": {
    "en_us": "Time Guardian",
    "ja_jp": "タイムガーディアン"
  },
  "icon": "chronodawn:time_crystal",
  "pages": [
    {
      "text": {
        "en_us": "The Time Guardian protects the secrets of time...",
        "ja_jp": "タイムガーディアンは時間の秘密を守る..."
      }
    },
    {
      "recipe": "chronodawn:time_crystal"
    }
  ]
}
```

### GUI Implementation

**Using Architectury MenuRegistry**:
```java
// Common module
public class ChronicleScreen extends AbstractContainerScreen<ChronicleMenu> {
    private CategoryListWidget categoryList;
    private EntryPageWidget contentArea;

    @Override
    protected void init() {
        super.init();

        // Left sidebar: Category list
        this.categoryList = new CategoryListWidget(
            this.leftPos + 10,
            this.topPos + 20,
            100,
            this.height - 40
        );

        // Right content: Entry pages
        this.contentArea = new EntryPageWidget(
            this.leftPos + 120,
            this.topPos + 20,
            this.width - 140,
            this.height - 40
        );
    }

    @Override
    public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        super.render(graphics, mouseX, mouseY, partialTick);
        this.categoryList.render(graphics, mouseX, mouseY, partialTick);
        this.contentArea.render(graphics, mouseX, mouseY, partialTick);
    }
}
```

**Registration (Fabric)**:
```java
// Fabric module
public class ChronoDawnFabric implements ModInitializer {
    @Override
    public void onInitialize() {
        // ...

        // Register Chronicle Book item
        UseItemCallback.EVENT.register((player, level, hand) -> {
            ItemStack stack = player.getItemInHand(hand);
            if (stack.is(ModItems.CHRONICLE_BOOK.get())) {
                if (level.isClientSide) {
                    Minecraft.getInstance().setScreen(new ChronicleScreen());
                }
                return InteractionResult.SUCCESS;
            }
            return InteractionResult.PASS;
        });
    }
}
```

**Registration (NeoForge)**:
```java
// NeoForge module
@Mod(ChronoDawn.MOD_ID)
public class ChronoDawnNeoForge {
    public ChronoDawnNeoForge() {
        // ...

        NeoForge.EVENT_BUS.addListener(this::onRightClickItem);
    }

    private void onRightClickItem(PlayerInteractEvent.RightClickItem event) {
        ItemStack stack = event.getItemStack();
        if (stack.is(ModItems.CHRONICLE_BOOK.get())) {
            if (event.getLevel().isClientSide()) {
                Minecraft.getInstance().setScreen(new ChronicleScreen());
            }
            event.setCanceled(true);
        }
    }
}
```

---

## Implementation Phases

### Phase 1: Basic Structure (3-4 hours)

- [x] Create data models (`Category`, `Entry`, `Page`)
- [x] Implement JSON loader (`ChronicleData.java`)
- [x] Create basic `ChronicleScreen` with layout
- [x] Test with 1 category and 1 entry

**Deliverable**: Screen opens, displays single category/entry

### Phase 2: Category Navigation (2-3 hours)

- [x] Implement `CategoryListWidget`
- [x] Handle category selection
- [x] Load entries for selected category
- [x] Category icon rendering

**Deliverable**: Can navigate between categories

### Phase 3: Entry Display (3-4 hours)

- [x] Implement `EntryPageWidget`
- [x] Text rendering with word wrap
- [x] Multi-page support (prev/next buttons)
- [x] Recipe display integration

**Deliverable**: Can read multi-page entries with recipes

### Phase 4: Bilingual Support (1-2 hours)

- [x] Detect client language
- [x] Load appropriate language text
- [x] Fallback to English for missing translations

**Deliverable**: Japanese and English both work

### Phase 5: Content Migration (2-3 hours)

- [x] Convert all Patchouli entries to JSON format
- [x] Verify all content displays correctly
- [x] Test both languages

**Deliverable**: Feature parity with Patchouli content

### Phase 6: Polish & Testing (1-2 hours)

- [x] Fix any rendering bugs
- [x] Optimize performance
- [x] Test on both loaders
- [x] User testing feedback

**Deliverable**: Production-ready guidebook

---

## Technical Considerations

### GUI Rendering

- Use `GuiGraphics` for all rendering (Minecraft 1.21+)
- Leverage vanilla `Font` class for text
- Reuse vanilla widgets where possible (`Button`, `EditBox`, etc.)

### Data Loading

- Load JSON from `data/chronodawn/chronicle/` at game startup
- Cache parsed data in memory
- Reload on F3+T (resource pack reload)

### Cross-Loader Compatibility

- Put **90%** of code in `common/` module
- Only loader-specific: Item right-click event registration
- Use Architectury's event abstractions where possible

### Performance

- Lazy-load entry content (only load visible pages)
- Pre-render text layout for caching
- Limit widget updates to 20 TPS

---

## Alternative Approaches

### Option A: Wait for GuidebookAPI 1.21

**Pros**:
- MIT license
- Multi-loader support
- Maintained by community

**Cons**:
- Release timeline unknown
- May not meet our needs
- Still external dependency

**Recommendation**: Monitor but don't block on this

### Option B: Fork Lavender for NeoForge

**Pros**:
- Proven codebase
- Feature-rich

**Cons**:
- Complex Markdown rendering
- Maintenance burden of fork
- Over-engineered for our needs

**Recommendation**: Not worth the effort

### Option C: Minimal UI (No Guidebook)

**Pros**:
- Zero implementation cost
- External docs (Wiki/GitHub) only

**Cons**:
- Poor user experience
- Inconsistent with modern mods

**Recommendation**: Unacceptable UX degradation

---

## Decision Matrix

| Criteria | Patchouli (Current) | Custom UI | GuidebookAPI (Wait) |
|----------|---------------------|-----------|---------------------|
| **Multi-Loader** | ✅ Excellent | ✅ Excellent | ✅ Excellent |
| **License** | ⚠️ Acceptable* | ✅ Perfect (MIT) | ✅ Perfect (MIT) |
| **Maintenance** | ✅ No work | ⚠️ Our responsibility | ✅ No work |
| **Features** | ✅ Rich | ⚠️ Basic (sufficient) | ❓ Unknown |
| **Effort** | ✅ Done | ❌ 10-15 hours | ⏳ Blocked |
| **Timeline** | ✅ Available now | ⏳ 1-2 weeks | ❓ Unknown |

*API dependency usage is acceptable per Patchouli documentation

---

## Recommendation

### Short-Term (Now)

**Use Patchouli** for initial releases:
- License concern resolved (API usage is acceptable)
- Both loaders supported
- Feature-complete
- Zero implementation cost

### Long-Term (Post 1.0 Release)

**Consider custom UI migration** if any of these occur:
1. Patchouli stops being maintained
2. Breaking changes in future Minecraft versions
3. Need features Patchouli doesn't support
4. GuidebookAPI 1.21 releases and proves inadequate

**Trigger for implementation**:
- Patchouli incompatibility with new Minecraft version
- Community requests for custom features
- Spare development capacity (post-content completion)

---

## Resources

### Documentation

- [Architectury Menus API](https://docs.architectury.dev/api/menus)
- [Architectury Tutorial Series](https://larsensmods.de/2025/architectury-introduction/)
- [Minecraft GUI Programming Guide](https://minecraft.wiki/w/Tutorials/Creating_a_GUI)

### Reference Implementations

- **Patchouli**: https://github.com/VazkiiMods/Patchouli
- **Lavender**: https://github.com/wisp-forest/lavender
- **GuideME**: https://github.com/AppliedEnergistics/GuideME

### Community

- Architectury Discord: https://discord.gg/architectury
- Fabric Discord: https://discord.gg/v6v4pMv
- NeoForge Discord: https://discord.neoforged.net/

---

## Conclusion

While a custom guidebook UI is technically feasible and would provide long-term benefits, **Patchouli remains the pragmatic choice** for now:

1. ✅ Works today on both loaders
2. ✅ License acceptable for API dependency usage
3. ✅ Feature-complete
4. ✅ Zero implementation cost

**Custom UI should be revisited** when:
- Patchouli becomes unmaintained, OR
- Breaking changes force migration, OR
- Development capacity available post-1.0

This plan serves as a **detailed roadmap** for that future migration, ensuring we're prepared if/when the need arises.

---

**Next Steps**:
1. Ship initial releases with Patchouli
2. Monitor Patchouli maintenance status
3. Re-evaluate after 1.0 release based on user feedback
4. Implement custom UI only if justified by concrete need

**Document Version**: 1.0
**Last Updated**: 2025-12-23
**Author**: Claude (via ksoichiro)
