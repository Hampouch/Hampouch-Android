package com.example.hampouch.core.network

import com.example.hampouch.BuildConfig
import com.example.hampouch.data.remote.ApiService
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

object NetworkModule {

    private val loggingInterceptor by lazy {
        HttpLoggingInterceptor().apply {
            level = if (BuildConfig.DEBUG) {
                HttpLoggingInterceptor.Level.BODY
            } else {
                HttpLoggingInterceptor.Level.NONE
            }
        }
    }

    private val okHttpClient by lazy {
        OkHttpClient.Builder()
            .addInterceptor(loggingInterceptor)
            .build()
    }

    // baseUrl()이 BuildConfig.BASE_URL 미설정 시 즉시 예외를 던지므로,
    // 클래스 초기화(<clinit>) 시점이 아니라 실제 호출 시점에 예외가 나도록 지연 생성한다.
    // <clinit>에서 던지면 ExceptionInInitializerError(Error)로 감싸져 호출부의 catch(Exception)에 잡히지 않는다.
    private val retrofit by lazy {
        Retrofit.Builder()
            .baseUrl(BuildConfig.BASE_URL)
            .client(okHttpClient)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
    }

    val apiService: ApiService by lazy { retrofit.create(ApiService::class.java) }
}
