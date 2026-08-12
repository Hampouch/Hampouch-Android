package com.example.hampouch.core.network

import java.util.concurrent.TimeUnit
import javax.inject.Provider
import okhttp3.Call
import okhttp3.Connection
import okhttp3.Interceptor
import okhttp3.Protocol
import okhttp3.Request
import okhttp3.Response
import okhttp3.ResponseBody.Companion.toResponseBody
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class AuthHeaderInterceptorTest {

    @Test
    fun `인증 endpoint 요청에 현재 session header를 추가한다`() {
        val interceptor = interceptor("Bearer access-token")
        val chain = RecordingChain(request("https://api.hampouch.com/api/challenges/current"))

        interceptor.intercept(chain)

        assertEquals("Bearer access-token", chain.proceededRequest?.header(AUTHORIZATION_HEADER))
    }

    @Test
    fun `공개 auth endpoint에는 session header를 추가하지 않는다`() {
        val interceptor = interceptor("Bearer access-token")
        val chain = RecordingChain(request("https://api.hampouch.com/api/auth/login"))

        interceptor.intercept(chain)

        assertNull(chain.proceededRequest?.header(AUTHORIZATION_HEADER))
    }

    @Test
    fun `호출부가 지정한 임시 header는 덮어쓰지 않는다`() {
        val interceptor = interceptor("Bearer stored-token")
        val request = request("https://api.hampouch.com/api/auth/nickname")
            .newBuilder()
            .header(AUTHORIZATION_HEADER, "Bearer pending-token")
            .build()
        val chain = RecordingChain(request)

        interceptor.intercept(chain)

        assertEquals("Bearer pending-token", chain.proceededRequest?.header(AUTHORIZATION_HEADER))
    }

    @Test
    fun `presigned 외부 host에는 session header를 전송하지 않는다`() {
        val interceptor = interceptor("Bearer access-token")
        val chain = RecordingChain(request("https://storage.example.com/presigned-upload"))

        interceptor.intercept(chain)

        assertNull(chain.proceededRequest?.header(AUTHORIZATION_HEADER))
    }

    private fun request(url: String): Request = Request.Builder().url(url).build()

    private fun interceptor(header: String?): AuthHeaderInterceptor = AuthHeaderInterceptor(
        tokenProvider = Provider { FakeTokenProvider(header) },
        apiHost = "api.hampouch.com"
    )

    private class FakeTokenProvider(
        private val header: String?
    ) : AuthTokenProvider {
        override suspend fun currentAuthHeader(): String? = header
    }

    private class RecordingChain(
        private val initialRequest: Request
    ) : Interceptor.Chain {
        var proceededRequest: Request? = null

        override fun request(): Request = initialRequest

        override fun proceed(request: Request): Response {
            proceededRequest = request
            return Response.Builder()
                .request(request)
                .protocol(Protocol.HTTP_1_1)
                .code(200)
                .message("OK")
                .body("".toResponseBody())
                .build()
        }

        override fun connection(): Connection? = null

        override fun call(): Call = error("테스트에서 사용하지 않습니다.")

        override fun connectTimeoutMillis(): Int = 0

        override fun withConnectTimeout(timeout: Int, unit: TimeUnit): Interceptor.Chain = this

        override fun readTimeoutMillis(): Int = 0

        override fun withReadTimeout(timeout: Int, unit: TimeUnit): Interceptor.Chain = this

        override fun writeTimeoutMillis(): Int = 0

        override fun withWriteTimeout(timeout: Int, unit: TimeUnit): Interceptor.Chain = this
    }
}
