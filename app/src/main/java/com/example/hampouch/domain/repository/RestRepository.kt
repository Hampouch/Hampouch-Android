package com.example.hampouch.domain.repository

import com.example.hampouch.domain.model.RestPeriod
import com.example.hampouch.domain.model.RestState
import kotlinx.coroutines.flow.StateFlow

// 쉬어가기 도메인
interface RestRepository {

    /** 현재 휴식 상태. 앱 전역에서 공유된다. */
    val restState: StateFlow<RestState>

    /** 앱 재시작 등으로 메모리 상태가 비었을 때 서버 기준으로 보정한다. */
    suspend fun syncStatus(): Result<Unit>

    suspend fun startBreak(period: RestPeriod): Result<Unit>

    /** 이미 휴식 중일 때 기간을 연장한다("더 쉬기"). */
    suspend fun extendBreak(period: RestPeriod): Result<Unit>

    /** 지금 바로 복귀한다. */
    suspend fun resumeNow(): Result<Unit>

    /** 복귀를 하루 미룬다. */
    suspend fun postponeOneDay(): Result<Unit>

    /** 계정이 바뀌었을 때 이전 계정의 상태를 지운다. */
    fun resetForAccount()
}
