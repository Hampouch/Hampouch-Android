package com.example.hampouch.domain.repository

interface UsersRepository {

    suspend fun fetchProfile(): Result<Unit>

    suspend fun updateNickname(nickname: String): Result<Unit>

    suspend fun changePassword(currentPassword: String, newPassword: String): Result<Unit>

    suspend fun fetchNotificationSchedule(): Result<Unit>

    /**
     * [RecordAlarmRepository]의 현재 상태를 서버에 반영한다.
     */
    suspend fun updateNotificationSchedule(): Result<Unit>

    suspend fun uploadProfilePhoto(localUri: String): Result<Unit>

    suspend fun resetProfilePhoto(): Result<Unit>
}
