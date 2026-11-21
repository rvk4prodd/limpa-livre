package com.limpalivre.optimizer.ui.state

import android.app.Application
import android.content.Intent
import android.net.Uri
import android.os.Environment
import android.provider.Settings
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.limpalivre.optimizer.data.model.AppStorageItem
import com.limpalivre.optimizer.data.model.BackgroundAppItem
import com.limpalivre.optimizer.data.model.BatteryInfo
import com.limpalivre.optimizer.data.model.CleanActionStatus
import com.limpalivre.optimizer.data.model.CleanReport
import com.limpalivre.optimizer.data.model.DeepCategoryType
import com.limpalivre.optimizer.data.model.DeepCleanCategory
import com.limpalivre.optimizer.data.model.DeviceOverview
import com.limpalivre.optimizer.data.model.PermissionsStatus
import com.limpalivre.optimizer.data.model.RamSummary
import com.limpalivre.optimizer.data.model.StorageSummary
import com.limpalivre.optimizer.data.preferences.AppPreferencesStore
import com.limpalivre.optimizer.domain.AppCacheScanner
import com.limpalivre.optimizer.domain.DeepStorageScanner
import com.limpalivre.optimizer.domain.PermissionsHelper
import com.limpalivre.optimizer.domain.RamAndBatteryMonitor
import com.limpalivre.optimizer.service.AutomationState
import com.limpalivre.optimizer.service.CleanAutomationCoordinator
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

enum class AppSortMode(val label: String) {
    CACHE_DESC("Maior cache"),
    TOTAL_DESC("Maior tamanho"),
    NAME_ASC("A–Z")
}

enum class AppManagerFilter(val label: String) {
    ALL("Todos"),
    HEAVIEST("Mais pesados"),
    UNUSED_30D("Pouco usados (30d+)"),
    SENSITIVE_PERMS("Permissões sensíveis")
}

data class MainUiState(
    val permissions: PermissionsStatus = PermissionsStatus(false, false, false, true),
    val storageSummary: StorageSummary = StorageSummary(0L, 0L, 0L),
    val ramSummary: RamSummary = RamSummary(0L, 0L, 0L, false),
    val batteryInfo: BatteryInfo = BatteryInfo(0, false, "Na bateria", 0f, 0, "Normal", "Li-ion"),
    val deviceOverview: DeviceOverview = DeviceOverview("", "", "", 0, 0, ""),
    val appCaches: List<AppStorageItem> = emptyList(),
    val appSearchQuery: String = "",
    val appSortMode: AppSortMode = AppSortMode.CACHE_DESC,
    val hideZeroCacheApps: Boolean = true,
    val isScanningApps: Boolean = false,
    val deepCategories: List<DeepCleanCategory> = emptyList(),
    val isScanningDeep: Boolean = false,
    val deepScanPathText: String = "",
    val isCleaningDeep: Boolean = false,
    val backgroundApps: List<BackgroundAppItem> = emptyList(),
    val isScanningRam: Boolean = false,
    val isOptimizingRam: Boolean = false,
    val isRunningFullAnalysis: Boolean = false,
    val appManagerFilter: AppManagerFilter = AppManagerFilter.ALL,
    val whitelist: Set<String> = emptySet(),
    val history: List<CleanReport> = emptyList(),
    val activeReport: CleanReport? = null,
    val showAccessibilityDisclosure: Boolean = false,
    val infoBannerMessage: String? = null
) {
    val selectedAppCacheBytes: Long
        get() = appCaches.filter { it.isSelected && !it.isWhitelisted }.sumOf { it.cacheBytes }

    val selectedAppCount: Int
        get() = appCaches.count { it.isSelected && !it.isWhitelisted }

    val totalAppCacheBytes: Long
        get() = appCaches.sumOf { it.cacheBytes }

    val totalDeepRecoverableBytes: Long
        get() = deepCategories.sumOf { it.selectedBytes }

    val totalDeepFoundBytes: Long
        get() = deepCategories.sumOf { it.totalBytes }

    val selectedDeepCount: Int
        get() = deepCategories.sumOf { it.selectedCount }

    val selectedBackgroundAppsCount: Int
        get() = backgroundApps.count { it.isSelected && !it.isWhitelisted }
}

