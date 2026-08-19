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
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
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
import com.example.hampouch.ui.mypage.components.SectionLabel
import com.example.hampouch.ui.mypage.components.SettingsMenuCard
import com.example.hampouch.ui.mypage.components.SettingsMenuDivider
import com.example.hampouch.ui.mypage.components.SettingsMenuRow
import com.example.hampouch.ui.mypage.components.SettingsToggleCard
import com.example.hampouch.ui.theme.HPBlack
import com.example.hampouch.ui.theme.HPGray2
import com.example.hampouch.ui.theme.HPMain
import com.example.hampouch.ui.theme.HPSub3
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

@Composable
fun AllSettingsScreen(
    onBackClick: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: AllSettingsViewModel = hiltViewModel()
) {
    val recordAlarmState by viewModel.recordAlarmState.collectAsStateWithLifecycle()
    AllSettingsContent(
        recordAlarmState = recordAlarmState,
        actions = AllSettingsActions(
            onBackClick = onBackClick,
            onUpdateRecordAlarm = viewModel::updateRecordAlarm
        ),
        modifier = modifier
    )
}

@Composable
private fun AllSettingsContent(
    recordAlarmState: RecordAlarmSettingsState,
    actions: AllSettingsActions,
    modifier: Modifier = Modifier
) {
    var showComingSoonDialog by remember { mutableStateOf(false) }
    var marketingAlarmEnabled by remember { mutableStateOf(false) }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(HPGray2)
    ) {
        MyPageMainTopBar(
            title = stringResource(R.string.settings_title),
            onBackClick = actions.onBackClick
        )
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp)
        ) {
            AllSettingsNotificationSection(
                recordAlarmState = recordAlarmState,
                actions = actions,
                marketingAlarmEnabled = marketingAlarmEnabled,
                onMarketingAlarmToggle = {
                    marketingAlarmEnabled = true
                    showComingSoonDialog = true
                }
            )
            Spacer(modifier = Modifier.height(24.dp))
            AllSettingsDataAndSupportSection(onComingSoonClick = { showComingSoonDialog = true })
            Spacer(modifier = Modifier.height(24.dp))
        }
    }

    if (showComingSoonDialog) {
        AlertDialog(
            onDismissRequest = {
                showComingSoonDialog = false
                marketingAlarmEnabled = false
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        showComingSoonDialog = false
                        marketingAlarmEnabled = false
                    }
                ) {
                    Text(stringResource(R.string.common_confirm))
                }
            },
            text = { Text(stringResource(R.string.settings_coming_soon_message)) }
        )
    }
}

@Composable
private fun AllSettingsNotificationSection(
    recordAlarmState: RecordAlarmSettingsState,
    actions: AllSettingsActions,
    marketingAlarmEnabled: Boolean,
    onMarketingAlarmToggle: () -> Unit
) {
    val context = LocalContext.current
    val timePickerDialog = remember(recordAlarmState.hour, recordAlarmState.minute) {
        TimePickerDialog(
            context,
            R.style.Theme_Hampouch_SpinnerTimePicker,
            { _, hourOfDay, minute -> actions.onUpdateRecordAlarm { it.copy(hour = hourOfDay, minute = minute) } },
            recordAlarmState.hour,
            recordAlarmState.minute,
            false
        )
    }

    fun onMissingReminderToggle(enabled: Boolean) {
        actions.onUpdateRecordAlarm { it.copy(missingReminderEnabled = enabled) }
    }

    SectionLabel(text = stringResource(R.string.settings_section_notification))
    Spacer(modifier = Modifier.height(8.dp))
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
                checked = recordAlarmState.missingReminderEnabled,
                onCheckedChange = ::onMissingReminderToggle,
                onRowClick = { onMissingReminderToggle(!recordAlarmState.missingReminderEnabled) }
            )
            if (recordAlarmState.missingReminderEnabled) {
                RecordAlarmMissingReminderDetail(
                    state = recordAlarmState,
                    onUpdate = actions.onUpdateRecordAlarm,
                    onTimeClick = { timePickerDialog.show() }
                )
            }
        }
        SettingsToggleCard(
            title = stringResource(R.string.settings_marketing_alarm_title),
            subtitle = stringResource(R.string.settings_marketing_alarm_subtitle),
            checked = marketingAlarmEnabled,
            onCheckedChange = { checked -> if (checked) onMarketingAlarmToggle() },
            onRowClick = { if (!marketingAlarmEnabled) onMarketingAlarmToggle() }
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
            .background(HPSub3)
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

@Composable
private fun AllSettingsDataAndSupportSection(onComingSoonClick: () -> Unit) {
    SectionLabel(text = stringResource(R.string.settings_section_data))
    Spacer(modifier = Modifier.height(8.dp))
    SettingsMenuCard {
        SettingsMenuRow(
            label = stringResource(R.string.settings_export_expense),
            onClick = onComingSoonClick,
            showChevron = false
        )
    }

    Spacer(modifier = Modifier.height(24.dp))
    SectionLabel(text = stringResource(R.string.settings_section_support))
    Spacer(modifier = Modifier.height(8.dp))
    SettingsMenuCard {
        SettingsMenuRow(
            label = stringResource(R.string.settings_customer_center),
            onClick = onComingSoonClick,
            showChevron = false
        )
        SettingsMenuDivider()
        SettingsMenuRow(
            label = stringResource(R.string.settings_terms),
            onClick = onComingSoonClick,
            showChevron = false
        )
    }
}

@Preview(showBackground = true, name = "3. 전체 설정")
@Composable
private fun AllSettingsScreenPreview() {
    HampouchTheme {
        AllSettingsContent(
            recordAlarmState = RecordAlarmSettingsState(),
            actions = AllSettingsActions(
                onBackClick = {},
                onUpdateRecordAlarm = {}
            )
        )
    }
}
