package com.example.hampouch.data.repository

import android.content.Context
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import com.example.hampouch.data.model.ChallengePeriodType
import com.example.hampouch.data.model.OnboardingRequest
import java.time.LocalDate

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
object OnboardingDataStore {

    // 온보딩을 끝냈지만 아직 어떤 계정에도 귀속되지 않은 "임시 보관함". 여기 있는 동안은 누구의 것도 아니다.
    // "건너뛰기"는 이 값을 만들지 않으므로(=null로 남음), 신규 계정이 예약된 값을 못 찾으면 자연히 빈 상태가 된다.
    private var pendingRequest: OnboardingRequest? by mutableStateOf(null)

    // 회원가입이 완료된 계정의 이메일에 실제로 예약된 온보딩 값. 신규 계정([User.isExistingMember] == false)이
    // 첫 로그인할 때만 [AccountDataCoordinator]가 조회한다 — 그 뒤로는 계정 자체가 "기존 회원"으로 바뀌어서
    // 다시 조회되지 않는다.
    private val reservedByEmail = mutableMapOf<String, OnboardingRequest>()
    private var restoredFromPrefs = false

    private fun prefs(context: Context) =
        context.applicationContext.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

    fun hasCompletedOnboarding(context: Context): Boolean =
        prefs(context).getBoolean(KEY_HAS_COMPLETED, false)

    fun restorePendingIfNeeded(context: Context) {
        if (restoredFromPrefs) return
        restoredFromPrefs = true
        val p = prefs(context)
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

    /** 온보딩을 실제로 끝까지 완료했을 때(=시작하기)만 호출한다. */
    fun captureOnboardingComplete(context: Context, request: OnboardingRequest) {
        pendingRequest = request
        prefs(context).edit()
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

    /**
     * 온보딩을 "건너뛰기"로 넘어갈 때 호출한다. 다음 콜드 스타트 때 온보딩 화면을 다시 보여주지 않도록
     * [hasCompletedOnboarding]만 true로 남기고, 대기 중이던 값은 비운다(=건너뛴 계정은 예약된 값이 없는 것으로
     * 취급되어 [AccountDataCoordinator]에서 빈 상태로 처리된다).
     */
    fun markOnboardingSkipped(context: Context) {
        pendingRequest = null
        prefs(context).edit()
            .putBoolean(KEY_HAS_COMPLETED, true)
            .putBoolean(KEY_HAS_PENDING, false)
            .apply()
    }

    /**
     * 회원가입(이메일/소셜)이 실제로 완료된 [email] 계정에 현재 대기 중인 온보딩 값을 예약해둔다.
     * 대기 중인 값이 없으면(=건너뛴 경우) 아무 일도 하지 않는다.
     */
    fun reserveForNewAccount(context: Context, email: String) {
        val pending = pendingRequest ?: return
        pendingRequest = null
        prefs(context).edit().putBoolean(KEY_HAS_PENDING, false).apply()
        reservedByEmail[email] = pending
    }

    /**
     * [email]에 예약된 온보딩 값을 꺼내 쓴다(있으면 제거). 신규 계정인데 예약된 값이 없으면(=건너뛴 경우)
     * null을 반환하며, 호출한 쪽에서 "챌린지 없음" 상태로 처리하면 된다.
     */
    fun takeReservedRequest(email: String): OnboardingRequest? = reservedByEmail.remove(email)
}
