package com.example.hampouch.ui.dialog

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.hampouch.ui.theme.Body16Bold
import com.example.hampouch.ui.theme.HPBlack
import com.example.hampouch.ui.theme.HPText
import com.example.hampouch.ui.theme.HPWhite
import com.example.hampouch.ui.theme.HampouchTheme

@Composable
fun MiniChallengeAddConfirmDialog(
    name: String,
    periodLabel: String,
    onCancel: () -> Unit,
    onConfirm: () -> Unit
) {
    Dialog(
        onDismissRequest = onCancel,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        MiniChallengeAddConfirmDialogCard(
            name = name,
            periodLabel = periodLabel,
            onCancel = onCancel,
            onConfirm = onConfirm
        )
    }
}

@Composable
private fun MiniChallengeAddConfirmDialogCard(
    name: String,
    periodLabel: String,
    onCancel: () -> Unit,
    onConfirm: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        MiniChallengeSummaryCard(name = name, periodLabel = periodLabel)
        Spacer(modifier = Modifier.height(29.dp))
        ConfirmActionCard(
            question = "해당 미니 챌린지를 추가할까요?",
            confirmLabel = "추가하기",
            onCancel = onCancel,
            onConfirm = onConfirm
        )
    }
}

@Composable
private fun MiniChallengeSummaryCard(name: String, periodLabel: String) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(20.dp))
            .background(HPWhite)
            .padding(15.dp)
    ) {
        Text(periodLabel, style = MaterialTheme.typography.bodySmall, color = HPText)
        Spacer(modifier = Modifier.height(6.dp))
        Text(name.ifBlank { "이름 없는 챌린지" }, style = Body16Bold, color = HPBlack)
    }
}

@Preview(showBackground = true, backgroundColor = 0xFFB0B0B0)
@Composable
private fun MiniChallengeAddConfirmDialogPreview() {
    HampouchTheme {
        MiniChallengeAddConfirmDialogCard(
            name = "챌린지명",
            periodLabel = "7일",
            onCancel = {},
            onConfirm = {}
        )
    }
}
