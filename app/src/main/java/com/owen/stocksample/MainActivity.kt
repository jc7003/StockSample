package com.owen.stocksample

import android.content.Intent
import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.owen.stocksample.databinding.ActivityMainBinding
import com.owen.stocksample.stock.ui.StockListActivity
import com.owen.stocksample.stock.ui.compose.StockListComposeActivity
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        with(binding) {
            btnStockList.setOnClickListener {
                startActivity(Intent(this@MainActivity, StockListActivity::class.java))
            }

            btnComposeStockList.setOnClickListener {
                startActivity(Intent(this@MainActivity, StockListComposeActivity::class.java))
            }
        }
    }
}