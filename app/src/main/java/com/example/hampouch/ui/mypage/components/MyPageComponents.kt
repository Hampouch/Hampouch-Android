package com.example.hampouch.ui.mypage.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.example.hampouch.R
import com.example.hampouch.data.model.ChallengeRecord
import com.example.hampouch.data.model.ChallengeStatus
import com.example.hampouch.data.model.DayOfWeekLabel
import com.example.hampouch.data.model.ReminderDayMode
import com.example.hampouch.ui.theme.HPBlack
import com.example.hampouch.ui.theme.HPGray3
import com.example.hampouch.ui.theme.HPGray4
import com.example.hampouch.ui.theme.HPGray5
import com.example.hampouch.ui.theme.HPMain
import com.example.hampouch.ui.theme.HPStatusFailBg
import com.example.hampouch.ui.theme.HPStatusFailText
import com.example.hampouch.ui.theme.HPStatusInProgressBg
import com.example.hampouch.ui.theme.HPStatusInProgressText
import com.example.hampouch.ui.theme.HPStatusSuccessBg
import com.example.hampouch.ui.theme.HPStatusSuccessText
import com.example.hampouch.ui.theme.HPSub4
import com.example.hampouch.ui.theme.HPText
import com.example.hampouch.ui.theme.HPWhite

internal fun formatWon(amount: Int): String = "%,d".format(amount)

@Composable
fun MyPageMainTopBar(
    title: String,
    onCalendarClick: () -> Unit,
    onNotificationClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(text = title, style = MaterialTheme.typography.titleSmall, color = HPBlack)
        Spacer(modifier = Modifier.weight(1f))
        IconButton(onClick = onCalendarClick) {
            Icon(
                imageVector = Icons.Filled.CalendarMonth,
                contentDescription = stringResource(R.string.cd_calendar),
                tint = HPBlack
            )
        }
        IconButton(onClick = onNotificationClick) {
            Icon(
                imageVector = Icons.Filled.Notifications,
                contentDescription = stringResource(R.string.cd_notification),
                tint = HPBlack
            )
        }
    }
}

@Composable
fun MyPageDetailTopBar(
    title: String,
    onBackClick: () -> Unit,
    showMoreMenu: Boolean = true,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .height(56.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        IconButton(onClick = onBackClick) {
            Icon(
                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                contentDescription = stringResource(R.string.cd_back),
                tint = HPBlack
            )
        }
        Text(
            text = title,
            style = MaterialTheme.typography.titleSmall,
            color = HPBlack,
            textAlign = TextAlign.Center,
            modifier = Modifier.weight(1f)
        )
        if (showMoreMenu) {
            IconButton(onClick = {}) {
                Icon(
                    imageVector = Icons.Filled.MoreVert,
                    contentDescription = stringResource(R.string.mypage_cd_more),
                    tint = HPBlack
                )
            }
        } else {
            Spacer(modifier = Modifier.width(48.dp))
        }
    }
}

@Composable
fun EditableConfirmField(
    value: String,
    onValueChange: (String) -> Unit,
    onConfirm: () -> Unit,
    modifier: Modifier = Modifier,
    textAlign: TextAlign = TextAlign.Start
) {
    val focusRequester = remember { FocusRequester() }
    LaunchedEffect(Unit) { focusRequester.requestFocus() }

    Row(
        modifier = modifier
            .clip(RoundedCornerShape(50))
            .background(HPWhite)
            .border(1.dp, HPGray5, RoundedCornerShape(50))
            .padding(start = 14.dp, end = 4.dp, top = 4.dp, bottom = 4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        val interactionSource = remember { MutableInteractionSource() }
        BasicTextField(
            value = value,
            onValueChange = onValueChange,
            modifier = Modifier
                .weight(1f, fill = false)
                .focusRequester(focusRequester),
            textStyle = MaterialTheme.typography.bodyMedium.copy(color = HPBlack, textAlign = textAlign),
            singleLine = true,
            interactionSource = interactionSource,
            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
            keyboardActions = KeyboardActions(onDone = { onConfirm() }),
            cursorBrush = SolidColor(HPMain)
        )
        Spacer(modifier = Modifier.width(8.dp))
        Box(
            modifier = Modifier
                .size(32.dp)
                .clip(CircleShape)
                .background(HPMain)
                .clickable(onClick = onConfirm),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Filled.Check,
                contentDescription = stringResource(R.string.mypage_cd_confirm),
                tint = HPWhite,
                modifier = Modifier.size(18.dp)
            )
        }
    }
}

