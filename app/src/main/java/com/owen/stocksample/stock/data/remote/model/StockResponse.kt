package com.owen.stocksample.stock.data.remote.model

import com.google.gson.annotations.SerializedName

data class StockResponse(
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
    val transaction: String? = null
)
