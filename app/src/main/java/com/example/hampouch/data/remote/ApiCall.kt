package com.example.hampouch.data.remote

import android.util.Log
import com.example.hampouch.data.remote.dto.ApiErrorBody
import com.example.hampouch.data.remote.dto.ApiResponse
import com.example.hampouch.domain.model.ApiException
import com.google.gson.Gson
import kotlinx.coroutines.CancellationException
import retrofit2.Response
import java.io.IOException


private val errorBodyGson = Gson()

fun Response<*>.toApiException(fallbackMessage: String): ApiException {
    val error = errorBody()?.string()?.let {
        runCatching { errorBodyGson.fromJson(it, ApiErrorBody::class.java) }.getOrNull()
    }
    return ApiException(
        code = error?.code ?: "UNKNOWN",
        message = error?.message ?: fallbackMessage,
        fieldErrors = error?.fieldErrors,
        httpStatus = code()
    )
}

fun <T> Response<ApiResponse<T>>.toApiResult(fallbackMessage: String): Result<T> {
    if (!isSuccessful) return Result.failure(toApiException(fallbackMessage))

    val envelope = body()
        ?: return Result.failure(
            ApiException(code = "EMPTY_RESPONSE", message = fallbackMessage, httpStatus = code())
        )
    val data = envelope.data
        ?: return Result.failure(
            ApiException(
                code = envelope.code.ifBlank { "EMPTY_RESPONSE" },
                message = envelope.message.ifBlank { fallbackMessage },
                httpStatus = code()
            )
        )
    return Result.success(data)
}

inline fun <T> runCatchingNetwork(tag: String, action: () -> Result<T>): Result<T> = try {
    action()
} catch (e: CancellationException) {
    throw e
} catch (e: ApiException) {
    safeErrorLog(tag, "API 오류", e)
    Result.failure(e)
} catch (e: IOException) {
    safeErrorLog(tag, "네트워크 오류", e)
    Result.failure(ApiException(code = "NETWORK_ERROR", message = "인터넷 연결을 확인해주세요."))
} catch (e: Exception) {
    safeErrorLog(tag, "처리 중 오류", e)
    Result.failure(ApiException(code = "UNKNOWN", message = "일시적인 오류가 발생했어요."))
}

fun unauthorized(): ApiException =
    ApiException(code = "AUTH_UNAUTHORIZED", message = "인증이 필요합니다.")

@PublishedApi
internal fun safeErrorLog(tag: String, message: String, error: Throwable) {
    runCatching { Log.e(tag, message, error) }
}
