---
description: Audit codebase for performance bottlenecks and thread-safety issues
allowed-tools: Read, Grep, Glob, Bash(grep*:rg*:find*), Write
---

## User Input

```text
$ARGUMENTS
```

**Optional**: Target area to audit (e.g., `structure-generation`, `boss-spawning`, `event-handlers`, `all`)
**Default**: `all` (audit all critical code paths)

You **MUST** consider the user input before proceeding (if not empty).

## Goal

Systematically audit the Minecraft mod codebase to identify:
- **T428**: Performance bottlenecks (main thread blocking)
- **T429**: Thread-safety issues (race conditions, non-thread-safe collections)
- **T430**: Dimension isolation problems (unnecessary cross-dimension processing)

Output a comprehensive audit report with findings, risk assessment, and fix recommendations.

## When to Run This Command

- Before major releases
- After implementing new structure generation systems
- After adding new boss spawning systems
- When investigating multiplayer crashes or lag
- When profiling shows unexpected CPU usage
- Periodically (recommended: monthly)

## Execution Steps

### 1. Load Audit Guidelines

Read `.claude/skills/audit-performance-thread-safety/SKILL.md` to understand:
- Audit methodology
- Code patterns to detect
- Severity classification
- Fix recommendations

### 2. Identify Critical Code Paths

**Find event handlers**:
```bash
grep -r "TickEvent\\.SERVER" common/src/main/java --include="*.java"
grep -r "TickEvent\\.SERVER_POST" common/src/main/java --include="*.java"
grep -r "TickEvent\\.SERVER_LEVEL_POST" common/src/main/java --include="*.java"
```

**Find structure generation code**:
```bash
find common/src/main/java -type f -name "*BossRoom*.java" -o -name "*Structure*.java"
```

**Find boss spawning systems**:
```bash
find common/src/main/java -type f -name "*Spawner*.java"
```

**Find structure-related mixins**:
```bash
find common/src/main/java -type d -name "mixin" -exec find {} -name "*Structure*.java" \;
```

### 3. Performance Analysis (T428)

For each file identified:

#### 3.1 Detect Main Thread Blocking

Search for these patterns:

**Large block scans**:
```bash
grep -r "BlockPos.betweenClosed" common/src/main/java --include="*.java" -n
```

Check each occurrence:
- Scan range (count blocks: `(maxX - minX) * (maxY - minY) * (maxZ - minZ)`)
- Frequency (every tick? every 600 ticks?)
- Chunking (is it split across ticks?)

**Severity classification**:
- > 1M blocks scanned: **CRITICAL**
- 100K - 1M blocks: **HIGH**
- 10K - 100K blocks: **MEDIUM**
- < 10K blocks: **LOW**

#### 3.2 Detect Repeated Scans

Look for:
- Multiple scans of same area in one method
- Delayed/scheduled rescans
- No caching of positions

**Document findings**:
```markdown
### Issue: Large block scan on main thread
**File**: `path/to/File.java` (lines X-Y)
**Scan range**: [minX-maxX, minY-maxY, minZ-minZ] = N blocks
**Frequency**: Every X ticks
**Estimated pause**: ~Nms
**Severity**: CRITICAL
```

### 4. Thread Safety Analysis (T429)

For each file identified:

#### 4.1 Detect Non-Thread-Safe Collections

**Find HashMap/HashSet**:
```bash
grep -r "new HashMap<>" common/src/main/java --include="*.java" -n
grep -r "new HashSet<>" common/src/main/java --include="*.java" -n
grep -r "= new HashMap" common/src/main/java --include="*.java" -n
grep -r "= new HashSet" common/src/main/java --include="*.java" -n
```

For each occurrence, check:
1. Is it `static`? (shared across threads)
2. Is it accessed from event handlers? (multi-threaded context)
3. Is it in a Mixin? (shared instance)

**Severity classification**:
- Static HashMap in SERVER_POST handler: **CRITICAL**
- Instance variable in Mixin: **HIGH**
- Local variable in method: **LOW** (safe)

#### 4.2 Detect Non-Atomic Operations

Search for these patterns:
```bash
grep -r "\.get(.*)" common/src/main/java --include="*.java" -A 2 -B 2
```

Look for:
```java
// BAD: get + modify + put (not atomic)
int count = map.get(key);
count++;
map.put(key, count);

// BAD: putIfAbsent + get (not atomic)
map.putIfAbsent(key, new HashSet<>());
Set<T> set = map.get(key);
```

