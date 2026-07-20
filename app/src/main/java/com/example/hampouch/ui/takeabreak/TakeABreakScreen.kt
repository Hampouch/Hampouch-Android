package com.example.hampouch.ui.takeabreak

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.composed
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.example.hampouch.R
import com.example.hampouch.ui.theme.HPBlack
import com.example.hampouch.ui.theme.HPMain
import com.example.hampouch.ui.theme.HPSub3
import com.example.hampouch.ui.theme.HPText
import com.example.hampouch.ui.theme.HPWhite
import com.example.hampouch.ui.theme.HampouchTheme

enum class BreakDuration(val label: String) {
    THREE_DAYS("3일 푹 쉬기"),
    ONE_WEEK("1주일 쉬기"),
    TWO_WEEKS("2주일 쉬기"),
    CUSTOM("직접 선택")
}

@Composable
fun TakeABreakScreen(
    onClose: () -> Unit = {},
    onKeepChallenge: () -> Unit = {},
    onStartBreak: (BreakDuration, customDays: Int?) -> Unit = { _, _ -> }
) {
    var selectedDuration by rememberSaveable { mutableStateOf(BreakDuration.ONE_WEEK) }
    var customBreakDays by rememberSaveable { mutableStateOf<Int?>(null) }
    var showCustomDurationDialog by remember { mutableStateOf(false) }

    val customDurationLabel = customBreakDays?.let { "${it}일 푹 쉬기" } ?: BreakDuration.CUSTOM.label

    if (showCustomDurationDialog) {
        CustomDurationDialog(
            initialDays = customBreakDays,
            onDismiss = { showCustomDurationDialog = false },
            onConfirm = { days ->
                customBreakDays = days
                showCustomDurationDialog = false
            }
        )
    }

    Scaffold(containerColor = HPWhite) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp, vertical = 10.dp)
        ) {
            Icon(
                imageVector = Icons.Filled.Close,
                contentDescription = "닫기",
                tint = HPBlack,
                modifier = Modifier
                    .size(28.dp)
                    .noRippleClickable(onClose)
            )
            Spacer(modifier = Modifier.height(14.dp))
            BreakIntro()

            Spacer(modifier = Modifier.height(20.dp))
            SavedProgressNotice()

            Spacer(modifier = Modifier.height(14.dp))
            BreakDurationPicker(
                selected = selectedDuration,
                customDurationLabel = customDurationLabel,
                onSelect = { duration ->
                    selectedDuration = duration
                    if (duration == BreakDuration.CUSTOM) {
                        showCustomDurationDialog = true
                    }
                }
            )

            BreakChangesNotice()

            Spacer(modifier = Modifier.height(28.dp))
            BreakPrimaryButton(
                text = "쉬기 시작하기",
                onClick = {
                    val days = if (selectedDuration == BreakDuration.CUSTOM) customBreakDays else null
                    onStartBreak(selectedDuration, days)
                }
            )

            TextButton(
                onClick = onKeepChallenge,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    "취소하고 챌린지 계속할래요",
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Bold,
                    color = HPText
                )
            }
        }
    }
}

@Composable
private fun BreakIntro(modifier: Modifier = Modifier) {
    Column(
        modifier = modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Image(
            painter = painterResource(R.drawable.img_hamster_take_a_break),
            contentDescription = "잠자는 포치",
            modifier = Modifier
                .width(159.dp)
                .height(174.dp)
        )
        Spacer(modifier = Modifier.height(11.dp))
        Text(
            text = "잠깐 쉬어갈까요?",
            style = MaterialTheme.typography.titleMedium,
            color = HPBlack,
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(top = 8.dp)
        )
        Spacer(modifier = Modifier.height(11.dp))
        Text(
            text = "챌린지만 멈출 뿐, 그 외의 기능은 그대로 쓸 수 있어요!",
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.Bold,
            color = HPText,
            textAlign = TextAlign.Center
        )
    }
}

