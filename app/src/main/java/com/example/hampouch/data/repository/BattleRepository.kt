package com.example.hampouch.data.repository

import android.content.Context
import android.util.Log
import com.example.hampouch.core.config.BattleConfig
import com.example.hampouch.core.network.NetworkModule
import com.example.hampouch.data.model.HamBattleChallenge
import com.example.hampouch.data.model.HamBattleChallengeRequest
import com.example.hampouch.data.model.HamBattleParticipantSpending
import com.example.hampouch.data.model.HamBattleParticipantStatus
import com.example.hampouch.data.remote.ApiException
import com.example.hampouch.data.remote.dto.ApiErrorBody
import com.example.hampouch.data.remote.dto.ApiResponse
import com.example.hampouch.data.remote.dto.BattleDetailData
import com.example.hampouch.data.remote.dto.BattleInvitationPreviewData
import com.example.hampouch.data.remote.dto.BattleParticipantDto
import com.example.hampouch.data.remote.dto.CreateBattleRequest
import com.example.hampouch.data.remote.dto.MyBattleSummaryDto
import com.example.hampouch.ui.hambattle.HamBattleMockData
import com.example.hampouch.ui.hambattle.HamBattleStore
import com.google.gson.Gson
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneOffset
import java.time.temporal.ChronoUnit
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.first
import retrofit2.Response

private const val TAG = "BattleRepository"

/** 목데이터의 "나" 표기와 동일한 규칙: 로그인한 사용자의 userId와 일치하는 참가자만 이 이름으로 보여준다. */
private const val ME_NAME = "나"

/**
 * 햄배틀(/api/battles*) 서버 연동.
 *
 * 목데이터 모드([BattleConfig.USE_SERVER_BATTLE] == false)에서는 [HamBattleMockData]의 기존 로컬 로직을
 * 그대로 쓰고, 서버 모드에서는 API 응답으로 [HamBattleStore]를 갱신한다. 화면단은 이 리포지토리만
 * 호출하면 되고 지금이 어느 모드인지는 신경 쓰지 않아도 된다.
 */
class BattleRepository private constructor(private val context: Context) {

    private val authRepository by lazy { AuthRepository.getInstance(context) }

    /** 참가 중인 햄배틀을 전부 조회해 상태별로 나눠 [HamBattleStore]에 반영한다. */
    suspend fun loadMyBattles(): Result<Unit> {
        if (!BattleConfig.USE_SERVER_BATTLE) return Result.success(Unit)
        return runCatching {
            val authorization = requireAuthorizationHeader()
            val myUserId = requireUserId()
            val response = NetworkModule.apiService.getMyBattles(authorization)
            val body = requireBody(response, "햄배틀 목록 조회에 실패했습니다.")
            val ready = mutableListOf<HamBattleChallenge>()
            val ongoing = mutableListOf<HamBattleChallenge>()
            val terminated = mutableListOf<HamBattleChallenge>()
            body.battles.forEach { dto ->
                val challenge = dto.toDomain(myUserId)
                when (dto.status) {
                    "READY" -> ready += challenge
                    "ONGOING" -> ongoing += challenge
                    else -> terminated += challenge
                }
            }
            HamBattleStore.replaceLists(ready, ongoing, terminated)
        }.onFailure { rethrowIfCancelled(it, "햄배틀 목록 조회") }
    }

    /** [battleId] 상세(READY/ONGOING/TERMINATED 공통 응답)를 조회해 [HamBattleStore]에 반영한다. */
    suspend fun loadBattleDetail(battleId: Long): Result<Unit> {
        if (!BattleConfig.USE_SERVER_BATTLE) return Result.success(Unit)
        return runCatching {
            val authorization = requireAuthorizationHeader()
            val myUserId = requireUserId()
            val response = NetworkModule.apiService.getBattleDetail(authorization, battleId)
            val body = requireBody(response, "햄배틀 상세 조회에 실패했습니다.")
            HamBattleStore.setDetail(battleId, body.toDomain(myUserId))
        }.onFailure { rethrowIfCancelled(it, "햄배틀 상세 조회") }
    }

    /** battleCode로 참가 전 미리보기를 조회한다. 로그인이 필요하다. */
    suspend fun loadInvitationPreview(battleCode: String): Result<BattleInvitationPreviewData> {
        return runCatching {
            val authorization = requireAuthorizationHeader()
            val response = NetworkModule.apiService.getBattleInvitation(authorization, battleCode)
            requireBody(response, "초대 정보를 불러오지 못했습니다.")
        }.onFailure { rethrowIfCancelled(it, "햄배틀 초대 조회") }
    }

    /** [battleCode]의 햄배틀에 참가한다. 성공 시 참가한 battleId를 반환하고 목록을 새로고침한다. */
    suspend fun join(battleCode: String): Result<Long> {
        return runCatching {
            val authorization = requireAuthorizationHeader()
            val response = NetworkModule.apiService.joinBattle(authorization, battleCode)
            val body = requireBody(response, "햄배틀 참가에 실패했습니다.")
            loadMyBattles().getOrThrow()
            body.battleId
        }.onFailure { rethrowIfCancelled(it, "햄배틀 참가") }
    }

