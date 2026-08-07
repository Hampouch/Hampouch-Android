package com.example.hampouch

import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.SystemBarStyle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import com.example.hampouch.navigation.AppNavHost
import com.example.hampouch.ui.notification.EXTRA_NOTIFICATION_ID
import com.example.hampouch.ui.theme.HampouchTheme

class MainActivity : ComponentActivity() {
    private var pendingNotificationId by mutableStateOf<String?>(null)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge(
            statusBarStyle = SystemBarStyle.light(Color.TRANSPARENT, Color.TRANSPARENT)
        )
        pendingNotificationId = intent?.getStringExtra(EXTRA_NOTIFICATION_ID)
        setContent {
            HampouchTheme {
                AppNavHost(
                    modifier = Modifier.fillMaxSize(),
                    pendingNotificationId = pendingNotificationId,
                    onPendingNotificationConsumed = { pendingNotificationId = null }
                )
            }
        }
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        setIntent(intent)
        pendingNotificationId = intent.getStringExtra(EXTRA_NOTIFICATION_ID)
    }
}
