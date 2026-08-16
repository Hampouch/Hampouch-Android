package com.example.hampouch.core.network

import com.example.hampouch.data.repository.AuthRepository
import javax.inject.Inject
import javax.inject.Provider
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import okhttp3.Authenticator
import okhttp3.Request
import okhttp3.Response
import okhttp3.Route

private const val REFRESH_TOKEN_PATH = "/api/auth/refresh"
class TokenAuthenticator @Inject constructor(
    private val authRepositoryProvider: Provider<AuthRepository>
) : Authenticator {

    override fun authenticate(route: Route?, response: Response): Request? {
        if (response.request.url.encodedPath.endsWith(REFRESH_TOKEN_PATH)) return null
        if (responseCount(response) > 1) return null
        val failedAuthHeader = response.request.header("Authorization") ?: return null

        return synchronized(this) {
            val authRepository = authRepositoryProvider.get()
            val currentSession = runBlocking { authRepository.userSession.first() } ?: return@synchronized null
            val currentAuthHeader = "${currentSession.tokenType} ${currentSession.accessToken}"

            if (failedAuthHeader != currentAuthHeader) {
                return@synchronized response.request.newBuilder()
                    .header("Authorization", currentAuthHeader)
                    .build()
            }

            val refreshedSession = runBlocking { authRepository.refreshAccessToken() }.getOrNull()
                ?: return@synchronized null

            response.request.newBuilder()
                .header("Authorization", "${refreshedSession.tokenType} ${refreshedSession.accessToken}")
                .build()
        }
    }

    private fun responseCount(response: Response): Int {
        var count = 1
        var priorResponse = response.priorResponse
        while (priorResponse != null) {
            count++
            priorResponse = priorResponse.priorResponse
        }
        return count
    }
}
