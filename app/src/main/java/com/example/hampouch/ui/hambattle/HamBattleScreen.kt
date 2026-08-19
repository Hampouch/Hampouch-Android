package com.example.hampouch.ui.hambattle

import android.widget.Toast
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.exclude
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.ScaffoldDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.hampouch.R
import com.example.hampouch.core.config.BattleConfig
import com.example.hampouch.data.local.HamBattleMockFixtures
import com.example.hampouch.domain.model.HamBattleChallenge
import com.example.hampouch.domain.model.HamBattleParticipantStatus
import com.example.hampouch.domain.model.HamBattleStatus
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.hampouch.navigation.BottomNavBar
import com.example.hampouch.navigation.BottomNavItem
import com.example.hampouch.ui.dialog.HamBattleChallengeCancelledDialog
import com.example.hampouch.ui.dialog.HamBattleMissedSpendingDialog
import com.example.hampouch.ui.dialog.HamBattleParticipationInvalidatedDialog
import com.example.hampouch.ui.dialog.HamBattleWaitingChallengeExpiredDialog
import com.example.hampouch.ui.theme.Body16Bold
import com.example.hampouch.ui.theme.HPBlack
import com.example.hampouch.ui.theme.HPGray3
import com.example.hampouch.ui.theme.HPGray5
import com.example.hampouch.ui.theme.HPMain
import com.example.hampouch.ui.theme.HPSub3
import com.example.hampouch.ui.theme.HPText
import com.example.hampouch.ui.theme.HPWhite
import com.example.hampouch.ui.theme.HampouchTheme

private val StatusBadgeBackground = Color(0xFFECF2E0)
private val StatusBadgeText = Color(0xFF729739)

