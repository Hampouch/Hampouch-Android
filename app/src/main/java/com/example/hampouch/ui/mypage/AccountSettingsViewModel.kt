package com.example.hampouch.ui.mypage

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.hampouch.data.repository.AuthRepository
import com.example.hampouch.domain.model.toUserMessage
import com.example.hampouch.domain.repository.UsersRepository
import com.example.hampouch.ui.widget.HomeWidgetStatePublisher
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

sealed interface AccountSettingsEvent {
    data object LoggedOut : AccountSettingsEvent

    data class ShowMessage(val message: String) : AccountSettingsEvent
}

data class AccountSettingsActions(
    val onBackClick: () -> Unit,
    val onProfileEditClick: () -> Unit,
    val onChangePasswordClick: () -> Unit,
    val onNotificationClick: () -> Unit,
    val onWithdraw: () -> Unit
)

@HiltViewModel
class AccountSettingsViewModel @Inject constructor(
    private val authRepository: AuthRepository,
    private val homeWidgetStatePublisher: HomeWidgetStatePublisher
) : ViewModel() {

    private val _events = Channel<AccountSettingsEvent>(Channel.BUFFERED)
    val events = _events.receiveAsFlow()

    fun withdraw() {
        viewModelScope.launch {
            authRepository.withdraw()
                .onSuccess {
                    homeWidgetStatePublisher.publishLoggedOut()
                    _events.send(AccountSettingsEvent.LoggedOut)
                }
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
            homeWidgetStatePublisher.publishLoggedOut()
            _events.send(AccountSettingsEvent.LoggedOut)
        }
    }
}

@HiltViewModel
class ChangePasswordViewModel @Inject constructor(
    private val usersRepository: UsersRepository
) : ViewModel() {

    suspend fun changePassword(currentPassword: String, newPassword: String): Result<Unit> =
        usersRepository.changePassword(currentPassword, newPassword)
}
