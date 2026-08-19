package com.example.hampouch.ui.common

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.calculateEndPadding
import androidx.compose.foundation.layout.calculateStartPadding
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.hampouch.ui.theme.Body16Bold
import com.example.hampouch.ui.theme.HampouchTheme
import com.example.hampouch.ui.theme.HPBlack
import com.example.hampouch.ui.theme.HPGray4
import com.example.hampouch.ui.theme.HPGray5
import com.example.hampouch.ui.theme.HPStatusInProgressText
import com.example.hampouch.ui.theme.HPSub
import com.example.hampouch.ui.theme.HPText
import com.example.hampouch.ui.theme.HPWhite
import kotlinx.coroutines.delay

@Composable
fun rememberCountdownSeconds(expiresAtMillis: Long?): Int? {
    var remainingSeconds by remember { mutableStateOf<Int?>(null) }
    LaunchedEffect(expiresAtMillis) {
        if (expiresAtMillis == null) {
            remainingSeconds = null
            return@LaunchedEffect
        }
        while (true) {
            val remaining = ((expiresAtMillis - System.currentTimeMillis()) / 1000).toInt()
            remainingSeconds = remaining.coerceAtLeast(0)
            if (remaining <= 0) break
            delay(1000)
        }
    }
    return remainingSeconds
}

fun formatRemainingTime(totalSeconds: Int): String {
    val minutes = totalSeconds / 60
    val seconds = totalSeconds % 60
    return "%d:%02d".format(minutes, seconds)
}

@Composable
fun OrDivider(text: String) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        val dash = PathEffect.dashPathEffect(floatArrayOf(10f, 8f))

        Canvas(
            Modifier
                .weight(1f)
                .height(1.dp)
        ) {
            drawLine(HPText, Offset.Zero, Offset(size.width, 0f), pathEffect = dash)
        }
        Text(
            text,
            modifier = Modifier.padding(horizontal = 8.dp),
            style = MaterialTheme.typography.bodyMedium,
            color = HPText
        )
        Canvas(
            Modifier
                .weight(1f)
                .height(1.dp)
        ) {
            drawLine(HPText, Offset.Zero, Offset(size.width, 0f), pathEffect = dash)
        }
    }
}

