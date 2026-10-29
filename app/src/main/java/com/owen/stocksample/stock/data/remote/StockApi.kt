package com.owen.stocksample.stock.data.remote

import com.owen.stocksample.stock.data.remote.model.StockBwiBbuDto
import com.owen.stocksample.stock.data.remote.model.StockDayAvgDto
import com.owen.stocksample.stock.data.remote.model.StockDayDto
import retrofit2.http.GET

interface StockApi {

    @GET(ApiConfigure.API_STOCK_DAY)
    suspend fun getStockDayList(): List<StockDayDto>

    @GET(ApiConfigure.API_STOCK_DAY_AVG_ALL)
    suspend fun getStockDayAvgList(): List<StockDayAvgDto>

    @GET(ApiConfigure.API_BWIBBU_ALL)
    suspend fun getStockBwiBbuList(): List<StockBwiBbuDto>
}
