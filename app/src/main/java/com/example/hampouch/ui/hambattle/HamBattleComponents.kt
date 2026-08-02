package com.example.hampouch.ui.hambattle

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.TextAutoSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.hampouch.data.model.HamBattleParticipantSpending
import com.example.hampouch.ui.theme.Body16Bold
import com.example.hampouch.ui.theme.HPBlack
import com.example.hampouch.ui.theme.HPGray2
import com.example.hampouch.ui.theme.HPGray4
import com.example.hampouch.ui.theme.HPGray5
import com.example.hampouch.ui.theme.HPMain
import com.example.hampouch.ui.theme.HPText
import com.example.hampouch.ui.theme.HPWhite
import java.util.Locale

val StatusWhoWonText = Color(0xFF5572AB)

private val ParticipantNameWidth = 56.dp

private val MyParticipantCardWidth = 130.dp

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

@Composable
fun ParticipantAvatar(size: Dp) {
    Box(
        modifier = Modifier
            .size(size)
            .clip(CircleShape)
            .background(HPGray4)
    )
}

@Composable
fun ParticipantAvatarLabel(
    participant: HamBattleParticipantSpending,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(32.dp)
            .clip(RoundedCornerShape(20.dp))
            .background(HPWhite),
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 10.dp, vertical = 5.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                ParticipantAvatar(size = 22.dp)
                Spacer(modifier = Modifier.width(5.dp))
                Text(
                    participant.name,
                    modifier = Modifier.width(ParticipantNameWidth),
                    style = MaterialTheme.typography.bodyMedium,
                    fontSize = 14.sp,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    color = HPBlack
                )
            }
            Text(
                formatWon(participant.amount),
                style = MaterialTheme.typography.bodyMedium,
                maxLines = 1,
                autoSize = TextAutoSize.StepBased(minFontSize = 10.sp, maxFontSize = 14.sp),
                color = HPBlack
            )
        }
    }
}

@Composable
private fun MyParticipantAvatarLabel(participant: HamBattleParticipantSpending) {
    Box(
        modifier = Modifier
            .width(MyParticipantCardWidth)
            .height(32.dp)
            .clip(RoundedCornerShape(20.dp))
            .background(HPWhite),
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 10.dp, vertical = 5.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                ParticipantAvatar(size = 22.dp)
                Spacer(modifier = Modifier.width(5.dp))
                Text(
                    participant.name,
                    style = MaterialTheme.typography.bodyMedium,
                    fontSize = 14.sp,
                    maxLines = 1,
                    color = HPBlack
                )
            }
            Text(
                formatWon(participant.amount),
                style = MaterialTheme.typography.bodyMedium,
                maxLines = 1,
                autoSize = TextAutoSize.StepBased(minFontSize = 10.sp, maxFontSize = 14.sp),
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
            MyParticipantAvatarLabel(participant = first)
        } else {
            ParticipantAvatarLabel(participant = first)
        }
        Text(
            "vs",
            modifier = Modifier.padding(horizontal = 8.dp),
            style = MaterialTheme.typography.bodyMedium,
            color = HPText
        )
        if (isFirstMe) {
            ParticipantAvatarLabel(participant = second)
        } else {
            MyParticipantAvatarLabel(participant = second)
        }
    }
}

@Composable
fun RankedParticipantList(
    participants: List<HamBattleParticipantSpending>,
    maxAmount: Float? = null
) {
    val ranked = participants.sortedBy { it.amount }
    val effectiveMaxAmount = maxAmount ?: ranked.maxOf { it.amount }.toFloat()
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
                ParticipantAvatar(size = 24.dp)
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
                    textAlign = TextAlign.End,
                    color = HPBlack
                )
            }
        }
    }
}
