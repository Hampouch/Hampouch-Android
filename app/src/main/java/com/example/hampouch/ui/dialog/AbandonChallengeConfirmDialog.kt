package com.example.hampouch.ui.dialog

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.hampouch.R
import com.example.hampouch.ui.theme.HPText
import com.example.hampouch.ui.theme.HampouchTheme

@Composable
fun AbandonChallengeConfirmDialog(
    onCancel: () -> Unit,
    onConfirm: () -> Unit
) {
    Dialog(
        onDismissRequest = onCancel,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        AbandonChallengeConfirmDialogCard(onCancel = onCancel, onConfirm = onConfirm)
    }
}

@Composable
private fun AbandonChallengeConfirmDialogCard(
    onCancel: () -> Unit,
    onConfirm: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp)
    ) {
        ConfirmActionCard(
            question = stringResource(R.string.amountadjustment_abandon_confirm_title),
            confirmLabel = stringResource(R.string.amountadjustment_abandon_confirm_button),
            onCancel = onCancel,
            onConfirm = onConfirm,
            subtextLines = listOf(
                stringResource(R.string.amountadjustment_abandon_confirm_bullet1),
                stringResource(R.string.amountadjustment_abandon_confirm_bullet2)
            ),
            subtextColor = HPText
        )
    }
}

@Preview(showBackground = true, backgroundColor = 0xFFB0B0B0)
@Composable
private fun AbandonChallengeConfirmDialogPreview() {
    HampouchTheme {
        AbandonChallengeConfirmDialogCard(onCancel = {}, onConfirm = {})
    }
}
