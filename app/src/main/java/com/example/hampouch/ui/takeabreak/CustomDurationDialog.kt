package com.example.hampouch.ui.takeabreak

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material3.Icon
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
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.hampouch.ui.onboarding.components.EditableAmountRow
import com.example.hampouch.ui.onboarding.components.OnboardingPrimaryButton
import com.example.hampouch.ui.theme.HPBlack
import com.example.hampouch.ui.theme.HPGray5
import com.example.hampouch.ui.theme.HPMain
import com.example.hampouch.ui.theme.HPWhite
import com.example.hampouch.ui.theme.HampouchTheme

@Composable
fun CustomDurationDialog(
    initialDays: Int?,
    onDismiss: () -> Unit,
    onConfirm: (days: Int) -> Unit,
    modifier: Modifier = Modifier
) {
    var days by remember { mutableStateOf(initialDays) }

    val confirmAndClose: () -> Unit = {
        days?.takeIf { it > 0 }?.let(onConfirm) ?: onDismiss()
    }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(HPBlack.copy(alpha = 0.5f))
                .imePadding()
                .noRippleClickable(onDismiss),
            contentAlignment = Alignment.Center
        ) {
            Column(
                modifier = modifier
                    .padding(horizontal = 40.dp)
                    .clip(RoundedCornerShape(24.dp))
                    .background(HPWhite)
                    .noRippleClickable {}
                    .padding(vertical = 32.dp, horizontal = 24.dp)
                    .fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Box(
                    modifier = Modifier
                        .size(96.dp)
                        .background(HPWhite, CircleShape)
                        .border(1.dp, HPGray5, CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        modifier = Modifier.size(40.dp),
                        imageVector = Icons.Default.CalendarMonth,
                        tint = HPMain,
                        contentDescription = "달력"
                    )
                }
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    text = "얼만큼 쉬어갈까요?",
                    style = MaterialTheme.typography.titleSmall,
                    color = HPBlack
                )
                Spacer(modifier = Modifier.height(16.dp))
                EditableAmountRow(
                    label = null,
                    value = days,
                    onValueChange = { days = it },
                    placeholder = "직접 입력",
                    suffix = "일"
                )
                Spacer(modifier = Modifier.height(20.dp))
                OnboardingPrimaryButton(
                    text = "확인",
                    enabled = (days ?: 0) > 0,
                    onClick = confirmAndClose
                )
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun CustomDurationDialogPreview() {
    HampouchTheme {
        CustomDurationDialog(initialDays = 5, onDismiss = {}, onConfirm = {})
    }
}
