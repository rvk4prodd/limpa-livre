package com.limpalivre.optimizer.ui.components

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
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
import androidx.compose.material.icons.rounded.CheckCircle
import androidx.compose.material.icons.rounded.Info
import androidx.compose.material.icons.rounded.Security
import androidx.compose.material.icons.rounded.Shield
import androidx.compose.material.icons.rounded.WarningAmber
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.limpalivre.optimizer.data.model.CleanActionStatus
import com.limpalivre.optimizer.data.model.CleanReport
import com.limpalivre.optimizer.data.model.CleanReportItem
import com.limpalivre.optimizer.data.model.RiskLevel
import com.limpalivre.optimizer.domain.ByteFormatter
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
fun AnimatedStorageGauge(
    usedFraction: Float,
    centerTitle: String,
    centerSubtitle: String,
    size: Dp = 152.dp,
    modifier: Modifier = Modifier
) {
    val animatedSweep by animateFloatAsState(
        targetValue = (usedFraction.coerceIn(0f, 1f) * 260f),
        animationSpec = tween(durationMillis = 850, easing = FastOutSlowInEasing),
        label = "gauge_sweep"
    )

    val gaugeColors = when {
        usedFraction >= 0.88f -> listOf(AmberWarning, CoralDanger)
        usedFraction >= 0.70f -> listOf(CyanAccent, AmberWarning)
        else -> listOf(EmeraldPrimary, CyanAccent)
    }

    Box(
        modifier = modifier.size(size),
        contentAlignment = Alignment.Center
    ) {
        Canvas(modifier = Modifier.size(size)) {
            val strokePx = 14.dp.toPx()
            val inset = strokePx / 2f
            val arcSize = Size(this.size.width - strokePx, this.size.height - strokePx)
            val topLeft = Offset(inset, inset)

            drawArc(
                color = BorderSubtle,
                startAngle = 140f,
                sweepAngle = 260f,
                useCenter = false,
                topLeft = topLeft,
                size = arcSize,
                style = Stroke(width = strokePx, cap = StrokeCap.Round)
            )

            drawArc(
                brush = Brush.sweepGradient(gaugeColors),
                startAngle = 140f,
                sweepAngle = animatedSweep,
                useCenter = false,
                topLeft = topLeft,
                size = arcSize,
                style = Stroke(width = strokePx, cap = StrokeCap.Round)
            )
        }

        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                text = centerTitle,
                style = MaterialTheme.typography.headlineSmall,
                color = TextPrimary,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = centerSubtitle,
                style = MaterialTheme.typography.bodySmall,
                color = TextSecondary
            )
        }
    }
}

@Composable
fun RiskBadge(riskLevel: RiskLevel, modifier: Modifier = Modifier) {
    val (label, bg, fg) = when (riskLevel) {
        RiskLevel.SAFE -> Triple("Seguro", Color(0xFF064E3B), EmeraldSoft)
        RiskLevel.CAUTION -> Triple("Atenção", Color(0xFF452809), AmberWarning)
        RiskLevel.MANUAL -> Triple("Manual", Color(0xFF3F1D24), CoralDanger)
    }

    Surface(
        modifier = modifier,
        color = bg,
        shape = RoundedCornerShape(999.dp)
    ) {
        Text(
            text = label,
            color = fg,
            style = MaterialTheme.typography.labelMedium,
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 3.dp)
        )
    }
}

@Composable
fun ExplanationBlock(
    whatItDoesTitle: String,
    whatItDoesText: String,
    problemText: String,
    riskLevel: RiskLevel,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier.fillMaxWidth(),
        color = BgCardElevated,
        shape = RoundedCornerShape(12.dp),
        border = BorderStroke(1.dp, BorderSubtle)
    ) {
        Column(
            modifier = Modifier.padding(12.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Rounded.Info,
                        contentDescription = null,
                        tint = CyanAccent,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = whatItDoesTitle,
                        style = MaterialTheme.typography.labelMedium,
                        color = CyanAccent
                    )
                }
                RiskBadge(riskLevel = riskLevel)
            }
            Text(
                text = whatItDoesText,
                style = MaterialTheme.typography.bodySmall,
                color = TextPrimary
            )

            HorizontalDivider(color = BorderSubtle, thickness = 0.5.dp)

            Row(verticalAlignment = Alignment.Top) {
                Icon(
                    imageVector = if (riskLevel == RiskLevel.SAFE) Icons.Rounded.Shield else Icons.Rounded.WarningAmber,
                    contentDescription = null,
                    tint = if (riskLevel == RiskLevel.SAFE) EmeraldSoft else AmberWarning,
                    modifier = Modifier
                        .padding(top = 2.dp)
                        .size(15.dp)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Column {
                    Text(
                        text = "Dá algum problema limpar?",
                        style = MaterialTheme.typography.labelMedium,
                        color = if (riskLevel == RiskLevel.SAFE) EmeraldSoft else AmberWarning
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = problemText,
                        style = MaterialTheme.typography.bodySmall,
                        color = TextSecondary
                    )
                }
            }
        }
    }
}

