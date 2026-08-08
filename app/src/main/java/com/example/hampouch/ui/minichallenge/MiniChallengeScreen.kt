package com.example.hampouch.ui.minichallenge

import android.widget.Toast
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
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
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.hampouch.R
import com.example.hampouch.data.model.MiniChallengeEntry
import com.example.hampouch.data.model.RecommendedMiniChallenge
import com.example.hampouch.ui.dialog.MiniChallengeAddConfirmDialog
import com.example.hampouch.ui.home.components.EmptyStateBlock
import com.example.hampouch.ui.home.components.SectionHeader
import com.example.hampouch.ui.minichallenge.components.MiniChallengeDateRow
import com.example.hampouch.ui.minichallenge.components.MiniChallengeItemRow
import com.example.hampouch.ui.minichallenge.components.MiniChallengeSummaryCard
import com.example.hampouch.ui.minichallenge.components.MiniChallengeTopBar
import com.example.hampouch.ui.minichallenge.components.RecommendedMiniChallengeRow
import com.example.hampouch.ui.theme.HPBlack
import com.example.hampouch.ui.theme.HPGray2
import com.example.hampouch.ui.theme.HPMain
import com.example.hampouch.ui.theme.HPWhite
import com.example.hampouch.ui.theme.HampouchTheme
import java.time.LocalDate

private enum class MiniChallengeStep { DASHBOARD, CREATE, RECOMMENDED_LIST }

@Composable
fun MiniChallengeScreen(
    modifier: Modifier = Modifier,
    initialDate: LocalDate = LocalDate.now(),
    onBackClick: () -> Unit = {},
    onNotificationClick: () -> Unit = {}
) {
    var step by remember { mutableStateOf(MiniChallengeStep.DASHBOARD) }
    val anchorDate = remember(initialDate) { initialDate }
    var selectedDate by remember(initialDate) { mutableStateOf(initialDate) }

    when (step) {
        MiniChallengeStep.CREATE -> MiniChallengeCreateScreen(
            modifier = modifier,
            existingNames = MiniChallengeStore.challengesFor(selectedDate).map { it.name },
            onBackClick = { step = MiniChallengeStep.DASHBOARD },
            onAddChallengeClick = { name, totalDays ->
                if (MiniChallengeStore.addChallenge(selectedDate, name, totalDays)) {
                    step = MiniChallengeStep.DASHBOARD
                }
            }
        )

        MiniChallengeStep.RECOMMENDED_LIST -> MiniChallengeRecommendedListScreen(
            modifier = modifier,
            recommendedChallenges = MiniChallengeStore.recommendedChallenges,
            existingNames = MiniChallengeStore.challengesFor(selectedDate).map { it.name },
            onBackClick = { step = MiniChallengeStep.DASHBOARD },
            onNotificationClick = onNotificationClick,
            onAddChallenge = { recommended: RecommendedMiniChallenge ->
                if (MiniChallengeStore.addRecommendedChallenge(selectedDate, recommended)) {
                    step = MiniChallengeStep.DASHBOARD
                }
            }
        )

        MiniChallengeStep.DASHBOARD -> MiniChallengeDashboardScreen(
            modifier = modifier,
            anchorDate = anchorDate,
            selectedDate = selectedDate,
            onDateSelected = { selectedDate = it },
            todayChallenges = MiniChallengeStore.challengesFor(selectedDate),
            recommendedChallenges = MiniChallengeStore.recommendedChallenges,
            onToggleChallenge = { id -> MiniChallengeStore.toggle(selectedDate, id) },
            onDeleteChallenge = { id -> MiniChallengeStore.removeChallenge(selectedDate, id) },
            onAddRecommendedChallenge = { recommended ->
                MiniChallengeStore.addRecommendedChallenge(selectedDate, recommended)
            },
            onBackClick = onBackClick,
            onNotificationClick = onNotificationClick,
            onStartNewChallengeClick = { step = MiniChallengeStep.CREATE },
            onViewAllRecommendedClick = { step = MiniChallengeStep.RECOMMENDED_LIST }
        )
    }
}

