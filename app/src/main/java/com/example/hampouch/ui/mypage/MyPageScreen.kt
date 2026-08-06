package com.example.hampouch.ui.mypage

import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.exclude
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Scaffold
import androidx.compose.material3.ScaffoldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.hampouch.R
import com.example.hampouch.data.model.MyPageProfile
import com.example.hampouch.data.model.TipPost
import com.example.hampouch.data.model.TipPostType
import com.example.hampouch.data.repository.ChallengeRepository
import com.example.hampouch.navigation.BottomNavBar
import com.example.hampouch.navigation.BottomNavItem
import com.example.hampouch.ui.challengeresult.ChallengeResultMockData
import com.example.hampouch.ui.challengeresult.ChallengeResultScreen
import com.example.hampouch.ui.dialog.CompleteDialog
import com.example.hampouch.ui.dialog.ConfirmActionCard
import com.example.hampouch.ui.hamtips.HamTipsBattleDetailScreen
import com.example.hampouch.ui.hamtips.HamTipsDetailScreen
import com.example.hampouch.ui.hamtips.HamTipsRepository
import com.example.hampouch.ui.hamtips.HamTipsWriteTipScreen
import com.example.hampouch.ui.mypage.components.MyPageMainTopBar
import com.example.hampouch.ui.mypage.components.MyPageMenuRow
import com.example.hampouch.ui.mypage.components.ProfileCard
import com.example.hampouch.ui.mypage.components.SettingsMenuCard
import com.example.hampouch.ui.mypage.components.SettingsMenuRow
import com.example.hampouch.ui.session.UserSession
import com.example.hampouch.ui.theme.HPGray2
import com.example.hampouch.ui.theme.HampouchTheme
import java.time.LocalDate

private enum class MyPageRoute {
    MAIN, ACCOUNT_SETTINGS, PROFILE_EDIT, ALL_SETTINGS, RECORD_ALARM, CHANGE_PASSWORD, CHALLENGE_HISTORY,
    CHALLENGE_RESULT, MY_TIPS, SAVED_TIPS, TIP_DETAIL, BATTLE_DETAIL, EDIT_TIP
}

