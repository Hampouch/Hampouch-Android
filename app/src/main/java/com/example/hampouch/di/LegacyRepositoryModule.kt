package com.example.hampouch.di

import android.content.Context
import com.example.hampouch.data.repository.AuthRepository
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

/**
 * MVVM 이관 과도기용 모듈.
 *
 * 아직 `getInstance(context)` 싱글톤으로 남아 있는 옛 Repository를 Hilt 그래프에 올려,
 * 새로 만드는 Repository 구현과 ViewModel이 지금부터 생성자 주입으로 받을 수 있게 한다.
 * 해당 클래스가 `@Inject constructor`로 바뀌면 여기서 지우기만 하면 되고, 주입받는 쪽은
 * 고칠 필요가 없다.
 */
@Module
@InstallIn(SingletonComponent::class)
object LegacyRepositoryModule {

    @Provides
    @Singleton
    fun provideAuthRepository(@ApplicationContext context: Context): AuthRepository =
        AuthRepository.getInstance(context)
}
