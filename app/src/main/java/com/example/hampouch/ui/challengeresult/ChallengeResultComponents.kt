package com.example.hampouch.ui.challengeresult

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowLeft
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.MoreHoriz
import androidx.compose.material.icons.filled.PieChart
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.hampouch.R
import com.example.hampouch.data.model.DailyRecordStatus
import com.example.hampouch.data.model.EmotionStat
import com.example.hampouch.data.model.SpendingEmotion
import com.example.hampouch.ui.theme.Body16Bold
import com.example.hampouch.ui.theme.HPBlack
import com.example.hampouch.ui.theme.HPGray4
import com.example.hampouch.ui.theme.HPMain
import com.example.hampouch.ui.theme.HPStatusFailBg
import com.example.hampouch.ui.theme.HPStatusFailText
import com.example.hampouch.ui.theme.HPStatusSuccessBg
import com.example.hampouch.ui.theme.HPStatusSuccessText
import com.example.hampouch.ui.theme.HPSub
import com.example.hampouch.ui.theme.HPSub1
import com.example.hampouch.ui.theme.HPSub2
import com.example.hampouch.ui.theme.HPSub3
import com.example.hampouch.ui.theme.HPSub4
import com.example.hampouch.ui.theme.HPText
import com.example.hampouch.ui.theme.HPTipEtcBg
import com.example.hampouch.ui.theme.HPTipEtcText
import com.example.hampouch.ui.theme.HPTipRecordBg
import com.example.hampouch.ui.theme.HPTipRecordText
import com.example.hampouch.ui.theme.HPTipRecruitBg
import com.example.hampouch.ui.theme.HPTipRecruitText
import com.example.hampouch.ui.theme.HPWhite
import java.time.LocalDate
import java.time.YearMonth
import java.util.Locale

val RecordSuccessColor = HPSub2
val RecordFailColor = HPSub

fun formatWon(amount: Int): String = String.format(Locale.KOREA, "%,d원", amount)

private fun formatNumber(amount: Int): String = String.format(Locale.KOREA, "%,d", amount)

@Composable
fun StatBox(label: String, value: String, highlighted: Boolean, modifier: Modifier = Modifier) {
    Column(
        modifier = modifier
            .clip(RoundedCornerShape(10.dp))
            .background(if (highlighted) HPMain else Color.Transparent)
            .border(
                width = if (highlighted) 0.dp else 1.dp,
                shape = RoundedCornerShape(10.dp),
                color = HPMain
            )
            .padding(vertical = 14.dp, horizontal = 8.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            label,
            style = MaterialTheme.typography.labelMedium,
            color = if (highlighted) HPWhite else HPText
        )
        Spacer(modifier = Modifier.height(6.dp))
        Text(
            value,
            style = Body16Bold,
            color = if (highlighted) HPWhite else HPBlack
        )
    }
}

@Composable
fun GoalSummaryCard(
    label: String,
    amount: Int,
    goalAmount: Int,
    actualAmount: Int,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(20.dp))
            .background(HPSub4)
            .padding(horizontal = 30.dp, vertical = 10.dp)
    ) {
        Text(label, style = Body16Bold, color = HPBlack)
        Spacer(modifier = Modifier.height(5.dp))
        Row(verticalAlignment = Alignment.Bottom) {
            Text(
                formatNumber(amount),
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold,
                color = HPSub1
            )
            Spacer(modifier = Modifier.width(5.dp))
            Text(
                "원",
                style = MaterialTheme.typography.bodyMedium,
                color = HPBlack,
                modifier = Modifier.padding(bottom = 4.dp)
            )
        }
        Spacer(modifier = Modifier.height(5.dp))
        Text(
            "목표 ${formatWon(goalAmount)}  →  실제 ${formatWon(actualAmount)} 사용",
            style = MaterialTheme.typography.bodySmall,
            fontSize = 14.sp,
            color = HPBlack
        )
    }
}

@Composable
fun ExpenseAnalysisLinkButton(onClick: () -> Unit, modifier: Modifier = Modifier) {
    OutlinedButton(
        onClick = onClick,
        modifier = modifier
            .fillMaxWidth()
            .height(58.dp),
        shape = RoundedCornerShape(10.dp),
        border = BorderStroke(1.dp, HPMain),
        colors = ButtonDefaults.outlinedButtonColors(
            containerColor = HPWhite,
            contentColor = HPMain
        )
    ) {
        Icon(
            Icons.Filled.PieChart,
            contentDescription = null,
            tint = HPMain,
            modifier = Modifier.size(20.dp)
        )
        Spacer(modifier = Modifier.width(8.dp))
        Text(
            "자세한 식비 지출 분석 보기",
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.Bold,
            color = HPBlack,
            modifier = Modifier.weight(1f)
        )
        Icon(Icons.AutoMirrored.Filled.KeyboardArrowRight, contentDescription = null, tint = HPText)
    }
}

