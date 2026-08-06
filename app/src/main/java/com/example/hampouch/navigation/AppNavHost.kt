package com.example.hampouch.navigation

import android.util.Log
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.platform.LocalContext
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.hampouch.ui.hambattle.HamBattleAddScreen
import com.example.hampouch.ui.hambattle.HamBattleChallengesResultPagerScreen
import com.example.hampouch.ui.hambattle.HamBattleEndedChallengesDetailScreen
import com.example.hampouch.ui.hambattle.HamBattleEndedChallengesScreen
import com.example.hampouch.ui.hambattle.HamBattleMockData
import com.example.hampouch.ui.hambattle.HamBattleScreen
import com.example.hampouch.ui.hambattle.HamBattleWaitingChallengeDetailScreen
import com.example.hampouch.data.model.ExpenseChallengePeriod
import com.example.hampouch.data.repository.AccountDataCoordinator
import com.example.hampouch.data.repository.ChallengeRepository
import com.example.hampouch.ui.amountadjustment.AmountAdjustmentMockData
import com.example.hampouch.ui.amountadjustment.AmountAdjustmentRoute
import com.example.hampouch.ui.challengeresult.ChallengeResultMockData
import com.example.hampouch.ui.challengeresult.ChallengeResultScreen
import com.example.hampouch.ui.expenseanalysis.CategoryDetailRoute
import com.example.hampouch.ui.expenseanalysis.ExpenseAnalysisHeaderMode
import com.example.hampouch.ui.expenseanalysis.ExpenseAnalysisRoute
import com.example.hampouch.ui.expenseanalysis.MonthlyExpenseRoute
import com.example.hampouch.ui.expenseanalysis.ReasonDetailRoute
import com.example.hampouch.ui.expensedetail.ExpenseCalendarRoute
import com.example.hampouch.ui.expensedetail.ExpenseDetailRoute
import com.example.hampouch.ui.expensedetail.ExpenseDetailStore
import com.example.hampouch.ui.expensedetail.ExpenseEditRoute
import com.example.hampouch.ui.expenseinput.ExpenseInputRoute
import com.example.hampouch.ui.home.HomeScreen
import com.example.hampouch.ui.login.LoginScreen
import com.example.hampouch.ui.minichallenge.MiniChallengeScreen
import com.example.hampouch.ui.notification.NotificationScreen
import com.example.hampouch.ui.notification.NotificationStore
import com.example.hampouch.ui.onboarding.OnboardingRoute
import com.example.hampouch.ui.onboarding.steps.LoadingStep
import com.example.hampouch.ui.session.UserSession
import com.example.hampouch.ui.signup.ResetPasswordScreen
import com.example.hampouch.ui.signup.SignUpScreen
import java.time.LocalDate
import java.time.YearMonth
import com.example.hampouch.ui.takeabreak.TakeABreakScreen
import com.example.hampouch.ui.takeabreak.TakeABreakStore

private const val TAG = "AppNavHost"

