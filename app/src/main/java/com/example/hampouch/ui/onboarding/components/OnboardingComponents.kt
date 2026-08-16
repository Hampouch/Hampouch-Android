package com.example.hampouch.ui.onboarding.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.example.hampouch.R
import com.example.hampouch.ui.theme.HPBlack
import com.example.hampouch.ui.theme.HPGray5
import com.example.hampouch.ui.theme.HPMain
import com.example.hampouch.ui.theme.HPSub
import com.example.hampouch.ui.theme.HPSub1
import com.example.hampouch.ui.theme.HPSub3
import com.example.hampouch.ui.theme.HPText
import com.example.hampouch.ui.theme.HPWhite
import kotlinx.coroutines.delay
import java.text.NumberFormat
import java.util.Locale
import androidx.compose.ui.tooling.preview.Preview
import com.example.hampouch.ui.theme.HampouchTheme

fun Int.toWonText(): String = NumberFormat.getNumberInstance(Locale.KOREA).format(this)

internal const val FocusHandoffDelayMillis: Long = 60L

/** Int로 안전하게 변환 가능한 자리수 상한(최대 999,999,999 < Int.MAX_VALUE)으로, 오버플로로 인한 서식·값 갱신 중단을 막는다. */
private const val MaxAmountInputDigits = 9

@Composable
fun OnboardingTopBar(
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.Start,
        verticalAlignment = Alignment.CenterVertically
    ) {
        IconButton(onClick = onBack) {
            Icon(
                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                contentDescription = stringResource(R.string.cd_back),
                tint = HPSub1
            )
        }
    }
}

@Preview(showBackground = true, name = "온보딩 상단바")
@Composable
private fun OnboardingTopBarPreview() {
    HampouchTheme {
        OnboardingTopBar(onBack = {})
    }
}

@Composable
fun OnboardingProgressBar(
    currentStep: Int,
    totalSteps: Int,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(11.dp)
    ) {
        repeat(totalSteps) { index ->
            val isActive = index == currentStep - 1
            Box(
                modifier = Modifier
                    .height(5.dp)
                    .weight(1f)
                    .background(
                        color = if (isActive) HPMain else HPGray5,
                        shape = RoundedCornerShape(2.5.dp)
                    )
            )
        }
    }
}

@Preview(showBackground = true, name = "온보딩 진행 바")
@Composable
private fun OnboardingProgressBarPreview() {
    HampouchTheme {
        OnboardingProgressBar(currentStep = 2, totalSteps = 4, modifier = Modifier.padding(16.dp))
    }
}

@Composable
fun OnboardingHeaderCard(
    stepNumber: Int,
    stepLabel: String,
    title: String,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .height(139.dp)
            .background(HPSub3, RoundedCornerShape(30.dp))
            .padding(horizontal = 20.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = stringResource(R.string.onboarding_step_format, stepNumber) + " $stepLabel",
            style = MaterialTheme.typography.labelLarge,
            color = HPSub,
            textAlign = TextAlign.Center
        )
        Text(
            text = title,
            style = MaterialTheme.typography.titleMedium,
            color = HPBlack,
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(top = 6.dp)
        )
    }
}

@Preview(showBackground = true, name = "온보딩 헤더 카드")
@Composable
private fun OnboardingHeaderCardPreview() {
    HampouchTheme {
        OnboardingHeaderCard(
            stepNumber = 1,
            stepLabel = "챌린지 기간 설정",
            title = "얼마 동안 절약해볼까요?",
            modifier = Modifier.padding(16.dp)
        )
    }
}

@Composable
fun OnboardingCaptionText(
    text: String,
    modifier: Modifier = Modifier
) {
    Text(
        text = text,
        style = MaterialTheme.typography.bodySmall,
        color = HPText,
        modifier = modifier.fillMaxWidth()
    )
}

@Preview(showBackground = true, name = "온보딩 설명 텍스트")
@Composable
private fun OnboardingCaptionTextPreview() {
    HampouchTheme {
        OnboardingCaptionText(
            text = "챌린지 기간은 최소 7일부터 최대 30일까지 설정할 수 있어요.",
            modifier = Modifier.padding(16.dp)
        )
    }
}

@Composable
fun OnboardingBulletList(
    lines: List<String>,
    modifier: Modifier = Modifier,
    color: Color = HPText
) {
    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        lines.forEach { line ->
            Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                Text(text = "•", style = MaterialTheme.typography.labelSmall, color = color)
                Text(text = line, style = MaterialTheme.typography.labelSmall, color = color)
            }
        }
    }
}

