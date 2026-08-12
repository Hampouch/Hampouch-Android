package com.example.hampouch.data.repository

import com.example.hampouch.data.local.NotificationMockDataSource
import com.example.hampouch.domain.model.NotificationItem
import com.example.hampouch.domain.repository.AccountScopedState
import com.example.hampouch.domain.repository.NotificationRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.SharingStarted
import java.time.LocalDate
import java.time.temporal.ChronoUnit
import javax.inject.Inject
import javax.inject.Singleton

private const val EXPIRY_DAYS = 7L

// TODO: 서버팀 알림 API 연동 시 목데이터 대신 서버 응답으로 채우도록 교체.
@Singleton
class NotificationRepositoryImpl @Inject constructor(
    private val mockDataSource: NotificationMockDataSource
) : NotificationRepository, AccountScopedState {

    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Default)

    private val allItems = MutableStateFlow(mockDataSource.populated())

    override val items: StateFlow<List<NotificationItem>> = allItems
        .map { list -> list.filterNot { isExpired(it) } }
        .stateIn(scope, SharingStarted.Eagerly, allItems.value.filterNot { isExpired(it) })

    private fun isExpired(item: NotificationItem): Boolean =
        ChronoUnit.DAYS.between(item.createdDate, LocalDate.now()) >= EXPIRY_DAYS

    override fun findById(id: String): NotificationItem? = items.value.find { it.id == id }

    override fun markRead(id: String) {
        allItems.value = allItems.value.map { item ->
            if (item.id == id) item.copy(isRead = true) else item
        }
    }

    override fun markAllRead() {
        allItems.value = allItems.value.map { it.copy(isRead = true) }
    }

    override fun resetForAccount() {
        allItems.value = mockDataSource.populated()
    }
}
