# Changelog

All notable changes to StorIQ will be documented in this file.

The format is based on [Keep a Changelog](https://keepachangelog.com/en/1.0.0/),
and this project adheres to [Semantic Versioning](https://semver.org/spec/v2.0.0.html).

## [Unreleased]

### Added
- Initial project structure with Clean Architecture + MVVM
- MediaStore scanner for images, videos, audio
- File scanner for downloads/documents
- App scanner with PackageManager + StorageStatsManager
- SHA-256 duplicate detection
- Dashboard with live storage stats and breakdown
- Analyzer screen with 8 category cards
- Clean screen with 5 cleanup categories
- Large Files screen with size grouping (4 tiers) and 6 sort options
- Duplicates screen with hash grouping, recommended keep, review grid
- Swipe Clean gallery with gestures, undo, review screen
- Apps screen with summary, unused apps, largest apps
- Storage History with charts, growth calculations, projections
- Onboarding flow (3 screens + initial scan)
- WorkManager for background scans with battery awareness
- Room database with 10 entities and 10 DAOs
- Material 3 theming with dark/light support
- Accessibility support (TalkBack, semantics, content descriptions)
- Unit tests for models, algorithms, history calculations
- CI/CD pipeline with GitHub Actions

### Changed
- N/A (initial release)

### Deprecated
- N/A

### Removed
- N/A

### Fixed
- N/A

### Security
- No network permissions declared
- Scoped Storage compliant implementation
- ProGuard/R8 obfuscation for release builds
- OWASP Dependency Check in CI
- Trivy vulnerability scanning in CI

---

## [1.0.0] - TBD

### Added
- First stable release
- All MVP features complete

---

## Release Notes Template

### [X.Y.Z] - YYYY-MM-DD

#### Added
- New features

#### Changed
- Changes to existing functionality

#### Deprecated
- Soon-to-be removed features

#### Removed
- Removed features

#### Fixed
- Bug fixes

#### Security
- Security improvements

---

## Version History

| Version | Date | Notes |
|---------|------|-------|
| 1.0.0 | TBD | First stable release |
| 0.1.0 | TBD | Alpha release |

---

## Links

- [GitHub Releases](https://github.com/OWNER/StorIQ/releases)
- [GitHub Commits](https://github.com/OWNER/StorIQ/commits/main)
- [Issue Tracker](https://github.com/OWNER/StorIQ/issues)