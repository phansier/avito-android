package com.avito.http

import okhttp3.Interceptor
import okhttp3.Request
import okhttp3.Response
import java.io.IOException
import java.util.concurrent.TimeUnit

/**
 * See also:
 * - [okhttp3.internal.http.RetryAndFollowUpInterceptor]
 */
public class RetryInterceptor(
    private val retries: Int = 5, // todo rename, it's tries
    private val allowedMethods: List<String> = listOf("GET"),
    private val allowedCodes: List<Int> = listOf(
        HttpCodes.CLIENT_TIMEOUT,
        HttpCodes.INTERNAL_ERROR,
        HttpCodes.BAD_GATEWAY,
        HttpCodes.UNAVAILABLE,
        HttpCodes.GATEWAY_TIMEOUT
    ),
    private val delayMs: Long = TimeUnit.SECONDS.toMillis(1),
    private val useIncreasingDelay: Boolean = true,
    private val modifyRetryRequest: (Request) -> Request = { it }
) : Interceptor {

    init {
        require(retries >= 1)
    }

    override fun intercept(chain: Interceptor.Chain): Response {
        val originalRequest = chain.request()
        var response: Response? = null
        var error: Throwable? = null

        var tryCount = 0
        while (response.shouldTry() && tryCount < retries) {

            tryCount++

            prepareForRetry(response)

            val request = if (tryCount > 1) {
                modifyRetryRequest(originalRequest)
            } else {
                originalRequest
            }

            try {
                response = chain.proceed(request)
            } catch (exception: IOException) {
                error = exception
                response = null
            }

            TimeUnit.MILLISECONDS.sleep(if (useIncreasingDelay) tryCount * delayMs else delayMs)
        }

        if (response == null) {
            throw error ?: IllegalStateException("Failed to execute request for unknown reason")
        }
        return response
    }

    private fun prepareForRetry(response: Response?) {
        if (response != null && response.shouldTry()) {
            response.close()
        }
    }

    private fun Response?.shouldTry(): Boolean = when {
        this == null -> true
        else -> request.method in allowedMethods && code in allowedCodes
    }
}
