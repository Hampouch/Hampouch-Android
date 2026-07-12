package com.example.hampouch.navigation

sealed class Screen(val route: String) {
    data object Onboarding : Screen("onboarding")
    data object Login : Screen("login")
    data object SignUp : Screen("signup")
    data object ResetPassword : Screen("reset_password")
    data object Loading : Screen("loading")
    data object Home : Screen("home")
}
