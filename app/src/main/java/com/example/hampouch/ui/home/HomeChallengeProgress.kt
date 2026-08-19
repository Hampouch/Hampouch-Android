package com.example.hampouch.ui.home

import com.example.hampouch.domain.model.ChallengeProgress
import com.example.hampouch.domain.model.HomeChallenge

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
