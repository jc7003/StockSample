package com.owen.stocksample.core.utils

import android.content.Context
import androidx.annotation.ColorInt
import com.google.android.material.color.MaterialColors
import com.owen.stocksample.R
import java.math.BigDecimal

object ColorUtils {

    private const val TAG = "ColorUtils"

    /** 依漲跌價差取得文字顏色：null 或 0 用 textColorPrimary，大於 0 用 upColor，小於 0 用 downColor。 */
    @ColorInt
    fun getChangeColor(context: Context, change: BigDecimal?): Int {
        val colorAttr = when (PriceTrend.of(change)) {
            PriceTrend.FLAT -> android.R.attr.textColorPrimary
            PriceTrend.UP -> R.attr.upColor
            PriceTrend.DOWN -> R.attr.downColor
        }
        return MaterialColors.getColor(context, colorAttr, TAG)
    }
}
