package com.example.hampouch.data.repository

import com.example.hampouch.ui.expensedetail.ExpenseDetailStore
import com.example.hampouch.ui.minichallenge.MiniChallengeStore
import com.example.hampouch.ui.mypage.RecordAlarmStore
import com.example.hampouch.ui.takeabreak.TakeABreakStore

object AccountDataCoordinator {

    private var syncedUserId: String? = null

    fun syncIfNeeded(userId: String) {
        if (userId == syncedUserId) return
        syncedUserId = userId
        ChallengeRepository.resetForAccount()
        ExpenseDetailStore.resetForAccount()
        MiniChallengeStore.resetForAccount()
        TakeABreakStore.resetForAccount()
        RecordAlarmStore.resetForAccount()
    }
}
