package com.owen.stocksample.core.network

import kotlin.coroutines.cancellation.CancellationException

/**
 * 將 API 呼叫包成 [Result]。
 *
 * 不用 runCatching：它會吞掉 [CancellationException]，導致 coroutine 無法正常取消。
 */
suspend inline fun <T> safeApiCall(block: suspend () -> T): Result<T> =
    try {
        Result.success(block())
    } catch (e: CancellationException) {
        throw e
    } catch (e: Exception) {
        Result.failure(e)
    }
