package com.example.hampouch.ui.takeabreak

import android.R.attr.text
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
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.hampouch.domain.model.BreakDuration
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.composed
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.hampouch.R
import com.example.hampouch.ui.theme.HPBlack
import com.example.hampouch.ui.theme.HPGray1
import com.example.hampouch.ui.theme.HPGray5
import com.example.hampouch.ui.theme.HPMain
import com.example.hampouch.ui.theme.HPSub
import com.example.hampouch.ui.theme.HPSub1
import com.example.hampouch.ui.theme.HPSub2
import com.example.hampouch.ui.theme.HPSub3
import com.example.hampouch.ui.theme.HPSub4
import com.example.hampouch.ui.theme.HPText
import com.example.hampouch.ui.theme.HPWhite
import com.example.hampouch.ui.theme.HampouchTheme

/** [BreakDuration]의 사용자 노출 문구. */
internal val BreakDuration.label: String
    get() = when (this) {
        BreakDuration.THREE_DAYS -> "3일 쉬기"
        BreakDuration.ONE_WEEK -> "1주 쉬기"
        BreakDuration.TWO_WEEKS -> "2주 쉬기"
        BreakDuration.CONTINUOUS -> "계속 쉬기"
    }

/**
 * 휴식 화면 진입점. ViewModel을 붙이고 상태·이벤트를 화면에 흘려준다.
 *
 * @param isExtending 이미 휴식 중이라 시작이 아니라 연장을 해야 하는 경우(홈 팝업의 "더 쉬기").
 */
@Composable
fun TakeABreakRoute(
    isExtending: Boolean,
    onBack: () -> Unit,
    onKeepChallenge: () -> Unit,
    onBreakStarted: () -> Unit,
    viewModel: TakeABreakViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    LaunchedEffect(viewModel) {
        viewModel.events.collect { event ->
            when (event) {
                TakeABreakEvent.BreakStarted -> onBreakStarted()
            }
        }
    }

    TakeABreakScreen(
        uiState = uiState,
        onBack = onBack,
        onKeepChallenge = onKeepChallenge,
        onSelectDuration = viewModel::selectDuration,
        onCustomDaysChange = viewModel::changeCustomDays,
        onSubmit = { viewModel.submit(isExtending) }
    )
}

@Composable
fun TakeABreakScreen(
    uiState: TakeABreakUiState = TakeABreakUiState(),
    onBack: () -> Unit,
    onKeepChallenge: () -> Unit,
    onSelectDuration: (BreakDuration) -> Unit,
    onCustomDaysChange: (String) -> Unit,
    onSubmit: () -> Unit
) {
    Scaffold(containerColor = HPSub3) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp)
        ) {
            Icon(
                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                contentDescription = "뒤로가기",
                tint = HPBlack,
                modifier = Modifier
                    .size(28.dp)
                    .noRippleClickable(onBack)
            )
            Spacer(modifier = Modifier.height(10.dp))
            BreakIntro()

            Spacer(modifier = Modifier.height(20.dp))
            SavedProgressNotice()

            Spacer(modifier = Modifier.height(20.dp))
            BreakDurationPicker(
                selected = uiState.selectedDuration,
                customDaysInput = uiState.customDaysInput,
                onSelect = onSelectDuration,
                onCustomDaysChange = onCustomDaysChange
            )

            Spacer(modifier = Modifier.height(10.dp))
            BreakChangesNotice()

            Spacer(modifier = Modifier.height(30.dp))
            uiState.errorMessage?.let { message ->
                Text(
                    text = message,
                    style = MaterialTheme.typography.bodySmall,
                    color = HPSub,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(8.dp))
            }
            BreakPrimaryButton(
                text = "쉬어가기",
                enabled = !uiState.isSubmitting,
                onClick = onSubmit
            )

            TextButton(
                onClick = onKeepChallenge,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    "취소하고 챌린지 계속하기",
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp,
                    color = HPText
                )
            }
        }
    }
}

@Composable
private fun BreakIntro(modifier: Modifier = Modifier) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .background(HPSub4, RoundedCornerShape(20.dp))
            .dashedBorder(color = HPMain, cornerRadius = 20.dp)
            .padding(vertical = 30.dp, horizontal = 15.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Image(
            painter = painterResource(R.drawable.img_hamster_take_a_break),
            contentDescription = "잠자는 포치",
            modifier = Modifier
                .width(159.dp)
                .height(172.dp)
        )
        Column() {
            Spacer(modifier = Modifier.height(20.dp))
            Text(
                text = "잠깐 쉬어갈까요?",
                style = MaterialTheme.typography.titleMedium,
                color = HPBlack,
                fontSize = 20.sp,
                textAlign = TextAlign.Center
            )
            Spacer(modifier = Modifier.height(20.dp))
            Text(
                text = "챌린지만 멈출 뿐,\n그 외의 기능은\n그대로 쓸 수 있어요!",
                style = MaterialTheme.typography.bodyMedium,
                color = HPText,
                textAlign = TextAlign.Center
            )
        }

    }
}

@Composable
private fun SavedProgressNotice(modifier: Modifier = Modifier) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .background(HPSub2, RoundedCornerShape(20.dp))
            .padding(horizontal = 20.dp, vertical = 10.dp),
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
        Text(
            text = "진행했던 챌린지의 기록은 안전하게 보관돼요.",
            style = MaterialTheme.typography.bodySmall,
            color = HPBlack
        )
    }
}

