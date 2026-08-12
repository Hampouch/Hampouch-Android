package com.example.hampouch.domain.repository

import com.example.hampouch.domain.model.BreakDuration
import com.example.hampouch.domain.model.RestState
import kotlinx.coroutines.flow.StateFlow

interface RestRepository {

    val restState: StateFlow<RestState>

    suspend fun syncStatus(): Result<Unit>

    suspend fun startBreak(duration: BreakDuration?, customDays: Int?): Result<Unit>

    suspend fun extendBreak(duration: BreakDuration?, customDays: Int?): Result<Unit>

    suspend fun resumeNow(): Result<Unit>

    suspend fun postponeOneDay(): Result<Unit>

    fun resetForAccount()
}
