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
    val role: UserRole = UserRole.NORMAL,
    // 목데이터 모드 전용: 신규 회원가입 계정은 false로 시작해 첫 로그인 때 온보딩 값을 받고, 그 이후 true(기존
    // 회원)로 바뀐다([LoginMockData.markAsExistingMember]). 고정 데모 계정(민준/에디터)은 항상 true.
    val isExistingMember: Boolean = true
)
