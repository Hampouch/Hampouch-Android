package com.example.hampouch.data.repository

import com.example.hampouch.core.config.RestConfig
import com.example.hampouch.data.remote.ApiService
import com.example.hampouch.data.remote.dto.RestResumeRequest
import com.example.hampouch.data.remote.dto.RestResumeWhen
import com.example.hampouch.data.remote.dto.RestStartRequest
import com.example.hampouch.data.remote.runCatchingNetwork
import com.example.hampouch.data.remote.toApiException
import com.example.hampouch.data.remote.unauthorized
import com.example.hampouch.domain.model.BreakDuration
import com.example.hampouch.domain.model.RestState
import com.example.hampouch.domain.repository.RestRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import java.time.LocalDate
import javax.inject.Inject
import javax.inject.Singleton

private const val TAG = "RestRepository"

private const val MAX_REST_DAYS = 3650

@Singleton
class RestRepositoryImpl @Inject constructor(
    private val apiService: ApiService,
    private val authRepository: AuthRepository
) : RestRepository {

    private val _restState = MutableStateFlow(RestState())
    override val restState: StateFlow<RestState> = _restState.asStateFlow()

    private fun resolveRestDays(duration: BreakDuration?, customDays: Int?): Int = when (duration) {
        BreakDuration.THREE_DAYS -> 3
        BreakDuration.ONE_WEEK -> 7
        BreakDuration.TWO_WEEKS -> 14
        BreakDuration.CONTINUOUS -> MAX_REST_DAYS
        null -> (customDays ?: 7).coerceIn(1, MAX_REST_DAYS)
    }

    override suspend fun syncStatus(): Result<Unit> {
        if (!RestConfig.USE_SERVER_REST) return Result.success(Unit)
        val header = authRepository.currentAuthHeader() ?: return Result.failure(unauthorized())
        return runCatchingNetwork(TAG) {
            val response = apiService.getCurrentChallenge(header)
            when {
                response.isSuccessful -> {
                    val resumeDate = response.body()?.data?.rest?.plannedResumeDate?.let(LocalDate::parse)
                    _restState.update { it.copy(plannedResumeDate = resumeDate) }
                    Result.success(Unit)
                }
                response.code() == 404 -> {
                    _restState.update { it.copy(plannedResumeDate = null) }
                    Result.success(Unit)
                }
                else -> Result.failure(response.toApiException("휴식 상태를 확인하지 못했습니다."))
            }
        }
    }

    override suspend fun startBreak(duration: BreakDuration?, customDays: Int?): Result<Unit> {
        if (!RestConfig.USE_SERVER_REST) {
            val days = resolveRestDays(duration, customDays).toLong()
            _restState.update { it.copy(plannedResumeDate = LocalDate.now().plusDays(days)) }
            return Result.success(Unit)
        }
        val header = authRepository.currentAuthHeader() ?: return Result.failure(unauthorized())
        return runCatchingNetwork(TAG) {
            val response = apiService.startRest(
                header,
                RestStartRequest(restDays = resolveRestDays(duration, customDays))
            )
            val data = response.body()?.data
            if (response.isSuccessful && data != null) {
                _restState.value = RestState(
                    restId = data.restId,
                    plannedResumeDate = LocalDate.parse(data.plannedResumeDate)
                )
                Result.success(Unit)
            } else {
                Result.failure(response.toApiException("휴식 시작에 실패했습니다."))
            }
        }
    }

    override suspend fun extendBreak(duration: BreakDuration?, customDays: Int?): Result<Unit> {
        if (!RestConfig.USE_SERVER_REST) {
            val days = resolveRestDays(duration, customDays).toLong()
            _restState.update {
                it.copy(plannedResumeDate = (it.plannedResumeDate ?: LocalDate.now()).plusDays(days))
            }
            return Result.success(Unit)
        }
        return resume(RestResumeWhen.EXTEND, extendDays = resolveRestDays(duration, customDays))
    }

    override suspend fun resumeNow(): Result<Unit> {
        if (!RestConfig.USE_SERVER_REST) {
            _restState.update { it.copy(plannedResumeDate = null) }
            return Result.success(Unit)
        }
        return resume(RestResumeWhen.NOW)
    }

    override suspend fun postponeOneDay(): Result<Unit> {
        if (!RestConfig.USE_SERVER_REST) {
            _restState.update { it.copy(plannedResumeDate = it.plannedResumeDate?.plusDays(1)) }
            return Result.success(Unit)
        }
        return resume(RestResumeWhen.TOMORROW)
    }

    private suspend fun resume(whenValue: String, extendDays: Int? = null): Result<Unit> {
        val header = authRepository.currentAuthHeader() ?: return Result.failure(unauthorized())
        return runCatchingNetwork(TAG) {
            val response = apiService.resumeRest(
                header,
                RestResumeRequest(`when` = whenValue, extendDays = extendDays)
            )
            val data = response.body()?.data
            if (response.isSuccessful && data != null) {
                _restState.value = RestState(
                    restId = data.restId,
                    plannedResumeDate = if (whenValue == RestResumeWhen.NOW) {
                        null
                    } else {
                        (data.plannedResumeDate ?: data.resumeDate)?.let(LocalDate::parse)
                    }
                )
                Result.success(Unit)
            } else {
                Result.failure(response.toApiException("복귀 처리에 실패했습니다."))
            }
        }
    }

    override fun resetForAccount() {
        _restState.value = RestState()
    }
}
