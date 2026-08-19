package com.example.hampouch.navigation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.hampouch.data.repository.AuthRepository
import com.example.hampouch.domain.repository.OnboardingLocalStore
import com.example.hampouch.data.repository.PendingChallengeResultStore
import com.example.hampouch.data.repository.SessionStatus
import com.example.hampouch.domain.model.AuthSession
import com.example.hampouch.domain.model.OnboardingRequest
import com.example.hampouch.domain.model.PendingChallengeResult
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import javax.inject.Inject

internal fun pendingChallengeResultRoute(
    userId: Long,
    pending: PendingChallengeResult?
): String? = pending
    ?.takeIf { it.userId == userId }
    ?.let { Screen.ChallengeSummary.createRoute(it.challengeId, locked = true) }

@HiltViewModel
class StartDestinationViewModel @Inject constructor(
    private val authRepository: AuthRepository,
    private val onboardingLocalStore: OnboardingLocalStore,
    private val pendingChallengeResultStore: PendingChallengeResultStore
) : ViewModel() {

    private val _startDestination = MutableStateFlow<String?>(null)
    val startDestination: StateFlow<String?> = _startDestination.asStateFlow()

    private val _pendingNicknameSession = MutableStateFlow<AuthSession?>(null)
    val pendingNicknameSession: StateFlow<AuthSession?> = _pendingNicknameSession.asStateFlow()

    private val _startupErrorMessage = MutableStateFlow<String?>(null)
    val startupErrorMessage: StateFlow<String?> = _startupErrorMessage.asStateFlow()

    private val _isResolving = MutableStateFlow(false)
    val isResolving: StateFlow<Boolean> = _isResolving.asStateFlow()

    val pendingChallengeResult: StateFlow<PendingChallengeResult?> = pendingChallengeResultStore.pending

    init {
        viewModelScope.launch { resolve() }
    }

    private suspend fun resolve() {
        if (_isResolving.value) return
        _isResolving.value = true
        try {
            onboardingLocalStore.restorePendingIfNeeded()
            val session = authRepository.userSession.first()
            val destination = when {
                session == null && onboardingLocalStore.hasCompletedOnboarding() -> {
                    pendingChallengeResultStore.clear()
                    onboardingLocalStore.resetOnboarding()
                    Screen.Onboarding.route
                }
                session == null -> {
                    pendingChallengeResultStore.clear()
                    Screen.Onboarding.route
                }
                else -> resolveForSession(session)
            }
            if (destination != null) {
                _startupErrorMessage.value = null
                _startDestination.value = destination
            }
        } finally {
            _isResolving.value = false
        }
    }

    private suspend fun resolveForSession(session: AuthSession): String? =
        when (val status = authRepository.checkSessionStatus(session)) {
            is SessionStatus.Valid -> resolveValidSession(session, status)
            SessionStatus.Invalid -> {
                pendingChallengeResultStore.clear()
                authRepository.clearSession()
                if (onboardingLocalStore.hasCompletedOnboarding()) {
                    Screen.Login.route
                } else {
                    Screen.Onboarding.route
                }
            }
            is SessionStatus.Unavailable -> {
                _startupErrorMessage.value = status.message
                null
            }
        }

    private suspend fun resolveValidSession(
        session: AuthSession,
        status: SessionStatus.Valid
    ): String {
        if (status.needsNickname) {
            _pendingNicknameSession.value = session
            return Screen.Login.route
        }
        authRepository.saveSession(authRepository.userSession.first() ?: session)
        val pending = pendingChallengeResultStore.pending.value
        val pendingRoute = pendingChallengeResultRoute(session.userId, pending)
        if (pending != null && pendingRoute == null) {
            pendingChallengeResultStore.clear()
        }
        return pendingRoute ?: Screen.Home.route
    }

    fun retry() {
        viewModelScope.launch { resolve() }
    }

    fun captureOnboardingComplete(request: OnboardingRequest) {
        onboardingLocalStore.captureOnboardingComplete(request)
    }

    fun clearPendingChallengeResult() {
        pendingChallengeResultStore.clear()
    }

    fun consumePendingNicknameSession() {
        _pendingNicknameSession.value = null
    }

    fun logout(skipOnboardingSplash: Boolean = true) {
        pendingChallengeResultStore.clear()
        if (skipOnboardingSplash) onboardingLocalStore.markSkipNextSplash()
        viewModelScope.launch { authRepository.clearSession() }
    }
}
