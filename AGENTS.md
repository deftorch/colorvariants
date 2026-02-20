# AGENTS.md — Color Variants Mod
## AI Agent Instructions for Google Jules

> **Version:** 1.0 | **Updated:** February 2026 | **Project:** Color Variants Minecraft Mod

---

## 🎯 PROJECT OVERVIEW

**Color Variants** is a Minecraft mod (Java Edition) that allows players to recolor any block using custom tools: Color Wand, Area Selector, Color Palette, and Eyedropper. It uses a **multiloader architecture** supporting both **Fabric** and **Forge** platforms.

**Current Status:** Prototype — 40% functional. Core rendering is broken. Major security, performance, and compatibility issues documented.

**Tech Stack:**
- Java 17
- Minecraft 1.20.1
- Gradle (multiloader build via `buildSrc/`)
- Mixins (SpongePowered)
- Fabric Loader ≥ 0.14 / Forge/NeoForge
- Fabric API (FRAPI for rendering)

**Repository Structure:**
```
colorvariants/
├── common/           ← Shared logic (items, GUI, network, core)
│   └── src/main/java/com/colorvariants/
│       ├── block/          ← ColoredBlockEntity (needs rewrite)
│       ├── client/         ← GUI screens, renderer stubs
│       ├── command/        ← /color command
│       ├── config/         ← ModConfig
│       ├── core/           ← ColorTransform, UndoRedo, TextureGenerator
│       ├── data/           ← ColorPresets, ColorSchemeManager
│       ├── item/           ← 4 tools
│       ├── mixin/          ← Block behavior mixins
│       ├── network/        ← Packets
│       └── platform/       ← Service interfaces
├── fabric/           ← Fabric-specific implementations
├── buildSrc/         ← Shared Gradle logic
└── gradle.properties ← Versions & mod metadata
```

---

## 🚨 CRITICAL PRIORITIES (Fix First)

Jules should **always** address issues in this priority order:

### P0 — Blocking (Must Fix Before Any Release)

| # | Issue | Files | Effort |
|---|-------|-------|--------|
| 1 | **Rendering not implemented** — colors not visible | `ColoredBlockRenderer.java`, new `MixinBlockModelRenderer.java` | 2–3 weeks |
| 2 | **Sodium/Embeddium incompatibility** — 90% of users broken | Add FRAPI support, `FabricNetworkHelper` | 1–2 weeks |
| 3 | **Area Selector multiplayer bug** — shared static state corrupts data | `AreaSelectorItem.java` | 4 hours |
| 4 | **No server-side validation** — exploitable by clients | `ColorUpdatePacket`, `AreaColorUpdatePacket` | 1 day |
| 5 | **No permission checks** — anyone can recolor protected blocks | `ColorWandItem`, `AreaSelectorItem` | 2 days |
| 6 | **BlockEntity approach wrong** — incompatible with vanilla blocks | Requires architecture change | 2 weeks |
| 7 | **Unbounded memory** — caches and undo/redo have no size limits | `TextureCache`, `UndoRedoManager` | 4 hours |
| 8 | **Thread safety violations** — `ConcurrentModificationException` risk | `ColorTransformManager` | 2 hours |

---

## 📋 CODING STANDARDS

### Java Style
- **Target:** Java 17 features (records, switch expressions, text blocks are acceptable)
- **Naming:** `camelCase` for methods/variables, `PascalCase` for classes, `UPPER_SNAKE_CASE` for constants
- **No magic numbers** — use named constants in `Constants.java`
- **Null safety:** Use `Optional<T>` for nullable returns; avoid returning `null`
- **Logging:** Use `Constants.LOG` (SLF4J), never `System.out.println`

### Mixin Rules
- Always prefix injected methods with `colorvariants$` (e.g., `colorvariants$renderColoredBlock`)
- Use `@Unique` for any added fields/methods in a mixin class
- Prefer `@Inject` with `CallbackInfo` over `@Overwrite`
- Register all mixins in `colorvariants.mixins.json` or `colorvariants.fabric.mixins.json`

### Architecture Rules
- **Never** use `static` fields for per-player or per-world state
- **Always** store player-specific state in item NBT or player capability
- Server-side handlers must validate: distance ≤ 8 blocks, rate limit, area ≤ 64×64×64
- Use `Services.*` interfaces (Service Locator pattern) — never call platform APIs directly from `common/`

