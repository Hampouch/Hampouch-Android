package com.example.hampouch.di

import android.content.Context
import com.example.hampouch.data.local.HamTipsMockDataSource
import com.example.hampouch.data.local.MiniChallengeLocalStore
import com.example.hampouch.data.repository.AccountDataCoordinator
import com.example.hampouch.domain.repository.ChallengeRepository
import com.example.hampouch.domain.repository.ExpenseRepository
import com.example.hampouch.domain.repository.RestRepository
import dagger.hilt.EntryPoint
import dagger.hilt.InstallIn
import dagger.hilt.android.EntryPointAccessors
import dagger.hilt.components.SingletonComponent

/**
 * MVVM 이관 과도기용 브리지.
 *
 * 아직 `object` 싱글톤으로 남아 생성자 주입을 받을 수 없는 코드(예: [com.example.hampouch.data.repository.AccountDataCoordinator])가
 * Hilt 그래프의 Repository를 꺼내 쓰기 위한 통로다. 해당 싱글톤들이 주입 가능한 클래스로
 * 바뀌면 이 파일은 통째로 삭제한다.
 */
@EntryPoint
@InstallIn(SingletonComponent::class)
interface LegacyEntryPoint {
    fun restRepository(): RestRepository
    fun expenseRepository(): ExpenseRepository
    fun challengeRepository(): ChallengeRepository
    fun accountDataCoordinator(): AccountDataCoordinator
    fun hamTipsMockDataSource(): HamTipsMockDataSource
    fun miniChallengeLocalStore(): MiniChallengeLocalStore
}

fun Context.legacyEntryPoint(): LegacyEntryPoint =
    EntryPointAccessors.fromApplication(applicationContext, LegacyEntryPoint::class.java)
