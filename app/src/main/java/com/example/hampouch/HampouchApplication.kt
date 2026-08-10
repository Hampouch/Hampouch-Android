package com.example.hampouch

import android.app.Application
import com.example.hampouch.data.repository.HamTipsRepository
import com.example.hampouch.ui.expensedetail.ExpenseDetailStore
import com.example.hampouch.ui.takeabreak.TakeABreakStore
import com.kakao.sdk.common.KakaoSdk

class HampouchApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        KakaoSdk.init(this, BuildConfig.KAKAO_NATIVE_APP_KEY)
        HamTipsRepository.attach(this)
        ExpenseDetailStore.attach(this)
        TakeABreakStore.attach(this)
    }
}
