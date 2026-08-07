package com.example.hampouch.data.remote.dto

data class SignUpRequest(
    val email: String,
    val password: String,
    val nickname: String
)

data class SignUpData(
    val userId: Long,
    val email: String,
    val nickname: String,
    val provider: String
)
