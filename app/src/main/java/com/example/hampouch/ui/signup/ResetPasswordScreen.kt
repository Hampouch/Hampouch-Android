package com.example.hampouch.ui.signup

import android.annotation.SuppressLint
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
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
import com.example.hampouch.data.remote.dto.EmailVerificationPurpose
import com.example.hampouch.data.repository.AuthRepository
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
    onResetSuccess: () -> Unit = {},
    onNavigateToSignUp: () -> Unit = {},
    onNavigateToLogin: () -> Unit = {}
) {
    var email by rememberSaveable { mutableStateOf("") }
    var emailCode by rememberSaveable { mutableStateOf("") }
    var password by rememberSaveable { mutableStateOf("") }
    var nickname by rememberSaveable { mutableStateOf("") }
    var isPasswordVisible by rememberSaveable { mutableStateOf(false) }
    var emailSendMessage by remember { mutableStateOf<String?>(null) }
    var emailVerifyMessage by remember { mutableStateOf<String?>(null) }
    var isEmailVerified by remember { mutableStateOf(false) }
    var emailCodeExpiresAtMillis by rememberSaveable { mutableStateOf<Long?>(null) }
    var isNicknameAvailable by remember { mutableStateOf<Boolean?>(null) }
    var resetErrorMessage by remember { mutableStateOf<String?>(null) }
    val isPasswordValid = password.length >= 8 &&
            password.any { it in 'a'..'z' || it in 'A'..'Z' } &&
            password.any { it.isDigit() }
    val showPasswordError = password.isNotEmpty() && !isPasswordValid
    val isResetEnabled = isEmailVerified && isPasswordValid
    val emailCodeRemainingSeconds = rememberCountdownSeconds(emailCodeExpiresAtMillis)
    val isEmailCodeExpired = emailCodeRemainingSeconds == 0
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    val authRepository = remember { AuthRepository.getInstance(context) }

    Scaffold(topBar = {}, bottomBar = {}, containerColor = HPSub3) { innerPadding ->
        Column(
            modifier = Modifier
                .padding(innerPadding)
                .verticalScroll(rememberScrollState())
                .padding(start = 20.dp, end = 20.dp, top = 60.dp, bottom = 20.dp)
                .fillMaxWidth(),
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
                    value = email,
                    onValueChange = {
                        email = it
                        isEmailVerified = false
                    },
                    placeholder = "hampouch@example.com",
                    keyboardOptions = KeyboardOptions(
                        keyboardType = KeyboardType.Email,
                        imeAction = ImeAction.Next
                    ),
                    onCheckClick = {
                        coroutineScope.launch {
                            authRepository.sendEmailVerificationCode(email, EmailVerificationPurpose.PASSWORD_RESET)
                                .onSuccess { data ->
                                    emailSendMessage = "인증번호가 발송되었습니다."
                                    emailCodeExpiresAtMillis =
                                        System.currentTimeMillis() + data.expiresInSeconds * 1000L
                                }
                                .onFailure { error ->
                                    emailSendMessage = error.message ?: "인증번호 발송에 실패했습니다."
                                }
                        }
                    }
                )
                emailSendMessage?.let { FieldMessage(it) }
                Spacer(modifier = Modifier.size(20.dp))
                LoginTextField(
                    label = if (emailCodeRemainingSeconds != null && !isEmailCodeExpired) {
                        "인증번호 (${formatRemainingTime(emailCodeRemainingSeconds)})"
                    } else {
                        "인증번호"
                    },
                    value = emailCode,
                    onValueChange = {
                        emailCode = it
                        isEmailVerified = false
                    },
                    placeholder = "인증번호를 입력해주세요.",
                    keyboardOptions = KeyboardOptions(
                        keyboardType = KeyboardType.Text,
                        imeAction = ImeAction.Next
                    ),
                    onCheckClick = {
                        coroutineScope.launch {
                            authRepository.verifyEmailCode(email, emailCode, EmailVerificationPurpose.PASSWORD_RESET)
                                .onSuccess {
                                    isEmailVerified = true
                                    emailCodeExpiresAtMillis = null
                                    emailVerifyMessage = "이메일 인증이 완료되었습니다."
                                }
                                .onFailure { error ->
                                    isEmailVerified = false
                                    emailVerifyMessage = error.message ?: "인증번호를 다시 확인해주세요."
                                }
                        }
                    },
                    isCheckEnabled = !isEmailCodeExpired
                )
                if (isEmailCodeExpired) {
                    FieldMessage("인증번호가 만료되었습니다.")
                } else {
                    emailVerifyMessage?.let { FieldMessage(it) }
                }
                Spacer(modifier = Modifier.size(20.dp))
                LoginTextField(
                    label = "비밀번호 재설정",
                    value = password,
                    onValueChange = { password = it },
                    placeholder = "8자 이상, 영문 + 숫자 조합",
                    keyboardOptions = KeyboardOptions(
                        keyboardType = KeyboardType.Password,
                        imeAction = ImeAction.Done
                    ),
                    isValid = isPasswordValid,
                    visualTransformation = if (isPasswordVisible) {
                        VisualTransformation.None
                    } else {
                        PasswordVisualTransformation()
                    },
                    trailingIcon = {
                        IconButton(onClick = { isPasswordVisible = !isPasswordVisible }) {
                            Image(
                                modifier = Modifier.size(20.dp),
                                painter = painterResource(
                                    id = if (isPasswordVisible) {
                                        R.drawable.login_eye
                                    } else {
                                        R.drawable.login_no_eye
                                    }
                                ),
                                contentDescription = if (isPasswordVisible) "Hide password" else "Show password",
                            )
                        }
                    }
                )
                if (showPasswordError) {
                    FieldMessage("비밀번호를 다시 입력해주세요.")
                }
            }

            resetErrorMessage?.let { FieldMessage(it) }

            Spacer(modifier = Modifier.size(30.dp))

            Button(
                onClick = {
                    coroutineScope.launch {
                        authRepository.resetPassword(email, password)
                            .onSuccess {
                                resetErrorMessage = null
                                onResetSuccess()
                            }
                            .onFailure { error ->
                                resetErrorMessage = error.message ?: "비밀번호 재설정에 실패했습니다."
                            }
                    }
                },
                enabled = isResetEnabled,
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
        ResetPasswordScreen()
    }
}
