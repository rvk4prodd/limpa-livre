package com.limpalivre.optimizer.domain

import org.junit.Assert.assertEquals
import org.junit.Test

class ByteFormatterTest {

    @Test
    fun formatsBytesKilobytesMegabytesAndGigabytes() {
        assertEquals("0 B", ByteFormatter.format(-50L))
        assertEquals("0 B", ByteFormatter.format(0L))
        assertEquals("512 B", ByteFormatter.format(512L))
        assertEquals("10 KB", ByteFormatter.format(10L * 1024L))
        assertEquals("1,5 MB", ByteFormatter.format((1.5 * 1024.0 * 1024.0).toLong()))
        assertEquals("2,00 GB", ByteFormatter.format(2L * 1024L * 1024L * 1024L))
    }
}
