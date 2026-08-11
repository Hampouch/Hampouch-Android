package com.example.hampouch.ui.mypage

import com.example.hampouch.domain.repository.AccountScopedState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import com.example.hampouch.domain.model.MyPageProfile
import com.example.hampouch.domain.model.User

object MyPageProfileStore : AccountScopedState {

    private var profileState: MyPageProfile? by mutableStateOf(null)

    /** 아직 편집한 적이 없으면 [user] 정보로 기본 프로필을 만든다. */
    fun profileFor(user: User): MyPageProfile =
        profileState ?: MyPageMockData.defaultProfile(user).also { profileState = it }

    /** 이미 만들어진 프로필. 없으면 null — 화면은 [profileFor]를 쓴다. */
    val profileOrNull: MyPageProfile? get() = profileState

    fun update(user: User, name: String, avatarUri: String?) {
        profileState = profileFor(user).copy(name = name, avatarUri = avatarUri)
    }

    override fun resetForAccount() {
        profileState = null
    }
}
