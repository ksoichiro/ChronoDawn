# Custom Mob Attack Animations Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Add visible attack animations to nine custom hostile mobs whose models currently freeze in their idle pose during melee strikes, using vanilla-conformant animation patterns per mob.

**Architecture:** Each mob's `setupAnim` gets an additional `if (<attackField> > 0.0F) { ... }` block appended after the existing walk/idle code. The block uses `Mth.sin(<attackField> * (float)Math.PI)` as a shared easing envelope. Body parts and rotation magnitudes vary per mob, chosen to match the closest vanilla mob (Iron Golem, Husk, Spider, etc.). For ClockworkColossus, the renderer is also updated to extract `state.attackTime` from the entity (1.21.2+ only), mirroring commit `6ea2b5ce`.

**Tech Stack:** Java 21, Minecraft Java Edition 1.20.1–1.21.11 (11 versions, two API eras), Architectury Loom, Mojang mappings, `net.minecraft.util.Mth`.

**Spec:** `docs/superpowers/specs/2026-05-05-mob-attack-animations-design.md`

---

## API Era Reference (used by every task below)

**Modern era (1.21.2, 1.21.4, 1.21.5, 1.21.6, 1.21.7, 1.21.8, 1.21.9, 1.21.10, 1.21.11 — 9 versions):**
- Method signature: `public void setupAnim(<XxxRenderState> state)`
- Attack field: `state.attackTime`
- Imports already include `net.minecraft.util.Mth` in every file (verified during the boss-renderer fix); no new imports needed.

**Legacy era (1.20.1, 1.21.1 — 2 versions):**
- Method signature: `public void setupAnim(<XxxEntity> entity, float limbSwing, float limbSwingAmount, float ageInTicks, float netHeadYaw, float headPitch)`
- Attack field: `entity.attackAnim` (already a `float` in 0..1 range during attack)
- `Mth` import already present.

**Where to insert:** Append the new `if` block at the end of `setupAnim`, immediately before the closing `}`. Do not modify any existing walk/idle code above it. Use `+=` for body parts that should compose with idle sway, and `=` for primary action parts that should fully replace the walk-cycle pose during the strike (matches the `EpochHuskModel` precedent).

**Reference fix to mirror:** commit `31190f0a` (EpochHusk modern era) and the legacy 1.20.1 / 1.21.1 EpochHusk equivalents.

---

## File Path Reference

For every task, the model file is at:
- Modern: `common/<VERSION>/src/main/java/com/chronodawn/client/model/<Mob>Model.java` for `<VERSION>` in {1.21.2, 1.21.4, 1.21.5, 1.21.6, 1.21.7, 1.21.8, 1.21.9, 1.21.10, 1.21.11}.
- Legacy: `common/<VERSION>/src/main/java/com/chronodawn/client/model/<Mob>Model.java` for `<VERSION>` in {1.20.1, 1.21.1}.

ClockworkColossus renderer:
- `common/<VERSION>/src/main/java/com/chronodawn/client/renderer/ClockworkColossusRenderer.java` for `<VERSION>` in the modern set above.

---

## Task 1: ClockworkColossus renderer attackTime extract (modern era only)

**Why first:** This unlocks the model-side animation we add in Task 10. Without the renderer extract, the model's `state.attackTime` reads stay zero and animation is dead code. Same bug class as the boss fix in commit `6ea2b5ce`.

**Files (9):**
- Modify: `common/1.21.2/src/main/java/com/chronodawn/client/renderer/ClockworkColossusRenderer.java`
- Modify: `common/1.21.4/src/main/java/com/chronodawn/client/renderer/ClockworkColossusRenderer.java`
- Modify: `common/1.21.5/src/main/java/com/chronodawn/client/renderer/ClockworkColossusRenderer.java`
- Modify: `common/1.21.6/src/main/java/com/chronodawn/client/renderer/ClockworkColossusRenderer.java`
- Modify: `common/1.21.7/src/main/java/com/chronodawn/client/renderer/ClockworkColossusRenderer.java`
- Modify: `common/1.21.8/src/main/java/com/chronodawn/client/renderer/ClockworkColossusRenderer.java`
- Modify: `common/1.21.9/src/main/java/com/chronodawn/client/renderer/ClockworkColossusRenderer.java`
- Modify: `common/1.21.10/src/main/java/com/chronodawn/client/renderer/ClockworkColossusRenderer.java`
- Modify: `common/1.21.11/src/main/java/com/chronodawn/client/renderer/ClockworkColossusRenderer.java`

