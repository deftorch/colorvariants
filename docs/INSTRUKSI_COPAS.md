# 📋 INSTRUKSI SIAP COPAS — Color Variants Mod
## Kumpulan Prompt untuk Google Jules

> **Cara Pakai:** Copy teks dalam kotak kode, paste ke Jules di jules.google  
> Pilih instruksi sesuai tugas yang ingin diselesaikan

---

## 🔴 INSTRUKSI #1 — Analisis & Scan Proyek (MULAI DARI SINI)

```
Lakukan analisis komprehensif proyek Color Variants Minecraft Mod.

Repository ini adalah mod Minecraft multiloader (Fabric + Forge) untuk merecolor block.
Status: 40% fungsional. Baca AGENTS.md terlebih dahulu untuk context lengkap.

Lakukan hal-hal berikut:

1. SCAN KODE: Temukan semua masalah di common/src/main/java/:
   - Static mutable fields di folder item/ (sangat berbahaya untuk multiplayer)
   - HashMap non-thread-safe di folder core/
   - Missing null checks
   - TODO/FIXME comments
   - Magic numbers (angka literal tanpa nama konstanta)

2. ANALISIS ARSITEKTUR: Verifikasi:
   - Apakah ada class yang import Fabric/Forge API langsung dari common/ (tidak boleh)
   - Apakah semua packet handler di network/ punya server-side validation
   - Apakah MixinBlockModelRenderer.java sudah ada (rendering implementation)

3. BUAT LAPORAN: Tulis laporan markdown dengan:
   - Daftar masalah yang ditemukan (sorted by severity)
   - Status setiap P0 issue dari AGENTS.md
   - Rekomendasi tindakan apa yang harus dikerjakan pertama

4. MULAI FIX: Segera fix masalah kecil yang ditemukan (magic numbers, TODO, null checks).
   Untuk masalah besar (rendering, security), buat branch terpisah.

Baca AGENTS.md sebelum memulai.
```

---

## 🔴 INSTRUKSI #2 — Fix Area Selector Multiplayer Bug (4 jam)

```
Fix critical multiplayer bug di AreaSelectorItem.java.

BUG: Class ini menggunakan "private static BlockPos firstPos" dan "private static BlockPos secondPos"
yang menyebabkan semua player di server BERBAGI posisi yang sama.
Ini menyebabkan: Player A select area, Player B select area lain → selection Player A tertimpa.

FIX YANG DIPERLUKAN:
Ganti static fields dengan NBT storage di ItemStack:

1. Hapus: private static BlockPos firstPos;
   Hapus: private static BlockPos secondPos;

2. Tambahkan method:
   - getFirstPos(ItemStack stack) → Optional<BlockPos>
   - setFirstPos(ItemStack stack, BlockPos pos) → void
   - getSecondPos(ItemStack stack) → Optional<BlockPos>  
   - setSecondPos(ItemStack stack, BlockPos pos) → void
   
   Gunakan tag.putIntArray("FirstPos", new int[]{x, y, z}) untuk menyimpan
   Gunakan tag.getIntArray("FirstPos") untuk membaca

3. Update semua bagian code yang menggunakan static fields

4. Tulis regression test:
   - Test bahwa 2 ItemStack berbeda menyimpan posisi berbeda
   - Test bahwa posisi tersimpan dan bisa dibaca kembali
   - Test bahwa ItemStack kosong mengembalikan Optional.empty()

Branch: fix/issue-012-area-selector-multiplayer
Label: bug, critical, multiplayer
```

---

## 🔴 INSTRUKSI #3 — Implementasi Rendering System (2-3 minggu)

