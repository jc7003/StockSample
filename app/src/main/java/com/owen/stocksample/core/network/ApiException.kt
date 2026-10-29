package com.owen.stocksample.core.network

import java.io.IOException

/**
 * 統一的網路錯誤型別。
 *
 * 必須繼承 [IOException]：OkHttp 攔截器若丟出非 IOException，
 * 不會傳回呼叫端，而是直接讓 App crash。
 */
sealed class ApiException(message: String) : IOException(message) {
    class NoNetwork : ApiException("沒有網路連線")
    class Timeout : ApiException("連線逾時")
    class Server(val code: Int) : ApiException("伺服器錯誤 ($code)")
    class Client(val code: Int) : ApiException("請求錯誤 ($code)")
}
