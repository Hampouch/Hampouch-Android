package com.example.hampouch.data.repository

import android.content.Context
import android.util.Log
import com.example.hampouch.ui.expensedetail.ExpenseDetailStore
import com.example.hampouch.ui.hambattle.HamBattleMockData
import com.example.hampouch.ui.login.LoginMockData
import com.example.hampouch.ui.minichallenge.MiniChallengeStore
import com.example.hampouch.ui.mypage.AllSettingsStore
import com.example.hampouch.ui.mypage.MyPageProfileStore
import com.example.hampouch.ui.mypage.RecordAlarmStore
import com.example.hampouch.ui.notification.NotificationStore
import com.example.hampouch.ui.takeabreak.TakeABreakStore

object AccountDataCoordinator {

    private const val TAG = "AccountDataCoordinator"

    private var syncedUserId: String? = null

    suspend fun syncIfNeeded(context: Context, userId: String, email: String?) {
        if (userId == syncedUserId) return
        syncedUserId = userId
        ExpenseDetailStore.resetForAccount()
        MiniChallengeStore.resetForAccount()
        TakeABreakStore.resetForAccount()
        RecordAlarmStore.resetForAccount()
        NotificationStore.resetForAccount()
        MyPageProfileStore.resetForAccount()
        AllSettingsStore.resetForAccount()
        HamBattleMockData.resetForAccount()
        HamTipsRepository.resetForAccount()

        val account = email?.let { e -> LoginMockData.accounts.find { it.email == e } }
        if (account == null || account.isExistingMember) {
            ChallengeRepository.resetForAccount()
            return
        }

        val request = OnboardingDataStore.takeReservedRequest(account.email)
        ChallengeRepository.resetEmpty()
        if (request != null) {
            ChallengeRepository.startNewChallenge(request).onFailure { error ->
                Log.e(TAG, "예약된 온보딩 요청으로 챌린지를 시작하지 못했습니다.", error)
            }
        }
        LoginMockData.markAsExistingMember(account.email)
    }
}
