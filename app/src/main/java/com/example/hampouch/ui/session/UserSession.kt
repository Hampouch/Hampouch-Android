package com.example.hampouch.ui.session

import androidx.compose.runtime.mutableStateOf
import com.example.hampouch.data.model.User
import com.example.hampouch.data.model.UserRole
import com.example.hampouch.ui.login.LoginMockData

object UserSession {
    private val currentUserState = mutableStateOf(LoginMockData.normalUser)

    val currentUser: User get() = currentUserState.value

    val isEditor: Boolean get() = currentUserState.value.role == UserRole.EDITOR

    fun login(user: User) {
        currentUserState.value = user
    }
}
