package com.owen.stocksample.stock.ui.compose

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import com.owen.stocksample.compose.StockSampleTheme
import com.owen.stocksample.stock.ui.StockViewModel
import dagger.hilt.android.AndroidEntryPoint

/** 以 Compose 實作的個股列表，功能與 StockListActivity 相同，共用 [StockViewModel]。 */
@AndroidEntryPoint
class StockListComposeActivity : ComponentActivity() {

    private val viewModel: StockViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        enableEdgeToEdge()
        super.onCreate(savedInstanceState)
        setContent {
            StockSampleTheme {
                StockListRoute(viewModel = viewModel, onBack = ::finish)
            }
        }
    }
}