@Preview(showBackground = true, name = "온보딩 불릿 목록")
@Composable
private fun OnboardingBulletListPreview() {
    HampouchTheme {
        OnboardingBulletList(
            lines = listOf("한 번 정한 기간은 중간에 바꿀 수 없어요.", "매일 소비 내역을 기록해야 해요."),
            modifier = Modifier.padding(16.dp)
        )
    }
}

@Composable
fun SkipText(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier.fillMaxWidth(),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = text,
            style = MaterialTheme.typography.labelLarge,
            color = HPText,
            textAlign = TextAlign.Center,
            modifier = Modifier
                .clip(RoundedCornerShape(8.dp))
                .clickable(onClick = onClick)
                .padding(horizontal = 12.dp, vertical = 8.dp)
        )
    }
}

@Preview(showBackground = true, name = "건너뛰기 텍스트")
@Composable
private fun SkipTextPreview() {
    HampouchTheme {
        SkipText(text = "다음에 할게요", onClick = {}, modifier = Modifier.padding(16.dp))
    }
}

@Composable
fun SectionCard(
    modifier: Modifier = Modifier,
    content: @Composable ColumnScope.() -> Unit
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .background(HPSub3, RoundedCornerShape(20.dp))
            .padding(16.dp),
        content = content
    )
}

@Preview(showBackground = true, name = "섹션 카드")
@Composable
private fun SectionCardPreview() {
    HampouchTheme {
        SectionCard(modifier = Modifier.padding(16.dp)) {
            Text(text = "챌린지 요약", style = MaterialTheme.typography.labelLarge, color = HPSub1)
        }
    }
}

