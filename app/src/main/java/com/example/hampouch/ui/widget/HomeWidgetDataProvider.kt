package com.example.hampouch.ui.widget

import android.content.Context
import androidx.core.content.edit
import androidx.glance.appwidget.updateAll
import com.example.hampouch.domain.model.ActiveChallenge
import com.example.hampouch.domain.model.HomeChallenge
import dagger.hilt.android.qualifiers.ApplicationContext
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.time.temporal.ChronoUnit
import java.util.Locale
import javax.inject.Inject
import javax.inject.Singleton

private const val PREFS_NAME = "home_widget_snapshot"
private const val TYPE_LOADING = "loading"
private const val TYPE_LOGGED_OUT = "logged_out"
private const val TYPE_IN_PROGRESS = "in_progress"
private const val TYPE_RESTING = "resting"
private const val TYPE_NO_ACTIVE = "no_active"

/**
 * 위젯 프로세스가 앱의 인메모리 상태에 의존하지 않도록 마지막 표시 상태를 저장한다.
 * 토큰·이메일은 저장하지 않고 계정 전환 감지용 내부 id와 화면 표시 데이터만 보관한다.
 */
@Singleton
class HomeWidgetSnapshotStore @Inject constructor(
    @param:ApplicationContext private val context: Context
) {
    private val preferences = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

    fun read(referenceToday: LocalDate = LocalDate.now()): HomeWidgetState =
        when (preferences.getString(KEY_TYPE, TYPE_LOADING)) {
            TYPE_LOGGED_OUT -> HomeWidgetState.LoggedOut
            TYPE_RESTING -> HomeWidgetState.Resting(
                plannedResumeDateLabel = preferences.getString(KEY_RESUME_DATE_LABEL, "").orEmpty()
            )
            TYPE_NO_ACTIVE -> HomeWidgetState.NoActiveChallenge
            TYPE_IN_PROGRESS -> readChallenge(referenceToday)
            else -> HomeWidgetState.Loading
        }

    suspend fun markSession(accountKey: String) {
        val previousAccount = preferences.getString(KEY_ACCOUNT, null)
        val previousType = preferences.getString(KEY_TYPE, null)
        if (previousAccount == accountKey && previousType != TYPE_LOGGED_OUT) return
        write(TYPE_LOADING, accountKey)
    }

    suspend fun publishLoggedOut() = write(TYPE_LOGGED_OUT, accountKey = null)

    suspend fun publishNoActive(accountKey: String) = write(TYPE_NO_ACTIVE, accountKey)

    suspend fun publishResting(accountKey: String, plannedResumeDate: LocalDate) {
        val resumeDateLabel = plannedResumeDate.format(periodLabelFormatter)
        if (preferences.getString(KEY_TYPE, null) == TYPE_RESTING &&
            preferences.getString(KEY_ACCOUNT, null) == accountKey &&
            preferences.getString(KEY_RESUME_DATE_LABEL, null) == resumeDateLabel
        ) {
            HomeWidget().updateAll(context)
            return
        }
        preferences.edit(commit = true) {
            clear()
            putString(KEY_TYPE, TYPE_RESTING)
            putString(KEY_ACCOUNT, accountKey)
            putString(KEY_RESUME_DATE_LABEL, resumeDateLabel)
        }
        HomeWidget().updateAll(context)
    }

    suspend fun publishChallenge(accountKey: String, challenge: ActiveChallenge, todaySpent: Int) {
        val today = LocalDate.now()
        val dailyLimit = challenge.dailyLimitOn(today)
        val values = ChallengeSnapshot(
            accountKey = accountKey,
            snapshotDate = today.toString(),
            periodStart = challenge.periodStart.toString(),
            periodEnd = challenge.effectivePeriodEnd.toString(),
            totalDays = challenge.totalDays,
            dailyLimit = dailyLimit,
            todayBalance = dailyLimit - todaySpent,
            savedAmount = challenge.savedAmount,
            streakDays = challenge.streakDays
        )
        if (matches(values)) {
            HomeWidget().updateAll(context)
            return
        }
        preferences.edit(commit = true) {
            clear()
            putString(KEY_TYPE, TYPE_IN_PROGRESS)
            putString(KEY_ACCOUNT, values.accountKey)
            putString(KEY_SNAPSHOT_DATE, values.snapshotDate)
            putString(KEY_PERIOD_START, values.periodStart)
            putString(KEY_PERIOD_END, values.periodEnd)
            putInt(KEY_TOTAL_DAYS, values.totalDays)
            putInt(KEY_DAILY_LIMIT, values.dailyLimit)
            putInt(KEY_TODAY_BALANCE, values.todayBalance)
            putInt(KEY_SAVED_AMOUNT, values.savedAmount)
            putInt(KEY_STREAK_DAYS, values.streakDays)
        }
        HomeWidget().updateAll(context)
    }

    private suspend fun write(type: String, accountKey: String?) {
        if (preferences.getString(KEY_TYPE, null) == type &&
            preferences.getString(KEY_ACCOUNT, null) == accountKey
        ) {
            HomeWidget().updateAll(context)
            return
        }
        preferences.edit(commit = true) {
            clear()
            putString(KEY_TYPE, type)
            accountKey?.let { putString(KEY_ACCOUNT, it) }
        }
        HomeWidget().updateAll(context)
    }

    private fun matches(values: ChallengeSnapshot): Boolean =
        preferences.getString(KEY_TYPE, null) == TYPE_IN_PROGRESS &&
            preferences.getString(KEY_ACCOUNT, null) == values.accountKey &&
            preferences.getString(KEY_SNAPSHOT_DATE, null) == values.snapshotDate &&
            preferences.getString(KEY_PERIOD_START, null) == values.periodStart &&
            preferences.getString(KEY_PERIOD_END, null) == values.periodEnd &&
            preferences.getInt(KEY_TOTAL_DAYS, -1) == values.totalDays &&
            preferences.getInt(KEY_DAILY_LIMIT, -1) == values.dailyLimit &&
            preferences.getInt(KEY_TODAY_BALANCE, Int.MIN_VALUE) == values.todayBalance &&
            preferences.getInt(KEY_SAVED_AMOUNT, Int.MIN_VALUE) == values.savedAmount &&
            preferences.getInt(KEY_STREAK_DAYS, -1) == values.streakDays

    private fun readChallenge(referenceToday: LocalDate): HomeWidgetState {
        val periodStart = preferences.getString(KEY_PERIOD_START, null)?.let(::parseDateOrNull)
            ?: return HomeWidgetState.Loading
        val periodEnd = preferences.getString(KEY_PERIOD_END, null)?.let(::parseDateOrNull)
            ?: return HomeWidgetState.Loading
        if (referenceToday.isBefore(periodStart) || referenceToday.isAfter(periodEnd)) {
            return HomeWidgetState.NoActiveChallenge
        }

        val snapshotDate = preferences.getString(KEY_SNAPSHOT_DATE, null)?.let(::parseDateOrNull)
        val dailyLimit = preferences.getInt(KEY_DAILY_LIMIT, 0)
        val todayBalance = if (snapshotDate == referenceToday) {
            preferences.getInt(KEY_TODAY_BALANCE, dailyLimit)
        } else {
            dailyLimit
        }
        return HomeWidgetState.InProgress(
            HomeChallenge(
                totalDays = preferences.getInt(KEY_TOTAL_DAYS, 0),
                dDay = ChronoUnit.DAYS.between(referenceToday, periodEnd).toInt().coerceAtLeast(0),
                periodStartLabel = periodStart.format(periodLabelFormatter),
                periodEndLabel = periodEnd.format(periodLabelFormatter),
                dailyLimit = dailyLimit,
                todayBalance = todayBalance,
                savedAmount = preferences.getInt(KEY_SAVED_AMOUNT, 0),
                streakDays = preferences.getInt(KEY_STREAK_DAYS, 0)
            )
        )
    }

    private fun parseDateOrNull(value: String): LocalDate? = runCatching { LocalDate.parse(value) }.getOrNull()

    private companion object {
        val periodLabelFormatter: DateTimeFormatter = DateTimeFormatter.ofPattern("M월 d일", Locale.KOREA)
        const val KEY_TYPE = "type"
        const val KEY_ACCOUNT = "account"
        const val KEY_RESUME_DATE_LABEL = "resume_date_label"
        const val KEY_SNAPSHOT_DATE = "snapshot_date"
        const val KEY_PERIOD_START = "period_start"
        const val KEY_PERIOD_END = "period_end"
        const val KEY_TOTAL_DAYS = "total_days"
        const val KEY_DAILY_LIMIT = "daily_limit"
        const val KEY_TODAY_BALANCE = "today_balance"
        const val KEY_SAVED_AMOUNT = "saved_amount"
        const val KEY_STREAK_DAYS = "streak_days"
    }
}

private data class ChallengeSnapshot(
    val accountKey: String,
    val snapshotDate: String,
    val periodStart: String,
    val periodEnd: String,
    val totalDays: Int,
    val dailyLimit: Int,
    val todayBalance: Int,
    val savedAmount: Int,
    val streakDays: Int
)
