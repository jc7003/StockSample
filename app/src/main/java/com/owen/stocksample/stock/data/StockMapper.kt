package com.owen.stocksample.stock.data

import com.owen.stocksample.stock.data.remote.model.StockBwiBbuDto
import com.owen.stocksample.stock.data.remote.model.StockDayDto
import com.owen.stocksample.stock.domain.Stock
import java.math.BigDecimal

/** 缺少代號或名稱的資料無法辨識，回傳 null。 */
fun StockDayDto.toDomain(monthlyAveragePrice: BigDecimal?, bwiBbuDto: StockBwiBbuDto?): Stock? {
    val code = code?.takeIf { it.isNotBlank() } ?: return null
    val name = name?.takeIf { it.isNotBlank() } ?: return null
    return Stock(
        code = code,
        name = name,
        tradeVolume = tradeVolume.toNumber()?.toLongOrNull(),
        tradeValue = tradeValue.toNumber()?.toLongOrNull(),
        openingPrice = openingPrice.toNumber()?.toBigDecimalOrNull(),
        highestPrice = highestPrice.toNumber()?.toBigDecimalOrNull(),
        lowestPrice = lowestPrice.toNumber()?.toBigDecimalOrNull(),
        closingPrice = closingPrice.toNumber()?.toBigDecimalOrNull(),
        change = change.toNumber()?.toBigDecimalOrNull(),
        monthlyAveragePrice = monthlyAveragePrice,
        transaction = transaction.toNumber()?.toLongOrNull(),
        peRatio = bwiBbuDto?.peRatio.toNumber()?.toBigDecimalOrNull(),
        dividendYield = bwiBbuDto?.dividendYield.toNumber()?.toBigDecimalOrNull(),
        pbRatio = bwiBbuDto?.pbRatio.toNumber()?.toBigDecimalOrNull(),
    )
}

/** 無成交時 API 回傳空字串，數字可能帶千分位逗號。 */
fun String?.toNumber(): String? =
    this?.replace(",", "")?.takeIf { it.isNotBlank() }
