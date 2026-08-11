package com.example.hampouch.di

import com.example.hampouch.data.repository.ChallengeRepositoryImpl
import com.example.hampouch.data.repository.ExpenseRepositoryImpl
import com.example.hampouch.data.repository.RestRepositoryImpl
import com.example.hampouch.domain.repository.ChallengeRepository
import com.example.hampouch.domain.repository.ExpenseRepository
import com.example.hampouch.domain.repository.RestRepository
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

/** 도메인 Repository 인터페이스 ↔ data 레이어 구현 연결. */
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
}
