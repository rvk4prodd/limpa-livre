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
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ManageSearch
import androidx.compose.material.icons.rounded.DeleteSweep
import androidx.compose.material.icons.rounded.ExpandLess
import androidx.compose.material.icons.rounded.ExpandMore
import androidx.compose.material.icons.rounded.FolderOpen
import androidx.compose.material.icons.rounded.Lock
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CheckboxDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
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
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.limpalivre.optimizer.data.model.DeepCategoryType
import com.limpalivre.optimizer.data.model.DeepCleanCategory
import com.limpalivre.optimizer.domain.ByteFormatter
import com.limpalivre.optimizer.ui.components.ExplanationBlock
import com.limpalivre.optimizer.ui.components.RiskBadge
import com.limpalivre.optimizer.ui.state.MainUiState
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
fun DeepCleanScreen(
    state: MainUiState,
    onGrantStoragePermission: () -> Unit,
    onStartScan: () -> Unit,
    onToggleCategory: (DeepCategoryType) -> Unit,
    onToggleFileItem: (DeepCategoryType, String) -> Unit,
    onConfirmClean: () -> Unit,
    modifier: Modifier = Modifier
) {
    var inspectingCategoryType by remember { mutableStateOf<DeepCategoryType?>(null) }
    var showConfirmDeleteDialog by remember { mutableStateOf(false) }

    Column(modifier = modifier.fillMaxSize()) {
        if (!state.permissions.allFilesGranted) {
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
                            text = "Acesso aos Arquivos Necessário",
                            style = MaterialTheme.typography.titleMedium,
                            color = TextPrimary
                        )
                        Text(
                            text = "Para encontrar caches profundos (.thumbnails, resíduos do WhatsApp/Telegram, arquivos temporários, duplicados e instaladores .apk), autorize o acesso ao armazenamento.",
                            style = MaterialTheme.typography.bodySmall,
                            color = TextSecondary
                        )
                        Button(
                            onClick = onGrantStoragePermission,
                            colors = ButtonDefaults.buttonColors(containerColor = EmeraldPrimary),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text("Conceder Acesso aos Arquivos", color = Color(0xFF042F22), fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
            return
        }

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 12.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "Limpeza Profunda",
                        style = MaterialTheme.typography.headlineSmall,
                        color = TextPrimary
                    )
                    Text(
                        text = "Caches ocultos, resíduos, duplicados e arquivos grandes",
                        style = MaterialTheme.typography.bodySmall,
                        color = TextSecondary
                    )
                }
                OutlinedButton(
                    onClick = onStartScan,
                    enabled = !state.isScanningDeep && !state.isCleaningDeep
                ) {
                    Icon(Icons.AutoMirrored.Rounded.ManageSearch, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(if (state.deepCategories.isEmpty()) "Varrer" else "Reescanear")
                }
            }

            if (state.isScanningDeep) {
                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    LinearProgressIndicator(
                        modifier = Modifier.fillMaxWidth(),
                        color = CyanAccent,
                        trackColor = BorderSubtle
                    )
                    Text(
                        text = "Analisando: ${state.deepScanPathText}",
                        style = MaterialTheme.typography.bodySmall,
                        color = CyanAccent,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }
        }

        if (state.deepCategories.isEmpty() && !state.isScanningDeep) {
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .padding(24.dp),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Icon(
                        imageVector = Icons.Rounded.FolderOpen,
                        contentDescription = null,
                        tint = CyanAccent,
                        modifier = Modifier.size(48.dp)
                    )
                    Text(
                        text = "Toque em 'Varrer' para inspecionar seu armazenamento em busca de resíduos ocultos, duplicados e arquivos temporários.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = TextSecondary
                    )
                }
            }
        } else {
            LazyColumn(
                modifier = Modifier.weight(1f),
                contentPadding = PaddingValues(start = 16.dp, end = 16.dp, bottom = 16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(state.deepCategories, key = { it.type.name }) { category ->
                    DeepCategoryCard(
                        category = category,
                        onToggleCategory = { onToggleCategory(category.type) },
                        onInspectItems = { inspectingCategoryType = category.type }
                    )
                }
            }
        }

        Surface(
            color = BgCard,
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
                        text = "${state.selectedDeepCount} item(ns) marcado(s)",
                        style = MaterialTheme.typography.labelMedium,
                        color = TextPrimary
                    )
                    Text(
                        text = ByteFormatter.format(state.totalDeepRecoverableBytes),
                        style = MaterialTheme.typography.titleMedium,
                        color = EmeraldSoft,
                        fontWeight = FontWeight.Bold
                    )
                }
                Button(
                    onClick = { showConfirmDeleteDialog = true },
                    enabled = state.selectedDeepCount > 0 && !state.isCleaningDeep && !state.isScanningDeep,
                    colors = ButtonDefaults.buttonColors(containerColor = EmeraldPrimary),
                    shape = RoundedCornerShape(14.dp)
                ) {
                    if (state.isCleaningDeep) {
                        CircularProgressIndicator(modifier = Modifier.size(18.dp), color = Color(0xFF042F22), strokeWidth = 2.dp)
                        Spacer(modifier = Modifier.width(8.dp))
                    } else {
                        Icon(Icons.Rounded.DeleteSweep, contentDescription = null, tint = Color(0xFF042F22))
                        Spacer(modifier = Modifier.width(8.dp))
                    }
                    Text("Limpar Marcados", color = Color(0xFF042F22), fontWeight = FontWeight.Bold)
                }
            }
        }
    }

    val inspectingCategory = state.deepCategories.firstOrNull { it.type == inspectingCategoryType }
    if (inspectingCategory != null) {
        InspectDeepFilesDialog(
            category = inspectingCategory,
            onToggleItem = { path -> onToggleFileItem(inspectingCategory.type, path) },
            onDismiss = { inspectingCategoryType = null }
        )
    }

    if (showConfirmDeleteDialog) {
        AlertDialog(
            onDismissRequest = { showConfirmDeleteDialog = false },
            containerColor = BgCard,
            title = { Text("Confirmar Limpeza Profunda", color = TextPrimary) },
            text = {
                Text(
                    text = "Serão removidos ${state.selectedDeepCount} item(ns) selecionados (${ByteFormatter.format(state.totalDeepRecoverableBytes)}). Pastas protegidas do sistema e arquivos originais de duplicados continuam intactos.",
                    style = MaterialTheme.typography.bodySmall,
                    color = TextSecondary
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        showConfirmDeleteDialog = false
                        onConfirmClean()
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = CoralDanger)
                ) {
                    Text("Apagar Selecionados", color = Color.White, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showConfirmDeleteDialog = false }) {
                    Text("Cancelar", color = TextSecondary)
                }
            }
        )
    }
}

