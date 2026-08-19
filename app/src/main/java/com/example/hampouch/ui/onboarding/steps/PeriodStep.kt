package com.example.hampouch.ui.onboarding.steps

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SelectableDates
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.hampouch.R
import com.example.hampouch.ui.onboarding.OnboardingMockData
import com.example.hampouch.ui.onboarding.OnboardingUiState
import com.example.hampouch.ui.onboarding.components.EditableAmountRow
import com.example.hampouch.ui.onboarding.components.LabeledInputRow
import com.example.hampouch.ui.onboarding.components.OnboardingBulletList
import com.example.hampouch.ui.onboarding.components.OnboardingCaptionText
import com.example.hampouch.ui.onboarding.components.OnboardingHeaderCard
import com.example.hampouch.ui.onboarding.components.OnboardingPrimaryButton
import com.example.hampouch.ui.onboarding.components.OnboardingProgressBar
import com.example.hampouch.ui.onboarding.components.OnboardingTopBar
import com.example.hampouch.ui.onboarding.components.PeriodPresetRow
import com.example.hampouch.ui.onboarding.components.SkipText
import com.example.hampouch.ui.theme.HPBlack
import com.example.hampouch.ui.theme.HPGray3
import com.example.hampouch.ui.theme.HPGray5
import com.example.hampouch.ui.theme.HPMain
import com.example.hampouch.ui.theme.HPSub
import com.example.hampouch.ui.theme.HPSub3
import com.example.hampouch.ui.theme.HPText
import com.example.hampouch.ui.theme.HPWhite
import com.example.hampouch.ui.theme.HampouchTheme
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneOffset

private fun LocalDate.toEpochMillisUtc(): Long = atStartOfDay(ZoneOffset.UTC).toInstant().toEpochMilli()
private fun Long.toLocalDateUtc(): LocalDate = Instant.ofEpochMilli(this).atZone(ZoneOffset.UTC).toLocalDate()

private const val MinPeriodDays = 1
private const val MaxPeriodDays = 100

internal fun directPeriodInputValue(periodDays: Int?, presetDays: Set<Int>): Int? =
    periodDays?.takeUnless { it in presetDays }

