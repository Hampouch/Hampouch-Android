package com.example.hampouch.data.repository

import com.example.hampouch.core.config.NotificationConfig
import com.example.hampouch.data.local.NotificationMockDataSource
import com.example.hampouch.data.remote.NotificationApi
import com.example.hampouch.data.remote.dto.DeviceTokenRequest
import com.example.hampouch.data.remote.dto.NotificationItemDto
import com.example.hampouch.data.remote.dto.NotificationTargetDto
import com.example.hampouch.data.remote.runCatchingNetwork
import com.example.hampouch.data.remote.toApiException
import com.example.hampouch.data.remote.unauthorized
import com.example.hampouch.domain.model.ApiException
import com.example.hampouch.domain.model.NotificationCategory
import com.example.hampouch.domain.model.NotificationItem
import com.example.hampouch.domain.model.NotificationSection
import com.example.hampouch.domain.model.NotificationTarget
import com.example.hampouch.domain.model.isEnabled
import com.example.hampouch.domain.repository.AccountScopedState
import com.example.hampouch.domain.repository.NotificationRepository
import com.example.hampouch.domain.repository.NotificationSettingsRepository
import com.google.android.gms.tasks.Task
import com.google.firebase.messaging.FirebaseMessaging
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.suspendCancellableCoroutine
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.temporal.ChronoUnit
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException

private const val TAG = "NotificationRepository"

private const val EXPIRY_DAYS = 7L
private const val MINUTES_PER_HOUR = 60
private const val MINUTES_PER_DAY = MINUTES_PER_HOUR * 24

private const val SCREEN_CHALLENGE_DETAIL = "CHALLENGE_DETAIL"
private const val SCREEN_CHALLENGE_RESULT = "CHALLENGE_RESULT"

private suspend fun <T> Task<T>.await(): T =
    suspendCancellableCoroutine { continuation ->
        addOnCompleteListener { task ->
            val exception = task.exception
            if (exception != null) {
                continuation.resumeWithException(exception)
            } else {
                continuation.resume(task.result)
            }
        }
    }

private fun resolveSection(createdDate: LocalDate, today: LocalDate): NotificationSection = when (createdDate) {
    today -> NotificationSection.TODAY
    today.minusDays(1) -> NotificationSection.YESTERDAY
    else -> NotificationSection.LAST_7_DAYS
}

private fun resolveTimeLabel(createdAt: LocalDateTime, now: LocalDateTime): String {
    val minutes = ChronoUnit.MINUTES.between(createdAt, now).coerceAtLeast(0)
    return when {
        minutes < 1 -> "방금"
        minutes < MINUTES_PER_HOUR -> "${minutes}분 전"
        minutes < MINUTES_PER_DAY -> "${minutes / MINUTES_PER_HOUR}시간 전"
        else -> "${minutes / MINUTES_PER_DAY}일전"
    }
}

private fun NotificationTargetDto?.toDomain(): NotificationTarget {
    val challengeId = this?.challengeId?.toString() ?: return NotificationTarget.Home
    return when (this.screen) {
        SCREEN_CHALLENGE_DETAIL, SCREEN_CHALLENGE_RESULT -> NotificationTarget.ChallengeSummary(challengeId)
        else -> NotificationTarget.Home
    }
}

private fun NotificationItemDto.toDomain(now: LocalDateTime): NotificationItem {
    val parsedCreatedAt = LocalDateTime.parse(createdAt)
    val resolvedCategory = runCatching { NotificationCategory.valueOf(category) }
        .getOrDefault(NotificationCategory.CHALLENGE)
    return NotificationItem(
        id = notificationId.toString(),
        category = resolvedCategory,
        section = resolveSection(parsedCreatedAt.toLocalDate(), now.toLocalDate()),
        title = title,
        message = message,
        timeLabel = resolveTimeLabel(parsedCreatedAt, now),
        isRead = isRead,
        createdDate = parsedCreatedAt.toLocalDate(),
        target = target.toDomain()
    )
}

