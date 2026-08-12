package com.example.hampouch.data.repository

import com.example.hampouch.domain.model.MyPageProfile
import com.example.hampouch.domain.model.User
import com.example.hampouch.domain.repository.AccountScopedState
import com.example.hampouch.domain.repository.MyPageProfileRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class MyPageProfileRepositoryImpl @Inject constructor() :
    MyPageProfileRepository, AccountScopedState {

    private val _profile = MutableStateFlow<MyPageProfile?>(null)
    override val profile: StateFlow<MyPageProfile?> = _profile.asStateFlow()

    override fun defaultProfileFor(user: User): MyPageProfile = MyPageProfile(
        name = user.name,
        handle = user.email,
        email = user.email
    )

    override fun update(user: User, name: String, avatarUri: String?) {
        _profile.value = (_profile.value ?: defaultProfileFor(user))
            .copy(name = name, avatarUri = avatarUri)
    }

    override fun resetForAccount() {
        _profile.value = null
    }
}