@Composable
private fun BreakDurationPicker(
    selected: BreakDuration?,
    customDaysInput: String,
    onSelect: (BreakDuration) -> Unit,
    onCustomDaysChange: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .background(HPSub4, RoundedCornerShape(20.dp))
            .padding(horizontal = 15.dp, vertical = 20.dp)
    ) {
        Text(
            text = "얼만큼 쉬어갈까요?",
            style = MaterialTheme.typography.titleSmall,
            color = HPBlack
        )
        Spacer(modifier = Modifier.height(14.dp))
        BreakDuration.entries.chunked(2).forEach { row ->
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 15.dp),
                horizontalArrangement = Arrangement.spacedBy(15.dp)
            ) {
                row.forEach { duration ->
                    DurationOptionButton(
                        label = duration.label,
                        selected = duration == selected,
                        onClick = { onSelect(duration) },
                        modifier = Modifier.weight(1f)
                    )
                }
            }
        }

        CustomDaysInputField(
            value = customDaysInput,
            onValueChange = onCustomDaysChange
        )
        Spacer(modifier = Modifier.height(20.dp))
        BreakDurationNotice()
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
                width = if (selected) 0.dp else 2.dp,
                color = HPGray5,
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
            color = if (selected) HPWhite else HPText
        )
    }
}

@Composable
private fun CustomDaysInputField(
    value: String,
    onValueChange: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val focusManager = LocalFocusManager.current
    var confirmed by remember { mutableStateOf(false) }
    var wasFocused by remember { mutableStateOf(false) }

    LaunchedEffect(value) {
        if (value.isEmpty()) confirmed = false
    }

    if (confirmed && value.isNotEmpty()) {
        Row(
            modifier = modifier
                .fillMaxWidth()
                .height(56.dp)
                .background(HPMain, RoundedCornerShape(10.dp))
                .clickable { confirmed = false },
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "${value}일 쉬기",
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Bold,
                color = HPWhite
            )
        }
        return
    }

    val isActive = value.isNotEmpty()
    Row(
        modifier = modifier
            .fillMaxWidth()
            .height(56.dp)
            .background(HPWhite, RoundedCornerShape(10.dp))
            .border(
                width = 1.dp,
                color = if (isActive) HPMain else HPSub2.copy(alpha = 0.4f),
                shape = RoundedCornerShape(10.dp)
            )
            .padding(horizontal = 20.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(modifier = Modifier.weight(1f)) {
            if (value.isEmpty()) {
                Text(
                    text = "직접 입력",
                    style = MaterialTheme.typography.bodyMedium,
                    color = HPGray5
                )
            }
            BasicTextField(
                value = value,
                onValueChange = { input -> onValueChange(input.filter { it.isDigit() }.take(3)) },
                singleLine = true,
                textStyle = MaterialTheme.typography.bodyMedium.copy(color = HPBlack),
                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.Number,
                    imeAction = ImeAction.Done
                ),
                keyboardActions = KeyboardActions(
                    onDone = {
                        if (value.isNotEmpty()) confirmed = true
                        focusManager.clearFocus()
                    }
                ),
                modifier = Modifier.onFocusChanged { focusState ->
                    if (wasFocused && !focusState.isFocused && value.isNotEmpty()) {
                        confirmed = true
                    }
                    wasFocused = focusState.isFocused
                }
            )
        }
        Text(
            text = "일",
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.Bold,
            fontSize = 18.sp,
            color = HPBlack
        )
    }
}

@Composable
private fun BreakDurationNotice(modifier: Modifier = Modifier) {
    Column(modifier = modifier.fillMaxWidth()) {
        BreakChangeBullet("언제든 챌린지를 다시 시작할 수 있어요.", color = HPText)
        BreakChangeBullet("지정된 기간이 지나면 포치가 알림을 보내드려요.", color = HPText)
    }
}

@Composable
private fun BreakChangesNotice(modifier: Modifier = Modifier) {
    Column(modifier = modifier.fillMaxWidth()) {
        Text(
            text = "쉬는 동안 달라지는 점",
            style = MaterialTheme.typography.labelLarge,
            fontSize = 16.sp,
            color = HPSub
        )
        Spacer(modifier = Modifier.height(2.dp))
        BreakChangeBullet("하루 한도 체크와 미입력 알림이 멈춰요.", color = HPText)
        BreakChangeBullet("홈화면이 휴식기로 변경돼요.", color = HPText)
    }
}

@Composable
private fun BreakChangeBullet(text: String, color: Color, modifier: Modifier = Modifier) {
    Row(
        modifier = modifier
            .fillMaxWidth()
    ) {
        Text(text = "•  ", style = MaterialTheme.typography.bodySmall, color = color)
        Text(text = text, style = MaterialTheme.typography.bodySmall, color = color)
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

private fun Modifier.dashedBorder(
    color: Color,
    cornerRadius: Dp,
    strokeWidth: Dp = 1.5.dp,
    dashLength: Dp = 6.dp,
    gapLength: Dp = 4.dp
): Modifier = drawWithContent {
    drawContent()
    val stroke = Stroke(
        width = strokeWidth.toPx(),
        pathEffect = PathEffect.dashPathEffect(
            floatArrayOf(dashLength.toPx(), gapLength.toPx()),
            0f
        )
    )
    drawRoundRect(
        color = color,
        style = stroke,
        cornerRadius = CornerRadius(cornerRadius.toPx())
    )
}

@Preview(showBackground = true)
@Composable
private fun TakeABreakScreenPreview() {
    HampouchTheme {
        TakeABreakScreen(
            onBack = {},
            onKeepChallenge = {},
            onSelectDuration = {},
            onCustomDaysChange = {},
            onSubmit = {}
        )
    }
}
