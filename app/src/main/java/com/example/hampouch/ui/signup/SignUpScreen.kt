package com.example.hampouch.ui.signup

import android.annotation.SuppressLint
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
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
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.hampouch.R
import com.example.hampouch.ui.common.FieldMessage
import com.example.hampouch.ui.common.FooterLinkRow
import com.example.hampouch.ui.common.LoginTextField
import com.example.hampouch.ui.common.OrDivider
import com.example.hampouch.ui.theme.Body16Bold
import com.example.hampouch.ui.theme.HPBlack
import com.example.hampouch.ui.theme.HPMain
import com.example.hampouch.ui.theme.HPSub3
import com.example.hampouch.ui.theme.HPSub4
import com.example.hampouch.ui.theme.HPText
import com.example.hampouch.ui.theme.HampouchTheme

@SuppressLint("UnusedMaterial3ScaffoldPaddingParameter")
@Composable
fun SignUpScreen(
    onSignUpSuccess: () -> Unit = {},
    onNavigateToLogin: () -> Unit = {}
) {
    var email by rememberSaveable { mutableStateOf("") }
    var emailCode by rememberSaveable { mutableStateOf("") }
    var password by rememberSaveable { mutableStateOf("") }
    var nickname by rememberSaveable { mutableStateOf("") }
    var isPasswordVisible by rememberSaveable { mutableStateOf(false) }
    var isEmailAvailable by remember { mutableStateOf<Boolean?>(null) }
    var isEmailCodeVerified by remember { mutableStateOf<Boolean?>(null) }
    var isNicknameAvailable by remember { mutableStateOf<Boolean?>(null) }
    var showPasswordError by rememberSaveable { mutableStateOf(false) }
    var isTermsChecked by rememberSaveable { mutableStateOf(false) }
    var isPrivacyChecked by rememberSaveable { mutableStateOf(false) }
    var isMarketingChecked by rememberSaveable { mutableStateOf(false) }
    var showTermsError by rememberSaveable { mutableStateOf(false) }
    val isPasswordValid = password.length >= 8 &&
        password.any { it.isLetter() } &&
        password.any { it.isDigit() }
    val areRequiredTermsChecked = isTermsChecked && isPrivacyChecked

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
            OrDivider(text = "이메일 회원가입")
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
                    ),
                    onCheckClick = {
                        // TODO: 서버 연결 후 실제 이메일 중복확인 로직 작성
                        isEmailAvailable = isEmailAvailable != true
                    }
                )
                if (isEmailAvailable != null) {
                    FieldMessage(
                        if (isEmailAvailable == true) "인증번호가 발송되었습니다." else "이미 가입된 이메일입니다."
                    )
                }
                Spacer(modifier = Modifier.size(20.dp))
                LoginTextField(
                    label = "인증번호",
                    value = emailCode,
                    onValueChange = { emailCode = it },
                    placeholder = "인증번호를 입력해주세요.",
                    keyboardOptions = KeyboardOptions(
                        keyboardType = KeyboardType.Text,
                        imeAction = ImeAction.Next
                    ),
                    onCheckClick = {
                        // TODO: 서버 연결 후 실제 인증번호 확인 로직 작성
                        isEmailCodeVerified = isEmailCodeVerified != true
                    }
                )
                if (isEmailCodeVerified != null) {
                    FieldMessage(
                        if (isEmailCodeVerified == true) "확인되었습니다." else "인증번호를 다시 확인해주세요."
                    )
                }
                Spacer(modifier = Modifier.size(20.dp))
                LoginTextField(
                    label = "비밀번호",
                    value = password,
                    onValueChange = { password = it },
                    placeholder = "8자 이상, 영문 + 숫자 조합",
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
                if (showPasswordError) {
                    FieldMessage("비밀번호를 다시 입력해주세요.")
                }
                Spacer(modifier = Modifier.size(20.dp))
                LoginTextField(
                    label = "닉네임",
                    value = nickname,
                    onValueChange = { nickname = it },
                    placeholder = "닉네임을 입력해주세요.",
                    keyboardOptions = KeyboardOptions(
                        keyboardType = KeyboardType.Text,
                        imeAction = ImeAction.Done
                    ),
                    onCheckClick = {
                        // TODO: 서버 연결 후 실제 닉네임 중복확인 로직 작성
                        isNicknameAvailable = isNicknameAvailable != true
                    }
                )
                if (isNicknameAvailable != null) {
                    FieldMessage(
                        if (isNicknameAvailable == true) "사용가능한 닉네임입니다." else "이미 존재하는 닉네임입니다."
                    )
                }

                Spacer(modifier = Modifier.size(30.dp))
                TermsAgreementSection(
                    isTermsChecked = isTermsChecked,
                    onTermsCheckedChange = { isTermsChecked = it },
                    isPrivacyChecked = isPrivacyChecked,
                    onPrivacyCheckedChange = { isPrivacyChecked = it },
                    isMarketingChecked = isMarketingChecked,
                    onMarketingCheckedChange = { isMarketingChecked = it }
                )
                if (showTermsError) {
                    FieldMessage("필수 항목을 체크해주세요.")
                }
            }



            Spacer(modifier = Modifier.size(30.dp))
            Button(
                onClick = {
                    // TODO: 서버 연결 후 실제 회원가입 API 호출로 교체
                    showPasswordError = !isPasswordValid
                    showTermsError = !areRequiredTermsChecked
                    if (isPasswordValid && areRequiredTermsChecked) {
                        onSignUpSuccess()
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                shape = RoundedCornerShape(10.dp),
                colors = ButtonDefaults.buttonColors(containerColor = HPMain),
            ) {
                Text("가입하고 시작하기", style = MaterialTheme.typography.bodyLarge)
            }

            Spacer(modifier = Modifier.size(10.dp))
            FooterLinkRow(
                text = "이미 계정이 있으신가요?",
                linkText = "로그인",
                onClick = onNavigateToLogin
            )
        }
    }
}

