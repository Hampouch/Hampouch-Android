package com.example.hampouch.data.remote.dto

// 도메인: rest (휴식 시작 / 복귀·더 쉬기) — 노션 API 명세서 참고

data class RestStartRequest(val restDays: Int)

data class RestStartData(
    val restId: Long,
    val restStartDate: String,
    val plannedResumeDate: String
)

/**
 * when = NOW(지금 바로) / TOMORROW(내일부터) / EXTEND(조금 더 쉴게요). EXTEND면 extendDays 필수.
 * "when"은 코틀린 예약어라 백틱으로 감싸되, 서버 계약대로 JSON 키는 그대로 "when"으로 직렬화된다.
 */
data class RestResumeRequest(
    val `when`: String,
    val extendDays: Int? = null
)

object RestResumeWhen {
    const val NOW = "NOW"
    const val TOMORROW = "TOMORROW"
    const val EXTEND = "EXTEND"
}

/** NOW·TOMORROW 응답은 resumeDate, EXTEND 응답은 plannedResumeDate로 내려온다. */
data class RestResumeData(
    val restId: Long,
    val resumeDate: String? = null,
    val plannedResumeDate: String? = null
)