- [ ] **Step 1: Inspect one renderer to confirm whether `extractRenderState` already exists**

Run: `grep -n "extractRenderState" common/1.21.5/src/main/java/com/chronodawn/client/renderer/ClockworkColossusRenderer.java`

Expected output: empty (no existing override) — in which case use the "add full method" pattern below. If output shows an existing `extractRenderState`, switch to the "append one line" pattern from commit `6ea2b5ce` (TemporalPhantom branch).

- [ ] **Step 2: Edit each of the 9 renderer files**

For each version, find this anchor and replace:

old_string (anchor):
```java
    public ClockworkColossusRenderState createRenderState() {
        return new ClockworkColossusRenderState();
    }

    @Override
```

new_string:
```java
    public ClockworkColossusRenderState createRenderState() {
        return new ClockworkColossusRenderState();
    }

    @Override
    public void extractRenderState(ClockworkColossusEntity entity, ClockworkColossusRenderState state, float partialTick) {
        super.extractRenderState(entity, state, partialTick);
        state.attackTime = entity.getAttackAnim(partialTick);
    }

    @Override
```

If the file already has `extractRenderState`, instead append `state.attackTime = entity.getAttackAnim(partialTick);` after the existing `super.extractRenderState(...)` line.

- [ ] **Step 3: Verify diff stats**

Run: `git diff --stat`
Expected: 9 files changed, ~54 insertions (6 lines × 9 files), 0 deletions.

- [ ] **Step 4: Build verify on 1.21.5**

Run: `run-wrapped ./gradlew build1_21_5`
Expected: `BUILD SUCCESSFUL`. No compile errors.

- [ ] **Step 5: Commit**

```bash
git add common/1.21.2/src/main/java/com/chronodawn/client/renderer/ClockworkColossusRenderer.java common/1.21.4/src/main/java/com/chronodawn/client/renderer/ClockworkColossusRenderer.java common/1.21.5/src/main/java/com/chronodawn/client/renderer/ClockworkColossusRenderer.java common/1.21.6/src/main/java/com/chronodawn/client/renderer/ClockworkColossusRenderer.java common/1.21.7/src/main/java/com/chronodawn/client/renderer/ClockworkColossusRenderer.java common/1.21.8/src/main/java/com/chronodawn/client/renderer/ClockworkColossusRenderer.java common/1.21.9/src/main/java/com/chronodawn/client/renderer/ClockworkColossusRenderer.java common/1.21.10/src/main/java/com/chronodawn/client/renderer/ClockworkColossusRenderer.java common/1.21.11/src/main/java/com/chronodawn/client/renderer/ClockworkColossusRenderer.java
git commit -m "fix(colossus): extract attack animation time in renderer (1.21.2+)

Mirrors the boss renderer fix from commit 6ea2b5ce. Without this,
state.attackTime stays zero and any model-side attack animation is
dead code. 1.20.1 and 1.21.1 are unaffected (legacy entity.attackAnim).

Co-Authored-By: Claude Opus 4.7 (1M context) <noreply@anthropic.com>"
```

---

## Task 2: ChronalLeech wave-strike animation

**Body parts:** `body1`, `body2`, `body3`, `body4`, `body5`, `body6`, `body7` (head-to-tail). Wing parts left untouched.

**Files (11):** all `common/<VERSION>/src/main/java/com/chronodawn/client/model/ChronalLeechModel.java` for `<VERSION>` in {1.20.1, 1.21.1, 1.21.2, 1.21.4, 1.21.5, 1.21.6, 1.21.7, 1.21.8, 1.21.9, 1.21.10, 1.21.11}.

- [ ] **Step 1: Append modern-era block to 9 model files**

For each modern-era file, locate the closing `}` of the `setupAnim(ChronalLeechRenderState state)` method and insert the following block immediately before it:

```java

        // Attack animation - segmented wave traveling head-to-tail (Silverfish-style)
        if (state.attackTime > 0.0F) {
            float a = state.attackTime;
            this.body1.xRot += 0.3F * Mth.sin((a - 0.00F) * (float)Math.PI);
            this.body2.xRot += 0.3F * Mth.sin((a - 0.15F) * (float)Math.PI);
            this.body3.xRot += 0.3F * Mth.sin((a - 0.30F) * (float)Math.PI);
            this.body4.xRot += 0.3F * Mth.sin((a - 0.45F) * (float)Math.PI);
            this.body5.xRot += 0.3F * Mth.sin((a - 0.60F) * (float)Math.PI);
            this.body6.xRot += 0.3F * Mth.sin((a - 0.75F) * (float)Math.PI);
            this.body7.xRot += 0.3F * Mth.sin((a - 0.90F) * (float)Math.PI);
        }
```

- [ ] **Step 2: Append legacy-era block to 2 model files (1.20.1, 1.21.1)**

Same code, but replace `state.attackTime` with `entity.attackAnim`:

```java

        // Attack animation - segmented wave traveling head-to-tail (Silverfish-style)
        if (entity.attackAnim > 0.0F) {
            float a = entity.attackAnim;
            this.body1.xRot += 0.3F * Mth.sin((a - 0.00F) * (float)Math.PI);
            this.body2.xRot += 0.3F * Mth.sin((a - 0.15F) * (float)Math.PI);
            this.body3.xRot += 0.3F * Mth.sin((a - 0.30F) * (float)Math.PI);
            this.body4.xRot += 0.3F * Mth.sin((a - 0.45F) * (float)Math.PI);
            this.body5.xRot += 0.3F * Mth.sin((a - 0.60F) * (float)Math.PI);
            this.body6.xRot += 0.3F * Mth.sin((a - 0.75F) * (float)Math.PI);
            this.body7.xRot += 0.3F * Mth.sin((a - 0.90F) * (float)Math.PI);
        }
```

- [ ] **Step 3: Verify diff stats**

Run: `git diff --stat`
Expected: 11 files changed, ~110–132 insertions (10 lines × 11 files plus blank line), 0 deletions.

- [ ] **Step 4: Build verify on 1.21.5**

Run: `run-wrapped ./gradlew build1_21_5`
Expected: `BUILD SUCCESSFUL`.

- [ ] **Step 5: Commit**

```bash
git add common/*/src/main/java/com/chronodawn/client/model/ChronalLeechModel.java
git commit -m "feat(mobs): add wave-strike attack animation to Chronal Leech

Body segments body1..body7 strike in sequence head-to-tail using
phase-shifted sine envelope, evoking a Silverfish-style segmented
strike. Wing parts intentionally left on idle/walk curves.

Co-Authored-By: Claude Opus 4.7 (1M context) <noreply@anthropic.com>"
```

---

## Task 3: ClockworkSentinel overhead-slam animation

**Body parts:** `left_arm`, `right_arm`.

**Files (11):** all `common/<VERSION>/src/main/java/com/chronodawn/client/model/ClockworkSentinelModel.java`.

- [ ] **Step 1: Append modern-era block to 9 model files**

```java

        // Attack animation - Iron Golem-style overhead slam (both arms straight up at peak)
        if (state.attackTime > 0.0F) {
            float armRaise = Mth.sin(state.attackTime * (float)Math.PI);
            float armRot = -(float)Math.PI * armRaise;
            this.left_arm.xRot = armRot;
            this.right_arm.xRot = armRot;
        }
```

- [ ] **Step 2: Append legacy-era block to 2 model files (1.20.1, 1.21.1)**

```java

        // Attack animation - Iron Golem-style overhead slam (both arms straight up at peak)
        if (entity.attackAnim > 0.0F) {
            float armRaise = Mth.sin(entity.attackAnim * (float)Math.PI);
            float armRot = -(float)Math.PI * armRaise;
            this.left_arm.xRot = armRot;
            this.right_arm.xRot = armRot;
        }
```

- [ ] **Step 3: Verify diff stats**

Run: `git diff --stat`
Expected: 11 files changed, ~77 insertions (7 lines × 11 files), 0 deletions.

- [ ] **Step 4: Build verify**

Run: `run-wrapped ./gradlew build1_21_5`
Expected: `BUILD SUCCESSFUL`.

- [ ] **Step 5: Commit**

