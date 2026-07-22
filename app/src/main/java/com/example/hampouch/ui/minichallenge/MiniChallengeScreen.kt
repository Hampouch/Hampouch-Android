package com.example.hampouch.ui.minichallenge

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
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
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
import java.util.UUID

private enum class MiniChallengeStep { DASHBOARD, CREATE, RECOMMENDED_LIST }

@Composable
fun MiniChallengeScreen(
    modifier: Modifier = Modifier,
    onBackClick: () -> Unit = {},
    onNotificationClick: () -> Unit = {}
) {
    var step by remember { mutableStateOf(MiniChallengeStep.DASHBOARD) }
    var todayChallenges by remember { mutableStateOf(MiniChallengeMockData.todayChallenges()) }
    var recommendedChallenges by remember { mutableStateOf(MiniChallengeMockData.recommendedChallenges()) }

    fun addChallenge(name: String, periodLabel: String) {
        todayChallenges = todayChallenges + MiniChallengeEntry(
            id = UUID.randomUUID().toString(),
            name = name.ifBlank { "이름 없는 챌린지" },
            periodLabel = periodLabel,
            isChecked = false
        )
    }

    fun addRecommendedChallenge(recommended: RecommendedMiniChallenge) {
        addChallenge(recommended.name, recommended.periodLabel)
        recommendedChallenges = recommendedChallenges.filterNot { it.id == recommended.id }
    }

    when (step) {
        MiniChallengeStep.CREATE -> MiniChallengeCreateScreen(
            modifier = modifier,
            onBackClick = { step = MiniChallengeStep.DASHBOARD },
            onAddChallengeClick = { name, periodLabel ->
                addChallenge(name, periodLabel)
                step = MiniChallengeStep.DASHBOARD
            }
        )

        MiniChallengeStep.RECOMMENDED_LIST -> MiniChallengeRecommendedListScreen(
            modifier = modifier,
            recommendedChallenges = recommendedChallenges,
            onBackClick = { step = MiniChallengeStep.DASHBOARD },
            onNotificationClick = onNotificationClick,
            onAddChallenge = { recommended: RecommendedMiniChallenge ->
                addRecommendedChallenge(recommended)
                step = MiniChallengeStep.DASHBOARD
            }
        )

        MiniChallengeStep.DASHBOARD -> MiniChallengeDashboardScreen(
            modifier = modifier,
            todayChallenges = todayChallenges,
            recommendedChallenges = recommendedChallenges,
            onToggleChallenge = { id ->
                todayChallenges = todayChallenges.map {
                    if (it.id == id) it.copy(isChecked = !it.isChecked) else it
                }
            },
            onAddRecommendedChallenge = ::addRecommendedChallenge,
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
    todayChallenges: List<MiniChallengeEntry>,
    recommendedChallenges: List<RecommendedMiniChallenge>,
    onToggleChallenge: (String) -> Unit,
    onAddRecommendedChallenge: (RecommendedMiniChallenge) -> Unit,
    onBackClick: () -> Unit = {},
    onNotificationClick: () -> Unit = {},
    onStartNewChallengeClick: () -> Unit = {},
    onViewAllRecommendedClick: () -> Unit = {}
) {
    val referenceToday = remember { LocalDate.now() }
    var selectedDate by remember { mutableStateOf(referenceToday) }
    var pendingChallenge by remember { mutableStateOf<RecommendedMiniChallenge?>(null) }

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
                    referenceToday.minusDays(1),
                    referenceToday,
                    referenceToday.plusDays(1)
                ),
                selectedDate = selectedDate,
                onDateSelected = { selectedDate = it }
            )
            Spacer(modifier = Modifier.height(20.dp))
            MiniChallengeSummaryCard(
                completedCount = todayChallenges.count { it.isChecked },
                totalCount = todayChallenges.size,
                streakDays = MiniChallengeMockData.streakDays
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
                            MiniChallengeItemRow(
                                item = item,
                                onToggle = { onToggleChallenge(item.id) }
                            )
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
                            pendingChallenge = recommendedChallenges.find { it.id == id }
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
