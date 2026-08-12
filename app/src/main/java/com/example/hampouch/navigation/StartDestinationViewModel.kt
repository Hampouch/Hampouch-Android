package com.example.hampouch.navigation

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.hampouch.data.repository.AuthRepository
import com.example.hampouch.data.repository.OnboardingDataStore
import com.example.hampouch.data.repository.SessionStatus
import com.example.hampouch.domain.model.AuthSession
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * 앱 진입 시 저장된 세션을 확인해 첫 화면을 정한다.
 *
 * @property startDestination null이면 아직 판단 중 — 화면은 빈 배경만 그린다.
 * @property pendingNicknameSession 소셜 로그인은 됐지만 닉네임이 없어 가입을 마저 받아야 하는 세션.
 */
@HiltViewModel
class StartDestinationViewModel @Inject constructor(
    @ApplicationContext private val context: Context,
    private val authRepository: AuthRepository
) : ViewModel() {

    private val _startDestination = MutableStateFlow<String?>(null)
    val startDestination: StateFlow<String?> = _startDestination.asStateFlow()

    private val _pendingNicknameSession = MutableStateFlow<AuthSession?>(null)
    val pendingNicknameSession: StateFlow<AuthSession?> = _pendingNicknameSession.asStateFlow()

    init {
        viewModelScope.launch { resolve() }
    }

    private suspend fun resolve() {
        OnboardingDataStore.restorePendingIfNeeded(context)
        val session = authRepository.userSession.first()
        _startDestination.value = when {
            session == null && OnboardingDataStore.hasCompletedOnboarding(context) -> Screen.Login.route
            session == null -> Screen.Onboarding.route
            else -> resolveForSession(session)
        }
    }

    private suspend fun resolveForSession(session: AuthSession): String =
        when (val status = authRepository.checkSessionStatus(session)) {
            is SessionStatus.Valid -> {
                if (status.needsNickname) {
                    _pendingNicknameSession.value = session
                    Screen.Login.route
                } else {
                    // checkSessionStatus가 nickname을 갱신했을 수 있으니 최신 세션을 다시 읽어서 동기화한다.
                    authRepository.saveSession(authRepository.userSession.first() ?: session)
                    Screen.Home.route
                }
            }
            SessionStatus.Invalid -> {
                authRepository.clearSession()
                if (OnboardingDataStore.hasCompletedOnboarding(context)) {
                    Screen.Login.route
                } else {
                    Screen.Onboarding.route
                }
            }
            SessionStatus.Unknown -> {
                authRepository.saveSession(session)
                Screen.Home.route
            }
        }

    fun consumePendingNicknameSession() {
        _pendingNicknameSession.value = null
    }

    fun logout() {
        viewModelScope.launch { authRepository.clearSession() }
    }
}
