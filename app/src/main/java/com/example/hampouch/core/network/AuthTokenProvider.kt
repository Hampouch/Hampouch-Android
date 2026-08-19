package com.example.hampouch.core.network

interface AuthTokenProvider {
    suspend fun currentAuthHeader(): String?
}
