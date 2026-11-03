package com.owen.stocksample.core.network

import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertSame
import org.junit.Assert.assertTrue
import org.junit.Test
import kotlin.coroutines.cancellation.CancellationException

class SafeApiCallTest {

    @Test
    fun safeApiCall_success_returnsSuccess() = runTest {
        val result = safeApiCall { 42 }

        assertEquals(42, result.getOrNull())
    }

    @Test
    fun safeApiCall_exception_returnsFailure() = runTest {
        val error = ApiException.Timeout()

        val result = safeApiCall { throw error }

        assertTrue(result.isFailure)
        assertSame(error, result.exceptionOrNull())
    }

    @Test(expected = CancellationException::class)
    fun safeApiCall_cancellation_rethrows() = runTest {
        // 取消必須往外拋，不能包成 Result.failure，否則 coroutine 無法正常取消
        safeApiCall { throw CancellationException("cancelled") }
    }
}
