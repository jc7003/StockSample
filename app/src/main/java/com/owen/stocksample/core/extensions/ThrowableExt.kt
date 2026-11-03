package com.owen.stocksample.core.extensions

import android.content.res.Resources
import com.owen.stocksample.R
import com.owen.stocksample.core.network.ApiException

/** 將錯誤轉為顯示給使用者的訊息，非 [ApiException] 一律顯示未知錯誤。 */
fun Throwable.toErrorMessage(resources: Resources): String = when (this) {
    is ApiException.NoNetwork -> resources.getString(R.string.error_no_network)
    is ApiException.Timeout -> resources.getString(R.string.error_timeout)
    is ApiException.Server -> resources.getString(R.string.error_server, code)
    is ApiException.Client -> resources.getString(R.string.error_client, code)
    else -> resources.getString(R.string.error_unknown)
}
