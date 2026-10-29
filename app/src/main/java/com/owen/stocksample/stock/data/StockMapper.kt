package com.owen.stocksample.stock.data

import com.owen.stocksample.stock.data.remote.model.StockDayDto
import com.owen.stocksample.stock.domain.Stock

/**
 * 缺少代號或名稱的資料無法辨識，回傳 null。
 */
fun StockDayDto.toDomain(monthlyAveragePrice: Double?): Stock? {
    val code = code?.takeIf { it.isNotBlank() } ?: return null
    val name = name?.takeIf { it.isNotBlank() } ?: return null
    return Stock(
        code = code,
        name = name,
        tradeVolume = tradeVolume.toNumber()?.toLongOrNull(),
        tradeValue = tradeValue.toNumber()?.toLongOrNull(),
        openingPrice = openingPrice.toNumber()?.toDoubleOrNull(),
        highestPrice = highestPrice.toNumber()?.toDoubleOrNull(),
        lowestPrice = lowestPrice.toNumber()?.toDoubleOrNull(),
        closingPrice = closingPrice.toNumber()?.toDoubleOrNull(),
        change = change.toNumber()?.toDoubleOrNull(),
        monthlyAveragePrice = monthlyAveragePrice,
        transaction = transaction.toNumber()?.toLongOrNull(),
    )
}

/**
 * 無成交時 API 回傳空字串，數字可能帶千分位逗號
 * */
fun String?.toNumber(): String? =
    this?.replace(",", "")?.takeIf { it.isNotBlank() }
