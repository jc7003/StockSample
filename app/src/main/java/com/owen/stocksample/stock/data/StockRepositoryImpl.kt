package com.owen.stocksample.stock.data

import com.owen.stocksample.core.network.safeApiCall
import com.owen.stocksample.stock.data.remote.StockApi
import com.owen.stocksample.stock.domain.Stock
import com.owen.stocksample.stock.domain.StockRepository
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import javax.inject.Inject

class StockRepositoryImpl @Inject constructor(
    private val stockApi: StockApi
) : StockRepository {

    override suspend fun getStocks(): Result<List<Stock>> = safeApiCall {
        coroutineScope {
            val stockDayList = async { stockApi.getStockDayList() }
            val stockDayAvgList = async { stockApi.getStockDayAvgList() }
            val stockBwiBbuList = async { stockApi.getStockBwiBbuList() }

            val monthlyAveragePrices = stockDayAvgList.await()
                .mapNotNull { dto ->
                    val code = dto.code ?: return@mapNotNull null
                    val price = dto.monthlyAveragePrice.toNumber()?.toBigDecimalOrNull()
                    code to price
                }
                .toMap()

            val bwiBbuMap = stockBwiBbuList.await()
                .mapNotNull { dto ->
                    val code = dto.code ?: return@mapNotNull null
                    code to dto
                }.toMap()

            stockDayList.await().mapNotNull { dto ->
                dto.toDomain(monthlyAveragePrices[dto.code], bwiBbuMap[dto.code])
            }
        }
    }
}