@Composable
private fun DeepCategoryCard(
    category: DeepCleanCategory,
    onToggleCategory: () -> Unit,
    onInspectItems: () -> Unit
) {
    var expanded by remember(category.type) { mutableStateOf(true) }
    val selectableItems = category.items.filter { !it.isProtectedOriginal }
    val allSelected = selectableItems.isNotEmpty() && selectableItems.all { it.isSelected }

    Card(
        colors = CardDefaults.cardColors(containerColor = BgCard),
        border = BorderStroke(
            1.dp,
            if (category.selectedCount > 0) EmeraldPrimary.copy(alpha = 0.5f) else BorderSubtle
        ),
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { expanded = !expanded },
                verticalAlignment = Alignment.CenterVertically
            ) {
                Checkbox(
                    checked = allSelected,
                    enabled = selectableItems.isNotEmpty(),
                    onCheckedChange = { onToggleCategory() },
                    colors = CheckboxDefaults.colors(checkedColor = EmeraldPrimary)
                )
                Column(modifier = Modifier.weight(1f)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = category.title,
                            style = MaterialTheme.typography.titleMedium,
                            color = TextPrimary,
                            modifier = Modifier.weight(1f, fill = false)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        RiskBadge(riskLevel = category.riskLevel)
                    }
                    Text(
                        text = "${category.items.size} encontrado(s) • ${ByteFormatter.format(category.totalBytes)}",
                        style = MaterialTheme.typography.bodySmall,
                        color = CyanAccent
                    )
                }
                IconButton(onClick = { expanded = !expanded }) {
                    Icon(
                        imageVector = if (expanded) Icons.Rounded.ExpandLess else Icons.Rounded.ExpandMore,
                        contentDescription = null,
                        tint = TextSecondary
                    )
                }
            }

            AnimatedVisibility(visible = expanded) {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    ExplanationBlock(
                        whatItDoesTitle = "O que são esses arquivos",
                        whatItDoesText = category.whatItIs,
                        problemText = category.willItCauseProblems,
                        riskLevel = category.riskLevel
                    )
                    OutlinedButton(
                        onClick = onInspectItems,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("Selecionar itens individuais (${category.selectedCount}/${category.items.size})")
                    }
                }
            }
        }
    }
}

@Composable
private fun InspectDeepFilesDialog(
    category: DeepCleanCategory,
    onToggleItem: (String) -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = BgCard,
        title = {
            Column {
                Text(category.title, style = MaterialTheme.typography.titleMedium, color = TextPrimary)
                Text(
                    text = "${category.selectedCount} selecionado(s) • ${ByteFormatter.format(category.selectedBytes)}",
                    style = MaterialTheme.typography.bodySmall,
                    color = EmeraldSoft
                )
            }
        },
        text = {
            LazyColumn(
                modifier = Modifier.heightIn(max = 360.dp),
                verticalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                items(category.items, key = { it.path }) { item ->
                    Surface(
                        color = BgCardElevated,
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable(enabled = !item.isProtectedOriginal) {
                                    onToggleItem(item.path)
                                }
                                .padding(8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            if (item.isProtectedOriginal) {
                                Icon(
                                    imageVector = Icons.Rounded.Lock,
                                    contentDescription = "Original protegido",
                                    tint = CyanAccent,
                                    modifier = Modifier.padding(horizontal = 10.dp).size(20.dp)
                                )
                            } else {
                                Checkbox(
                                    checked = item.isSelected,
                                    onCheckedChange = { onToggleItem(item.path) },
                                    colors = CheckboxDefaults.colors(checkedColor = EmeraldPrimary)
                                )
                            }
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = item.name,
                                    style = MaterialTheme.typography.labelMedium,
                                    color = TextPrimary,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                                Text(
                                    text = if (item.isDirectory) item.detailNote else "${ByteFormatter.format(item.sizeBytes)} • ${item.detailNote}",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = if (item.isProtectedOriginal) CyanAccent else TextSecondary,
                                    maxLines = 2,
                                    overflow = TextOverflow.Ellipsis
                                )
                            }
                        }
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = onDismiss,
                colors = ButtonDefaults.buttonColors(containerColor = EmeraldPrimary)
            ) {
                Text("Concluir Seleção", color = Color(0xFF042F22), fontWeight = FontWeight.Bold)
            }
        }
    )
}
