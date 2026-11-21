package com.limpalivre.optimizer.domain

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TemporaryFolder
import java.io.File

class DuplicateDetectorTest {

    @get:Rule
    val tempFolder = TemporaryFolder()

    @Test
    fun detectsExactDuplicatesAndProtectsOldestOriginal() {
        val dir = tempFolder.newFolder("files")
        val payload = ByteArray(16 * 1024) { (it % 127).toByte() }
        val differentPayload = ByteArray(16 * 1024) { ((it + 3) % 127).toByte() }

        val original = File(dir, "doc_original.pdf").apply {
            writeBytes(payload)
            setLastModified(1_700_000_000_000L)
        }
        val copy1 = File(dir, "doc_copia1.pdf").apply {
            writeBytes(payload)
            setLastModified(1_700_000_100_000L)
        }
        val copy2 = File(dir, "doc_copia2.pdf").apply {
            writeBytes(payload)
            setLastModified(1_700_000_200_000L)
        }
        val uniqueSameSize = File(dir, "outro.pdf").apply {
            writeBytes(differentPayload)
        }

        val items = DuplicateDetector.findDuplicates(listOf(original, copy1, copy2, uniqueSameSize))

        assertEquals(3, items.size)
        val originalItem = items.first { it.path == original.absolutePath }
        val copy1Item = items.first { it.path == copy1.absolutePath }
        val copy2Item = items.first { it.path == copy2.absolutePath }

        assertTrue(originalItem.isProtectedOriginal)
        assertFalse(originalItem.isSelected)
        assertFalse(copy1Item.isProtectedOriginal)
        assertTrue(copy1Item.isSelected)
        assertTrue(copy2Item.isSelected)
    }
}
