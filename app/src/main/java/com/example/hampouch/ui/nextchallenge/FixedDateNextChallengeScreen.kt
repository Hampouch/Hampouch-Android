package com.example.hampouch.ui.nextchallenge

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
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
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.hampouch.domain.model.FixedDateChallengeDraft
import com.example.hampouch.domain.model.FixedDateDraftState
import com.example.hampouch.ui.challengeresult.formatWon
import com.example.hampouch.ui.expensedetail.DashedDivider
import com.example.hampouch.ui.theme.Body16Bold
import com.example.hampouch.ui.theme.HPBlack
import com.example.hampouch.ui.theme.HPGray2
import com.example.hampouch.ui.theme.HPMain
import com.example.hampouch.ui.theme.HPSub
import com.example.hampouch.ui.theme.HPSub2
import com.example.hampouch.ui.theme.HPSub3
import com.example.hampouch.ui.theme.HPText
import com.example.hampouch.ui.theme.HPWhite
import com.example.hampouch.ui.theme.HampouchTheme
import java.time.LocalDate

@Composable
fun FixedDateNextChallengeRoute(
    onBackClick: () -> Unit,
    onStartChallengeClick: () -> Unit,
    onEditSettingsClick: (FixedDateChallengeDraft) -> Unit,
    modifier: Modifier = Modifier,
    viewModel: FixedDateNextChallengeViewModel = hiltViewModel()
) {
    val draft by viewModel.draft.collectAsStateWithLifecycle()
    val context = LocalContext.current

    LaunchedEffect(viewModel) {
        viewModel.loadDraft()
        viewModel.events.collect { event ->
            when (event) {
                FixedDateNextChallengeEvent.Started -> onStartChallengeClick()
                FixedDateNextChallengeEvent.Unavailable -> onBackClick()
                is FixedDateNextChallengeEvent.ShowMessage ->
                    Toast.makeText(context, event.message, Toast.LENGTH_SHORT).show()
            }
        }
    }

    draft?.takeIf { it.isDue }?.let { currentDraft ->
        FixedDateNextChallengeScreen(
            draft = currentDraft,
            onBackClick = onBackClick,
            onStartChallengeClick = { viewModel.startChallenge(currentDraft) },
            onEditSettingsClick = { onEditSettingsClick(currentDraft) },
            modifier = modifier
        )
    }
}

@Composable
@Suppress("LongMethod", "UnusedParameter")
fun FixedDateNextChallengeScreen(
    draft: FixedDateChallengeDraft,
    onBackClick: () -> Unit,
    onStartChallengeClick: () -> Unit,
    onEditSettingsClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Scaffold(modifier = modifier, containerColor = HPGray2) { innerPadding ->
        Column(
            modifier = Modifier
                .padding(innerPadding)
                .fillMaxSize()
                .padding(horizontal = 20.dp)
        ) {
            NextChallengeTopBar(onBack = onBackClick)
            Column(
                modifier = Modifier
                    .weight(1f)
                    .verticalScroll(rememberScrollState())
            ) {
                NextChallengeHeroCard(
                    title = "약속한 날짜가 됐어요.",
                    subtitle = "이어서 시작할게요!",
                    backgroundColor = HPSub3
                )
                Spacer(Modifier.height(14.dp))
                Text(
                    text = "날짜 고정으로 매월 ${draft.fixedDay}일 자동 시작하도록 설정해뒀어요.",
                    modifier = Modifier.fillMaxWidth(),
                    color = HPText,
                    style = MaterialTheme.typography.bodySmall,
                    fontWeight = FontWeight.Medium,
                    textAlign = TextAlign.Center
                )
                Spacer(Modifier.height(28.dp))
                Text(
                    "챌린지 상세",
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.SemiBold,
                    color = HPBlack
                )
                Spacer(Modifier.height(4.dp))
                Text(
                    "미리 설정했던 정보로 이전 챌린지와 동일해요.",
                    color = HPText,
                    style = MaterialTheme.typography.bodySmall,
                    fontWeight = FontWeight.Medium
                )
                Spacer(Modifier.height(12.dp))
                ChallengeDraftCard(draft)
                Spacer(Modifier.height(20.dp))
            }
            Button(
                onClick = onStartChallengeClick,
                modifier = Modifier.fillMaxWidth().height(56.dp),
                shape = RoundedCornerShape(10.dp),
                colors = ButtonDefaults.buttonColors(containerColor = HPMain)
            ) {
                Text(
                    "챌린지 참가하기",
                    color = HPWhite,
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.SemiBold
                )
            }
            Spacer(Modifier.height(8.dp))
        }
    }
}

@Composable
private fun ChallengeDraftCard(draft: FixedDateChallengeDraft) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .border(1.dp, HPSub2, RoundedCornerShape(20.dp))
            .padding(horizontal = 15.dp, vertical = 20.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                "${draft.nextStartDate.monthValue}월 챌린지",
                style = Body16Bold,
                fontWeight = FontWeight.SemiBold,
                color = HPBlack
            )
            Text(
                text = "${draft.nextStartDate.monthValue}.${draft.nextStartDate.dayOfMonth} ~ " +
                    "${draft.nextEndDate.monthValue}.${draft.nextEndDate.dayOfMonth}",
                modifier = Modifier
                    .background(HPSub2, RoundedCornerShape(30.dp))
                    .padding(horizontal = 20.dp, vertical = 2.dp),
                color = HPWhite,
                fontSize = 16.sp,
                fontWeight = FontWeight.SemiBold
            )
        }
        Spacer(Modifier.height(20.dp))
        ChallengeDraftRow("기간", "${draft.durationDays}일")
        Spacer(Modifier.height(10.dp))
        DashedDivider(color = HPSub2)
        Spacer(Modifier.height(10.dp))
        ChallengeDraftRow("챌린지 전체 식비 목표", formatWon(draft.budgetTotal))
        Spacer(Modifier.height(10.dp))
        DashedDivider(color = HPSub2)
        Spacer(Modifier.height(10.dp))
        ChallengeDraftRow("하루 식비 목표", formatWon(draft.dailyLimit), valueColor = HPSub)
        Spacer(Modifier.height(10.dp))
        DashedDivider(color = HPSub2)
    }
}

@Composable
private fun ChallengeDraftRow(label: String, value: String, valueColor: androidx.compose.ui.graphics.Color = HPBlack) {
    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
        Text(
            label,
            color = HPText,
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.Medium
        )
        Text(
            value,
            color = valueColor,
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.Medium
        )
    }
}

@Preview(
    name = "날짜 고정 다음 챌린지",
    showBackground = true,
    widthDp = 412,
    heightDp = 916
)
@Composable
@Suppress("MagicNumber")
private fun FixedDateNextChallengeScreenPreview() {
    HampouchTheme {
        FixedDateNextChallengeScreen(
            draft = FixedDateChallengeDraft(
                state = FixedDateDraftState.DUE,
                sourceChallengeId = 10L,
                previousStartDate = LocalDate.of(2026, 5, 1),
                previousEndDate = LocalDate.of(2026, 5, 31),
                fixedDay = 1,
                nextStartDate = LocalDate.of(2026, 6, 1),
                nextEndDate = LocalDate.of(2026, 6, 30),
                durationDays = 30,
                budgetTotal = 350_000,
                dailyLimit = 11_667
            ),
            onBackClick = {},
            onStartChallengeClick = {},
            onEditSettingsClick = {}
        )
    }
}
