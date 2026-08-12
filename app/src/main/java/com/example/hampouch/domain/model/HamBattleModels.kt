package com.example.hampouch.domain.model

import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.time.temporal.ChronoUnit

data class HamBattleChallengeRequest(
    val challengeName: String,
    val participantCount: String,
    val durationDays: String,
    val startDateMillis: Long?,
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
    /** 서버가 내려준 상태(READY/ONGOING/TERMINATED)가 있으면 날짜 기반 추정 대신 이 값을 그대로 쓴다. */
    val serverStatus: String? = null,
    /** READY 목록 응답처럼 participants가 비어 있어도 실제 참가 인원을 알고 있을 때 쓴다. */
    val joinedCountOverride: Int? = null,
    /** TERMINATED 상세 응답의 벌칙 대상자 닉네임(penaltyUserNickname). */
    val penaltyUserName: String? = null,
    /** TERMINATED 목록 응답의 winnerNickname. 목록 항목엔 participants가 없어 별도로 들고 있는다. */
    val winnerName: String? = null
) {
    val isOneVsOne: Boolean get() = type == "1 vs 1"
    val joinedCount: Int get() = joinedCountOverride ?: participants.size
    val isFull: Boolean get() = totalCount > 0 && joinedCount >= totalCount

    fun effectiveStartDate(referenceToday: LocalDate = LocalDate.now()): LocalDate? =
        startDate ?: if (isFull) referenceToday else null

    fun effectiveEndDate(referenceToday: LocalDate = LocalDate.now()): LocalDate? =
        effectiveStartDate(referenceToday)?.plusDays((durationDays - 1).toLong())

    fun status(referenceToday: LocalDate = LocalDate.now()): HamBattleStatus {
        if (cancelled) return HamBattleStatus.ENDED
        when (serverStatus) {
            "READY" -> return HamBattleStatus.WAITING
            "ONGOING" -> return HamBattleStatus.ACTIVE
            "TERMINATED", "FINISHED" -> return HamBattleStatus.ENDED
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
