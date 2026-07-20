package com.example.hampouch.navigation

sealed class Screen(val route: String) {
    data object Onboarding : Screen("onboarding")
    data object Login : Screen("login")
    data object SignUp : Screen("signup")
    data object ResetPassword : Screen("reset_password")
    data object Loading : Screen("loading")
    data object Home : Screen("home")
    data object HamBattle : Screen("hambattle")
    data object HamBattleAdd : Screen("hambattle_add")
    data object ChallengeResult : Screen("challenge_result/{challengeId}") {
        fun createRoute(challengeId: String) = "challenge_result/$challengeId"
    }
    data object HamBattleEndedChallenges : Screen("hambattle_ended_challenges")
    data object HamBattleEndedChallengeDetail : Screen("hambattle_ended_challenge_detail/{challengeId}") {
        fun createRoute(challengeId: String) = "hambattle_ended_challenge_detail/$challengeId"
    }
}
