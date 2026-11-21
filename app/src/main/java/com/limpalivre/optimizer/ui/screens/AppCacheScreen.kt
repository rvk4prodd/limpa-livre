package com.limpalivre.optimizer.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
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
import androidx.compose.material.icons.rounded.CleaningServices
import androidx.compose.material.icons.rounded.ExpandLess
import androidx.compose.material.icons.rounded.ExpandMore
import androidx.compose.material.icons.rounded.Lock
import androidx.compose.material.icons.rounded.LockOpen
import androidx.compose.material.icons.rounded.Refresh
import androidx.compose.material.icons.rounded.Search
import androidx.compose.material.icons.rounded.Settings
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CheckboxDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.limpalivre.optimizer.data.model.AppStorageItem
import com.limpalivre.optimizer.domain.ByteFormatter
import com.limpalivre.optimizer.service.AutomationState
import com.limpalivre.optimizer.ui.components.AppIconImage
import com.limpalivre.optimizer.ui.components.ExplanationBlock
import com.limpalivre.optimizer.ui.components.RiskBadge
import com.limpalivre.optimizer.ui.state.AppSortMode
import com.limpalivre.optimizer.ui.state.MainUiState
import com.limpalivre.optimizer.ui.theme.AmberWarning
import com.limpalivre.optimizer.ui.theme.BgCard
import com.limpalivre.optimizer.ui.theme.BgCardElevated
import com.limpalivre.optimizer.ui.theme.BorderSubtle
import com.limpalivre.optimizer.ui.theme.CyanAccent
import com.limpalivre.optimizer.ui.theme.EmeraldPrimary
import com.limpalivre.optimizer.ui.theme.EmeraldSoft
import com.limpalivre.optimizer.ui.theme.TextPrimary
import com.limpalivre.optimizer.ui.theme.TextSecondary

