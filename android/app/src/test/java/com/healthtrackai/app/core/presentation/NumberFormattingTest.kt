package com.healthtrackai.app.core.presentation

import org.junit.Assert.assertEquals
import org.junit.Test

class NumberFormattingTest {
    @Test
    fun `formats grouped integers with Vietnamese separators`() {
        assertEquals("6.420", formatVietnameseInteger(6_420))
        assertEquals("9.300", formatVietnameseInteger(9_300))
    }
}
