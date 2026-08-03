package com.example.hampouch.ui.notification

import android.Manifest
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.content.pm.PackageManager
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import com.example.hampouch.R

private const val TEST_CHANNEL_ID = "hampouch_test_channel"
private const val TEST_NOTIFICATION_ID = 1001

// TODO: 실제 알림(FCM) 연동 후 제거 - 잠금화면 알림 팝업 디자인을 실기기에서 확인하기 위한 임시 목데이터 알림 발송기.
object SystemNotificationSender {

    private fun ensureChannel(context: Context) {
        val channel = NotificationChannel(
            TEST_CHANNEL_ID,
            context.getString(R.string.notification_channel_name),
            NotificationManager.IMPORTANCE_HIGH
        ).apply {
            description = context.getString(R.string.notification_channel_description)
        }
        val manager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        manager.createNotificationChannel(channel)
    }

    fun sendTestLockScreenNotification(context: Context) {
        if (ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.POST_NOTIFICATIONS
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            return
        }

        ensureChannel(context)

        val title = context.getString(R.string.notification_lock_preview_title)
        val body = context.getString(R.string.notification_lock_preview_body)

        val notification = NotificationCompat.Builder(context, TEST_CHANNEL_ID)
            .setSmallIcon(R.mipmap.ic_launcher)
            .setContentTitle(title)
            .setContentText(body)
            .setStyle(NotificationCompat.BigTextStyle().bigText(body))
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
            .setAutoCancel(true)
            .build()

        NotificationManagerCompat.from(context).notify(TEST_NOTIFICATION_ID, notification)
    }
}