### Rendering Rules
- Use **vertex coloring** (modify `BakedQuad` vertex data), NOT texture generation
- Must handle: multi-face blocks, tint indices, null direction quads
- Must hook into `ModelBlockRenderer` via mixin
- For Sodium: implement via Fabric Rendering API (FRAPI) — `FabricBakedModel` + `RenderContext`

---

## 🔧 DEVELOPMENT ENVIRONMENT

### Building
```bash
./gradlew build                    # Build all loaders
./gradlew :fabric:build            # Fabric only
./gradlew :forge:build             # Forge only
./gradlew :common:compileJava      # Quick syntax check
```

### Testing
```bash
./gradlew test                     # Run all unit tests
./gradlew :common:test             # Common module tests only
./gradlew test --tests "*.ColorTransformTest" # Specific test class
```

### Running in Dev
```bash
./gradlew :fabric:runClient        # Launch Fabric dev client
./gradlew :fabric:runServer        # Launch Fabric dev server
./gradlew :forge:runClient         # Launch Forge dev client
```

### Code Quality
```bash
./gradlew checkstyleMain           # Run Checkstyle
./gradlew spotbugsMain             # Run SpotBugs (if configured)
./gradlew jacocoTestReport         # Generate coverage report
```

---

## 🏗️ IMPLEMENTATION PATTERNS

### Fix: Area Selector Multiplayer Bug (START HERE — 4 hours)

**Problem:** `private static BlockPos firstPos` is shared across all players.

**Solution:** Store positions in item NBT:
```java
// In AreaSelectorItem.java — REPLACE static fields with NBT:
private static final String NBT_FIRST_POS = "FirstPos";
private static final String NBT_SECOND_POS = "SecondPos";

public Optional<BlockPos> getFirstPos(ItemStack stack) {
    CompoundTag tag = stack.getOrCreateTag();
    if (!tag.contains(NBT_FIRST_POS)) return Optional.empty();
    int[] coords = tag.getIntArray(NBT_FIRST_POS);
    return Optional.of(new BlockPos(coords[0], coords[1], coords[2]));
}

public void setFirstPos(ItemStack stack, BlockPos pos) {
    CompoundTag tag = stack.getOrCreateTag();
    tag.putIntArray(NBT_FIRST_POS, new int[]{pos.getX(), pos.getY(), pos.getZ()});
}
```

### Fix: Server Validation (PacketHandler)

```java
// ColorUpdatePacket handler — add these checks:
private static final int MAX_DISTANCE_SQ = 64; // 8 blocks squared
private static final int MAX_RATE_PER_SECOND = 20;
private static final int MAX_AREA_VOLUME = 262144; // 64^3

public static void handle(ColorUpdatePacket packet, ServerPlayer player) {
    // 1. Distance check
    if (player.distanceToSqr(Vec3.atCenterOf(packet.pos)) > MAX_DISTANCE_SQ) {
        Constants.LOG.warn("Player {} tried to color block out of range", player.getName());
        return;
    }
    // 2. Rate limiting (use per-player cooldown map with ConcurrentHashMap)
    // 3. Permission check (claim mods, op status)
    // 4. Area volume check for AreaColorUpdatePacket
    ColorTransformManager.get(player.level()).setTransform(packet.pos, packet.transform);
}
```

### Fix: Thread-Safe ColorTransformManager

```java
// Replace HashMap with ConcurrentHashMap:
private final Map<BlockPos, ColorTransform> transforms = new ConcurrentHashMap<>();

// Use computeIfAbsent instead of manual put:
public ColorTransform getTransform(BlockPos pos) {
    return transforms.getOrDefault(pos, ColorTransform.NONE);
}
```

### Fix: LRU Cache with Size Limits

```java
// In TextureCache.java — add size limit:
private static final int MAX_CACHE_SIZE = 256;
private final LinkedHashMap<ResourceLocation, Object> cache =
    new LinkedHashMap<>(MAX_CACHE_SIZE, 0.75f, true) {
        @Override
        protected boolean removeEldestEntry(Map.Entry<ResourceLocation, Object> eldest) {
            return size() > MAX_CACHE_SIZE;
        }
    };
```

### Rendering Implementation (Critical Path)

