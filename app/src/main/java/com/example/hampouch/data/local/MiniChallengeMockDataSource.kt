package com.example.hampouch.data.local

import com.example.hampouch.domain.model.MiniChallengeState

/** 목데이터 모드에서 쓰는 초기 미니 챌린지 상태. 구현 연결은 [com.example.hampouch.di.MockDataModule]에서 한다. */
interface MiniChallengeMockDataSource {
    fun initialState(): MiniChallengeState
}
