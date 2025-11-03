# Research Findings: Chronosphere Mod

## Decision 1: Minecraft Version

**Decision**: Minecraft Java Edition 1.21.1 with NeoForge 21.1.x

**Rationale**:
- **Community Standard (2025)**: Version 1.21.1 has become the community focal point for mod development in 2025, similar to how 1.20.4 was the standard for the 1.20.x series. The community follows a pattern of stabilizing around the .1 minor version until the next major release (1.22).
- **Active Support**: NeoForge continues active development for 1.21.1 with strong recommendation from the NeoForge team that modders maintain support for this version. As of 2025, the 1.21.1 NeoForm artifact has been requested over 1.2 million times.
- **Java Requirements**: Minecraft 1.21.1 requires Java 21 (bundled with Microsoft's OpenJDK since 1.21.8), providing access to modern Java features and performance improvements.
- **Stability**: With 1.21.1 reaching 92 NeoForge versions and being actively maintained throughout 2025, it represents a stable foundation with sufficient maturity for production mod development.
- **Tooling Support**: Major mod development tools like MCreator have prioritized 1.21.x support, with 1.20.x support being phased out or moved to plugin-only support.

**Alternatives Considered**:
- **Minecraft 1.20.4**: While previously the community standard with 238 NeoForge versions, it is being phased out in 2025. MCreator has dropped native support for 1.20.1 in their 2025.1 release. Using 1.20.4 would limit access to newer Minecraft features and eventually face reduced community support.
- **Minecraft 1.21.4**: Too bleeding-edge for production use. While NeoForge supports it, the version introduces breaking changes (e.g., tag system sensitivity that crashes on missing tag references). Better suited for experimental development rather than a stable production mod.

## Decision 2: Mod Loader

**Decision**: NeoForge 21.1.x

**Rationale**:

### API Capabilities for Complex Mods
- **Dimension/Portal Management**: NeoForge provides comprehensive dimension and portal APIs through official documentation (https://docs.neoforged.net). The Custom Portal API Reforged library (NeoForge port of Fabric's Custom Portal API) offers extensive customization for portal frames, blocks, tinting, ignition sources, and destinations.
- **World Generation**: Excellent support for custom world generation through Biome Modifiers (data-driven system for injecting PlacedFeatures, modifying mob spawns, climate, and foliage/water colors) and Datapack Registries for programmatic JSON generation.
- **Event Handling**: Mature event system designed for complex mods, with extensive hooks for entity behavior, world events, and gameplay mechanics.
- **Ecosystem for Large Mods**: Forge's (and by extension NeoForge's) history of supporting large-scale overhaul mods like Tinker's Construct and Immersive Engineering demonstrates proven capabilities for complex projects.

### Performance in Multiplayer
- **Improved over Legacy Forge**: NeoForge addresses the performance bottlenecks and developer frustrations accumulated over Forge's decade-long history. It is "significantly more stable and has less impact on performance" than Forge.
- **Server Optimization Ecosystem**: Active development of performance optimization tools throughout 2025, including:
  - Extreme Optimization! modpack (published Feb 19, 2025)
  - Connectivity mod for multiplayer experience enhancement
  - ModernFix for memory optimization in large modpacks
  - Server-specific optimization tools like Spark for performance testing
- **Production-Ready**: Multiple server-oriented optimization modpacks demonstrate NeoForge's viability for multiplayer environments.

### Community Support & Documentation
- **Official Documentation**: Comprehensive documentation at https://docs.neoforged.net covering registries, resources, biome modifiers, and worldgen.
- **Active Development**: The NeoForged project shows strong activity in 2025 with continuous version releases and community engagement.
- **Mod Availability**: "More and more mods progressively moving over to it since Minecraft 1.20.4" - growing ecosystem with migration from legacy Forge.
- **Community Resources**: Established tutorial content (e.g., McJty's dimension/portal guides for 1.18+ applicable to NeoForge).

### Long-term Stability
- **Maintained Fork**: NeoForge is the actively maintained successor to MinecraftForge for modern Minecraft versions, with institutional backing from the modding community.
- **Version Support**: Demonstrated commitment to supporting multiple concurrent versions (1.20.4, 1.21.1, 1.21.4) while recommending stability on established versions.
- **Migration Path**: Being a Forge fork, it maintains API compatibility concepts, allowing potential code reuse and community knowledge transfer.

**Alternatives Considered**:

- **Fabric**:
  - **Pros**: Superior raw performance (significantly higher FPS, faster loading, lower memory usage), lightweight architecture, rapid update cycle (days/hours vs weeks/months), excellent testing framework (Fabric Loader JUnit + GameTest with 2025 Client Test API updates).
  - **Cons**: Smaller mod library, less suited for large-scale overhaul mods, requires more low-level implementation work for complex systems. While performance is better, the maturity of APIs for complex dimension/portal/worldgen management is less proven than NeoForge.
  - **Why Rejected**: For a complex mod with custom dimensions, entities, world generation, and event handling, NeoForge's mature ecosystem and proven track record with similar large-scale mods outweighs Fabric's performance advantages. The Custom Portal API Reforged and comprehensive worldgen documentation make development faster despite slightly lower runtime performance.

- **Legacy Forge**:
  - **Pros**: Largest mod library, most extensive history and community knowledge.
  - **Cons**: "Significantly more heavy, leading to lower peak FPS", slower adaptation to Minecraft updates, accumulated technical debt over a decade. Performance issues particularly problematic for multiplayer servers.
  - **Why Rejected**: NeoForge is the designated successor that maintains the API familiarity while fixing performance and stability issues. Using legacy Forge would mean accepting known problems that NeoForge was created to solve.

## Decision 3: Testing Framework

**Decision**: Hybrid Approach - JUnit 5 + Fabric GameTest Framework (via test environment)

**Rationale**:

### Testing Strategy for Complex Mod Requirements

For a mod with custom dimensions, boss fights, item effects, and dimension travel, testing requirements span multiple layers:

1. **Unit Testing**: Component logic (damage calculations, cooldown timers, portal location algorithms)
2. **Integration Testing**: Feature interactions (boss mechanics, dimension travel flows, item effect chains)
3. **Gameplay Testing**: End-to-end scenarios (complete boss fights, dimension transition sequences)

### Framework Selection

**Primary Framework: JUnit 5**
- Industry-standard Java testing framework with excellent IDE integration
- Essential for unit testing utility classes, helper methods, and isolated component logic
- Can be integrated with NeoForge environment through mcjunitlib (https://github.com/alcatrazEscapee/mcjunitlib)
- mcjunitlib launches JUnit tests within the transforming class loader environment inside a running MinecraftServer instance, solving Minecraft's runtime byte-code modification (Mixin) challenges

**Integration Framework: Minecraft GameTest**
- Minecraft's official testing framework for in-world scenarios
- NeoForge provides access to the Game Test Framework (similar to Forge's implementation documented at https://gist.github.com/SizableShrimp/60ad4109e3d0a23107a546b3bc0d9752)
- Structure-based testing approach: save test scenarios using structure blocks, reference .nbt files from test methods
- Ideal for boss fights: set up arena conditions, spawn bosses, simulate combat, verify phase transitions and victory conditions
- Supports both server-side and client-side feature testing

### Testing Best Practices for This Mod

**Dimension Travel Testing**:
- Unit tests: Portal location algorithms, dimension registry logic
- GameTest: Structure-based tests for portal activation, transition mechanics, spawn location validation

**Boss Fight Testing**:
- Unit tests: Damage calculations, health scaling, attack pattern logic
- GameTest: Arena setup via structure blocks, complete encounter flows, reward distribution verification
- Test method annotations with TestContext for success/fail assertions

**Item Effect Testing**:
- Unit tests: Effect duration calculations, stack behavior, cooldown management
- GameTest: Player interaction simulation, effect application/removal in live world context

### Automation vs Manual Testing

**Automated Testing (80% of coverage)**:
- JUnit for all unit-testable logic
- GameTest for repeatable integration scenarios
- Run with `-Dfabric-api.gametest` JVM argument (similar approach works for NeoForge)
- CI/CD integration possible for regression prevention

**Manual Testing (20% for UX validation)**:
- Visual effects and animations
- Player experience and feel
- Balance tuning (difficulty, rewards)
- Multiplayer-specific behavior and synchronization edge cases

### Development Workflow

1. Write unit tests first for core logic (TDD approach)
2. Implement GameTest structures for complex scenarios as features develop
3. Use structure blocks to capture test scenarios during manual testing sessions
4. Automate discovered edge cases as regression tests
5. Maintain test separation: unit tests in `src/test/java`, GameTest structures in `src/test/resources`

**Alternatives Considered**:

- **Pure Manual Testing**:
  - **Pros**: No framework setup overhead, fastest for initial prototyping.
  - **Cons**: No regression protection, time-consuming for repeated scenarios (every boss fight requires manual arena setup), error-prone for complex multi-step scenarios like dimension travel chains.
  - **Why Rejected**: For a complex mod with intricate mechanics, manual-only testing would become a development bottleneck and make refactoring risky.

- **Only JUnit (No GameTest)**:
  - **Pros**: Simpler setup, familiar to all Java developers.
  - **Cons**: Cannot test in-world scenarios like boss fights without extensive mocking. Boss AI, dimension transitions, and portal mechanics require actual Minecraft world context.
  - **Why Rejected**: Boss fights and dimension travel are core features that require world simulation. Mocking the entire Minecraft world would be more complex than using GameTest.

- **McTester (Sponge Integration Framework)**:
  - **Pros**: Automated player simulation, directs real single-player client to perform actions.
  - **Cons**: Designed primarily for Sponge API (server-side plugin framework), integration with NeoForge client-side features uncertain. Less documentation for mod development compared to GameTest.
  - **Why Rejected**: While powerful for integration testing, GameTest is officially supported by Minecraft and has better NeoForge integration. McTester adds unnecessary complexity for our use case.

- **Fabric Loader JUnit + Client Test API**:
  - **Pros**: Excellent framework with 2025 updates (world creation API, screenshot API with golden image comparison, input simulation, network synchronization).
  - **Cons**: Fabric-specific, would require maintaining separate Fabric test environment alongside NeoForge development.
  - **Why Rejected**: Maintaining cross-loader compatibility just for testing adds development overhead without sufficient benefit. NeoForge's GameTest support is adequate for our needs.

## Decision 4: Multi-Loader Architecture (Updated)

**Decision**: Architectury Framework with NeoForge + Fabric support

**Rationale**:

**プロジェクト要件による採用決定:**
- より広範なプレイヤー層へのリーチ（NeoForgeとFabricの両ユーザー）
- 将来的なローダー追加の柔軟性
- コードの再利用性向上

**Architectury 1.21.1対応:**
- Architectury API: 13.0.8+fabric / 13.0.8+neoforge
- Architectury Loom: 1.7-SNAPSHOT以上
- Architectury Plugin: 3.4-SNAPSHOT以上
- プロジェクト構造: common / fabric / neoforge の3モジュール

**既知の課題と対処方針:**
1. **エンティティレンダリング問題** (Issue #641):
   - NeoForge環境でEntityRendererRegistryが正常動作しない既知の問題
   - 対処: プラットフォーム固有モジュール(neoforge/)で手動イベント登録を実装

2. **ローダー固有APIの統合**:
   - Custom Portal API: NeoForge版とFabric版を各プラットフォームモジュールで統合
   - Biome Modifiers: Architectury BiomeModifications APIで抽象化

3. **開発複雑性の管理**:
   - common/で共通ロジックを最大化（80%目標）
   - プラットフォーム固有実装は最小限に抑制（20%）
   - @ExpectPlatformアノテーションで環境依存コードを明示的に分離

**プロジェクト構造:**
```
project/
├── common/           # ローダー非依存コード（共通ロジック）
│   ├── src/main/java/
│   │   └── com/chronosphere/
│   │       ├── core/           # ディメンション、ポータルロジック
│   │       ├── blocks/         # ブロック定義
│   │       ├── items/          # アイテム定義
│   │       ├── entities/       # エンティティ定義
│   │       └── platform/       # @ExpectPlatform抽象化
│   └── src/main/resources/
│       └── data/chronosphere/  # 共通データパック
├── fabric/           # Fabric固有実装
│   ├── src/main/java/
│   │   └── com/chronosphere/fabric/
│   │       ├── ChronosphereFabric.java
│   │       └── platform/       # @ExpectPlatform実装
│   └── src/main/resources/
│       └── fabric.mod.json
├── neoforge/         # NeoForge固有実装
│   ├── src/main/java/
│   │   └── com/chronosphere/neoforge/
│   │       ├── ChronosphereNeoForge.java
│   │       └── platform/       # @ExpectPlatform実装
│   └── src/main/resources/
│       └── META-INF/neoforge.mods.toml
├── build.gradle      # ルートビルド設定
└── settings.gradle   # マルチモジュール設定
```

**Alternatives Considered**:

- **NeoForge単独開発**:
  - **Pros**: シンプルな単一モジュール、NeoForge固有APIへのフルアクセス、既知の問題を回避
  - **Cons**: Fabricユーザーは除外、将来の拡張性が低い
  - **Why Rejected**: プロジェクト要件として両ローダー対応が優先事項

- **Fabric単独開発**:
  - **Pros**: 軽量、高パフォーマンス、迅速なアップデート
  - **Cons**: 大規模ModコミュニティはNeoForge寄り、Custom Portal APIなどの成熟したライブラリが少ない
  - **Why Rejected**: NeoForgeエコシステムの充実度とコミュニティ規模

- **完全独立実装（2つのMod）**:
  - **Pros**: 各ローダーに最適化可能、Architecturyの抽象化オーバーヘッドなし
  - **Cons**: 開発コストが2倍、メンテナンス負担大、バグ修正が両方に必要
  - **Why Rejected**: 開発リソースの制約、コードの二重管理は非効率

**Implementation Strategy**:

1. **commonモジュールで実装する要素**:
   - ディメンション登録ロジック（Architectury Registry API）
   - ブロック/アイテム/エンティティの定義
   - ボスAI、ポータルロジック、時間操作メカニクス
   - イベントハンドラー（Architectury Event API）
   - データパック（レシピ、ルートテーブル、ワールド生成）

2. **プラットフォーム固有モジュールで実装する要素**:
   - Mod初期化とローダー統合
   - エンティティレンダラー登録（手動イベント登録）
   - ポータルAPI統合（Custom Portal API Reforged/Fabric版）
   - 設定ディレクトリ取得などのOS/環境依存処理
   - ローダー固有のGUIやネットワーキング拡張

3. **@ExpectPlatformパターンの活用**:
   ```java
   // common: インターフェース定義
   public class ChronospherePlatform {
       @ExpectPlatform
       public static Path getConfigDirectory() {
           throw new AssertionError();
       }
   }

   // neoforge: 実装
   public class ChronospherePlatformImpl {
       public static Path getConfigDirectory() {
           return NeoForgeLoader.getInstance()
               .getConfigDirectory().toPath();
       }
   }

   // fabric: 実装
   public class ChronospherePlatformImpl {
       public static Path getConfigDirectory() {
           return FabricLoader.getInstance()
               .getConfigDir();
       }
   }
   ```

**Risk Mitigation**:

- **エンティティレンダリング問題**: neoforge/モジュールで手動イベント登録を実装、fabric/モジュールでは標準APIを使用
- **ポータルAPI差異**: 共通インターフェースを定義し、各ローダーで適切な実装を提供
- **テストの複雑性**: 各ローダー環境で個別にGameTestを実行、commonモジュールのユニットテストは単一環境で実施
- **ビルド時間増加**: Gradle並列ビルドを有効化、CI/CDで各ローダーのビルドを並行実行

## Summary & Implementation Recommendations

### Technology Stack (Updated for Multi-Loader)
```
Minecraft: Java Edition 1.21.1
Architecture: Architectury Multi-Loader Framework
Mod Loaders: NeoForge 21.1.x + Fabric Loader 0.17.2+
Architectury API: 13.0.8
Java Version: Java 21 (OpenJDK)
Testing: JUnit 5 + Minecraft GameTest Framework (per loader)
Testing Library: mcjunitlib for JUnit integration
```

### Development Priorities

1. **Phase 1 - Foundation**: Set up NeoForge 1.21.1 development environment with JUnit integration via mcjunitlib
2. **Phase 2 - Core Systems**: Implement dimension registry and portal systems using Custom Portal API Reforged, write unit tests for portal logic
3. **Phase 3 - Worldgen**: Develop custom world generation using Biome Modifiers and Datapack Registries, create GameTest structures for biome validation
4. **Phase 4 - Entities & Combat**: Implement custom entities and boss mechanics with unit tests for calculations and GameTest for complete encounters
5. **Phase 5 - Integration**: Comprehensive integration testing of dimension travel + boss fights + item effects
6. **Phase 6 - Optimization**: Profile and optimize using Spark and other NeoForge performance tools for multiplayer scenarios

### Risk Mitigation

- **Performance Risk**: Plan for performance testing early using optimization modpacks as baseline. Monitor multiplayer performance continuously.
- **API Stability Risk**: Lock to NeoForge 21.1.x LTS rather than tracking latest bleeding-edge versions. Avoid 1.21.4+ features until next LTS emerges.
- **Testing Complexity Risk**: Start with simple GameTest examples before complex boss fight scenarios. Build test structure library incrementally.
- **Community Support Risk**: Engage with NeoForge Discord/forums early for API guidance. Contribute documentation improvements back to community.

### References

- NeoForge Documentation: https://docs.neoforged.net
- Custom Portal API Reforged: https://moddedmc.wiki/cs/project/cpapireforged/latest/docs
- mcjunitlib: https://github.com/alcatrazEscapee/mcjunitlib
- GameTest Framework: https://minecraft.wiki/w/GameTest
- McJty Dimension Tutorial: https://www.mcjty.eu/docs/1.18/ep5

## Decision 5: Portal Color Customization

**Decision**: Orange/gold-themed portal color (#db8813 / RGB: 219, 136, 19)

**Rationale**:

### Design Goal
- **Visual Differentiation**: Distinguish Chronosphere portal from all other portals (Nether: purple, End: green, Aether mod: blue)
- **Thematic Consistency**: Orange/gold color evokes clock hands, brass gears, and ancient timepieces
- **Single Color Application**: One RGB value controls all portal visual elements (simplifies customization)

### Implementation Details

**Custom Portal API Integration**:
- Both Fabric and NeoForge versions use Custom Portal API's `.tintColor(r, g, b)` method
- Located in loader-specific modules:
  - Fabric: `fabric/src/main/java/com/chronosphere/fabric/compat/CustomPortalFabric.java`
  - NeoForge: `neoforge/src/main/java/com/chronosphere/neoforge/compat/CustomPortalNeoForge.java`

**Initial Implementation** (CustomPortalFabric.java - Original):
```java
private static final int PORTAL_COLOR_R = 138;
private static final int PORTAL_COLOR_G = 43;
private static final int PORTAL_COLOR_B = 226;
```
**Initial Color**: RGB(138, 43, 226) - Blue-violet / Purple

**Attempted Implementation** (Blue theme - Rejected):
```java
private static final int PORTAL_COLOR_R = 78;
private static final int PORTAL_COLOR_G = 123;
private static final int PORTAL_COLOR_B = 236;
```
**Attempted Color**: #4e7bec / RGB(78, 123, 236) - Blue
**Why Rejected**: Discovered conflict with Aether mod's blue portal during in-game testing

**Final Implementation**:
```java
private static final int PORTAL_COLOR_R = 219;
private static final int PORTAL_COLOR_G = 136;
private static final int PORTAL_COLOR_B = 19;
```
**Final Color**: #db8813 / RGB(219, 136, 19) - Orange/Gold

**Affected Visual Elements**:
1. **Portal Block**: The visible portal surface between frame blocks
2. **Teleport Overlay**: Screen overlay effect during teleportation
3. **Particles**: Particle effects around portal frame

All three elements use the same RGB color values, ensuring consistent theming.

**Implementation Scope**:
- Simple constant update (3 integer values per loader)
- No API changes or complex logic required
- Verified through in-game testing for visual appearance
- NeoForge implementation depends on T049 (Custom Portal API integration for NeoForge)

**Alternatives Considered**:

- **Keep Purple Theme (Default)**:
  - **Pros**: Matches existing Nether portal aesthetic, familiar to players
  - **Cons**: Visual confusion with Nether portals, lacks unique identity
  - **Why Rejected**: Mod requires distinct visual identity to avoid player confusion

- **Blue Theme (#4e7bec)**:
  - **Pros**: Strong association with time/temporal themes in gaming culture
  - **Cons**: Conflicts with popular Aether mod's blue portal (discovered during testing)
  - **Why Rejected**: Must avoid confusion with widely-used dimension mods

- **Multi-Color Gradients**:
  - **Pros**: More visually striking, could create unique animated effects
  - **Cons**: Custom Portal API only supports single RGB value, would require custom rendering logic
  - **Why Rejected**: Implementation complexity outweighs benefit; single color provides sufficient differentiation

- **Green Theme**:
  - **Pros**: Alternative color differentiation
  - **Cons**: Associated with End portal, may confuse players
  - **Why Rejected**: End portal association too strong

- **Red Theme**:
  - **Pros**: Alternative color differentiation
  - **Cons**: Implies danger/nether themes, doesn't fit temporal concept
  - **Why Rejected**: Red lacks thematic connection to time/clockwork

**Related Tasks**: T034d-i (Portal Color Customization)

## Decision 6: Structure NBT Creation Process

**Decision**: Use Structure Blocks in-game to create NBT files for Ancient Ruins and Forgotten Library

**Rationale**:

### Design Goals
- **Authentic Minecraft Aesthetics**: Structures should feel like natural part of the game world
- **Gameplay Integration**: Structures must contain specific loot (blueprints, Clockstone ore, chests)
- **Size Constraints**: Java Edition limit of 48×48×48 blocks requires modular design approach

### Structure Block Workflow

**Step 1: In-Game Construction**
1. Build structure in creative mode in appropriate biome/dimension
2. Place Structure Block at corner position (lower corner recommended)
3. Set block to **Save mode** (click [D] button until [S] appears)
4. Enter structure name using namespace format: `chronosphere:structure_name`

**Step 2: Configuration**
- **Relative Position**: Offset from Structure Block to opposite corner (auto-detect with Corner mode)
- **Structure Size**: Bounding box dimensions (max 48×48×48)
- **Include entities**: Enable if structure contains mobs/items in chests
- **Remove blocks**: Disable to preserve full structure

**Step 3: Save Location**
- Saved to: `.minecraft/saves/(WorldName)/generated/chronosphere/structures/`
- File format: `structure_name.nbt` (binary NBT format)
- Copy file to mod resources: `common/src/main/resources/data/chronosphere/structures/`

**Step 4: Worldgen Integration**
Structures require 3 JSON files in `common/src/main/resources/data/chronosphere/worldgen/`:

1. **template_pool/[name].json**: Defines which NBT to place
   - `elements[].location`: Path to NBT file
   - `elements[].projection`: `rigid` (exact) or `terrain_matching` (adapts to ground)
   - `elements[].processors`: Optional modifications (replace blocks, add decay)

2. **structure/[name].json**: Configured structure feature
   - `type`: Structure type (`village` for surface, `bastion_remnant` for fixed Y)
   - `start_pool`: References template pool
   - `biomes`: Which biomes allow generation
   - `adapt_noise`: Add terrain beneath structure

3. **structure_set/[name].json**: Worldwide placement
   - `spacing`: Average chunks between structures
   - `separation`: Minimum chunk distance (must be < spacing)
   - `salt`: Unique random seed

### Design Concepts

**Ancient Ruins** (T067):
- **Location**: Overworld surface, any biome (plains/forest preferred)
- **Size**: ~30×20×30 blocks (within 48³ limit)
- **Theme**: Crumbling stone structure with moss, vines, weathering
- **Required Elements**:
  - Clockstone Ore blocks (10-20 embedded in structure)
  - Chest with Time Hourglass blueprint (Written Book item)
  - Chest with random loot (iron, gold, emeralds)
  - Decorative elements: Cracked stone bricks, cobwebs, vines
- **Generation**: Overworld, 0.1% per chunk (rare but discoverable)

**Forgotten Library** (T070):
- **Location**: Chronosphere surface, any biome
- **Size**: ~35×25×35 blocks (larger than Ancient Ruins)
- **Theme**: Ancient library with bookshelves, reading areas, decay
- **Required Elements**:
  - Portal Stabilizer blueprint (Written Book in lectern or chest)
  - Multiple bookshelves (decoration, ~20-30)
  - Chests with random loot
  - Decorative elements: Crafting tables, enchanting table, candles
- **Generation**: Chronosphere, 0.5% per chunk (higher than Ancient Ruins)

### Implementation Strategy

**Phase 1: Placeholder NBTs** (Current - T067, T070):
- Create simple test structures (5×5×5) to verify worldgen integration
- Focus: Confirm structure_set placement, biome filtering, JSON configuration

**Phase 2: Final Design**:
- Build full structures in creative world
- Test loot table generation and blueprint placement
- Verify visual quality in different biomes

**Phase 3: Iteration**:
- Adjust generation frequency based on playtesting
- Add structure variants (ruined/intact versions)
- Balance loot tables

### Technical Considerations

**Loot Tables**:
- Blueprints are Written Books with custom text (recipe instructions)
- Loot tables defined in `data/chronosphere/loot_tables/chests/`
- Reference: `ancient_ruins.json`, `forgotten_library.json`

**Worldgen Performance**:
- Structure generation is chunk-based (no performance impact after generation)
- Lower generation frequency (0.1-0.5% per chunk) prevents world clutter
- Salt values prevent overlap with vanilla/mod structures

**Multi-Loader Compatibility**:
- NBT files are platform-agnostic (work on Fabric and NeoForge)
- JSON files in `common/src/main/resources/` shared across loaders
- No loader-specific code needed for structure generation

**Size Limitations Workaround**:
- For structures >48³: Use Jigsaw blocks to combine multiple NBTs
- Not needed for Ancient Ruins or Forgotten Library (both fit in limit)

### Alternative Approaches Considered

- **External Tools (WorldEdit, MCEdit)**:
  - **Pros**: More powerful editing, copy/paste from existing worlds
  - **Cons**: Requires external tool installation, less accessible
  - **Why Rejected**: Structure Blocks are built-in and sufficient for our needs

- **Procedural Generation (Code-based)**:
  - **Pros**: Infinite variations, no NBT files needed
  - **Cons**: Complex implementation, harder to iterate on design
  - **Why Rejected**: Fixed designs are easier to balance and provide consistent experience

- **Jigsaw Structures (Multi-part)**:
  - **Pros**: Allows structures >48³, adds variation with piece randomization
  - **Cons**: More complex JSON setup, requires multiple NBT files
  - **Why Rejected**: Our structures fit within 48³ limit; complexity not justified

**Related Tasks**: T067 (Ancient Ruins NBT), T070 (Forgotten Library NBT)

## Decision 7: Day-Night Cycle vs Fixed Time

**Current Status**: Chronosphere dimension has day-night cycle (`"fixed_time": false` in dimension_type.json)

**Background**:
During gameplay testing, user observed that Chronosphere has a day-night cycle, unlike major dimension mods (Twilight Forest, Aether, Blue Skies) which use fixed time. This raises design questions about how time mechanics impact gameplay and thematic consistency.

### Analysis: Major Dimension Mods' Time Design

**Twilight Forest**:
- **Setting**: Perpetual twilight (fixed time)
- **Rationale**: Atmospheric ambiance - the dimension is defined by its eternal twilight
- **Impact**: Creates unique visual identity, no day-night cycle means no time-based survival pressure

**The Aether**:
- **Setting**: Eternal daylight (fixed time) until boss defeat
- **Rationale**: Progression mechanic - Sun Spirit boss controls the eternal day
- **Impact**: Defeating Sun Spirit unlocks day-night cycle control via Sun Altar
- **Design Philosophy**: Fixed time as gameplay obstacle, not just aesthetic choice

**Blue Skies**:
- **Everbright**: Eternal day and frigid cold
- **Everdawn**: Perpetual sunrise/dusk with warmth
- **Rationale**: Contrasting paired dimensions with distinct atmospheric identities
- **Impact**: Each dimension has unique visual/temperature characteristics

### Gameplay Impact Analysis

**Fixed Time Advantages**:
1. **Atmospheric Identity**: Creates memorable, distinct visual signature
2. **Reduced Survival Pressure**: No monster spawn cycles, more exploration-focused
3. **No Bed Spam**: Players can't skip nights, encouraging structure interaction
4. **Visual Consistency**: Screenshots/videos always show dimension's intended look
5. **Thematic Reinforcement**: Time "standing still" or "frozen" can match lore

**Day-Night Cycle Advantages**:
1. **Time Flow Representation**: Shows time is "moving" - relevant for time-themed mod
2. **Vanilla Familiarity**: Players understand existing Minecraft survival mechanics
3. **Monster Spawn Variation**: Nighttime danger adds survival challenge
4. **Bed Utility**: Beds serve their normal function (skip night)
5. **Dynamic Atmosphere**: Sunrises/sunsets add visual variety

### Thematic Considerations for Chronosphere

**Mod Theme**: Time manipulation and temporal mechanics

**Options**:

**Option A: Fixed Time (Eternal Twilight/Dusk)**
- **Pros**:
  - Aligns with "frozen in time" or "time distortion" narrative
  - Matches player expectations from other dimension mods
  - Reduces survival difficulty (no night spawns)
  - Creates unique visual identity distinct from Overworld
- **Cons**:
  - Contradicts "time manipulation" theme (time appears stopped, not manipulated)
  - Less dynamic gameplay
- **Time Value**: 6000 ticks (noon), 13000 ticks (sunset/dusk), or 18000 ticks (midnight)

**Option B: Normal Day-Night Cycle (Current)**
- **Pros**:
  - Shows time is flowing (consistent with time theme)
  - Familiar survival mechanics
  - Time Distortion Effect (Slowness IV) applies consistently regardless of time
- **Cons**:
  - Less distinct from Overworld
  - Higher survival difficulty (nighttime mobs + Slowness IV debuff)
  - No unique visual signature

**Option C: Modified Time Cycle (Special)**
- **Pros**:
  - Unique time manipulation mechanic (faster/slower/reversed cycle)
  - Reinforces time theme through actual time behavior
  - Creates memorable gameplay quirk
- **Cons**:
  - Complex implementation (requires custom time progression logic)
  - May confuse players expecting normal cycle
  - Could interfere with crop growth/mechanics
- **Implementation**: Use time speed multiplier or reversed cycle (sunset → sunrise)

**Option D: Progressive Time Unlock (Aether-style)**
- **Pros**:
  - Ties time control to mod progression
  - Creates motivation to craft time-manipulation items
  - Could unlock time control via Portal Stabilizer or new item
- **Cons**:
  - Most complex implementation
  - Requires additional items/mechanics beyond current design
  - May frustrate players if initial fixed time is inconvenient

### Recommended Decision

**Decision: Option A - Fixed Time (Bright Mysterious Atmosphere)**

**Selected Values**:
- `fixed_time`: 6000 ticks (noon) - bright but frozen time
- `effects`: `minecraft:overworld` - standard dimension behavior
- Biome `sky_color`: 9474192 (0x909090) - desaturated grey sky
- Biome `fog_color`: 12632256 (0xC0C0C0) - light grey fog

**Rationale**:
- **Bright Time**: 6000 ticks provides good visibility for exploration (not too dark)
- **Mysterious Sky**: Grey sky color (via biome definition) creates mysterious atmosphere without End dimension's dark purple tint
- **Biome-Based Color Control**: Sky/fog colors defined in biome JSON allow precise color control while keeping standard overworld effects (sun, moon, stars)
- **Frozen Time**: No day-night cycle reinforces temporal anomaly narrative
- **Future Enhancement**: Can implement sky color progression tied to mod completion (Option D element)

**Playtesting Feedback** (2025-10-27):
- Initial implementation: 13000 ticks (dusk) was too dark
- Second iteration: `effects: the_end` felt too much like End dimension
- User preference: Bright but mysterious atmosphere with custom grey sky color
- Desired aesthetics: Desaturated colors (grey sky) suggesting temporal distortion, but not End-like

**Sky Color Implementation Methods**:
1. **Dimension effects** (`effects` field in dimension_type.json):
   - `minecraft:overworld` - Normal behavior with custom biome colors (selected)
   - `minecraft:the_nether` - Red/brown atmospheric haze
   - `minecraft:the_end` - Dark grey/purple with special rendering (too distinctive)

2. **Biome colors** (in worldgen/biome/*.json):
   - `sky_color`: Direct RGB as decimal (9474192 = 0x909090 grey)
   - `fog_color`: Fog/horizon color (12632256 = 0xC0C0C0 light grey)
   - Vanilla Overworld default: sky_color 7907327 (0x78A5FF bright blue)

3. **Custom DimensionSpecialEffects** (client-side code):
   - Requires Fabric/NeoForge client module implementation
   - More control but significantly more complex
   - Not needed for simple color changes

**Color Value Reference**:
- Grey sky: 0x909090 = 9474192 (current)
- Alternative greys: 0x808080 (8421504), 0xA0A0A0 (10526880)
- Blue-grey: 0x8090A0 (8425632) - subtle cold tone
- Original blue: 0x78A5FF (7907327) - vanilla overworld

**Note**: This decision can be revisited after playtesting to evaluate:
1. Visual appeal of grey sky (may need custom sky color for better effect)
2. Optimal fixed_time value (6000 = noon, can adjust 4000-8000 for lighting preference)
3. Future implementation of sky color unlock mechanic tied to boss defeat

**Design Questions to Consider**:
1. Should Chronosphere feel **safe** (eternal day) or **challenging** (night spawns)?
2. Does "time manipulation" mean time is **active** (flowing) or **controlled** (frozen)?
3. Should time mechanics be **atmospheric** (fixed time for mood) or **mechanical** (cycle affects gameplay)?
4. Is the Time Distortion Effect (Slowness IV) sufficient challenge, or do we want nighttime danger too?

### Implementation Impact

**To Change to Fixed Time**:
```json
// chronosphere.json
"fixed_time": 6000,  // Eternal noon
// OR
"fixed_time": 13000, // Eternal sunset/dusk (recommended for "time frozen at twilight" feel)
// OR
"fixed_time": 18000  // Eternal midnight (more dangerous/mysterious)
```

**Current State** (Day-Night Cycle):
```json
"fixed_time": false,  // Normal cycle
"has_skylight": true
```

**Compatibility Notes**:
- Both Fabric and NeoForge support `fixed_time` via vanilla dimension_type.json
- No loader-specific code needed
- Change requires world regeneration or `/reload` command

### Cross-Mod Examples Summary

| Mod | Time Setting | Design Reason |
|-----|--------------|---------------|
| Twilight Forest | Fixed twilight | Atmospheric identity |
| Aether | Fixed day → Unlockable | Progression mechanic (boss reward) |
| Blue Skies (Everbright) | Fixed day | Contrasting paired dimensions |
| Blue Skies (Everdawn) | Fixed dusk | Temperature/visual theming |
| **Chronosphere (current)** | **Day-night cycle** | **Undecided** |

**Related Configuration**: `common/src/main/resources/data/chronosphere/dimension_type/chronosphere.json`

## Decision 8: Multi-Noise Biome Generation for Multiple Biomes

**Current Status**: Chronosphere uses fixed biome source (`"type": "minecraft:fixed"`) with single `chronosphere_plains` biome, causing poor ocean/land visibility

**Background**:
Players cannot distinguish between ocean and land because both use the same biome. The dimension needs multiple biomes (ocean, plains, forest) with distinct characteristics.

### Multi-Noise Parameter System

**6 Climate Parameters** (Minecraft 1.21.1):

1. **temperature**: -1.0〜1.0 (5 levels)
   - Level 0-4 ranges: -1.0~-0.45, -0.45~-0.15, -0.15~0.2, 0.2~0.55, 0.55~1.0
   - Affects biome selection only (not terrain generation)

2. **humidity (vegetation)**: -1.0〜1.0 (5 levels)
   - Level 0-4 ranges: -1.0~-0.35, -0.35~-0.1, -0.1~0.1, 0.1~0.3, 0.3~1.0
   - Affects biome selection only

3. **continentalness**: -1.0〜1.0
   - Determines ocean/beach/land biomes
   - Negative values → ocean
   - Zero/low positive → beaches/coasts
   - High positive → inland biomes

4. **erosion**: -1.0〜1.0 (7 levels)
   - Level 0-6 ranges: -1.0~-0.78, -0.78~-0.375, -0.375~-0.2225, -0.2225~0.05, 0.05~0.45, 0.45~0.55, 0.55~1.0
   - High erosion → flat terrain
   - Low erosion → mountainous terrain

5. **weirdness**: -1.0〜1.0
   - Controls terrain irregularities and ridges

6. **depth**: Vertical parameter (typically 0 for surface biomes)

7. **offset**: Fine-tuning parameter for biome priority (typically 0.0)

### JSON Structure

**File Location**: `data/<namespace>/worldgen/multi_noise_biome_source_parameter_list/<name>.json`

**Format**:
```json
{
  "preset": "minecraft:overworld",
  "biomes": [
    {
      "biome": "namespace:biome_id",
      "parameters": {
        "temperature": [min, max],
        "humidity": [min, max],
        "continentalness": [min, max],
        "erosion": [min, max],
        "depth": 0,
        "weirdness": [min, max],
        "offset": 0.0
      }
    }
  ]
}
```

### Biome Design for Chronosphere

**Planned Biomes**:

1. **chronosphere_ocean**:
   - **Purpose**: Ocean biome with clear water visibility
   - **Parameters**:
     - `continentalness`: [-1.0, -0.4] (deep ocean/ocean range)
     - `erosion`: [0.0, 1.0] (flat ocean floor)
     - `temperature`: [0.0, 0.5] (neutral)
     - `humidity`: [0.0, 0.5] (neutral)
   - **Visual**: Custom water_color for visibility, no tree features
   - **Spawns**: Water creatures (fish, squid)

2. **chronosphere_plains** (existing, will update):
   - **Purpose**: Flat land with sparse Time Wood trees
   - **Parameters**:
     - `continentalness`: [-0.1, 0.3] (coast to low inland)
     - `erosion`: [0.3, 1.0] (flat to moderately flat)
     - `temperature`: [0.0, 0.5] (neutral)
     - `humidity`: [0.0, 0.3] (low to neutral)
   - **Visual**: Existing grey sky, existing tree density
   - **Spawns**: Basic hostile/neutral mobs

3. **chronosphere_forest**:
   - **Purpose**: Dense forest with high Time Wood tree concentration
   - **Parameters**:
     - `continentalness`: [-0.1, 0.3] (same as plains for smooth transition)
     - `erosion`: [0.3, 1.0] (flat to moderately flat)
     - `temperature`: [0.0, 0.5] (neutral)
     - `humidity**: [0.4, 1.0] (high humidity for forest)
   - **Visual**: Same sky color, increased tree density
   - **Spawns**: Forest-appropriate mobs

### Implementation Strategy

**Phase 1: Biome JSON Creation** (T088e-f):
- Create `chronosphere_ocean.json` with appropriate water_color and empty tree features
- Create `chronosphere_forest.json` with increased tree placement frequency

**Phase 2: Multi-Noise Configuration** (T088g):
- Create `multi_noise_biome_source_parameter_list/chronosphere.json`
- Configure climate parameters for ocean/plains/forest distribution

**Phase 3: Dimension Update** (T088h):
- Update `dimension/chronosphere.json` to use:
  ```json
  "biome_source": {
    "type": "minecraft:multi_noise",
    "preset": "chronosphere:chronosphere"
  }
  ```

**Phase 4: Testing** (T088i):
- Verify ocean/land distinction in-game
- Check biome transitions (smooth vs abrupt)
- Validate tree distribution differences

### Expected Results

- **Ocean Visibility**: Players can clearly see where water bodies are due to distinct water_color
- **Biome Variety**: Three distinct biomes provide exploration diversity
- **Smooth Transitions**: Adjacent continentalness values prevent abrupt biome borders
- **Tree Distribution**: Plains have sparse trees, forests are dense with Time Wood

### Alternative Approaches Considered

- **Single Biome with Modified Colors**:
  - **Pros**: Simpler implementation
  - **Cons**: Cannot solve ocean visibility issue (water in plains biome still has same color)
  - **Why Rejected**: Doesn't address core problem

- **More Biomes (Desert, Mountain, etc.)**:
  - **Pros**: Greater variety
  - **Cons**: Increased complexity, requires more texture/feature work
  - **Why Rejected**: Three biomes sufficient for MVP (US1)

- **Custom BiomeSource Code**:
  - **Pros**: Full control over generation
  - **Cons**: Complex implementation, loader-specific code
  - **Why Rejected**: Multi-noise system is data-driven and sufficient

**Related Tasks**: T088d-i (Multiple Biomes Enhancement)

**Related Files**:
- `data/chronosphere/worldgen/biome/chronosphere_ocean.json`
- `data/chronosphere/worldgen/biome/chronosphere_forest.json`
- `data/chronosphere/worldgen/multi_noise_biome_source_parameter_list/chronosphere.json`
- `data/chronosphere/dimension/chronosphere.json`

## Decision 9: Decorative Terrain Features

**Purpose**: Add decorative terrain elements (boulders, fallen logs, disk features) to enhance visual diversity during exploration

**Background**:
After implementing multiple biomes, vegetation, and mob spawning, the terrain still lacks small-scale decorative elements that add visual interest and make the world feel more natural and lived-in.

### Vanilla Decorative Features Analysis

**1. Forest Rock (Boulders)**:
- **Feature Type**: `minecraft:forest_rock`
- **Blocks Used**: Cobblestone, Mossy Cobblestone
- **Generation**: Random clusters of 1-4 blocks, occasionally larger formations
- **Vanilla Usage**: Taiga, Old Growth Taiga biomes
- **Implementation**:
  ```json
  {
    "type": "minecraft:forest_rock",
    "config": {
      "state": {
        "Name": "minecraft:cobblestone"
      }
    }
  }
  ```

**2. Disk Features (Ground Patches)**:
- **Feature Type**: `minecraft:disk`
- **Blocks Used**: Sand, Gravel, Clay, Dirt
- **Generation**: Circular patches on ground surface, 2-8 block radius
- **Vanilla Usage**: Swamps (clay), Rivers (gravel), Beaches (sand)
- **Implementation**:
  ```json
  {
    "type": "minecraft:disk",
    "config": {
      "state_provider": {
        "type": "minecraft:simple_state_provider",
        "state": {
          "Name": "minecraft:gravel"
        }
      },
      "target": {
        "type": "minecraft:matching_blocks",
        "blocks": ["minecraft:grass_block", "minecraft:dirt"]
      },
      "radius": {
        "type": "minecraft:uniform",
        "value": {
          "min_inclusive": 2,
          "max_inclusive": 5
        }
      },
      "half_height": 2
    }
  }
  ```

**3. Fallen Logs**:
- **Feature Type**: No dedicated vanilla feature, but can be implemented using:
  - `minecraft:simple_block` for single horizontal logs
  - `minecraft:block_column` for vertical variations
  - Custom configuration needed
- **Blocks Used**: Any log block (horizontally oriented)
- **Generation**: 1-3 logs placed horizontally on ground
- **Vanilla Usage**: Not directly present in vanilla, but common in modded dimensions
- **Implementation Strategy**:
  ```json
  {
    "type": "minecraft:simple_block",
    "config": {
      "to_place": {
        "type": "minecraft:simple_state_provider",
        "state": {
          "Name": "chronosphere:time_wood_log",
          "Properties": {
            "axis": "x"
          }
        }
      }
    }
  }
  ```

### Feature Placement in Biomes

**Decoration Step**: `vegetation` (GenerationStep.Decoration.VEGETAL_DECORATION)
- Placed after terrain features but before structures
- Same step as flowers, grass, and other decorative vegetation
- Ensures features appear on final terrain surface

**Frequency Guidelines**:
- **Boulders**: 0-2 per chunk (rare, make them feel special)
- **Fallen Logs**: 0-1 per chunk (rare, adds occasional surprise)
- **Gravel Disks**: 1-3 per chunk (more common, adds texture variety)

### Design for Chronosphere

**Boulder Variant**:
- **Block**: Mossy Cobblestone (suggests age/abandonment)
- **Biomes**: chronosphere_plains, chronosphere_forest
- **Placement**: 0.05 probability per chunk (5% chance = 1 every 20 chunks)

**Fallen Log Variant**:
- **Block**: Time Wood Log (horizontal orientation)
- **Biomes**: chronosphere_forest only
- **Placement**: 0.03 probability per chunk (3% chance = 1 every 33 chunks)

**Gravel Disk Variant**:
- **Block**: Gravel
- **Biomes**: chronosphere_plains (creates visual breaks in grass)
- **Placement**: 0.1 probability per chunk (10% chance = 2 every 20 chunks)

### Implementation Files Required

1. **Configured Features** (`data/chronosphere/worldgen/configured_feature/`):
   - `boulder.json` - Forest rock feature with mossy cobblestone
   - `fallen_log.json` - Simple block feature with horizontal Time Wood Log
   - `gravel_disk.json` - Disk feature for gravel patches

2. **Placed Features** (`data/chronosphere/worldgen/placed_feature/`):
   - `boulder_placed.json` - Rare placement with count and probability
   - `fallen_log_placed.json` - Rare placement in forests
   - `gravel_disk_placed.json` - Common placement in plains

3. **Biome Updates**:
   - Add placed features to `features` array in biome JSONs
   - Use `vegetation` generation step

### Expected Visual Impact

- **Terrain Texture Variety**: Gravel disks break up uniform grass surfaces
- **Exploration Landmarks**: Boulders create minor navigation points
- **Natural Decay Theme**: Fallen logs and mossy boulders suggest abandoned/timeless environment
- **Biome Distinctiveness**: Forest has fallen logs, plains has gravel disks

### Alternatives Considered

- **Custom Stone Formations**:
  - **Pros**: More unique visual identity
  - **Cons**: Requires custom NBT structures or complex feature code
  - **Why Rejected**: Vanilla features sufficient for MVP enhancement

- **More Feature Types** (mushroom patches, dead bushes, etc.):
  - **Pros**: Greater variety
  - **Cons**: May clutter terrain, diminishing returns
  - **Why Rejected**: Three feature types provide sufficient diversity without overcrowding

- **Higher Placement Frequency**:
  - **Pros**: More visible impact
  - **Cons**: Risk of terrain feeling cluttered or repetitive
  - **Why Rejected**: Rare features feel more special and don't overwhelm landscape

### Performance Considerations

- **Generation Cost**: Minimal - simple block placement features
- **Runtime Cost**: None - decorative features are static blocks
- **Chunk Generation**: No noticeable performance impact expected
- **Comparison**: Similar to vanilla flower/grass generation

### Implementation Notes (2025-11-01)

**Completed Implementation**:
- **Boulder**: Uses `minecraft:forest_rock` with mossy cobblestone, 2 per chunk
- **Fallen Log**: Uses `minecraft:random_patch` with 3 tries, xz_spread=2, creates 2-3 scattered logs
- **Gravel Disk**: Uses `minecraft:disk` with radius 2-5 blocks, 3 per chunk

**Placement Adjustments**:
- Initial `rarity_filter` approach failed; switched to `count` (matching vanilla implementation)
- Added `block_predicate_filter` to prevent fallen logs from spawning on leaves or water
- Used `OCEAN_FLOOR_WG` heightmap for fallen logs to ensure ground placement
- Fixed multi_noise `continentalness` ranges to prevent forest biome in ocean (0.0+ for land biomes)

**Known Limitations**:
- Fallen logs use `random_patch`, creating scattered logs rather than straight fallen trees
- For straight fallen logs, NBT structures would be required (future improvement: T088ai-a)

**Related Tasks**: T088ad-ai (Decorative Terrain Features - Completed), T088ai-a (Future: Straight fallen logs)

**Related Files**:
- `data/chronosphere/worldgen/configured_feature/boulder.json`
- `data/chronosphere/worldgen/configured_feature/fallen_log.json`
- `data/chronosphere/worldgen/configured_feature/gravel_disk.json`
- `data/chronosphere/worldgen/placed_feature/boulder_placed.json`
- `data/chronosphere/worldgen/placed_feature/fallen_log_placed.json`
- `data/chronosphere/worldgen/placed_feature/gravel_disk_placed.json`
- `data/chronosphere/worldgen/biome/chronosphere_plains.json` (added boulder, gravel_disk)
- `data/chronosphere/worldgen/biome/chronosphere_forest.json` (added boulder, fallen_log)
- `data/chronosphere/worldgen/multi_noise_biome_source_parameter_list/chronosphere.json` (fixed continentalness ranges)

## Decision 10: Custom Portal API - Portal Placement Control

**Current Issue**: Portals spawning on Chronosphere side sometimes appear on top of trees instead of ground level

**Background**:
When players travel from Overworld to Chronosphere via portal, Custom Portal API searches for suitable return portal location. The default behavior can place portals on top of trees or other tall structures, making them inaccessible.

### Custom Portal API Methods Available (Version 0.0.1-beta66-1.21)

**Portal Placement Y-Range Control**:

1. **`setPortalSearchYRange(int bottomY, int topY)`**
   - Sets Y-level range for searching valid location to create **destination portal** (Chronosphere-side portal when traveling FROM Overworld)
   - Default: Uses world's bottom and top bounds
   - Returns: `CustomPortalBuilder` (for method chaining)

2. **`setReturnPortalSearchYRange(int bottomY, int topY)`**
   - Sets Y-level range for searching valid location to create **return portal** (Overworld-side portal when traveling FROM Chronosphere)
   - Default: Uses world's bottom and top bounds
   - Returns: `CustomPortalBuilder` (for method chaining)

**Return Dimension Configuration**:

3. **`returnDim(Identifier returnDimID, boolean onlyIgnitableInReturnDim)`**
   - Specifies dimension players return to when exiting destination portal
   - Parameters:
     - `returnDimID`: Identifier of return dimension (e.g., `minecraft:overworld`)
     - `onlyIgnitableInReturnDim`: Restricts portal ignition to return dimension only
   - Returns: `CustomPortalBuilder`

4. **`onlyLightInOverworld()`**
   - Convenience method setting portal to be ignitable exclusively in Overworld
   - Internally sets `onlyIgnitableInReturnDim` to true
   - Returns: `CustomPortalBuilder`

### Recommended Solution for Tree-Top Portal Issue

**Option A: Restrict Destination Portal Y-Range** (Recommended)

Update `CustomPortalFabric.java` to limit portal search range:

```java
CustomPortalBuilder.beginPortal()
    .frameBlock(ModBlocks.CLOCKSTONE_BLOCK.get())
    .lightWithItem(ModItems.TIME_HOURGLASS.get())
    .destDimID(dimensionId)
    .tintColor(PORTAL_COLOR_R, PORTAL_COLOR_G, PORTAL_COLOR_B)
    .setPortalSearchYRange(60, 120)  // Limit portal placement to Y=60-120
    .registerPortal();
```

**Rationale**:
- Chronosphere has fixed time at noon (Y=6000 ticks), making visibility less of an issue
- Restricting Y-range to 60-120 ensures portals spawn on ground level, not tree canopy
- Range accounts for terrain variation (plains Y=64-80, hills Y=80-120)
- Players can still build portals at any Y-level; this only affects **automatic portal creation** when traveling without existing portal

**Option B: Use Custom Portal Search Logic**

Implement custom portal placement logic using Architectury Events:
- Listen for player teleportation events
- Override default portal search behavior
- Implement ground-level detection (check for solid blocks below, air above)
- More complex but allows precise control

**Why Option A is Preferred**:
- Simple one-line configuration change
- No custom event handling required
- Sufficient for MVP (can refine later if needed)
- Compatible with both Fabric and NeoForge (when NeoForge portal integration is implemented)

### Alternative Considerations

**Custom Portal Search Algorithm**:
- Custom Portal API does **not** provide methods for custom search algorithms
- Default search algorithm: Scans Y-range from bottom to top, finds first valid air space
- "Valid" means: Air blocks for portal frame + solid blocks for frame placement
- Trees with leaves create valid spaces, hence the issue

**Biome-Specific Y-Ranges**:
- Could set different Y-ranges per biome (e.g., ocean Y=50-70, plains Y=60-80, forest Y=70-90)
- Not supported by Custom Portal API directly
- Would require custom implementation via event handlers

**Ground-Level Detection Enhancement** (Future Improvement):
- Implement custom event handler to override portal placement
- Check for solid ground blocks below portal location
- Reject locations on tree canopy or floating structures
- Ensure minimum 3-block clearance above ground
- More robust but requires significant custom code

### Implementation Impact

**Immediate Change (Option A)**:
- File: `fabric/src/main/java/com/chronosphere/fabric/compat/CustomPortalFabric.java`
- Change: Add `.setPortalSearchYRange(60, 120)` to builder chain
- Testing: Enter Chronosphere multiple times in forest biome, verify portal spawns on ground
- NeoForge: Same change needed in `neoforge/src/main/java/com/chronosphere/neoforge/compat/CustomPortalNeoForge.java` (T049)

**Y-Range Selection Rationale**:
- Bottom Y=60: Slightly below typical ground level (Y=64), allows for valley spawns
- Top Y=120: Well above tree canopy height (Time Wood trees ~10-15 blocks tall)
- Range width: 60 blocks provides sufficient search space for varied terrain

**Testing Plan**:
1. Create portal in Overworld near Time Wood forest
2. Enter Chronosphere
3. Verify return portal spawns on ground level, not on tree canopy
4. Test in multiple biomes (plains, forest, ocean shore)
5. Confirm portal remains accessible and functional

**Known Limitations**:
- Does not prevent manual portal construction at any Y-level
- Only affects automatic portal creation during initial travel
- May need adjustment if custom mountains/valleys added in future biomes

**Related Tasks**: T049a (Fix Chronosphere-side portal placement)

**References**:
- Custom Portal API GitHub: https://github.com/kyrptonaught/customportalapi
- CustomPortalBuilder source: https://github.com/kyrptonaught/customportalapi/blob/1.19.4/src/main/java/net/kyrptonaught/customportalapi/api/CustomPortalBuilder.java
- Documentation: https://moddedmc.wiki/en/project/cpapireforged/latest/docs/basicportal

## Decision 11: Mob Spawner NBT Format and Troubleshooting (Minecraft 1.21.1)

**Purpose**: Document correct NBT format for mob spawners in custom structures and common issues preventing spawner functionality

**Background**:
Structure NBT files created with structure blocks may contain mob spawners that don't function properly when placed in custom dimensions. Understanding the correct NBT format for Minecraft 1.21.1 is critical for debugging spawner issues.

### NBT Format Changes in Minecraft 1.21+

**CRITICAL CHANGE**: Minecraft 1.21 introduced a new entity format requirement:

**Old Format (Pre-1.21)**:
```json
{
  "SpawnData": {
    "id": "minecraft:zombie"
  }
}
```

**New Format (1.21+)**:
```json
{
  "SpawnData": {
    "entity": {
      "id": "minecraft:zombie"
    }
  }
}
```

**Key Difference**: Entity data must be wrapped in an `entity:{}` compound tag within SpawnData (and SpawnPotentials).

### Complete Spawner NBT Structure (1.21.1)

**Block Entity Structure**:
```nbt
{
  id: "minecraft:mob_spawner",  // Block entity identifier
  x: 100,                        // World X coordinate (int)
  y: 64,                         // World Y coordinate (int)
  z: 200,                        // World Z coordinate (int)
  keepPacked: 0b,                // Validity flag (byte)

  // Spawner-specific tags (all optional, have defaults)
  Delay: 20s,                    // Ticks until next spawn (short)
  MinSpawnDelay: 200s,           // Min random delay between spawns (short)
  MaxSpawnDelay: 800s,           // Max random delay between spawns (short)
  SpawnCount: 4s,                // Entities to spawn per attempt (short)
  SpawnRange: 4s,                // Spawn radius in blocks (short)
  RequiredPlayerRange: 16s,      // Player activation distance (short)
  MaxNearbyEntities: 6s,         // Entity cap within spawn range (short)

  // Entity definition (CRITICAL - must use 1.21+ format)
  SpawnData: {
    entity: {                    // NEW: entity wrapper required in 1.21+
      id: "minecraft:zombie"
    }
  },

  // Spawn potentials (optional, for multiple mob types)
  SpawnPotentials: [
    {
      weight: 3,                 // Relative spawn probability
      data: {
        entity: {                // NEW: entity wrapper required in 1.21+
          id: "minecraft:zombie"
        }
      }
    },
    {
      weight: 1,
      data: {
        entity: {
          id: "minecraft:skeleton"
        }
      }
    }
  ]
}
```

### Default Values (When Tags Omitted)

| Tag | Default Value | Description |
|-----|---------------|-------------|
| `Delay` | 20 | Initial delay (1 second) |
| `MinSpawnDelay` | 200 | 10 seconds |
| `MaxSpawnDelay` | 800 | 40 seconds |
| `SpawnCount` | 4 | 4 entities per spawn attempt |
| `SpawnRange` | 4 | 4-block radius from spawner |
| `RequiredPlayerRange` | 16 | Player must be within 16 blocks |
| `MaxNearbyEntities` | 6 | Max 6 entities of same type nearby |

**Note**: All spawner-specific tags use **short** data type (indicated by `s` suffix in NBT).

### Common Spawner Issues in Custom Structures

**Issue 1: SpawnData/SpawnPotentials Mismatch**

**Problem**: Setting only `SpawnData` without `SpawnPotentials` causes spawner to work once, then reset to default (pig spawner).

**Explanation**:
- `SpawnData` holds the **next** entity to spawn
- After spawning, spawner picks a new entity from `SpawnPotentials` list
- If `SpawnPotentials` is missing or empty, spawner generates default list (pig spawner)

**Solution**: Always set **both** `SpawnData` and `SpawnPotentials` to the same entity:
```nbt
SpawnData: {
  entity: { id: "minecraft:zombie" }
},
SpawnPotentials: [
  {
    weight: 1,
    data: {
      entity: { id: "minecraft:zombie" }
    }
  }
]
```

**Issue 2: Old NBT Format (Pre-1.21)**

**Problem**: Spawners saved in Minecraft 1.20.x or earlier use old format without `entity:{}` wrapper.

**Symptoms**:
- Spawner appears inactive (no flames)
- No mobs spawn
- Console logs may show NBT parsing errors

**Solution**: Update NBT format to 1.21+ structure:
```nbt
// OLD (breaks in 1.21+):
SpawnData: { id: "minecraft:zombie" }

// NEW (correct for 1.21+):
SpawnData: { entity: { id: "minecraft:zombie" } }
```

**Issue 3: Player Distance (RequiredPlayerRange)**

**Problem**: Spawner only activates when player is within 16 blocks (default).

**Symptoms**:
- Spawner flames not visible from distance
- Mobs don't spawn unless player approaches
- Not actually broken, just not activated

**Solution**:
- This is **intended behavior** (vanilla mechanic)
- Increase `RequiredPlayerRange` if needed (e.g., `32s` for 32-block range)
- Spawners in custom structures should use default (16 blocks) for consistency

**Issue 4: MaxNearbyEntities Cap**

**Problem**: Spawner stops spawning when too many nearby entities exist.

**Symptoms**:
- Spawner active (flames visible) but no new mobs spawn
- Area already has many mobs of spawner's type

**Calculation**: Counts entities within a box of `(SpawnRange*2+1) × (SpawnRange*2+1) × 8` blocks centered on spawner.

**Solution**:
- Increase `MaxNearbyEntities` (e.g., `12s` for 12 entities)
- Or reduce `SpawnRange` to make counting area smaller
- Default (6) is usually sufficient for normal gameplay

**Issue 5: Custom Dimension Spawning**

**Problem**: Spawners may not work in custom dimensions depending on world generator type.

**Known Issues**:
- **Superflat Dimensions**: Structure spawners often fail in dimensions using superflat generators
- **Custom Biome Restrictions**: If custom dimension only contains custom biomes, structures may not generate
- **Height Restrictions**: Spawners placed too high or too low may fail to activate

**Solutions**:
- Ensure dimension uses **noise-based** world generation (not superflat)
- Include at least one vanilla biome in dimension's biome list
- Keep spawner Y-coordinates within reasonable range (Y=0 to Y=256)
- Test in multiple locations within dimension

### Time Distortion Effect Impact on Spawning

**Question**: Does Slowness IV effect prevent mobs from spawning?

**Answer**: **No**, Slowness effect does **not** prevent spawning.

**Analysis**:
- Slowness is applied to entities **after** they spawn (via `EntityEventHandler`)
- Spawning mechanics check:
  - Player distance (`RequiredPlayerRange`)
  - Nearby entity count (`MaxNearbyEntities`)
  - Available spawn space (air blocks)
  - Light level (for some mob types)
- Slowness affects **movement speed**, not spawn eligibility
- Chronosphere's Time Distortion Effect applies Slowness IV to hostile mobs every tick, but this happens post-spawn

**Implementation Reference** (`EntityEventHandler.java`):
```java
// Time Distortion Effect is applied AFTER entity exists in world
for (var entity : level.getAllEntities()) {
    if (entity instanceof LivingEntity livingEntity) {
        TimeDistortionEffect.applyTimeDistortion(livingEntity);
    }
}
```

**Conclusion**: If spawner isn't working, the issue is **not** Time Distortion Effect. Check:
1. NBT format (1.21+ entity wrapper)
2. SpawnData/SpawnPotentials both set
3. Player within 16 blocks
4. Nearby entity count below limit
5. Spawner placed in noise-based dimension (not superflat)

### Verification Steps for Broken Spawners

**Step 1: Extract Structure NBT**

Use NBT editing tools to inspect spawner data:
- **NBTExplorer** (Windows/Linux): https://github.com/jaquadro/NBTExplorer
- **NBT Studio** (Cross-platform): https://github.com/tryashtar/nbt-studio
- **Command-line**: `nbt` tool (Python): `pip install nbt`

**Location in Structure NBT**:
```
structure.nbt
└── palette[]           // Block palette
    └── [index]         // Block entry
        ├── Name: "minecraft:spawner"
        └── Properties: { ... }
└── blocks[]            // Block placements
    └── [index]
        ├── pos: [x, y, z]
        ├── state: <palette_index>
        └── nbt:        // Block entity NBT HERE
            ├── id: "minecraft:mob_spawner"
            ├── SpawnData: { ... }
            └── SpawnPotentials: [ ... ]
```

**Step 2: Validate NBT Format**

Check for 1.21+ compliance:
```
✓ SpawnData.entity.id exists (NOT SpawnData.id directly)
✓ SpawnPotentials[].data.entity.id exists
✓ Both SpawnData and SpawnPotentials use same entity
✓ All delay/range values are short type (e.g., "200s" not "200")
```

**Step 3: In-Game Testing**

1. Place structure in test world using `/place structure` command
2. Approach spawner (within 16 blocks)
3. Check for flames (indicates activation)
4. Wait 10-40 seconds (default spawn delay)
5. If no spawn, check nearby entity count
6. Use `/data get block ~ ~ ~` to inspect spawner NBT in-world

**Step 4: Console Log Analysis**

Check logs for errors:
- NBT parsing errors: "Invalid NBT tag type for SpawnData"
- Entity ID errors: "Unknown entity id: <entity>"
- Spawner errors: "Mob spawner at [x,y,z] failed to spawn"

**Step 5: Manual Spawner Placement**

Test with `/setblock` command to verify spawner configuration:
```
/setblock ~ ~ ~ minecraft:spawner{SpawnData:{entity:{id:"minecraft:zombie"}},SpawnPotentials:[{weight:1,data:{entity:{id:"minecraft:zombie"}}}],Delay:20s,MinSpawnDelay:200s,MaxSpawnDelay:800s,SpawnCount:4s,SpawnRange:4s,MaxNearbyEntities:6s,RequiredPlayerRange:16s}
```

If manual spawner works but structure spawner doesn't, issue is in structure NBT file.

### Fixing Spawner NBT in Structures

**Option 1: Re-save Structure (Recommended)**

1. Build structure in Minecraft 1.21.1 world
2. Place spawner blocks
3. Use `/setblock` to configure each spawner with correct 1.21+ NBT
4. Use Structure Block to save structure (Save mode)
5. Copy resulting NBT file to mod resources

**Option 2: Edit NBT File Manually**

1. Open structure NBT in NBTExplorer or NBT Studio
2. Navigate to `blocks[] -> [spawner entry] -> nbt`
3. Update `SpawnData` and `SpawnPotentials` to 1.21+ format
4. Ensure all values use correct data types (short for numbers)
5. Save and test in-game

**Option 3: Convert with Script**

Python script to convert old spawner format to 1.21+:
```python
import nbt

# Load structure
structure = nbt.NBTFile("structure.nbt", "rb")

# Find spawner blocks
for block in structure["blocks"]:
    if "nbt" in block and block["nbt"]["id"].value == "minecraft:mob_spawner":
        spawner_nbt = block["nbt"]

        # Fix SpawnData format
        if "SpawnData" in spawner_nbt and "entity" not in spawner_nbt["SpawnData"]:
            old_entity = spawner_nbt["SpawnData"]
            spawner_nbt["SpawnData"] = nbt.TAG_Compound()
            spawner_nbt["SpawnData"]["entity"] = old_entity

        # Fix SpawnPotentials format
        if "SpawnPotentials" in spawner_nbt:
            for potential in spawner_nbt["SpawnPotentials"]:
                if "data" in potential and "entity" not in potential["data"]:
                    old_entity = potential["data"]
                    potential["data"] = nbt.TAG_Compound()
                    potential["data"]["entity"] = old_entity

# Save fixed structure
structure.write_file("structure_fixed.nbt")
```

### Example: Working Zombie/Skeleton Mixed Spawner

**Complete NBT for structure**:
```nbt
{
  id: "minecraft:mob_spawner",
  Delay: 20s,
  MinSpawnDelay: 200s,
  MaxSpawnDelay: 800s,
  SpawnCount: 4s,
  SpawnRange: 4s,
  MaxNearbyEntities: 6s,
  RequiredPlayerRange: 16s,
  SpawnData: {
    entity: {
      id: "minecraft:zombie"
    }
  },
  SpawnPotentials: [
    {
      weight: 3,
      data: {
        entity: {
          id: "minecraft:zombie"
        }
      }
    },
    {
      weight: 1,
      data: {
        entity: {
          id: "minecraft:skeleton"
        }
      }
    }
  ]
}
```

**Result**: Spawns zombies 75% of the time, skeletons 25% of the time.

### Summary & Quick Reference

**Checklist for Working Spawners**:
- [ ] Use 1.21+ NBT format (`SpawnData.entity.id`, not `SpawnData.id`)
- [ ] Set both `SpawnData` and `SpawnPotentials` to same entity
- [ ] Use short data type for all numeric values (e.g., `200s`)
- [ ] Place spawner in noise-based dimension (not superflat)
- [ ] Ensure player can approach within 16 blocks
- [ ] Verify structure saves with "Include entities" enabled
- [ ] Test in-game before finalizing structure

**Most Common Mistake**: Forgetting to wrap entity data in `entity:{}` compound in 1.21+ format.

**Related Files**:
- Structure NBT files: `common/src/main/resources/data/chronosphere/structure/*.nbt`
- Entity event handler: `common/src/main/java/com/chronosphere/events/EntityEventHandler.java`
- Time Distortion Effect: `common/src/main/java/com/chronosphere/core/time/TimeDistortionEffect.java`

**References**:
- Minecraft Wiki - Block Entity Format: https://minecraft.wiki/w/Block_entity_format
- Minecraft Wiki - Spawner: https://minecraft.wiki/w/Spawner
- NBT Format Documentation: https://minecraft.wiki/w/NBT_format
- Custom Portal API: https://github.com/kyrptonaught/customportalapi (for dimension mechanics)

## Decision 11: Monster Spawner NBT Format in Minecraft 1.21.1

**Issue**: Monster spawners in Desert Clock Tower structure not spawning mobs (T095c)

**Related Task**: T095c [US2] Fix monster spawner in Desert Clock Tower

### Minecraft 1.21+ Critical NBT Format Change

**Most Important Change**: Minecraft 1.21 introduced a new entity format for spawners.

- **Old Format (1.20.x and earlier)**: `SpawnData: { id: "minecraft:zombie" }`
- **New Format (1.21+)**: `SpawnData: { entity: { id: "minecraft:zombie" } }`

**Key Difference**: Entity data must be wrapped in an `entity:{}` tag. This applies to both `SpawnData` and `SpawnPotentials`.

### Complete Spawner NBT Structure (1.21.1)

```nbt
{
  id: "minecraft:mob_spawner",
  x: 100, y: 64, z: 200,
  keepPacked: 0b,

  // Spawner-specific tags (all optional with defaults)
  Delay: 20s,                    // Ticks until next spawn
  MinSpawnDelay: 200s,           // Minimum spawn interval (10 seconds)
  MaxSpawnDelay: 800s,           // Maximum spawn interval (40 seconds)
  SpawnCount: 4s,                // Entities per spawn attempt
  SpawnRange: 4s,                // Spawn radius in blocks
  RequiredPlayerRange: 16s,      // Player activation distance
  MaxNearbyEntities: 6s,         // Max nearby entity limit

  // Entity definition (CRITICAL - 1.21+ format required)
  SpawnData: {
    entity: {                    // NEW: entity wrapper required
      id: "minecraft:zombie"
    }
  },

  // Spawn candidates (for multiple mob types, optional)
  SpawnPotentials: [
    {
      weight: 3,
      data: {
        entity: {                // NEW: entity wrapper required
          id: "minecraft:zombie"
        }
      }
    },
    {
      weight: 1,
      data: {
        entity: {
          id: "minecraft:skeleton"
        }
      }
    }
  ]
}
```

**Important**: All numeric values use **short type** (e.g., `200s`).

### Common Issues and Causes

**Issue 1: SpawnData/SpawnPotentials Mismatch**
- **Symptom**: Spawner works once then reverts to default (pig spawner)
- **Cause**: Only `SpawnData` set without `SpawnPotentials`
- **Fix**: **Set both** to same entity

**Issue 2: Old NBT Format (pre-1.21)**
- **Symptom**: Spawner inactive (no flames), no mob spawning
- **Cause**: Old format without `entity:{}` wrapper
- **Fix**: Update to 1.21+ structure

**Issue 3: Player Distance (RequiredPlayerRange)**
- **Symptom**: Doesn't work unless player within 16 blocks
- **Fix**: This is **intended behavior** (vanilla mechanic)

**Issue 4: MaxNearbyEntities Limit**
- **Symptom**: Spawner active but no new mobs spawning
- **Cause**: Too many entities of same type nearby
- **Fix**: Increase `MaxNearbyEntities` (e.g., `12s`)

**Issue 5: Custom Dimension Spawning**
- **Known Issues**:
  - Superflat dimensions: Structure spawners often fail
  - Custom biome restrictions: May not generate if only custom biomes
  - Height limits: May not work at extreme Y-levels
- **Fix**: Use noise-based world generation (not superflat)

### Time Distortion Effect Impact

**Question**: Does Slowness IV effect prevent spawning?

**Answer**: **No**, Slowness effect does NOT prevent spawning.

**Reasoning**:
- Slowness is applied **after** entity spawns (via `EntityEventHandler`)
- Spawn check only considers:
  - Player distance (`RequiredPlayerRange`)
  - Nearby entity count (`MaxNearbyEntities`)
  - Available spawn space (air blocks)
  - Light level (for some mob types)
- Slowness affects **movement speed**, not spawn conditions

**Conclusion**: If spawner not working, cause is **NOT** Time Distortion Effect. Check:
1. NBT format (1.21+ entity wrapper)
2. Both SpawnData and SpawnPotentials set
3. Player within 16 blocks
4. Nearby entity count below limit
5. Placed in noise-based dimension (not superflat)

### Spawner Verification Steps

**Step 1**: Inspect structure NBT with NBT editor
- NBTExplorer (Windows/Linux): https://github.com/jaquadro/NBTExplorer
- NBT Studio (cross-platform): https://github.com/tryashtar/nbt-studio

**Step 2**: Verify NBT format
```
✓ SpawnData.entity.id exists (not SpawnData.id directly)
✓ SpawnPotentials[].data.entity.id exists
✓ Both SpawnData and SpawnPotentials use same entity
✓ All delay/range values are short type (e.g., "200s" not "200")
```

**Step 3**: In-game testing
1. Place structure with `/place structure` command
2. Approach spawner (within 16 blocks)
3. Check for flames (indicates activation)
4. Wait 10-40 seconds (default spawn delay)
5. If no spawn, check nearby entity count

**Step 4**: Console log analysis
- NBT parse errors: "Invalid NBT tag type for SpawnData"
- Entity ID errors: "Unknown entity id: <entity>"

**Step 5**: Test with manual spawner placement
```
/setblock ~ ~ ~ minecraft:spawner{SpawnData:{entity:{id:"minecraft:zombie"}},SpawnPotentials:[{weight:1,data:{entity:{id:"minecraft:zombie"}}}],Delay:20s,MinSpawnDelay:200s,MaxSpawnDelay:800s,SpawnCount:4s,SpawnRange:4s,MaxNearbyEntities:6s,RequiredPlayerRange:16s}
```

If manual spawner works but structure spawner doesn't, issue is in structure NBT file.

### Fixing Spawner NBT

**Option 1: Recreate Structure In-Game (Recommended)**
1. Build structure in Minecraft 1.21.1 world
2. Place spawner blocks
3. Use `/setblock` to set each spawner with correct 1.21+ NBT
4. Save structure with Structure Block (Save mode)
5. Copy generated NBT file to mod resources

**Option 2: Manually Edit NBT File**
1. Open structure NBT with NBTExplorer or NBT Studio
2. Navigate to `blocks[] -> [spawner entry] -> nbt`
3. Update `SpawnData` and `SpawnPotentials` to 1.21+ format
4. Ensure all values use correct data types (short for numbers)
5. Save and test in-game

**Option 3: Convert with Script**
Python script to convert old format to 1.21+ format:

```python
import nbtlib
from nbtlib import nbt

# Load structure NBT
structure = nbtlib.load('desert_clock_tower.nbt')

# Find spawner blocks
for block in structure['blocks']:
    if block['state']['Name'] == 'minecraft:mob_spawner':
        nbt_data = block.get('nbt', {})

        # Check if old format (SpawnData.id exists directly)
        if 'SpawnData' in nbt_data and 'id' in nbt_data['SpawnData']:
            old_id = nbt_data['SpawnData']['id']

            # Convert to new format
            nbt_data['SpawnData'] = {
                'entity': {
                    'id': old_id
                }
            }

        # Convert SpawnPotentials if exists
        if 'SpawnPotentials' in nbt_data:
            for potential in nbt_data['SpawnPotentials']:
                if 'data' in potential and 'id' in potential['data']:
                    old_id = potential['data']['id']
                    potential['data'] = {
                        'entity': {
                            'id': old_id
                        }
                    }

# Save updated structure
structure.save('desert_clock_tower_fixed.nbt')
```

**Requirements**: `pip install nbtlib`

### Working Zombie/Skeleton Mixed Spawner Example

```nbt
{
  id: "minecraft:mob_spawner",
  Delay: 20s,
  MinSpawnDelay: 200s,
  MaxSpawnDelay: 800s,
  SpawnCount: 4s,
  SpawnRange: 4s,
  MaxNearbyEntities: 6s,
  RequiredPlayerRange: 16s,
  SpawnData: {
    entity: { id: "minecraft:zombie" }
  },
  SpawnPotentials: [
    {
      weight: 3,
      data: { entity: { id: "minecraft:zombie" } }
    },
    {
      weight: 1,
      data: { entity: { id: "minecraft:skeleton" } }
    }
  ]
}
```

**Result**: 75% chance for zombie, 25% chance for skeleton.

### Working Spawner Checklist

- [ ] Using 1.21+ NBT format (`SpawnData.entity.id`, not `SpawnData.id`)
- [ ] Both `SpawnData` and `SpawnPotentials` set to same entity
- [ ] All numeric values use short type (e.g., `200s`)
- [ ] Placed in noise-based dimension (not superflat)
- [ ] Player can approach within 16 blocks
- [ ] Structure saved with "Include entities" enabled
- [ ] Tested in-game before finalizing structure

**Most Common Mistake**: Forgetting to wrap entity data with `entity:{}` compound tag in 1.21+ format.

### Recommended Fix for Desert Clock Tower (T095c)

**Step-by-Step Process**:

1. **Verify Current Issue**:
   - Test Desert Clock Tower in-game: `/place structure chronosphere:desert_clock_tower`
   - Approach spawner within 16 blocks
   - Check if flames appear and mobs spawn

2. **If Spawners Not Working**:
   - **Option A** (Recommended): Recreate structure in Minecraft 1.21.1
     ```
     # In-game commands
     /setblock <x> <y> <z> minecraft:spawner{SpawnData:{entity:{id:"minecraft:zombie"}},SpawnPotentials:[{weight:1,data:{entity:{id:"minecraft:zombie"}}}]}
     ```
     - Save with Structure Block
     - Replace `desert_clock_tower.nbt` file

   - **Option B**: Edit NBT with NBTExplorer
     - Open `common/src/main/resources/data/chronosphere/structure/desert_clock_tower.nbt`
     - Find spawner blocks in `blocks[]` array
     - Update `SpawnData` and `SpawnPotentials` to 1.21+ format
     - Save and test

3. **Verification**:
   - Build mod: `./gradlew :fabric:build`
   - Test in-game with fresh world generation
   - Confirm mobs spawn from Desert Clock Tower spawners

### Related Files

- Structure NBT: `common/src/main/resources/data/chronosphere/structure/desert_clock_tower.nbt`
- Entity Event Handler: `common/src/main/java/com/chronosphere/events/EntityEventHandler.java`
- Time Distortion Effect: `common/src/main/java/com/chronosphere/core/time/TimeDistortionEffect.java`

### Final Design Decision: Abandoning Spawners in Desert Clock Tower

**Date**: 2025-11-03

**Decision**: Remove monster spawners from Desert Clock Tower structure and replace with alternative blocks.

**Rationale**:

After investigating the spawner NBT format issue and attempting to implement working spawners, practical gameplay considerations led to abandoning this approach:

1. **Light Level Constraints**:
   - Monster spawners require light level ≤11 to spawn hostile mobs
   - Desert Clock Tower's narrow interior makes it extremely difficult to maintain low light levels across the entire spawn range (4-block radius)
   - Achieving proper darkness would require significant simplification of interior design

2. **Design vs Functionality Trade-off**:
   - Maintaining low light levels would sacrifice the structure's architectural appeal
   - Interior decorations, windows, and open spaces would need to be eliminated
   - The resulting simplified design would be aesthetically unsatisfying

3. **Player Interaction Reality**:
   - Players naturally place torches while exploring structures for visibility
   - Even a single torch within the spawn range (4 blocks) would prevent spawning
   - The narrow movement corridors mean any torch placement likely disables the spawner
   - Making design sacrifices for a mechanic that players will immediately disable is counterproductive

4. **Alternative Approach**:
   - Natural mob spawning in dark areas of the structure is sufficient for challenge
   - Players can still encounter hostile mobs without relying on spawners
   - Structure design can prioritize aesthetics and exploration experience
   - Boss fight (Time Guardian on top floor) provides the intended challenge

**Implementation**:
- Updated `desert_clock_tower.nbt` to replace spawner blocks with decorative blocks (e.g., chests, decorative blocks)
- Maintained structure's architectural integrity and visual appeal
- Natural mob spawning remains available in darker areas

**Conclusion**:
Spawners are better suited for large, open structures (dungeons, fortresses) where light control is feasible. For compact, architecturally detailed structures like Desert Clock Tower, natural mob spawning provides a better balance between challenge and design quality.

**Related Task**: T095c - Marked as completed with alternative solution (spawner removal)

### References

- Minecraft Wiki - Block Entity Format: https://minecraft.wiki/w/Block_entity_format
- Minecraft Wiki - Spawner: https://minecraft.wiki/w/Spawner
- NBT Format Documentation: https://minecraft.wiki/w/NBT_format
- NBTExplorer: https://github.com/jaquadro/NBTExplorer
- NBT Studio: https://github.com/tryashtar/nbt-studio
- Structure Block Tutorial: https://minecraft.wiki/w/Structure_Block
