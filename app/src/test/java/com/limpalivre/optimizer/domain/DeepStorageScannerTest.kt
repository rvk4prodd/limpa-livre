package com.limpalivre.optimizer.domain

import com.limpalivre.optimizer.data.model.DeepCategoryType
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TemporaryFolder
import java.io.File

class DeepStorageScannerTest {

    @get:Rule
    val tempFolder = TemporaryFolder()

    @Test
    fun scansAndCleansSelectedCategoriesWhilePreservingProtectedFiles() = runTest {
        val root = tempFolder.newFolder("mock_sdcard")

        val downloadDir = File(root, "Download").apply { mkdirs() }
        val emptyLeftover = File(root, "ResiduoAppAntigo/subpasta").apply { mkdirs() }
        val thumbsDir = File(root, "Pictures/.thumbnails").apply { mkdirs() }
        val waShared = File(root, "WhatsApp/.Shared").apply { mkdirs() }

        val tempFile = File(downloadDir, "rastros.tmp").apply { writeBytes(ByteArray(2048)) }
        val thumbFile = File(thumbsDir, "12345.jpg").apply { writeBytes(ByteArray(4096)) }
        val apkFile = File(downloadDir, "app_velho.apk").apply { writeBytes(ByteArray(8192)) }
        val waTemp = File(waShared, "temp_share").apply { writeBytes(ByteArray(1024)) }
        val userPhoto = File(root, "DCIM/Camera/foto.jpg").apply {
            parentFile?.mkdirs()
            writeBytes(ByteArray(5000))
        }

        val scanner = DeepStorageScanner(root)
        val categories = scanner.scan()

        val tempCat = categories.first { it.type == DeepCategoryType.TEMP_LOGS }
        val thumbCat = categories.first { it.type == DeepCategoryType.THUMBNAILS_CACHE }
        val emptyCat = categories.first { it.type == DeepCategoryType.EMPTY_RESIDUAL }
        val apkCat = categories.first { it.type == DeepCategoryType.APK_INSTALLERS }
        val waCat = categories.first { it.type == DeepCategoryType.MESSENGER_CACHE }

        assertEquals(1, tempCat.items.size)
        assertEquals(1, thumbCat.items.size)
        assertTrue(emptyCat.items.size >= 2)
        assertEquals(1, apkCat.items.size)
        assertFalse(apkCat.items.first().isSelected) // APK não é marcado por padrão
        assertEquals(1, waCat.items.size)
        assertEquals(waTemp.absolutePath, waCat.items.first().path)

        val report = scanner.cleanSelected(categories)

        assertFalse(report.hadAnyProblem)
        assertFalse(tempFile.exists())
        assertFalse(thumbFile.exists())
        assertFalse(emptyLeftover.exists())
        assertTrue(downloadDir.exists()) // pasta protegida preservada
        assertTrue(apkFile.exists())     // APK desmarcado preservado
        assertTrue(userPhoto.exists())   // foto em DCIM/Camera preservada
    }
}
