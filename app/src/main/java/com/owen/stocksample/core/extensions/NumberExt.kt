package com.owen.stocksample.core.extensions

import java.math.BigDecimal
import java.math.RoundingMode
import java.text.DecimalFormat
import java.text.DecimalFormatSymbols
import java.util.Locale
import kotlin.math.abs

/** 數值缺漏（如無成交）時顯示的文字 */
const val PLACEHOLDER_TEXT = "--"

/** 千分位，小數維持原值，如 1,000、1,123,123、1,050.1234 */
private const val DISPLAY_PATTERN = "#,##0"

/** 單位縮寫：千分位、固定一位小數，如 1.2萬、3,456.7億 */
private const val COMPACT_PATTERN = "#,##0.0"

/** 由大到小排列，相鄰單位相差一萬倍 */
private val COMPACT_UNITS = listOf(
    1_0000_0000_0000L to "兆",
    1_0000_0000L to "億",
    1_0000L to "萬",
)

private val COMPACT_UNIT_STEP = BigDecimal.valueOf(1_0000L)

/** DecimalFormat 允許的最大小數位數，等同不限制 */
private const val MAX_FRACTION_DIGITS = 340

/** 數值轉為千分位格式的顯示文字，null 時顯示 [PLACEHOLDER_TEXT]。 */
fun Number?.toDisplayText(): String {
    this ?: return PLACEHOLDER_TEXT
    // DecimalFormat 非執行緒安全，每次建立新的實例
    return DecimalFormat(DISPLAY_PATTERN, DecimalFormatSymbols(Locale.US))
        .apply { maximumFractionDigits = MAX_FRACTION_DIGITS }
        .format(this)
}

/**
 * 數值轉為中文單位縮寫，如 12,345 → 1.2萬、33,961,234,567 → 339.6億，小數四捨五入至一位。
 * 未滿一萬時同 [toDisplayText]，null 時顯示 [PLACEHOLDER_TEXT]。
 */
fun Long?.toCompactText(): String {
    this ?: return PLACEHOLDER_TEXT
    var index = COMPACT_UNITS.indexOfFirst { (unit, _) -> abs(this) >= unit }
    if (index == -1) return toDisplayText()

    var scaled = scaleTo(COMPACT_UNITS[index].first)
    if (scaled.abs() >= COMPACT_UNIT_STEP && index > 0) {
        index--
        scaled = scaleTo(COMPACT_UNITS[index].first)
    }
    return DecimalFormat(COMPACT_PATTERN, DecimalFormatSymbols(Locale.US)).format(scaled) +
        COMPACT_UNITS[index].second
}

private fun Long.scaleTo(unit: Long): BigDecimal =
    BigDecimal.valueOf(this).divide(BigDecimal.valueOf(unit)).setScale(1, RoundingMode.HALF_UP)
