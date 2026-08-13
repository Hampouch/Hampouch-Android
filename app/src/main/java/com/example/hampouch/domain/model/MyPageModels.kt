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
    val endDateLabel: String,
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
    SUN(R.string.day_sunday);

    companion object
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

fun DayOfWeekLabel.Companion.of(dayOfWeek: java.time.DayOfWeek): DayOfWeekLabel = when (dayOfWeek) {
    java.time.DayOfWeek.MONDAY -> DayOfWeekLabel.MON
    java.time.DayOfWeek.TUESDAY -> DayOfWeekLabel.TUE
    java.time.DayOfWeek.WEDNESDAY -> DayOfWeekLabel.WED
    java.time.DayOfWeek.THURSDAY -> DayOfWeekLabel.THU
    java.time.DayOfWeek.FRIDAY -> DayOfWeekLabel.FRI
    java.time.DayOfWeek.SATURDAY -> DayOfWeekLabel.SAT
    java.time.DayOfWeek.SUNDAY -> DayOfWeekLabel.SUN
}

/** 오늘 기록 누락 리마인더를 띄워야 하는지. [dismissedDate]가 오늘이면 이미 닫은 것으로 본다. */
fun RecordAlarmSettingsState.isMissingReminderDue(
    dismissedDate: java.time.LocalDate?,
    referenceToday: java.time.LocalDate,
    currentTime: java.time.LocalTime,
    hasExpenseToday: Boolean
): Boolean {
    if (!missingReminderEnabled || hasExpenseToday) return false
    if (dismissedDate == referenceToday) return false
    if (DayOfWeekLabel.of(referenceToday.dayOfWeek) !in selectedDays) return false
    return !currentTime.isBefore(java.time.LocalTime.of(hour, minute))
}
