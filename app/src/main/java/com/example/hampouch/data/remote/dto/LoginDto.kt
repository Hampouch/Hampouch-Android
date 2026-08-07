package com.example.hampouch.data.remote.dto

data class LoginRequest(
    val email: String,
    val password: String
)

data class LoginData(
    val accessToken: String,
    val refreshToken: String,
    val tokenType: String,
    val accessTokenExpiresInMs: Long,
    val refreshTokenExpiresInMs: Long,
    val user: AuthUser
)
