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
            val stockDays = async { stockApi.getStockDayList() }
            val stockDayAvgs = async { stockApi.getStockDayAvgList() }

            val monthlyAveragePrices = stockDayAvgs.await()
                .mapNotNull { dto ->
                    val code = dto.code ?: return@mapNotNull null
                    val price = dto.monthlyAveragePrice.toNumber()?.toDoubleOrNull()
                    code to price
                }
                .toMap()

            stockDays.await().mapNotNull { dto ->
                dto.toDomain(monthlyAveragePrices[dto.code])
            }
        }
    }
}
