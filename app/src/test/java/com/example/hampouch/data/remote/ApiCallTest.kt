package com.example.hampouch.data.remote

import com.example.hampouch.data.remote.dto.ApiResponse
import com.example.hampouch.domain.model.ApiException
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.ResponseBody.Companion.toResponseBody
import java.io.IOException
import kotlinx.coroutines.CancellationException
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
        assertEquals(400, error.httpStatus)
    }

    @Test
    fun `표준 JSON 500은 HTTP status와 서버 code message를 함께 보존한다`() {
        val response = Response.error<ApiResponse<String>>(
            500,
            """{"code":"INTERNAL_ERROR","message":"서버 처리에 실패했습니다.","status":500}"""
                .toResponseBody("application/json".toMediaType())
        )

        val error = response.toApiResult("서버 오류입니다.").exceptionOrNull() as ApiException

        assertEquals(500, error.httpStatus)
        assertEquals("INTERNAL_ERROR", error.code)
        assertEquals("서버 처리에 실패했습니다.", error.message)
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
        assertEquals(500, error.httpStatus)
    }

    @Test
    fun `성공 envelope의 data를 Result 성공값으로 변환한다`() {
        val response = Response.success(ApiResponse(code = "SUCCESS", message = "성공", data = "payload"))

        val result = response.toApiResult("요청에 실패했습니다.")

        assertTrue(result.isSuccess)
        assertEquals("payload", result.getOrNull())
    }

    @Test
    fun `네트워크 오류를 별도 code로 변환한다`() {
        val result = runCatchingNetwork<String>("ApiCallTest") { throw IOException("offline") }

        val error = result.exceptionOrNull() as ApiException
        assertEquals("NETWORK_ERROR", error.code)
        assertEquals(null, error.httpStatus)
    }

    @Test(expected = CancellationException::class)
    fun `coroutine cancellation은 Result 실패로 삼키지 않는다`() {
        runCatchingNetwork<String>("ApiCallTest") { throw CancellationException("cancel") }
    }
}
