package com.example.hampouch.data.repository

import android.content.Context
import android.util.Log
import com.example.hampouch.core.config.MiniChallengeConfig
import com.example.hampouch.core.network.NetworkModule
import com.example.hampouch.domain.model.MiniChallengeDaySummary
import com.example.hampouch.domain.model.MiniChallengeEntry
import com.example.hampouch.domain.model.RecommendedMiniChallenge
import com.example.hampouch.domain.model.ApiException
import com.example.hampouch.data.local.MiniChallengeLocalStore
import com.example.hampouch.di.legacyEntryPoint
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
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.first
import retrofit2.Response

private const val TAG = "MiniChallengeRepository"

/**
 * 미니 챌린지(/api/mini-challenges*) 서버 연동.
 *
 * 목데이터 모드([MiniChallengeConfig.USE_SERVER_MINI_CHALLENGE] == false)에서는 [MiniChallengeStore]의 기존 로컬 로직을
 * 그대로 쓰고, 서버 모드에서는 API 응답으로 [MiniChallengeStore]를 갱신한다. 화면단은 이 리포지토리만
 * 호출하면 되고 지금이 어느 모드인지는 신경 쓰지 않아도 된다.
 */
class MiniChallengeRepository private constructor(private val context: Context) {

    private val authRepository by lazy { AuthRepository.getInstance(context) }
    private val store: MiniChallengeLocalStore get() = context.legacyEntryPoint().miniChallengeLocalStore()

    /** [date]의 미니 챌린지 목록/요약을 조회해 [MiniChallengeStore]에 반영한다. */
    suspend fun loadChallenges(date: LocalDate): Result<Unit> {
        if (!MiniChallengeConfig.USE_SERVER_MINI_CHALLENGE) return Result.success(Unit)
        return runCatching {
            val authorization = requireAuthorizationHeader()
            val response = NetworkModule.apiService.getMiniChallenges(authorization, date.toString())
            val body = requireBody(response, "미니 챌린지 조회에 실패했습니다.")
            store.setChallengesForDate(date, body.items.map { it.toDomain() })
            store.setSummaryForDate(
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
    suspend fun loadRecommended(durationDays: Int? = null): Result<Unit> {
        if (!MiniChallengeConfig.USE_SERVER_MINI_CHALLENGE) return Result.success(Unit)
        return runCatching {
            val authorization = requireAuthorizationHeader()
            val response = NetworkModule.apiService.getRecommendedMiniChallenges(authorization, durationDays)
            val body = requireBody(response, "추천 목록 조회에 실패했습니다.")
            store.replaceRecommendedChallenges(body.items.map { it.toDomain() })
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
    suspend fun addRecommended(date: LocalDate, recommended: RecommendedMiniChallenge): Result<LocalDate?> {
        if (!MiniChallengeConfig.USE_SERVER_MINI_CHALLENGE) {
            val added = store.addRecommendedChallenge(date, recommended)
            return Result.success(if (added) date else null)
        }
        return runCatching {
            val authorization = requireAuthorizationHeader()
            val recommendedId = recommended.id.toLongOrNull()
                ?: throw ApiException(code = "MINI_RECOMMENDED_NOT_FOUND", message = "추천 미니 챌린지를 찾을 수 없습니다.")
            val response = NetworkModule.apiService.addRecommendedMiniChallenge(
                authorization,
                AddRecommendedMiniChallengeRequest(recommendedId = recommendedId)
            )
            requireBody(response, "미니 챌린지 추가에 실패했습니다.")
            store.removeRecommended(recommended.id)
            val today = LocalDate.now()
            loadChallenges(today).getOrThrow()
            today
        }.onFailure { rethrowIfCancelled(it, "미니 챌린지 추가(추천)") }
    }

    /**
     * 커스텀 미니 챌린지를 새로 만든다. [addRecommended]와 동일하게, 서버 모드에서는 항상 오늘 날짜부터
     * 생성되므로 [date]가 아니라 실제 반영된 날짜를 반환한다. 중복 이름 등으로 추가되지 않았으면 null.
     */
    suspend fun addCustom(date: LocalDate, name: String, totalDays: Int?): Result<LocalDate?> {
        if (!MiniChallengeConfig.USE_SERVER_MINI_CHALLENGE) {
            val added = store.addChallenge(date, name, totalDays)
            return Result.success(if (added) date else null)
        }
        return runCatching {
            val authorization = requireAuthorizationHeader()
            val response = NetworkModule.apiService.addCustomMiniChallenge(
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
    suspend fun remove(date: LocalDate, id: String): Result<Unit> {
        if (!MiniChallengeConfig.USE_SERVER_MINI_CHALLENGE) {
            store.removeChallenge(date, id)
            return Result.success(Unit)
        }
        return runCatching {
            val authorization = requireAuthorizationHeader()
            val miniChallengeId = id.toLongOrNull()
                ?: throw ApiException(code = "MINI_NOT_FOUND", message = "미니 챌린지를 찾을 수 없습니다.")
            val response = NetworkModule.apiService.deleteMiniChallenge(authorization, miniChallengeId)
            if (!response.isSuccessful) throw parseError(response.errorBody()?.string(), "미니 챌린지 삭제에 실패했습니다.")
            loadChallenges(date).getOrThrow()
        }.onFailure { rethrowIfCancelled(it, "미니 챌린지 삭제") }
    }

    /** [date] 기준으로 [id]의 체크 상태를 [checked]로 바꾼다(PUT은 멱등이라 재시도해도 안전). */
    suspend fun setChecked(date: LocalDate, id: String, checked: Boolean): Result<Unit> {
        if (!MiniChallengeConfig.USE_SERVER_MINI_CHALLENGE) {
            store.toggle(date, id)
            return Result.success(Unit)
        }
        return runCatching {
            val authorization = requireAuthorizationHeader()
            val miniChallengeId = id.toLongOrNull()
                ?: throw ApiException(code = "MINI_NOT_FOUND", message = "미니 챌린지를 찾을 수 없습니다.")
            val response = NetworkModule.apiService.checkMiniChallenge(
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

    companion object {
        @Volatile
        private var instance: MiniChallengeRepository? = null

        fun getInstance(context: Context): MiniChallengeRepository =
            instance ?: synchronized(this) {
                instance ?: MiniChallengeRepository(context.applicationContext).also { instance = it }
            }
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
