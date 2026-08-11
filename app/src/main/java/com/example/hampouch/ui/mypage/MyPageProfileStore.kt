package com.example.hampouch.ui.mypage

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import com.example.hampouch.domain.model.MyPageProfile

object MyPageProfileStore {

    private var profileState: MyPageProfile? by mutableStateOf(null)

    val profile: MyPageProfile
        get() = profileState ?: MyPageMockData.defaultProfile().also { profileState = it }

    fun update(name: String, avatarUri: String?) {
        profileState = profile.copy(name = name, avatarUri = avatarUri)
    }

    fun resetForAccount() {
        profileState = null
    }
}
