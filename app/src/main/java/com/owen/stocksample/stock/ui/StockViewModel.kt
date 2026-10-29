package com.owen.stocksample.stock.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.owen.stocksample.stock.domain.Stock
import com.owen.stocksample.stock.domain.StockRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class StockViewModel @Inject constructor(
    private val stockRepository: StockRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow<StockUiState>(StockUiState.Loading)
    val uiState: StateFlow<StockUiState> = _uiState.asStateFlow()

    private var loadJob: Job? = null

    init {
        loadStocks()
    }

    fun loadStocks() {
        if (loadJob?.isActive == true) {
            return
        }

        loadJob = viewModelScope.launch {
            _uiState.value = StockUiState.Loading
            stockRepository.getStocks()
                .onSuccess { _uiState.value = StockUiState.Success(it) }
                .onFailure { _uiState.value = StockUiState.Error(it) }
        }
    }
}

sealed interface StockUiState {
    data object Loading : StockUiState
    data class Success(val stocks: List<Stock>) : StockUiState
    data class Error(val throwable: Throwable) : StockUiState
}
