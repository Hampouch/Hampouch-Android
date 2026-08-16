package com.example.hampouch.ui.challengeresult

import com.example.hampouch.domain.model.ActiveChallenge
import com.example.hampouch.domain.model.ChallengeResultStatus
import com.example.hampouch.domain.model.ChallengeResultSummary
import com.example.hampouch.ui.challengeresult.ChallengeResultUiState
import com.example.hampouch.domain.model.ChallengeState
import com.example.hampouch.domain.model.DailyRecordStatus.SUCCESS
import com.example.hampouch.domain.model.EmotionStat
import com.example.hampouch.domain.model.ExpenseRecord
import com.example.hampouch.domain.model.SpendingEmotion
import java.time.LocalDate
import kotlin.math.roundToInt

object ChallengeResultMockData {

    private fun reasonIdToEmotion(reasonId: String?): SpendingEmotion = when (reasonId) {
        "stress" -> SpendingEmotion.STRESS
        "reward" -> SpendingEmotion.REWARD
        "lazy" -> SpendingEmotion.LAZY
        "craving" -> SpendingEmotion.CRAVING
        else -> SpendingEmotion.ETC
    }

    private fun computeEmotionStats(records: List<ExpenseRecord>): List<EmotionStat> {
        val totalAmount = records.sumOf { it.amount }
        if (totalAmount <= 0) return SpendingEmotion.entries.map { EmotionStat(it, 0) }
        return SpendingEmotion.entries.map { emotion ->
            val amount = records.filter { reasonIdToEmotion(it.reasonId) == emotion }.sumOf { it.amount }
            EmotionStat(emotion, (amount * 100.0 / totalAmount).roundToInt())
        }
    }

    private fun serverResultOrNull(
        challenge: ActiveChallenge,
        isOngoing: Boolean
    ): ChallengeResultUiState? {
        if (isOngoing) return null
        val summary = challenge.resultSummary ?: return null
        return fromServerResult(challenge, summary)
    }

    fun forChallenge(
        challenge: ActiveChallenge,
        challengeState: ChallengeState,
        recordsForDate: (LocalDate) -> List<ExpenseRecord>,
        hasRecordOnDate: (LocalDate) -> Boolean,
        referenceToday: LocalDate = LocalDate.now()
    ): ChallengeResultUiState {
        val isOngoing = challenge.isOngoingOn(referenceToday)
        val serverResult = serverResultOrNull(challenge, isOngoing)
        if (serverResult != null) return serverResult
        return computeLocalResult(challenge, challengeState, recordsForDate, hasRecordOnDate, referenceToday)
    }

