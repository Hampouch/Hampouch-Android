package com.example.hampouch.di

import com.example.hampouch.data.repository.BattleRepositoryImpl
import com.example.hampouch.data.repository.HamTipsRepositoryImpl
import com.example.hampouch.data.repository.MiniChallengeRepositoryImpl
import com.example.hampouch.data.repository.MyPageProfileRepositoryImpl
import com.example.hampouch.data.repository.RecordAlarmRepositoryImpl
import com.example.hampouch.domain.repository.AccountScopedState
import com.example.hampouch.data.local.HamBattleMockStore
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import dagger.multibindings.ElementsIntoSet

@Module
@InstallIn(SingletonComponent::class)
object AccountScopedStateModule {

    @Provides
    @ElementsIntoSet
    fun provideUiScopedStates(
        miniChallengeRepository: MiniChallengeRepositoryImpl,
        hamTipsRepository: HamTipsRepositoryImpl,
        battleRepository: BattleRepositoryImpl,
        recordAlarmRepository: RecordAlarmRepositoryImpl,
        myPageProfileRepository: MyPageProfileRepositoryImpl,
        hamBattleMockStore: HamBattleMockStore
    ): Set<AccountScopedState> = setOf(
        hamTipsRepository,
        battleRepository,
        miniChallengeRepository,
        recordAlarmRepository,
        myPageProfileRepository,
        hamBattleMockStore
    )
}
