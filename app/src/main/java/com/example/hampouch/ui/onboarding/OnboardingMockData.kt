package com.example.hampouch.ui.onboarding

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Cake
import androidx.compose.material.icons.filled.DeliveryDining
import androidx.compose.material.icons.filled.LocalBar
import androidx.compose.material.icons.filled.LocalCafe
import androidx.compose.material.icons.filled.MoreHoriz
import androidx.compose.material.icons.filled.Restaurant
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material.icons.filled.Storefront
import androidx.compose.material.icons.filled.Subscriptions
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import com.example.hampouch.R
import com.example.hampouch.ui.theme.HPMain
import com.example.hampouch.ui.theme.HPSub

data class CategoryOption(
    val id: String,
    val labelResId: Int,
    val icon: ImageVector,
    val accentColor: Color
)

data class SalaryDayPreset(
    val day: Int,
    val labelResId: Int
)

data class PeriodPreset(
    val days: Int,
    val labelResId: Int
)

object OnboardingMockData {
    val periodPresets = listOf(
        PeriodPreset(days = 7, labelResId = R.string.onboarding_period_1week),
        PeriodPreset(days = 14, labelResId = R.string.onboarding_period_2weeks),
        PeriodPreset(days = 30, labelResId = R.string.onboarding_period_1month)
    )

    val salaryDayPresets = listOf(
        SalaryDayPreset(day = 1, labelResId = R.string.onboarding_salary_preset_1),
        SalaryDayPreset(day = 10, labelResId = R.string.onboarding_salary_preset_10),
        SalaryDayPreset(day = 25, labelResId = R.string.onboarding_salary_preset_25)
    )

    val categoryOptions = listOf(
        CategoryOption("food", R.string.category_food, Icons.Filled.Restaurant, Color(0xFFED6C30)),
        CategoryOption("cafe", R.string.category_cafe, Icons.Filled.LocalCafe, HPMain),
        CategoryOption("delivery", R.string.category_delivery, Icons.Filled.DeliveryDining, Color(0xFF2859C5)),
        CategoryOption("convenience", R.string.category_convenience, Icons.Filled.Storefront, Color(0xFFFCC21B)),
        CategoryOption("mart", R.string.category_mart, Icons.Filled.ShoppingCart, Color(0xFF178BFD)),
        CategoryOption("dining_out", R.string.category_dining_out, Icons.Filled.LocalBar, HPSub),
        CategoryOption("snack", R.string.category_snack, Icons.Filled.Cake, Color(0xFFED6C30)),
        CategoryOption("subscription", R.string.category_subscription, Icons.Filled.Subscriptions, Color(0xFF2859C5)),
        CategoryOption("etc", R.string.category_etc, Icons.Filled.MoreHoriz, HPSub)
    )
}
