# Contributing to StorIQ

Thank you for your interest in contributing to StorIQ! This document provides guidelines for contributing to the project.

## 🤝 Code of Conduct

By participating in this project, you agree to abide by our [Code of Conduct](CODE_OF_CONDUCT.md). Please read it before contributing.

## 🚀 Getting Started

### Prerequisites
- Android Studio Hedgehog (2023.1.1) or later
- JDK 17
- Android SDK 34
- Git

### Development Setup

1. **Fork and clone the repository:**
```bash
git clone https://github.com/YOUR_USERNAME/StorIQ.git
cd StorIQ
```

2. **Open in Android Studio:**
   - File → Open → Select the `StorIQ` directory
   - Wait for Gradle sync to complete

3. **Run the app:**
   - Connect an Android device or start an emulator (API 26+)
   - Click Run ▶️ or press `Shift+F10`

## 🏗️ Project Structure

```
StorIQ/
├── app/
│   ├── core/                    # Shared architecture
│   │   ├── database/            # Room (entities, DAOs, database)
│   │   ├── media/               # Media/File/App scanners
│   │   ├── model/               # Data models
│   │   ├── permissions/         # Permission management
│   │   ├── storage/             # Repository interface + impl
│   │   ├── ui/                  # Theme, animations, accessibility
│   │   └── util/                # Error handling, performance, battery, privacy
│   ├── feature/                 # Feature modules
│   │   ├── analyzer/            # Storage analysis
│   │   ├── cleanup/             # Cleanup recommendations
│   │   ├── dashboard/           # Home dashboard
│   │   ├── duplicates/          # Duplicate detection
│   │   ├── largefiles/          # Large files with grouping
│   │   ├── swipeclean/          # Tinder-style media review
│   │   ├── apps/                # App intelligence
│   │   ├── history/             # Storage history charts
│   │   ├── onboarding/          # Onboarding flow
│   │   └── more/                # Secondary features
│   └── MainActivity.kt          # Navigation host
├── .github/
│   ├── workflows/               # CI/CD pipelines
│   ├── dependabot.yml           # Automated dependency updates
│   ├── CODEOWNERS               # Code ownership
│   └── PULL_REQUEST_TEMPLATE/   # PR templates
├── detekt.yml                   # Static analysis config
├── .editorconfig                # Editor config
├── suppressions.xml             # Dependency check suppressions
└── gradle/libs.versions.toml    # Version catalog
```

## 🔧 Development Workflow

### Branch Naming
- `feature/description` - New features
- `fix/description` - Bug fixes
- `refactor/description` - Code refactoring
- `docs/description` - Documentation updates
- `chore/description` - Maintenance tasks

