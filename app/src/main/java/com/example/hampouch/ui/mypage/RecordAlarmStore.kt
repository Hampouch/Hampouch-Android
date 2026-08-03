package com.example.hampouch.ui.mypage

import androidx.compose.runtime.MutableState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import com.example.hampouch.data.model.DayOfWeekLabel
import com.example.hampouch.data.model.RecordAlarmSettingsState
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.LocalTime

// TODO: 서버팀 알림 설정 API 연동 시 이 목데이터 대신 서버 응답으로 state를 채우도록 교체.
object RecordAlarmStore {

    val stateHolder: MutableState<RecordAlarmSettingsState> = mutableStateOf(RecordAlarmSettingsState())

    var state: RecordAlarmSettingsState
        get() = stateHolder.value
        set(value) { stateHolder.value = value }

    private var dismissedDate: LocalDate? by mutableStateOf(null)

    private fun dayOfWeekLabelFor(dayOfWeek: DayOfWeek): DayOfWeekLabel = when (dayOfWeek) {
        DayOfWeek.MONDAY -> DayOfWeekLabel.MON
        DayOfWeek.TUESDAY -> DayOfWeekLabel.TUE
        DayOfWeek.WEDNESDAY -> DayOfWeekLabel.WED
        DayOfWeek.THURSDAY -> DayOfWeekLabel.THU
        DayOfWeek.FRIDAY -> DayOfWeekLabel.FRI
        DayOfWeek.SATURDAY -> DayOfWeekLabel.SAT
        DayOfWeek.SUNDAY -> DayOfWeekLabel.SUN
    }

    fun isMissingReminderDue(
        referenceToday: LocalDate,
        currentTime: LocalTime,
        hasExpenseToday: Boolean
    ): Boolean {
        val current = state
        if (!current.missingReminderEnabled || hasExpenseToday) return false
        if (dismissedDate == referenceToday) return false
        if (dayOfWeekLabelFor(referenceToday.dayOfWeek) !in current.selectedDays) return false
        return !currentTime.isBefore(LocalTime.of(current.hour, current.minute))
    }

    fun dismissForToday(referenceToday: LocalDate) {
        dismissedDate = referenceToday
    }
}
