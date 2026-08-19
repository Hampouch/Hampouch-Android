package com.example.hampouch.ui.notification.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.hampouch.R
import com.example.hampouch.domain.model.NotificationCategory
import com.example.hampouch.domain.model.NotificationItem
import com.example.hampouch.domain.model.NotificationSection
import com.example.hampouch.domain.model.NotificationTarget
import com.example.hampouch.ui.common.ScreenCenteredTopBar
import com.example.hampouch.ui.theme.HPBlack
import com.example.hampouch.ui.theme.HPMain
import com.example.hampouch.ui.theme.HPSub3
import com.example.hampouch.ui.theme.HPSub4
import com.example.hampouch.ui.theme.HPText
import com.example.hampouch.ui.theme.HPTipDiscountText
import com.example.hampouch.ui.theme.HPTipEtcText
import com.example.hampouch.ui.theme.HampouchTheme
import java.time.LocalDate

@Composable
fun NotificationTopBar(
    onBackClick: () -> Unit,
    onMarkAllReadClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    ScreenCenteredTopBar(
        modifier = modifier,
        leading = {
            IconButton(onClick = onBackClick) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = stringResource(R.string.cd_back),
                    tint = HPBlack
                )
            }
        },
        trailing = {
            TextButton(
                onClick = onMarkAllReadClick,
                colors = ButtonDefaults.textButtonColors(contentColor = HPMain)
            ) {
                Text(
                    text = stringResource(R.string.notification_mark_all_read),
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Bold,
                    color = HPMain
                )
            }
        }
    ) {
        Text(
            text = stringResource(R.string.notification_title),
            style = MaterialTheme.typography.titleSmall,
            color = HPBlack,
            fontWeight = FontWeight.Normal,
            textAlign = TextAlign.Center
        )
    }
}

@Preview(showBackground = true, name = "알림 상단바")
@Composable
private fun NotificationTopBarPreview() {
    HampouchTheme {
        NotificationTopBar(onBackClick = {}, onMarkAllReadClick = {})
    }
}

private fun categoryColor(category: NotificationCategory): Color = when (category) {
    NotificationCategory.CHALLENGE -> HPMain
    NotificationCategory.HAM_BATTLE -> HPTipDiscountText
    NotificationCategory.COMMUNITY -> HPTipEtcText
}

@Composable
fun NotificationListItem(
    item: NotificationItem,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .then(if (!item.isRead) Modifier.background(HPSub3) else Modifier)
            .clickable(onClick = onClick)
            .padding(16.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = stringResource(item.category.labelResId),
                style = MaterialTheme.typography.labelMedium,
                color = categoryColor(item.category),
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.weight(1f))
            Text(
                text = item.timeLabel,
                style = MaterialTheme.typography.labelSmall,
                color = HPText
            )
        }
        Spacer(modifier = Modifier.height(6.dp))
        Text(
            text = item.title,
            style = MaterialTheme.typography.bodyMedium,
            color = HPBlack,
            fontWeight = FontWeight.Bold
        )
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = item.message,
            style = MaterialTheme.typography.bodySmall,
            color = HPText
        )
    }
}

@Preview(showBackground = true, name = "알림 리스트 아이템")
@Composable
private fun NotificationListItemPreview() {
    HampouchTheme {
        NotificationListItem(
            item = NotificationItem(
                id = "n1",
                category = NotificationCategory.CHALLENGE,
                section = NotificationSection.TODAY,
                title = "오늘의 지출 한도를 초과했어요",
                message = "식비 챌린지 예산을 확인해보세요",
                timeLabel = "5분 전",
                isRead = false,
                createdDate = LocalDate.now(),
                target = NotificationTarget.Home
            ),
            onClick = {},
            modifier = Modifier.padding(16.dp)
        )
    }
}
