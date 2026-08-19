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
import androidx.compose.runtime.LaunchedEffect
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
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
import com.example.hampouch.domain.model.MyPageProfile
import com.example.hampouch.domain.model.toUserMessage
import com.example.hampouch.ui.dialog.ConfirmActionCard
import com.example.hampouch.ui.mypage.components.MyPageMainTopBar
import com.example.hampouch.ui.mypage.components.ProfileCard
import com.example.hampouch.ui.mypage.components.SettingsMenuCard
import com.example.hampouch.ui.mypage.components.SettingsMenuDivider
import com.example.hampouch.ui.mypage.components.SettingsMenuRow
import com.example.hampouch.ui.theme.HPGray2
import com.example.hampouch.ui.theme.HPSub
import com.example.hampouch.data.local.AccountMockDataSource
import com.example.hampouch.ui.theme.HampouchTheme
import kotlinx.coroutines.launch

@Composable
fun AccountSettingsScreen(
    profile: MyPageProfile,
    onBackClick: () -> Unit,
    onProfileEditClick: () -> Unit,
    onChangePasswordClick: () -> Unit,
    onLoggedOut: (isWithdrawal: Boolean) -> Unit,
    modifier: Modifier = Modifier,
    viewModel: AccountSettingsViewModel = hiltViewModel()
) {
    val context = LocalContext.current

    LaunchedEffect(viewModel) {
        viewModel.events.collect { event ->
            when (event) {
                is AccountSettingsEvent.LoggedOut -> onLoggedOut(event.isWithdrawal)
                is AccountSettingsEvent.ShowMessage ->
                    Toast.makeText(context, event.message, Toast.LENGTH_SHORT).show()
            }
        }
    }

    AccountSettingsContent(
        profile = profile,
        actions = AccountSettingsActions(
            onBackClick = onBackClick,
            onProfileEditClick = onProfileEditClick,
            onChangePasswordClick = onChangePasswordClick,
            onWithdraw = viewModel::withdraw
        ),
        modifier = modifier
    )
}

@Composable
private fun AccountSettingsContent(
    profile: MyPageProfile,
    actions: AccountSettingsActions,
    modifier: Modifier = Modifier
) {
    var showWithdrawConfirm by remember { mutableStateOf(false) }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(HPGray2)
    ) {
        MyPageMainTopBar(
            title = stringResource(R.string.account_settings_title),
            onBackClick = actions.onBackClick
        )
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp)
        ) {
            ProfileCard(
                name = profile.name,
                email = profile.email,
                avatarUri = profile.avatarUri
            )
            Spacer(modifier = Modifier.height(20.dp))
            AccountSettingsMenu(actions = actions, onWithdrawClick = { showWithdrawConfirm = true })
            Spacer(modifier = Modifier.height(24.dp))
        }
    }

    if (showWithdrawConfirm) {
        AccountWithdrawDialog(
            onDismiss = { showWithdrawConfirm = false },
            onConfirm = {
                showWithdrawConfirm = false
                actions.onWithdraw()
            }
        )
    }
}

@Composable
private fun AccountSettingsMenu(actions: AccountSettingsActions, onWithdrawClick: () -> Unit) {
    SettingsMenuCard {
        SettingsMenuRow(
            label = stringResource(R.string.account_settings_profile_edit),
            onClick = actions.onProfileEditClick
        )
        SettingsMenuDivider()
        SettingsMenuRow(
            label = stringResource(R.string.settings_change_password),
            onClick = actions.onChangePasswordClick
        )
        SettingsMenuDivider()
        SettingsMenuRow(
            label = stringResource(R.string.settings_withdraw),
            onClick = onWithdrawClick,
            labelColor = HPSub,
            showChevron = false
        )
    }
}

@Composable
private fun AccountWithdrawDialog(onDismiss: () -> Unit, onConfirm: () -> Unit) {
    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        ConfirmActionCard(
            question = stringResource(R.string.settings_withdraw_confirm_title),
            subtext = stringResource(R.string.settings_withdraw_confirm_subtext),
            confirmLabel = stringResource(R.string.settings_withdraw),
            onCancel = onDismiss,
            onConfirm = onConfirm
        )
    }
}

@Preview(showBackground = true, name = "2. 계정 설정")
@Composable
private fun AccountSettingsScreenPreview() {
    HampouchTheme {
        AccountSettingsContent(
            profile = MyPageMockData.defaultProfile(AccountMockDataSource.normalUser),
            actions = AccountSettingsActions(
                onBackClick = {},
                onProfileEditClick = {},
                onChangePasswordClick = {},
                onWithdraw = {}
            )
        )
    }
}
