package com.example.hampouch.domain.model

import java.time.LocalDate

enum class BreakDuration {
    THREE_DAYS,
    ONE_WEEK,
    TWO_WEEKS,
    CONTINUOUS
}

sealed interface RestPeriod {
    data class Preset(val duration: BreakDuration) : RestPeriod

    data class Custom(val days: Int) : RestPeriod {
        init {
            require(days > 0) { "직접 입력한 휴식 기간은 1일 이상이어야 합니다." }
        }
    }
}

sealed interface RestState {
    data object NotResting : RestState

    data class Resting(
        val plannedResumeDate: LocalDate,
        val restId: Long? = null
    ) : RestState

    val isResting: Boolean get() = this is Resting

    // 휴식 기간이 끝나 복귀 안내를 띄워야 하는 시점인지.
    fun isBreakOver(referenceToday: LocalDate): Boolean =
        this is Resting && !referenceToday.isBefore(plannedResumeDate)
}
