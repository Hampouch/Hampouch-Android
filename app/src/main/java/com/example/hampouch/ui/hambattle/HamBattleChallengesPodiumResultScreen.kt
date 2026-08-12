package com.example.hampouch.ui.hambattle

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.hampouch.R
import com.example.hampouch.core.config.BattleConfig
import com.example.hampouch.data.local.HamBattleMockFixtures
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import com.example.hampouch.domain.model.HamBattleChallenge
import com.example.hampouch.domain.model.HamBattleParticipantSpending
import com.example.hampouch.domain.model.HamBattleParticipantStatus
import com.example.hampouch.ui.theme.Body16Bold
import com.example.hampouch.ui.theme.HPBlack
import com.example.hampouch.ui.theme.HPGray4
import com.example.hampouch.ui.theme.HPMain
import com.example.hampouch.ui.theme.HPSub1
import com.example.hampouch.ui.theme.HPSub2
import com.example.hampouch.ui.theme.HPSub4
import com.example.hampouch.ui.theme.HPText
import com.example.hampouch.ui.theme.HPWhite
import com.example.hampouch.ui.theme.HampouchTheme
import java.time.LocalDate
import java.time.format.DateTimeFormatter

private enum class ResultTab { TODAY, TOTAL }

private val PodiumHeights = mapOf(1 to 110.dp, 2 to 75.dp, 3 to 55.dp)
private val PodiumColors = mapOf(1 to HPSub1, 2 to HPMain, 3 to HPSub2)

@Composable
fun HamBattleChallengesPodiumResultScreen(
    challenge: HamBattleChallenge,
    onBackClick: () -> Unit = {},
    onStartNewChallengeClick: () -> Unit = {},
    viewModel: HamBattleViewModel = hiltViewModel()
) {
    var selectedTab by remember { mutableStateOf(ResultTab.TODAY) }
    val ranked = remember(challenge, selectedTab) {
        val participants = if (selectedTab == ResultTab.TODAY) {
            if (BattleConfig.USE_SERVER_BATTLE) {
                challenge.participants.map { it.copy(amount = it.todayAmount ?: it.amount) }
            } else {
                viewModel.participantsForToday(challenge)
            }
        } else {
            challenge.participants
        }
        participants
            .filter { it.status != HamBattleParticipantStatus.DISQUALIFIED }
            .sortedBy { it.amount }
    }
    val lastPlaceName = ranked.lastOrNull()?.name.orEmpty()
    val extraRanked = if (ranked.size > 3) ranked.drop(3).take(3) else emptyList()

    Scaffold(
        topBar = { ResultTopBar(title = challenge.title, onBackClick = onBackClick) },
        containerColor = HPSub4
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(top = innerPadding.calculateTopPadding())
        ) {
            Spacer(modifier = Modifier.height(10.dp))
            ResultTabToggle(
                selectedTab = selectedTab,
                onSelect = { selectedTab = it },
                modifier = Modifier
                    .align(Alignment.CenterHorizontally)
            )

            if (selectedTab == ResultTab.TODAY) {
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    text = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd")),
                    style = Body16Bold,
                    color = HPMain,
                    modifier = Modifier.align(Alignment.CenterHorizontally)
                )
                Spacer(modifier = Modifier.height(24.dp))
            } else {
                Text(
                    text = " ",
                    style = Body16Bold,
                    color = HPMain,
                    modifier = Modifier.align(Alignment.CenterHorizontally)
                )
                Spacer(modifier = Modifier.height(40.dp))
            }

            PodiumChart(
                ranked = ranked,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp)
            )

            ResultBottomSheet(
                extraRanked = extraRanked,
                penalty = challenge.penalty,
                lastPlaceName = lastPlaceName,
                showPenaltyBox = ranked.size < 4,
                onStartNewChallengeClick = onStartNewChallengeClick,
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ResultTopBar(title: String, onBackClick: () -> Unit) {
    CenterAlignedTopAppBar(
        title = {
            Text(
                title,
                style = MaterialTheme.typography.titleSmall,
                color = HPBlack,
                maxLines = 1
            )
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
        colors = TopAppBarDefaults.topAppBarColors(containerColor = HPSub4)
    )
}

@Composable
private fun ResultTabToggle(
    selectedTab: ResultTab,
    onSelect: (ResultTab) -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .clip(RoundedCornerShape(50))
            .background(HPWhite)
            .padding(4.dp)
    ) {
        ResultTabItem("Today", selectedTab == ResultTab.TODAY) { onSelect(ResultTab.TODAY) }
        ResultTabItem("Total", selectedTab == ResultTab.TOTAL) { onSelect(ResultTab.TOTAL) }
    }
}

@Composable
private fun ResultTabItem(label: String, selected: Boolean, onClick: () -> Unit) {
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(50))
            .background(if (selected) HPMain else Color.Transparent)
            .clickable(onClick = onClick)
            .padding(horizontal = 24.dp, vertical = 10.dp)
    ) {
        Text(
            label,
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.Bold,
            color = if (selected) HPWhite else HPText
        )
    }
}