    /**
     * 새 햄배틀을 만든다. capacity는 2~10명, durationDays는 3/7/14/31 중 하나만 허용된다(서버 검증 실패 시
     * [ApiException]으로 던져진다).
     */
    suspend fun create(request: HamBattleChallengeRequest): Result<HamBattleChallenge> {
        if (!BattleConfig.USE_SERVER_BATTLE) {
            return Result.success(HamBattleMockData.startNewChallenge(request))
        }
        return runCatching {
            val authorization = requireAuthorizationHeader()
            val startDate = request.startDateMillis?.let {
                Instant.ofEpochMilli(it).atZone(ZoneOffset.UTC).toLocalDate()
            } ?: LocalDate.now()
            val response = NetworkModule.apiService.createBattle(
                authorization,
                CreateBattleRequest(
                    title = request.challengeName,
                    capacity = parseParticipantTotalCount(request.participantCount),
                    durationDays = parseDurationDays(request.durationDays),
                    startDate = startDate.toString(),
                    penalty = request.penalty
                )
            )
            val body = requireBody(response, "햄배틀 생성에 실패했습니다.")
            HamBattleStore.rememberCapacity(body.battleId, body.capacity)
            loadMyBattles().getOrThrow()
            HamBattleChallenge(
                id = body.battleId.toString(),
                type = if (body.capacity <= 2) "1 vs 1" else "그룹",
                title = body.title,
                penalty = body.penalty,
                totalCount = body.capacity,
                durationDays = body.durationDays,
                startDate = runCatching { LocalDate.parse(body.startDate) }.getOrNull(),
                battleId = body.battleId,
                battleCode = body.battleCode,
                serverStatus = body.status,
                joinedCountOverride = 1
            )
        }.onFailure { rethrowIfCancelled(it, "햄배틀 생성") }
    }

    private suspend fun requireAuthorizationHeader(): String {
        val session = authRepository.userSession.first()
            ?: throw ApiException(code = "AUTH_UNAUTHORIZED", message = "로그인이 필요합니다.")
        return "${session.tokenType} ${session.accessToken}"
    }

    private suspend fun requireUserId(): Long {
        val session = authRepository.userSession.first()
            ?: throw ApiException(code = "AUTH_UNAUTHORIZED", message = "로그인이 필요합니다.")
        return session.userId
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
        private var instance: BattleRepository? = null

        fun getInstance(context: Context): BattleRepository =
            instance ?: synchronized(this) {
                instance ?: BattleRepository(context.applicationContext).also { instance = it }
            }
    }
}

private fun parseParticipantTotalCount(option: String): Int =
    if (option == "1 vs 1") 2 else option.removeSuffix("인").toIntOrNull() ?: 2

private fun parseDurationDays(option: String): Int =
    option.removeSuffix("일").toIntOrNull() ?: 7

private fun durationDaysBetween(startDate: String, endDate: String): Int =
    runCatching {
        (ChronoUnit.DAYS.between(LocalDate.parse(startDate), LocalDate.parse(endDate)) + 1).toInt()
    }.getOrDefault(1)

/** 참가자 목록에서 나를 찾아 [ME_NAME]으로 라벨링한다(다른 화면 로직이 전부 "나" 문자열로 나를 식별한다). */
private fun BattleParticipantDto.toDomain(myUserId: Long): HamBattleParticipantSpending {
    val disqualified = isValid == false
    return HamBattleParticipantSpending(
        name = if (userId == myUserId) ME_NAME else nickname,
        amount = totalAmount,
        status = if (disqualified) HamBattleParticipantStatus.DISQUALIFIED else HamBattleParticipantStatus.NORMAL,
        avatarUrl = avatarUrl,
        userId = userId,
        todayAmount = todayAmount
    )
}

private fun MyBattleSummaryDto.toDomain(myUserId: Long): HamBattleChallenge {
    val participants = participants?.map { it.toDomain(myUserId) }.orEmpty()
    val totalCount = when {
        capacity != null -> capacity
        participants.isNotEmpty() -> participants.size
        else -> 0
    }
    return HamBattleChallenge(
        id = battleId.toString(),
        type = if (totalCount in 1..2) "1 vs 1" else "그룹",
        title = title,
        penalty = penalty,
        participants = participants,
        totalCount = totalCount,
        durationDays = durationDaysBetween(startDate, endDate),
        startDate = runCatching { LocalDate.parse(startDate) }.getOrNull(),
        battleId = battleId,
        battleCode = battleCode,
        serverStatus = status,
        joinedCountOverride = joinedCount,
        winnerName = winnerNickname
    )
}

private fun BattleDetailData.toDomain(myUserId: Long): HamBattleChallenge {
    val domainParticipants = participants.map { it.toDomain(myUserId) }
    return HamBattleChallenge(
        id = battleId.toString(),
        type = if (domainParticipants.size <= 2) "1 vs 1" else "그룹",
        title = title,
        penalty = penalty,
        participants = domainParticipants,
        totalCount = domainParticipants.size,
        durationDays = durationDaysBetween(startDate, endDate),
        startDate = runCatching { LocalDate.parse(startDate) }.getOrNull(),
        battleId = battleId,
        battleCode = battleCode,
        serverStatus = status,
        penaltyUserName = penaltyUserNickname
    )
}
