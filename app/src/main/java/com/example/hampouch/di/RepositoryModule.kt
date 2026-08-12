package com.example.hampouch.di

import com.example.hampouch.data.repository.BattleRepositoryImpl
import com.example.hampouch.data.repository.ChallengeRepositoryImpl
import com.example.hampouch.data.repository.ExpenseRepositoryImpl
import com.example.hampouch.data.repository.HamTipsRepositoryImpl
import com.example.hampouch.data.repository.MiniChallengeRepositoryImpl
import com.example.hampouch.data.repository.NotificationRepositoryImpl
import com.example.hampouch.data.repository.RestRepositoryImpl
import com.example.hampouch.domain.repository.BattleRepository
import com.example.hampouch.domain.repository.ChallengeRepository
import com.example.hampouch.domain.repository.ExpenseRepository
import com.example.hampouch.domain.repository.HamTipsRepository
import com.example.hampouch.domain.repository.MiniChallengeRepository
import com.example.hampouch.domain.repository.NotificationRepository
import com.example.hampouch.domain.repository.RestRepository
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

// 도메인 Repository 인터페이스 ↔ data 레이어 구현 연결.
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
}
