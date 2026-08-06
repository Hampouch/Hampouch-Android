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
 * 로그인(서버 또는 목데이터) 완료 후 저장되는 인증 세션.
 * 화면 전역에서 "지금 로그인되어 있는지"를 판단하는 데 쓰인다([com.example.hampouch.data.repository.AuthRepository.userSession]).
 *
 * 목데이터 모드([com.example.hampouch.core.config.AuthConfig.USE_SERVER_AUTH] == false)에서는
 * accessToken/refreshToken 등이 실제 서버 토큰이 아닌 더미 값으로 채워진다.
 */
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