@Composable
fun LabeledInputRow(
    label: String?,
    valueText: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    icon: ImageVector? = null,
    suffix: String? = null,
    valueColor: Color? = null
) {
    Column(modifier = modifier.fillMaxWidth()) {
        if (label != null) {
            Text(text = label, style = MaterialTheme.typography.labelLarge, color = HPSub1)
        }
        Row(
            modifier = Modifier
                .padding(top = if (label != null) 8.dp else 0.dp)
                .fillMaxWidth()
                .height(43.dp)
                .clip(RoundedCornerShape(9.5.dp))
                .background(HPWhite, RoundedCornerShape(9.5.dp))
                .border(1.dp, HPGray5, RoundedCornerShape(9.5.dp))
                .clickable(onClick = onClick)
                .padding(horizontal = 16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            if (icon != null) {
                Icon(imageVector = icon, contentDescription = null, tint = HPMain, modifier = Modifier.size(18.dp))
            }
            Text(
                text = valueText,
                style = MaterialTheme.typography.bodyMedium,
                color = valueColor ?: if (valueText.isEmpty()) HPText else HPSub1,
                modifier = Modifier.weight(1f)
            )
            if (suffix != null) {
                Text(text = suffix, style = MaterialTheme.typography.bodyMedium, color = HPBlack)
            }
        }
    }
}

@Preview(showBackground = true, name = "라벨 입력 행")
@Composable
private fun LabeledInputRowPreview() {
    HampouchTheme {
        LabeledInputRow(
            label = "챌린지 기간",
            valueText = "5월 1일 - 5월 14일",
            onClick = {},
            suffix = "14일",
            modifier = Modifier.padding(16.dp)
        )
    }
}

@Composable
fun EditableAmountRow(
    label: String?,
    value: Int?,
    onValueChange: (Int) -> Unit,
    placeholder: String,
    modifier: Modifier = Modifier,
    icon: ImageVector? = null,
    suffix: String? = null,
    valueColor: Color? = null,
    editSeedValue: Int? = value,
    maxValue: Int? = null,
    maxValueErrorText: String? = null
) {
    var isEditing by remember { mutableStateOf(false) }
    var textFieldValue by remember { mutableStateOf(TextFieldValue("")) }
    var hasFocusedOnce by remember { mutableStateOf(false) }
    var showMaxValueError by remember { mutableStateOf(false) }
    val focusRequester = remember { FocusRequester() }
    val focusManager = LocalFocusManager.current
    val keyboardController = LocalSoftwareKeyboardController.current

    LaunchedEffect(isEditing) {
        if (isEditing) {
            val seedText = editSeedValue?.toWonText().orEmpty()
            textFieldValue = TextFieldValue(text = seedText, selection = TextRange(0, seedText.length))
            hasFocusedOnce = false
            showMaxValueError = false
            focusManager.clearFocus(force = true)
            keyboardController?.hide()
            delay(FocusHandoffDelayMillis)
            focusRequester.requestFocus()
            delay(FocusHandoffDelayMillis)
            keyboardController?.show()
        } else {
            showMaxValueError = false
        }
    }

    Column(modifier = modifier.fillMaxWidth()) {
        if (label != null) {
            Text(text = label, style = MaterialTheme.typography.labelLarge, color = HPSub1)
        }
        Row(
            modifier = Modifier
                .padding(top = if (label != null) 8.dp else 0.dp)
                .fillMaxWidth()
                .height(43.dp)
                .clip(RoundedCornerShape(9.5.dp))
                .background(HPWhite, RoundedCornerShape(9.5.dp))
                .border(1.dp, if (isEditing) HPMain else HPGray5, RoundedCornerShape(9.5.dp))
                .then(
                    if (isEditing) {
                        Modifier
                    } else {
                        Modifier.clickable {
                            hasFocusedOnce = false
                            isEditing = true
                        }
                    }
                )
                .padding(horizontal = 16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            if (icon != null) {
                Icon(imageVector = icon, contentDescription = null, tint = HPMain, modifier = Modifier.size(18.dp))
            }
            if (isEditing) {
                Box(modifier = Modifier.weight(1f)) {
                    if (textFieldValue.text.isEmpty()) {
                        Text(text = placeholder, style = MaterialTheme.typography.bodyMedium, color = HPText)
                    }
                    BasicTextField(
                        value = textFieldValue,
                        onValueChange = { newValue ->
                            val digitsOnly = newValue.text.filter(Char::isDigit).take(MaxAmountInputDigits)
                            val normalizedDigitsRaw = digitsOnly.trimStart('0')
                                .ifEmpty { if (digitsOnly.isEmpty()) "" else "0" }
                            val exceedsMax = maxValue != null &&
                                (normalizedDigitsRaw.toLongOrNull() ?: Long.MAX_VALUE) > maxValue
                            val normalizedDigits = if (exceedsMax) maxValue.toString() else normalizedDigitsRaw
                            showMaxValueError = exceedsMax
                            val strippedLeadingZeros = digitsOnly.length - normalizedDigitsRaw.length
                            val digitsBeforeCursor = newValue.text.take(newValue.selection.end).count(Char::isDigit)
                            val normalizedCursorDigits = (digitsBeforeCursor - strippedLeadingZeros).coerceAtLeast(0)
                            val formattedText = normalizedDigits.toLongOrNull()
                                ?.let { NumberFormat.getNumberInstance(Locale.KOREA).format(it) }
                                ?: normalizedDigits
                            val cursorIndex = if (exceedsMax) {
                                formattedText.length
                            } else {
                                var digitsSeen = 0
                                var index = formattedText.length
                                for ((i, char) in formattedText.withIndex()) {
                                    if (digitsSeen == normalizedCursorDigits) {
                                        index = i
                                        break
                                    }
                                    if (char.isDigit()) digitsSeen++
                                }
                                index
                            }
                            textFieldValue = TextFieldValue(text = formattedText, selection = TextRange(cursorIndex))
                            normalizedDigits.toIntOrNull()?.let(onValueChange)
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .focusRequester(focusRequester)
                            .onFocusChanged { focusState ->
                                if (focusState.isFocused) {
                                    hasFocusedOnce = true
                                } else if (hasFocusedOnce) {
                                    isEditing = false
                                }
                            },
                        textStyle = MaterialTheme.typography.bodyMedium.copy(color = HPSub1),
                        singleLine = true,
                        cursorBrush = SolidColor(HPMain),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number, imeAction = ImeAction.Done),
                        keyboardActions = KeyboardActions(onDone = {
                            keyboardController?.hide()
                            isEditing = false
                        })
                    )
                }
            } else {
                Text(
                    text = value?.toWonText() ?: placeholder,
                    style = MaterialTheme.typography.bodyMedium,
                    color = valueColor ?: if (value == null) HPText else HPSub1,
                    modifier = Modifier.weight(1f)
                )
            }
            if (suffix != null) {
                Text(text = suffix, style = MaterialTheme.typography.bodyMedium, color = HPBlack)
            }
        }
        if (showMaxValueError && maxValueErrorText != null) {
            Text(
                text = maxValueErrorText,
                style = MaterialTheme.typography.labelSmall,
                color = HPSub,
                modifier = Modifier.padding(top = 4.dp)
            )
        }
    }
}

@Preview(showBackground = true, name = "금액 입력 행")
@Composable
private fun EditableAmountRowPreview() {
    HampouchTheme {
        EditableAmountRow(
            label = "하루 지출 목표",
            value = 20_000,
            onValueChange = {},
            placeholder = "금액을 입력하세요",
            suffix = "원",
            modifier = Modifier.padding(16.dp)
        )
    }
}

@Composable
fun OnboardingPrimaryButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    containerColor: Color = HPMain
) {
    androidx.compose.material3.Button(
        onClick = onClick,
        enabled = enabled,
        modifier = modifier
            .fillMaxWidth()
            .height(56.dp),
        shape = RoundedCornerShape(10.dp),
        colors = ButtonDefaults.buttonColors(
            containerColor = containerColor,
            contentColor = HPWhite,
            disabledContainerColor = HPSub3,
            disabledContentColor = HPText
        )
    ) {
        Text(text = text, style = MaterialTheme.typography.titleSmall)
    }
}

@Preview(showBackground = true, name = "온보딩 주요 버튼")
@Composable
private fun OnboardingPrimaryButtonPreview() {
    HampouchTheme {
        OnboardingPrimaryButton(text = "다음", onClick = {}, modifier = Modifier.padding(16.dp))
    }
}

@Composable
fun OnboardingSecondaryButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    OutlinedButton(
        onClick = onClick,
        modifier = modifier
            .fillMaxWidth()
            .height(56.dp),
        shape = RoundedCornerShape(10.dp),
        border = BorderStroke(1.dp, HPMain),
        colors = ButtonDefaults.outlinedButtonColors(containerColor = HPWhite, contentColor = HPMain)
    ) {
        Text(text = text, style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
    }
}

@Preview(showBackground = true, name = "온보딩 보조 버튼")
@Composable
private fun OnboardingSecondaryButtonPreview() {
    HampouchTheme {
        OnboardingSecondaryButton(text = "이전", onClick = {}, modifier = Modifier.padding(16.dp))
    }
}

@Composable
fun PeriodPresetRow(
    options: List<Pair<Int, String>>,
    selectedDays: Int?,
    onSelect: (Int) -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        options.forEach { (days, label) ->
            val selected = days == selectedDays
            Box(
                modifier = Modifier
                    .weight(1f)
                    .height(48.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(
                        color = if (selected) HPMain else HPWhite,
                        shape = RoundedCornerShape(12.dp)
                    )
                    .border(
                        width = if (selected) 0.dp else 1.dp,
                        color = HPGray5,
                        shape = RoundedCornerShape(12.dp)
                    )
                    .clickable { onSelect(days) },
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = label,
                    style = MaterialTheme.typography.labelLarge,
                    color = if (selected) HPWhite else HPSub1,
                    maxLines = 1
                )
            }
        }
    }
}

