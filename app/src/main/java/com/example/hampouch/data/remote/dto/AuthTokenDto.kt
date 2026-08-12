package com.example.hampouch.data.remote.dto

data class RefreshTokenRequest(
    val refreshToken: String
)

data class RefreshTokenData(
    val accessToken: String,
    val refreshToken: String,
    val tokenType: String,
    val accessTokenExpiresInMs: Long,
    val refreshTokenExpiresInMs: Long
)

data class LogoutRequest(
    val refreshToken: String
)