```bash
git add common/*/src/main/java/com/chronodawn/client/model/ClockworkSentinelModel.java
git commit -m "feat(mobs): add overhead-slam attack animation to Clockwork Sentinel

Both arms raise straight up at attack peak (Iron Golem-style).

Co-Authored-By: Claude Opus 4.7 (1M context) <noreply@anthropic.com>"
```

---

## Task 4: Floq gape-and-lunge animation

**Body parts:** `mouth`, `body`.

**Files (11):** all `common/<VERSION>/src/main/java/com/chronodawn/client/model/FloqModel.java`.

- [ ] **Step 1: Append modern-era block to 9 model files**

```java

        // Attack animation - jaw gapes and body tilts forward (Slime/Ghast-style lunge)
        if (state.attackTime > 0.0F) {
            float progress = Mth.sin(state.attackTime * (float)Math.PI);
            this.mouth.xRot += 0.5F * progress;
            this.body.xRot += 0.2F * progress;
        }
```

- [ ] **Step 2: Append legacy-era block to 2 model files (1.20.1, 1.21.1)**

```java

        // Attack animation - jaw gapes and body tilts forward (Slime/Ghast-style lunge)
        if (entity.attackAnim > 0.0F) {
            float progress = Mth.sin(entity.attackAnim * (float)Math.PI);
            this.mouth.xRot += 0.5F * progress;
            this.body.xRot += 0.2F * progress;
        }
```

- [ ] **Step 3: Verify diff stats and build**

Run: `git diff --stat`
Expected: 11 files changed, ~66 insertions, 0 deletions.

Run: `run-wrapped ./gradlew build1_21_5`
Expected: `BUILD SUCCESSFUL`.

- [ ] **Step 4: Commit**

```bash
git add common/*/src/main/java/com/chronodawn/client/model/FloqModel.java
git commit -m "feat(mobs): add gape-and-lunge attack animation to Floq

Mouth opens and body tilts forward at attack peak. Eyes left on
existing idle motion.

Co-Authored-By: Claude Opus 4.7 (1M context) <noreply@anthropic.com>"
```

---

## Task 5: HourglassGolem overhead-slam animation

