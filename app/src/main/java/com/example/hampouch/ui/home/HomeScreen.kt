package com.example.hampouch.ui.home

import android.util.Log
import android.widget.Toast
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.Saver
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.hampouch.R
import com.example.hampouch.domain.model.ChallengeState
import com.example.hampouch.domain.model.ExpenseEntry
import com.example.hampouch.domain.model.ExpenseRecord
import com.example.hampouch.ui.home.HomeUiState
import com.example.hampouch.domain.model.toUserMessage
import com.example.hampouch.core.config.BattleConfig
import com.example.hampouch.core.config.ChallengeConfig
import com.example.hampouch.navigation.BottomNavBar
import com.example.hampouch.navigation.BottomNavItem
import com.example.hampouch.ui.common.previewChallengeState
import com.example.hampouch.ui.dialog.ChallengeEndedDialog
import com.example.hampouch.ui.dialog.MissingExpenseReminderDialog
import com.example.hampouch.ui.dialog.TakeABreakEndedDialog
import com.example.hampouch.ui.expensedetail.resolveReasonLabel
import com.example.hampouch.ui.hambattle.HamBattleScreen
import com.example.hampouch.ui.hambattle.HamBattleViewModel
import com.example.hampouch.ui.hamtips.HamTipsScreen
import com.example.hampouch.ui.home.components.ChallengeBanner
import com.example.hampouch.ui.home.components.CharacterGaugeSection
import com.example.hampouch.ui.home.components.DateSelectorRow
import com.example.hampouch.ui.home.components.HomeHeader
import com.example.hampouch.ui.home.components.MiniChallengeSection
import com.example.hampouch.ui.home.components.NoActiveChallengeSection
import com.example.hampouch.ui.home.components.ReturnToTodayButton
import com.example.hampouch.ui.home.components.SavingsStreakRow
import com.example.hampouch.ui.home.components.TodayExpenseSection
import com.example.hampouch.ui.minichallenge.MiniChallengeEvent
import com.example.hampouch.ui.minichallenge.MiniChallengeViewModel
import com.example.hampouch.ui.mypage.MyPageScreen
import com.example.hampouch.domain.model.HamBattleStatus
import com.example.hampouch.domain.model.isMissingReminderDue
import com.example.hampouch.ui.mypage.RecordAlarmViewModel
import com.example.hampouch.ui.theme.HPGray2
import com.example.hampouch.ui.theme.HPSub3
import com.example.hampouch.ui.theme.HPSub4
import com.example.hampouch.ui.theme.HampouchTheme
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.LocalTime
import java.util.UUID
import kotlinx.coroutines.launch

private const val MOCK_USER_NAME = "민준"
private const val MAX_HOME_EXPENSE_ITEMS = 4
private const val REMINDER_TIME_CHECK_INTERVAL_MILLIS = 30_000L

internal fun recentHomeExpenses(expenses: List<ExpenseEntry>): List<ExpenseEntry> =
    expenses.take(MAX_HOME_EXPENSE_ITEMS)

private val LocalDateSaver: Saver<LocalDate, Long> = Saver(
    save = { it.toEpochDay() },
    restore = { LocalDate.ofEpochDay(it) }
)

