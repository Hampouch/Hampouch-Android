package com.example.hampouch.domain.repository

import com.example.hampouch.domain.model.NotificationItem
import kotlinx.coroutines.flow.StateFlow

interface NotificationRepository {

    val items: StateFlow<List<NotificationItem>>

    fun findById(id: String): NotificationItem?

    suspend fun refresh(): Result<Unit>

    suspend fun markRead(id: String): Result<Unit>

    suspend fun markAllRead(): Result<Unit>

    suspend fun registerDeviceToken(token: String): Result<Unit>

    suspend fun unregisterDeviceToken(token: String): Result<Unit>

    suspend fun syncDeviceToken(): Result<Unit>

    suspend fun unregisterCurrentDeviceToken(): Result<Unit>
}
