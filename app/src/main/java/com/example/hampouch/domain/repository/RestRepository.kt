package com.example.hampouch.domain.repository

import com.example.hampouch.domain.model.RestPeriod
import com.example.hampouch.domain.model.RestState
import kotlinx.coroutines.flow.StateFlow

interface RestRepository {

    val restState: StateFlow<RestState>

    suspend fun syncStatus(): Result<Unit>

    suspend fun startBreak(period: RestPeriod): Result<Unit>

    /** 이미 휴식 중일 때 기간을 연장한다("더 쉬기"). */
    suspend fun extendBreak(period: RestPeriod): Result<Unit>

    suspend fun resumeNow(): Result<Unit>

    suspend fun postponeOneDay(): Result<Unit>

    fun resetForAccount()
}
