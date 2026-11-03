package com.owen.stocksample.stock.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.owen.stocksample.stock.domain.Stock
import com.owen.stocksample.stock.domain.StockRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class StockViewModel @Inject constructor(
    private val stockRepository: StockRepository,
) : ViewModel() {

    /** API 回傳的原始資料，排序前；null 為載入中 */
    private val _loadResult = MutableStateFlow<Result<List<Stock>>?>(null)

    private val _sort = MutableStateFlow(StockSort.CODE_DESCENDING)
    val sort: StateFlow<StockSort> = _sort.asStateFlow()

    val uiState: StateFlow<StockUiState> = combine(_loadResult, _sort) { result, sort ->
        result?.fold(
            onSuccess = { StockUiState.Success(sort.sort(it), sort) },
            onFailure = { StockUiState.Error(it) },
        ) ?: StockUiState.Loading
    }.flowOn(Dispatchers.Default)
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), StockUiState.Loading)

    private var loadJob: Job? = null

    init {
        loadStocks()
    }

    fun loadStocks() {
        if (loadJob?.isActive == true) {
            return
        }

        loadJob = viewModelScope.launch {
            _loadResult.value = null
            _loadResult.value = stockRepository.getStocks()
        }
    }

    fun updateSort(sort: StockSort) {
        _sort.value = sort
    }
}

sealed interface StockUiState {
    data object Loading : StockUiState
    /** [stocks] 已依 [sort] 排序，兩者一起送出，畫面才能判斷列表是否因排序變更。 */
    data class Success(val stocks: List<Stock>, val sort: StockSort) : StockUiState
    data class Error(val throwable: Throwable) : StockUiState
}
