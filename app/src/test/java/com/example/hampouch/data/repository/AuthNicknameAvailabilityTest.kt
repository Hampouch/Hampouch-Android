package com.example.hampouch.data.repository

import com.example.hampouch.data.remote.dto.ApiResponse
import com.example.hampouch.data.remote.dto.NicknameCheckData
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.ResponseBody.Companion.toResponseBody
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import retrofit2.Response

class AuthNicknameAvailabilityTest {

    @Test
    fun `사용 가능한 닉네임 응답은 true를 반환한다`() {
        val response = Response.success(
            ApiResponse(
                code = "SUCCESS",
                message = "요청이 성공했습니다.",
                data = NicknameCheckData(nickname = "햄포치", available = true)
            )
        )

        val result = response.toNicknameAvailabilityResult()

        assertTrue(result.getOrThrow())
    }

    @Test
    fun `닉네임 중복 409 응답은 false를 반환한다`() {
        val response = Response.error<ApiResponse<NicknameCheckData>>(
            409,
            """{"code":"USER_NICKNAME_ALREADY_EXISTS","message":"이미 존재하는 닉네임입니다.","status":409}"""
                .toResponseBody("application/json".toMediaType())
        )

        val result = response.toNicknameAvailabilityResult()

        assertTrue(result.isSuccess)
        assertFalse(result.getOrThrow())
    }

    @Test
    fun `닉네임 중복 코드가 아닌 오류는 실패로 보존한다`() {
        val response = Response.error<ApiResponse<NicknameCheckData>>(
            400,
            """{"code":"VALIDATION_ERROR","message":"입력값이 올바르지 않습니다.","status":400}"""
                .toResponseBody("application/json".toMediaType())
        )

        val result = response.toNicknameAvailabilityResult()

        assertTrue(result.isFailure)
        assertEquals("입력값이 올바르지 않습니다.", result.exceptionOrNull()?.message)
    }
}
