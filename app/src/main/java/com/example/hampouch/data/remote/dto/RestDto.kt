package com.example.hampouch.data.remote.dto

data class RestStartRequest(val restDays: Int)

data class RestStartData(
    val restId: Long,
    val restStartDate: String,
    val plannedResumeDate: String
)

data class RestResumeRequest(
    val `when`: String,
    val extendDays: Int? = null
)

object RestResumeWhen {
    const val NOW = "NOW"
    const val TOMORROW = "TOMORROW"
    const val EXTEND = "EXTEND"
}

data class RestResumeData(
    val restId: Long,
    val resumeDate: String? = null,
    val plannedResumeDate: String? = null
)
