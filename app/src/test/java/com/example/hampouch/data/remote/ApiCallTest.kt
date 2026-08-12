package com.example.hampouch.data.remote

import com.example.hampouch.data.remote.dto.ApiResponse
import com.example.hampouch.domain.model.ApiException
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.ResponseBody.Companion.toResponseBody
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import retrofit2.Response

class ApiCallTest {

    @Test
    fun `HTTP 오류 본문의 code message fieldErrors를 보존한다`() {
        val response = Response.error<ApiResponse<String>>(
            400,
            """{"code":"INVALID_FIELD","message":"입력값을 확인해주세요.","status":400,"fieldErrors":{"email":"invalid"}}"""
                .toResponseBody("application/json".toMediaType())
        )

        val error = response.toApiResult("요청에 실패했습니다.").exceptionOrNull() as ApiException

        assertEquals("INVALID_FIELD", error.code)
        assertEquals("입력값을 확인해주세요.", error.message)
        assertEquals("invalid", error.fieldErrors?.get("email"))
    }

    @Test
    fun `파싱할 수 없는 오류 본문은 fallback을 사용한다`() {
        val response = Response.error<ApiResponse<String>>(
            500,
            "not-json".toResponseBody("text/plain".toMediaType())
        )

        val error = response.toApiResult("서버 오류입니다.").exceptionOrNull() as ApiException

        assertEquals("UNKNOWN", error.code)
        assertEquals("서버 오류입니다.", error.message)
    }

    @Test
    fun `성공 envelope의 data를 Result 성공값으로 변환한다`() {
        val response = Response.success(ApiResponse(code = "SUCCESS", message = "성공", data = "payload"))

        val result = response.toApiResult("요청에 실패했습니다.")

        assertTrue(result.isSuccess)
        assertEquals("payload", result.getOrNull())
    }
}
