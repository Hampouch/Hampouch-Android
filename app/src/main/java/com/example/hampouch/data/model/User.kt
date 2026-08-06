package com.example.hampouch.data.model

enum class UserRole {
    NORMAL,
    EDITOR
}

data class User(
    val id: String,
    val name: String,
    val email: String,
    val password: String,
    val role: UserRole = UserRole.NORMAL
)
