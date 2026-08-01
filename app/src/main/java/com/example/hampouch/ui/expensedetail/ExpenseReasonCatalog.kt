package com.example.hampouch.ui.expensedetail

import com.example.hampouch.R

object ExpenseReasonCatalog {
    data class Reason(val id: String, val labelResId: Int)

    val reasons = listOf(
        Reason("stress", R.string.expensedetail_reason_stress),
        Reason("reward", R.string.expensedetail_reason_reward),
        Reason("craving", R.string.expensedetail_reason_craving),
        Reason("lazy", R.string.expensedetail_reason_lazy)
    )

    fun byId(id: String?): Reason? = reasons.firstOrNull { it.id == id }
}
