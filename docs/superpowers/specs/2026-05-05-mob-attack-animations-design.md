# Custom Mob Attack Animations — Design

Date: 2026-05-05
Status: Approved (pending implementation plan)
Theme: Add visible attack animations to nine custom hostile mobs whose models currently freeze in their idle pose during melee strikes.

---

## 1. Background and Scope

A recent investigation (commit `31190f0a`, "fix(husk): add melee attack animation to Epoch Husk") revealed a class of dormant bug: most ChronoDawn custom hostile mobs use `EntityModel`-derived models that do not inherit vanilla's humanoid swing animation, and their `setupAnim` methods never reference `state.attackTime` (or the legacy `entity.attackAnim`). The mobs deal damage on a `MeleeAttackGoal` cycle, but the model stays frozen in its idle/walk pose, so the player has no visual cue that the strike happened.

A separate fix (commit `6ea2b5ce`, "fix(bosses): extract attack animation time in renderers (1.21.2+)") wired up `state.attackTime` extraction for the five bosses whose model code already read the field but whose renderers never populated it.

This design covers the remaining nine mobs whose model code does **not yet** reference any attack animation field at all.

### In scope

Custom-modeled hostile mobs missing all attack animation logic:

- `ChronalLeechEntity` (Monster, MeleeAttackGoal)
- `ClockworkSentinelEntity` (Monster, MeleeAttackGoal)
- `FloqEntity` (Monster, MeleeAttackGoal)
- `HourglassGolemEntity` (Monster, MeleeAttackGoal)
- `ForgottenMinuteEntity` (Monster, custom `doHurtTarget`)
- `ParadoxCrawlerEntity` (Monster, MeleeAttackGoal subclass)
- `TemporalWraithEntity` (Monster, MeleeAttackGoal)
- `TimelineStriderEntity` (Monster, MeleeAttackGoal)
- `ClockworkColossusEntity` (Boss, MeleeAttackGoal + RangedAttackMob)

For all nine, both eras of model API are in scope:
- **1.20.1, 1.21.1**: legacy multi-arg `setupAnim`, animation read from `entity.attackAnim`.
- **1.21.2 – 1.21.11**: modern single-arg `setupAnim(<RenderState>)`, animation read from `state.attackTime`.

For `ClockworkColossusEntity`, the renderer extract layer is also in scope — its `RenderState` already declares `public float attackTime` but no `extractRenderState` populates it, mirroring the boss bug fixed in `6ea2b5ce`.

### Out of scope

- **MomentCreeperEntity** — uses `SwellGoal` plus `MeleeAttackGoal`; the missing visual cue is the `swelling` size pulse, not an arm strike. Different system; deferred to its own design.
- **SecondhandArcherEntity** — `RangedBowAttackGoal` + `ArmedModel`; needs bow-draw animation tied to `isUsingItem`, not melee `attackTime`. Deferred.
- **EpochHusk and the five bosses** — already wired up by prior commits.
- **Friendly mobs** (Animal/WaterAnimal/AbstractVillager subclasses) — not melee attackers.
- **Adding new render-layer effects** (glow on attack, particle bursts, hit-flash). YAGNI — the visible cue is the body movement itself.
- **Tuning ChronalLeech wing motion or ForgottenMinute wing flap rate during attack.** Both could be enhanced later; the initial pass keeps the wing parts on their existing idle/walk curves.

---

## 2. Approach: Vanilla-Conformant Per-Mob Pattern

Three design directions were considered:

1. **Uniform Husk-style swing for all humanoids.** Lowest implementation cost, but reads as identical to the Husk and to each other — players cannot distinguish a Sentinel attack from a Strider attack at a glance.
2. **Fully bespoke animation per mob.** Highest visual quality, highest design and tuning cost. Risks inconsistency with vanilla's existing visual language for "this mob is striking."
3. **Vanilla-conformant: pick the closest vanilla mob analog for each ChronoDawn mob and reuse that animation pattern.** ← chosen.

Option 3 was selected because vanilla mobs have decade-tuned attack animations that players already recognize as the universal "attack happening now" cue. Borrowing the closest vanilla pattern keeps each ChronoDawn mob's strike legible while still differentiating the mobs from each other (golems slam overhead, zombies swing forward, spiders rear up, etc.). Implementation cost stays bounded — each animation is a 3–10 line block that piggybacks on the existing `setupAnim` body.

The cost is bounded: 9 mobs × 11 versions = 99 model files, plus 9 `ClockworkColossusRenderer` files for the extract step. Each file gets one self-contained `if (attackTime > 0) { ... }` block appended after the existing walk/idle code.

### Animation timing curve (shared)

All mobs use the same easing envelope as `EpochHuskModel`:

```java
float progress = Mth.sin(<attackField> * (float)Math.PI);
```

where `<attackField>` is `state.attackTime` (1.21.2+) or `entity.attackAnim` (1.20.1 / 1.21.1). `progress` rises from 0 to 1 and falls back to 0 over the attack window, providing smooth ease-in-ease-out without abrupt pose snaps.

---

## 3. Per-Mob Animation Specifications

