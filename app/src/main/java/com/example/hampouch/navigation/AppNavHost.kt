package com.example.hampouch.navigation

import android.util.Log
import android.widget.Toast
import androidx.activity.compose.BackHandler
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.Alignment
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.AnnotatedString
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.hampouch.domain.model.AuthSession
import com.example.hampouch.domain.model.ActiveChallenge
import com.example.hampouch.domain.model.ChallengeResultStatus
import com.example.hampouch.domain.model.HamBattleChallenge
import com.example.hampouch.domain.model.HamBattleStatus
import com.example.hampouch.domain.model.PendingChallengeResult
import com.example.hampouch.ui.hambattle.HamBattleAddScreen
import com.example.hampouch.ui.hambattle.HamBattleChallengesResultPagerScreen
import com.example.hampouch.ui.hambattle.HamBattleEndedChallengesDetailScreen
import com.example.hampouch.ui.hambattle.HamBattleEndedChallengesScreen
import com.example.hampouch.ui.hambattle.HamBattleEvent
import com.example.hampouch.ui.hambattle.HamBattleViewModel
import com.example.hampouch.ui.hambattle.HamBattleScreen
import com.example.hampouch.ui.hambattle.HamBattleWaitingChallengeDetailScreen
import com.example.hampouch.ui.hambattle.BattleInviteViewModel
import com.example.hampouch.ui.hambattle.buildBattleInviteUrl
import com.example.hampouch.core.config.BattleConfig
import com.example.hampouch.core.config.ExpenseConfig
import com.example.hampouch.domain.model.ExpenseChallengePeriod
import com.example.hampouch.ui.amountadjustment.AmountAdjustmentMockData
import com.example.hampouch.ui.amountadjustment.AmountAdjustmentRoute
import com.example.hampouch.ui.challengeresult.ChallengeResultMockData
import com.example.hampouch.ui.challengeresult.ChallengeResultUiState
import com.example.hampouch.ui.common.ChallengeLookupViewModel
import com.example.hampouch.ui.common.ExpenseLookupViewModel
import com.example.hampouch.ui.common.FullScreenLoadError
import com.example.hampouch.ui.common.FullScreenLoadingIndicator
import com.example.hampouch.ui.common.LoadState
import com.example.hampouch.ui.challengeresult.ChallengeResultScreen
import com.example.hampouch.ui.expenseanalysis.CategoryDetailRoute
import com.example.hampouch.ui.expenseanalysis.ExpenseAnalysisHeaderMode
import com.example.hampouch.ui.expenseanalysis.ExpenseAnalysisRoute
import com.example.hampouch.ui.expenseanalysis.MonthlyExpenseRoute
import com.example.hampouch.ui.expenseanalysis.ReasonDetailRoute
import com.example.hampouch.ui.expensedetail.ExpenseCalendarRoute
import com.example.hampouch.ui.expensedetail.ExpenseDetailEvent
import com.example.hampouch.ui.expensedetail.ExpenseDetailRoute
import com.example.hampouch.ui.expensedetail.ExpenseDetailViewModel
import com.example.hampouch.ui.expensedetail.ExpenseEditRoute
import com.example.hampouch.ui.expenseinput.ExpenseInputEvent
import com.example.hampouch.ui.expenseinput.ExpenseInputRoute
import com.example.hampouch.ui.expenseinput.ExpenseInputViewModel
import com.example.hampouch.ui.home.HomeScreen
import com.example.hampouch.ui.login.LoginScreen
import com.example.hampouch.ui.minichallenge.MiniChallengeScreen
import com.example.hampouch.ui.nextchallenge.NextChallengeRoute
import com.example.hampouch.ui.nextchallenge.NextChallengeTakeABreakRoute
import com.example.hampouch.ui.nextchallenge.FixedDateNextChallengeRoute
import com.example.hampouch.ui.onboarding.OnboardingRoute
import com.example.hampouch.ui.onboarding.steps.LoadingStep
import com.example.hampouch.ui.signup.ResetPasswordScreen
import com.example.hampouch.ui.signup.SignUpScreen
import java.time.LocalDate
import java.time.YearMonth
import com.example.hampouch.domain.model.toUserMessage
import com.example.hampouch.ui.takeabreak.TakeABreakRoute
import com.example.hampouch.ui.theme.HPGray2
import com.example.hampouch.ui.theme.HPMain
import com.example.hampouch.ui.theme.HPText
import com.example.hampouch.ui.theme.HPWhite
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

private const val TAG = "AppNavHost"

private fun PendingChallengeResult.toUiState(): ChallengeResultUiState = ChallengeResultUiState(
    status = ChallengeResultStatus.FAIL,
    title = title,
    periodStart = periodStart,
    periodEnd = periodEnd,
    totalDays = totalDays,
    successDays = successDays,
    streakDays = streakDays,
    amountLabel = "초과 금액",
    amountValue = amountValue,
    goalAmount = goalAmount,
    actualAmount = actualAmount,
    dailyLimit = dailyLimit,
    emotionStats = emotionStats,
    dailyRecords = dailyRecords,
    isEditable = false
)

@Composable
private fun rememberHamBattleChallenge(
    challengeId: String?,
    viewModel: HamBattleViewModel
): HamBattleChallenge? {
    if (!BattleConfig.USE_SERVER_BATTLE) {
        return viewModel.mockChallenges.value.find { it.id == challengeId }
    }
    val battleState by viewModel.state.collectAsStateWithLifecycle()
    val battleId = remember(challengeId) { challengeId?.toLongOrNull() }
    LaunchedEffect(battleId) {
        if (battleId != null) viewModel.loadBattleDetail(battleId)
    }
    return battleId?.let { battleState.detailFor(it) }
}

private data class StartupGateState(
    val resolvedStartDestination: String?,
    val startupErrorMessage: String?,
    val isResolving: Boolean
)

