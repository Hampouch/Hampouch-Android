package com.example.hampouch.ui.common

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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.hampouch.R
import com.example.hampouch.ui.theme.HPMain
import com.example.hampouch.ui.theme.HPStatusFailBg
import com.example.hampouch.ui.theme.HPStatusFailText
import com.example.hampouch.ui.theme.HPText
import com.example.hampouch.ui.theme.HPWhite
import com.example.hampouch.ui.theme.HampouchTheme

@Composable
fun FullScreenLoadingIndicator(modifier: Modifier = Modifier) {
    Box(modifier = modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        CircularProgressIndicator(color = HPMain)
    }
}

@Composable
fun InlineLoadingIndicator(modifier: Modifier = Modifier) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(120.dp)
            .clip(RoundedCornerShape(16.dp))
            .background(HPWhite),
        contentAlignment = Alignment.Center
    ) {
        CircularProgressIndicator(color = HPMain)
    }
}

@Composable
fun FullScreenLoadError(
    message: String,
    onRetry: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 24.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                text = message,
                style = MaterialTheme.typography.bodyMedium,
                color = HPText
            )
            Spacer(modifier = Modifier.height(20.dp))
            Button(
                onClick = onRetry,
                shape = RoundedCornerShape(10.dp),
                colors = ButtonDefaults.buttonColors(containerColor = HPMain, contentColor = HPWhite)
            ) {
                Text(stringResource(R.string.common_retry))
            }
        }
    }
}

@Composable
fun StaleDataRefreshBanner(
    message: String,
    onRetry: () -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(HPStatusFailBg)
            .padding(horizontal = 14.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = message,
            style = MaterialTheme.typography.labelMedium,
            fontWeight = FontWeight.Medium,
            color = HPStatusFailText,
            modifier = Modifier.weight(1f)
        )
        TextButton(onClick = onRetry) {
            Text(
                text = stringResource(R.string.common_retry),
                style = MaterialTheme.typography.labelMedium,
                color = HPStatusFailText
            )
        }
    }
}

@Composable
fun InlineLoadErrorCard(
    message: String,
    onRetry: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(HPWhite)
            .padding(20.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = message,
            style = MaterialTheme.typography.bodyMedium,
            color = HPText
        )
        Spacer(modifier = Modifier.height(12.dp))
        TextButton(onClick = onRetry) {
            Text(stringResource(R.string.common_retry), color = HPMain)
        }
    }
}

@Preview(showBackground = true, name = "전체 화면 에러")
@Composable
private fun FullScreenLoadErrorPreview() {
    HampouchTheme {
        FullScreenLoadError(message = "일시적인 오류로 정보를 불러오지 못했습니다.", onRetry = {})
    }
}

@Preview(showBackground = true, name = "갱신 실패 배너")
@Composable
private fun StaleDataRefreshBannerPreview() {
    HampouchTheme {
        StaleDataRefreshBanner(
            message = "최신 정보를 불러오지 못했습니다.",
            onRetry = {},
            modifier = Modifier.padding(16.dp)
        )
    }
}
