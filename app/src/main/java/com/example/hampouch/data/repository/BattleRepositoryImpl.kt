package com.example.hampouch.data.repository

import android.util.Log
import com.example.hampouch.core.config.BattleConfig
import com.example.hampouch.domain.model.HamBattleChallenge
import com.example.hampouch.domain.model.HamBattleChallengeRequest
import com.example.hampouch.domain.model.HamBattleParticipantSpending
import com.example.hampouch.domain.model.HamBattleParticipantStatus
import com.example.hampouch.domain.model.ApiException
import com.example.hampouch.data.remote.dto.ApiResponse
import com.example.hampouch.data.remote.dto.BattleDetailData
import com.example.hampouch.data.local.BattleMockDataSource
import com.example.hampouch.data.remote.BattleApi
import com.example.hampouch.data.remote.toApiResult
import com.example.hampouch.data.remote.dto.BattleInvitationPreviewData
import com.example.hampouch.domain.model.BattleInvitationPreview
import com.example.hampouch.domain.model.BattleState
import com.example.hampouch.domain.model.HamBattleServerState
import com.example.hampouch.domain.repository.AccountScopedState
import com.example.hampouch.domain.repository.BattleRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import javax.inject.Inject
import javax.inject.Singleton
import com.example.hampouch.data.remote.dto.BattleParticipantDto
import com.example.hampouch.data.remote.dto.CreateBattleRequest
import com.example.hampouch.data.remote.dto.MyBattleSummaryDto
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneOffset
import java.time.temporal.ChronoUnit
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.first
import retrofit2.Response

private const val TAG = "BattleRepository"

private const val ME_NAME = "나"

