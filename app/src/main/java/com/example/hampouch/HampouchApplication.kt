package com.example.hampouch

import android.app.Application
import com.example.hampouch.core.network.NetworkModule
import com.example.hampouch.data.repository.HamTipsRepository
import com.example.hampouch.domain.repository.ExpenseRepository
import com.example.hampouch.ui.hambattle.HamBattleMockData
import com.kakao.sdk.common.KakaoSdk
import dagger.hilt.android.HiltAndroidApp
import javax.inject.Inject

@HiltAndroidApp
class HampouchApplication : Application() {

    @Inject
    lateinit var expenseRepository: ExpenseRepository

    override fun onCreate() {
        super.onCreate()
        NetworkModule.attach(this)
        KakaoSdk.init(this, BuildConfig.KAKAO_NATIVE_APP_KEY)
        HamTipsRepository.attach(this)
        HamBattleMockData.attach(expenseRepository)
    }
}
