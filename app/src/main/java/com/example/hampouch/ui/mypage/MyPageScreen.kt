package com.example.hampouch.ui.mypage

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.hampouch.R
import com.example.hampouch.data.model.MyPageProfile
import com.example.hampouch.navigation.BottomNavBar
import com.example.hampouch.navigation.BottomNavItem
import com.example.hampouch.ui.mypage.components.MyPageMainTopBar
import com.example.hampouch.ui.mypage.components.MyPageMenuRow
import com.example.hampouch.ui.mypage.components.ProfileCard
import com.example.hampouch.ui.theme.HPGray2
import com.example.hampouch.ui.theme.HampouchTheme

private enum class MyPageRoute {
    MAIN, ALL_SETTINGS, RECORD_ALARM, CHANGE_PASSWORD, CHALLENGE_HISTORY
}

@Composable
fun MyPageScreen(
    selectedBottomTab: BottomNavItem,
    onItemSelected: (BottomNavItem) -> Unit,
    onAddClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    var route by remember { mutableStateOf(MyPageRoute.MAIN) }
    var profile by remember { mutableStateOf(MyPageMockData.defaultProfile()) }
    var isEditingName by remember { mutableStateOf(false) }
    var editingName by remember { mutableStateOf(profile.name) }
    val challengeRecords = remember { MyPageMockData.challengeHistory() }

    when (route) {
        MyPageRoute.MAIN -> {
            Scaffold(
                modifier = modifier,
                containerColor = HPGray2,
                bottomBar = {
                    BottomNavBar(
                        selectedItem = selectedBottomTab,
                        onItemSelected = onItemSelected,
                        onAddClick = onAddClick
                    )
                }
            ) { innerPadding ->
                MyPageMainContent(
                    profile = profile,
                    isEditingName = isEditingName,
                    editingName = editingName,
                    onEditingNameChange = { editingName = it },
                    onStartEditName = {
                        editingName = profile.name
                        isEditingName = true
                    },
                    onConfirmEditName = {
                        if (editingName.isNotBlank()) {
                            profile = profile.copy(name = editingName)
                        }
                        isEditingName = false
                    },
                    onChallengeHistoryClick = { route = MyPageRoute.CHALLENGE_HISTORY },
                    onAllSettingsClick = { route = MyPageRoute.ALL_SETTINGS },
                    modifier = Modifier.padding(innerPadding)
                )
            }
        }

        MyPageRoute.ALL_SETTINGS -> {
            AllSettingsScreen(
                onBackClick = { route = MyPageRoute.MAIN },
                onRecordAlarmClick = { route = MyPageRoute.RECORD_ALARM },
                onChangePasswordClick = { route = MyPageRoute.CHANGE_PASSWORD },
                modifier = modifier.fillMaxSize()
            )
        }

        MyPageRoute.RECORD_ALARM -> {
            RecordAlarmScreen(
                onBackClick = { route = MyPageRoute.ALL_SETTINGS },
                modifier = modifier.fillMaxSize()
            )
        }

        MyPageRoute.CHANGE_PASSWORD -> {
            ChangePasswordScreen(
                onBackClick = { route = MyPageRoute.ALL_SETTINGS },
                onSubmitSuccess = { route = MyPageRoute.ALL_SETTINGS },
                modifier = modifier.fillMaxSize()
            )
        }

        MyPageRoute.CHALLENGE_HISTORY -> {
            ChallengeHistoryScreen(
                records = challengeRecords,
                onBackClick = { route = MyPageRoute.MAIN },
                modifier = modifier.fillMaxSize()
            )
        }
    }
}

@Composable
private fun MyPageMainContent(
    profile: MyPageProfile,
    isEditingName: Boolean,
    editingName: String,
    onEditingNameChange: (String) -> Unit,
    onStartEditName: () -> Unit,
    onConfirmEditName: () -> Unit,
    onChallengeHistoryClick: () -> Unit,
    onAllSettingsClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 20.dp)
    ) {
        Spacer(modifier = Modifier.height(12.dp))
        MyPageMainTopBar(
            title = stringResource(R.string.mypage_title),
            onCalendarClick = {},
            onNotificationClick = {}
        )
        Spacer(modifier = Modifier.height(16.dp))
        ProfileCard(
            name = profile.name,
            handle = profile.handle,
            isEditingName = isEditingName,
            editingName = editingName,
            onEditingNameChange = onEditingNameChange,
            onStartEditName = onStartEditName,
            onConfirmEditName = onConfirmEditName
        )
        Spacer(modifier = Modifier.height(20.dp))
        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
            MyPageMenuRow(
                label = stringResource(R.string.mypage_menu_challenge_history),
                onClick = onChallengeHistoryClick
            )
            MyPageMenuRow(
                label = stringResource(R.string.mypage_menu_my_tips),
                onClick = {}
            )
            MyPageMenuRow(
                label = stringResource(R.string.mypage_menu_saved_tips),
                onClick = {}
            )
            MyPageMenuRow(
                label = stringResource(R.string.mypage_menu_all_settings),
                onClick = onAllSettingsClick
            )
        }
        Spacer(modifier = Modifier.height(24.dp))
    }
}

@Preview(showBackground = true, name = "1. 마이페이지 메인")
@Composable
private fun MyPageScreenPreview() {
    HampouchTheme {
        MyPageScreen(
            selectedBottomTab = BottomNavItem.MY_PAGE,
            onItemSelected = {},
            onAddClick = {}
        )
    }
}
