package com.example.hampouch.di

import android.content.Context
import com.example.hampouch.data.local.HamTipsMockDataSource
import com.example.hampouch.data.repository.AccountDataCoordinator
import com.example.hampouch.domain.repository.ChallengeRepository
import com.example.hampouch.domain.repository.ExpenseRepository
import com.example.hampouch.domain.repository.RestRepository
import dagger.hilt.EntryPoint
import dagger.hilt.InstallIn
import dagger.hilt.android.EntryPointAccessors
import dagger.hilt.components.SingletonComponent

/**
 * `object` 싱글톤처럼 생성자 주입을 받을 수 없는 코드가 Hilt 그래프의 의존성을 꺼내 쓰기 위한 통로.
 */
@EntryPoint
@InstallIn(SingletonComponent::class)
interface LegacyEntryPoint {
    fun restRepository(): RestRepository
    fun expenseRepository(): ExpenseRepository
    fun challengeRepository(): ChallengeRepository
    fun accountDataCoordinator(): AccountDataCoordinator
    fun hamTipsMockDataSource(): HamTipsMockDataSource
}

fun Context.legacyEntryPoint(): LegacyEntryPoint =
    EntryPointAccessors.fromApplication(applicationContext, LegacyEntryPoint::class.java)
