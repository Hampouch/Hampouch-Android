package com.example.hampouch.ui.home

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
import androidx.compose.runtime.saveable.Saver
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.hampouch.data.model.ExpenseEntry
import com.example.hampouch.data.model.HomeUiState
import com.example.hampouch.data.repository.ChallengeRepository
import com.example.hampouch.navigation.BottomNavBar
import com.example.hampouch.navigation.BottomNavItem
import com.example.hampouch.ui.dialog.ChallengeEndedDialog
import com.example.hampouch.ui.dialog.MissingExpenseReminderDialog
import com.example.hampouch.ui.dialog.TakeABreakEndedDialog
import com.example.hampouch.ui.expensedetail.ExpenseDetailStore
import com.example.hampouch.ui.expensedetail.resolveReasonLabel
import com.example.hampouch.ui.hambattle.HamBattleScreen
import com.example.hampouch.ui.hamtips.HamTipsScreen
import com.example.hampouch.ui.home.components.ChallengeBanner
import com.example.hampouch.ui.home.components.CharacterGaugeSection
import com.example.hampouch.ui.home.components.DateSelectorRow
import com.example.hampouch.ui.home.components.HomeHeader
import com.example.hampouch.ui.home.components.MiniChallengeSection
import com.example.hampouch.ui.home.components.NoActiveChallengeSection
import com.example.hampouch.ui.home.components.SavingsStreakRow
import com.example.hampouch.ui.home.components.TodayExpenseSection
import com.example.hampouch.ui.home.components.WarningBannerList
import com.example.hampouch.ui.minichallenge.MiniChallengeStore
import com.example.hampouch.ui.mypage.MyPageScreen
import com.example.hampouch.ui.mypage.RecordAlarmStore
import com.example.hampouch.ui.session.UserSession
import com.example.hampouch.ui.takeabreak.TakeABreakStore
import com.example.hampouch.ui.theme.HPGray2
import com.example.hampouch.ui.theme.HPSub4
import com.example.hampouch.ui.theme.HampouchTheme
import java.time.LocalDate
import java.time.LocalTime

private const val MOCK_USER_NAME = "민준"

private val LocalDateSaver: Saver<LocalDate, Long> = Saver(
    save = { it.toEpochDay() },
    restore = { LocalDate.ofEpochDay(it) }
)