class MainViewModel(application: Application) : AndroidViewModel(application) {

    private val context = application.applicationContext
    private val prefs = AppPreferencesStore(context)
    private val appCacheScanner = AppCacheScanner(context)
    private val ramMonitor = RamAndBatteryMonitor(context)
    private val deepScanner = DeepStorageScanner(Environment.getExternalStorageDirectory())

    private val _uiState = MutableStateFlow(
        MainUiState(
            whitelist = prefs.getWhitelist(),
            history = prefs.getHistory()
        )
    )
    val uiState: StateFlow<MainUiState> = _uiState.asStateFlow()
    val automationState: StateFlow<AutomationState> = CleanAutomationCoordinator.state

    init {
        refreshSystemSnapshot()
        viewModelScope.launch {
            if (PermissionsHelper.hasUsageStatsPermission(context)) {
                scanAppCaches()
                scanBackgroundApps()
            }
        }
    }

    fun onHostResume() {
        refreshSystemSnapshot()
        viewModelScope.launch {
            if (CleanAutomationCoordinator.state.value.awaitingReportGeneration) {
                finalizeAutomationReport()
            } else if (PermissionsHelper.hasUsageStatsPermission(context) && _uiState.value.appCaches.isEmpty()) {
                scanAppCaches()
                scanBackgroundApps()
            }
        }
    }

    fun refreshSystemSnapshot() {
        val perms = PermissionsHelper.getPermissionsStatus(context)
        val storage = ramMonitor.getStorageSummary()
        val ram = ramMonitor.getRamSummary()
        val battery = ramMonitor.getBatteryInfo()
        val device = ramMonitor.getDeviceOverview()
        _uiState.update {
            it.copy(
                permissions = perms,
                storageSummary = storage,
                ramSummary = ram,
                batteryInfo = battery,
                deviceOverview = device,
                whitelist = prefs.getWhitelist(),
                history = prefs.getHistory()
            )
        }
    }

    fun runFullAnalysis() {
        viewModelScope.launch {
            _uiState.update { it.copy(isRunningFullAnalysis = true, infoBannerMessage = null) }
            refreshSystemSnapshot()
            if (PermissionsHelper.hasUsageStatsPermission(context)) {
                scanAppCachesInternal()
                scanBackgroundAppsInternal()
            }
            if (PermissionsHelper.hasAllFilesPermission(context)) {
                scanDeepStorageInternal()
            }
            _uiState.update { it.copy(isRunningFullAnalysis = false) }
        }
    }

    fun scanAppCaches() {
        viewModelScope.launch {
            scanAppCachesInternal()
        }
    }

    private suspend fun scanAppCachesInternal() {
        _uiState.update { it.copy(isScanningApps = true) }
        val list = appCacheScanner.scanInstalledApps(_uiState.value.whitelist)
        _uiState.update { it.copy(appCaches = list, isScanningApps = false) }
    }

    fun updateAppSearch(query: String) {
        _uiState.update { it.copy(appSearchQuery = query) }
    }

    fun updateAppSortMode(mode: AppSortMode) {
        _uiState.update { it.copy(appSortMode = mode) }
    }

    fun toggleHideZeroCacheApps() {
        _uiState.update { it.copy(hideZeroCacheApps = !it.hideZeroCacheApps) }
    }

    fun toggleAppSelection(packageName: String) {
        _uiState.update { current ->
            val updated = current.appCaches.map { app ->
                if (app.packageName == packageName && !app.isWhitelisted) {
                    app.copy(isSelected = !app.isSelected)
                } else {
                    app
                }
            }
            current.copy(appCaches = updated)
        }
    }

    fun selectAllVisibleApps(select: Boolean) {
        _uiState.update { current ->
            val updated = current.appCaches.map { app ->
                if (app.isWhitelisted || app.cacheBytes <= 0L) app
                else app.copy(isSelected = select)
            }
            current.copy(appCaches = updated)
        }
    }

