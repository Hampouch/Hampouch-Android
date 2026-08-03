package com.example.hampouch.ui.dialog

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.hampouch.ui.theme.HampouchTheme

@Composable
fun NextChallengeStartConfirmDialog(
    onCancel: () -> Unit,
    onConfirm: () -> Unit
) {
    Dialog(
        onDismissRequest = onCancel,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        NextChallengeStartConfirmDialogCard(onCancel = onCancel, onConfirm = onConfirm)
    }
}

@Composable
private fun NextChallengeStartConfirmDialogCard(
    onCancel: () -> Unit,
    onConfirm: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp)
    ) {
        ConfirmActionCard(
            question = "새로운 챌린지를 시작할까요?",
            confirmLabel = "시작하기",
            onCancel = onCancel,
            onConfirm = onConfirm
        )
    }
}

@Preview(showBackground = true, backgroundColor = 0xFFB0B0B0)
@Composable
private fun NextChallengeStartConfirmDialogPreview() {
    HampouchTheme {
        NextChallengeStartConfirmDialogCard(onCancel = {}, onConfirm = {})
    }
}
