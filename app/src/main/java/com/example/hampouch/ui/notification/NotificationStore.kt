package com.example.hampouch.ui.notification

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import com.example.hampouch.data.model.NotificationItem

// TODO: 서버팀 알림 API 연동 시 이 목데이터 대신 서버 응답으로 items를 채우도록 교체.
object NotificationStore {

    var items: List<NotificationItem> by mutableStateOf(NotificationMockData.populated())
        private set

    val hasUnread: Boolean
        get() = items.any { !it.isRead }

    fun markRead(id: String) {
        items = items.map { item -> if (item.id == id) item.copy(isRead = true) else item }
    }

    fun markAllRead() {
        items = items.map { it.copy(isRead = true) }
    }

    fun resetForAccount() {
        items = NotificationMockData.populated()
    }
}
