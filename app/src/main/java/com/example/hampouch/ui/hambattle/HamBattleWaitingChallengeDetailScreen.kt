package com.example.hampouch.ui.hambattle

import androidx.compose.foundation.BorderStroke
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
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.hampouch.data.model.HamBattleParticipantSpending
import com.example.hampouch.data.model.HamBattleWaitingChallenge
import com.example.hampouch.ui.theme.Body16Bold
import com.example.hampouch.ui.theme.HPBlack
import com.example.hampouch.ui.theme.HPGray3
import com.example.hampouch.ui.theme.HPGray4
import com.example.hampouch.ui.theme.HPMain
import com.example.hampouch.ui.theme.HPSub2
import com.example.hampouch.ui.theme.HPText
import com.example.hampouch.ui.theme.HPWhite
import com.example.hampouch.ui.theme.HampouchTheme

@Composable
fun HamBattleWaitingChallengeDetailScreen(
    challenge: HamBattleWaitingChallenge,
    onBackClick: () -> Unit = {},
    onShareToCommunityClick: () -> Unit = {},
    onCopyLinkClick: () -> Unit = {}
) {
    Scaffold(
        topBar = { WaitingDetailTopBar(title = challenge.title, onBackClick = onBackClick) },
        containerColor = HPWhite
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(top = innerPadding.calculateTopPadding())
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp, vertical = 16.dp)
        ) {
            WaitingInfoCard(challenge = challenge)

            Spacer(modifier = Modifier.height(24.dp))
            WaitingPenaltyBox(penalty = challenge.penalty)

            Spacer(modifier = Modifier.height(16.dp))
            OutlinedButton(
                onClick = onShareToCommunityClick,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                shape = RoundedCornerShape(10.dp),
                border = BorderStroke(1.dp, HPMain),
                colors = ButtonDefaults.outlinedButtonColors(contentColor = HPMain)
            ) {
                Text(
                    "커뮤니티에 공유하기",
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.Bold
                )
            }

            Spacer(modifier = Modifier.height(12.dp))
            Button(
                onClick = onCopyLinkClick,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                shape = RoundedCornerShape(10.dp),
                colors = ButtonDefaults.buttonColors(containerColor = HPMain)
            ) {
                Text(
                    "링크 다시 복사하기",
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
private fun WaitingDetailTopBar(title: String, onBackClick: () -> Unit) {
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
private fun WaitingInfoCard(challenge: HamBattleWaitingChallenge) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(20.dp))
            .background(HPGray3)
            .padding(15.dp)
    ) {
        Row() {
            Text(
                challenge.startDateShortLabel,
                style = Body16Bold,
                color = HPMain
            )
            Text(
                "부터 챌린지가 시작됩니다.",
                style = Body16Bold,
                color = HPBlack
            )
        }

        Spacer(modifier = Modifier.height(8.dp))
        Text(
            "· 챌린지가 시작되면 입장할 수 없어요.",
            style = MaterialTheme.typography.bodySmall,
            color = HPText
        )
        Text(
            "· 3일 이상 지출 미입력 시 무효 처리 됩니다.",
            style = MaterialTheme.typography.bodySmall,
            color = HPText
        )

        Spacer(modifier = Modifier.height(14.dp))
        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
            challenge.participants.forEach { participant ->
                WaitingParticipantRow(participant = participant, isMe = participant.name == "나")
            }
            repeat(challenge.totalCount - challenge.participants.size) {
                WaitingEmptySlotRow()
            }
        }
    }
}

@Composable
private fun WaitingParticipantRow(participant: HamBattleParticipantSpending, isMe: Boolean) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .background(if (isMe) HPSub2 else HPWhite)
            .padding(horizontal = 16.dp, vertical = 14.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            "01",
            style = Body16Bold,
            color = if (isMe) HPWhite else HPText,
            modifier = Modifier.width(28.dp)
        )
        Box(
            modifier = Modifier
                .size(32.dp)
                .clip(CircleShape)
                .background(if (isMe) HPWhite else HPGray4)
        )
        Spacer(modifier = Modifier.width(12.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(
                participant.name,
                style = MaterialTheme.typography.bodySmall,
                fontWeight = FontWeight.Bold,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                color = if (isMe) HPMain else HPBlack
            )
            Text("0", style = Body16Bold, color = if (isMe) HPMain else HPBlack)
        }
    }
}

@Composable
private fun WaitingEmptySlotRow() {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .background(HPWhite)
            .padding(horizontal = 16.dp, vertical = 14.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            "01",
            style = Body16Bold,
            color = HPText,
            modifier = Modifier.width(28.dp)
        )
    }
}

@Composable
private fun WaitingPenaltyBox(penalty: String) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(20.dp))
            .border(1.dp, HPText, RoundedCornerShape(20.dp))
            .padding(horizontal = 20.dp, vertical = 15.dp)
    ) {
        Text("벌칙", style = Body16Bold, color = HPBlack)
        Spacer(modifier = Modifier.height(6.dp))
        Text(
            penalty,
            style = MaterialTheme.typography.bodyMedium,
            color = HPBlack
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun WaitingInfoCardPreview() {
    HampouchTheme {
        Box(modifier = Modifier.padding(16.dp)) {
            WaitingInfoCard(challenge = HamBattleMockData.waitingChallenges.first())
        }
    }
}

@Preview
@Composable
private fun HamBattleWaitingChallengeDetailScreenPreview() {
    HampouchTheme {
        HamBattleWaitingChallengeDetailScreen(challenge = HamBattleMockData.waitingChallenges.first())
    }
}
