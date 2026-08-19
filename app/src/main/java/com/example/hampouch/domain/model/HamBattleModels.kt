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
val HamBattleDurationOptions = listOf("3일", "7일", "14일", "31일")
val HamBattleDefaultPenaltyOptions = listOf("커피 사기", "밥 사기", "영화 사기")

enum class HamBattleParticipantStatus {
    NORMAL,
    MISSED_CONSECUTIVE_LOGS,
    DISQUALIFIED
}

data class HamBattleParticipantSpending(
    val name: String,
    val amount: Long,
    val status: HamBattleParticipantStatus = HamBattleParticipantStatus.NORMAL,
    val avatarUrl: String? = null,
    val userId: Long? = null,
    val todayAmount: Long? = null,
    val rank: Int? = null
)

enum class HamBattleStatus {
    WAITING, ACTIVE, ENDED
}

sealed interface HamBattleServerState {
    data class Ready(val joinedCount: Int) : HamBattleServerState
    data object Ongoing : HamBattleServerState
    data class Terminated(val winnerName: String?) : HamBattleServerState
    data object Cancelled : HamBattleServerState
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
    val battleId: Long? = null,
    val battleCode: String? = null,
    val serverState: HamBattleServerState? = null,
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
            HamBattleServerState.Cancelled -> return HamBattleStatus.ENDED
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
