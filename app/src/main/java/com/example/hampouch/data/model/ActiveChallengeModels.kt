package com.example.hampouch.data.model

import java.time.LocalDate
import java.time.temporal.ChronoUnit

data class ActiveChallenge(
    val id: String,
    val totalDays: Int,
    val periodStart: LocalDate,
    val periodEnd: LocalDate,
    val dailyLimit: Int,
    val targetAmount: Int,
    val savedAmount: Int,
    val streakDays: Int,
    val editCount: Int
) {
    val maxEditCount: Int
        get() = if (totalDays >= 15) 2 else 1

    val canEditTarget: Boolean
        get() = editCount < maxEditCount

    fun dDayFrom(referenceToday: LocalDate): Int =
        ChronoUnit.DAYS.between(referenceToday, periodEnd).toInt()
}

// 챌린지 시작일부터 기준일까지, 실제 지출 내역을 근거로 계산한 진행 현황.
// 홈/챌린지 결과 화면이 이 하나의 계산 결과를 공유해 서로 다른 숫자를 보여주지 않도록 한다.
data class ChallengeProgress(
    val savedAmount: Int,
    val streakDays: Int,
    val dailyRecords: Map<LocalDate, DailyRecordStatus>
)
