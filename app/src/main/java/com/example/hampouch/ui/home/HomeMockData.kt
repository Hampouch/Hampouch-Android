package com.example.hampouch.ui.home

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Cake
import androidx.compose.material.icons.filled.DeliveryDining
import androidx.compose.material.icons.filled.LocalCafe
import androidx.compose.material.icons.filled.RamenDining
import androidx.compose.material.icons.filled.Restaurant
import androidx.compose.material.icons.filled.ShoppingBasket
import androidx.compose.material.icons.filled.SportsBar
import androidx.compose.material.icons.filled.Storefront
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import com.example.hampouch.R
import com.example.hampouch.domain.model.ActiveChallenge
import com.example.hampouch.domain.model.ExpenseEntry
import com.example.hampouch.domain.model.HomeChallenge
import com.example.hampouch.ui.home.HomeUiState
import com.example.hampouch.domain.model.ChallengeState
import com.example.hampouch.domain.model.MiniChallengeEntry
import com.example.hampouch.domain.model.MiniChallengeDuration
import com.example.hampouch.domain.model.miniChallengeDuration
import com.example.hampouch.ui.minichallenge.MiniChallengeMockData
import com.example.hampouch.ui.theme.HPMain
import com.example.hampouch.ui.theme.HPSub2
import com.example.hampouch.ui.theme.HPText
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.Locale

object HomeCategoryCatalog {
    data class Category(
        val id: String,
        val labelResId: Int,
        val icon: ImageVector,
        val accentColor: Color,
        val chipIconResId: Int
    )

    val defaultIcon: ImageVector = Icons.Filled.Restaurant
    val defaultColor: Color = HPText

    val categories = listOf(
        Category("delivery", R.string.category_delivery, Icons.Filled.DeliveryDining, Color(0xFF2859C5), R.drawable.icon_delivery),
        Category("dining_out", R.string.category_dining_out, Icons.Filled.RamenDining, HPSub2, R.drawable.icon_eatout),
        Category("convenience", R.string.category_convenience, Icons.Filled.Storefront, Color(0xFF178BFD), R.drawable.icon_conv),
        Category("cafe", R.string.category_cafe, Icons.Filled.LocalCafe, HPMain, R.drawable.icon_cafe),
        Category("snack", R.string.category_snack, Icons.Filled.Cake, Color(0xFFED6C30), R.drawable.icon_snack),
        Category("mart", R.string.category_mart, Icons.Filled.ShoppingBasket, Color(0xFFAB3A3A), R.drawable.icon_shopping),
        Category("drink", R.string.category_drink, Icons.Filled.SportsBar, Color(0xFFF2A74E), R.drawable.icon_beer),
        Category("etc", R.string.category_etc, Icons.Filled.Restaurant, HPText, R.drawable.icon_etc)
    )

    fun byId(id: String?): Category? = categories.firstOrNull { it.id == id }
}

private val periodLabelFormatter: DateTimeFormatter =
    DateTimeFormatter.ofPattern("M월 d일", Locale.KOREA)

private fun buildChallenge(
    challenge: ActiveChallenge,
    date: LocalDate,
    todayBalance: Int,
    savedAmount: Int = challenge.savedAmount,
    streakDays: Int = challenge.streakDays
): HomeChallenge = HomeChallenge(
    totalDays = challenge.totalDays,
    dDay = challenge.dDayFrom(date).coerceAtLeast(0),
    periodStartLabel = challenge.periodStart.format(periodLabelFormatter),
    periodEndLabel = challenge.periodEnd.format(periodLabelFormatter),
    dailyLimit = challenge.dailyLimitOn(date),
    todayBalance = todayBalance,
    savedAmount = savedAmount,
    streakDays = streakDays,
    isEnded = challenge.periodEnd.isBefore(LocalDate.now())
)

object HomeMockData {


    fun freshDayState(challengeState: ChallengeState, userName: String, date: LocalDate): HomeUiState {
        val challenge = challengeState.challengeFor(date)
        return HomeUiState(
            userName = userName,
            selectedDate = date,
            challenge = challenge?.let { buildChallenge(it, date, todayBalance = it.dailyLimitOn(date)) },
            expenses = emptyList(),
            miniChallenges = MiniChallengeMockData.todayChallenges()
        )
    }