private data class EmotionVisual(
    val iconRes: Int?,
    val background: Color,
    val tint: Color,
    val label: String
)

private fun SpendingEmotion.toVisual(): EmotionVisual = when (this) {
    SpendingEmotion.CRAVING -> EmotionVisual(
        R.drawable.icon_fork,
        Color(0x33729739),
        Color(0xFF729739),
        "먹고 싶어서"
    )

    SpendingEmotion.STRESS -> EmotionVisual(
        R.drawable.icon_stress,
        Color(0x33E17866),
        Color(0xFFE17866),
        "스트레스"
    )

    SpendingEmotion.LAZY -> EmotionVisual(
        R.drawable.icon_sleepy,
        Color(0x3342A8A7),
        Color(0x8542A8A7),
        "귀찮아서"
    )

    SpendingEmotion.REWARD -> EmotionVisual(
        R.drawable.icon_present,
        Color(0x33B19347),
        Color(0xFFB19347),
        "보상"
    )

    // 기타 항목은 별도 png가 없어 벡터 아이콘을 그대로 사용
    SpendingEmotion.ETC -> EmotionVisual(null, HPTipEtcBg, HPBlack, "기타")
}

@Composable
fun SpendingEmotionAnalysis(stats: List<EmotionStat>, modifier: Modifier = Modifier) {
    Column(modifier = modifier.fillMaxWidth()) {
        Text("소비 감정 분석", style = Body16Bold, fontSize = 18.sp, color = HPBlack)
        Spacer(modifier = Modifier.height(10.dp))
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            stats.forEach { stat ->
                EmotionChip(stat = stat, modifier = Modifier.weight(1f))
            }
        }
    }
}

