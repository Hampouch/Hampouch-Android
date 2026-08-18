package com.example.hampouch.ui.minichallenge

import com.example.hampouch.domain.model.normalizeMiniChallengeName
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.consumeWindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.hampouch.R
import com.example.hampouch.ui.common.FieldMessage
import com.example.hampouch.ui.dialog.MiniChallengeAddConfirmDialog
import com.example.hampouch.ui.minichallenge.components.MiniChallengeCreateTopBar
import com.example.hampouch.ui.minichallenge.components.MiniChallengeDurationRow
import com.example.hampouch.ui.minichallenge.components.MiniChallengeNameField
import com.example.hampouch.ui.theme.HPBlack
import com.example.hampouch.ui.theme.HPGray5
import com.example.hampouch.ui.theme.HPMain
import com.example.hampouch.ui.theme.HPSub
import com.example.hampouch.ui.theme.HPSub4
import com.example.hampouch.ui.theme.HPText
import com.example.hampouch.ui.theme.HPWhite
import com.example.hampouch.ui.theme.HampouchTheme
import com.example.hampouch.domain.model.MiniChallengeDuration
import com.example.hampouch.domain.model.miniChallengeDuration
import com.example.hampouch.ui.theme.HPSub3

@Composable
fun MiniChallengeCreateScreen(
    modifier: Modifier = Modifier,
    existingNames: List<String> = emptyList(),
    onBackClick: () -> Unit,
    onAddChallengeClick: (name: String, duration: MiniChallengeDuration) -> Unit
) {
    var name by remember { mutableStateOf("") }
    val durationOptions = listOf(
        stringResource(R.string.minichallenge_duration_today),
        stringResource(R.string.minichallenge_duration_3days),
        stringResource(R.string.minichallenge_duration_7days),
        stringResource(R.string.minichallenge_duration_14days),
        stringResource(R.string.minichallenge_duration_31days)
    )
    var selectedDurationIndex by remember { mutableStateOf<Int?>(null) }
    var showConfirmDialog by remember { mutableStateOf(false) }
    var showDuplicateNameError by remember { mutableStateOf(false) }
    val selectedDurationDays = MiniChallengeDurationDayValues.getOrNull(selectedDurationIndex ?: -1)
    val selectedDuration = if (selectedDurationDays == null) {
        null
    } else {
        miniChallengeDuration(selectedDurationDays)
    }
    val isFormValid = name.isNotBlank() && selectedDurationIndex != null

    Scaffold(
        modifier = modifier,
        containerColor = HPWhite,
        topBar = { MiniChallengeCreateTopBar(onBackClick = onBackClick) },
        bottomBar = {
            Button(
                onClick = {
                    if (existingNames.any { normalizeMiniChallengeName(it) == normalizeMiniChallengeName(name) }) {
                        showDuplicateNameError = true
                    } else {
                        showDuplicateNameError = false
                        showConfirmDialog = true
                    }
                },
                enabled = isFormValid,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp)
                    .height(52.dp),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = HPMain,
                    contentColor = HPWhite,
                    disabledContainerColor = HPGray5,
                    disabledContentColor = HPWhite
                )
            ) {
                Text(
                    stringResource(R.string.minichallenge_add_challenge_button),
                    style = MaterialTheme.typography.titleSmall
                )
            }
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .consumeWindowInsets(innerPadding)
                .verticalScroll(rememberScrollState())
                .imePadding()
                .padding(horizontal = 20.dp)
        ) {
            Spacer(modifier = Modifier.height(12.dp))
            Text(
                text = stringResource(R.string.minichallenge_label),
                style = MaterialTheme.typography.labelLarge,
                fontWeight = FontWeight.Bold,
                color = HPSub
            )
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = stringResource(R.string.minichallenge_create_headline),
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = HPBlack
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = stringResource(R.string.minichallenge_create_subtitle_line1),
                style = MaterialTheme.typography.bodySmall,
                color = HPText
            )
            Text(
                text = stringResource(R.string.minichallenge_create_subtitle_line2),
                style = MaterialTheme.typography.bodySmall,
                color = HPText
            )
            Spacer(modifier = Modifier.height(30.dp))

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(20.dp))
                    .background(HPSub3)
                    .padding(horizontal = 12.dp, vertical = 20.dp)
            ) {
                Text(
                    text = stringResource(R.string.minichallenge_name_label),
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Bold,
                    color = HPBlack
                )
                Spacer(modifier = Modifier.height(10.dp))
                MiniChallengeNameField(
                    value = name,
                    onValueChange = {
                        name = it
                        showDuplicateNameError = false
                    }
                )
                if (showDuplicateNameError) {
                    FieldMessage(stringResource(R.string.minichallenge_name_duplicate_error))
                }
                Spacer(modifier = Modifier.height(20.dp))
                Text(
                    text = stringResource(R.string.minichallenge_period_label),
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Bold,
                    color = HPBlack
                )
                Spacer(modifier = Modifier.height(10.dp))
                MiniChallengeDurationRow(
                    options = durationOptions,
                    selectedIndex = selectedDurationIndex,
                    onSelect = { selectedDurationIndex = it }
                )
            }
        }
    }

    if (showConfirmDialog) {
        MiniChallengeAddConfirmDialog(
            name = name,
            periodLabel = when (val duration = selectedDuration) {
                MiniChallengeDuration.Today -> "오늘만"
                is MiniChallengeDuration.Period -> "${duration.serverDays}일간"
                null -> ""
            },
            onCancel = { showConfirmDialog = false },
            onConfirm = {
                showConfirmDialog = false
                onAddChallengeClick(name, requireNotNull(selectedDuration))
            }
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun MiniChallengeCreateScreenPreview() {
    HampouchTheme {
        MiniChallengeCreateScreen(onBackClick = {}, onAddChallengeClick = { _, _ -> })
    }
}
