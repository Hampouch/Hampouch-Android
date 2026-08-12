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
    data object LoggedOut : AccountSettingsEvent

    data class ShowMessage(val message: String) : AccountSettingsEvent
}

@HiltViewModel
class AccountSettingsViewModel @Inject constructor(
    private val authRepository: AuthRepository
) : ViewModel() {

    private val _events = Channel<AccountSettingsEvent>(Channel.BUFFERED)
    val events = _events.receiveAsFlow()

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
            _events.send(AccountSettingsEvent.LoggedOut)
        }
    }
}
