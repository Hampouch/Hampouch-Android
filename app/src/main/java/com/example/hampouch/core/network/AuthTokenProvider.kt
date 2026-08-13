package com.example.hampouch.core.network

/** 네트워크 계층이 data 구현을 알지 않고 현재 인증 헤더를 조회하기 위한 경계. */
interface AuthTokenProvider {
    suspend fun currentAuthHeader(): String?
}
