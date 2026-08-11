package com.example.hampouch.domain.model

import java.time.LocalDate

/**
 * 휴식 기간 프리셋. 화면에서 "직접 입력"을 고르면 이 값 없이 일수만 전달된다.
 * 사용자에게 보이는 문구는 [com.example.hampouch.ui.takeabreak.label]에 있다.
 */
enum class BreakDuration {
    THREE_DAYS,
    ONE_WEEK,
    TWO_WEEKS,
    CONTINUOUS
}

/**
 * 휴식 상태.
 *
 * [plannedResumeDate]는 복귀(예정)일이며 이 날짜부터는 휴식이 아니다. null이면 휴식 중이 아니다.
 * [restId]는 휴식 도메인에 상태 조회 API가 없어 생성/복귀 응답으로만 채워진다.
 */
data class RestState(
    val restId: Long? = null,
    val plannedResumeDate: LocalDate? = null
) {
    val isResting: Boolean get() = plannedResumeDate != null

    /** 휴식 기간이 끝나 복귀 안내를 띄워야 하는 시점인지. */
    fun isBreakOver(referenceToday: LocalDate): Boolean {
        val resumeDate = plannedResumeDate ?: return false
        return !referenceToday.isBefore(resumeDate)
    }
}
