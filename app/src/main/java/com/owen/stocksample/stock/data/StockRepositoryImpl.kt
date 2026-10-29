package com.owen.stocksample.stock.data

import com.owen.stocksample.stock.data.remote.StockApi
import com.owen.stocksample.stock.domain.StockRepository
import javax.inject.Inject

class StockRepositoryImpl @Inject constructor(
    private val stockApi: StockApi
) : StockRepository {
    // Repository implementation
}
