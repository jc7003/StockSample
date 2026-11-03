package com.owen.stocksample.stock.data

import com.owen.stocksample.core.network.safeApiCall
import com.owen.stocksample.stock.data.remote.StockApi
import com.owen.stocksample.stock.domain.Stock
import com.owen.stocksample.stock.domain.StockRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.withContext
import javax.inject.Inject

class StockRepositoryImpl @Inject constructor(
    private val stockApi: StockApi,
) : StockRepository {

    override suspend fun getStocks(): Result<List<Stock>> = safeApiCall {
        withContext(Dispatchers.Default) {
            val stockDayList = async { stockApi.getStockDayList() }
            val stockDayAvgList = async { stockApi.getStockDayAvgList() }
            // 估值資料只在點開個股時顯示，失敗時以空資料繼續，不影響列表
            val stockBwiBbuList = async {
                safeApiCall { stockApi.getStockBwiBbuList() }.getOrDefault(emptyList())
            }

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
