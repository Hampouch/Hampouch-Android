package com.example.hampouch.ui.dialog

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.hampouch.domain.model.HamBattleChallengeRequest
import com.example.hampouch.ui.theme.HampouchTheme

@Composable
fun HamBattleJoinConfirmDialog(
    request: HamBattleChallengeRequest,
    onCancel: () -> Unit,
    onConfirm: () -> Unit
) {
    Dialog(
        onDismissRequest = onCancel,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        HamBattleJoinConfirmDialogCard(request = request, onCancel = onCancel, onConfirm = onConfirm)
    }
}

@Composable
private fun HamBattleJoinConfirmDialogCard(
    request: HamBattleChallengeRequest,
    onCancel: () -> Unit,
    onConfirm: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        ChallengeSummaryCard(request)
        Spacer(modifier = Modifier.height(29.dp))
        ConfirmActionCard(
            question = "해당 햄배틀 챌린지에 참가할까요?",
            confirmLabel = "참가하기",
            onCancel = onCancel,
            onConfirm = onConfirm
        )
    }
}

private val PreviewJoinChallengeRequest = HamBattleChallengeRequest(
    challengeName = "5월 식비 절약왕 가리기",
    participantCount = "5인",
    durationDays = "7일",
    startDateMillis = null,
    penalty = "커피 쿠폰 쏘기"
)

@Preview(showBackground = true, backgroundColor = 0xFFB0B0B0)
@Composable
private fun HamBattleJoinConfirmDialogPreview() {
    HampouchTheme {
        HamBattleJoinConfirmDialogCard(
            request = PreviewJoinChallengeRequest,
            onCancel = {},
            onConfirm = {}
        )
    }
}
