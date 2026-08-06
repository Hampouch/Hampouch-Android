package com.example.hampouch.ui.hambattle

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.input.TextFieldLineLimits
import androidx.compose.foundation.text.input.TextFieldState
import androidx.compose.foundation.text.input.clearText
import androidx.compose.foundation.text.input.rememberTextFieldState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ArrowBack
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SelectableDates
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.PlatformTextStyle
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.LineHeightStyle
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.hampouch.data.model.HamBattleChallengeRequest
import com.example.hampouch.data.model.HamBattleDefaultPenaltyOptions
import com.example.hampouch.data.model.HamBattleDurationOptions
import com.example.hampouch.data.model.HamBattleParticipantOptions
import com.example.hampouch.ui.common.CheckButton
import com.example.hampouch.ui.dialog.HamBattleStartConfirmDialog
import com.example.hampouch.ui.theme.Body16Bold
import com.example.hampouch.ui.theme.HPBlack
import com.example.hampouch.ui.theme.HPGray1
import com.example.hampouch.ui.theme.HPGray5
import com.example.hampouch.ui.theme.HPMain
import com.example.hampouch.ui.theme.HPSub4
import com.example.hampouch.ui.theme.HPText
import com.example.hampouch.ui.theme.HPWhite
import com.example.hampouch.ui.theme.HampouchTheme
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneOffset
import java.time.format.DateTimeFormatter

