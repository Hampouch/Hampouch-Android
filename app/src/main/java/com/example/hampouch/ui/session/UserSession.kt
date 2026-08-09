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
        // TODO: 서버팀 로그인 API 연동 시 토큰 기반 세션 저장/복원으로 교체
        context.applicationContext
            .getSharedPreferences(SESSION_PREFS_NAME, Context.MODE_PRIVATE)
            .edit()
            .putString(KEY_LOGGED_IN_USER_ID, user.id)
            .apply()
    }

    fun restore(context: Context): Boolean {
        val savedUserId = context.applicationContext
            .getSharedPreferences(SESSION_PREFS_NAME, Context.MODE_PRIVATE)
            .getString(KEY_LOGGED_IN_USER_ID, null) ?: return false
        val user = LoginMockData.accounts.find { it.id == savedUserId } ?: return false
        currentUserState.value = user
        isLoggedInState.value = true
        return true
    }

    fun logout(context: Context) {
        isLoggedInState.value = false
        currentUserState.value = LoginMockData.normalUser
        context.applicationContext
            .getSharedPreferences(SESSION_PREFS_NAME, Context.MODE_PRIVATE)
            .edit()
            .remove(KEY_LOGGED_IN_USER_ID)
            .apply()
    }

    // TODO: 서버팀 회원 탈퇴 API 연동 시 실제 계정 삭제 요청으로 교체. 현재는 목데이터 계정 목록에서
    fun withdraw(context: Context) {
        LoginMockData.removeAccount(currentUserState.value.id)
        logout(context)
    }
}
