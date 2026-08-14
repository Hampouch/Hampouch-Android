package com.example.hampouch.ui.mypage

import androidx.lifecycle.ViewModel
import com.example.hampouch.domain.model.MyPageProfile
import com.example.hampouch.domain.model.NotificationSettingsState
import com.example.hampouch.domain.model.RecordAlarmSettingsState
import com.example.hampouch.domain.model.User
import com.example.hampouch.data.repository.AuthRepository
import com.example.hampouch.domain.repository.MyPageProfileRepository
import com.example.hampouch.domain.repository.NotificationSettingsRepository
import com.example.hampouch.domain.repository.RecordAlarmRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.StateFlow
import java.time.LocalDate
import javax.inject.Inject

@HiltViewModel
class AllSettingsViewModel @Inject constructor(
    private val notificationSettingsRepository: NotificationSettingsRepository
) : ViewModel() {

    val state: StateFlow<NotificationSettingsState> = notificationSettingsRepository.state

    fun setChallengeAlarmEnabled(enabled: Boolean) {
        notificationSettingsRepository.update { it.copy(challengeAlarmEnabled = enabled) }
    }

    fun setHamBattleAlarmEnabled(enabled: Boolean) {
        notificationSettingsRepository.update { it.copy(hamBattleAlarmEnabled = enabled) }
    }

    fun setCommunityAlarmEnabled(enabled: Boolean) {
        notificationSettingsRepository.update { it.copy(communityAlarmEnabled = enabled) }
    }
}

@HiltViewModel
class RecordAlarmViewModel @Inject constructor(
    private val recordAlarmRepository: RecordAlarmRepository
) : ViewModel() {

    val state: StateFlow<RecordAlarmSettingsState> = recordAlarmRepository.state
    val dismissedDate: StateFlow<LocalDate?> = recordAlarmRepository.dismissedDate

    fun update(transform: (RecordAlarmSettingsState) -> RecordAlarmSettingsState) {
        recordAlarmRepository.update(transform)
    }

    fun dismissForToday(referenceToday: LocalDate) {
        recordAlarmRepository.dismissForToday(referenceToday)
    }
}

@HiltViewModel
class MyPageProfileViewModel @Inject constructor(
    private val myPageProfileRepository: MyPageProfileRepository,
    private val authRepository: AuthRepository
) : ViewModel() {

    val profile: StateFlow<MyPageProfile?> = myPageProfileRepository.profile

    fun profileFor(user: User, stored: MyPageProfile?): MyPageProfile =
        stored ?: myPageProfileRepository.defaultProfileFor(user)

    fun update(user: User, name: String, avatarUri: String?) {
        myPageProfileRepository.update(user, name, avatarUri)
    }

    /** 서버에 닉네임 사용 가능 여부를 묻는다. 성공하면 "이미 쓰이는 중인가"를 돌려준다. */
    suspend fun isNicknameTaken(nickname: String): Result<Boolean> =
        authRepository.checkNicknameAvailability(nickname).map { !it }
}
