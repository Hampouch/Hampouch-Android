package com.example.hampouch.data.repository

import android.content.Context
import android.util.Log
import com.example.hampouch.data.local.AccountMockDataSource
import com.example.hampouch.domain.repository.AccountScopedState
import com.example.hampouch.domain.repository.ChallengeRepository
import com.example.hampouch.domain.repository.ExpenseRepository
import com.example.hampouch.domain.repository.RestRepository
import javax.inject.Inject
import javax.inject.Singleton

private const val TAG = "AccountDataCoordinator"

/**
 * 로그인 계정이 바뀌었을 때 이전 계정의 캐시를 비운다.
 *
 * 무엇을 비울지는 [AccountScopedState] 구현들이 등록으로 알려주므로, 여기서는 화면 쪽 스토어를
 * 하나씩 알 필요가 없다([com.example.hampouch.di.AccountScopedStateModule] 참고).
 */
@Singleton
class AccountDataCoordinator @Inject constructor(
    private val expenseRepository: ExpenseRepository,
    private val restRepository: RestRepository,
    private val challengeRepository: ChallengeRepository,
    private val accountScopedStates: Set<@JvmSuppressWildcards AccountScopedState>
) {

    private var syncedUserId: String? = null

    suspend fun syncIfNeeded(context: Context, userId: String, email: String?) {
        if (userId == syncedUserId) return
        syncedUserId = userId

        expenseRepository.resetForAccount()
        restRepository.resetForAccount()
        accountScopedStates.forEach { it.resetForAccount() }
        HamTipsRepository.resetForAccount()

        val account = email?.let { e -> AccountMockDataSource.accounts.find { it.email == e } }
        if (account == null || account.isExistingMember) {
            challengeRepository.resetForAccount()
            return
        }

        // 온보딩만 마치고 가입한 새 계정이면, 예약해 둔 온보딩 결과로 첫 챌린지를 시작한다.
        val request = OnboardingDataStore.takeReservedRequest(account.email)
        challengeRepository.resetEmpty()
        if (request != null) {
            challengeRepository.startNewChallenge(request).onFailure { error ->
                Log.e(TAG, "예약된 온보딩 요청으로 챌린지를 시작하지 못했습니다.", error)
            }
        }
        AccountMockDataSource.markAsExistingMember(account.email)
    }
}
