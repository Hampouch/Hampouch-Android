package com.example.hampouch.ui.notification

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import com.example.hampouch.data.model.NotificationItem
import java.time.LocalDate
import java.time.temporal.ChronoUnit

private const val EXPIRY_DAYS = 7L

// TODO: 서버팀 알림 API 연동 시 이 목데이터 대신 서버 응답으로 items를 채우도록 교체.
object NotificationStore {

    private var allItems: List<NotificationItem> by mutableStateOf(NotificationMockData.populated())

    val items: List<NotificationItem>
        get() = allItems.filterNot { isExpired(it) }

    val hasUnread: Boolean
        get() = items.any { !it.isRead }

    private fun isExpired(item: NotificationItem): Boolean =
        ChronoUnit.DAYS.between(item.createdDate, LocalDate.now()) >= EXPIRY_DAYS

    fun markRead(id: String) {
        allItems = allItems.map { item -> if (item.id == id) item.copy(isRead = true) else item }
    }

    fun markAllRead() {
        allItems = allItems.map { it.copy(isRead = true) }
    }

    fun resetForAccount() {
        allItems = NotificationMockData.populated()
    }
}
