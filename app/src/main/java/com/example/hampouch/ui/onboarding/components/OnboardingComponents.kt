package com.example.hampouch.ui.onboarding.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.SheetState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.example.hampouch.R
import com.example.hampouch.ui.theme.HPGray5
import com.example.hampouch.ui.theme.HPMain
import com.example.hampouch.ui.theme.HPSub1
import com.example.hampouch.ui.theme.HPSub3
import com.example.hampouch.ui.theme.HPText
import com.example.hampouch.ui.theme.HPWhite
import java.text.NumberFormat
import java.util.Locale

fun Int.toWonText(): String = NumberFormat.getNumberInstance(Locale.KOREA).format(this)

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
            val isFilled = index < currentStep
            Box(
                modifier = Modifier
                    .height(5.dp)
                    .weight(1f)
                    .background(
                        color = if (isFilled) HPMain else HPText,
                        shape = RoundedCornerShape(2.5.dp)
                    )
            )
        }
    }
}

@Composable
fun OnboardingHeaderCard(
    title: String,
    subtitle: String,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .height(139.dp)
            .background(HPSub3, RoundedCornerShape(30.dp))
            .padding(horizontal = 20.dp),
        verticalArrangement = Arrangement.Center
    ) {
        Text(text = title, style = MaterialTheme.typography.titleMedium, color = HPSub1)
        Text(
            text = subtitle,
            style = MaterialTheme.typography.bodySmall,
            color = HPText,
            modifier = Modifier.padding(top = 6.dp)
        )
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

@Composable
fun LabeledInputRow(
    label: String,
    valueText: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier.fillMaxWidth()) {
        Text(text = label, style = MaterialTheme.typography.labelLarge, color = HPSub1)
        Row(
            modifier = Modifier
                .padding(top = 8.dp)
                .fillMaxWidth()
                .height(43.dp)
                .background(HPWhite, RoundedCornerShape(9.5.dp))
                .border(1.dp, HPGray5, RoundedCornerShape(9.5.dp))
                .clickable(onClick = onClick)
                .padding(horizontal = 16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = valueText,
                style = MaterialTheme.typography.bodyMedium,
                color = if (valueText.isEmpty()) HPText else HPSub1
            )
            Icon(
                imageVector = Icons.Filled.ChevronRight,
                contentDescription = null,
                tint = HPText
            )
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
fun FloatingNextButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    IconButton(
        onClick = onClick,
        modifier = modifier
            .size(50.dp)
            .background(HPMain, CircleShape)
    ) {
        Icon(
            imageVector = Icons.Filled.ArrowForward,
            contentDescription = stringResource(R.string.cd_next),
            tint = HPWhite
        )
    }
}

@Composable
fun SegmentedSelector(
    options: List<String>,
    selectedIndex: Int,
    onSelect: (Int) -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        options.forEachIndexed { index, label ->
            val selected = index == selectedIndex
            Box(
                modifier = Modifier
                    .weight(1f)
                    .height(43.dp)
                    .background(
                        color = if (selected) HPMain else HPWhite,
                        shape = RoundedCornerShape(21.5.dp)
                    )
                    .border(
                        width = if (selected) 0.dp else 1.dp,
                        color = HPGray5,
                        shape = RoundedCornerShape(21.5.dp)
                    )
                    .clickable { onSelect(index) },
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
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .weight(1f)
            .height(39.dp)
            .background(
                color = if (selected) HPMain else HPWhite,
                shape = RoundedCornerShape(19.5.dp)
            )
            .border(
                width = 1.dp,
                color = if (selected) HPMain else HPGray5,
                shape = RoundedCornerShape(19.5.dp)
            )
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelLarge,
            color = if (selected) HPWhite else HPSub1
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AmountInputSheet(
    title: String,
    amountText: String,
    onAmountTextChange: (String) -> Unit,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit,
    sheetState: SheetState,
    modifier: Modifier = Modifier,
    confirmEnabled: Boolean = amountText.isNotBlank()
) {
    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = HPWhite,
        modifier = modifier
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp)
                .padding(bottom = 32.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(text = title, style = MaterialTheme.typography.titleSmall, color = HPSub1)

            Image(
                painter = painterResource(R.drawable.img_hamster_mascot),
                contentDescription = stringResource(R.string.cd_hamster_mascot),
                modifier = Modifier
                    .padding(top = 12.dp)
                    .size(118.dp)
            )

            OutlinedTextField(
                value = amountText,
                onValueChange = { newValue -> onAmountTextChange(newValue.filter(Char::isDigit)) },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 20.dp),
                placeholder = { Text(stringResource(R.string.onboarding_amount_placeholder)) },
                suffix = { Text(stringResource(R.string.onboarding_won_suffix)) },
                singleLine = true,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                shape = RoundedCornerShape(9.5.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = HPMain,
                    unfocusedBorderColor = HPGray5
                )
            )

            OnboardingPrimaryButton(
                text = stringResource(R.string.onboarding_confirm),
                onClick = onConfirm,
                enabled = confirmEnabled,
                modifier = Modifier.padding(top = 20.dp)
            )
        }
    }
}
