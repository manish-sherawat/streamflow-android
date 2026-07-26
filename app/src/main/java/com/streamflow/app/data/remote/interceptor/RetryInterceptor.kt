package com.streamflow.app.data.remote.interceptor

import okhttp3.Interceptor
import okhttp3.Response
import java.io.IOException
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class RetryInterceptor @Inject constructor() : Interceptor {
    private val maxRetries = 3
    private val initialBackoffMs = 1000L

    override fun intercept(chain: Interceptor.Chain): Response {
        val request = chain.request()
        var response: Response? = null
        var exception: IOException? = null
        var tryCount = 0

        while (tryCount < maxRetries) {
            try {
                response?.close()
                response = chain.proceed(request)
                if (response.isSuccessful || response.code < 500) {
                    return response
                }
            } catch (e: IOException) {
                exception = e
            }

            tryCount++
            if (tryCount < maxRetries) {
                try {
                    Thread.sleep(initialBackoffMs * tryCount)
                } catch (e: InterruptedException) {
                    Thread.currentThread().interrupt()
                    break
                }
            }
        }

        if (response != null) {
            return response
        }
        throw exception ?: IOException("Network request failed after $maxRetries retries")
    }
}
