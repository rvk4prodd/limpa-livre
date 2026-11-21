package com.limpalivre.optimizer.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.BatteryChargingFull
import androidx.compose.material.icons.rounded.DeleteOutline
import androidx.compose.material.icons.rounded.Memory
import androidx.compose.material.icons.rounded.PhoneAndroid
import androidx.compose.material.icons.rounded.Refresh
import androidx.compose.material.icons.rounded.Settings
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CheckboxDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.limpalivre.optimizer.domain.ByteFormatter
import com.limpalivre.optimizer.ui.components.AppIconImage
import com.limpalivre.optimizer.ui.state.AppManagerFilter
import com.limpalivre.optimizer.ui.state.MainUiState
import com.limpalivre.optimizer.ui.theme.AmberWarning
import com.limpalivre.optimizer.ui.theme.BgCard
import com.limpalivre.optimizer.ui.theme.BgCardElevated
import com.limpalivre.optimizer.ui.theme.BorderSubtle
import com.limpalivre.optimizer.ui.theme.CoralDanger
import com.limpalivre.optimizer.ui.theme.CyanAccent
import com.limpalivre.optimizer.ui.theme.EmeraldPrimary
import com.limpalivre.optimizer.ui.theme.EmeraldSoft
import com.limpalivre.optimizer.ui.theme.TextPrimary
import com.limpalivre.optimizer.ui.theme.TextSecondary

@Composable
fun OptimizeScreen(
    state: MainUiState,
    onRefreshRam: () -> Unit,
    onToggleBgApp: (String) -> Unit,
    onOptimizeRam: () -> Unit,
    onChangeAppFilter: (AppManagerFilter) -> Unit,
    onOpenAppSettings: (String) -> Unit,
    onUninstallApp: (String) -> Unit,
    onOpenBatterySettings: () -> Unit,
    modifier: Modifier = Modifier
) {
    var selectedTab by remember { mutableIntStateOf(0) }
    val tabs = listOf("RAM", "Gerenciador", "Bateria")

    Column(modifier = modifier.fillMaxSize()) {
        TabRow(
            selectedTabIndex = selectedTab,
            containerColor = BgCard,
            contentColor = EmeraldSoft
        ) {
            tabs.forEachIndexed { index, title ->
                Tab(
                    selected = selectedTab == index,
                    onClick = { selectedTab = index },
                    text = {
                        Text(
                            text = title,
                            fontWeight = if (selectedTab == index) FontWeight.Bold else FontWeight.Normal
                        )
                    }
                )
            }
        }

        when (selectedTab) {
            0 -> RamTab(
                state = state,
                onRefreshRam = onRefreshRam,
                onToggleBgApp = onToggleBgApp,
                onOptimizeRam = onOptimizeRam
            )
            1 -> AppManagerTab(
                state = state,
                onChangeAppFilter = onChangeAppFilter,
                onOpenAppSettings = onOpenAppSettings,
                onUninstallApp = onUninstallApp
            )
            else -> BatteryHealthTab(
                state = state,
                onOpenBatterySettings = onOpenBatterySettings
            )
        }
    }
}

