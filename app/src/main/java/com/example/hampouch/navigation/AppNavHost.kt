package com.example.hampouch.navigation

import android.util.Log
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.hampouch.ui.hambattle.HamBattleAddScreen
import com.example.hampouch.ui.hambattle.HamBattleChallengeResultPagerScreen
import com.example.hampouch.ui.hambattle.HamBattleEndedChallengeDetailScreen
import com.example.hampouch.ui.hambattle.HamBattleEndedChallengesScreen
import com.example.hampouch.ui.hambattle.HamBattleMockData
import com.example.hampouch.ui.hambattle.HamBattleScreen
import com.example.hampouch.ui.hambattle.HamBattleWaitingChallengeDetailScreen
import com.example.hampouch.ui.challengeresult.ChallengeResultScreen
import com.example.hampouch.ui.expensedetail.ExpenseCalendarRoute
import com.example.hampouch.ui.expensedetail.ExpenseDetailRoute
import com.example.hampouch.ui.expensedetail.ExpenseDetailStore
import com.example.hampouch.ui.expensedetail.ExpenseEditRoute
import com.example.hampouch.ui.home.HomeScreen
import com.example.hampouch.ui.login.LoginScreen
import com.example.hampouch.ui.minichallenge.MiniChallengeScreen
import com.example.hampouch.ui.onboarding.OnboardingRoute
import com.example.hampouch.ui.onboarding.steps.LoadingStep
import com.example.hampouch.ui.signup.ResetPasswordScreen
import com.example.hampouch.ui.signup.SignUpScreen
import java.time.LocalDate
import com.example.hampouch.ui.takeabreak.TakeABreakScreen

private const val TAG = "AppNavHost"

@Composable
fun AppNavHost(
    modifier: Modifier = Modifier,
    navController: NavHostController = rememberNavController()
) {
    // 회원가입/비밀번호 재설정 완료 후 Login 화면에서 한 번 보여줄 완료 메시지.
    var completeDialogMessage by remember { mutableStateOf<String?>(null) }

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

    NavHost(
        navController = navController,
        startDestination = Screen.Onboarding.route,
        modifier = modifier
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
            HomeScreen(
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
                }
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
                    HamBattleChallengeResultPagerScreen(
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
                    HamBattleEndedChallengeDetailScreen(
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
                        onBackClick = { navController.popBackStack() }
                    )
                }
            }
        }

        composable(Screen.ChallengeSummary.route) {
            ChallengeResultScreen(
                onBackClick = { navController.popBackStack() },
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
