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
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.hampouch.R
import com.example.hampouch.ui.theme.HPMain
import com.example.hampouch.ui.theme.HPSub3
import com.example.hampouch.ui.theme.HPText

@Composable
fun CompleteDialog(onDismiss: () -> Unit) {
    // TODO: completeDialogCard를 alertDialog에 띄워야함.
}

@Composable
private fun CompleteDialogCard() {
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
                text = "회원가입이 완료되었습니다.",
                textAlign = TextAlign.Center,
                color = HPText,
                style = MaterialTheme.typography.bodySmall
            )
            Spacer(modifier = Modifier.height(12.dp))
            TextButton(onClick = {}) { Text("닫기") }
        }
    }
}

@Preview
@Composable
private fun CompleteDialogCardPreview() {
    CompleteDialogCard()
}