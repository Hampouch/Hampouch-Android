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
import com.example.hampouch.ui.theme.HampouchTheme

@Composable
fun OnboardingSkipConfirmDialog(
    onCancel: () -> Unit,
    onConfirm: () -> Unit
) {
    Dialog(
        onDismissRequest = onCancel,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        OnboardingSkipConfirmDialogCard(onCancel = onCancel, onConfirm = onConfirm)
    }
}

@Composable
private fun OnboardingSkipConfirmDialogCard(
    onCancel: () -> Unit,
    onConfirm: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp)
    ) {
        ConfirmActionCard(
            question = stringResource(R.string.onboarding_skip_confirm_title),
            confirmLabel = stringResource(R.string.onboarding_skip),
            onCancel = onCancel,
            onConfirm = onConfirm,
            subtext = stringResource(R.string.onboarding_skip_confirm_subtitle)
        )
    }
}

@Preview(showBackground = true, backgroundColor = 0xFFB0B0B0)
@Composable
private fun OnboardingSkipConfirmDialogPreview() {
    HampouchTheme {
        OnboardingSkipConfirmDialogCard(onCancel = {}, onConfirm = {})
    }
}
