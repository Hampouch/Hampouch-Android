package com.example.hampouch.data.repository

import com.example.hampouch.domain.model.NotificationSettingsState
import com.example.hampouch.domain.repository.AccountScopedState
import com.example.hampouch.domain.repository.NotificationSettingsRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import javax.inject.Inject
import javax.inject.Singleton

// TODO: 서버팀 알림 설정 API 연동 시 로컬 상태 대신 서버 응답으로 채우도록 교체.
@Singleton
class NotificationSettingsRepositoryImpl @Inject constructor() :
    NotificationSettingsRepository, AccountScopedState {

    private val _state = MutableStateFlow(NotificationSettingsState())
    override val state: StateFlow<NotificationSettingsState> = _state.asStateFlow()

    override fun update(transform: (NotificationSettingsState) -> NotificationSettingsState) {
        _state.update(transform)
    }

    override fun resetForAccount() {
        _state.value = NotificationSettingsState()
    }
}
