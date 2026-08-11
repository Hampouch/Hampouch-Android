package com.example.hampouch.ui.mypage

import com.example.hampouch.domain.repository.AccountScopedState
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import com.example.hampouch.domain.model.NotificationSettingsState

// TODO: 서버팀 알림 설정 API 연동 시 이 목데이터 대신 서버 응답으로 state를 채우도록 교체.
object AllSettingsStore : AccountScopedState {

    val stateHolder: MutableState<NotificationSettingsState> = mutableStateOf(NotificationSettingsState())

    var state: NotificationSettingsState
        get() = stateHolder.value
        set(value) { stateHolder.value = value }

    override fun resetForAccount() {
        state = NotificationSettingsState()
    }
}
