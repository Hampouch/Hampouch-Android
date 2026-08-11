package com.example.hampouch.di

import com.example.hampouch.domain.repository.AccountScopedState
import com.example.hampouch.ui.hambattle.HamBattleMockData
import com.example.hampouch.ui.hambattle.HamBattleStore
import com.example.hampouch.ui.minichallenge.MiniChallengeStore
import com.example.hampouch.ui.mypage.AllSettingsStore
import com.example.hampouch.ui.mypage.MyPageProfileStore
import com.example.hampouch.ui.mypage.RecordAlarmStore
import com.example.hampouch.ui.notification.NotificationStore
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import dagger.multibindings.ElementsIntoSet

/**
 * 계정 전환 시 비워야 하는 상태들을 등록한다.
 * data 레이어는 [AccountScopedState] 인터페이스만 알면 되도록, ui 참조는 이 파일에만 둔다.
 */
@Module
@InstallIn(SingletonComponent::class)
object AccountScopedStateModule {

    @Provides
    @ElementsIntoSet
    fun provideUiScopedStates(): Set<AccountScopedState> = setOf(
        MiniChallengeStore,
        RecordAlarmStore,
        NotificationStore,
        MyPageProfileStore,
        AllSettingsStore,
        HamBattleMockData,
        HamBattleStore
    )
}
