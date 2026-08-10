package com.example.hampouch.ui.widget

import com.example.hampouch.data.model.HomeChallenge
import com.example.hampouch.data.repository.ChallengeRepository
import com.example.hampouch.ui.expensedetail.ExpenseDetailStore
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.Locale

/**
 * 위젯이 그릴 [HomeWidgetState]를 만든다.
 *
 * 위젯은 앱과 같은 프로세스에서 실행되므로 지금은 홈 화면([com.example.hampouch.ui.home.HomeScreen])이
 * 쓰는 것과 동일한 인메모리 저장소([ChallengeRepository], [ExpenseDetailStore])를 그대로 읽어 계산한다.
 * TODO: 서버 챌린지/지출 API 연동이 끝나면 위젯 갱신도 그 결과를 반영하도록 교체.
 */
object HomeWidgetDataProvider {

    private val periodLabelFormatter = DateTimeFormatter.ofPattern("M월 d일", Locale.KOREA)

    fun currentState(referenceToday: LocalDate = LocalDate.now()): HomeWidgetState {
        val challenge = ChallengeRepository.challengeFor(referenceToday)
            ?: return HomeWidgetState.NoActiveChallenge

        val progress = ChallengeRepository.computeProgress(referenceToday, challenge) { date ->
            ExpenseDetailStore.recordsForDate(date).sumOf { it.amount }
        }
        val dailyLimit = challenge.dailyLimitOn(referenceToday)
        val todaySpent = ExpenseDetailStore.recordsForDate(referenceToday).sumOf { it.amount }

        return HomeWidgetState.InProgress(
            HomeChallenge(
                totalDays = challenge.totalDays,
                dDay = challenge.dDayFrom(referenceToday).coerceAtLeast(0),
                periodStartLabel = challenge.periodStart.format(periodLabelFormatter),
                periodEndLabel = challenge.periodEnd.format(periodLabelFormatter),
                dailyLimit = dailyLimit,
                todayBalance = dailyLimit - todaySpent,
                savedAmount = progress.savedAmount,
                streakDays = progress.streakDays,
                isEnded = challenge.periodEnd.isBefore(referenceToday)
            )
        )
    }
}