@Composable
fun PeriodStep(
    state: OnboardingUiState,
    onPeriodEnabledChange: (Boolean) -> Unit,
    onPeriodChange: (Int) -> Unit,
    onDateFixedChange: (Boolean) -> Unit,
    onStartDateChange: (LocalDate) -> Unit,
    onNext: () -> Unit,
    onBack: () -> Unit,
    onNavigateToLogin: () -> Unit,
    modifier: Modifier = Modifier
) {
    var showDatePicker by remember { mutableStateOf(false) }
    var directInputResetKey by remember { mutableIntStateOf(0) }
    var isDirectInputPending by remember { mutableStateOf(false) }
    val presetDays = remember { OnboardingMockData.periodPresets.map { it.days }.toSet() }
    val directInputValue = directPeriodInputValue(state.challengePeriodDays, presetDays)
    val daySuffix = stringResource(R.string.onboarding_day_suffix)
    val periodDaysOutOfRange = state.periodEnabled && state.challengePeriodDays != null &&
        (state.challengePeriodDays < MinPeriodDays || state.challengePeriodDays > MaxPeriodDays)

    val startDateText = state.startDate?.let { date ->
        stringResource(R.string.onboarding_start_date_format, date.year, date.monthValue, date.dayOfMonth)
    } ?: stringResource(R.string.onboarding_start_date_label)

    Scaffold(
        modifier = modifier.fillMaxSize().imePadding(),
        bottomBar = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .navigationBarsPadding()
                    .padding(horizontal = 20.dp, vertical = 16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                SkipText(text = stringResource(R.string.onboarding_skip), onClick = onNavigateToLogin)

                OnboardingPrimaryButton(
                    text = stringResource(R.string.onboarding_button_next),
                    enabled = (state.periodEnabled && state.challengePeriodDays != null && !periodDaysOutOfRange) ||
                        (state.dateFixed && state.startDate != null),
                    onClick = onNext
                )
            }
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .padding(innerPadding)
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp, vertical = 16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            OnboardingTopBar(onBack = onBack)

                OnboardingProgressBar(currentStep = 2, totalSteps = 3)

                OnboardingHeaderCard(
                    stepNumber = 2,
                    stepLabel = stringResource(R.string.onboarding_step2_badge),
                    title = stringResource(R.string.onboarding_step2_title)
                )

                OnboardingCaptionText(text = stringResource(R.string.onboarding_step2_caption))

                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(if (state.periodEnabled) HPSub3 else HPGray3, RoundedCornerShape(20.dp))
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = stringResource(R.string.onboarding_period_label),
                            style = MaterialTheme.typography.labelLarge,
                            color = HPBlack
                        )
                        Switch(
                            checked = state.periodEnabled,
                            onCheckedChange = { enabled ->
                                isDirectInputPending = false
                                onPeriodEnabledChange(enabled)
                            },
                            colors = SwitchDefaults.colors(
                                checkedThumbColor = HPWhite,
                                checkedTrackColor = HPMain,
                                uncheckedThumbColor = HPWhite,
                                uncheckedTrackColor = HPGray5
                            )
                        )
                    }

                    if (state.periodEnabled) {
                        PeriodPresetRow(
                            options = OnboardingMockData.periodPresets.map { it.days to stringResource(it.labelResId) },
                            selectedDays = state.challengePeriodDays.takeUnless { isDirectInputPending },
                            onSelect = { days ->
                                isDirectInputPending = false
                                directInputResetKey++
                                onPeriodChange(days)
                            }
                        )
                        EditableAmountRow(
                            label = null,
                            value = directInputValue,
                            onValueChange = { days ->
                                isDirectInputPending = true
                                onPeriodChange(days)
                            },
                            onValueCleared = {
                                isDirectInputPending = true
                                onPeriodChange(0)
                            },
                            placeholder = stringResource(R.string.onboarding_direct_input),
                            suffix = daySuffix,
                            onDone = { days ->
                                isDirectInputPending = false
                                if (days != null) {
                                    onPeriodChange(days)
                                    if (days in presetDays) directInputResetKey++
                                }
                            },
                            inputResetKey = directInputResetKey
                        )
                        if (periodDaysOutOfRange) {
                            Text(
                                text = stringResource(R.string.onboarding_period_range_error),
                                style = MaterialTheme.typography.labelSmall,
                                color = HPSub
                            )
                        }
                    }
                }

                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(if (state.dateFixed) HPSub3 else HPGray3, RoundedCornerShape(20.dp))
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = stringResource(R.string.onboarding_date_fixed_label),
                                style = MaterialTheme.typography.labelLarge,
                                color = HPBlack
                            )
                            Text(
                                text = stringResource(R.string.onboarding_date_fixed_description),
                                style = MaterialTheme.typography.labelSmall,
                                color = HPText
                            )
                        }
                        Switch(
                            checked = state.dateFixed,
                            onCheckedChange = onDateFixedChange,
                            colors = SwitchDefaults.colors(
                                checkedThumbColor = HPWhite,
                                checkedTrackColor = HPMain,
                                uncheckedThumbColor = HPWhite,
                                uncheckedTrackColor = HPGray5
                            )
                        )
                    }

                    OnboardingBulletList(
                        lines = listOf(
                            stringResource(R.string.onboarding_date_fixed_bullet1),
                            stringResource(R.string.onboarding_date_fixed_bullet2)
                        )
                    )

                    if (state.dateFixed) {
                        LabeledInputRow(
                            label = null,
                            valueText = startDateText,
                            icon = Icons.Filled.CalendarToday,
                            onClick = { showDatePicker = true }
                        )
                    }
                }
        }
    }

    if (showDatePicker) {
        StartDatePickerDialog(
            initialDate = state.startDate ?: LocalDate.now(),
            onDateSelected = {
                onStartDateChange(it)
                showDatePicker = false
            },
            onDismiss = { showDatePicker = false }
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun StartDatePickerDialog(
    initialDate: LocalDate,
    onDateSelected: (LocalDate) -> Unit,
    onDismiss: () -> Unit
) {
    val today = remember { LocalDate.now() }
    val minSelectableMillis = remember(today) { today.toEpochMillisUtc() }
    val maxSelectableMillis = remember(today) { today.plusDays(MaxPeriodDays.toLong() + 1).toEpochMillisUtc() }
    val datePickerState = rememberDatePickerState(
        initialSelectedDateMillis = initialDate.toEpochMillisUtc(),
        selectableDates = remember(minSelectableMillis, maxSelectableMillis) {
            object : SelectableDates {
                override fun isSelectableDate(utcTimeMillis: Long): Boolean =
                    utcTimeMillis in minSelectableMillis..maxSelectableMillis
            }
        }
    )
    DatePickerDialog(
        onDismissRequest = onDismiss,
        confirmButton = {
            TextButton(onClick = {
                datePickerState.selectedDateMillis?.let { millis -> onDateSelected(millis.toLocalDateUtc()) }
            }) {
                Text(stringResource(R.string.common_confirm))
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(stringResource(R.string.common_cancel))
            }
        }
    ) {
        DatePicker(state = datePickerState)
    }
}

@Preview(showBackground = true, name = "DateFixed enabled")
@Composable
private fun PeriodStepDateFixedPreview() {
    HampouchTheme {
        PeriodStep(
            state = OnboardingUiState(periodEnabled = false, dateFixed = true),
            onPeriodEnabledChange = {},
            onPeriodChange = {},
            onDateFixedChange = {},
            onStartDateChange = {},
            onNext = {},
            onBack = {},
            onNavigateToLogin = {}
        )
    }
}

@Preview(showBackground = true, name = "Period enabled")
@Composable
private fun PeriodStepPeriodPreview() {
    HampouchTheme {
        PeriodStep(
            state = OnboardingUiState(periodEnabled = true, challengePeriodDays = 30, dateFixed = false),
            onPeriodEnabledChange = {},
            onPeriodChange = {},
            onDateFixedChange = {},
            onStartDateChange = {},
            onNext = {},
            onBack = {},
            onNavigateToLogin = {}
        )
    }
}
