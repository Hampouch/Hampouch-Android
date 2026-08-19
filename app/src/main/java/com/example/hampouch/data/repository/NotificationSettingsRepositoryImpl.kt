package com.example.hampouch.data.repository

import android.content.Context
import com.example.hampouch.domain.model.NotificationSettingsState
import com.example.hampouch.domain.repository.AccountScopedState
import com.example.hampouch.domain.repository.NotificationSettingsRepository
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import javax.inject.Inject
import javax.inject.Singleton

private const val PREFS_NAME = "hampouch_notification_settings"
private const val ACCOUNT_MARKETING_PREFIX = "account_marketing_"
private const val PENDING_MARKETING_PREFIX = "pending_marketing_"

@Singleton
class NotificationSettingsRepositoryImpl @Inject constructor(
    @ApplicationContext context: Context
) :
    NotificationSettingsRepository, AccountScopedState {

    private val _state = MutableStateFlow(NotificationSettingsState())
    override val state: StateFlow<NotificationSettingsState> = _state.asStateFlow()
    private val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
    private var activeAccountMarketingKey: String? = null

    override fun update(transform: (NotificationSettingsState) -> NotificationSettingsState) {
        _state.update(transform)
        activeAccountMarketingKey?.let { key ->
            prefs.edit().putBoolean(key, _state.value.marketingInformationEnabled).apply()
        }
    }

    override fun reserveMarketingConsent(email: String, enabled: Boolean) {
        prefs.edit().putBoolean(pendingMarketingKey(email), enabled).apply()
    }

    override fun activateAccount(accountId: String, email: String?) {
        val accountKey = "$ACCOUNT_MARKETING_PREFIX$accountId"
        activeAccountMarketingKey = accountKey
        val pendingKey = email?.takeIf { it.isNotBlank() }?.let(::pendingMarketingKey)
        val hasPendingConsent = pendingKey != null && prefs.contains(pendingKey)
        val enabled = if (hasPendingConsent) {
            prefs.getBoolean(pendingKey, false)
        } else {
            prefs.getBoolean(accountKey, false)
        }
        if (hasPendingConsent && pendingKey != null) {
            prefs.edit()
                .putBoolean(accountKey, enabled)
                .remove(pendingKey)
                .apply()
        }
        _state.update { it.copy(marketingInformationEnabled = enabled) }
    }

    override fun resetForAccount() {
        activeAccountMarketingKey = null
        _state.value = NotificationSettingsState()
    }

    private fun pendingMarketingKey(email: String): String =
        "$PENDING_MARKETING_PREFIX${email.trim().lowercase()}"
}
