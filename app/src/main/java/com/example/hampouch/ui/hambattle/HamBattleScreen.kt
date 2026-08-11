package com.example.hampouch.ui.hambattle

import android.widget.Toast
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
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
import com.example.hampouch.ui.common.NotificationBellIcon
import com.example.hampouch.domain.model.HamBattleChallenge
import com.example.hampouch.domain.model.HamBattleParticipantStatus
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
import com.example.hampouch.ui.theme.HPSub4
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
    activeChallenges: List<HamBattleChallenge> = HamBattleMockData.activeChallenges(),
    waitingChallenges: List<HamBattleChallenge> = HamBattleMockData.waitingChallenges(),
    onStartNewChallengeClick: () -> Unit = {},
    onNotificationClick: () -> Unit = {},
    onViewEndedChallengesClick: () -> Unit = {},
    onChallengeClick: (String) -> Unit = {},
    onWaitingChallengeClick: (String) -> Unit = {},
    onViewEndedChallengeDetailClick: (String) -> Unit = {},
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
                onNotificationClick = onNotificationClick,
                modifier = Modifier.padding(horizontal = 20.dp)
            )
            if (activeChallenges.isEmpty() && waitingChallenges.isEmpty()) {
                HamBattleEmptyContent(
                    modifier = Modifier.weight(1f),
                    onStartNewChallengeClick = onStartNewChallengeClick
                )
            } else {
                HamBattleChallengeListContent(
                    modifier = Modifier.weight(1f),
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

    // 아래의 만료/탈락/취소 감지는 로컬 지출 기록(ExpenseDetailStore)을 기준으로 목데이터를 스스로 갱신하는
    // 시뮬레이션이다. 서버 모드에서는 이 상태들을 서버가 이미 계산해서 내려주고(READY/ONGOING/TERMINATED,
    // isValid 등), 되돌려 보낼 수 있는 대응 API도 아직 없어서 목데이터 모드에서만 동작시킨다.
    if (!BattleConfig.USE_SERVER_BATTLE) {
        val expiredWaitingChallenge = remember(waitingChallenges) {
            waitingChallenges.firstOrNull { it.isExpired() }
        }

        LaunchedEffect(activeChallenges) {
            activeChallenges.forEach { challenge ->
                val me = challenge.participants.find { it.name == "나" }
                if (me != null &&
                    me.status != HamBattleParticipantStatus.DISQUALIFIED &&
                    HamBattleMockData.myMissedStreakDays(challenge) >= 3
                ) {
                    HamBattleMockData.disqualifyMe(challenge.id)
                }
            }
            HamBattleMockData.activeChallenges().forEach { challenge ->
                val remaining = challenge.participants.count { it.status != HamBattleParticipantStatus.DISQUALIFIED }
                if (remaining <= 1) {
                    HamBattleMockData.cancelChallenge(challenge.id)
                }
            }
        }

        val justCancelledChallenge = HamBattleMockData.challenges.firstOrNull { challenge ->
            challenge.cancelled && !HamBattleMockData.isCancellationAcknowledged(challenge.id)
        }
        val justDisqualifiedChallenge = if (justCancelledChallenge != null) {
            null
        } else {
            activeChallenges.firstOrNull { challenge ->
                val me = challenge.participants.find { it.name == "나" }
                me?.status == HamBattleParticipantStatus.DISQUALIFIED &&
                    !HamBattleMockData.isDisqualificationAcknowledged(challenge.id)
            }
        }
        val missedWarningChallenge = if (justCancelledChallenge != null || justDisqualifiedChallenge != null) {
            null
        } else {
            activeChallenges.firstOrNull { challenge ->
                HamBattleMockData.myMissedStreakDays(challenge) == 2 &&
                    !HamBattleMockData.wasMissedWarningShownToday(challenge.id)
            }
        }

        when {
            expiredWaitingChallenge != null -> HamBattleWaitingChallengeExpiredDialog(
                challengeTitle = expiredWaitingChallenge.title,
                onRecreateClick = {
                    HamBattleMockData.removeWaitingChallenge(expiredWaitingChallenge.id)
                    onStartNewChallengeClick()
                },
                onConfirmClick = {
                    HamBattleMockData.removeWaitingChallenge(expiredWaitingChallenge.id)
                }
            )

            justCancelledChallenge != null -> HamBattleChallengeCancelledDialog(
                challengeTitle = justCancelledChallenge.title,
                onRecreateClick = {
                    HamBattleMockData.acknowledgeCancellation(justCancelledChallenge.id)
                    onStartNewChallengeClick()
                },
                onConfirmClick = {
                    HamBattleMockData.acknowledgeCancellation(justCancelledChallenge.id)
                    onViewEndedChallengeDetailClick(justCancelledChallenge.id)
                }
            )

            justDisqualifiedChallenge != null -> HamBattleParticipationInvalidatedDialog(
                challengeTitle = justDisqualifiedChallenge.title,
                onConfirmClick = {
                    HamBattleMockData.acknowledgeDisqualification(justDisqualifiedChallenge.id)
                }
            )

            missedWarningChallenge != null -> HamBattleMissedSpendingDialog(
                challengeTitle = missedWarningChallenge.title,
                onInputSpendingClick = {
                    HamBattleMockData.markMissedWarningShown(missedWarningChallenge.id)
                    onAddClick()
                },
                onConfirmClick = {
                    HamBattleMockData.markMissedWarningShown(missedWarningChallenge.id)
                }
            )
        }
    }
}

@Composable
private fun HamBattleMainTopBar(onNotificationClick: () -> Unit, modifier: Modifier = Modifier) {
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
        NotificationBellIcon(onClick = onNotificationClick)
    }
}

