package com.owen.stocksample.stock.domain

import java.math.BigDecimal

/**
 * 個股日成交資訊。無成交或無資料的欄位為 null。
 * 價格與比率使用 [BigDecimal] 避免浮點誤差，比較大小時需用 compareTo，== 會連小數位數一起比較。
 */
data class Stock(
    val code: String,
    val name: String,
    /** 成交股數 */
    val tradeVolume: Long?,
    /** 成交金額（元） */
    val tradeValue: Long?,
    val openingPrice: BigDecimal?,
    val highestPrice: BigDecimal?,
    val lowestPrice: BigDecimal?,
    val closingPrice: BigDecimal?,
    /** 漲跌價差 */
    val change: BigDecimal?,
    /** 月平均價 */
    val monthlyAveragePrice: BigDecimal?,
    /** 成交筆數 */
    val transaction: Long?,
    /** 本益比 */
    val peRatio: BigDecimal?,
    /** 殖利率 */
    val dividendYield: BigDecimal?,
    /** 股價淨值比 */
    val pbRatio: BigDecimal?,
)
