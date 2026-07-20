package com.example.hampouch.ui.hambattle

import android.R.attr.maxLines
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.text.TextAutoSize
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.Notifications
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.hampouch.data.model.HamBattleActiveChallenge
import com.example.hampouch.data.model.HamBattleParticipantSpending
import com.example.hampouch.data.model.HamBattleWaitingChallenge
import com.example.hampouch.ui.theme.Body16Bold
import com.example.hampouch.ui.theme.HPBlack
import com.example.hampouch.ui.theme.HPGray3
import com.example.hampouch.ui.theme.HPGray4
import com.example.hampouch.ui.theme.HPGray5
import com.example.hampouch.ui.theme.HPMain
import com.example.hampouch.ui.theme.HPSub4
import com.example.hampouch.ui.theme.HPText
import com.example.hampouch.ui.theme.HPWhite
import com.example.hampouch.ui.theme.HampouchTheme
import java.util.Locale

private val StatusBadgeBackground = Color(0xFFECF2E0)
private val StatusBadgeText = Color(0xFF729739)
private val StatusWhoWonText = Color(0xFF5572AB)

@Composable
fun HamBattleScreen(
    activeChallenges: List<HamBattleActiveChallenge> = HamBattleMockData.activeChallenges,
    waitingChallenges: List<HamBattleWaitingChallenge> = HamBattleMockData.waitingChallenges,
    onStartNewChallengeClick: () -> Unit = {},
    onNotificationClick: () -> Unit = {},
    onViewEndedChallengesClick: () -> Unit = {}
) {
    Scaffold(
        topBar = { HamBattleTopBar(onNotificationClick = onNotificationClick) },
        containerColor = HPWhite
    ) { innerPadding ->
        if (activeChallenges.isEmpty() && waitingChallenges.isEmpty()) {
            HamBattleEmptyContent(
                modifier = Modifier.padding(innerPadding),
                onStartNewChallengeClick = onStartNewChallengeClick
            )
        } else {
            HamBattleChallengeListContent(
                modifier = Modifier.padding(innerPadding),
                activeChallenges = activeChallenges,
                waitingChallenges = waitingChallenges,
                onStartNewChallengeClick = onStartNewChallengeClick,
                onViewEndedChallengesClick = onViewEndedChallengesClick
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun HamBattleTopBar(onNotificationClick: () -> Unit) {
    CenterAlignedTopAppBar(
        title = {
            Text("햄배틀", style = MaterialTheme.typography.titleSmall, color = HPBlack)
        },
        actions = {
            IconButton(onClick = onNotificationClick) {
                Icon(Icons.Filled.Notifications, contentDescription = "알림", tint = HPBlack)
            }
        },
        colors = TopAppBarDefaults.topAppBarColors(containerColor = HPWhite)
    )
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
    activeChallenges: List<HamBattleActiveChallenge>,
    waitingChallenges: List<HamBattleWaitingChallenge>,
    onStartNewChallengeClick: () -> Unit,
    onViewEndedChallengesClick: () -> Unit
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
                ActiveChallengeCard(challenge)
            }
        }

        if (waitingChallenges.isNotEmpty()) {
            Spacer(modifier = Modifier.height(30.dp))
            SectionLabel("대기중")
            Spacer(modifier = Modifier.height(12.dp))
            waitingChallenges.forEachIndexed { index, challenge ->
                if (index > 0) Spacer(modifier = Modifier.height(16.dp))
                WaitingChallengeCard(challenge)
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
        Text("새 챌린지 시작하기", style = MaterialTheme.typography.bodyLarge, color = HPWhite)
    }
}

@Composable
private fun SectionLabel(text: String) {
    Text(text, style = Body16Bold, color = HPText)
}

@Composable
private fun ActiveChallengeCard(challenge: HamBattleActiveChallenge) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(20.dp))
            .background(HPSub4)
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
            fontWeight = FontWeight.Bold,
            color = HPMain
        )

        Spacer(modifier = Modifier.height(16.dp))
        if (challenge.isOneVsOne && challenge.participants.size == 2) {
            OneVsOneRow(
                first = challenge.participants[0],
                second = challenge.participants[1]
            )
        } else {
            RankedParticipantList(participants = challenge.participants)
        }

        Spacer(modifier = Modifier.height(14.dp))
        HorizontalDivider(color = HPText, thickness = 1.dp)
        Spacer(modifier = Modifier.height(10.dp))
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(challenge.dDay, style = Body16Bold, color = HPMain)
            Text(challenge.statusMessage, style = Body16Bold, color = StatusWhoWonText)
        }
    }
}

