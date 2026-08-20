package com.example.hampouch.data.remote.dto

data class UsersMeData(
    val nickname: String,
    val profileImageUrl: String?,
    val handle: String
)

data class UsersNicknameUpdateRequest(val nickname: String)

data class UsersNicknameUpdateData(val nickname: String)

data class UsersPasswordChangeRequest(
    val currentPassword: String,
    val newPassword: String
)

data class UsersMissingInputScheduleData(
    val enabled: Boolean,
    val days: List<String>,
    val time: String
)

data class UsersLimitExceededScheduleData(val enabled: Boolean)

data class UsersRecordAlertData(
    val enabled: Boolean,
    val missingInput: UsersMissingInputScheduleData,
    val limitExceeded: UsersLimitExceededScheduleData
)

data class UsersNotificationScheduleData(
    val challengeAlert: Boolean,
    val battleAlert: Boolean,
    val communityAlert: Boolean,
    val recordAlert: UsersRecordAlertData
)

data class UsersNotificationScheduleRequest(
    val challengeAlert: Boolean,
    val battleAlert: Boolean,
    val communityAlert: Boolean,
    val recordAlert: UsersRecordAlertData
)

data class UsersProfilePhotoPresignRequest(
    val contentType: String,
    val size: Long
)

data class UsersProfilePhotoPresignData(
    val imageKey: String,
    val uploadUrl: String,
    val expiresInSeconds: Int
)

data class UsersProfilePhotoApplyRequest(val imageKey: String)

data class UsersProfilePhotoApplyData(val imageUrl: String)
