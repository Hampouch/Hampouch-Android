package com.example.hampouch.ui.login

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.hampouch.data.repository.AuthRepository
import com.example.hampouch.domain.model.AuthSession
import com.example.hampouch.domain.model.SocialCredential
import com.example.hampouch.domain.model.toUserMessage
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

data class LoginUiState(
    val email: String = "",
    val password: String = "",
    val isPasswordVisible: Boolean = false,
    val isSubmitting: Boolean = false,
    val errorMessage: String? = null,
    /** null이 아니면 소셜 가입 닉네임 입력 다이얼로그를 띄운다. */
    val pendingSocialSignUp: AuthSession? = null,
    val socialNickname: String = "",
    val isSocialNicknameAvailable: Boolean = false,
    val socialNicknameCheckMessage: String? = null
)

sealed interface LoginEvent {
    data object LoggedIn : LoginEvent
}

@HiltViewModel
class LoginViewModel @Inject constructor(
    private val authRepository: AuthRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(LoginUiState())
    val uiState: StateFlow<LoginUiState> = _uiState.asStateFlow()

    private val _events = Channel<LoginEvent>(Channel.BUFFERED)
    val events = _events.receiveAsFlow()

    /** 앱 시작 시 이미 "닉네임 필요" 상태로 복원된 세션이 있으면 다이얼로그를 이어서 띄운다. */
    fun restorePendingSocialSignUp(session: AuthSession?) {
        if (session != null && _uiState.value.pendingSocialSignUp == null) {
            _uiState.value = _uiState.value.copy(pendingSocialSignUp = session)
        }
    }

    fun changeEmail(value: String) {
        _uiState.value = _uiState.value.copy(email = value)
    }

    fun changePassword(value: String) {
        _uiState.value = _uiState.value.copy(password = value)
    }

    fun togglePasswordVisibility() {
        _uiState.value = _uiState.value.copy(isPasswordVisible = !_uiState.value.isPasswordVisible)
    }

    fun showError(message: String) {
        _uiState.value = _uiState.value.copy(errorMessage = message)
    }

    fun login() {
        val state = _uiState.value
        if (state.isSubmitting) return
        _uiState.value = state.copy(isSubmitting = true, errorMessage = null)
        viewModelScope.launch {
            authRepository.login(state.email, state.password)
                .onSuccess {
                    _uiState.value = _uiState.value.copy(isSubmitting = false, errorMessage = null)
                    _events.send(LoginEvent.LoggedIn)
                }
                .onFailure { error ->
                    _uiState.value = _uiState.value.copy(
                        isSubmitting = false,
                        errorMessage = error.toUserMessage("로그인에 실패했습니다.")
                    )
                }
        }
    }

    /** 카카오/구글 SDK가 돌려준 자격증명으로 서버 로그인을 마친다. */
    fun loginWithSocial(credential: SocialCredential, failureMessage: String) {
        if (_uiState.value.isSubmitting) return
        _uiState.value = _uiState.value.copy(isSubmitting = true, errorMessage = null)
        viewModelScope.launch {
            authRepository.loginWithSocial(credential)
                .onSuccess { outcome ->
                    _uiState.value = _uiState.value.copy(
                        isSubmitting = false,
                        errorMessage = null,
                        // 닉네임이 없는 신규 소셜 계정이면 가입을 마저 받아야 한다.
                        pendingSocialSignUp = outcome.session.takeIf { outcome.requiresNickname }
                    )
                    if (!outcome.requiresNickname) _events.send(LoginEvent.LoggedIn)
                }
                .onFailure { error ->
                    _uiState.value = _uiState.value.copy(
                        isSubmitting = false,
                        errorMessage = error.toUserMessage(failureMessage)
                    )
                }
        }
    }

    fun changeSocialNickname(value: String) {
        _uiState.value = _uiState.value.copy(
            socialNickname = value,
            isSocialNicknameAvailable = false,
            socialNicknameCheckMessage = null
        )
    }

    fun checkSocialNickname() {
        val nickname = _uiState.value.socialNickname
        viewModelScope.launch {
            authRepository.checkNicknameAvailability(nickname)
                .onSuccess { available ->
                    _uiState.value = _uiState.value.copy(
                        isSocialNicknameAvailable = available,
                        socialNicknameCheckMessage =
                            if (available) "사용 가능한 닉네임입니다." else "이미 존재하는 닉네임입니다."
                    )
                }
                .onFailure { error ->
                    _uiState.value = _uiState.value.copy(
                        isSocialNicknameAvailable = false,
                        socialNicknameCheckMessage = error.toUserMessage("닉네임 확인에 실패했습니다.")
                    )
                }
        }
    }

    fun completeSocialSignUp() {
        val state = _uiState.value
        val session = state.pendingSocialSignUp ?: return
        viewModelScope.launch {
            authRepository.completeSocialSignUp(session, state.socialNickname)
                .onSuccess {
                    _uiState.value = _uiState.value.copy(
                        pendingSocialSignUp = null,
                        socialNickname = "",
                        isSocialNicknameAvailable = false,
                        socialNicknameCheckMessage = null,
                        errorMessage = null
                    )
                    _events.send(LoginEvent.LoggedIn)
                }
                .onFailure { error ->
                    _uiState.value = _uiState.value.copy(
                        socialNicknameCheckMessage = error.toUserMessage("닉네임 설정에 실패했습니다.")
                    )
                }
        }
    }

    fun cancelSocialSignUp() {
        _uiState.value = _uiState.value.copy(
            pendingSocialSignUp = null,
            socialNickname = "",
            isSocialNicknameAvailable = false,
            socialNicknameCheckMessage = null
        )
    }
}
