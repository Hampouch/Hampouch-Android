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
import androidx.compose.ui.unit.dp
import com.example.hampouch.R
import com.example.hampouch.data.model.NotificationCategory
import com.example.hampouch.data.model.NotificationItem
import com.example.hampouch.ui.theme.HPBlack
import com.example.hampouch.ui.theme.HPMain
import com.example.hampouch.ui.theme.HPSub4
import com.example.hampouch.ui.theme.HPText
import com.example.hampouch.ui.theme.HPTipDiscountText
import com.example.hampouch.ui.theme.HPTipEtcText

@Composable
fun NotificationTopBar(
    onBackClick: () -> Unit,
    onMarkAllReadClick: () -> Unit,
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
            text = stringResource(R.string.notification_title),
            style = MaterialTheme.typography.titleSmall,
            color = HPBlack,
            fontWeight = FontWeight.Normal,
            textAlign = TextAlign.Center,
            modifier = Modifier.weight(1f)
        )
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
    onClick: () -> Unit = {}
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .then(if (!item.isRead) Modifier.background(HPSub4) else Modifier)
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
