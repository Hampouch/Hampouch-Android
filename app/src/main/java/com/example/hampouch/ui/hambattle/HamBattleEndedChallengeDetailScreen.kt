package com.example.hampouch.ui.hambattle

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
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
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.hampouch.R
import com.example.hampouch.data.model.HamBattleEndedChallenge
import com.example.hampouch.data.model.HamBattleParticipantSpending
import com.example.hampouch.data.model.HamBattleParticipantStatus
import com.example.hampouch.ui.theme.Body16Bold
import com.example.hampouch.ui.theme.HPBlack
import com.example.hampouch.ui.theme.HPGray2
import com.example.hampouch.ui.theme.HPGray3
import com.example.hampouch.ui.theme.HPGray4
import com.example.hampouch.ui.theme.HPMain
import com.example.hampouch.ui.theme.HPSub2
import com.example.hampouch.ui.theme.HPSub3
import com.example.hampouch.ui.theme.HPSub4
import com.example.hampouch.ui.theme.HPText
import com.example.hampouch.ui.theme.HPWhite
import com.example.hampouch.ui.theme.HampouchTheme

private val LastPlaceBadgeBackground = Color(0xFFFBF0EC)
private val LastPlaceBadgeText = Color(0xFFE17866)

@Composable
fun HamBattleEndedChallengeDetailScreen(
    challenge: HamBattleEndedChallenge,
    onBackClick: () -> Unit = {},
    onShareResultClick: () -> Unit = {},
    onStartNewChallengeClick: () -> Unit = {}
) {
    val sorted = remember(challenge) { challenge.participants.sortedBy { it.amount } }
    val ranked = remember(sorted) {
        sorted.filter { it.status != HamBattleParticipantStatus.DISQUALIFIED }
    }
    val disqualified = remember(sorted) {
        sorted.filter { it.status == HamBattleParticipantStatus.DISQUALIFIED }
    }
    val winner = ranked.firstOrNull()
    val lastPlaceName = ranked.lastOrNull()?.name.orEmpty()

    Scaffold(
        topBar = { EndedDetailTopBar(title = challenge.title, onBackClick = onBackClick) },
        containerColor = HPWhite
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .verticalScroll(rememberScrollState())
                .padding(20.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(15.dp)
            ) {
                SummaryCard(
                    title = challenge.title,
                    type = challenge.type,
                    periodLabel = challenge.periodLabel,
                    winner = winner,
                    participantCount = challenge.participants.size
                )

                Spacer(modifier = Modifier.height(24.dp))
                Text("최종 순위", style = Body16Bold, color = HPBlack)
                Spacer(modifier = Modifier.height(12.dp))
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    ranked.forEachIndexed { index, participant ->
                        EndedRankRow(
                            rank = index + 1,
                            participant = participant,
                            isWinner = index == 0,
                            isMe = participant.name == "나",
                            isLastPlace = index == ranked.lastIndex
                        )
                    }
                    disqualified.forEach { participant ->
                        EndedDisqualifiedRow(participant = participant)
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))
            }

            EndedPenaltyBox(penalty = challenge.penalty, penaltyTargetName = lastPlaceName)

            Spacer(modifier = Modifier.height(16.dp))
            OutlinedButton(
                onClick = onShareResultClick,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                shape = RoundedCornerShape(10.dp),
                border = BorderStroke(1.dp, HPMain),
                colors = ButtonDefaults.outlinedButtonColors(contentColor = HPMain)
            ) {
                Icon(Icons.Filled.Share, contentDescription = null, modifier = Modifier.size(26.dp))
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    "결과 공유하기",
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.Bold
                )
            }

            Spacer(modifier = Modifier.height(12.dp))
            Button(
                onClick = onStartNewChallengeClick,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                shape = RoundedCornerShape(10.dp),
                colors = ButtonDefaults.buttonColors(containerColor = HPMain)
            ) {
                Text(
                    "새 챌린지 시작하기",
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.Bold,
                    color = HPWhite
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun EndedDetailTopBar(title: String, onBackClick: () -> Unit) {
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
private fun SummaryCard(
    title: String,
    type: String,
    periodLabel: String,
    winner: HamBattleParticipantSpending?,
    participantCount: Int
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(20.dp))
            .background(HPSub4)
            .padding(20.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Box(
            modifier = Modifier
                .clip(RoundedCornerShape(50))
                .background(HPMain)
                .padding(horizontal = 14.dp, vertical = 6.dp)
        ) {
            Text(
                "챌린지 종료",
                style = MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.Bold,
                color = HPWhite
            )
        }

        Spacer(modifier = Modifier.height(10.dp))
        Text(title, style = Body16Bold, color = HPBlack, textAlign = TextAlign.Center)
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            periodLabel,
            style = MaterialTheme.typography.bodySmall,
            fontWeight = FontWeight.Bold,
            color = HPText
        )
        Text(
            type,
            style = MaterialTheme.typography.bodySmall,
            fontWeight = FontWeight.Bold,
            color = HPText
        )

        if (winner != null) {
            Spacer(modifier = Modifier.height(16.dp))
            Image(
                modifier = Modifier.size(20.dp),
                painter = painterResource(
                    R.drawable.icon_crown
                ),
                contentDescription = "1등",
            )
            Spacer(modifier = Modifier.height(6.dp))
            Box(
                modifier = Modifier
                    .size(56.dp)
                    .clip(CircleShape)
                    .background(HPGray4)
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(winner.name, style = Body16Bold, color = HPBlack)
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                formatWon(winner.amount),
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold,
                color = HPMain
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                "${participantCount}명 중 가장 적게 썼어요!",
                style = MaterialTheme.typography.bodySmall,
                fontWeight = FontWeight.Bold,
                color = HPText
            )
        }
    }
}

@Composable
private fun EndedRankRow(
    rank: Int,
    participant: HamBattleParticipantSpending,
    isWinner: Boolean,
    isMe: Boolean,
    isLastPlace: Boolean
) {
    val backgroundColor = when {
        isWinner -> HPSub3
        isMe -> HPSub2
        else -> HPGray3
    }
    val nameColor = when {
        isWinner -> HPMain
        isMe -> HPMain
        else -> HPMain
    }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .background(backgroundColor)
            .padding(horizontal = 16.dp, vertical = 14.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            "%02d".format(rank),
            style = Body16Bold,
            color = if (isMe) HPMain else HPBlack,
            modifier = Modifier.width(28.dp)
        )
        Box(
            modifier = Modifier
                .size(32.dp)
                .clip(CircleShape)
                .background(HPGray4)
        )
        Spacer(modifier = Modifier.width(12.dp))
        Column(modifier = Modifier.weight(1f)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    participant.name,
                    style = MaterialTheme.typography.bodySmall,
                    fontWeight = FontWeight.Bold,
                    color = nameColor
                )
                if (isWinner) {
                    Spacer(modifier = Modifier.width(4.dp))
                    Image(
                        modifier = Modifier.size(20.dp),
                        painter = painterResource(
                            R.drawable.icon_crown
                        ),
                        contentDescription = "1등",
                    )
                }
                if (isLastPlace) {
                    Spacer(modifier = Modifier.width(6.dp))
                    LastPlaceBadge()
                }
            }
            Text(formatWon(participant.amount), style = Body16Bold, color = HPBlack)
        }
    }
}

