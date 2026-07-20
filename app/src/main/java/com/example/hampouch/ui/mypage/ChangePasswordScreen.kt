package com.example.hampouch.ui.mypage

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.hampouch.R
import com.example.hampouch.ui.common.FieldMessage
import com.example.hampouch.ui.common.LoginTextField
import com.example.hampouch.ui.mypage.components.MyPageDetailTopBar
import com.example.hampouch.ui.theme.HPGray2
import com.example.hampouch.ui.theme.HPMain
import com.example.hampouch.ui.theme.HampouchTheme

@Composable
fun ChangePasswordScreen(
    onBackClick: () -> Unit,
    onSubmitSuccess: () -> Unit,
    modifier: Modifier = Modifier
) {
    var currentPassword by rememberSaveable { mutableStateOf("") }
    var newPassword by rememberSaveable { mutableStateOf("") }
    var isCurrentPasswordVisible by rememberSaveable { mutableStateOf(false) }
    var isNewPasswordVisible by rememberSaveable { mutableStateOf(false) }
    var showError by rememberSaveable { mutableStateOf(false) }

    val isNewPasswordValid = newPassword.length >= 8 &&
        newPassword.any { it.isLetter() } &&
        newPassword.any { it.isDigit() }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(HPGray2)
    ) {
        MyPageDetailTopBar(
            title = stringResource(R.string.change_password_title),
            onBackClick = onBackClick,
            showMoreMenu = false,
            modifier = Modifier.padding(horizontal = 8.dp)
        )
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp, vertical = 12.dp)
        ) {
            LoginTextField(
                label = stringResource(R.string.change_password_current_label),
                value = currentPassword,
                onValueChange = { currentPassword = it },
                placeholder = stringResource(R.string.change_password_current_placeholder),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password, imeAction = ImeAction.Next),
                visualTransformation = if (isCurrentPasswordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                trailingIcon = {
                    IconButton(onClick = { isCurrentPasswordVisible = !isCurrentPasswordVisible }) {
                        Image(
                            modifier = Modifier.height(20.dp),
                            painter = painterResource(
                                id = if (isCurrentPasswordVisible) R.drawable.login_eye else R.drawable.login_no_eye
                            ),
                            contentDescription = null
                        )
                    }
                }
            )
            Spacer(modifier = Modifier.height(20.dp))
            LoginTextField(
                label = stringResource(R.string.change_password_new_label),
                value = newPassword,
                onValueChange = { newPassword = it },
                placeholder = stringResource(R.string.change_password_new_placeholder),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password, imeAction = ImeAction.Done),
                visualTransformation = if (isNewPasswordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                trailingIcon = {
                    IconButton(onClick = { isNewPasswordVisible = !isNewPasswordVisible }) {
                        Image(
                            modifier = Modifier.height(20.dp),
                            painter = painterResource(
                                id = if (isNewPasswordVisible) R.drawable.login_eye else R.drawable.login_no_eye
                            ),
                            contentDescription = null
                        )
                    }
                }
            )
            if (showError) {
                FieldMessage(stringResource(R.string.change_password_error))
            }

            Spacer(modifier = Modifier.height(30.dp))
            Button(
                onClick = {
                    showError = !isNewPasswordValid
                    if (isNewPasswordValid) onSubmitSuccess()
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                shape = RoundedCornerShape(10.dp),
                colors = ButtonDefaults.buttonColors(containerColor = HPMain)
            ) {
                Text(stringResource(R.string.change_password_submit), style = MaterialTheme.typography.bodyLarge)
            }
        }
    }
}

@Preview(showBackground = true, name = "비밀번호 변경")
@Composable
private fun ChangePasswordScreenPreview() {
    HampouchTheme {
        ChangePasswordScreen(onBackClick = {}, onSubmitSuccess = {})
    }
}
