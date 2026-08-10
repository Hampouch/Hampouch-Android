package com.example.hampouch.ui.mypage

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.hampouch.R
import com.example.hampouch.data.model.MyPageProfile
import com.example.hampouch.data.remote.toUserMessage
import com.example.hampouch.data.repository.AuthRepository
import com.example.hampouch.ui.dialog.ConfirmActionCard
import com.example.hampouch.ui.mypage.components.MyPageDetailTopBar
import com.example.hampouch.ui.mypage.components.ProfileCard
import com.example.hampouch.ui.mypage.components.SettingsMenuCard
import com.example.hampouch.ui.mypage.components.SettingsMenuDivider
import com.example.hampouch.ui.mypage.components.SettingsMenuRow
import com.example.hampouch.ui.theme.HPGray2
import com.example.hampouch.ui.theme.HPSub
import com.example.hampouch.ui.theme.HampouchTheme
import kotlinx.coroutines.launch

@Composable
fun AccountSettingsScreen(
    profile: MyPageProfile,
    onBackClick: () -> Unit,
    onProfileEditClick: () -> Unit,
    onChangePasswordClick: () -> Unit,
    onLoggedOut: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    val authRepository = remember { AuthRepository.getInstance(context) }
    var showWithdrawConfirm by remember { mutableStateOf(false) }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(HPGray2)
    ) {
        MyPageDetailTopBar(
            title = stringResource(R.string.account_settings_title),
            onBackClick = onBackClick,
            showMoreMenu = false,
            modifier = Modifier.padding(horizontal = 8.dp)
        )
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp)
        ) {
            ProfileCard(
                name = profile.name,
                handle = profile.handle,
                avatarUri = profile.avatarUri
            )
            Spacer(modifier = Modifier.height(20.dp))
            SettingsMenuCard {
                SettingsMenuRow(
                    label = stringResource(R.string.account_settings_profile_edit),
                    onClick = onProfileEditClick
                )
                SettingsMenuDivider()
                SettingsMenuRow(
                    label = stringResource(R.string.settings_change_password),
                    onClick = onChangePasswordClick
                )
                SettingsMenuDivider()
                SettingsMenuRow(
                    label = stringResource(R.string.settings_withdraw),
                    onClick = { showWithdrawConfirm = true },
                    labelColor = HPSub,
                    showChevron = false
                )
            }
            Spacer(modifier = Modifier.height(24.dp))
        }
    }

    if (showWithdrawConfirm) {
        Dialog(
            onDismissRequest = { showWithdrawConfirm = false },
            properties = DialogProperties(usePlatformDefaultWidth = false)
        ) {
            ConfirmActionCard(
                question = stringResource(R.string.settings_withdraw_confirm_title),
                subtext = stringResource(R.string.settings_withdraw_confirm_subtext),
                confirmLabel = stringResource(R.string.settings_withdraw),
                onCancel = { showWithdrawConfirm = false },
                onConfirm = {
                    showWithdrawConfirm = false
                    coroutineScope.launch {
                        authRepository.withdraw()
                            .onSuccess { onLoggedOut() }
                            .onFailure { error ->
                                Toast.makeText(
                                    context,
                                    error.toUserMessage("회원 탈퇴에 실패했습니다."),
                                    Toast.LENGTH_SHORT
                                ).show()
                            }
                    }
                }
            )
        }
    }
}

@Preview(showBackground = true, name = "2. 계정 설정")
@Composable
private fun AccountSettingsScreenPreview() {
    HampouchTheme {
        AccountSettingsScreen(
            profile = MyPageMockData.defaultProfile(),
            onBackClick = {},
            onProfileEditClick = {},
            onChangePasswordClick = {},
            onLoggedOut = {}
        )
    }
}
