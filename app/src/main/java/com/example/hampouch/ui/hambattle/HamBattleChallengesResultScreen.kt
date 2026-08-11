package com.example.hampouch.ui.hambattle

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.hampouch.domain.model.HamBattleChallenge
import com.example.hampouch.domain.model.HamBattleParticipantSpending
import com.example.hampouch.domain.model.HamBattleParticipantStatus
import com.example.hampouch.ui.theme.Body16Bold
import com.example.hampouch.ui.theme.HPBlack
import com.example.hampouch.ui.theme.HPGray4
import com.example.hampouch.ui.theme.HPMain
import com.example.hampouch.ui.theme.HPSub
import com.example.hampouch.ui.theme.HPSub2
import com.example.hampouch.ui.theme.HPSub3
import com.example.hampouch.ui.theme.HPSub4
import com.example.hampouch.ui.theme.HPText
import com.example.hampouch.ui.theme.HPWhite
import com.example.hampouch.ui.theme.HampouchTheme

private val StatusBadgeBackground = Color(0xFFECF2E0)
private val StatusBadgeText = Color(0xFF729739)

@Composable
fun HamBattleChallengesResultScreen(
    challenge: HamBattleChallenge,
    onBackClick: () -> Unit = {},
    onStartNewChallengeClick: () -> Unit = {}
) {
    val sorted = remember(challenge) { challenge.participants.sortedBy { it.amount } }
    val ranked = remember(sorted) {
        sorted.filter { it.status != HamBattleParticipantStatus.DISQUALIFIED }
    }
    val disqualified = remember(sorted) {
        sorted.filter { it.status == HamBattleParticipantStatus.DISQUALIFIED }
    }
    val lastPlaceName = ranked.lastOrNull()?.name.orEmpty()

    Scaffold(
        topBar = { OneVsOneTopBar(title = challenge.title, onBackClick = onBackClick) },
        containerColor = HPWhite
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(top = innerPadding.calculateTopPadding())
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp)
        ) {
            RankingCard(
                type = challenge.type,
                dDay = challenge.dDayLabel(),
                periodLabel = challenge.periodLabel(),
                ranked = ranked,
                disqualified = disqualified
            )

            Spacer(modifier = Modifier.height(15.dp))
            OneVsOnePenaltyBox(penalty = challenge.penalty, lastPlaceName = lastPlaceName)

            Spacer(modifier = Modifier.height(15.dp))
            Button(
                onClick = onStartNewChallengeClick,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                shape = RoundedCornerShape(10.dp),
                colors = ButtonDefaults.buttonColors(containerColor = HPMain)
            ) {
                Text("새 챌린지 시작하기", style = MaterialTheme.typography.bodyLarge, color = HPWhite)
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun OneVsOneTopBar(title: String, onBackClick: () -> Unit) {
    CenterAlignedTopAppBar(
        title = {
            Text(title, style = MaterialTheme.typography.titleSmall, color = HPBlack, maxLines = 1)
        },
        navigationIcon = {
            IconButton(onClick = onBackClick) {
                Icon(
                    Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = "뒤로가기",
                    tint = HPBlack
                )
            }
        },
        colors = TopAppBarDefaults.topAppBarColors(containerColor = HPWhite)
    )
}

@Composable
private fun RankingCard(
    type: String,
    dDay: String,
    periodLabel: String,
    ranked: List<HamBattleParticipantSpending>,
    disqualified: List<HamBattleParticipantSpending>
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(20.dp))
            .background(HPSub4)
            .padding(15.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text("현재 순위", style = Body16Bold, color = HPBlack)
            StatusBadge(text = "진행중")
        }

        Spacer(modifier = Modifier.height(8.dp))
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text("$type · $dDay", style = MaterialTheme.typography.bodyMedium, color = HPText)
            Text(periodLabel, style = MaterialTheme.typography.bodyMedium, color = HPText)
        }

        Spacer(modifier = Modifier.height(16.dp))
        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
            ranked.forEachIndexed { index, participant ->
                RankRow(
                    rank = index + 1,
                    participant = participant,
                    highlighted = participant.name == "나"
                )
            }
            disqualified.forEach { participant ->
                DisqualifiedRow(participant = participant)
            }
        }
    }
}