@Composable
fun CleanReportDialog(
    report: CleanReport,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = BgCard,
        title = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = if (!report.hadAnyProblem) Icons.Rounded.CheckCircle else Icons.Rounded.WarningAmber,
                    contentDescription = null,
                    tint = if (!report.hadAnyProblem) EmeraldSoft else AmberWarning,
                    modifier = Modifier.size(26.dp)
                )
                Spacer(modifier = Modifier.width(10.dp))
                Column {
                    Text(
                        text = report.operationType,
                        style = MaterialTheme.typography.titleMedium,
                        color = TextPrimary
                    )
                    Text(
                        text = if (report.totalBytesFreed > 0L) {
                            "${ByteFormatter.format(report.totalBytesFreed)} liberados"
                        } else {
                            "${report.itemsCleanedCount} item(ns) processado(s)"
                        },
                        style = MaterialTheme.typography.bodySmall,
                        color = EmeraldSoft,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Surface(
                    color = if (!report.hadAnyProblem) Color(0xFF063D2F) else Color(0xFF3E290D),
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Text(
                        text = report.summaryDiagnosis,
                        style = MaterialTheme.typography.bodySmall,
                        color = TextPrimary,
                        modifier = Modifier.padding(10.dp)
                    )
                }

                LazyColumn(
                    modifier = Modifier.heightIn(max = 320.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(report.details) { item ->
                        ReportItemRow(item = item)
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = onDismiss,
                colors = ButtonDefaults.buttonColors(containerColor = EmeraldPrimary)
            ) {
                Text("Entendi", color = Color(0xFF042F22), fontWeight = FontWeight.Bold)
            }
        }
    )
}

@Composable
private fun ReportItemRow(item: CleanReportItem) {
    val statusColor = when (item.status) {
        CleanActionStatus.SUCCESS, CleanActionStatus.ALREADY_CLEAN -> EmeraldSoft
        CleanActionStatus.PARTIAL, CleanActionStatus.MANUAL_REQUIRED -> AmberWarning
        CleanActionStatus.SKIPPED -> TextSecondary
        CleanActionStatus.FAILED -> CoralDanger
    }

    Card(
        colors = CardDefaults.cardColors(containerColor = BgCardElevated),
        shape = RoundedCornerShape(10.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(10.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = item.title,
                    style = MaterialTheme.typography.titleMedium,
                    color = TextPrimary,
                    modifier = Modifier.weight(1f)
                )
                Text(
                    text = if (item.bytesFreed > 0L) "+${ByteFormatter.format(item.bytesFreed)}" else item.status.label,
                    style = MaterialTheme.typography.labelMedium,
                    color = statusColor
                )
            }
            if (item.subtitle.isNotBlank()) {
                Text(
                    text = item.subtitle,
                    style = MaterialTheme.typography.bodySmall,
                    color = TextSecondary
                )
            }
            Text(
                text = "• O que foi feito: ${item.whatWasDone}",
                style = MaterialTheme.typography.bodySmall,
                color = TextPrimary
            )
            Text(
                text = "• Risco/Problema: ${item.problemAssessment}",
                style = MaterialTheme.typography.bodySmall,
                color = TextSecondary
            )
        }
    }
}

@Composable
fun AccessibilityDisclosureDialog(
    onOpenAccessibilitySettings: () -> Unit,
    onOpenOwnAppInfo: () -> Unit,
    onUseManualMode: () -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = BgCard,
        title = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Rounded.Security,
                    contentDescription = null,
                    tint = EmeraldSoft
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Automação Transparente",
                    style = MaterialTheme.typography.titleMedium,
                    color = TextPrimary
                )
            }
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(
                    text = "O Android bloqueia por segurança que um aplicativo apague o cache interno de outros aplicativos diretamente. Para limpar vários apps com 1 toque:",
                    style = MaterialTheme.typography.bodySmall,
                    color = TextSecondary
                )
                Surface(
                    color = BgCardElevated,
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(10.dp),
                        verticalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Text(
                            text = "• Só atua quando você toca em Limpar no LimpaLivre.",
                            style = MaterialTheme.typography.bodySmall,
                            color = TextPrimary
                        )
                        Text(
                            text = "• Clica apenas em Armazenamento e Limpar cache.",
                            style = MaterialTheme.typography.bodySmall,
                            color = TextPrimary
                        )
                        Text(
                            text = "• Bloqueio rígido: NUNCA clica em Limpar dados ou Desinstalar.",
                            style = MaterialTheme.typography.bodySmall,
                            color = EmeraldSoft,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }
                Surface(
                    color = Color(0xFF1E293B),
                    shape = RoundedCornerShape(10.dp),
                    border = BorderStroke(1.dp, BorderSubtle)
                ) {
                    Column(
                        modifier = Modifier.padding(10.dp),
                        verticalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Text(
                            text = "Dica para Android 13, 14, 15 e 16 (Configuração restrita):",
                            style = MaterialTheme.typography.labelMedium,
                            color = AmberWarning
                        )
                        Text(
                            text = "Se o sistema mostrar 'Configuração restrita', toque em Info do App abaixo → menu ⋮ no canto superior direito → 'Permitir configurações restritas' e depois ative em Acessibilidade.",
                            style = MaterialTheme.typography.bodySmall,
                            color = TextSecondary
                        )
                    }
                }
            }
        },
        confirmButton = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Button(
                    onClick = onOpenAccessibilitySettings,
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(containerColor = EmeraldPrimary)
                ) {
                    Text("Abrir Acessibilidade", color = Color(0xFF042F22), fontWeight = FontWeight.Bold)
                }
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedButton(
                        onClick = onOpenOwnAppInfo,
                        modifier = Modifier.weight(1f)
                    ) {
                        Text("Info do App", style = MaterialTheme.typography.labelMedium)
                    }
                    OutlinedButton(
                        onClick = onUseManualMode,
                        modifier = Modifier.weight(1f)
                    ) {
                        Text("Modo Guiado", style = MaterialTheme.typography.labelMedium)
                    }
                }
                TextButton(
                    onClick = onDismiss,
                    modifier = Modifier.align(Alignment.End)
                ) {
                    Text("Cancelar", color = TextSecondary)
                }
            }
        }
    )
}