private val StartDateFormatter = DateTimeFormatter.ofPattern("yyyy.MM.dd")

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HamBattleAddScreen(
    onBackClick: () -> Unit = {},
    onStartClick: (HamBattleChallengeRequest) -> Unit = {}
) {
    var challengeName by rememberSaveable { mutableStateOf("") }
    var selectedParticipantCount by rememberSaveable { mutableStateOf(HamBattleParticipantOptions[1]) }
    var selectedDuration by rememberSaveable { mutableStateOf(HamBattleDurationOptions[1]) }
    var startDateMillis by rememberSaveable { mutableStateOf<Long?>(null) }
    var showDatePicker by remember { mutableStateOf(false) }
    var showStartConfirmDialog by remember { mutableStateOf(false) }
    var penaltyOptions by remember { mutableStateOf(HamBattleDefaultPenaltyOptions) }
    var selectedPenalty by rememberSaveable { mutableStateOf(HamBattleDefaultPenaltyOptions.first()) }
    val customPenaltyInputState = rememberTextFieldState()

    val currentRequest = HamBattleChallengeRequest(
        challengeName = challengeName,
        participantCount = selectedParticipantCount,
        durationDays = selectedDuration,
        startDateMillis = startDateMillis,
        penalty = selectedPenalty
    )
    val isStartEnabled = challengeName.isNotBlank() &&
        selectedParticipantCount.isNotBlank() &&
        selectedDuration.isNotBlank() &&
        startDateMillis != null &&
        selectedPenalty.isNotBlank()

    Scaffold(
        topBar = { HamBattleAddTopBar(onBackClick = onBackClick) },
        containerColor = HPGray1
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .padding(innerPadding)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp)
                .fillMaxWidth()
        ) {
            ChallengeIntroSection()

            Spacer(modifier = Modifier.height(20.dp))
            ChallengeNameField(
                value = challengeName,
                onValueChange = { challengeName = it }
            )

            Spacer(modifier = Modifier.height(20.dp))
            ChallengeDetailsCard(
                selectedParticipantCount = selectedParticipantCount,
                onParticipantCountSelect = { selectedParticipantCount = it },
                selectedDuration = selectedDuration,
                onDurationSelect = { selectedDuration = it },
                startDateMillis = startDateMillis,
                onStartDateClick = { showDatePicker = true },
                penaltyOptions = penaltyOptions,
                selectedPenalty = selectedPenalty,
                onPenaltySelect = { selectedPenalty = it },
                customPenaltyInputState = customPenaltyInputState,
                onAddCustomPenalty = {
                    val newPenalty = customPenaltyInputState.text.toString().trim()
                    if (newPenalty.isNotEmpty()) {
                        if (newPenalty !in penaltyOptions) {
                            penaltyOptions = penaltyOptions + newPenalty
                        }
                        selectedPenalty = newPenalty
                        customPenaltyInputState.clearText()
                    }
                }
            )

            Spacer(modifier = Modifier.height(16.dp))
            Text(
                "시작일까지 아무도 참여하지 않으면 챌린지는 자동으로 사라져요",
                modifier = Modifier.fillMaxWidth(),
                style = MaterialTheme.typography.bodySmall,
                fontSize = 14.sp,
                color = HPText
            )

            Spacer(modifier = Modifier.height(40.dp))
            StartChallengeButton(
                enabled = isStartEnabled,
                onClick = { showStartConfirmDialog = true }
            )
        }
    }

    if (showDatePicker) {
        StartDatePickerDialog(
            initialDateMillis = startDateMillis,
            onDismiss = { showDatePicker = false },
            onConfirm = { selectedMillis ->
                startDateMillis = selectedMillis
                showDatePicker = false
            }
        )
    }

    if (showStartConfirmDialog) {
        HamBattleStartConfirmDialog(
            request = currentRequest,
            onCancel = { showStartConfirmDialog = false },
            onConfirm = {
                showStartConfirmDialog = false
                onStartClick(currentRequest)
            }
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun HamBattleAddTopBar(onBackClick: () -> Unit) {
    CenterAlignedTopAppBar(
        title = {
            Text("햄배틀", style = MaterialTheme.typography.titleSmall, color = HPBlack)
        },
        navigationIcon = {
            IconButton(onClick = onBackClick) {
                Icon(
                    Icons.AutoMirrored.Outlined.ArrowBack,
                    contentDescription = "뒤로가기",
                    tint = HPBlack
                )
            }
        },
        colors = TopAppBarDefaults.topAppBarColors(containerColor = HPGray1)
    )
}

@Composable
private fun ChallengeIntroSection() {
    Text("NEW CHALLENGE", style = Body16Bold, color = HPMain)
    Spacer(modifier = Modifier.height(8.dp))
    Text("누구랑 대결 해볼까요?", style = MaterialTheme.typography.titleMedium, color = HPBlack)
    Spacer(modifier = Modifier.height(4.dp))
    Text(
        "기간 내 식비 총액이 적은 사람이 우승해요.",
        style = MaterialTheme.typography.bodyMedium,
        color = HPText
    )
}

@Composable
private fun ChallengeNameField(value: String, onValueChange: (String) -> Unit) {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        modifier = Modifier.fillMaxWidth(),
        placeholder = {
            Text("챌린지 이름을 설정해주세요.", color = HPGray5)
        },
        singleLine = true,
        shape = RoundedCornerShape(10.dp),
        colors = OutlinedTextFieldDefaults.colors(
            unfocusedBorderColor = Color.Transparent,
            focusedBorderColor = HPMain,
            unfocusedContainerColor = HPWhite,
            focusedContainerColor = HPWhite
        )
    )
}

@Composable
private fun ChallengeDetailsCard(
    selectedParticipantCount: String,
    onParticipantCountSelect: (String) -> Unit,
    selectedDuration: String,
    onDurationSelect: (String) -> Unit,
    startDateMillis: Long?,
    onStartDateClick: () -> Unit,
    penaltyOptions: List<String>,
    selectedPenalty: String,
    onPenaltySelect: (String) -> Unit,
    customPenaltyInputState: TextFieldState,
    onAddCustomPenalty: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(20.dp))
            .background(HPSub4)
            .padding(horizontal = 15.dp, vertical = 20.dp)
    ) {
        OptionPillGroup(
            title = "참가 인원",
            options = HamBattleParticipantOptions,
            selected = selectedParticipantCount,
            onSelect = onParticipantCountSelect
        )

        Spacer(modifier = Modifier.height(20.dp))
        OptionPillGroup(
            title = "기간",
            options = HamBattleDurationOptions,
            selected = selectedDuration,
            onSelect = onDurationSelect
        )
        Spacer(modifier = Modifier.height(10.dp))
        StartDateField(dateMillis = startDateMillis, onClick = onStartDateClick)

        Spacer(modifier = Modifier.height(20.dp))
        PenaltySection(
            options = penaltyOptions,
            selected = selectedPenalty,
            onSelect = onPenaltySelect,
            inputState = customPenaltyInputState,
            onAddCustomPenalty = onAddCustomPenalty
        )
    }
}

@Composable
private fun OptionPillGroup(
    title: String,
    options: List<String>,
    selected: String,
    onSelect: (String) -> Unit
) {
    Text(title, style = Body16Bold, color = HPBlack)
    Spacer(modifier = Modifier.height(10.dp))
    FlowRow(
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        options.forEach { option ->
            SelectablePill(
                text = option,
                selected = option == selected,
                onClick = { onSelect(option) }
            )
        }
    }
}

