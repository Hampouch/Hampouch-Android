package com.example.hampouch.ui.home

import com.example.hampouch.domain.model.ChallengeProgress
import com.example.hampouch.domain.model.HomeChallenge

/**
 * 서버 모드에서는 GET /api/challenges/current의 progress 값을 단일 기준으로 사용한다.
 * 로컬 진행률 계산은 목 모드에서만 홈 화면 값에 반영한다.
 */
internal fun resolveHomeChallengeProgress(
    challenge: HomeChallenge,
    localProgress: ChallengeProgress?,
    useServerChallenge: Boolean
): HomeChallenge {
    if (useServerChallenge || localProgress == null) return challenge

    return challenge.copy(
        savedAmount = localProgress.savedAmount,
        streakDays = localProgress.streakDays
    )
}
