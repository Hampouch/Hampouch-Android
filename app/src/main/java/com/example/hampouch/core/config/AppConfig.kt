package com.example.hampouch.core.config

object AppConfig {
    const val USE_SERVER: Boolean = true
}

object AuthConfig {
    const val USE_SERVER_AUTH: Boolean = AppConfig.USE_SERVER
}

object CommunityConfig {
    const val USE_SERVER_COMMUNITY: Boolean = AppConfig.USE_SERVER
}
