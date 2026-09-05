# Security Policy for StorIQ

## Supported Versions

| Version | Supported          |
| ------- | ------------------ |
| 1.x.x   | ✅ Yes             |
| < 1.0   | ❌ No              |

## Reporting a Vulnerability

We take security vulnerabilities seriously. If you discover a security vulnerability in StorIQ, please report it responsibly.

### How to Report

**DO NOT** create a public GitHub issue for security vulnerabilities.

Instead, please email us at: **security@storiq.app**

Include the following information:
- Description of the vulnerability
- Steps to reproduce
- Potential impact
- Suggested fix (if any)
- Your contact information

### Response Timeline

- **Acknowledgment**: Within 48 hours
- **Initial Assessment**: Within 5 business days
- **Fix Development**: Within 30 days (depending on severity)
- **Release**: As soon as possible after fix verification

## Security Features

### Privacy by Design
- **No network permissions** - App works completely offline
- **Local-only processing** - All analysis happens on device
- **No analytics/tracking** - No Firebase, Google Analytics, or similar
- **Scoped Storage compliance** - Uses MediaStore and Storage Access Framework
- **No personal data collection** - No accounts, no cloud sync

### Data Protection
- **Room database encryption** - SQLCipher for sensitive data
- **Secure deletion** - Uses Android trash/recycle APIs
- **Permission minimalism** - Only requests necessary permissions
- **Contextual permissions** - Explains why each permission is needed

### Code Security
- **ProGuard/R8 obfuscation** - Release builds are obfuscated
- **No debug logs in release** - ProGuard strips debug logs
- **Certificate pinning** - Not applicable (no network)
- **Root detection** - Optional, can be enabled for enterprise

### Dependency Management
- **Weekly dependency updates** via Dependabot
- **OWASP Dependency Check** in CI pipeline
- **Trivy vulnerability scanning** in CI pipeline
- **Signed dependencies** - All from Maven Central/GitHub Packages

## Security Checklist for Contributors

### Before Submitting PR
- [ ] No hardcoded secrets, API keys, or passwords
- [ ] No logging of sensitive data (file paths, URIs, user data)
- [ ] Proper permission handling with rationale
- [ ] No unsafe reflection or dynamic code loading
- [ ] ProGuard rules updated for new classes
- [ ] Dependencies reviewed for known vulnerabilities

### Code Review Security Focus
- [ ] Input validation and sanitization
- [ ] Proper error handling (no stack traces to users)
- [ ] Secure random number generation
- [ ] No SQL injection (Room parameterized queries)
- [ ] No path traversal (SAF used for file access)
- [ ] Proper intent validation

## Known Security Considerations

### Storage Access
- App requires media permissions to scan files
- Uses MediaStore for images/videos/audio
- Uses Storage Access Framework for downloads/documents
- No access to /data/, /Android/data/, /Android/obb/

### Deletion Safety
- All deletions go through Android trash/recycle bin
- User must explicitly confirm each deletion
- No automatic/background deletion
- Review screen shows exactly what will be deleted

### Background Work
- WorkManager for periodic scans
- Battery-aware scheduling (respects power save mode)
- No wake locks unless user explicitly enables
- Scans can be cancelled by user

## Incident Response

### If a vulnerability is discovered in production:
1. **Immediate**: Assess severity and impact
2. **Short-term**: Deploy hotfix or mitigation
3. **Medium-term**: Full fix with proper testing
4. **Long-term**: Process improvement to prevent recurrence

### Communication
- Security advisories published on GitHub Security Advisories
- Users notified via Play Store update description
- No public disclosure until fix is available

## Compliance

### Standards
- **OWASP MASVS** - Mobile Application Security Verification Standard
- **OWASP MSTG** - Mobile Security Testing Guide
- **Android Security Best Practices** - Google's guidelines

### Regulations
- **GDPR** - No personal data collected, right to deletion inherent
- **CCPA** - No sale of personal information
- **Google Play Policies** - Full compliance with Developer Program Policies

## Contact

For security questions or concerns:
- **Email**: security@storiq.app
- **PGP Key**: Available on request
- **Bug Bounty**: Not currently offered, but responsible disclosure appreciated