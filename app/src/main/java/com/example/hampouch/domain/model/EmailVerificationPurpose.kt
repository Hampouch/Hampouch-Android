package com.example.hampouch.domain.model

/** 이메일 인증 코드를 어떤 용도로 발송/검증하는지. */
enum class EmailVerificationPurpose {
    SIGNUP,
    PASSWORD_RESET
}
