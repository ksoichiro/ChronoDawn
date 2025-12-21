# Feature Specification: Chrono Dawn Mod - 時間操作をテーマにしたMinecraft Modの開発

**Branch**: `main`
**Created**: 2025-10-18
**Status**: Draft
**Input**: User description: "Minecraftのmodを開発してください。ベースとなるアイデアは @DRAFT.md に記述されています。"

## Game Design Philosophy *(critical design decisions)*

This section documents critical design decisions that define the core gameplay experience of Chrono Dawn Mod. These decisions fundamentally impact implementation and player experience quality.

### Design Decision 1: Respawn Mechanics - End Dimension Approach (2025-10-26)

**Background**:
Initial designs considered complex custom respawn logic where "Portal Stabilizer possession determines respawn location." However, implementation verification revealed this approach had issues with both game balance and implementation complexity.

**Decision**:
Chrono Dawn dimension respawn behavior **follows Minecraft's standard mechanics completely (same as End dimension)**.

**Core Principle - "End-like Balance"**:
```
Entry:    One-way portal (Time Hourglass required) → Creates tension
Return:   Portal Stabilizer makes it bidirectional → Player achievement
Death:    Normal Minecraft respawn (bed/anchor or world spawn) → Fair difficulty
Escape:   Break bed and die to return to Overworld → Always possible
```

**Rationale**:

1. **Balanced Difficulty**
   - ❌ Bad design: Player trapped in Chrono Dawn on death → Can be stuck without resources
   - ✅ Good design: Respawn at bed/world spawn on death → Always escapable

2. **Maintains Tension**
   - One-way portal itself creates sufficient tension
   - Players carefully prepare before entering
   - Even if death allows escape, Portal Stabilizer is still needed for convenient travel

3. **Player Agency**
   - Set bed in Chrono Dawn → Establish base and continue exploration
   - Break bed and die → Emergency escape mechanism
   - Craft Portal Stabilizer → Enable convenient round trips

4. **Implementation Simplicity**
   - No custom respawn logic required
   - Leverages existing Minecraft systems
   - Reduces risk of bugs and unexpected behaviors

**Comparison with End Dimension**:

| Feature | End | Chrono Dawn |
|---------|-----|--------------|
| Entry Portal | End Portal (one-way) | Time Hourglass Portal (one-way) |
| Return Mechanism | Defeat Ender Dragon → Return Portal | Use Portal Stabilizer → Bidirectional |
| Death Respawn | Bed/World Spawn (standard) | Bed/World Spawn (standard) |
| Escape Method | Break bed + die | Break bed + die |
| Tension Source | Portal is one-way ✅ | Portal is one-way ✅ |
| Difficulty Level | Moderate (can escape) | Moderate (can escape) |

**Portal Stabilizer Role** (Clarification):
- ✅ **Affects**: Portal travel (one-way → bidirectional)
- ❌ **Does NOT affect**: Respawn location (always follows Minecraft standard)

**Edge Case - "Getting Trapped"**:
- **Scenario**: Player enters Chrono Dawn, portal deactivates, no bed set
- **Old Design**: Trapped in Chrono Dawn unless Portal Stabilizer is crafted
- **New Design**: Can die and respawn in Overworld (bed/world spawn)
  - Maintains tension: Portal is still one-way, can't easily return to retrieve items
  - Prevents frustration: Always has an escape route
  - Preserves motivation: Must craft Portal Stabilizer for convenient travel

**Implementation Note**:
- No custom respawn handler (PlayerEventHandler remains placeholder)
- Chrono Dawn dimension_type.json has `"bed_works": true`
- Players respawn following vanilla Minecraft mechanics

**Design Philosophy Summary**:
> "Like the End, Chrono Dawn creates tension through one-way travel, not through punishing death. Players feel challenged but never trapped. The Portal Stabilizer is an achievement that enables convenience, not survival necessity."

---

### Design Decision 2: Portal Visual Design - Color Theme (2025-10-26)

**Background**:
Chrono Dawn Mod introduces a custom dimensional portal using the Custom Portal API. Visual differentiation from Minecraft's existing portal types (Nether's purple, End's green) and popular mod portals (Aether's blue) is critical for player recognition and mod identity.