@Composable
private fun HamBattleEmptyContent(
    modifier: Modifier = Modifier,
    onStartNewChallengeClick: () -> Unit
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 20.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
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

@Composable
private fun HamBattleChallengeListContent(
    modifier: Modifier = Modifier,
    activeChallenges: List<HamBattleChallenge>,
    waitingChallenges: List<HamBattleChallenge>,
    onStartNewChallengeClick: () -> Unit,
    onViewEndedChallengesClick: () -> Unit,
    onChallengeClick: (String) -> Unit = {},
    onWaitingChallengeClick: (String) -> Unit = {}
) {
    Column(
        modifier = modifier
            .verticalScroll(rememberScrollState())
            .fillMaxWidth()
            .padding(horizontal = 20.dp, vertical = 16.dp)
    ) {
        StartNewChallengeButton(onClick = onStartNewChallengeClick)

        if (activeChallenges.isNotEmpty()) {
            Spacer(modifier = Modifier.height(30.dp))
            SectionLabel("진행중")
            Spacer(modifier = Modifier.height(12.dp))
            activeChallenges.forEachIndexed { index, challenge ->
                if (index > 0) Spacer(modifier = Modifier.height(10.dp))
                ActiveChallengeCard(
                    challenge = challenge,
                    onClick = { onChallengeClick(challenge.id) }
                )
            }
        }

        if (waitingChallenges.isNotEmpty()) {
            Spacer(modifier = Modifier.height(30.dp))
            SectionLabel("대기중")
            Spacer(modifier = Modifier.height(12.dp))
            waitingChallenges.forEachIndexed { index, challenge ->
                if (index > 0) Spacer(modifier = Modifier.height(16.dp))
                WaitingChallengeCard(
                    challenge = challenge,
                    onClick = { onWaitingChallengeClick(challenge.id) }
                )
            }
        }

        Spacer(modifier = Modifier.height(20.dp))
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
private fun ActiveChallengeCard(challenge: HamBattleChallenge, onClick: () -> Unit = {}) {
    var showAllRanks by remember { mutableStateOf(false) }
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(20.dp))
            .background(HPSub4)
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
        HorizontalDivider(color = HPText, thickness = 1.dp)
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

@Composable
private fun WaitingChallengeCard(challenge: HamBattleChallenge, onClick: () -> Unit = {}) {
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
        HorizontalDivider(color = HPText, thickness = 1.dp)
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
            TextButton(
                onClick = {
                    clipboardManager.setText(AnnotatedString(challenge.link))
                    Toast.makeText(context, "링크가 복사되었어요.", Toast.LENGTH_SHORT).show()
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
        HamBattleScreen(
            selectedBottomTab = BottomNavItem.HAM_BATTLE,
            onItemSelected = {},
            onAddClick = {}
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
            ActiveChallengeCard(challenge = HamBattleMockData.activeChallenges()[0])
        }
    }
}

@Preview(showBackground = true)
@Composable
fun WaitingChallengeCardPreview() {
    HampouchTheme {
        Box(modifier = Modifier.padding(16.dp)) {
            WaitingChallengeCard(challenge = HamBattleMockData.waitingChallenges()[0])
        }
    }
}
