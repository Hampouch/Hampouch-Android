package com.example.hampouch.ui.session

import android.content.Context
import androidx.compose.runtime.mutableStateOf
import com.example.hampouch.data.model.User
import com.example.hampouch.data.model.UserRole
import com.example.hampouch.ui.login.LoginMockData

private const val SESSION_PREFS_NAME = "hampouch_session"
private const val KEY_LOGGED_IN_USER_ID = "logged_in_user_id"

object UserSession {
    private val currentUserState = mutableStateOf(LoginMockData.normalUser)
    private val isLoggedInState = mutableStateOf(false)

    val currentUser: User get() = currentUserState.value

    val isLoggedIn: Boolean get() = isLoggedInState.value

    val isEditor: Boolean get() = currentUserState.value.role == UserRole.EDITOR

    fun login(context: Context, user: User) {
        currentUserState.value = user
        isLoggedInState.value = true
        // 목데이터 계정 id만 로컬에 저장해 로그인 상태를 유지한다.
        // TODO: 서버팀 로그인 API 연동 시 토큰 기반 세션 저장/복원으로 교체
        context.applicationContext
            .getSharedPreferences(SESSION_PREFS_NAME, Context.MODE_PRIVATE)
            .edit()
            .putString(KEY_LOGGED_IN_USER_ID, user.id)
            .apply()
    }

    // 앱 재실행 시 저장된 목데이터 계정 id로 로그인 상태를 복원한다. 복원에 성공하면 true.
    fun restore(context: Context): Boolean {
        val savedUserId = context.applicationContext
            .getSharedPreferences(SESSION_PREFS_NAME, Context.MODE_PRIVATE)
            .getString(KEY_LOGGED_IN_USER_ID, null) ?: return false
        val user = LoginMockData.accounts.find { it.id == savedUserId } ?: return false
        currentUserState.value = user
        isLoggedInState.value = true
        return true
    }
}