@Composable
fun HamBattleScreen(
    selectedBottomTab: BottomNavItem,
    onItemSelected: (BottomNavItem) -> Unit,
    onAddClick: () -> Unit,
    modifier: Modifier = Modifier,
    activeChallenges: List<HamBattleChallenge> = emptyList(),
    waitingChallenges: List<HamBattleChallenge> = emptyList(),
    onStartNewChallengeClick: () -> Unit,
    onViewEndedChallengesClick: () -> Unit,
    onChallengeClick: (String) -> Unit,
    onWaitingChallengeClick: (String) -> Unit,
    onViewEndedChallengeDetailClick: (String) -> Unit,
    viewModel: HamBattleViewModel = hiltViewModel()
) {
    val context = LocalContext.current
    val mockChallenges by viewModel.mockChallenges.collectAsStateWithLifecycle()
    val isRefreshing by viewModel.isRefreshing.collectAsStateWithLifecycle()
    var refreshRequestedByGesture by remember { mutableStateOf(false) }
    LaunchedEffect(isRefreshing) {
        if (!isRefreshing && refreshRequestedByGesture) {
            refreshRequestedByGesture = false
            Toast.makeText(context, "햄배틀을 새로고침했어요.", Toast.LENGTH_SHORT).show()
        }
    }

    HamBattleContent(
        selectedBottomTab = selectedBottomTab,
        onItemSelected = onItemSelected,
        onAddClick = onAddClick,
        modifier = modifier,
        activeChallenges = activeChallenges,
        waitingChallenges = waitingChallenges,
        isRefreshing = isRefreshing,
        onRefresh = {
            refreshRequestedByGesture = true
            viewModel.refreshMyBattles()
        },
        onStartNewChallengeClick = onStartNewChallengeClick,
        onViewEndedChallengesClick = onViewEndedChallengesClick,
        onChallengeClick = onChallengeClick,
        onWaitingChallengeClick = onWaitingChallengeClick
    )

    if (!BattleConfig.USE_SERVER_BATTLE) {
        val expiredWaitingChallenge = remember(waitingChallenges) {
            waitingChallenges.firstOrNull { it.isExpired() }
        }

        LaunchedEffect(activeChallenges) {
            activeChallenges.forEach { challenge ->
                val me = challenge.participants.find { it.name == "나" }
                if (me != null &&
                    me.status != HamBattleParticipantStatus.DISQUALIFIED &&
                    viewModel.myMissedStreakDays(challenge) >= 3
                ) {
                    viewModel.disqualifyMe(challenge.id)
                }
            }
            viewModel.mockChallengesWith(HamBattleStatus.ACTIVE).forEach { challenge ->
                val remaining = challenge.participants.count { it.status != HamBattleParticipantStatus.DISQUALIFIED }
                if (remaining <= 1) {
                    viewModel.cancelChallenge(challenge.id)
                }
            }
        }

        val justCancelledChallenge = mockChallenges.firstOrNull { challenge ->
            challenge.cancelled && !viewModel.isCancellationAcknowledged(challenge.id)
        }
        val justDisqualifiedChallenge = if (justCancelledChallenge != null) {
            null
        } else {
            activeChallenges.firstOrNull { challenge ->
                val me = challenge.participants.find { it.name == "나" }
                me?.status == HamBattleParticipantStatus.DISQUALIFIED &&
                    !viewModel.isDisqualificationAcknowledged(challenge.id)
            }
        }
        val missedWarningChallenge = if (justCancelledChallenge != null || justDisqualifiedChallenge != null) {
            null
        } else {
            activeChallenges.firstOrNull { challenge ->
                viewModel.myMissedStreakDays(challenge) == 2 &&
                    !viewModel.wasMissedWarningShownToday(challenge.id)
            }
        }

        when {
            expiredWaitingChallenge != null -> HamBattleWaitingChallengeExpiredDialog(
                challengeTitle = expiredWaitingChallenge.title,
                onRecreateClick = {
                    viewModel.removeWaitingChallenge(expiredWaitingChallenge.id)
                    onStartNewChallengeClick()
                },
                onConfirmClick = {
                    viewModel.removeWaitingChallenge(expiredWaitingChallenge.id)
                }
            )

            justCancelledChallenge != null -> HamBattleChallengeCancelledDialog(
                challengeTitle = justCancelledChallenge.title,
                onRecreateClick = {
                    viewModel.acknowledgeCancellation(justCancelledChallenge.id)
                    onStartNewChallengeClick()
                },
                onConfirmClick = {
                    viewModel.acknowledgeCancellation(justCancelledChallenge.id)
                    onViewEndedChallengeDetailClick(justCancelledChallenge.id)
                }
            )

            justDisqualifiedChallenge != null -> HamBattleParticipationInvalidatedDialog(
                challengeTitle = justDisqualifiedChallenge.title,
                onConfirmClick = {
                    viewModel.acknowledgeDisqualification(justDisqualifiedChallenge.id)
                }
            )

            missedWarningChallenge != null -> HamBattleMissedSpendingDialog(
                challengeTitle = missedWarningChallenge.title,
                onInputSpendingClick = {
                    viewModel.markMissedWarningShown(missedWarningChallenge.id)
                    onAddClick()
                },
                onConfirmClick = {
                    viewModel.markMissedWarningShown(missedWarningChallenge.id)
                }
            )
        }
    }
}

