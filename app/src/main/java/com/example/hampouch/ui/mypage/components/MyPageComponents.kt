package com.example.hampouch.ui.mypage.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.example.hampouch.R
import com.example.hampouch.ui.common.NotificationBellIcon
import androidx.compose.ui.text.style.TextOverflow
import com.example.hampouch.ui.hamtips.components.rememberImageBitmapFromUri
import com.example.hampouch.domain.model.ChallengeRecord
import com.example.hampouch.domain.model.ChallengeStatus
import com.example.hampouch.domain.model.DayOfWeekLabel
import com.example.hampouch.domain.model.ReminderDayMode
import com.example.hampouch.domain.model.TipCategory
import com.example.hampouch.domain.model.TipPost
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
import com.example.hampouch.ui.theme.HPTipDiscountBg
import com.example.hampouch.ui.theme.HPTipDiscountText
import com.example.hampouch.ui.theme.HPTipEtcBg
import com.example.hampouch.ui.theme.HPTipEtcText
import com.example.hampouch.ui.theme.HPTipRecordBg
import com.example.hampouch.ui.theme.HPTipRecordText
import com.example.hampouch.ui.theme.HPTipRecruitBg
import com.example.hampouch.ui.theme.HPTipRecruitText
import com.example.hampouch.ui.theme.HPWhite

internal fun formatWon(amount: Int): String = "%,d".format(amount)

@Composable
fun MyPageMainTopBar(
    title: String,
    onBackClick: () -> Unit,
    onNotificationClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .height(64.dp),
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
            fontWeight = FontWeight.Normal,
            textAlign = TextAlign.Center,
            modifier = Modifier.weight(1f)
        )
        NotificationBellIcon(onClick = onNotificationClick)
    }
}

@Composable
fun ProfileAvatar(avatarUri: String?, modifier: Modifier = Modifier, size: androidx.compose.ui.unit.Dp = 80.dp) {
    Box(
        modifier = modifier
            .size(size)
            .clip(CircleShape)
            .background(HPWhite),
        contentAlignment = Alignment.Center
    ) {
        val bitmap = avatarUri?.let { rememberImageBitmapFromUri(it) }
        if (bitmap != null) {
            Image(
                bitmap = bitmap,
                contentDescription = stringResource(R.string.mypage_cd_profile_image),
                contentScale = ContentScale.Crop,
                modifier = Modifier.fillMaxSize()
            )
        } else {
            Icon(
                imageVector = Icons.Filled.Person,
                contentDescription = stringResource(R.string.mypage_cd_profile_image),
                tint = HPGray5,
                modifier = Modifier.size(size / 2)
            )
        }
    }
}

@Composable
fun ProfileCard(
    name: String,
    handle: String,
    avatarUri: String?,
    modifier: Modifier = Modifier,
    onClick: (() -> Unit)? = null
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(20.dp))
            .background(HPSub4)
            .then(if (onClick != null) Modifier.clickable(onClick = onClick) else Modifier)
            .padding(vertical = 20.dp, horizontal = 20.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        ProfileAvatar(avatarUri = avatarUri, size = 56.dp)
        Spacer(modifier = Modifier.width(14.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = name,
                style = MaterialTheme.typography.titleSmall,
                color = HPBlack,
                fontWeight = FontWeight.Normal,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = stringResource(R.string.mypage_handle_format, handle),
                style = MaterialTheme.typography.bodySmall,
                color = HPText
            )
        }
        if (onClick != null) {
            Icon(
                imageVector = Icons.Filled.ChevronRight,
                contentDescription = stringResource(R.string.mypage_cd_go_detail),
                tint = HPText
            )
        }
    }
}

@Composable
fun MyPageMenuRow(label: String, onClick: () -> Unit, modifier: Modifier = Modifier) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(HPWhite)
            .clickable(onClick = onClick)
            .padding(horizontal = 16.dp, vertical = 18.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodyMedium,
            color = HPBlack,
            modifier = Modifier.weight(1f)
        )
        Icon(imageVector = Icons.Filled.ChevronRight, contentDescription = null, tint = HPText)
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
            Text(text = title, style = MaterialTheme.typography.bodyMedium, color = HPBlack)
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
fun SettingsNavigateCard(
    title: String,
    subtitle: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(HPWhite)
            .clickable(onClick = onClick)
            .padding(horizontal = 16.dp, vertical = 14.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(text = title, style = MaterialTheme.typography.bodyMedium, color = HPBlack)
            Spacer(modifier = Modifier.height(2.dp))
            Text(text = subtitle, style = MaterialTheme.typography.bodySmall, color = HPText)
        }
        Icon(imageVector = Icons.Filled.ChevronRight, contentDescription = null, tint = HPText)
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
    labelColor: Color = HPBlack,
    showChevron: Boolean = true
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(horizontal = 16.dp, vertical = 16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodyMedium,
            color = labelColor,
            modifier = Modifier.weight(1f)
        )
        if (showChevron) {
            Icon(imageVector = Icons.Filled.ChevronRight, contentDescription = null, tint = HPText)
        }
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
        fontWeight = FontWeight.Normal,
        modifier = modifier.padding(start = 4.dp)
    )
}

@Composable
fun StatusBadge(status: ChallengeStatus, modifier: Modifier = Modifier) {
    val (bg, textColor) = when (status) {
        ChallengeStatus.IN_PROGRESS -> HPTipRecruitBg to HPTipRecruitText
        ChallengeStatus.SUCCESS -> HPStatusInProgressBg to HPStatusInProgressText
        ChallengeStatus.FAIL -> HPStatusSuccessBg to HPStatusSuccessText
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
            fontWeight = FontWeight.Normal
        )
    }
}

