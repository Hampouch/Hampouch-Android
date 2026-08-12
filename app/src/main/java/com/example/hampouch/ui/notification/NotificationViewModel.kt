package com.example.hampouch.ui.notification

import androidx.lifecycle.ViewModel
import com.example.hampouch.domain.model.NotificationItem
import com.example.hampouch.domain.repository.NotificationRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.StateFlow
import javax.inject.Inject

@HiltViewModel
class NotificationViewModel @Inject constructor(
    private val notificationRepository: NotificationRepository
) : ViewModel() {

    val items: StateFlow<List<NotificationItem>> = notificationRepository.items

    fun findById(id: String): NotificationItem? = notificationRepository.findById(id)

    fun markRead(id: String) = notificationRepository.markRead(id)

    fun markAllRead() = notificationRepository.markAllRead()
}
