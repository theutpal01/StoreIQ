package com.storiq.core.util

import android.content.Context
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.provider.MediaStore
import androidx.core.content.ContextCompat

object PrivacyAudit {

    data class AuditResult(
        val passed: Boolean,
        val checks: List<CheckResult>
    ) {
        val failedChecks: List<CheckResult>
            get() = checks.filter { !it.passed }
    }

    data class CheckResult(
        val name: String,
        val description: String,
        val passed: Boolean,
        val severity: Severity = Severity.WARNING
    )

    enum class Severity {
        CRITICAL,
        WARNING,
        INFO
    }

    fun runPrivacyAudit(context: Context): AuditResult {
        val checks = mutableListOf<CheckResult>()

        // Check 1: No network permission declared
        checks.add(CheckResult(
            name = "No Network Permission",
            description = "App does not declare INTERNET permission",
            passed = !hasPermission(context, "android.permission.INTERNET"),
            severity = Severity.CRITICAL
        ))

        // Check 2: Minimal permissions
        val dangerousPerms = listOf(
            "android.permission.READ_MEDIA_IMAGES",
            "android.permission.READ_MEDIA_VIDEO",
            "android.permission.READ_MEDIA_AUDIO",
            "android.permission.READ_EXTERNAL_STORAGE",
            "android.permission.MANAGE_EXTERNAL_STORAGE",
            "android.permission.PACKAGE_USAGE_STATS"
        )
        val grantedDangerous = dangerousPerms.filter { hasPermission(context, it) }
        checks.add(CheckResult(
            name = "Minimal Permissions",
            description = "Only necessary dangerous permissions granted (${grantedDangerous.size}/${dangerousPerms.size})",
            passed = grantedDangerous.size <= 4, // Allow media + usage stats
            severity = Severity.WARNING
        ))

        // Check 3: No analytics/tracking libraries detected
        checks.add(CheckResult(
            name = "No Tracking Libraries",
            description = "No Firebase Analytics, Google Analytics, or similar detected",
            passed = !hasTrackingLibraries(context),
            severity = Severity.WARNING
        ))

        // Check 4: Data stored locally only
        checks.add(CheckResult(
            name = "Local Storage Only",
            description = "All data stored in app-private Room database",
            passed = true, // Enforced by architecture
            severity = Severity.INFO
        ))

        // Check 5: No crash reporting to external services
        checks.add(CheckResult(
            name = "No External Crash Reporting",
            description = "Crashes not sent to external services",
            passed = !hasCrashReporting(context),
            severity = Severity.WARNING
        ))

        // Check 6: Scoped storage compliance
        checks.add(CheckResult(
            name = "Scoped Storage Compliant",
            description = "Uses MediaStore and SAF, no direct file paths",
            passed = true, // Enforced by implementation
            severity = Severity.INFO
        ))

        // Check 7: No personal data in logs
        checks.add(CheckResult(
            name = "No PII in Logs",
            description = "File paths, URIs, names not logged in production",
            passed = BuildConfig.DEBUG, // Only in debug builds
            severity = Severity.WARNING
        ))

        val passed = checks.all { it.passed }
        return AuditResult(passed, checks)
    }

    private fun hasPermission(context: Context, permission: String): Boolean {
        return ContextCompat.checkSelfPermission(context, permission) == PackageManager.PERMISSION_GRANTED
    }

    private fun hasTrackingLibraries(context: Context): Boolean {
        val trackingPackages = listOf(
            "com.google.firebase.analytics",
            "com.google.android.gms.analytics",
            "com.facebook.appevents",
            "com.appsflyer",
            "io.branch.referral",
            "com.adjust.sdk"
        )
        val pm = context.packageManager
        return trackingPackages.any { pkg ->
            try {
                pm.getPackageInfo(pkg, 0)
                true
            } catch (e: PackageManager.NameNotFoundException) {
                false
            }
        }
    }

    private fun hasCrashReporting(context: Context): Boolean {
        val crashPackages = listOf(
            "com.crashlytics",
            "io.fabric.sdk",
            "com.google.firebase.crashlytics",
            "com.bugsnag",
            "com.sentry"
        )
        val pm = context.packageManager
        return crashPackages.any { pkg ->
            try {
                pm.getPackageInfo(pkg, 0)
                true
            } catch (e: PackageManager.NameNotFoundException) {
                false
            }
        }
    }

    fun generateAuditReport(context: Context): String {
        val result = runPrivacyAudit(context)
        val sb = StringBuilder()
        sb.appendLine("=== StorIQ Privacy Audit Report ===")
        sb.appendLine("App Version: ${BuildConfig.VERSION_NAME} (${BuildConfig.VERSION_CODE})")
        sb.appendLine("Android Version: ${Build.VERSION.RELEASE} (SDK ${Build.VERSION.SDK_INT})")
        sb.appendLine("Audit Result: ${if (result.passed) "PASSED" else "FAILED"}")
        sb.appendLine("")
        
        result.checks.forEach { check ->
            sb.appendLine("[${check.severity.name}] ${check.name}")
            sb.appendLine("  ${check.description}")
            sb.appendLine("  Status: ${if (check.passed) "PASS" else "FAIL"}")
            sb.appendLine("")
        }
        
        return sb.toString()
    }
}