**Document findings**:
```markdown
### Issue: Non-thread-safe HashMap
**File**: `path/to/File.java` (lines X-Y)
**Collection**: `private static final Map<K, V> map = new HashMap<>();`
**Risk**: ConcurrentModificationException, data corruption
**Fix**: Replace with `ConcurrentHashMap`
**Severity**: CRITICAL
```

### 5. Dimension Isolation Analysis (T430)

For each event handler:

#### 5.1 Detect Cross-Dimension Processing

**Find handlers without dimension filter**:

Check each `TickEvent.SERVER_POST` handler:
```java
// BAD: No dimension check
for (ServerLevel level : server.getAllLevels()) {
    processLevel(level);
}
```

**Look for dimension filter**:
```bash
grep -r "dimension().equals" common/src/main/java --include="*.java" -B 5
grep -r "CHRONO_DAWN_DIMENSION" common/src/main/java --include="*.java" -B 5
```

#### 5.2 Verify Expected Dimensions

For each handler, determine:
- Which dimension(s) should it process?
- Does it have early-exit dimension filter?
- Is it using `SERVER_POST` (global) or `SERVER_LEVEL_POST` (per-dimension)?

**Expected behavior**:
- Boss room placers → Only Chrono Dawn
- Boss spawners → Only Chrono Dawn
- Time distortion → Only Chrono Dawn
- Portal handlers → Multiple dimensions (OK)

**Document findings**:
```markdown
### Issue: No dimension filtering
**File**: `path/to/File.java` (lines X-Y)
**Handler**: `checkAndPlaceRooms()`
**Impact**: Processes all dimensions, wastes CPU in Overworld/Nether/End
**Expected**: Only Chrono Dawn dimension
**Fix**: Add early-exit dimension filter
**Severity**: MEDIUM
```

### 6. Generate Audit Report

Create `audit_report_performance_thread_safety.md` with this structure:

```markdown
# Performance and Thread Safety Audit Report

**Date**: [YYYY-MM-DD]
**Branch**: [current-branch]
**Audited Areas**: [structure-generation / boss-spawning / event-handlers / all]

---

## Executive Summary

[Brief overview: X issues found, Y critical, Z high priority]
[Overall risk assessment: CRITICAL / HIGH / MEDIUM / LOW]

---

## T428: Main Thread Blocking in Structure Generation

### Findings

[Detailed findings from Phase 3]

#### Issue 1: [Title]
**File**: `path/to/File.java` (lines X-Y)
**Pattern**: [Large block scan / Repeated scan / Batch operation]
**Impact**:
- Blocks scanned: [count]
- Estimated pause: [ms]
- Frequency: Every [ticks]
**Severity**: [CRITICAL / HIGH / MEDIUM]

### Root Cause Analysis

[Why these issues exist]

### Recommended Fixes

#### High Priority (Must Fix)
1. [Fix description]

#### Medium Priority (Should Fix)
2. [Fix description]

---

## T429: Non-Thread-Safe Collection Usage

### Findings

[Detailed findings from Phase 4]

#### Issue 1: [Title]
**File**: `path/to/File.java` (lines X-Y)
**Collection**: `[code snippet]`
**Risk**: [ConcurrentModificationException / Data corruption / Lost updates]
**Fix**: Replace with `ConcurrentHashMap`
**Severity**: [CRITICAL / HIGH / MEDIUM]

### Root Cause Analysis

[Why these issues exist]

### Recommended Fixes

#### High Priority (Must Fix)
1. [Fix description with code example]

---

## T430: Dimension Filtering in Chunk Processing

### Findings

[Detailed findings from Phase 5]

#### Issue 1: [Title]
**File**: `path/to/File.java` (lines X-Y)
**Handler**: `methodName()`
**Impact**: Wastes CPU in [dimensions]
**Expected**: Only [target dimension]
**Fix**: Add dimension filter
**Severity**: MEDIUM

### Recommended Fixes

1. [Fix description with code example]

---

## Summary of Issues Found

| ID | Issue | Severity | Files Affected | Fix Priority |
|----|-------|----------|----------------|--------------|
| T428-1 | [Description] | CRITICAL | [files] | HIGH |
| T429-1 | [Description] | CRITICAL | [files] | HIGH |
| T430-1 | [Description] | MEDIUM | [files] | MEDIUM |

---

## Next Steps

1. Review audit findings
2. Prioritize fixes (CRITICAL → HIGH → MEDIUM)
3. Implement fixes
4. Test fixes
5. Document changes

---

## Estimated Effort

- Investigation completion: [hours]
- Critical fixes: [hours]
- High-priority fixes: [hours]
- Testing: [hours]
- **Total**: [hours] ([days])

---

## Risk Assessment

**Without Fixes**:
- Single-Player: [impact]
- Multiplayer: [impact]
- Dimension Mixing: [impact]

**With Fixes**:
- Single-Player: [improvement]
- Multiplayer: [improvement]
- Dimension Isolation: [improvement]

---

**Auditor**: Claude Sonnet 4.5
**Report Generated**: [YYYY-MM-DD HH:MM:SS]
```

