# StorIQ — Android Storage Intelligence & Device Assistant

> **Understand your device. Take back your space.**

StorIQ is a privacy-first, intelligent storage management assistant for Android that helps users understand, review, and safely reclaim device storage.

## Features

- **Storage Dashboard** — Real-time storage overview with breakdown by category
- **Storage Analyzer** — Detailed analysis of media, documents, downloads, archives, and APKs
- **Large File Detection** — Find and review files consuming the most space
- **Duplicate Detection** — Identify exact duplicate files using content hashes
- **Cleanup Recommendations** — Smart suggestions for reclaiming space
- **Swipe Clean** — Tinder-style photo/video review for rapid cleanup decisions
- **App Intelligence** — Storage usage and last-used tracking for installed apps
- **Unused App Detection** — Find apps not used in 30/60/90/180 days
- **Storage History** — Track storage trends over time
- **Privacy Dashboard** — Permission insights and controls
- **Device Information** — Hardware and system details

## Architecture

- **Clean Architecture + MVVM**
- **Kotlin** with **Coroutines** and **Flow**
- **Jetpack Compose** with **Material 3**
- **Room** for local persistence
- **DataStore** for preferences
- **WorkManager** for background scanning
- **Navigation Compose** for navigation

## Project Structure

```
app/
├── core/
│   ├── common/
│   ├── database/
│   ├── model/
│   ├── permissions/
│   ├── storage/
│   ├── media/
│   ├── ui/
│   └── util/
├── feature/
│   ├── onboarding/
│   ├── dashboard/
│   ├── analyzer/
│   ├── cleanup/
│   ├── duplicates/
│   ├── largefiles/
│   ├── swipeclean/
│   ├── apps/
│   ├── downloads/
│   ├── history/
│   ├── explorer/
│   ├── privacy/
│   ├── device/
│   └── settings/
└── MainActivity.kt
```

## Development Phases

### Phase 1 — Foundation (Current)
- [x] Project configuration (Gradle Kotlin DSL)
- [x] Compose Material 3 theme
- [x] Navigation with 5 main tabs
- [x] Clean Architecture + MVVM setup
- [x] Room database with entities
- [x] Permission abstraction layer
- [x] Storage repository interface
- [x] Onboarding flow (3 screens)
- [x] Shell screens for all 5 tabs

### Phase 2 — Storage
- [ ] Storage statistics
- [ ] MediaStore scanner
- [ ] File metadata extraction
- [ ] Categorization
- [ ] Large file detection
- [ ] Dashboard with real data

### Phase 3 — Cleanup
- [ ] Duplicate detection
- [ ] Recommendation engine
- [ ] Review UI
- [ ] Deletion abstraction
- [ ] Confirmation flows
- [ ] Post-delete refresh

### Phase 4 — Swipe Clean
- [ ] Media repository
- [ ] Fullscreen gallery
- [ ] Card stack with gestures
- [ ] Keep/Delete decisions
- [ ] Undo functionality
- [ ] Session state management
- [ ] Review grid
- [ ] Deletion confirmation

### Phase 5 — App Intelligence
- [ ] Installed apps listing
- [ ] App storage breakdown
- [ ] Usage information
- [ ] Unused app recommendations
- [ ] App detail screen

### Phase 6 — History
- [ ] Storage snapshots
- [ ] Trend graphs
- [ ] Growth calculation
- [ ] Recommendations

### Phase 7 — Polish
- [ ] Animations
- [ ] Accessibility
- [ ] Error handling
- [ ] Performance optimization
- [ ] Battery impact
- [ ] Privacy audit
- [ ] Device compatibility
- [ ] Release build

## Getting Started

### Prerequisites
- Android Studio Hedgehog or later
- JDK 17
- Android SDK 34

### Building
```bash
./gradlew assembleDebug
```

### Running Tests
```bash
./gradlew test
./gradlew connectedAndroidTest
```

## Privacy & Security

- All analysis happens on-device
- No personal data leaves the device without explicit consent
- Uses only Android-supported storage APIs
- Respects scoped storage restrictions
- Never silently deletes files
- Requires explicit confirmation for all destructive actions

## Tech Stack

- **Language**: Kotlin 1.9.22
- **UI**: Jetpack Compose, Material 3
- **Architecture**: Clean Architecture + MVVM
- **Database**: Room 2.6.1
- **Preferences**: DataStore 1.1.1
- **Background Work**: WorkManager 2.9.0
- **Image Loading**: Coil 2.6.0
- **Navigation**: Navigation Compose 2.7.7
- **Coroutines**: 1.7.3
- **Serialization**: Kotlinx Serialization 1.6.3

## License

Proprietary — All rights reserved.