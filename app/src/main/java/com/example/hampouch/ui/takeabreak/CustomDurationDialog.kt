package com.example.hampouch.ui.takeabreak

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SelectableDates
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.hampouch.ui.theme.HPBlack
import com.example.hampouch.ui.theme.HPGray5
import com.example.hampouch.ui.theme.HPMain
import com.example.hampouch.ui.theme.HPSub3
import com.example.hampouch.ui.theme.HPText
import com.example.hampouch.ui.theme.HPWhite
import com.example.hampouch.ui.theme.HampouchTheme
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneOffset
import java.time.temporal.ChronoUnit

private fun LocalDate.toUtcMillis(): Long = atStartOfDay(ZoneOffset.UTC).toInstant().toEpochMilli()

private fun Long.toUtcLocalDate(): LocalDate =
    Instant.ofEpochMilli(this).atZone(ZoneOffset.UTC).toLocalDate()

private fun daysFromToday(today: LocalDate, date: LocalDate): Int =
    ChronoUnit.DAYS.between(today, date).toInt().coerceAtLeast(1)

@Composable
fun CustomDurationDialog(
    initialDays: Int?,
    onDismiss: () -> Unit,
    onConfirm: (days: Int) -> Unit,
    modifier: Modifier = Modifier
) {
    val today = remember { LocalDate.now() }
    var selectedDate by remember {
        mutableStateOf(initialDays?.let { today.plusDays(it.toLong()) })
    }
    var showDatePicker by remember { mutableStateOf(false) }

    val confirmAndClose: () -> Unit = {
        selectedDate?.let { onConfirm(daysFromToday(today, it)) } ?: onDismiss()
    }

    Dialog(
        onDismissRequest = confirmAndClose,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(HPBlack.copy(alpha = 0.5f))
                .noRippleClickable(confirmAndClose),
            contentAlignment = Alignment.Center
        ) {
            Column(
                modifier = modifier
                    .padding(horizontal = 40.dp)
                    .noRippleClickable {}
                    .padding(vertical = 32.dp, horizontal = 24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Box(
                    modifier = Modifier
                        .size(96.dp)
                        .background(HPWhite, CircleShape)
                        .border(1.dp, HPGray5, CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        modifier = Modifier.size(40.dp),
                        imageVector = Icons.Default.CalendarMonth,
                        tint = HPMain,
                        contentDescription = "달력"
                    )
                }
                Spacer(modifier = Modifier.height(16.dp))
                DateInputChip(
                    label = selectedDate?.let { "${daysFromToday(today, it)}일 푹 쉬기" } ?: "날짜 입력",
                    onClick = { showDatePicker = true }
                )
            }
        }
    }

    if (showDatePicker) {
        FutureDatePickerDialog(
            today = today,
            initialDate = selectedDate ?: today,
            onDismiss = { showDatePicker = false },
            onDateSelected = {
                selectedDate = it
                showDatePicker = false
            }
        )
    }
}

@Composable
private fun DateInputChip(label: String, onClick: () -> Unit, modifier: Modifier = Modifier) {
    Row(
        modifier = modifier
            .background(HPWhite, RoundedCornerShape(30))
            .clickable(onClick = onClick)
            .padding(horizontal = 14.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = Icons.Filled.Edit,
            contentDescription = null,
            tint = HPText,
            modifier = Modifier.size(14.dp)
        )
        Spacer(modifier = Modifier.width(6.dp))
        Text(
            text = label,
            style = MaterialTheme.typography.labelLarge,
            fontWeight = FontWeight.Bold,
            color = HPText
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun FutureDatePickerDialog(
    today: LocalDate,
    initialDate: LocalDate,
    onDismiss: () -> Unit,
    onDateSelected: (LocalDate) -> Unit
) {
    val datePickerState = rememberDatePickerState(
        initialSelectedDateMillis = initialDate.toUtcMillis(),
        selectableDates = object : SelectableDates {
            override fun isSelectableDate(utcTimeMillis: Long): Boolean =
                utcTimeMillis.toUtcLocalDate().isAfter(today)
        }
    )
    DatePickerDialog(
        onDismissRequest = onDismiss,
        confirmButton = {
            TextButton(onClick = {
                datePickerState.selectedDateMillis?.let { onDateSelected(it.toUtcLocalDate()) }
            }) {
                Text("확인")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("취소")
            }
        }
    ) {
        DatePicker(state = datePickerState)
    }
}

@Preview(showBackground = true)
@Composable
private fun CustomDurationDialogPreview() {
    HampouchTheme {
        CustomDurationDialog(initialDays = 5, onDismiss = {}, onConfirm = {})
    }
}
