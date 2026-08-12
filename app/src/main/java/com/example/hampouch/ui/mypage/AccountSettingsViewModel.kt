package com.example.hampouch.ui.mypage

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.hampouch.data.repository.AuthRepository
import com.example.hampouch.domain.model.toUserMessage
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

sealed interface AccountSettingsEvent {
    /** 로그아웃 또는 탈퇴가 끝나 세션이 사라짐 — 로그인/온보딩으로 보낸다. */
    data object LoggedOut : AccountSettingsEvent

    data class ShowMessage(val message: String) : AccountSettingsEvent
}

@HiltViewModel
class AccountSettingsViewModel @Inject constructor(
    private val authRepository: AuthRepository
) : ViewModel() {

    private val _events = Channel<AccountSettingsEvent>(Channel.BUFFERED)
    val events = _events.receiveAsFlow()

    /** 탈퇴는 서버에서 계정 삭제가 성공했을 때만 세션을 지운다(실패 시 로그인 유지). */
    fun withdraw() {
        viewModelScope.launch {
            authRepository.withdraw()
                .onSuccess { _events.send(AccountSettingsEvent.LoggedOut) }
                .onFailure { error ->
                    _events.send(
                        AccountSettingsEvent.ShowMessage(error.toUserMessage("회원 탈퇴에 실패했습니다."))
                    )
                }
        }
    }

    fun logout() {
        viewModelScope.launch {
            authRepository.logout()
                .onFailure { error ->
                    _events.send(
                        AccountSettingsEvent.ShowMessage(error.toUserMessage("로그아웃에 실패했습니다."))
                    )
                }
            // 로그아웃은 서버 호출이 실패해도 로컬 세션을 지우므로 항상 나간다.
            _events.send(AccountSettingsEvent.LoggedOut)
        }
    }
}
