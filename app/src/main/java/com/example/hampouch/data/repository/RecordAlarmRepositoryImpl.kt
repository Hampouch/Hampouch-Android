package com.example.hampouch.data.repository

import android.content.Context
import com.example.hampouch.domain.model.RecordAlarmSettingsState
import com.example.hampouch.domain.repository.AccountScopedState
import com.example.hampouch.domain.repository.RecordAlarmRepository
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import java.time.LocalDate
import javax.inject.Inject
import javax.inject.Singleton

private const val PREFS_NAME = "hampouch_record_alarm"
private const val KEY_DISMISSED_DATE_EPOCH_DAY = "dismissed_date_epoch_day"

@Singleton
class RecordAlarmRepositoryImpl @Inject constructor(
    @ApplicationContext private val context: Context
) : RecordAlarmRepository, AccountScopedState {

    private fun prefs() =
        context.applicationContext.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

    private val _state = MutableStateFlow(RecordAlarmSettingsState())
    override val state: StateFlow<RecordAlarmSettingsState> = _state.asStateFlow()

    private val _dismissedDate = MutableStateFlow(
        prefs().takeIf { it.contains(KEY_DISMISSED_DATE_EPOCH_DAY) }
            ?.let { LocalDate.ofEpochDay(it.getLong(KEY_DISMISSED_DATE_EPOCH_DAY, 0L)) }
    )
    override val dismissedDate: StateFlow<LocalDate?> = _dismissedDate.asStateFlow()

    override fun update(transform: (RecordAlarmSettingsState) -> RecordAlarmSettingsState) {
        _state.update(transform)
    }

    override fun dismissForToday(referenceToday: LocalDate) {
        _dismissedDate.value = referenceToday
        prefs().edit().putLong(KEY_DISMISSED_DATE_EPOCH_DAY, referenceToday.toEpochDay()).apply()
    }

    override fun clearDismissal() {
        _dismissedDate.value = null
        prefs().edit().remove(KEY_DISMISSED_DATE_EPOCH_DAY).apply()
    }

    override fun resetForAccount() {
        _state.value = RecordAlarmSettingsState()
        _dismissedDate.value = null
        prefs().edit().remove(KEY_DISMISSED_DATE_EPOCH_DAY).apply()
    }
}