```
Implementasikan rendering system untuk Color Variants Mod.

MASALAH SAAT INI: Warna block TIDAK tampil di game. Data warna tersimpan tapi tidak pernah
di-render. TextureGenerator.java ada tapi tidak terhubung ke rendering pipeline, dan
pendekatannya salah (texture generation vs vertex coloring).

SOLUSI: Implementasikan vertex coloring via Mixin ke ModelBlockRenderer.

LANGKAH:

1. Buat file: common/src/main/java/com/colorvariants/mixin/MixinBlockModelRenderer.java
   - @Mixin(ModelBlockRenderer.class)
   - @Inject ke method "tesselateBlock" di @At("HEAD") dengan cancellable = true
   - Cek ColorTransformManager.get(level).getTransform(pos)
   - Jika ada transform, cancel rendering asli dan render ulang dengan vertex coloring
   - WAJIB: Handle semua 6 Direction + null direction (quads tanpa arah)
   - WAJIB: Handle tint indices (untuk block seperti grass yang punya warna overlay)
   - WAJIB: Clone array vertex sebelum dimodifikasi, JANGAN modifikasi aslinya

2. Daftarkan mixin di: common/src/main/resources/colorvariants.mixins.json
   Tambahkan: "mixin.MixinBlockModelRenderer" ke array "client"

3. Hapus TextureGenerator.java (pendekatan yang salah, tidak dipakai)

4. Update ColoredBlockRenderer.java agar tidak lagi referensi TextureGenerator

5. Tulis test:
   - Test bahwa ColorTransform dengan warna merah menghasilkan vertex color yang benar
   - Test bahwa block tanpa transform tidak diubah warnanya
   - Test untuk tint index handling

Catatan penting:
- Jangan menggunakan pendekatan texture generation (tidak kompatibel dengan multi-face blocks)
- Vertex coloring adalah pendekatan yang benar untuk Minecraft 1.20.x
- Gunakan int[] vertices = quad.getVertices().clone() sebelum modifikasi

Branch: fix/issue-001-rendering-system
Label: critical, rendering
```

---

## 🔴 INSTRUKSI #4 — Security: Server-Side Validation

```
Tambahkan validasi server-side untuk semua packet handlers di Color Variants Mod.

MASALAH: Saat ini server mempercayai semua data dari client 100% tanpa validasi.
Ini bisa dieksploitasi untuk: mewarnai block jauh (cheat), griefing tanpa limit,
crash server dengan kirim request sangat besar.

FILE YANG PERLU DIUPDATE:
- common/src/main/java/com/colorvariants/network/ColorUpdatePacket.java
- common/src/main/java/com/colorvariants/network/AreaColorUpdatePacket.java

VALIDASI YANG HARUS DITAMBAHKAN di method handle():

1. Distance Check:
   double distanceSq = player.distanceToSqr(packet.pos.getX() + 0.5, packet.pos.getY() + 0.5, packet.pos.getZ() + 0.5);
   if (distanceSq > 64.0) { // 8 blocks
       Constants.LOG.warn("Player {} tried to color block out of range: {}", player.getName().getString(), distanceSq);
       return;
   }

2. Rate Limiting:
   Buat class RateLimiter di util/ dengan ConcurrentHashMap<UUID, Long> lastAction
   Max 20 packets per second per player
   
3. Area Size Limit (untuk AreaColorUpdatePacket):
   Hitung volume: (maxX - minX) * (maxY - minY) * (maxZ - minZ)
   Jika > 262144 (64 kubik), tolak dan log warning

4. Null Validation:
   Validasi packet.pos != null, packet.transform != null
   
Setelah validasi gagal: LOG warning, RETURN (jangan throw exception).

Tulis test:
- Test bahwa packet dari jarak >8 blok ditolak
- Test bahwa rate limit bekerja (21 packet dalam 1 detik, hanya 20 yang diproses)
- Test bahwa area >64^3 ditolak

Branch: fix/issue-security-server-validation
Label: security, critical
```

---

## 🟡 INSTRUKSI #5 — Thread Safety Fixes

```
Perbaiki masalah thread safety di core managers Color Variants Mod.

MASALAH: ColorTransformManager menggunakan HashMap biasa yang tidak thread-safe.
Di Minecraft, server tick thread dan async save thread bisa akses bersamaan → ConcurrentModificationException.

FILE: common/src/main/java/com/colorvariants/core/ColorTransformManager.java

PERUBAHAN:
1. Ganti: private final Map<BlockPos, ColorTransform> transforms = new HashMap<>();
   Dengan: private final Map<BlockPos, ColorTransform> transforms = new ConcurrentHashMap<>();

2. Cek apakah ada iterasi yang bisa bermasalah:
   - for (BlockPos p : transforms.keySet()) → OK dengan ConcurrentHashMap
   - transforms.entrySet().iterator() → perlu synchronized block jika hapus selama iterasi

FILE: common/src/main/java/com/colorvariants/util/TextureCache.java

PERUBAHAN:
1. Tambahkan maksimum cache size 256 entries menggunakan LinkedHashMap dengan access-order:
   private static final int MAX_SIZE = 256;
   private final LinkedHashMap<Key, Value> cache = new LinkedHashMap<>(MAX_SIZE, 0.75f, true) {
       @Override
       protected boolean removeEldestEntry(Map.Entry<Key, Value> eldest) {
           return size() > MAX_SIZE;
       }
   };
2. Wrap dengan Collections.synchronizedMap() karena LinkedHashMap tidak thread-safe

FILE: common/src/main/java/com/colorvariants/core/UndoRedoManager.java
PERUBAHAN:
1. Tambahkan limit ke undo/redo stack: private static final int MAX_STACK_SIZE = 50;
2. Ketika push ke stack dan size > MAX_STACK_SIZE, hapus yang paling lama

Tulis test untuk:
- Concurrent access ke ColorTransformManager dari 2 thread
- Cache tidak melebihi MAX_SIZE
- Undo stack tidak melebihi MAX_STACK_SIZE

Branch: refactor/thread-safety-fixes
Label: bug, performance
```