@Composable
@Suppress("LongParameterList", "LongMethod", "CyclomaticComplexMethod")
fun HomeScreen(
    modifier: Modifier = Modifier,
    initialBottomTab: BottomNavItem = BottomNavItem.HOME,
    openHamTipsWriteBattleOnStart: Boolean = false,
    initialHamTipsWriteBattleLink: String = "",
    onExitHamTipsWriteBattle: () -> Unit,
    initialMyTipDetailPostId: String? = null,
    initialMyTipDetailScrollToComments: Boolean = false,
    initialPopularPostId: String? = null,
    onStartChallengeClick: () -> Unit,
    onCalendarClick: () -> Unit,
    onChallengeSummaryClick: (String) -> Unit,
    onNavigateToMiniChallenge: (LocalDate) -> Unit,
    onHamBattleStartNewChallengeClick: () -> Unit,
    onHamBattleChallengeClick: (String) -> Unit,
    onHamBattleViewEndedChallengesClick: () -> Unit,
    onHamBattleViewEndedChallengeDetailClick: (String) -> Unit,
    onHamBattleWaitingChallengeClick: (String) -> Unit,
    onHamBattleJoinedFromCommunityClick: (String) -> Unit,
    onHamBattleJoinedFullFromCommunityClick: () -> Unit,
    onNavigateToExpenseDetail: (String) -> Unit,
    onNavigateToExpenseCalendar: () -> Unit,
    onNavigateToChallengeEndExpenseCalendar: () -> Unit,
    onNavigateToChallengeExpenseAnalysis: (totalDays: Int, periodStart: LocalDate, periodEnd: LocalDate) -> Unit,
    onNavigateToTakeABreak: () -> Unit,
    onExtendBreak: () -> Unit,
    onAddExpenseClick: () -> Unit,
    onNavigateToAmountAdjustment: () -> Unit,
    onNavigateToYesterdayExpenseInput: () -> Unit,
    onLoggedOut: (isWithdrawal: Boolean) -> Unit,
    onChallengeEndedFinishClick: () -> Unit,
    onFixedDateChallengeDue: () -> Unit,
    viewModel: HomeViewModel = hiltViewModel()
) {
    val referenceToday = remember { LocalDate.now() }
    var currentTime by remember { mutableStateOf(LocalTime.now()) }
    LaunchedEffect(Unit) {
        while (true) {
            delay(REMINDER_TIME_CHECK_INTERVAL_MILLIS)
            currentTime = LocalTime.now()
        }
    }
    val coroutineScope = rememberCoroutineScope()
    val restState by viewModel.restState.collectAsStateWithLifecycle()
    var selectedBottomTab by rememberSaveable { mutableStateOf(initialBottomTab) }
    val homeContext = LocalContext.current
    LaunchedEffect(viewModel) {
        viewModel.events.collect { event ->
            when (event) {
                HomeEvent.ResumedFromBreak -> onStartChallengeClick()
                HomeEvent.ChallengeEndAcknowledged -> onChallengeEndedFinishClick()
                HomeEvent.FixedDateChallengeDue -> onFixedDateChallengeDue()
                is HomeEvent.ShowMessage ->
                    Toast.makeText(homeContext, event.message, Toast.LENGTH_SHORT).show()
            }
        }
    }
    var pendingOpenHamTipsWriteBattle by remember { mutableStateOf(openHamTipsWriteBattleOnStart) }
    var pendingMyTipDetailPostId by remember { mutableStateOf(initialMyTipDetailPostId) }
    var pendingPopularPostId by remember { mutableStateOf(initialPopularPostId) }
    var selectedDate by rememberSaveable(stateSaver = LocalDateSaver) { mutableStateOf(referenceToday) }
    val context = LocalContext.current
    val miniChallengeViewModel: MiniChallengeViewModel = hiltViewModel()
    val miniChallengeState by miniChallengeViewModel.state.collectAsStateWithLifecycle()
    val battleViewModel: HamBattleViewModel = hiltViewModel()
    val recordAlarmViewModel: RecordAlarmViewModel = hiltViewModel()
    val recordAlarmState by recordAlarmViewModel.state.collectAsStateWithLifecycle()
    val reminderDismissedDate by recordAlarmViewModel.dismissedDate.collectAsStateWithLifecycle()
    val todayRecordsLoaded by viewModel.todayRecordsLoaded.collectAsStateWithLifecycle()
    val battleState by battleViewModel.state.collectAsStateWithLifecycle()
    LaunchedEffect(selectedDate) { miniChallengeViewModel.loadChallenges(selectedDate) }
    LaunchedEffect(miniChallengeViewModel) {
        miniChallengeViewModel.events.collect { event ->
            if (event is MiniChallengeEvent.ShowMessage) {
                Toast.makeText(context, event.message, Toast.LENGTH_SHORT).show()
            }
        }
    }
    LaunchedEffect(selectedDate) { viewModel.loadDay(selectedDate) }
    LaunchedEffect(selectedBottomTab) {
        if (selectedBottomTab == BottomNavItem.HAM_BATTLE) battleViewModel.loadMyBattles()
    }
    val records by viewModel.records.collectAsStateWithLifecycle()
    val daysWithRecord by viewModel.daysWithRecord.collectAsStateWithLifecycle()
    val challengeState by viewModel.challengeState.collectAsStateWithLifecycle()
    val currentUser by viewModel.currentUser.collectAsStateWithLifecycle()
    val recordsForDate: (LocalDate) -> List<ExpenseRecord> = { date ->
        records.values.filter { it.date == date }
    }
    val baseUiState = remember(selectedDate, challengeState, currentUser) {
        mockStateForDate(challengeState, currentUser.name, selectedDate, referenceToday)
    }
    val storeExpenses = recordsForDate(selectedDate).asReversed().map { record ->
        ExpenseEntry(
            id = record.id,
            categoryId = record.categoryId,
            customCategoryName = record.customCategoryName,
            name = record.expenseName,
            reasonTag = resolveReasonLabel(record.reasonId, record.customReason),
            amount = record.amount
        )
    }
    val uiState = baseUiState.copy(expenses = storeExpenses)
    val resolvedChallenge = challengeState.challengeFor(selectedDate)
    val isChallengeOverByToday = resolvedChallenge != null &&
        referenceToday.isAfter(resolvedChallenge.effectivePeriodEnd)
    val liveChallenge = if (isChallengeOverByToday) null else uiState.challenge?.let { challenge ->
        val liveDailyLimit = resolvedChallenge?.dailyLimitOn(selectedDate) ?: challenge.dailyLimit
        val todaySpent = uiState.expenses.sumOf { it.amount }
        val localProgress = resolvedChallenge
            ?.takeUnless { ChallengeConfig.USE_SERVER_CHALLENGE }
            ?.let { rc ->
                challengeState.computeProgress(
                    referenceToday,
                    rc,
                    hasRecordOnDate = { date -> recordsForDate(date).isNotEmpty() || date in daysWithRecord }
                ) { date -> recordsForDate(date).sumOf { it.amount } }
            }
        val challengeWithTodayBalance = challenge.copy(
            dailyLimit = liveDailyLimit,
            todayBalance = liveDailyLimit - todaySpent
        )
        resolveHomeChallengeProgress(
            challenge = challengeWithTodayBalance,
            localProgress = localProgress,
            useServerChallenge = ChallengeConfig.USE_SERVER_CHALLENGE
        )
    }
    val displayedUiState = uiState.copy(
        challenge = liveChallenge,
        miniChallenges = miniChallengeState.challengesFor(selectedDate),
        pastChallengeEnded = isChallengeOverByToday
    )

    Scaffold(
        modifier = modifier,
        containerColor = HPGray2,
        bottomBar = {
            if (selectedBottomTab != BottomNavItem.MY_PAGE && selectedBottomTab != BottomNavItem.COMMUNITY && selectedBottomTab != BottomNavItem.HAM_BATTLE) {
                BottomNavBar(
                    selectedItem = selectedBottomTab,
                    onItemSelected = { selectedBottomTab = it },
                    onAddClick = onAddExpenseClick,
                    modifier = Modifier.navigationBarsPadding()
                )
            }
        }
    ) { innerPadding ->
        when (selectedBottomTab) {
            BottomNavItem.HOME -> {
                HomeContent(
                    uiState = displayedUiState,
                    referenceToday = referenceToday,
                    onDateSelected = { date -> if (!date.isAfter(referenceToday)) selectedDate = date },
                    onReturnToTodayClick = { selectedDate = referenceToday },
                    onToggleMiniChallenge = { id ->
                        miniChallengeState.challengesFor(selectedDate).find { it.id == id }?.let { target ->
                            miniChallengeViewModel.setChecked(selectedDate, id, !target.isChecked)
                        }
                    },
                    onViewAllMiniChallengesClick = { onNavigateToMiniChallenge(selectedDate) },
                    onReminderClick = onNavigateToYesterdayExpenseInput,
                    onStartChallengeClick = onStartChallengeClick,
                    onCalendarClick = onCalendarClick,
                    onChallengeSummaryClick = { resolvedChallenge?.let { onChallengeSummaryClick(it.id) } },
                    onExpenseClick = onNavigateToExpenseDetail,
                    onViewAllExpensesClick = onNavigateToExpenseCalendar,
                    onAddExpenseClick = onAddExpenseClick,
                    modifier = Modifier.padding(innerPadding)
                )

                val hasExpenseToday = recordsForDate(referenceToday).isNotEmpty() ||
                    referenceToday in daysWithRecord
                when {
                    restState.isBreakOver(referenceToday) -> TakeABreakEndedDialog(
                        onStartNowClick = viewModel::resumeNow,
                        onStartTomorrowClick = viewModel::postponeOneDay,
                        onRestMoreClick = onExtendBreak
                    )

                    challengeState.isChallengeJustEnded(referenceToday) -> ChallengeEndedDialog(
                        totalDays = challengeState.activeChallenge!!.totalDays,
                        hasVisitedExpenseEdit = challengeState.hasVisitedExpenseEditAfterEnd,
                        onEditExpenseClick = onNavigateToChallengeEndExpenseCalendar,
                        onFinishChallengeClick = viewModel::acknowledgeChallengeEnd
                    )

                    // Gate on todayRecordsLoaded so this isn't evaluated before today's records
                    // have finished loading (otherwise hasExpenseToday is briefly a stale false on
                    // app restart, flashing the reminder even when today is already logged).
                    todayRecordsLoaded && recordAlarmState.isMissingReminderDue(
                        dismissedDate = reminderDismissedDate,
                        referenceToday = referenceToday,
                        currentTime = currentTime,
                        hasExpenseToday = hasExpenseToday
                    ) ->
                        MissingExpenseReminderDialog(
                            onInputNowClick = {
                                recordAlarmViewModel.dismissForToday(referenceToday)
                                onAddExpenseClick()
                            },
                            onNoSpendingTodayClick = {
                                viewModel.markNoSpending(referenceToday)
                                recordAlarmViewModel.dismissForToday(referenceToday)
                            },
                            onLaterClick = {
                                recordAlarmViewModel.dismissForToday(referenceToday)
                                viewModel.markNoRecord(referenceToday)
                            }
                        )
                }
            }

            BottomNavItem.HAM_BATTLE -> HamBattleScreen(
                selectedBottomTab = selectedBottomTab,
                onItemSelected = { selectedBottomTab = it },
                onAddClick = onAddExpenseClick,
                modifier = Modifier.padding(innerPadding),
                activeChallenges = if (BattleConfig.USE_SERVER_BATTLE) {
                    battleState.ongoingBattles
                } else {
                    battleViewModel.mockChallengesWith(HamBattleStatus.ACTIVE)
                },
                waitingChallenges = if (BattleConfig.USE_SERVER_BATTLE) {
                    battleState.readyBattles
                } else {
                    battleViewModel.mockChallengesWith(HamBattleStatus.WAITING)
                },
                onStartNewChallengeClick = onHamBattleStartNewChallengeClick,
                onChallengeClick = onHamBattleChallengeClick,
                onViewEndedChallengesClick = onHamBattleViewEndedChallengesClick,
                onWaitingChallengeClick = onHamBattleWaitingChallengeClick,
                onViewEndedChallengeDetailClick = onHamBattleViewEndedChallengeDetailClick,
            )

            BottomNavItem.MY_PAGE -> {
                MyPageScreen(
                    selectedBottomTab = selectedBottomTab,
                    onItemSelected = { selectedBottomTab = it },
                    onAddClick = onAddExpenseClick,
                    modifier = Modifier.padding(innerPadding),
                    onNavigateToHamBattleLink = onHamBattleWaitingChallengeClick,
                    onLoggedOut = onLoggedOut,
                    onNavigateToChallengeExpenseAnalysis = onNavigateToChallengeExpenseAnalysis,
                    onNavigateToAmountAdjustment = onNavigateToAmountAdjustment,
                    onNavigateToTakeABreak = onNavigateToTakeABreak,
                    onStartNewChallengeClick = { onStartChallengeClick() },
                    initialTipDetailPostId = pendingMyTipDetailPostId,
                    initialTipDetailScrollToComments = initialMyTipDetailScrollToComments
                )
                LaunchedEffect(Unit) { pendingMyTipDetailPostId = null }
            }

            BottomNavItem.COMMUNITY -> {
                HamTipsScreen(
                    selectedBottomTab = selectedBottomTab,
                    onItemSelected = { selectedBottomTab = it },
                    onAddClick = onAddExpenseClick,
                    modifier = Modifier.padding(innerPadding),
                    onNavigateToHamBattleLink = onHamBattleJoinedFromCommunityClick,
                    onNavigateToHamBattleTab = onHamBattleJoinedFullFromCommunityClick,
                    openWriteBattleOnStart = pendingOpenHamTipsWriteBattle,
                    initialWriteBattleLink = initialHamTipsWriteBattleLink,
                    onExitWriteBattle = onExitHamTipsWriteBattle,
                    initialPopularPostId = pendingPopularPostId
                )
                LaunchedEffect(Unit) {
                    pendingOpenHamTipsWriteBattle = false
                    pendingPopularPostId = null
                }
            }
        }
    }
}