@Composable
fun ChallengeRecordCard(record: ChallengeRecord, onClick: () -> Unit = {}, modifier: Modifier = Modifier) {
    val isSuccess = record.status == ChallengeStatus.SUCCESS
    val progressColor = if (isSuccess) HPStatusInProgressText else HPStatusSuccessText
    val progressFraction = if (record.targetAmount > 0) {
        (record.actualAmount.toFloat() / record.targetAmount.toFloat()).coerceIn(0f, 1f)
    } else {
        0f
    }

    Column(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(20.dp))
            .background(HPWhite)
            .clickable(onClick = onClick)
            .padding(20.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.Top
        ) {
            Column {
                Text(
                    text = stringResource(R.string.challenge_history_card_title_format, record.totalDays),
                    style = MaterialTheme.typography.bodyMedium,
                    color = HPBlack,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = if (record.endDateLabel != null) {
                        stringResource(
                            R.string.challenge_history_period_format,
                            record.startDateLabel,
                            record.endDateLabel
                        )
                    } else {
                        stringResource(R.string.challenge_history_period_ongoing_format, record.startDateLabel)
                    },
                    style = MaterialTheme.typography.bodySmall,
                    color = HPText
                )
            }
            StatusBadge(status = record.status)
        }

        Spacer(modifier = Modifier.height(16.dp))
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = stringResource(R.string.challenge_history_target_label),
                    style = MaterialTheme.typography.bodySmall,
                    color = HPText
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = stringResource(R.string.challenge_history_amount_won_format, formatWon(record.targetAmount)),
                    style = MaterialTheme.typography.bodyMedium,
                    color = HPBlack,
                    fontWeight = FontWeight.Bold
                )
            }
            Icon(
                imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                contentDescription = null,
                tint = HPText,
                modifier = Modifier.size(20.dp)
            )
            Column(modifier = Modifier.weight(1f), horizontalAlignment = Alignment.End) {
                Text(
                    text = stringResource(R.string.challenge_history_actual_label),
                    style = MaterialTheme.typography.bodySmall,
                    color = HPText
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = stringResource(R.string.challenge_history_amount_won_format, formatWon(record.actualAmount)),
                    style = MaterialTheme.typography.bodyMedium,
                    color = HPMain,
                    fontWeight = FontWeight.Bold
                )
            }
        }

        Spacer(modifier = Modifier.height(12.dp))
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(8.dp)
                .clip(RoundedCornerShape(50))
                .background(HPGray4)
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth(progressFraction)
                    .fillMaxHeight()
                    .clip(RoundedCornerShape(50))
                    .background(progressColor)
            )
        }
    }
}

private val ReminderModeSegmentedOptions = listOf(
    ReminderDayMode.WEEKDAY,
    ReminderDayMode.WEEKEND,
    ReminderDayMode.DAILY
)

@Composable
fun ReminderModeSegmentedRow(
    selectedMode: ReminderDayMode,
    onModeSelected: (ReminderDayMode) -> Unit,
    modifier: Modifier = Modifier
) {
    Row(modifier = modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        ReminderModeSegmentedOptions.forEach { mode ->
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
                    text = stringResource(mode.labelResId),
                    style = MaterialTheme.typography.bodyMedium,
                    color = if (selected) HPWhite else HPText,
                    fontWeight = FontWeight.Normal
                )
            }
        }
    }
}

@Composable
fun TipCategoryBadge(category: TipCategory, modifier: Modifier = Modifier) {
    val (bg, textColor) = when (category) {
        TipCategory.WHAT_TO_EAT -> HPStatusSuccessBg to HPStatusSuccessText
        TipCategory.COOKING -> HPStatusInProgressBg to HPStatusInProgressText
        TipCategory.SHOPPING -> HPStatusFailBg to HPStatusFailText
        TipCategory.DISCOUNT -> HPTipDiscountBg to HPTipDiscountText
        TipCategory.RECRUIT -> HPTipRecruitBg to HPTipRecruitText
        TipCategory.RECORD -> HPTipRecordBg to HPTipRecordText
        TipCategory.ETC -> HPTipEtcBg to HPTipEtcText
    }
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(50))
            .background(bg)
            .padding(horizontal = 12.dp, vertical = 6.dp)
    ) {
        Text(
            text = stringResource(category.labelResId),
            style = MaterialTheme.typography.labelMedium,
            color = textColor,
            fontWeight = FontWeight.Normal
        )
    }
}

@Composable
fun TipPostCard(tip: TipPost, modifier: Modifier = Modifier, onClick: () -> Unit = {}) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(20.dp))
            .background(HPWhite)
            .border(1.dp, HPGray4, RoundedCornerShape(20.dp))
            .clickable(onClick = onClick)
            .padding(16.dp)
    ) {
        TipCategoryBadge(category = tip.category)
        Spacer(modifier = Modifier.height(10.dp))
        Text(
            text = tip.title,
            style = MaterialTheme.typography.bodyMedium,
            color = HPBlack,
            fontWeight = FontWeight.Normal,
            maxLines = 2,
            overflow = TextOverflow.Ellipsis
        )
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = tip.subtitle,
            style = MaterialTheme.typography.bodySmall,
            color = HPText,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
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
                    fontWeight = FontWeight.Normal
                )
            }
        }
    }
}
