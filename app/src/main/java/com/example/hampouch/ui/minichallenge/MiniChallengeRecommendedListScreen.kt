package com.example.hampouch.ui.minichallenge

import com.example.hampouch.domain.model.normalizeMiniChallengeName
import android.widget.Toast
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.hampouch.R
import com.example.hampouch.domain.model.RecommendedMiniChallenge
import com.example.hampouch.ui.dialog.MiniChallengeAddConfirmDialog
import com.example.hampouch.ui.home.components.EmptyStateBlock
import com.example.hampouch.ui.minichallenge.components.MiniChallengeFilterTabRow
import com.example.hampouch.ui.minichallenge.components.MiniChallengeTopBar
import com.example.hampouch.ui.minichallenge.components.RecommendedMiniChallengeListCard
import com.example.hampouch.ui.theme.HPGray2
import com.example.hampouch.ui.theme.HampouchTheme

@Composable
fun MiniChallengeRecommendedListScreen(
    recommendedChallenges: List<RecommendedMiniChallenge>,
    modifier: Modifier = Modifier,
    existingNames: List<String> = emptyList(),
    onBackClick: () -> Unit,
    onNotificationClick: () -> Unit,
    onAddChallenge: (RecommendedMiniChallenge) -> Unit
) {
    val durationOptions = listOf(
        stringResource(R.string.minichallenge_duration_today),
        stringResource(R.string.minichallenge_duration_3days),
        stringResource(R.string.minichallenge_duration_7days),
        stringResource(R.string.minichallenge_duration_14days),
        stringResource(R.string.minichallenge_duration_31days)
    )
    var selectedDurationIndex by remember { mutableStateOf(0) }
    var pendingChallenge by remember { mutableStateOf<RecommendedMiniChallenge?>(null) }
    val context = LocalContext.current
    val duplicateNameMessage = stringResource(R.string.minichallenge_name_duplicate_error)

    val filteredChallenges = remember(selectedDurationIndex, recommendedChallenges) {
        val selectedTotalDays = MiniChallengeDurationDayValues[selectedDurationIndex]
        recommendedChallenges.filter { it.duration.serverDays == (selectedTotalDays ?: 1) }
    }

    Scaffold(
        modifier = modifier,
        containerColor = HPGray2,
        topBar = {
            MiniChallengeTopBar(
                title = stringResource(R.string.minichallenge_recommended_title),
                onBackClick = onBackClick,
                onNotificationClick = onNotificationClick
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            MiniChallengeFilterTabRow(
                options = durationOptions,
                selectedIndex = selectedDurationIndex,
                onSelect = { selectedDurationIndex = it },
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 16.dp)
            )
            if (filteredChallenges.isEmpty()) {
                EmptyStateBlock(
                    title = stringResource(R.string.minichallenge_recommended_empty),
                    modifier = Modifier.padding(horizontal = 20.dp)
                )
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(horizontal = 20.dp, vertical = 4.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(items = filteredChallenges, key = { it.id }) { item ->
                        RecommendedMiniChallengeListCard(
                            item = item,
                            onAddClick = {
                                if (existingNames.any { normalizeMiniChallengeName(it) == normalizeMiniChallengeName(item.name) }) {
                                    Toast.makeText(context, duplicateNameMessage, Toast.LENGTH_SHORT).show()
                                } else {
                                    pendingChallenge = item
                                }
                            }
                        )
                    }
                }
            }
        }
    }

    val challenge = pendingChallenge
    if (challenge != null) {
        MiniChallengeAddConfirmDialog(
            name = challenge.name,
            periodLabel = challenge.periodLabel,
            onCancel = { pendingChallenge = null },
            onConfirm = {
                pendingChallenge = null
                onAddChallenge(challenge)
            }
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun MiniChallengeRecommendedListScreenPreview() {
    HampouchTheme {
        MiniChallengeRecommendedListScreen(
            recommendedChallenges = MiniChallengeMockData.recommendedChallenges(),
            onBackClick = {}, onNotificationClick = {}, onAddChallenge = {}
        )
    }
}
