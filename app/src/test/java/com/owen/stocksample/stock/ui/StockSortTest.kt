package com.owen.stocksample.stock.ui

import com.owen.stocksample.stock.domain.Stock
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class StockSortTest {

    private val stocks = listOf("2317", "0050", "2330", "1101").map(::stock)

    @Test
    fun codeDescending_sortsByCodeDescending() {
        val sorted = StockSort.CODE_DESCENDING.sort(stocks)

        assertEquals(listOf("2330", "2317", "1101", "0050"), sorted.map { it.code })
    }

    @Test
    fun codeAscending_sortsByCodeAscending() {
        val sorted = StockSort.CODE_ASCENDING.sort(stocks)

        assertEquals(listOf("0050", "1101", "2317", "2330"), sorted.map { it.code })
    }

    @Test
    fun sort_doesNotModifyOriginalList() {
        val original = stocks.map { it.code }

        StockSort.CODE_ASCENDING.sort(stocks)

        assertEquals(original, stocks.map { it.code })
    }

    @Test
    fun sort_emptyList_returnsEmpty() {
        assertTrue(StockSort.CODE_DESCENDING.sort(emptyList()).isEmpty())
    }

    @Test
    fun entries_orderMatchesBottomSheet() {
        // 宣告順序即 bottom sheet 的顯示順序
        assertEquals(listOf(StockSort.CODE_DESCENDING, StockSort.CODE_ASCENDING), StockSort.entries)
    }
}

internal fun stock(code: String) = Stock(
    code = code,
    name = "股票$code",
    tradeVolume = null,
    tradeValue = null,
    openingPrice = null,
    highestPrice = null,
    lowestPrice = null,
    closingPrice = null,
    change = null,
    monthlyAveragePrice = null,
    transaction = null,
    peRatio = null,
    dividendYield = null,
    pbRatio = null,
)
