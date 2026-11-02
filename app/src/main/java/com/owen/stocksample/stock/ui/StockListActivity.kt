package com.owen.stocksample.stock.ui

import android.os.Bundle
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.isVisible
import androidx.core.view.updatePadding
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.owen.stocksample.R
import com.owen.stocksample.core.extensions.showSingle
import com.owen.stocksample.core.extensions.toCompactText
import com.owen.stocksample.core.extensions.toDisplayText
import com.owen.stocksample.core.network.ApiException
import com.owen.stocksample.core.utils.ColorUtils
import com.owen.stocksample.databinding.ActivityStockListBinding
import com.owen.stocksample.databinding.ItemStockBinding
import com.owen.stocksample.dialog.SimpleCenterDialog
import com.owen.stocksample.stock.domain.Stock
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

@AndroidEntryPoint
class StockListActivity : AppCompatActivity() {

    private lateinit var binding: ActivityStockListBinding

    private val viewModel: StockViewModel by viewModels()

    private lateinit var adapter: StockListAdapter

    /** 目前列表顯示的排序方式，用來判斷是否需要捲回頂端 */
    private var renderedSort: StockSort? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityStockListBinding.inflate(layoutInflater)
        setContentView(binding.root)

        with(binding) {

            setSupportActionBar(toolbar)
            toolbar.setNavigationOnClickListener {
                finish()
            }
            btnSort.setOnClickListener {
                BottomDialogStockSort().showSingle(supportFragmentManager)
            }

            swipeRefresh.setOnRefreshListener {
                viewModel.loadStocks()
            }
            errorView.btnRetry.setOnClickListener {
                viewModel.loadStocks()
            }

            adapter = StockListAdapter().apply {
                // 等資料到達後才還原捲動位置，避免空列表時就套用而失效
                stateRestorationPolicy = RecyclerView.Adapter.StateRestorationPolicy.PREVENT_WHEN_EMPTY
            }
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
            // 錯誤時隱藏列表避免與錯誤畫面重疊；只是隱藏，資料與捲動位置都會保留
            rvStock.isVisible = state !is StockUiState.Error

            when (state) {
                StockUiState.Loading -> Unit
                is StockUiState.Success -> {
                    if (binding.rvStock.adapter !is StockListAdapter) {
                        binding.rvStock.adapter = adapter
                    }

                    // 只有排序變更時才捲回頂端；旋轉螢幕後 renderedSort 為 null，保留 RecyclerView 還原的位置
                    val sort = viewModel.sort.value
                    val sortChanged = renderedSort != null && renderedSort != sort
                    renderedSort = sort
                    adapter.submitList(state.stocks) {
                        if (sortChanged) {
                            rvStock.post {
                                rvStock.scrollToPosition(0)
                            }
                        }
                    }
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
                binding.root.setOnClickListener {
                    val stock = it.tag as Stock

                    // 無資料的項目整行不顯示
                    val msg = listOfNotNull(
                        stock.peRatio?.let { getString(R.string.stock_alert_pe_ratio, it.toDisplayText()) },
                        stock.dividendYield?.let { getString(R.string.stock_alert_dividend_yield, it.toDisplayText()) },
                        stock.pbRatio?.let { getString(R.string.stock_alert_pb_ratio, it.toDisplayText()) },
                    ).joinToString("\n")
                        .ifEmpty { getString(R.string.stock_alert_no_valuation) }

                    SimpleCenterDialog.newInstance(
                        requestKey = SimpleCenterDialog.TAG,
                        title = stock.name,
                        message = msg,
                        positiveText = getString(R.string.common_confirm)
                    ).showSingle(supportFragmentManager)
                }
            }

            fun bind(stock: Stock) {
                with(binding) {
                    root.tag = stock
                    tvCode.text = stock.code
                    tvName.text = stock.name
                    tvOpeningPrice.text = stock.openingPrice.toDisplayText()
                    tvHighestPrice.text = stock.highestPrice.toDisplayText()
                    tvLowestPrice.text = stock.lowestPrice.toDisplayText()
                    tvMonthlyAveragePrice.text = stock.monthlyAveragePrice.toDisplayText()

                    tvTransaction.text = stock.transaction.toDisplayText()
                    tvTradeVolume.text = stock.tradeVolume.toCompactText()
                    tvTradeValue.text = stock.tradeValue.toCompactText()

                    tvChange.text = stock.change.toDisplayText()
                    tvChange.setTextColor(ColorUtils.getChangeColor(root.context, stock.change))

                    val closingPrice = stock.closingPrice
                    val monthlyAveragePrice = stock.monthlyAveragePrice
                    val diffFromAverage = if (closingPrice != null && monthlyAveragePrice != null) {
                        closingPrice - monthlyAveragePrice
                    } else {
                        null
                    }
                    tvClosingPrice.text = closingPrice.toDisplayText()
                    tvClosingPrice.setTextColor(
                        ColorUtils.getChangeColor(
                            root.context,
                            diffFromAverage
                        )
                    )
                }
            }
        }
    }

    companion object {
        /** 切換排序後列表淡入的時間（毫秒） */
        private const val SORT_FADE_IN_DURATION = 150L
    }
}
