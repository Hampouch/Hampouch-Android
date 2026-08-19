package com.example.hampouch.data.repository

import android.util.Log
import com.example.hampouch.core.config.MiniChallengeConfig
import com.example.hampouch.domain.model.MiniChallengeDaySummary
import com.example.hampouch.domain.model.MiniChallengeState
import com.example.hampouch.domain.repository.AccountScopedState
import com.example.hampouch.domain.repository.MiniChallengeRepository
import com.example.hampouch.domain.model.MiniChallengeEntry
import com.example.hampouch.domain.model.RecommendedMiniChallenge
import com.example.hampouch.domain.model.MiniChallengeDuration
import com.example.hampouch.domain.model.miniChallengeDuration
import com.example.hampouch.domain.model.ApiException
import com.example.hampouch.data.local.MiniChallengeMockDataSource
import com.example.hampouch.data.remote.MiniChallengeApi
import com.example.hampouch.data.remote.toApiException
import com.example.hampouch.data.remote.toApiResult
import com.example.hampouch.data.remote.dto.AddCustomMiniChallengeRequest
import com.example.hampouch.data.remote.dto.AddRecommendedMiniChallengeRequest
import com.example.hampouch.data.remote.dto.ApiResponse
import com.example.hampouch.data.remote.dto.CustomMiniChallengeBody
import com.example.hampouch.data.remote.dto.MiniChallengeCheckRequest
import com.example.hampouch.data.remote.dto.MiniChallengeCreatedData
import com.example.hampouch.data.remote.dto.MiniChallengeDayData
import com.example.hampouch.data.remote.dto.MiniChallengeItemDto
import com.example.hampouch.data.remote.dto.RecommendedMiniChallengeDto
import java.time.LocalDate
import java.util.UUID
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import retrofit2.Response

private const val TAG = "MiniChallengeRepository"

