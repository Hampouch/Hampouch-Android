package com.example.hampouch.ui.login

import android.annotation.SuppressLint
import android.util.Log
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.text.KeyboardOptions
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
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.rememberCoroutineScope
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
import androidx.credentials.exceptions.GetCredentialCancellationException
import androidx.credentials.exceptions.NoCredentialException
import com.example.hampouch.R
import com.example.hampouch.core.auth.SocialAuthManager
import com.example.hampouch.data.model.AuthSession
import com.example.hampouch.data.remote.toUserMessage
import com.example.hampouch.data.repository.AuthRepository
import com.example.hampouch.ui.common.FooterLinkRow
import com.example.hampouch.ui.common.LoginTextField
import com.example.hampouch.ui.common.OrDivider
import com.example.hampouch.ui.dialog.CompleteDialog
import com.example.hampouch.ui.dialog.SocialSignUpNicknameDialog
import com.example.hampouch.ui.theme.HPBlack
import com.example.hampouch.ui.theme.HPMain
import com.example.hampouch.ui.theme.HPSub
import com.example.hampouch.ui.theme.HPSub3
import com.example.hampouch.ui.theme.HPText
import com.example.hampouch.ui.theme.HPWhite
import com.example.hampouch.ui.theme.HampouchTheme
import kotlinx.coroutines.launch

private const val TAG = "LoginScreen"

/**
 * 구글 로그인 실패를 사용자에게 보여줄 메시지로 변환한다.
 * 계정 선택창을 취소한 경우는 오류가 아니므로 null을 돌려주고 아무 메시지도 띄우지 않는다.
 */
private fun googleSignInErrorMessage(error: Throwable): String? = when (error) {
    is GetCredentialCancellationException -> null
    is NoCredentialException -> "기기에 로그인된 구글 계정이 없습니다."
    else -> "구글 로그인에 실패했습니다."
}

