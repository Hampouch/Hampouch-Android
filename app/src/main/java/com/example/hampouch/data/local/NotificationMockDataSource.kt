package com.example.hampouch.data.local

import com.example.hampouch.domain.model.NotificationItem

/** 목데이터 모드에서 쓰는 초기 알림 목록. 구현 연결은 [com.example.hampouch.di.MockDataModule]에서 한다. */
interface NotificationMockDataSource {
    fun populated(): List<NotificationItem>
}
