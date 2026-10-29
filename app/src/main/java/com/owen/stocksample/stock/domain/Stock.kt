package com.owen.stocksample.stock.domain

/**
 * 個股日成交資訊。無成交的欄位為 null。
 */
data class Stock(
    val code: String,
    val name: String,
    /** 成交股數 */
    val tradeVolume: Long?,
    /** 成交金額（元） */
    val tradeValue: Long?,
    val openingPrice: Double?,
    val highestPrice: Double?,
    val lowestPrice: Double?,
    val closingPrice: Double?,
    /** 漲跌價差 */
    val change: Double?,
    /** 月平均價 */
    val monthlyAveragePrice: Double?,
    /** 成交筆數 */
    val transaction: Long?,
)
