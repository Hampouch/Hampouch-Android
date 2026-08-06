package com.example.hampouch.data.repository

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import com.example.hampouch.data.model.OnboardingRequest

// TODO: 서버팀 회원가입/로그인 API 연동 시, 계정별 첫 온보딩 데이터 저장을 서버 응답 기반으로 교체.
object OnboardingDataStore {

    private var pendingRequest: OnboardingRequest? by mutableStateOf(null)
    private val savedRequestByUserId = mutableMapOf<String, OnboardingRequest>()

    fun captureOnboardingComplete(request: OnboardingRequest) {
        pendingRequest = request
    }

    fun consumePendingForLogin(userId: String): OnboardingRequest? {
        val pending = pendingRequest ?: return null
        pendingRequest = null
        if (savedRequestByUserId.containsKey(userId)) return null
        savedRequestByUserId[userId] = pending
        return pending
    }
}