@Composable
private fun PodiumChart(ranked: List<HamBattleParticipantSpending>, modifier: Modifier = Modifier) {
    val top = ranked.take(3)
    val ordered = if (top.size == 3) listOf(top[1], top[0], top[2]) else top

    Row(
        modifier = modifier,
        horizontalArrangement = Arrangement.SpaceEvenly,
        verticalAlignment = Alignment.Bottom
    ) {
        ordered.forEach { participant ->
            val rank = ranked.indexOf(participant) + 1
            PodiumColumn(rank = rank, participant = participant)
        }
    }
}

@Composable
private fun PodiumColumn(rank: Int, participant: HamBattleParticipantSpending) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        if (rank == 1) {
            Image(
                modifier = Modifier.size(28.dp),
                painter = painterResource(
                    R.drawable.icon_crown
                ),
                contentDescription = "1등",
            )
            Spacer(modifier = Modifier.height(4.dp))
        } else {
            Spacer(modifier = Modifier.height(32.dp))
        }

        ParticipantAvatar(size = 50.dp, avatarUrl = participant.avatarUrl)
        Spacer(modifier = Modifier.height(8.dp))
        Column(
            modifier = Modifier
                .width(90.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Text(
                participant.name,
                modifier = Modifier.fillMaxWidth(),
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                color = HPMain
            )
        }
        Spacer(modifier = Modifier.height(6.dp))
        Box(
            modifier = Modifier
                .clip(RoundedCornerShape(50))
                .background(HPWhite)
                .padding(horizontal = 14.dp, vertical = 6.dp)
        ) {
            Text(
                formatWon(participant.amount),
                style = MaterialTheme.typography.bodyMedium,
                color = HPBlack
            )
        }
        Spacer(modifier = Modifier.height(8.dp))

        val podiumRank = rank.coerceAtMost(3)
        Box(
            modifier = Modifier
                .width(90.dp)
                .height(PodiumHeights.getValue(podiumRank))
                .clip(RoundedCornerShape(topStart = 20.dp, topEnd = 20.dp))
                .background(PodiumColors.getValue(podiumRank)),
            contentAlignment = Alignment.TopCenter
        ) {
            Text(
                rank.toString(),
                modifier = Modifier.padding(top = 12.dp),
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Bold,
                color = HPWhite
            )
        }
    }
}

private val ResultBottomSheetPenaltyContentMinHeight = 182.dp