### 7. Output Summary

After generating the report, provide a concise summary:

```
✓ Audit completed

Findings:
  - T428 (Performance): X issues (Y critical, Z high)
  - T429 (Thread Safety): X issues (Y critical, Z high)
  - T430 (Dimension Filtering): X issues (Y medium)

Report saved to: audit_report_performance_thread_safety.md

Critical issues requiring immediate attention: [count]

Next steps:
  1. Review report: audit_report_performance_thread_safety.md
  2. Prioritize fixes (start with CRITICAL)
  3. Run build tests after fixes
```

## Audit Scope Options

If `$ARGUMENTS` specifies a target area:

**`structure-generation`**:
- Audit only structure generation code
- Focus on T428 (performance) and T429 (thread safety in structure placement)
- Files: `*BossRoom*.java`, `*Structure*.java`, mixins

**`boss-spawning`**:
- Audit only boss spawning systems
- Focus on T429 (thread safety in spawner maps) and T430 (dimension filtering)
- Files: `*Spawner*.java`

**`event-handlers`**:
- Audit only event handlers
- Focus on T429 (shared state) and T430 (dimension filtering)
- Files: `events/*.java`, `*EventHandler.java`

**`all`** (default):
- Audit all critical code paths
- Comprehensive analysis

## Common Patterns Reference

### Performance Issues (T428)

**Pattern 1: Large block scan**
```java
// DETECTED: BlockPos.betweenClosed with large range
for (BlockPos pos : BlockPos.betweenClosed(minX, minY, minZ, maxX, maxY, maxZ)) {
    // Check if range > 100K blocks
}
```

**Pattern 2: Repeated scans**
```java
// DETECTED: Multiple scans of same area
removeWater();          // Scan 1
placeTemplate();
finalizeWaterlogging(); // Scan 2
scheduledFinalize();    // Scan 3
```

### Thread Safety Issues (T429)

**Pattern 1: Static HashMap**
```java
// DETECTED: HashMap in multi-threaded context
private static final Map<ResourceLocation, Set<BlockPos>> map = new HashMap<>();
```

**Pattern 2: Non-atomic operations**
```java
// DETECTED: get + put (not atomic)
map.putIfAbsent(key, new HashSet<>());
Set<BlockPos> set = map.get(key);
```

### Dimension Issues (T430)

**Pattern 1: No dimension filter**
```java
// DETECTED: Processing all dimensions
TickEvent.SERVER_POST.register(server -> {
    for (ServerLevel level : server.getAllLevels()) {
        // No dimension check!
    }
});
```

## Error Handling

If no issues found:
```
✓ Audit completed

No issues found in [target area].

All code paths appear to be:
  - Performance-optimized (no large block scans)
  - Thread-safe (using ConcurrentHashMap)
  - Dimension-isolated (proper filtering)

Report saved to: audit_report_performance_thread_safety.md
```

If files are missing:
```
✗ Error: Target files not found

Searched locations:
  - common/src/main/java/com/chronodawn/worldgen/
  - common/src/main/java/com/chronodawn/events/
  - common/src/main/java/com/chronodawn/mixin/

Please verify project structure.
```

## Follow-Up Commands

After audit completion, suggest:

1. **If critical issues found**:
   ```
   Next steps:
     1. Review report
     2. Fix critical issues first (see report for recommendations)
     3. Run: ./gradlew build (verify fixes compile)
     4. Run: /test-performance-fixes (manual testing guide)
   ```

2. **If no critical issues**:
   ```
   Next steps:
     1. Review report for optimization opportunities
     2. Consider fixing medium-priority issues
     3. Schedule next audit (recommended: 1 month)
   ```

## Reference

- Audit methodology: `.claude/skills/audit-performance-thread-safety/SKILL.md`
- Testing guide: `TESTING_VERIFICATION_GUIDE.md` (if exists)
- Related tasks: T428, T429, T430
