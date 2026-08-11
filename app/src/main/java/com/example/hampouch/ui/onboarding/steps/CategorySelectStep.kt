package com.example.hampouch.ui.onboarding.steps

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.hampouch.R
import com.example.hampouch.ui.common.ChipGrid
import com.example.hampouch.ui.onboarding.OnboardingMockData
import com.example.hampouch.ui.onboarding.components.CategoryChip
import com.example.hampouch.ui.onboarding.components.OnboardingCaptionText
import com.example.hampouch.ui.onboarding.components.OnboardingHeaderCard
import com.example.hampouch.ui.onboarding.components.OnboardingPrimaryButton
import com.example.hampouch.ui.onboarding.components.OnboardingProgressBar
import com.example.hampouch.ui.onboarding.components.OnboardingTopBar
import com.example.hampouch.ui.theme.HPBlack
import com.example.hampouch.ui.theme.HPSub1
import com.example.hampouch.ui.theme.HPSub2
import com.example.hampouch.ui.theme.HPSub3
import com.example.hampouch.ui.theme.HPText
import com.example.hampouch.ui.theme.HPWhite
import com.example.hampouch.ui.theme.HampouchTheme

@Composable
fun CategorySelectStep(
    selectedCategoryIds: Set<String>,
    onToggleCategory: (String) -> Unit,
    onStart: () -> Unit,
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    Scaffold(
        modifier = modifier.fillMaxSize()
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 20.dp, vertical = 16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            OnboardingTopBar(onBack = onBack)

            OnboardingProgressBar(currentStep = 4, totalSteps = 4)

            OnboardingHeaderCard(
                stepNumber = 4,
                stepLabel = stringResource(R.string.onboarding_step4_badge),
                title = stringResource(R.string.onboarding_step4_title)
            )

            OnboardingCaptionText(text = stringResource(R.string.onboarding_step4_caption))

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(HPSub3, RoundedCornerShape(20.dp))
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Column {
                    Text(
                        text = stringResource(R.string.onboarding_category_section_label),
                        style = MaterialTheme.typography.labelLarge,
                        color = HPBlack
                    )
                    Text(
                        text = stringResource(R.string.onboarding_category_duplicate_hint),
                        style = MaterialTheme.typography.labelSmall,
                        color = HPText.copy(alpha = 0.7f)
                    )
                }

                ChipGrid(items = OnboardingMockData.categoryOptions) { category ->
                    CategoryChip(
                        label = stringResource(category.labelResId),
                        icon = category.icon,
                        accentColor = category.accentColor,
                        selected = category.id in selectedCategoryIds,
                        onClick = { onToggleCategory(category.id) }
                    )
                }
            }

            Box(modifier = Modifier.weight(1f))

            OnboardingPrimaryButton(
                text = stringResource(R.string.onboarding_start_bytecut),
                onClick = onStart
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun CategorySelectStepPreview() {
    HampouchTheme {
        CategorySelectStep(
            selectedCategoryIds = setOf("delivery"),
            onToggleCategory = {},
            onStart = {},
            onBack = {}
        )
    }
}
