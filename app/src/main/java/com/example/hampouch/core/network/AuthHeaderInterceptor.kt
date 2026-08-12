package com.example.hampouch.core.network

import javax.inject.Inject
import javax.inject.Provider
import javax.inject.Qualifier
import kotlinx.coroutines.runBlocking
import okhttp3.Interceptor
import okhttp3.Response

private val PUBLIC_AUTH_PATHS = setOf(
    "/api/auth/social",
    "/api/auth/login",
    "/api/auth/email/send",
    "/api/auth/email/verify",
    "/api/auth/nickname/check",
    "/api/auth/signup",
    "/api/auth/password/reset",
    "/api/auth/refresh"
)

class AuthHeaderInterceptor @Inject constructor(
    private val tokenProvider: Provider<AuthTokenProvider>,
    @param:ApiHost private val apiHost: String
) : Interceptor {

    override fun intercept(chain: Interceptor.Chain): Response {
        val request = chain.request()
        if (
            request.url.host != apiHost ||
            request.header(AUTHORIZATION_HEADER) != null ||
            request.url.encodedPath in PUBLIC_AUTH_PATHS
        ) {
            return chain.proceed(request)
        }

        val authorization = runBlocking { tokenProvider.get().currentAuthHeader() }
            ?: return chain.proceed(request)

        return chain.proceed(
            request.newBuilder()
                .header(AUTHORIZATION_HEADER, authorization)
                .build()
        )
    }
}

internal const val AUTHORIZATION_HEADER = "Authorization"

@Qualifier
@Retention(AnnotationRetention.BINARY)
annotation class ApiHost
