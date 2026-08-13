package com.example.hampouch.domain.model

import java.time.LocalDate

sealed interface ExpenseCategorySelection {
    data class Preset(val id: String) : ExpenseCategorySelection
    data class Custom(val name: String) : ExpenseCategorySelection {
        init { require(name.isNotBlank()) { "custom category name은 비어 있을 수 없습니다." } }
    }
}

sealed interface ExpenseReasonSelection {
    data class Preset(val id: String) : ExpenseReasonSelection
    data class Custom(val value: String) : ExpenseReasonSelection {
        init { require(value.isNotBlank()) { "custom reason은 비어 있을 수 없습니다." } }
    }
}

data class ExpenseRecord(
    val id: String,
    val date: LocalDate,
    val amount: Int,
    val category: ExpenseCategorySelection?,
    val expenseName: String? = null,
    val reason: ExpenseReasonSelection?,
    val memo: String? = null,
    val photoUris: List<String> = emptyList()
) {
    constructor(
        id: String,
        date: LocalDate,
        amount: Int,
        categoryId: String? = null,
        customCategoryName: String? = null,
        expenseName: String? = null,
        reasonId: String? = null,
        customReason: String? = null,
        memo: String? = null,
        photoUris: List<String> = emptyList()
    ) : this(
        id = id,
        date = date,
        amount = amount,
        category = categorySelection(categoryId, customCategoryName),
        expenseName = expenseName,
        reason = reasonSelection(reasonId, customReason),
        memo = memo,
        photoUris = photoUris
    )

    init {
        require(amount > 0) { "실제 지출 금액은 0보다 커야 합니다. 무지출은 별도 상태로 기록합니다." }
    }

    val categoryId: String? get() = (category as? ExpenseCategorySelection.Preset)?.id
    val customCategoryName: String? get() = (category as? ExpenseCategorySelection.Custom)?.name
    val reasonId: String? get() = (reason as? ExpenseReasonSelection.Preset)?.id
    val customReason: String? get() = (reason as? ExpenseReasonSelection.Custom)?.value

    val hasDetail: Boolean
        get() = expenseName != null ||
            category != null ||
            reason != null ||
            !memo.isNullOrBlank() ||
            photoUris.isNotEmpty()
}

private fun categorySelection(
    presetId: String?,
    customName: String?
): ExpenseCategorySelection? = when {
    presetId != null && customName != null ->
        throw IllegalArgumentException("preset category와 custom category를 동시에 지정할 수 없습니다.")
    presetId != null -> ExpenseCategorySelection.Preset(presetId)
    customName != null -> ExpenseCategorySelection.Custom(customName)
    else -> null
}

private fun reasonSelection(
    presetId: String?,
    customValue: String?
): ExpenseReasonSelection? = when {
    presetId != null && customValue != null ->
        throw IllegalArgumentException("preset reason과 custom reason을 동시에 지정할 수 없습니다.")
    presetId != null -> ExpenseReasonSelection.Preset(presetId)
    customValue != null -> ExpenseReasonSelection.Custom(customValue)
    else -> null
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
