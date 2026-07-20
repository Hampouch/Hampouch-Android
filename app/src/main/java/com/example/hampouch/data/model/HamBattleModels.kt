package com.example.hampouch.data.model

data class HamBattleChallengeRequest(
    val challengeName: String,
    val participantCount: String,
    val durationDays: String,
    val startDateMillis: Long?,
    val penalty: String
)

val HamBattleParticipantOptions = listOf("1 vs 1", "3인", "4인", "5인", "6인", "7인", "8인", "9인", "10인")
val HamBattleDurationOptions = listOf("3일", "7일", "14일", "31일")
val HamBattleDefaultPenaltyOptions = listOf("커피 사기", "밥 사기", "영화 사기")

data class HamBattleParticipantSpending(
    val name: String,
    val amount: Int
)

data class HamBattleActiveChallenge(
    val id: String,
    val type: String,
    val isOneVsOne: Boolean,
    val title: String,
    val penalty: String,
    val participants: List<HamBattleParticipantSpending>,
    val dDay: String,
    val statusMessage: String
)

data class HamBattleWaitingChallenge(
    val id: String,
    val type: String,
    val title: String,
    val penalty: String,
    val joinedCount: Int,
    val totalCount: Int,
    val startsInDay: String,
    val startDateLabel: String
)