@Composable
private fun EndedDisqualifiedRow(participant: HamBattleParticipantSpending) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .background(HPGray4)
            .padding(horizontal = 16.dp, vertical = 14.dp),
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
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.Bold,
            color = HPText
        )
    }
}

@Composable
private fun LastPlaceBadge() {
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(50))
            .background(LastPlaceBadgeBackground)
            .padding(horizontal = 8.dp, vertical = 2.dp)
    ) {
        Text(
            "꼴찌",
            style = MaterialTheme.typography.labelSmall,
            fontWeight = FontWeight.Bold,
            color = LastPlaceBadgeText
        )
    }
}

@Composable
private fun EndedPenaltyBox(penalty: String, penaltyTargetName: String) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(20.dp))
            .border(width = 1.dp, color = HPText, shape = RoundedCornerShape(20.dp))
            .background(HPWhite)
            .padding(horizontal = 20.dp, vertical = 15.dp)
    ) {
        Text("벌칙", style = Body16Bold, color = HPBlack)
        Spacer(modifier = Modifier.height(6.dp))
        Text(
            penalty,
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.Bold,
            color = HPBlack
        )
        Spacer(modifier = Modifier.height(6.dp))
        Row() {
            Text(
                penaltyTargetName,
                style = MaterialTheme.typography.bodySmall,
                fontWeight = FontWeight.Bold,
                color = HPMain
            )
            Text(
                " 님이 벌칙을 수행해요.",
                style = MaterialTheme.typography.bodySmall,
                color = HPText
            )
        }

    }
}

@Preview
@Composable
private fun HamBattleEndedChallengeDetailScreenPreview() {
    HampouchTheme {
        HamBattleEndedChallengeDetailScreen(challenge = HamBattleMockData.endedChallenges[1])
    }
}
