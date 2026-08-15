package com.example.hampouch.ui.signup

import com.example.hampouch.domain.model.ApiException
import com.example.hampouch.domain.model.toUserMessage

private val EMAIL_VERIFICATION_RESTART_CODES = setOf(
    "AUTH_EMAIL_VERIFICATION_NOT_FOUND",
    "AUTH_EMAIL_CODE_EXPIRED",
    "AUTH_EMAIL_CODE_ATTEMPT_EXCEEDED"
)

private fun Throwable.requiresEmailVerificationRestart(): Boolean =
    (this as? ApiException)?.code in EMAIL_VERIFICATION_RESTART_CODES

internal fun SignUpUiState.emailVerificationFailed(error: Throwable): SignUpUiState {
    val shouldRestart = error.requiresEmailVerificationRestart()
    return copy(
        emailCode = if (shouldRestart) "" else emailCode,
        isEmailVerified = false,
        hasSentEmailCode = if (shouldRestart) false else hasSentEmailCode,
        emailCodeExpiresAtMillis = if (shouldRestart) null else emailCodeExpiresAtMillis,
        emailVerifyMessage = error.toUserMessage("인증번호를 다시 확인해주세요.")
    )
}

internal fun ResetPasswordUiState.emailVerificationFailed(error: Throwable): ResetPasswordUiState {
    val shouldRestart = error.requiresEmailVerificationRestart()
    return copy(
        emailCode = if (shouldRestart) "" else emailCode,
        isEmailVerified = false,
        hasSentEmailCode = if (shouldRestart) false else hasSentEmailCode,
        emailCodeExpiresAtMillis = if (shouldRestart) null else emailCodeExpiresAtMillis,
        emailVerifyMessage = error.toUserMessage("인증번호를 다시 확인해주세요.")
    )
}
