package com.limpalivre.optimizer

import android.Manifest
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Apps
import androidx.compose.material.icons.rounded.Dashboard
import androidx.compose.material.icons.rounded.FolderDelete
import androidx.compose.material.icons.rounded.Security
import androidx.compose.material.icons.rounded.Speed
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.limpalivre.optimizer.domain.PermissionsHelper
import com.limpalivre.optimizer.ui.components.AccessibilityDisclosureDialog
import com.limpalivre.optimizer.ui.components.CleanReportDialog
import com.limpalivre.optimizer.ui.screens.AppCacheScreen
import com.limpalivre.optimizer.ui.screens.DashboardScreen
import com.limpalivre.optimizer.ui.screens.DeepCleanScreen
import com.limpalivre.optimizer.ui.screens.HubScreen
import com.limpalivre.optimizer.ui.screens.OptimizeScreen
import com.limpalivre.optimizer.ui.state.MainViewModel
import com.limpalivre.optimizer.ui.theme.BgCard
import com.limpalivre.optimizer.ui.theme.BgPrimary
import com.limpalivre.optimizer.ui.theme.EmeraldSoft
import com.limpalivre.optimizer.ui.theme.LimpaLivreTheme
import com.limpalivre.optimizer.ui.theme.TextSecondary

private data class NavTab(
    val label: String,
    val icon: ImageVector
)

class MainActivity : ComponentActivity() {

    private val viewModel: MainViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            LimpaLivreTheme {
                LimpaLivreRoot(viewModel = viewModel)
            }
        }
    }
}

