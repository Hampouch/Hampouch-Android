package com.example.hampouch.di

import android.content.Context
import com.example.hampouch.data.repository.AuthRepository
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

/** `getInstance(context)` 싱글톤인 Repository를 Hilt 그래프에 올려 생성자 주입이 가능하게 한다. */
@Module
@InstallIn(SingletonComponent::class)
object LegacyRepositoryModule {

    @Provides
    @Singleton
    fun provideAuthRepository(@ApplicationContext context: Context): AuthRepository =
        AuthRepository.getInstance(context)
}
