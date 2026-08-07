package com.example.hampouch.data.model

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
    val status: HamBattleParticipantStatus = HamBattleParticipantStatus.NORMAL
)

/** 챌린지가 지금 어느 단계인지. 저장된 값이 아니라 날짜/인원을 기준으로 매번 계산된다. */
enum class HamBattleStatus {
    WAITING, ACTIVE, ENDED
}

private val HamBattlePeriodDateFormatter = DateTimeFormatter.ofPattern("yy.MM.dd")
private val HamBattleShortDateFormatter = DateTimeFormatter.ofPattern("MM.dd")

/**
 * 햄배틀 챌린지 하나를 나타내는 단일 모델.
 *
 * 진행중/대기중/종료는 별도 상태값으로 저장하지 않고, [startDate]·[durationDays]·참가 인원을
 * 기준으로 [status]에서 매번 계산한다 — 그래야 날짜가 지나거나 인원이 다 찼을 때
 * 화면 간 상태가 어긋나지 않는다.
 */
data class HamBattleChallenge(
    val id: String,
    val type: String,
    val title: String,
    val penalty: String,
    val participants: List<HamBattleParticipantSpending> = emptyList(),
    val totalCount: Int,
    val durationDays: Int,
    /** null이면 아직 시작일이 정해지지 않은 챌린지. */
    val startDate: LocalDate? = null,
    val link: String = "",
    /** 지출 미입력 탈락자가 늘어 남은 인원이 1명 이하가 되는 등, 진행 도중 강제로 종료된 경우. */
    val cancelled: Boolean = false
) {
    val isOneVsOne: Boolean get() = type == "1 vs 1"
    val joinedCount: Int get() = participants.size
    val isFull: Boolean get() = totalCount > 0 && joinedCount >= totalCount

    /**
     * 정해둔 시작일이 있으면 정원이 다 차도 그 날짜까지는 기다린다(방만 잠긴다).
     * 시작일 자체가 없는 챌린지(커뮤니티 참가로 생긴 경우 등)만 정원이 차는 즉시 시작한다.
     */
    fun effectiveStartDate(referenceToday: LocalDate = LocalDate.now()): LocalDate? =
        startDate ?: if (isFull) referenceToday else null

    fun effectiveEndDate(referenceToday: LocalDate = LocalDate.now()): LocalDate? =
        effectiveStartDate(referenceToday)?.plusDays((durationDays - 1).toLong())

    fun status(referenceToday: LocalDate = LocalDate.now()): HamBattleStatus {
        if (cancelled) return HamBattleStatus.ENDED
        // 정원이 다 안 찼으면 시작일이 지났든 말든 진행중이 될 수 없다 — 계속 대기중이고,
        // 시작일까지 못 채웠으면 isExpired로 자동 취소 대상이 될 뿐이다.
        if (!isFull) return HamBattleStatus.WAITING
        val start = effectiveStartDate(referenceToday) ?: return HamBattleStatus.WAITING
        if (referenceToday.isBefore(start)) return HamBattleStatus.WAITING
        val end = start.plusDays((durationDays - 1).toLong())
        return if (referenceToday.isAfter(end)) HamBattleStatus.ENDED else HamBattleStatus.ACTIVE
    }

    /** 시작일까지 정원을 못 채운 채로 시작일이 지나버린 경우 = 자동 취소 대상. */
    fun isExpired(referenceToday: LocalDate = LocalDate.now()): Boolean =
        !isFull && startDate != null && referenceToday.isAfter(startDate)

    /** 진행중 카드에 쓰는 "D-3" 형태. 대기중이면 아직 시작 전이라는 뜻으로 "D-DAY"를 돌려준다. */
    fun dDayLabel(referenceToday: LocalDate = LocalDate.now()): String {
        val end = effectiveEndDate(referenceToday) ?: return "D-DAY"
        val days = ChronoUnit.DAYS.between(referenceToday, end)
        return if (days <= 0) "D-DAY" else "D-$days"
    }

    /** "26.05.01 - 26.05.07 (7일)" 형태의 진행 기간 라벨. */
    fun periodLabel(referenceToday: LocalDate = LocalDate.now()): String {
        val start = effectiveStartDate(referenceToday) ?: return ""
        val end = start.plusDays((durationDays - 1).toLong())
        return "${start.format(HamBattlePeriodDateFormatter)} - ${end.format(HamBattlePeriodDateFormatter)} (${durationDays}일)"
    }

    /** 대기중 카드에 쓰는 "시작까지 D-2" 형태. */
    fun startsInDayLabel(referenceToday: LocalDate = LocalDate.now()): String {
        val start = startDate ?: return "D-DAY"
        val days = ChronoUnit.DAYS.between(referenceToday, start)
        return if (days <= 0) "D-DAY" else "D-$days"
    }

    /** "5월 1일 시작" 형태. */
    fun startDateLabel(): String {
        val start = startDate ?: return "시작일 미정"
        return "${start.monthValue}월 ${start.dayOfMonth}일 시작"
    }

    /** "05.01" 형태. */
    fun startDateShortLabel(): String {
        val start = startDate ?: return ""
        return start.format(HamBattleShortDateFormatter)
    }
}
