package com.example.hampouch.ui.hambattle

import androidx.compose.foundation.background
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
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.hampouch.R
import com.example.hampouch.data.model.HamBattleParticipantSpending
import com.example.hampouch.data.model.HamBattleParticipantStatus
import com.example.hampouch.ui.theme.Body16Bold
import com.example.hampouch.ui.theme.HPBlack
import com.example.hampouch.ui.theme.HPGray4
import com.example.hampouch.ui.theme.HPGray5
import com.example.hampouch.ui.theme.HPMain
import com.example.hampouch.ui.theme.HPText
import com.example.hampouch.ui.theme.HPWhite
import com.example.hampouch.ui.theme.HampouchTheme
import java.util.Locale

val StatusWhoWonText = Color(0xFF5572AB)

private val ParticipantNameWidth = 56.dp

fun formatWon(amount: Int): String {
    return String.format(Locale.KOREA, "%,d원", amount)
}

@Composable
fun SectionLabel(text: String) {
    Text(text, style = Body16Bold, color = HPText)
}

@Composable
fun TypeBadge(text: String, muted: Boolean = false) {
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(30))
            .background(if (muted) HPGray5 else HPMain)
            .padding(horizontal = 12.dp, vertical = 6.dp)
    ) {
        Text(
            text,
            style = MaterialTheme.typography.labelLarge,
            fontWeight = FontWeight.Bold,
            color = HPWhite
        )
    }
}

/** avatarUrl이 null이거나 로드에 실패하면 기본 아바타([R.drawable.icon_normal_avatar])를 보여준다. */
@Composable
fun ParticipantAvatar(size: Dp, avatarUrl: String? = null) {
    Box(
        modifier = Modifier
            .size(size)
            .clip(CircleShape)
            .background(HPGray4)
    ) {
        AsyncImage(
            model = avatarUrl,
            contentDescription = null,
            contentScale = ContentScale.Crop,
            placeholder = painterResource(R.drawable.icon_normal_avatar),
            error = painterResource(R.drawable.icon_normal_avatar),
            fallback = painterResource(R.drawable.icon_normal_avatar),
            modifier = Modifier.fillMaxSize()
        )
    }
}

@Composable
fun ParticipantAvatarLabel(
    participant: HamBattleParticipantSpending,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(20.dp))
            .background(HPWhite),
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 10.dp, vertical = 6.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                ParticipantAvatar(size = 22.dp, avatarUrl = participant.avatarUrl)
                Spacer(modifier = Modifier.width(5.dp))
                Text(
                    participant.name,
                    modifier = Modifier.widthIn(max = 80.dp),
                    style = MaterialTheme.typography.bodySmall,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    color = HPBlack
                )
            }
            Text(
                formatWon(participant.amount),
                style = MaterialTheme.typography.bodySmall,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                color = HPBlack
            )
        }
    }
}

@Composable
private fun MyParticipantAvatarLabel(participant: HamBattleParticipantSpending, modifier: Modifier = Modifier) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(20.dp))
            .background(HPWhite),
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 10.dp, vertical = 6.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                ParticipantAvatar(size = 22.dp, avatarUrl = participant.avatarUrl)
                Spacer(modifier = Modifier.width(5.dp))
                Text(
                    participant.name,
                    modifier = Modifier.widthIn(max = 80.dp),
                    style = MaterialTheme.typography.bodySmall,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    color = HPBlack
                )
            }
            Text(
                formatWon(participant.amount),
                style = MaterialTheme.typography.bodySmall,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                color = HPBlack
            )
        }
    }
}

@Composable
fun OneVsOneRow(first: HamBattleParticipantSpending, second: HamBattleParticipantSpending) {
    val isFirstMe = first.name == "나"
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        if (isFirstMe) {
            MyParticipantAvatarLabel(participant = first, modifier = Modifier.weight(1f))
        } else {
            ParticipantAvatarLabel(participant = first, modifier = Modifier.weight(1f))
        }
        Text(
            "vs",
            modifier = Modifier.padding(horizontal = 8.dp),
            style = MaterialTheme.typography.bodyMedium,
            color = HPText
        )
        if (isFirstMe) {
            ParticipantAvatarLabel(participant = second, modifier = Modifier.weight(1f))
        } else {
            MyParticipantAvatarLabel(participant = second, modifier = Modifier.weight(1f))
        }
    }
}

