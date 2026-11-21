package com.limpalivre.optimizer.data.model

data class StorageSummary(
    val totalBytes: Long,
    val usedBytes: Long,
    val freeBytes: Long
) {
    val usedFraction: Float
        get() = if (totalBytes > 0L) (usedBytes.toFloat() / totalBytes.toFloat()).coerceIn(0f, 1f) else 0f
}

data class RamSummary(
    val totalBytes: Long,
    val availableBytes: Long,
    val usedBytes: Long,
    val lowMemory: Boolean
) {
    val usedFraction: Float
        get() = if (totalBytes > 0L) (usedBytes.toFloat() / totalBytes.toFloat()).coerceIn(0f, 1f) else 0f
}

data class BackgroundAppItem(
    val packageName: String,
    val appName: String,
    val lastUsedMillis: Long,
    val isActiveProcess: Boolean,
    val isWhitelisted: Boolean,
    val isSelected: Boolean,
    val explanation: String
)

data class BatteryInfo(
    val levelPercent: Int,
    val isCharging: Boolean,
    val chargeSource: String,
    val temperatureCelsius: Float,
    val voltageMv: Int,
    val healthLabel: String,
    val technology: String
)

data class DeviceOverview(
    val manufacturer: String,
    val model: String,
    val androidVersion: String,
    val apiLevel: Int,
    val cpuCores: Int,
    val primaryAbi: String
)

data class PermissionsStatus(
    val usageStatsGranted: Boolean,
    val allFilesGranted: Boolean,
    val accessibilityEnabled: Boolean,
    val notificationsGranted: Boolean
) {
    val coreGrantedCount: Int
        get() = listOf(usageStatsGranted, allFilesGranted, accessibilityEnabled).count { it }
}
