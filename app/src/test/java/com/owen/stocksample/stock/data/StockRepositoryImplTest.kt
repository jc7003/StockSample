package com.owen.stocksample.stock.data

import com.owen.stocksample.core.network.ApiException
import com.owen.stocksample.stock.data.remote.StockApi
import com.owen.stocksample.stock.data.remote.model.StockBwiBbuDto
import com.owen.stocksample.stock.data.remote.model.StockDayAvgDto
import com.owen.stocksample.stock.data.remote.model.StockDayDto
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test
import java.math.BigDecimal

class StockRepositoryImplTest {

    /** 回傳固定資料的 StockApi，設定各 API 的 error 時改為丟出該例外。 */
    private class FakeStockApi(
        var dayList: List<StockDayDto> = emptyList(),
        var dayAvgList: List<StockDayAvgDto> = emptyList(),
        var bwiBbuList: List<StockBwiBbuDto> = emptyList(),
        var dayError: Exception? = null,
        var dayAvgError: Exception? = null,
        var bwiBbuError: Exception? = null,
    ) : StockApi {

        override suspend fun getStockDayList(): List<StockDayDto> {
            dayError?.let { throw it }
            return dayList
        }

        override suspend fun getStockDayAvgList(): List<StockDayAvgDto> {
            dayAvgError?.let { throw it }
            return dayAvgList
        }

        override suspend fun getStockBwiBbuList(): List<StockBwiBbuDto> {
            bwiBbuError?.let { throw it }
            return bwiBbuList
        }
    }

    private val api = FakeStockApi()
    private val repository = StockRepositoryImpl(api)

    @Test
    fun getStocks_mergesByCode() = runTest {
        api.dayList = listOf(dayDto("2330"), dayDto("2317"))
        api.dayAvgList = listOf(
            StockDayAvgDto(code = "2317", monthlyAveragePrice = "200.50"),
            StockDayAvgDto(code = "2330", monthlyAveragePrice = "1,040.25"),
        )
        api.bwiBbuList = listOf(
            StockBwiBbuDto(code = "2330", peRatio = "25.30"),
            StockBwiBbuDto(code = "2317", peRatio = "12.10"),
        )

        val stocks = repository.getStocks().getOrThrow()

        assertEquals(listOf("2330", "2317"), stocks.map { it.code })
        assertEquals(BigDecimal("1040.25"), stocks[0].monthlyAveragePrice)
        assertEquals(BigDecimal("25.30"), stocks[0].peRatio)
        assertEquals(BigDecimal("200.50"), stocks[1].monthlyAveragePrice)
        assertEquals(BigDecimal("12.10"), stocks[1].peRatio)
    }

    @Test
    fun getStocks_missingAvgAndBwiBbu_fieldsAreNull() = runTest {
        api.dayList = listOf(dayDto("9999"))

        val stock = repository.getStocks().getOrThrow().single()

        assertNull(stock.monthlyAveragePrice)
        assertNull(stock.peRatio)
        assertNull(stock.dividendYield)
        assertNull(stock.pbRatio)
    }

    @Test
    fun getStocks_emptyMonthlyAveragePrice_isNull() = runTest {
        api.dayList = listOf(dayDto("2330"))
        api.dayAvgList = listOf(StockDayAvgDto(code = "2330", monthlyAveragePrice = ""))

        val stock = repository.getStocks().getOrThrow().single()

        assertNull(stock.monthlyAveragePrice)
    }

    @Test
    fun getStocks_ignoresAvgAndBwiBbuWithoutCode() = runTest {
        api.dayList = listOf(dayDto("2330"))
        api.dayAvgList = listOf(StockDayAvgDto(code = null, monthlyAveragePrice = "100"))
        api.bwiBbuList = listOf(StockBwiBbuDto(code = null, peRatio = "10"))

        val stock = repository.getStocks().getOrThrow().single()

        assertNull(stock.monthlyAveragePrice)
        assertNull(stock.peRatio)
    }

    @Test
    fun getStocks_filtersInvalidDayDto() = runTest {
        api.dayList = listOf(dayDto("2330"), dayDto("").copy(name = "無代號"), dayDto("2317").copy(name = null))

        val stocks = repository.getStocks().getOrThrow()

        assertEquals(listOf("2330"), stocks.map { it.code })
    }

    @Test
    fun getStocks_emptyResponse_returnsEmptyList() = runTest {
        val stocks = repository.getStocks().getOrThrow()

        assertTrue(stocks.isEmpty())
    }

    @Test
    fun getStocks_dayApiError_returnsFailure() = runTest {
        api.dayError = ApiException.Server(503)

        val error = repository.getStocks().exceptionOrNull()

        // 不用 assertSame：測試時 coroutines 會為了補上 stack trace 而複製例外，實例可能不同
        assertTrue(error is ApiException.Server)
        assertEquals(503, (error as ApiException.Server).code)
    }

    @Test
    fun getStocks_dayAvgApiError_returnsFailure() = runTest {
        api.dayList = listOf(dayDto("2330"))
        api.dayAvgError = ApiException.Timeout()

        val error = repository.getStocks().exceptionOrNull()

        assertTrue(error is ApiException.Timeout)
    }

    @Test
    fun getStocks_bwiBbuApiError_stillReturnsStocks() = runTest {
        // 估值資料失敗不影響列表，只是本益比等欄位為 null
        api.dayList = listOf(dayDto("2330"))
        api.dayAvgList = listOf(StockDayAvgDto(code = "2330", monthlyAveragePrice = "1,040.25"))
        api.bwiBbuError = ApiException.NoNetwork()

        val stock = repository.getStocks().getOrThrow().single()

        assertEquals(BigDecimal("1040.25"), stock.monthlyAveragePrice)
        assertNull(stock.peRatio)
        assertNull(stock.dividendYield)
        assertNull(stock.pbRatio)
    }

    private fun dayDto(code: String) = StockDayDto(
        code = code,
        name = "股票$code",
        closingPrice = "100.00",
    )
}
