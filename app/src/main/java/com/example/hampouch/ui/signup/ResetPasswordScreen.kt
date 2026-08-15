package com.example.hampouch.ui.signup

import android.annotation.SuppressLint
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.hampouch.R
import com.example.hampouch.domain.model.EmailVerificationPurpose
import com.example.hampouch.domain.model.toUserMessage
import com.example.hampouch.ui.common.FieldLinkMessage
import com.example.hampouch.ui.common.FieldMessage
import com.example.hampouch.ui.common.FooterLinkRow
import com.example.hampouch.ui.common.LoginTextField
import com.example.hampouch.ui.common.OrDivider
import com.example.hampouch.ui.common.formatRemainingTime
import com.example.hampouch.ui.common.rememberCountdownSeconds
import com.example.hampouch.ui.theme.HPMain
import com.example.hampouch.ui.theme.HPSub3
import com.example.hampouch.ui.theme.HPText
import com.example.hampouch.ui.theme.HampouchTheme
import kotlinx.coroutines.launch

@SuppressLint("UnusedMaterial3ScaffoldPaddingParameter")
@Composable
fun ResetPasswordScreen(
    onResetSuccess: () -> Unit,
    onNavigateToSignUp: () -> Unit,
    onNavigateToLogin: () -> Unit,
    viewModel: ResetPasswordViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    LaunchedEffect(viewModel) {
        viewModel.events.collect { event ->
            when (event) {
                ResetPasswordEvent.Reset -> onResetSuccess()
            }
        }
    }
    val emailCodeRemainingSeconds = rememberCountdownSeconds(uiState.emailCodeExpiresAtMillis)
    val isEmailCodeExpired = emailCodeRemainingSeconds == 0
    val isEmailFieldEnabled =
        !uiState.isSendingEmailCode && !uiState.isEmailVerified && (!uiState.hasSentEmailCode || isEmailCodeExpired)
    val isCodeFieldEnabled =
        uiState.hasSentEmailCode && !isEmailCodeExpired && !uiState.isVerifyingEmailCode && !uiState.isEmailVerified

    Scaffold(
        modifier = Modifier.fillMaxSize().imePadding(),
        topBar = {},
        bottomBar = {},
        containerColor = HPSub3
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .padding(innerPadding)
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(start = 20.dp, end = 20.dp, top = 60.dp, bottom = 20.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Image(
                painter = painterResource(R.drawable.logo_title),
                contentDescription = "title_logo",
                modifier = Modifier.size(width = 212.dp, height = 48.dp),
            )
            Text(text = "식비 절약 챌린지", style = MaterialTheme.typography.bodyMedium, color = HPText)

            Spacer(modifier = Modifier.size(30.dp))
            OrDivider(text = "비밀번호 재설정")
            Spacer(modifier = Modifier.size(30.dp))

            Column(horizontalAlignment = Alignment.Start) {
                LoginTextField(
                    label = "가입한 이메일",
                    value = uiState.email,
                    onValueChange = viewModel::changeEmail,
                    placeholder = "hampouch@example.com",
                    keyboardOptions = KeyboardOptions(
                        keyboardType = KeyboardType.Email,
                        imeAction = ImeAction.Next
                    ),
                    onCheckClick = viewModel::sendEmailCode,
                    isCheckEnabled = isEmailFieldEnabled,
                    enabled = isEmailFieldEnabled
                )
                uiState.emailSendMessage?.let { FieldMessage(it) }
                if (uiState.hasSentEmailCode && !uiState.isEmailVerified) {
                    FieldLinkMessage("이메일을 잘못 입력하셨나요?", viewModel::resetEmailVerification)
                }
                Spacer(modifier = Modifier.size(20.dp))
                LoginTextField(
                    label = if (emailCodeRemainingSeconds != null && !isEmailCodeExpired) {
                        "인증번호 (${formatRemainingTime(emailCodeRemainingSeconds)})"
                    } else {
                        "인증번호"
                    },
                    value = uiState.emailCode,
                    onValueChange = viewModel::changeEmailCode,
                    placeholder = "인증번호를 입력해주세요.",
                    keyboardOptions = KeyboardOptions(
                        keyboardType = KeyboardType.Text,
                        imeAction = ImeAction.Next
                    ),
                    onCheckClick = viewModel::verifyEmailCode,
                    isCheckEnabled = isCodeFieldEnabled,
                    enabled = isCodeFieldEnabled
                )
                if (isEmailCodeExpired) {
                    FieldMessage("인증번호가 만료되었습니다.")
                } else {
                    uiState.emailVerifyMessage?.let { FieldMessage(it) }
                }
                Spacer(modifier = Modifier.size(20.dp))
                LoginTextField(
                    label = "비밀번호 재설정",
                    value = uiState.password,
                    onValueChange = viewModel::changePassword,
                    placeholder = "8자 이상, 영문 + 숫자 조합",
                    keyboardOptions = KeyboardOptions(
                        keyboardType = KeyboardType.Password,
                        imeAction = ImeAction.Done
                    ),
                    isValid = uiState.isPasswordValid,
                    visualTransformation = if (uiState.isPasswordVisible) {
                        VisualTransformation.None
                    } else {
                        PasswordVisualTransformation()
                    },
                    trailingIcon = {
                        IconButton(onClick = viewModel::togglePasswordVisibility) {
                            Image(
                                modifier = Modifier.size(20.dp),
                                painter = painterResource(
                                    id = if (uiState.isPasswordVisible) {
                                        R.drawable.login_eye
                                    } else {
                                        R.drawable.login_no_eye
                                    }
                                ),
                                contentDescription = if (uiState.isPasswordVisible) "Hide password" else "Show password",
                            )
                        }
                    }
                )
                if (uiState.showPasswordError) {
                    FieldMessage("비밀번호는 8자 이상이며 영문, 숫자를 포함해야 합니다.")
                }
            }

            uiState.resetErrorMessage?.let { FieldMessage(it) }

            Spacer(modifier = Modifier.size(30.dp))

            Button(
                onClick = viewModel::resetPassword,
                enabled = uiState.isResetEnabled,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                shape = RoundedCornerShape(10.dp),
                colors = ButtonDefaults.buttonColors(containerColor = HPMain),
            ) {
                Text("비밀번호 재설정", style = MaterialTheme.typography.bodyLarge)
            }

            Spacer(modifier = Modifier.size(10.dp))
            FooterLinkRow(
                text = "계정이 없으신가요?",
                linkText = "회원가입",
                onClick = onNavigateToSignUp
            )
            FooterLinkRow(
                text = "",
                linkText = "로그인 하러가기",
                onClick = onNavigateToLogin
            )
        }
    }
}

@Preview
@Composable
fun ResetPasswordScreenPreview() {
    HampouchTheme {
        ResetPasswordScreen(onResetSuccess = {}, onNavigateToSignUp = {}, onNavigateToLogin = {})
    }
}
