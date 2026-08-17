package com.example.hampouch.ui.notification

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.hampouch.R
import com.example.hampouch.domain.model.NotificationItem
import com.example.hampouch.domain.model.NotificationSection
import com.example.hampouch.ui.mypage.components.SectionLabel
import com.example.hampouch.ui.notification.components.NotificationListItem
import com.example.hampouch.ui.notification.components.NotificationTopBar
import com.example.hampouch.ui.theme.HPBlack
import com.example.hampouch.ui.theme.HPGray2
import com.example.hampouch.ui.theme.HPSub3
import com.example.hampouch.ui.theme.HPText
import com.example.hampouch.ui.theme.HPWhite
import com.example.hampouch.ui.theme.HampouchTheme

@Composable
fun NotificationScreen(
    notifications: List<NotificationItem>,
    onBackClick: () -> Unit,
    modifier: Modifier = Modifier,
    onMarkAllReadClick: () -> Unit,
    onNotificationClick: (NotificationItem) -> Unit
) {
    val notificationsBySection = remember(notifications) { notifications.groupBy { it.section } }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(HPGray2)
            .statusBarsPadding()
    ) {
        NotificationTopBar(
            onBackClick = onBackClick,
            onMarkAllReadClick = onMarkAllReadClick,
            modifier = Modifier.padding(horizontal = 8.dp)
        )
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 20.dp)
        ) {
            if (notifications.isEmpty()) {
                item(contentType = "empty_state") {
                    Box(modifier = Modifier.fillParentMaxSize(), contentAlignment = Alignment.Center) {
                        Text(
                            text = stringResource(R.string.notification_empty_message),
                            style = MaterialTheme.typography.bodyMedium,
                            color = HPText
                        )
                    }
                }
            } else {
                NotificationSection.entries.forEach { section ->
                    val sectionItems = notificationsBySection[section].orEmpty()
                    if (sectionItems.isNotEmpty()) {
                        item(key = "section:$section", contentType = "section_header") {
                            SectionLabel(
                                text = stringResource(section.labelResId),
                                modifier = Modifier.padding(top = 16.dp, bottom = 8.dp)
                            )
                        }
                        items(
                            items = sectionItems,
                            key = { it.id },
                            contentType = { "notification" }
                        ) { item ->
                            NotificationListItem(
                                item = item,
                                onClick = { onNotificationClick(item) }
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                        }
                    }
                }
                item(contentType = "footer") { Spacer(modifier = Modifier.height(16.dp)) }
            }
        }
    }
}

private val LockScreenBackdrop = Color(0xFF241C17)

@Composable
private fun LockScreenNotificationCard(
    title: String,
    body: String,
    timeLabel: String,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(20.dp))
            .background(HPWhite)
            .padding(16.dp),
        verticalAlignment = Alignment.Top
    ) {
        Box(
            modifier = Modifier
                .size(40.dp)
                .clip(CircleShape)
                .background(HPSub3),
            contentAlignment = Alignment.Center
        ) {
            Image(
                painter = painterResource(R.drawable.img_hamster_normal),
                contentDescription = stringResource(R.string.notification_cd_app_icon),
                contentScale = ContentScale.Crop,
                modifier = Modifier.size(40.dp)
            )
        }
        Spacer(modifier = Modifier.width(12.dp))
        Column(modifier = Modifier.weight(1f)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.bodyMedium,
                    color = HPBlack,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.weight(1f)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = timeLabel,
                    style = MaterialTheme.typography.labelSmall,
                    color = HPText
                )
            }
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = body,
                style = MaterialTheme.typography.bodySmall,
                color = HPText
            )
        }
    }
}

@Composable
private fun LockScreenNotificationMockup(modifier: Modifier = Modifier) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .background(LockScreenBackdrop)
            .padding(horizontal = 16.dp, vertical = 32.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        LockScreenNotificationCard(
            title = stringResource(R.string.notification_lock_preview_title),
            body = stringResource(R.string.notification_lock_preview_body),
            timeLabel = stringResource(R.string.notification_lock_preview_time_now)
        )
        LockScreenNotificationCard(
            title = stringResource(R.string.notification_lock_preview_placeholder_title),
            body = stringResource(R.string.notification_lock_preview_placeholder_body),
            timeLabel = stringResource(R.string.notification_lock_preview_placeholder_time)
        )
    }
}

@Preview(showBackground = true, name = "1. 알림 - 빈 상태")
@Composable
private fun NotificationScreenEmptyPreview() {
    HampouchTheme {
        NotificationScreen(notifications = NotificationMockData.empty(), onBackClick = {}, onMarkAllReadClick = {}, onNotificationClick = {})
    }
}

@Preview(showBackground = true, name = "2. 알림 - 상황별 예시")
@Composable
private fun NotificationScreenFilledPreview() {
    HampouchTheme {
        NotificationScreen(notifications = NotificationMockData.populated(), onBackClick = {}, onMarkAllReadClick = {}, onNotificationClick = {})
    }
}

@Preview(showBackground = true, name = "3. 잠금화면 알림 팝업 예시")
@Composable
private fun LockScreenNotificationMockupPreview() {
    HampouchTheme {
        LockScreenNotificationMockup()
    }
}