@Composable
fun AppCacheScreen(
    state: MainUiState,
    automationState: AutomationState,
    onGrantUsagePermission: () -> Unit,
    onRefreshScan: () -> Unit,
    onSearchChange: (String) -> Unit,
    onSortModeChange: (AppSortMode) -> Unit,
    onToggleHideZero: () -> Unit,
    onToggleSelectApp: (String) -> Unit,
    onSelectAll: (Boolean) -> Unit,
    onToggleWhitelist: (String) -> Unit,
    onOpenAppSettings: (String) -> Unit,
    onStartCleanSelected: () -> Unit,
    onAdvanceManualGuided: () -> Unit,
    onSkipManualGuided: () -> Unit,
    onFinishManualGuided: () -> Unit,
    modifier: Modifier = Modifier
) {
    val filteredApps = remember(
        state.appCaches,
        state.appSearchQuery,
        state.appSortMode,
        state.hideZeroCacheApps
    ) {
        state.appCaches
            .filter { app ->
                val matchesSearch = state.appSearchQuery.isBlank() ||
                    app.appName.contains(state.appSearchQuery, ignoreCase = true) ||
                    app.packageName.contains(state.appSearchQuery, ignoreCase = true)
                val matchesZero = !state.hideZeroCacheApps || app.cacheBytes > 64L * 1024L
                matchesSearch && matchesZero
            }
            .let { list ->
                when (state.appSortMode) {
                    AppSortMode.CACHE_DESC -> list.sortedByDescending { it.cacheBytes }
                    AppSortMode.TOTAL_DESC -> list.sortedByDescending { it.totalBytes }
                    AppSortMode.NAME_ASC -> list.sortedBy { it.appName.lowercase() }
                }
            }
    }

    Column(modifier = modifier.fillMaxSize()) {
        if (!state.permissions.usageStatsGranted) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(20.dp),
                contentAlignment = Alignment.Center
            ) {
                Card(
                    colors = CardDefaults.cardColors(containerColor = BgCard),
                    border = BorderStroke(1.dp, BorderSubtle),
                    shape = RoundedCornerShape(18.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(20.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = "Permissão de Uso Necessária",
                            style = MaterialTheme.typography.titleMedium,
                            color = TextPrimary
                        )
                        Text(
                            text = "Para descobrir quanto espaço de cache cada aplicativo está consumindo no seu celular, o Android exige a permissão 'Acesso ao uso'.",
                            style = MaterialTheme.typography.bodySmall,
                            color = TextSecondary
                        )
                        Button(
                            onClick = onGrantUsagePermission,
                            colors = ButtonDefaults.buttonColors(containerColor = EmeraldPrimary),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text("Permitir Acesso ao Uso", color = Color(0xFF042F22), fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
            return
        }

        if (automationState.isManualGuidedMode && automationState.currentApp != null) {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                colors = CardDefaults.cardColors(containerColor = Color(0xFF0D2D2B)),
                border = BorderStroke(1.dp, EmeraldPrimary),
                shape = RoundedCornerShape(16.dp)
            ) {
                val current = automationState.currentApp!!
                Column(
                    modifier = Modifier.padding(14.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text = "Modo Guiado (${automationState.currentIndex + 1}/${automationState.queue.size}): ${current.appName}",
                        style = MaterialTheme.typography.titleMedium,
                        color = EmeraldSoft
                    )
                    Text(
                        text = "Toque em Armazenamento → Limpar cache na tela que abriu. Quando voltar para cá, avance para o próximo app.",
                        style = MaterialTheme.typography.bodySmall,
                        color = TextPrimary
                    )
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Button(
                            onClick = onAdvanceManualGuided,
                            colors = ButtonDefaults.buttonColors(containerColor = EmeraldPrimary),
                            modifier = Modifier.weight(1f)
                        ) {
                            Text("Limpei • Próximo", color = Color(0xFF042F22), fontWeight = FontWeight.Bold)
                        }
                        OutlinedButton(onClick = onSkipManualGuided) {
                            Text("Pular")
                        }
                        TextButton(onClick = onFinishManualGuided) {
                            Text("Concluir", color = AmberWarning)
                        }
                    }
                }
            }
        }

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 10.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "Cache dos Aplicativos",
                        style = MaterialTheme.typography.headlineSmall,
                        color = TextPrimary
                    )
                    Text(
                        text = "Total detectado: ${ByteFormatter.format(state.totalAppCacheBytes)} • Toque em qualquer app para ver detalhes",
                        style = MaterialTheme.typography.bodySmall,
                        color = TextSecondary
                    )
                }
                IconButton(onClick = onRefreshScan, enabled = !state.isScanningApps) {
                    if (state.isScanningApps) {
                        CircularProgressIndicator(modifier = Modifier.size(20.dp), strokeWidth = 2.dp)
                    } else {
                        Icon(Icons.Rounded.Refresh, contentDescription = "Atualizar", tint = EmeraldSoft)
                    }
                }
            }

            OutlinedTextField(
                value = state.appSearchQuery,
                onValueChange = onSearchChange,
                placeholder = { Text("Buscar aplicativo (ex: WhatsApp, Instagram...)") },
                leadingIcon = { Icon(Icons.Rounded.Search, contentDescription = null) },
                singleLine = true,
                shape = RoundedCornerShape(14.dp),
                modifier = Modifier.fillMaxWidth()
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                AppSortMode.entries.forEach { mode ->
                    FilterChip(
                        selected = state.appSortMode == mode,
                        onClick = { onSortModeChange(mode) },
                        label = { Text(mode.label, style = MaterialTheme.typography.labelMedium) }
                    )
                }
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                FilterChip(
                    selected = !state.hideZeroCacheApps,
                    onClick = onToggleHideZero,
                    label = { Text(if (state.hideZeroCacheApps) "Mostrar zerados" else "Ocultando zerados") }
                )
                Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                    TextButton(onClick = { onSelectAll(true) }) {
                        Text("Marcar todos")
                    }
                    TextButton(onClick = { onSelectAll(false) }) {
                        Text("Desmarcar")
                    }
                }
            }
        }

        LazyColumn(
            modifier = Modifier.weight(1f),
            contentPadding = PaddingValues(start = 16.dp, end = 16.dp, bottom = 16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            items(filteredApps, key = { it.packageName }) { app ->
                AppCacheCard(
                    app = app,
                    onToggleSelect = { onToggleSelectApp(app.packageName) },
                    onToggleWhitelist = { onToggleWhitelist(app.packageName) },
                    onOpenSettings = { onOpenAppSettings(app.packageName) }
                )
            }
        }

        Surface(
            color = BgCard,
            tonalElevation = 6.dp,
            border = BorderStroke(1.dp, BorderSubtle)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 12.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column {
                    Text(
                        text = "${state.selectedAppCount} app(s) selecionado(s)",
                        style = MaterialTheme.typography.labelMedium,
                        color = TextPrimary
                    )
                    Text(
                        text = ByteFormatter.format(state.selectedAppCacheBytes),
                        style = MaterialTheme.typography.titleMedium,
                        color = EmeraldSoft,
                        fontWeight = FontWeight.Bold
                    )
                }
                Button(
                    onClick = onStartCleanSelected,
                    enabled = state.selectedAppCount > 0,
                    colors = ButtonDefaults.buttonColors(containerColor = EmeraldPrimary),
                    shape = RoundedCornerShape(14.dp)
                ) {
                    Icon(
                        imageVector = Icons.Rounded.CleaningServices,
                        contentDescription = null,
                        tint = Color(0xFF042F22)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Limpar Selecionados",
                        color = Color(0xFF042F22),
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}

@Composable
private fun AppCacheCard(
    app: AppStorageItem,
    onToggleSelect: () -> Unit,
    onToggleWhitelist: () -> Unit,
    onOpenSettings: () -> Unit
) {
    var expanded by remember(app.packageName) { mutableStateOf(false) }

    Card(
        colors = CardDefaults.cardColors(containerColor = BgCard),
        border = BorderStroke(
            width = 1.dp,
            color = if (app.isSelected && !app.isWhitelisted) EmeraldPrimary.copy(alpha = 0.55f) else BorderSubtle
        ),
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { expanded = !expanded },
                verticalAlignment = Alignment.CenterVertically
            ) {
                Checkbox(
                    checked = app.isSelected && !app.isWhitelisted,
                    enabled = !app.isWhitelisted,
                    onCheckedChange = { onToggleSelect() },
                    colors = CheckboxDefaults.colors(checkedColor = EmeraldPrimary)
                )
                AppIconImage(packageName = app.packageName, contentDescription = app.appName)
                Spacer(modifier = Modifier.width(12.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = app.appName,
                            style = MaterialTheme.typography.titleMedium,
                            color = TextPrimary,
                            modifier = Modifier.weight(1f, fill = false)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        if (app.isWhitelisted) {
                            Surface(
                                color = BgCardElevated,
                                shape = RoundedCornerShape(999.dp)
                            ) {
                                Text(
                                    text = "Protegido",
                                    style = MaterialTheme.typography.labelMedium,
                                    color = CyanAccent,
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
                                )
                            }
                        } else {
                            RiskBadge(riskLevel = app.explanation.riskLevel)
                        }
                    }
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = "${app.category.label} • Cache: ${ByteFormatter.format(app.cacheBytes)}",
                        style = MaterialTheme.typography.bodySmall,
                        color = EmeraldSoft,
                        fontWeight = FontWeight.SemiBold
                    )
                }
                IconButton(onClick = { expanded = !expanded }) {
                    Icon(
                        imageVector = if (expanded) Icons.Rounded.ExpandLess else Icons.Rounded.ExpandMore,
                        contentDescription = "Ver explicação",
                        tint = TextSecondary
                    )
                }
            }

            AnimatedVisibility(visible = expanded) {
                Column(
                    modifier = Modifier.padding(top = 10.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    ExplanationBlock(
                        whatItDoesTitle = "O que fica no cache de ${app.appName}",
                        whatItDoesText = app.explanation.whatIsInCache,
                        problemText = app.explanation.willItCauseProblems,
                        riskLevel = app.explanation.riskLevel
                    )
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        OutlinedButton(
                            onClick = onToggleWhitelist,
                            modifier = Modifier.weight(1f)
                        ) {
                            Icon(
                                imageVector = if (app.isWhitelisted) Icons.Rounded.LockOpen else Icons.Rounded.Lock,
                                contentDescription = null,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(if (app.isWhitelisted) "Remover Proteção" else "Proteger App")
                        }
                        OutlinedButton(
                            onClick = onOpenSettings,
                            modifier = Modifier.weight(1f)
                        ) {
                            Icon(
                                imageVector = Icons.Rounded.Settings,
                                contentDescription = null,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Abrir Config")
                        }
                    }
                }
            }
        }
    }
}
