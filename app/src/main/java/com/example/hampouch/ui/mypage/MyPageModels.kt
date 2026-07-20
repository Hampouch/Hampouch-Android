package com.example.hampouch.ui.mypage

import com.example.hampouch.R

data class MyPageProfile(
    val name: String,
    val handle: String,
    val userId: String
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
    val achievedDays: Int,
    val startDateLabel: String,
    val endDateLabel: String?,
    val dailyLimit: Int,
    val totalSaved: Int
)

enum class ReminderDayMode {
    DAILY, WEEKEND_ONLY, CUSTOM
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
    val recordAlarmEnabled: Boolean = true,
    val challengeAlarmEnabled: Boolean = true,
    val hamBattleAlarmEnabled: Boolean = true,
    val communityAlarmEnabled: Boolean = false
)

data class RecordAlarmSettingsState(
    val receiveEnabled: Boolean = true,
    val missingReminderEnabled: Boolean = true,
    val limitOverEnabled: Boolean = true,
    val secondReceiveEnabled: Boolean = true,
    val dayMode: ReminderDayMode = ReminderDayMode.DAILY,
    val selectedDays: Set<DayOfWeekLabel> = setOf(
        DayOfWeekLabel.MON,
        DayOfWeekLabel.TUE,
        DayOfWeekLabel.WED,
        DayOfWeekLabel.THU,
        DayOfWeekLabel.FRI,
        DayOfWeekLabel.SAT
    ),
    val timeLabel: String = ""
)