| Mob | Vanilla analog | Body parts | Peak pose |
|---|---|---|---|
| ChronalLeech | Silverfish (segmented wave) | `body1`–`body7` | Each segment `xRot += 0.3F * Mth.sin((attack - i*0.15F) * PI)` for i = 0..6, producing a strike wave that travels head-to-tail |
| ClockworkSentinel | Iron Golem (overhead slam) | `left_arm`, `right_arm` | Both `xRot = -PI * progress` (arms straight up at peak) |
| Floq | Ghast / Slime (gape + lunge) | `mouth`, `body` | `mouth.xRot += 0.5F * progress` (jaw opens), `body.xRot += 0.2F * progress` (tilts forward) |
| HourglassGolem | Iron Golem (overhead slam) | `leftArm`, `rightArm` | Both `xRot = -PI * progress` |
| ForgottenMinute | Vex (forward thrust) | `right_arm`, `left_arm` | Both `xRot = -PI/2 * progress` (Husk-style) |
| ParadoxCrawler | Spider (rear up) | `rightFrontLeg`, `leftFrontLeg`, `body` | Front legs `xRot -= 1.0F * progress`, body `xRot -= 0.2F * progress` (back-arched threat pose) |
| TemporalWraith | Husk / Drowned | `left_arm`, `right_arm` | Both `xRot = -PI/2 * progress` |
| TimelineStrider | Husk / Zombie | `leftArm`, `rightArm` | Both `xRot = -PI/2 * progress` |
| ClockworkColossus | Iron Golem (boss-scale slam) | `left_arm`, `right_arm`, `body` | Arms `xRot = -PI * progress`, body `xRot += 0.15F * progress` (slight forward lean for weight) |

All rotations use `+=` (additive on top of walk/idle pose) for non-conflicting parts and `=` (overwrite) for parts where the attack pose should fully replace the walk cycle (arms, mouth). The Husk fix uses `=` for the same reason; we follow that precedent.

### ChronalLeech wave-strike detail

The seven `bodyN` parts are arranged head-to-tail along the model's spine. The phase-shifted formula `(attack - i*0.15F)` causes the strike wave to start at the head segment (i=0) and travel toward the tail (i=6), creating a recognizable "lunge from the front" silhouette. The 0.3 amplitude is intentionally modest — segmented worms in vanilla (Silverfish, Endermite) animate subtly, and overshooting risks looking glitchy. We can tune up if visual testing shows the cue is too quiet.

### ClockworkColossus renderer extract

Add to `ClockworkColossusRenderer.extractRenderState` (or create the override if absent), in all nine 1.21.2+ versions:

```java
state.attackTime = entity.getAttackAnim(partialTick);
```

This mirrors the pattern from commit `6ea2b5ce` for the other five bosses. The 1.20.1 / 1.21.1 model reads `entity.attackAnim` directly, so no renderer change is needed for those eras.

---

## 4. Versioning and File Layout

For each of the 9 mobs:

- **Modern era (9 versions: 1.21.2, 1.21.4, 1.21.5, 1.21.6, 1.21.7, 1.21.8, 1.21.9, 1.21.10, 1.21.11)**:
  - Append the `if (state.attackTime > 0.0F) { ... }` block at the end of `setupAnim(XxxRenderState state)`.
- **Legacy era (2 versions: 1.20.1, 1.21.1)**:
  - Append the equivalent block, reading `entity.attackAnim` instead, at the end of `setupAnim(Entity, float, float, float, float, float)`.

Total file edits:
- Models: 9 mobs × 11 versions = **99 files**.
- ClockworkColossus renderer: **9 files** (1.21.2+ only).
- Grand total: **108 files**, expected `+8` to `+12` lines per file.

1.21.3 shares modules with 1.21.2 and is not edited directly; runtime verification covered by `gameTestAll`.

### Special path note

For `ChronalLeech` the wave-strike loop is identical across versions, only the field name changes. We will write the per-segment lines explicitly (no for-loop) to keep the per-version diffs reviewable and to match the existing setupAnim style in this codebase.

---

## 5. Risks and Mitigations

| Risk | Mitigation |
|---|---|
| Walk/idle code overwrites attack pose for `=` parts | Place attack block **after** all walk/idle code in setupAnim, matching Husk pattern |
| Attack block multiplies with idle sway and looks jittery | Walk-idle sway code in most models is gated behind `walkAnimationSpeed > 0.01` or similar; use additive `+=` for body-tilt parts and overwrite `=` for primary action parts |
| Animation invisible if mob's attack range is short and the player is looking past it | Out of scope for this fix — visibility from off-axis is a model-LOD concern, not animation |
| Per-segment phase math wrong on ChronalLeech | Visual smoke test in `runClientFabric1_21_5` covers this; 0.3 amplitude is conservative so worst case is "less visible" not "broken-looking" |
| Per-version copy-paste drift | Implementation plan lists exact 11-version × 9-mob matrix; verification step runs `build1_21_5` (cheap compile check) and at least one `runClient...` smoke test |

---

## 6. Verification

1. `./gradlew build1_21_5` (compile check, ~1.5 min).
2. `./gradlew runClientFabric1_21_5` — spawn each affected mob in creative, attack the player, visually confirm the animation triggers.
3. (Optional) Repeat smoke on 1.20.1 (legacy API) to catch field-name regressions.
4. (Optional) `./gradlew checkAll` before final merge to catch silent breakage on the seven untested versions, per the project memory note about per-task verification on a single version not being sufficient for multi-version changes.

---

## 7. Out-of-Scope Follow-ups

These were observed during the investigation and intentionally deferred:

- MomentCreeper missing swell-size animation (`state.swelling`).
- SecondhandArcher missing bow-draw animation (`isUsingItem` + `ArmedModel`).
- ForgottenMinute wing flap rate increase during attack.
- ChronalLeech wing motion during attack.
- Hit-flash / glow / particle effects on melee impact (cross-cutting concern, not a per-mob fix).

Each of these warrants its own brainstorm and design pass.
