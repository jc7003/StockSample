package com.owen.stocksample.stock.ui

import androidx.annotation.StringRes
import com.owen.stocksample.R
import com.owen.stocksample.stock.domain.Stock

/** 個股列表的排序方式，宣告順序即 bottom sheet 的顯示順序。 */
enum class StockSort(
    @get:StringRes val labelRes: Int,
    private val comparator: Comparator<Stock>,
) {
    CODE_DESCENDING(R.string.stock_sort_code_descending, compareByDescending { it.code }),
    CODE_ASCENDING(R.string.stock_sort_code_ascending, compareBy { it.code });

    /** 依排序方式排列個股。 */
    fun sort(stocks: List<Stock>): List<Stock> = stocks.sortedWith(comparator)
}