---

## 🟡 INSTRUKSI #6 — Sodium Compatibility (FRAPI)

```
Implementasikan kompatibilitas dengan Sodium/Embeddium untuk Color Variants Mod.

KONTEKS: 90% pengguna Fabric menggunakan Sodium. Sodium mengganti rendering engine Minecraft,
sehingga mixin ke ModelBlockRenderer tidak bekerja. Kita perlu menggunakan Fabric Rendering API (FRAPI).

SOLUSI: Implementasikan FabricBakedModel approach atau gunakan Indium sebagai intermediary.

OPSI A (Lebih sederhana — Require Indium):
1. Update fabric/src/main/resources/fabric.mod.json:
   Tambahkan ke "depends": { "indium": "*" }
   Tambahkan ke "suggests": { "sodium": "*" }
2. Tambahkan komentar di README bahwa Indium required jika menggunakan Sodium

OPSI B (Lebih kompleks — Native FRAPI support):
1. Tambahkan Fabric Rendering API ke fabric/build.gradle dependencies
2. Buat class FabricColoredBlockRenderer yang implement FabricBakedModel
3. Wrap semua BakedModel yang terpengaruh dengan ColoredModelWrapper
4. Daftarkan via ModelLoadingPlugin

Implementasikan OPSI A dulu (lebih cepat), lalu buat issue untuk OPSI B sebagai follow-up.

Setelah implementasi:
1. Update README.md dengan info kompatibilitas Sodium
2. Update fabric.mod.json
3. Tulis komentar di AGENTS.md bahwa Sodium compat sudah via Indium

Branch: feat/sodium-compatibility
Label: compatibility, major
```

---

## 🟢 INSTRUKSI #7 — Tambah Test Coverage

```
Tambahkan unit tests untuk Color Variants Mod untuk mencapai minimum 70% coverage.

SETUP: Buat folder test jika belum ada:
common/src/test/java/com/colorvariants/

Buat test class berikut (PRIORITAS URUTAN INI):

1. ColorTransformTest.java — test semua operasi color:
   - test bahwa NONE transform tidak mengubah warna
   - test bahwa RGB values tersimpan dan dibaca dengan benar
   - test operasi multiply/overlay colors
   - test serialisasi/deserialisasi ke NBT

2. AreaSelectorItemTest.java — REGRESSION TEST untuk multiplayer bug:
   - test bahwa 2 ItemStack menyimpan posisi BERBEDA (ini adalah regression test utama)
   - test setFirstPos/getFirstPos roundtrip
   - test ItemStack kosong mengembalikan Optional.empty()

3. UndoRedoManagerTest.java:
   - test undo basic
   - test redo setelah undo
   - test stack tidak melebihi MAX_SIZE (50)
   - test clear redo stack ketika action baru dilakukan

4. ColorSchemeManagerTest.java:
   - test save scheme
   - test load scheme
   - test preset names tersedia

5. PacketValidationTest.java:
   - test validasi jarak (mock player dan packet)
   - test rate limiting
   - test null packet fields ditolak

Gunakan JUnit 5 (@Test, @BeforeEach, @DisplayName).
Jangan gunakan Minecraft game objects yang susah di-mock.
Gunakan simple Java objects untuk model data.

Target: minimum 70% line coverage di common module.

Branch: test/add-unit-test-coverage
Label: testing
```

---

## 🟢 INSTRUKSI #8 — Setup Build Quality Tools

