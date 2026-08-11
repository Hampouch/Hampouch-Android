package com.example.hampouch.domain.model

import com.example.hampouch.R

data class MyPageProfile(
    val name: String,
    val handle: String,
    val email: String,
    val avatarUri: String? = null
)

enum class ChallengeStatus(val labelResId: Int) {
    IN_PROGRESS(R.string.challenge_history_status_in_progress),
    SUCCESS(R.string.challenge_history_status_success),
    FAIL(R.string.challenge_history_status_fail)
}

data class ChallengeRecord(
    val id: String,
    val status: ChallengeStatus,
    val totalDays: Int,
    val startDateLabel: String,
    val endDateLabel: String?,
    val targetAmount: Int,
    val actualAmount: Int
)

enum class ReminderDayMode(val labelResId: Int) {
    WEEKDAY(R.string.record_alarm_mode_weekday),
    WEEKEND(R.string.record_alarm_mode_weekend),
    DAILY(R.string.record_alarm_mode_daily),
    CUSTOM(R.string.record_alarm_mode_custom)
}

enum class DayOfWeekLabel(val labelResId: Int) {
    MON(R.string.day_monday),
    TUE(R.string.day_tuesday),
    WED(R.string.day_wednesday),
    THU(R.string.day_thursday),
    FRI(R.string.day_friday),
    SAT(R.string.day_saturday),
    SUN(R.string.day_sunday)
}

data class NotificationSettingsState(
    val challengeAlarmEnabled: Boolean = true,
    val hamBattleAlarmEnabled: Boolean = true,
    val communityAlarmEnabled: Boolean = false
)

data class RecordAlarmSettingsState(
    val receiveEnabled: Boolean = false,
    val missingReminderEnabled: Boolean = false,
    val limitOverEnabled: Boolean = false,
    val dayMode: ReminderDayMode = ReminderDayMode.WEEKDAY,
    val selectedDays: Set<DayOfWeekLabel> = setOf(
        DayOfWeekLabel.MON,
        DayOfWeekLabel.TUE,
        DayOfWeekLabel.WED,
        DayOfWeekLabel.THU,
        DayOfWeekLabel.FRI
    ),
    val hour: Int = 20,
    val minute: Int = 0
)
