package com.example.hampouch.data.repository

import android.content.Context
import com.example.hampouch.domain.model.ChallengePeriodType
import com.example.hampouch.domain.model.OnboardingRequest
import com.example.hampouch.domain.model.ChallengePeriod
import dagger.hilt.android.qualifiers.ApplicationContext
import java.time.LocalDate
import javax.inject.Inject
import javax.inject.Singleton

private const val PREFS_NAME = "hampouch_onboarding"
private const val KEY_HAS_COMPLETED = "has_completed_onboarding"
private const val KEY_HAS_PENDING = "has_pending_onboarding"
private const val KEY_LAST_MONTH_EXPENSE = "last_month_expense"
private const val KEY_PERIOD_TYPE = "period_type"
private const val KEY_CUSTOM_PERIOD_DAYS = "custom_period_days"
private const val KEY_DATE_FIXED = "date_fixed"
private const val KEY_START_DATE_EPOCH_DAY = "start_date_epoch_day"
private const val KEY_DAILY_TARGET = "daily_target"
private const val KEY_TOTAL_TARGET = "total_target"
private const val KEY_CATEGORY_IDS = "category_ids"

@Singleton
class OnboardingLocalStore @Inject constructor(
    @ApplicationContext private val context: Context
) {

    private var pendingRequest: OnboardingRequest? = null

    private val reservedByEmail = mutableMapOf<String, OnboardingRequest>()
    private var restoredFromPrefs = false

    private fun prefs() =
        context.applicationContext.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

    fun hasCompletedOnboarding(): Boolean =
        prefs().getBoolean(KEY_HAS_COMPLETED, false)

    fun restorePendingIfNeeded() {
        if (restoredFromPrefs) return
        restoredFromPrefs = true
        val p = prefs()
        if (!p.getBoolean(KEY_HAS_PENDING, false)) return
        val period = if (p.getBoolean(KEY_DATE_FIXED, true)) {
            if (!p.contains(KEY_START_DATE_EPOCH_DAY)) return
            ChallengePeriod.FixedStart(LocalDate.ofEpochDay(p.getLong(KEY_START_DATE_EPOCH_DAY, 0L)))
        } else {
            if (!p.contains(KEY_CUSTOM_PERIOD_DAYS)) return
            p.getInt(KEY_CUSTOM_PERIOD_DAYS, 0).takeIf { it > 0 }?.let(ChallengePeriod::Duration) ?: return
        }
        if (!p.contains(KEY_DAILY_TARGET) || !p.contains(KEY_TOTAL_TARGET)) return
        pendingRequest = OnboardingRequest(
            lastMonthFoodExpense = p.getInt(KEY_LAST_MONTH_EXPENSE, 0).takeIf {
                p.contains(KEY_LAST_MONTH_EXPENSE)
            },
            period = period,
            dailyTargetAmount = p.getInt(KEY_DAILY_TARGET, 0),
            totalTargetAmount = p.getInt(KEY_TOTAL_TARGET, 0),
            topSpendingCategoryIds = p.getString(KEY_CATEGORY_IDS, "").orEmpty()
                .split(",")
                .filter { it.isNotEmpty() }
        )
    }

    fun captureOnboardingComplete(request: OnboardingRequest) {
        pendingRequest = request
        val editor = prefs().edit()
            .putBoolean(KEY_HAS_COMPLETED, true)
            .putBoolean(KEY_HAS_PENDING, true)
            .putString(KEY_PERIOD_TYPE, request.challengePeriodType.name)
            .putBoolean(KEY_DATE_FIXED, request.dateFixed)
            .putInt(KEY_DAILY_TARGET, request.dailyTargetAmount)
            .putInt(KEY_TOTAL_TARGET, request.totalTargetAmount)
            .putString(KEY_CATEGORY_IDS, request.topSpendingCategoryIds.joinToString(","))
        request.lastMonthFoodExpense?.let { editor.putInt(KEY_LAST_MONTH_EXPENSE, it) }
            ?: editor.remove(KEY_LAST_MONTH_EXPENSE)
        when (val period = request.period) {
            is ChallengePeriod.FixedStart -> editor
                .putLong(KEY_START_DATE_EPOCH_DAY, period.startDate.toEpochDay())
                .remove(KEY_CUSTOM_PERIOD_DAYS)
            is ChallengePeriod.Duration -> editor
                .putInt(KEY_CUSTOM_PERIOD_DAYS, period.days)
                .remove(KEY_START_DATE_EPOCH_DAY)
        }
        editor.apply()
    }

    fun markOnboardingSkipped() {
        pendingRequest = null
        prefs().edit()
            .putBoolean(KEY_HAS_COMPLETED, true)
            .putBoolean(KEY_HAS_PENDING, false)
            .apply()
    }

    fun resetOnboarding() {
        pendingRequest = null
        prefs().edit().clear().apply()
    }

    fun reserveForNewAccount(email: String) {
        val pending = pendingRequest ?: return
        pendingRequest = null
        prefs().edit().putBoolean(KEY_HAS_PENDING, false).apply()
        reservedByEmail[email] = pending
    }

    fun takeReservedRequest(email: String): OnboardingRequest? = reservedByEmail.remove(email)
}
