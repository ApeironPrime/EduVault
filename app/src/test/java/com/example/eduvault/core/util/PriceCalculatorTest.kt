package com.example.eduvault.core.util

import org.junit.Assert.assertEquals
import org.junit.Test

class PriceCalculatorTest {

    @Test
    fun `file size zero or negative falls back to quantity label or default 1`() {
        assertEquals(1, PriceCalculator.calculatePrice(0L, ""))
        assertEquals(1, PriceCalculator.calculatePrice(-100L, ""))
    }

    @Test
    fun `file size under 2MB returns 1 credit`() {
        // 1 MB
        assertEquals(1, PriceCalculator.calculatePrice(1024L * 1024L, ""))
        // Exactly 2 MB
        assertEquals(1, PriceCalculator.calculatePrice(2L * 1024L * 1024L, ""))
    }

    @Test
    fun `file size between 2MB and 5MB returns 2 credits`() {
        // 2.1 MB
        val size2_1Mb = (2.1 * 1024.0 * 1024.0).toLong()
        assertEquals(2, PriceCalculator.calculatePrice(size2_1Mb, ""))
        // Exactly 5 MB
        assertEquals(2, PriceCalculator.calculatePrice(5L * 1024L * 1024L, ""))
    }

    @Test
    fun `file size greater than 5MB returns 3 credits`() {
        // 5.5 MB
        val size5_5Mb = (5.5 * 1024.0 * 1024.0).toLong()
        assertEquals(3, PriceCalculator.calculatePrice(size5_5Mb, ""))
        // 50 MB
        val size50Mb = (50L * 1024L * 1024L)
        assertEquals(3, PriceCalculator.calculatePrice(size50Mb, ""))
    }

    @Test
    fun `quantityLabel parsing handles MB suffix and comma decimal`() {
        assertEquals(1, PriceCalculator.calculatePrice(0L, "1,5 MB"))
        assertEquals(1, PriceCalculator.calculatePrice(0L, "2 MB"))
        assertEquals(2, PriceCalculator.calculatePrice(0L, "2.5 MB"))
        assertEquals(2, PriceCalculator.calculatePrice(0L, "4,8 MB"))
        assertEquals(3, PriceCalculator.calculatePrice(0L, "6.2 MB"))
        assertEquals(3, PriceCalculator.calculatePrice(0L, "15 MB"))
    }

    @Test
    fun `invalid quantityLabel returns default 1 credit`() {
        assertEquals(1, PriceCalculator.calculatePrice(0L, "Unknown size"))
        assertEquals(1, PriceCalculator.calculatePrice(0L, "abc MB"))
    }
}
