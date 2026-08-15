package com.example.hampouch.ui.signup

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.hampouch.data.repository.AuthRepository
import com.example.hampouch.domain.model.EmailVerificationPurpose
import com.example.hampouch.domain.model.toUserMessage
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

data class ResetPasswordUiState(
    val email: String = "",
    val emailCode: String = "",
    val password: String = "",
    val isPasswordVisible: Boolean = false,
    val emailSendMessage: String? = null,
    val emailVerifyMessage: String? = null,
    val isEmailVerified: Boolean = false,
    val isSendingEmailCode: Boolean = false,
    val hasSentEmailCode: Boolean = false,
    val isVerifyingEmailCode: Boolean = false,
    val emailCodeExpiresAtMillis: Long? = null,
    val resetErrorMessage: String? = null
) {
    val isPasswordValid: Boolean
        get() = password.length >= 8 &&
            password.any { it in 'a'..'z' || it in 'A'..'Z' } &&
            password.any { it.isDigit() }

    val showPasswordError: Boolean get() = password.isNotEmpty() && !isPasswordValid

    val isResetEnabled: Boolean get() = isEmailVerified && isPasswordValid
}

sealed interface ResetPasswordEvent {
    data object Reset : ResetPasswordEvent
}

@HiltViewModel
class ResetPasswordViewModel @Inject constructor(
    private val authRepository: AuthRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(ResetPasswordUiState())
    val uiState: StateFlow<ResetPasswordUiState> = _uiState.asStateFlow()

    private val _events = Channel<ResetPasswordEvent>(Channel.BUFFERED)
    val events = _events.receiveAsFlow()

    private fun update(block: ResetPasswordUiState.() -> ResetPasswordUiState) {
        _uiState.value = _uiState.value.block()
    }

    fun changeEmail(value: String) = update {
        copy(
            email = value,
            isEmailVerified = false,
            hasSentEmailCode = false,
            emailSendMessage = null,
            emailVerifyMessage = null,
            emailCodeExpiresAtMillis = null
        )
    }

    fun changeEmailCode(value: String) = update { copy(emailCode = value, isEmailVerified = false) }

    fun changePassword(value: String) = update { copy(password = value) }

    fun togglePasswordVisibility() = update { copy(isPasswordVisible = !isPasswordVisible) }

    fun resetEmailVerification() = update {
        copy(
            hasSentEmailCode = false,
            emailCode = "",
            emailSendMessage = null,
            emailVerifyMessage = null,
            emailCodeExpiresAtMillis = null
        )
    }

    fun sendEmailCode() {
        val email = _uiState.value.email
        if (email.isBlank()) {
            update { copy(emailSendMessage = "이메일을 입력해주세요.") }
            return
        }
        update { copy(isSendingEmailCode = true) }
        viewModelScope.launch {
            authRepository.sendEmailVerificationCode(email, EmailVerificationPurpose.PASSWORD_RESET)
                .onSuccess { result ->
                    update {
                        copy(
                            emailSendMessage = result.message,
                            hasSentEmailCode = true,
                            emailCode = "",
                            emailCodeExpiresAtMillis =
                                System.currentTimeMillis() + result.expiresInSeconds * 1000L
                        )
                    }
                }
                .onFailure { error ->
                    update { copy(emailSendMessage = error.toUserMessage("인증번호 발송에 실패했습니다.")) }
                }
            update { copy(isSendingEmailCode = false) }
        }
    }

    fun verifyEmailCode() {
        val state = _uiState.value
        update { copy(isVerifyingEmailCode = true) }
        viewModelScope.launch {
            authRepository.verifyEmailCode(state.email, state.emailCode, EmailVerificationPurpose.PASSWORD_RESET)
                .onSuccess { result ->
                    update {
                        copy(
                            isEmailVerified = true,
                            emailCodeExpiresAtMillis = null,
                            emailVerifyMessage = result.message
                        )
                    }
                }
                .onFailure { error ->
                    update { emailVerificationFailed(error) }
                }
            update { copy(isVerifyingEmailCode = false) }
        }
    }

    fun resetPassword() {
        val state = _uiState.value
        viewModelScope.launch {
            authRepository.resetPassword(state.email, state.password)
                .onSuccess {
                    update { copy(resetErrorMessage = null) }
                    _events.send(ResetPasswordEvent.Reset)
                }
                .onFailure { error ->
                    update { copy(resetErrorMessage = error.toUserMessage("비밀번호 재설정에 실패했습니다.")) }
                }
        }
    }
}
