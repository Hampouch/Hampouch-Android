package com.example.hampouch.domain.model

enum class UserRole {
    NORMAL,
    EDITOR
}

data class User(
    val id: String,
    val name: String,
    val email: String,
    val password: String,
    val role: UserRole = UserRole.NORMAL,
    val isExistingMember: Boolean = true
)
