package com.example.hampouch.data.repository

import android.util.Log
import com.example.hampouch.domain.repository.AccountScopedState
import com.example.hampouch.domain.repository.ChallengeRepository
import com.example.hampouch.domain.repository.ExpenseRepository
import com.example.hampouch.domain.repository.OnboardingLocalStore
import com.example.hampouch.domain.repository.NotificationSettingsRepository
import com.example.hampouch.domain.repository.RestRepository
import javax.inject.Inject
import javax.inject.Singleton

private const val TAG = "AccountDataCoordinator"

@Singleton
class AccountDataCoordinator @Inject constructor(
    private val expenseRepository: ExpenseRepository,
    private val restRepository: RestRepository,
    private val challengeRepository: ChallengeRepository,
    private val accountScopedStates: Set<@JvmSuppressWildcards AccountScopedState>,
    private val onboardingLocalStore: OnboardingLocalStore,
    private val notificationSettingsRepository: NotificationSettingsRepository
) {

    private var syncedUserId: String? = null

    suspend fun syncIfNeeded(userId: String, email: String?) {
        if (userId == syncedUserId) return
        syncedUserId = userId

        expenseRepository.resetForAccount()
        restRepository.resetForAccount()
        accountScopedStates.forEach { it.resetForAccount() }
        notificationSettingsRepository.activateAccount(userId, email)

        /** 예약된 온보딩 요청은 이번 프로세스의 회원가입 성공 직후에만 존재하므로, 그 존재 여부가 곧 "신규 회원" 신호다. */
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
