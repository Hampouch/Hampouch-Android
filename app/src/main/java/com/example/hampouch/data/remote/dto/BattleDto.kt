package com.example.hampouch.data.remote.dto

/** 목록(/api/battles)과 상세(/api/battles/{battleId})에서 공통으로 쓰는 참가자 정보. */
data class BattleParticipantDto(
    val userId: Long,
    val nickname: String,
    val avatarUrl: String?,
    val todayAmount: Int?,
    val totalAmount: Int,
    val isValid: Boolean? = null
)

data class MyBattleSummaryDto(
    val battleId: Long,
    val battleCode: String,
    val title: String,
    val penalty: String,
    val startDate: String,
    val endDate: String,
    val status: String,
    val capacity: Int? = null,
    val joinedCount: Int? = null,
    val participants: List<BattleParticipantDto>? = null,
    val winnerNickname: String? = null
)

data class MyBattlesData(
    val battles: List<MyBattleSummaryDto>
)

/** GET /api/battles/{battleId} 상세 응답. capacity 필드는 내려주지 않는다. */
data class BattleDetailData(
    val battleId: Long,
    val battleCode: String,
    val title: String,
    val penalty: String,
    val status: String,
    val startDate: String,
    val endDate: String,
    val participants: List<BattleParticipantDto>,
    val penaltyUserId: Long? = null,
    val penaltyUserNickname: String? = null,
    val finalizedAt: String? = null
)

/** GET /api/battles/invitations/{battleCode} 응답. 참가 전 미리보기라 battleId는 포함하지 않는다. */
data class BattleInvitationPreviewData(
    val title: String,
    val penalty: String,
    val capacity: Int,
    val joinedCount: Int,
    val startDate: String,
    val durationDays: Int
)

/** POST /api/battles/invitations/{battleCode} 응답. */
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
