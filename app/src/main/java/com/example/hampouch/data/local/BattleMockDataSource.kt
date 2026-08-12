package com.example.hampouch.data.local

import com.example.hampouch.domain.model.HamBattleChallenge
import com.example.hampouch.domain.model.HamBattleChallengeRequest

/** 목데이터 모드의 햄배틀 생성. 구현 연결은 [com.example.hampouch.di.MockDataModule]에서 한다. */
interface BattleMockDataSource {
    fun startNewChallenge(request: HamBattleChallengeRequest): HamBattleChallenge
}