@Composable
private fun StartupGate(
    state: StartupGateState,
    onRetry: () -> Unit,
    modifier: Modifier = Modifier
) {
    when {
        state.resolvedStartDestination == null && state.startupErrorMessage != null -> {
            StartupConnectionErrorScreen(
                message = state.startupErrorMessage,
                isRetrying = state.isResolving,
                onRetry = onRetry,
                modifier = modifier
            )
        }
        else -> {
            Box(modifier = modifier.fillMaxSize().background(HPGray2))
        }
    }
}

@Composable
fun AppNavHost(
    modifier: Modifier = Modifier,
    navController: NavHostController = rememberNavController()
) {
    var completeDialogMessage by remember { mutableStateOf<String?>(null) }

    val context = LocalContext.current
    val clipboardManager = LocalClipboardManager.current
    val startDestinationViewModel: StartDestinationViewModel = hiltViewModel()
    val battleViewModel: HamBattleViewModel = hiltViewModel()
    val battleInviteViewModel: BattleInviteViewModel = hiltViewModel()
    val battleState by battleViewModel.state.collectAsStateWithLifecycle()
    val pendingBattleInviteCode by battleInviteViewModel.pendingBattleCode.collectAsStateWithLifecycle()
    var externalJoinCodeInFlight by remember { mutableStateOf<String?>(null) }
    var pendingHomeTab by remember { mutableStateOf<BottomNavItem?>(null) }
    LaunchedEffect(battleViewModel) {
        battleViewModel.events.collect { event ->
            when (event) {
                is HamBattleEvent.Created -> {
                    event.challenge.battleCode
                        ?.let(::buildBattleInviteUrl)
                        ?.let { inviteUrl ->
                            clipboardManager.setText(AnnotatedString(inviteUrl))
                            Toast.makeText(context, "초대 링크가 복사되었어요.", Toast.LENGTH_SHORT).show()
                        }
                    navController.popBackStack()
                }
                is HamBattleEvent.Joined -> externalJoinCodeInFlight?.let { code ->
                    externalJoinCodeInFlight = null
                    battleInviteViewModel.consume(code)
                    pendingHomeTab = BottomNavItem.HAM_BATTLE
                    navController.navigate(Screen.Home.route) {
                        popUpTo(Screen.Home.route) { inclusive = true }
                        launchSingleTop = true
                    }
                }
                is HamBattleEvent.ShowMessage -> {
                    Toast.makeText(context, event.message, Toast.LENGTH_SHORT).show()
                    externalJoinCodeInFlight?.let { code ->
                        externalJoinCodeInFlight = null
                        battleInviteViewModel.consume(code)
                        pendingHomeTab = BottomNavItem.HAM_BATTLE
                        navController.navigate(Screen.Home.route) {
                            popUpTo(Screen.Home.route) { inclusive = true }
                            launchSingleTop = true
                        }
                    }
                }
            }
        }
    }
    val coroutineScope = rememberCoroutineScope()
    val startDestination by startDestinationViewModel.startDestination.collectAsStateWithLifecycle()
    val pendingChallengeResult by startDestinationViewModel.pendingChallengeResult.collectAsStateWithLifecycle()
    val startupErrorMessage by startDestinationViewModel.startupErrorMessage.collectAsStateWithLifecycle()
    val isResolving by startDestinationViewModel.isResolving.collectAsStateWithLifecycle()
    val pendingNicknameSession by startDestinationViewModel.pendingNicknameSession.collectAsStateWithLifecycle()
    var openCommunityWriteBattle by remember { mutableStateOf(false) }
    var pendingWriteBattleLink by remember { mutableStateOf("") }
    var pendingMyTipDetail by remember { mutableStateOf<Pair<String, Boolean>?>(null) }
    var pendingCommunityPopularPostId by remember { mutableStateOf<String?>(null) }
    val currentBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = currentBackStackEntry?.destination?.route

    val resolvedStartDestination = startDestination

    if (resolvedStartDestination == null) {
        StartupGate(
            state = StartupGateState(
                resolvedStartDestination = resolvedStartDestination,
                startupErrorMessage = startupErrorMessage,
                isResolving = isResolving
            ),
            onRetry = startDestinationViewModel::retry,
            modifier = modifier
        )
        return
    }

    LaunchedEffect(pendingBattleInviteCode, currentRoute) {
        val battleCode = pendingBattleInviteCode ?: return@LaunchedEffect
        val unauthenticatedRoutes = setOf(
            Screen.Onboarding.route,
            Screen.Login.route,
            Screen.SignUp.route,
            Screen.ResetPassword.route,
            Screen.Loading.route
        )
        if (
            currentRoute != null &&
            currentRoute !in unauthenticatedRoutes &&
            externalJoinCodeInFlight == null
        ) {
            externalJoinCodeInFlight = battleCode
            battleViewModel.join(battleCode)
        }
    }

    val onBottomNavItemSelected: (BottomNavItem) -> Unit = { item ->
        pendingHomeTab = item
        navController.navigate(Screen.Home.route) {
            popUpTo(Screen.Home.route) { inclusive = true }
        }
    }

    val onBottomNavAddClick: () -> Unit = {
        navController.navigate(Screen.ExpenseInput.createRoute(LocalDate.now()))
    }

    val transitionSpec = tween<IntOffset>(durationMillis = 300)

    NavHost(
        navController = navController,
        startDestination = resolvedStartDestination,
        modifier = modifier,
        enterTransition = {
            slideInHorizontally(animationSpec = transitionSpec, initialOffsetX = { it / 4 }) +
                fadeIn(animationSpec = tween(300))
        },
        exitTransition = {
            slideOutHorizontally(animationSpec = transitionSpec, targetOffsetX = { -it / 4 }) +
                fadeOut(animationSpec = tween(300))
        },
        popEnterTransition = {
            slideInHorizontally(animationSpec = transitionSpec, initialOffsetX = { -it / 4 }) +
                fadeIn(animationSpec = tween(300))
        },
        popExitTransition = {
            slideOutHorizontally(animationSpec = transitionSpec, targetOffsetX = { it / 4 }) +
                fadeOut(animationSpec = tween(300))
        }
    ) {
        composable(Screen.Onboarding.route) {
            val goToLogin: () -> Unit = {
                navController.navigate(Screen.Login.route) {
                    popUpTo(Screen.Onboarding.route) { inclusive = true }
                }
            }
            OnboardingRoute(
                onOnboardingComplete = { request ->
                    Log.d(TAG, "Onboarding finished with mock request: $request")
                    startDestinationViewModel.captureOnboardingComplete(request)
                    goToLogin()
                },
                onNavigateToLogin = goToLogin
            )
        }

        composable(Screen.Login.route) {
            val message = completeDialogMessage
            LoginScreen(
                completeDialogMessage = message,
                onCompleteDialogDismissed = { completeDialogMessage = null },
                pendingNicknameSession = pendingNicknameSession,
                onLoginSuccess = {
                    startDestinationViewModel.consumePendingNicknameSession()
                    navController.navigate(Screen.Loading.route) {
                        popUpTo(Screen.Login.route) { inclusive = true }
                    }
                },
                onNavigateToSignUp = { navController.navigate(Screen.SignUp.route) },
                onNavigateToResetPassword = { navController.navigate(Screen.ResetPassword.route) }
            )
        }

        composable(Screen.SignUp.route) {
            SignUpScreen(
                onSignUpSuccess = {
                    completeDialogMessage = "회원가입이 완료되었습니다."
                    navController.navigate(Screen.Login.route) {
                        popUpTo(Screen.SignUp.route) { inclusive = true }
                    }
                },
                onNavigateToLogin = { navController.navigate(Screen.Login.route) }
            )
        }

        composable(Screen.ResetPassword.route) {
            ResetPasswordScreen(
                onResetSuccess = {
                    completeDialogMessage = "비밀번호가 변경되었습니다."
                    navController.navigate(Screen.Login.route) {
                        popUpTo(Screen.ResetPassword.route) { inclusive = true }
                    }
                },
                onNavigateToSignUp = { navController.navigate(Screen.SignUp.route) },
                onNavigateToLogin = { navController.navigate(Screen.Login.route) }
            )
        }

        composable(Screen.Loading.route) {
            LoadingStep(
                onTimeout = {
                    navController.navigate(Screen.Home.route) {
                        popUpTo(0) { inclusive = true }
                    }
                }
            )
        }

        composable(Screen.Home.route) {
            val homeChallengeVm: ChallengeLookupViewModel = hiltViewModel()
            val homeChallengeState by homeChallengeVm.challengeState.collectAsStateWithLifecycle()
            val startTab = pendingHomeTab
                ?: if (openCommunityWriteBattle) BottomNavItem.COMMUNITY else BottomNavItem.HOME
            val openWriteBattle = openCommunityWriteBattle
            val writeBattleLink = pendingWriteBattleLink
            val myTipDetail = pendingMyTipDetail
            val popularPostId = pendingCommunityPopularPostId
            LaunchedEffect(Unit) {
                openCommunityWriteBattle = false
                pendingHomeTab = null
                pendingWriteBattleLink = ""
                pendingMyTipDetail = null
                pendingCommunityPopularPostId = null
            }
            HomeScreen(
                initialBottomTab = startTab,
                openHamTipsWriteBattleOnStart = openWriteBattle,
                initialHamTipsWriteBattleLink = writeBattleLink,
                onExitHamTipsWriteBattle = { navController.popBackStack() },
                initialMyTipDetailPostId = myTipDetail?.first,
                initialMyTipDetailScrollToComments = myTipDetail?.second ?: false,
                initialPopularPostId = popularPostId,
                onStartChallengeClick = {
                    navController.navigate(Screen.NextChallengeTakeABreak.route)
                },
                onNavigateToMiniChallenge = { date ->
                    navController.navigate(Screen.MiniChallenge.createRoute(date))
                },
                onCalendarClick = {
                    navController.navigate(Screen.ExpenseCalendar.route)
                },
                onChallengeSummaryClick = { challengeId ->
                    navController.navigate(Screen.ChallengeSummary.createRoute(challengeId))
                },
                onHamBattleStartNewChallengeClick = { navController.navigate(Screen.HamBattleAdd.route) },
                onHamBattleChallengeClick = { challengeId ->
                    navController.navigate(Screen.ChallengeResult.createRoute(challengeId))
                },
                onHamBattleViewEndedChallengesClick = {
                    navController.navigate(Screen.HamBattleEndedChallenges.route)
                },
                onHamBattleViewEndedChallengeDetailClick = { challengeId ->
                    navController.navigate(
                        Screen.HamBattleEndedChallengeDetail.createRoute(challengeId)
                    )
                },
                onHamBattleWaitingChallengeClick = { challengeId ->
                    navController.navigate(
                        Screen.HamBattleWaitingChallengeDetail.createRoute(challengeId)
                    )
                },
                onHamBattleJoinedFromCommunityClick = { challengeId ->
                    pendingHomeTab = BottomNavItem.HAM_BATTLE
                    navController.navigate(Screen.Home.route) {
                        popUpTo(Screen.Home.route) { inclusive = true }
                    }
                    navController.navigate(
                        Screen.HamBattleWaitingChallengeDetail.createRoute(challengeId)
                    )
                },
                onHamBattleJoinedFullFromCommunityClick = {
                    pendingHomeTab = BottomNavItem.HAM_BATTLE
                    navController.navigate(Screen.Home.route) {
                        popUpTo(Screen.Home.route) { inclusive = true }
                    }
                },
                onNavigateToExpenseDetail = { expenseId ->
                    navController.navigate(Screen.ExpenseDetail.createRoute(expenseId))
                },
                onNavigateToExpenseCalendar = {
                    navController.navigate(Screen.ExpenseCalendar.route)
                },
                onNavigateToChallengeEndExpenseCalendar = {
                    navController.navigate(Screen.ChallengeEndExpenseCalendar.route)
                },
                onNavigateToChallengeExpenseAnalysis = { totalDays, start, end ->
                    navController.navigate(Screen.ExpenseAnalysisChallenge.createRoute(totalDays, start, end))
                },
                onNavigateToTakeABreak = {
                    navController.navigate(Screen.TakeABreak.createRoute())
                },
                onExtendBreak = {
                    navController.navigate(Screen.TakeABreak.createRoute(extend = true))
                },
                onAddExpenseClick = {
                    navController.navigate(Screen.ExpenseInput.createRoute(LocalDate.now()))
                },
                onAddExpenseClickForDate = { date ->
                    navController.navigate(Screen.ExpenseInput.createRoute(date))
                },
                onNavigateToAmountAdjustment = {
                    navController.navigate(Screen.AmountAdjustment.route)
                },
                onNavigateToYesterdayExpenseInput = {
                    navController.navigate(Screen.ExpenseInput.createRoute(LocalDate.now().minusDays(1)))
                },
                onLoggedOut = { isWithdrawal ->
                    startDestinationViewModel.logout(skipOnboardingSplash = !isWithdrawal)
                    navController.navigate(Screen.Onboarding.route) {
                        popUpTo(0) { inclusive = true }
                    }
                },
                onChallengeEndedFinishClick = {
                    val challenge = homeChallengeState.activeChallenge ?: return@HomeScreen
                    navController.navigate(Screen.ChallengeSummary.createRoute(challenge.id, locked = true))
                },
                onFixedDateChallengeDue = {
                    navController.navigate(Screen.FixedDateNextChallenge.route)
                }
            )
        }

        composable(
            route = Screen.ExpenseDetail.route,
            arguments = listOf(navArgument("expenseId") { type = NavType.StringType })
        ) { backStackEntry ->
            val expenseId = backStackEntry.arguments?.getString("expenseId").orEmpty()
            val viewModel: ExpenseDetailViewModel = hiltViewModel()
            val uiState by viewModel.uiState.collectAsStateWithLifecycle()
            LaunchedEffect(viewModel) {
                viewModel.events.collect { event ->
                    when (event) {
                        ExpenseDetailEvent.Finished -> navController.popBackStack()
                        is ExpenseDetailEvent.ShowMessage -> {
                            Log.e(TAG, event.message)
                            Toast.makeText(context, event.message, Toast.LENGTH_LONG).show()
                        }
                    }
                }
            }
            val detailLoadState = uiState.loadState
            when {
                uiState.record != null -> ExpenseDetailRoute(
                    record = uiState.record!!,
                    onBackClick = { navController.popBackStack() },
                    onEditClick = { navController.navigate(Screen.ExpenseEdit.createRoute(expenseId)) },
                    onDeleted = viewModel::delete,
                    staleErrorMessage = (detailLoadState as? LoadState.Failure)?.message,
                    onRetryStaleData = viewModel::retry
                )
                detailLoadState is LoadState.Failure ->
                    FullScreenLoadError(message = detailLoadState.message, onRetry = viewModel::retry)
                else -> FullScreenLoadingIndicator()
            }
        }

        composable(
            route = Screen.ExpenseEdit.route,
            arguments = listOf(navArgument("expenseId") { type = NavType.StringType })
        ) {
            val viewModel: ExpenseDetailViewModel = hiltViewModel()
            val uiState by viewModel.uiState.collectAsStateWithLifecycle()
            val editExpenseLookup: ExpenseLookupViewModel = hiltViewModel()
            val editChallengeState by editExpenseLookup.challengeState.collectAsStateWithLifecycle()
            LaunchedEffect(viewModel) {
                viewModel.events.collect { event ->
                    when (event) {
                        ExpenseDetailEvent.Finished -> navController.popBackStack()
                        is ExpenseDetailEvent.ShowMessage -> {
                            Log.e(TAG, event.message)
                            Toast.makeText(context, event.message, Toast.LENGTH_LONG).show()
                        }
                    }
                }
            }
            val editLoadState = uiState.loadState
            when {
                uiState.record != null -> {
                    val current = uiState.record!!
                    val activeChallenge = editChallengeState.activeChallenge
                    val isEditDateSelectable: (LocalDate) -> Boolean = { date ->
                        if (editChallengeState.hasOngoingChallenge && activeChallenge != null) {
                            !date.isBefore(activeChallenge.periodStart) && !date.isAfter(activeChallenge.effectivePeriodEnd)
                        } else {
                            val lastEndedChallenge = editChallengeState.challenges.lastOrNull()
                            lastEndedChallenge == null || date.isAfter(lastEndedChallenge.effectivePeriodEnd)
                        }
                    }
                    ExpenseEditRoute(
                        record = current,
                        onBackClick = { navController.popBackStack() },
                        onSaved = viewModel::save,
                        isDateSelectable = isEditDateSelectable
                    )
                }
                editLoadState is LoadState.Failure ->
                    FullScreenLoadError(message = editLoadState.message, onRetry = viewModel::retry)
                else -> FullScreenLoadingIndicator()
            }
        }

        composable(Screen.ExpenseCalendar.route) {
            ExpenseCalendarRoute(
                onBackClick = { navController.popBackStack() },
                onExpenseClick = { expenseId ->
                    navController.navigate(Screen.ExpenseDetail.createRoute(expenseId))
                },
                onExpenseAnalysisClick = {
                    navController.navigate(Screen.ExpenseAnalysis.createRoute(YearMonth.now()))
                },
                onAddExpenseClick = { date ->
                    navController.navigate(Screen.ExpenseInput.createRoute(date))
                }
            )
        }

        composable(Screen.ChallengeEndExpenseCalendar.route) {
            val challengeVm: ChallengeLookupViewModel = hiltViewModel()
            val challengeState by challengeVm.challengeState.collectAsStateWithLifecycle()
            val active = challengeState.activeChallenge ?: return@composable
            ExpenseCalendarRoute(
                onBackClick = {
                    challengeVm.markVisitedExpenseEditAfterEnd()
                    navController.popBackStack()
                },
                onExpenseClick = { expenseId ->
                    navController.navigate(Screen.ExpenseDetail.createRoute(expenseId))
                },
                challengePeriod = ExpenseChallengePeriod(startDate = active.periodStart, endDate = active.periodEnd),
                onAddExpenseClick = { date ->
                    navController.navigate(Screen.ExpenseInput.createRoute(date))
                },
                onExpenseAnalysisClick = { navController.navigate(Screen.ExpenseAnalysisMonthly.route) },
                restrictToChallengePeriod = true
            )
        }

        composable(
            route = Screen.ExpenseInput.route,
            arguments = listOf(navArgument("initialDateEpochDay") { type = NavType.LongType })
        ) {
            val viewModel: ExpenseInputViewModel = hiltViewModel()
            val uiState by viewModel.uiState.collectAsStateWithLifecycle()
            LaunchedEffect(viewModel) {
                viewModel.events.collect { event ->
                    when (event) {
                        ExpenseInputEvent.Saved -> navController.popBackStack()
                        ExpenseInputEvent.NoSpendSaved -> navController.navigate(Screen.Home.route) {
                            popUpTo(Screen.Home.route) { inclusive = true }
                        }
                        is ExpenseInputEvent.ShowMessage -> {
                            Log.e(TAG, event.message)
                            Toast.makeText(context, event.message, Toast.LENGTH_LONG).show()
                        }
                    }
                }
            }
            ExpenseInputRoute(
                todayBalance = uiState.todayBalance,
                dailyLimit = uiState.dailyLimit,
                form = uiState.form,
                showMaxAmountError = uiState.showMaxAmountError,
                onBackClick = {
                    viewModel.discardDraft()
                    navController.popBackStack()
                },
                onNoSpendingToday = { viewModel.markNoSpend(uiState.form.date) },
                onDateSelected = viewModel::changeDate,
                isDateSelectable = viewModel::isDateSelectable,
                onAmountDigit = viewModel::appendAmountDigit,
                onAmountDelete = viewModel::deleteAmountDigit,
                onStepChanged = viewModel::changeStep,
                onExpenseNameChange = viewModel::changeExpenseName,
                onCategorySelected = viewModel::selectCategory,
                onCustomCategoryConfirm = viewModel::confirmCustomCategory,
                onCustomCategoryTextChange = viewModel::changeCustomCategory,
                onReasonSelected = viewModel::selectReason,
                onCustomReasonConfirm = viewModel::confirmCustomReason,
                onCustomReasonTextChange = viewModel::changeCustomReason,
                onMemoChange = viewModel::changeMemo,
                onPhotosAdded = viewModel::addPhotos,
                onPhotosRemoved = viewModel::removePhotos,
                onPhotoReplaced = viewModel::replacePhoto,
                onComplete = viewModel::save
            )
        }

        composable(
            route = Screen.ExpenseAnalysis.route,
            arguments = listOf(navArgument("monthEpochDay") { type = NavType.LongType })
        ) { backStackEntry ->
            val epochDay = backStackEntry.arguments?.getLong("monthEpochDay") ?: LocalDate.now().toEpochDay()
            val month = YearMonth.from(LocalDate.ofEpochDay(epochDay))
            ExpenseAnalysisRoute(
                headerMode = ExpenseAnalysisHeaderMode.Month(month),
                onBackClick = { navController.popBackStack() },
                onCategoryDetailClick = { start, end ->
                    navController.navigate(Screen.ExpenseAnalysisCategoryDetail.createRoute(start, end, "delivery"))
                },
                onReasonDetailClick = { start, end ->
                    navController.navigate(Screen.ExpenseAnalysisReasonDetail.createRoute(start, end, "stress"))
                },
                onMonthlyViewClick = { navController.navigate(Screen.ExpenseAnalysisMonthly.route) }
            )
        }

        composable(
            route = Screen.ExpenseAnalysisChallenge.route,
            arguments = listOf(
                navArgument("totalDays") { type = NavType.IntType },
                navArgument("startEpochDay") { type = NavType.LongType },
                navArgument("endEpochDay") { type = NavType.LongType }
            )
        ) { backStackEntry ->
            val totalDays = backStackEntry.arguments?.getInt("totalDays") ?: 0
            val startEpochDay = backStackEntry.arguments?.getLong("startEpochDay") ?: LocalDate.now().toEpochDay()
            val endEpochDay = backStackEntry.arguments?.getLong("endEpochDay") ?: LocalDate.now().toEpochDay()
            ExpenseAnalysisRoute(
                headerMode = ExpenseAnalysisHeaderMode.Challenge(
                    totalDays = totalDays,
                    periodStart = LocalDate.ofEpochDay(startEpochDay),
                    periodEnd = LocalDate.ofEpochDay(endEpochDay)
                ),
                onBackClick = { navController.popBackStack() },
                onCategoryDetailClick = { start, end ->
                    navController.navigate(Screen.ExpenseAnalysisCategoryDetail.createRoute(start, end, "delivery"))
                },
                onReasonDetailClick = { start, end ->
                    navController.navigate(Screen.ExpenseAnalysisReasonDetail.createRoute(start, end, "stress"))
                },
                onMonthlyViewClick = { navController.navigate(Screen.ExpenseAnalysisMonthly.route) }
            )
        }

        composable(Screen.ExpenseAnalysisMonthly.route) {
            MonthlyExpenseRoute(onBackClick = { navController.popBackStack() })
        }

        composable(
            route = Screen.ExpenseAnalysisCategoryDetail.route,
            arguments = listOf(
                navArgument("startEpochDay") { type = NavType.LongType },
                navArgument("endEpochDay") { type = NavType.LongType },
                navArgument("initialCategoryId") { type = NavType.StringType }
            )
        ) { backStackEntry ->
            val startEpochDay = backStackEntry.arguments?.getLong("startEpochDay") ?: LocalDate.now().toEpochDay()
            val endEpochDay = backStackEntry.arguments?.getLong("endEpochDay") ?: LocalDate.now().toEpochDay()
            val initialCategoryId = backStackEntry.arguments?.getString("initialCategoryId") ?: "delivery"
            CategoryDetailRoute(
                periodStart = LocalDate.ofEpochDay(startEpochDay),
                periodEnd = LocalDate.ofEpochDay(endEpochDay),
                initialCategoryId = initialCategoryId,
                onBackClick = { navController.popBackStack() }
            )
        }

        composable(
            route = Screen.ExpenseAnalysisReasonDetail.route,
            arguments = listOf(
                navArgument("startEpochDay") { type = NavType.LongType },
                navArgument("endEpochDay") { type = NavType.LongType },
                navArgument("initialReasonId") { type = NavType.StringType }
            )
        ) { backStackEntry ->
            val startEpochDay = backStackEntry.arguments?.getLong("startEpochDay") ?: LocalDate.now().toEpochDay()
            val endEpochDay = backStackEntry.arguments?.getLong("endEpochDay") ?: LocalDate.now().toEpochDay()
            val initialReasonId = backStackEntry.arguments?.getString("initialReasonId") ?: "stress"
            ReasonDetailRoute(
                periodStart = LocalDate.ofEpochDay(startEpochDay),
                periodEnd = LocalDate.ofEpochDay(endEpochDay),
                initialReasonId = initialReasonId,
                onBackClick = { navController.popBackStack() }
            )
        }

        composable(
            route = Screen.MiniChallenge.route,
            arguments = listOf(navArgument("initialDateEpochDay") { type = NavType.LongType })
        ) { backStackEntry ->
            val epochDay = backStackEntry.arguments?.getLong("initialDateEpochDay") ?: LocalDate.now().toEpochDay()
            MiniChallengeScreen(
                initialDate = LocalDate.ofEpochDay(epochDay),
                onBackClick = { navController.popBackStack() }
            )
        }

        composable(Screen.HamBattle.route) {
            LaunchedEffect(Unit) { battleViewModel.loadMyBattles() }
            HamBattleScreen(
                selectedBottomTab = BottomNavItem.HAM_BATTLE,
                onItemSelected = onBottomNavItemSelected,
                onAddClick = {},
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
                onStartNewChallengeClick = { navController.navigate(Screen.HamBattleAdd.route) },
                onChallengeClick = { challengeId ->
                    navController.navigate(Screen.ChallengeResult.createRoute(challengeId))
                },
                onViewEndedChallengesClick = {
                    navController.navigate(Screen.HamBattleEndedChallenges.route)
                },
                onWaitingChallengeClick = { challengeId ->
                    navController.navigate(
                        Screen.HamBattleWaitingChallengeDetail.createRoute(
                            challengeId
                        )
                    )
                },
                onViewEndedChallengeDetailClick = { challengeId ->
                    navController.navigate(Screen.HamBattleEndedChallengeDetail.createRoute(challengeId))
                },
                viewModel = battleViewModel
            )
        }

        composable(Screen.HamBattleAdd.route) {
            val genericErrorMessage = "햄배틀 생성에 실패했어요."
            HamBattleAddScreen(
                onBackClick = { navController.popBackStack() },
                onStartClick = { request ->
                    Log.d(TAG, "HamBattle challenge started: $request")
                    battleViewModel.create(request)
                }
            )
        }

        composable(
            route = Screen.ChallengeResult.route,
            arguments = listOf(navArgument("challengeId") { type = NavType.StringType })
        ) { backStackEntry ->
            val challengeId = backStackEntry.arguments?.getString("challengeId")
            val challenge = rememberHamBattleChallenge(challengeId, battleViewModel)
            if (challenge != null) {
                BottomNavScaffold(
                    selectedItem = BottomNavItem.HAM_BATTLE,
                    onItemSelected = onBottomNavItemSelected,
                    onAddClick = onBottomNavAddClick
                ) {
                    HamBattleChallengesResultPagerScreen(
                        challenge = challenge,
                        onBackClick = { navController.popBackStack() },
                        onStartNewChallengeClick = { navController.navigate(Screen.HamBattleAdd.route) }
                    )
                }
            }
        }

        composable(Screen.HamBattleEndedChallenges.route) {
            BottomNavScaffold(
                selectedItem = BottomNavItem.HAM_BATTLE,
                onItemSelected = onBottomNavItemSelected,
                onAddClick = onBottomNavAddClick
            ) {
                HamBattleEndedChallengesScreen(
                    endedChallenges = if (BattleConfig.USE_SERVER_BATTLE) {
                        battleState.terminatedBattles
                    } else {
                        battleViewModel.mockChallengesWith(HamBattleStatus.ENDED)
                    },
                    onBackClick = { navController.popBackStack() },
                    onChallengeClick = { challengeId ->
                        navController.navigate(
                            Screen.HamBattleEndedChallengeDetail.createRoute(challengeId)
                        )
                    }
                )
            }
        }

        composable(
            route = Screen.HamBattleEndedChallengeDetail.route,
            arguments = listOf(navArgument("challengeId") { type = NavType.StringType })
        ) { backStackEntry ->
            val challengeId = backStackEntry.arguments?.getString("challengeId")
            val challenge = rememberHamBattleChallenge(challengeId, battleViewModel)
            if (challenge != null) {
                BottomNavScaffold(
                    selectedItem = BottomNavItem.HAM_BATTLE,
                    onItemSelected = onBottomNavItemSelected,
                    onAddClick = onBottomNavAddClick
                ) {
                    HamBattleEndedChallengesDetailScreen(
                        challenge = challenge,
                        onBackClick = { navController.popBackStack() },
                        onShareResultClick = { onBottomNavItemSelected(BottomNavItem.COMMUNITY) },
                        onStartNewChallengeClick = { navController.navigate(Screen.HamBattleAdd.route) }
                    )
                }
            }
        }

        composable(
            route = Screen.HamBattleWaitingChallengeDetail.route,
            arguments = listOf(navArgument("challengeId") { type = NavType.StringType })
        ) { backStackEntry ->
            val challengeId = backStackEntry.arguments?.getString("challengeId")
            val challenge = rememberHamBattleChallenge(challengeId, battleViewModel)
            if (challenge != null) {
                BottomNavScaffold(
                    selectedItem = BottomNavItem.HAM_BATTLE,
                    onItemSelected = onBottomNavItemSelected,
                    onAddClick = onBottomNavAddClick
                ) {
                    HamBattleWaitingChallengeDetailScreen(
                        challenge = challenge,
                        onBackClick = { navController.popBackStack() },
                        onShareToCommunityClick = {
                            pendingHomeTab = null
                            openCommunityWriteBattle = true
                            pendingWriteBattleLink = challenge.battleCode
                                ?.let(::buildBattleInviteUrl)
                                .orEmpty()
                            navController.navigate(Screen.Home.route)
                        }
                    )
                }
            }
        }

        composable(
            route = Screen.ChallengeSummary.route,
            arguments = listOf(
                navArgument("challengeId") { type = NavType.StringType },
                navArgument("locked") { type = NavType.BoolType; defaultValue = false }
            )
        ) { backStackEntry ->
            val challengeId = backStackEntry.arguments?.getString("challengeId").orEmpty()
            val locked = backStackEntry.arguments?.getBoolean("locked") ?: false
            val expenseLookup: ExpenseLookupViewModel = hiltViewModel()
            val challengeState by expenseLookup.challengeState.collectAsStateWithLifecycle()
            val challengeLookup: ChallengeLookupViewModel = hiltViewModel()
            val fixedDateDraft by challengeLookup.fixedDateDraft.collectAsStateWithLifecycle()
            val challenge = challengeState.challengeById(challengeId)
            LaunchedEffect(challengeId) { expenseLookup.loadResult(challengeId) }
            LaunchedEffect(challengeId) { challengeLookup.loadFixedDateDraft() }
            val state = if (challenge != null) {
                ChallengeResultMockData.forChallenge(
                    challenge, challengeState, expenseLookup::recordsForDate, expenseLookup::hasRecordOnDate
                )
            } else {
                pendingChallengeResult
                    ?.takeIf { it.challengeId == challengeId }
                    ?.toUiState()
            }
            if (state != null) {
                BackHandler(enabled = locked) {}
                ChallengeResultScreen(
                    state = state,
                    onBackClick = { navController.popBackStack() },
                    showBackButton = !locked,
                    showFollowUpActions = locked,
                    onExpenseAnalysisClick = {
                        navController.navigate(
                            Screen.ExpenseAnalysisChallenge.createRoute(state.totalDays, state.periodStart, state.periodEnd)
                        )
                    },
                    onAdjustGoalClick = { navController.navigate(Screen.AmountAdjustment.route) },
                    onShareClick = { onBottomNavItemSelected(BottomNavItem.COMMUNITY) },
                    onStartNewChallengeClick = { suggestedTargetAmount ->
                        val dueDraft = fixedDateDraft?.takeIf { it.isDue }
                        if (dueDraft != null) {
                            navController.navigate(Screen.FixedDateNextChallenge.route)
                        } else {
                            navController.navigate(
                                Screen.NextChallenge.createRoute(challengeId, suggestedTargetAmount)
                            )
                        }
                    },
                    onTakeABreakClick = { navController.navigate(Screen.TakeABreak.createRoute()) }
                )
            }
        }

        composable(
            route = Screen.NextChallenge.route,
            arguments = listOf(
                navArgument("challengeId") { type = NavType.StringType },
                navArgument("suggestedTargetAmount") { type = NavType.IntType }
            )
        ) { backStackEntry ->
            val challengeId = backStackEntry.arguments?.getString("challengeId").orEmpty()
            val suggestedTargetAmount = backStackEntry.arguments?.getInt("suggestedTargetAmount") ?: 0
            val expenseLookup: ExpenseLookupViewModel = hiltViewModel()
            val challengeState by expenseLookup.challengeState.collectAsStateWithLifecycle()
            val challenge = challengeState.challengeById(challengeId)
            val previousResult = if (challenge != null) {
                ChallengeResultMockData.forChallenge(
                    challenge, challengeState, expenseLookup::recordsForDate, expenseLookup::hasRecordOnDate
                )
            } else {
                pendingChallengeResult
                    ?.takeIf { it.challengeId == challengeId }
                    ?.toUiState()
            }
            if (previousResult != null) {
                NextChallengeRoute(
                    previousResult = previousResult,
                    suggestedTargetAmount = suggestedTargetAmount,
                    onStartChallengeClick = {
                        startDestinationViewModel.clearPendingChallengeResult()
                        pendingHomeTab = null
                        navController.navigate(Screen.Home.route) {
                            popUpTo(Screen.Home.route) { inclusive = true }
                        }
                    }
                )
            }
        }

        composable(Screen.FixedDateNextChallenge.route) {
            FixedDateNextChallengeRoute(
                onBackClick = { navController.popBackStack() },
                onStartChallengeClick = {
                    startDestinationViewModel.clearPendingChallengeResult()
                    pendingHomeTab = null
                    navController.navigate(Screen.Home.route) {
                        popUpTo(Screen.Home.route) { inclusive = true }
                    }
                },
                onEditSettingsClick = { draft ->
                    navController.navigate(
                        Screen.FixedDateNextChallengeEdit.createRoute(draft.sourceChallengeId.toString())
                    )
                }
            )
        }

        composable(
            route = Screen.FixedDateNextChallengeEdit.route,
            arguments = listOf(navArgument("challengeId") { type = NavType.StringType })
        ) { backStackEntry ->
            val challengeId = backStackEntry.arguments?.getString("challengeId").orEmpty()
            val expenseLookup: ExpenseLookupViewModel = hiltViewModel()
            val challengeState by expenseLookup.challengeState.collectAsStateWithLifecycle()
            val challengeLookup: ChallengeLookupViewModel = hiltViewModel()
            val fixedDateDraft by challengeLookup.fixedDateDraft.collectAsStateWithLifecycle()
            LaunchedEffect(challengeId) { challengeLookup.loadFixedDateDraft() }
            val draft = fixedDateDraft
            val challenge = challengeState.challengeById(challengeId) ?: draft?.let {
                ActiveChallenge(
                    id = it.sourceChallengeId.toString(),
                    totalDays = java.time.temporal.ChronoUnit.DAYS
                        .between(it.previousStartDate, it.previousEndDate).toInt() + 1,
                    periodStart = it.previousStartDate,
                    periodEnd = it.previousEndDate,
                    dailyLimit = it.dailyLimit,
                    targetAmount = it.budgetTotal,
                    savedAmount = 0,
                    streakDays = 0,
                    editCount = 0,
                    repeatMonthly = true
                )
            }
            if (challenge != null && draft != null) {
                val previousResult = ChallengeResultMockData.forChallenge(
                    challenge, challengeState, expenseLookup::recordsForDate, expenseLookup::hasRecordOnDate
                )
                NextChallengeRoute(
                    previousResult = previousResult,
                    suggestedTargetAmount = draft.budgetTotal,
                    fixedDateDraft = draft,
                    onStartChallengeClick = {
                        startDestinationViewModel.clearPendingChallengeResult()
                        pendingHomeTab = null
                        navController.navigate(Screen.Home.route) {
                            popUpTo(Screen.Home.route) { inclusive = true }
                        }
                    }
                )
            }
        }

        composable(Screen.NextChallengeTakeABreak.route) {
            NextChallengeTakeABreakRoute(
                onBackClick = { navController.popBackStack() },
                onStartChallengeClick = {
                    startDestinationViewModel.clearPendingChallengeResult()
                    pendingHomeTab = null
                    navController.navigate(Screen.Home.route) {
                        popUpTo(Screen.Home.route) { inclusive = true }
                    }
                }
            )
        }

        composable(
            route = Screen.TakeABreak.route,
            arguments = listOf(
                navArgument("extend") { type = NavType.BoolType; defaultValue = false }
            )
        ) { backStackEntry ->
            val isExtending = backStackEntry.arguments?.getBoolean("extend") ?: false
            TakeABreakRoute(
                isExtending = isExtending,
                onBack = { navController.popBackStack() },
                onKeepChallenge = { navController.popBackStack() },
                onBreakStarted = {
                    startDestinationViewModel.clearPendingChallengeResult()
                    pendingHomeTab = null
                    navController.navigate(Screen.Home.route) {
                        popUpTo(Screen.Home.route) { inclusive = true }
                    }
                }
            )
        }

        composable(Screen.AmountAdjustment.route) {
            val expenseLookup: ExpenseLookupViewModel = hiltViewModel()
            val challengeState by expenseLookup.challengeState.collectAsStateWithLifecycle()
            val currentChallenge = challengeState.activeChallenge?.let {
                AmountAdjustmentMockData.challenge(challengeState, expenseLookup::spentOnDate)
            }
            var retainedChallenge by remember { mutableStateOf(currentChallenge) }
            SideEffect {
                if (currentChallenge != null) retainedChallenge = currentChallenge
            }
            val adjustmentChallenge = currentChallenge ?: retainedChallenge ?: return@composable
            AmountAdjustmentRoute(
                challenge = adjustmentChallenge,
                onBackClick = { navController.popBackStack() },
                onChallengeAbandoned = { challengeId, _ ->
                    navController.navigate(Screen.ChallengeSummary.createRoute(challengeId, locked = true)) {
                        popUpTo(0) { inclusive = true }
                    }
                },
                onGoalAmountUpdated = {
                    pendingHomeTab = null
                    navController.navigate(Screen.Home.route) {
                        popUpTo(Screen.Home.route) { inclusive = true }
                    }
                }
            )
        }
    }
}

@Composable
private fun StartupConnectionErrorScreen(
    message: String,
    isRetrying: Boolean,
    onRetry: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .fillMaxSize()
            .background(HPGray2)
            .padding(horizontal = 24.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                text = "서버에 연결할 수 없습니다",
                style = MaterialTheme.typography.titleLarge
            )
            Spacer(modifier = Modifier.height(12.dp))
            Text(
                text = message,
                style = MaterialTheme.typography.bodyMedium,
                color = HPText
            )
            Spacer(modifier = Modifier.height(24.dp))
            Button(
                onClick = onRetry,
                enabled = !isRetrying,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                shape = RoundedCornerShape(10.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = HPMain,
                    contentColor = HPWhite
                )
            ) {
                Text(if (isRetrying) "다시 연결하는 중..." else "다시 시도")
            }
        }
    }
}
