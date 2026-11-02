package com.owen.stocksample.core.utils

import android.content.Context
import androidx.annotation.ColorInt
import com.google.android.material.color.MaterialColors
import com.owen.stocksample.R
import java.math.BigDecimal

object ColorUtils {

    /** 依漲跌價差取得文字顏色：null 或 0 用 textColorPrimary，大於 0 用 upColor，小於 0 用 downColor。 */
    @ColorInt
    fun getChangeColor(context: Context, change: BigDecimal?): Int {
        // 用 signum 判斷正負，BigDecimal 的 == 會比較小數位數，0.00 不等於 0
        val colorAttr = when (change?.signum() ?: 0) {
            0 -> android.R.attr.textColorPrimary
            1 -> R.attr.upColor
            else -> R.attr.downColor
        }
        return MaterialColors.getColor(context, colorAttr, "ColorUtils")
    }
}
