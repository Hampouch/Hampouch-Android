package com.example.hampouch.ui.dialog

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.hampouch.R
import com.example.hampouch.ui.theme.HPText
import com.example.hampouch.ui.theme.HampouchTheme

@Composable
fun AmountAdjustmentConfirmDialog(
    onCancel: () -> Unit,
    onConfirm: () -> Unit,
    subtext: String? = null
) {
    Dialog(
        onDismissRequest = onCancel,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        AmountAdjustmentConfirmDialogCard(onCancel = onCancel, onConfirm = onConfirm, subtext = subtext)
    }
}

@Composable
private fun AmountAdjustmentConfirmDialogCard(
    onCancel: () -> Unit,
    onConfirm: () -> Unit,
    subtext: String? = null
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp)
    ) {
        ConfirmActionCard(
            question = stringResource(R.string.amountadjustment_confirm_question),
            confirmLabel = stringResource(R.string.amountadjustment_confirm_button),
            onCancel = onCancel,
            onConfirm = onConfirm,
            subtext = subtext,
            subtextColor = HPText,
            subtextTextAlign = TextAlign.Start
        )
    }
}

@Preview(showBackground = true, backgroundColor = 0xFFB0B0B0)
@Composable
private fun AmountAdjustmentConfirmDialogPreview() {
    HampouchTheme {
        AmountAdjustmentConfirmDialogCard(onCancel = {}, onConfirm = {})
    }
}

@Preview(showBackground = true, backgroundColor = 0xFFB0B0B0)
@Composable
private fun AmountAdjustmentConfirmDialogLastEditPreview() {
    HampouchTheme {
        AmountAdjustmentConfirmDialogCard(
            onCancel = {},
            onConfirm = {},
            subtext = stringResource(R.string.amountadjustment_last_edit_warning)
        )
    }
}
