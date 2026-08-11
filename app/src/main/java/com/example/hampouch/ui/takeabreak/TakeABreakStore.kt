package com.example.hampouch.ui.takeabreak

import android.content.Context
import android.util.Log
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import com.example.hampouch.core.config.RestConfig
import com.example.hampouch.core.network.NetworkModule
import com.example.hampouch.data.remote.ApiException
import com.example.hampouch.data.remote.dto.ApiErrorBody
import com.example.hampouch.data.remote.dto.RestResumeRequest
import com.example.hampouch.data.remote.dto.RestResumeWhen
import com.example.hampouch.data.remote.dto.RestStartRequest
import com.example.hampouch.data.repository.AuthRepository
import com.google.gson.Gson
import kotlinx.coroutines.CancellationException
import retrofit2.Response
import java.time.LocalDate

private const val TAG = "TakeABreakStore"

// 서버(restDays·extendDays) 방어 상한. "계속 쉬기"는 서버에 무기한 개념이 없어 이 상한으로 대체 전송한다.
private const val MAX_REST_DAYS = 3650

object TakeABreakStore {

    /** 휴식 도메인엔 상태 조회 API가 없어, 생성/복귀 응답으로만 채워진다. syncStatus()가 challenge 도메인의 GET /api/challenges/current(rest 블록)로 보정. */
    var restId: Long? by mutableStateOf(null)
        private set

    /** 복귀(예정)일 — 이 날짜부터는 휴식이 아니다. null이면 휴식 중이 아님. */
    var plannedResumeDate: LocalDate? by mutableStateOf(null)
        private set

    private lateinit var appContext: Context
    private val authRepository: AuthRepository get() = AuthRepository.getInstance(appContext)

    fun attach(context: Context) {
        appContext = context.applicationContext
    }

    fun isBreakOver(referenceToday: LocalDate): Boolean {
        val resumeDate = plannedResumeDate ?: return false
        return !referenceToday.isBefore(resumeDate)
    }

    private fun resolveRestDays(duration: BreakDuration?, customDays: Int?): Int = when (duration) {
        BreakDuration.THREE_DAYS -> 3
        BreakDuration.ONE_WEEK -> 7
        BreakDuration.TWO_WEEKS -> 14
        BreakDuration.CONTINUOUS -> MAX_REST_DAYS
        null -> (customDays ?: 7).coerceIn(1, MAX_REST_DAYS)
    }

    /** 앱 재시작 등으로 메모리 상태가 비어있을 때, 서버 기준 휴식 상태로 동기화한다. */
    suspend fun syncStatus(): Result<Unit> {
        if (!RestConfig.USE_SERVER_REST) return Result.success(Unit)
        val header = requireAuthHeader().getOrElse { return Result.failure(it) }
        return runCatchingNetwork {
            val response = NetworkModule.apiService.getCurrentChallenge(header)
            when {
                response.isSuccessful -> {
                    plannedResumeDate = response.body()?.data?.rest?.plannedResumeDate?.let(LocalDate::parse)
                    Result.success(Unit)
                }
                response.code() == 404 -> {
                    plannedResumeDate = null
                    Result.success(Unit)
                }
                else -> Result.failure(errorFrom(response, "휴식 상태를 확인하지 못했습니다."))
            }
        }
    }

    suspend fun startBreak(duration: BreakDuration?, customDays: Int?): Result<Unit> {
        if (!RestConfig.USE_SERVER_REST) {
            plannedResumeDate = LocalDate.now().plusDays(resolveRestDays(duration, customDays).toLong())
            return Result.success(Unit)
        }
        val header = requireAuthHeader().getOrElse { return Result.failure(it) }
        return runCatchingNetwork {
            val response = NetworkModule.apiService.startRest(
                header,
                RestStartRequest(restDays = resolveRestDays(duration, customDays))
            )
            val data = response.body()?.data
            if (response.isSuccessful && data != null) {
                restId = data.restId
                plannedResumeDate = LocalDate.parse(data.plannedResumeDate)
                Result.success(Unit)
            } else {
                Result.failure(errorFrom(response, "휴식 시작에 실패했습니다."))
            }
        }
    }

    /** 휴식 종료 알림에서 "더 쉬기"를 골랐을 때 — 이미 휴식 중이므로 시작이 아니라 연장(resume EXTEND)을 호출한다. */
    suspend fun extendBreak(duration: BreakDuration?, customDays: Int?): Result<Unit> {
        if (!RestConfig.USE_SERVER_REST) {
            val base = plannedResumeDate ?: LocalDate.now()
            plannedResumeDate = base.plusDays(resolveRestDays(duration, customDays).toLong())
            return Result.success(Unit)
        }
        return resume(RestResumeWhen.EXTEND, extendDays = resolveRestDays(duration, customDays))
    }

    suspend fun resumeNow(): Result<Unit> {
        if (!RestConfig.USE_SERVER_REST) {
            plannedResumeDate = null
            return Result.success(Unit)
        }
        return resume(RestResumeWhen.NOW)
    }

    suspend fun postponeOneDay(): Result<Unit> {
        if (!RestConfig.USE_SERVER_REST) {
            plannedResumeDate = plannedResumeDate?.plusDays(1)
            return Result.success(Unit)
        }
        return resume(RestResumeWhen.TOMORROW)
    }

    private suspend fun resume(whenValue: String, extendDays: Int? = null): Result<Unit> {
        val header = requireAuthHeader().getOrElse { return Result.failure(it) }
        return runCatchingNetwork {
            val response = NetworkModule.apiService.resumeRest(
                header,
                RestResumeRequest(`when` = whenValue, extendDays = extendDays)
            )
            val data = response.body()?.data
            if (response.isSuccessful && data != null) {
                restId = data.restId
                plannedResumeDate = if (whenValue == RestResumeWhen.NOW) {
                    null
                } else {
                    (data.plannedResumeDate ?: data.resumeDate)?.let(LocalDate::parse)
                }
                Result.success(Unit)
            } else {
                Result.failure(errorFrom(response, "복귀 처리에 실패했습니다."))
            }
        }
    }

    fun resetForAccount() {
        restId = null
        plannedResumeDate = null
    }

    private suspend fun requireAuthHeader(): Result<String> {
        val header = authRepository.currentAuthHeader()
        return if (header != null) {
            Result.success(header)
        } else {
            Result.failure(ApiException(code = "AUTH_UNAUTHORIZED", message = "인증이 필요합니다."))
        }
    }

    private fun errorFrom(response: Response<*>, fallbackMessage: String): ApiException {
        val error = response.errorBody()?.string()?.let {
            runCatching { Gson().fromJson(it, ApiErrorBody::class.java) }.getOrNull()
        }
        return ApiException(code = error?.code ?: "UNKNOWN", message = error?.message ?: fallbackMessage)
    }

    private inline fun <T> runCatchingNetwork(action: () -> Result<T>): Result<T> = try {
        action()
    } catch (e: CancellationException) {
        throw e
    } catch (e: ApiException) {
        Log.e(TAG, "휴식 API 오류", e)
        Result.failure(e)
    } catch (e: Exception) {
        Log.e(TAG, "휴식 네트워크 오류", e)
        Result.failure(ApiException(code = "NETWORK_ERROR", message = "인터넷 연결을 확인해주세요."))
    }
}
