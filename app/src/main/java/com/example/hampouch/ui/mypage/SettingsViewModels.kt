package com.example.hampouch.ui.mypage

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.hampouch.domain.model.MyPageProfile
import com.example.hampouch.domain.model.NotificationSettingsState
import com.example.hampouch.domain.model.RecordAlarmSettingsState
import com.example.hampouch.domain.model.User
import com.example.hampouch.domain.model.toUserMessage
import com.example.hampouch.data.repository.AuthRepository
import com.example.hampouch.domain.repository.MyPageProfileRepository
import com.example.hampouch.domain.repository.BattleRepository
import com.example.hampouch.domain.repository.NotificationSettingsRepository
import com.example.hampouch.domain.repository.RecordAlarmRepository
import com.example.hampouch.domain.repository.UsersRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch
import java.time.LocalDate
import javax.inject.Inject

@HiltViewModel
class AllSettingsViewModel @Inject constructor(
    private val notificationSettingsRepository: NotificationSettingsRepository,
    private val usersRepository: UsersRepository
) : ViewModel() {

    val state: StateFlow<NotificationSettingsState> = notificationSettingsRepository.state

    init {
        viewModelScope.launch { usersRepository.fetchNotificationSchedule() }
    }

    fun setChallengeAlarmEnabled(enabled: Boolean) {
        notificationSettingsRepository.update { it.copy(challengeAlarmEnabled = enabled) }
        syncSchedule()
    }

    fun setHamBattleAlarmEnabled(enabled: Boolean) {
        notificationSettingsRepository.update { it.copy(hamBattleAlarmEnabled = enabled) }
        syncSchedule()
    }

    fun setCommunityAlarmEnabled(enabled: Boolean) {
        notificationSettingsRepository.update { it.copy(communityAlarmEnabled = enabled) }
        syncSchedule()
    }

    private fun syncSchedule() {
        viewModelScope.launch { usersRepository.updateNotificationSchedule() }
    }
}

@HiltViewModel
class RecordAlarmViewModel @Inject constructor(
    private val recordAlarmRepository: RecordAlarmRepository,
    private val usersRepository: UsersRepository
) : ViewModel() {

    val state: StateFlow<RecordAlarmSettingsState> = recordAlarmRepository.state
    val dismissedDate: StateFlow<LocalDate?> = recordAlarmRepository.dismissedDate

    init {
        viewModelScope.launch { usersRepository.fetchNotificationSchedule() }
    }

    fun update(transform: (RecordAlarmSettingsState) -> RecordAlarmSettingsState) {
        recordAlarmRepository.update(transform)
        viewModelScope.launch { usersRepository.updateNotificationSchedule() }
    }

    fun dismissForToday(referenceToday: LocalDate) {
        recordAlarmRepository.dismissForToday(referenceToday)
    }
}

sealed interface MyPageProfileEvent {
    data class ShowMessage(val message: String) : MyPageProfileEvent
}

@HiltViewModel
class MyPageProfileViewModel @Inject constructor(
    private val myPageProfileRepository: MyPageProfileRepository,
    private val authRepository: AuthRepository,
    private val usersRepository: UsersRepository,
    private val battleRepository: BattleRepository
) : ViewModel() {

    val profile: StateFlow<MyPageProfile?> = myPageProfileRepository.profile

    private val _events = Channel<MyPageProfileEvent>(Channel.BUFFERED)
    val events = _events.receiveAsFlow()

    init {
        viewModelScope.launch { usersRepository.fetchProfile() }
    }

    fun profileFor(user: User, stored: MyPageProfile?): MyPageProfile =
        stored ?: myPageProfileRepository.defaultProfileFor(user)

    fun update(user: User, name: String, avatarUri: String?) {
        val previous = myPageProfileRepository.profile.value ?: myPageProfileRepository.defaultProfileFor(user)
        myPageProfileRepository.update(user, name, avatarUri)
        viewModelScope.launch {
            if (name != previous.name) {
                usersRepository.updateNickname(name).onFailure { error ->
                    _events.send(MyPageProfileEvent.ShowMessage(error.toUserMessage("닉네임 변경에 실패했습니다.")))
                }
            }
            when {
                avatarUri != null && avatarUri != previous.avatarUri ->
                    usersRepository.uploadProfilePhoto(avatarUri)
                        .onSuccess {
                            battleRepository.updateMyAvatar(myPageProfileRepository.profile.value?.avatarUri)
                        }
                        .onFailure { error ->
                            _events.send(MyPageProfileEvent.ShowMessage(error.toUserMessage("프로필 사진 변경에 실패했습니다.")))
                        }
                avatarUri == null && previous.avatarUri != null ->
                    usersRepository.resetProfilePhoto()
                        .onSuccess { battleRepository.updateMyAvatar(null) }
                        .onFailure { error ->
                            _events.send(MyPageProfileEvent.ShowMessage(error.toUserMessage("프로필 사진 변경에 실패했습니다.")))
                        }
            }
        }
    }

    /** 서버에 닉네임 사용 가능 여부를 묻는다. 성공하면 "이미 쓰이는 중인가"를 돌려준다. */
    suspend fun isNicknameTaken(nickname: String): Result<Boolean> =
        authRepository.checkNicknameAvailability(nickname).map { !it }
}
