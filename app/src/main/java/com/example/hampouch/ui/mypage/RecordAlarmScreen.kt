package com.example.hampouch.ui.mypage

import android.app.TimePickerDialog
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.hampouch.R
import com.example.hampouch.domain.model.DayOfWeekLabel
import com.example.hampouch.domain.model.RecordAlarmSettingsState
import com.example.hampouch.domain.model.ReminderDayMode
import com.example.hampouch.ui.mypage.components.DayOfWeekChipsRow
import com.example.hampouch.ui.mypage.components.MyPageMainTopBar
import com.example.hampouch.ui.mypage.components.ReminderModeSegmentedRow
import com.example.hampouch.ui.mypage.components.SettingsToggleCard
import com.example.hampouch.ui.theme.HPBlack
import com.example.hampouch.ui.theme.HPGray2
import com.example.hampouch.ui.theme.HPMain
import com.example.hampouch.ui.theme.HPSub4
import com.example.hampouch.ui.theme.HPWhite
import com.example.hampouch.ui.theme.HampouchTheme
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle

private fun daysForMode(mode: ReminderDayMode): Set<DayOfWeekLabel> = when (mode) {
    ReminderDayMode.WEEKDAY -> setOf(
        DayOfWeekLabel.MON, DayOfWeekLabel.TUE, DayOfWeekLabel.WED, DayOfWeekLabel.THU, DayOfWeekLabel.FRI
    )
    ReminderDayMode.WEEKEND -> setOf(DayOfWeekLabel.SAT, DayOfWeekLabel.SUN)
    ReminderDayMode.DAILY -> DayOfWeekLabel.entries.toSet()
    ReminderDayMode.CUSTOM -> emptySet()
}

private fun formatTime(hour: Int, minute: Int): String = "%02d:%02d".format(hour, minute)

data class RecordAlarmActions(
    val onBackClick: () -> Unit,
    val onNotificationClick: () -> Unit,
    val onUpdate: ((RecordAlarmSettingsState) -> RecordAlarmSettingsState) -> Unit
)

@Composable
fun RecordAlarmScreen(
    onBackClick: () -> Unit,
    modifier: Modifier = Modifier,
    onNotificationClick: () -> Unit,
    viewModel: RecordAlarmViewModel = hiltViewModel()
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    RecordAlarmContent(
        state = state,
        actions = RecordAlarmActions(
            onBackClick = onBackClick,
            onNotificationClick = onNotificationClick,
            onUpdate = viewModel::update
        ),
        modifier = modifier
    )
}

@Composable
private fun RecordAlarmContent(
    state: RecordAlarmSettingsState,
    actions: RecordAlarmActions,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val timePickerDialog = remember(state.hour, state.minute) {
        TimePickerDialog(
            context,
            R.style.Theme_Hampouch_SpinnerTimePicker,
            { _, hourOfDay, minute -> actions.onUpdate { it.copy(hour = hourOfDay, minute = minute) } },
            state.hour,
            state.minute,
            false
        )
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(HPGray2)
    ) {
        MyPageMainTopBar(
            title = stringResource(R.string.record_alarm_title),
            onBackClick = actions.onBackClick,
            onNotificationClick = actions.onNotificationClick
        )
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp)
        ) {
            RecordAlarmToggleSection(
                state = state,
                actions = actions,
                onTimeClick = { timePickerDialog.show() }
            )
            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}