Create `common/src/main/java/com/colorvariants/mixin/MixinBlockModelRenderer.java`:
```java
@Mixin(ModelBlockRenderer.class)
public class MixinBlockModelRenderer {

    @Inject(method = "tesselateBlock", at = @At("HEAD"), cancellable = true)
    public void colorvariants$renderColoredBlock(
        BlockAndTintGetter level, BakedModel model, BlockState state,
        BlockPos pos, PoseStack poseStack, VertexConsumer consumer,
        boolean checkSides, RandomSource random, long seed, int overlay,
        CallbackInfo ci
    ) {
        ColorTransform transform = ColorTransformManager.get(level).getTransform(pos);
        if (transform.isNone()) return;

        ci.cancel();
        // Render all directions including null (general quads)
        for (Direction dir : Direction.values()) {
            renderQuads(model.getQuads(state, dir, random), transform, level, state, pos, consumer, poseStack, overlay);
        }
        renderQuads(model.getQuads(state, null, random), transform, level, state, pos, consumer, poseStack, overlay);
    }

    private void renderQuads(List<BakedQuad> quads, ColorTransform transform,
                              BlockAndTintGetter level, BlockState state, BlockPos pos,
                              VertexConsumer consumer, PoseStack poseStack, int overlay) {
        for (BakedQuad quad : quads) {
            int[] vertices = quad.getVertices().clone(); // NEVER mutate original
            int baseColor = transform.toARGB();
            for (int v = 0; v < 4; v++) {
                vertices[v * 8 + 3] = multiplyColors(vertices[v * 8 + 3], baseColor);
            }
            // Put modified quad using consumer.putBulkData(...)
        }
    }
}
```

---

## 🧪 TESTING REQUIREMENTS

**Minimum coverage before any PR is merged:** 70% line coverage on `common/` module.

### Test Organization
```
common/src/test/java/com/colorvariants/
├── core/
│   ├── ColorTransformTest.java      ← Test all color math
│   └── UndoRedoManagerTest.java     ← Test stack limits, undo/redo
├── data/
│   └── ColorSchemeManagerTest.java  ← Test save/load
├── item/
│   └── AreaSelectorItemTest.java    ← Test NBT storage (regression for MP bug)
└── network/
    └── PacketValidationTest.java    ← Test server-side validation logic
```

### Required Tests for Every Fix
- The bug scenario that caused the issue (regression test)
- Happy path (correct behavior)
- Edge cases (null, empty, max values)

### Example: Area Selector Regression Test
```java
@Test
void areaSelector_twoPlayersHaveIndependentPositions() {
    ItemStack player1Stack = new ItemStack(ColorVariants.AREA_SELECTOR.get());
    ItemStack player2Stack = new ItemStack(ColorVariants.AREA_SELECTOR.get());
    AreaSelectorItem item = (AreaSelectorItem) player1Stack.getItem();

    item.setFirstPos(player1Stack, new BlockPos(10, 64, 10));
    item.setFirstPos(player2Stack, new BlockPos(30, 64, 30));

    assertEquals(new BlockPos(10, 64, 10), item.getFirstPos(player1Stack).orElseThrow());
    assertEquals(new BlockPos(30, 64, 30), item.getFirstPos(player2Stack).orElseThrow());
}
```

---

## 🔄 WORKFLOW & PR GUIDELINES

### Branch Naming
```
fix/issue-001-rendering-system
fix/issue-012-area-selector-multiplayer
feat/sodium-compatibility
feat/permission-system
refactor/thread-safe-transforms
test/color-transform-coverage
```

### Commit Message Format
```
fix(rendering): implement vertex coloring for block color display (#001)
fix(security): add server-side distance and rate limit validation (#003)
feat(compat): add Sodium/FRAPI rendering integration (#017)
test(area-selector): add regression test for multiplayer static state bug
```

### PR Checklist
- [ ] Issue number referenced in PR title
- [ ] Unit tests added/updated (minimum 1 regression test per bug fix)
- [ ] No new `static` fields for shared state
- [ ] `./gradlew build` passes
- [ ] `./gradlew test` passes with ≥70% coverage maintained
- [ ] Multiplayer behavior considered
- [ ] Server-side validation present for any client→server packet

### What Jules Should NOT Do
- ❌ Break the multiloader architecture — changes in `common/` must not import Fabric/Forge APIs
- ❌ Remove the Service Locator pattern — always use `Services.*`
- ❌ Use `static` fields for per-player/per-world mutable state
- ❌ Merge anything that reduces test coverage below 70%
- ❌ Change Minecraft version targets without explicit instruction
- ❌ Introduce new dependencies without noting them in the PR description

