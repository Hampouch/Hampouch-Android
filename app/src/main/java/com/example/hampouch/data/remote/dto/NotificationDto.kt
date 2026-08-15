package com.example.hampouch.data.remote.dto

data class NotificationListData(
    val items: List<NotificationItemDto>
)

data class NotificationItemDto(
    val notificationId: Long,
    val category: String,
    val title: String,
    val message: String,
    val target: NotificationTargetDto?,
    val isRead: Boolean,
    val createdAt: String
)

data class NotificationTargetDto(
    val screen: String,
    val challengeId: Long?
)

data class DeviceTokenRequest(
    val token: String
)