@Composable
private fun EmotionChip(stat: EmotionStat, modifier: Modifier = Modifier) {
    val visual = stat.emotion.toVisual()
    Column(
        modifier = modifier
            .width(67.dp)
            .height(94.dp)
            .clip(RoundedCornerShape(10.dp))
            .background(visual.background)
            .padding(vertical = 12.dp, horizontal = 4.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.SpaceEvenly
    ) {
        if (visual.iconRes != null) {
            Image(
                painter = painterResource(visual.iconRes),
                contentDescription = null,
                modifier = Modifier.size(22.dp)
            )
        } else {
            Icon(
                Icons.Filled.MoreHoriz,
                contentDescription = null,
                tint = visual.tint,
                modifier = Modifier.size(20.dp)
            )
        }
        Spacer(modifier = Modifier.height(6.dp))
        Text("${stat.percent}%", style = Body16Bold, color = HPBlack)
        Spacer(modifier = Modifier.height(2.dp))
        Text(
            visual.label,
            style = MaterialTheme.typography.labelSmall,
            color = HPText,
            fontWeight = FontWeight.Bold,
            fontSize = 9.sp,
            maxLines = 1
        )
    }
}

@Composable
fun DailyRecordCalendar(
    periodStart: LocalDate,
    periodEnd: LocalDate,
    records: Map<LocalDate, DailyRecordStatus>,
    modifier: Modifier = Modifier
) {
    val periodMonths = remember(periodStart, periodEnd) {
        generateSequence(YearMonth.from(periodStart)) { it.plusMonths(1) }
            .takeWhile { it <= YearMonth.from(periodEnd) }
            .toList()
    }
    var monthIndex by remember(periodMonths) { mutableIntStateOf(0) }
    val yearMonth = periodMonths[monthIndex]

    Column(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(20.dp))
            .background(HPSub4)
            .padding(16.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(
                onClick = { monthIndex-- },
                enabled = monthIndex > 0
            ) {
                Icon(
                    Icons.AutoMirrored.Filled.KeyboardArrowLeft,
                    contentDescription = "이전 달",
                    tint = if (monthIndex > 0) HPBlack else HPGray4
                )
            }
            Text(
                "${yearMonth.year}년 ${yearMonth.monthValue}월",
                style = Body16Bold,
                color = HPBlack
            )
            IconButton(
                onClick = { monthIndex++ },
                enabled = monthIndex < periodMonths.lastIndex
            ) {
                Icon(
                    Icons.AutoMirrored.Filled.KeyboardArrowRight,
                    contentDescription = "다음 달",
                    tint = if (monthIndex < periodMonths.lastIndex) HPBlack else HPGray4
                )
            }
        }

        Spacer(modifier = Modifier.height(8.dp))
        Row(modifier = Modifier.fillMaxWidth()) {
            listOf("일", "월", "화", "수", "목", "금", "토").forEach { day ->
                Text(
                    day,
                    modifier = Modifier.weight(1f),
                    style = MaterialTheme.typography.labelMedium,
                    color = HPSub2,
                    fontSize = 16.sp,
                    textAlign = TextAlign.Center
                )
            }
        }

        Spacer(modifier = Modifier.height(8.dp))
        val days = remember(yearMonth) { generateCalendarGrid(yearMonth) }
        days.chunked(7).forEach { week ->
            Row(modifier = Modifier.fillMaxWidth()) {
                week.forEach { date ->
                    CalendarDayCell(
                        date = date,
                        inCurrentMonth = YearMonth.from(date) == yearMonth,
                        inPeriod = !date.isBefore(periodStart) && !date.isAfter(periodEnd),
                        status = records[date],
                        modifier = Modifier.weight(1f)
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(12.dp))
        Row(
            modifier = Modifier.padding(start = 20.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            CalendarLegendItem(color = RecordSuccessColor, label = "성공")
            Spacer(modifier = Modifier.width(16.dp))
            CalendarLegendItem(color = RecordFailColor, label = "실패")
        }
    }
}

private fun generateCalendarGrid(yearMonth: YearMonth): List<LocalDate> {
    val firstDayOfMonth = yearMonth.atDay(1)
    val leadingDays = firstDayOfMonth.dayOfWeek.value % 7
    val gridStart = firstDayOfMonth.minusDays(leadingDays.toLong())
    val totalCells = leadingDays + yearMonth.lengthOfMonth()
    val weeks = (totalCells + 6) / 7
    return (0 until weeks * 7).map { gridStart.plusDays(it.toLong()) }
}

@Composable
private fun CalendarDayCell(
    date: LocalDate,
    inCurrentMonth: Boolean,
    inPeriod: Boolean,
    status: DailyRecordStatus?,
    modifier: Modifier = Modifier
) {
    val background = when (status) {
        DailyRecordStatus.SUCCESS -> RecordSuccessColor
        DailyRecordStatus.FAIL -> RecordFailColor
        null -> Color.Transparent
    }
    val textColor = when {
        status != null -> HPBlack
        !inCurrentMonth -> HPMain
        inPeriod -> HPBlack
        else -> HPText
    }
    Box(
        modifier = modifier
            .padding(2.dp)
            .aspectRatio(1f)
            .clip(RoundedCornerShape(10.dp))
            .background(background),
        contentAlignment = Alignment.Center
    ) {
        Text(
            date.dayOfMonth.toString(),
            style = MaterialTheme.typography.labelMedium,
            fontWeight = FontWeight.Bold,
            fontSize = 16.sp,
            color = textColor
        )
    }
}

@Composable
private fun CalendarLegendItem(color: Color, label: String) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Box(
            modifier = Modifier
                .size(16.dp)
                .clip(CircleShape)
                .background(color)
        )
        Spacer(modifier = Modifier.width(6.dp))
        Text(label, style = MaterialTheme.typography.labelMedium, fontSize = 14.sp, color = HPText)
    }
}

@Composable
fun ChallengeResultBottomActions(
    onShareClick: () -> Unit,
    onStartNewChallengeClick: () -> Unit,
    onTakeABreakClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier.fillMaxWidth()) {
        OutlinedButton(
            onClick = onShareClick,
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp),
            shape = RoundedCornerShape(10.dp),
            border = BorderStroke(1.dp, HPMain),
            colors = ButtonDefaults.outlinedButtonColors(
                containerColor = HPWhite,
                contentColor = HPBlack
            )
        ) {
            Text(
                "결과 공유하기",
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.Bold
            )
        }
        Spacer(modifier = Modifier.height(20.dp))
        Button(
            onClick = onStartNewChallengeClick,
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp),
            shape = RoundedCornerShape(10.dp),
            colors = ButtonDefaults.buttonColors(containerColor = HPMain)
        ) {
            Text(
                "다음 챌린지 시작하기",
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.Bold,
                color = HPWhite
            )
        }
        Spacer(modifier = Modifier.height(10.dp))
        TextButton(
            onClick = onTakeABreakClick,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(
                "챌린지 쉬어가기",
                style = MaterialTheme.typography.bodyMedium,
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = HPText
            )
        }
    }
}
