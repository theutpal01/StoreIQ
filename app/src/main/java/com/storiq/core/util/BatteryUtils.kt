package com.storiq.core.util

import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.BatteryManager
import android.os.Build
import android.os.PowerManager
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

object BatteryUtils {

    fun isBatteryOptimized(context: Context): Boolean {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            val powerManager = context.getSystemService(Context.POWER_SERVICE) as PowerManager
            return powerManager.isIgnoringBatteryOptimizations(context.packageName)
        }
        return true
    }

    fun requestBatteryOptimizationExemption(context: Context): Intent {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            val intent = Intent(android.provider.Settings.ACTION_REQUEST_IGNORE_BATTERY_OPTIMIZATIONS)
            intent.data = android.net.Uri.parse("package:${context.packageName}")
            return intent
        }
        return Intent()
    }

    fun getBatteryLevel(context: Context): Int {
        val batteryStatus: Intent? = context.registerReceiver(null, IntentFilter(Intent.ACTION_BATTERY_CHANGED))
        val level = batteryStatus?.getIntExtra(BatteryManager.EXTRA_LEVEL, -1) ?: -1
        val scale = batteryStatus?.getIntExtra(BatteryManager.EXTRA_SCALE, -1) ?: -1
        return if (level == -1 || scale == -1) -1 else (level * 100) / scale
    }

    fun isCharging(context: Context): Boolean {
        val batteryStatus: Intent? = context.registerReceiver(null, IntentFilter(Intent.ACTION_BATTERY_CHANGED))
        val status = batteryStatus?.getIntExtra(BatteryManager.EXTRA_STATUS, -1) ?: -1
        return status == BatteryManager.BATTERY_STATUS_CHARGING || status == BatteryManager.BATTERY_STATUS_FULL
    }

    fun isPowerSaveMode(context: Context): Boolean {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            val powerManager = context.getSystemService(Context.POWER_SERVICE) as PowerManager
            return powerManager.isPowerSaveMode
        }
        return false
    }

    fun shouldThrottleBackgroundWork(context: Context): Boolean {
        return isPowerSaveMode(context) || getBatteryLevel(context) < 15
    }

    fun getOptimalScanInterval(context: Context): Long {
        return when {
            isCharging(context) -> 60 * 60 * 1000 // 1 hour when charging
            getBatteryLevel(context) > 50 -> 4 * 60 * 60 * 1000 // 4 hours
            getBatteryLevel(context) > 20 -> 8 * 60 * 60 * 1000 // 8 hours
            else -> 24 * 60 * 60 * 1000 // 24 hours
        }
    }

    suspend fun performIfBatteryOk(
        context: Context,
        threshold: Int = 15,
        block: suspend () -> Unit
    ) {
        val batteryLevel = getBatteryLevel(context)
        val charging = isCharging(context)
        
        if (batteryLevel > threshold || charging) {
            block()
        } else {
            // Schedule for later when charging
        }
    }
}