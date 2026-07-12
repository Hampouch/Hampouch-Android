package com.example.hampouch.navigation

import android.util.Log
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.hampouch.ui.login.LoginScreen
import com.example.hampouch.ui.onboarding.OnboardingRoute
import com.example.hampouch.ui.onboarding.steps.LoadingStep
import com.example.hampouch.ui.signup.ResetPasswordScreen
import com.example.hampouch.ui.signup.SignUpScreen

private const val TAG = "AppNavHost"

@Composable
fun AppNavHost(
    modifier: Modifier = Modifier,
    navController: NavHostController = rememberNavController()
) {
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
            LoginScreen(
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
                    navController.navigate(Screen.Loading.route) {
                        popUpTo(Screen.SignUp.route) { inclusive = true }
                    }
                },
                onNavigateToLogin = { navController.navigate(Screen.Login.route) }
            )
        }

        composable(Screen.ResetPassword.route) {
            ResetPasswordScreen(
                onResetSuccess = {
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
            // TODO: HomeScreen 구현되면 교체
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text("홈 화면 준비 중")
            }
        }
    }
}
