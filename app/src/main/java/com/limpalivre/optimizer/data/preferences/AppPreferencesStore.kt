package com.limpalivre.optimizer.data.preferences

import android.content.Context
import android.content.SharedPreferences
import com.limpalivre.optimizer.data.model.CleanActionStatus
import com.limpalivre.optimizer.data.model.CleanReport
import com.limpalivre.optimizer.data.model.CleanReportItem
import org.json.JSONArray
import org.json.JSONObject

class AppPreferencesStore(context: Context) {

    private val prefs: SharedPreferences =
        context.getSharedPreferences("limpalivre_prefs", Context.MODE_PRIVATE)

    fun getWhitelist(): Set<String> {
        return prefs.getStringSet(KEY_WHITELIST, emptySet())?.toSet() ?: emptySet()
    }

    fun toggleWhitelist(packageName: String): Set<String> {
        val current = getWhitelist().toMutableSet()
        if (!current.add(packageName)) {
            current.remove(packageName)
        }
        prefs.edit().putStringSet(KEY_WHITELIST, current).apply()
        return current
    }

    fun saveReport(report: CleanReport) {
        val existing = getHistory().toMutableList()
        existing.add(0, report)
        val trimmed = existing.take(MAX_HISTORY_ITEMS)
        val array = JSONArray()
        trimmed.forEach { array.put(reportToJson(it)) }
        prefs.edit().putString(KEY_HISTORY_JSON, array.toString()).apply()
    }

    fun getHistory(): List<CleanReport> {
        val raw = prefs.getString(KEY_HISTORY_JSON, null) ?: return emptyList()
        return try {
            val array = JSONArray(raw)
            buildList {
                for (i in 0 until array.length()) {
                    add(reportFromJson(array.getJSONObject(i)))
                }
            }
        } catch (_: Exception) {
            emptyList()
        }
    }

    fun clearHistory() {
        prefs.edit().remove(KEY_HISTORY_JSON).apply()
    }

    private fun reportToJson(report: CleanReport): JSONObject {
        val detailsArray = JSONArray()
        report.details.forEach { item ->
            detailsArray.put(
                JSONObject().apply {
                    put("title", item.title)
                    put("subtitle", item.subtitle)
                    put("bytesBefore", item.bytesBefore)
                    put("bytesFreed", item.bytesFreed)
                    put("status", item.status.name)
                    put("whatWasDone", item.whatWasDone)
                    put("problemAssessment", item.problemAssessment)
                }
            )
        }
        return JSONObject().apply {
            put("id", report.id)
            put("timestampMillis", report.timestampMillis)
            put("operationType", report.operationType)
            put("totalBytesFreed", report.totalBytesFreed)
            put("itemsCleanedCount", report.itemsCleanedCount)
            put("itemsFailedCount", report.itemsFailedCount)
            put("summaryDiagnosis", report.summaryDiagnosis)
            put("hadAnyProblem", report.hadAnyProblem)
            put("details", detailsArray)
        }
    }

    private fun reportFromJson(obj: JSONObject): CleanReport {
        val detailsArray = obj.optJSONArray("details") ?: JSONArray()
        val details = buildList {
            for (i in 0 until detailsArray.length()) {
                val itemObj = detailsArray.getJSONObject(i)
                val statusName = itemObj.optString("status", CleanActionStatus.SUCCESS.name)
                val status = runCatching { CleanActionStatus.valueOf(statusName) }
                    .getOrDefault(CleanActionStatus.SUCCESS)
                add(
                    CleanReportItem(
                        title = itemObj.optString("title"),
                        subtitle = itemObj.optString("subtitle"),
                        bytesBefore = itemObj.optLong("bytesBefore"),
                        bytesFreed = itemObj.optLong("bytesFreed"),
                        status = status,
                        whatWasDone = itemObj.optString("whatWasDone"),
                        problemAssessment = itemObj.optString("problemAssessment")
                    )
                )
            }
        }
        return CleanReport(
            id = obj.optString("id"),
            timestampMillis = obj.optLong("timestampMillis"),
            operationType = obj.optString("operationType"),
            totalBytesFreed = obj.optLong("totalBytesFreed"),
            itemsCleanedCount = obj.optInt("itemsCleanedCount"),
            itemsFailedCount = obj.optInt("itemsFailedCount"),
            summaryDiagnosis = obj.optString("summaryDiagnosis"),
            hadAnyProblem = obj.optBoolean("hadAnyProblem"),
            details = details
        )
    }

    companion object {
        private const val KEY_WHITELIST = "whitelist_packages"
        private const val KEY_HISTORY_JSON = "clean_history_json"
        private const val MAX_HISTORY_ITEMS = 50
    }
}
