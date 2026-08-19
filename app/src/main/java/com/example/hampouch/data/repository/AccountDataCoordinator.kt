package com.example.hampouch.data.repository

import android.content.Context
import android.util.Log
import com.example.hampouch.domain.repository.AccountScopedState
import com.example.hampouch.domain.repository.ChallengeRepository
import com.example.hampouch.domain.repository.ExpenseRepository
import com.example.hampouch.domain.repository.OnboardingLocalStore
import com.example.hampouch.domain.repository.RestRepository
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

private const val TAG = "AccountDataCoordinator"
private const val PREFS_NAME = "hampouch_account_sync"
private const val KEY_SYNCED_USER_ID = "synced_user_id"

@Singleton
class AccountDataCoordinator @Inject constructor(
    @ApplicationContext private val context: Context,
    private val expenseRepository: ExpenseRepository,
    private val restRepository: RestRepository,
    private val challengeRepository: ChallengeRepository,
    private val accountScopedStates: Set<@JvmSuppressWildcards AccountScopedState>,
    private val onboardingLocalStore: OnboardingLocalStore
) {

    private fun prefs() =
        context.applicationContext.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

    private var syncedUserId: String? = prefs().getString(KEY_SYNCED_USER_ID, null)

    suspend fun syncIfNeeded(userId: String, email: String?) {
        if (userId == syncedUserId) return
        syncedUserId = userId
        prefs().edit().putString(KEY_SYNCED_USER_ID, userId).apply()

        expenseRepository.resetForAccount()
        restRepository.resetForAccount()
        accountScopedStates.forEach { it.resetForAccount() }

        val request = email?.let { onboardingLocalStore.takeReservedRequest(it) }
        if (request == null) {
            challengeRepository.resetForAccount()
            return
        }

        challengeRepository.resetEmpty()
        challengeRepository.startNewChallenge(request).onFailure { error ->
            Log.e(TAG, "예약된 온보딩 요청으로 챌린지를 시작하지 못했습니다.", error)
        }
    }
}
