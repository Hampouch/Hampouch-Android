package com.example.hampouch.navigation

import java.time.LocalDate
import java.time.YearMonth

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
    data object ChallengeSummary : Screen("challenge_summary/{challengeId}") {
        fun createRoute(challengeId: String) = "challenge_summary/$challengeId"
    }
    data object TakeABreak : Screen("take_a_break")
    data object AmountAdjustment : Screen("amount_adjustment")
    data object ExpenseDetail : Screen("expense_detail/{expenseId}") {
        fun createRoute(expenseId: String) = "expense_detail/$expenseId"
    }
    data object ExpenseEdit : Screen("expense_edit/{expenseId}") {
        fun createRoute(expenseId: String) = "expense_edit/$expenseId"
    }
    data object ExpenseCalendar : Screen("expense_calendar")
    data object ChallengeEndExpenseCalendar : Screen("challenge_end_expense_calendar")
    data object ExpenseInput : Screen("expense_input/{initialDateEpochDay}") {
        fun createRoute(initialDate: LocalDate) = "expense_input/${initialDate.toEpochDay()}"
    }
    data object ExpenseAnalysis : Screen("expense_analysis/{monthEpochDay}") {
        fun createRoute(month: YearMonth) = "expense_analysis/${month.atDay(1).toEpochDay()}"
    }
    data object ExpenseAnalysisChallenge : Screen("expense_analysis_challenge/{totalDays}/{startEpochDay}/{endEpochDay}") {
        fun createRoute(totalDays: Int, start: LocalDate, end: LocalDate) =
            "expense_analysis_challenge/$totalDays/${start.toEpochDay()}/${end.toEpochDay()}"
    }
    data object ExpenseAnalysisMonthly : Screen("expense_analysis_monthly")
    data object ExpenseAnalysisCategoryDetail :
        Screen("expense_analysis_category/{startEpochDay}/{endEpochDay}/{initialCategoryId}") {
        fun createRoute(start: LocalDate, end: LocalDate, initialCategoryId: String) =
            "expense_analysis_category/${start.toEpochDay()}/${end.toEpochDay()}/$initialCategoryId"
    }
    data object ExpenseAnalysisReasonDetail :
        Screen("expense_analysis_reason/{startEpochDay}/{endEpochDay}/{initialReasonId}") {
        fun createRoute(start: LocalDate, end: LocalDate, initialReasonId: String) =
            "expense_analysis_reason/${start.toEpochDay()}/${end.toEpochDay()}/$initialReasonId"
    }
    data object Notification : Screen("notification")
}
