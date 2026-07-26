package com.example.hampouch.navigation

import java.time.LocalDate

sealed class Screen(val route: String) {
    data object Onboarding : Screen("onboarding")
    data object Login : Screen("login")
    data object SignUp : Screen("signup")
    data object ResetPassword : Screen("reset_password")
    data object Loading : Screen("loading")
    data object Home : Screen("home")
    data object MiniChallenge : Screen("mini_challenge/{initialDateEpochDay}") {
        fun createRoute(initialDate: LocalDate) = "mini_challenge/${initialDate.toEpochDay()}"
    }
    data object HamBattle : Screen("hambattle")
    data object HamBattleAdd : Screen("hambattle_add")
    data object ChallengeResult : Screen("challenge_result/{challengeId}") {
        fun createRoute(challengeId: String) = "challenge_result/$challengeId"
    }
    data object HamBattleEndedChallenges : Screen("hambattle_ended_challenges")
    data object HamBattleEndedChallengeDetail : Screen("hambattle_ended_challenge_detail/{challengeId}") {
        fun createRoute(challengeId: String) = "hambattle_ended_challenge_detail/$challengeId"
    }
    data object HamBattleWaitingChallengeDetail : Screen("hambattle_waiting_challenge_detail/{challengeId}") {
        fun createRoute(challengeId: String) = "hambattle_waiting_challenge_detail/$challengeId"
    }
    data object ChallengeSummary : Screen("challenge_summary")
    data object TakeABreak : Screen("take_a_break")
}