@Composable
fun MyPageScreen(
    selectedBottomTab: BottomNavItem,
    onItemSelected: (BottomNavItem) -> Unit,
    onAddClick: () -> Unit,
    modifier: Modifier = Modifier,
    onNavigateToHamBattleLink: (String) -> Unit = {},
    onNotificationClick: () -> Unit = {},
    onLoggedOut: () -> Unit = {},
    onNavigateToChallengeExpenseAnalysis: (totalDays: Int, periodStart: LocalDate, periodEnd: LocalDate) -> Unit = { _, _, _ -> },
    onNavigateToAmountAdjustment: () -> Unit = {},
    onNavigateToTakeABreak: () -> Unit = {},
    onStartNewChallengeClick: (Int) -> Unit = {}
) {
    val context = LocalContext.current
    var route by rememberSaveable { mutableStateOf(MyPageRoute.MAIN) }
    var previousListRoute by rememberSaveable { mutableStateOf(MyPageRoute.MY_TIPS) }
    val profile = MyPageProfileStore.profile
    var showLogoutConfirm by remember { mutableStateOf(false) }
    var showPasswordChangedDialog by remember { mutableStateOf(false) }
    var selectedPostId by rememberSaveable { mutableStateOf<String?>(null) }
    var selectedChallengeId by rememberSaveable { mutableStateOf<String?>(null) }
    val challengeRecords = remember { MyPageMockData.challengeHistory() }
    val myTips = MyPageMockData.myTips()
    val savedTips = MyPageMockData.savedTips()

    val onTipClick: (TipPost) -> Unit = { tip ->
        selectedPostId = tip.id
        previousListRoute = route
        route = if (tip.type == TipPostType.BATTLE) MyPageRoute.BATTLE_DETAIL else MyPageRoute.TIP_DETAIL
    }

    AnimatedContent(
        targetState = route,
        transitionSpec = { fadeIn(tween(300)).togetherWith(fadeOut(tween(300))) },
        label = "mypage_route_transition"
    ) { currentRoute ->
    when (currentRoute) {
        MyPageRoute.MAIN -> {
            Box(modifier = modifier.fillMaxSize()) {
                Scaffold(
                    modifier = Modifier.fillMaxSize(),
                    containerColor = HPGray2,
                    contentWindowInsets = ScaffoldDefaults.contentWindowInsets.exclude(WindowInsets.statusBars),
                    bottomBar = {
                        BottomNavBar(
                            selectedItem = selectedBottomTab,
                            onItemSelected = onItemSelected,
                            onAddClick = onAddClick
                        )
                    }
                ) { innerPadding ->
                    MyPageMainContent(
                        profile = profile,
                        onBackClick = { onItemSelected(BottomNavItem.HOME) },
                        onProfileCardClick = { route = MyPageRoute.ACCOUNT_SETTINGS },
                        onChallengeHistoryClick = { route = MyPageRoute.CHALLENGE_HISTORY },
                        onMyTipsClick = { route = MyPageRoute.MY_TIPS },
                        onSavedTipsClick = { route = MyPageRoute.SAVED_TIPS },
                        onAllSettingsClick = { route = MyPageRoute.ALL_SETTINGS },
                        onLogoutClick = { showLogoutConfirm = true },
                        onNotificationClick = onNotificationClick,
                        modifier = Modifier.padding(innerPadding)
                    )
                }
                if (showLogoutConfirm) {
                    Dialog(
                        onDismissRequest = { showLogoutConfirm = false },
                        properties = DialogProperties(usePlatformDefaultWidth = false)
                    ) {
                        ConfirmActionCard(
                            question = stringResource(R.string.settings_logout_confirm_question),
                            confirmLabel = stringResource(R.string.settings_logout),
                            onCancel = { showLogoutConfirm = false },
                            onConfirm = {
                                showLogoutConfirm = false
                                UserSession.logout(context)
                                onLoggedOut()
                            }
                        )
                    }
                }
            }
        }

        MyPageRoute.ACCOUNT_SETTINGS -> {
            BackHandler { route = MyPageRoute.MAIN }
            Box(modifier = modifier.fillMaxSize()) {
                AccountSettingsScreen(
                    profile = profile,
                    onBackClick = { route = MyPageRoute.MAIN },
                    onProfileEditClick = { route = MyPageRoute.PROFILE_EDIT },
                    onChangePasswordClick = { route = MyPageRoute.CHANGE_PASSWORD },
                    onLoggedOut = onLoggedOut,
                    modifier = Modifier.fillMaxSize()
                )
                if (showPasswordChangedDialog) {
                    CompleteDialog(
                        message = stringResource(R.string.change_password_success_message),
                        onDismiss = {
                            showPasswordChangedDialog = false
                            route = MyPageRoute.MAIN
                        }
                    )
                }
            }
        }

        MyPageRoute.PROFILE_EDIT -> {
            BackHandler { route = MyPageRoute.ACCOUNT_SETTINGS }
            ProfileEditScreen(
                currentName = profile.name,
                currentAvatarUri = profile.avatarUri,
                isNicknameTaken = MyPageMockData::isNicknameTaken,
                onBackClick = { route = MyPageRoute.ACCOUNT_SETTINGS },
                onSubmit = { newName, newAvatarUri ->
                    MyPageProfileStore.update(newName, newAvatarUri)
                    route = MyPageRoute.MAIN
                },
                modifier = modifier.fillMaxSize()
            )
        }

        MyPageRoute.ALL_SETTINGS -> {
            BackHandler { route = MyPageRoute.MAIN }
            AllSettingsScreen(
                onBackClick = { route = MyPageRoute.MAIN },
                onRecordAlarmClick = { route = MyPageRoute.RECORD_ALARM },
                modifier = modifier.fillMaxSize()
            )
        }

        MyPageRoute.RECORD_ALARM -> {
            BackHandler { route = MyPageRoute.ALL_SETTINGS }
            RecordAlarmScreen(
                onBackClick = { route = MyPageRoute.ALL_SETTINGS },
                modifier = modifier.fillMaxSize()
            )
        }

        MyPageRoute.CHANGE_PASSWORD -> {
            BackHandler { route = MyPageRoute.ACCOUNT_SETTINGS }
            ChangePasswordScreen(
                email = profile.email,
                onBackClick = { route = MyPageRoute.ACCOUNT_SETTINGS },
                onSubmitSuccess = {
                    showPasswordChangedDialog = true
                    route = MyPageRoute.ACCOUNT_SETTINGS
                },
                modifier = modifier.fillMaxSize()
            )
        }

        MyPageRoute.CHALLENGE_HISTORY -> {
            BackHandler { route = MyPageRoute.MAIN }
            ChallengeHistoryScreen(
                records = challengeRecords,
                onBackClick = { route = MyPageRoute.MAIN },
                onRecordClick = { record ->
                    selectedChallengeId = record.id
                    route = MyPageRoute.CHALLENGE_RESULT
                },
                onNotificationClick = onNotificationClick,
                modifier = modifier.fillMaxSize()
            )
        }

        MyPageRoute.CHALLENGE_RESULT -> {
            BackHandler { route = MyPageRoute.CHALLENGE_HISTORY }
            val challenge = selectedChallengeId?.let { id -> ChallengeRepository.challenges.find { it.id == id } }
            if (challenge != null) {
                val state = ChallengeResultMockData.forChallenge(challenge)
                ChallengeResultScreen(
                    state = state,
                    onBackClick = { route = MyPageRoute.CHALLENGE_HISTORY },
                    onExpenseAnalysisClick = {
                        onNavigateToChallengeExpenseAnalysis(state.totalDays, state.periodStart, state.periodEnd)
                    },
                    onAdjustGoalClick = onNavigateToAmountAdjustment,
                    onStartNewChallengeClick = onStartNewChallengeClick,
                    onTakeABreakClick = onNavigateToTakeABreak
                )
            }
        }

        MyPageRoute.MY_TIPS -> {
            BackHandler { route = MyPageRoute.MAIN }
            TipListScreen(
                title = stringResource(R.string.mypage_menu_my_tips),
                emptyMessage = stringResource(R.string.my_tips_empty_message),
                tips = myTips,
                onBackClick = { route = MyPageRoute.MAIN },
                onTipClick = onTipClick,
                modifier = modifier.fillMaxSize()
            )
        }

        MyPageRoute.SAVED_TIPS -> {
            BackHandler { route = MyPageRoute.MAIN }
            TipListScreen(
                title = stringResource(R.string.mypage_menu_saved_tips),
                emptyMessage = stringResource(R.string.saved_tips_empty_message),
                tips = savedTips,
                onBackClick = { route = MyPageRoute.MAIN },
                onTipClick = onTipClick,
                modifier = modifier.fillMaxSize()
            )
        }

        MyPageRoute.TIP_DETAIL -> {
            val post = HamTipsRepository.allPosts.find { it.id == selectedPostId }
            if (post != null) {
                BackHandler { route = previousListRoute }
                HamTipsDetailScreen(
                    post = post,
                    onBackClick = { route = previousListRoute },
                    onEditClick = { route = MyPageRoute.EDIT_TIP },
                    onDeleted = { route = previousListRoute },
                    modifier = modifier.fillMaxSize()
                )
            }
        }

        MyPageRoute.BATTLE_DETAIL -> {
            val post = HamTipsRepository.allPosts.find { it.id == selectedPostId }
            if (post != null) {
                BackHandler { route = previousListRoute }
                HamTipsBattleDetailScreen(
                    post = post,
                    onBackClick = { route = previousListRoute },
                    onDeleted = { route = previousListRoute },
                    onNavigateToBattleLink = onNavigateToHamBattleLink,
                    modifier = modifier.fillMaxSize()
                )
            }
        }

        MyPageRoute.EDIT_TIP -> {
            val post = HamTipsRepository.allPosts.find { it.id == selectedPostId }
            if (post != null) {
                BackHandler { route = MyPageRoute.TIP_DETAIL }
                HamTipsWriteTipScreen(
                    editingPost = post,
                    onBackClick = { route = MyPageRoute.TIP_DETAIL },
                    onSubmitted = { route = MyPageRoute.TIP_DETAIL }
                )
            }
        }
    }
    }
}

