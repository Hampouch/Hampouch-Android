package com.example.hampouch.domain.model

enum class AuthProvider {
    LOCAL,
    KAKAO,
    GOOGLE
}

data class SocialCredential(
    val provider: AuthProvider,
    val providerToken: String,
    val email: String?
)

data class AuthSession(
    val provider: AuthProvider,
    val userId: Long,
    val role: String,
    val status: String,
    val accessToken: String,
    val refreshToken: String,
    val tokenType: String,
    val nickname: String?,
    val email: String?,
    val profileImageUrl: String?
)

data class SocialLoginOutcome(
    val session: AuthSession,
    val isNewUser: Boolean,
    val requiresNickname: Boolean
)
