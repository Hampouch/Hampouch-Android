package com.example.hampouch.ui.onboarding.steps

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.ChevronLeft
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.hampouch.R
import com.example.hampouch.ui.onboarding.components.OnboardingPrimaryButton
import com.example.hampouch.ui.theme.HPBlack
import com.example.hampouch.ui.theme.HPGray4
import com.example.hampouch.ui.theme.HPMain
import com.example.hampouch.ui.theme.HPSub1
import com.example.hampouch.ui.theme.HPSub3
import com.example.hampouch.ui.theme.HPText
import com.example.hampouch.ui.theme.HPWhite
import com.example.hampouch.ui.theme.HampouchTheme
import java.time.YearMonth
import java.time.temporal.WeekFields
import java.util.Locale

@Composable
fun SalaryCalendarSheet(
    initialSelectedDay: Int?,
    onDaySelected: (Int) -> Unit,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier
) {
    var yearMonth by remember { mutableStateOf(YearMonth.now()) }
    var selectedDay by remember { mutableStateOf(initialSelectedDay) }
    val weekdays = stringResource(R.string.onboarding_calendar_weekdays).split(",")

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(HPBlack.copy(alpha = 0.5f))
                .clickable(
                    indication = null,
                    interactionSource = remember { MutableInteractionSource() },
                    onClick = onDismiss
                ),
            contentAlignment = Alignment.Center
        ) {
            Column(
                modifier = modifier
                    .padding(horizontal = 24.dp)
                    .background(HPWhite, RoundedCornerShape(24.dp))
                    .clickable(
                        indication = null,
                        interactionSource = remember { MutableInteractionSource() },
                        onClick = {}
                    )
                    .padding(20.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Row(
                    modifier = Modifier
                        .background(HPSub3, RoundedCornerShape(16.dp))
                        .padding(horizontal = 14.dp, vertical = 8.dp),
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Filled.CalendarMonth,
                        contentDescription = null,
                        tint = HPMain,
                        modifier = Modifier.size(16.dp)
                    )
                    Text(
                        text = stringResource(R.string.onboarding_step7_title),
                        style = MaterialTheme.typography.labelLarge,
                        color = HPSub1
                    )
                }

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 16.dp),
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(onClick = { yearMonth = yearMonth.minusMonths(1) }) {
                        Icon(
                            imageVector = Icons.Filled.ChevronLeft,
                            contentDescription = stringResource(R.string.cd_previous_month),
                            tint = HPSub1
                        )
                    }
                    Box(
                        modifier = Modifier
                            .background(HPGray4, RoundedCornerShape(10.dp))
                            .padding(horizontal = 20.dp, vertical = 6.dp)
                    ) {
                        Text(
                            text = stringResource(
                                R.string.onboarding_calendar_month_format,
                                yearMonth.year,
                                yearMonth.monthValue
                            ),
                            style = MaterialTheme.typography.labelLarge,
                            color = HPSub1
                        )
                    }
                    IconButton(onClick = { yearMonth = yearMonth.plusMonths(1) }) {
                        Icon(
                            imageVector = Icons.Filled.ChevronRight,
                            contentDescription = stringResource(R.string.cd_next_month),
                            tint = HPSub1
                        )
                    }
                }

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 12.dp),
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    weekdays.forEach { day ->
                        Text(
                            text = day,
                            style = MaterialTheme.typography.labelSmall,
                            color = HPText,
                            textAlign = TextAlign.Center,
                            modifier = Modifier.weight(1f)
                        )
                    }
                }

                val leadingBlanks = yearMonth.atDay(1).get(WeekFields.of(Locale.KOREA).dayOfWeek()) - 1
                val daysInMonth = yearMonth.lengthOfMonth()
                val cells: List<Int?> = List(leadingBlanks) { null } + (1..daysInMonth).toList()

                LazyVerticalGrid(
                    columns = GridCells.Fixed(7),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 8.dp),
                    horizontalArrangement = Arrangement.spacedBy(4.dp),
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    items(cells) { day ->
                        if (day == null) {
                            Box(modifier = Modifier.aspectRatio(1f))
                        } else {
                            val isSelected = day == selectedDay
                            Box(
                                modifier = Modifier
                                    .aspectRatio(1f)
                                    .background(
                                        color = if (isSelected) HPMain else HPSub3,
                                        shape = CircleShape
                                    )
                                    .clickable { selectedDay = day },
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = day.toString(),
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = if (isSelected) HPWhite else HPSub1
                                )
                            }
                        }
                    }
                }

                OnboardingPrimaryButton(
                    text = stringResource(R.string.onboarding_confirm),
                    enabled = selectedDay != null,
                    onClick = { selectedDay?.let(onDaySelected) },
                    modifier = Modifier.padding(top = 20.dp)
                )
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun SalaryCalendarSheetPreview() {
    HampouchTheme {
        SalaryCalendarSheet(
            initialSelectedDay = 15,
            onDaySelected = {},
            onDismiss = {}
        )
    }
}
