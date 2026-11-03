package com.owen.stocksample.stock.ui

import com.owen.stocksample.core.network.ApiException
import com.owen.stocksample.stock.domain.Stock
import com.owen.stocksample.stock.domain.StockRepository
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertSame
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class StockViewModelTest {

    /** 回傳 [result] 的 StockRepository，設定 [gate] 時會等它完成才回傳，用來模擬載入中。 */
    private class FakeStockRepository(
        var result: Result<List<Stock>>,
        var gate: CompletableDeferred<Unit>? = null,
    ) : StockRepository {

        var callCount = 0
            private set

        override suspend fun getStocks(): Result<List<Stock>> {
            callCount++
            gate?.await()
            return result
        }
    }

    @Before
    fun setUp() {
        // viewModelScope 使用 Dispatchers.Main，單元測試需替換
        Dispatchers.setMain(UnconfinedTestDispatcher())
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun init_loadsStocks_sortedByDefault() = runTest {
        val repository = FakeStockRepository(Result.success(listOf("1101", "2330", "2317").map(::stock)))

        val viewModel = StockViewModel(repository)
        val state = viewModel.awaitLoaded()

        assertEquals(1, repository.callCount)
        assertEquals(StockSort.CODE_DESCENDING, viewModel.sort.value)
        assertEquals(listOf("2330", "2317", "1101"), (state as StockUiState.Success).stocks.map { it.code })
    }

    @Test
    fun init_repositoryFailure_emitsError() = runTest {
        val error = ApiException.NoNetwork()
        val repository = FakeStockRepository(Result.failure(error))

        val state = StockViewModel(repository).awaitLoaded()

        assertTrue(state is StockUiState.Error)
        assertSame(error, (state as StockUiState.Error).throwable)
    }

    @Test
    fun updateSort_resortsStocks() = runTest {
        val repository = FakeStockRepository(Result.success(listOf("1101", "2330", "2317").map(::stock)))
        val viewModel = StockViewModel(repository)
        viewModel.awaitLoaded()

        viewModel.updateSort(StockSort.CODE_ASCENDING)
        val state = viewModel.uiState.first {
            it is StockUiState.Success && it.stocks.first().code == "1101"
        }

        assertEquals(StockSort.CODE_ASCENDING, viewModel.sort.value)
        assertEquals(listOf("1101", "2317", "2330"), (state as StockUiState.Success).stocks.map { it.code })
        // 切換排序不重新呼叫 API
        assertEquals(1, repository.callCount)
    }

    @Test
    fun loadStocks_whileLoading_ignored() = runTest {
        val gate = CompletableDeferred<Unit>()
        val repository = FakeStockRepository(Result.success(listOf(stock("2330"))), gate)
        val viewModel = StockViewModel(repository)

        viewModel.loadStocks()
        viewModel.loadStocks()

        assertEquals(1, repository.callCount)
        gate.complete(Unit)
        assertTrue(viewModel.awaitLoaded() is StockUiState.Success)
    }

    @Test
    fun loadStocks_afterLoaded_reloadsWithNewData() = runTest {
        val repository = FakeStockRepository(Result.success(listOf(stock("2330"))))
        val viewModel = StockViewModel(repository)
        viewModel.awaitLoaded()

        repository.result = Result.success(listOf(stock("2330"), stock("2317")))
        viewModel.loadStocks()
        val state = viewModel.uiState.first { it is StockUiState.Success && it.stocks.size == 2 }

        assertEquals(2, repository.callCount)
        assertEquals(listOf("2330", "2317"), (state as StockUiState.Success).stocks.map { it.code })
    }

    @Test
    fun loadStocks_afterError_retrySucceeds() = runTest {
        val repository = FakeStockRepository(Result.failure(ApiException.Timeout()))
        val viewModel = StockViewModel(repository)
        assertTrue(viewModel.awaitLoaded() is StockUiState.Error)

        repository.result = Result.success(listOf(stock("2330")))
        viewModel.loadStocks()
        val state = viewModel.uiState.first { it is StockUiState.Success }

        assertEquals(listOf("2330"), (state as StockUiState.Success).stocks.map { it.code })
    }

    /** 等待第一個非 Loading 的狀態，uiState 為 WhileSubscribed，需有收集者才會開始運作。 */
    private suspend fun StockViewModel.awaitLoaded(): StockUiState =
        uiState.first { it !is StockUiState.Loading }
}
