package com.example.hampouch.ui.home

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.hampouch.data.model.HomeUiState
import com.example.hampouch.navigation.BottomNavBar
import com.example.hampouch.navigation.BottomNavItem
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
import com.example.hampouch.ui.theme.HPGray2
import com.example.hampouch.ui.theme.HPSub4
import com.example.hampouch.ui.theme.HampouchTheme
import java.time.LocalDate

private const val MOCK_USER_NAME = "민준"

@Composable
fun HomeScreen(
    modifier: Modifier = Modifier,
    onStartChallengeClick: () -> Unit = {},
    onNavigateToMiniChallenge: (LocalDate) -> Unit = {},
    onHamBattleStartNewChallengeClick: () -> Unit = {},
    onHamBattleChallengeClick: (String) -> Unit = {},
    onHamBattleViewEndedChallengesClick: () -> Unit = {},
    onHamBattleWaitingChallengeClick: (String) -> Unit = {},
    onLogout: () -> Unit = {}
) {
    val referenceToday = remember { LocalDate.now() }
    var selectedBottomTab by rememberSaveable { mutableStateOf(BottomNavItem.HOME) }
    var selectedDate by remember { mutableStateOf(referenceToday) }
    val uiState = remember(selectedDate) { mockStateForDate(selectedDate, referenceToday) }
    val displayedUiState = uiState.copy(miniChallenges = MiniChallengeStore.challengesFor(selectedDate))

    Scaffold(
        modifier = modifier,
        containerColor = HPGray2,
        bottomBar = {
            if (selectedBottomTab != BottomNavItem.MY_PAGE && selectedBottomTab != BottomNavItem.COMMUNITY) {
                BottomNavBar(
                    selectedItem = selectedBottomTab,
                    onItemSelected = { selectedBottomTab = it },
                    onAddClick = {}
                )
            }
        }
    ) { innerPadding ->
        when (selectedBottomTab) {
            BottomNavItem.HOME -> HomeContent(
                uiState = displayedUiState,
                referenceToday = referenceToday,
                onDateSelected = { date -> if (!date.isAfter(referenceToday)) selectedDate = date },
                onToggleMiniChallenge = { id -> MiniChallengeStore.toggle(selectedDate, id) },
                onViewAllMiniChallengesClick = { onNavigateToMiniChallenge(selectedDate) },
                onSuggestionClick = {},
                onStartChallengeClick = onStartChallengeClick,
                modifier = Modifier.padding(innerPadding)
            )

            BottomNavItem.HAM_BATTLE -> Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(bottom = innerPadding.calculateBottomPadding())
            ) {
                HamBattleScreen(
                    onStartNewChallengeClick = onHamBattleStartNewChallengeClick,
                    onChallengeClick = onHamBattleChallengeClick,
                    onViewEndedChallengesClick = onHamBattleViewEndedChallengesClick,
                    onWaitingChallengeClick = onHamBattleWaitingChallengeClick
                )
            }

            BottomNavItem.MY_PAGE -> MyPageScreen(
                selectedBottomTab = selectedBottomTab,
                onItemSelected = { selectedBottomTab = it },
                onAddClick = {},
                modifier = Modifier.padding(innerPadding),
                onNavigateToHamBattleLink = onHamBattleWaitingChallengeClick
            )

            BottomNavItem.COMMUNITY -> HamTipsScreen(
                selectedBottomTab = selectedBottomTab,
                onItemSelected = { selectedBottomTab = it },
                onAddClick = {},
                modifier = Modifier.padding(innerPadding),
                onNavigateToHamBattleLink = onHamBattleWaitingChallengeClick
            )
        }
    }
}

private fun mockStateForDate(date: LocalDate, referenceToday: LocalDate): HomeUiState = when (date) {
    referenceToday.minusDays(1) -> HomeMockData.lowBalanceWithWarningState(MOCK_USER_NAME, date)
    else -> HomeMockData.freshDayState(MOCK_USER_NAME, date)
}

@Composable
private fun HomeContent(
    uiState: HomeUiState,
    referenceToday: LocalDate,
    onDateSelected: (LocalDate) -> Unit,
    onToggleMiniChallenge: (String) -> Unit,
    onViewAllMiniChallengesClick: () -> Unit = {},
    onSuggestionClick: (String) -> Unit,
    onStartChallengeClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 20.dp)
    ) {
        Spacer(modifier = Modifier.height(12.dp))
        HomeHeader(
            userName = uiState.userName,
            hasUnreadNotification = true,
            onCalendarClick = {},
            onNotificationClick = {}
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
            onViewAllClick = {},
            onAddExpenseClick = {}
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
