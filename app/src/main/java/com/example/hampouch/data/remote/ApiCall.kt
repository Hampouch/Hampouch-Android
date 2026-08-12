package com.example.hampouch.data.remote

import android.util.Log
import com.example.hampouch.data.remote.dto.ApiErrorBody
import com.example.hampouch.data.remote.dto.ApiResponse
import com.example.hampouch.domain.model.ApiException
import com.google.gson.Gson
import kotlinx.coroutines.CancellationException
import retrofit2.Response
import java.io.IOException

// 서버 호출을 감싸는 공통 헬퍼. 도메인별 Repository 구현이 공유한다.

private val errorBodyGson = Gson()

fun Response<*>.toApiException(fallbackMessage: String): ApiException {
    val error = errorBody()?.string()?.let {
        runCatching { errorBodyGson.fromJson(it, ApiErrorBody::class.java) }.getOrNull()
    }
    return ApiException(
        code = error?.code ?: "UNKNOWN",
        message = error?.message ?: fallbackMessage,
        fieldErrors = error?.fieldErrors
    )
}

/** HTTP 상태와 서버 envelope를 data 계층의 공통 [Result] 경계로 변환한다. */
fun <T> Response<ApiResponse<T>>.toApiResult(fallbackMessage: String): Result<T> {
    if (!isSuccessful) return Result.failure(toApiException(fallbackMessage))

    val envelope = body()
        ?: return Result.failure(ApiException(code = "EMPTY_RESPONSE", message = fallbackMessage))
    val data = envelope.data
        ?: return Result.failure(
            ApiException(
                code = envelope.code.ifBlank { "EMPTY_RESPONSE" },
                message = envelope.message.ifBlank { fallbackMessage }
            )
        )
    return Result.success(data)
}

inline fun <T> runCatchingNetwork(tag: String, action: () -> Result<T>): Result<T> = try {
    action()
} catch (e: CancellationException) {
    throw e
} catch (e: ApiException) {
    Log.e(tag, "API 오류", e)
    Result.failure(e)
} catch (e: IOException) {
    Log.e(tag, "네트워크 오류", e)
    Result.failure(ApiException(code = "NETWORK_ERROR", message = "인터넷 연결을 확인해주세요."))
} catch (e: Exception) {
    // 응답 파싱 실패 등 연결과 무관한 오류. 네트워크 문제로 오인하지 않도록 메시지를 분리한다.
    Log.e(tag, "처리 중 오류", e)
    Result.failure(ApiException(code = "UNKNOWN", message = "일시적인 오류가 발생했어요."))
}

/** 인증이 필요한 호출에서 access token이 없을 때 쓰는 실패값. */
fun unauthorized(): ApiException =
    ApiException(code = "AUTH_UNAUTHORIZED", message = "인증이 필요합니다.")
