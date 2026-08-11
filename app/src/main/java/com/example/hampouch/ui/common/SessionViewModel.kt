package com.example.hampouch.ui.common

import androidx.lifecycle.ViewModel
import com.example.hampouch.data.repository.AuthRepository
import com.example.hampouch.domain.model.User
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.StateFlow
import javax.inject.Inject

/**
 * 현재 로그인 사용자를 읽는 화면들이 쓰는 얇은 ViewModel.
 *
 * 예전에는 `ui/session/UserSession` 전역 object가 Compose 상태로 들고 있었는데,
 * 세션의 실제 주인은 인증 도메인이라 그쪽으로 옮기고 화면은 이 ViewModel로 구독한다.
 */
@HiltViewModel
class SessionViewModel @Inject constructor(
    authRepository: AuthRepository
) : ViewModel() {

    val currentUser: StateFlow<User> = authRepository.currentUser
}