**Decision**:
Chrono Dawn portal uses an **orange/gold color theme** with hex color **#db8813** (RGB: 219, 136, 19).

**Core Principle - "Visual Identity Through Color"**:
```
Portal Block:      Orange (#db8813) → Clear differentiation from all other portals
Teleport Overlay:  Orange (#db8813) → Consistent thematic experience
Particles:         Orange (#db8813) → Unified visual feedback
```

**Rationale**:

1. **Clear Differentiation**
   - ❌ Bad design: Purple portal → Confusion with Nether portals
   - ❌ Bad design: Blue portal → Confusion with Aether mod portals
   - ✅ Good design: Orange portal → Instantly recognizable as Chrono Dawn

2. **Thematic Consistency**
   - Orange/gold evokes imagery of clock hands and watch mechanisms
   - Warm metallic tone suggests brass gears and ancient timepieces
   - Represents both aged mechanical devices and temporal energy
   - Creates cohesive mod identity across visual elements

3. **Implementation Simplicity**
   - Single RGB value controls all portal visual elements (block, overlay, particles)
   - Custom Portal API's `.tintColor(r, g, b)` method provides straightforward implementation
   - No complex rendering logic or gradient systems required

**Color Specification**:

| Property | Value |
|----------|-------|
| Hex Color | #db8813 |
| RGB | 219, 136, 19 |
| Theme | Orange/Gold (Time/Clockwork) |

**Implementation**:
```java
// Constants in CustomPortalFabric.java and CustomPortalNeoForge.java
private static final int PORTAL_COLOR_R = 219;
private static final int PORTAL_COLOR_G = 136;
private static final int PORTAL_COLOR_B = 19;
```

**Affected Visual Elements**:
- Portal Block: The visible portal surface between frame blocks
- Teleport Overlay: Screen overlay effect during dimensional teleportation
- Particles: Particle effects around portal frame edges

**Comparison with Other Portals**:

