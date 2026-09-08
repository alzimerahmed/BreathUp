# BreathUp — Minimalist, data-oriented quit-smoking tracker for Android

<div align="center">

<img src="fastlane/metadata/android/en-US/images/featureGraphic.png" alt="BreathUp feature graphic" width="100%">

[![Platform](https://img.shields.io/badge/Android-8.0%2B_(API_26%2B)-3DDC84?logo=android&logoColor=white&labelColor=1a1a1a&style=flat-square)](https://developer.android.com)
[![Language](https://img.shields.io/badge/Kotlin-2.x-7F52FF?logo=kotlin&logoColor=white&labelColor=1a1a1a&style=flat-square)](https://kotlinlang.org)
[![UI](https://img.shields.io/badge/Jetpack%20Compose-Material%203-6750A4?logo=jetpackcompose&logoColor=white&labelColor=1a1a1a&style=flat-square)](https://developer.android.com/jetpack/compose)
[![License](https://img.shields.io/badge/License-GPLv3-blue?logo=gnu&labelColor=1a1a1a&style=flat-square)](LICENSE)
[![Release](https://img.shields.io/badge/Version-1.6.0-625B71?labelColor=1a1a1a&style=flat-square)](https://github.com/alzimerahmed84/BreathUp/releases)

*Track every cigarette, resist cravings, and taper down at your own pace — 100% local, no ads, no accounts.*

[Features](#features) • [Tech Stack](#tech-stack) • [Building](#quick-start--building)

</div>

---

## Features

- **One-tap logging** — log a cigarette instantly; a live timer shows time since the last one.
- **Home screen widgets** — 1x1 quick-add and 3x1 timer/counter widgets (Glance), with Android 12+ dynamic pinning.
- **Mindful craving pause** — guided breathing to resist cravings; resisted attempts are tracked and counted.
- **Tapering reduction plan** — gradual daily-limit reductions with scheduled check-ins, snoozing, and keep-current options.
- **Historical baseline generator** — compute your past smoking baseline and project money and health savings.
- **Analytics** — daily/weekly charts, trigger distributions (stress, alcohol, coffee, custom triggers), editable logs.
- **WHO health & financial stats** — counts, averages, streaks, savings, and WHO health-recovery milestones.
- **Achievements** — milestone and secret badges with local notifications.
- **Data portability** — local JSON backup and restore.
- **Theming** — light/dark/system, AMOLED dark, Material You dynamic color, 9 color presets, font presets with variable-font tuning, dynamic app icons.

## Screenshots

<p align="center">
  <img src="fastlane/metadata/android/en-US/images/phoneScreenshots/1.png" width="32%" alt="Home dashboard"><img src="fastlane/metadata/android/en-US/images/phoneScreenshots/2.png" width="32%" alt="Analytics charts"><img src="fastlane/metadata/android/en-US/images/phoneScreenshots/3.png" width="32%" alt="Trigger statistics">
  <img src="fastlane/metadata/android/en-US/images/phoneScreenshots/4.png" width="32%" alt="Settings"><img src="fastlane/metadata/android/en-US/images/phoneScreenshots/5.png" width="32%" alt="Tapering plan"><img src="fastlane/metadata/android/en-US/images/phoneScreenshots/6.png" width="32%" alt="Achievements">
</p>

## Tech Stack

| Layer | Technology |
|---|---|
| Language | Kotlin (Coroutines, Flow) |
| UI | Jetpack Compose, Material 3 Expressive, Navigation Compose |
| Widgets | Jetpack Glance |
| Persistence | Room (schema-exported, KSP), DataStore Preferences, Gson (backups) |
| DI | Koin |
| Platform | minSdk 26, target/compileSdk 37, Java 17 |

## Project Structure

```
app/src/main/java/com/smokingtracker/
├── SmokingTrackerApp.kt      # Application: Koin, notification channels
├── MainActivity.kt           # Theme hosting, edge-to-edge, splash
├── MainViewModel.kt          # App-wide state (theme, entries, settings)
├── HomeViewModel.kt          # Logging, triggers, tapering check-ins
├── AchievementsManager.kt    # Achievement definitions + coordinator
├── StatisticsManager.kt      # Stats computation
├── BackupManager.kt          # JSON backup/restore
├── data/
│   ├── DataStoreManager.kt   # Preferences
│   ├── local/                # Room: database, DAO, entity, migrations
│   └── repository/           # SmokingRepository
├── di/AppModule.kt           # Koin wiring
├── notification/             # Ongoing status notification
├── ui/                       # Compose screens + theme tokens
└── widget/                   # Glance widgets
```

## Quick Start / Building

```bash
git clone <your-fork-url>
cd BreathUp
./gradlew assembleDebug
```

The debug APK lands in `app/build/outputs/apk/debug/`. Requires JDK 17 and an Android SDK with API 37.

## Usage

Open the app, complete the one-screen registration (pack price, pack size, daily baseline), then tap the big button to log a cigarette or hold to log a resisted craving. Add the 3x1 widget for a live smoke-free timer on your home screen. Back up your data anytime from Settings → Backup.

## Localization

English, Russian, German, Spanish, French, Italian, Portuguese, Turkish, Ukrainian.

## Contributing

Personal project — issues and suggestions are welcome via the tracker, but pull requests are not being accepted for now.

## Roadmap

- [ ] Craving intensity logging and outcome notes
- [ ] Compassionate relapse/slips flow with previous-best streaks
- [ ] Daily mission nudges
- [ ] Local trigger-correlation insights
- [ ] Nicotine/CO half-life health timeline
- [ ] Milestone share cards

## License

BreathUp — Minimalist, data-oriented smoking tracker for Android.
Copyright (C) 2026 Alzimer Ahmed

This program is free software: you can redistribute it and/or modify it under the terms of the GNU General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.

This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for more details.

You should have received a copy of the GNU General Public License along with this program. If not, see <https://www.gnu.org/licenses/>