@Preview(showBackground = true, name = "기간 프리셋 선택 행")
@Composable
private fun PeriodPresetRowPreview() {
    HampouchTheme {
        PeriodPresetRow(
            options = listOf(7 to "7일", 14 to "14일", 30 to "30일"),
            selectedDays = 14,
            onSelect = {},
            modifier = Modifier.padding(16.dp)
        )
    }
}

private val CategoryChipMinHeight = 44.dp

@Composable
fun CategoryChip(
    label: String,
    icon: ImageVector?,
    accentColor: Color,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .fillMaxHeight()
            .heightIn(min = CategoryChipMinHeight)
            .clip(RoundedCornerShape(22.dp))
            .background(
                color = if (selected) HPMain else HPWhite,
                shape = RoundedCornerShape(22.dp)
            )
            .border(
                width = 1.dp,
                color = if (selected) HPMain else HPGray5,
                shape = RoundedCornerShape(22.dp)
            )
            .clickable(onClick = onClick)
            .padding(horizontal = 10.dp, vertical = 8.dp),
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically
    ) {
        if (icon != null) {
            Box(
                modifier = Modifier
                    .size(22.dp)
                    .background(
                        color = if (selected) HPWhite else accentColor.copy(alpha = 0.15f),
                        shape = CircleShape
                    ),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = accentColor,
                    modifier = Modifier.size(13.dp)
                )
            }
            Spacer(modifier = Modifier.width(6.dp))
        }
        Text(
            text = label,
            modifier = Modifier.weight(1f, fill = false),
            style = MaterialTheme.typography.labelLarge,
            color = if (selected) HPWhite else HPSub1,
            textAlign = TextAlign.Center,
            overflow = TextOverflow.Ellipsis
        )
    }
}

@Preview(showBackground = true, name = "카테고리 칩")
@Composable
private fun CategoryChipPreview() {
    HampouchTheme {
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.padding(16.dp)) {
            CategoryChip(
                label = "식비",
                icon = Icons.Filled.ShoppingCart,
                accentColor = HPMain,
                selected = true,
                onClick = {}
            )
            CategoryChip(
                label = "카페",
                icon = Icons.Filled.ShoppingCart,
                accentColor = HPSub,
                selected = false,
                onClick = {}
            )
        }
    }
}
