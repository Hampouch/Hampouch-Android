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
    /** 닉네임 최초 설정이 필요한지. 신규 가입뿐 아니라 닉네임 설정 전에 이탈한 계정도 true로 내려온다. */
    val needsNickname: Boolean,
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

data class SetNicknameRequest(
    val nickname: String
)

data class SetNicknameData(
    val userId: Long,
    val nickname: String
)

data class ApiErrorBody(
    val code: String,
    val message: String,
    val status: Int,
    val fieldErrors: Map<String, String>? = null
)
