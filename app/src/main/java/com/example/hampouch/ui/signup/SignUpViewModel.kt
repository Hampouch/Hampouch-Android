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

data class SignUpUiState(
    val email: String = "",
    val emailCode: String = "",
    val password: String = "",
    val nickname: String = "",
    val isPasswordVisible: Boolean = false,
    val emailSendMessage: String? = null,
    val emailVerifyMessage: String? = null,
    val isEmailVerified: Boolean = false,
    val isSendingEmailCode: Boolean = false,
    val hasSentEmailCode: Boolean = false,
    val isVerifyingEmailCode: Boolean = false,
    val emailCodeExpiresAtMillis: Long? = null,
    val nicknameCheckMessage: String? = null,
    val isNicknameAvailable: Boolean = false,
    val isTermsChecked: Boolean = false,
    val isPrivacyChecked: Boolean = false,
    val isMarketingChecked: Boolean = false,
    val signUpErrorMessage: String? = null
) {
    val isPasswordValid: Boolean
        get() = password.length >= 8 &&
            password.any { it in 'a'..'z' || it in 'A'..'Z' } &&
            password.any { it.isDigit() }

    val showPasswordError: Boolean get() = password.isNotEmpty() && !isPasswordValid

    val areRequiredTermsChecked: Boolean get() = isTermsChecked && isPrivacyChecked

    val isSignUpEnabled: Boolean
        get() = isEmailVerified && isNicknameAvailable && isPasswordValid && areRequiredTermsChecked
}

sealed interface SignUpEvent {
    data object SignedUp : SignUpEvent
}

@HiltViewModel
class SignUpViewModel @Inject constructor(
    private val authRepository: AuthRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(SignUpUiState())
    val uiState: StateFlow<SignUpUiState> = _uiState.asStateFlow()

    private val _events = Channel<SignUpEvent>(Channel.BUFFERED)
    val events = _events.receiveAsFlow()

    private fun update(block: SignUpUiState.() -> SignUpUiState) {
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

    fun changeNickname(value: String) = update { copy(nickname = value, isNicknameAvailable = false) }

    fun togglePasswordVisibility() = update { copy(isPasswordVisible = !isPasswordVisible) }

    fun setTermsChecked(value: Boolean) = update { copy(isTermsChecked = value) }

    fun setPrivacyChecked(value: Boolean) = update { copy(isPrivacyChecked = value) }

    fun setMarketingChecked(value: Boolean) = update { copy(isMarketingChecked = value) }

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
            authRepository.sendEmailVerificationCode(email, EmailVerificationPurpose.SIGNUP)
                .onSuccess { expiresInSeconds ->
                    update {
                        copy(
                            emailSendMessage = "인증번호가 발송되었습니다.",
                            hasSentEmailCode = true,
                            emailCode = "",
                            emailCodeExpiresAtMillis =
                                System.currentTimeMillis() + expiresInSeconds * 1000L
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
            authRepository.verifyEmailCode(state.email, state.emailCode, EmailVerificationPurpose.SIGNUP)
                .onSuccess {
                    update {
                        copy(
                            isEmailVerified = true,
                            emailCodeExpiresAtMillis = null,
                            emailVerifyMessage = "이메일 인증이 완료되었습니다."
                        )
                    }
                }
                .onFailure { error ->
                    update {
                        copy(
                            isEmailVerified = false,
                            emailVerifyMessage = error.toUserMessage("인증번호를 다시 확인해주세요.")
                        )
                    }
                }
            update { copy(isVerifyingEmailCode = false) }
        }
    }

    fun checkNickname() {
        val nickname = _uiState.value.nickname
        viewModelScope.launch {
            authRepository.checkNicknameAvailability(nickname)
                .onSuccess { available ->
                    update {
                        copy(
                            isNicknameAvailable = available,
                            nicknameCheckMessage =
                                if (available) "사용가능한 닉네임입니다." else "이미 존재하는 닉네임입니다."
                        )
                    }
                }
                .onFailure { error ->
                    update {
                        copy(
                            isNicknameAvailable = false,
                            nicknameCheckMessage = error.toUserMessage("닉네임 확인에 실패했습니다.")
                        )
                    }
                }
        }
    }

    fun signUp() {
        val state = _uiState.value
        viewModelScope.launch {
            authRepository.signUp(state.email, state.password, state.nickname)
                .onSuccess {
                    update { copy(signUpErrorMessage = null) }
                    _events.send(SignUpEvent.SignedUp)
                }
                .onFailure { error ->
                    update { copy(signUpErrorMessage = error.toUserMessage("회원가입에 실패했습니다.")) }
                }
        }
    }
}
