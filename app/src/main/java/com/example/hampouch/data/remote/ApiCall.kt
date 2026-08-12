package com.example.hampouch.data.remote

import android.util.Log
import com.example.hampouch.data.remote.dto.ApiErrorBody
import com.example.hampouch.domain.model.ApiException
import com.google.gson.Gson
import kotlinx.coroutines.CancellationException
import retrofit2.Response


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

inline fun <T> runCatchingNetwork(tag: String, action: () -> Result<T>): Result<T> = try {
    action()
} catch (e: CancellationException) {
    throw e
} catch (e: ApiException) {
    Log.e(tag, "API 오류", e)
    Result.failure(e)
} catch (e: Exception) {
    Log.e(tag, "네트워크 오류", e)
    Result.failure(ApiException(code = "NETWORK_ERROR", message = "인터넷 연결을 확인해주세요."))
}

fun unauthorized(): ApiException =
    ApiException(code = "AUTH_UNAUTHORIZED", message = "인증이 필요합니다.")
