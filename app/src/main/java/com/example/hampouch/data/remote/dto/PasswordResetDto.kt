package com.example.hampouch.data.remote.dto

data class PasswordResetRequest(
    val email: String,
    val newPassword: String
)
