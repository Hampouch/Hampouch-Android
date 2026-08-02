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
import com.example.hampouch.data.model.ExpenseEntry
import com.example.hampouch.data.model.HomeChallenge
import com.example.hampouch.data.model.HomeUiState
import com.example.hampouch.data.model.HomeWarning
import com.example.hampouch.data.model.MiniChallengeEntry
import com.example.hampouch.data.model.WarningVariant
import com.example.hampouch.data.repository.ChallengeRepository
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
        val accentColor: Color
    )

    val defaultIcon: ImageVector = Icons.Filled.Restaurant
    val defaultColor: Color = HPText

    val categories = listOf(
        Category("delivery", R.string.category_delivery, Icons.Filled.DeliveryDining, Color(0xFF2859C5)),
        Category("dining_out", R.string.category_dining_out, Icons.Filled.RamenDining, HPSub2),
        Category("convenience", R.string.category_convenience, Icons.Filled.Storefront, Color(0xFF178BFD)),
        Category("cafe", R.string.category_cafe, Icons.Filled.LocalCafe, HPMain),
        Category("snack", R.string.category_snack, Icons.Filled.Cake, Color(0xFFED6C30)),
        Category("mart", R.string.category_mart, Icons.Filled.ShoppingBasket, Color(0xFFAB3A3A)),
        Category("drink", R.string.category_drink, Icons.Filled.SportsBar, Color(0xFFF2A74E)),
        Category("etc", R.string.category_etc, Icons.Filled.Restaurant, HPText)
    )

    fun byId(id: String?): Category? = categories.firstOrNull { it.id == id }
}

private val periodLabelFormatter: DateTimeFormatter =
    DateTimeFormatter.ofPattern("M월 d일", Locale.KOREA)

private fun buildChallenge(
    todayBalance: Int,
    savedAmount: Int = ChallengeRepository.activeChallenge.savedAmount,
    streakDays: Int = ChallengeRepository.activeChallenge.streakDays
): HomeChallenge {
    val challenge = ChallengeRepository.activeChallenge
    return HomeChallenge(
        totalDays = challenge.totalDays,
        dDay = challenge.dDayFrom(LocalDate.now()),
        periodStartLabel = challenge.periodStart.format(periodLabelFormatter),
        periodEndLabel = challenge.periodEnd.format(periodLabelFormatter),
        dailyLimit = challenge.dailyLimit,
        todayBalance = todayBalance,
        savedAmount = savedAmount,
        streakDays = streakDays
    )
}

object HomeMockData {

    fun freshDayState(userName: String, date: LocalDate): HomeUiState = HomeUiState(
        userName = userName,
        selectedDate = date,
        challenge = buildChallenge(todayBalance = ChallengeRepository.activeChallenge.dailyLimit),
        expenses = emptyList(),
        miniChallenges = MiniChallengeMockData.todayChallenges(),
        warnings = emptyList()
    )

    fun lowBalanceWithWarningState(userName: String, date: LocalDate): HomeUiState = HomeUiState(
        userName = userName,
        selectedDate = date,
        challenge = buildChallenge(todayBalance = 300),
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
        miniChallenges = MiniChallengeMockData.yesterdayChallenges(),
        warnings = listOf(
            HomeWarning(
                id = "w1",
                variant = WarningVariant.SUGGESTION,
                title = "목표 금액이 너무 힘든가요?",
                message = "3일 연속 한도 초과 - 금액을 조정해보세요"
            ),
            HomeWarning(
                id = "w2",
                variant = WarningVariant.ALERT,
                title = "배달 주의 구간이에요 !",
                message = "현재 배달비만 18,000원 소비했어요"
            )
        )
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
            MiniChallengeEntry(id = "m1", name = "커피 사먹지 않기", totalDays = 7, achievedDays = 2, isChecked = true),
            MiniChallengeEntry(id = "m2", name = "배달 음식 참기", totalDays = null, achievedDays = 0, isChecked = true)
        )
    )

    fun normalBalanceState(userName: String, date: LocalDate): HomeUiState = HomeUiState(
        userName = userName,
        selectedDate = date,
        challenge = buildChallenge(todayBalance = 7_300),
        expenses = listOf(
            ExpenseEntry(id = "e1", categoryId = "cafe", name = "스타벅스", reasonTag = "스트레스", amount = 4_500),
            ExpenseEntry(id = "e2", categoryId = "convenience", name = "세븐일레븐", amount = 3_200),
            ExpenseEntry(id = "e3", amount = 4_500)
        ),
        miniChallenges = listOf(
            MiniChallengeEntry(id = "m1", name = "커피 사먹지 않기", totalDays = 7, achievedDays = 4, isChecked = true),
            MiniChallengeEntry(id = "m2", name = "배달 음식 참기", totalDays = 7, achievedDays = 3, isChecked = false)
        ),
        warnings = emptyList()
    )

    fun decreasingBalanceState(userName: String, date: LocalDate): HomeUiState = HomeUiState(
        userName = userName,
        selectedDate = date,
        challenge = buildChallenge(todayBalance = 17_300),
        expenses = listOf(
            ExpenseEntry(id = "e1", categoryId = "cafe", name = "스타벅스", reasonTag = "스트레스", amount = 2_700),
            ExpenseEntry(id = "e2", categoryId = "convenience", name = "세븐일레븐", amount = 1_500),
            ExpenseEntry(id = "e3", amount = 1_500)
        ),
        miniChallenges = listOf(
            MiniChallengeEntry(id = "m1", name = "커피 사먹지 않기", totalDays = 7, achievedDays = 2, isChecked = true),
            MiniChallengeEntry(id = "m2", name = "배달 음식 참기", totalDays = null, achievedDays = 0, isChecked = true),
            MiniChallengeEntry(id = "m3", name = "물 많이 마시기", totalDays = 7, achievedDays = 3, isChecked = true),
            MiniChallengeEntry(id = "m4", name = "계단 이용하기", totalDays = 7, achievedDays = 0, isChecked = true)
        ),
        warnings = emptyList()
    )
}
