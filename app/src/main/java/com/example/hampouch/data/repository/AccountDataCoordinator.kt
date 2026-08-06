package com.example.hampouch.data.repository

import com.example.hampouch.ui.expensedetail.ExpenseDetailStore
import com.example.hampouch.ui.minichallenge.MiniChallengeStore
import com.example.hampouch.ui.mypage.AllSettingsStore
import com.example.hampouch.ui.mypage.MyPageProfileStore
import com.example.hampouch.ui.mypage.RecordAlarmStore
import com.example.hampouch.ui.notification.NotificationStore
import com.example.hampouch.ui.takeabreak.TakeABreakStore

object AccountDataCoordinator {

    private var syncedUserId: String? = null

    fun syncIfNeeded(userId: String) {
        if (userId == syncedUserId) return
        syncedUserId = userId
        ExpenseDetailStore.resetForAccount()
        MiniChallengeStore.resetForAccount()
        TakeABreakStore.resetForAccount()
        RecordAlarmStore.resetForAccount()
        NotificationStore.resetForAccount()
        MyPageProfileStore.resetForAccount()
        AllSettingsStore.resetForAccount()

        val newAccountOnboarding = OnboardingDataStore.consumePendingForLogin(userId)
        if (newAccountOnboarding != null) {
            ChallengeRepository.resetEmpty()
            ChallengeRepository.startNewChallenge(newAccountOnboarding)
        } else {
            ChallengeRepository.resetForAccount()
        }
    }
}