    fun lowBalanceWithWarningState(challengeState: ChallengeState, userName: String, date: LocalDate): HomeUiState = HomeUiState(
        userName = userName,
        selectedDate = date,
        challenge = buildChallenge(challengeState.activeChallenge!!, date, todayBalance = 300),
        expenses = listOf(
            ExpenseEntry(id = "e1", categoryId = "cafe", name = "스타벅스", reasonTag = "스트레스", amount = 4_500),
            ExpenseEntry(id = "e2", categoryId = "convenience", name = "세븐일레븐", amount = 3_200),
            ExpenseEntry(id = "e3", amount = 4_500),
            ExpenseEntry(
                id = "e4",
                customCategoryName = "자취요리",
                name = "장보기 재료",
                reasonTag = "스트레스",
                amount = 15_800
            )
        ),
        miniChallenges = MiniChallengeMockData.yesterdayChallenges()
    )

    fun noActiveChallengeState(userName: String, date: LocalDate): HomeUiState = HomeUiState(
        userName = userName,
        selectedDate = date,
        challenge = null,
        expenses = listOf(
            ExpenseEntry(id = "e1", categoryId = "cafe", name = "스타벅스", reasonTag = "스트레스", amount = 4_500),
            ExpenseEntry(id = "e2", categoryId = "convenience", name = "세븐일레븐", amount = 3_200),
            ExpenseEntry(id = "e3", amount = 4_500)
        ),
        miniChallenges = listOf(
            MiniChallengeEntry(id = "m1", name = "커피 사먹지 않기", duration = miniChallengeDuration(7), achievedDays = 2, isChecked = true),
            MiniChallengeEntry(id = "m2", name = "배달 음식 참기", duration = MiniChallengeDuration.Today, achievedDays = 0, isChecked = true)
        )
    )

    fun normalBalanceState(challengeState: ChallengeState, userName: String, date: LocalDate): HomeUiState = HomeUiState(
        userName = userName,
        selectedDate = date,
        challenge = buildChallenge(challengeState.activeChallenge!!, date, todayBalance = 7_300),
        expenses = listOf(
            ExpenseEntry(id = "e1", categoryId = "cafe", name = "스타벅스", reasonTag = "스트레스", amount = 4_500),
            ExpenseEntry(id = "e2", categoryId = "convenience", name = "세븐일레븐", amount = 3_200),
            ExpenseEntry(id = "e3", amount = 4_500)
        ),
        miniChallenges = listOf(
            MiniChallengeEntry(id = "m1", name = "커피 사먹지 않기", duration = miniChallengeDuration(7), achievedDays = 4, isChecked = true),
            MiniChallengeEntry(id = "m2", name = "배달 음식 참기", duration = miniChallengeDuration(7), achievedDays = 3, isChecked = false)
        )
    )

    fun decreasingBalanceState(challengeState: ChallengeState, userName: String, date: LocalDate): HomeUiState = HomeUiState(
        userName = userName,
        selectedDate = date,
        challenge = buildChallenge(challengeState.activeChallenge!!, date, todayBalance = 17_300),
        expenses = listOf(
            ExpenseEntry(id = "e1", categoryId = "cafe", name = "스타벅스", reasonTag = "스트레스", amount = 2_700),
            ExpenseEntry(id = "e2", categoryId = "convenience", name = "세븐일레븐", amount = 1_500),
            ExpenseEntry(id = "e3", amount = 1_500)
        ),
        miniChallenges = listOf(
            MiniChallengeEntry(id = "m1", name = "커피 사먹지 않기", duration = miniChallengeDuration(7), achievedDays = 2, isChecked = true),
            MiniChallengeEntry(id = "m2", name = "배달 음식 참기", duration = MiniChallengeDuration.Today, achievedDays = 0, isChecked = true),
            MiniChallengeEntry(id = "m3", name = "물 많이 마시기", duration = miniChallengeDuration(7), achievedDays = 3, isChecked = true),
            MiniChallengeEntry(id = "m4", name = "계단 이용하기", duration = miniChallengeDuration(7), achievedDays = 0, isChecked = true)
        )
    )
}
