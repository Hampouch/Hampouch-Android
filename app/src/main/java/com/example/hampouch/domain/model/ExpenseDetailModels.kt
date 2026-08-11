package com.example.hampouch.domain.model

import java.time.LocalDate

data class ExpenseRecord(
    val id: String,
    val date: LocalDate,
    val amount: Int,
    val categoryId: String? = null,
    val customCategoryName: String? = null,
    val expenseName: String? = null,
    val reasonId: String? = null,
    val customReason: String? = null,
    val memo: String? = null,
    val photoUris: List<String> = emptyList()
) {
    val hasDetail: Boolean
        get() = expenseName != null ||
            categoryId != null ||
            customCategoryName != null ||
            reasonId != null ||
            customReason != null ||
            !memo.isNullOrBlank() ||
            photoUris.isNotEmpty()
}

enum class ExpenseCalendarViewMode {
    MONTHLY,
    WEEKLY
}

data class ExpenseChallengePeriod(
    val startDate: LocalDate,
    val endDate: LocalDate
) {
    fun isActiveOn(date: LocalDate): Boolean = !date.isBefore(startDate) && !date.isAfter(endDate)
}
