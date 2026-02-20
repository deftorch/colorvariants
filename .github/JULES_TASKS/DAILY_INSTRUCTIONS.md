# 🤖 INSTRUKSI HARIAN JULES — Color Variants Mod
## Daily Autonomous Task Instructions

> **Dijalankan:** Setiap hari pukul 06:00 UTC (otomatis via GitHub Actions)  
> **Tujuan:** Jules menganalisis, memperbaiki, dan meningkatkan proyek secara otomatis tanpa perlu instruksi manual

---

## 📋 CHECKLIST HARIAN WAJIB

Jules harus menyelesaikan semua tugas berikut setiap hari secara berurutan:

---

### ✅ TUGAS 1: Analisis Kode Otomatis (15 menit)

Setiap hari, Jules harus memindai seluruh repository dan mencari:

**Cari dan Tandai:**
```
Scan file: common/src/**/*.java, fabric/src/**/*.java
```

| Yang Dicari | Tindakan Jules |
|-------------|----------------|
| `TODO` / `FIXME` / `HACK` | Buat branch `fix/auto-todo-YYYYMMDD`, implementasikan, buat PR |
| `private static` di folder `item/` | Perbaiki ke NBT storage, buat PR dengan label `bug` |
| `new HashMap<>()` di folder `core/` | Ganti dengan `ConcurrentHashMap`, buat PR |
| `System.out.println` | Ganti dengan `Constants.LOG`, buat PR |
| Method `public` tanpa Javadoc | Tambahkan Javadoc, buat PR dengan label `documentation` |
| `@Deprecated` API usage | Update ke API terbaru, buat PR |
| Magic number (angka literal tanpa nama) | Ekstrak ke konstanta di `Constants.java` |

**Format PR:**
```
Title: fix(auto): [deskripsi singkat] — YYYY-MM-DD
Label: auto-fix, jules-generated
Body: "Auto-generated fix by Jules daily analysis. [penjelasan]"
```

---

### ✅ TUGAS 2: Verifikasi Build (5 menit)

```bash
# Jules harus menjalankan ini dan memastikan ZERO error:
./gradlew :common:compileJava :fabric:compileJava

# Jika ada error → buat issue dengan label "build-broken" + "urgent"
# Jika ada warning → catat dalam daily report issue
```

---

### ✅ TUGAS 3: Progress Tracking P0 Issues

Setiap hari Jules memperbarui status 17 P0 issues dengan mengecek:

**Cek apakah file-file berikut sudah diimplementasikan:**

```
common/src/main/java/com/colorvariants/mixin/MixinBlockModelRenderer.java
└── Status: ❌ BELUM ADA → Jules harus mulai implementasi rendering
               ⚠️ ADA TAPI KOSONG → Jules harus lengkapi
               ✅ ADA DAN BERFUNGSI → update issue sebagai resolved

common/src/main/java/com/colorvariants/item/AreaSelectorItem.java
└── Cek: apakah masih ada "private static BlockPos" → jika ya, perbaiki SEGERA
```

**Update GitHub Issue #001 dengan status terkini setiap hari.**

---

### ✅ TUGAS 4: Test Coverage Monitor

```bash
./gradlew :common:test :common:jacocoTestReport

# Cek coverage di: common/build/reports/jacoco/test/
# Target: ≥70% line coverage

# Jika coverage < 70% → Jules menulis test baru untuk menutup gap
# Prioritas test yang ditulis:
# 1. ColorTransformTest (core logic)
# 2. AreaSelectorItemTest (regression MP bug)
# 3. PacketValidationTest (security)
```

---

### ✅ TUGAS 5: Dependency Security Check

```bash
# Cek apakah ada library dengan versi lama/vulnerable
./gradlew dependencyUpdates 2>/dev/null || true

# Periksa gradle.properties — apakah ada versi yang sudah > 6 bulan lama?
# Jika ya → buat PR untuk update dengan label "dependencies"
```

---

### ✅ TUGAS 6: Daily Report Issue

Jules membuat atau memperbarui GitHub Issue harian dengan format:

```markdown
## 📅 Daily Analysis Report — YYYY-MM-DD

### Build Status
- [x] common compiles ✅ / ❌
- [x] fabric compiles ✅ / ❌

### P0 Progress
| Issue | Status | Progress |
|-------|--------|----------|
| #001 Rendering | 🔴 Not started | 0% |
| #012 Area Selector | ✅ Fixed | 100% |
| ... | ... | ... |

### Auto-Fixes Applied Today
- [list of PRs created]

### Test Coverage
- Current: XX% (Target: 70%)
- Tests added today: X

### Action Items for Tomorrow
- [list what still needs doing]
```

---

## 🗓️ JADWAL MINGGUAN KHUSUS

### Setiap Senin — Sprint Planning Review
Jules membuat issue dengan:
- Ringkasan progress minggu lalu
- Estimasi P0 issues yang bisa diselesaikan minggu ini
- Identifikasi blocker

### Setiap Rabu — Mid-Week Checkpoint  
Jules memperbarui semua open PR dengan status terkini.

### Setiap Jumat — Weekly Summary
Jules membuat PR summary report yang berisi:
- PRs merged minggu ini
- PRs pending review
- Issues resolved
- Issues baru ditemukan
- Coverage trend (naik/turun)