@Composable
private fun TermsAgreementSection(
    isTermsChecked: Boolean,
    onTermsCheckedChange: (Boolean) -> Unit,
    isPrivacyChecked: Boolean,
    onPrivacyCheckedChange: (Boolean) -> Unit,
    isMarketingChecked: Boolean,
    onMarketingCheckedChange: (Boolean) -> Unit
) {
    val isAllChecked = isTermsChecked && isPrivacyChecked && isMarketingChecked
    val onToggleAll: () -> Unit = {
        val next = !isAllChecked
        onTermsCheckedChange(next)
        onPrivacyCheckedChange(next)
        onMarketingCheckedChange(next)
    }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(HPSub4)
            .padding(16.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clickable(onClick = onToggleAll),
            verticalAlignment = Alignment.CenterVertically
        ) {
            TermsCheckIcon(checked = isAllChecked)
            Spacer(modifier = Modifier.size(10.dp))
            Text("전체 동의", style = Body16Bold, color = HPBlack)
        }
        Spacer(modifier = Modifier.size(12.dp))
        HorizontalDivider(color = HPBlack, thickness = 1.dp)
        Spacer(modifier = Modifier.size(12.dp))
        TermsCheckItem(
            checked = isTermsChecked,
            onCheckedChange = onTermsCheckedChange,
            prefix = "[필수]",
            label = "서비스 이용 약관"
        )
        Spacer(modifier = Modifier.size(8.dp))
        TermsCheckItem(
            checked = isPrivacyChecked,
            onCheckedChange = onPrivacyCheckedChange,
            prefix = "[필수]",
            label = "개인정보 처리방침"
        )
        Spacer(modifier = Modifier.size(8.dp))
        TermsCheckItem(
            checked = isMarketingChecked,
            onCheckedChange = onMarketingCheckedChange,
            prefix = "[선택]",
            label = "마케팅 정보 수신 동의"
        )
    }
}

@Composable
private fun TermsCheckItem(
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
    prefix: String,
    label: String
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onCheckedChange(!checked) },
        verticalAlignment = Alignment.CenterVertically
    ) {
        TermsCheckIcon(checked = checked)
        Spacer(modifier = Modifier.size(10.dp))
        Text("$prefix $label", style = MaterialTheme.typography.bodyMedium, color = HPText)
    }
}

@Composable
private fun TermsCheckIcon(checked: Boolean) {
    Image(
        painter = painterResource(
            id = if (checked) R.drawable.icon_checked else R.drawable.icon_unchecked
        ),
        contentDescription = null,
        modifier = Modifier.size(24.dp)
    )
}

@Preview
@Composable
fun SignUpScreenPreview() {
    HampouchTheme {
        SignUpScreen()
    }
}
