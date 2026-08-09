package com.example.hampouch.data.remote.dto

data class AuthMeData(
    val userId: Long,
    val nickname: String,
    val needsNickname: Boolean,
    val role: String,
    val status: String
)
