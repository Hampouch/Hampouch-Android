package com.example.hampouch.data.remote.dto

data class BattleParticipantDto(
    val userId: Long,
    val nickname: String,
    val avatarUrl: String?,
    val todayAmount: Int?,
    val totalAmount: Int,
    val isValid: Boolean? = null
)

sealed interface MyBattleSummaryDto {
    val battleId: Long
    val battleCode: String
    val title: String
    val penalty: String
    val startDate: String
    val endDate: String

    data class Ready(
        override val battleId: Long,
        override val battleCode: String,
        override val title: String,
        override val penalty: String,
        override val startDate: String,
        override val endDate: String,
        val capacity: Int,
        val joinedCount: Int
    ) : MyBattleSummaryDto

    data class Ongoing(
        override val battleId: Long,
        override val battleCode: String,
        override val title: String,
        override val penalty: String,
        override val startDate: String,
        override val endDate: String,
        val participants: List<BattleParticipantDto>
    ) : MyBattleSummaryDto

    data class Terminated(
        override val battleId: Long,
        override val battleCode: String,
        override val title: String,
        override val penalty: String,
        override val startDate: String,
        override val endDate: String,
        val winnerNickname: String
    ) : MyBattleSummaryDto
}

data class MyBattlesData(
    val battles: List<MyBattleSummaryDto>
)

data class BattleDetailData(
    val battleId: Long,
    val battleCode: String,
    val title: String,
    val penalty: String,
    val status: String,
    val startDate: String,
    val endDate: String,
    val participants: List<BattleParticipantDto>,
    val penaltyTargetNickname: String? = null
)

data class BattleInvitationPreviewData(
    val title: String,
    val penalty: String,
    val capacity: Int,
    val joinedCount: Int,
    val startDate: String,
    val durationDays: Int
)

data class JoinBattleData(
    val battleId: Long
)

data class CreateBattleRequest(
    val title: String,
    val capacity: Int,
    val durationDays: Int,
    val startDate: String,
    val penalty: String
)

data class CreateBattleData(
    val battleId: Long,
    val battleCode: String,
    val title: String,
    val capacity: Int,
    val durationDays: Int,
    val startDate: String,
    val endDate: String,
    val penalty: String,
    val status: String
)
