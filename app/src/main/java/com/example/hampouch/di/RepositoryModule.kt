package com.example.hampouch.di

import com.example.hampouch.data.repository.BattleRepositoryImpl
import com.example.hampouch.data.repository.ChallengeRepositoryImpl
import com.example.hampouch.data.repository.ExpenseRepositoryImpl
import com.example.hampouch.data.repository.HamTipsRepositoryImpl
import com.example.hampouch.data.repository.MiniChallengeRepositoryImpl
import com.example.hampouch.data.repository.MyPageProfileRepositoryImpl
import com.example.hampouch.data.repository.OnboardingLocalStoreImpl
import com.example.hampouch.data.repository.RecordAlarmRepositoryImpl
import com.example.hampouch.data.repository.RestRepositoryImpl
import com.example.hampouch.data.repository.UsersRepositoryImpl
import com.example.hampouch.data.repository.AuthRepository
import com.example.hampouch.core.network.AuthTokenProvider
import com.example.hampouch.domain.repository.BattleRepository
import com.example.hampouch.domain.repository.ChallengeRepository
import com.example.hampouch.domain.repository.ExpenseRepository
import com.example.hampouch.domain.repository.HamTipsRepository
import com.example.hampouch.domain.repository.MiniChallengeRepository
import com.example.hampouch.domain.repository.MyPageProfileRepository
import com.example.hampouch.domain.repository.OnboardingLocalStore
import com.example.hampouch.domain.repository.RecordAlarmRepository
import com.example.hampouch.domain.repository.RestRepository
import com.example.hampouch.domain.repository.UsersRepository
import com.example.hampouch.ui.widget.HomeWidgetRefreshRequester
import com.example.hampouch.ui.widget.HomeWidgetStatePublisher
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
    abstract fun bindAuthTokenProvider(impl: AuthRepository): AuthTokenProvider

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
    abstract fun bindMiniChallengeRepository(impl: MiniChallengeRepositoryImpl): MiniChallengeRepository

    @Binds
    @Singleton
    abstract fun bindHamTipsRepository(impl: HamTipsRepositoryImpl): HamTipsRepository

    @Binds
    @Singleton
    abstract fun bindBattleRepository(impl: BattleRepositoryImpl): BattleRepository

    @Binds
    @Singleton
    abstract fun bindRecordAlarmRepository(impl: RecordAlarmRepositoryImpl): RecordAlarmRepository

    @Binds
    @Singleton
    abstract fun bindMyPageProfileRepository(
        impl: MyPageProfileRepositoryImpl
    ): MyPageProfileRepository

    @Binds
    @Singleton
    abstract fun bindUsersRepository(impl: UsersRepositoryImpl): UsersRepository

    @Binds
    @Singleton
    abstract fun bindHomeWidgetRefreshRequester(
        impl: HomeWidgetStatePublisher
    ): HomeWidgetRefreshRequester

    @Binds
    @Singleton
    abstract fun bindOnboardingLocalStore(impl: OnboardingLocalStoreImpl): OnboardingLocalStore
}