@Composable
private fun MyPageMainContent(
    profile: MyPageProfile,
    onBackClick: () -> Unit,
    onProfileCardClick: () -> Unit,
    onChallengeHistoryClick: () -> Unit,
    onMyTipsClick: () -> Unit,
    onSavedTipsClick: () -> Unit,
    onAllSettingsClick: () -> Unit,
    onLogoutClick: () -> Unit,
    onNotificationClick: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 20.dp)
    ) {
        MyPageMainTopBar(
            title = stringResource(R.string.mypage_title),
            onBackClick = onBackClick,
            onNotificationClick = onNotificationClick
        )
        Spacer(modifier = Modifier.height(16.dp))
        ProfileCard(
            name = profile.name,
            handle = profile.handle,
            avatarUri = profile.avatarUri,
            onClick = onProfileCardClick
        )
        Spacer(modifier = Modifier.height(20.dp))
        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
            MyPageMenuRow(
                label = stringResource(R.string.mypage_menu_challenge_history),
                onClick = onChallengeHistoryClick
            )
            MyPageMenuRow(
                label = stringResource(R.string.mypage_menu_my_tips),
                onClick = onMyTipsClick
            )
            MyPageMenuRow(
                label = stringResource(R.string.mypage_menu_saved_tips),
                onClick = onSavedTipsClick
            )
            MyPageMenuRow(
                label = stringResource(R.string.mypage_menu_all_settings),
                onClick = onAllSettingsClick
            )
        }
        Spacer(modifier = Modifier.height(20.dp))
        SettingsMenuCard {
            SettingsMenuRow(
                label = stringResource(R.string.settings_logout),
                onClick = onLogoutClick,
                showChevron = false
            )
        }
        Spacer(modifier = Modifier.height(24.dp))
    }
}

@Preview(showBackground = true, name = "1. 마이페이지 메인")
@Composable
private fun MyPageScreenPreview() {
    HampouchTheme {
        MyPageScreen(
            selectedBottomTab = BottomNavItem.MY_PAGE,
            onItemSelected = {},
            onAddClick = {}
        )
    }
}