@Composable
private fun RankRow(rank: Int, participant: HamBattleParticipantSpending, highlighted: Boolean) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .border(
                width = if (highlighted) 1.dp else 0.dp,
                color = if (highlighted) HPSub else Color.Transparent,
                shape = RoundedCornerShape(14.dp)
            )
            .background(if (highlighted) HPSub2.copy(alpha = 0.4f) else HPWhite)
            .padding(horizontal = 10.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            "%02d".format(rank),
            style = Body16Bold,
            color = if (highlighted) HPMain else HPBlack,
            modifier = Modifier.width(28.dp)
        )
        Box(
            modifier = Modifier
                .size(32.dp)
                .clip(CircleShape)
                .background(if (highlighted) HPWhite else HPGray4)
        )
        Spacer(modifier = Modifier.width(12.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(
                participant.name,
                style = MaterialTheme.typography.bodySmall,
                fontWeight = FontWeight.Bold,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                color = HPMain
            )
            Text(
                formatWon(participant.amount),
                fontSize = 16.sp,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                color = HPBlack
            )
        }
        if (participant.status == HamBattleParticipantStatus.MISSED_CONSECUTIVE_LOGS) {
            MissedLogBadge()
        }
    }
}

@Composable
private fun DisqualifiedRow(participant: HamBattleParticipantSpending) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .background(HPGray4)
            .padding(horizontal = 10.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            "탈락",
            style = Body16Bold,
            color = HPText,
            fontSize = 14.sp,
            modifier = Modifier.width(28.dp)
        )
        Box(
            modifier = Modifier
                .size(32.dp)
                .clip(CircleShape)
                .background(HPWhite)
        )
        Spacer(modifier = Modifier.width(12.dp))
        Text(
            participant.name,
            modifier = Modifier.weight(1f),
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.Bold,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            color = HPText
        )
    }
}

@Composable
private fun MissedLogBadge() {
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(30))
            .background(HPSub3)
            .padding(horizontal = 10.dp, vertical = 5.dp)
    ) {
        Text(
            "연속 지출\n미기록",
            style = MaterialTheme.typography.labelSmall.copy(lineHeight = 14.sp),
            fontWeight = FontWeight.Bold,
            color = HPMain,
            textAlign = TextAlign.Center
        )
    }
}

@Composable
private fun StatusBadge(text: String) {
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(50))
            .background(StatusBadgeBackground)
            .padding(horizontal = 12.dp, vertical = 6.dp)
    ) {
        Text(
            text,
            style = MaterialTheme.typography.labelMedium,
            fontWeight = FontWeight.Bold,
            color = StatusBadgeText
        )
    }
}

@Composable
private fun OneVsOnePenaltyBox(penalty: String, lastPlaceName: String) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(20.dp))
            .border(1.dp, HPText, RoundedCornerShape(20.dp))
            .padding(horizontal = 20.dp, vertical = 10.dp)
    ) {
        Text("벌칙", style = Body16Bold, color = HPBlack)
        Spacer(modifier = Modifier.height(6.dp))
        Text(penalty, style = MaterialTheme.typography.bodyMedium, color = HPBlack)
        Spacer(modifier = Modifier.height(6.dp))
        Text(
            "현재 꼴찌 : $lastPlaceName",
            style = MaterialTheme.typography.bodySmall,
            fontWeight = FontWeight.Bold,
            color = HPText
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun RankingCardPreview() {
    HampouchTheme {
        Box(modifier = Modifier.padding(16.dp)) {
            RankingCard(
                type = "개인전",
                dDay = "D-3",
                periodLabel = "2024.03.01 ~ 2024.03.07",
                ranked = listOf(
                    HamBattleParticipantSpending("나", 15000),
                    HamBattleParticipantSpending("친구1", 20000, HamBattleParticipantStatus.MISSED_CONSECUTIVE_LOGS)
                ),
                disqualified = listOf(
                    HamBattleParticipantSpending("친구2", 50000, HamBattleParticipantStatus.DISQUALIFIED)
                )
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun OneVsOnePenaltyBoxPreview() {
    HampouchTheme {
        Box(modifier = Modifier.padding(16.dp)) {
            OneVsOnePenaltyBox(penalty = "커피 사기", lastPlaceName = "친구1")
        }
    }
}

@Preview
@Composable
private fun HamBattleChallengesResultScreenPreview() {
    HampouchTheme {
        HamBattleChallengesResultScreen(challenge = HamBattleMockData.activeChallenges()[0])
    }
}
