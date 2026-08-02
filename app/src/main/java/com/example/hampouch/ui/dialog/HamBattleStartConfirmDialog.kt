package com.example.hampouch.ui.dialog

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.hampouch.data.model.HamBattleChallengeRequest
import com.example.hampouch.ui.theme.Body16Bold
import com.example.hampouch.ui.theme.HPBlack
import com.example.hampouch.ui.theme.HPGray3
import com.example.hampouch.ui.theme.HPMain
import com.example.hampouch.ui.theme.HPSub
import com.example.hampouch.ui.theme.HPText
import com.example.hampouch.ui.theme.HPWhite
import com.example.hampouch.ui.theme.HampouchTheme

@Composable
fun HamBattleStartConfirmDialog(
    request: HamBattleChallengeRequest,
    onCancel: () -> Unit,
    onConfirm: () -> Unit
) {
    Dialog(
        onDismissRequest = onCancel,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        HamBattleStartConfirmDialogCard(request = request, onCancel = onCancel, onConfirm = onConfirm)
    }
}

@Composable
private fun HamBattleStartConfirmDialogCard(
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
            question = "해당 햄배틀 챌린지를 시작할까요?",
            confirmLabel = "시작하기",
            onCancel = onCancel,
            onConfirm = onConfirm
        )
    }
}

@Composable
fun ChallengeSummaryCard(request: HamBattleChallengeRequest) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(20.dp))
            .background(HPWhite)
            .padding(15.dp)
    ) {
        Text(
            "${request.durationDays} · ${request.participantCount}",
            style = MaterialTheme.typography.bodySmall,
            color = HPText
        )
        Spacer(modifier = Modifier.height(6.dp))
        Text(
            request.challengeName.ifBlank { "이름 없는 챌린지" },
            fontSize = 16.sp,
            color = HPBlack
        )
        Spacer(modifier = Modifier.height(6.dp))
        Row {
            Text(
                "벌칙: ",
                style = MaterialTheme.typography.bodyMedium,
                fontSize = 12.sp,
                color = HPText
            )
            Text(
                request.penalty,
                style = MaterialTheme.typography.bodyMedium,
                fontSize = 12.sp,
                color = HPMain,
            )
        }
    }
}

@Composable
fun ConfirmActionCard(
    question: String,
    confirmLabel: String,
    onCancel: () -> Unit,
    onConfirm: () -> Unit,
    subtext: String? = null
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(20.dp))
            .background(HPWhite)
            .padding(horizontal = 25.dp, vertical = 30.dp)
    ) {
        Text(
            question,
            modifier = Modifier.fillMaxWidth(),
            textAlign = TextAlign.Start,
            style = MaterialTheme.typography.titleSmall,
            color = HPBlack
        )
        if (subtext != null) {
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                subtext,
                modifier = Modifier.fillMaxWidth(),
                textAlign = TextAlign.Center,
                style = MaterialTheme.typography.bodySmall,
                color = HPSub
            )
        }
        Spacer(modifier = Modifier.height(20.dp))
        Row(modifier = Modifier.fillMaxWidth()) {
            Button(
                onClick = onCancel,
                modifier = Modifier
                    .weight(1f)
                    .height(64.dp),
                shape = RoundedCornerShape(10.dp),
                colors = ButtonDefaults.buttonColors(containerColor = HPGray3)
            ) {
                Text(
                    "취소",
                    style = MaterialTheme.typography.bodyLarge,
                    color = HPText
                )
            }
            Spacer(modifier = Modifier.width(20.dp))
            Button(
                onClick = onConfirm,
                modifier = Modifier
                    .weight(1f)
                    .height(64.dp),
                shape = RoundedCornerShape(10.dp),
                colors = ButtonDefaults.buttonColors(containerColor = HPMain)
            ) {
                Text(
                    confirmLabel,
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.Bold,
                    color = HPWhite
                )
            }
        }
    }
}

private val PreviewChallengeRequest = HamBattleChallengeRequest(
    challengeName = "5월 식비 절약왕 가리기",
    participantCount = "5인",
    durationDays = "7일",
    startDateMillis = null,
    penalty = "커피 쿠폰 쏘기"
)

@Preview(showBackground = true, backgroundColor = 0xFFB0B0B0)
@Composable
private fun HamBattleStartConfirmDialogPreview() {
    HampouchTheme {
        HamBattleStartConfirmDialogCard(
            request = PreviewChallengeRequest,
            onCancel = {},
            onConfirm = {}
        )
    }
}
