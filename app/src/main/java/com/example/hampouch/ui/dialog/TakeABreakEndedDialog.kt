package com.example.hampouch.ui.dialog

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.hampouch.R
import com.example.hampouch.ui.theme.HPBlack
import com.example.hampouch.ui.theme.HPMain
import com.example.hampouch.ui.theme.HPSub4
import com.example.hampouch.ui.theme.HPText
import com.example.hampouch.ui.theme.HPWhite
import com.example.hampouch.ui.theme.HampouchTheme

@Composable
fun TakeABreakEndedDialog(
    onStartNowClick: () -> Unit,
    onStartTomorrowClick: () -> Unit,
    onRestMoreClick: () -> Unit
) {
    Dialog(
        onDismissRequest = {},
        properties = DialogProperties(usePlatformDefaultWidth = false, dismissOnBackPress = false, dismissOnClickOutside = false)
    ) {
        TakeABreakEndedDialogCard(
            onStartNowClick = onStartNowClick,
            onStartTomorrowClick = onStartTomorrowClick,
            onRestMoreClick = onRestMoreClick
        )
    }
}

@Composable
private fun TakeABreakEndedDialogCard(
    onStartNowClick: () -> Unit,
    onStartTomorrowClick: () -> Unit,
    onRestMoreClick: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(24.dp))
                .background(HPSub4)
                .padding(horizontal = 24.dp, vertical = 28.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                stringResource(R.string.takeabreak_ended_welcome_back),
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Bold,
                color = HPBlack,
                textAlign = TextAlign.Center
            )
            Spacer(modifier = Modifier.height(12.dp))
            Image(
                painter = painterResource(R.drawable.logo_title),
                contentDescription = stringResource(R.string.cd_hampouch_logo),
                modifier = Modifier
                    .width(180.dp)
                    .height(42.dp)
            )
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                stringResource(R.string.takeabreak_ended_title),
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold,
                color = HPBlack,
                textAlign = TextAlign.Center
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                stringResource(R.string.takeabreak_ended_subtitle_line1),
                style = MaterialTheme.typography.bodySmall,
                color = HPText,
                textAlign = TextAlign.Center
            )
            Text(
                stringResource(R.string.takeabreak_ended_subtitle_line2),
                style = MaterialTheme.typography.bodySmall,
                color = HPText,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(24.dp))
            Button(
                onClick = onStartNowClick,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                shape = RoundedCornerShape(10.dp),
                colors = ButtonDefaults.buttonColors(containerColor = HPMain)
            ) {
                Text(
                    stringResource(R.string.takeabreak_ended_start_now_button),
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.Bold,
                    color = HPWhite
                )
            }

            Spacer(modifier = Modifier.height(10.dp))
            Button(
                onClick = onStartTomorrowClick,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                shape = RoundedCornerShape(10.dp),
                colors = ButtonDefaults.buttonColors(containerColor = HPWhite)
            ) {
                Text(
                    stringResource(R.string.takeabreak_ended_start_tomorrow_button),
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.Bold,
                    color = HPText
                )
            }

            Spacer(modifier = Modifier.height(14.dp))
            Text(
                stringResource(R.string.takeabreak_ended_rest_more_link),
                style = MaterialTheme.typography.bodyMedium,
                color = HPText,
                modifier = Modifier.clickable(onClick = onRestMoreClick)
            )
        }
    }
}

@Preview(showBackground = true, backgroundColor = 0xFFB0B0B0)
@Composable
private fun TakeABreakEndedDialogPreview() {
    HampouchTheme {
        TakeABreakEndedDialogCard(
            onStartNowClick = {},
            onStartTomorrowClick = {},
            onRestMoreClick = {}
        )
    }
}
