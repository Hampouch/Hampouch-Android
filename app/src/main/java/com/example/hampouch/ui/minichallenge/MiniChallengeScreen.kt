package com.example.hampouch.ui.minichallenge

import com.example.hampouch.domain.model.normalizeMiniChallengeName
import android.widget.Toast
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.hampouch.R
import com.example.hampouch.domain.model.MiniChallengeEntry
import com.example.hampouch.domain.model.RecommendedMiniChallenge
import com.example.hampouch.domain.model.toUserMessage
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
import kotlinx.coroutines.launch

private enum class MiniChallengeStep { DASHBOARD, CREATE, RECOMMENDED_LIST }

@Composable
fun MiniChallengeScreen(
    modifier: Modifier = Modifier,
    initialDate: LocalDate = LocalDate.now(),
    onBackClick: () -> Unit,
    onNotificationClick: () -> Unit,
    viewModel: MiniChallengeViewModel = hiltViewModel()
) {
    var step by remember { mutableStateOf(MiniChallengeStep.DASHBOARD) }
    var selectedDate by remember(initialDate) { mutableStateOf(initialDate) }

    val context = LocalContext.current
    val miniChallengeState by viewModel.state.collectAsStateWithLifecycle()
    val futureDateMessage = stringResource(R.string.minichallenge_future_date_blocked)

    LaunchedEffect(selectedDate) { viewModel.loadChallenges(selectedDate) }
    LaunchedEffect(Unit) { viewModel.loadRecommended() }
    LaunchedEffect(viewModel) {
        viewModel.events.collect { event ->
            when (event) {
                is MiniChallengeEvent.Added -> {
                    selectedDate = event.date
                    step = MiniChallengeStep.DASHBOARD
                }
                is MiniChallengeEvent.ShowMessage ->
                    Toast.makeText(context, event.message, Toast.LENGTH_SHORT).show()
            }
        }
    }

    when (step) {
        MiniChallengeStep.CREATE -> MiniChallengeCreateScreen(
            modifier = modifier,
            existingNames = miniChallengeState.challengesFor(selectedDate).map { it.name },
            onBackClick = { step = MiniChallengeStep.DASHBOARD },
            onAddChallengeClick = { name, duration ->
                viewModel.addCustom(selectedDate, name, duration)
            }
        )

        MiniChallengeStep.RECOMMENDED_LIST -> MiniChallengeRecommendedListScreen(
            modifier = modifier,
            recommendedChallenges = miniChallengeState.recommendedChallenges,
            existingNames = miniChallengeState.challengesFor(selectedDate).map { it.name },
            onBackClick = { step = MiniChallengeStep.DASHBOARD },
            onNotificationClick = onNotificationClick,
            onAddChallenge = { recommended: RecommendedMiniChallenge ->
                viewModel.addRecommended(selectedDate, recommended)
            }
        )

        MiniChallengeStep.DASHBOARD -> MiniChallengeDashboardScreen(
            modifier = modifier,
            selectedDate = selectedDate,
            onDateSelected = { date ->
                if (date.isAfter(LocalDate.now())) {
                    Toast.makeText(context, futureDateMessage, Toast.LENGTH_SHORT).show()
                } else {
                    selectedDate = date
                }
            },
            todayChallenges = miniChallengeState.challengesFor(selectedDate),
            recommendedChallenges = miniChallengeState.recommendedChallenges,
            streakDaysOverride = miniChallengeState.summaryFor(selectedDate)?.streakDays,
            onToggleChallenge = { id ->
                miniChallengeState.challengesFor(selectedDate).find { it.id == id }?.let { target ->
                    viewModel.setChecked(selectedDate, id, !target.isChecked)
                }
            },
            onDeleteChallenge = { id ->
                viewModel.remove(selectedDate, id)
            },
            onAddRecommendedChallenge = { recommended ->
                viewModel.addRecommended(selectedDate, recommended)
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
    selectedDate: LocalDate,
    onDateSelected: (LocalDate) -> Unit,
    todayChallenges: List<MiniChallengeEntry>,
    recommendedChallenges: List<RecommendedMiniChallenge>,
    streakDaysOverride: Int? = null,
    onToggleChallenge: (String) -> Unit,
    onDeleteChallenge: (String) -> Unit,
    onAddRecommendedChallenge: (RecommendedMiniChallenge) -> Unit,
    onBackClick: () -> Unit,
    onNotificationClick: () -> Unit,
    onStartNewChallengeClick: () -> Unit,
    onViewAllRecommendedClick: () -> Unit
) {
    var pendingChallenge by remember { mutableStateOf<RecommendedMiniChallenge?>(null) }
    val context = LocalContext.current
    val duplicateNameMessage = stringResource(R.string.minichallenge_name_duplicate_error)
    val today = remember { LocalDate.now() }

    Scaffold(
        modifier = modifier,
        containerColor = HPGray2,
        topBar = {
            MiniChallengeTopBar(
                onBackClick = onBackClick,
                onNotificationClick = onNotificationClick
            )
        },
        bottomBar = {
            Button(
                onClick = onStartNewChallengeClick,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp)
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
        }
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 20.dp)
        ) {
            item(contentType = "date") {
                Spacer(modifier = Modifier.height(10.dp))
                MiniChallengeDateRow(
                    dates = listOf(
                        selectedDate.minusDays(1),
                        selectedDate,
                        selectedDate.plusDays(1)
                    ),
                    selectedDate = selectedDate,
                    today = today,
                    onDateSelected = onDateSelected
                )
                Spacer(modifier = Modifier.height(20.dp))
            }
            item(contentType = "summary") {
                MiniChallengeSummaryCard(
                    completedCount = todayChallenges.count { it.isChecked },
                    totalCount = todayChallenges.size,
                    streakDays = streakDaysOverride ?: (todayChallenges.maxOfOrNull { it.achievedDays } ?: 0)
                )
                Spacer(modifier = Modifier.height(20.dp))
            }
            item(contentType = "section_header") {
                Text(
                    stringResource(R.string.minichallenge_today_title),
                    style = MaterialTheme.typography.titleSmall,
                    color = HPBlack
                )
                Spacer(modifier = Modifier.height(12.dp))
            }
            if (todayChallenges.isEmpty()) {
                item(contentType = "empty_state") {
                    EmptyStateBlock(title = stringResource(R.string.minichallenge_today_empty))
                }
            } else {
                items(
                    items = todayChallenges,
                    key = { it.id },
                    contentType = { "today_challenge" }
                ) { item ->
                    MiniChallengeItemRow(
                        item = item,
                        onToggle = { onToggleChallenge(item.id) },
                        onDelete = { onDeleteChallenge(item.id) }
                    )
                    Spacer(modifier = Modifier.height(10.dp))
                }
            }
            item(contentType = "recommended") {
                Spacer(modifier = Modifier.height(10.dp))
                SectionHeader(
                    title = stringResource(R.string.minichallenge_recommended_title),
                    onViewAllClick = onViewAllRecommendedClick
                )
                Spacer(modifier = Modifier.height(5.dp))
                if (recommendedChallenges.isEmpty()) {
                    EmptyStateBlock(title = stringResource(R.string.minichallenge_recommended_empty))
                } else {
                    RecommendedMiniChallengeRow(
                        items = recommendedChallenges,
                        onAddClick = { id ->
                            val recommended = recommendedChallenges.find { it.id == id }
                            if (recommended != null && todayChallenges.any { normalizeMiniChallengeName(it.name) == normalizeMiniChallengeName(recommended.name) }) {
                                Toast.makeText(context, duplicateNameMessage, Toast.LENGTH_SHORT).show()
                            } else {
                                pendingChallenge = recommended
                            }
                        }
                    )
                }
                Spacer(modifier = Modifier.height(20.dp))
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
                onAddRecommendedChallenge(challenge)
            }
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun MiniChallengeScreenPreview() {
    HampouchTheme {
        MiniChallengeScreen(onBackClick = {}, onNotificationClick = {})
    }
}