@Singleton
class MiniChallengeRepositoryImpl @Inject constructor(
    private val apiService: MiniChallengeApi,
    private val authRepository: AuthRepository,
    private val mockDataSource: MiniChallengeMockDataSource
) : MiniChallengeRepository, AccountScopedState {

    private fun seedState(): MiniChallengeState =
        if (MiniChallengeConfig.USE_SERVER_MINI_CHALLENGE) MiniChallengeState() else mockDataSource.initialState()

    private val _state = MutableStateFlow(seedState())
    override val state: StateFlow<MiniChallengeState> = _state.asStateFlow()
    private val serverCreationMutex = Mutex()

    private fun challengesFor(date: LocalDate) = _state.value.challengesFor(date)

    private fun setChallengesForDate(date: LocalDate, entries: List<MiniChallengeEntry>) {
        _state.update { it.copy(challengesByDate = it.challengesByDate + (date to entries)) }
    }

    private fun setSummaryForDate(date: LocalDate, summary: MiniChallengeDaySummary) {
        _state.update { it.copy(summaryByDate = it.summaryByDate + (date to summary)) }
    }

    private fun setDayResponse(date: LocalDate, body: MiniChallengeDayData) {
        setChallengesForDate(date, body.items.map { it.toDomain() })
        setSummaryForDate(
            date,
            MiniChallengeDaySummary(
                checkedCount = body.summary.checkedCount,
                totalCount = body.summary.totalCount,
                streakDays = body.summary.streakDays
            )
        )
    }

    private fun replaceRecommendedChallenges(items: List<RecommendedMiniChallenge>) {
        _state.update { it.copy(recommendedChallenges = items) }
    }

    private fun removeRecommended(id: String) {
        _state.update { it.copy(recommendedChallenges = it.recommendedChallenges.filterNot { r -> r.id == id }) }
    }

    private fun toggleLocal(date: LocalDate, id: String) {
        setChallengesForDate(
            date,
            challengesFor(date).map { entry ->
                if (entry.id == id) entry.copy(isChecked = !entry.isChecked) else entry
            }
        )
    }

    private fun removeLocal(date: LocalDate, id: String) {
        setChallengesForDate(date, challengesFor(date).filterNot { it.id == id })
    }

    private fun addLocal(date: LocalDate, name: String, duration: MiniChallengeDuration): Boolean {
        val trimmedName = validMiniChallengeNameOrNull(name) ?: return false
        if (_state.value.isNameTaken(date, trimmedName)) return false
        setChallengesForDate(
            date,
            challengesFor(date) + MiniChallengeEntry(
                id = UUID.randomUUID().toString(),
                name = trimmedName,
                duration = duration,
                achievedDays = 0,
                isChecked = false,
                startDate = date
            )
        )
        return true
    }

    private fun addRecommendedLocal(date: LocalDate, recommended: RecommendedMiniChallenge): Boolean {
        val added = addLocal(date, recommended.name, recommended.duration)
        if (added) removeRecommended(recommended.id)
        return added
    }

    override fun resetForAccount() {
        _state.value = seedState()
    }

    override suspend fun loadChallenges(date: LocalDate): Result<Unit> {
        if (!MiniChallengeConfig.USE_SERVER_MINI_CHALLENGE) return Result.success(Unit)
        return runCatching {
            requireAuthentication()
            val response = apiService.getMiniChallenges(date.toString())
            val body = requireBody(response, "미니 챌린지 조회에 실패했습니다.")
            setDayResponse(date, body)
        }.onFailure { rethrowIfCancelled(it, "미니 챌린지 조회") }
    }

    override suspend fun loadRecommended(durationDays: Int?): Result<Unit> {
        if (!MiniChallengeConfig.USE_SERVER_MINI_CHALLENGE) return Result.success(Unit)
        return runCatching {
            requireAuthentication()
            val response = apiService.getRecommendedMiniChallenges(durationDays)
            val body = requireBody(response, "추천 목록 조회에 실패했습니다.")
            replaceRecommendedChallenges(body.items.map { it.toDomain() })
        }.onFailure { rethrowIfCancelled(it, "미니 챌린지 추천 목록 조회") }
    }

    override suspend fun addRecommended(date: LocalDate, recommended: RecommendedMiniChallenge): Result<LocalDate> {
        if (!MiniChallengeConfig.USE_SERVER_MINI_CHALLENGE) {
            val added = addRecommendedLocal(date, recommended)
            return if (added) Result.success(date) else Result.failure(
                ApiException(code = "MINI_DUPLICATE", message = "이미 추가된 미니 챌린지입니다.")
            )
        }
        return serverCreationMutex.withLock {
            runCatching {
                requireAuthentication()
                ensureServerCreationNameAvailable(recommended.name)
                val recommendedId = recommended.id.toLongOrNull()
                    ?: throw ApiException(
                        code = "MINI_RECOMMENDED_NOT_FOUND",
                        message = "추천 미니 챌린지를 찾을 수 없습니다."
                    )
                val response = apiService.addRecommendedMiniChallenge(
                    AddRecommendedMiniChallengeRequest(recommendedId = recommendedId)
                )
                val created = requireBody(response, "미니 챌린지 추가에 실패했습니다.")
                val createdDate = created.createdStartDate()
                loadChallenges(createdDate).getOrThrow()
                createdDate
            }
        }.onFailure { rethrowIfCancelled(it, "미니 챌린지 추가(추천)") }
    }

    override suspend fun addCustom(
        date: LocalDate,
        name: String,
        duration: MiniChallengeDuration
    ): Result<LocalDate> {
        val trimmedName = validMiniChallengeNameOrNull(name)
            ?: return Result.failure(
                ApiException(code = "MINI_NAME_REQUIRED", message = "미니 챌린지 이름을 입력해주세요.")
            )
        if (!MiniChallengeConfig.USE_SERVER_MINI_CHALLENGE) {
            val added = addLocal(date, trimmedName, duration)
            return if (added) Result.success(date) else Result.failure(
                ApiException(code = "MINI_DUPLICATE", message = "이미 추가된 미니 챌린지입니다.")
            )
        }
        return serverCreationMutex.withLock {
            runCatching {
                requireAuthentication()
                ensureServerCreationNameAvailable(trimmedName)
                val response = apiService.addCustomMiniChallenge(
                    AddCustomMiniChallengeRequest(
                        custom = CustomMiniChallengeBody(
                            title = trimmedName,
                            durationDays = duration.serverDays
                        )
                    )
                )
                val created = requireBody(response, "미니 챌린지 추가에 실패했습니다.")
                val createdDate = created.createdStartDate()
                loadChallenges(createdDate).getOrThrow()
                createdDate
            }
        }.onFailure { rethrowIfCancelled(it, "미니 챌린지 추가(커스텀)") }
    }

    private suspend fun ensureServerCreationNameAvailable(name: String) {
        val response = apiService.getMiniChallenges()
        val body = requireBody(response, "오늘의 미니 챌린지 조회에 실패했습니다.")
        val serverCreationDate = LocalDate.parse(body.date)
        setDayResponse(serverCreationDate, body)
        if (_state.value.isNameTaken(serverCreationDate, name)) {
            throw ApiException(code = "MINI_DUPLICATE", message = "이미 추가된 미니 챌린지입니다.")
        }
    }

    override suspend fun remove(date: LocalDate, id: String): Result<Unit> {
        if (!MiniChallengeConfig.USE_SERVER_MINI_CHALLENGE) {
            removeLocal(date, id)
            return Result.success(Unit)
        }
        return runCatching {
            requireAuthentication()
            val miniChallengeId = id.toLongOrNull()
                ?: throw ApiException(code = "MINI_NOT_FOUND", message = "미니 챌린지를 찾을 수 없습니다.")
            val response = apiService.deleteMiniChallenge(miniChallengeId)
            if (!response.isSuccessful) throw response.toApiException("미니 챌린지 삭제에 실패했습니다.")
            loadChallenges(date).getOrThrow()
        }.onFailure { rethrowIfCancelled(it, "미니 챌린지 삭제") }
    }

    override suspend fun setChecked(date: LocalDate, id: String, checked: Boolean): Result<Unit> {
        if (!MiniChallengeConfig.USE_SERVER_MINI_CHALLENGE) {
            toggleLocal(date, id)
            return Result.success(Unit)
        }
        return runCatching {
            requireAuthentication()
            val miniChallengeId = id.toLongOrNull()
                ?: throw ApiException(code = "MINI_NOT_FOUND", message = "미니 챌린지를 찾을 수 없습니다.")
            val response = apiService.checkMiniChallenge(
                miniChallengeId,
                MiniChallengeCheckRequest(date = date.toString(), checked = checked)
            )
            requireBody(response, "체크 상태 변경에 실패했습니다.")
            loadChallenges(date).getOrThrow()
        }.onFailure { rethrowIfCancelled(it, "미니 챌린지 체크") }
    }

    private suspend fun requireAuthentication() {
        if (authRepository.userSession.first() == null) {
            throw ApiException(code = "AUTH_UNAUTHORIZED", message = "로그인이 필요합니다.")
        }
    }

    private fun <T> requireBody(response: Response<ApiResponse<T>>, fallbackMessage: String): T {
        return response.toApiResult(fallbackMessage).getOrThrow()
    }

    private fun rethrowIfCancelled(error: Throwable, action: String) {
        if (error is CancellationException) throw error
        Log.e(TAG, "$action 네트워크 오류", error)
    }

}

internal fun validMiniChallengeNameOrNull(name: String): String? =
    name.trim().takeIf { it.isNotEmpty() }

private fun MiniChallengeItemDto.toDomain(): MiniChallengeEntry = MiniChallengeEntry(
    id = miniChallengeId.toString(),
    name = title,
    duration = miniChallengeDuration(durationDays),
    achievedDays = progressDays,
    isChecked = checked
)

private fun RecommendedMiniChallengeDto.toDomain(): RecommendedMiniChallenge = RecommendedMiniChallenge(
    id = recommendedId.toString(),
    duration = miniChallengeDuration(durationDays),
    name = title
)

internal fun MiniChallengeCreatedData.createdStartDate(): LocalDate = LocalDate.parse(startDate)