| Portal Type | Color | Theme |
|-------------|-------|-------|
| Nether | Purple (RGB: ~138, 43, 226) | Fire/Hell |
| End | Green (particle-based) | Void/Ender |
| Aether (Mod) | Blue | Sky/Heaven |
| Chrono Dawn | Orange (#db8813) | Time/Clockwork |

**Alternative Colors Considered**:
- **Purple (Initial)**: Rejected due to confusion with Nether portals
- **Blue (#4e7bec)**: Rejected due to conflict with Aether mod portals (discovered during testing)
- **Green**: Rejected due to association with End portal
- **Red**: Rejected as it implies danger/nether themes
- **Multi-Color Gradients**: Rejected due to API limitations and implementation complexity

**Design Philosophy Summary**:
> "Chrono Dawn portal's orange/gold color theme creates instant visual recognition, evokes clockwork and temporal machinery, and ensures players never confuse it with vanilla or popular mod portals. Simplicity in implementation maintains performance while delivering clear differentiation."

---

## User Scenarios & Testing *(mandatory)*

### User Story 1 - ディメンションへの初回突入と帰還路の確保 (Priority: P1)

プレイヤーはオーバーワールドの古代遺跡でクロックストーンと時の砂時計の設計図を発見し、ポータルを作成してクロノドーンに突入する。しかし、突入直後にポータルが機能停止し、プレイヤーは閉じ込められる。プレイヤーは忘れられた図書館でポータル安定化装置の設計図を発見し、これを作成・使用することでオーバーワールドへの自由な往来を確保する。

**Why this priority**: Modの基本的なゲームループ(ディメンション往復)を確立する最重要フェーズ。これがないとプレイヤーはディメンション内で閉じ込められたままになり、Modとして成立しない。

**Independent Test**: オーバーワールドで古代遺跡を発見し、ポータルを作成してクロノドーンに突入できること。ディメンション内で生存に必要な食料(時間の果実)を入手でき、忘れられた図書館でポータル安定化装置の設計図を発見できること。安定化装置を作成してポータルを修復し、オーバーワールドとクロノドーンを自由に往来できることを確認。

**Acceptance Scenarios**:

1. **Given** プレイヤーがオーバーワールドにいる, **When** 古代遺跡を探索する, **Then** クロックストーンと時の砂時計の設計図を発見できる
2. **Given** プレイヤーが時の砂時計を作成した, **When** ポータルを起動する, **Then** クロノドーンに突入できる
3. **Given** プレイヤーがクロノドーンに突入した, **When** 突入が完了する, **Then** ポータルが機能停止し、通常の手段では帰還できない状態になる
4. **Given** プレイヤーがクロノドーンにいる, **When** 忘れられた図書館を探索する, **Then** ポータル安定化装置の設計図を発見できる
5. **Given** プレイヤーがポータル安定化装置を作成した, **When** ポータルフレームに使用する, **Then** クロノドーン次元が安定化され、壮大なエフェクト(パーティクル+サウンド)とメッセージが全プレイヤーに表示される **And** Time Hourglassでポータルを再点火できるようになる **And** ポータルを通じてオーバーワールドへ自由に帰還できるようになる
6. **Given** プレイヤーがクロノドーン内にいる, **When** 時間の果実を入手・使用する, **Then** 満腹度が回復し、採掘速度が一時的に上昇する

---

### User Story 2 - 時間操作アイテムの獲得とボス戦への準備 (Priority: P2)

プレイヤーは砂漠の時計塔を探索し、強化クロックストーンを入手する。これを使用して時間操作アイテム(タイムクロック、空間連結ツルハシなど)を作成し、戦闘と採掘の効率を向上させる。その後、時の番人(中ボス)を撃破してマスタークロックへの鍵を入手する。

**Why this priority**: P1で確立した基本ループに戦略性と進行感を追加する。時間操作アイテムはプレイヤーに「時間を操作している」という実感を与え、Modの独自性を強化する。

**Independent Test**: 砂漠の時計塔を探索して強化クロックストーンを入手できること。タイムクロックや空間連結ツルハシなどの時間操作アイテムを作成できること。タイムクロックを使用して周囲のMobの攻撃をキャンセルできること。空間連結ツルハシで採掘時にドロップが増加することを確認。時の番人を発見し、戦闘に勝利してマスタークロックへの鍵を入手できることを確認。

**Acceptance Scenarios**:

1. **Given** プレイヤーがクロノドーンにいる, **When** 砂漠の時計塔を探索する, **Then** 強化クロックストーンを入手できる
2. **Given** プレイヤーが強化クロックストーンを入手した, **When** レシピに従ってクラフトする, **Then** タイムクロック、空間連結ツルハシなどの時間操作アイテムを作成できる
3. **Given** プレイヤーがタイムクロックを所持している, **When** 使用する(クールダウンあり), **Then** 周囲のMobの次の攻撃AIルーチンが強制的にキャンセルされる
4. **Given** プレイヤーが空間連結ツルハシを所持している, **When** ブロックを破壊する, **Then** 確率でドロップアイテムが2倍になる
5. **Given** プレイヤーが時間操作アイテムを所持している, **When** 時の番人と戦闘する, **Then** 時の番人を撃破し、マスタークロックへの鍵を入手できる

---

### User Story 3 - ラスボス撃破と最終報酬の獲得 (Priority: P3)

プレイヤーはマスタークロックへの鍵を使用して最深部に到達し、ラスボス「時間の暴君」と戦闘する。暴君を撃破すると静止のコアが破壊され、クロノドーンの時間が安定化する。プレイヤーは報酬として「クロノスの瞳」を獲得し、究極のアーティファクト(クロノブレード、時の番人のメイルなど)を作成できるようになる。

**Why this priority**: Modのエンドゲームコンテンツであり、全てのストーリーと探索の集大成。P1とP2が完成していれば、これがなくてもModとして遊べるが、これがあることで完全な達成感を提供する。

**Independent Test**: マスタークロックへの鍵を使用してマスタークロック最深部に到達できること。時間の暴君と戦闘し、撃破できること。撃破後に静止のコアが破壊され、クロノスの瞳を獲得できること。クロノスの瞳と静止のコアの破片を使用して究極のアーティファクト(クロノブレード、時の番人のメイル、時間の残響ブーツ、空間連結ツルハシ、不安定な懐中時計)を作成できることを確認。

**Acceptance Scenarios**:

1. **Given** プレイヤーがマスタークロックへの鍵を所持している, **When** マスタークロックの入口で使用する, **Then** 最深部への道が開かれる
2. **Given** プレイヤーがマスタークロック最深部に到達した, **When** 時間の暴君と戦闘する, **Then** 時間の暴君を撃破できる
3. **Given** プレイヤーが時間の暴君を撃破した, **When** 撃破が完了する, **Then** 静止のコアが破壊され、クロノスの瞳と静止のコアの破片を獲得できる
4. **Given** プレイヤーがクロノスの瞳を獲得した, **When** クロノドーンに滞在する, **Then** ディメンション内のMobの速度低下がさらに強化され、探索が安定化する
5. **Given** プレイヤーが静止のコアの破片を所持している, **When** レシピに従ってクラフトする, **Then** 究極のアーティファクト(クロノブレード、時の番人のメイル、時間の残響ブーツなど)を作成できる
6. **Given** プレイヤーがクロノブレードを所持している, **When** Mobに攻撃を命中させる, **Then** 確率でMobの次の攻撃AIがスキップされ、一方的な攻撃機会が生まれる
7. **Given** プレイヤーが時の番人のメイルを装備している, **When** 致命的なダメージを受ける, **Then** 確率でダメージ前のHPと位置にロールバックされ、即死を回避できる
8. **Given** プレイヤーが時間の残響ブーツを装備している, **When** ダッシュする, **Then** 敵のターゲットを引くデコイ(残像エンティティ)が短時間召喚される
9. **Given** プレイヤーが不安定な懐中時計を所持している, **When** 使用する, **Then** 周囲のMobとプレイヤーの速度ステータス効果が瞬時に入れ替わる

---

### Edge Cases

- プレイヤーがポータル安定化装置を作成せずにクロノドーンで死亡した場合、どのように脱出するか?(リスポーン処理の設定)
- ポータル安定化はディメンション全体に適用されるため、安定化後に新しく作成したポータルも即座にTime Hourglassで点火可能になる(複数ポータル作成が自由)
- 安定化後、オーバーワールドからクロノドーンに移動した際、クロノドーン側に生成されたポータルは自動的にDEACTIVATEされない(そのまま残り、双方向移動可能)
- Portal Stabilizerは点火機能を持たず、ディメンション安定化のみを行う。点火はTime Hourglassで実施する
- 時の番人を倒さずにマスタークロックに到達しようとした場合、どうなるか?(鍵が必要なため進行不可)
- クロノブレードや時の番人のメイルの確率発動はどの程度の頻度か?(10%、25%、50%など - バランス調整が必要)
- 反転共鳴が発動した際、プレイヤーが安全地帯に逃げ込めない場合のリスク軽減策は?(効果時間の調整、警告表示など)
- 不安定な砂岩が修復される間に他のブロックが配置された場合の挙動は?(配置したブロックを破壊して元に戻す、または修復をスキップする)
- 時間の果実の採掘速度上昇効果は既存のHasteエンチャントと重複するか?(重複する、または上限を設ける)
- プレイヤーが不安定な懐中時計を連続使用した場合、速度効果が蓄積されるか?(効果の上書き、またはクールダウンの設定)

## Requirements *(mandatory)*

### Functional Requirements

- **FR-001**: システムはオーバーワールドに古代遺跡を生成し、プレイヤーがクロックストーンと時の砂時計の設計図を発見できるようにしなければならない
- **FR-002**: プレイヤーは時の砂時計を作成し、ポータルを起動してクロノドーンディメンションに突入できなければならない
- **FR-003**: プレイヤーがクロノドーンに突入した直後、ポータルは機能停止し、プレイヤーは通常の手段では帰還できない状態にならなければならない
- **FR-004**: クロノドーン内に忘れられた図書館を生成し、プレイヤーがポータル安定化装置の設計図を発見できるようにしなければならない
- **FR-005**: プレイヤーはポータル安定化装置を作成し、使用することでクロノドーンディメンションを安定化し、その後Time Hourglassでポータルを再点火することで、オーバーワールドとクロノドーンを自由に往来できるようにしなければならない
- **FR-006**: クロノドーン内の大半のカスタムMobに恒常的な極端な速度低下(Slowness IV～V相当)を付与し、プレイヤーは通常の速度で行動できなければならない
- **FR-007**: クロノドーン内に時間の果実を生成し、プレイヤーが入手・使用することで満腹度を回復し、一時的に採掘速度を上昇(Haste I相当)させなければならない
- **FR-008**: 特定のブロック(逆流の砂岩など)の破壊イベントをフックし、数秒後に元のブロックに修復する処理を実行しなければならない。修復完了までドロップアイテムの放出を停止しなければならない
- **FR-009**: 不安定な菌糸ブロックのエンティティ衝突イベントをフックし、衝突したプレイヤーにランダムな速度効果(Speed IまたはSlowness I)を0.5秒間付与しなければならない
- **FR-010**: プレイヤーが不安定な砂時計を作成した際、または中ボス・ラスボスを撃破した直後の短時間(30秒～1分)に限定して反転共鳴を発動し、プレイヤーにSlowness IV、周囲のMobにSpeed IIを付与しなければならない
- **FR-011**: クロノドーン内に砂漠の時計塔を生成し、プレイヤーが強化クロックストーンを入手できるようにしなければならない
- **FR-012**: プレイヤーは強化クロックストーンを使用してタイムクロック、空間連結ツルハシなどの時間操作アイテムを作成できなければならない
- **FR-013**: タイムクロックを使用すると、周囲のMobの次の攻撃AIルーチンを強制的にキャンセルしなければならない(クールダウンあり)
- **FR-014**: 空間連結ツルハシでブロックを破壊すると、確率でドロップアイテム生成処理を二重に実行しなければならない
- **FR-015**: クロノドーン内に中ボス「時の番人」を生成し、プレイヤーが撃破するとマスタークロックへの鍵を入手できなければならない
- **FR-016**: マスタークロックへの鍵を使用すると、マスタークロック最深部への道が開かれなければならない
- **FR-017**: マスタークロック最深部にラスボス「時間の暴君」を生成し、プレイヤーが撃破すると静止のコアが破壊され、クロノスの瞳と静止のコアの破片を獲得できなければならない
- **FR-018**: クロノスの瞳を獲得すると、クロノドーン内のMobの基本速度低下効果がさらに強化されなければならない
- **FR-019**: 静止のコアの破片を使用して究極のアーティファクト(クロノブレード、時の番人のメイル、時間の残響ブーツ、空間連結ツルハシの強化版、不安定な懐中時計)を作成できなければならない
- **FR-020**: クロノブレードで攻撃を命中させると、確率でMobの次の攻撃AIを強制的にスキップしなければならない
- **FR-021**: 時の番人のメイルを装備している状態で致命的なダメージを受けると、確率でダメージ前のHPと位置にロールバックしなければならない
- **FR-022**: 時間の残響ブーツを装備してダッシュすると、敵のターゲットを引くデコイ(残像エンティティ)を短時間召喚しなければならない
- **FR-023**: 不安定な懐中時計を使用すると、周囲のMobとプレイヤーの現在の速度ステータス効果を瞬時に入れ替えなければならない
- **FR-024**: プレイヤーがクロノドーンで死亡した際のリスポーン処理は、オーバーワールドまたはクロノドーン内の安全地点のいずれかに設定されなければならない
- **FR-025**: システムはサーバー負荷を抑えるため、既存のMinecraftシステム(ステータス効果、イベントフック)を最大限活用して時間特性を実装しなければならない

#### 新規追加要件 (Gameplay Enhancement)

- **FR-026**: クロノドーン内に時間をテーマにした独自モブ（時の亡霊、時計仕掛けの番兵、時間の管理者）を生成し、プレイヤーに多様な戦闘・取引体験を提供しなければならない
- **FR-027**: 時計仕掛けの番兵は時間歪曲効果（Slowness IV）に免疫を持ち、通常速度で動作しなければならない
- **FR-028**: 時の亡霊は攻撃を受けた際にブロックをすり抜けて逃げる特性を持ち、攻撃時にプレイヤーにSlowness IIを付与しなければならない
- **FR-029**: 時間の管理者は中立モブとして取引インターフェースを提供し、時間関連アイテムと交換可能なアイテムを提供しなければならない
- **FR-030**: クロノドーン内に時のクリスタル鉱石を生成し、プレイヤーがY座標0-48で採掘できるようにしなければならない
- **FR-031**: プレイヤーはクロックストーンと時のクリスタルを使用して基本装備セット（Tier 1: 剣・斧・シャベル・クワ・フルアーマー）を作成できなければならない
- **FR-032**: プレイヤーは強化クロックストーンと時のクリスタルを使用して上位装備セット（Tier 2: Enhanced Clockstone装備）を作成できなければならない
- **FR-033**: Tier 2の剣は攻撃時に確率で敵を2秒間凍結させ、Tier 2のフルアーマー装備時には時間歪曲効果への完全免疫を付与しなければならない
- **FR-034**: プレイヤーは時間の果実を使用して加工食料（時の果実パイ、時のジャム）を作成でき、それぞれ異なるバフ効果を得られなければならない
- **FR-035**: クロノドーン内に時の小麦を自生させ、プレイヤーが収穫・栽培して時のパンを作成できるようにしなければならない
- **FR-036**: クロノドーン内に追加バイオーム（山岳、沼地、雪原、洞窟）を生成し、プレイヤーに探索の多様性を提供しなければならない
- **FR-037**: 各バイオームに固有のブロック（時間の苔、凍結した時の氷）を生成し、バイオームの識別性を向上させなければならない
- **FR-038**: プレイヤーは装飾ブロック（時計仕掛けブロック、時のクリスタルブロック、時間のレンガ）と建築用バリエーション（階段・ハーフブロック・壁・フェンス）を作成できなければならない
- **FR-039**: マスタークロック撃破後に時間をテーマにした独自地形（時間の裂け目峡谷、浮遊する時計仕掛けの廃墟、時のクリスタル洞窟）を生成し、エンドゲームの探索要素を提供しなければならない

### Key Entities

- **クロックストーン**: クロノドーンの基本素材。古代遺跡で発見でき、ポータル作成やアイテムの基本素材として使用される。
- **時の砂時計**: クロックストーンから作成される特殊アイテム。ポータルを起動してクロノドーンへの突入を可能にする。
- **ポータル安定化装置**: 忘れられた図書館で設計図を発見し、クラフトによって作成される。機能停止したポータルを修復し、自由な往来を可能にする。
- **時間の果実**: クロノドーン内で入手できる特殊な食料。満腹度回復と一時的な採掘速度上昇効果を持つ。
- **強化クロックストーン**: 砂漠の時計塔で入手できる上位素材。時間操作アイテムのレシピに必要。
- **タイムクロック**: 強化クロックストーンから作成されるユーティリティアイテム。使用すると周囲のMobの攻撃AIをキャンセルする(クールダウンあり)。
- **空間連結ツルハシ**: 強化クロックストーンから作成されるツール。ブロック破壊時に確率でドロップアイテムが2倍になる。
- **不安定な砂時計**: 究極アイテムの素材。クラフト時に反転共鳴を誘発するリスクを伴う。
- **マスタークロックへの鍵**: 時の番人を撃破すると入手できる重要アイテム。マスタークロック最深部への道を開く。
- **静止のコアの破片**: 時間の暴君を撃破すると入手できる最重要素材。究極のアーティファクトの作成に必須。
- **クロノスの瞳**: 時間の暴君を撃破すると入手できる究極のアーティファクト。獲得後、クロノドーンのMobの速度低下がさらに強化される。
- **クロノブレード**: 静止のコアの破片から作成される究極の剣。攻撃命中時、確率でMobの次の攻撃AIをスキップする。
- **時の番人のメイル**: 静止のコアの破片から作成される究極のチェストプレート。致命的なダメージを受けた際、確率でダメージ前の状態にロールバックする。
- **時間の残響ブーツ**: 静止のコアの破片から作成される究極のブーツ。ダッシュ時に敵のターゲットを引くデコイを召喚する。
- **不安定な懐中時計**: 静止のコアの破片から作成される究極のユーティリティアイテム。使用時、周囲のMobとプレイヤーの速度ステータス効果を入れ替える。
- **古代遺跡**: オーバーワールドに生成される構造物。クロックストーンと時の砂時計の設計図を発見できる。
- **忘れられた図書館**: クロノドーン内に生成される構造物。ポータル安定化装置の設計図を発見できる。
- **砂漠の時計塔**: クロノドーン内に生成される構造物。強化クロックストーンを入手できる。
- **マスタークロック**: クロノドーン内の最深部にある構造物。時間の暴君が待ち構えている。
- **時の番人**: クロノドーン内に出現する中ボス。撃破するとマスタークロックへの鍵をドロップする。
- **時間の暴君**: マスタークロック最深部に出現するラスボス。ディメンションを永遠に静止させた元凶。撃破すると静止のコアの破片とクロノスの瞳を入手できる。
- **逆流の砂岩**: クロノドーン内に生成される特殊ブロック。破壊後数秒で元の状態に修復される。
- **不安定な菌糸**: クロノドーン内に生成される特殊ブロック。衝突したプレイヤーにランダムな速度効果を付与する。

#### 新規追加モブ (Gameplay Enhancement)

- **時の亡霊 (Temporal Wraith)**: クロノドーン内の森林・平原バイオームに出現する敵対モブ。攻撃を受けるとブロックをすり抜けて逃げる特性を持ち、攻撃時にプレイヤーにSlowness IIを付与する。
- **時計仕掛けの番兵 (Clockwork Sentinel)**: 砂漠バイオームや構造物に出現する敵対モブ。時間歪曲効果（Slowness IV）に免疫を持ち、通常速度で動く脅威。撃破すると古代の歯車（Ancient Gear）をドロップする。
- **時間の管理者 (Time Keeper)**: 忘れられた図書館周辺に出現する中立モブ。村人のように取引が可能で、時間関連アイテム（時の砂時計、ポータル安定化装置の素材など）と交換できる。

#### 新規追加装備 (Gameplay Enhancement)

- **時のクリスタル鉱石 (Time Crystal Ore)**: クロックストーン鉱石よりも希少な鉱石。Y座標0-48に生成され、鉱脈サイズは3-5ブロック。時のクリスタルを採掘できる。
- **時のクリスタル (Time Crystal)**: 時のクリスタル鉱石から採掘できる素材。装備のクラフトに使用され、耐久性を向上させる。
- **クロックストーン装備セット (Tier 1)**: クロックストーンと時のクリスタルから作成される基本装備。剣・斧・シャベル・クワ・フルアーマー（ヘルメット・チェストプレート・レギンス・ブーツ）を含む。鉄装備よりやや優れた性能。
- **強化クロックストーン装備セット (Tier 2)**: 強化クロックストーンと時のクリスタルから作成される上位装備。剣は敵を攻撃時に確率で2秒間凍結させ、フルアーマー装備時には時間歪曲効果への完全免疫を得る。

#### 新規追加食料 (Gameplay Enhancement)

- **時の果実パイ (Time Fruit Pie)**: 時間の果実3個と小麦から作成。満腹度8回復 + 30秒間Haste II効果。
- **時のジャム (Time Jam)**: 時間の果実4個と砂糖から作成。満腹度4回復 + 60秒間Speed I効果。
- **時の小麦 (Time Wheat)**: クロノドーン内の平原・森林バイオームに自生する作物。8段階の成長を経て収穫可能。バニラの小麦と同様に栽培できる。
- **時の小麦の種 (Time Wheat Seeds)**: 時の小麦を収穫すると入手。栽培に使用。
- **時のパン (Time Bread)**: 時の小麦3個から作成。満腹度5回復。

#### 新規追加ブロック (Gameplay Enhancement)

- **時計仕掛けブロック (Clockwork Block)**: 装飾用ブロック。歯車が回転するアニメーションテクスチャを持つ。
- **時のクリスタルブロック (Time Crystal Block)**: 装飾用ブロック。光レベル10を発し、時のクリスタル9個から作成される。
- **時間のレンガ (Temporal Bricks)**: 建築用ブロック。クロックストーン4個から作成。階段・ハーフブロック・壁・フェンスのバリエーションあり。
- **時間の苔 (Temporal Moss)**: 沼地バイオーム限定の装飾ブロック。バニラの苔のように周囲に広がる特性を持つ。
- **凍結した時の氷 (Frozen Time Ice)**: 雪原バイオーム限定のブロック。通常の氷と異なり溶けず、永続的に滑りやすい。

#### 新規追加バイオーム (Gameplay Enhancement)

- **クロノドーン山岳 (Chrono Dawn Mountain)**: 高標高の石地形。植生は少なく、岩がちな地形。
- **クロノドーン沼地 (Chrono Dawn Swamp)**: 水と粘土が多い地形。時間の苔が自生し、独自の植生を持つ。
- **クロノドーン雪原 (Chrono Dawn Snowy)**: 雪と氷に覆われた地形。凍結した時の氷が生成され、凍った時間をテーマにしている。
- **クロノドーン洞窟 (Chrono Dawn Cave)**: 地下バイオーム。壁に時のクリスタル鉱石が露出している。

#### 新規追加地形特性 (US3 Enhancement)

- **時間の裂け目峡谷 (Temporal Rift Canyon)**: 歪んだ地形と浮遊ブロックを持つ特殊構造物。壁に時のクリスタル鉱脈が露出している。
- **浮遊する時計仕掛けの廃墟 (Floating Clockwork Ruins)**: 壊れた時計仕掛けのメカニズムを持つ浮島構造物。ルート・チェストを含む。
- **時のクリスタル洞窟 (Time Crystal Caverns)**: 地下に生成される結晶構造物。発光効果を持つ時のクリスタルが形成されている。

## Assumptions and Dependencies

### Assumptions

- プレイヤーはMinecraftの基本操作(移動、採掘、クラフト、戦闘)に習熟していることを前提とする
- 確率発動の効果(クロノブレード、時の番人のメイルなど)の具体的な確率値は、実装後のバランス調整で決定される(推奨範囲: 10%～50%)
- 古代遺跡、忘れられた図書館、砂漠の時計塔、マスタークロックの構造物はプレイヤーが発見可能な範囲に生成される
- クロノドーン内の時間特性(Mob速度低下、ブロック修復など)はプレイヤーの探索体験を損なわない程度に調整される
- サーバー環境では複数プレイヤーが同時にクロノドーンに滞在する可能性があるが、個々のプレイヤー体験は独立している

### Dependencies

- Minecraftのバージョン: [実装時に決定 - 推奨: 最新の安定版または広く使用されているバージョン]
- Modローダー: [実装時に決定 - Forge, Fabric, NeoForgeなど]
- サーバー環境での動作を考慮した設計が必要
- 既存のMinecraftゲームシステム(ディメンション生成、ポータルメカニズム、エンティティ挙動、ステータス効果)に依存

## Success Criteria *(mandatory)*

### Measurable Outcomes

- **SC-001**: プレイヤーはオーバーワールドで古代遺跡を発見し、10分以内にクロノドーンへの突入に必要な素材と設計図を入手できる
- **SC-002**: プレイヤーはクロノドーンに突入後、30分以内にポータル安定化装置を作成し、オーバーワールドへの帰還路を確保できる
- **SC-003**: クロノドーン内のMobは通常のMobと比較して50%以上の速度低下が観察され、プレイヤーは戦略的な戦闘を行える
- **SC-004**: プレイヤーは砂漠の時計塔を探索し、20分以内に強化クロックストーンと時間操作アイテムを入手できる
- **SC-005**: プレイヤーは時の番人との戦闘を5回以内の試行で撃破し、マスタークロックへの鍵を入手できる
- **SC-006**: プレイヤーは時間の暴君との戦闘を10回以内の試行で撃破し、クロノスの瞳と静止のコアの破片を入手できる
- **SC-007**: プレイヤーは究極のアーティファクトを装備し、通常の装備と比較して戦闘効率が30%以上向上することを体感できる
- **SC-008**: サーバー負荷は既存のMinecraft環境と比較して10%以内の増加に抑えられる
- **SC-009**: プレイヤーの90%以上が、ポータル安定化装置の作成プロセスを理解し、初回プレイで帰還路を確保できる
- **SC-010**: プレイヤーの80%以上が、時間操作アイテムの効果を体感し、「時間を操作している」という独自性を評価する
