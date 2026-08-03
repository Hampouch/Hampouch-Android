package com.example.hampouch.ui.challengeresult

import android.content.ActivityNotFoundException
import android.net.Uri
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.graphics.asAndroidBitmap
import androidx.compose.ui.graphics.layer.drawLayer
import androidx.compose.ui.graphics.rememberGraphicsLayer
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.hampouch.R
import com.example.hampouch.data.model.ChallengeResultStatus
import com.example.hampouch.data.model.ChallengeResultUiState
import com.example.hampouch.ui.theme.Body16Bold
import com.example.hampouch.ui.theme.HPBlack
import com.example.hampouch.ui.theme.HPSub1
import com.example.hampouch.ui.theme.HPSub2
import com.example.hampouch.ui.theme.HPSub4
import com.example.hampouch.ui.theme.HPText
import com.example.hampouch.ui.theme.HPWhite
import com.example.hampouch.ui.theme.HampouchTheme
import java.time.LocalDate
import kotlinx.coroutines.launch

private fun formatPeriodDate(date: LocalDate): String = "${date.monthValue}월 ${date.dayOfMonth}일"

@Composable
fun ChallengeResultScreen(
    state: ChallengeResultUiState = ChallengeResultMockData.inProgress(),
    onBackClick: () -> Unit = {},
    onExpenseAnalysisClick: () -> Unit = {},
    onAdjustGoalClick: () -> Unit = {},
    onShareClick: () -> Unit = {},
    onStartNewChallengeClick: () -> Unit = {},
    onTakeABreakClick: () -> Unit = {}
) {
    val isFinished = state.status != ChallengeResultStatus.IN_PROGRESS
    var showGoalAdjustmentDialog by remember { mutableStateOf(false) }
    var showShareOptionsDialog by remember { mutableStateOf(false) }

    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    val screenGraphicsLayer = rememberGraphicsLayer()

    var pendingShareUri by remember { mutableStateOf<Uri?>(null) }
    val shareLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult()
    ) {
        pendingShareUri?.let { uri -> deleteImage(context, uri) }
        pendingShareUri = null
    }

    Scaffold(
        topBar = { ChallengeResultTopBar(onBackClick = onBackClick) },
        containerColor = HPWhite
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .verticalScroll(rememberScrollState())
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .drawWithContent {
                        screenGraphicsLayer.record { this@drawWithContent.drawContent() }
                        drawLayer(screenGraphicsLayer)
                    }
            ) {
                Box(
                    modifier = Modifier
                        .matchParentSize()
                        .background(HPWhite)
                )

                Column(modifier = Modifier.fillMaxWidth()) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 20.dp)
                    ) {
                        ChallengeStatusHeroCard(state = state)
                    }

                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 20.dp),
                        verticalArrangement = Arrangement.spacedBy(20.dp)
                    ) {
                        Spacer(modifier = Modifier.height(4.dp))

                        if (isFinished) {
                            GoalSummaryCard(
                                label = if (state.status == ChallengeResultStatus.COMPLETE) "목표 대비 총 절약" else state.amountLabel,
                                amount = state.amountValue,
                                goalAmount = state.goalAmount,
                                actualAmount = state.actualAmount
                            )
                        }

                        if (state.status == ChallengeResultStatus.IN_PROGRESS) {
                            GoalAmountAdjustmentLinkButton(onClick = onAdjustGoalClick)
                        }
                        ExpenseAnalysisLinkButton(onClick = onExpenseAnalysisClick)
                        SpendingEmotionAnalysis(stats = state.emotionStats)
                    }
                }
            }

            Spacer(modifier = Modifier.height(20.dp))
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp)
            ) {
                Text(
                    "하루하루 기록",
                    style = Body16Bold,
                    fontSize = 18.sp,
                    color = HPBlack
                )
                Spacer(modifier = Modifier.height(10.dp))
                DailyRecordCalendar(
                    periodStart = state.periodStart,
                    periodEnd = state.periodEnd,
                    records = state.dailyRecords
                )
            }

            if (isFinished) {
                Spacer(modifier = Modifier.height(20.dp))
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 20.dp)
                ) {
                    ChallengeResultBottomActions(
                        onShareClick = { showShareOptionsDialog = true },
                        onStartNewChallengeClick = {
                            if (state.status == ChallengeResultStatus.FAIL) {
                                showGoalAdjustmentDialog = true
                            } else {
                                onStartNewChallengeClick()
                            }
                        },
                        onTakeABreakClick = onTakeABreakClick
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))
        }
    }

    if (showGoalAdjustmentDialog) {
        ChallengeGoalAdjustmentDialog(
            currentDailyLimit = state.dailyLimit,
            onDismissRequest = { showGoalAdjustmentDialog = false },
            onStartNewChallengeClick = {
                showGoalAdjustmentDialog = false
                onStartNewChallengeClick()
            }
        )
    }

    if (showShareOptionsDialog) {
        ShareOptionsDialog(
            onDismissRequest = { showShareOptionsDialog = false },
            onOptionSelected = { option ->
                showShareOptionsDialog = false
                coroutineScope.launch {
                    val bitmap = screenGraphicsLayer.toImageBitmap().asAndroidBitmap()
                    val savedUri = saveBitmapToGallery(context, bitmap)
                    if (savedUri == null) {
                        Toast.makeText(context, "이미지 저장에 실패했어요.", Toast.LENGTH_SHORT).show()
                    } else if (option == ShareOption.SAVE_IMAGE) {
                        Toast.makeText(context, "이미지를 저장했어요.", Toast.LENGTH_SHORT).show()
                    } else {
                        val targetPackage = option.packageName ?: defaultSmsPackage(context)
                        if (targetPackage != null) {
                            grantShareUriPermission(context, targetPackage, savedUri)
                        }
                        pendingShareUri = savedUri
                        try {
                            shareLauncher.launch(buildShareImageIntent(savedUri, targetPackage))
                        } catch (e: ActivityNotFoundException) {
                            Toast.makeText(context, "설치된 앱을 찾을 수 없어요.", Toast.LENGTH_SHORT).show()
                            pendingShareUri = null
                        }
                    }
                }
                onShareClick()
            }
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ChallengeResultTopBar(onBackClick: () -> Unit) {
    CenterAlignedTopAppBar(
        title = {
            Text("챌린지 결과", style = MaterialTheme.typography.titleSmall, color = HPBlack)
        },
        navigationIcon = {
            IconButton(onClick = onBackClick) {
                Icon(
                    Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = "뒤로가기",
                    tint = HPBlack
                )
            }
        },
        colors = TopAppBarDefaults.topAppBarColors(containerColor = HPWhite)
    )
}

@Composable
private fun ChallengeStatusHeroCard(state: ChallengeResultUiState) {
    val (badgeText, hamsterRes, hamsterDescription) = when (state.status) {
        ChallengeResultStatus.COMPLETE -> Triple(
            "CHALLENGE COMPLETE",
            R.drawable.img_hamster_success,
            "성공한 포치"
        )

        ChallengeResultStatus.FAIL -> Triple(
            "CHALLENGE FAIL",
            R.drawable.img_hamster_fail,
            "실패한 포치"
        )

        ChallengeResultStatus.IN_PROGRESS -> Triple(
            "CHALLENGE IN PROGRESS",
            R.drawable.img_hamster_normal,
            "진행중인 포치"
        )
    }
    val subtitle = when (state.status) {
        ChallengeResultStatus.COMPLETE -> "${state.totalDays}일 챌린지 완주했어요!"
        ChallengeResultStatus.FAIL -> "그래도 ${state.successDays}일은 해냈어요!"
        ChallengeResultStatus.IN_PROGRESS -> "${state.totalDays}일 챌린지 진행중이에요!"
    }

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(30.dp))
            .background(HPSub4)
    ) {
        Box(
            modifier = Modifier
                .size(170.dp)
                .align(Alignment.TopStart)
                .offset(x = (-60).dp, y = (-40).dp)
                .clip(CircleShape)
                .background(HPSub2.copy(alpha = 0.10f))
        )
        Box(
            modifier = Modifier
                .size(170.dp)
                .align(Alignment.BottomEnd)
                .offset(x = 50.dp, y = 40.dp)
                .clip(CircleShape)
                .background(HPSub2.copy(alpha = 0.10f))
        )

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 15.dp, vertical = 32.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                badgeText,
                style = MaterialTheme.typography.labelLarge,
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = HPSub1
            )
            Spacer(modifier = Modifier.height(30.dp))
            Image(
                painter = painterResource(hamsterRes),
                contentDescription = hamsterDescription,
                modifier = Modifier
                    .width(160.dp)
                    .height(174.dp)
            )
            Spacer(modifier = Modifier.height(15.dp))
            Text(subtitle, style = MaterialTheme.typography.titleSmall, color = HPBlack)
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                "${formatPeriodDate(state.periodStart)} ~ ${formatPeriodDate(state.periodEnd)}",
                style = MaterialTheme.typography.bodySmall,
                color = HPText
            )

            Spacer(modifier = Modifier.height(20.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                StatBox(
                    label = "성공한 날",
                    value = "${state.successDays}/${state.totalDays}",
                    highlighted = false,
                    modifier = Modifier.weight(1f)
                )
                StatBox(
                    label = state.amountLabel,
                    value = formatWon(state.amountValue),
                    highlighted = true,
                    modifier = Modifier.weight(1f)
                )
                StatBox(
                    label = "연속 달성",
                    value = "${state.streakDays}일",
                    highlighted = false,
                    modifier = Modifier.weight(1f)
                )
            }
        }
    }
}

@Preview(showBackground = true, name = "진행중")
@Composable
private fun ChallengeResultScreenInProgressPreview() {
    HampouchTheme {
        ChallengeResultScreen(state = ChallengeResultMockData.inProgress())
    }
}

@Preview(showBackground = true, name = "완료")
@Composable
private fun ChallengeResultScreenCompletePreview() {
    HampouchTheme {
        ChallengeResultScreen(state = ChallengeResultMockData.complete)
    }
}

@Preview(showBackground = true, name = "실패")
@Composable
private fun ChallengeResultScreenFailPreview() {
    HampouchTheme {
        ChallengeResultScreen(state = ChallengeResultMockData.fail)
    }
}
