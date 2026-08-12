package com.example.hampouch.data.repository

import com.example.hampouch.domain.model.RecordAlarmSettingsState
import com.example.hampouch.domain.repository.AccountScopedState
import com.example.hampouch.domain.repository.RecordAlarmRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import java.time.LocalDate
import javax.inject.Inject
import javax.inject.Singleton

// TODO: 서버팀 알림 설정 API 연동 시 로컬 상태 대신 서버 응답으로 채우도록 교체.
@Singleton
class RecordAlarmRepositoryImpl @Inject constructor() :
    RecordAlarmRepository, AccountScopedState {

    private val _state = MutableStateFlow(RecordAlarmSettingsState())
    override val state: StateFlow<RecordAlarmSettingsState> = _state.asStateFlow()

    private val _dismissedDate = MutableStateFlow<LocalDate?>(null)
    override val dismissedDate: StateFlow<LocalDate?> = _dismissedDate.asStateFlow()

    override fun update(transform: (RecordAlarmSettingsState) -> RecordAlarmSettingsState) {
        _state.update(transform)
    }

    override fun dismissForToday(referenceToday: LocalDate) {
        _dismissedDate.value = referenceToday
    }

    override fun resetForAccount() {
        _state.value = RecordAlarmSettingsState()
        _dismissedDate.value = null
    }
}