@Composable
private fun ResultBottomSheet(
    extraRanked: List<HamBattleParticipantSpending>,
    penalty: String,
    lastPlaceName: String,
    showPenaltyBox: Boolean,
    onStartNewChallengeClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val sheetModifier = modifier
        .clip(RoundedCornerShape(topStart = 40.dp, topEnd = 40.dp))
        .background(HPWhite)
        .padding(start = 20.dp, end = 20.dp, top = 30.dp, bottom = 12.dp)

    if (showPenaltyBox) {
        BoxWithConstraints(modifier = sheetModifier) {
            if (maxHeight >= ResultBottomSheetPenaltyContentMinHeight) {
                Box(modifier = Modifier.fillMaxSize()) {
                    PenaltyBox(
                        penalty = penalty,
                        lastPlaceName = lastPlaceName,
                        modifier = Modifier.align(Alignment.TopCenter)
                    )
                    StartNewChallengeButton(
                        onClick = onStartNewChallengeClick,
                        modifier = Modifier.align(Alignment.BottomCenter)
                    )
                }
            } else {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .verticalScroll(rememberScrollState())
                ) {
                    PenaltyBox(penalty = penalty, lastPlaceName = lastPlaceName)
                    Spacer(modifier = Modifier.height(20.dp))
                    StartNewChallengeButton(onClick = onStartNewChallengeClick)
                }
            }
        }
    } else {
        Column(
            modifier = sheetModifier.verticalScroll(rememberScrollState())
        ) {
            extraRanked.forEachIndexed { index, participant ->
                if (index > 0) {
                    HorizontalDivider(
                        color = HPGray4,
                        thickness = 1.dp,
                        modifier = Modifier.padding(vertical = 12.dp)
                    )
                }
                ExtraRankRow(rank = index + 4, participant = participant)
            }
        }
    }
}

@Composable
private fun ExtraRankRow(rank: Int, participant: HamBattleParticipantSpending) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier.fillMaxWidth()
    ) {
        Text(
            "%02d".format(rank),
            style = MaterialTheme.typography.bodyMedium,
            color = HPText,
            modifier = Modifier.width(28.dp)
        )
        ParticipantAvatar(size = 36.dp, avatarUrl = participant.avatarUrl)
        Spacer(modifier = Modifier.width(12.dp))
        Text(
            participant.name,
            style = Body16Bold,
            color = HPBlack,
            modifier = Modifier.weight(1f),
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
        Text(formatWon(participant.amount), style = Body16Bold, color = HPBlack)
    }
}

@Composable
private fun PenaltyBox(penalty: String, lastPlaceName: String, modifier: Modifier = Modifier) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(20.dp))
            .border(1.dp, HPText, RoundedCornerShape(20.dp))
            .padding(horizontal = 20.dp, vertical = 15.dp)
    ) {
        Text("벌칙", style = Body16Bold, color = HPBlack)
        Spacer(modifier = Modifier.height(6.dp))
        Text(penalty, style = MaterialTheme.typography.bodyMedium, color = HPBlack)
        Spacer(modifier = Modifier.height(6.dp))
        Text("현재 꼴찌 : $lastPlaceName", style = MaterialTheme.typography.bodySmall, color = HPText)
    }
}

@Composable
private fun StartNewChallengeButton(onClick: () -> Unit, modifier: Modifier = Modifier) {
    Button(
        onClick = onClick,
        modifier = modifier
            .fillMaxWidth()
            .height(56.dp),
        shape = RoundedCornerShape(10.dp),
        colors = ButtonDefaults.buttonColors(containerColor = HPMain)
    ) {
        Text("새 햄배틀 시작하기", style = MaterialTheme.typography.bodyLarge, color = HPWhite)
    }
}

@Preview(showBackground = true)
@Composable
private fun ResultTabTogglePreview() {
    HampouchTheme {
        var selectedTab by remember { mutableStateOf(ResultTab.TODAY) }
        Box(modifier = Modifier.padding(20.dp), contentAlignment = Alignment.Center) {
            ResultTabToggle(selectedTab = selectedTab, onSelect = { selectedTab = it })
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun PodiumChartPreview() {
    HampouchTheme {
        Box(modifier = Modifier.padding(20.dp).background(HPSub4)) {
            PodiumChart(ranked = HamBattleMockFixtures.activeChallenges()[1].participants)
        }
    }
}

@Preview
@Composable
private fun HambattleChallengesPodiumResultScreenOneVsOnePreview() {
    HampouchTheme {
        HamBattleChallengesPodiumResultScreen(challenge = HamBattleMockFixtures.activeChallenges()[0])
    }
}

@Preview
@Composable
private fun HambattleChallengesPodiumResultScreenGroupPreview() {
    HampouchTheme {
        HamBattleChallengesPodiumResultScreen(challenge = HamBattleMockFixtures.activeChallenges()[1])
    }
}
