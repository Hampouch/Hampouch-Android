package com.example.hampouch.core.network

import android.content.Context
import com.example.hampouch.data.remote.ApiService
import dagger.hilt.EntryPoint
import dagger.hilt.InstallIn
import dagger.hilt.android.EntryPointAccessors
import dagger.hilt.components.SingletonComponent
import okhttp3.OkHttpClient

/**
 * MVVM 이관 과도기용 브리지.
 *
 * 네트워크 의존성의 진짜 정의는 [com.example.hampouch.di.NetworkModule]에 있고, 여기서는
 * 아직 Hilt 주입으로 옮기지 못한 옛 싱글톤(`AuthRepository`, `HamTipsRepository` 등)이
 * 같은 인스턴스를 쓸 수 있도록 EntryPoint로 꺼내 주기만 한다.
 *
 * 각 도메인이 Repository 인터페이스 + 생성자 주입으로 이관되면 이 파일은 통째로 삭제한다.
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