---

## 🤖 AUTONOMOUS ANALYSIS TASKS

Jules is authorized to **proactively identify and fix** the following without explicit prompting:

1. **Code smells:** Magic numbers, hardcoded strings, missing null checks
2. **Missing `@Unique` annotations** on mixin-added fields
3. **TODO/FIXME comments** — implement or convert to GitHub Issues
4. **Missing Javadoc** on public API methods
5. **Thread safety** — any `HashMap` in a class accessed from multiple threads should become `ConcurrentHashMap`
6. **Resource leaks** — streams or connections not closed in `finally` or try-with-resources
7. **Deprecated API usage** — upgrade to current Minecraft 1.20.1 APIs

**For each autonomous fix, Jules should:**
- Create a focused branch (`fix/auto-<description>`)
- Write a clear description of what was found and why it's a problem
- Include test coverage for the fix

---

## 📦 DEPENDENCY MANAGEMENT

### Current Dependencies (do not remove)
- Fabric Loader / Fabric API
- SpongePowered Mixin 0.8.5
- JSR305 (FindBugs annotations)
- NeoForge MDK (Legacy Forge support)

### Approved New Dependencies (may add if needed)
- `it.unimi.dsi:fastutil:8.5.12` — high-performance primitive collections
- `org.junit.jupiter:junit-jupiter:5.9.3` — testing
- `org.mockito:mockito-core:5.3.1` — mocking in tests
- Fabric Rendering API (FRAPI) — for Sodium compatibility

### Requires Approval Before Adding
- Any networking library
- Any reflection utilities
- Any dependency >1MB
- Any non-standard Minecraft modding library

---

## 🌐 COMPATIBILITY TARGETS

| Platform | Loader | Minecraft | Priority |
|----------|--------|-----------|----------|
| Fabric + Sodium | Fabric 0.14+ | 1.20.1 | 🔴 Critical |
| Fabric vanilla | Fabric 0.14+ | 1.20.1 | 🔴 Critical |
| Forge | Forge / NeoForge | 1.20.1 | 🟡 Major |
| Fabric + Iris Shaders | Fabric 0.14+ | 1.20.1 | 🟡 Major |

---

## 📁 KEY FILES REFERENCE

| File | Purpose | Status |
|------|---------|--------|
| `common/.../core/ColorTransform.java` | Color data model | ✅ Mostly OK |
| `common/.../core/ColorTransformManager.java` | Per-world color storage | ⚠️ Not thread-safe |
| `common/.../core/TextureGenerator.java` | WRONG APPROACH — delete | ❌ Remove |
| `common/.../core/UndoRedoManager.java` | Undo/redo | ⚠️ No stack limit |
| `common/.../client/renderer/ColoredBlockRenderer.java` | Rendering stub | ❌ Implement |
| `common/.../mixin/MixinBlockRenderDispatcher.java` | Render hook | ❌ Incomplete |
| `common/.../item/AreaSelectorItem.java` | Area tool | ❌ MP bug |
| `common/.../network/ColorUpdatePacket.java` | Color packet | ❌ No validation |
| `common/.../util/TextureCache.java` | Texture cache | ❌ No size limit |
| `fabric/.../platform/FabricNetworkHelper.java` | Fabric networking | ✅ OK |

---

## 🗓️ IMPLEMENTATION PHASES

### Phase 1 — Critical Fixes (Target: 2 weeks)
1. Area Selector NBT fix (4 hrs)
2. Server validation (1 day)
3. Thread-safe transforms (2 hrs)
4. Cache size limits (4 hrs)
5. **Rendering system** — vertex coloring mixin (2–3 weeks, parallel)

### Phase 2 — Major Improvements (Target: weeks 3–6)
1. Sodium/FRAPI compatibility
2. Permission system
3. Config validation
4. Performance optimization

### Phase 3 — Quality (Target: weeks 7–10)
1. Test coverage to 80%+
2. Documentation
3. Compatibility testing matrix
4. Beta release prep

---

*This file is Jules's primary context source. Keep it updated as the codebase evolves.*
*For full issue details, see the `docs/` folder: Bug Catalog, Technical Analysis, Implementation Guide.*
