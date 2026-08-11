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

/**
 * 목데이터 공급자 연결.
 *
 * 목데이터 생성 코드가 아직 화면 패키지에 남아 있어(`ui/**/*MockData.kt`) data 레이어에서 바로 쓸 수
 * 없다. UI 의존을 이 파일 한 곳에만 몰아 두고, 목데이터를 data 레이어로 옮기면 여기만 고치면 된다.
 */
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
