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
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.hampouch.data.model.HamBattleParticipantSpending
import com.example.hampouch.ui.theme.Body16Bold
import com.example.hampouch.ui.theme.HPBlack
import com.example.hampouch.ui.theme.HPGray2
import com.example.hampouch.ui.theme.HPGray4
import com.example.hampouch.ui.theme.HPMain
import com.example.hampouch.ui.theme.HPText
import com.example.hampouch.ui.theme.HPWhite
import java.util.Locale

val StatusWhoWonText = Color(0xFF5572AB)

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
            .background(if (muted) HPText else HPMain)
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
            .background(HPGray2)
    )
}

@Composable
fun ParticipantAvatarLabel(
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
fun OneVsOneRow(first: HamBattleParticipantSpending, second: HamBattleParticipantSpending) {
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
fun RankedParticipantList(participants: List<HamBattleParticipantSpending>) {
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
                    color = HPBlack
                )
                ParticipantAvatar(size = 24.dp)
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    participant.name,
                    modifier = Modifier.width(56.dp),
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Bold,
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