private fun mockStateForDate(
    challengeState: ChallengeState,
    userName: String,
    date: LocalDate,
    referenceToday: LocalDate
): HomeUiState = HomeMockData.freshDayState(challengeState, userName, date)

@Composable
private fun HomeContent(
    uiState: HomeUiState,
    referenceToday: LocalDate,
    onDateSelected: (LocalDate) -> Unit,
    onReturnToTodayClick: () -> Unit,
    onToggleMiniChallenge: (String) -> Unit,
    onViewAllMiniChallengesClick: () -> Unit,
    onReminderClick: () -> Unit,
    onStartChallengeClick: () -> Unit,
    onCalendarClick: () -> Unit,
    onChallengeSummaryClick: () -> Unit,
    onExpenseClick: (String) -> Unit,
    onViewAllExpensesClick: () -> Unit,
    onAddExpenseClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 20.dp)
    ) {
        HomeHeader(
            userName = uiState.userName,
            onCalendarClick = onCalendarClick
        )
        Spacer(modifier = Modifier.height(5.dp))
        DateSelectorRow(
            referenceToday = referenceToday,
            selectedDate = uiState.selectedDate,
            onDateSelected = onDateSelected
        )
        val isToday = uiState.selectedDate == referenceToday
        if (!isToday) {
            Spacer(modifier = Modifier.height(15.dp))
            ReturnToTodayButton(onClick = onReturnToTodayClick)
        }
        Spacer(modifier = Modifier.height(10.dp))

        AnimatedContent(
            targetState = uiState,
            contentKey = { it.selectedDate },
            transitionSpec = {
                val movingForward = targetState.selectedDate.isAfter(initialState.selectedDate)
                val slideSpec = tween<IntOffset>(durationMillis = 300)
                val fadeSpec = tween<Float>(durationMillis = 300)
                ((slideInHorizontally(animationSpec = slideSpec) { fullWidth ->
                    if (movingForward) fullWidth / 4 else -fullWidth / 4
                } + fadeIn(animationSpec = fadeSpec)) togetherWith
                    (slideOutHorizontally(animationSpec = slideSpec) { fullWidth ->
                        if (movingForward) -fullWidth / 4 else fullWidth / 4
                    } + fadeOut(animationSpec = fadeSpec))).using(null)
            },
            label = "home_date_content"
        ) { animatedUiState ->
            Column {
                val challenge = animatedUiState.challenge
                when {
                    challenge == null && animatedUiState.pastChallengeEnded -> NoActiveChallengeSection(
                        title = stringResource(R.string.home_ended_challenge_title),
                        subtitle = stringResource(R.string.home_ended_challenge_subtitle),
                        ctaText = stringResource(R.string.home_ended_challenge_cta),
                        onCtaClick = onChallengeSummaryClick
                    )
                    challenge == null && animatedUiState.selectedDate != referenceToday -> NoActiveChallengeSection(
                        title = stringResource(R.string.home_no_past_challenge_title),
                        subtitle = stringResource(R.string.home_no_challenge_subtitle)
                    )
                    challenge == null -> NoActiveChallengeSection(
                        title = stringResource(R.string.home_no_challenge_title),
                        subtitle = stringResource(R.string.home_no_challenge_subtitle),
                        ctaText = stringResource(R.string.home_no_challenge_cta),
                        onCtaClick = onStartChallengeClick
                    )
                    else -> {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(20.dp))
                                .background(HPSub3)
                                .clickable(onClick = onChallengeSummaryClick)
                                .padding(horizontal = 15.dp, vertical = 13.dp)
                        ) {
                            ChallengeBanner(challenge = challenge)
                            Spacer(modifier = Modifier.height(10.dp))
                            CharacterGaugeSection(challenge = challenge)
                            Spacer(modifier = Modifier.height(12.dp))
                            SavingsStreakRow(savedAmount = challenge.savedAmount, streakDays = challenge.streakDays)
                        }
                    }
                }

                Spacer(modifier = Modifier.height(15.dp))
                TodayExpenseSection(
                    title = stringResource(
                        homeExpenseTitleRes(animatedUiState.selectedDate, referenceToday)
                    ),
                    expenses = recentHomeExpenses(animatedUiState.expenses),
                    onViewAllClick = onViewAllExpensesClick,
                    onAddExpenseClick = onAddExpenseClick,
                    onExpenseClick = onExpenseClick
                )
                Spacer(modifier = Modifier.height(15.dp))
                MiniChallengeSection(
                    items = animatedUiState.miniChallenges,
                    onViewAllClick = onViewAllMiniChallengesClick,
                    onToggle = onToggleMiniChallenge
                )
            }
        }
        Spacer(modifier = Modifier.height(15.dp))
    }
}

