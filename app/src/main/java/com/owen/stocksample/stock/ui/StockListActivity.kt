package com.owen.stocksample.stock.ui

import android.os.Bundle
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.isVisible
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.owen.stocksample.R
import com.owen.stocksample.core.network.ApiException
import com.owen.stocksample.core.utils.ColorUtils
import com.owen.stocksample.core.extensions.toDisplayText
import com.owen.stocksample.databinding.ActivityStockListBinding
import com.owen.stocksample.databinding.ItemStockBinding
import com.owen.stocksample.stock.domain.Stock
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

@AndroidEntryPoint
class StockListActivity : AppCompatActivity() {

    private lateinit var binding: ActivityStockListBinding

    private val viewModel: StockViewModel by viewModels()

    private lateinit var adapter: StockListAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityStockListBinding.inflate(layoutInflater)
        setContentView(binding.root)

        with(binding) {

            setSupportActionBar(toolbar)
            toolbar.setNavigationOnClickListener {
                finish()
            }

            swipeRefresh.setOnRefreshListener {
                viewModel.loadStocks()
            }
            errorView.btnRetry.setOnClickListener {
                viewModel.loadStocks()
            }

            adapter = StockListAdapter()
        }

        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.uiState.collect(::render)
            }
        }
    }

    private fun render(state: StockUiState) {
        with(binding) {
            swipeRefresh.isRefreshing = state is StockUiState.Loading
            errorView.root.isVisible = state is StockUiState.Error

            when (state) {
                StockUiState.Loading -> Unit
                is StockUiState.Success -> {
                    if (binding.rvStock.adapter !is StockListAdapter) {
                        binding.rvStock.adapter = adapter
                    }
                    adapter.submitList(state.stocks)
                }

                is StockUiState.Error -> errorView.tvError.text = state.throwable.toMessage()
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


    inner class StockListAdapter : ListAdapter<Stock, StockListAdapter.StockViewHolder>(
        object : DiffUtil.ItemCallback<Stock>() {
            override fun areItemsTheSame(
                oldItem: Stock,
                newItem: Stock
            ): Boolean {
                return oldItem.code == newItem.code
            }

            override fun areContentsTheSame(
                oldItem: Stock,
                newItem: Stock
            ): Boolean {
                return oldItem == newItem
            }
        }
    ) {

        override fun onCreateViewHolder(
            parent: ViewGroup,
            viewType: Int
        ): StockViewHolder {

            return StockViewHolder(
                ItemStockBinding.inflate(LayoutInflater.from(parent.context), parent, false)
            )
        }

        override fun onBindViewHolder(
            holder: StockViewHolder,
            position: Int
        ) {
            holder.bind(getItem(position))
        }

        inner class StockViewHolder(
            private val binding: ItemStockBinding
        ) : RecyclerView.ViewHolder(binding.root) {

            init {
                //todo click event
            }

            fun bind(stock: Stock) {
                with(binding) {
                    tvCode.text = stock.code
                    tvName.text = stock.name
                    tvOpeningPrice.text = stock.openingPrice.toDisplayText()
                    tvHighestPrice.text = stock.highestPrice.toDisplayText()
                    tvLowestPrice.text = stock.lowestPrice.toDisplayText()
                    tvMonthlyAveragePrice.text = stock.monthlyAveragePrice.toDisplayText()

                    tvTransaction.text = stock.transaction.toDisplayText()
                    tvTradeVolume.text = stock.tradeVolume.toDisplayText()
                    tvTradeValue.text = stock.tradeValue.toDisplayText()

                    tvChange.text = stock.change.toDisplayText()
                    tvChange.setTextColor(ColorUtils.getChangeColor(root.context, stock.change))

                    tvClosingPrice.text = stock.closingPrice.toDisplayText()
                    tvClosingPrice.setTextColor(ColorUtils.getChangeColor(root.context, stock.closingPrice))
                }
            }
        }
    }

}
