package com.owen.stocksample.stock.ui

import android.os.Bundle
import android.util.Log
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.isVisible
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.owen.stocksample.R
import com.owen.stocksample.core.network.ApiException
import com.owen.stocksample.databinding.ActivityStockListBinding
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

@AndroidEntryPoint
class StockListActivity : AppCompatActivity() {

    private lateinit var binding: ActivityStockListBinding

    private val viewModel: StockViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityStockListBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.btnRetry.setOnClickListener { viewModel.loadStocks() }

        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.uiState.collect(::render)
            }
        }
    }

    private fun render(state: StockUiState) {
        with(binding) {
            progressBar.isVisible = state is StockUiState.Loading
            tvError.isVisible = state is StockUiState.Error
            btnRetry.isVisible = state is StockUiState.Error

            when (state) {
                StockUiState.Loading -> Unit
                is StockUiState.Success -> {
                    Log.d("Owen", "render: ${state.stocks.size}")
                    // TODO: 交給 RecyclerView adapter 顯示
                }
                is StockUiState.Error -> tvError.text = state.throwable.toMessage()
            }
        }
    }

    private fun Throwable.toMessage(): String = when (this) {
        is ApiException.NoNetwork -> getString(R.string.error_no_network)
        is ApiException.Timeout -> getString(R.string.error_timeout)
        is ApiException.Server -> getString(R.string.error_server, code)
        is ApiException.Client -> getString(R.string.error_client, code)
        else -> getString(R.string.error_unknown)
    }
}
