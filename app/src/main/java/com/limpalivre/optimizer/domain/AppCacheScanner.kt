package com.limpalivre.optimizer.domain

import android.Manifest
import android.app.usage.StorageStatsManager
import android.app.usage.UsageStatsManager
import android.content.Context
import android.content.pm.ApplicationInfo
import android.content.pm.PackageInfo
import android.content.pm.PackageManager
import android.os.Build
import android.os.Process
import android.os.storage.StorageManager
import com.limpalivre.optimizer.data.explainer.ExplainerCatalog
import com.limpalivre.optimizer.data.model.AppCategory
import com.limpalivre.optimizer.data.model.AppStorageItem
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File

class AppCacheScanner(private val context: Context) {

    private val packageManager = context.packageManager
    private val storageStatsManager =
        context.getSystemService(Context.STORAGE_STATS_SERVICE) as StorageStatsManager
    private val usageStatsManager =
        context.getSystemService(Context.USAGE_STATS_SERVICE) as UsageStatsManager

    private val trackedSensitivePermissions = mapOf(
        Manifest.permission.CAMERA to "Câmera",
        Manifest.permission.RECORD_AUDIO to "Microfone",
        Manifest.permission.ACCESS_FINE_LOCATION to "Localização",
        Manifest.permission.READ_CONTACTS to "Contatos",
        Manifest.permission.READ_SMS to "SMS",
        Manifest.permission.READ_CALL_LOG to "Chamadas"
    )

    suspend fun scanInstalledApps(
        whitelist: Set<String>,
        includeSystemApps: Boolean = false
    ): List<AppStorageItem> = withContext(Dispatchers.IO) {
        val usageMap = loadLastUsedMap()
        val apps = getInstalledApplicationsCompat()
        val selfPackage = context.packageName

        val results = mutableListOf<AppStorageItem>()
        for (appInfo in apps) {
            if (appInfo.packageName == selfPackage) continue
            val isPureSystem = (appInfo.flags and ApplicationInfo.FLAG_SYSTEM) != 0 &&
                (appInfo.flags and ApplicationInfo.FLAG_UPDATED_SYSTEM_APP) == 0
            if (isPureSystem && !includeSystemApps) continue

            val appName = runCatching {
                packageManager.getApplicationLabel(appInfo).toString()
            }.getOrDefault(appInfo.packageName)

            val stats = queryAppStorage(appInfo.packageName)
            val category = ExplainerCatalog.classifyApp(appInfo.packageName, appInfo.category)
            val explanation = ExplainerCatalog.explainApp(appInfo.packageName, appName, appInfo.category)
            val isWhitelisted = appInfo.packageName in whitelist
            val defaultSelected = !isWhitelisted &&
                stats.cacheBytes > 1024L * 1024L &&
                category != AppCategory.BANKING

            val sensitivePerms = readGrantedSensitivePermissions(appInfo.packageName)

            results.add(
                AppStorageItem(
                    packageName = appInfo.packageName,
                    appName = appName,
                    category = category,
                    cacheBytes = stats.cacheBytes,
                    dataBytes = stats.dataBytes,
                    appBytes = stats.appBytes,
                    lastTimeUsed = usageMap[appInfo.packageName] ?: 0L,
                    isSystemApp = isPureSystem,
                    isWhitelisted = isWhitelisted,
                    isSelected = defaultSelected,
                    sensitivePermissions = sensitivePerms,
                    explanation = explanation
                )
            )
        }

        results.sortedWith(
            compareByDescending<AppStorageItem> { it.cacheBytes }
                .thenByDescending { it.totalBytes }
                .thenBy { it.appName.lowercase() }
        )
    }

    suspend fun measureCacheBytes(packageNames: List<String>): Map<String, Long> =
        withContext(Dispatchers.IO) {
            packageNames.associateWith { pkg ->
                queryAppStorage(pkg).cacheBytes
            }
        }

    fun clearOwnCache(): Long {
        var freed = 0L
        listOfNotNull(context.cacheDir, context.externalCacheDir).forEach { dir ->
            freed += deleteDirContents(dir)
        }
        return freed
    }

    private fun deleteDirContents(dir: File): Long {
        if (!dir.exists() || !dir.isDirectory) return 0L
        var bytes = 0L
        dir.listFiles()?.forEach { child ->
            if (child.isDirectory) {
                bytes += deleteDirContents(child)
                child.delete()
            } else {
                val len = child.length()
                if (child.delete()) {
                    bytes += len
                }
            }
        }
        return bytes
    }

    private data class StorageTriple(
        val cacheBytes: Long,
        val dataBytes: Long,
        val appBytes: Long
    )

    private fun queryAppStorage(packageName: String): StorageTriple {
        return try {
            val stats = storageStatsManager.queryStatsForPackage(
                StorageManager.UUID_DEFAULT,
                packageName,
                Process.myUserHandle()
            )
            StorageTriple(
                cacheBytes = stats.cacheBytes.coerceAtLeast(0L),
                dataBytes = (stats.dataBytes - stats.cacheBytes).coerceAtLeast(0L),
                appBytes = stats.appBytes.coerceAtLeast(0L)
            )
        } catch (_: Exception) {
            StorageTriple(0L, 0L, 0L)
        }
    }

    private fun loadLastUsedMap(): Map<String, Long> {
        return try {
            val now = System.currentTimeMillis()
            val oneYearAgo = now - 365L * 24L * 60L * 60L * 1000L
            val stats = usageStatsManager.queryUsageStats(
                UsageStatsManager.INTERVAL_YEARLY,
                oneYearAgo,
                now
            ) ?: return emptyMap()
            val map = HashMap<String, Long>(stats.size)
            for (stat in stats) {
                val prev = map[stat.packageName] ?: 0L
                if (stat.lastTimeUsed > prev) {
                    map[stat.packageName] = stat.lastTimeUsed
                }
            }
            map
        } catch (_: Exception) {
            emptyMap()
        }
    }

    private fun readGrantedSensitivePermissions(packageName: String): List<String> {
        return try {
            val info = getPackageInfoPermissionsCompat(packageName) ?: return emptyList()
            val requested = info.requestedPermissions ?: return emptyList()
            val flags = info.requestedPermissionsFlags ?: return emptyList()
            val granted = mutableListOf<String>()
            for (i in requested.indices) {
                val label = trackedSensitivePermissions[requested[i]] ?: continue
                val isGranted = i < flags.size &&
                    (flags[i] and PackageInfo.REQUESTED_PERMISSION_GRANTED) != 0
                if (isGranted && label !in granted) {
                    granted.add(label)
                }
            }
            granted
        } catch (_: Exception) {
            emptyList()
        }
    }

    @Suppress("DEPRECATION")
    private fun getInstalledApplicationsCompat(): List<ApplicationInfo> {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            packageManager.getInstalledApplications(
                PackageManager.ApplicationInfoFlags.of(PackageManager.GET_META_DATA.toLong())
            )
        } else {
            packageManager.getInstalledApplications(PackageManager.GET_META_DATA)
        }
    }

    @Suppress("DEPRECATION")
    private fun getPackageInfoPermissionsCompat(packageName: String): PackageInfo? {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            packageManager.getPackageInfo(
                packageName,
                PackageManager.PackageInfoFlags.of(PackageManager.GET_PERMISSIONS.toLong())
            )
        } else {
            packageManager.getPackageInfo(packageName, PackageManager.GET_PERMISSIONS)
        }
    }
}
