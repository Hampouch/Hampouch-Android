package com.example.hampouch.core.messaging

import android.util.Log
import com.example.hampouch.data.repository.AuthRepository
import com.example.hampouch.domain.model.NotificationCategory
import com.example.hampouch.domain.model.isEnabled
import com.example.hampouch.domain.repository.NotificationRepository
import com.example.hampouch.domain.repository.NotificationSettingsRepository
import com.example.hampouch.ui.notification.SystemNotificationSender
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch
import javax.inject.Inject

private const val TAG = "HampouchFcmService"
private const val KEY_NOTIFICATION_ID = "notificationId"
private const val KEY_TITLE = "title"
private const val KEY_MESSAGE = "message"
private const val KEY_CATEGORY = "category"

@AndroidEntryPoint
class HampouchFirebaseMessagingService : FirebaseMessagingService() {

    @Inject
    lateinit var notificationRepository: NotificationRepository

    @Inject
    lateinit var notificationSettingsRepository: NotificationSettingsRepository

    @Inject
    lateinit var authRepository: AuthRepository

    private val serviceScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    override fun onNewToken(token: String) {
        super.onNewToken(token)
        serviceScope.launch {
            if (authRepository.currentAuthHeader() == null) return@launch
            notificationRepository.registerDeviceToken(token).onFailure {
                Log.w(TAG, "새 FCM 토큰 재등록 실패", it)
            }
        }
    }

    override fun onMessageReceived(message: RemoteMessage) {
        super.onMessageReceived(message)
        val notificationId = message.data[KEY_NOTIFICATION_ID] ?: return
        val title = message.data[KEY_TITLE] ?: message.notification?.title ?: return
        val body = message.data[KEY_MESSAGE] ?: message.notification?.body ?: return
        val category = message.data[KEY_CATEGORY]?.let { raw ->
            runCatching { NotificationCategory.valueOf(raw) }.getOrNull()
        }
        if (category != null && !notificationSettingsRepository.state.value.isEnabled(category)) return
        SystemNotificationSender.notify(applicationContext, id = notificationId, title = title, message = body)
    }

    override fun onDestroy() {
        super.onDestroy()
        serviceScope.cancel()
    }
}
