package com.example.hampouch.domain.model

import com.example.hampouch.R
import java.time.LocalDate

enum class NotificationCategory(val labelResId: Int) {
    CHALLENGE(R.string.notification_category_challenge),
    HAM_BATTLE(R.string.notification_category_hambattle),
    COMMUNITY(R.string.notification_category_community)
}

fun NotificationSettingsState.isEnabled(category: NotificationCategory): Boolean = when (category) {
    NotificationCategory.CHALLENGE -> challengeAlarmEnabled
    NotificationCategory.HAM_BATTLE -> hamBattleAlarmEnabled
    NotificationCategory.COMMUNITY -> communityAlarmEnabled
}

enum class NotificationSection(val labelResId: Int) {
    TODAY(R.string.notification_section_today),
    YESTERDAY(R.string.notification_section_yesterday),
    LAST_7_DAYS(R.string.notification_section_last_7_days)
}

sealed class NotificationTarget {
    data object Home : NotificationTarget()
    data object ExpenseInput : NotificationTarget()
    data object AmountAdjustment : NotificationTarget()
    data class ChallengeSummary(val challengeId: String) : NotificationTarget()
    data class HamBattleDetail(val challengeId: String) : NotificationTarget()
    data class HamBattleEndedDetail(val challengeId: String) : NotificationTarget()
    data class HamBattleWaitingDetail(val challengeId: String) : NotificationTarget()
    data class MyTipDetail(val postId: String, val scrollToComments: Boolean = false) : NotificationTarget()
    data class CommunityPopularPost(val postId: String) : NotificationTarget()
}

data class NotificationItem(
    val id: String,
    val category: NotificationCategory,
    val section: NotificationSection,
    val title: String,
    val message: String,
    val timeLabel: String,
    val isRead: Boolean,
    val createdDate: LocalDate,
    val target: NotificationTarget
)
