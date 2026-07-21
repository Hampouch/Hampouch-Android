package com.example.hampouch.data.model

import com.example.hampouch.R

data class MyPageProfile(
    val name: String,
    val handle: String,
    val email: String
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

enum class TipCategory(val labelResId: Int) {
    COOKING(R.string.tip_category_cooking),
    SHOPPING(R.string.tip_category_shopping),
    DISCOUNT(R.string.tip_category_discount)
}

data class TipPost(
    val id: String,
    val category: TipCategory,
    val title: String,
    val subtitle: String
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
