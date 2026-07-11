package com.example.hampouch

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.ui.Modifier
import com.example.hampouch.navigation.AppNavHost
import com.example.hampouch.ui.theme.HampouchTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            HampouchTheme {
                AppNavHost(modifier = Modifier.fillMaxSize())
            }
        }
    }
}