@Preview(showBackground = true, name = "구분선")
@Composable
private fun OrDividerPreview() {
    HampouchTheme {
        OrDivider(text = "또는")
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LoginTextField(
    label: String,
    value: String,
    onValueChange: (String) -> Unit,
    placeholder: String,
    keyboardOptions: KeyboardOptions,
    visualTransformation: VisualTransformation = VisualTransformation.None,
    trailingIcon: @Composable (() -> Unit)? = null,
    onCheckClick: (() -> Unit)? = null,
    isCheckEnabled: Boolean = true,
    isValid: Boolean = false,
    enabled: Boolean = true
) {
    val interactionSource = remember { MutableInteractionSource() }
    val colors = if (isValid) {
        OutlinedTextFieldDefaults.colors(
            unfocusedBorderColor = HPStatusInProgressText,
            focusedBorderColor = HPStatusInProgressText,
            unfocusedContainerColor = HPWhite,
            focusedContainerColor = HPWhite,
        )
    } else {
        OutlinedTextFieldDefaults.colors(
            unfocusedBorderColor = HPGray5,
            unfocusedContainerColor = HPWhite,
            focusedContainerColor = HPWhite,
        )
    }

    Text(label, style = MaterialTheme.typography.bodyMedium, color = HPText)
    Row(verticalAlignment = Alignment.CenterVertically) {
        BasicTextField(
            value = value,
            onValueChange = onValueChange,
            enabled = enabled,
            modifier = Modifier
                .weight(1f)
                .height(48.dp),
            textStyle = MaterialTheme.typography.bodyMedium.copy(color = HPBlack),
            visualTransformation = visualTransformation,
            keyboardOptions = keyboardOptions,
            singleLine = true,
            interactionSource = interactionSource,
            decorationBox = { innerTextField ->
                OutlinedTextFieldDefaults.DecorationBox(
                    value = value,
                    innerTextField = innerTextField,
                    enabled = enabled,
                    singleLine = true,
                    visualTransformation = visualTransformation,
                    interactionSource = interactionSource,
                    placeholder = {
                        Text(
                            text = placeholder,
                            color = HPGray5,
                            style = MaterialTheme.typography.bodyMedium
                        )
                    },
                    trailingIcon = trailingIcon,
                    colors = colors,
                    contentPadding = OutlinedTextFieldDefaults.contentPadding(
                        start = 12.dp,
                        top = 8.dp,
                        end = 12.dp,
                        bottom = 8.dp
                    ),
                    container = {
                        OutlinedTextFieldDefaults.Container(
                            enabled = enabled,
                            isError = false,
                            interactionSource = interactionSource,
                            colors = colors,
                            shape = RoundedCornerShape(10.dp)
                        )
                    }
                )
            }
        )
        if (onCheckClick != null) {
            Spacer(modifier = Modifier.size(8.dp))
            CheckButton(onClick = onCheckClick, enabled = isCheckEnabled)
        }
    }
}

@Preview(showBackground = true, name = "로그인 텍스트 필드")
@Composable
private fun LoginTextFieldPreview() {
    HampouchTheme {
        LoginTextField(
            label = "이메일",
            value = "hampouch@example.com",
            onValueChange = {},
            placeholder = "이메일을 입력하세요",
            keyboardOptions = KeyboardOptions.Default,
            onCheckClick = {},
            isCheckEnabled = true,
            isValid = true
        )
    }
}

@Composable
fun CheckButton(onClick: () -> Unit, enabled: Boolean = true) {
    Button(
        onClick = onClick,
        enabled = enabled,
        modifier = Modifier.height(48.dp),
        shape = RoundedCornerShape(10.dp),
        colors = ButtonDefaults.buttonColors(
            containerColor = HPWhite,
            contentColor = HPBlack,
            disabledContainerColor = HPWhite,
            disabledContentColor = HPGray5
        ),
        contentPadding = PaddingValues(horizontal = 16.dp)
    ) {
        Text(
            "확인",
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.Bold
        )
    }
}

@Preview(showBackground = true, name = "확인 버튼")
@Composable
private fun CheckButtonPreview() {
    HampouchTheme {
        CheckButton(onClick = {})
    }
}

@Composable
fun FieldMessage(text: String) {
    Spacer(modifier = Modifier.size(8.dp))
    Text(text, style = MaterialTheme.typography.bodyMedium, color = HPSub)
}

@Preview(showBackground = true, name = "필드 메시지")
@Composable
private fun FieldMessagePreview() {
    HampouchTheme {
        FieldMessage(text = "이미 사용 중인 이메일이에요.")
    }
}

@Composable
fun FieldLinkMessage(text: String, onClick: () -> Unit) {
    Spacer(modifier = Modifier.size(8.dp))
    Text(
        text,
        style = MaterialTheme.typography.bodyMedium,
        color = HPSub,
        modifier = Modifier.clickable(onClick = onClick)
    )
}

@Preview(showBackground = true, name = "필드 링크 메시지")
@Composable
private fun FieldLinkMessagePreview() {
    HampouchTheme {
        FieldLinkMessage(text = "인증 코드 재전송", onClick = {})
    }
}

@Composable
fun FooterLinkRow(
    text: String,
    linkText: String,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier.height(24.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(text, style = MaterialTheme.typography.bodyMedium, color = HPText)
        TextButton(
            onClick = onClick,
            contentPadding = PaddingValues(horizontal = 8.dp, vertical = 0.dp),
            colors = ButtonDefaults.textButtonColors(contentColor = HPSub)
        ) {
            Text(linkText, style = Body16Bold, color = HPSub)
        }
    }
}

@Preview(showBackground = true, name = "하단 링크 행")
@Composable
private fun FooterLinkRowPreview() {
    HampouchTheme {
        FooterLinkRow(text = "계정이 없으신가요?", linkText = "회원가입", onClick = {})
    }
}

@Composable
fun ReasonTagAndAmountColumn(
    reasonTag: String?,
    amountText: String,
    modifier: Modifier = Modifier
) {
    Column(horizontalAlignment = Alignment.End, modifier = modifier) {
        if (reasonTag != null) {
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(50))
                    .background(HPGray4)
                    .padding(horizontal = 10.dp, vertical = 4.dp)
            ) {
                Text(reasonTag, style = MaterialTheme.typography.labelMedium, color = HPText)
            }
            Spacer(modifier = Modifier.height(4.dp))
        }
        Text(
            amountText,
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.Bold,
            color = HPBlack
        )
    }
}

@Preview(showBackground = true, name = "이유 태그 · 금액")
@Composable
private fun ReasonTagAndAmountColumnPreview() {
    HampouchTheme {
        ReasonTagAndAmountColumn(reasonTag = "스트레스", amountText = "4,500원")
    }
}

/**
 * 제목을 좌우 액션 요소의 폭과 무관하게 화면(바) 전체 너비 기준으로 항상 중앙 정렬하는 상단바 레이아웃.
 * `Row` + `weight(1f)` 방식은 좌우 요소 폭이 다르면 제목이 남는 공간의 중앙으로 쏠리므로 사용하지 않는다.
 *
 * [contentPadding]은 leading/trailing 아이콘의 시각적 여백 조정에만 쓰이고, 제목 중앙 정렬의 기준이 되는
 * 전체 너비에는 영향을 주지 않는다. 이 바를 감싸는 [modifier]에 비대칭 `padding`(예: start=4dp, end=20dp)을
 * 직접 걸면 바 전체 폭 자체가 비대칭으로 줄어들어 제목이 다시 한쪽으로 치우치므로 사용하지 않는다.
 */
private val ScreenCenteredTopBarHeight = 64.dp

@Composable
fun ScreenCenteredTopBar(
    modifier: Modifier = Modifier,
    contentPadding: PaddingValues = PaddingValues(0.dp),
    leading: @Composable () -> Unit = {},
    trailing: @Composable () -> Unit = {},
    title: @Composable () -> Unit
) {
    val layoutDirection = LocalLayoutDirection.current
    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(ScreenCenteredTopBarHeight)
    ) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            title()
        }
        Box(
            modifier = Modifier
                .align(Alignment.CenterStart)
                .padding(start = contentPadding.calculateStartPadding(layoutDirection))
        ) { leading() }
        Box(
            modifier = Modifier
                .align(Alignment.CenterEnd)
                .padding(end = contentPadding.calculateEndPadding(layoutDirection))
        ) { trailing() }
    }
}

