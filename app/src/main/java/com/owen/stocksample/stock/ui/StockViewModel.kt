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
    private val stockRepository: StockRepository
) : ViewModel() {

    /** API 回傳的原始資料，排序前 */
    private val _loadState = MutableStateFlow<StockUiState>(StockUiState.Loading)

    private val _sort = MutableStateFlow(StockSort.CODE_DESCENDING)
    val sort: StateFlow<StockSort> = _sort.asStateFlow()

    val uiState: StateFlow<StockUiState> = combine(_loadState, _sort) { state, sort ->
        if (state is StockUiState.Success) {
            StockUiState.Success(sort.sort(state.stocks))
        } else {
            state
        }
    }.flowOn(Dispatchers.Default).stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), StockUiState.Loading)

    private var loadJob: Job? = null

    init {
        loadStocks()
    }

    fun loadStocks() {
        if (loadJob?.isActive == true) {
            return
        }

        loadJob = viewModelScope.launch {
            _loadState.value = StockUiState.Loading
            stockRepository.getStocks()
                .onSuccess { _loadState.value = StockUiState.Success(it) }
                .onFailure { _loadState.value = StockUiState.Error(it) }
        }
    }

    fun updateSort(sort: StockSort) {
        _sort.value = sort
    }
}

sealed interface StockUiState {
    data object Loading : StockUiState
    data class Success(val stocks: List<Stock>) : StockUiState
    data class Error(val throwable: Throwable) : StockUiState
}
