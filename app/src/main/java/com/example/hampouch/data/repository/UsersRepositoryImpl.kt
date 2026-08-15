package com.example.hampouch.data.repository

import android.content.Context
import android.net.Uri
import com.example.hampouch.core.config.UsersConfig
import com.example.hampouch.data.remote.UsersApi
import com.example.hampouch.data.remote.dto.UsersLimitExceededScheduleData
import com.example.hampouch.data.remote.dto.UsersMeData
import com.example.hampouch.data.remote.dto.UsersMissingInputScheduleData
import com.example.hampouch.data.remote.dto.UsersNicknameUpdateRequest
import com.example.hampouch.data.remote.dto.UsersNotificationScheduleData
import com.example.hampouch.data.remote.dto.UsersNotificationScheduleRequest
import com.example.hampouch.data.remote.dto.UsersPasswordChangeRequest
import com.example.hampouch.data.remote.dto.UsersProfilePhotoApplyRequest
import com.example.hampouch.data.remote.dto.UsersProfilePhotoPresignRequest
import com.example.hampouch.data.remote.dto.UsersRecordAlertData
import com.example.hampouch.data.remote.runCatchingNetwork
import com.example.hampouch.data.remote.toApiException
import com.example.hampouch.data.remote.unauthorized
import com.example.hampouch.domain.model.ApiException
import com.example.hampouch.domain.model.DayOfWeekLabel
import com.example.hampouch.domain.model.MyPageProfile
import com.example.hampouch.domain.model.NotificationSettingsState
import com.example.hampouch.domain.model.RecordAlarmSettingsState
import com.example.hampouch.domain.model.ReminderDayMode
import com.example.hampouch.domain.repository.MyPageProfileRepository
import com.example.hampouch.domain.repository.NotificationSettingsRepository
import com.example.hampouch.domain.repository.RecordAlarmRepository
import com.example.hampouch.domain.repository.UsersRepository
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import javax.inject.Inject
import javax.inject.Singleton

private const val TAG = "UsersRepository"

