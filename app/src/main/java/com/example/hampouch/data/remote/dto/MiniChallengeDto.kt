package com.example.hampouch.data.remote.dto

data class MiniChallengeItemDto(
    val miniChallengeId: Long,
    val title: String,
    val durationDays: Int,
    val progressDays: Int,
    val itemStreak: Int,
    val checked: Boolean
)

data class MiniChallengeSummaryDto(
    val checkedCount: Int,
    val totalCount: Int,
    val streakDays: Int
)

data class MiniChallengeDayData(
    val date: String,
    val summary: MiniChallengeSummaryDto,
    val items: List<MiniChallengeItemDto>
)

data class RecommendedMiniChallengeDto(
    val recommendedId: Long,
    val title: String,
    val durationDays: Int
)

data class RecommendedMiniChallengeListData(
    val items: List<RecommendedMiniChallengeDto>
)

data class AddRecommendedMiniChallengeRequest(
    val recommendedId: Long
)

data class CustomMiniChallengeBody(
    val title: String,
    val durationDays: Int
)

data class AddCustomMiniChallengeRequest(
    val custom: CustomMiniChallengeBody
)

data class MiniChallengeCreatedData(
    val miniChallengeId: Long,
    val title: String,
    val durationDays: Int,
    val startDate: String,
    val endDate: String
)

data class MiniChallengeCheckRequest(
    val date: String,
    val checked: Boolean
)

data class MiniChallengeCheckData(
    val miniChallengeId: Long,
    val date: String,
    val checked: Boolean
)
