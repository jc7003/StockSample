package com.owen.stocksample.stock.domain

interface StockRepository {

    /** 上市個股日成交資訊 */
    suspend fun getStocks(): Result<List<Stock>>
}
