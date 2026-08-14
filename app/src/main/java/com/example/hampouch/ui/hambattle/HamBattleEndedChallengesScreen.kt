package com.example.hampouch.ui.hambattle

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.hampouch.data.local.HamBattleMockFixtures
import com.example.hampouch.domain.model.HamBattleChallenge
import com.example.hampouch.ui.common.NotificationBellIcon
import com.example.hampouch.ui.theme.Body16Bold
import com.example.hampouch.ui.theme.HPBlack
import com.example.hampouch.ui.theme.HPGray4
import com.example.hampouch.ui.theme.HPMain
import com.example.hampouch.ui.theme.HPText
import com.example.hampouch.ui.theme.HPWhite
import com.example.hampouch.ui.theme.HampouchTheme

@Composable
fun HamBattleEndedChallengesScreen(
    endedChallenges: List<HamBattleChallenge> = emptyList(),
    onBackClick: () -> Unit,
    onNotificationClick: () -> Unit,
    onChallengeClick: (String) -> Unit
) {
    Scaffold(
        topBar = {
            EndedChallengesTopBar(onBackClick = onBackClick, onNotificationClick = onNotificationClick)
        },
        containerColor = HPWhite
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(top = innerPadding.calculateTopPadding())
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp, vertical = 16.dp)
        ) {
            SectionLabel("종료")
            Spacer(modifier = Modifier.height(12.dp))

            endedChallenges.forEachIndexed { index, challenge ->
                if (index > 0) Spacer(modifier = Modifier.height(10.dp))
                EndedChallengeCard(
                    challenge = challenge,
                    onClick = { onChallengeClick(challenge.id) }
                )
            }
        }
    }
}

@Composable
private fun EndedChallengesTopBar(onBackClick: () -> Unit, onNotificationClick: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .statusBarsPadding()
            .height(64.dp)
            .padding(horizontal = 20.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        IconButton(onClick = onBackClick) {
            Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "뒤로가기", tint = HPBlack)
        }
        Text(
            "햄배틀",
            style = MaterialTheme.typography.titleSmall,
            color = HPBlack,
            textAlign = TextAlign.Center,
            modifier = Modifier.weight(1f)
        )
        NotificationBellIcon(onClick = onNotificationClick)
    }
}

@Composable
private fun EndedChallengeCard(challenge: HamBattleChallenge, onClick: () -> Unit) {
    val ranked = remember(challenge) { challenge.participants.sortedBy { it.amount } }
    val winnerName = ranked.firstOrNull()?.name ?: challenge.winnerName.orEmpty()

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(20.dp))
            .background(HPGray4)
            .clickable(onClick = onClick)
            .padding(20.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            TypeBadge(text = challenge.type, muted = true)
            Text("종료", style = MaterialTheme.typography.bodySmall, color = HPText)
        }

        Spacer(modifier = Modifier.height(12.dp))
        Text(challenge.title, style = Body16Bold, color = HPBlack)
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            challenge.penalty,
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.Bold,
            color = HPMain
        )

        Spacer(modifier = Modifier.height(16.dp))
        if (challenge.isOneVsOne && challenge.participants.size == 2) {
            OneVsOneRow(first = challenge.participants[0], second = challenge.participants[1])
        } else if (ranked.isNotEmpty()) {
            RankedParticipantList(participants = ranked)
        }

        Spacer(modifier = Modifier.height(14.dp))
        HorizontalDivider(color = HPText, thickness = 1.dp)
        Spacer(modifier = Modifier.height(10.dp))
        Text(
            "$winnerName 승리",
            modifier = Modifier.fillMaxWidth(),
            textAlign = TextAlign.Center,
            style = Body16Bold,
            color = StatusWhoWonText
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun EndedChallengeCardPreview() {
    HampouchTheme {
        Box(modifier = Modifier.padding(16.dp)) {
            EndedChallengeCard(
                challenge = HamBattleMockFixtures.endedChallenges()[0],
                onClick = {}
            )
        }
    }
}

@Preview
@Composable
private fun HamBattleEndedChallengesScreenPreview() {
    HampouchTheme {
        HamBattleEndedChallengesScreen(
            endedChallenges = HamBattleMockFixtures.endedChallenges(),
            onBackClick = {}, onNotificationClick = {}, onChallengeClick = {}
        )
    }
}