@Composable
private fun RamTab(
    state: MainUiState,
    onRefreshRam: () -> Unit,
    onToggleBgApp: (String) -> Unit,
    onOptimizeRam: () -> Unit
) {
    Column(modifier = Modifier.fillMaxSize()) {
        LazyColumn(
            modifier = Modifier.weight(1f),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            item {
                Card(
                    colors = CardDefaults.cardColors(containerColor = BgCard),
                    border = BorderStroke(1.dp, BorderSubtle),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(14.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Rounded.Memory, contentDescription = null, tint = CyanAccent)
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = "Memória RAM em tempo real",
                                    style = MaterialTheme.typography.titleMedium,
                                    color = TextPrimary
                                )
                            }
                            IconButton(onClick = onRefreshRam) {
                                Icon(Icons.Rounded.Refresh, contentDescription = "Atualizar RAM", tint = EmeraldSoft)
                            }
                        }
                        LinearProgressIndicator(
                            progress = { state.ramSummary.usedFraction },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(10.dp),
                            color = CyanAccent,
                            trackColor = BorderSubtle
                        )
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = "Em uso: ${ByteFormatter.format(state.ramSummary.usedBytes)}",
                                style = MaterialTheme.typography.bodySmall,
                                color = TextSecondary
                            )
                            Text(
                                text = "Livre: ${ByteFormatter.format(state.ramSummary.availableBytes)} de ${ByteFormatter.format(state.ramSummary.totalBytes)}",
                                style = MaterialTheme.typography.bodySmall,
                                color = EmeraldSoft
                            )
                        }
                        Text(
                            text = "O que faz: encerra apenas processos ociosos em segundo plano dos apps escolhidos. Não apaga conversas, fotos ou logins.",
                            style = MaterialTheme.typography.bodySmall,
                            color = TextSecondary
                        )
                    }
                }
            }

            if (state.backgroundApps.isEmpty()) {
                item {
                    Card(
                        colors = CardDefaults.cardColors(containerColor = BgCard),
                        shape = RoundedCornerShape(16.dp)
                    ) {
                        Text(
                            text = "Nenhum aplicativo recente em segundo plano encontrado (ou Acesso ao Uso pendente).",
                            style = MaterialTheme.typography.bodySmall,
                            color = TextSecondary,
                            modifier = Modifier.padding(16.dp)
                        )
                    }
                }
            } else {
                items(state.backgroundApps, key = { it.packageName }) { item ->
                    Card(
                        colors = CardDefaults.cardColors(containerColor = BgCard),
                        border = BorderStroke(1.dp, BorderSubtle),
                        shape = RoundedCornerShape(14.dp)
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(10.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Checkbox(
                                checked = item.isSelected && !item.isWhitelisted,
                                enabled = !item.isWhitelisted,
                                onCheckedChange = { onToggleBgApp(item.packageName) },
                                colors = CheckboxDefaults.colors(checkedColor = EmeraldPrimary)
                            )
                            AppIconImage(packageName = item.packageName, contentDescription = item.appName, size = 38.dp)
                            Spacer(modifier = Modifier.width(10.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Text(item.appName, style = MaterialTheme.typography.titleMedium, color = TextPrimary)
                                Text(
                                    text = when {
                                        item.isWhitelisted -> "Protegido na lista segura"
                                        item.isActiveProcess -> "Processo ativo em segundo plano"
                                        else -> "Executado recentemente"
                                    },
                                    style = MaterialTheme.typography.bodySmall,
                                    color = if (item.isActiveProcess) AmberWarning else TextSecondary
                                )
                            }
                        }
                    }
                }
            }
        }

        Surface(color = BgCard, border = BorderStroke(1.dp, BorderSubtle)) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 12.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "${state.selectedBackgroundAppsCount} app(s) selecionado(s)",
                    style = MaterialTheme.typography.labelMedium,
                    color = TextPrimary
                )
                Button(
                    onClick = onOptimizeRam,
                    enabled = state.selectedBackgroundAppsCount > 0 && !state.isOptimizingRam,
                    colors = ButtonDefaults.buttonColors(containerColor = EmeraldPrimary)
                ) {
                    Text(
                        text = if (state.isOptimizingRam) "Otimizando..." else "Otimizar RAM",
                        color = Color(0xFF042F22),
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun AppManagerTab(
    state: MainUiState,
    onChangeAppFilter: (AppManagerFilter) -> Unit,
    onOpenAppSettings: (String) -> Unit,
    onUninstallApp: (String) -> Unit
) {
    val thirtyDaysAgo = remember { System.currentTimeMillis() - 30L * 24L * 60L * 60L * 1000L }
    val filtered = remember(state.appCaches, state.appManagerFilter) {
        when (state.appManagerFilter) {
            AppManagerFilter.ALL -> state.appCaches.sortedBy { it.appName.lowercase() }
            AppManagerFilter.HEAVIEST -> state.appCaches.sortedByDescending { it.totalBytes }
            AppManagerFilter.UNUSED_30D -> state.appCaches
                .filter { it.lastTimeUsed in 1L until thirtyDaysAgo }
                .sortedBy { it.lastTimeUsed }
            AppManagerFilter.SENSITIVE_PERMS -> state.appCaches
                .filter { it.sensitivePermissions.isNotEmpty() }
                .sortedByDescending { it.sensitivePermissions.size }
        }
    }

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        item {
            FlowRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                AppManagerFilter.entries.forEach { filter ->
                    FilterChip(
                        selected = state.appManagerFilter == filter,
                        onClick = { onChangeAppFilter(filter) },
                        label = { Text(filter.label) }
                    )
                }
            }
        }

        items(filtered, key = { it.packageName }) { app ->
            Card(
                colors = CardDefaults.cardColors(containerColor = BgCard),
                border = BorderStroke(1.dp, BorderSubtle),
                shape = RoundedCornerShape(14.dp)
            ) {
                Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        AppIconImage(packageName = app.packageName, contentDescription = app.appName)
                        Spacer(modifier = Modifier.width(12.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(app.appName, style = MaterialTheme.typography.titleMedium, color = TextPrimary)
                            Text(
                                text = "Total: ${ByteFormatter.format(app.totalBytes)} (App ${ByteFormatter.format(app.appBytes)} + Dados ${ByteFormatter.format(app.dataBytes)} + Cache ${ByteFormatter.format(app.cacheBytes)})",
                                style = MaterialTheme.typography.bodySmall,
                                color = TextSecondary
                            )
                        }
                    }

                    if (app.sensitivePermissions.isNotEmpty()) {
                        FlowRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                            app.sensitivePermissions.forEach { perm ->
                                Surface(color = BgCardElevated, shape = RoundedCornerShape(999.dp)) {
                                    Text(
                                        text = perm,
                                        style = MaterialTheme.typography.labelMedium,
                                        color = AmberWarning,
                                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
                                    )
                                }
                            }
                        }
                    }

                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        OutlinedButton(
                            onClick = { onOpenAppSettings(app.packageName) },
                            modifier = Modifier.weight(1f)
                        ) {
                            Icon(Icons.Rounded.Settings, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Configurar")
                        }
                        if (!app.isSystemApp) {
                            OutlinedButton(
                                onClick = { onUninstallApp(app.packageName) },
                                modifier = Modifier.weight(1f)
                            ) {
                                Icon(Icons.Rounded.DeleteOutline, contentDescription = null, tint = CoralDanger, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("Desinstalar", color = CoralDanger)
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun BatteryHealthTab(
    state: MainUiState,
    onOpenBatterySettings: () -> Unit
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = BgCard),
                border = BorderStroke(1.dp, BorderSubtle),
                shape = RoundedCornerShape(16.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Rounded.BatteryChargingFull, contentDescription = null, tint = EmeraldSoft, modifier = Modifier.size(26.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Diagnóstico da Bateria", style = MaterialTheme.typography.titleMedium, color = TextPrimary)
                    }
                    InfoRow("Carga atual", "${state.batteryInfo.levelPercent}% (${if (state.batteryInfo.isCharging) "Carregando" else "Descarregando"})")
                    InfoRow("Fonte de energia", state.batteryInfo.chargeSource)
                    InfoRow("Temperatura", "${state.batteryInfo.temperatureCelsius} °C")
                    InfoRow("Saúde reportada", state.batteryInfo.healthLabel)
                    InfoRow("Voltagem", "${state.batteryInfo.voltageMv} mV")
                    InfoRow("Tecnologia", state.batteryInfo.technology)

                    Button(
                        onClick = onOpenBatterySettings,
                        colors = ButtonDefaults.buttonColors(containerColor = EmeraldPrimary),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("Abrir Consumo de Bateria do Sistema", color = Color(0xFF042F22), fontWeight = FontWeight.Bold)
                    }
                }
            }
        }

        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = BgCard),
                border = BorderStroke(1.dp, BorderSubtle),
                shape = RoundedCornerShape(16.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Rounded.PhoneAndroid, contentDescription = null, tint = CyanAccent, modifier = Modifier.size(24.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Especificações do Aparelho", style = MaterialTheme.typography.titleMedium, color = TextPrimary)
                    }
                    InfoRow("Dispositivo", "${state.deviceOverview.manufacturer} ${state.deviceOverview.model}")
                    InfoRow("Android", "${state.deviceOverview.androidVersion} (API ${state.deviceOverview.apiLevel})")
                    InfoRow("Núcleos de CPU", "${state.deviceOverview.cpuCores} núcleos")
                    InfoRow("Arquitetura", state.deviceOverview.primaryAbi)
                }
            }
        }
    }
}

@Composable
private fun InfoRow(label: String, value: String) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(label, style = MaterialTheme.typography.bodySmall, color = TextSecondary)
        Text(value, style = MaterialTheme.typography.bodySmall, color = TextPrimary, fontWeight = FontWeight.SemiBold)
    }
}
