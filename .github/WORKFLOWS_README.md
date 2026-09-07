# GitHub Actions CI/CD Setup for StorIQ

## Workflows

### `ci.yml` - Main CI Pipeline
Runs on every push/PR to main/develop branches.

**Jobs:**
1. **code-quality** - Ktlint, Detekt, Android Lint
2. **unit-tests** - JUnit tests with JaCoCo coverage
3. **build-debug** - Debug APK build (depends on quality + tests)
4. **build-release** - Release AAB build (main branch only)
5. **instrumented-tests** - Android instrumented tests on emulator
6. **security-scan** - OWASP Dependency Check + Trivy
7. **publish-internal** - Deploy to Play Store Internal track (main branch)
8. **notify-failure** - Failure notification

## Required Secrets

Add these in GitHub Settings > Secrets and Variables > Actions:

### Keystore (for release builds)
```
KEYSTORE_BASE64          # base64-encoded keystore.jks
KEYSTORE_PASSWORD        # keystore password
KEY_ALIAS                # key alias
KEY_PASSWORD             # key password
```

### Play Store (for publishing)
```
PLAY_SERVICE_ACCOUNT_KEY  # Google Play service account JSON
```

## Local Development

### Run all checks locally:
```bash
# Format code
./gradlew ktlintFormat

# Check formatting
./gradlew ktlintCheck

# Run detekt
./gradlew detekt

# Run Android Lint
./gradlew lintDebug

# Run unit tests with coverage
./gradlew testDebugUnitTest jacocoTestReport

# Run all checks
./gradlew check
```

### Generate baseline files:
```bash
# Detekt baseline
./gradlew detektBaseline

# Android Lint baseline
./gradlew lintBaseline
```

## Artifacts

### Build Artifacts (30-day retention)
- `app-debug.apk` - Debug APK
- `app-release.aab` - Release bundle (main branch only)

### Test Reports (7-day retention)
- `unit-test-results` - JUnit XML reports
- `coverage-report` - JaCoCo HTML/XML reports
- `instrumented-test-results` - Android test results

### Quality Reports (7-day retention)
- `lint-report` - Android Lint HTML report
- `detekt-report` - Detekt HTML/XML/SARIF reports

### Security Reports (30-day retention)
- `dependency-check-report` - OWASP Dependency Check
- `trivy-results.sarif` - Trivy vulnerability scan (uploaded to GitHub Security)

## Triggers

| Event | Branches | Workflows |
|-------|----------|-----------|
| Push | main, develop | All |
| Pull Request | main, develop | All except publish |
| Schedule | - | Security scan (weekly) |

## Badges

Add to README.md:
```markdown
![CI](https://github.com/OWNER/REPO/workflows/CI/badge.svg)
![Coverage](https://codecov.io/gh/OWNER/REPO/branch/main/graph/badge.svg)
```