    private fun computeLocalResult(
        challenge: ActiveChallenge,
        challengeState: ChallengeState,
        recordsForDate: (LocalDate) -> List<ExpenseRecord>,
        hasRecordOnDate: (LocalDate) -> Boolean,
        referenceToday: LocalDate
    ): ChallengeResultUiState {
        val isActiveChallenge = challenge.id == challengeState.activeChallenge?.id
        val isOngoing = challenge.isOngoingOn(referenceToday)
        val trackedEnd = if (referenceToday.isBefore(challenge.effectivePeriodEnd)) referenceToday else challenge.effectivePeriodEnd
        val recordsInPeriod = generateSequence(challenge.periodStart) { it.plusDays(1) }
            .takeWhile { !it.isAfter(trackedEnd) }
            .flatMap { recordsForDate(it) }
            .toList()
        val actualAmount = recordsInPeriod.sumOf { it.amount }

        val progress = challengeState.computeProgress(
            referenceToday,
            challenge,
            hasRecordOnDate = hasRecordOnDate
        ) { date -> recordsForDate(date).sumOf { it.amount } }
        val successDays = progress.dailyRecords.values.count { it == SUCCESS }

        // 판정 순서:
        // 1) 이번 세션에서 방금 포기한 챌린지(로컬 abandonedDate)는 무조건 실패.
        // 2) 서버가 이미 "FAIL"을 확정해서 내려줬다면(포기 처리도 서버에서는 FAIL로 내려올 수 있음)
        //    로컬 abandonedDate가 없어도(세션이 바뀌어 소실됐어도) 실패로 확정한다. remoteStatus는
        //    서버가 이미 최종 처리를 끝냈다는 뜻이므로 날짜 범위보다 우선한다.
        // 3) 그 다음에야 "아직 기간이 끝나지 않았는가"(isOngoing)를 본다. SUCCESS는 여기서 막는다 —
        //    서버가 기간 종료 전에 미리 계산한 SUCCESS 값으로 진행 중인 챌린지가 조기에 성공
        //    표시되는 문제(최초 신고된 버그)를 막기 위함이다.
        // 4) 기간이 끝났는데 SUCCESS도 아니고 FAIL도 아닌 낯선 상태값이면, actualAmount로
        //    임의 추정하지 않고 실패로 취급한다.
        val status = when {
            challenge.abandonedDate != null -> ChallengeResultStatus.FAIL
            challenge.remoteStatus == "FAIL" -> ChallengeResultStatus.FAIL
            isOngoing -> ChallengeResultStatus.IN_PROGRESS
            challenge.remoteStatus == "SUCCESS" -> ChallengeResultStatus.COMPLETE
            challenge.remoteStatus != null -> ChallengeResultStatus.FAIL
            actualAmount <= challenge.targetAmount -> ChallengeResultStatus.COMPLETE
            else -> ChallengeResultStatus.FAIL
        }
        val amountLabel = if (status == ChallengeResultStatus.FAIL) "초과 금액" else "총 절약"
        val amountValue = when (status) {
            ChallengeResultStatus.IN_PROGRESS -> progress.savedAmount
            ChallengeResultStatus.FAIL -> (actualAmount - challenge.targetAmount).coerceAtLeast(0)
            ChallengeResultStatus.COMPLETE -> (challenge.targetAmount - actualAmount).coerceAtLeast(0)
        }

        return ChallengeResultUiState(
            status = status,
            title = "${challenge.totalDays}일 챌린지",
            periodStart = challenge.periodStart,
            periodEnd = challenge.periodEnd,
            totalDays = challenge.totalDays,
            successDays = successDays,
            streakDays = progress.streakDays,
            amountLabel = amountLabel,
            amountValue = amountValue,
            goalAmount = challenge.targetAmount,
            actualAmount = actualAmount,
            dailyLimit = challenge.dailyLimit,
            emotionStats = computeEmotionStats(recordsInPeriod),
            dailyRecords = progress.dailyRecords,
            isEditable = isActiveChallenge && challengeState.hasOngoingChallenge
        )
    }

    private fun fromServerResult(
        challenge: ActiveChallenge,
        summary: ChallengeResultSummary
    ): ChallengeResultUiState {
        // 이 결과가 사용되는 시점은 이미 챌린지가 끝난 뒤이므로, remoteStatus가
        // "SUCCESS"가 아니면(FAIL, 포기 상태값 등) 성공이 아닌 것으로 취급한다.
        val status = if (challenge.remoteStatus == "SUCCESS" && challenge.abandonedDate == null) {
            ChallengeResultStatus.COMPLETE
        } else {
            ChallengeResultStatus.FAIL
        }
        val amountLabel = if (status == ChallengeResultStatus.FAIL) "초과 금액" else "총 절약"
        val amountValue = if (status == ChallengeResultStatus.FAIL) summary.overAmount else summary.savedAmount

        return ChallengeResultUiState(
            status = status,
            title = "${challenge.totalDays}일 챌린지",
            periodStart = challenge.periodStart,
            periodEnd = challenge.periodEnd,
            totalDays = challenge.totalDays,
            successDays = summary.successDays,
            streakDays = summary.maxStreak,
            amountLabel = amountLabel,
            amountValue = amountValue,
            goalAmount = summary.budgetTotal,
            actualAmount = summary.actualSpent,
            dailyLimit = challenge.dailyLimit,
            emotionStats = challenge.emotionBreakdown,
            dailyRecords = challenge.calendarDays,
            isEditable = false
        )
    }

    fun inProgress(
        challengeState: ChallengeState,
        recordsForDate: (LocalDate) -> List<ExpenseRecord>,
        hasRecordOnDate: (LocalDate) -> Boolean = { true },
        referenceToday: LocalDate = LocalDate.now()
    ): ChallengeResultUiState =
        forChallenge(challengeState.activeChallenge!!, challengeState, recordsForDate, hasRecordOnDate, referenceToday)

    fun recommendedTightenedTarget(actualAmount: Int): Int =
        com.example.hampouch.domain.model.recommendedTightenedTarget(actualAmount)
}
