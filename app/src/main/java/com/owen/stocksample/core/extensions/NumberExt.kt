package com.owen.stocksample.core.extensions

/** 數值缺漏（如無成交）時顯示的文字 */
const val PLACEHOLDER_TEXT = "--"

fun Number?.toDisplayText(): String = this?.toString() ?: PLACEHOLDER_TEXT
