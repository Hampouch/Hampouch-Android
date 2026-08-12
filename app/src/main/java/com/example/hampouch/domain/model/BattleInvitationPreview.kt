package com.example.hampouch.domain.model

import java.time.LocalDate

/** 초대 코드로 참가하기 전에 보여주는 햄배틀 요약. */
data class BattleInvitationPreview(
    val title: String,
    val penalty: String,
    val capacity: Int,
    val joinedCount: Int,
    val startDate: LocalDate?,
    val durationDays: Int
) {
    val isFull: Boolean get() = joinedCount >= capacity
}
