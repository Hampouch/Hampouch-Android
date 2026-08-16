package com.example.hampouch.data.local

import com.example.hampouch.domain.model.NotificationItem

interface NotificationMockDataSource {
    fun populated(): List<NotificationItem>
}
