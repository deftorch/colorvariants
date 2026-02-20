# TODO — Color Variants Mod
## Task Tracking & Implementation Checklist

> **Last Updated:** February 2026  
> **Total Tasks:** 86 issues documented  
> **Current Sprint:** Phase 1 — Critical Fixes

**Quick Links:**
- [P0 Critical](#-p0--critical-blocking-issues) (17 tasks) — **Must fix before release**
- [P1 Major](#-p1--major-issues) (35 tasks) — Important for quality
- [P2 Minor](#-p2--minor-improvements) (24 tasks) — Polish
- [Jules Daily Tasks](#-jules-daily-tasks) — Automated maintenance

---

## 📊 Progress Overview

```
Overall Progress: [████████░░░░░░░░░░░░] 40%

P0 Critical:  [███░░░░░░░░░░░░░░░░░] 3/17 (18%)
P1 Major:     [██░░░░░░░░░░░░░░░░░░] 4/35 (11%)
P2 Minor:     [█████░░░░░░░░░░░░░░░] 6/24 (25%)
P3 Future:    [░░░░░░░░░░░░░░░░░░░░] 0/10 (0%)
```

**Current Sprint Goals:**
- [ ] Complete rendering system implementation
- [ ] Fix all multiplayer critical bugs
- [ ] Add server-side security validation
- [ ] Reach 70% test coverage

---

## 🔴 P0 — Critical Blocking Issues

> **Priority:** These BLOCK production release. Must be fixed first.

### Rendering (5 tasks)

- [ ] **#001** Implement vertex coloring rendering system
  - **Effort:** 2-3 weeks
  - **Assignee:** Jules / Lead Dev
  - **Status:** 🔴 Not Started
  - **Blocker:** This is THE main blocker
  - **Files:** Create `MixinBlockModelRenderer.java`
  - **Tests Required:** Vertex color modification, tint handling
  - **Details:** See [AGENTS.md Rendering Implementation](AGENTS.md#rendering-implementation)

- [ ] **#002** Delete TextureGenerator.java (wrong approach)
  - **Effort:** 1 hour
  - **Depends On:** #001
  - **Assignee:** Anyone
  - **Status:** 🔴 Blocked by #001
  - **Action:** Remove file, update references

- [ ] **#003** Update ColoredBlockRenderer.java to use vertex coloring
  - **Effort:** 4 hours
  - **Depends On:** #001
  - **Assignee:** Jules
  - **Status:** 🔴 Blocked by #001

- [ ] **#004** Register MixinBlockModelRenderer in mixins.json
  - **Effort:** 15 minutes
  - **Depends On:** #001
  - **Assignee:** Anyone
  - **Status:** 🔴 Blocked by #001
  - **File:** `common/src/main/resources/colorvariants.mixins.json`

- [ ] **#005** Handle multi-face blocks (logs, grass, etc.)
  - **Effort:** 1 day
  - **Depends On:** #001
  - **Assignee:** Lead Dev
  - **Status:** 🔴 Blocked by #001
  - **Notes:** Must handle all 6 directions + null direction

### Security (3 tasks)

- [ ] **#006** Add server-side validation to ColorUpdatePacket
  - **Effort:** 1 day
  - **Assignee:** Jules
  - **Status:** 🔴 Not Started
  - **Required Checks:**
    - [ ] Distance validation (≤8 blocks)
    - [ ] Rate limiting (≤20 packets/sec)
    - [ ] Null checks
  - **Files:** `network/ColorUpdatePacket.java`
  - **Prompt:** Use [Instruksi #4](docs/INSTRUKSI_COPAS.md#instruksi-4)

- [ ] **#007** Add server-side validation to AreaColorUpdatePacket
  - **Effort:** 1 day
  - **Assignee:** Jules
  - **Status:** 🔴 Not Started
  - **Required Checks:**
    - [ ] Distance validation
    - [ ] Area size limit (≤64³ blocks)
    - [ ] Rate limiting
  - **Files:** `network/AreaColorUpdatePacket.java`

- [ ] **#008** Implement RateLimiter utility class
  - **Effort:** 3 hours
  - **Assignee:** Jules
  - **Status:** 🔴 Not Started
  - **Files:** Create `common/src/main/java/com/colorvariants/util/RateLimiter.java`
  - **Implementation:** `ConcurrentHashMap<UUID, Long>` with cleanup

### Multiplayer (3 tasks)

- [ ] **#012** Fix Area Selector static state bug
  - **Effort:** 4 hours
  - **Assignee:** Jules
  - **Status:** 🟡 Ready to Start
  - **Priority:** **START HERE** — Easiest P0 task
  - **Solution:** Store positions in ItemStack NBT, not static fields
  - **Files:** `item/AreaSelectorItem.java`
  - **Regression Test:** Test 2 ItemStacks have independent positions
  - **Prompt:** Use [Instruksi #2](docs/INSTRUKSI_COPAS.md#instruksi-2)

- [ ] **#013** Add permission checks to ColorWandItem
  - **Effort:** 2 days
  - **Assignee:** Lead Dev
  - **Status:** 🔴 Not Started
  - **Integration:** FTB Chunks, GriefPrevention, LuckPerms
  - **Files:** `item/ColorWandItem.java`

- [ ] **#014** Add permission checks to AreaSelectorItem
  - **Effort:** 2 days
  - **Assignee:** Lead Dev
  - **Status:** 🔴 Not Started
  - **Depends On:** #013 (same implementation pattern)

### Data Integrity (3 tasks)

- [ ] **#015** Make ColorTransformManager thread-safe
  - **Effort:** 2 hours
  - **Assignee:** Jules
  - **Status:** 🔴 Not Started
  - **Solution:** Replace `HashMap` with `ConcurrentHashMap`
  - **Files:** `core/ColorTransformManager.java`
  - **Tests:** Concurrent access from multiple threads

- [ ] **#016** Add size limit to TextureCache
  - **Effort:** 2 hours
  - **Assignee:** Jules
  - **Status:** 🔴 Not Started
  - **Solution:** LinkedHashMap with max 256 entries (LRU)
  - **Files:** `util/TextureCache.java`

- [ ] **#017** Add size limit to UndoRedoManager stack
  - **Effort:** 1 hour
  - **Assignee:** Jules
  - **Status:** 🔴 Not Started
  - **Solution:** Limit to 50 actions, remove eldest
  - **Files:** `core/UndoRedoManager.java`

### Architecture (3 tasks)

- [ ] **#018** Remove BlockEntity approach (incompatible with vanilla)
  - **Effort:** 2 weeks
  - **Assignee:** Lead Dev
  - **Status:** 🔴 Not Started
  - **Impact:** Major refactor
  - **Notes:** BlockEntity can't be added to vanilla blocks

- [ ] **#019** Implement proper world data storage
  - **Effort:** 1 week
  - **Depends On:** #018
  - **Assignee:** Lead Dev
  - **Status:** 🔴 Not Started
  - **Solution:** Use SavedData per dimension

- [ ] **#020** Add data migration system
  - **Effort:** 3 days
  - **Depends On:** #019
  - **Assignee:** Lead Dev
  - **Status:** 🔴 Not Started
  - **Purpose:** Migrate old BlockEntity data to new system

---

## 🟡 P1 — Major Issues

> **Priority:** Important for quality release. Should fix before v1.0.

### Compatibility (7 tasks)

- [ ] **#017** Implement Sodium/Embeddium compatibility via FRAPI
  - **Effort:** 1-2 weeks
  - **Assignee:** Lead Dev
  - **Status:** 🔴 Not Started
  - **Impact:** 90% of Fabric users need this
  - **Option A:** Require Indium dependency (quick)
  - **Option B:** Native FRAPI implementation (better)
  - **Prompt:** Use [Instruksi #6](docs/INSTRUKSI_COPAS.md#instruksi-6)

- [ ] **#018** Test with Iris Shaders compatibility
  - **Effort:** 1 week
  - **Depends On:** #001, #017
  - **Assignee:** QA
  - **Status:** 🔴 Blocked
  - **Test Matrix:** 10 popular shader packs

- [ ] **#019** Test with Optifine compatibility
  - **Effort:** 3 days
  - **Depends On:** #001
  - **Assignee:** QA
  - **Status:** 🔴 Blocked
  - **Expected:** Limited support, document issues

- [ ] **#020** WorldEdit integration
  - **Effort:** 2 weeks
  - **Assignee:** Community / Lead Dev
  - **Status:** 🔴 Not Started
  - **Features:** Copy colors, paste colors, select colored regions

- [ ] **#021** Litematica integration
  - **Effort:** 1 week
  - **Assignee:** Community
  - **Status:** 🔴 Not Started
  - **Features:** Save/load colors in schematics

- [ ] **#022** Create mod compatibility integration
  - **Effort:** 1 week
  - **Assignee:** Community
  - **Status:** 🔴 Not Started

- [ ] **#023** FTB Chunks claim integration
  - **Effort:** 3 days
  - **Assignee:** Jules
  - **Status:** 🔴 Not Started
  - **Purpose:** Respect claim permissions

### Testing (6 tasks)

- [ ] **#024** Setup JUnit 5 test framework
  - **Effort:** 2 hours
  - **Assignee:** Jules
  - **Status:** 🔴 Not Started
  - **Files:** `common/build.gradle`, create test folder structure

- [ ] **#025** Write ColorTransformTest
  - **Effort:** 4 hours
  - **Assignee:** Jules
  - **Status:** 🔴 Not Started
  - **Coverage Target:** 90%+
  - **Tests:** RGB operations, serialization, validation

- [ ] **#026** Write AreaSelectorItemTest (regression)
  - **Effort:** 2 hours
  - **Depends On:** #012
  - **Assignee:** Jules
  - **Status:** 🔴 Blocked
  - **Must Test:** Multiplayer bug doesn't return

- [ ] **#027** Write UndoRedoManagerTest
  - **Effort:** 3 hours
  - **Assignee:** Jules
  - **Status:** 🔴 Not Started
  - **Tests:** Undo, redo, stack limits

- [ ] **#028** Write PacketValidationTest
  - **Effort:** 4 hours
  - **Depends On:** #006, #007
  - **Assignee:** Jules
  - **Status:** 🔴 Blocked
  - **Tests:** Distance, rate limiting, area size validation

- [ ] **#029** Reach 70% test coverage minimum
  - **Effort:** 1 week
  - **Assignee:** Jules + Team
  - **Status:** 🔴 Not Started
  - **Current:** ~0%
  - **Tool:** JaCoCo

### Configuration (4 tasks)

- [ ] **#030** Implement proper config validation
  - **Effort:** 1 day
  - **Assignee:** Jules
  - **Status:** 🔴 Not Started
  - **Files:** `config/ModConfig.java`
  - **Validate:** Ranges, types, conflicts

- [ ] **#031** Add config GUI (ModMenu for Fabric)
  - **Effort:** 1 week
  - **Assignee:** Community
  - **Status:** 🔴 Not Started

- [ ] **#032** Add server-side config overrides
  - **Effort:** 3 days
  - **Assignee:** Lead Dev
  - **Status:** 🔴 Not Started
  - **Purpose:** Server can enforce limits on clients

- [ ] **#033** Config migration system
  - **Effort:** 2 days
  - **Assignee:** Jules
  - **Status:** 🔴 Not Started

### Performance (6 tasks)

- [ ] **#034** Profile memory usage with 10K colored blocks
  - **Effort:** 1 day
  - **Assignee:** Lead Dev
  - **Status:** 🔴 Not Started
  - **Tool:** VisualVM or JProfiler

- [ ] **#035** Optimize ColorTransform storage (use int instead of float)
  - **Effort:** 3 days
  - **Assignee:** Lead Dev
  - **Status:** 🔴 Not Started
  - **Savings:** 12 bytes → 4 bytes per transform

- [ ] **#036** Implement chunk-based caching
  - **Effort:** 1 week
  - **Assignee:** Lead Dev
  - **Status:** 🔴 Not Started
  - **Purpose:** Only keep loaded chunks in memory

- [ ] **#037** Batch packet sending for area updates
  - **Effort:** 2 days
  - **Assignee:** Jules
  - **Status:** 🔴 Not Started
  - **Current:** 1 packet per block (bad for 64³ areas)
  - **Solution:** Send chunk-based or batched updates

- [ ] **#038** Add async world save
  - **Effort:** 3 days
  - **Assignee:** Lead Dev
  - **Status:** 🔴 Not Started
  - **Purpose:** Don't block main thread on save

- [ ] **#039** Optimize GUI rendering (color picker redraws)
  - **Effort:** 2 days
  - **Assignee:** Community
  - **Status:** 🔴 Not Started

### Documentation (4 tasks)

- [ ] **#040** Write user guide (how to use mod)
  - **Effort:** 1 week
  - **Assignee:** Community / Jules
  - **Status:** 🔴 Not Started
  - **Format:** Wiki pages with screenshots

- [ ] **#041** Write API documentation (for other mods)
  - **Effort:** 3 days
  - **Assignee:** Lead Dev
  - **Status:** 🔴 Not Started

- [ ] **#042** Create video tutorials
  - **Effort:** 1 week
  - **Assignee:** Community
  - **Status:** 🔴 Not Started
  - **Topics:** Basic usage, advanced features, modpack integration

- [ ] **#043** Generate JavaDoc for all public APIs
  - **Effort:** 2 days
  - **Assignee:** Jules
  - **Status:** 🔴 Not Started
  - **Tool:** `./gradlew javadoc`

### Network (4 tasks)

- [ ] **#044** Implement delta sync (only send changed blocks)
  - **Effort:** 1 week
  - **Assignee:** Lead Dev
  - **Status:** 🔴 Not Started

- [ ] **#045** Add packet compression for large updates
  - **Effort:** 3 days
  - **Assignee:** Lead Dev
  - **Status:** 🔴 Not Started

- [ ] **#046** Implement client-side prediction
  - **Effort:** 1 week
  - **Assignee:** Lead Dev
  - **Status:** 🔴 Not Started
  - **Purpose:** Show color change immediately, rollback if rejected

- [ ] **#047** Add disconnect/reconnect color persistence
  - **Effort:** 2 days
  - **Assignee:** Jules
  - **Status:** 🔴 Not Started

### UX & Accessibility (5 tasks)

- [ ] **#048** Add colorblind mode to color picker
  - **Effort:** 1 week
  - **Assignee:** Community
  - **Status:** 🔴 Not Started
  - **Options:** Protanopia, Deuteranopia, Tritanopia

- [ ] **#049** Add keyboard shortcuts for tools
  - **Effort:** 2 days
  - **Assignee:** Jules
  - **Status:** 🔴 Not Started
  - **Examples:** Ctrl+Z (undo), Ctrl+Y (redo)

- [ ] **#050** Add tooltips to all GUI elements
  - **Effort:** 1 day
  - **Assignee:** Jules
  - **Status:** 🔴 Not Started

- [ ] **#051** Improve color picker UX (add hex input)
  - **Effort:** 3 days
  - **Assignee:** Community
  - **Status:** 🔴 Not Started

- [ ] **#052** Add localization (i18n) support
  - **Effort:** 1 week
  - **Assignee:** Community
  - **Status:** 🔴 Not Started
  - **Languages:** EN, ES, FR, DE, ZH, JA, PT, RU

### Cross-Version (3 tasks)

- [ ] **#053** Add version adapter pattern
  - **Effort:** 1 week
  - **Assignee:** Lead Dev
  - **Status:** 🔴 Not Started
  - **Purpose:** Support multiple Minecraft versions

- [ ] **#054** Port to Minecraft 1.20.4
  - **Effort:** 2 weeks
  - **Depends On:** #053
  - **Assignee:** Lead Dev
  - **Status:** 🔴 Not Started

- [ ] **#055** Port to Minecraft 1.21
  - **Effort:** 3 weeks
  - **Depends On:** #053
  - **Assignee:** Lead Dev
  - **Status:** 🔴 Not Started

---

## 🟢 P2 — Minor Improvements

> **Priority:** Polish and enhancements. Nice to have for v1.0.

### Features (12 tasks)

- [ ] **#056** Add color history (recent colors)
  - **Effort:** 1 day
  - **Assignee:** Jules
  - **Status:** 🔴 Not Started

- [ ] **#057** Add color favorites/bookmarks
  - **Effort:** 2 days
  - **Assignee:** Jules
  - **Status:** 🔴 Not Started

- [ ] **#058** Add preset categories (Neon, Pastel, etc.)
  - **Effort:** 3 days
  - **Assignee:** Community
  - **Status:** 🔴 Not Started

- [ ] **#059** Add color palette import/export
  - **Effort:** 3 days
  - **Assignee:** Jules
  - **Status:** 🔴 Not Started
  - **Format:** JSON or custom format

- [ ] **#060** Add color randomizer tool
  - **Effort:** 1 day
  - **Assignee:** Jules
  - **Status:** 🔴 Not Started

- [ ] **#061** Add color picker from image file
  - **Effort:** 1 week
  - **Assignee:** Community
  - **Status:** 🔴 Not Started

- [ ] **#062** Add gradient tool
  - **Effort:** 2 weeks
  - **Assignee:** Lead Dev
  - **Status:** 🔴 Not Started

- [ ] **#063** Add pattern/texture overlays
  - **Effort:** 2 weeks
  - **Assignee:** Lead Dev
  - **Status:** 🔴 Not Started

- [ ] **#064** Add color replace tool (find/replace color)
  - **Effort:** 1 week
  - **Assignee:** Community
  - **Status:** 🔴 Not Started

- [ ] **#065** Add color info panel (show RGB, HSV, Hex)
  - **Effort:** 2 days
  - **Assignee:** Jules
  - **Status:** 🔴 Not Started

- [ ] **#066** Add copy/paste color between blocks
  - **Effort:** 3 days
  - **Assignee:** Jules
  - **Status:** 🔴 Not Started

- [ ] **#067** Add mass color clear command
  - **Effort:** 2 days
  - **Assignee:** Jules
  - **Status:** 🔴 Not Started
  - **Command:** `/color clear <radius>`

### Code Quality (8 tasks)

- [ ] **#068** Remove all magic numbers
  - **Effort:** 2 days
  - **Assignee:** Jules
  - **Status:** 🔴 Not Started
  - **Tool:** Checkstyle MagicNumber rule

- [ ] **#069** Add Javadoc to all public methods
  - **Effort:** 1 week
  - **Assignee:** Jules
  - **Status:** 🔴 Not Started

- [ ] **#070** Setup SpotBugs for static analysis
  - **Effort:** 1 day
  - **Assignee:** Jules
  - **Status:** 🔴 Not Started

- [ ] **#071** Setup Checkstyle enforcement
  - **Effort:** 1 day
  - **Assignee:** Jules
  - **Status:** 🔴 Not Started

- [ ] **#072** Refactor long methods (>150 lines)
  - **Effort:** 1 week
  - **Assignee:** Jules
  - **Status:** 🔴 Not Started

- [ ] **#073** Add null-safety annotations (@Nullable, @NotNull)
  - **Effort:** 3 days
  - **Assignee:** Jules
  - **Status:** 🔴 Not Started

- [ ] **#074** Replace System.out with proper logging
  - **Effort:** 2 hours
  - **Assignee:** Jules
  - **Status:** 🔴 Not Started
  - **Check:** `grep -r "System.out"`

- [ ] **#075** Fix all Checkstyle violations
  - **Effort:** 1 week
  - **Depends On:** #071
  - **Assignee:** Jules
  - **Status:** 🔴 Not Started

### Performance (4 tasks)

- [ ] **#076** Add performance metrics logging
  - **Effort:** 2 days
  - **Assignee:** Jules
  - **Status:** 🔴 Not Started

- [ ] **#077** Profile FPS impact of 1000+ colored blocks
  - **Effort:** 1 day
  - **Assignee:** QA
  - **Status:** 🔴 Not Started
  - **Target:** <5% FPS drop

- [ ] **#078** Profile memory impact
  - **Effort:** 1 day
  - **Assignee:** QA
  - **Status:** 🔴 Not Started
  - **Target:** <50MB for 10K colored blocks

- [ ] **#079** Optimize packet size (protocol v2)
  - **Effort:** 1 week
  - **Assignee:** Lead Dev
  - **Status:** 🔴 Not Started

---

## 💡 P3 — Future Features

> **Priority:** Post-v1.0 enhancements. Nice to have but not essential.

- [ ] **#080** Color animations system
  - **Effort:** 3 weeks
  - **Status:** 🔴 Not Started

- [ ] **#081** Particle effects based on color
  - **Effort:** 2 weeks
  - **Status:** 🔴 Not Started

- [ ] **#082** Sound effects based on color frequency
  - **Effort:** 1 week
  - **Status:** 🔴 Not Started

- [ ] **#083** API for other mods to add custom colors
  - **Effort:** 2 weeks
  - **Status:** 🔴 Not Started

- [ ] **#084** Color biome integration
  - **Effort:** 3 weeks
  - **Status:** 🔴 Not Started

- [ ] **#085** Machine learning color suggestions
  - **Effort:** 1 month
  - **Status:** 🔴 Not Started

- [ ] **#086** VR support for color picker
  - **Effort:** 2 months
  - **Status:** 🔴 Not Started

- [ ] **#087** Web interface for remote color editing
  - **Effort:** 1 month
  - **Status:** 🔴 Not Started

- [ ] **#088** Mobile app for color palette design
  - **Effort:** 2 months
  - **Status:** 🔴 Not Started

- [ ] **#089** Multiplayer collaborative coloring
  - **Effort:** 3 weeks
  - **Status:** 🔴 Not Started

---

## 🤖 Jules Daily Tasks

> **Automated:** Jules runs these checks every day (06:00 UTC via GitHub Actions)

### Morning Checklist (Every Day)

- [ ] Verify `MixinBlockModelRenderer.java` exists and is complete
- [ ] Scan for `private static` in `item/` folder → Fix if found
- [ ] Scan for `TODO`, `FIXME`, `HACK` comments → Implement or create issues
- [ ] Run `./gradlew :common:compileJava` → Fix any errors
- [ ] Run `./gradlew :common:test` → Fix failing tests
- [ ] Check test coverage → Add tests if <70%
- [ ] Create/update daily analysis GitHub issue

### Weekly Tasks (Every Monday)

- [ ] Review all open PRs → Comment on status
- [ ] Update progress in this TODO.md
- [ ] Check for dependency updates → Create PR if needed
- [ ] Run full security scan → Report findings

### Monthly Tasks (1st of each month)

- [ ] Regenerate JavaDoc
- [ ] Update README.md statistics
- [ ] Review roadmap progress
- [ ] Archive completed tasks


---

## 🎯 Sprint Planning

### Sprint 1 (Weeks 1-2) — Critical Fixes

- [ ] #012 Area Selector fix
- [ ] #006, #007 Packet validation
- [ ] #015, #016, #017 Thread safety & limits
- [ ] Start #001 Rendering

### Sprint 2 (Weeks 3-4) — Rendering Implementation

- [ ] Complete #001 Rendering
- [ ] #002, #003, #004 Rendering cleanup
- [ ] #024, #025 Setup tests

### Sprint 3 (Weeks 5-6) — Security & Testing

- [ ] #008 RateLimiter
- [ ] #013, #014 Permission checks
- [ ] #026, #027, #028 Write tests
- [ ] #029 Reach 70% coverage

### Sprint 4 (Weeks 7-8) — Compatibility

- [ ] #017 Sodium/FRAPI implementation
- [ ] #018, #019 Shader testing
- [ ] #023 FTB Chunks integration

---

**Total Estimated Time:**
- P0 only: 8-10 weeks
- P0 + P1: 20-24 weeks (5-6 months)
- P0 + P1 + P2: 32-40 weeks (8-10 months)
- All (P0-P3): 50-60 weeks (12-15 months)

---

*This TODO is auto-updated by Jules daily. For detailed issue info, see [Bug Catalog](docs/guide/03_Bug_Catalog.md).*
