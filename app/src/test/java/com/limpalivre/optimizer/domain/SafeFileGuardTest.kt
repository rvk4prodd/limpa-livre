package com.limpalivre.optimizer.domain

import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TemporaryFolder
import java.io.File

class SafeFileGuardTest {

    @get:Rule
    val tempFolder = TemporaryFolder()

    @Test
    fun protectsCoreSystemDirectoriesAndCamera() {
        val root = tempFolder.newFolder("storage")
        val dcim = File(root, "DCIM").apply { mkdirs() }
        val camera = File(dcim, "Camera").apply { mkdirs() }
        val download = File(root, "Download").apply { mkdirs() }
        val androidDir = File(root, "Android").apply { mkdirs() }
        val randomEmpty = File(root, "AppAntigoResiduo").apply { mkdirs() }

        assertTrue(SafeFileGuard.isProtectedDirectory(root, root))
        assertTrue(SafeFileGuard.isProtectedDirectory(dcim, root))
        assertTrue(SafeFileGuard.isProtectedDirectory(camera, root))
        assertTrue(SafeFileGuard.isProtectedDirectory(download, root))
        assertTrue(SafeFileGuard.isProtectedDirectory(androidDir, root))
        assertFalse(SafeFileGuard.isProtectedDirectory(randomEmpty, root))
        assertTrue(SafeFileGuard.isSafeEmptyFolderToDelete(randomEmpty, root))
        assertFalse(SafeFileGuard.isSafeEmptyFolderToDelete(download, root))
    }

    @Test
    fun classifiesTempLogsAndInstallersSafely() {
        val root = tempFolder.newFolder("storage2")
        val tmpFile = File(root, "dump.tmp").apply { writeText("abc") }
        val logFile = File(root, "debug.log").apply { writeText("log") }
        val apkFile = File(root, "installer.apk").apply { writeText("apk") }
        val obbFile = File(root, "game.obb").apply { writeText("obb") }
        val photoFile = File(root, "foto.jpg").apply { writeText("img") }

        assertTrue(SafeFileGuard.isTempOrLogFile(tmpFile))
        assertTrue(SafeFileGuard.isTempOrLogFile(logFile))
        assertFalse(SafeFileGuard.isTempOrLogFile(photoFile))
        assertTrue(SafeFileGuard.isInstallerFile(apkFile))
        assertTrue(SafeFileGuard.isProtectedFileForDeletion(obbFile, root))
    }
}
