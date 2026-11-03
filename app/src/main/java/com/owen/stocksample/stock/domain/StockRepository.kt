package com.owen.stocksample.stock.domain

interface StockRepository {

    /** 取得上市個股日成交資訊，並合併月平均價、本益比、殖利率及股價淨值比。 */
    suspend fun getStocks(): Result<List<Stock>>
}
