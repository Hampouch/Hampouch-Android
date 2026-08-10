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

/**
 * GET /api/challenges/current(도메인: challenge)의 응답 중 "휴식 중"일 때만 채워지는 rest 블록.
 * challenge 도메인 자체는 아직 서버 연동 전이라 challenge/progress/consumption 등 나머지 필드는 다루지 않는다.
 * rest 도메인에는 상태 조회 API가 없어, 앱을 재시작한 뒤 현재 휴식 상태(복귀 예정일)를 알아낼 유일한
 * 방법이 이 API라서 필요한 rest 블록만 최소로 반영했다.
 */
data class ChallengeCurrentStatusData(val rest: RestStatusData? = null)

data class RestStatusData(
    val restStartDate: String,
    val plannedResumeDate: String
)
