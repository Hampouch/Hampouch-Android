package com.example.hampouch.di

import com.example.hampouch.data.repository.BattleRepositoryImpl
import com.example.hampouch.data.repository.ChallengeRepositoryImpl
import com.example.hampouch.data.repository.ExpenseRepositoryImpl
import com.example.hampouch.data.repository.HamTipsRepositoryImpl
import com.example.hampouch.data.repository.MiniChallengeRepositoryImpl
import com.example.hampouch.data.repository.MyPageProfileRepositoryImpl
import com.example.hampouch.data.repository.NotificationRepositoryImpl
import com.example.hampouch.data.repository.NotificationSettingsRepositoryImpl
import com.example.hampouch.data.repository.RecordAlarmRepositoryImpl
import com.example.hampouch.data.repository.RestRepositoryImpl
import com.example.hampouch.domain.repository.BattleRepository
import com.example.hampouch.domain.repository.ChallengeRepository
import com.example.hampouch.domain.repository.ExpenseRepository
import com.example.hampouch.domain.repository.HamTipsRepository
import com.example.hampouch.domain.repository.MiniChallengeRepository
import com.example.hampouch.domain.repository.MyPageProfileRepository
import com.example.hampouch.domain.repository.NotificationRepository
import com.example.hampouch.domain.repository.NotificationSettingsRepository
import com.example.hampouch.domain.repository.RecordAlarmRepository
import com.example.hampouch.domain.repository.RestRepository
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class RepositoryModule {

    @Binds
    @Singleton
    abstract fun bindRestRepository(impl: RestRepositoryImpl): RestRepository

    @Binds
    @Singleton
    abstract fun bindExpenseRepository(impl: ExpenseRepositoryImpl): ExpenseRepository

    @Binds
    @Singleton
    abstract fun bindChallengeRepository(impl: ChallengeRepositoryImpl): ChallengeRepository

    @Binds
    @Singleton
    abstract fun bindNotificationRepository(impl: NotificationRepositoryImpl): NotificationRepository

    @Binds
    @Singleton
    abstract fun bindMiniChallengeRepository(impl: MiniChallengeRepositoryImpl): MiniChallengeRepository

    @Binds
    @Singleton
    abstract fun bindHamTipsRepository(impl: HamTipsRepositoryImpl): HamTipsRepository

    @Binds
    @Singleton
    abstract fun bindBattleRepository(impl: BattleRepositoryImpl): BattleRepository

    @Binds
    @Singleton
    abstract fun bindNotificationSettingsRepository(
        impl: NotificationSettingsRepositoryImpl
    ): NotificationSettingsRepository

    @Binds
    @Singleton
    abstract fun bindRecordAlarmRepository(impl: RecordAlarmRepositoryImpl): RecordAlarmRepository

    @Binds
    @Singleton
    abstract fun bindMyPageProfileRepository(
        impl: MyPageProfileRepositoryImpl
    ): MyPageProfileRepository
}
