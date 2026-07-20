package com.example.hampouch.ui.mypage

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
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
import com.example.hampouch.data.model.NotificationSettingsState
import com.example.hampouch.ui.mypage.components.EditableConfirmField
import com.example.hampouch.ui.mypage.components.MyPageDetailTopBar
import com.example.hampouch.ui.mypage.components.SectionLabel
import com.example.hampouch.ui.mypage.components.SettingsMenuCard
import com.example.hampouch.ui.mypage.components.SettingsMenuDivider
import com.example.hampouch.ui.mypage.components.SettingsMenuRow
import com.example.hampouch.ui.mypage.components.SettingsToggleCard
import com.example.hampouch.ui.theme.HPGray2
import com.example.hampouch.ui.theme.HPSub
import com.example.hampouch.ui.theme.HampouchTheme

@Composable
fun AllSettingsScreen(
    onBackClick: () -> Unit,
    onRecordAlarmClick: () -> Unit,
    onChangePasswordClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    var notificationState by remember { mutableStateOf(NotificationSettingsState()) }
    var userId by remember { mutableStateOf(MyPageMockData.defaultProfile().userId) }
    var isEditingId by remember { mutableStateOf(false) }
    var editingId by remember { mutableStateOf(userId) }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(HPGray2)
    ) {
        MyPageDetailTopBar(
            title = stringResource(R.string.settings_title),
            onBackClick = onBackClick,
            modifier = Modifier.padding(horizontal = 8.dp)
        )
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp)
        ) {
            SectionLabel(text = stringResource(R.string.settings_section_notification))
            Spacer(modifier = Modifier.height(8.dp))
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                SettingsToggleCard(
                    title = stringResource(R.string.settings_record_alarm_title),
                    subtitle = stringResource(R.string.settings_record_alarm_subtitle),
                    checked = notificationState.recordAlarmEnabled,
                    onCheckedChange = { notificationState = notificationState.copy(recordAlarmEnabled = it) },
                    onRowClick = onRecordAlarmClick
                )
                SettingsToggleCard(
                    title = stringResource(R.string.settings_challenge_alarm_title),
                    subtitle = stringResource(R.string.settings_challenge_alarm_subtitle),
                    checked = notificationState.challengeAlarmEnabled,
                    onCheckedChange = { notificationState = notificationState.copy(challengeAlarmEnabled = it) }
                )
                SettingsToggleCard(
                    title = stringResource(R.string.settings_hambattle_alarm_title),
                    subtitle = stringResource(R.string.settings_hambattle_alarm_subtitle),
                    checked = notificationState.hamBattleAlarmEnabled,
                    onCheckedChange = { notificationState = notificationState.copy(hamBattleAlarmEnabled = it) }
                )
                SettingsToggleCard(
                    title = stringResource(R.string.settings_community_alarm_title),
                    subtitle = stringResource(R.string.settings_community_alarm_subtitle),
                    checked = notificationState.communityAlarmEnabled,
                    onCheckedChange = { notificationState = notificationState.copy(communityAlarmEnabled = it) }
                )
            }

            Spacer(modifier = Modifier.height(24.dp))
            SectionLabel(text = stringResource(R.string.settings_section_account))
            Spacer(modifier = Modifier.height(8.dp))
            SettingsMenuCard {
                if (isEditingId) {
                    Box(modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 12.dp)) {
                        EditableConfirmField(
                            value = editingId,
                            onValueChange = { editingId = it },
                            onConfirm = {
                                if (editingId.isNotBlank()) userId = editingId
                                isEditingId = false
                            }
                        )
                    }
                } else {
                    SettingsMenuRow(
                        label = stringResource(R.string.settings_edit_id),
                        onClick = {
                            editingId = userId
                            isEditingId = true
                        }
                    )
                }
                SettingsMenuDivider()
                SettingsMenuRow(
                    label = stringResource(R.string.settings_change_password),
                    onClick = onChangePasswordClick
                )
            }

            Spacer(modifier = Modifier.height(24.dp))
            SectionLabel(text = stringResource(R.string.settings_section_data))
            Spacer(modifier = Modifier.height(8.dp))
            SettingsMenuCard {
                SettingsMenuRow(label = stringResource(R.string.settings_export_expense), onClick = {})
                SettingsMenuDivider()
                SettingsMenuRow(
                    label = stringResource(R.string.settings_reset_data),
                    onClick = {},
                    labelColor = HPSub
                )
            }

            Spacer(modifier = Modifier.height(24.dp))
            SectionLabel(text = stringResource(R.string.settings_section_support))
            Spacer(modifier = Modifier.height(8.dp))
            SettingsMenuCard {
                SettingsMenuRow(label = stringResource(R.string.settings_customer_center), onClick = {})
                SettingsMenuDivider()
                SettingsMenuRow(label = stringResource(R.string.settings_terms_privacy), onClick = {})
            }

            Spacer(modifier = Modifier.height(24.dp))
            SettingsMenuCard {
                SettingsMenuRow(label = stringResource(R.string.settings_logout), onClick = {})
                SettingsMenuDivider()
                SettingsMenuRow(
                    label = stringResource(R.string.settings_withdraw),
                    onClick = {},
                    labelColor = HPSub
                )
            }
            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}

@Preview(showBackground = true, name = "2. 전체 설정")
@Composable
private fun AllSettingsScreenPreview() {
    HampouchTheme {
        AllSettingsScreen(onBackClick = {}, onRecordAlarmClick = {}, onChangePasswordClick = {})
    }
}