@Singleton
class NotificationRepositoryImpl @Inject constructor(
    private val notificationApi: NotificationApi,
    private val authRepository: AuthRepository,
    private val mockDataSource: NotificationMockDataSource,
    private val notificationSettingsRepository: NotificationSettingsRepository
) : NotificationRepository, AccountScopedState {

    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Default)

    private val allItems = MutableStateFlow(initialItems())

    override val items: StateFlow<List<NotificationItem>> = combine(
        allItems,
        notificationSettingsRepository.state
    ) { list, settings ->
        list.filterNot { isExpired(it) }.filter { settings.isEnabled(it.category) }
    }.stateIn(
        scope,
        SharingStarted.Eagerly,
        allItems.value.filterNot { isExpired(it) }
            .filter { notificationSettingsRepository.state.value.isEnabled(it.category) }
    )

    private fun initialItems(): List<NotificationItem> =
        if (NotificationConfig.USE_SERVER_NOTIFICATION) emptyList() else mockDataSource.populated()

    private fun isExpired(item: NotificationItem): Boolean =
        ChronoUnit.DAYS.between(item.createdDate, LocalDate.now()) >= EXPIRY_DAYS

    override fun findById(id: String): NotificationItem? = items.value.find { it.id == id }

    override suspend fun refresh(): Result<Unit> {
        if (!NotificationConfig.USE_SERVER_NOTIFICATION) return Result.success(Unit)
        if (authRepository.currentAuthHeader() == null) return Result.failure(unauthorized())
        return runCatchingNetwork(TAG) {
            val response = notificationApi.getNotifications()
            val data = response.body()?.data
            if (response.isSuccessful && data != null) {
                val now = LocalDateTime.now()
                allItems.value = data.items.map { it.toDomain(now) }
                Result.success(Unit)
            } else {
                Result.failure(response.toApiException("알림을 불러오지 못했습니다."))
            }
        }
    }

    override suspend fun markRead(id: String): Result<Unit> {
        if (!NotificationConfig.USE_SERVER_NOTIFICATION) {
            markLocalRead(id)
            return Result.success(Unit)
        }
        val notificationId = id.toLongOrNull()
            ?: return Result.failure(ApiException(code = "NOTIFICATION_NOT_FOUND", message = "알림을 찾을 수 없습니다."))
        if (authRepository.currentAuthHeader() == null) return Result.failure(unauthorized())
        return runCatchingNetwork(TAG) {
            val response = notificationApi.markNotificationRead(notificationId)
            if (response.isSuccessful) {
                markLocalRead(id)
                Result.success(Unit)
            } else {
                Result.failure(response.toApiException("알림 읽음 처리에 실패했습니다."))
            }
        }
    }

    override suspend fun markAllRead(): Result<Unit> {
        if (!NotificationConfig.USE_SERVER_NOTIFICATION) {
            markAllLocalRead()
            return Result.success(Unit)
        }
        if (authRepository.currentAuthHeader() == null) return Result.failure(unauthorized())
        return runCatchingNetwork(TAG) {
            val response = notificationApi.markAllNotificationsRead()
            if (response.isSuccessful) {
                markAllLocalRead()
                Result.success(Unit)
            } else {
                Result.failure(response.toApiException("전체 읽음 처리에 실패했습니다."))
            }
        }
    }

    override suspend fun registerDeviceToken(token: String): Result<Unit> {
        if (!NotificationConfig.USE_SERVER_NOTIFICATION) return Result.success(Unit)
        if (authRepository.currentAuthHeader() == null) return Result.failure(unauthorized())
        return runCatchingNetwork(TAG) {
            val response = notificationApi.registerDeviceToken(DeviceTokenRequest(token))
            if (response.isSuccessful) {
                Result.success(Unit)
            } else {
                Result.failure(response.toApiException("기기 토큰 등록에 실패했습니다."))
            }
        }
    }

    override suspend fun unregisterDeviceToken(token: String): Result<Unit> {
        if (!NotificationConfig.USE_SERVER_NOTIFICATION) return Result.success(Unit)
        if (authRepository.currentAuthHeader() == null) return Result.failure(unauthorized())
        return runCatchingNetwork(TAG) {
            val response = notificationApi.unregisterDeviceToken(DeviceTokenRequest(token))
            if (response.isSuccessful) {
                Result.success(Unit)
            } else {
                Result.failure(response.toApiException("기기 토큰 해제에 실패했습니다."))
            }
        }
    }

    override suspend fun syncDeviceToken(): Result<Unit> {
        if (!NotificationConfig.USE_SERVER_NOTIFICATION) return Result.success(Unit)
        return runCatchingNetwork(TAG) {
            val token = FirebaseMessaging.getInstance().token.await()
            registerDeviceToken(token)
        }
    }

    override suspend fun unregisterCurrentDeviceToken(): Result<Unit> {
        if (!NotificationConfig.USE_SERVER_NOTIFICATION) return Result.success(Unit)
        return runCatchingNetwork(TAG) {
            val token = FirebaseMessaging.getInstance().token.await()
            unregisterDeviceToken(token)
        }
    }

    private fun markLocalRead(id: String) {
        allItems.value = allItems.value.map { item -> if (item.id == id) item.copy(isRead = true) else item }
    }

    private fun markAllLocalRead() {
        allItems.value = allItems.value.map { it.copy(isRead = true) }
    }

    override fun resetForAccount() {
        allItems.value = initialItems()
    }
}