@Composable
fun RankedParticipantList(
    participants: List<HamBattleParticipantSpending>,
    maxAmount: Float? = null
) {
    val normal = participants.filter { it.status != HamBattleParticipantStatus.DISQUALIFIED }
    val disqualified = participants.filter { it.status == HamBattleParticipantStatus.DISQUALIFIED }
    val ranked = normal.sortedBy { it.amount }
    val effectiveMaxAmount = maxAmount ?: participants.maxOf { it.amount }.toFloat()
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        ranked.forEachIndexed { index, participant ->
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    "%02d".format(index + 1),
                    modifier = Modifier.width(24.dp),
                    style = MaterialTheme.typography.bodyMedium,
                    color = HPBlack
                )
                ParticipantAvatar(size = 24.dp, avatarUrl = participant.avatarUrl)
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    participant.name,
                    modifier = Modifier.width(ParticipantNameWidth),
                    style = MaterialTheme.typography.bodyMedium,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    color = HPBlack
                )
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .height(6.dp)
                        .clip(RoundedCornerShape(50))
                        .background(HPWhite)
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxHeight()
                            .fillMaxWidth(participant.amount / effectiveMaxAmount)
                            .clip(RoundedCornerShape(50))
                            .background(HPMain)
                    )
                }
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    formatWon(participant.amount),
                    modifier = Modifier.width(80.dp),
                    fontSize = 14.sp,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    textAlign = TextAlign.End,
                    color = HPBlack
                )
            }
        }
        disqualified.forEach { participant ->
            DisqualifiedParticipantRow(participant = participant)
        }
    }
}

@Composable
private fun DisqualifiedParticipantRow(participant: HamBattleParticipantSpending) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .background(HPGray4)
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            "탈락",
            style = Body16Bold,
            color = HPText,
            fontSize = 14.sp,
            modifier = Modifier.width(28.dp)
        )
        ParticipantAvatar(size = 24.dp, avatarUrl = participant.avatarUrl)
        Spacer(modifier = Modifier.width(8.dp))
        Text(
            participant.name,
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.Bold,
            color = HPText
        )
    }
}

@Preview(showBackground = true)
@Composable
fun SectionLabelPreview() {
    HampouchTheme {
        SectionLabel(text = "섹션 라벨")
    }
}

@Preview(showBackground = true)
@Composable
fun TypeBadgePreview() {
    HampouchTheme {
        Column(
            modifier = Modifier.padding(10.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            TypeBadge(text = "기본 뱃지")
            TypeBadge(text = "비활성 뱃지", muted = true)
        }
    }
}

@Preview(showBackground = true)
@Composable
fun ParticipantAvatarPreview() {
    HampouchTheme {
        ParticipantAvatar(size = 40.dp)
    }
}

@Preview(showBackground = true)
@Composable
fun ParticipantAvatarLabelPreview() {
    HampouchTheme {
        Column(
            modifier = Modifier
                .background(HPGray5)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            ParticipantAvatarLabel(
                participant = HamBattleParticipantSpending("햄스터", 45000)
            )
            MyParticipantAvatarLabel(
                participant = HamBattleParticipantSpending("나", 12000)
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
fun OneVsOneRowPreview() {
    HampouchTheme {
        Box(
            modifier = Modifier
                .background(HPGray5)
                .padding(16.dp)
        ) {
            OneVsOneRow(
                first = HamBattleParticipantSpending("나", 50000),
                second = HamBattleParticipantSpending("상대방", 35000)
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
fun RankedParticipantListPreview() {
    HampouchTheme {
        val mockParticipants = listOf(
            HamBattleParticipantSpending("나", 50000),
            HamBattleParticipantSpending("친구1", 30000),
            HamBattleParticipantSpending("친구2", 85000),
            HamBattleParticipantSpending("친구3", 15000)
        )
        Box(
            modifier = Modifier
                .background(HPGray5)
                .padding(16.dp)
        ) {
            RankedParticipantList(participants = mockParticipants)
        }
    }
}