---

## 🔥 TUGAS PRIORITAS UTAMA (Ulangi Setiap Hari sampai Selesai)

**Jules harus selalu mengerjakan ini PERTAMA sebelum tugas lain:**

### 🔴 PRIORITY 1: Implementasi Rendering System
**File target:** `common/src/main/java/com/colorvariants/mixin/MixinBlockModelRenderer.java`

Jika file ini belum ada atau belum berfungsi, Jules harus:
1. Buat branch `fix/issue-001-rendering-system`
2. Implementasikan vertex coloring mixin (lihat AGENTS.md bagian Rendering Implementation)
3. Tambahkan ke `colorvariants.mixins.json`
4. Tulis test rendering
5. Buat PR dengan description yang jelas

**Jangan pindah ke tugas lain sampai PR ini dibuat.**

### 🔴 PRIORITY 2: Fix Area Selector Jika Belum
**File target:** `common/src/main/java/com/colorvariants/item/AreaSelectorItem.java`

Jika masih ada `private static BlockPos`:
1. Buat branch `fix/issue-012-area-selector-mp`
2. Implementasikan NBT storage (lihat AGENTS.md)
3. Tulis regression test
4. Buat PR

---

## 📝 TEMPLATE PROMPT UNTUK JULES

Gunakan prompt ini ketika memberikan tugas harian ke Jules secara manual:

### Prompt Analisis Harian:
```
Lakukan analisis harian proyek Color Variants Mod:

1. Scan semua file Java di common/ dan fabric/ untuk:
   - TODO, FIXME, HACK comments
   - static mutable fields di item classes
   - HashMap yang tidak thread-safe di core managers
   - Public methods tanpa Javadoc

2. Cek apakah MixinBlockModelRenderer.java sudah diimplementasikan.
   Jika belum, implementasikan vertex coloring sesuai AGENTS.md.

3. Cek apakah AreaSelectorItem.java masih punya static state.
   Jika ya, fix ke NBT storage.

4. Jalankan tests dan report coverage.

5. Buat daily report sebagai GitHub issue.

Buat PR terpisah untuk setiap perbaikan yang ditemukan.
Ikuti branch naming: fix/auto-[deskripsi]-YYYYMMDD
```

### Prompt Fix Rendering:
```
Implementasikan rendering system untuk Color Variants Mod.

Context: Mod ini menyimpan warna block ke ColorTransformManager, 
tapi warna tidak tampil di game karena rendering belum diimplementasikan.

Tugas:
1. Buat MixinBlockModelRenderer.java di common/src/main/java/com/colorvariants/mixin/
2. Inject ke ModelBlockRenderer.tesselateBlock method
3. Untuk block yang punya ColorTransform, render dengan vertex coloring
4. Jangan mutate array vertex asli (clone dulu)
5. Handle: semua Direction + null direction (general quads)
6. Handle tint indices untuk block seperti grass
7. Daftarkan di colorvariants.mixins.json

Setelah rendering berfungsi:
8. Hapus TextureGenerator.java (approach yang salah)
9. Update ColoredBlockRenderer.java untuk menggunakan approach baru
10. Tulis unit test

Branch: fix/issue-001-rendering-system
```

### Prompt Security Fix:
```
Tambahkan server-side validation untuk semua packet handlers di Color Variants Mod.

File yang perlu diupdate:
- common/src/main/java/com/colorvariants/network/ColorUpdatePacket.java
- common/src/main/java/com/colorvariants/network/AreaColorUpdatePacket.java

Validasi yang harus ditambahkan:
1. Distance check: player.distanceToSqr(packet.pos) <= 64 (8 blok)
2. Rate limiting: max 20 packets/second per player (gunakan ConcurrentHashMap dengan timestamp)
3. Area size limit: max 262144 blocks (64^3) untuk AreaColorUpdatePacket
4. Null check untuk semua fields

Jika validasi gagal: log warning, return tanpa mengeksekusi, JANGAN throw exception.

Branch: fix/issue-security-validation
```

---

## ⚙️ KONFIGURASI JULES CLI (Optional)

Jika menggunakan Jules CLI tools:

```bash
# Install Jules CLI
npm install -g @google/jules-cli

# Daily analysis task
jules remote new \
  --repo username/colorvariants \
  --session "$(cat .github/JULES_TASKS/DAILY_PROMPT.md)" \
  --branch "auto/daily-$(date +%Y%m%d)"

# Monitor task
jules remote list

# Apply patches locally
jules remote apply --session <session-id>
```

---

## 🚫 BATASAN HARIAN (Jules TIDAK boleh melakukan)

Tanpa explicit approval dari maintainer, Jules dilarang:

- ❌ Merge langsung ke `main` atau `develop`
- ❌ Mengubah Minecraft version target
- ❌ Menghapus file yang masih digunakan (kecuali `TextureGenerator.java` — sudah disetujui)
- ❌ Mengubah public API yang breaking
- ❌ Menambah dependency baru tanpa mention di PR description
- ❌ Mengubah build system (buildSrc/) tanpa explicit instruction
- ❌ Mengurangi test coverage
- ❌ Force push ke branch manapun

---

*File ini digunakan oleh Jules sebagai instruksi operasional harian.*  
*Update file ini jika ada perubahan prioritas atau prosedur.*
