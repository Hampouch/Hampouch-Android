package com.example.hampouch.data.local

import com.example.hampouch.domain.model.HamBattleChallenge
import com.example.hampouch.domain.model.HamBattleChallengeRequest

interface BattleMockDataSource {
    fun startNewChallenge(request: HamBattleChallengeRequest): HamBattleChallenge
}
