package com.example.hampouch.di

import com.example.hampouch.data.local.BattleMockDataSource
import com.example.hampouch.data.local.ExpenseMockDataSource
import java.time.LocalDate
import com.example.hampouch.data.local.HamTipsMockDataSource
import com.example.hampouch.data.local.MiniChallengeMockDataSource
import com.example.hampouch.data.local.NotificationMockDataSource
import com.example.hampouch.domain.model.ExpenseRecord
import com.example.hampouch.domain.model.HamBattleChallenge
import com.example.hampouch.domain.model.HamBattleChallengeRequest
import com.example.hampouch.domain.model.MiniChallengeState
import com.example.hampouch.domain.model.NotificationItem
import com.example.hampouch.domain.model.TipPost
import com.example.hampouch.domain.repository.ChallengeRepository
import com.example.hampouch.ui.expensedetail.ExpenseDetailMockData
import com.example.hampouch.data.local.HamBattleMockStore
import com.example.hampouch.ui.hamtips.HamTipsMockData
import com.example.hampouch.ui.minichallenge.MiniChallengeMockData
import com.example.hampouch.ui.notification.NotificationMockData
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

// 목데이터 공급자 연결
@Module
@InstallIn(SingletonComponent::class)
object MockDataModule {

    @Provides
    @Singleton
    fun provideExpenseMockDataSource(
        challengeRepository: ChallengeRepository
    ): ExpenseMockDataSource = object : ExpenseMockDataSource {
        override fun initialRecords(): Map<String, ExpenseRecord> =
            ExpenseDetailMockData.initialRecords(challengeRepository.state.value)
    }

    @Provides
    @Singleton
    fun provideHamTipsMockDataSource(): HamTipsMockDataSource = object : HamTipsMockDataSource {
        override fun allPosts(): List<TipPost> = HamTipsMockData.allPosts()
    }

    @Provides
    @Singleton
    fun provideMiniChallengeMockDataSource(): MiniChallengeMockDataSource =
        object : MiniChallengeMockDataSource {
            override fun initialState(): MiniChallengeState = MiniChallengeState(
                challengesByDate = mapOf(
                    LocalDate.now().minusDays(1) to MiniChallengeMockData.yesterdayChallenges(),
                    LocalDate.now() to MiniChallengeMockData.todayChallenges()
                ),
                recommendedChallenges = MiniChallengeMockData.recommendedChallenges()
            )
        }

    @Provides
    @Singleton
    fun provideNotificationMockDataSource(): NotificationMockDataSource =
        object : NotificationMockDataSource {
            override fun populated(): List<NotificationItem> = NotificationMockData.populated()
        }

    @Provides
    @Singleton
    fun provideBattleMockDataSource(
        mockStore: HamBattleMockStore
    ): BattleMockDataSource = object : BattleMockDataSource {
        override fun startNewChallenge(request: HamBattleChallengeRequest): HamBattleChallenge =
            mockStore.startNewChallenge(request)
    }
}
