package com.example.hampouch

import android.app.Application
import com.kakao.sdk.common.KakaoSdk
import com.example.hampouch.ui.widget.HomeWidgetStatePublisher
import dagger.hilt.android.HiltAndroidApp
import javax.inject.Inject

@HiltAndroidApp
class HampouchApplication : Application() {

    @Inject
    lateinit var homeWidgetStatePublisher: HomeWidgetStatePublisher

    override fun onCreate() {
        super.onCreate()
        KakaoSdk.init(this, BuildConfig.KAKAO_NATIVE_APP_KEY)
        homeWidgetStatePublisher.start()
    }
}
