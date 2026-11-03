package com.owen.stocksample.stock.ui

import android.content.res.Resources
import com.owen.stocksample.R
import com.owen.stocksample.core.extensions.toDisplayText
import com.owen.stocksample.stock.domain.Stock

/** 組出本益比、殖利率、股價淨值比的訊息，無資料的項目整行不顯示，全部無資料時顯示提示文字。 */
fun Stock.toValuationMessage(resources: Resources): String =
    listOfNotNull(
        peRatio?.let { resources.getString(R.string.stock_alert_pe_ratio, it.toDisplayText()) },
        dividendYield?.let { resources.getString(R.string.stock_alert_dividend_yield, it.toDisplayText()) },
        pbRatio?.let { resources.getString(R.string.stock_alert_pb_ratio, it.toDisplayText()) },
    ).joinToString("\n")
        .ifEmpty { resources.getString(R.string.stock_alert_no_valuation) }
