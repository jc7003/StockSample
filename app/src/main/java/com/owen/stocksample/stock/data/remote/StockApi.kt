package com.owen.stocksample.stock.data.remote

import com.owen.stocksample.stock.data.remote.model.StockResponse
import retrofit2.Response
import retrofit2.http.GET

interface StockApi {

    @GET("exchangeReport/STOCK_DAY_ALL")
    suspend fun getStockList(): Response<List<StockResponse>>
}