@Composable
private fun HomeScreenPreviewScaffold(state: HomeUiState, referenceToday: LocalDate) {
    Scaffold(
        containerColor = HPGray2,
        bottomBar = {
            BottomNavBar(selectedItem = BottomNavItem.HOME, onItemSelected = {}, onAddClick = {})
        }
    ) { innerPadding ->
        HomeContent(
            uiState = state,
            referenceToday = referenceToday,
            onDateSelected = {},
            onReturnToTodayClick = {},
            onToggleMiniChallenge = {},
            onReminderClick = {},
            onStartChallengeClick = {},
            onViewAllMiniChallengesClick = {},
            onCalendarClick = {},
            onChallengeSummaryClick = {},
            onExpenseClick = {},
            onViewAllExpensesClick = {},
            onAddExpenseClick = {},
            modifier = Modifier.padding(innerPadding)
        )
    }
}

@Preview(showBackground = true, name = "1. 챌린지 시작 - 통통(100%)")
@Composable
private fun HomeScreenChubbyPreview() {
    val today = remember { LocalDate.now() }
    HampouchTheme { HomeScreenPreviewScaffold(HomeMockData.freshDayState(previewChallengeState(today), MOCK_USER_NAME, today), today) }
}

@Preview(showBackground = true, name = "2. 지출 반영 예시 - 통통(86%)")
@Composable
private fun HomeScreenDecreasingPreview() {
    val today = remember { LocalDate.now() }
    HampouchTheme { HomeScreenPreviewScaffold(HomeMockData.decreasingBalanceState(previewChallengeState(today), MOCK_USER_NAME, today), today) }
}

@Preview(showBackground = true, name = "3. 보통(36%)")
@Composable
private fun HomeScreenNormalPreview() {
    val today = remember { LocalDate.now() }
    HampouchTheme { HomeScreenPreviewScaffold(HomeMockData.normalBalanceState(previewChallengeState(today), MOCK_USER_NAME, today), today) }
}

@Preview(showBackground = true, name = "4-5. 홀쭉(15%) + 경고 배너")
@Composable
private fun HomeScreenWarningPreview() {
    val today = remember { LocalDate.now() }
    HampouchTheme { HomeScreenPreviewScaffold(HomeMockData.lowBalanceWithWarningState(previewChallengeState(today), MOCK_USER_NAME, today), today) }
}

@Preview(showBackground = true, name = "6. 진행 중인 챌린지 없음")
@Composable
private fun HomeScreenNoChallengePreview() {
    val today = remember { LocalDate.now() }
    HampouchTheme { HomeScreenPreviewScaffold(HomeMockData.noActiveChallengeState(MOCK_USER_NAME, today), today) }
}