@Composable
fun ProfileCard(
    name: String,
    handle: String,
    isEditingName: Boolean,
    editingName: String,
    onEditingNameChange: (String) -> Unit,
    onStartEditName: () -> Unit,
    onConfirmEditName: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(20.dp))
            .background(HPSub4)
            .padding(vertical = 28.dp, horizontal = 20.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Box(
            modifier = Modifier
                .size(80.dp)
                .clip(CircleShape)
                .background(HPWhite),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Filled.Person,
                contentDescription = stringResource(R.string.mypage_cd_profile_image),
                tint = HPGray5,
                modifier = Modifier.size(40.dp)
            )
        }
        Spacer(modifier = Modifier.height(12.dp))
        Text(text = name, style = MaterialTheme.typography.titleSmall, color = HPBlack)
        Spacer(modifier = Modifier.height(8.dp))
        if (isEditingName) {
            EditableConfirmField(
                value = editingName,
                onValueChange = onEditingNameChange,
                onConfirm = onConfirmEditName,
                textAlign = TextAlign.Center
            )
        } else {
            Row(
                modifier = Modifier
                    .clip(RoundedCornerShape(50))
                    .background(HPWhite.copy(alpha = 0.6f))
                    .clickable(onClick = onStartEditName)
                    .padding(horizontal = 14.dp, vertical = 6.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Filled.Edit,
                    contentDescription = null,
                    tint = HPText,
                    modifier = Modifier.size(14.dp)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = stringResource(R.string.mypage_name_change),
                    style = MaterialTheme.typography.labelLarge,
                    color = HPText
                )
            }
        }
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = stringResource(R.string.mypage_handle_format, handle),
            style = MaterialTheme.typography.bodySmall,
            color = HPText
        )
    }
}

@Composable
fun MyPageMenuRow(label: String, onClick: () -> Unit, modifier: Modifier = Modifier) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(HPWhite)
            .clickable(onClick = onClick)
            .padding(horizontal = 16.dp, vertical = 18.dp)
    ) {
        Text(text = label, style = MaterialTheme.typography.bodyMedium, color = HPBlack, fontWeight = FontWeight.Bold)
    }
}

@Composable
fun SettingsToggleCard(
    title: String,
    subtitle: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
    modifier: Modifier = Modifier,
    onRowClick: (() -> Unit)? = null
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(HPWhite)
            .padding(horizontal = 16.dp, vertical = 14.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(
            modifier = Modifier
                .weight(1f)
                .then(if (onRowClick != null) Modifier.clickable(onClick = onRowClick) else Modifier)
        ) {
            Text(text = title, style = MaterialTheme.typography.bodyMedium, color = HPBlack, fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.height(2.dp))
            Text(text = subtitle, style = MaterialTheme.typography.bodySmall, color = HPText)
        }
        Switch(
            checked = checked,
            onCheckedChange = onCheckedChange,
            colors = SwitchDefaults.colors(
                checkedThumbColor = HPWhite,
                checkedTrackColor = HPMain,
                uncheckedThumbColor = HPWhite,
                uncheckedTrackColor = HPGray5
            )
        )
    }
}

@Composable
fun SettingsMenuCard(modifier: Modifier = Modifier, content: @Composable () -> Unit) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(HPWhite)
    ) {
        content()
    }
}

@Composable
fun SettingsMenuRow(
    label: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    labelColor: Color = HPBlack
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(horizontal = 16.dp, vertical = 16.dp)
    ) {
        Text(text = label, style = MaterialTheme.typography.bodyMedium, color = labelColor)
    }
}

@Composable
fun SettingsMenuDivider(modifier: Modifier = Modifier) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(1.dp)
            .background(HPGray4)
    )
}

@Composable
fun SectionLabel(text: String, modifier: Modifier = Modifier) {
    Text(
        text = text,
        style = MaterialTheme.typography.labelMedium,
        color = HPText,
        modifier = modifier.padding(start = 4.dp)
    )
}

