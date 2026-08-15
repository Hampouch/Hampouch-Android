package com.example.hampouch.navigation

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Chat
import androidx.compose.material.icons.automirrored.outlined.Chat
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.LocalFireDepartment
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.LocalFireDepartment
import androidx.compose.material.icons.outlined.Person
import androidx.compose.ui.graphics.vector.ImageVector
import com.example.hampouch.R

enum class BottomNavItem(
    val route: String,
    val labelResId: Int,
    val icon: ImageVector,
    val outlineIcon: ImageVector
) {
    HOME("home", R.string.nav_home, Icons.Filled.Home, Icons.Outlined.Home),
    HAM_BATTLE(
        "ham_battle",
        R.string.nav_ham_battle,
        Icons.Filled.LocalFireDepartment,
        Icons.Outlined.LocalFireDepartment
    ),
    COMMUNITY(
        "community",
        R.string.nav_community,
        Icons.AutoMirrored.Filled.Chat,
        Icons.AutoMirrored.Outlined.Chat
    ),
    MY_PAGE("my_page", R.string.nav_my_page, Icons.Filled.Person, Icons.Outlined.Person)
}
