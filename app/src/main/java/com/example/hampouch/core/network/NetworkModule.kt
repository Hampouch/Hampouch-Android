package com.example.hampouch.core.network

import android.content.Context
import com.example.hampouch.BuildConfig
import com.example.hampouch.data.remote.ApiService
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

object NetworkModule {

    private lateinit var appContext: Context

    /**
     * [com.example.hampouch.HampouchApplication.onCreate]에서 한 번 호출한다.
     * 401 응답을 가로채 토큰을 재발급하는 [TokenAuthenticator]가 AuthRepository에
     * 접근하려면 Context가 필요하기 때문이다.
     */
    fun attach(context: Context) {
        appContext = context.applicationContext
    }

    private val loggingInterceptor by lazy {
        HttpLoggingInterceptor().apply {
            level = if (BuildConfig.DEBUG) {
                HttpLoggingInterceptor.Level.BODY
            } else {
                HttpLoggingInterceptor.Level.NONE
            }
        }
    }

    private val tokenAuthenticator by lazy { TokenAuthenticator { appContext } }

    val okHttpClient by lazy {
        OkHttpClient.Builder()
            .addInterceptor(loggingInterceptor)
            .authenticator(tokenAuthenticator)
            .build()
    }

    private val retrofit by lazy {
        Retrofit.Builder()
            .baseUrl(BuildConfig.BASE_URL)
            .client(okHttpClient)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
    }

    val apiService: ApiService by lazy { retrofit.create(ApiService::class.java) }
}