### Commit Messages
Follow [Conventional Commits](https://www.conventionalcommits.org/):
```
type(scope): description

[optional body]

[optional footer]
```

Types: `feat`, `fix`, `refactor`, `docs`, `chore`, `test`, `perf`, `ci`

Examples:
```
feat(dashboard): add storage breakdown chart
fix(swipeclean): handle undo after session complete
refactor(storage): extract media scanner interface
docs(readme): update build instructions
```

### Pull Request Process

1. **Create a feature branch** from `main`
2. **Make your changes** with tests
3. **Run all checks locally:**
```bash
./gradlew ktlintCheck detekt lintDebug testDebugUnitTest
```
4. **Push and create PR** using the appropriate template
5. **Address review feedback** promptly
6. **Squash and merge** after approval

## ✅ Code Quality Standards

### Before Committing
Run these checks locally:
```bash
# Format code
./gradlew ktlintFormat

# Check formatting
./gradlew ktlintCheck

# Static analysis
./gradlew detekt

# Android Lint
./gradlew lintDebug

# Unit tests
./gradlew testDebugUnitTest

# All checks
./gradlew check
```

### Code Style
- **Kotlin** - Follow [Kotlin Coding Conventions](https://kotlinlang.org/docs/coding-conventions.html)
- **Compose** - Follow [Compose Guidelines](https://developer.android.com/jetpack/compose/guidelines)
- **Architecture** - Clean Architecture + MVVM
- **Naming** - Follow [Android Naming Conventions](https://source.android.com/docs/setup/contribute/code-style)

### Testing Requirements
- **Unit tests** for all business logic (ViewModels, Repository, UseCases)
- **UI tests** for critical user flows
- **Integration tests** for database operations
- **Minimum 80% coverage** for new code

## 🏛️ Architecture Guidelines

### Clean Architecture Layers
```
UI (Compose)
    ↓
ViewModel (State + Business Logic)
    ↓
Use Cases (Single Responsibility)
    ↓
Repository (Data Abstraction)
    ↓
Data Sources (Room, MediaStore, PackageManager)
```

### Principles
- **Dependency Rule** - Inner layers don't know about outer layers
- **Single Responsibility** - Each class has one reason to change
- **Interface Segregation** - Small, focused interfaces
- **Dependency Inversion** - Depend on abstractions, not concretions

### State Management
- **StateFlow/LiveData** for observable state
- **State hoisting** - Keep state in ViewModels
- **Unidirectional data flow** - Events up, state down

## 🔒 Security Guidelines

### Privacy First
- No network permissions in manifest
- All processing on-device
- No analytics or tracking
- No crash reporting to external services

### Permissions
- Request contextually with clear rationale
- Support partial media access (Android 13+)
- Graceful degradation when denied

### Data Handling
- Room with SQLCipher for sensitive data
- No personal data in logs (debug builds only)
- Secure deletion via Android trash API
- No data leaves device without explicit consent

## 📱 Platform Compatibility

### Minimum SDK: 26 (Android 8.0)
### Target SDK: 34 (Android 14)

### Tested Configurations
- Android 8.0 - 14
- Various manufacturers (Samsung, Pixel, Xiaomi, OnePlus)
- Different screen sizes (phone, tablet, foldable)
- Light/Dark themes
- RTL languages

### Scoped Storage Compliance
- MediaStore for images/videos/audio
- Storage Access Framework for documents
- StorageStatsManager for app sizes
- No direct file path access

## 📦 Release Process

### Versioning
Follows [Semantic Versioning](https://semver.org/):
- `MAJOR.MINOR.PATCH`
- `1.0.0` - First stable release
- `1.1.0` - New features, backward compatible
- `1.0.1` - Bug fixes

### Release Checklist
- [ ] All CI checks pass
- [ ] Version bumped in `build.gradle.kts`
- [ ] Changelog updated (`CHANGELOG.md`)
- [ ] Release notes written
- [ ] Play Store listing updated
- [ ] Screenshots updated
- [ ] Privacy policy reviewed

## 🐛 Bug Reports

### Before Reporting
1. Check existing issues
2. Test on latest `main` branch
3. Try to reproduce on different devices/versions

### Bug Report Template
Use the [Bug Report template](.github/ISSUE_TEMPLATE/bug_report.md) with:
- Clear reproduction steps
- Device/Android version
- App version
- Screenshots/logs
- Expected vs actual behavior

## 💡 Feature Requests

### Process
1. Check existing issues/discussions
2. Open a [Feature Request](.github/ISSUE_TEMPLATE/feature_request.md)
3. Discuss design and approach
4. Implement with tests

### Criteria
- Aligns with product vision
- Privacy-first approach
- Good UX/UI
- Maintainable implementation
- Proper testing

## 📚 Resources

### Documentation
- [Architecture Decision Records](docs/adr/)
- [API Documentation](docs/api/)
- [Testing Guide](docs/testing.md)
- [Performance Guide](docs/performance.md)

### External Links
- [Android Developers](https://developer.android.com/)
- [Kotlin Documentation](https://kotlinlang.org/docs/home.html)
- [Jetpack Compose](https://developer.android.com/jetpack/compose)
- [Material Design 3](https://m3.material.io/)
- [Room Database](https://developer.android.com/training/data-storage/room)
- [WorkManager](https://developer.android.com/topic/libraries/architecture/workmanager)

## 🙋 Getting Help

- **GitHub Discussions** - General questions
- **GitHub Issues** - Bugs and feature requests
- **Discord** - Real-time chat (link in repo description)
- **Email** - maintainers@storiq.app

## 🏷️ Labels

| Label | Description |
|-------|-------------|
| `bug` | Something isn't working |
| `feature` | New feature request |
| `refactor` | Code improvement |
| `docs` | Documentation |
| `test` | Testing related |
| `performance` | Performance improvement |
| `security` | Security related |
| `accessibility` | A11y improvements |
| `android` | Platform specific |
| `compose` | Jetpack Compose |
| `architecture` | Architecture changes |
| `dependencies` | Dependency updates |
| `ci` | CI/CD related |
| `good first issue` | Good for newcomers |
| `help wanted` | Community contribution welcome |

---

Thank you for contributing to StorIQ! 🎉