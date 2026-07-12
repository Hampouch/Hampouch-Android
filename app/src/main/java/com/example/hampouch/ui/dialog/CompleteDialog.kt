package com.example.hampouch.ui.dialog

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.hampouch.R
import com.example.hampouch.ui.theme.HPMain
import com.example.hampouch.ui.theme.HPSub3
import com.example.hampouch.ui.theme.HPText
import kotlinx.coroutines.delay

private const val CompleteDialogDurationMillis = 2000L

@Composable
fun CompleteDialog(
    message: String = "회원가입이 완료되었습니다.",
    onDismiss: () -> Unit
) {
    LaunchedEffect(Unit) {
        delay(CompleteDialogDurationMillis)
        onDismiss()
    }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(dismissOnBackPress = false, dismissOnClickOutside = false)
    ) {
        CompleteDialogCard(message = message)
    }
}

@Composable
private fun CompleteDialogCard(message: String) {
    Column(
        modifier = Modifier
            .width(372.dp)
            .height(263.dp)
            .clip(RoundedCornerShape(20.dp))
            .background(HPSub3),

        ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(top = 30.dp, bottom = 30.dp, start = 15.dp, end = 15.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Image(
                painter = painterResource(id = R.drawable.logo_app),
                contentDescription = "app_logo",
                modifier = Modifier.size(80.dp)
            )
            Spacer(modifier = Modifier.height(10.dp))
            Image(
                painter = painterResource(id = R.drawable.logo_title),
                contentDescription = "app_logo",
                modifier = Modifier
                    .width(212.dp)
                    .height(48.dp)
            )
            Spacer(modifier = Modifier.height(10.dp))
            Text(
                text = "포치와 함께 식비를 절약해봐요 :)",
                style = MaterialTheme.typography.bodyMedium,
                color = HPMain,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = message,
                textAlign = TextAlign.Center,
                color = HPText,
                style = MaterialTheme.typography.bodySmall
            )
        }
    }
}

@Preview
@Composable
private fun CompleteDialogCardPreview() {
    CompleteDialogCard(message = "회원가입이 완료되었습니다.")
}