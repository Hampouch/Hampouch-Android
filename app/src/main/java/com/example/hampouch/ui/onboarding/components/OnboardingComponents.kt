package com.example.hampouch.ui.onboarding.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
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
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.text.style.TextAlign
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
import java.text.NumberFormat
import java.util.Locale

fun Int.toWonText(): String = NumberFormat.getNumberInstance(Locale.KOREA).format(this)

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

@Composable
fun SkipText(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Text(
        text = text,
        style = MaterialTheme.typography.labelLarge,
        color = HPText,
        textAlign = TextAlign.Center,
        modifier = modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(vertical = 8.dp)
    )
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
    editSeedValue: Int? = value
) {
    var isEditing by remember { mutableStateOf(false) }
    var textFieldValue by remember { mutableStateOf(TextFieldValue("")) }
    var hasFocusedOnce by remember { mutableStateOf(false) }
    val focusRequester = remember { FocusRequester() }
    val keyboardController = LocalSoftwareKeyboardController.current

    LaunchedEffect(isEditing) {
        if (isEditing) {
            val seedText = editSeedValue?.toString().orEmpty()
            textFieldValue = TextFieldValue(text = seedText, selection = TextRange(0, seedText.length))
            hasFocusedOnce = false
            focusRequester.requestFocus()
            keyboardController?.show()
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
                .background(HPWhite, RoundedCornerShape(9.5.dp))
                .border(1.dp, if (isEditing) HPMain else HPGray5, RoundedCornerShape(9.5.dp))
                .then(
                    if (isEditing) Modifier else Modifier.clickable { isEditing = true }
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
                            val digitsOnly = newValue.text.filter(Char::isDigit)
                            textFieldValue = if (digitsOnly == newValue.text) {
                                newValue
                            } else {
                                TextFieldValue(text = digitsOnly, selection = TextRange(digitsOnly.length))
                            }
                            digitsOnly.toIntOrNull()?.let(onValueChange)
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

@Composable
fun RowScope.CategoryChip(
    label: String,
    icon: ImageVector?,
    accentColor: Color,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .weight(1f)
            .height(44.dp)
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
            .padding(horizontal = 10.dp),
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
            style = MaterialTheme.typography.labelLarge,
            color = if (selected) HPWhite else HPSub1,
            maxLines = 1
        )
    }
}