@Composable
fun HomeScreen(
    modifier: Modifier = Modifier,
    initialBottomTab: BottomNavItem = BottomNavItem.HOME,
    openHamTipsWriteBattleOnStart: Boolean = false,
    initialHamTipsWriteBattleLink: String = "",
    onExitHamTipsWriteBattle: () -> Unit = {},
    onStartChallengeClick: () -> Unit = {},
    onCalendarClick: () -> Unit = {},
    onChallengeSummaryClick: (String) -> Unit = {},
    onNavigateToMiniChallenge: (LocalDate) -> Unit = {},
    onHamBattleStartNewChallengeClick: () -> Unit = {},
    onHamBattleChallengeClick: (String) -> Unit = {},
    onHamBattleViewEndedChallengesClick: () -> Unit = {},
    onHamBattleViewEndedChallengeDetailClick: (String) -> Unit = {},
    onHamBattleWaitingChallengeClick: (String) -> Unit = {},
    onNavigateToExpenseDetail: (String) -> Unit = {},
    onNavigateToExpenseCalendar: () -> Unit = {},
    onNavigateToChallengeEndExpenseCalendar: () -> Unit = {},
    onNavigateToChallengeExpenseAnalysis: (totalDays: Int, periodStart: LocalDate, periodEnd: LocalDate) -> Unit = { _, _, _ -> },
    onNavigateToTakeABreak: () -> Unit = {},
    onAddExpenseClick: () -> Unit = {},
    onNavigateToAmountAdjustment: () -> Unit = {},
    onNotificationClick: () -> Unit = {},
    onLoggedOut: () -> Unit = {}
) {
    val referenceToday = remember { LocalDate.now() }
    var selectedBottomTab by rememberSaveable { mutableStateOf(initialBottomTab) }
    var pendingOpenHamTipsWriteBattle by remember { mutableStateOf(openHamTipsWriteBattleOnStart) }
    var selectedDate by rememberSaveable(stateSaver = LocalDateSaver) { mutableStateOf(referenceToday) }
    val baseUiState = remember(selectedDate) { mockStateForDate(selectedDate, referenceToday) }
    val storeExpenses = ExpenseDetailStore.recordsForDate(selectedDate).map { record ->
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
    val resolvedChallenge = ChallengeRepository.challengeFor(selectedDate)
    val liveChallenge = uiState.challenge?.let { challenge ->
        val todaySpent = uiState.expenses.sumOf { it.amount }
        val progress = resolvedChallenge?.let { rc ->
            ChallengeRepository.computeProgress(referenceToday, rc) { date ->
                ExpenseDetailStore.recordsForDate(date).sumOf { it.amount }
            }
        }
        challenge.copy(
            todayBalance = challenge.dailyLimit - todaySpent,
            savedAmount = progress?.savedAmount ?: challenge.savedAmount,
            streakDays = progress?.streakDays ?: challenge.streakDays
        )
    }
    val displayedUiState = uiState.copy(
        challenge = liveChallenge,
        miniChallenges = MiniChallengeStore.challengesFor(selectedDate)
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
                    onToggleMiniChallenge = { id -> MiniChallengeStore.toggle(selectedDate, id) },
                    onViewAllMiniChallengesClick = { onNavigateToMiniChallenge(selectedDate) },
                    onSuggestionClick = { onNavigateToAmountAdjustment() },
                    onStartChallengeClick = onStartChallengeClick,
                    onCalendarClick = onCalendarClick,
                    onChallengeSummaryClick = { resolvedChallenge?.let { onChallengeSummaryClick(it.id) } },
                    onExpenseClick = onNavigateToExpenseDetail,
                    onViewAllExpensesClick = onNavigateToExpenseCalendar,
                    onAddExpenseClick = onAddExpenseClick,
                    onNotificationClick = onNotificationClick,
                    modifier = Modifier.padding(innerPadding)
                )

                val hasExpenseToday = ExpenseDetailStore.recordsForDate(referenceToday).isNotEmpty()
                when {
                    TakeABreakStore.isBreakOver(referenceToday) -> TakeABreakEndedDialog(
                        onStartNowClick = {
                            TakeABreakStore.endBreakNow()
                            onStartChallengeClick()
                        },
                        onStartTomorrowClick = { TakeABreakStore.postponeOneDay() },
                        onRestMoreClick = onNavigateToTakeABreak
                    )

                    ChallengeRepository.isChallengeJustEnded(referenceToday) -> ChallengeEndedDialog(
                        totalDays = ChallengeRepository.activeChallenge.totalDays,
                        hasVisitedExpenseEdit = ChallengeRepository.hasVisitedExpenseEditAfterEnd,
                        onEditExpenseClick = onNavigateToChallengeEndExpenseCalendar,
                        onFinishChallengeClick = {
                            ChallengeRepository.acknowledgeChallengeEnd()
                            onStartChallengeClick()
                        }
                    )

                    RecordAlarmStore.isMissingReminderDue(referenceToday, LocalTime.now(), hasExpenseToday) ->
                        MissingExpenseReminderDialog(
                            onInputNowClick = {
                                RecordAlarmStore.dismissForToday(referenceToday)
                                onAddExpenseClick()
                            },
                            onNoSpendingTodayClick = { RecordAlarmStore.dismissForToday(referenceToday) },
                            onLaterClick = { RecordAlarmStore.dismissForToday(referenceToday) }
                        )
                }
            }

            BottomNavItem.HAM_BATTLE -> HamBattleScreen(
                selectedBottomTab = selectedBottomTab,
                onItemSelected = { selectedBottomTab = it },
                onAddClick = onAddExpenseClick,
                modifier = Modifier.padding(innerPadding),
                onStartNewChallengeClick = onHamBattleStartNewChallengeClick,
                onNotificationClick = onNotificationClick,
                onChallengeClick = onHamBattleChallengeClick,
                onViewEndedChallengesClick = onHamBattleViewEndedChallengesClick,
                onWaitingChallengeClick = onHamBattleWaitingChallengeClick,
                onViewEndedChallengeDetailClick = onHamBattleViewEndedChallengeDetailClick,
            )

            BottomNavItem.MY_PAGE -> MyPageScreen(
                selectedBottomTab = selectedBottomTab,
                onItemSelected = { selectedBottomTab = it },
                onAddClick = onAddExpenseClick,
                modifier = Modifier.padding(innerPadding),
                onNavigateToHamBattleLink = onHamBattleWaitingChallengeClick,
                onNotificationClick = onNotificationClick,
                onLoggedOut = onLoggedOut,
                onNavigateToChallengeExpenseAnalysis = onNavigateToChallengeExpenseAnalysis,
                onNavigateToAmountAdjustment = onNavigateToAmountAdjustment,
                onNavigateToTakeABreak = onNavigateToTakeABreak,
                onStartNewChallengeClick = onStartChallengeClick
            )

            BottomNavItem.COMMUNITY -> {
                HamTipsScreen(
                    selectedBottomTab = selectedBottomTab,
                    onItemSelected = { selectedBottomTab = it },
                    onAddClick = onAddExpenseClick,
                    modifier = Modifier.padding(innerPadding),
                    onNavigateToHamBattleLink = onHamBattleWaitingChallengeClick,
                    onNotificationClick = onNotificationClick,
                    openWriteBattleOnStart = pendingOpenHamTipsWriteBattle,
                    initialWriteBattleLink = initialHamTipsWriteBattleLink,
                    onExitWriteBattle = onExitHamTipsWriteBattle
                )
                LaunchedEffect(Unit) { pendingOpenHamTipsWriteBattle = false }
            }
        }
    }
}