@Composable
private fun PenaltySection(
    options: List<String>,
    selected: String,
    onSelect: (String) -> Unit,
    inputState: TextFieldState,
    onAddCustomPenalty: () -> Unit
) {
    OptionPillGroup(title = "벌칙", options = options, selected = selected, onSelect = onSelect)
    Spacer(modifier = Modifier.height(8.dp))
    Row(verticalAlignment = Alignment.CenterVertically) {
        OutlinedTextField(
            state = inputState,
            modifier = Modifier
                .weight(1f)
                .height(48.dp),
            placeholder = {
                Text(
                    "직접 입력",
                    color = HPGray5,
                    fontWeight = FontWeight.Bold,
                    style = TextStyle(
                        fontSize = 16.sp,
                        lineHeight = 24.sp,
                        platformStyle = PlatformTextStyle(includeFontPadding = false),
                        lineHeightStyle = LineHeightStyle(
                            alignment = LineHeightStyle.Alignment.Center,
                            trim = LineHeightStyle.Trim.Both
                        )
                    )
                )
            },
            lineLimits = TextFieldLineLimits.SingleLine,
            shape = RoundedCornerShape(10.dp),
            contentPadding = OutlinedTextFieldDefaults.contentPadding(top = 10.dp, bottom = 10.dp),
            colors = OutlinedTextFieldDefaults.colors(
                unfocusedBorderColor = HPGray5,
                focusedBorderColor = HPMain,
                unfocusedContainerColor = HPWhite,
                focusedContainerColor = HPWhite
            )
        )
        Spacer(modifier = Modifier.width(10.dp))
        CheckButton(onClick = onAddCustomPenalty)
    }
}

@Composable
private fun StartChallengeButton(enabled: Boolean, onClick: () -> Unit) {
    Button(
        onClick = onClick,
        enabled = enabled,
        modifier = Modifier
            .fillMaxWidth()
            .height(56.dp),
        shape = RoundedCornerShape(10.dp),
        colors = ButtonDefaults.buttonColors(
            containerColor = HPMain,
            disabledContainerColor = HPMain.copy(alpha = 0.4f),
            disabledContentColor = HPWhite.copy(alpha = 0.8f)
        )
    ) {
        Text("햄배틀 시작", style = MaterialTheme.typography.bodyLarge, color = HPWhite)
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun StartDatePickerDialog(
    initialDateMillis: Long?,
    onDismiss: () -> Unit,
    onConfirm: (Long?) -> Unit
) {
    val todayStartOfDayMillis =
        LocalDate.now().atStartOfDay(ZoneOffset.UTC).toInstant().toEpochMilli()
    val datePickerState = rememberDatePickerState(
        initialSelectedDateMillis = initialDateMillis,
        selectableDates = object : SelectableDates {
            override fun isSelectableDate(utcTimeMillis: Long): Boolean {
                return utcTimeMillis >= todayStartOfDayMillis
            }
        }
    )
    DatePickerDialog(
        onDismissRequest = onDismiss,
        confirmButton = {
            TextButton(onClick = { onConfirm(datePickerState.selectedDateMillis) }) {
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

@Composable
private fun SelectablePill(
    text: String,
    selected: Boolean,
    onClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(50))
            .background(if (selected) HPMain else HPWhite)
            .border(1.dp, if (selected) HPMain else HPGray5, RoundedCornerShape(50))
            .clickable(onClick = onClick)
            .padding(horizontal = 13.dp, vertical = 10.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = text,
            style = if (selected) Body16Bold else MaterialTheme.typography.bodyMedium,
            color = if (selected) HPWhite else HPBlack
        )
    }
}

@Composable
private fun StartDateField(dateMillis: Long?, onClick: () -> Unit) {
    val dateText = dateMillis?.let {
        Instant.ofEpochMilli(it).atZone(ZoneOffset.UTC).toLocalDate().format(StartDateFormatter)
    }
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(44.dp)
            .clip(RoundedCornerShape(10.dp))
            .background(HPWhite)
            .border(1.dp, HPGray5, RoundedCornerShape(10.dp))
            .clickable(onClick = onClick)
            .padding(horizontal = 15.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            Icons.Filled.CalendarMonth,
            contentDescription = null,
            tint = HPMain,
            modifier = Modifier.size(24.dp)
        )
        Spacer(modifier = Modifier.width(10.dp))
        Text(
            text = dateText ?: "시작일",
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.Bold,
            color = if (dateText != null) HPMain else HPGray5
        )
    }
}

@Preview
@Composable
private fun HamBattleAddScreenPreview() {
    HampouchTheme {
        HamBattleAddScreen()
    }
}
