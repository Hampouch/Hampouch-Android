package com.example.hampouch.domain.model

enum class EmailVerificationPurpose {
    SIGNUP,
    PASSWORD_RESET
}

data class EmailSendResult(
    val expiresInSeconds: Long,
    val message: String
)

data class EmailVerifyResult(
    val message: String
)
