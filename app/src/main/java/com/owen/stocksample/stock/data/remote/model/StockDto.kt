package com.owen.stocksample.stock.data.remote.model

import androidx.annotation.Keep
import com.google.gson.annotations.SerializedName

/** 上市個股日成交資訊。 */
@Keep
data class StockDayDto(
    @SerializedName("Date")
    val date: String? = null,
    @SerializedName("Code")
    val code: String? = null,
    @SerializedName("Name")
    val name: String? = null,
    @SerializedName("TradeVolume")
    val tradeVolume: String? = null,
    @SerializedName("TradeValue")
    val tradeValue: String? = null,
    @SerializedName("OpeningPrice")
    val openingPrice: String? = null,
    @SerializedName("HighestPrice")
    val highestPrice: String? = null,
    @SerializedName("LowestPrice")
    val lowestPrice: String? = null,
    @SerializedName("ClosingPrice")
    val closingPrice: String? = null,
    @SerializedName("Change")
    val change: String? = null,
    @SerializedName("Transaction")
    val transaction: String? = null,
)

/** 上市個股日收盤價及月均價。 */
@Keep
data class StockDayAvgDto(
    @SerializedName("Date")
    val date: String? = null,
    @SerializedName("Code")
    val code: String? = null,
    @SerializedName("Name")
    val name: String? = null,
    @SerializedName("ClosingPrice")
    val closingPrice: String? = null,
    @SerializedName("MonthlyAveragePrice")
    val monthlyAveragePrice: String? = null,
)

/** 上市個股日本益比、殖利率及股價淨值比。 */
@Keep
data class StockBwiBbuDto(
    @SerializedName("Date")
    val date: String? = null,
    @SerializedName("Code")
    val code: String? = null,
    @SerializedName("Name")
    val name: String? = null,
    /** 本益比 */
    @SerializedName("PEratio")
    val peRatio: String? = null,
    /** 殖利率 */
    @SerializedName("DividendYield")
    val dividendYield: String? = null,
    /** 股價淨值比 */
    @SerializedName("PBratio")
    val pbRatio: String? = null,
)
