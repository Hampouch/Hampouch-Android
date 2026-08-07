package com.example.hampouch.ui.login

import androidx.compose.runtime.mutableStateListOf
import com.example.hampouch.data.model.User
import com.example.hampouch.data.model.UserRole

// TODO: 서버팀 회원가입/로그인 API 연동 시 이 인메모리 계정 목록 대신 서버 응답으로 교체.
object LoginMockData {

    val normalUser = User(
        id = "user_me",
        name = "절약왕민준",
        email = "user@hampouch.com",
        password = "user1234",
        role = UserRole.NORMAL
    )

    val editorUser = User(
        id = "editor_pochi",
        name = "햄포치 에디터",
        email = "editor@hampouch.com",
        password = "editor1234",
        role = UserRole.EDITOR
    )

    private val registeredAccounts = mutableStateListOf(normalUser, editorUser)

    val accounts: List<User> get() = registeredAccounts

    fun findAccount(email: String, password: String): User? =
        accounts.find { it.email == email && it.password == password }

    /**
     * 목데이터 회원가입([AuthRepository.signUp], [AuthRepository.completeSocialSignUp])이 성공하면 호출해,
     * 곧바로 같은 세션 내에서 로그인 화면으로 돌아가 로그인할 수 있도록 계정을 등록한다.
     * 이미 같은 이메일로 등록된 계정이 있으면 덮어쓴다(예: 소셜 회원가입 재테스트).
     */
    fun register(email: String, password: String, nickname: String): User {
        val newUser = User(
            id = "user_${System.currentTimeMillis()}",
            name = nickname,
            email = email,
            password = password,
            role = UserRole.NORMAL
        )
        registeredAccounts.removeAll { it.email == email }
        registeredAccounts.add(newUser)
        return newUser
    }
}
