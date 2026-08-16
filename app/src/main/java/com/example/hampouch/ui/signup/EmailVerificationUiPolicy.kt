package com.example.hampouch.ui.signup

import com.example.hampouch.domain.model.EmailVerificationFailure
import com.example.hampouch.domain.model.toEmailVerificationFailure

internal fun SignUpUiState.emailVerificationFailed(error: Throwable): SignUpUiState {
    val failure = error.toEmailVerificationFailure("인증번호를 다시 확인해주세요.")
    val shouldRestart = failure is EmailVerificationFailure.RestartRequired
    return copy(
        emailCode = if (shouldRestart) "" else emailCode,
        isEmailVerified = false,
        hasSentEmailCode = if (shouldRestart) false else hasSentEmailCode,
        emailCodeExpiresAtMillis = if (shouldRestart) null else emailCodeExpiresAtMillis,
        emailVerifyMessage = failure.message
    )
}

internal fun ResetPasswordUiState.emailVerificationFailed(error: Throwable): ResetPasswordUiState {
    val failure = error.toEmailVerificationFailure("인증번호를 다시 확인해주세요.")
    val shouldRestart = failure is EmailVerificationFailure.RestartRequired
    return copy(
        emailCode = if (shouldRestart) "" else emailCode,
        isEmailVerified = false,
        hasSentEmailCode = if (shouldRestart) false else hasSentEmailCode,
        emailCodeExpiresAtMillis = if (shouldRestart) null else emailCodeExpiresAtMillis,
        emailVerifyMessage = failure.message
    )
}
