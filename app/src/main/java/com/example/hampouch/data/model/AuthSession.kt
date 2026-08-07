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

/**
 * [com.example.hampouch.data.repository.AuthRepository.loginWithSocial] 결과.
 * isNewUser == true면 서버에 닉네임이 아직 없는 신규 가입자이므로, 화면단에서 닉네임 입력 다이얼로그를 띄운 뒤
 * [com.example.hampouch.data.repository.AuthRepository.completeSocialSignUp]까지 마쳐야 로그인이 완료된다.
 * 이 경우 [session]은 아직 DataStore에 저장되지 않은 상태다.
 */
data class SocialLoginOutcome(
    val session: AuthSession,
    val isNewUser: Boolean
)
