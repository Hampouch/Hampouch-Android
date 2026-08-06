package com.example.hampouch.ui.challengeresult

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
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
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
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
import com.example.hampouch.ui.theme.Body16Bold
import com.example.hampouch.ui.theme.HPBlack
import com.example.hampouch.ui.theme.HPGray4
import com.example.hampouch.ui.theme.HPGray5
import com.example.hampouch.ui.theme.HPMain
import com.example.hampouch.ui.theme.HPSub2
import com.example.hampouch.ui.theme.HPSub3
import com.example.hampouch.ui.theme.HPSub4
import com.example.hampouch.ui.theme.HPText
import com.example.hampouch.ui.theme.HPWhite
import com.example.hampouch.ui.theme.HampouchTheme
import kotlin.math.roundToInt

private enum class GoalAdjustmentOption(val label: String, val multiplier: Double) {
    KEEP("현재 유지", 1.0),
    RELAX_10("10% 여유", 1.1),
    RELAX_20("20% 여유", 1.2)
}

@Composable
fun ChallengeGoalAdjustmentDialog(
    currentGoalAmount: Int,
    onDismissRequest: () -> Unit,
    onStartNewChallengeClick: (Int) -> Unit
) {
    Dialog(
        onDismissRequest = onDismissRequest,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        ChallengeGoalAdjustmentCard(
            currentGoalAmount = currentGoalAmount,
            onStartNewChallengeClick = onStartNewChallengeClick,
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp)
        )
    }
}

@Composable
private fun ChallengeGoalAdjustmentCard(
    currentGoalAmount: Int,
    onStartNewChallengeClick: (Int) -> Unit,
    modifier: Modifier = Modifier
) {
    var selectedOption by remember { mutableStateOf<GoalAdjustmentOption?>(null) }

    Column(
        modifier = modifier
            .clip(RoundedCornerShape(20.dp))
            .background(HPSub3)
            .padding(horizontal = 25.dp, vertical = 40.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            "목표 금액이 너무 힘들었나요?",
            style = MaterialTheme.typography.titleSmall,
            color = HPMain
        )
        Spacer(modifier = Modifier.height(10.dp))
        Text(
            "다음 챌린지 목표 금액을 조정해봐요.\n조금 더 여유롭게 설정하면 성공 확률이 올라가요.",
            style = MaterialTheme.typography.bodySmall,
            color = HPText,
            fontSize = 15.sp,
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(20.dp))
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            GoalAdjustmentOption.entries.forEach { option ->
                val amount = (currentGoalAmount * option.multiplier).roundToInt()
                GoalOptionCard(
                    label = option.label,
                    value = formatWon(amount),
                    selected = option == selectedOption,
                    onClick = { selectedOption = option },
                    modifier = Modifier.weight(1f)
                )
            }
        }

        Spacer(modifier = Modifier.height(20.dp))
        Button(
            onClick = {
                selectedOption?.let { option ->
                    onStartNewChallengeClick((currentGoalAmount * option.multiplier).roundToInt())
                }
            },
            enabled = selectedOption != null,
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp),
            shape = RoundedCornerShape(10.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = HPMain,
                disabledContainerColor = HPGray4,
                disabledContentColor = HPGray5
            )
        ) {
            Text(
                "다음 챌린지 시작하기",
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.Bold,
                color = if (selectedOption != null) HPWhite else HPText
            )
        }
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            "이후 챌린지 설정에서 세부 조정이 가능해요.",
            style = MaterialTheme.typography.labelMedium,
            fontSize = 16.sp,
            color = HPMain
        )
    }
}

@Composable
private fun GoalOptionCard(
    label: String,
    value: String,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .clip(RoundedCornerShape(10.dp))
            .clickable(onClick = onClick)
            .background(if (selected) HPMain else HPSub4)
            .border(
                width = if (selected) 0.dp else 1.dp,
                color = HPMain,
                shape = RoundedCornerShape(10.dp)
            )
            .padding(15.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            label,
            style = MaterialTheme.typography.labelMedium,
            color = if (selected) HPWhite else HPBlack
        )
        Spacer(modifier = Modifier.height(6.dp))
        Text(
            value,
            style = Body16Bold,
            fontWeight = FontWeight.Bold,
            fontSize = 15.sp,
            maxLines = 1,
            color = if (selected) HPWhite else HPBlack
        )
    }
}

@Preview(showBackground = true, backgroundColor = 0xFFB0B0B0)
@Composable
private fun ChallengeGoalAdjustmentCardPreview() {
    HampouchTheme {
        ChallengeGoalAdjustmentCard(currentGoalAmount = 400_000, onStartNewChallengeClick = {})
    }
}
