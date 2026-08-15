package com.example.hampouch.data.repository

import com.example.hampouch.data.remote.dto.ApiResponse
import com.example.hampouch.data.remote.dto.AuthMeData
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.ResponseBody.Companion.toResponseBody
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import retrofit2.Response

class AuthSessionStatusTest {

    @Test
    fun `auth me success returns valid session state`() {
        val response = Response.success(
            ApiResponse(
                code = "SUCCESS",
                message = "조회 성공",
                data = AuthMeData(
                    userId = 1L,
                    nickname = "햄주머니",
                    needsNickname = false,
                    role = "USER",
                    status = "ACTIVE"
                )
            )
        )

        assertEquals(
            SessionStatus.Valid(needsNickname = false, nickname = "햄주머니"),
            response.toSessionStatus()
        )
    }

    @Test
    fun `auth me unauthorized returns invalid session state`() {
        val response = errorResponse(401, "AUTH_UNAUTHORIZED", "인증이 필요합니다.")

        assertEquals(SessionStatus.Invalid, response.toSessionStatus())
    }

    @Test
    fun `auth me server error preserves message as unavailable state`() {
        val response = errorResponse(500, "INTERNAL_SERVER_ERROR", "일시적인 서버 오류입니다.")

        val status = response.toSessionStatus()

        assertTrue(status is SessionStatus.Unavailable)
        assertEquals("일시적인 서버 오류입니다.", (status as SessionStatus.Unavailable).message)
    }

    private fun errorResponse(
        httpStatus: Int,
        code: String,
        message: String
    ): Response<ApiResponse<AuthMeData>> = Response.error(
        httpStatus,
        """{"code":"$code","message":"$message","status":$httpStatus}"""
            .toResponseBody("application/json".toMediaType())
    )
}
