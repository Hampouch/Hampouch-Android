package com.example.hampouch.data.repository

import com.example.hampouch.core.config.RestConfig
import com.example.hampouch.data.remote.ChallengeApi
import com.example.hampouch.data.remote.RestApi
import com.example.hampouch.data.remote.dto.RestResumeRequest
import com.example.hampouch.data.remote.dto.RestResumeWhen
import com.example.hampouch.data.remote.dto.RestStartRequest
import com.example.hampouch.data.remote.runCatchingNetwork
import com.example.hampouch.data.remote.toApiResult
import com.example.hampouch.data.remote.unauthorized
import com.example.hampouch.domain.model.BreakDuration
import com.example.hampouch.domain.model.MAX_REST_DAYS
import com.example.hampouch.domain.model.RestState
import com.example.hampouch.domain.model.RestPeriod
import com.example.hampouch.domain.repository.RestRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.time.LocalDate
import javax.inject.Inject
import javax.inject.Singleton

private const val TAG = "RestRepository"

@Singleton
class RestRepositoryImpl @Inject constructor(
    private val challengeApi: ChallengeApi,
    private val restApi: RestApi,
    private val authRepository: AuthRepository
) : RestRepository {

    private val _restState = MutableStateFlow<RestState>(RestState.NotResting)
    override val restState: StateFlow<RestState> = _restState.asStateFlow()

    private fun resolveRestDays(period: RestPeriod): Int = when (period) {
        is RestPeriod.Preset -> when (period.duration) {
            BreakDuration.THREE_DAYS -> 3
            BreakDuration.ONE_WEEK -> 7
            BreakDuration.TWO_WEEKS -> 14
            BreakDuration.CONTINUOUS -> MAX_REST_DAYS
        }
        is RestPeriod.Custom -> period.days
    }

    override suspend fun syncStatus(): Result<Unit> {
        if (!RestConfig.USE_SERVER_REST) return Result.success(Unit)
        if (authRepository.currentAuthHeader() == null) return Result.failure(unauthorized())
        return runCatchingNetwork(TAG) {
            val response = challengeApi.getCurrentChallenge()
            when {
                response.code() == 404 -> {
                    _restState.value = RestState.NotResting
                    Result.success(Unit)
                }
                else -> response.toApiResult("휴식 상태를 확인하지 못했습니다.").map { data ->
                    val resumeDate = data.rest?.plannedResumeDate?.let(LocalDate::parse)
                    _restState.value = resumeDate?.let { RestState.Resting(it) } ?: RestState.NotResting
                }
            }
        }
    }

    override suspend fun startBreak(period: RestPeriod): Result<Unit> {
        if (!RestConfig.USE_SERVER_REST) {
            val days = resolveRestDays(period).toLong()
            _restState.value = RestState.Resting(LocalDate.now().plusDays(days))
            return Result.success(Unit)
        }
        if (authRepository.currentAuthHeader() == null) return Result.failure(unauthorized())
        return runCatchingNetwork(TAG) {
            val response = restApi.startRest(
                RestStartRequest(restDays = resolveRestDays(period))
            )
            response.toApiResult("휴식 시작에 실패했습니다.").map { data ->
                _restState.value = RestState.Resting(
                    restId = data.restId,
                    plannedResumeDate = LocalDate.parse(data.plannedResumeDate)
                )
            }
        }
    }

    /** 휴식 종료 알림에서 "더 쉬기"를 골랐을 때 — 이미 휴식 중이므로 시작이 아니라 연장(resume EXTEND)을 호출한다. */
    override suspend fun extendBreak(period: RestPeriod): Result<Unit> {
        if (!RestConfig.USE_SERVER_REST) {
            val days = resolveRestDays(period).toLong()
            val resumeDate = (_restState.value as? RestState.Resting)?.plannedResumeDate ?: LocalDate.now()
            _restState.value = RestState.Resting(resumeDate.plusDays(days))
            return Result.success(Unit)
        }
        return resume(RestResumeWhen.EXTEND, extendDays = resolveRestDays(period))
    }

    override suspend fun resumeNow(): Result<Unit> {
        if (!RestConfig.USE_SERVER_REST) {
            _restState.value = RestState.NotResting
            return Result.success(Unit)
        }
        return resume(RestResumeWhen.NOW)
    }

    override suspend fun postponeOneDay(): Result<Unit> {
        if (!RestConfig.USE_SERVER_REST) {
            val state = _restState.value as? RestState.Resting
                ?: return Result.failure(IllegalStateException("휴식 중일 때만 복귀를 하루 미룰 수 있습니다."))
            _restState.value = state.copy(plannedResumeDate = state.plannedResumeDate.plusDays(1))
            return Result.success(Unit)
        }
        return resume(RestResumeWhen.TOMORROW)
    }

    private suspend fun resume(whenValue: String, extendDays: Int? = null): Result<Unit> {
        if (authRepository.currentAuthHeader() == null) return Result.failure(unauthorized())
        return runCatchingNetwork(TAG) {
            val response = restApi.resumeRest(
                RestResumeRequest(`when` = whenValue, extendDays = extendDays)
            )
            response.toApiResult("복귀 처리에 실패했습니다.").mapCatching { data ->
                if (whenValue == RestResumeWhen.NOW) {
                    _restState.value = RestState.NotResting
                } else {
                    val resumeDate = (data.plannedResumeDate ?: data.resumeDate)?.let(LocalDate::parse)
                        ?: error("서버 응답에 복귀 예정일이 없습니다.")
                    _restState.value = RestState.Resting(
                        restId = data.restId,
                        plannedResumeDate = resumeDate
                    )
                }
            }
        }
    }

    override fun resetForAccount() {
        _restState.value = RestState.NotResting
    }
}
