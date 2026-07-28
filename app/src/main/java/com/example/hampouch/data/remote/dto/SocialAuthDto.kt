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
 * 4xx 등 에러 응답 바디. 성공 응답([ApiResponse])과 달리 data 대신 status/fieldErrors를 담는다.
 */
data class ApiErrorBody(
    val code: String,
    val message: String,
    val status: Int,
    val fieldErrors: Map<String, String>? = null
)