@Composable
private fun LimpaLivreRoot(viewModel: MainViewModel) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val automationState by viewModel.automationState.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }

    var selectedTab by rememberSaveable { mutableIntStateOf(0) }

    val notificationPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) {
        viewModel.refreshSystemSnapshot()
    }

    val legacyStoragePermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestMultiplePermissions()
    ) {
        viewModel.refreshSystemSnapshot()
    }

    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_RESUME) {
                viewModel.onHostResume()
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)
        }
    }

    LaunchedEffect(uiState.infoBannerMessage) {
        val msg = uiState.infoBannerMessage
        if (!msg.isNullOrBlank()) {
            snackbarHostState.showSnackbar(msg)
            viewModel.clearBanner()
        }
    }

    val requestStoragePermission = {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            PermissionsHelper.openAllFilesAccessSettings(context)
        } else {
            legacyStoragePermissionLauncher.launch(
                arrayOf(
                    Manifest.permission.READ_EXTERNAL_STORAGE,
                    Manifest.permission.WRITE_EXTERNAL_STORAGE
                )
            )
        }
    }

    val requestNotificationPermission = {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            notificationPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
        } else {
            PermissionsHelper.openAppInfoSettings(context, context.packageName)
        }
    }

    val navTabs = listOf(
        NavTab("Início", Icons.Rounded.Dashboard),
        NavTab("Apps", Icons.Rounded.Apps),
        NavTab("Profunda", Icons.Rounded.FolderDelete),
        NavTab("Otimizar", Icons.Rounded.Speed),
        NavTab("Central", Icons.Rounded.Security)
    )

    Scaffold(
        containerColor = BgPrimary,
        snackbarHost = { SnackbarHost(snackbarHostState) },
        bottomBar = {
            NavigationBar(containerColor = BgCard) {
                navTabs.forEachIndexed { index, tab ->
                    NavigationBarItem(
                        selected = selectedTab == index,
                        onClick = { selectedTab = index },
                        icon = { Icon(tab.icon, contentDescription = tab.label) },
                        label = { Text(tab.label) },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = EmeraldSoft,
                            selectedTextColor = EmeraldSoft,
                            unselectedIconColor = TextSecondary,
                            unselectedTextColor = TextSecondary
                        )
                    )
                }
            }
        }
    ) { innerPadding ->
        val contentModifier = Modifier.padding(innerPadding)
        when (selectedTab) {
            0 -> DashboardScreen(
                state = uiState,
                onRunFullAnalysis = { viewModel.runFullAnalysis() },
                onNavigateApps = { selectedTab = 1 },
                onNavigateDeepClean = { selectedTab = 2 },
                onNavigateOptimize = { selectedTab = 3 },
                onNavigateHub = { selectedTab = 4 },
                modifier = contentModifier
            )
            1 -> AppCacheScreen(
                state = uiState,
                automationState = automationState,
                onGrantUsagePermission = { PermissionsHelper.openUsageAccessSettings(context) },
                onRefreshScan = { viewModel.scanAppCaches() },
                onSearchChange = { viewModel.updateAppSearch(it) },
                onSortModeChange = { viewModel.updateAppSortMode(it) },
                onToggleHideZero = { viewModel.toggleHideZeroCacheApps() },
                onToggleSelectApp = { viewModel.toggleAppSelection(it) },
                onSelectAll = { viewModel.selectAllVisibleApps(it) },
                onToggleWhitelist = { viewModel.toggleWhitelist(it) },
                onOpenAppSettings = { viewModel.openAppInfo(it) },
                onStartCleanSelected = { viewModel.requestStartAppCacheClean() },
                onAdvanceManualGuided = { viewModel.advanceManualGuidedNext() },
                onSkipManualGuided = { viewModel.skipManualGuidedCurrent() },
                onFinishManualGuided = { viewModel.finishManualGuidedNow() },
                modifier = contentModifier
            )
            2 -> DeepCleanScreen(
                state = uiState,
                onGrantStoragePermission = requestStoragePermission,
                onStartScan = { viewModel.scanDeepStorage() },
                onToggleCategory = { viewModel.toggleDeepCategorySelection(it) },
                onToggleFileItem = { type, path -> viewModel.toggleDeepFileItem(type, path) },
                onConfirmClean = { viewModel.cleanSelectedDeepFiles() },
                modifier = contentModifier
            )
            3 -> OptimizeScreen(
                state = uiState,
                onRefreshRam = { viewModel.scanBackgroundApps() },
                onToggleBgApp = { viewModel.toggleBackgroundAppSelection(it) },
                onOptimizeRam = { viewModel.optimizeSelectedRam() },
                onChangeAppFilter = { viewModel.setAppManagerFilter(it) },
                onOpenAppSettings = { viewModel.openAppInfo(it) },
                onUninstallApp = { PermissionsHelper.openUninstallDialog(context, it) },
                onOpenBatterySettings = { PermissionsHelper.openBatterySettings(context) },
                modifier = contentModifier
            )
            4 -> HubScreen(
                state = uiState,
                onGrantUsageStats = { PermissionsHelper.openUsageAccessSettings(context) },
                onGrantAllFiles = requestStoragePermission,
                onRequestAccessibility = { PermissionsHelper.openAccessibilitySettings(context) },
                onGrantNotifications = requestNotificationPermission,
                onToggleWhitelist = { viewModel.toggleWhitelist(it) },
                onOpenReport = { viewModel.openReport(it) },
                onClearHistory = { viewModel.clearHistory() },
                modifier = contentModifier
            )
        }
    }

    uiState.activeReport?.let { report ->
        CleanReportDialog(
            report = report,
            onDismiss = { viewModel.dismissReport() }
        )
    }

    if (uiState.showAccessibilityDisclosure) {
        AccessibilityDisclosureDialog(
            onOpenAccessibilitySettings = {
                viewModel.dismissAccessibilityDisclosure()
                PermissionsHelper.openAccessibilitySettings(context)
            },
            onOpenOwnAppInfo = {
                PermissionsHelper.openAppInfoSettings(context, context.packageName)
            },
            onUseManualMode = {
                viewModel.startManualGuidedClean()
            },
            onDismiss = {
                viewModel.dismissAccessibilityDisclosure()
            }
        )
    }
}