@Composable
private fun SavedProgressNotice(modifier: Modifier = Modifier) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .background(HPSub3, RoundedCornerShape(20.dp))
            .padding(start = 20.dp, end = 20.dp, top = 10.dp, bottom = 10.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(34.dp)
                .background(HPWhite, CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Image(
                painter = painterResource(R.drawable.icon_warning),
                contentDescription = "주의",
                modifier = Modifier
                    .width(16.dp)
                    .height(20.dp)
            )
        }

        Spacer(modifier = Modifier.width(10.dp))
        Column {
            Text(
                text = "지금까지 모은 절약 금액과 연속 기록은",
                style = MaterialTheme.typography.bodySmall,
                color = HPBlack
            )
            Text(
                text = "안전하게 보관되니 걱정 마세요.",
                style = MaterialTheme.typography.bodySmall,
                color = HPBlack
            )
        }
    }
}

@Composable
private fun BreakDurationPicker(
    selected: BreakDuration,
    customDurationLabel: String,
    onSelect: (BreakDuration) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier.fillMaxWidth()) {
        Text(
            modifier = Modifier.padding(start = 20.dp),
            text = "언제 다시 시작할까요?",
            style = MaterialTheme.typography.titleSmall,
            color = HPBlack
        )
        Spacer(modifier = Modifier.height(14.dp))
        BreakDuration.entries.chunked(2).forEach { row ->
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(start = 20.dp, end = 20.dp, bottom = 14.dp),
                horizontalArrangement = Arrangement.spacedBy(20.dp)
            ) {
                row.forEach { duration ->
                    DurationOptionButton(
                        label = if (duration == BreakDuration.CUSTOM) customDurationLabel else duration.label,
                        selected = duration == selected,
                        onClick = { onSelect(duration) },
                        modifier = Modifier.weight(1f)
                    )
                }
            }
        }
    }
}

@Composable
private fun DurationOptionButton(
    label: String,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .height(64.dp)
            .background(
                color = if (selected) HPMain else HPWhite,
                shape = RoundedCornerShape(10.dp)
            )
            .border(
                width = if (selected) 0.dp else 1.dp,
                color = HPMain,
                shape = RoundedCornerShape(10.dp)
            )
            .clickable(onClick = onClick),
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.Bold,
            color = if (selected) HPWhite else HPMain
        )
    }
}

@Composable
private fun BreakChangesNotice(modifier: Modifier = Modifier) {
    Column(modifier = modifier.fillMaxWidth()) {
        Text(
            modifier = Modifier.padding(start = 20.dp),
            text = "쉬는 동안 달라지는 점",
            style = MaterialTheme.typography.labelLarge,
            color = HPText
        )
        Spacer(modifier = Modifier.height(6.dp))
        BreakChangeBullet("하루 한도 체크와 미입력 알림이 멈춰요.")
        BreakChangeBullet("새로운 햄배틀 매칭이 잠시 중단돼요.")
    }
}

@Composable
private fun BreakChangeBullet(text: String, modifier: Modifier = Modifier) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(start = 20.dp)
    ) {
        Text(text = "•  ", style = MaterialTheme.typography.bodySmall, color = HPText)
        Text(text = text, style = MaterialTheme.typography.bodySmall, color = HPText)
    }
}

@Composable
internal fun BreakPrimaryButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    height: Dp = 56.dp
) {
    Button(
        onClick = onClick,
        enabled = enabled,
        modifier = modifier
            .fillMaxWidth()
            .height(height),
        shape = RoundedCornerShape(10.dp),
        colors = ButtonDefaults.buttonColors(containerColor = HPMain, contentColor = HPWhite)
    ) {
        Text(text, style = MaterialTheme.typography.titleSmall)
    }
}

internal fun Modifier.noRippleClickable(onClick: () -> Unit): Modifier = composed {
    clickable(
        interactionSource = remember { MutableInteractionSource() },
        indication = null,
        onClick = onClick
    )
}

@Preview(showBackground = true)
@Composable
private fun TakeABreakScreenPreview() {
    HampouchTheme {
        TakeABreakScreen()
    }
}
