package com.streamflow.app.data.remote.auth

import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.runBlocking
import okhttp3.Interceptor
import okhttp3.Response
import javax.inject.Inject
import javax.inject.Singleton

import com.streamflow.app.data.remote.NetworkConfig

@Singleton
class AuthInterceptor @Inject constructor(
    private val firebaseAuth: FirebaseAuth
) : Interceptor {

    override fun intercept(chain: Interceptor.Chain): Response {
        val originalRequest = chain.request()

        val requestBuilder = originalRequest.newBuilder()
            .header("X-API-Key", NetworkConfig.API_KEY)
            .header("Cache-Control", "no-cache, no-store, must-revalidate")
            .header("Pragma", "no-cache")

        val currentUser = firebaseAuth.currentUser
        if (currentUser != null) {
            val token = try {
                com.google.android.gms.tasks.Tasks.await(
                    currentUser.getIdToken(false),
                    3,
                    java.util.concurrent.TimeUnit.SECONDS
                )?.token
            } catch (e: Exception) {
                null
            }

            if (!token.isNullOrEmpty()) {
                requestBuilder.header("Authorization", "Bearer $token")
            }
        }

        return chain.proceed(requestBuilder.build())
    }
}
