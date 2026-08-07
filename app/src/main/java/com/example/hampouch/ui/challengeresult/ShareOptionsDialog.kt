package com.example.hampouch.ui.challengeresult

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.hampouch.R
import com.example.hampouch.ui.theme.HPBlack
import com.example.hampouch.ui.theme.HPGray3
import com.example.hampouch.ui.theme.HPGray4
import com.example.hampouch.ui.theme.HPText
import com.example.hampouch.ui.theme.HPWhite
import com.example.hampouch.ui.theme.HampouchTheme

enum class ShareOption(val label: String, val iconRes: Int, val packageName: String?) {
    KAKAO_TALK("카카오톡", R.drawable.login_kakao, "com.kakao.talk"),
    INSTAGRAM("인스타그램", R.drawable.icon_instagram, "com.instagram.android"),
    MESSAGE("메세지", R.drawable.icon_message, null),
    SAVE_IMAGE("이미지로 저장", R.drawable.icon_gallery, null)
}

@Composable
fun ShareOptionsDialog(
    onDismissRequest: () -> Unit,
    onOptionSelected: (ShareOption) -> Unit = {}
) {
    Dialog(
        onDismissRequest = onDismissRequest,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(20.dp))
                    .background(HPWhite)
                    .padding(horizontal = 25.dp, vertical = 30.dp),
            ) {
                ShareOption.entries.forEach { option ->
                    ShareOptionRow(
                        option = option,
                        onClick = {
                            onOptionSelected(option)
                            onDismissRequest()
                        }
                    )
                    Spacer(modifier = Modifier.height(20.dp))
                }
                CancelRow(onClick = onDismissRequest)
            }
        }
    }
}

@Composable
private fun ShareOptionRow(option: ShareOption, onClick: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(horizontal = 20.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Image(
            painter = painterResource(option.iconRes),
            contentDescription = null,
            modifier = Modifier.size(35.dp)
        )
        Spacer(modifier = Modifier.width(13.dp))
        Text(
            option.label,
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.Bold,
            color = HPBlack
        )
    }
}

@Composable
private fun CancelRow(onClick: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(64.dp)
            .clip(RoundedCornerShape(10.dp))
            .background(HPGray4)
            .clickable(onClick = onClick),
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            "취소",
            style = MaterialTheme.typography.bodyLarge,
            fontWeight = FontWeight.Bold,
            color = HPText
        )
    }
}

@Preview(showBackground = true, backgroundColor = 0xFFB0B0B0)
@Composable
private fun ShareOptionsDialogPreview() {
    HampouchTheme {
        ShareOptionsDialog(onDismissRequest = {})
    }
}
