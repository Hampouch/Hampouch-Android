package com.example.hampouch.domain.repository

import com.example.hampouch.domain.model.NotificationItem
import kotlinx.coroutines.flow.StateFlow

interface NotificationRepository {

    val items: StateFlow<List<NotificationItem>>

    fun findById(id: String): NotificationItem?

    fun markRead(id: String)

    fun markAllRead()
}
