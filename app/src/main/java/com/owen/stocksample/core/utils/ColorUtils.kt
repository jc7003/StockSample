package com.owen.stocksample.core.utils

import android.content.Context
import androidx.annotation.ColorInt
import com.google.android.material.color.MaterialColors
import com.owen.stocksample.R

object ColorUtils {

    /** 依漲跌價差取得文字顏色：null 或 0 用 textColorPrimary，大於 0 用 upColor，小於 0 用 downColor。 */
    @ColorInt
    fun getChangeColor(context: Context, change: Double?): Int {
        val colorAttr = when {
            change == null || change == 0.0 -> android.R.attr.textColorPrimary
            change > 0 -> R.attr.upColor
            else -> R.attr.downColor
        }
        return MaterialColors.getColor(context, colorAttr, "ColorUtils")
    }
}
