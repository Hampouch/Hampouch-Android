package com.example.hampouch.ui.onboarding

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Cake
import androidx.compose.material.icons.filled.DeliveryDining
import androidx.compose.material.icons.filled.House
import androidx.compose.material.icons.filled.LocalCafe
import androidx.compose.material.icons.filled.ShoppingBasket
import androidx.compose.material.icons.filled.SportsBar
import androidx.compose.material.icons.filled.Storefront
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import com.example.hampouch.R
import com.example.hampouch.ui.theme.HPMain
import com.example.hampouch.ui.theme.HPSub2
import com.example.hampouch.ui.theme.HPText

data class CategoryOption(
    val id: String,
    val labelResId: Int,
    val icon: ImageVector?,
    val accentColor: Color
)

data class PeriodPreset(
    val days: Int,
    val labelResId: Int
)

object OnboardingMockData {
    val periodPresets = listOf(
        PeriodPreset(days = 7, labelResId = R.string.onboarding_period_7days),
        PeriodPreset(days = 14, labelResId = R.string.onboarding_period_14days),
        PeriodPreset(days = 30, labelResId = R.string.onboarding_period_30days)
    )

    val categoryOptions = listOf(
        CategoryOption("delivery", R.string.category_delivery, Icons.Filled.DeliveryDining, Color(0xFF2859C5)),
        CategoryOption("dining_out", R.string.category_dining_out, Icons.Filled.House, HPSub2),
        CategoryOption("convenience", R.string.category_convenience, Icons.Filled.Storefront, Color(0xFF178BFD)),
        CategoryOption("cafe", R.string.category_cafe, Icons.Filled.LocalCafe, HPMain),
        CategoryOption("snack", R.string.category_snack, Icons.Filled.Cake, Color(0xFFED6C30)),
        CategoryOption("mart", R.string.category_mart, Icons.Filled.ShoppingBasket, Color(0xFFAB3A3A)),
        CategoryOption("dining_out_home", R.string.category_dining_out, Icons.Filled.House, HPSub2),
        CategoryOption("drink", R.string.category_drink, Icons.Filled.SportsBar, Color(0xFFF2A74E)),
        CategoryOption("other", R.string.category_other, null, HPText)
    )
}
