package com.example.hampouch.data.model

enum class AuthProvider {
    LOCAL,
    KAKAO,
    GOOGLE
}

/**
 * 소셜 SDK에서 받아오는 정보.
 *
 * 닉네임은 가입 시 사용자에게 직접 입력받고 프로필 사진은 마이페이지에서 설정하므로,
 * 소셜 계정에서는 서버 인증용 토큰과 이메일만 가져온다.
 */
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
    /**
     * 닉네임 입력 단계를 거쳐야 하는지.
     *
     * 신규 가입(isNewUser)뿐 아니라, 이전에 소셜 로그인으로 계정만 만들어두고 닉네임 입력을
     * 마치지 않은 경우도 포함한다. 이때 서버는 isNewUser=false로 응답하므로 /me의
     * needsNickname으로 판별한다.
     */
    val requiresNickname: Boolean
)
