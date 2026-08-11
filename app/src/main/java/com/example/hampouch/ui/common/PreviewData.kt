package com.example.hampouch.ui.common

import com.example.hampouch.domain.model.ActiveChallenge
import com.example.hampouch.domain.model.ChallengeState
import java.time.LocalDate

/**
 * @Preview 전용 챌린지 상태.
 *
 * 목데이터 생성 함수 중에는 진행 중 챌린지를 전제로 하는 것들이 있어(`activeChallenge!!`,
 * `requireNotNull`) 빈 [ChallengeState]를 넘기면 미리보기가 터진다. 항상 이 샘플을 쓴다.
 */
fun previewChallengeState(referenceToday: LocalDate = LocalDate.now()): ChallengeState {
    val periodStart = referenceToday.minusDays(6)
    return ChallengeState(
        challenges = listOf(
            ActiveChallenge(
                id = "preview_challenge",
                totalDays = 14,
                periodStart = periodStart,
                periodEnd = periodStart.plusDays(13),
                dailyLimit = 20_000,
                targetAmount = 280_000,
                savedAmount = 21_400,
                streakDays = 4,
                editCount = 0
            )
        )
    )
}