@Composable
fun AppNavHost(
    modifier: Modifier = Modifier,
    navController: NavHostController = rememberNavController()
) {
    var completeDialogMessage by remember { mutableStateOf<String?>(null) }

    val context = LocalContext.current
    val startDestination = remember {
        if (UserSession.restore(context)) Screen.Home.route else Screen.Onboarding.route
    }

    LaunchedEffect(UserSession.currentUser.id) {
        AccountDataCoordinator.syncIfNeeded(UserSession.currentUser.id)
    }

    var openCommunityWriteBattle by remember { mutableStateOf(false) }
    var pendingWriteBattleLink by remember { mutableStateOf("") }
    var pendingHomeTab by remember { mutableStateOf<BottomNavItem?>(null) }

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
        startDestination = startDestination,
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
            OnboardingRoute(
                onOnboardingComplete = { request ->
                    Log.d(TAG, "Onboarding finished with mock request: $request")
                    ChallengeRepository.startNewChallenge(request)
                    navController.navigate(Screen.Login.route)
                },
                onNavigateToLogin = { navController.navigate(Screen.Login.route) }
            )
        }

        composable(Screen.Login.route) {
            val message = completeDialogMessage
            LaunchedEffect(message) {
                if (message != null) {
                    completeDialogMessage = null
                }
            }
            LoginScreen(
                completeDialogMessage = message,
                onLoginSuccess = {
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
                        popUpTo(Screen.Onboarding.route) { inclusive = true }
                    }
                }
            )
        }

        composable(Screen.Home.route) {
            val startTab = pendingHomeTab
                ?: if (openCommunityWriteBattle) BottomNavItem.COMMUNITY else BottomNavItem.HOME
            val openWriteBattle = openCommunityWriteBattle
            val writeBattleLink = pendingWriteBattleLink
            LaunchedEffect(Unit) {
                openCommunityWriteBattle = false
                pendingHomeTab = null
                pendingWriteBattleLink = ""
            }
            HomeScreen(
                initialBottomTab = startTab,
                openHamTipsWriteBattleOnStart = openWriteBattle,
                initialHamTipsWriteBattleLink = writeBattleLink,
                onExitHamTipsWriteBattle = { navController.popBackStack() },
                onStartChallengeClick = {
                    navController.navigate(Screen.Onboarding.route) {
                        popUpTo(Screen.Home.route) { inclusive = true }
                    }
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
                    // 커뮤니티에서 참가하기로 들어온 경우, 뒤로가기 하면 원래 보던 게시글이 아니라
                    // 햄배틀 탭(방금 참가한 챌린지가 보이는 화면)으로 돌아가도록 백스택을 새로 짠다.
                    pendingHomeTab = BottomNavItem.HAM_BATTLE
                    navController.navigate(Screen.Home.route) {
                        popUpTo(Screen.Home.route) { inclusive = true }
                    }
                    navController.navigate(
                        Screen.HamBattleWaitingChallengeDetail.createRoute(challengeId)
                    )
                },
                onHamBattleJoinedFullFromCommunityClick = {
                    // 참가하면서 정원이 다 찼으면 더 이상 "대기중 상세"가 아니라 햄배틀 탭
                    // (진행중/대기중 목록)으로 보낸다.
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
                    navController.navigate(Screen.TakeABreak.route)
                },
                onAddExpenseClick = {
                    navController.navigate(Screen.ExpenseInput.createRoute(LocalDate.now()))
                },
                onNavigateToAmountAdjustment = {
                    navController.navigate(Screen.AmountAdjustment.route)
                },
                onNotificationClick = { navController.navigate(Screen.Notification.route) },
                onLoggedOut = {
                    navController.navigate(Screen.Onboarding.route) {
                        popUpTo(0) { inclusive = true }
                    }
                }
            )
        }

        composable(
            route = Screen.ExpenseDetail.route,
            arguments = listOf(navArgument("expenseId") { type = NavType.StringType })
        ) { backStackEntry ->
            val expenseId = backStackEntry.arguments?.getString("expenseId").orEmpty()
            val record = ExpenseDetailStore.byId(expenseId)
            if (record != null) {
                ExpenseDetailRoute(
                    record = record,
                    onBackClick = { navController.popBackStack() },
                    onEditClick = { navController.navigate(Screen.ExpenseEdit.createRoute(expenseId)) },
                    onDeleted = {
                        ExpenseDetailStore.delete(expenseId)
                        navController.popBackStack()
                    }
                )
            }
        }

        composable(
            route = Screen.ExpenseEdit.route,
            arguments = listOf(navArgument("expenseId") { type = NavType.StringType })
        ) { backStackEntry ->
            val expenseId = backStackEntry.arguments?.getString("expenseId").orEmpty()
            val record = ExpenseDetailStore.byId(expenseId)
            if (record != null) {
                ExpenseEditRoute(
                    record = record,
                    onBackClick = { navController.popBackStack() },
                    onSaved = { updated ->
                        ExpenseDetailStore.upsert(updated)
                        navController.popBackStack()
                    }
                )
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
            val active = ChallengeRepository.activeChallenge
            ExpenseCalendarRoute(
                onBackClick = {
                    ChallengeRepository.markVisitedExpenseEditAfterEnd()
                    navController.popBackStack()
                },
                onExpenseClick = { expenseId ->
                    navController.navigate(Screen.ExpenseDetail.createRoute(expenseId))
                },
                challengePeriod = ExpenseChallengePeriod(startDate = active.periodStart, endDate = active.periodEnd),
                onAddExpenseClick = { date ->
                    navController.navigate(Screen.ExpenseInput.createRoute(date))
                },
                restrictToChallengePeriod = true
            )
        }

        composable(
            route = Screen.ExpenseInput.route,
            arguments = listOf(navArgument("initialDateEpochDay") { type = NavType.LongType })
        ) { backStackEntry ->
            val epochDay = backStackEntry.arguments?.getLong("initialDateEpochDay") ?: LocalDate.now().toEpochDay()
            val initialDate = LocalDate.ofEpochDay(epochDay)
            val dailyLimit = ChallengeRepository.activeChallenge.dailyLimitOn(initialDate)
            val alreadySpent = ExpenseDetailStore.recordsForDate(initialDate).sumOf { it.amount }
            ExpenseInputRoute(
                todayBalance = (dailyLimit - alreadySpent).coerceAtLeast(0),
                dailyLimit = dailyLimit,
                initialDate = initialDate,
                onBackClick = { navController.popBackStack() },
                onNoSpendingToday = { navController.popBackStack() },
                onComplete = { record ->
                    ExpenseDetailStore.upsert(record)
                    navController.popBackStack()
                }
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
                onMonthlyViewClick = { navController.navigate(Screen.ExpenseAnalysisMonthly.route) },
                onCategoryDetailClick = { start, end ->
                    navController.navigate(Screen.ExpenseAnalysisCategoryDetail.createRoute(start, end, "delivery"))
                },
                onReasonDetailClick = { start, end ->
                    navController.navigate(Screen.ExpenseAnalysisReasonDetail.createRoute(start, end, "stress"))
                }
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
                }
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
                onBackClick = { navController.popBackStack() },
                onNotificationClick = { navController.navigate(Screen.Notification.route) }
            )
        }

        composable(Screen.HamBattle.route) {
            HamBattleScreen(
                selectedBottomTab = BottomNavItem.HAM_BATTLE,
                onItemSelected = onBottomNavItemSelected,
                onAddClick = {},
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
                }
            )
        }

        composable(Screen.HamBattleAdd.route) {
            HamBattleAddScreen(
                onBackClick = { navController.popBackStack() },
                onStartClick = { request ->
                    Log.d(TAG, "HamBattle challenge started: $request")
                    HamBattleMockData.startNewChallenge(request)
                    navController.popBackStack()
                }
            )
        }

        composable(
            route = Screen.ChallengeResult.route,
            arguments = listOf(navArgument("challengeId") { type = NavType.StringType })
        ) { backStackEntry ->
            val challengeId = backStackEntry.arguments?.getString("challengeId")
            val challenge = HamBattleMockData.challenges.find { it.id == challengeId }
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
                    onBackClick = { navController.popBackStack() },
                    onNotificationClick = { navController.navigate(Screen.Notification.route) },
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
            val challenge = HamBattleMockData.challenges.find { it.id == challengeId }
            if (challenge != null) {
                BottomNavScaffold(
                    selectedItem = BottomNavItem.HAM_BATTLE,
                    onItemSelected = onBottomNavItemSelected,
                    onAddClick = onBottomNavAddClick
                ) {
                    HamBattleEndedChallengesDetailScreen(
                        challenge = challenge,
                        onBackClick = { navController.popBackStack() },
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
            val challenge = HamBattleMockData.challenges.find { it.id == challengeId }
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
                            // pendingHomeTab이 이전에 넘겼던(하지만 실제로 화면에 그려지지 않아
                            // 아직 소비되지 않았을 수 있는) 값을 그대로 들고 있으면 커뮤니티 탭이
                            // 아니라 그 탭으로 잘못 열리므로, 여기서 명시적으로 지워준다.
                            pendingHomeTab = null
                            openCommunityWriteBattle = true
                            pendingWriteBattleLink = challenge.link
                            navController.navigate(Screen.Home.route)
                        }
                    )
                }
            }
        }

        composable(
            route = Screen.ChallengeSummary.route,
            arguments = listOf(navArgument("challengeId") { type = NavType.StringType })
        ) { backStackEntry ->
            val challengeId = backStackEntry.arguments?.getString("challengeId").orEmpty()
            val challenge = ChallengeRepository.challenges.find { it.id == challengeId }
            if (challenge != null) {
                val state = ChallengeResultMockData.forChallenge(challenge)
                ChallengeResultScreen(
                    state = state,
                    onBackClick = { navController.popBackStack() },
                    onExpenseAnalysisClick = {
                        navController.navigate(
                            Screen.ExpenseAnalysisChallenge.createRoute(state.totalDays, state.periodStart, state.periodEnd)
                        )
                    },
                    onAdjustGoalClick = { navController.navigate(Screen.AmountAdjustment.route) },
                    onStartNewChallengeClick = {
                        pendingHomeTab = null
                        navController.navigate(Screen.Home.route) {
                            popUpTo(Screen.Home.route) { inclusive = true }
                        }
                    },
                    onTakeABreakClick = { navController.navigate(Screen.TakeABreak.route) }
                )
            }
        }

        composable(Screen.TakeABreak.route) {
            TakeABreakScreen(
                onClose = { navController.popBackStack() },
                onKeepChallenge = { navController.popBackStack() },
                onStartBreak = { duration, customDays ->
                    TakeABreakStore.startBreak(duration, customDays)
                    pendingHomeTab = null
                    navController.navigate(Screen.Home.route) {
                        popUpTo(Screen.Home.route) { inclusive = true }
                    }
                }
            )
        }

        composable(Screen.AmountAdjustment.route) {
            AmountAdjustmentRoute(
                challenge = AmountAdjustmentMockData.challenge(),
                onBackClick = { navController.popBackStack() }
            )
        }

        composable(Screen.Notification.route) {
            NotificationScreen(
                notifications = NotificationStore.items,
                onBackClick = { navController.popBackStack() },
                onMarkAllReadClick = { NotificationStore.markAllRead() },
                onNotificationClick = { item -> NotificationStore.markRead(item.id) }
            )
        }
    }
}
