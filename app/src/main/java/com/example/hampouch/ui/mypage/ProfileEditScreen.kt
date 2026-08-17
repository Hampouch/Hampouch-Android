package com.example.hampouch.ui.mypage

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.PhotoCamera
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.hampouch.R
import com.example.hampouch.domain.model.toUserMessage
import kotlinx.coroutines.launch
import com.example.hampouch.ui.common.OrDivider
import com.example.hampouch.ui.dialog.CompleteDialog
import com.example.hampouch.ui.mypage.components.MyPageMainTopBar
import com.example.hampouch.ui.mypage.components.ProfileAvatar
import com.example.hampouch.ui.theme.HPBlack
import com.example.hampouch.ui.theme.HPGray2
import com.example.hampouch.ui.theme.HPGray4
import com.example.hampouch.ui.theme.HPGray5
import com.example.hampouch.ui.theme.HPMain
import com.example.hampouch.ui.theme.HPSub
import com.example.hampouch.ui.theme.HPText
import com.example.hampouch.ui.theme.HPWhite
import com.example.hampouch.ui.theme.HampouchTheme

@Composable
fun ProfileEditScreen(
    currentName: String,
    currentAvatarUri: String?,
    onValidateNickname: suspend (String) -> Result<Boolean>,
    onBackClick: () -> Unit,
    onSubmit: (newName: String, newAvatarUri: String?) -> Unit,
    modifier: Modifier = Modifier,
    onNotificationClick: () -> Unit
) {
    var isEditingName by remember { mutableStateOf(false) }
    var nicknameInput by remember { mutableStateOf("") }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    var avatarUri by remember { mutableStateOf(currentAvatarUri) }
    var showCompleteDialog by remember { mutableStateOf(false) }
    val nicknameFocusRequester = remember { FocusRequester() }
    val keyboardController = LocalSoftwareKeyboardController.current
    var isCheckingNickname by remember { mutableStateOf(false) }
    val coroutineScope = rememberCoroutineScope()

    val emptyNicknameMessage = stringResource(R.string.profile_edit_nickname_empty)
    val duplicateErrorMessage = stringResource(R.string.profile_edit_nickname_duplicate)
    val nicknameCheckFailedMessage = stringResource(R.string.profile_edit_nickname_check_failed)
    val isCompleteEnabled = errorMessage == null && !isCheckingNickname &&
        !(isEditingName && nicknameInput.trim().isEmpty())

    LaunchedEffect(isEditingName) {
        if (isEditingName) {
            nicknameFocusRequester.requestFocus()
            keyboardController?.show()
        }
    }

    val photoPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia()
    ) { uri ->
        if (uri != null) {
            avatarUri = uri.toString()
        }
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(HPGray2)
    ) {
        MyPageMainTopBar(
            title = stringResource(R.string.profile_edit_title),
            onBackClick = onBackClick,
            onNotificationClick = onNotificationClick
        )
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .imePadding()
                .padding(horizontal = 20.dp, vertical = 12.dp)
        ) {
            OrDivider(text = stringResource(R.string.profile_edit_section_user_info))
            Spacer(modifier = Modifier.height(24.dp))

            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Box(contentAlignment = Alignment.BottomEnd) {
                    ProfileAvatar(avatarUri = avatarUri, size = 96.dp)
                    Box(
                        modifier = Modifier
                            .size(32.dp)
                            .clip(CircleShape)
                            .background(HPWhite)
                            .border(1.dp, HPGray4, CircleShape)
                            .clickable {
                                photoPickerLauncher.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly))
                            },
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Filled.PhotoCamera,
                            contentDescription = stringResource(R.string.profile_edit_cd_camera),
                            tint = HPBlack,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }
                Spacer(modifier = Modifier.height(20.dp))

                Text(
                    text = stringResource(R.string.profile_edit_nickname_label),
                    style = MaterialTheme.typography.bodyMedium,
                    color = HPText,
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(8.dp))

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp)
                        .clip(RoundedCornerShape(10.dp))
                        .background(HPWhite)
                        .border(1.dp, HPGray5, RoundedCornerShape(10.dp))
                        .clickable(enabled = !isEditingName) {
                            isEditingName = true
                            nicknameInput = ""
                            errorMessage = null
                        }
                        .padding(horizontal = 12.dp),
                    contentAlignment = Alignment.CenterStart
                ) {
                    if (isEditingName) {
                        BasicTextField(
                            value = nicknameInput,
                            onValueChange = {
                                nicknameInput = it
                                errorMessage = null
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .focusRequester(nicknameFocusRequester),
                            textStyle = MaterialTheme.typography.bodyMedium.copy(color = HPBlack),
                            singleLine = true,
                            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
                            keyboardActions = KeyboardActions(onDone = {}),
                            cursorBrush = SolidColor(HPMain)
                        )
                    } else {
                        Text(
                            text = currentName,
                            style = MaterialTheme.typography.bodyMedium,
                            color = HPText,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }
                if (errorMessage != null) {
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = errorMessage.orEmpty(),
                        style = MaterialTheme.typography.bodySmall,
                        color = HPSub,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }

            Spacer(modifier = Modifier.height(30.dp))
            Button(
                onClick = {
                    val finalName = if (isEditingName) nicknameInput.trim() else currentName
                    when {
                        isEditingName && finalName.isEmpty() -> errorMessage = emptyNicknameMessage
                        !isEditingName -> showCompleteDialog = true
                        else -> coroutineScope.launch {
                            isCheckingNickname = true
                            onValidateNickname(finalName)
                                .onSuccess { taken ->
                                    if (taken) {
                                        errorMessage = duplicateErrorMessage
                                    } else {
                                        errorMessage = null
                                        showCompleteDialog = true
                                    }
                                }
                                .onFailure { error ->
                                    errorMessage = error.toUserMessage(nicknameCheckFailedMessage)
                                }
                            isCheckingNickname = false
                        }
                    }
                },
                enabled = isCompleteEnabled,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                shape = RoundedCornerShape(10.dp),
                colors = ButtonDefaults.buttonColors(containerColor = HPMain)
            ) {
                Text(stringResource(R.string.profile_edit_complete), style = MaterialTheme.typography.bodyLarge)
            }
        }
    }

    if (showCompleteDialog) {
        CompleteDialog(
            message = stringResource(R.string.profile_edit_success_message),
            onDismiss = {
                showCompleteDialog = false
                val finalName = if (isEditingName && nicknameInput.trim().isNotEmpty()) nicknameInput.trim() else currentName
                onSubmit(finalName, avatarUri)
            }
        )
    }
}


@Preview(showBackground = true, name = "9. 프로필 수정")
@Composable
private fun ProfileEditScreenPreview() {
    HampouchTheme {
        ProfileEditScreen(
            currentName = "절약왕 민준",
            currentAvatarUri = null,
            onValidateNickname = { Result.success(it == "햄포치") },
            onBackClick = {},
            onSubmit = { _, _ -> }, onNotificationClick = {}
        )
    }
}
