# Contributing to Color Variants Mod

Panduan ini berlaku untuk **kontributor manusia** dan **AI Agents*.

## 🚀 Quick Start

```bash
git clone https://github.com/deftorch/colorvariants
cd colorvariants
./gradlew build       # Verifikasi setup
./gradlew :fabric:runClient  # Test di game
```

**Requirement:** Java 17+, Gradle 8+

---

## 🌿 Branch Strategy

```
main          ← Production releases only
develop       ← Integration branch, semua PR merge ke sini
fix/XXX       ← Bug fixes
feat/XXX      ← New features
refactor/XXX  ← Code improvements
test/XXX      ← Test additions
auto/XXX      ← Jules auto-generated fixes
```

---

## 📝 Commit Convention

Format: `type(scope): description (#issue)`

| Type | Kapan Digunakan |
|------|-----------------|
| `fix` | Bug fix |
| `feat` | Fitur baru |
| `refactor` | Code improvement tanpa behavior change |
| `test` | Menambah/update tests |
| `docs` | Dokumentasi saja |
| `perf` | Performance improvement |
| `build` | Build system / CI changes |
| `chore` | Maintenance tasks |

**Scopes:** `rendering`, `security`, `network`, `items`, `config`, `compat`, `core`, `build`, `ci`

**Contoh:**
```
fix(items): store area selector positions in NBT instead of static fields (#012)
feat(compat): add FRAPI rendering support for Sodium compatibility (#017)
test(core): add regression test for concurrent ColorTransformManager access
```

---

## ✅ PR Requirements

Sebelum membuat PR, pastikan:

- [ ] Branch dibuat dari `develop` (bukan `main`)
- [ ] Commit message mengikuti format di atas
- [ ] `./gradlew build` berhasil (zero errors)
- [ ] `./gradlew :common:test` berhasil (semua test pass)
- [ ] Coverage tidak turun di bawah 70%
- [ ] Untuk bug fix: ada regression test
- [ ] Untuk fitur baru: ada unit test
- [ ] Tidak ada `private static` fields baru di `item/` package
- [ ] Tidak ada `System.out.println` baru
- [ ] Tidak ada import Fabric/Forge API langsung di `common/` package

---

## 🏗️ Architecture Rules

### The Golden Rules

1. **`common/` is platform-agnostic** — Tidak boleh import `net.fabricmc.*` atau `net.minecraftforge.*` langsung
2. **No static mutable state for per-player/per-world data** — Gunakan NBT atau capability
3. **All server packets must validate** — Jarak, rate limit, ukuran area
4. **Thread safety matters** — Gunakan `ConcurrentHashMap` untuk Map yang diakses dari multiple threads

### Mixin Naming
```java
// BENAR: prefix dengan colorvariants$
@Inject(...)
private void colorvariants$myInjectedMethod(CallbackInfo ci) { ... }

// SALAH: tanpa prefix (bisa conflict dengan mod lain)
@Inject(...)
private void myMethod(CallbackInfo ci) { ... }
```

### Service Locator Pattern
```java
// BENAR: gunakan Services
Services.PLATFORM.getPlatformName();
Services.REGISTRY.registerItem(...);

// SALAH: langsung call platform API dari common
FabricLoader.getInstance().getEnvironmentType(); // ❌ di common/
```

---

## 🧪 Testing Guide

### Struktur Test
```
common/src/test/java/com/colorvariants/
├── core/           ← Test logic utama
├── data/           ← Test data layer
├── item/           ← Test items (terutama regression tests)
└── network/        ← Test packet validation
```

### Menulis Test yang Baik
```java
@Test
@DisplayName("Area selector stores positions independently per ItemStack")
void areaSelector_differentStacks_haveIndependentPositions() {
    // ARRANGE
    ItemStack stack1 = new ItemStack(/* ... */);
    ItemStack stack2 = new ItemStack(/* ... */);
    AreaSelectorItem item = /* ... */;
    
    // ACT
    item.setFirstPos(stack1, new BlockPos(10, 64, 10));
    item.setFirstPos(stack2, new BlockPos(30, 64, 30));
    
    // ASSERT
    assertEquals(new BlockPos(10, 64, 10), item.getFirstPos(stack1).orElseThrow(),
        "Stack 1 should have its own position");
    assertEquals(new BlockPos(30, 64, 30), item.getFirstPos(stack2).orElseThrow(),
        "Stack 2 should have its own position");
}
```

**Rules untuk tests:**
- Gunakan `@DisplayName` untuk deskripsi yang jelas
- Pattern: Arrange → Act → Assert
- Satu test untuk satu behavior
- Test edge cases: null, empty, maximum values
- Regression tests harus mendeskripsikan bug yang dicegah

---

## 🤖 Instruksi Khusus untuk AI Agents

AI Agents harus:
1. **Selalu baca `AGENTS.md`** sebelum memulai task apapun
2. **Selalu buat branch terpisah** untuk setiap fix (tidak boleh commit langsung ke develop/main)
3. **Selalu tulis test** untuk setiap bug fix (minimum 1 regression test)
4. **Selalu mention issue number** di commit dan PR title
5. **Tidak boleh merge** sendiri — buat PR dan tunggu review

Jules diizinkan untuk proaktif:
- Memperbaiki TODO/FIXME yang ditemukan
- Menambahkan Javadoc yang kurang
- Memperbaiki code style violations
- Menambahkan test untuk code yang belum tercakup

---

## 📫 Issue Reporting

### Bug Report
```markdown
**Bug:** Deskripsi singkat
**Severity:** P0/P1/P2/P3
**Reproducible:** Ya/Tidak
**Steps:**
1. ...
2. ...
**Expected:** ...
**Actual:** ...
**Environment:** Minecraft 1.20.1, Fabric/Forge, Sodium Yes/No
```

### Feature Request
```markdown
**Feature:** Deskripsi
**Rationale:** Mengapa dibutuhkan?
**Scope:** Perkiraan effort (kecil/sedang/besar)
**Risks:** Potensi masalah
```

---

## 🏷️ Labels

| Label | Deskripsi |
|-------|-----------|
| `critical` | P0 — blocker |
| `bug` | P1 — bug penting |
| `enhancement` | P2/P3 — improvement |
| `security` | Isu keamanan |
| `multiplayer` | Isu spesifik multiplayer |
| `rendering` | Isu rendering |
| `compatibility` | Isu kompatibilitas mod lain |
| `jules-task` | Ditugaskan ke Jules |
| `auto-fix` | Di-generate otomatis oleh Jules |
| `daily-analysis` | Dari daily automated analysis |
| `needs-review` | Menunggu code review |
| `good-first-issue` | Bagus untuk kontributor baru |

---

*Terima kasih sudah berkontribusi ke Color Variants Mod!*