    fun toggleWhitelist(packageName: String) {
        val updatedWhitelist = prefs.toggleWhitelist(packageName)
        _uiState.update { current ->
            val updatedApps = current.appCaches.map { app ->
                if (app.packageName == packageName) {
                    val protectedNow = packageName in updatedWhitelist
                    app.copy(
                        isWhitelisted = protectedNow,
                        isSelected = if (protectedNow) false else app.isSelected
                    )
                } else {
                    app
                }
            }
            val updatedBg = current.backgroundApps.map { bg ->
                if (bg.packageName == packageName) {
                    val protectedNow = packageName in updatedWhitelist
                    bg.copy(
                        isWhitelisted = protectedNow,
                        isSelected = if (protectedNow) false else bg.isSelected
                    )
                } else {
                    bg
                }
            }
            current.copy(
                whitelist = updatedWhitelist,
                appCaches = updatedApps,
                backgroundApps = updatedBg
            )
        }
    }

    fun requestStartAppCacheClean() {
        val selected = _uiState.value.appCaches.filter { it.isSelected && !it.isWhitelisted }
        if (selected.isEmpty()) {
            _uiState.update { it.copy(infoBannerMessage = "Selecione pelo menos um aplicativo para limpar o cache.") }
            return
        }

        if (PermissionsHelper.isAccessibilityServiceEnabled(context)) {
            startAutomatedClean(selected)
        } else {
            _uiState.update { it.copy(showAccessibilityDisclosure = true) }
        }
    }

    fun dismissAccessibilityDisclosure() {
        _uiState.update { it.copy(showAccessibilityDisclosure = false) }
    }

    fun startManualGuidedClean() {
        _uiState.update { it.copy(showAccessibilityDisclosure = false) }
        val selected = _uiState.value.appCaches.filter { it.isSelected && !it.isWhitelisted }
        if (selected.isEmpty()) return
        CleanAutomationCoordinator.startManualGuidedQueue(selected)
        val first = CleanAutomationCoordinator.state.value.currentApp ?: return
        openAppInfo(first.packageName)
    }

    fun advanceManualGuidedNext() {
        val next = CleanAutomationCoordinator.recordOutcomeAndAdvance(CleanActionStatus.SUCCESS)
        if (next != null) {
            openAppInfo(next.packageName)
        } else {
            viewModelScope.launch { finalizeAutomationReport() }
        }
    }

    fun skipManualGuidedCurrent() {
        val next = CleanAutomationCoordinator.recordOutcomeAndAdvance(CleanActionStatus.SKIPPED)
        if (next != null) {
            openAppInfo(next.packageName)
        } else {
            viewModelScope.launch { finalizeAutomationReport() }
        }
    }

    fun finishManualGuidedNow() {
        CleanAutomationCoordinator.cancelRun()
        viewModelScope.launch { finalizeAutomationReport() }
    }

    private fun startAutomatedClean(selected: List<AppStorageItem>) {
        CleanAutomationCoordinator.startAutomatedQueue(selected)
        val first = CleanAutomationCoordinator.state.value.currentApp ?: return
        openAppInfo(first.packageName)
    }

    private suspend fun finalizeAutomationReport() {
        appCacheScanner.clearOwnCache()
        val report = CleanAutomationCoordinator.consumeAndBuildReport(appCacheScanner) ?: return
        prefs.saveReport(report)
        refreshSystemSnapshot()
        scanAppCachesInternal()
        _uiState.update {
            it.copy(
                activeReport = report,
                history = prefs.getHistory()
            )
        }
    }

