# Voxel Sandbox Java (starter)

Starter project LibGDX: satu codebase Java untuk Android dan PC.

## Kontrol
- PC: WASD / Arrow (atur ulang lewat menu), Space untuk lompat, K untuk menu keybind.
- Android: joystick kiri bawah untuk bergerak, tombol JUMP kanan bawah.
- Keybind: tekan K, tap nama aksi, lalu tekan tombol keyboard baru. ESC untuk menutup menu.

## Buka di PC
1. Install JDK 17 dan Android Studio atau IntelliJ IDEA.
2. Buka folder project ini sebagai Gradle project.
3. Jalankan task `lwjgl3 > application > run` (atau `./gradlew lwjgl3:run` jika wrapper sudah dibuat).

## Build Android
1. Buka project di Android Studio.
2. Pastikan Android SDK Platform 35 dan JDK 17 tersedia.
3. Jalankan task `android > Tasks > build > assembleDebug`.
4. APK ada di `android/build/outputs/apk/debug/`.

## Catatan
Ini adalah fondasi/prototype: terrain datar, player box, gerak, joystick, tombol lompat, dan keybind PC.
Belum ada mining/placing block, inventory, world save, collision antarblok, atau multiplayer.
Untuk produksi, tambahkan chunk meshing, block atlas texture, collision, dan penyimpanan world.
