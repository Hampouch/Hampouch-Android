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

    private val registeredAccounts = mutableStateListOf(normalUser, editorUser)

    val accounts: List<User> get() = registeredAccounts

    /**
     * 이메일 대소문자·앞뒤 공백 차이로 같은 계정을 다른 이메일로 오인하지 않도록 비교 전에 정규화한다.
     * 텍스트 필드를 지웠다가 다시 입력하는 등의 편집 후에도 중복/존재 여부 검사가 우회되지 않게 하기 위함.
     */
    private fun normalize(email: String): String = email.trim().lowercase()

    fun isRegistered(email: String): Boolean =
        accounts.any { normalize(it.email) == normalize(email) }

    fun findAccount(email: String, password: String): User? =
        accounts.find { normalize(it.email) == normalize(email) && it.password == password }

    /**
     * 목데이터 회원가입([AuthRepository.signUp], [AuthRepository.completeSocialSignUp])이 성공하면 호출해,
     * 곧바로 같은 세션 내에서 로그인 화면으로 돌아가 로그인할 수 있도록 계정을 등록한다.
     * 이미 같은 이메일로 등록된 계정이 있으면 덮어쓴다(예: 소셜 회원가입 재테스트).
     * 신규 계정이라 [User.isExistingMember]는 false로 시작한다.
     */
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

    /**
     * 신규 계정이 로그인을 마쳐 온보딩 값(또는 건너뛰기)을 반영했으면 호출한다. 그 이후로는 기존 회원과
     * 동일하게 취급되어, 다시 로그인해도 온보딩 값이 재적용되지 않고 항상 원래 목데이터를 보여준다.
     */
    fun markAsExistingMember(email: String) {
        val index = registeredAccounts.indexOfFirst { normalize(it.email) == normalize(email) }
        if (index == -1) return
        registeredAccounts[index] = registeredAccounts[index].copy(isExistingMember = true)
    }

    /**
     * 회원탈퇴 시 호출한다. 계정을 목록에서 제거해 같은 이메일/비밀번호로는 다시 로그인할 수 없게 한다.
     * (앱 프로세스를 새로 시작하면 데모 계정 목록이 초기 상태로 되돌아간다.)
     */
    fun removeAccount(userId: String) {
        registeredAccounts.removeAll { it.id == userId }
    }

    /**
     * 목데이터 비밀번호 재설정([AuthRepository.resetPassword])이 성공하면 호출해, 저장된 비밀번호를
     * 실제로 갱신한다. 대상 계정이 없으면 아무 것도 하지 않고 false를 반환한다.
     */
    fun updatePassword(email: String, newPassword: String): Boolean {
        val index = registeredAccounts.indexOfFirst { normalize(it.email) == normalize(email) }
        if (index == -1) return false
        registeredAccounts[index] = registeredAccounts[index].copy(password = newPassword)
        return true
    }
}
