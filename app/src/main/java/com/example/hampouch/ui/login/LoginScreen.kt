package com.example.hampouch.ui.login

import android.annotation.SuppressLint
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.hampouch.R
import com.example.hampouch.ui.theme.Body16Bold
import com.example.hampouch.ui.theme.HPBlack
import com.example.hampouch.ui.theme.HPGray5
import com.example.hampouch.ui.theme.HPMain
import com.example.hampouch.ui.theme.HPSub
import com.example.hampouch.ui.theme.HPSub4
import com.example.hampouch.ui.theme.HPText
import com.example.hampouch.ui.theme.HPWhite
import com.example.hampouch.ui.theme.HampouchTheme

@SuppressLint("UnusedMaterial3ScaffoldPaddingParameter")
@Composable
fun LoginScreen() {
    var email by rememberSaveable { mutableStateOf("") }
    var password by rememberSaveable { mutableStateOf("") }
    var isPasswordVisible by rememberSaveable { mutableStateOf(false) }
    var showLoginError by rememberSaveable { mutableStateOf(false) }

    Scaffold(topBar = {}, bottomBar = {}, containerColor = HPSub4) { innerPadding ->
        Column(
            modifier = Modifier
                .padding(innerPadding)
                .padding(start = 20.dp, end = 20.dp, top = 60.dp)
                .fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Image(
                painter = painterResource(R.drawable.title_logo),
                contentDescription = "title_logo",
                modifier = Modifier.size(width = 212.dp, height = 48.dp),
            )
            Text(text = "식비 절약 챌린지", style = MaterialTheme.typography.bodyMedium, color = HPText)

            Spacer(modifier = Modifier.size(30.dp))

            SocialLoginButton(
                iconRes = R.drawable.kakao_login,
                iconDescription = "kakao_login",
                label = "카카오로 계속하기",
                onClick = {}
            )
            Spacer(modifier = Modifier.size(10.dp))
            SocialLoginButton(
                iconRes = R.drawable.google_login,
                iconDescription = "google_login",
                label = "구글로 계속하기",
                onClick = {}
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
                                        R.drawable.eye_login
                                    } else {
                                        R.drawable.no_eye_login
                                    }
                                ),
                                contentDescription = if (isPasswordVisible) "Hide password" else "Show password",
                            )
                        }
                    }
                )
            }
            if (showLoginError) {
                Spacer(modifier = Modifier.size(8.dp))
                Text(
                    "이메일 및 비밀번호를 다시 확인해주세요.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = HPSub
                )
            }
            Spacer(modifier = Modifier.size(30.dp))
            Button(
                onClick = {
                    // TODO: 서버 연결 후 다시 로직 작성
                    showLoginError = true
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
                onClick = {}
            )
            Spacer(modifier = Modifier.size(10.dp))
            FooterLinkRow(
                text = "비밀번호를 잊으셨나요?",
                linkText = "비밀번호 재설정",
                onClick = {}
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

@Composable
private fun OrDivider(text: String) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        val dash = PathEffect.dashPathEffect(floatArrayOf(10f, 8f))

        Canvas(
            Modifier
                .weight(1f)
                .height(1.dp)
        ) {
            drawLine(HPText, Offset.Zero, Offset(size.width, 0f), pathEffect = dash)
        }
        Text(
            text,
            modifier = Modifier.padding(horizontal = 8.dp),
            style = MaterialTheme.typography.bodyMedium,
            color = HPText
        )
        Canvas(
            Modifier
                .weight(1f)
                .height(1.dp)
        ) {
            drawLine(HPText, Offset.Zero, Offset(size.width, 0f), pathEffect = dash)
        }
    }
}

@Composable
private fun LoginTextField(
    label: String,
    value: String,
    onValueChange: (String) -> Unit,
    placeholder: String,
    keyboardOptions: KeyboardOptions,
    visualTransformation: VisualTransformation = VisualTransformation.None,
    trailingIcon: @Composable (() -> Unit)? = null
) {
    Text(label, style = MaterialTheme.typography.bodyMedium, color = HPText)
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        shape = RoundedCornerShape(10.dp),
        colors = OutlinedTextFieldDefaults.colors(
            unfocusedBorderColor = HPGray5,
            unfocusedContainerColor = HPWhite,
            focusedContainerColor = HPWhite,
        ),
        placeholder = {
            Text(text = placeholder, color = HPGray5, style = MaterialTheme.typography.bodyMedium)
        },
        visualTransformation = visualTransformation,
        keyboardOptions = keyboardOptions,
        trailingIcon = trailingIcon,
        singleLine = true,
        modifier = Modifier
            .fillMaxWidth()
            .height(48.dp)
    )
}

@Composable
private fun FooterLinkRow(
    text: String,
    linkText: String,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier.height(24.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(text, style = MaterialTheme.typography.bodyMedium, color = HPText)
        TextButton(
            onClick = onClick,
            contentPadding = PaddingValues(horizontal = 8.dp, vertical = 0.dp),
            colors = ButtonDefaults.textButtonColors(contentColor = HPSub)
        ) {
            Text(linkText, style = Body16Bold, color = HPSub)
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
