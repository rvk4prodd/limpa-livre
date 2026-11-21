package com.limpalivre.optimizer.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.CheckCircle
import androidx.compose.material.icons.rounded.DeleteSweep
import androidx.compose.material.icons.rounded.History
import androidx.compose.material.icons.rounded.Lock
import androidx.compose.material.icons.rounded.LockOpen
import androidx.compose.material.icons.rounded.RadioButtonUnchecked
import androidx.compose.material.icons.rounded.Shield
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.limpalivre.optimizer.data.model.CleanReport
import com.limpalivre.optimizer.domain.ByteFormatter
import com.limpalivre.optimizer.ui.state.MainUiState
import com.limpalivre.optimizer.ui.theme.AmberWarning
import com.limpalivre.optimizer.ui.theme.BgCard
import com.limpalivre.optimizer.ui.theme.BorderSubtle
import com.limpalivre.optimizer.ui.theme.CyanAccent
import com.limpalivre.optimizer.ui.theme.EmeraldSoft
import com.limpalivre.optimizer.ui.theme.TextPrimary
import com.limpalivre.optimizer.ui.theme.TextSecondary
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun HubScreen(
    state: MainUiState,
    onGrantUsageStats: () -> Unit,
    onGrantAllFiles: () -> Unit,
    onRequestAccessibility: () -> Unit,
    onGrantNotifications: () -> Unit,
    onToggleWhitelist: (String) -> Unit,
    onOpenReport: (CleanReport) -> Unit,
    onClearHistory: () -> Unit,
    modifier: Modifier = Modifier
) {
    val dateFormatter = remember { SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault()) }

    LazyColumn(
        modifier = modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        item {
            Text(
                text = "Central de Controle",
                style = MaterialTheme.typography.headlineSmall,
                color = TextPrimary
            )
            Text(
                text = "Permissões do sistema, lista protegida e histórico de limpezas",
                style = MaterialTheme.typography.bodySmall,
                color = TextSecondary
            )
        }

        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = BgCard),
                border = BorderStroke(1.dp, BorderSubtle),
                shape = RoundedCornerShape(16.dp)
            ) {
                Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text(
                        text = "Permissões do Sistema",
                        style = MaterialTheme.typography.titleMedium,
                        color = TextPrimary
                    )
                    PermissionItemRow(
                        title = "Acesso ao Uso (Cache de Apps)",
                        description = "Permite ler quanto cache e dados cada app ocupa de verdade.",
                        granted = state.permissions.usageStatsGranted,
                        onAction = onGrantUsageStats
                    )
                    PermissionItemRow(
                        title = "Acesso a Todos os Arquivos",
                        description = "Necessário para achar temporários, .thumbnails, duplicados e resíduos.",
                        granted = state.permissions.allFilesGranted,
                        onAction = onGrantAllFiles
                    )
                    PermissionItemRow(
                        title = "Acessibilidade (Opcional • 1 Toque)",
                        description = "Automatiza o clique em 'Limpar cache' de vários apps seguidos. Nunca toca em 'Limpar dados'.",
                        granted = state.permissions.accessibilityEnabled,
                        onAction = onRequestAccessibility
                    )
                    PermissionItemRow(
                        title = "Notificações (Opcional)",
                        description = "Exibe lembretes ou progresso se você usar notificações.",
                        granted = state.permissions.notificationsGranted,
                        onAction = onGrantNotifications
                    )
                }
            }
        }

        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = BgCard),
                border = BorderStroke(1.dp, EmeraldSoft.copy(alpha = 0.4f)),
                shape = RoundedCornerShape(16.dp)
            ) {
                Row(
                    modifier = Modifier.padding(14.dp),
                    verticalAlignment = Alignment.Top
                ) {
                    Icon(Icons.Rounded.Shield, contentDescription = null, tint = EmeraldSoft, modifier = Modifier.size(22.dp))
                    Spacer(modifier = Modifier.width(10.dp))
                    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        Text(
                            text = "100% Gratuito • Zero Internet • Sem Paywall",
                            style = MaterialTheme.typography.titleMedium,
                            color = EmeraldSoft
                        )
                        Text(
                            text = "O LimpaLivre não declara sequer permissão de acesso à internet no AndroidManifest. Nenhum dado do seu celular sai do aparelho, sem assinaturas, sem anúncios e sem bloqueios pagos.",
                            style = MaterialTheme.typography.bodySmall,
                            color = TextSecondary
                        )
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
                Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Rounded.Lock, contentDescription = null, tint = CyanAccent, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "Lista Protegida (${state.whitelist.size})",
                            style = MaterialTheme.typography.titleMedium,
                            color = TextPrimary
                        )
                    }
                    if (state.whitelist.isEmpty()) {
                        Text(
                            text = "Nenhum app protegido. Você pode proteger apps na aba 'Apps' para nunca serem incluídos em limpezas em lote.",
                            style = MaterialTheme.typography.bodySmall,
                            color = TextSecondary
                        )
                    } else {
                        state.whitelist.sorted().forEach { pkg ->
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(pkg, style = MaterialTheme.typography.bodySmall, color = TextPrimary)
                                IconButton(onClick = { onToggleWhitelist(pkg) }) {
                                    Icon(Icons.Rounded.LockOpen, contentDescription = "Remover proteção", tint = AmberWarning)
                                }
                            }
                        }
                    }
                }
            }
        }

        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Rounded.History, contentDescription = null, tint = CyanAccent, modifier = Modifier.size(20.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "Histórico de Limpezas (${state.history.size})",
                        style = MaterialTheme.typography.titleMedium,
                        color = TextPrimary
                    )
                }
                if (state.history.isNotEmpty()) {
                    TextButton(onClick = onClearHistory) {
                        Icon(Icons.Rounded.DeleteSweep, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Limpar")
                    }
                }
            }
        }

        if (state.history.isEmpty()) {
            item {
                Text(
                    text = "Suas limpezas aparecerão aqui com diagnóstico completo de tudo o que foi feito.",
                    style = MaterialTheme.typography.bodySmall,
                    color = TextSecondary
                )
            }
        } else {
            items(state.history, key = { it.id }) { report ->
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { onOpenReport(report) },
                    colors = CardDefaults.cardColors(containerColor = BgCard),
                    border = BorderStroke(1.dp, BorderSubtle),
                    shape = RoundedCornerShape(14.dp)
                ) {
                    Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(report.operationType, style = MaterialTheme.typography.titleMedium, color = TextPrimary)
                            Text(
                                text = if (report.totalBytesFreed > 0L) "+${ByteFormatter.format(report.totalBytesFreed)}" else "${report.itemsCleanedCount} item(ns)",
                                style = MaterialTheme.typography.labelMedium,
                                color = EmeraldSoft,
                                fontWeight = FontWeight.Bold
                            )
                        }
                        Text(
                            text = dateFormatter.format(Date(report.timestampMillis)),
                            style = MaterialTheme.typography.bodySmall,
                            color = TextSecondary
                        )
                        Text(
                            text = report.summaryDiagnosis,
                            style = MaterialTheme.typography.bodySmall,
                            color = TextSecondary,
                            maxLines = 2
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun PermissionItemRow(
    title: String,
    description: String,
    granted: Boolean,
    onAction: () -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = if (granted) Icons.Rounded.CheckCircle else Icons.Rounded.RadioButtonUnchecked,
            contentDescription = null,
            tint = if (granted) EmeraldSoft else AmberWarning,
            modifier = Modifier.size(22.dp)
        )
        Spacer(modifier = Modifier.width(10.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(title, style = MaterialTheme.typography.labelMedium, color = TextPrimary)
            Text(description, style = MaterialTheme.typography.bodySmall, color = TextSecondary)
        }
        Spacer(modifier = Modifier.width(8.dp))
        OutlinedButton(onClick = onAction) {
            Text(if (granted) "Ativa" else "Permitir")
        }
    }
}
