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
    WHAT_TO_EAT(R.string.tip_category_what_to_eat),
    SHOPPING(R.string.tip_category_shopping),
    COOKING(R.string.tip_category_cooking),
    DISCOUNT(R.string.tip_category_discount),
    RECRUIT(R.string.tip_category_recruit)
}

data class TipPost(
    val id: String,
    val category: TipCategory,
    val title: String,
    val subtitle: String,
    val authorName: String = "",
    val isEditorAuthor: Boolean = false,
    val postedMinutesAgo: Int = 0,
    val viewCount: Int = 0,
    val commentCount: Int = 0,
    val likeCount: Int = 0,
    val hasImage: Boolean = false
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
