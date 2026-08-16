package com.example.hampouch.navigation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.hampouch.data.repository.AuthRepository
import com.example.hampouch.data.repository.OnboardingLocalStore
import com.example.hampouch.data.repository.SessionStatus
import com.example.hampouch.domain.model.AuthSession
import com.example.hampouch.domain.model.OnboardingRequest
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class StartDestinationViewModel @Inject constructor(
    private val authRepository: AuthRepository,
    private val onboardingLocalStore: OnboardingLocalStore
) : ViewModel() {

    private val _startDestination = MutableStateFlow<String?>(null)
    val startDestination: StateFlow<String?> = _startDestination.asStateFlow()

    private val _pendingNicknameSession = MutableStateFlow<AuthSession?>(null)
    val pendingNicknameSession: StateFlow<AuthSession?> = _pendingNicknameSession.asStateFlow()

    private val _startupErrorMessage = MutableStateFlow<String?>(null)
    val startupErrorMessage: StateFlow<String?> = _startupErrorMessage.asStateFlow()

    private val _isResolving = MutableStateFlow(false)
    val isResolving: StateFlow<Boolean> = _isResolving.asStateFlow()

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
                    onboardingLocalStore.resetOnboarding()
                    Screen.Onboarding.route
                }
                session == null -> Screen.Onboarding.route
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
            is SessionStatus.Valid -> {
                if (status.needsNickname) {
                    _pendingNicknameSession.value = session
                    Screen.Login.route
                } else {
                    authRepository.saveSession(authRepository.userSession.first() ?: session)
                    Screen.Home.route
                }
            }
            SessionStatus.Invalid -> {
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

    fun retry() {
        viewModelScope.launch { resolve() }
    }

    fun captureOnboardingComplete(request: OnboardingRequest) {
        onboardingLocalStore.captureOnboardingComplete(request)
    }

    fun markOnboardingSkipped() {
        onboardingLocalStore.markOnboardingSkipped()
    }

    fun consumePendingNicknameSession() {
        _pendingNicknameSession.value = null
    }

    fun logout() {
        viewModelScope.launch { authRepository.clearSession() }
    }
}
