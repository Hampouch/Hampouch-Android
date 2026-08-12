package com.example.hampouch.domain.repository

import com.example.hampouch.domain.model.MyPageProfile
import com.example.hampouch.domain.model.NotificationSettingsState
import com.example.hampouch.domain.model.RecordAlarmSettingsState
import com.example.hampouch.domain.model.User
import kotlinx.coroutines.flow.StateFlow
import java.time.LocalDate

interface NotificationSettingsRepository {
    val state: StateFlow<NotificationSettingsState>

    fun update(transform: (NotificationSettingsState) -> NotificationSettingsState)
}

interface RecordAlarmRepository {
    val state: StateFlow<RecordAlarmSettingsState>

    /** 기록 누락 리마인더를 마지막으로 닫은 날짜. 오늘이면 다시 띄우지 않는다. */
    val dismissedDate: StateFlow<LocalDate?>

    fun update(transform: (RecordAlarmSettingsState) -> RecordAlarmSettingsState)

    fun dismissForToday(referenceToday: LocalDate)
}

interface MyPageProfileRepository {
    /** 아직 편집한 적이 없으면 null. 화면은 [defaultProfileFor]로 기본값을 만든다. */
    val profile: StateFlow<MyPageProfile?>

    fun defaultProfileFor(user: User): MyPageProfile

    fun update(user: User, name: String, avatarUri: String?)
}
