package com.example.hampouch.di

import com.example.hampouch.BuildConfig
import com.example.hampouch.core.network.AuthHeaderInterceptor
import com.example.hampouch.core.network.ApiHost
import com.example.hampouch.core.network.TokenAuthenticator
import com.example.hampouch.data.remote.AuthApi
import com.example.hampouch.data.remote.BattleApi
import com.example.hampouch.data.remote.ChallengeApi
import com.example.hampouch.data.remote.CommunityApi
import com.example.hampouch.data.remote.ExpenseApi
import com.example.hampouch.data.remote.MiniChallengeApi
import com.example.hampouch.data.remote.RestApi
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import okhttp3.OkHttpClient
import okhttp3.HttpUrl.Companion.toHttpUrl
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object NetworkModule {

    @Provides
    @Singleton
    fun provideLoggingInterceptor(): HttpLoggingInterceptor = HttpLoggingInterceptor().apply {
        level = if (BuildConfig.DEBUG) {
            // 로그인 응답 body에는 access/refresh token이 있으므로 전체 body를 기록하지 않는다.
            HttpLoggingInterceptor.Level.BASIC
        } else {
            HttpLoggingInterceptor.Level.NONE
        }
    }

    @Provides
    @ApiHost
    fun provideApiHost(): String = BuildConfig.BASE_URL.toHttpUrl().host

    @Provides
    @Singleton
    fun provideOkHttpClient(
        loggingInterceptor: HttpLoggingInterceptor,
        authHeaderInterceptor: AuthHeaderInterceptor,
        tokenAuthenticator: TokenAuthenticator
    ): OkHttpClient = OkHttpClient.Builder()
        .addInterceptor(authHeaderInterceptor)
        .addInterceptor(loggingInterceptor)
        .authenticator(tokenAuthenticator)
        .build()

    @Provides
    @Singleton
    fun provideRetrofit(okHttpClient: OkHttpClient): Retrofit = Retrofit.Builder()
        .baseUrl(BuildConfig.BASE_URL)
        .client(okHttpClient)
        .addConverterFactory(GsonConverterFactory.create())
        .build()

    @Provides
    @Singleton
    fun provideAuthApi(retrofit: Retrofit): AuthApi = retrofit.create(AuthApi::class.java)

    @Provides
    @Singleton
    fun provideBattleApi(retrofit: Retrofit): BattleApi = retrofit.create(BattleApi::class.java)

    @Provides
    @Singleton
    fun provideMiniChallengeApi(retrofit: Retrofit): MiniChallengeApi =
        retrofit.create(MiniChallengeApi::class.java)

    @Provides
    @Singleton
    fun provideCommunityApi(retrofit: Retrofit): CommunityApi = retrofit.create(CommunityApi::class.java)

    @Provides
    @Singleton
    fun provideExpenseApi(retrofit: Retrofit): ExpenseApi = retrofit.create(ExpenseApi::class.java)

    @Provides
    @Singleton
    fun provideChallengeApi(retrofit: Retrofit): ChallengeApi = retrofit.create(ChallengeApi::class.java)

    @Provides
    @Singleton
    fun provideRestApi(retrofit: Retrofit): RestApi = retrofit.create(RestApi::class.java)
}
