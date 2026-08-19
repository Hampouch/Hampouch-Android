package com.example.hampouch.domain.repository

import com.example.hampouch.domain.model.MyPageProfile
import com.example.hampouch.domain.model.RecordAlarmSettingsState
import com.example.hampouch.domain.model.User
import kotlinx.coroutines.flow.StateFlow
import java.time.LocalDate

interface RecordAlarmRepository {
    val state: StateFlow<RecordAlarmSettingsState>

    val dismissedDate: StateFlow<LocalDate?>

    fun update(transform: (RecordAlarmSettingsState) -> RecordAlarmSettingsState)

    fun dismissForToday(referenceToday: LocalDate)
}

interface MyPageProfileRepository {
    val profile: StateFlow<MyPageProfile?>

    fun defaultProfileFor(user: User): MyPageProfile

    fun update(user: User, name: String, avatarUri: String?)

    fun setProfile(profile: MyPageProfile)
}