```
Setup code quality tools untuk Color Variants Mod.

TOOLS YANG PERLU DIKONFIGURASI:

1. JaCoCo (Test Coverage):
   Di common/build.gradle, tambahkan:
   plugins { id 'jacoco' }
   jacoco { toolVersion = "0.8.10" }
   jacocoTestReport {
     dependsOn test
     reports { xml.required = true; html.required = true }
   }
   jacocoTestCoverageVerification {
     violationRules {
       rule {
         limit { minimum = 0.70 } // 70% minimum
       }
     }
   }

2. Checkstyle:
   Buat config/checkstyle/checkstyle.xml dengan rules:
   - JavadocMethod untuk public methods
   - MagicNumber check
   - ConstantName (UPPER_SNAKE_CASE)
   - LocalVariableName (camelCase)
   Di common/build.gradle: apply plugin 'checkstyle'

3. Dependency Updates Plugin:
   Di buildSrc/build.gradle atau root build.gradle:
   plugins { id 'com.github.ben-manes.versions' version '0.47.0' }
   
4. SpotBugs (static analysis):
   plugins { id 'com.github.spotbugs' version '5.1.3' }
   spotbugsMain { effort = "max"; reportLevel = "medium" }

Setelah setup:
- Jalankan ./gradlew build untuk verifikasi semua tool berjalan
- Fix semua checkstyle violations yang ditemukan
- Pastikan CI di .github/workflows/ci.yml sudah include semua checks ini

Branch: build/setup-quality-tools
Label: build, ci
```

---

## 📅 INSTRUKSI HARIAN OTOMATIS (Untuk Scheduled Task)

```
=== DAILY MAINTENANCE TASK — Color Variants Mod ===
Tanggal: [Jules akan mengisi otomatis]

Lakukan semua tugas berikut secara berurutan:

TUGAS 1 - PRIORITY CHECK:
Cek apakah file ini ada dan berfungsi:
- common/src/main/java/com/colorvariants/mixin/MixinBlockModelRenderer.java
Jika TIDAK ADA: Ini adalah blocker terbesar. Mulai implementasi sesuai AGENTS.md bagian Rendering.

TUGAS 2 - STATIC STATE CHECK:
Jalankan: grep -rn "private static" common/src/main/java/com/colorvariants/item/
Jika ada hasil → Fix SEGERA, buat PR.

TUGAS 3 - TODO SCAN:
Jalankan: grep -rn "TODO\|FIXME\|HACK" common/src/main/java/ fabric/src/main/java/
Implementasikan setiap TODO yang bisa diselesaikan dalam 1 jam.
Untuk TODO yang kompleks, buat GitHub issue.

TUGAS 4 - BUILD CHECK:
Jalankan: ./gradlew :common:compileJava
Jika ada error → Fix dulu sebelum lanjut.
Catat semua warnings.

TUGAS 5 - COVERAGE CHECK:
Jalankan: ./gradlew :common:test :common:jacocoTestReport
Jika coverage < 70% → Tulis test baru untuk menutup gap.

TUGAS 6 - DAILY REPORT:
Buat atau update GitHub issue "Daily Analysis — [tanggal]" dengan:
- Status P0 issues (berapa yang sudah fix)
- Coverage saat ini
- PRs yang dibuat hari ini
- Apa yang akan dikerjakan besok

Selalu buat PR terpisah untuk setiap fix.
Jangan merge ke main/develop tanpa review.
```

---

## 🆘 INSTRUKSI EMERGENCY — Jika Build Broken

```
BUILD EMERGENCY — Color Variants Mod

Build sedang broken. Perbaiki ASAP.

LANGKAH:

1. Cek error di console: ./gradlew :common:compileJava 2>&1 | head -50

2. Kategorikan error:
   - Import error → cek apakah dependency ada di build.gradle
   - Mixin error → cek apakah class yang di-mixin masih ada di Minecraft 1.20.1
   - API change → cek apakah ada method yang berubah signature-nya

3. Fix error SATU PER SATU, jangan semua sekaligus

4. Setelah fix, verifikasi:
   ./gradlew :common:compileJava :fabric:compileJava
   ./gradlew :common:test

5. Buat PR dengan label "hotfix" + "urgent"

6. Update GitHub issue "Build Status" dengan apa yang diperbaiki

JANGAN:
- Suppress error dengan @SuppressWarnings tanpa penjelasan
- Comment out code yang error tanpa penjelasan
- Delete test yang failing tanpa menggantinya
```

---

*Simpan file ini. Semua instruksi di atas siap dicopy-paste langsung ke Jules.*
