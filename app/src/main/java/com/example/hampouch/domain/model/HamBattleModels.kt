package com.example.hampouch.domain.model

import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.time.temporal.ChronoUnit

data class HamBattleChallengeRequest(
    val challengeName: String,
    val participantCount: String,
    val durationDays: String,
    val startDateMillis: Long,
    val penalty: String
)

val HamBattleParticipantOptions = listOf("1 vs 1", "3인", "4인", "5인", "6인", "7인", "8인", "9인", "10인")
// 서버(POST /api/battles)가 durationDays로 3/7/14/31만 허용하므로 이 목록도 그대로 맞춘다.
val HamBattleDurationOptions = listOf("3일", "7일", "14일", "31일")
val HamBattleDefaultPenaltyOptions = listOf("커피 사기", "밥 사기", "영화 사기")

enum class HamBattleParticipantStatus {
    NORMAL,
    MISSED_CONSECUTIVE_LOGS,
    DISQUALIFIED
}

data class HamBattleParticipantSpending(
    val name: String,
    val amount: Int,
    val status: HamBattleParticipantStatus = HamBattleParticipantStatus.NORMAL,
    val avatarUrl: String? = null,
    val userId: Long? = null,
    /** 서버 상세 응답(todayAmount)에서만 채워짐. "오늘/전체" 토글에 쓴다 — null이면 [amount]로 대체. */
    val todayAmount: Int? = null
)

enum class HamBattleStatus {
    WAITING, ACTIVE, ENDED
}

sealed interface HamBattleServerState {
    data class Ready(val joinedCount: Int) : HamBattleServerState
    data object Ongoing : HamBattleServerState
    data class Terminated(val winnerName: String?) : HamBattleServerState
}

private val HamBattlePeriodDateFormatter = DateTimeFormatter.ofPattern("yy.MM.dd")
private val HamBattleShortDateFormatter = DateTimeFormatter.ofPattern("MM.dd")

data class HamBattleChallenge(
    val id: String,
    val type: String,
    val title: String,
    val penalty: String,
    val participants: List<HamBattleParticipantSpending> = emptyList(),
    val totalCount: Int,
    val durationDays: Int,
    val startDate: LocalDate? = null,
    val cancelled: Boolean = false,
    /** 서버 연동 시에만 채워짐. 참가자 전용 리소스다. */
    val battleId: Long? = null,
    /**
     * 초대 코드. 생성 시 1회 발급되며 재발급되지 않는다(서버 명세).
     * "링크 다시 복사하기"가 클립보드에 넣는 값이자, 커뮤니티 배틀 글이 배틀을 지목하는 키다.
     */
    val battleCode: String? = null,
    /** 서버 응답은 상태별 필수값을 각 subtype으로 보존한다. null은 목데이터의 날짜 기반 상태다. */
    val serverState: HamBattleServerState? = null,
    /** TERMINATED 상세 응답의 벌칙 대상자 닉네임(penaltyUserNickname). */
    val penaltyUserName: String? = null,
) {
    val isOneVsOne: Boolean get() = type == "1 vs 1"
    val joinedCount: Int
        get() = (serverState as? HamBattleServerState.Ready)?.joinedCount ?: participants.size
    val winnerName: String?
        get() = (serverState as? HamBattleServerState.Terminated)?.winnerName
    val isFull: Boolean get() = totalCount > 0 && joinedCount >= totalCount

    fun effectiveStartDate(referenceToday: LocalDate = LocalDate.now()): LocalDate? =
        startDate ?: if (isFull) referenceToday else null

    fun effectiveEndDate(referenceToday: LocalDate = LocalDate.now()): LocalDate? =
        effectiveStartDate(referenceToday)?.plusDays((durationDays - 1).toLong())

    fun status(referenceToday: LocalDate = LocalDate.now()): HamBattleStatus {
        if (cancelled) return HamBattleStatus.ENDED
        when (serverState) {
            is HamBattleServerState.Ready -> return HamBattleStatus.WAITING
            HamBattleServerState.Ongoing -> return HamBattleStatus.ACTIVE
            is HamBattleServerState.Terminated -> return HamBattleStatus.ENDED
            null -> Unit
        }
        if (!isFull) return HamBattleStatus.WAITING
        val start = effectiveStartDate(referenceToday) ?: return HamBattleStatus.WAITING
        if (referenceToday.isBefore(start)) return HamBattleStatus.WAITING
        val end = start.plusDays((durationDays - 1).toLong())
        return if (referenceToday.isAfter(end)) HamBattleStatus.ENDED else HamBattleStatus.ACTIVE
    }

    fun isExpired(referenceToday: LocalDate = LocalDate.now()): Boolean =
        !isFull && startDate != null && referenceToday.isAfter(startDate)

    fun dDayLabel(referenceToday: LocalDate = LocalDate.now()): String {
        val end = effectiveEndDate(referenceToday) ?: return "D-DAY"
        val days = ChronoUnit.DAYS.between(referenceToday, end)
        return if (days <= 0) "D-DAY" else "D-$days"
    }

    fun periodLabel(referenceToday: LocalDate = LocalDate.now()): String {
        val start = effectiveStartDate(referenceToday) ?: return ""
        val end = start.plusDays((durationDays - 1).toLong())
        return "${start.format(HamBattlePeriodDateFormatter)} - ${end.format(HamBattlePeriodDateFormatter)} (${durationDays}일)"
    }

    fun startsInDayLabel(referenceToday: LocalDate = LocalDate.now()): String {
        val start = startDate ?: return "D-DAY"
        val days = ChronoUnit.DAYS.between(referenceToday, start)
        return if (days <= 0) "D-DAY" else "D-$days"
    }

    fun startDateLabel(): String {
        val start = startDate ?: return "시작일 미정"
        return "${start.monthValue}월 ${start.dayOfMonth}일 시작"
    }

    fun startDateShortLabel(): String {
        val start = startDate ?: return ""
        return start.format(HamBattleShortDateFormatter)
    }
}
