package com.example.hampouch.data.remote.dto

enum class EmailVerificationPurpose {
    SIGNUP,
    PASSWORD_RESET
}

data class EmailSendRequest(
    val email: String,
    val purpose: String
)

data class EmailSendData(
    val expiresInSeconds: Int
)

data class EmailVerifyRequest(
    val email: String,
    val code: String,
    val purpose: String
)

data class EmailVerifyData(
    val email: String,
    val purpose: String,
    val verified: Boolean
)
