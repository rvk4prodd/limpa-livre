package com.limpalivre.optimizer.data.explainer

import android.content.pm.ApplicationInfo
import com.limpalivre.optimizer.data.model.AppCategory
import com.limpalivre.optimizer.data.model.DeepCategoryType
import com.limpalivre.optimizer.data.model.RiskLevel
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class ExplainerCatalogTest {

    @Test
    fun explainsKnownAndUnknownAppsWithRiskAssessments() {
        val wa = ExplainerCatalog.explainApp("com.whatsapp", "WhatsApp", ApplicationInfo.CATEGORY_SOCIAL)
        assertEquals(RiskLevel.SAFE, wa.riskLevel)
        assertTrue(wa.willItCauseProblems.contains("Não apaga conversas", ignoreCase = true))

        val bankCategory = ExplainerCatalog.classifyApp("com.nu.production.nubank", ApplicationInfo.CATEGORY_UNDEFINED)
        assertEquals(AppCategory.BANKING, bankCategory)

        val unknownGame = ExplainerCatalog.explainApp("com.example.rpg", "Exemplo RPG", ApplicationInfo.CATEGORY_GAME)
        assertEquals(RiskLevel.CAUTION, unknownGame.riskLevel)
    }

    @Test
    fun explainsEveryDeepCategory() {
        DeepCategoryType.entries.forEach { type ->
            val copy = ExplainerCatalog.explainDeepCategory(type)
            assertTrue(copy.title.isNotBlank())
            assertTrue(copy.whatItIs.isNotBlank())
            assertTrue(copy.willItCauseProblems.isNotBlank())
        }
    }
}
