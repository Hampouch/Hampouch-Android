package com.example.hampouch.di

import com.example.hampouch.data.local.ExpenseMockDataSource
import com.example.hampouch.data.local.HamTipsMockDataSource
import com.example.hampouch.data.local.MiniChallengeLocalStore
import com.example.hampouch.domain.model.ExpenseRecord
import com.example.hampouch.domain.model.TipPost
import com.example.hampouch.domain.repository.ChallengeRepository
import com.example.hampouch.ui.expensedetail.ExpenseDetailMockData
import com.example.hampouch.ui.hamtips.HamTipsMockData
import com.example.hampouch.ui.minichallenge.MiniChallengeStore
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

/** 목데이터 공급자 연결. 목데이터 생성 코드(`ui/**/*MockData.kt`)에 대한 의존을 여기 한 곳에 모은다. */
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
    fun provideMiniChallengeLocalStore(): MiniChallengeLocalStore = MiniChallengeStore
}
