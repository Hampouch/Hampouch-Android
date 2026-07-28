package com.example.hampouch.data.model

enum class AuthProvider {
    LOCAL,
    KAKAO,
    GOOGLE
}

/**
 * SDK에서 발급받은 소셜 인증 정보. 서버 검증(/api/auth/social) 전 단계의 값이다.
 */
data class SocialCredential(
    val provider: AuthProvider,
    val providerToken: String,
    val nickname: String?,
    val email: String?,
    val profileImageUrl: String?
)

/**
 * 서버의 소셜 로그인 검증까지 완료된 뒤 저장되는 로그인 세션.
 */
data class UserSession(
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
