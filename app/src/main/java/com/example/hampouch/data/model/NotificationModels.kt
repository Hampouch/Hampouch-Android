package com.example.hampouch.data.model

import com.example.hampouch.R

enum class NotificationCategory(val labelResId: Int) {
    CHALLENGE(R.string.notification_category_challenge),
    HAM_BATTLE(R.string.notification_category_hambattle),
    COMMUNITY(R.string.notification_category_community)
}

enum class NotificationSection(val labelResId: Int) {
    TODAY(R.string.notification_section_today),
    YESTERDAY(R.string.notification_section_yesterday),
    LAST_7_DAYS(R.string.notification_section_last_7_days)
}

data class NotificationItem(
    val id: String,
    val category: NotificationCategory,
    val section: NotificationSection,
    val title: String,
    val message: String,
    val timeLabel: String,
    val isRead: Boolean
)