@Composable
private fun OneVsOneRow(first: HamBattleParticipantSpending, second: HamBattleParticipantSpending) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        ParticipantAvatarLabel(participant = first, modifier = Modifier.weight(1f, fill = false))
        Text(
            "vs",
            modifier = Modifier.padding(horizontal = 8.dp),
            style = MaterialTheme.typography.bodyMedium,
            color = HPText
        )
        ParticipantAvatarLabel(participant = second, modifier = Modifier.weight(1f, fill = false))
    }
}

@Composable
private fun ParticipantAvatarLabel(
    participant: HamBattleParticipantSpending,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .height(32.dp)
            .clip(RoundedCornerShape(20.dp))
            .background(HPWhite),
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            ParticipantAvatar(size = 22.dp)
            Spacer(modifier = Modifier.width(5.dp))
            Text(
                "${participant.name}  ${formatWon(participant.amount)}",
                style = MaterialTheme.typography.bodyMedium,
                maxLines = 1,
                autoSize = TextAutoSize.StepBased(minFontSize = 10.sp, maxFontSize = 14.sp),
                fontWeight = FontWeight.Bold,
                color = HPBlack
            )
        }
    }
}

@Composable
private fun RankedParticipantList(participants: List<HamBattleParticipantSpending>) {
    val maxAmount = participants.maxOf { it.amount }.toFloat()
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        participants.forEachIndexed { index, participant ->
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    "%02d".format(index + 1),
                    modifier = Modifier.width(24.dp),
                    style = MaterialTheme.typography.bodyMedium,
                    color = HPText
                )
                ParticipantAvatar(size = 24.dp)
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    participant.name,
                    modifier = Modifier.width(56.dp),
                    style = MaterialTheme.typography.bodyMedium,
                    color = HPBlack
                )
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .height(6.dp)
                        .clip(RoundedCornerShape(50))
                        .background(HPGray4)
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxHeight()
                            .fillMaxWidth(participant.amount / maxAmount)
                            .clip(RoundedCornerShape(50))
                            .background(HPMain)
                    )
                }
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    formatWon(participant.amount),
                    style = Body16Bold,
                    color = HPBlack
                )
            }
        }
    }
}

@Composable
private fun WaitingChallengeCard(challenge: HamBattleWaitingChallenge) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(20.dp))
            .background(HPGray3)
            .drawBehind {
                // 점선 박스 생성
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
            .padding(20.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            TypeBadge(text = challenge.type, muted = true)
            Text(
                "시작까지 ${challenge.startsInDay}",
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
            fontWeight = FontWeight.Bold,
            color = HPMain
        )

        Spacer(modifier = Modifier.height(16.dp))
        Row(verticalAlignment = Alignment.CenterVertically) {
            repeat(challenge.totalCount) { index ->
                if (index > 0) Spacer(modifier = Modifier.width(8.dp))
                if (index < challenge.joinedCount) {
                    ParticipantAvatar(size = 28.dp)
                } else {
                    EmptyAvatarSlot(size = 28.dp)
                }
            }
            Spacer(modifier = Modifier.weight(1f))
            Text(
                "${challenge.totalCount}명 중 ${challenge.joinedCount}명",
                style = MaterialTheme.typography.bodySmall,
                color = HPText
            )
        }

        Spacer(modifier = Modifier.height(14.dp))
        HorizontalDivider(color = HPText, thickness = 1.dp)
        Spacer(modifier = Modifier.height(10.dp))
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                challenge.startDateLabel,
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Bold,
                color = HPMain
            )
            Text(
                "링크 다시 복사",
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Bold,
                color = StatusWhoWonText
            )
        }
    }
}

@Composable
private fun TypeBadge(text: String, muted: Boolean = false) {
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(30))
            .background(if (muted) HPGray4 else HPMain)
            .padding(horizontal = 12.dp, vertical = 6.dp)
    ) {
        Text(
            text,
            style = MaterialTheme.typography.labelLarge,
            fontWeight = FontWeight.Bold,
            color = if (muted) HPText else HPWhite
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
private fun ParticipantAvatar(size: Dp) {
    Box(
        modifier = Modifier
            .size(size)
            .clip(CircleShape)
            .background(HPGray4)
    )
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

private fun formatWon(amount: Int): String {
    return String.format(Locale.KOREA, "%,d원", amount)
}

@Preview
@Composable
private fun HamBattleScreenPreview() {
    HampouchTheme {
        HamBattleScreen()
    }
}

@Preview
@Composable
private fun HamBattleScreenEmptyPreview() {
    HampouchTheme {
        HamBattleScreen(activeChallenges = emptyList(), waitingChallenges = emptyList())
    }
}