    fun openAppInfo(packageName: String) {
        val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
            data = Uri.parse("package:$packageName")
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_NO_ANIMATION)
        }
        runCatching { context.startActivity(intent) }
    }

    fun scanDeepStorage() {
        viewModelScope.launch {
            scanDeepStorageInternal()
        }
    }

    private suspend fun scanDeepStorageInternal() {
        _uiState.update { it.copy(isScanningDeep = true, deepScanPathText = "Iniciando varredura...") }
        val categories = deepScanner.scan { pathHint ->
            _uiState.update { it.copy(deepScanPathText = pathHint) }
        }
        _uiState.update {
            it.copy(
                deepCategories = categories,
                isScanningDeep = false,
                deepScanPathText = ""
            )
        }
    }

    fun toggleDeepCategorySelection(type: DeepCategoryType) {
        _uiState.update { current ->
            val updated = current.deepCategories.map { cat ->
                if (cat.type != type) {
                    cat
                } else {
                    val allSelectableSelected = cat.items
                        .filter { !it.isProtectedOriginal }
                        .all { it.isSelected }
                    val nextSelect = !allSelectableSelected
                    cat.copy(
                        items = cat.items.map { item ->
                            if (item.isProtectedOriginal) item else item.copy(isSelected = nextSelect)
                        }
                    )
                }
            }
            current.copy(deepCategories = updated)
        }
    }

    fun toggleDeepFileItem(type: DeepCategoryType, filePath: String) {
        _uiState.update { current ->
            val updated = current.deepCategories.map { cat ->
                if (cat.type != type) {
                    cat
                } else {
                    cat.copy(
                        items = cat.items.map { item ->
                            if (item.path == filePath && !item.isProtectedOriginal) {
                                item.copy(isSelected = !item.isSelected)
                            } else {
                                item
                            }
                        }
                    )
                }
            }
            current.copy(deepCategories = updated)
        }
    }

    fun cleanSelectedDeepFiles() {
        viewModelScope.launch {
            _uiState.update { it.copy(isCleaningDeep = true) }
            val report = deepScanner.cleanSelected(_uiState.value.deepCategories)
            prefs.saveReport(report)
            refreshSystemSnapshot()
            scanDeepStorageInternal()
            _uiState.update {
                it.copy(
                    isCleaningDeep = false,
                    activeReport = report,
                    history = prefs.getHistory()
                )
            }
        }
    }

    fun scanBackgroundApps() {
        viewModelScope.launch {
            scanBackgroundAppsInternal()
        }
    }

    private suspend fun scanBackgroundAppsInternal() {
        _uiState.update { it.copy(isScanningRam = true) }
        val bg = ramMonitor.getBackgroundApps(_uiState.value.whitelist)
        _uiState.update {
            it.copy(
                backgroundApps = bg,
                ramSummary = ramMonitor.getRamSummary(),
                isScanningRam = false
            )
        }
    }

    fun toggleBackgroundAppSelection(packageName: String) {
        _uiState.update { current ->
            val updated = current.backgroundApps.map { item ->
                if (item.packageName == packageName && !item.isWhitelisted) {
                    item.copy(isSelected = !item.isSelected)
                } else {
                    item
                }
            }
            current.copy(backgroundApps = updated)
        }
    }

    fun optimizeSelectedRam() {
        val selected = _uiState.value.backgroundApps.filter { it.isSelected && !it.isWhitelisted }
        if (selected.isEmpty()) {
            _uiState.update { it.copy(infoBannerMessage = "Selecione pelo menos um aplicativo em segundo plano.") }
            return
        }
        viewModelScope.launch {
            _uiState.update { it.copy(isOptimizingRam = true) }
            val report = ramMonitor.optimizeSelectedBackgroundApps(selected)
            prefs.saveReport(report)
            refreshSystemSnapshot()
            scanBackgroundAppsInternal()
            _uiState.update {
                it.copy(
                    isOptimizingRam = false,
                    activeReport = report,
                    history = prefs.getHistory()
                )
            }
        }
    }

    fun setAppManagerFilter(filter: AppManagerFilter) {
        _uiState.update { it.copy(appManagerFilter = filter) }
    }

    fun openReport(report: CleanReport) {
        _uiState.update { it.copy(activeReport = report) }
    }

    fun dismissReport() {
        _uiState.update { it.copy(activeReport = null) }
    }

    fun clearHistory() {
        prefs.clearHistory()
        _uiState.update { it.copy(history = emptyList()) }
    }

    fun clearBanner() {
        _uiState.update { it.copy(infoBannerMessage = null) }
    }
}
