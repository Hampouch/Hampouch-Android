package com.example.hampouch.domain.model

import java.time.LocalDate

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