@Singleton
class UsersRepositoryImpl @Inject constructor(
    @ApplicationContext private val context: Context,
    private val usersApi: UsersApi,
    private val authRepository: AuthRepository,
    private val myPageProfileRepository: MyPageProfileRepository,
    private val notificationSettingsRepository: NotificationSettingsRepository,
    private val recordAlarmRepository: RecordAlarmRepository,
    private val okHttpClient: OkHttpClient
) : UsersRepository {

    private fun isRemoteUrl(uri: String): Boolean = uri.startsWith("http://") || uri.startsWith("https://")

    override suspend fun fetchProfile(): Result<Unit> {
        if (!UsersConfig.USE_SERVER_USERS) return Result.success(Unit)
        if (authRepository.currentAuthHeader() == null) return Result.failure(unauthorized())
        return runCatchingNetwork(TAG) {
            val response = usersApi.getMe()
            val data = response.body()?.data
            if (response.isSuccessful && data != null) {
                applyProfileData(data)
                Result.success(Unit)
            } else {
                Result.failure(response.toApiException("사용자 정보를 불러오지 못했습니다."))
            }
        }
    }

    private fun applyProfileData(data: UsersMeData) {
        val user = authRepository.currentUser.value
        myPageProfileRepository.setProfile(
            MyPageProfile(
                name = data.nickname,
                handle = data.handle,
                email = user.email,
                avatarUri = data.profileImageUrl
            )
        )
    }

    override suspend fun updateNickname(nickname: String): Result<Unit> {
        if (!UsersConfig.USE_SERVER_USERS) return Result.success(Unit)
        if (authRepository.currentAuthHeader() == null) return Result.failure(unauthorized())
        return runCatchingNetwork(TAG) {
            val response = usersApi.updateNickname(UsersNicknameUpdateRequest(nickname = nickname))
            if (response.isSuccessful && response.body()?.data != null) {
                Result.success(Unit)
            } else {
                Result.failure(response.toApiException("닉네임 변경에 실패했습니다."))
            }
        }
    }

    override suspend fun changePassword(currentPassword: String, newPassword: String): Result<Unit> {
        if (!UsersConfig.USE_SERVER_USERS) return Result.success(Unit)
        if (authRepository.currentAuthHeader() == null) return Result.failure(unauthorized())
        return runCatchingNetwork(TAG) {
            val response = usersApi.changePassword(
                UsersPasswordChangeRequest(currentPassword = currentPassword, newPassword = newPassword)
            )
            if (response.isSuccessful) {
                Result.success(Unit)
            } else {
                Result.failure(response.toApiException("비밀번호 변경에 실패했습니다."))
            }
        }
    }

    override suspend fun fetchNotificationSchedule(): Result<Unit> {
        if (!UsersConfig.USE_SERVER_USERS) return Result.success(Unit)
        if (authRepository.currentAuthHeader() == null) return Result.failure(unauthorized())
        return runCatchingNetwork(TAG) {
            val response = usersApi.getNotificationSchedule()
            val data = response.body()?.data
            if (response.isSuccessful && data != null) {
                applyScheduleData(data)
                Result.success(Unit)
            } else {
                Result.failure(response.toApiException("알림 설정을 불러오지 못했습니다."))
            }
        }
    }

    override suspend fun updateNotificationSchedule(): Result<Unit> {
        if (!UsersConfig.USE_SERVER_USERS) return Result.success(Unit)
        if (authRepository.currentAuthHeader() == null) return Result.failure(unauthorized())
        return runCatchingNetwork(TAG) {
            val response = usersApi.updateNotificationSchedule(buildScheduleRequest())
            val data = response.body()?.data
            if (response.isSuccessful && data != null) {
                applyScheduleData(data)
                Result.success(Unit)
            } else {
                Result.failure(response.toApiException("알림 설정 변경에 실패했습니다."))
            }
        }
    }

    private fun buildScheduleRequest(): UsersNotificationScheduleRequest {
        val notification = notificationSettingsRepository.state.value
        val record = recordAlarmRepository.state.value
        return UsersNotificationScheduleRequest(
            challengeAlert = notification.challengeAlarmEnabled,
            battleAlert = notification.hamBattleAlarmEnabled,
            communityAlert = notification.communityAlarmEnabled,
            recordAlert = UsersRecordAlertData(
                enabled = record.receiveEnabled,
                missingInput = UsersMissingInputScheduleData(
                    enabled = record.missingReminderEnabled,
                    days = record.selectedDays.map { it.name },
                    time = "%02d:%02d".format(record.hour, record.minute)
                ),
                limitExceeded = UsersLimitExceededScheduleData(enabled = record.limitOverEnabled)
            )
        )
    }

    private fun applyScheduleData(data: UsersNotificationScheduleData) {
        notificationSettingsRepository.update {
            NotificationSettingsState(
                challengeAlarmEnabled = data.challengeAlert,
                hamBattleAlarmEnabled = data.battleAlert,
                communityAlarmEnabled = data.communityAlert
            )
        }
        recordAlarmRepository.update { current ->
            val days = data.recordAlert.missingInput.days
                .mapNotNull { runCatching { DayOfWeekLabel.valueOf(it) }.getOrNull() }
                .toSet()
            val timeParts = data.recordAlert.missingInput.time.split(":")
            val hour = timeParts.getOrNull(0)?.toIntOrNull() ?: current.hour
            val minute = timeParts.getOrNull(1)?.toIntOrNull() ?: current.minute
            current.copy(
                receiveEnabled = data.recordAlert.enabled,
                missingReminderEnabled = data.recordAlert.missingInput.enabled,
                limitOverEnabled = data.recordAlert.limitExceeded.enabled,
                dayMode = ReminderDayMode.CUSTOM,
                selectedDays = days,
                hour = hour,
                minute = minute
            )
        }
    }

    private data class LocalImagePayload(val bytes: ByteArray, val contentType: String)

    private suspend fun readLocalImage(uriString: String): LocalImagePayload = withContext(Dispatchers.IO) {
        val uri = Uri.parse(uriString)
        val resolver = context.contentResolver
        val bytes = resolver.openInputStream(uri)?.use { it.readBytes() }
            ?: throw ApiException("USER_IMAGE_READ_FAILED", "이미지를 읽을 수 없습니다.")
        LocalImagePayload(bytes = bytes, contentType = resolver.getType(uri) ?: "image/jpeg")
    }

    override suspend fun uploadProfilePhoto(localUri: String): Result<Unit> {
        if (!UsersConfig.USE_SERVER_USERS) {
            myPageProfileRepository.profile.value?.let {
                myPageProfileRepository.setProfile(it.copy(avatarUri = localUri))
            }
            return Result.success(Unit)
        }
        if (isRemoteUrl(localUri)) return Result.success(Unit)
        if (authRepository.currentAuthHeader() == null) return Result.failure(unauthorized())
        return runCatchingNetwork(TAG) {
            val payload = readLocalImage(localUri)
            val presignResponse = usersApi.presignProfilePhoto(
                UsersProfilePhotoPresignRequest(contentType = payload.contentType, size = payload.bytes.size.toLong())
            )
            val presignData = presignResponse.body()?.data
            if (!presignResponse.isSuccessful || presignData == null) {
                return@runCatchingNetwork Result.failure(presignResponse.toApiException("이미지 업로드에 실패했습니다."))
            }
            withContext(Dispatchers.IO) {
                val body = payload.bytes.toRequestBody(payload.contentType.toMediaTypeOrNull())
                val request = Request.Builder().url(presignData.uploadUrl).put(body).build()
                okHttpClient.newCall(request).execute().use { httpResponse ->
                    if (!httpResponse.isSuccessful) {
                        throw ApiException("USER_IMAGE_UPLOAD_FAILED", "이미지 업로드에 실패했습니다.")
                    }
                }
            }
            val applyResponse = usersApi.applyProfilePhoto(UsersProfilePhotoApplyRequest(imageKey = presignData.imageKey))
            val applyData = applyResponse.body()?.data
            if (applyResponse.isSuccessful && applyData != null) {
                myPageProfileRepository.profile.value?.let {
                    myPageProfileRepository.setProfile(it.copy(avatarUri = applyData.profileImageUrl))
                }
                Result.success(Unit)
            } else {
                Result.failure(applyResponse.toApiException("프로필 사진 반영에 실패했습니다."))
            }
        }
    }

    override suspend fun resetProfilePhoto(): Result<Unit> {
        if (!UsersConfig.USE_SERVER_USERS) {
            myPageProfileRepository.profile.value?.let {
                myPageProfileRepository.setProfile(it.copy(avatarUri = null))
            }
            return Result.success(Unit)
        }
        if (authRepository.currentAuthHeader() == null) return Result.failure(unauthorized())
        return runCatchingNetwork(TAG) {
            val response = usersApi.resetProfilePhoto()
            if (response.isSuccessful) {
                myPageProfileRepository.profile.value?.let {
                    myPageProfileRepository.setProfile(it.copy(avatarUri = null))
                }
                Result.success(Unit)
            } else {
                Result.failure(response.toApiException("기본 이미지로 변경하지 못했습니다."))
            }
        }
    }
}
