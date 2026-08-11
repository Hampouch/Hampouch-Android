package com.example.hampouch.data.local

import com.example.hampouch.domain.model.User
import com.example.hampouch.domain.model.UserRole

/**
 * 목데이터 모드의 인메모리 계정 목록.
 *
 * TODO: 서버팀 회원가입/로그인 API 연동이 끝나면 통째로 삭제.
 */
object AccountMockDataSource {

    val normalUser = User(
        id = "user_me",
        name = "절약왕민준",
        email = "user@hampouch.com",
        password = "user1234",
        role = UserRole.NORMAL,
        isExistingMember = true
    )

    val editorUser = User(
        id = "editor_pochi",
        name = "햄포치 에디터",
        email = "editor@hampouch.com",
        password = "editor1234",
        role = UserRole.EDITOR,
        isExistingMember = true
    )

    private val registeredAccounts = mutableListOf(normalUser, editorUser)

    val accounts: List<User> get() = registeredAccounts

    private fun normalize(email: String): String = email.trim().lowercase()

    fun isRegistered(email: String): Boolean =
        accounts.any { normalize(it.email) == normalize(email) }

    fun findAccount(email: String, password: String): User? =
        accounts.find { normalize(it.email) == normalize(email) && it.password == password }

    fun register(email: String, password: String, nickname: String): User {
        val newUser = User(
            id = "user_${System.currentTimeMillis()}",
            name = nickname,
            email = email,
            password = password,
            role = UserRole.NORMAL,
            isExistingMember = false
        )
        registeredAccounts.removeAll { normalize(it.email) == normalize(email) }
        registeredAccounts.add(newUser)
        return newUser
    }

    fun markAsExistingMember(email: String) {
        val index = registeredAccounts.indexOfFirst { normalize(it.email) == normalize(email) }
        if (index == -1) return
        registeredAccounts[index] = registeredAccounts[index].copy(isExistingMember = true)
    }

    fun removeAccount(userId: String) {
        registeredAccounts.removeAll { it.id == userId }
    }

    fun updatePassword(email: String, newPassword: String): Boolean {
        val index = registeredAccounts.indexOfFirst { normalize(it.email) == normalize(email) }
        if (index == -1) return false
        registeredAccounts[index] = registeredAccounts[index].copy(password = newPassword)
        return true
    }
}
