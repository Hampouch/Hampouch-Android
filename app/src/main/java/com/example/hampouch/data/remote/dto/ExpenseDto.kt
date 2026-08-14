package com.example.hampouch.data.remote.dto

/** PUT /api/expenses/no-spend — 0원 지출이 아니라 "그 날짜는 지출 없이 마감" 기록이다. */
data class ExpenseNoSpendRequest(
    val date: String
)

data class ExpenseCreateRequest(
    /** 서버 필수 항목은 date/price뿐이다. 지출명을 안 적었으면 null로 보낸다. */
    val name: String?,
    val price: Int,
    /** 미선택이면 null — 서버가 내부적으로 ETC를 붙인다. 직접입력이면 "ETC". */
    val category: String?,
    val customCategory: String?,
    /** 미선택이면 null — 서버가 내부적으로 ETC를 붙인다. 직접입력이면 "ETC". */
    val emotion: String?,
    val customEmotion: String?,
    val date: String,
    val memo: String?,
    val imageKey: String?
)

data class ExpenseUpdateRequest(
    val name: String?,
    val price: Int,
    val category: String?,
    val customCategory: String?,
    val emotion: String?,
    val customEmotion: String?,
    val date: String,
    val memo: String?
)

data class ExpenseIdData(val expenseId: Long)

data class ExpenseDetailData(
    val expenseId: Long,
    /** 지출명은 선택 입력이라 서버가 null을 내려줄 수 있다. */
    val name: String?,
    val price: Int,
    val date: String,
    val category: String?,
    val customCategory: String?,
    val emotion: String?,
    val customEmotion: String?,
    val memo: String?,
    val imageUrl: String?
)

data class ExpensePhotoPresignRequest(
    val contentType: String,
    val size: Long
)

data class ExpensePhotoPresignData(
    val imageKey: String,
    val uploadUrl: String,
    val expiresInSeconds: Int
)

data class ExpensePhotoConfirmRequest(val imageKey: String)

data class ExpenseDaySummaryItemData(
    val expenseId: Long,
    /** 지출명은 선택 입력이라 서버가 null을 내려줄 수 있다. */
    val name: String?,
    val price: Int,
    val category: String?,
    val categoryLabel: String? = null,
    val emotion: String?,
    val emotionLabel: String? = null
)

data class ExpenseDaySummaryData(
    val date: String,
    val totalAmount: Int,
    /** ACTIVE 지출이 있거나 "오늘은 안 썼어요" 기록이 있으면 true. */
    val hasRecord: Boolean? = null,
    val expenses: List<ExpenseDaySummaryItemData>
)

data class ExpenseDailyAmountData(val date: String, val amount: Int)

data class ExpensePeriodSummaryData(
    val periodStart: String,
    val periodEnd: String,
    val totalAmount: Int,
    val dailyAverage: Int,
    val dailyBreakdown: List<ExpenseDailyAmountData>
)

data class ExpenseCategoryAmountData(val category: String, val amount: Int, val ratio: Int)

data class ExpenseEmotionAmountData(val emotion: String, val amount: Int, val ratio: Int)

data class ExpenseWeekdayAmountData(val dayOfWeek: String, val amount: Int)

data class ExpenseAnalysisData(
    val periodStart: String,
    val periodEnd: String,
    val totalAmount: Int,
    val categoryBreakdown: List<ExpenseCategoryAmountData>,
    val emotionBreakdown: List<ExpenseEmotionAmountData>,
    val weekdayBreakdown: List<ExpenseWeekdayAmountData>,
    val weekdayInsight: String,
    val pouchInsight: String
)

data class ExpenseTagAnalysisItemData(
    val expenseId: Long,
    val date: String,
    /** 지출명은 선택 입력이라 서버가 null을 내려줄 수 있다. */
    val name: String?,
    val category: String?,
    val categoryLabel: String? = null,
    val emotionLabel: String? = null,
    val price: Int
)

data class ExpenseCategoryAnalysisData(
    val category: String,
    val totalAmount: Int,
    val count: Int,
    val ratio: Int,
    val items: List<ExpenseTagAnalysisItemData>
)

data class ExpenseEmotionAnalysisData(
    val emotion: String,
    val totalAmount: Int,
    val count: Int,
    val ratio: Int,
    val items: List<ExpenseTagAnalysisItemData>
)

data class ExpenseTrendPointData(val month: String, val amount: Int)

data class ExpenseTrendData(
    val month: String,
    val totalAmount: Int,
    val monthlyAverage: Int,
    val diffRateFromLastMonth: Int? = null,
    val trend: List<ExpenseTrendPointData>,
    val trendInsight: String
)
