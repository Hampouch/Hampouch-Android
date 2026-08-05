package com.example.hampouch.ui.notification

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import com.example.hampouch.R
import com.example.hampouch.data.model.NotificationItem
import com.example.hampouch.data.model.NotificationSection
import com.example.hampouch.ui.mypage.components.SectionLabel
import com.example.hampouch.ui.notification.components.NotificationListItem
import com.example.hampouch.ui.notification.components.NotificationTopBar
import com.example.hampouch.ui.theme.HPBlack
import com.example.hampouch.ui.theme.HPGray2
import com.example.hampouch.ui.theme.HPMain
import com.example.hampouch.ui.theme.HPSub3
import com.example.hampouch.ui.theme.HPText
import com.example.hampouch.ui.theme.HPWhite
import com.example.hampouch.ui.theme.HampouchTheme

@Composable
fun NotificationScreen(
    notifications: List<NotificationItem>,
    onBackClick: () -> Unit,
    modifier: Modifier = Modifier,
    onMarkAllReadClick: () -> Unit = {},
    onNotificationClick: (NotificationItem) -> Unit = {}
) {
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
        // TODO: 실제 알림(FCM) 연동 후 제거 - 잠금화면 알림 팝업 디자인 실기기 테스트용 임시 버튼.
        TestNotificationTriggerButton(modifier = Modifier.padding(horizontal = 20.dp))
        Spacer(modifier = Modifier.height(12.dp))
        if (notifications.isEmpty()) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text(
                    text = stringResource(R.string.notification_empty_message),
                    style = MaterialTheme.typography.bodyMedium,
                    color = HPText
                )
            }
        } else {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = 20.dp)
            ) {
                NotificationSection.entries.forEach { section ->
                    val items = notifications.filter { it.section == section }
                    if (items.isNotEmpty()) {
                        SectionLabel(
                            text = stringResource(section.labelResId),
                            modifier = Modifier.padding(top = 16.dp, bottom = 8.dp)
                        )
                        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            items.forEach { item ->
                                NotificationListItem(
                                    item = item,
                                    onClick = { onNotificationClick(item) }
                                )
                            }
                        }
                    }
                }
                Spacer(modifier = Modifier.height(24.dp))
            }
        }
    }
}

// TODO: 실제 알림(FCM) 연동 후 제거 - 잠금화면 알림 팝업 디자인 실기기 테스트용 임시 버튼.
@Composable
private fun TestNotificationTriggerButton(modifier: Modifier = Modifier) {
    val context = LocalContext.current
    val permissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { granted -> if (granted) SystemNotificationSender.sendTestLockScreenNotification(context) }

    Button(
        onClick = {
            val permissionGranted = Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU ||
                ContextCompat.checkSelfPermission(
                    context,
                    Manifest.permission.POST_NOTIFICATIONS
                ) == PackageManager.PERMISSION_GRANTED
            if (permissionGranted) {
                SystemNotificationSender.sendTestLockScreenNotification(context)
            } else {
                permissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
            }
        },
        modifier = modifier
            .fillMaxWidth()
            .height(48.dp),
        shape = RoundedCornerShape(10.dp),
        colors = ButtonDefaults.buttonColors(containerColor = HPMain, contentColor = HPWhite)
    ) {
        Text(
            text = stringResource(R.string.notification_test_send_button),
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.Bold
        )
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
        NotificationScreen(notifications = NotificationMockData.empty(), onBackClick = {})
    }
}

@Preview(showBackground = true, name = "2. 알림 - 상황별 예시")
@Composable
private fun NotificationScreenFilledPreview() {
    HampouchTheme {
        NotificationScreen(notifications = NotificationMockData.populated(), onBackClick = {})
    }
}

@Preview(showBackground = true, name = "3. 잠금화면 알림 팝업 예시")
@Composable
private fun LockScreenNotificationMockupPreview() {
    HampouchTheme {
        LockScreenNotificationMockup()
    }
}