@Singleton
class BattleRepositoryImpl @Inject constructor(
    private val apiService: BattleApi,
    private val authRepository: AuthRepository,
    private val mockDataSource: BattleMockDataSource
) : BattleRepository, AccountScopedState {

    private val _state = MutableStateFlow(BattleState())
    override val state: StateFlow<BattleState> = _state.asStateFlow()
    private var capacityByBattleId: Map<Long, Int> = emptyMap()

    private fun replaceLists(
        ready: List<HamBattleChallenge>,
        ongoing: List<HamBattleChallenge>,
        terminated: List<HamBattleChallenge>
    ) {
        val readyCapacities = ready.mapNotNull { c -> c.battleId?.let { it to c.totalCount } }
        capacityByBattleId = capacityByBattleId + readyCapacities
        _state.update {
            it.copy(readyBattles = ready, ongoingBattles = ongoing, terminatedBattles = terminated)
        }
    }

    private fun rememberCapacity(battleId: Long, capacity: Int) {
        capacityByBattleId = capacityByBattleId + (battleId to capacity)
    }

    private fun setDetail(battleId: Long, detail: HamBattleChallenge) {
        val knownCapacity = capacityByBattleId[battleId]
        val merged = if (knownCapacity != null && knownCapacity > detail.totalCount) {
            detail.copy(totalCount = knownCapacity)
        } else {
            detail
        }
        _state.update { it.copy(detailByBattleId = it.detailByBattleId + (battleId to merged)) }
    }

    override fun resetForAccount() {
        capacityByBattleId = emptyMap()
        _state.value = BattleState()
    }

    override suspend fun loadMyBattles(): Result<Unit> {
        if (!BattleConfig.USE_SERVER_BATTLE) return Result.success(Unit)
        return runCatching {
            requireAuthentication()
            val myUserId = requireUserId()
            val response = apiService.getMyBattles()
            val body = requireBody(response, "햄배틀 목록 조회에 실패했습니다.")
            val ready = mutableListOf<HamBattleChallenge>()
            val ongoing = mutableListOf<HamBattleChallenge>()
            val terminated = mutableListOf<HamBattleChallenge>()
            body.battles.forEach { dto ->
                when (dto) {
                    is MyBattleSummaryDto.Ready -> ready += dto.toDomain()
                    is MyBattleSummaryDto.Ongoing -> ongoing += dto.toDomain(myUserId)
                    is MyBattleSummaryDto.Terminated -> terminated += dto.toDomain()
                }
            }
            replaceLists(ready, ongoing, terminated)
        }.onFailure { rethrowIfCancelled(it, "햄배틀 목록 조회") }
    }

    override suspend fun loadBattleDetail(battleId: Long): Result<Unit> {
        if (!BattleConfig.USE_SERVER_BATTLE) return Result.success(Unit)
        return runCatching {
            requireAuthentication()
            val myUserId = requireUserId()
            val response = apiService.getBattleDetail(battleId)
            val body = requireBody(response, "햄배틀 상세 조회에 실패했습니다.")
            setDetail(battleId, body.toDomain(myUserId))
        }.onFailure { rethrowIfCancelled(it, "햄배틀 상세 조회") }
    }

    override suspend fun loadInvitationPreview(battleCode: String): Result<BattleInvitationPreview> {
        return runCatching {
            requireAuthentication()
            val response = apiService.getBattleInvitation(battleCode)
            requireBody(response, "초대 정보를 불러오지 못했습니다.").toDomain()
        }.onFailure { rethrowIfCancelled(it, "햄배틀 초대 조회") }
    }

    override suspend fun join(battleCode: String): Result<Long> {
        return runCatching {
            requireAuthentication()
            val response = apiService.joinBattle(battleCode)
            val body = requireBody(response, "햄배틀 참가에 실패했습니다.")
            loadMyBattles().getOrThrow()
            body.battleId
        }.onFailure { rethrowIfCancelled(it, "햄배틀 참가") }
    }

    override suspend fun create(request: HamBattleChallengeRequest): Result<HamBattleChallenge> {
        if (!BattleConfig.USE_SERVER_BATTLE) {
            return Result.success(mockDataSource.startNewChallenge(request))
        }
        return runCatching {
            requireAuthentication()
            val startDate = Instant.ofEpochMilli(request.startDateMillis)
                .atZone(ZoneOffset.UTC)
                .toLocalDate()
            val response = apiService.createBattle(
                CreateBattleRequest(
                    title = request.challengeName,
                    capacity = parseParticipantTotalCount(request.participantCount),
                    durationDays = parseDurationDays(request.durationDays),
                    startDate = startDate.toString(),
                    penalty = request.penalty
                )
            )
            val body = requireBody(response, "햄배틀 생성에 실패했습니다.")
            rememberCapacity(body.battleId, body.capacity)
            loadMyBattles().getOrThrow()
            HamBattleChallenge(
                id = body.battleId.toString(),
                type = if (body.capacity <= 2) "1 vs 1" else "그룹",
                title = body.title,
                penalty = body.penalty,
                totalCount = body.capacity,
                durationDays = body.durationDays,
                startDate = parseServerDate(body.startDate, "createBattle.startDate"),
                cancelled = body.status == "CANCELLED",
                battleId = body.battleId,
                battleCode = body.battleCode,
                serverState = when (body.status) {
                    "READY" -> HamBattleServerState.Ready(joinedCount = 1)
                    "ONGOING" -> HamBattleServerState.Ongoing
                    "TERMINATED" -> HamBattleServerState.Terminated(winnerName = null)
                    "CANCELLED" -> HamBattleServerState.Cancelled
                    else -> throw ApiException("CONTRACT_VIOLATION", "지원하지 않는 battle status입니다: ${body.status}")
                }
            )
        }.onFailure { rethrowIfCancelled(it, "햄배틀 생성") }
    }

    private suspend fun requireAuthentication() {
        if (authRepository.userSession.first() == null) {
            throw ApiException(code = "AUTH_UNAUTHORIZED", message = "로그인이 필요합니다.")
        }
    }

    private suspend fun requireUserId(): Long {
        val session = authRepository.userSession.first()
            ?: throw ApiException(code = "AUTH_UNAUTHORIZED", message = "로그인이 필요합니다.")
        return session.userId
    }

    private fun <T> requireBody(response: Response<ApiResponse<T>>, fallbackMessage: String): T {
        return response.toApiResult(fallbackMessage).getOrThrow()
    }

    private fun rethrowIfCancelled(error: Throwable, action: String) {
        if (error is CancellationException) throw error
        Log.e(TAG, "$action 네트워크 오류", error)
    }

}

private fun parseParticipantTotalCount(option: String): Int =
    if (option == "1 vs 1") 2 else option.removeSuffix("인").toIntOrNull() ?: 2

private fun parseDurationDays(option: String): Int =
    option.removeSuffix("일").toIntOrNull() ?: 7

