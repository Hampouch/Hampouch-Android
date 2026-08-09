package com.example.hampouch.ui.dialog

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.hampouch.R
import com.example.hampouch.ui.common.FieldMessage
import com.example.hampouch.ui.common.LoginTextField
import com.example.hampouch.ui.common.OrDivider
import com.example.hampouch.ui.theme.HPMain
import com.example.hampouch.ui.theme.HPSub1
import com.example.hampouch.ui.theme.HPSub3
import com.example.hampouch.ui.theme.HampouchTheme

@Composable
fun SocialSignUpNicknameDialog(
    nickname: String,
    onNicknameChange: (String) -> Unit,
    onCheckNickname: () -> Unit,
    nicknameCheckMessage: String?,
    isSignUpEnabled: Boolean,
    onSignUp: () -> Unit,
    onBack: () -> Unit
) {
    Dialog(
        onDismissRequest = onBack,
        properties = DialogProperties(
            dismissOnBackPress = true,
            dismissOnClickOutside = false,
            usePlatformDefaultWidth = false
        )
    ) {
        SocialSignUpNicknameDialogCard(
            nickname = nickname,
            onNicknameChange = onNicknameChange,
            onCheckNickname = onCheckNickname,
            nicknameCheckMessage = nicknameCheckMessage,
            isSignUpEnabled = isSignUpEnabled,
            onSignUp = onSignUp,
            onBack = onBack
        )
    }
}

@Composable
private fun SocialSignUpNicknameDialogCard(
    nickname: String,
    onNicknameChange: (String) -> Unit,
    onCheckNickname: () -> Unit,
    nicknameCheckMessage: String?,
    isSignUpEnabled: Boolean,
    onSignUp: () -> Unit,
    onBack: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 24.dp)
            .clip(RoundedCornerShape(20.dp))
            .background(HPSub3)
            .verticalScroll(rememberScrollState())
            .imePadding()
            .padding(horizontal = 15.dp, vertical = 30.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.Start
        ) {
            IconButton(onClick = onBack) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = stringResource(R.string.cd_back),
                    tint = HPSub1
                )
            }
        }
        Image(
            painter = painterResource(id = R.drawable.logo_app),
            contentDescription = "app_logo",
            modifier = Modifier.size(80.dp)
        )
        Spacer(modifier = Modifier.height(10.dp))
        Image(
            painter = painterResource(id = R.drawable.logo_title),
            contentDescription = "app_logo_title",
            modifier = Modifier
                .width(212.dp)
                .height(48.dp)
        )
        Spacer(modifier = Modifier.height(10.dp))
        OrDivider(text = "소셜 회원가입")
        Spacer(modifier = Modifier.height(10.dp))
        Column(
            modifier = Modifier.fillMaxWidth(),
            horizontalAlignment = Alignment.Start
        ) {
            LoginTextField(
                label = "닉네임",
                value = nickname,
                onValueChange = onNicknameChange,
                placeholder = "닉네임을 입력해주세요.",
                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.Text,
                    imeAction = ImeAction.Done
                ),
                onCheckClick = onCheckNickname
            )
            nicknameCheckMessage?.let { FieldMessage(it) }
        }
        Spacer(modifier = Modifier.height(20.dp))
        Button(
            onClick = onSignUp,
            enabled = isSignUpEnabled,
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp),
            shape = RoundedCornerShape(10.dp),
            colors = ButtonDefaults.buttonColors(containerColor = HPMain)
        ) {
            Text("가입하고 시작하기", style = MaterialTheme.typography.bodyLarge)
        }
    }
}

@Preview
@Composable
private fun SocialSignUpNicknameDialogCardPreview() {
    var nickname by remember { mutableStateOf("포치") }
    HampouchTheme {
        SocialSignUpNicknameDialogCard(
            nickname = nickname,
            onNicknameChange = { nickname = it },
            onCheckNickname = {},
            nicknameCheckMessage = "사용 가능한 닉네임입니다.",
            isSignUpEnabled = true,
            onSignUp = {},
            onBack = {}
        )
    }
}