@Composable
private fun MiniChallengeDashboardScreen(
    modifier: Modifier = Modifier,
    anchorDate: LocalDate,
    selectedDate: LocalDate,
    onDateSelected: (LocalDate) -> Unit,
    todayChallenges: List<MiniChallengeEntry>,
    recommendedChallenges: List<RecommendedMiniChallenge>,
    onToggleChallenge: (String) -> Unit,
    onDeleteChallenge: (String) -> Unit,
    onAddRecommendedChallenge: (RecommendedMiniChallenge) -> Unit,
    onBackClick: () -> Unit = {},
    onNotificationClick: () -> Unit = {},
    onStartNewChallengeClick: () -> Unit = {},
    onViewAllRecommendedClick: () -> Unit = {}
) {
    var pendingChallenge by remember { mutableStateOf<RecommendedMiniChallenge?>(null) }
    val context = LocalContext.current
    val duplicateNameMessage = stringResource(R.string.minichallenge_name_duplicate_error)

    Scaffold(
        modifier = modifier,
        containerColor = HPGray2,
        topBar = {
            MiniChallengeTopBar(
                onBackClick = onBackClick,
                onNotificationClick = onNotificationClick
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp)
        ) {
            Spacer(modifier = Modifier.height(16.dp))
            MiniChallengeDateRow(
                dates = listOf(
                    anchorDate.minusDays(1),
                    anchorDate,
                    anchorDate.plusDays(1)
                ),
                selectedDate = selectedDate,
                onDateSelected = onDateSelected
            )
            Spacer(modifier = Modifier.height(20.dp))
            MiniChallengeSummaryCard(
                completedCount = todayChallenges.count { it.isChecked },
                totalCount = todayChallenges.size,
                streakDays = todayChallenges.maxOfOrNull { it.achievedDays } ?: 0
            )
            Spacer(modifier = Modifier.height(28.dp))

            Column {
                Text(
                    stringResource(R.string.minichallenge_today_title),
                    style = MaterialTheme.typography.titleSmall,
                    color = HPBlack
                )
                Spacer(modifier = Modifier.height(12.dp))
                if (todayChallenges.isEmpty()) {
                    EmptyStateBlock(title = stringResource(R.string.minichallenge_today_empty))
                } else {
                    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        todayChallenges.forEach { item ->
                            key(item.id) {
                                MiniChallengeItemRow(
                                    item = item,
                                    onToggle = { onToggleChallenge(item.id) },
                                    onDelete = { onDeleteChallenge(item.id) }
                                )
                            }
                        }
                    }
                }
            }
            Spacer(modifier = Modifier.height(28.dp))

            Column {
                SectionHeader(
                    title = stringResource(R.string.minichallenge_recommended_title),
                    onViewAllClick = onViewAllRecommendedClick
                )
                Spacer(modifier = Modifier.height(12.dp))
                if (recommendedChallenges.isEmpty()) {
                    EmptyStateBlock(title = stringResource(R.string.minichallenge_recommended_empty))
                } else {
                    RecommendedMiniChallengeRow(
                        items = recommendedChallenges,
                        onAddClick = { id ->
                            val recommended = recommendedChallenges.find { it.id == id }
                            if (recommended != null && todayChallenges.any { MiniChallengeStore.normalizeName(it.name) == MiniChallengeStore.normalizeName(recommended.name) }) {
                                Toast.makeText(context, duplicateNameMessage, Toast.LENGTH_SHORT).show()
                            } else {
                                pendingChallenge = recommended
                            }
                        }
                    )
                }
            }
            Spacer(modifier = Modifier.height(28.dp))

            Button(
                onClick = onStartNewChallengeClick,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = HPMain,
                    contentColor = HPWhite
                )
            ) {
                Text(
                    stringResource(R.string.minichallenge_start_button),
                    style = MaterialTheme.typography.titleSmall
                )
            }
            Spacer(modifier = Modifier.height(20.dp))
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
                onAddRecommendedChallenge(challenge)
            }
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun MiniChallengeScreenPreview() {
    HampouchTheme {
        MiniChallengeScreen()
    }
}
