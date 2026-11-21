package com.limpalivre.optimizer.domain

import java.util.Locale

object ByteFormatter {
    private const val KB = 1024L
    private const val MB = KB * 1024L
    private const val GB = MB * 1024L

    fun format(bytes: Long): String {
        val safe = if (bytes < 0L) 0L else bytes
        return when {
            safe < KB -> "$safe B"
            safe < MB -> "${formatDecimal(safe.toDouble() / KB, 0)} KB"
            safe < GB -> "${formatDecimal(safe.toDouble() / MB, 1)} MB"
            else -> "${formatDecimal(safe.toDouble() / GB, 2)} GB"
        }
    }

    private fun formatDecimal(value: Double, decimals: Int): String {
        if (decimals == 0) {
            return String.format(Locale.US, "%.0f", value)
        }
        return String.format(Locale.US, "%.${decimals}f", value).replace('.', ',')
    }
}
