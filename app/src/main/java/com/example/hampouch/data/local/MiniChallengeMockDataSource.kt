package com.example.hampouch.data.local

import com.example.hampouch.domain.model.MiniChallengeState

interface MiniChallengeMockDataSource {
    fun initialState(): MiniChallengeState
}
