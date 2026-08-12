package com.example.hampouch.data.repository

import android.util.Log
import com.example.hampouch.data.local.AccountMockDataSource
import com.example.hampouch.domain.repository.AccountScopedState
import com.example.hampouch.domain.repository.ChallengeRepository
import com.example.hampouch.domain.repository.ExpenseRepository
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
    private val onboardingLocalStore: OnboardingLocalStore
) {

    private var syncedUserId: String? = null

    suspend fun syncIfNeeded(userId: String, email: String?) {
        if (userId == syncedUserId) return
        syncedUserId = userId

        expenseRepository.resetForAccount()
        restRepository.resetForAccount()
        accountScopedStates.forEach { it.resetForAccount() }

        val account = email?.let { e -> AccountMockDataSource.accounts.find { it.email == e } }
        if (account == null || account.isExistingMember) {
            challengeRepository.resetForAccount()
            return
        }

        // 온보딩만 마치고 가입한 새 계정이면, 예약해 둔 온보딩 결과로 첫 챌린지를 시작한다.
        val request = onboardingLocalStore.takeReservedRequest(account.email)
        challengeRepository.resetEmpty()
        if (request != null) {
            challengeRepository.startNewChallenge(request).onFailure { error ->
                Log.e(TAG, "예약된 온보딩 요청으로 챌린지를 시작하지 못했습니다.", error)
            }
        }
        AccountMockDataSource.markAsExistingMember(account.email)
    }
}
