package com.example.hampouch.ui.common

import androidx.lifecycle.ViewModel
import com.example.hampouch.data.repository.AuthRepository
import com.example.hampouch.domain.model.User
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.StateFlow
import javax.inject.Inject

@HiltViewModel
class SessionViewModel @Inject constructor(
    authRepository: AuthRepository
) : ViewModel() {

    val currentUser: StateFlow<User> = authRepository.currentUser
}
