package com.owen.stocksample.stock.domain

import com.owen.stocksample.stock.ui.stock
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test
import java.math.BigDecimal

class StockTest {

    @Test
    fun diffFromAverage_closingAboveAverage_positive() {
        val stock = stock("2330").copy(
            closingPrice = BigDecimal("1060.00"),
            monthlyAveragePrice = BigDecimal("1040.25"),
        )

        assertEquals(BigDecimal("19.75"), stock.diffFromAverage)
    }

    @Test
    fun diffFromAverage_closingBelowAverage_negative() {
        val stock = stock("2330").copy(
            closingPrice = BigDecimal("100"),
            monthlyAveragePrice = BigDecimal("100.5"),
        )

        assertEquals(BigDecimal("-0.5"), stock.diffFromAverage)
    }

    @Test
    fun diffFromAverage_missingClosingPrice_null() {
        val stock = stock("2330").copy(monthlyAveragePrice = BigDecimal("100"))

        assertNull(stock.diffFromAverage)
    }

    @Test
    fun diffFromAverage_missingMonthlyAveragePrice_null() {
        val stock = stock("2330").copy(closingPrice = BigDecimal("100"))

        assertNull(stock.diffFromAverage)
    }
}
