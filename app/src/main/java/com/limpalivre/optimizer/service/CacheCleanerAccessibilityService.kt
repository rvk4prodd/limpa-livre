package com.limpalivre.optimizer.service

import android.accessibilityservice.AccessibilityService
import android.content.Intent
import android.net.Uri
import android.os.Handler
import android.os.Looper
import android.provider.Settings
import android.view.accessibility.AccessibilityEvent
import android.view.accessibility.AccessibilityNodeInfo
import com.limpalivre.optimizer.MainActivity
import com.limpalivre.optimizer.data.model.CleanActionStatus

class CacheCleanerAccessibilityService : AccessibilityService() {

    private val handler = Handler(Looper.getMainLooper())
    private var lastActionMillis = 0L

    private val allowedSettingsPackages = setOf(
        "com.android.settings",
        "com.miui.securitycenter",
        "com.samsung.android.lool",
        "com.coloros.settings",
        "com.oplus.settings",
        "com.vivo.settings",
        "com.huawei.systemmanager",
        "com.iqoo.secure"
    )

    private val timeoutRunnable = Runnable {
        val state = CleanAutomationCoordinator.state.value
        if (state.isRunning) {
            val next = CleanAutomationCoordinator.recordOutcomeAndAdvance(CleanActionStatus.MANUAL_REQUIRED)
            if (next != null) {
                openAppSettings(next.packageName)
            } else {
                returnToLimpaLivre()
            }
        }
    }

    override fun onAccessibilityEvent(event: AccessibilityEvent?) {
        val state = CleanAutomationCoordinator.state.value
        if (!state.isRunning || state.isManualGuidedMode) return

        val pkg = event?.packageName?.toString() ?: return
        if (pkg !in allowedSettingsPackages) return

        val now = System.currentTimeMillis()
        if (now - lastActionMillis < 450L) return

        val root = rootInActiveWindow ?: return
        schedulePerAppTimeout()

        val nodes = collectAllNodes(root)
        var storageCandidate: AccessibilityNodeInfo? = null

        for (node in nodes) {
            val text = extractNodeText(node) ?: continue
            when (CacheAutomationMatcher.classifyLabel(text)) {
                ButtonMatch.FORBIDDEN_DATA_WIPE -> continue
                ButtonMatch.CLEAR_CACHE -> {
                    val clickable = findClickableTarget(node)
                    if (clickable != null && clickable.isEnabled && node.isEnabled) {
                        lastActionMillis = System.currentTimeMillis()
                        val clicked = clickable.performAction(AccessibilityNodeInfo.ACTION_CLICK)
                        if (clicked) {
                            handler.postDelayed({
                                advanceAfterClean(CleanActionStatus.SUCCESS)
                            }, 550L)
                            return
                        }
                    } else if (!node.isEnabled || (clickable != null && !clickable.isEnabled)) {
                        // Botão "Limpar cache" existe mas já está desabilitado (0 B de cache)
                        lastActionMillis = System.currentTimeMillis()
                        advanceAfterClean(CleanActionStatus.ALREADY_CLEAN)
                        return
                    }
                }
                ButtonMatch.STORAGE_SECTION -> {
                    if (storageCandidate == null) {
                        storageCandidate = findClickableTarget(node)
                    }
                }
                ButtonMatch.NONE -> Unit
            }
        }

        if (!state.openedStorageForCurrent && storageCandidate != null && storageCandidate.isEnabled) {
            lastActionMillis = System.currentTimeMillis()
            CleanAutomationCoordinator.markStorageSectionOpened()
            storageCandidate.performAction(AccessibilityNodeInfo.ACTION_CLICK)
        }
    }

    override fun onInterrupt() {
        handler.removeCallbacks(timeoutRunnable)
    }

    override fun onDestroy() {
        super.onDestroy()
        handler.removeCallbacksAndMessages(null)
    }

    private fun advanceAfterClean(status: CleanActionStatus) {
        handler.removeCallbacks(timeoutRunnable)
        val next = CleanAutomationCoordinator.recordOutcomeAndAdvance(status)
        if (next != null) {
            openAppSettings(next.packageName)
        } else {
            returnToLimpaLivre()
        }
    }

    private fun schedulePerAppTimeout() {
        handler.removeCallbacks(timeoutRunnable)
        handler.postDelayed(timeoutRunnable, PER_APP_TIMEOUT_MS)
    }

    private fun openAppSettings(packageName: String) {
        val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
            data = Uri.parse("package:$packageName")
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_NO_ANIMATION)
        }
        runCatching { startActivity(intent) }
        schedulePerAppTimeout()
    }

    private fun returnToLimpaLivre() {
        handler.removeCallbacks(timeoutRunnable)
        val intent = Intent(this, MainActivity::class.java).apply {
            addFlags(
                Intent.FLAG_ACTIVITY_NEW_TASK or
                    Intent.FLAG_ACTIVITY_SINGLE_TOP or
                    Intent.FLAG_ACTIVITY_CLEAR_TOP
            )
            putExtra(EXTRA_AUTOMATION_FINISHED, true)
        }
        runCatching { startActivity(intent) }
    }

    private fun collectAllNodes(root: AccessibilityNodeInfo): List<AccessibilityNodeInfo> {
        val result = ArrayList<AccessibilityNodeInfo>(64)
        val stack = ArrayDeque<AccessibilityNodeInfo>()
        stack.add(root)
        while (stack.isNotEmpty() && result.size < 250) {
            val current = stack.removeLast()
            result.add(current)
            for (i in 0 until current.childCount) {
                current.getChild(i)?.let { stack.add(it) }
            }
        }
        return result
    }

    private fun extractNodeText(node: AccessibilityNodeInfo): String? {
        val text = node.text?.toString()?.trim()
        if (!text.isNullOrBlank()) return text
        val desc = node.contentDescription?.toString()?.trim()
        if (!desc.isNullOrBlank()) return desc
        return null
    }

    private fun findClickableTarget(node: AccessibilityNodeInfo): AccessibilityNodeInfo? {
        var current: AccessibilityNodeInfo? = node
        var depth = 0
        while (current != null && depth < 5) {
            if (current.isClickable) return current
            current = current.parent
            depth++
        }
        return null
    }

    companion object {
        const val EXTRA_AUTOMATION_FINISHED = "extra_automation_finished"
        private const val PER_APP_TIMEOUT_MS = 6500L
    }
}