private fun durationDaysBetween(startDate: String, endDate: String): Int {
    val start = parseServerDate(startDate, "battle.startDate")
    val end = parseServerDate(endDate, "battle.endDate")
    val days = (ChronoUnit.DAYS.between(start, end) + 1).toInt()
    if (days <= 0) {
        throw ApiException("CONTRACT_VIOLATION", "battle 종료일은 시작일보다 빠를 수 없습니다.")
    }
    return days
}

private fun BattleParticipantDto.toDomain(myUserId: Long): HamBattleParticipantSpending {
    val disqualified = isValid == false
    return HamBattleParticipantSpending(
        name = if (userId == myUserId) ME_NAME else nickname,
        amount = totalAmount,
        status = if (disqualified) HamBattleParticipantStatus.DISQUALIFIED else HamBattleParticipantStatus.NORMAL,
        avatarUrl = avatarUrl,
        userId = userId,
        todayAmount = todayAmount,
        rank = rank
    )
}

private fun MyBattleSummaryDto.Ready.toDomain(): HamBattleChallenge = HamBattleChallenge(
    id = battleId.toString(),
    type = if (capacity <= 2) "1 vs 1" else "그룹",
    title = title,
    penalty = penalty,
    totalCount = capacity,
    durationDays = durationDaysBetween(startDate, endDate),
    startDate = parseServerDate(startDate, "battle.ready.startDate"),
    battleId = battleId,
    battleCode = battleCode,
    serverState = HamBattleServerState.Ready(joinedCount)
)

private fun MyBattleSummaryDto.Ongoing.toDomain(myUserId: Long): HamBattleChallenge {
    val domainParticipants = participants.map { it.toDomain(myUserId) }
    return HamBattleChallenge(
        id = battleId.toString(),
        type = if (domainParticipants.size <= 2) "1 vs 1" else "그룹",
        title = title,
        penalty = penalty,
        participants = domainParticipants,
        totalCount = domainParticipants.size,
        durationDays = durationDaysBetween(startDate, endDate),
        startDate = parseServerDate(startDate, "battle.ongoing.startDate"),
        battleId = battleId,
        battleCode = battleCode,
        serverState = HamBattleServerState.Ongoing
    )
}

private fun MyBattleSummaryDto.Terminated.toDomain(): HamBattleChallenge = HamBattleChallenge(
    id = battleId.toString(),
    type = "종료",
    title = title,
    penalty = penalty,
    totalCount = 0,
    durationDays = durationDaysBetween(startDate, endDate),
    startDate = parseServerDate(startDate, "battle.terminated.startDate"),
    battleId = battleId,
    battleCode = battleCode,
    serverState = HamBattleServerState.Terminated(winnerNickname)
)

internal fun BattleDetailData.toDomain(myUserId: Long): HamBattleChallenge {
    val domainParticipants = participants.map { it.toDomain(myUserId) }
    return HamBattleChallenge(
        id = battleId.toString(),
        type = if (domainParticipants.size <= 2) "1 vs 1" else "그룹",
        title = title,
        penalty = penalty,
        participants = domainParticipants,
        totalCount = domainParticipants.size,
        durationDays = durationDaysBetween(startDate, endDate),
        startDate = parseServerDate(startDate, "battle.detail.startDate"),
        cancelled = status == "CANCELLED",
        battleId = battleId,
        battleCode = battleCode,
        serverState = when (status) {
            "READY" -> HamBattleServerState.Ready(domainParticipants.size)
            "ONGOING" -> HamBattleServerState.Ongoing
            "TERMINATED" -> HamBattleServerState.Terminated(winnerName = null)
            "CANCELLED" -> HamBattleServerState.Cancelled
            else -> throw ApiException("CONTRACT_VIOLATION", "지원하지 않는 battle status입니다: $status")
        },
        penaltyUserName = penaltyTargetNickname
    )
}

private fun BattleInvitationPreviewData.toDomain(): BattleInvitationPreview = BattleInvitationPreview(
    title = title,
    penalty = penalty,
    capacity = capacity,
    joinedCount = joinedCount,
    startDate = parseServerDate(startDate, "battle.invitation.startDate"),
    durationDays = durationDays
)

private fun parseServerDate(value: String, field: String): LocalDate = try {
    LocalDate.parse(value)
} catch (error: Exception) {
    throw ApiException(
        code = "CONTRACT_VIOLATION",
        message = "서버 응답의 $field 형식이 올바르지 않습니다."
    ).also { it.initCause(error) }
}
