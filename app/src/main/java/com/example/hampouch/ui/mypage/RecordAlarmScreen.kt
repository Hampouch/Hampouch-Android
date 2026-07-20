package com.example.hampouch.ui.mypage

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Alarm
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.hampouch.R
import com.example.hampouch.ui.mypage.components.DayOfWeekChipsRow
import com.example.hampouch.ui.mypage.components.MyPageDetailTopBar
import com.example.hampouch.ui.mypage.components.ReminderModeSegmentedRow
import com.example.hampouch.ui.mypage.components.SectionLabel
import com.example.hampouch.ui.mypage.components.SettingsToggleCard
import com.example.hampouch.ui.theme.HPBlack
import com.example.hampouch.ui.theme.HPGray2
import com.example.hampouch.ui.theme.HPMain
import com.example.hampouch.ui.theme.HPSub4
import com.example.hampouch.ui.theme.HPWhite
import com.example.hampouch.ui.theme.HampouchTheme

private fun daysForMode(mode: ReminderDayMode, current: Set<DayOfWeekLabel>): Set<DayOfWeekLabel> = when (mode) {
    ReminderDayMode.DAILY -> DayOfWeekLabel.entries.toSet()
    ReminderDayMode.WEEKEND_ONLY -> setOf(DayOfWeekLabel.SAT, DayOfWeekLabel.SUN)
    ReminderDayMode.CUSTOM -> current
}

@Composable
fun RecordAlarmScreen(
    onBackClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val timeValue = stringResource(R.string.record_alarm_time_value)
    var state by remember { mutableStateOf(RecordAlarmSettingsState(timeLabel = timeValue)) }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(HPGray2)
    ) {
        MyPageDetailTopBar(
            title = stringResource(R.string.record_alarm_title),
            onBackClick = onBackClick,
            modifier = Modifier.padding(horizontal = 8.dp)
        )
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp)
        ) {
            SettingsToggleCard(
                title = stringResource(R.string.record_alarm_receive_title),
                subtitle = stringResource(R.string.record_alarm_receive_subtitle),
                checked = state.receiveEnabled,
                onCheckedChange = { state = state.copy(receiveEnabled = it) }
            )

            Spacer(modifier = Modifier.height(24.dp))
            SectionLabel(text = stringResource(R.string.record_alarm_type_label))
            Spacer(modifier = Modifier.height(8.dp))
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                SettingsToggleCard(
                    title = stringResource(R.string.record_alarm_missing_reminder_title),
                    subtitle = stringResource(R.string.record_alarm_missing_reminder_subtitle),
                    checked = state.missingReminderEnabled,
                    onCheckedChange = { state = state.copy(missingReminderEnabled = it) }
                )
                SettingsToggleCard(
                    title = stringResource(R.string.record_alarm_limit_over_title),
                    subtitle = stringResource(R.string.record_alarm_limit_over_subtitle),
                    checked = state.limitOverEnabled,
                    onCheckedChange = { state = state.copy(limitOverEnabled = it) }
                )
            }

            Spacer(modifier = Modifier.height(24.dp))
            SectionLabel(text = stringResource(R.string.record_alarm_days_label))
            Spacer(modifier = Modifier.height(8.dp))
            ReminderModeSegmentedRow(
                selectedMode = state.dayMode,
                onModeSelected = { mode ->
                    state = state.copy(dayMode = mode, selectedDays = daysForMode(mode, state.selectedDays))
                }
            )
            Spacer(modifier = Modifier.height(10.dp))
            DayOfWeekChipsRow(
                selectedDays = state.selectedDays,
                onDayToggle = { day ->
                    val updated = if (day in state.selectedDays) state.selectedDays - day else state.selectedDays + day
                    state = state.copy(dayMode = ReminderDayMode.CUSTOM, selectedDays = updated)
                }
            )

            Spacer(modifier = Modifier.height(10.dp))
            SettingsToggleCard(
                title = stringResource(R.string.record_alarm_receive_title),
                subtitle = stringResource(R.string.record_alarm_receive_subtitle),
                checked = state.secondReceiveEnabled,
                onCheckedChange = { state = state.copy(secondReceiveEnabled = it) }
            )

            Spacer(modifier = Modifier.height(24.dp))
            SectionLabel(text = stringResource(R.string.record_alarm_time_label))
            Spacer(modifier = Modifier.height(8.dp))
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(16.dp))
                    .background(HPWhite)
                    .padding(horizontal = 16.dp, vertical = 16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = stringResource(R.string.record_alarm_time_label),
                    style = MaterialTheme.typography.bodyMedium,
                    color = HPBlack,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.weight(1f))
                Text(text = state.timeLabel, style = MaterialTheme.typography.bodyMedium, color = HPMain)
            }
            Spacer(modifier = Modifier.height(10.dp))
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(16.dp))
                    .background(HPSub4)
                    .border(1.dp, HPMain.copy(alpha = 0.3f), RoundedCornerShape(16.dp))
                    .padding(horizontal = 16.dp, vertical = 14.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Filled.Alarm,
                    contentDescription = null,
                    tint = HPBlack,
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = stringResource(R.string.record_alarm_summary_format, state.timeLabel),
                    style = MaterialTheme.typography.bodyMedium,
                    color = HPBlack,
                    fontWeight = FontWeight.Bold
                )
            }
            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}

@Preview(showBackground = true, name = "3. 기록 알림")
@Composable
private fun RecordAlarmScreenPreview() {
    HampouchTheme {
        RecordAlarmScreen(onBackClick = {})
    }
}
