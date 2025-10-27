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
