package com.owen.stocksample.core.utils

import org.junit.Assert.assertEquals
import org.junit.Test
import java.math.BigDecimal

class PriceTrendTest {

    @Test
    fun of_null_flat() {
        assertEquals(PriceTrend.FLAT, PriceTrend.of(null))
    }

    @Test
    fun of_zeroWithAnyScale_flat() {
        // BigDecimal 的 == 會比較小數位數，0.00 也必須視為平盤
        assertEquals(PriceTrend.FLAT, PriceTrend.of(BigDecimal.ZERO))
        assertEquals(PriceTrend.FLAT, PriceTrend.of(BigDecimal("0.00")))
        assertEquals(PriceTrend.FLAT, PriceTrend.of(BigDecimal("-0.0000")))
    }

    @Test
    fun of_positive_up() {
        assertEquals(PriceTrend.UP, PriceTrend.of(BigDecimal("0.01")))
        assertEquals(PriceTrend.UP, PriceTrend.of(BigDecimal("10.5")))
    }

    @Test
    fun of_negative_down() {
        assertEquals(PriceTrend.DOWN, PriceTrend.of(BigDecimal("-0.01")))
        assertEquals(PriceTrend.DOWN, PriceTrend.of(BigDecimal("-10.5")))
    }
}
