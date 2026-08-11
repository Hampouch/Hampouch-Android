package com.example.hampouch.data.remote

import android.util.Log
import com.example.hampouch.data.remote.dto.ApiErrorBody
import com.example.hampouch.domain.model.ApiException
import com.google.gson.Gson
import kotlinx.coroutines.CancellationException
import retrofit2.Response

// 서버 호출을 감싸는 공통 헬퍼. 도메인별 Repository 구현이 공유한다.

private val errorBodyGson = Gson()

/** 실패 응답의 body를 [ApiErrorBody]로 파싱해 [ApiException]으로 바꾼다. 파싱 실패 시 [fallbackMessage]. */
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

/**
 * 네트워크 호출 중 튀어나온 예외를 [ApiException] 실패로 정규화한다.
 *
 * [CancellationException]은 코루틴 취소 신호이므로 삼키지 않고 그대로 전파한다 — 이걸 실패로
 * 바꿔버리면 화면을 벗어난 뒤에도 에러 토스트가 뜬다.
 */
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

/** 인증이 필요한 호출에서 access token이 없을 때 쓰는 실패값. */
fun unauthorized(): ApiException =
    ApiException(code = "AUTH_UNAUTHORIZED", message = "인증이 필요합니다.")
