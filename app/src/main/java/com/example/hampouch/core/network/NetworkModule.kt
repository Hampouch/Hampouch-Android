package com.example.hampouch.core.network

import android.content.Context
import com.example.hampouch.data.remote.ApiService
import dagger.hilt.EntryPoint
import dagger.hilt.InstallIn
import dagger.hilt.android.EntryPointAccessors
import dagger.hilt.components.SingletonComponent
import okhttp3.OkHttpClient

/**
 * 네트워크 의존성의 정의는 [com.example.hampouch.di.NetworkModule]에 있다.
 * 여기서는 생성자 주입을 받을 수 없는 싱글톤(`AuthRepository`, `HamTipsRepository` 등)이
 * 같은 인스턴스를 쓰도록 EntryPoint로 꺼내 준다.
 */
object NetworkModule {

    @EntryPoint
    @InstallIn(SingletonComponent::class)
    interface NetworkEntryPoint {
        fun apiService(): ApiService
        fun okHttpClient(): OkHttpClient
    }

    private lateinit var appContext: Context

    /** [com.example.hampouch.HampouchApplication.onCreate]에서 한 번 호출한다. */
    fun attach(context: Context) {
        appContext = context.applicationContext
    }

    private val entryPoint: NetworkEntryPoint
        get() = EntryPointAccessors.fromApplication(appContext, NetworkEntryPoint::class.java)

    val okHttpClient: OkHttpClient get() = entryPoint.okHttpClient()

    val apiService: ApiService get() = entryPoint.apiService()
}
