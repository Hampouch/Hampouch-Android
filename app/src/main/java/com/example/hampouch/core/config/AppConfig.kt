package com.example.hampouch.core.config

object AppConfig {
    const val USE_SERVER: Boolean = false
}

object AuthConfig {
    const val USE_SERVER_AUTH: Boolean = AppConfig.USE_SERVER
}

object CommunityConfig {
    const val USE_SERVER_COMMUNITY: Boolean = AppConfig.USE_SERVER
}

object ExpenseConfig {
    const val USE_SERVER_EXPENSE: Boolean = AppConfig.USE_SERVER
}

object MiniChallengeConfig {
    const val USE_SERVER_MINI_CHALLENGE: Boolean = AppConfig.USE_SERVER
}

object ChallengeConfig {
    const val USE_SERVER_CHALLENGE: Boolean = AppConfig.USE_SERVER
}
