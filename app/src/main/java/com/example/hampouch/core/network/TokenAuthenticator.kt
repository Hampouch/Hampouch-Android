package com.example.hampouch.core.network

import android.content.Context
import com.example.hampouch.data.repository.AuthRepository
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import okhttp3.Authenticator
import okhttp3.Request
import okhttp3.Response
import okhttp3.Route

private const val REFRESH_TOKEN_PATH = "/api/auth/refresh"

/**
 * access token이 만료되어 서버가 401을 내려주면 refresh token으로 새 토큰을 발급받아
 * 실패했던 요청을 새 access token으로 한 번만 다시 시도한다.
 *
 * - 여러 요청이 동시에 401을 받아도 [synchronized] 블록으로 refresh 호출이 한 번만 일어나게 한다.
 *   락을 얻고 나서 현재 세션의 access token이 실패한 요청의 토큰과 다르면, 다른 요청이 이미
 *   재발급을 끝낸 것이므로 네트워크 호출 없이 새 토큰으로만 재시도한다.
 * - refresh token 자체가 만료/무효/폐기됐다면 [AuthRepository.refreshAccessToken]이 세션을
 *   지우고 실패를 반환하므로, 여기서는 null을 돌려줘 원래의 401 응답이 호출자에게 전달되게 한다
 *   (이후 화면단에서 로그인 화면으로 보내는 처리를 하게 된다).
 * - `/api/auth/refresh` 요청 자체가 401을 받은 경우와, 재발급한 토큰으로도 다시 401을 받은
 *   경우에는 더 이상 재시도하지 않고 포기한다(무한 루프 방지).
 */
class TokenAuthenticator(private val contextProvider: () -> Context) : Authenticator {

    override fun authenticate(route: Route?, response: Response): Request? {
        // 재발급 요청 자체가 실패한 경우는 재시도하지 않는다.
        if (response.request.url.encodedPath.endsWith(REFRESH_TOKEN_PATH)) return null
        // 이미 새 토큰으로 한 번 재시도했는데도 실패했다면 더 이상 시도하지 않는다.
        if (responseCount(response) > 1) return null
        // Authorization 헤더가 없는 요청(로그인/회원가입 등)의 401은 토큰 문제가 아니므로 무시한다.
        val failedAuthHeader = response.request.header("Authorization") ?: return null

        return synchronized(this) {
            val authRepository = AuthRepository.getInstance(contextProvider())
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