**Body parts:** `leftArm`, `rightArm` (camelCase — distinct from ClockworkSentinel's snake_case).

**Files (11):** all `common/<VERSION>/src/main/java/com/chronodawn/client/model/HourglassGolemModel.java`.

- [ ] **Step 1: Append modern-era block to 9 model files**

```java

        // Attack animation - Iron Golem-style overhead slam (both arms straight up at peak)
        if (state.attackTime > 0.0F) {
            float armRaise = Mth.sin(state.attackTime * (float)Math.PI);
            float armRot = -(float)Math.PI * armRaise;
            this.leftArm.xRot = armRot;
            this.rightArm.xRot = armRot;
        }
```

- [ ] **Step 2: Append legacy-era block to 2 model files (1.20.1, 1.21.1)**

```java

        // Attack animation - Iron Golem-style overhead slam (both arms straight up at peak)
        if (entity.attackAnim > 0.0F) {
            float armRaise = Mth.sin(entity.attackAnim * (float)Math.PI);
            float armRot = -(float)Math.PI * armRaise;
            this.leftArm.xRot = armRot;
            this.rightArm.xRot = armRot;
        }
```

- [ ] **Step 3: Verify diff stats and build**

Run: `git diff --stat`
Expected: 11 files changed, ~77 insertions, 0 deletions.

Run: `run-wrapped ./gradlew build1_21_5`
Expected: `BUILD SUCCESSFUL`.

- [ ] **Step 4: Commit**

```bash
git add common/*/src/main/java/com/chronodawn/client/model/HourglassGolemModel.java
git commit -m "feat(mobs): add overhead-slam attack animation to Hourglass Golem

Both arms raise straight up at attack peak (Iron Golem-style).

Co-Authored-By: Claude Opus 4.7 (1M context) <noreply@anthropic.com>"
```

---

## Task 6: ForgottenMinute forward-thrust animation

**Body parts:** `right_arm`, `left_arm` (snake_case). Wing parts left on existing curves.

**Files (11):** all `common/<VERSION>/src/main/java/com/chronodawn/client/model/ForgottenMinuteModel.java`.

- [ ] **Step 1: Append modern-era block to 9 model files**

```java

        // Attack animation - Husk-style forward arm thrust (Vex-like)
        if (state.attackTime > 0.0F) {
            float armRaise = Mth.sin(state.attackTime * (float)Math.PI);
            float armRot = -(float)Math.PI / 2.0F * armRaise;
            this.right_arm.xRot = armRot;
            this.left_arm.xRot = armRot;
        }
```

- [ ] **Step 2: Append legacy-era block to 2 model files (1.20.1, 1.21.1)**

```java

        // Attack animation - Husk-style forward arm thrust (Vex-like)
        if (entity.attackAnim > 0.0F) {
            float armRaise = Mth.sin(entity.attackAnim * (float)Math.PI);
            float armRot = -(float)Math.PI / 2.0F * armRaise;
            this.right_arm.xRot = armRot;
            this.left_arm.xRot = armRot;
        }
```

- [ ] **Step 3: Verify diff stats and build**

Run: `git diff --stat`
Expected: 11 files changed, ~77 insertions, 0 deletions.

Run: `run-wrapped ./gradlew build1_21_5`
Expected: `BUILD SUCCESSFUL`.

- [ ] **Step 4: Commit**

```bash
git add common/*/src/main/java/com/chronodawn/client/model/ForgottenMinuteModel.java
git commit -m "feat(mobs): add forward-thrust attack animation to Forgotten Minute

Both arms swing forward at attack peak (Husk/Vex-style). Wings left
on existing idle/flap curves.

Co-Authored-By: Claude Opus 4.7 (1M context) <noreply@anthropic.com>"
```

---

## Task 7: ParadoxCrawler rear-up animation

**Body parts:** `rightFrontLeg`, `leftFrontLeg`, `body` (camelCase legs). Middle and hind legs and `head`/`neck` left on walk cycle.

**Files (11):** all `common/<VERSION>/src/main/java/com/chronodawn/client/model/ParadoxCrawlerModel.java`.

- [ ] **Step 1: Append modern-era block to 9 model files**

```java

        // Attack animation - Spider-style rear up (front legs lift, body arches back)
        if (state.attackTime > 0.0F) {
            float progress = Mth.sin(state.attackTime * (float)Math.PI);
            this.rightFrontLeg.xRot -= 1.0F * progress;
            this.leftFrontLeg.xRot -= 1.0F * progress;
            this.body.xRot -= 0.2F * progress;
        }
```

- [ ] **Step 2: Append legacy-era block to 2 model files (1.20.1, 1.21.1)**

```java

        // Attack animation - Spider-style rear up (front legs lift, body arches back)
        if (entity.attackAnim > 0.0F) {
            float progress = Mth.sin(entity.attackAnim * (float)Math.PI);
            this.rightFrontLeg.xRot -= 1.0F * progress;
            this.leftFrontLeg.xRot -= 1.0F * progress;
            this.body.xRot -= 0.2F * progress;
        }
```

- [ ] **Step 3: Verify diff stats and build**

Run: `git diff --stat`
Expected: 11 files changed, ~77 insertions, 0 deletions.

Run: `run-wrapped ./gradlew build1_21_5`
Expected: `BUILD SUCCESSFUL`.

- [ ] **Step 4: Commit**

```bash
git add common/*/src/main/java/com/chronodawn/client/model/ParadoxCrawlerModel.java
git commit -m "feat(mobs): add rear-up attack animation to Paradox Crawler

Front legs lift and body arches back at attack peak (Spider-style
threat pose). Middle/hind legs and head/neck left on walk cycle.

Co-Authored-By: Claude Opus 4.7 (1M context) <noreply@anthropic.com>"
```

---

## Task 8: TemporalWraith forward-thrust animation

**Body parts:** `left_arm`, `right_arm` (snake_case).

**Files (11):** all `common/<VERSION>/src/main/java/com/chronodawn/client/model/TemporalWraithModel.java`.

- [ ] **Step 1: Append modern-era block to 9 model files**

```java

        // Attack animation - Husk-style forward arm thrust
        if (state.attackTime > 0.0F) {
            float armRaise = Mth.sin(state.attackTime * (float)Math.PI);
            float armRot = -(float)Math.PI / 2.0F * armRaise;
            this.left_arm.xRot = armRot;
            this.right_arm.xRot = armRot;
        }
```

- [ ] **Step 2: Append legacy-era block to 2 model files (1.20.1, 1.21.1)**

```java

        // Attack animation - Husk-style forward arm thrust
        if (entity.attackAnim > 0.0F) {
            float armRaise = Mth.sin(entity.attackAnim * (float)Math.PI);
            float armRot = -(float)Math.PI / 2.0F * armRaise;
            this.left_arm.xRot = armRot;
            this.right_arm.xRot = armRot;
        }
```

- [ ] **Step 3: Verify diff stats and build**

Run: `git diff --stat`
Expected: 11 files changed, ~77 insertions, 0 deletions.

Run: `run-wrapped ./gradlew build1_21_5`
Expected: `BUILD SUCCESSFUL`.

- [ ] **Step 4: Commit**

```bash
git add common/*/src/main/java/com/chronodawn/client/model/TemporalWraithModel.java
git commit -m "feat(mobs): add forward-thrust attack animation to Temporal Wraith

Both arms swing forward at attack peak (Husk/Drowned-style).

Co-Authored-By: Claude Opus 4.7 (1M context) <noreply@anthropic.com>"
```

---

## Task 9: TimelineStrider forward-thrust animation

**Body parts:** `leftArm`, `rightArm` (camelCase — distinct from TemporalWraith's snake_case).

**Files (11):** all `common/<VERSION>/src/main/java/com/chronodawn/client/model/TimelineStriderModel.java`.

- [ ] **Step 1: Append modern-era block to 9 model files**

```java

        // Attack animation - Husk-style forward arm thrust
        if (state.attackTime > 0.0F) {
            float armRaise = Mth.sin(state.attackTime * (float)Math.PI);
            float armRot = -(float)Math.PI / 2.0F * armRaise;
            this.leftArm.xRot = armRot;
            this.rightArm.xRot = armRot;
        }
```

- [ ] **Step 2: Append legacy-era block to 2 model files (1.20.1, 1.21.1)**

```java

        // Attack animation - Husk-style forward arm thrust
        if (entity.attackAnim > 0.0F) {
            float armRaise = Mth.sin(entity.attackAnim * (float)Math.PI);
            float armRot = -(float)Math.PI / 2.0F * armRaise;
            this.leftArm.xRot = armRot;
            this.rightArm.xRot = armRot;
        }
```

- [ ] **Step 3: Verify diff stats and build**

Run: `git diff --stat`
Expected: 11 files changed, ~77 insertions, 0 deletions.

Run: `run-wrapped ./gradlew build1_21_5`
Expected: `BUILD SUCCESSFUL`.

- [ ] **Step 4: Commit**

```bash
git add common/*/src/main/java/com/chronodawn/client/model/TimelineStriderModel.java
git commit -m "feat(mobs): add forward-thrust attack animation to Timeline Strider

Both arms swing forward at attack peak (Husk-style).

Co-Authored-By: Claude Opus 4.7 (1M context) <noreply@anthropic.com>"
```

---

## Task 10: ClockworkColossus boss-scale slam animation

**Why last in the model batch:** Depends on Task 1 (renderer extract). Without Task 1, this code is dead on 1.21.2+.

**Body parts:** `left_arm`, `right_arm`, `body` (snake_case arms).

**Files (11):** all `common/<VERSION>/src/main/java/com/chronodawn/client/model/ClockworkColossusModel.java`.

- [ ] **Step 1: Append modern-era block to 9 model files**

```java

        // Attack animation - boss-scale Iron Golem slam (arms overhead, slight forward lean)
        if (state.attackTime > 0.0F) {
            float armRaise = Mth.sin(state.attackTime * (float)Math.PI);
            float armRot = -(float)Math.PI * armRaise;
            this.left_arm.xRot = armRot;
            this.right_arm.xRot = armRot;
            this.body.xRot += 0.15F * armRaise;
        }
```

- [ ] **Step 2: Append legacy-era block to 2 model files (1.20.1, 1.21.1)**

```java

        // Attack animation - boss-scale Iron Golem slam (arms overhead, slight forward lean)
        if (entity.attackAnim > 0.0F) {
            float armRaise = Mth.sin(entity.attackAnim * (float)Math.PI);
            float armRot = -(float)Math.PI * armRaise;
            this.left_arm.xRot = armRot;
            this.right_arm.xRot = armRot;
            this.body.xRot += 0.15F * armRaise;
        }
```

- [ ] **Step 3: Verify diff stats and build**

Run: `git diff --stat`
Expected: 11 files changed, ~88 insertions, 0 deletions.

Run: `run-wrapped ./gradlew build1_21_5`
Expected: `BUILD SUCCESSFUL`.

- [ ] **Step 4: Commit**

```bash
git add common/*/src/main/java/com/chronodawn/client/model/ClockworkColossusModel.java
git commit -m "feat(mobs): add boss-scale slam attack animation to Clockwork Colossus

Both arms raise overhead and body leans forward at attack peak,
giving the boss strike visible weight. Pairs with the renderer
attackTime extract added in the previous commit.

Co-Authored-By: Claude Opus 4.7 (1M context) <noreply@anthropic.com>"
```

---

## Task 11: Final verification

- [ ] **Step 1: Quick compile sanity on a non-1.21.5 version**

Run: `run-wrapped ./gradlew build1_21_11`
Expected: `BUILD SUCCESSFUL`. Catches Identifier-vs-ResourceLocation regressions and any 1.21.11-only API drift.

- [ ] **Step 2: Quick compile sanity on legacy era**

Run: `run-wrapped ./gradlew build1_20_1`
Expected: `BUILD SUCCESSFUL`. Catches `entity.attackAnim` typos and legacy `setupAnim` signature mismatches.

- [ ] **Step 3: Smoke test in a live client**

Run: `run-wrapped ./gradlew runClientFabric1_21_5`

Spawn each affected mob using a creative-mode spawn egg and let it hit the player. Confirm visible animation:
- ChronalLeech — segmented body wave on strike
- ClockworkSentinel — arms slam overhead
- Floq — mouth opens and body tilts
- HourglassGolem — arms slam overhead
- ForgottenMinute — arms thrust forward
- ParadoxCrawler — front legs rise, body arches
- TemporalWraith — arms thrust forward
- TimelineStrider — arms thrust forward
- ClockworkColossus — arms overhead, body leans forward

Stop the client when done.

- [ ] **Step 4: (Optional) Full multi-version verification**

Run: `run-wrapped ./gradlew checkAll`
Expected: `BUILD SUCCESSFUL` for every version task. This is the safety net per the project memory note about per-task verification on a single version not being sufficient for multi-version changes. Skip if time-constrained, but flag the skip to the user.

- [ ] **Step 5: Confirm clean tree and final commit graph**

Run: `git status --short` — expected: empty.
Run: `git log --oneline -12` — expected: ten new commits since the brainstorm-spec commit (`docs(spec): add custom mob attack animations design`), one per task.

---

## Notes for the implementing agent

- **Anchor placement:** Each `setupAnim` ends with a closing `}`. Insert the new block **before** that `}`, leaving the existing walk/idle code untouched. Use a leading blank line to keep the diff readable.
- **Per-version drift:** 1.21.11 renamed `ResourceLocation` to `Identifier` in some files; `Mth` is unaffected. The model files themselves typically don't import `ResourceLocation`, so 1.21.11 model edits look identical to 1.21.5.
- **Field name reminder per mob:** snake_case (`left_arm`, `right_arm`) on ClockworkSentinel, ForgottenMinute, TemporalWraith, ClockworkColossus. camelCase (`leftArm`, `rightArm`) on HourglassGolem, TimelineStrider. Do NOT cross these — wrong case is a hard compile error.
- **`Mth` import:** Already imported in every existing setupAnim that uses sin/cos for walk; do not add a duplicate import.
- **Do not modify** `super.setupAnim(state)` (or the legacy 6-arg call) at the top of each method — it dispatches walk/limb-swing handling to the parent and must stay first.
- **Subagent worktree note:** If a subagent is spawned to do file edits, the prompt MUST instruct `pwd` confirmation against `/Users/ksoichiro/src/github.com/ksoichiro/ChronoDawn/.claude/worktrees/quizzical-shimmying-snail/` to avoid leaking commits to the main repo (per a known recurring issue captured in the project memory).
