package com.owen.stocksample.compose

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.colorResource
import com.owen.stocksample.R

/** 漲跌顏色，M3 ColorScheme 沒有對應欄位，另外提供。 */
@Immutable
data class StockColors(
    /** 上漲 */
    val up: Color,
    /** 下跌 */
    val down: Color,
)

private val LocalStockColors = staticCompositionLocalOf {
    StockColors(up = Color.Unspecified, down = Color.Unspecified)
}

/** 漲跌顏色，與內建 token 一樣以 MaterialTheme.stockColors 取得 */
val MaterialTheme.stockColors: StockColors
    @Composable
    @ReadOnlyComposable
    get() = LocalStockColors.current

/** Compose 畫面的主題，使用 M3 預設配色，與 View 版的 Theme.Material3.DayNight 一致，並提供 [stockColors]。 */
@Composable
fun StockSampleTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit,
) {
    // 沿用 colors.xml 的色碼，與 View 版維持同一份來源
    val stockColors = if (darkTheme) {
        StockColors(up = colorResource(R.color.up_color_dark), down = colorResource(R.color.down_color_dark))
    } else {
        StockColors(up = colorResource(R.color.up_color), down = colorResource(R.color.down_color))
    }

    CompositionLocalProvider(LocalStockColors provides stockColors) {
        MaterialTheme(
            colorScheme = if (darkTheme) darkColorScheme() else lightColorScheme(),
            content = content,
        )
    }
}
