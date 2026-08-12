package com.example.hampouch.data.repository

import android.util.Log
import com.example.hampouch.core.config.MiniChallengeConfig
import com.example.hampouch.domain.model.MiniChallengeDaySummary
import com.example.hampouch.domain.model.MiniChallengeState
import com.example.hampouch.domain.repository.AccountScopedState
import com.example.hampouch.domain.repository.MiniChallengeRepository
import com.example.hampouch.domain.model.MiniChallengeEntry
import com.example.hampouch.domain.model.RecommendedMiniChallenge
import com.example.hampouch.domain.model.ApiException
import com.example.hampouch.data.local.MiniChallengeMockDataSource
import com.example.hampouch.data.remote.ApiService
import com.example.hampouch.data.remote.dto.AddCustomMiniChallengeRequest
import com.example.hampouch.data.remote.dto.AddRecommendedMiniChallengeRequest
import com.example.hampouch.data.remote.dto.ApiErrorBody
import com.example.hampouch.data.remote.dto.ApiResponse
import com.example.hampouch.data.remote.dto.CustomMiniChallengeBody
import com.example.hampouch.data.remote.dto.MiniChallengeCheckRequest
import com.example.hampouch.data.remote.dto.MiniChallengeItemDto
import com.example.hampouch.data.remote.dto.RecommendedMiniChallengeDto
import com.google.gson.Gson
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
import retrofit2.Response

private const val TAG = "MiniChallengeRepository"

