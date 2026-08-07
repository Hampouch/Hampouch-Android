package com.example.hampouch.data.remote.dto

data class SocialLoginRequest(
    val provider: String,
    val providerToken: String
)

data class ApiResponse<T>(
    val code: String,
    val message: String,
    val data: T?
)

data class SocialLoginData(
    val isNewUser: Boolean,
    val accessToken: String,
    val refreshToken: String,
    val tokenType: String,
    val accessTokenExpiresInMs: Long,
    val refreshTokenExpiresInMs: Long,
    val user: AuthUser
)

data class AuthUser(
    val userId: Long,
    val role: String,
    val status: String
)

/**
 * 소셜 회원가입 온보딩 마지막 단계(닉네임 최초 설정, PATCH /api/auth/nickname)용 요청/응답.
 * accessToken으로 인증된 사용자 본인의 닉네임만 설정할 수 있다.
 */
data class SetNicknameRequest(
    val nickname: String
)

data class SetNicknameData(
    val userId: Long,
    val nickname: String
)

/**
 * 4xx 등 에러 응답 바디. 성공 응답([ApiResponse])과 달리 data 대신 status/fieldErrors를 담는다.
 */
data class ApiErrorBody(
    val code: String,
    val message: String,
    val status: Int,
    val fieldErrors: Map<String, String>? = null
)
