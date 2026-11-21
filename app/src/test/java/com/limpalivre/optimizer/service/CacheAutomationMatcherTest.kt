package com.limpalivre.optimizer.service

import org.junit.Assert.assertEquals
import org.junit.Test

class CacheAutomationMatcherTest {

    @Test
    fun matchesClearCacheAndStorageAcrossLanguages() {
        assertEquals(ButtonMatch.CLEAR_CACHE, CacheAutomationMatcher.classifyLabel("Limpar cache"))
        assertEquals(ButtonMatch.CLEAR_CACHE, CacheAutomationMatcher.classifyLabel("  Clear cache "))
        assertEquals(ButtonMatch.CLEAR_CACHE, CacheAutomationMatcher.classifyLabel("Borrar caché"))

        assertEquals(ButtonMatch.STORAGE_SECTION, CacheAutomationMatcher.classifyLabel("Armazenamento e cache"))
        assertEquals(ButtonMatch.STORAGE_SECTION, CacheAutomationMatcher.classifyLabel("Storage & cache"))
        assertEquals(ButtonMatch.STORAGE_SECTION, CacheAutomationMatcher.classifyLabel("Almacenamiento"))
    }

    @Test
    fun strictlyForbidsDataWipeOrUninstallLabels() {
        assertEquals(ButtonMatch.FORBIDDEN_DATA_WIPE, CacheAutomationMatcher.classifyLabel("Limpar dados"))
        assertEquals(ButtonMatch.FORBIDDEN_DATA_WIPE, CacheAutomationMatcher.classifyLabel("Limpar armazenamento"))
        assertEquals(ButtonMatch.FORBIDDEN_DATA_WIPE, CacheAutomationMatcher.classifyLabel("Clear storage"))
        assertEquals(ButtonMatch.FORBIDDEN_DATA_WIPE, CacheAutomationMatcher.classifyLabel("Clear data"))
        assertEquals(ButtonMatch.FORBIDDEN_DATA_WIPE, CacheAutomationMatcher.classifyLabel("Desinstalar"))
        assertEquals(ButtonMatch.FORBIDDEN_DATA_WIPE, CacheAutomationMatcher.classifyLabel("Forçar parada"))
    }
}