// 미니 챌린지 연동
@Singleton
class MiniChallengeRepositoryImpl @Inject constructor(
    private val apiService: ApiService,
    private val authRepository: AuthRepository,
    private val mockDataSource: MiniChallengeMockDataSource
) : MiniChallengeRepository, AccountScopedState {

    private fun seedState(): MiniChallengeState =
        if (MiniChallengeConfig.USE_SERVER_MINI_CHALLENGE) MiniChallengeState() else mockDataSource.initialState()

    private val _state = MutableStateFlow(seedState())
    override val state: StateFlow<MiniChallengeState> = _state.asStateFlow()

    private fun challengesFor(date: LocalDate) = _state.value.challengesFor(date)

    private fun setChallengesForDate(date: LocalDate, entries: List<MiniChallengeEntry>) {
        _state.update { it.copy(challengesByDate = it.challengesByDate + (date to entries)) }
    }

    private fun setSummaryForDate(date: LocalDate, summary: MiniChallengeDaySummary) {
        _state.update { it.copy(summaryByDate = it.summaryByDate + (date to summary)) }
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

    /** 중복 이름이면 추가하지 않고 false. */
    private fun addLocal(date: LocalDate, name: String, totalDays: Int?): Boolean {
        val trimmedName = name.trim().ifBlank { "이름 없는 챌린지" }
        if (_state.value.isNameTaken(date, trimmedName)) return false
        setChallengesForDate(
            date,
            challengesFor(date) + MiniChallengeEntry(
                id = UUID.randomUUID().toString(),
                name = trimmedName,
                totalDays = totalDays,
                achievedDays = 0,
                isChecked = false,
                startDate = date
            )
        )
        return true
    }

    private fun addRecommendedLocal(date: LocalDate, recommended: RecommendedMiniChallenge): Boolean {
        val added = addLocal(date, recommended.name, recommended.totalDays)
        if (added) removeRecommended(recommended.id)
        return added
    }

    override fun resetForAccount() {
        _state.value = seedState()
    }

    /** [date]의 미니 챌린지 목록/요약을 조회해 [MiniChallengeStore]에 반영한다. */
    override suspend fun loadChallenges(date: LocalDate): Result<Unit> {
        if (!MiniChallengeConfig.USE_SERVER_MINI_CHALLENGE) return Result.success(Unit)
        return runCatching {
            val authorization = requireAuthorizationHeader()
            val response = apiService.getMiniChallenges(authorization, date.toString())
            val body = requireBody(response, "미니 챌린지 조회에 실패했습니다.")
            setChallengesForDate(date, body.items.map { it.toDomain() })
            setSummaryForDate(
                date,
                MiniChallengeDaySummary(
                    checkedCount = body.summary.checkedCount,
                    totalCount = body.summary.totalCount,
                    streakDays = body.summary.streakDays
                )
            )
        }.onFailure { rethrowIfCancelled(it, "미니 챌린지 조회") }
    }

    /** 추천 카탈로그를 조회해 [MiniChallengeStore]에 반영한다. [durationDays]가 null이면 전체 기간. */
    override suspend fun loadRecommended(durationDays: Int?): Result<Unit> {
        if (!MiniChallengeConfig.USE_SERVER_MINI_CHALLENGE) return Result.success(Unit)
        return runCatching {
            val authorization = requireAuthorizationHeader()
            val response = apiService.getRecommendedMiniChallenges(authorization, durationDays)
            val body = requireBody(response, "추천 목록 조회에 실패했습니다.")
            replaceRecommendedChallenges(body.items.map { it.toDomain() })
        }.onFailure { rethrowIfCancelled(it, "미니 챌린지 추천 목록 조회") }
    }

    /**
     * 추천 카탈로그의 [recommended]를 내 미니 챌린지로 추가한다.
     *
     * 서버(/api/mini-challenges) 요청에는 날짜 필드가 없어 항상 서버의 오늘 날짜부터 시작하는 챌린지가
     * 생긴다 — [date](화면에서 보고 있던 탭)는 실제 생성 결과와 무관하다. 그래서 목데이터 모드에서만 [date]
     * 그대로, 서버 모드에서는 항상 오늘 날짜를 반환한다. 성공 시 반환되는 날짜가 실제로 새 항목이 추가된
     * 날짜이므로, 호출한 화면은 그 날짜로 선택 탭을 옮겨야 방금 추가한 항목을 바로 볼 수 있다.
     * 중복 이름 등으로 추가되지 않았으면 null.
     */
    override suspend fun addRecommended(date: LocalDate, recommended: RecommendedMiniChallenge): Result<LocalDate?> {
        if (!MiniChallengeConfig.USE_SERVER_MINI_CHALLENGE) {
            val added = addRecommendedLocal(date, recommended)
            return Result.success(if (added) date else null)
        }
        return runCatching {
            val authorization = requireAuthorizationHeader()
            val recommendedId = recommended.id.toLongOrNull()
                ?: throw ApiException(code = "MINI_RECOMMENDED_NOT_FOUND", message = "추천 미니 챌린지를 찾을 수 없습니다.")
            val response = apiService.addRecommendedMiniChallenge(
                authorization,
                AddRecommendedMiniChallengeRequest(recommendedId = recommendedId)
            )
            requireBody(response, "미니 챌린지 추가에 실패했습니다.")
            removeRecommended(recommended.id)
            val today = LocalDate.now()
            loadChallenges(today).getOrThrow()
            today
        }.onFailure { rethrowIfCancelled(it, "미니 챌린지 추가(추천)") }
    }

    /**
     * 커스텀 미니 챌린지를 새로 만든다. [addRecommended]와 동일하게, 서버 모드에서는 항상 오늘 날짜부터
     * 생성되므로 [date]가 아니라 실제 반영된 날짜를 반환한다. 중복 이름 등으로 추가되지 않았으면 null.
     */
    override suspend fun addCustom(date: LocalDate, name: String, totalDays: Int?): Result<LocalDate?> {
        if (!MiniChallengeConfig.USE_SERVER_MINI_CHALLENGE) {
            val added = addLocal(date, name, totalDays)
            return Result.success(if (added) date else null)
        }
        return runCatching {
            val authorization = requireAuthorizationHeader()
            val response = apiService.addCustomMiniChallenge(
                authorization,
                AddCustomMiniChallengeRequest(
                    custom = CustomMiniChallengeBody(title = name.trim(), durationDays = totalDays.toDurationDays())
                )
            )
            requireBody(response, "미니 챌린지 추가에 실패했습니다.")
            val today = LocalDate.now()
            loadChallenges(today).getOrThrow()
            today
        }.onFailure { rethrowIfCancelled(it, "미니 챌린지 추가(커스텀)") }
    }

    /** [id]의 미니 챌린지를 삭제한다. */
    override suspend fun remove(date: LocalDate, id: String): Result<Unit> {
        if (!MiniChallengeConfig.USE_SERVER_MINI_CHALLENGE) {
            removeLocal(date, id)
            return Result.success(Unit)
        }
        return runCatching {
            val authorization = requireAuthorizationHeader()
            val miniChallengeId = id.toLongOrNull()
                ?: throw ApiException(code = "MINI_NOT_FOUND", message = "미니 챌린지를 찾을 수 없습니다.")
            val response = apiService.deleteMiniChallenge(authorization, miniChallengeId)
            if (!response.isSuccessful) throw parseError(response.errorBody()?.string(), "미니 챌린지 삭제에 실패했습니다.")
            loadChallenges(date).getOrThrow()
        }.onFailure { rethrowIfCancelled(it, "미니 챌린지 삭제") }
    }

    /** [date] 기준으로 [id]의 체크 상태를 [checked]로 바꾼다(PUT은 멱등이라 재시도해도 안전). */
    override suspend fun setChecked(date: LocalDate, id: String, checked: Boolean): Result<Unit> {
        if (!MiniChallengeConfig.USE_SERVER_MINI_CHALLENGE) {
            toggleLocal(date, id)
            return Result.success(Unit)
        }
        return runCatching {
            val authorization = requireAuthorizationHeader()
            val miniChallengeId = id.toLongOrNull()
                ?: throw ApiException(code = "MINI_NOT_FOUND", message = "미니 챌린지를 찾을 수 없습니다.")
            val response = apiService.checkMiniChallenge(
                authorization,
                miniChallengeId,
                MiniChallengeCheckRequest(date = date.toString(), checked = checked)
            )
            requireBody(response, "체크 상태 변경에 실패했습니다.")
            loadChallenges(date).getOrThrow()
        }.onFailure { rethrowIfCancelled(it, "미니 챌린지 체크") }
    }

    private suspend fun requireAuthorizationHeader(): String {
        val session = authRepository.userSession.first()
            ?: throw ApiException(code = "AUTH_UNAUTHORIZED", message = "로그인이 필요합니다.")
        return "${session.tokenType} ${session.accessToken}"
    }

    private fun <T> requireBody(response: Response<ApiResponse<T>>, fallbackMessage: String): T {
        val body = response.body()?.data
        if (response.isSuccessful && body != null) return body
        throw parseError(response.errorBody()?.string(), fallbackMessage)
    }

    private fun parseError(errorBodyString: String?, fallbackMessage: String): ApiException {
        val error = errorBodyString?.let {
            runCatching { Gson().fromJson(it, ApiErrorBody::class.java) }.getOrNull()
        }
        return ApiException(
            code = error?.code ?: "UNKNOWN",
            message = error?.message ?: fallbackMessage,
            fieldErrors = error?.fieldErrors
        )
    }

    /** [kotlin.runCatching]은 [CancellationException]도 그대로 삼켜버리므로, onFailure에서 다시 던져 취소를 정상 전파한다. */
    private fun rethrowIfCancelled(error: Throwable, action: String) {
        if (error is CancellationException) throw error
        Log.e(TAG, "$action 네트워크 오류", error)
    }

}

/** 도메인의 "오늘만"(totalDays == null)은 서버에서 durationDays=1로 표현된다. */
private fun Int?.toDurationDays(): Int = this ?: 1

/** durationDays=1은 도메인에서 "오늘만"(totalDays == null)로 표현한다. */
private fun Int.toTotalDaysOrNull(): Int? = if (this == 1) null else this

private fun MiniChallengeItemDto.toDomain(): MiniChallengeEntry = MiniChallengeEntry(
    id = miniChallengeId.toString(),
    name = title,
    totalDays = durationDays?.toTotalDaysOrNull(),
    achievedDays = progressDays,
    isChecked = checked
)

private fun RecommendedMiniChallengeDto.toDomain(): RecommendedMiniChallenge = RecommendedMiniChallenge(
    id = recommendedId.toString(),
    totalDays = durationDays?.toTotalDaysOrNull(),
    name = title
)
