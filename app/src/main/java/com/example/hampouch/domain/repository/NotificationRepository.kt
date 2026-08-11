package com.example.hampouch.domain.repository

import com.example.hampouch.domain.model.NotificationItem
import kotlinx.coroutines.flow.StateFlow

/** 알림 도메인. [items]는 만료된 항목이 걸러진 목록이다. */
interface NotificationRepository {

    val items: StateFlow<List<NotificationItem>>

    fun findById(id: String): NotificationItem?

    fun markRead(id: String)

    fun markAllRead()
}