private fun mockStateForDate(date: LocalDate, referenceToday: LocalDate): HomeUiState =
    HomeMockData.freshDayState(UserSession.currentUser.name, date)

@Composable
private fun HomeContent(
    uiState: HomeUiState,
    referenceToday: LocalDate,
    onDateSelected: (LocalDate) -> Unit,
    onToggleMiniChallenge: (String) -> Unit,
    onViewAllMiniChallengesClick: () -> Unit = {},
    onSuggestionClick: (String) -> Unit,
    onStartChallengeClick: () -> Unit,
    onCalendarClick: () -> Unit = {},
    onChallengeSummaryClick: () -> Unit = {},
    onExpenseClick: (String) -> Unit = {},
    onViewAllExpensesClick: () -> Unit = {},
    onAddExpenseClick: () -> Unit = {},
    onNotificationClick: () -> Unit = {},
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
            onCalendarClick = onCalendarClick,
            onNotificationClick = onNotificationClick
        )
        Spacer(modifier = Modifier.height(16.dp))
        DateSelectorRow(
            referenceToday = referenceToday,
            selectedDate = uiState.selectedDate,
            onDateSelected = onDateSelected
        )
        Spacer(modifier = Modifier.height(20.dp))

        val challenge = uiState.challenge
        if (challenge == null) {
            NoActiveChallengeSection(onStartChallengeClick = onStartChallengeClick)
        } else {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(20.dp))
                    .background(HPSub4)
                    .clickable(onClick = onChallengeSummaryClick)
                    .padding(horizontal = 20.dp, vertical = 16.dp)
            ) {
                ChallengeBanner(challenge = challenge)
                Spacer(modifier = Modifier.height(16.dp))
                CharacterGaugeSection(challenge = challenge)
                Spacer(modifier = Modifier.height(16.dp))
                SavingsStreakRow(savedAmount = challenge.savedAmount, streakDays = challenge.streakDays)
            }

            if (uiState.warnings.isNotEmpty()) {
                Spacer(modifier = Modifier.height(16.dp))
                WarningBannerList(warnings = uiState.warnings, onSuggestionClick = onSuggestionClick)
            }
        }

        Spacer(modifier = Modifier.height(24.dp))
        TodayExpenseSection(
            expenses = uiState.expenses,
            onViewAllClick = onViewAllExpensesClick,
            onAddExpenseClick = onAddExpenseClick,
            onExpenseClick = onExpenseClick
        )
        Spacer(modifier = Modifier.height(24.dp))
        MiniChallengeSection(
            items = uiState.miniChallenges,
            onViewAllClick = onViewAllMiniChallengesClick,
            onToggle = onToggleMiniChallenge
        )
        Spacer(modifier = Modifier.height(24.dp))
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
            onToggleMiniChallenge = {},
            onSuggestionClick = {},
            onStartChallengeClick = {},
            modifier = Modifier.padding(innerPadding)
        )
    }
}

@Preview(showBackground = true, name = "1. 챌린지 시작 - 통통(100%)")
@Composable
private fun HomeScreenChubbyPreview() {
    val today = remember { LocalDate.now() }
    HampouchTheme { HomeScreenPreviewScaffold(HomeMockData.freshDayState(MOCK_USER_NAME, today), today) }
}

@Preview(showBackground = true, name = "2. 지출 반영 예시 - 통통(86%)")
@Composable
private fun HomeScreenDecreasingPreview() {
    val today = remember { LocalDate.now() }
    HampouchTheme { HomeScreenPreviewScaffold(HomeMockData.decreasingBalanceState(MOCK_USER_NAME, today), today) }
}

@Preview(showBackground = true, name = "3. 보통(36%)")
@Composable
private fun HomeScreenNormalPreview() {
    val today = remember { LocalDate.now() }
    HampouchTheme { HomeScreenPreviewScaffold(HomeMockData.normalBalanceState(MOCK_USER_NAME, today), today) }
}

@Preview(showBackground = true, name = "4-5. 홀쭉(15%) + 경고 배너")
@Composable
private fun HomeScreenWarningPreview() {
    val today = remember { LocalDate.now() }
    HampouchTheme { HomeScreenPreviewScaffold(HomeMockData.lowBalanceWithWarningState(MOCK_USER_NAME, today), today) }
}

@Preview(showBackground = true, name = "6. 진행 중인 챌린지 없음")
@Composable
private fun HomeScreenNoChallengePreview() {
    val today = remember { LocalDate.now() }
    HampouchTheme { HomeScreenPreviewScaffold(HomeMockData.noActiveChallengeState(MOCK_USER_NAME, today), today) }
}
