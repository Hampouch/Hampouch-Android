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
        // 재발급 요청 자체가 실패한 경우는 재시도하지 않는다.
        if (response.request.url.encodedPath.endsWith(REFRESH_TOKEN_PATH)) return null
        // 이미 새 토큰으로 한 번 재시도했는데도 실패했다면 더 이상 시도하지 않는다.
        if (responseCount(response) > 1) return null
        // Authorization 헤더가 없는 요청(로그인/회원가입 등)의 401은 토큰 문제가 아니므로 무시한다.
        val failedAuthHeader = response.request.header("Authorization") ?: return null

        return synchronized(this) {
            val authRepository = authRepositoryProvider.get()
            val currentSession = runBlocking { authRepository.userSession.first() } ?: return@synchronized null
            val currentAuthHeader = "${currentSession.tokenType} ${currentSession.accessToken}"

            if (failedAuthHeader != currentAuthHeader) {
                // 다른 요청이 락을 먼저 잡고 이미 재발급을 끝낸 상태. 새 토큰으로만 재시도한다.
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
