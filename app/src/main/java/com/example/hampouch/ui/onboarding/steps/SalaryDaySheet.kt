package com.example.hampouch.ui.onboarding.steps

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.SheetState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.example.hampouch.R
import com.example.hampouch.ui.onboarding.OnboardingMockData
import com.example.hampouch.ui.onboarding.components.OnboardingPrimaryButton
import com.example.hampouch.ui.onboarding.components.SegmentedSelector
import com.example.hampouch.ui.theme.HPSub1
import com.example.hampouch.ui.theme.HPWhite
import com.example.hampouch.ui.theme.HampouchTheme

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SalaryDaySheet(
    initialSelectedDay: Int?,
    onSalaryDaySelected: (Int) -> Unit,
    onOpenCalendar: () -> Unit,
    onDismiss: () -> Unit,
    sheetState: SheetState,
    modifier: Modifier = Modifier
) {
    var selectedDay by remember { mutableStateOf(initialSelectedDay) }
    val directInputLabel = stringResource(R.string.onboarding_direct_input)
    val options = OnboardingMockData.salaryDayPresets.map { stringResource(it.labelResId) } + directInputLabel
    val selectedIndex = OnboardingMockData.salaryDayPresets
        .indexOfFirst { it.day == selectedDay }
        .let { if (it == -1) options.lastIndex else it }

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
            verticalArrangement = Arrangement.spacedBy(20.dp)
        ) {
            Text(
                text = stringResource(R.string.onboarding_step5_title),
                style = MaterialTheme.typography.titleSmall,
                color = HPSub1
            )

            SegmentedSelector(
                options = options,
                selectedIndex = selectedIndex,
                onSelect = { index ->
                    if (index == options.lastIndex) {
                        onOpenCalendar()
                    } else {
                        selectedDay = OnboardingMockData.salaryDayPresets[index].day
                    }
                }
            )

            OnboardingPrimaryButton(
                text = stringResource(R.string.onboarding_confirm),
                enabled = selectedDay != null,
                onClick = { selectedDay?.let(onSalaryDaySelected) }
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@androidx.compose.ui.tooling.preview.Preview(showBackground = true)
@Composable
private fun SalaryDaySheetPreview() {
    HampouchTheme {
        SalaryDaySheet(
            initialSelectedDay = 25,
            onSalaryDaySelected = {},
            onOpenCalendar = {},
            onDismiss = {},
            sheetState = androidx.compose.material3.rememberModalBottomSheetState(skipPartiallyExpanded = true)
        )
    }
}
