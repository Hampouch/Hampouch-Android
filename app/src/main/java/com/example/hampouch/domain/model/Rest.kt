package com.example.hampouch.domain.model

import java.time.LocalDate

// 휴식 기간 프리셋
enum class BreakDuration {
    THREE_DAYS,
    ONE_WEEK,
    TWO_WEEKS,
    CONTINUOUS
}

// 휴식 상태
data class RestState(
    val restId: Long? = null,
    val plannedResumeDate: LocalDate? = null
) {
    val isResting: Boolean get() = plannedResumeDate != null

    // 휴식 기간이 끝나 복귀 안내를 띄워야 하는 시점인지.
    fun isBreakOver(referenceToday: LocalDate): Boolean {
        val resumeDate = plannedResumeDate ?: return false
        return !referenceToday.isBefore(resumeDate)
    }
}
