package com.example.hampouch.ui.home

import com.example.hampouch.domain.model.ChallengeProgress
import com.example.hampouch.domain.model.HomeChallenge

internal fun resolveHomeChallengeProgress(
    challenge: HomeChallenge,
    localProgress: ChallengeProgress?,
    useServerChallenge: Boolean
): HomeChallenge {
    if (localProgress == null) return challenge

    return challenge.copy(
        savedAmount = if (useServerChallenge) challenge.savedAmount else localProgress.savedAmount,
        // Always match the challenge-detail screen's locally computed streak, since the server's
        // streak field can drift from what's actually shown there for an ongoing challenge.
        streakDays = localProgress.streakDays
    )
}
