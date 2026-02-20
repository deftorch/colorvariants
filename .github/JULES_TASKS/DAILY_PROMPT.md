Lakukan analisis harian proyek Color Variants Mod dan perbaiki masalah yang ditemukan.

Baca AGENTS.md terlebih dahulu untuk mendapatkan context lengkap tentang proyek.

=== TUGAS HARI INI ===

STEP 1 — CEK BLOCKER UTAMA:
Cek apakah file ini ada: common/src/main/java/com/colorvariants/mixin/MixinBlockModelRenderer.java
- Jika TIDAK ADA: Ini adalah P0 Critical. Implementasikan vertex coloring mixin sesuai AGENTS.md bagian "Rendering Implementation". Buat branch: fix/issue-001-rendering-system
- Jika ADA: Verifikasi bahwa implementasinya benar (inject ke tesselateBlock, handle semua Direction)

STEP 2 — CEK AREA SELECTOR BUG:
Jalankan: grep -n "private static BlockPos\|private static.*firstPos\|private static.*secondPos" common/src/main/java/com/colorvariants/item/AreaSelectorItem.java
- Jika ada hasil: Perbaiki ke NBT storage. Buat branch: fix/issue-012-area-selector-multiplayer
- Tulis regression test yang membuktikan 2 ItemStack menyimpan posisi berbeda

STEP 3 — SCAN TODO/FIXME:
Jalankan: grep -rn "TODO\|FIXME\|HACK" common/src/main/java/ fabric/src/main/java/
Untuk setiap TODO yang bisa diselesaikan dalam 1 jam: implementasikan dan buat PR.
Untuk TODO yang besar: buat GitHub issue dengan label "jules-task".

STEP 4 — CEK STATIC STATE:
Jalankan: grep -rn "private static" common/src/main/java/com/colorvariants/item/
Jika ada (selain final constants): Ini adalah bug. Fix segera ke NBT/instance variable.

STEP 5 — CEK THREAD SAFETY:
Jalankan: grep -rn "new HashMap\|new ArrayList" common/src/main/java/com/colorvariants/core/
Jika ada non-thread-safe collections di core managers: ganti dengan ConcurrentHashMap/CopyOnWriteArrayList.

STEP 6 — BUILD CHECK:
Jalankan: ./gradlew :common:compileJava
Catat semua warning dan error.
Fix semua error sebelum lanjut.

STEP 7 — TEST CHECK:
Jalankan: ./gradlew :common:test
Jika ada test yang failing: fix dulu.
Jika coverage < 70%: tulis test baru untuk menutup gap.

STEP 8 — LAPORAN HARIAN:
Buat GitHub issue dengan title "Daily Analysis — [tanggal hari ini]" dan label "daily-analysis".
Isi: status P0 issues, coverage saat ini, PRs yang dibuat hari ini, rencana besok.

=== ATURAN ===
- Buat PR TERPISAH untuk setiap fix (jangan campur beberapa fix dalam 1 PR)
- Jangan merge sendiri ke main/develop
- Selalu tulis test untuk setiap bug fix
- Commit message format: fix(scope): description (#issue-number)
