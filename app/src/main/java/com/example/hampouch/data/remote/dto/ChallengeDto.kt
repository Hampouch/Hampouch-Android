package com.example.hampouch.data.remote.dto

data class ChallengeCreateRequest(
    val durationDays: Int,
    val budgetTotal: Int,
    val startDate: String,
    val resetByPayday: Boolean,
    val paydayDay: Int? = null,
    val weakCategories: List<String> = emptyList()
)

data class ChallengeCreateData(
    val challengeId: Long,
    val dailyLimit: Int,
    val startDate: String,
    val endDate: String,
    val status: String
)

data class ChallengeSummaryDto(
    val id: Long,
    val durationDays: Int,
    val startDate: String,
    val endDate: String,
    val budgetTotal: Int,
    val dailyLimit: Int,
    val status: String
)

data class ChallengeProgressDto(
    val elapsedDays: Int,
    val remainingDays: Int,
    val successDays: Int,
    val overDays: Int,
    val currentStreak: Int,
    val savedAmountSoFar: Int
)

data class ChallengeConsumptionDto(
    val todaySpent: Int,
    val todayRemaining: Int,
    val dailyLimit: Int,
    val usageRate: Double,
    val character: String,
    val alertLevel: String
)

data class ChallengeWarningCardDto(
    val type: String? = null
)

data class ChallengeAdjustmentDto(
    val usedCount: Int,
    val maxCount: Int
)

data class ChallengeRestDto(
    val restStartDate: String,
    val plannedResumeDate: String
)

data class ChallengeCurrentData(
    val challenge: ChallengeSummaryDto?,
    val progress: ChallengeProgressDto? = null,
    val consumption: ChallengeConsumptionDto? = null,
    val warningCards: List<ChallengeWarningCardDto>? = null,
    val expenseInputState: String? = null,
    val adjustment: ChallengeAdjustmentDto? = null,
    val rest: ChallengeRestDto? = null
)

data class ChallengeHistoryItemDto(
    val challengeId: Long,
    val status: String,
    val startDate: String,
    val endDate: String,
    val durationDays: Int,
    val budgetTotal: Int,
    val actualSpent: Int,
    val savedAmount: Int
)

data class ChallengeHistoryListData(
    val items: List<ChallengeHistoryItemDto>
)

data class ChallengeResultPeriodDto(
    val startDate: String,
    val endDate: String,
    val durationDays: Int
)

data class ChallengeResultSummaryDto(
    val successDays: Int,
    val overDays: Int,
    val savedAmount: Int,
    val overAmount: Int,
    val maxStreak: Int,
    val budgetTotal: Int,
    val actualSpent: Int
)

data class ChallengeEmotionBreakdownDto(
    val emotion: String,
    val amount: Int,
    val ratio: Int
)

data class ChallengeResultData(
    val challengeId: Long,
    val status: String,
    val expenseLockedAt: String?,
    val period: ChallengeResultPeriodDto,
    val summary: ChallengeResultSummaryDto,
    val emotionBreakdown: List<ChallengeEmotionBreakdownDto> = emptyList()
)

data class ChallengeStatusData(
    val challengeId: Long,
    val status: String
)

data class ChallengeCloseData(
    val challengeId: Long,
    val status: String,
    val expenseLockedAt: String
)

data class ChallengeFocusCategoriesRequest(
    val categories: List<String>
)

data class ChallengeFocusCategoriesData(
    val challengeId: Long,
    val categories: List<String>
)

data class ChallengeFixedDateStartRequest(
    val sourceChallengeId: Long,
    val startDate: String,
    val budgetTotal: Int,
    val fixedDay: Int
)

data class ChallengeFixedDateStartData(
    val challengeId: Long,
    val durationDays: Int,
    val dailyLimit: Int,
    val startDate: String,
    val endDate: String,
    val status: String
)

data class ChallengeFixedDateDraftData(
    val state: String,
    val sourceChallengeId: Long,
    val previousStartDate: String,
    val previousEndDate: String,
    val fixedDay: Int,
    val nextStartDate: String,
    val nextEndDate: String,
    val durationDays: Int,
    val budgetTotal: Int,
    val dailyLimit: Int
)

data class ChallengeCalendarDayDto(
    val date: String,
    val status: String,
    val spentAmount: Int
)

data class ChallengeCalendarData(
    val challengeId: Long,
    val year: Int,
    val month: Int,
    val days: List<ChallengeCalendarDayDto>
)

data class ChallengeDayRequest(
    val date: String,
    val spentAmount: Int
)

data class ChallengeDayData(
    val date: String,
    val spentAmount: Int,
    val dailyLimit: Int,
    val status: String
)

data class ChallengeAdjustRequest(
    val option: String? = null,
    val budgetTotal: Int? = null
)

data class ChallengeAdjustData(
    val challengeId: Long,
    val budgetTotal: Int,
    val dailyLimit: Int,
    val usedCount: Int,
    val maxCount: Int
)

data class ChallengeRecommendationData(
    val message: String
)
