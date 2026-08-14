package com.example.hampouch.ui.notification

import com.example.hampouch.domain.model.NotificationCategory
import com.example.hampouch.domain.model.NotificationItem
import com.example.hampouch.domain.model.NotificationSection
import com.example.hampouch.domain.model.NotificationTarget
import com.example.hampouch.domain.repository.NotificationRepository
import java.time.LocalDate
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import org.junit.Assert.assertTrue
import org.junit.Test

class NotificationViewModelTest {

    @Test
    fun `markAllRead는 노출 중인 모든 알림을 읽음 상태로 전이한다`() {
        val repository = FakeNotificationRepository()
        val viewModel = NotificationViewModel(repository)

        viewModel.markAllRead()

        assertTrue(viewModel.items.value.all { it.isRead })
    }

    private class FakeNotificationRepository : NotificationRepository {
        private val mutableItems = MutableStateFlow(
            listOf(notification("1"), notification("2"))
        )
        override val items: StateFlow<List<NotificationItem>> = mutableItems

        override fun findById(id: String): NotificationItem? = items.value.find { it.id == id }

        override fun markRead(id: String) {
            mutableItems.value = mutableItems.value.map { if (it.id == id) it.copy(isRead = true) else it }
        }

        override fun markAllRead() {
            mutableItems.value = mutableItems.value.map { it.copy(isRead = true) }
        }

        private fun notification(id: String) = NotificationItem(
            id = id,
            category = NotificationCategory.CHALLENGE,
            section = NotificationSection.TODAY,
            title = "제목",
            message = "내용",
            timeLabel = "방금 전",
            isRead = false,
            createdDate = LocalDate.now(),
            target = NotificationTarget.Home
        )
    }
}
