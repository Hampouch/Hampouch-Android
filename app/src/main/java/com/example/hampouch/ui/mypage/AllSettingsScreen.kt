package com.example.hampouch.ui.mypage

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
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
import com.example.hampouch.domain.model.NotificationSettingsState
import com.example.hampouch.ui.mypage.components.MyPageMainTopBar
import com.example.hampouch.ui.mypage.components.SectionLabel
import com.example.hampouch.ui.mypage.components.SettingsMenuCard
import com.example.hampouch.ui.mypage.components.SettingsMenuDivider
import com.example.hampouch.ui.mypage.components.SettingsMenuRow
import com.example.hampouch.ui.mypage.components.SettingsNavigateCard
import com.example.hampouch.ui.mypage.components.SettingsToggleCard
import com.example.hampouch.ui.theme.HPGray2
import com.example.hampouch.ui.theme.HampouchTheme
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle

@Composable
fun AllSettingsScreen(
    onBackClick: () -> Unit,
    onRecordAlarmClick: () -> Unit,
    modifier: Modifier = Modifier,
    onNotificationClick: () -> Unit,
    viewModel: AllSettingsViewModel = hiltViewModel()
) {
    val notificationState by viewModel.state.collectAsStateWithLifecycle()
    AllSettingsContent(
        notificationState = notificationState,
        actions = AllSettingsActions(
            onBackClick = onBackClick,
            onRecordAlarmClick = onRecordAlarmClick,
            onNotificationClick = onNotificationClick,
            onChallengeAlarmChange = viewModel::setChallengeAlarmEnabled,
            onHamBattleAlarmChange = viewModel::setHamBattleAlarmEnabled,
            onCommunityAlarmChange = viewModel::setCommunityAlarmEnabled,
            onMarketingInformationChange = viewModel::setMarketingInformationEnabled
        ),
        modifier = modifier
    )
}

@Composable
private fun AllSettingsContent(
    notificationState: NotificationSettingsState,
    actions: AllSettingsActions,
    modifier: Modifier = Modifier
) {
    var showComingSoonDialog by remember { mutableStateOf(false) }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(HPGray2)
    ) {
        MyPageMainTopBar(
            title = stringResource(R.string.settings_title),
            onBackClick = actions.onBackClick,
            onNotificationClick = actions.onNotificationClick
        )
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp)
        ) {
            AllSettingsNotificationSection(notificationState = notificationState, actions = actions)
            Spacer(modifier = Modifier.height(24.dp))
            AllSettingsDataAndSupportSection(onComingSoonClick = { showComingSoonDialog = true })
            Spacer(modifier = Modifier.height(24.dp))
        }
    }

    if (showComingSoonDialog) {
        AlertDialog(
            onDismissRequest = { showComingSoonDialog = false },
            confirmButton = {
                TextButton(onClick = { showComingSoonDialog = false }) {
                    Text(stringResource(R.string.common_confirm))
                }
            },
            text = { Text(stringResource(R.string.settings_coming_soon_message)) }
        )
    }
}

@Composable
private fun AllSettingsNotificationSection(notificationState: NotificationSettingsState, actions: AllSettingsActions) {
    SectionLabel(text = stringResource(R.string.settings_section_notification))
    Spacer(modifier = Modifier.height(8.dp))
    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        SettingsNavigateCard(
            title = stringResource(R.string.settings_record_alarm_title),
            subtitle = stringResource(R.string.settings_record_alarm_subtitle),
            onClick = actions.onRecordAlarmClick
        )
        SettingsToggleCard(
            title = stringResource(R.string.settings_challenge_alarm_title),
            subtitle = stringResource(R.string.settings_challenge_alarm_subtitle),
            checked = notificationState.challengeAlarmEnabled,
            onCheckedChange = actions.onChallengeAlarmChange,
            onRowClick = { actions.onChallengeAlarmChange(!notificationState.challengeAlarmEnabled) }
        )
        SettingsToggleCard(
            title = stringResource(R.string.settings_hambattle_alarm_title),
            subtitle = stringResource(R.string.settings_hambattle_alarm_subtitle),
            checked = notificationState.hamBattleAlarmEnabled,
            onCheckedChange = actions.onHamBattleAlarmChange,
            onRowClick = { actions.onHamBattleAlarmChange(!notificationState.hamBattleAlarmEnabled) }
        )
        SettingsToggleCard(
            title = stringResource(R.string.settings_community_alarm_title),
            subtitle = stringResource(R.string.settings_community_alarm_subtitle),
            checked = notificationState.communityAlarmEnabled,
            onCheckedChange = actions.onCommunityAlarmChange,
            onRowClick = { actions.onCommunityAlarmChange(!notificationState.communityAlarmEnabled) }
        )
        SettingsToggleCard(
            title = stringResource(R.string.settings_marketing_information_title),
            subtitle = stringResource(R.string.settings_marketing_information_subtitle),
            checked = notificationState.marketingInformationEnabled,
            onCheckedChange = actions.onMarketingInformationChange,
            onRowClick = {
                actions.onMarketingInformationChange(!notificationState.marketingInformationEnabled)
            }
        )
    }
}

@Composable
private fun AllSettingsDataAndSupportSection(onComingSoonClick: () -> Unit) {
    SectionLabel(text = stringResource(R.string.settings_section_data))
    Spacer(modifier = Modifier.height(8.dp))
    SettingsMenuCard {
        SettingsMenuRow(
            label = stringResource(R.string.settings_export_expense),
            onClick = onComingSoonClick,
            showChevron = false
        )
    }

    Spacer(modifier = Modifier.height(24.dp))
    SectionLabel(text = stringResource(R.string.settings_section_support))
    Spacer(modifier = Modifier.height(8.dp))
    SettingsMenuCard {
        SettingsMenuRow(
            label = stringResource(R.string.settings_customer_center),
            onClick = onComingSoonClick,
            showChevron = false
        )
        SettingsMenuDivider()
        SettingsMenuRow(
            label = stringResource(R.string.settings_terms),
            onClick = onComingSoonClick,
            showChevron = false
        )
    }
}

@Preview(showBackground = true, name = "3. 전체 설정")
@Composable
private fun AllSettingsScreenPreview() {
    HampouchTheme {
        AllSettingsContent(
            notificationState = NotificationSettingsState(),
            actions = AllSettingsActions(
                onBackClick = {},
                onRecordAlarmClick = {},
                onNotificationClick = {},
                onChallengeAlarmChange = {},
                onHamBattleAlarmChange = {},
                onCommunityAlarmChange = {},
                onMarketingInformationChange = {}
            )
        )
    }
}
