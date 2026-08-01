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
import com.example.hampouch.ui.home.HomeScreen
import com.example.hampouch.ui.login.LoginScreen
import com.example.hampouch.ui.minichallenge.MiniChallengeScreen
import com.example.hampouch.ui.onboarding.OnboardingRoute
import com.example.hampouch.ui.onboarding.steps.LoadingStep
import com.example.hampouch.ui.session.UserSession
import com.example.hampouch.ui.signup.ResetPasswordScreen
import com.example.hampouch.ui.signup.SignUpScreen
import java.time.LocalDate
import java.time.YearMonth
import com.example.hampouch.ui.takeabreak.TakeABreakScreen

private const val TAG = "AppNavHost"

@Composable
fun AppNavHost(
    modifier: Modifier = Modifier,
    navController: NavHostController = rememberNavController()
) {
    // 회원가입/비밀번호 재설정 완료 후 Login 화면에서 한 번 보여줄 완료 메시지.
    var completeDialogMessage by remember { mutableStateOf<String?>(null) }

    // 앱 재실행 시 저장된 목데이터 로그인 상태를 복원해 홈 화면부터 시작한다.
    val context = LocalContext.current
    val startDestination = remember {
        if (UserSession.restore(context)) Screen.Home.route else Screen.Onboarding.route
    }

    // 햄배틀 모집 상세에서 "커뮤니티에 공유하기"를 누르면 Home을 커뮤니티 탭 + 모집 글쓰기 화면으로 새로 연다.
    var openCommunityWriteBattle by remember { mutableStateOf(false) }

    // 하단 네비바가 있는 화면에서 탭을 눌렀을 때: 햄배틀 탭이면 기존 홈 인스턴스로(탭 상태 보존), 그 외에는 홈을 새로 연다.
    val onBottomNavItemSelected: (BottomNavItem) -> Unit = { item ->
        if (item == BottomNavItem.HAM_BATTLE) {
            navController.popBackStack(Screen.Home.route, false)
        } else {
            navController.navigate(Screen.Home.route) {
                popUpTo(Screen.Home.route) { inclusive = true }
            }
        }
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
                    navController.navigate(Screen.Login.route)
                }
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
            val startTab = if (openCommunityWriteBattle) BottomNavItem.COMMUNITY else BottomNavItem.HOME
            val openWriteBattle = openCommunityWriteBattle
            LaunchedEffect(Unit) { openCommunityWriteBattle = false }
            HomeScreen(
                initialBottomTab = startTab,
                openHamTipsWriteBattleOnStart = openWriteBattle,
                onStartChallengeClick = {
                    navController.navigate(Screen.Onboarding.route) {
                        popUpTo(Screen.Home.route) { inclusive = true }
                    }
                },
                onNavigateToMiniChallenge = { date ->
                    navController.navigate(Screen.MiniChallenge.createRoute(date))
                },
                onCalendarClick = { navController.navigate(Screen.ChallengeSummary.route) },
                onHamBattleStartNewChallengeClick = { navController.navigate(Screen.HamBattleAdd.route) },
                onHamBattleChallengeClick = { challengeId ->
                    navController.navigate(Screen.ChallengeResult.createRoute(challengeId))
                },
                onHamBattleViewEndedChallengesClick = {
                    navController.navigate(Screen.HamBattleEndedChallenges.route)
                },
                onHamBattleWaitingChallengeClick = { challengeId ->
                    navController.navigate(
                        Screen.HamBattleWaitingChallengeDetail.createRoute(challengeId)
                    )
                },
                onNavigateToExpenseDetail = { expenseId ->
                    navController.navigate(Screen.ExpenseDetail.createRoute(expenseId))
                },
                onNavigateToExpenseCalendar = {
                    navController.navigate(Screen.ExpenseCalendar.route)
                },
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
                onBackClick = { navController.popBackStack() }
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
                    navController.popBackStack()
                }
            )
        }

        composable(
            route = Screen.ChallengeResult.route,
            arguments = listOf(navArgument("challengeId") { type = NavType.StringType })
        ) { backStackEntry ->
            val challengeId = backStackEntry.arguments?.getString("challengeId")
            val challenge = HamBattleMockData.activeChallenges.find { it.id == challengeId }
            if (challenge != null) {
                BottomNavScaffold(
                    selectedItem = BottomNavItem.HAM_BATTLE,
                    onItemSelected = onBottomNavItemSelected
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
                onItemSelected = onBottomNavItemSelected
            ) {
                HamBattleEndedChallengesScreen(
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
            val challenge = HamBattleMockData.endedChallenges.find { it.id == challengeId }
            if (challenge != null) {
                BottomNavScaffold(
                    selectedItem = BottomNavItem.HAM_BATTLE,
                    onItemSelected = onBottomNavItemSelected
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
            val challenge = HamBattleMockData.waitingChallenges.find { it.id == challengeId }
            if (challenge != null) {
                BottomNavScaffold(
                    selectedItem = BottomNavItem.HAM_BATTLE,
                    onItemSelected = onBottomNavItemSelected
                ) {
                    HamBattleWaitingChallengeDetailScreen(
                        challenge = challenge,
                        onBackClick = { navController.popBackStack() },
                        onShareToCommunityClick = {
                            openCommunityWriteBattle = true
                            navController.navigate(Screen.Home.route) {
                                popUpTo(Screen.Home.route) { inclusive = true }
                            }
                        }
                    )
                }
            }
        }

        composable(Screen.ChallengeSummary.route) {
            // TODO: 챌린지 성공/실패 화면으로 넘어가려면
            //  state: ChallengeResultUiState = ChallengeResultMockData.complete
            //  state: ChallengeResultUiState = ChallengeResultMockData.fail
            val state = ChallengeResultMockData.complete
            ChallengeResultScreen(
                state = state,
                onBackClick = { navController.popBackStack() },
                onExpenseAnalysisClick = {
                    navController.navigate(
                        Screen.ExpenseAnalysisChallenge.createRoute(state.totalDays, state.periodStart, state.periodEnd)
                    )
                },
                onStartNewChallengeClick = {
                    navController.navigate(Screen.Home.route) {
                        popUpTo(Screen.Home.route) { inclusive = true }
                    }
                },
                onTakeABreakClick = { navController.navigate(Screen.TakeABreak.route) }
            )
        }

        composable(Screen.TakeABreak.route) {
            TakeABreakScreen(
                onClose = { navController.popBackStack() },
                onKeepChallenge = { navController.popBackStack() },
                onStartBreak = { _, _ ->
                    navController.navigate(Screen.Home.route) {
                        popUpTo(Screen.Home.route) { inclusive = true }
                    }
                }
            )
        }
    }
}
