package com.example.hampouch.data.repository

import android.content.Context
import com.example.hampouch.domain.model.ChallengePeriodType
import com.example.hampouch.domain.model.OnboardingRequest
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

// TODO: 서버팀 회원가입/로그인 API 연동 시, 계정별 첫 온보딩 데이터 저장을 서버 응답 기반으로 교체.
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
        pendingRequest = OnboardingRequest(
            lastMonthFoodExpense = p.getInt(KEY_LAST_MONTH_EXPENSE, -1).takeIf { it >= 0 },
            challengePeriodType = runCatching {
                ChallengePeriodType.valueOf(p.getString(KEY_PERIOD_TYPE, null).orEmpty())
            }.getOrDefault(ChallengePeriodType.ONE_MONTH),
            customPeriodDays = p.getInt(KEY_CUSTOM_PERIOD_DAYS, -1).takeIf { it >= 0 },
            dateFixed = p.getBoolean(KEY_DATE_FIXED, true),
            startDate = p.getLong(KEY_START_DATE_EPOCH_DAY, -1L).takeIf { it >= 0 }?.let(LocalDate::ofEpochDay),
            dailyTargetAmount = p.getInt(KEY_DAILY_TARGET, -1).takeIf { it >= 0 },
            totalTargetAmount = p.getInt(KEY_TOTAL_TARGET, -1).takeIf { it >= 0 },
            topSpendingCategoryIds = p.getString(KEY_CATEGORY_IDS, "").orEmpty()
                .split(",")
                .filter { it.isNotEmpty() }
        )
    }

    fun captureOnboardingComplete(request: OnboardingRequest) {
        pendingRequest = request
        prefs().edit()
            .putBoolean(KEY_HAS_COMPLETED, true)
            .putBoolean(KEY_HAS_PENDING, true)
            .putInt(KEY_LAST_MONTH_EXPENSE, request.lastMonthFoodExpense ?: -1)
            .putString(KEY_PERIOD_TYPE, request.challengePeriodType.name)
            .putInt(KEY_CUSTOM_PERIOD_DAYS, request.customPeriodDays ?: -1)
            .putBoolean(KEY_DATE_FIXED, request.dateFixed)
            .putLong(KEY_START_DATE_EPOCH_DAY, request.startDate?.toEpochDay() ?: -1L)
            .putInt(KEY_DAILY_TARGET, request.dailyTargetAmount ?: -1)
            .putInt(KEY_TOTAL_TARGET, request.totalTargetAmount ?: -1)
            .putString(KEY_CATEGORY_IDS, request.topSpendingCategoryIds.joinToString(","))
            .apply()
    }

    fun markOnboardingSkipped() {
        pendingRequest = null
        prefs().edit()
            .putBoolean(KEY_HAS_COMPLETED, true)
            .putBoolean(KEY_HAS_PENDING, false)
            .apply()
    }

    fun reserveForNewAccount(email: String) {
        val pending = pendingRequest ?: return
        pendingRequest = null
        prefs().edit().putBoolean(KEY_HAS_PENDING, false).apply()
        reservedByEmail[email] = pending
    }

    fun takeReservedRequest(email: String): OnboardingRequest? = reservedByEmail.remove(email)
}
