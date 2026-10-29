package com.owen.stocksample.core.network

import okhttp3.Interceptor
import okhttp3.Response
import java.net.ConnectException
import java.net.SocketTimeoutException
import java.net.UnknownHostException
import javax.inject.Inject

/**
 * 將連線失敗與非 2xx 回應轉成 [ApiException]。
 */
class ErrorInterceptor @Inject constructor() : Interceptor {

    override fun intercept(chain: Interceptor.Chain): Response {
        val response = try {
            chain.proceed(chain.request())
        } catch (_: UnknownHostException) {
            throw ApiException.NoNetwork()
        } catch (_: ConnectException) {
            throw ApiException.NoNetwork()
        } catch (_: SocketTimeoutException) {
            throw ApiException.Timeout()
        }

        if (response.isSuccessful) {
            return response
        }

        val code = response.code
        response.close()
        throw when (code) {
            in 500..599 -> {
                ApiException.Server(code)
            }
            else -> {
                ApiException.Client(code)
            }
        }
    }
}