@SuppressLint("UnusedMaterial3ScaffoldPaddingParameter")
@Composable
fun LoginScreen(
    completeDialogMessage: String? = null,
    pendingNicknameSession: AuthSession? = null,
    onLoginSuccess: () -> Unit = {},
    onNavigateToSignUp: () -> Unit = {},
    onNavigateToResetPassword: () -> Unit = {}
) {
    val context = LocalContext.current
    var email by rememberSaveable { mutableStateOf("") }
    var password by rememberSaveable { mutableStateOf("") }
    var isPasswordVisible by rememberSaveable { mutableStateOf(false) }
    var loginErrorMessage by rememberSaveable { mutableStateOf<String?>(null) }
    var visibleCompleteDialogMessage by remember { mutableStateOf(completeDialogMessage) }
    val coroutineScope = rememberCoroutineScope()
    val authRepository = remember { AuthRepository.getInstance(context) }

    var pendingSocialSignUp by remember { mutableStateOf(pendingNicknameSession) }
    var socialNickname by rememberSaveable { mutableStateOf("") }
    var isSocialNicknameAvailable by remember { mutableStateOf(false) }
    var socialNicknameCheckMessage by remember { mutableStateOf<String?>(null) }

    visibleCompleteDialogMessage?.let { message ->
        CompleteDialog(
            message = message,
            onDismiss = { visibleCompleteDialogMessage = null }
        )
    }

    pendingSocialSignUp?.let { session ->
        SocialSignUpNicknameDialog(
            nickname = socialNickname,
            onNicknameChange = {
                socialNickname = it
                isSocialNicknameAvailable = false
                socialNicknameCheckMessage = null
            },
            onCheckNickname = {
                coroutineScope.launch {
                    authRepository.checkNicknameAvailability(socialNickname)
                        .onSuccess { data ->
                            isSocialNicknameAvailable = data.available
                            socialNicknameCheckMessage = if (data.available) {
                                "사용 가능한 닉네임입니다."
                            } else {
                                "이미 존재하는 닉네임입니다."
                            }
                        }
                        .onFailure { error ->
                            isSocialNicknameAvailable = false
                            socialNicknameCheckMessage = error.toUserMessage("닉네임 확인에 실패했습니다.")
                        }
                }
            },
            nicknameCheckMessage = socialNicknameCheckMessage,
            isSignUpEnabled = isSocialNicknameAvailable,
            onBack = {
                pendingSocialSignUp = null
                socialNickname = ""
                isSocialNicknameAvailable = false
                socialNicknameCheckMessage = null
            },
            onSignUp = {
                coroutineScope.launch {
                    authRepository.completeSocialSignUp(session, socialNickname)
                        .onSuccess {
                            pendingSocialSignUp = null
                            socialNickname = ""
                            isSocialNicknameAvailable = false
                            socialNicknameCheckMessage = null
                            loginErrorMessage = null
                            onLoginSuccess()
                        }
                        .onFailure { error ->
                            Log.e(TAG, "소셜 회원가입 닉네임 설정 실패", error)
                            socialNicknameCheckMessage = error.toUserMessage("닉네임 설정에 실패했습니다.")
                        }
                }
            }
        )
    }

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
                .padding(start = 20.dp, end = 20.dp, top = 60.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Image(
                painter = painterResource(R.drawable.logo_title),
                contentDescription = "title_logo",
                modifier = Modifier.size(width = 212.dp, height = 48.dp),
            )
            Text(text = "식비 절약 챌린지", style = MaterialTheme.typography.bodyMedium, color = HPText)

            Spacer(modifier = Modifier.size(30.dp))

            SocialLoginButton(
                iconRes = R.drawable.login_kakao,
                iconDescription = "kakao_login",
                label = "카카오로 계속하기",
                onClick = {
                    SocialAuthManager.signInWithKakao(context) { result ->
                        result.onSuccess { credential ->
                            coroutineScope.launch {
                                authRepository.loginWithSocial(credential)
                                    .onSuccess { outcome ->
                                        loginErrorMessage = null
                                        if (outcome.isNewUser) {
                                            pendingSocialSignUp = outcome.session
                                        } else {
                                            onLoginSuccess()
                                        }
                                    }
                                    .onFailure { error ->
                                        Log.e(TAG, "카카오 로그인 실패", error)
                                        loginErrorMessage = error.toUserMessage("카카오 로그인에 실패했습니다.")
                                    }
                            }
                        }.onFailure { error ->
                            Log.e(TAG, "카카오 로그인 실패", error)
                            loginErrorMessage = "카카오 로그인에 실패했습니다."
                        }
                    }
                }
            )
            Spacer(modifier = Modifier.size(10.dp))
            SocialLoginButton(
                iconRes = R.drawable.login_google,
                iconDescription = "google_login",
                label = "구글로 계속하기",
                onClick = {
                    coroutineScope.launch {
                        SocialAuthManager.signInWithGoogle(context)
                            .onSuccess { credential ->
                                authRepository.loginWithSocial(credential)
                                    .onSuccess { outcome ->
                                        loginErrorMessage = null
                                        if (outcome.isNewUser) {
                                            pendingSocialSignUp = outcome.session
                                        } else {
                                            onLoginSuccess()
                                        }
                                    }
                                    .onFailure { error ->
                                        Log.e(TAG, "구글 로그인 실패", error)
                                        loginErrorMessage = error.toUserMessage("구글 로그인에 실패했습니다.")
                                    }
                            }
                            .onFailure { error ->
                                Log.e(TAG, "구글 로그인 실패", error)
                                googleSignInErrorMessage(error)?.let { loginErrorMessage = it }
                            }
                    }
                }
            )

            Spacer(modifier = Modifier.size(30.dp))
            OrDivider(text = "또는 이메일로 계속하기")
            Spacer(modifier = Modifier.size(30.dp))

            Column(horizontalAlignment = Alignment.Start) {
                LoginTextField(
                    label = "이메일",
                    value = email,
                    onValueChange = { email = it },
                    placeholder = "hampouch@example.com",
                    keyboardOptions = KeyboardOptions(
                        keyboardType = KeyboardType.Email,
                        imeAction = ImeAction.Next
                    )
                )
                Spacer(modifier = Modifier.size(20.dp))
                LoginTextField(
                    label = "비밀번호",
                    value = password,
                    onValueChange = { password = it },
                    placeholder = "비밀번호를 입력해주세요.",
                    keyboardOptions = KeyboardOptions(
                        keyboardType = KeyboardType.Password,
                        imeAction = ImeAction.Done
                    ),
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
            }
            loginErrorMessage?.let { message ->
                Spacer(modifier = Modifier.size(8.dp))
                Text(
                    message,
                    style = MaterialTheme.typography.bodyMedium,
                    color = HPSub
                )
            }
            Spacer(modifier = Modifier.size(30.dp))
            Button(
                onClick = {
                    coroutineScope.launch {
                        authRepository.login(email, password)
                            .onSuccess {
                                loginErrorMessage = null
                                onLoginSuccess()
                            }
                            .onFailure { error ->
                                Log.e(TAG, "이메일 로그인 실패", error)
                                loginErrorMessage = error.toUserMessage("로그인에 실패했습니다.")
                            }
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                shape = RoundedCornerShape(10.dp),
                colors = ButtonDefaults.buttonColors(containerColor = HPMain),
            ) {
                Text("로그인", style = MaterialTheme.typography.bodyLarge)
            }

            Spacer(modifier = Modifier.size(10.dp))
            FooterLinkRow(
                text = "계정이 없으신가요?",
                linkText = "회원가입",
                onClick = onNavigateToSignUp
            )
            Spacer(modifier = Modifier.size(10.dp))
            FooterLinkRow(
                text = "비밀번호를 잊으셨나요?",
                linkText = "비밀번호 재설정",
                onClick = onNavigateToResetPassword
            )
        }
    }
}

@Composable
private fun SocialLoginButton(
    iconRes: Int,
    iconDescription: String,
    label: String,
    onClick: () -> Unit
) {
    Button(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth(),
        colors = ButtonDefaults.buttonColors(containerColor = HPWhite)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.Start,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Image(
                painter = painterResource(iconRes),
                contentDescription = iconDescription,
                modifier = Modifier.size(35.dp)
            )
            Spacer(modifier = Modifier.size(13.dp))
            Text(label, color = HPBlack, style = MaterialTheme.typography.bodyMedium)
        }
    }
}

@Preview
@Composable
fun LoginScreenPreview() {
    HampouchTheme {
        LoginScreen()
    }
}
