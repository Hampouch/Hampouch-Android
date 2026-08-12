package com.example.hampouch.di

import com.example.hampouch.data.repository.BattleRepositoryImpl
import com.example.hampouch.data.repository.HamTipsRepositoryImpl
import com.example.hampouch.data.repository.MiniChallengeRepositoryImpl
import com.example.hampouch.data.repository.NotificationRepositoryImpl
import com.example.hampouch.domain.repository.AccountScopedState
import com.example.hampouch.ui.hambattle.HamBattleMockData
import com.example.hampouch.ui.mypage.AllSettingsStore
import com.example.hampouch.ui.mypage.MyPageProfileStore
import com.example.hampouch.ui.mypage.RecordAlarmStore
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
        notificationRepository: NotificationRepositoryImpl,
        miniChallengeRepository: MiniChallengeRepositoryImpl,
        hamTipsRepository: HamTipsRepositoryImpl,
        battleRepository: BattleRepositoryImpl
    ): Set<AccountScopedState> = setOf(
        hamTipsRepository,
        battleRepository,
        notificationRepository,
        miniChallengeRepository,
        RecordAlarmStore,
        MyPageProfileStore,
        AllSettingsStore,
        HamBattleMockData
    )
}