@Composable
private fun HamBattleContent(
    selectedBottomTab: BottomNavItem,
    onItemSelected: (BottomNavItem) -> Unit,
    onAddClick: () -> Unit,
    modifier: Modifier = Modifier,
    activeChallenges: List<HamBattleChallenge>,
    waitingChallenges: List<HamBattleChallenge>,
    isRefreshing: Boolean,
    onRefresh: () -> Unit,
    onStartNewChallengeClick: () -> Unit,
    onViewEndedChallengesClick: () -> Unit,
    onChallengeClick: (String) -> Unit,
    onWaitingChallengeClick: (String) -> Unit
) {
    Scaffold(
        modifier = modifier,
        containerColor = HPWhite,
        contentWindowInsets = ScaffoldDefaults.contentWindowInsets.exclude(WindowInsets.statusBars),
        bottomBar = {
            BottomNavBar(
                selectedItem = selectedBottomTab,
                onItemSelected = onItemSelected,
                onAddClick = onAddClick
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .padding(innerPadding)
                .fillMaxSize()
        ) {
            HamBattleMainTopBar(
                modifier = Modifier.padding(horizontal = 20.dp)
            )
            PullToRefreshBox(
                isRefreshing = isRefreshing,
                onRefresh = onRefresh,
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
            ) {
                if (activeChallenges.isEmpty() && waitingChallenges.isEmpty()) {
                    HamBattleEmptyContent(
                        modifier = Modifier.fillMaxSize(),
                        onStartNewChallengeClick = onStartNewChallengeClick
                    )
                } else {
                    HamBattleChallengeListContent(
                        modifier = Modifier.fillMaxSize(),
                        activeChallenges = activeChallenges,
                        waitingChallenges = waitingChallenges,
                        onStartNewChallengeClick = onStartNewChallengeClick,
                        onViewEndedChallengesClick = onViewEndedChallengesClick,
                        onChallengeClick = onChallengeClick,
                        onWaitingChallengeClick = onWaitingChallengeClick
                    )
                }
            }
        }
    }
}

@Composable
private fun HamBattleMainTopBar(modifier: Modifier = Modifier) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .height(64.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Spacer(modifier = Modifier.width(48.dp))
        Text(
            text = "햄배틀",
            style = MaterialTheme.typography.titleSmall,
            color = HPBlack,
            textAlign = TextAlign.Center,
            modifier = Modifier.weight(1f)
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun HamBattleMainTopBarPreview() {
    HampouchTheme {
        HamBattleMainTopBar()
    }
}

@Composable
private fun HamBattleEmptyContent(
    modifier: Modifier = Modifier,
    onStartNewChallengeClick: () -> Unit
) {
    LazyColumn(
        modifier = modifier
            .fillMaxSize(),
        contentPadding = PaddingValues(horizontal = 20.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        item {
            StartNewChallengeButton(onClick = onStartNewChallengeClick)
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                "진행중인 챌린지가 없어요.\n새 챌린지를 시작해봐요.",
                modifier = Modifier.fillMaxWidth(),
                textAlign = TextAlign.Center,
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Bold,
                color = HPText
            )
        }
    }
}

@Composable
private fun HamBattleChallengeListContent(
    modifier: Modifier = Modifier,
    activeChallenges: List<HamBattleChallenge>,
    waitingChallenges: List<HamBattleChallenge>,
    onStartNewChallengeClick: () -> Unit,
    onViewEndedChallengesClick: () -> Unit,
    onChallengeClick: (String) -> Unit,
    onWaitingChallengeClick: (String) -> Unit
) {
    LazyColumn(
        modifier = modifier
            .fillMaxWidth(),
        contentPadding = PaddingValues(horizontal = 20.dp, vertical = 5.dp)
    ) {
        item(contentType = "action") {
            StartNewChallengeButton(onClick = onStartNewChallengeClick)
        }

        if (activeChallenges.isNotEmpty()) {
            item(contentType = "section_header") {
                Spacer(modifier = Modifier.height(30.dp))
                SectionLabel("진행중")
                Spacer(modifier = Modifier.height(12.dp))
            }
            items(activeChallenges, key = { it.id }, contentType = { "active_challenge" }) { challenge ->
                ActiveChallengeCard(
                    challenge = challenge,
                    onClick = { onChallengeClick(challenge.id) }
                )
                Spacer(modifier = Modifier.height(10.dp))
            }
        }

        if (waitingChallenges.isNotEmpty()) {
            item(contentType = "section_header") {
                Spacer(modifier = Modifier.height(20.dp))
                SectionLabel("대기중")
                Spacer(modifier = Modifier.height(12.dp))
            }
            items(waitingChallenges, key = { it.id }, contentType = { "waiting_challenge" }) { challenge ->
                WaitingChallengeCard(
                    challenge = challenge,
                    onClick = { onWaitingChallengeClick(challenge.id) }
                )
                Spacer(modifier = Modifier.height(16.dp))
            }
        }

        item(contentType = "navigation") {
            Spacer(modifier = Modifier.height(4.dp))
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable(onClick = onViewEndedChallengesClick)
                    .padding(vertical = 8.dp),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("종료된 챌린지 모두 보기", style = MaterialTheme.typography.bodyMedium, color = HPText)
                Icon(
                    Icons.AutoMirrored.Filled.KeyboardArrowRight,
                    contentDescription = null,
                    tint = HPText,
                    modifier = Modifier.size(18.dp)
                )
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun HamBattleChallengeListContentPreview() {
    HampouchTheme {
        HamBattleChallengeListContent(
            activeChallenges = HamBattleMockFixtures.activeChallenges(),
            waitingChallenges = HamBattleMockFixtures.waitingChallenges(),
            onStartNewChallengeClick = {}, onViewEndedChallengesClick = {},
            onChallengeClick = {}, onWaitingChallengeClick = {}
        )
    }
}

@Composable
private fun StartNewChallengeButton(onClick: () -> Unit) {
    Button(
        onClick = onClick,
        modifier = Modifier
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
private fun StartNewChallengeButtonPreview() {
    HampouchTheme {
        Box(modifier = Modifier.padding(16.dp)) {
            StartNewChallengeButton(onClick = {})
        }
    }
}

private fun battleStatusMessage(challenge: HamBattleChallenge): String {
    if (challenge.isOneVsOne && challenge.participants.size == 2) {
        val (first, second) = challenge.participants
        val winner = if (first.amount <= second.amount) first else second
        val subject = if (winner.name == "나") "내가" else "${winner.name}님이"
        return "현재 $subject 이기는 중"
    }

    val myRank = challenge.participants
        .sortedBy { it.amount }
        .indexOfFirst { it.name == "나" } + 1
    return if (myRank > 0) "현재 ${myRank}위" else ""
}

private const val CollapsedRankCount = 3

@Composable
private fun ActiveChallengeCard(challenge: HamBattleChallenge, onClick: () -> Unit) {
    var showAllRanks by remember { mutableStateOf(false) }
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(20.dp))
            .background(HPSub3)
            .clickable(onClick = onClick)
            .padding(20.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            TypeBadge(text = challenge.type)
            StatusBadge(text = "진행중")
        }

        Spacer(modifier = Modifier.height(12.dp))
        Text(challenge.title, style = Body16Bold, color = HPBlack)
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            challenge.penalty,
            style = MaterialTheme.typography.bodyMedium,
            color = HPMain
        )

        Spacer(modifier = Modifier.height(16.dp))
        if (challenge.isOneVsOne && challenge.participants.size == 2) {
            OneVsOneRow(
                first = challenge.participants[0],
                second = challenge.participants[1]
            )
        } else {
            val ranked = challenge.participants.sortedBy { it.amount }
            val visibleRanks = if (showAllRanks) ranked else ranked.take(CollapsedRankCount)
            RankedParticipantList(
                participants = visibleRanks,
                maxAmount = ranked.maxOf { it.amount }.toFloat()
            )

            if (ranked.size > CollapsedRankCount) {
                Spacer(modifier = Modifier.height(12.dp))
                RankingToggleButton(
                    expanded = showAllRanks,
                    onClick = { showAllRanks = !showAllRanks }
                )
            }
        }

        Spacer(modifier = Modifier.height(14.dp))
        HorizontalDivider(color = HPGray3, thickness = 1.dp)
        Spacer(modifier = Modifier.height(10.dp))
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(challenge.dDayLabel(), style = Body16Bold, color = HPMain)
            Text(battleStatusMessage(challenge), style = Body16Bold, color = StatusWhoWonText)
        }
    }
}

@Composable
private fun RankingToggleButton(expanded: Boolean, onClick: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(10.dp))
            .clickable(onClick = onClick)
            .padding(vertical = 6.dp),
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            if (expanded) "접기" else "순위 전체보기",
            style = MaterialTheme.typography.bodySmall,
            color = HPText
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun RankingToggleButtonPreview() {
    HampouchTheme {
        Box(modifier = Modifier.padding(16.dp)) {
            RankingToggleButton(expanded = false, onClick = {})
        }
    }
}

@Composable
private fun WaitingChallengeCard(challenge: HamBattleChallenge, onClick: () -> Unit) {
    val clipboardManager = LocalClipboardManager.current
    val context = LocalContext.current
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(20.dp))
            .background(HPGray3)
            .clickable(onClick = onClick)
            .drawBehind {
                val strokeWidthPx = 1.dp.toPx()
                val cornerRadiusPx = 20.dp.toPx()
                val inset = strokeWidthPx / 2

                drawRoundRect(
                    color = HPText,
                    topLeft = Offset(inset, inset),
                    size = Size(size.width - strokeWidthPx, size.height - strokeWidthPx),
                    cornerRadius = CornerRadius(cornerRadiusPx, cornerRadiusPx),
                    style = Stroke(
                        width = strokeWidthPx,
                        pathEffect = PathEffect.dashPathEffect(
                            intervals = floatArrayOf(20f, 20f),
                            phase = 0f
                        )
                    )
                )
            }
            .padding(horizontal = 25.dp, vertical = 15.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            TypeBadge(text = challenge.type, muted = true)
            Text(
                "시작까지 ${challenge.startsInDayLabel()}",
                style = MaterialTheme.typography.bodySmall,
                fontWeight = FontWeight.Bold,
                color = HPText
            )
        }

        Spacer(modifier = Modifier.height(12.dp))
        Text(challenge.title, style = Body16Bold, color = HPBlack)
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            challenge.penalty,
            style = MaterialTheme.typography.bodyMedium,
            color = HPMain
        )

        Spacer(modifier = Modifier.height(16.dp))
        val avatarsPerRow = 7
        val avatarRows = (0 until challenge.totalCount).chunked(avatarsPerRow)
        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            avatarRows.forEachIndexed { rowIndex, indices ->
                Row(verticalAlignment = Alignment.CenterVertically) {
                    indices.forEachIndexed { i, index ->
                        if (i > 0) Spacer(modifier = Modifier.width(8.dp))
                        if (index < challenge.joinedCount) {
                            ParticipantAvatar(size = 32.dp)
                        } else {
                            EmptyAvatarSlot(size = 32.dp)
                        }
                    }
                    if (rowIndex == avatarRows.lastIndex) {
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            "${challenge.totalCount}명 중 ${challenge.joinedCount}명",
                            style = if (challenge.totalCount == avatarsPerRow) {
                                MaterialTheme.typography.bodySmall.copy(fontSize = 9.sp)
                            } else {
                                MaterialTheme.typography.bodySmall
                            },
                            color = HPText
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(10.dp))
        HorizontalDivider(color = HPGray3, thickness = 1.dp)
        Spacer(modifier = Modifier.height(10.dp))
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                challenge.startDateLabel(),
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Bold,
                color = HPMain
            )
            val battleCode = challenge.battleCode
            val inviteUrl = battleCode?.let(::buildBattleInviteUrl)
            TextButton(
                enabled = inviteUrl != null,
                onClick = {
                    if (inviteUrl != null) {
                        clipboardManager.setText(AnnotatedString(inviteUrl))
                        Toast.makeText(context, "초대 링크가 복사되었어요.", Toast.LENGTH_SHORT).show()
                    }
                }
            ) {
                Text(
                    "링크 다시 복사",
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Bold,
                    color = StatusWhoWonText
                )
            }
        }
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
private fun EmptyAvatarSlot(size: Dp) {
    val dash = PathEffect.dashPathEffect(floatArrayOf(6f, 4f))
    Canvas(modifier = Modifier.size(size)) {
        val strokeWidthPx = 1.5.dp.toPx()
        drawCircle(
            color = HPGray5,
            radius = (size.toPx() - strokeWidthPx) / 2,
            style = Stroke(width = strokeWidthPx, pathEffect = dash)
        )
    }
}

@Preview(showBackground = true)
@Composable
fun HamBattleScreenPreview() {
    HampouchTheme {
        HamBattleContent(
            selectedBottomTab = BottomNavItem.HAM_BATTLE,
            onItemSelected = {},
            onAddClick = {},
            activeChallenges = HamBattleMockFixtures.activeChallenges(),
            waitingChallenges = HamBattleMockFixtures.waitingChallenges(),
            isRefreshing = false,
            onRefresh = {},
            onStartNewChallengeClick = {}, onViewEndedChallengesClick = {},
            onChallengeClick = {}, onWaitingChallengeClick = {}
        )
    }
}

@Preview(showBackground = true)
@Composable
fun HamBattleEmptyContentPreview() {
    HampouchTheme {
        HamBattleEmptyContent(onStartNewChallengeClick = {})
    }
}

@Preview(showBackground = true)
@Composable
fun ActiveChallengeCardPreview() {
    HampouchTheme {
        Box(modifier = Modifier.padding(16.dp)) {
            ActiveChallengeCard(challenge = HamBattleMockFixtures.activeChallenges()[0], onClick = {})
        }
    }
}

@Preview(showBackground = true)
@Composable
fun WaitingChallengeCardPreview() {
    HampouchTheme {
        Box(modifier = Modifier.padding(16.dp)) {
            WaitingChallengeCard(challenge = HamBattleMockFixtures.waitingChallenges()[0], onClick = {})
        }
    }
}