@Composable
private fun RecordAlarmToggleSection(
    state: RecordAlarmSettingsState,
    actions: RecordAlarmActions,
    onTimeClick: () -> Unit
) {
    fun onMasterToggle(enabled: Boolean) {
        actions.onUpdate {
            it.copy(
                receiveEnabled = enabled,
                missingReminderEnabled = enabled,
                limitOverEnabled = enabled
            )
        }
    }

    fun onMissingReminderToggle(enabled: Boolean) {
        actions.onUpdate {
            it.copy(
                missingReminderEnabled = enabled,
                receiveEnabled = enabled && it.limitOverEnabled
            )
        }
    }

    fun onLimitOverToggle(enabled: Boolean) {
        actions.onUpdate {
            it.copy(
                limitOverEnabled = enabled,
                receiveEnabled = enabled && it.missingReminderEnabled
            )
        }
    }

    SettingsToggleCard(
        title = stringResource(R.string.record_alarm_receive_title),
        subtitle = stringResource(R.string.record_alarm_receive_subtitle),
        checked = state.receiveEnabled,
        onCheckedChange = ::onMasterToggle,
        onRowClick = { onMasterToggle(!state.receiveEnabled) }
    )

    Spacer(modifier = Modifier.height(16.dp))
    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(16.dp))
                .background(HPWhite)
        ) {
            SettingsToggleCard(
                title = stringResource(R.string.record_alarm_missing_reminder_title),
                subtitle = stringResource(R.string.record_alarm_missing_reminder_subtitle),
                checked = state.missingReminderEnabled,
                onCheckedChange = ::onMissingReminderToggle,
                onRowClick = { onMissingReminderToggle(!state.missingReminderEnabled) }
            )
            if (state.missingReminderEnabled) {
                RecordAlarmMissingReminderDetail(
                    state = state,
                    onUpdate = actions.onUpdate,
                    onTimeClick = onTimeClick
                )
            }
        }
        SettingsToggleCard(
            title = stringResource(R.string.record_alarm_limit_over_title),
            subtitle = stringResource(R.string.record_alarm_limit_over_subtitle),
            checked = state.limitOverEnabled,
            onCheckedChange = ::onLimitOverToggle,
            onRowClick = { onLimitOverToggle(!state.limitOverEnabled) }
        )
    }
}

@Composable
private fun RecordAlarmMissingReminderDetail(
    state: RecordAlarmSettingsState,
    onUpdate: ((RecordAlarmSettingsState) -> RecordAlarmSettingsState) -> Unit,
    onTimeClick: () -> Unit
) {
    Column(modifier = Modifier.padding(horizontal = 16.dp, vertical = 14.dp)) {
        ReminderModeSegmentedRow(
            selectedMode = state.dayMode,
            onModeSelected = { mode ->
                onUpdate { it.copy(dayMode = mode, selectedDays = daysForMode(mode)) }
            }
        )
        Spacer(modifier = Modifier.height(10.dp))
        DayOfWeekChipsRow(
            selectedDays = state.selectedDays,
            onDayToggle = { day ->
                val updated = if (day in state.selectedDays) {
                    state.selectedDays - day
                } else {
                    state.selectedDays + day
                }
                onUpdate { it.copy(dayMode = ReminderDayMode.CUSTOM, selectedDays = updated) }
            }
        )
        Spacer(modifier = Modifier.height(14.dp))
        RecordAlarmTimeRow(state = state, onTimeClick = onTimeClick)
    }
}

@Composable
private fun RecordAlarmTimeRow(state: RecordAlarmSettingsState, onTimeClick: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onTimeClick)
            .padding(vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = stringResource(R.string.record_alarm_time_label),
            style = MaterialTheme.typography.bodyMedium,
            color = HPBlack
        )
        Spacer(modifier = Modifier.weight(1f))
        Text(
            text = formatTime(state.hour, state.minute),
            style = MaterialTheme.typography.bodyMedium,
            color = HPMain
        )
    }
    Spacer(modifier = Modifier.height(14.dp))
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(HPSub4)
            .padding(horizontal = 14.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = stringResource(
                R.string.record_alarm_summary_format,
                stringResource(state.dayMode.labelResId),
                formatTime(state.hour, state.minute)
            ),
            style = MaterialTheme.typography.bodySmall,
            color = HPBlack
        )
    }
}

@Preview(showBackground = true, name = "4. 기록 알림")
@Composable
private fun RecordAlarmScreenPreview() {
    HampouchTheme {
        RecordAlarmContent(
            state = RecordAlarmSettingsState(),
            actions = RecordAlarmActions(onBackClick = {}, onNotificationClick = {}, onUpdate = {})
        )
    }
}
