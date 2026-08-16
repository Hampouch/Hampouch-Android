package com.example.hampouch.domain.model

enum class EmailVerificationErrorCode(val serverCode: String) {
    VERIFICATION_NOT_FOUND("AUTH_EMAIL_VERIFICATION_NOT_FOUND"),
    CODE_EXPIRED("AUTH_EMAIL_CODE_EXPIRED"),
    ATTEMPT_EXCEEDED("AUTH_EMAIL_CODE_ATTEMPT_EXCEEDED"),
    CODE_MISMATCH("AUTH_EMAIL_CODE_MISMATCH");

    companion object {
        fun from(serverCode: String?): EmailVerificationErrorCode? =
            entries.find { it.serverCode == serverCode }
    }
}

sealed interface EmailVerificationFailure {
    val message: String

    data class RestartRequired(override val message: String, val code: EmailVerificationErrorCode) :
        EmailVerificationFailure

    data class RetainInput(override val message: String) : EmailVerificationFailure
}

fun Throwable.toEmailVerificationFailure(fallback: String): EmailVerificationFailure {
    val message = toUserMessage(fallback)
    val code = EmailVerificationErrorCode.from((this as? ApiException)?.code)
    return if (code != null && code != EmailVerificationErrorCode.CODE_MISMATCH) {
        EmailVerificationFailure.RestartRequired(message, code)
    } else {
        EmailVerificationFailure.RetainInput(message)
    }
}
