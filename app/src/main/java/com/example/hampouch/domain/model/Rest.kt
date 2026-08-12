package com.example.hampouch.domain.model

import java.time.LocalDate

enum class BreakDuration {
    THREE_DAYS,
    ONE_WEEK,
    TWO_WEEKS,
    CONTINUOUS
}

data class RestState(
    val restId: Long? = null,
    val plannedResumeDate: LocalDate? = null
) {
    val isResting: Boolean get() = plannedResumeDate != null

    fun isBreakOver(referenceToday: LocalDate): Boolean {
        val resumeDate = plannedResumeDate ?: return false
        return !referenceToday.isBefore(resumeDate)
    }
}
