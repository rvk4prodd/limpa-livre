package com.limpalivre.optimizer.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.AutoAwesome
import androidx.compose.material.icons.rounded.BatteryChargingFull
import androidx.compose.material.icons.rounded.CleaningServices
import androidx.compose.material.icons.rounded.FolderDelete
import androidx.compose.material.icons.rounded.Memory
import androidx.compose.material.icons.rounded.Security
import androidx.compose.material.icons.rounded.Speed
import androidx.compose.material.icons.rounded.VerifiedUser
import androidx.compose.material.icons.rounded.WarningAmber
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.limpalivre.optimizer.domain.ByteFormatter
import com.limpalivre.optimizer.ui.components.AnimatedStorageGauge
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
fun DashboardScreen(
    state: MainUiState,
    onRunFullAnalysis: () -> Unit,
    onNavigateApps: () -> Unit,
    onNavigateDeepClean: () -> Unit,
    onNavigateOptimize: () -> Unit,
    onNavigateHub: () -> Unit,
    modifier: Modifier = Modifier
) {
    val storagePercent = (state.storageSummary.usedFraction * 100f).toInt()
    val ramPercent = (state.ramSummary.usedFraction * 100f).toInt()

    LazyColumn(
        modifier = modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "LimpaLivre",
                        style = MaterialTheme.typography.headlineLarge,
                        color = TextPrimary
                    )
                    Text(
                        text = "Otimizador completo • 100% gratuito e offline",
                        style = MaterialTheme.typography.bodySmall,
                        color = EmeraldSoft
                    )
                }
                Surface(
                    color = Color(0xFF064E3B),
                    shape = RoundedCornerShape(999.dp)
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.VerifiedUser,
                            contentDescription = null,
                            tint = EmeraldSoft,
                            modifier = Modifier.size(14.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "Sem Paywall",
                            style = MaterialTheme.typography.labelMedium,
                            color = EmeraldSoft
                        )
                    }
                }
            }
        }

        if (!state.permissions.usageStatsGranted || !state.permissions.allFilesGranted) {
            item {
                Card(
                    colors = CardDefaults.cardColors(containerColor = Color(0xFF281E0E)),
                    border = BorderStroke(1.dp, AmberWarning.copy(alpha = 0.5f)),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(14.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.WarningAmber,
                            contentDescription = null,
                            tint = AmberWarning,
                            modifier = Modifier.size(26.dp)
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "Permissões recomendadas pendentes",
                                style = MaterialTheme.typography.titleMedium,
                                color = TextPrimary
                            )
                            Text(
                                text = "Conceda acesso ao uso e arquivos para ler o tamanho real do cache e fazer limpeza profunda.",
                                style = MaterialTheme.typography.bodySmall,
                                color = TextSecondary
                            )
                        }
                        Spacer(modifier = Modifier.width(8.dp))
                        OutlinedButton(onClick = onNavigateHub) {
                            Text("Configurar")
                        }
                    }
                }
            }
        }

        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = BgCard),
                border = BorderStroke(1.dp, BorderSubtle),
                shape = RoundedCornerShape(20.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(18.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceEvenly
                    ) {
                        AnimatedStorageGauge(
                            usedFraction = state.storageSummary.usedFraction,
                            centerTitle = "$storagePercent%",
                            centerSubtitle = "Armazenamento"
                        )
                        Column(
                            verticalArrangement = Arrangement.spacedBy(8.dp),
                            modifier = Modifier.padding(start = 12.dp)
                        ) {
                            MetricMiniRow(
                                label = "Livre",
                                value = ByteFormatter.format(state.storageSummary.freeBytes),
                                accent = EmeraldSoft
                            )
                            MetricMiniRow(
                                label = "Usado",
                                value = ByteFormatter.format(state.storageSummary.usedBytes),
                                accent = TextPrimary
                            )
                            MetricMiniRow(
                                label = "Total",
                                value = ByteFormatter.format(state.storageSummary.totalBytes),
                                accent = TextSecondary
                            )
                        }
                    }

                    Surface(
                        color = BgCardElevated,
                        shape = RoundedCornerShape(14.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(
                            modifier = Modifier.padding(12.dp),
                            verticalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(
                                    text = "Memória RAM ($ramPercent% em uso)",
                                    style = MaterialTheme.typography.labelMedium,
                                    color = TextPrimary
                                )
                                Text(
                                    text = "${ByteFormatter.format(state.ramSummary.availableBytes)} livre",
                                    style = MaterialTheme.typography.labelMedium,
                                    color = CyanAccent
                                )
                            }
                            LinearProgressIndicator(
                                progress = { state.ramSummary.usedFraction },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(8.dp),
                                color = CyanAccent,
                                trackColor = BorderSubtle
                            )
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(
                                        imageVector = Icons.Rounded.BatteryChargingFull,
                                        contentDescription = null,
                                        tint = EmeraldSoft,
                                        modifier = Modifier.size(14.dp)
                                    )
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text(
                                        text = "Bateria ${state.batteryInfo.levelPercent}% • ${state.batteryInfo.temperatureCelsius}°C",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = TextSecondary
                                    )
                                }
                                Text(
                                    text = "${state.deviceOverview.manufacturer} ${state.deviceOverview.model}",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = TextSecondary
                                )
                            }
                        }
                    }

                    Button(
                        onClick = onRunFullAnalysis,
                        enabled = !state.isRunningFullAnalysis && !state.isScanningApps && !state.isScanningDeep,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(50.dp),
                        shape = RoundedCornerShape(14.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = EmeraldPrimary)
                    ) {
                        if (state.isRunningFullAnalysis || state.isScanningApps || state.isScanningDeep) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(20.dp),
                                color = Color(0xFF042F22),
                                strokeWidth = 2.5.dp
                            )
                            Spacer(modifier = Modifier.width(10.dp))
                            Text(
                                text = "Analisando sistema...",
                                color = Color(0xFF042F22),
                                fontWeight = FontWeight.Bold
                            )
                        } else {
                            Icon(
                                imageVector = Icons.Rounded.AutoAwesome,
                                contentDescription = null,
                                tint = Color(0xFF042F22)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "Analisar Celular Agora",
                                color = Color(0xFF042F22),
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }
        }

        item {
            Text(
                text = "O que podemos otimizar",
                style = MaterialTheme.typography.titleMedium,
                color = TextPrimary
            )
        }

        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                QuickActionTile(
                    icon = Icons.Rounded.CleaningServices,
                    title = "Cache de Apps",
                    metric = if (state.appCaches.isNotEmpty()) {
                        ByteFormatter.format(state.totalAppCacheBytes)
                    } else if (state.permissions.usageStatsGranted) {
                        "Toque para analisar"
                    } else {
                        "Requer permissão"
                    },
                    subtitle = "${state.appCaches.size} apps listados • Explicações por app",
                    accent = EmeraldSoft,
                    onClick = onNavigateApps,
                    modifier = Modifier.weight(1f)
                )
                QuickActionTile(
                    icon = Icons.Rounded.FolderDelete,
                    title = "Limpeza Profunda",
                    metric = if (state.deepCategories.isNotEmpty()) {
                        ByteFormatter.format(state.totalDeepFoundBytes)
                    } else {
                        "Varredura real"
                    },
                    subtitle = "Temporários, miniaturas, duplicados e +",
                    accent = CyanAccent,
                    onClick = onNavigateDeepClean,
                    modifier = Modifier.weight(1f)
                )
            }
        }

        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                QuickActionTile(
                    icon = Icons.Rounded.Memory,
                    title = "RAM e Apps",
                    metric = "${ByteFormatter.format(state.ramSummary.availableBytes)} livre",
                    subtitle = "Processos, apps pesados e bateria",
                    accent = AmberWarning,
                    onClick = onNavigateOptimize,
                    modifier = Modifier.weight(1f)
                )
                QuickActionTile(
                    icon = Icons.Rounded.Security,
                    title = "Central & Saúde",
                    metric = "${state.permissions.coreGrantedCount}/3 ativas",
                    subtitle = "Permissões, histórico e lista protegida",
                    accent = EmeraldSoft,
                    onClick = onNavigateHub,
                    modifier = Modifier.weight(1f)
                )
            }
        }

        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = BgCard),
                border = BorderStroke(1.dp, BorderSubtle),
                shape = RoundedCornerShape(16.dp)
            ) {
                Row(
                    modifier = Modifier.padding(14.dp),
                    verticalAlignment = Alignment.Top
                ) {
                    Icon(
                        imageVector = Icons.Rounded.Speed,
                        contentDescription = null,
                        tint = CyanAccent,
                        modifier = Modifier.size(22.dp)
                    )
                    Spacer(modifier = Modifier.width(10.dp))
                    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        Text(
                            text = "Transparência total em cada limpeza",
                            style = MaterialTheme.typography.titleMedium,
                            color = TextPrimary
                        )
                        Text(
                            text = "Antes de apagar qualquer item, você vê exatamente o que aquele cache faz e se removê-lo pode causar algum efeito. Depois da limpeza, um diagnóstico confirma tudo que foi feito.",
                            style = MaterialTheme.typography.bodySmall,
                            color = TextSecondary
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun MetricMiniRow(label: String, value: String, accent: Color) {
    Column {
        Text(
            text = label,
            style = MaterialTheme.typography.bodySmall,
            color = TextSecondary
        )
        Text(
            text = value,
            style = MaterialTheme.typography.titleMedium,
            color = accent,
            fontWeight = FontWeight.Bold
        )
    }
}

@Composable
private fun QuickActionTile(
    icon: ImageVector,
    title: String,
    metric: String,
    subtitle: String,
    accent: Color,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        onClick = onClick,
        modifier = modifier,
        colors = CardDefaults.cardColors(containerColor = BgCard),
        border = BorderStroke(1.dp, BorderSubtle),
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(
            modifier = Modifier.padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Icon(
                imageVector = icon,
                contentDescription = title,
                tint = accent,
                modifier = Modifier.size(24.dp)
            )
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium,
                color = TextPrimary
            )
            Text(
                text = metric,
                style = MaterialTheme.typography.labelMedium,
                color = accent,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = subtitle,
                style = MaterialTheme.typography.bodySmall,
                color = TextSecondary
            )
        }
    }
}