@Composable
fun StatusBadge(status: ChallengeStatus, modifier: Modifier = Modifier) {
    val (bg, textColor) = when (status) {
        ChallengeStatus.IN_PROGRESS -> HPStatusInProgressBg to HPStatusInProgressText
        ChallengeStatus.SUCCESS -> HPStatusSuccessBg to HPStatusSuccessText
        ChallengeStatus.FAIL -> HPStatusFailBg to HPStatusFailText
    }
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(50))
            .background(bg)
            .padding(horizontal = 12.dp, vertical = 6.dp)
    ) {
        Text(
            text = stringResource(status.labelResId),
            style = MaterialTheme.typography.labelMedium,
            color = textColor,
            fontWeight = FontWeight.Bold
        )
    }
}

@Composable
fun ChallengeRecordCard(record: ChallengeRecord, modifier: Modifier = Modifier) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(20.dp))
            .background(HPWhite)
            .border(1.dp, HPGray4, RoundedCornerShape(20.dp))
            .padding(16.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            StatusBadge(status = record.status)
            if (record.endDateLabel != null) {
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = stringResource(R.string.challenge_history_status_ended),
                    style = MaterialTheme.typography.bodySmall,
                    color = HPText
                )
            }
        }
        Spacer(modifier = Modifier.height(10.dp))
        Text(
            text = if (record.endDateLabel != null) {
                stringResource(
                    R.string.challenge_history_days_ended_format,
                    record.totalDays,
                    record.startDateLabel,
                    record.endDateLabel
                )
            } else {
                stringResource(R.string.challenge_history_days_ongoing_format, record.totalDays, record.startDateLabel)
            },
            style = MaterialTheme.typography.bodyMedium,
            color = HPBlack,
            fontWeight = FontWeight.Bold
        )
        Spacer(modifier = Modifier.height(10.dp))
        Box(
            modifier = Modifier
                .clip(RoundedCornerShape(50))
                .background(HPMain)
                .padding(horizontal = 14.dp, vertical = 6.dp)
        ) {
            Text(
                text = stringResource(R.string.challenge_history_progress_format, record.achievedDays, record.totalDays),
                style = MaterialTheme.typography.labelLarge,
                color = HPWhite,
                fontWeight = FontWeight.Bold
            )
        }
        Spacer(modifier = Modifier.height(10.dp))
        Row {
            StatChip(text = stringResource(R.string.challenge_history_daily_limit_format, formatWon(record.dailyLimit)))
            Spacer(modifier = Modifier.width(8.dp))
            StatChip(text = stringResource(R.string.challenge_history_total_saved_format, formatWon(record.totalSaved)))
        }
    }
}

@Composable
private fun StatChip(text: String, modifier: Modifier = Modifier) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(50))
            .background(HPGray3)
            .padding(horizontal = 12.dp, vertical = 6.dp)
    ) {
        Text(text = text, style = MaterialTheme.typography.labelMedium, color = HPText)
    }
}

@Composable
fun ReminderModeSegmentedRow(
    selectedMode: ReminderDayMode,
    onModeSelected: (ReminderDayMode) -> Unit,
    modifier: Modifier = Modifier
) {
    Row(modifier = modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        val modes = ReminderDayMode.entries
        val labels = listOf(
            stringResource(R.string.record_alarm_mode_daily),
            stringResource(R.string.record_alarm_mode_weekend),
            stringResource(R.string.record_alarm_mode_custom)
        )
        modes.forEachIndexed { index, mode ->
            val selected = mode == selectedMode
            Box(
                modifier = Modifier
                    .weight(1f)
                    .clip(RoundedCornerShape(12.dp))
                    .background(if (selected) HPMain else HPGray3)
                    .clickable { onModeSelected(mode) }
                    .padding(vertical = 12.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = labels[index],
                    style = MaterialTheme.typography.bodyMedium,
                    color = if (selected) HPWhite else HPText,
                    fontWeight = if (selected) FontWeight.Bold else FontWeight.Normal
                )
            }
        }
    }
}

@Composable
fun DayOfWeekChipsRow(
    selectedDays: Set<DayOfWeekLabel>,
    onDayToggle: (DayOfWeekLabel) -> Unit,
    modifier: Modifier = Modifier
) {
    Row(modifier = modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(6.dp)) {
        DayOfWeekLabel.entries.forEach { day ->
            val selected = day in selectedDays
            Box(
                modifier = Modifier
                    .weight(1f)
                    .clip(RoundedCornerShape(12.dp))
                    .background(if (selected) HPMain else HPSub4)
                    .clickable { onDayToggle(day) }
                    .padding(vertical = 10.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = stringResource(day.labelResId),
                    style = MaterialTheme.typography.bodyMedium,
                    color = if (selected) HPWhite else HPText,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}
