package com.owen.stocksample.stock.ui

import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.owen.stocksample.R
import com.owen.stocksample.databinding.ActivityStockListBinding
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class StockListActivity : AppCompatActivity() {

    private lateinit var binding: ActivityStockListBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityStockListBinding.inflate(layoutInflater)
        setContentView(binding.root)
    }
}