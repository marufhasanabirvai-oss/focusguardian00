# Focus Guardian - Native Android Self-Control & Wellbeing Blocker

Built with **Kotlin**, **Jetpack Compose (Material 3)**, **Room Database**, **DataStore Preferences**, and clean architecture.

## Architecture Overview
- **VpnService**: Intercepts DNS port 53 locally via 127.0.0.1 to sinkhole configured domains and adult categories.
- **AccessibilityService**: Recursively monitors visible screen nodes for blocked words using Unicode-aware regex boundaries.
- **Visual Pattern Matcher**: MediaProjection service computes 64-bit dHash and compares Hamming distance on-device.
- **Device Administration**: Prevents casual uninstall. Includes Device Owner ADB provision for permanent enrollment.
- **Dual Time Selection System**:
  - *Basic Mode (Minute-based)*: Quick focus sessions from 1 to 100 minutes (5m, 10m, 25m Pomodoro, 45m, 60m, 100m) with clean expiration.
  - *Advanced Mode (Day-based Commitment Lock)*: Strict irreversible 1 to 30 days lock preventing any switch disable, alteration, or bypass.
- **Penalty Engine**: Dispatches `ACTION_MAIN` (`CATEGORY_HOME`) and displays non-dismissible lockout countdown screen.

## Cloud APK Build (No Android Studio required!)
This project includes `.github/workflows/build-apk.yml`. You can push or upload this project to a GitHub repository in your browser, and GitHub Actions will automatically compile `app-debug.apk` for free in 3 minutes!

## Permissions Required
1. `BIND_ACCESSIBILITY_SERVICE`: Inspects on-screen text in real-time.
2. `BIND_VPN_SERVICE`: Local DNS sinkhole for website & adult blocking.
3. `BIND_DEVICE_ADMIN`: Self-control uninstall prevention.
4. `POST_NOTIFICATIONS`: Android 13+ penalty warnings.
5. `RECEIVE_BOOT_COMPLETED`: Re-engages active protection locks on restart.
