package com.example.hampouch.data.model

enum class AuthProvider {
    KAKAO,
    GOOGLE
}

data class UserSession(
    val provider: AuthProvider,
    val token: String,
    val nickname: String?,
    val email: String?,
    val profileImageUrl: String?
)
