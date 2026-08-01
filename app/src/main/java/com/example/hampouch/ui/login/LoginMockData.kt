package com.example.hampouch.ui.login

import com.example.hampouch.data.model.User
import com.example.hampouch.data.model.UserRole

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

    val accounts = listOf(normalUser, editorUser)

    fun findAccount(email: String, password: String): User? =
        accounts.find { it.email == email && it.password == password }
}
