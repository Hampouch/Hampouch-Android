package com.example.hampouch.ui.mypage

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsFocusedAsState
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
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
import com.example.hampouch.ui.common.OrDivider
import com.example.hampouch.ui.mypage.components.MyPageMainTopBar
import com.example.hampouch.ui.theme.HPBlack
import com.example.hampouch.ui.theme.HPGray2
import com.example.hampouch.ui.theme.HPGray5
import com.example.hampouch.ui.theme.HPMain
import com.example.hampouch.ui.theme.HPText
import com.example.hampouch.ui.theme.HPWhite
import com.example.hampouch.ui.theme.HampouchTheme

private enum class PasswordField { CURRENT, NEW, CONFIRM }

@Composable
fun ChangePasswordScreen(
    email: String,
    onBackClick: () -> Unit,
    onSubmitSuccess: () -> Unit,
    modifier: Modifier = Modifier,
    onNotificationClick: () -> Unit = {}
) {
    var emailInput by rememberSaveable { mutableStateOf(email) }
    var isEmailFieldTouched by rememberSaveable { mutableStateOf(false) }
    val emailInteractionSource = remember { MutableInteractionSource() }
    val isEmailFieldFocused by emailInteractionSource.collectIsFocusedAsState()

    LaunchedEffect(isEmailFieldFocused) {
        if (isEmailFieldFocused && !isEmailFieldTouched) {
            emailInput = ""
            isEmailFieldTouched = true
        }
    }

    var currentPassword by rememberSaveable { mutableStateOf("") }
    var newPassword by rememberSaveable { mutableStateOf("") }
    var confirmPassword by rememberSaveable { mutableStateOf("") }
    var isCurrentPasswordVisible by rememberSaveable { mutableStateOf(false) }
    var isNewPasswordVisible by rememberSaveable { mutableStateOf(false) }
    var isConfirmPasswordVisible by rememberSaveable { mutableStateOf(false) }
    var errorField by rememberSaveable { mutableStateOf<PasswordField?>(null) }

    val isNewPasswordValid = newPassword.length >= 8 &&
        newPassword.any { it.isLetter() } &&
        newPassword.any { it.isDigit() }

    val errorMessage = stringResource(R.string.change_password_error)

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(HPGray2)
    ) {
        MyPageMainTopBar(
            title = stringResource(R.string.change_password_title),
            onBackClick = onBackClick,
            onNotificationClick = onNotificationClick,
            modifier = Modifier.padding(start = 4.dp, end = 20.dp)
        )
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .imePadding()
                .padding(horizontal = 20.dp, vertical = 12.dp)
        ) {
            OrDivider(text = stringResource(R.string.change_password_user_info_section))
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = stringResource(R.string.change_password_email_label),
                style = MaterialTheme.typography.bodyMedium,
                color = HPText
            )
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp)
                    .clip(RoundedCornerShape(10.dp))
                    .background(HPWhite)
                    .border(1.dp, HPGray5, RoundedCornerShape(10.dp))
                    .padding(horizontal = 12.dp),
                contentAlignment = Alignment.CenterStart
            ) {
                BasicTextField(
                    value = emailInput,
                    onValueChange = { emailInput = it },
                    modifier = Modifier.fillMaxWidth(),
                    textStyle = MaterialTheme.typography.bodyMedium.copy(
                        color = if (isEmailFieldTouched) HPBlack else HPText
                    ),
                    singleLine = true,
                    interactionSource = emailInteractionSource,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email, imeAction = ImeAction.Next)
                )
            }

            Spacer(modifier = Modifier.height(24.dp))
            OrDivider(text = stringResource(R.string.change_password_form_section))
            Spacer(modifier = Modifier.height(16.dp))

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
            if (errorField == PasswordField.CURRENT) {
                FieldMessage(errorMessage)
            }

            Spacer(modifier = Modifier.height(20.dp))
            LoginTextField(
                label = stringResource(R.string.change_password_new_label),
                value = newPassword,
                onValueChange = { newPassword = it },
                placeholder = stringResource(R.string.change_password_new_placeholder),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password, imeAction = ImeAction.Next),
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
            if (errorField == PasswordField.NEW) {
                FieldMessage(errorMessage)
            }

            Spacer(modifier = Modifier.height(20.dp))
            LoginTextField(
                label = stringResource(R.string.change_password_new_confirm_label),
                value = confirmPassword,
                onValueChange = { confirmPassword = it },
                placeholder = stringResource(R.string.change_password_new_placeholder),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password, imeAction = ImeAction.Done),
                visualTransformation = if (isConfirmPasswordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                trailingIcon = {
                    IconButton(onClick = { isConfirmPasswordVisible = !isConfirmPasswordVisible }) {
                        Image(
                            modifier = Modifier.height(20.dp),
                            painter = painterResource(
                                id = if (isConfirmPasswordVisible) R.drawable.login_eye else R.drawable.login_no_eye
                            ),
                            contentDescription = null
                        )
                    }
                }
            )
            if (errorField == PasswordField.CONFIRM) {
                FieldMessage(errorMessage)
            }

            Spacer(modifier = Modifier.height(30.dp))
            Button(
                onClick = {
                    errorField = when {
                        currentPassword.isBlank() -> PasswordField.CURRENT
                        !isNewPasswordValid -> PasswordField.NEW
                        newPassword != confirmPassword -> PasswordField.CONFIRM
                        else -> null
                    }
                    if (errorField == null) onSubmitSuccess()
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

@Preview(showBackground = true, name = "13. 비밀번호 변경")
@Composable
private fun ChangePasswordScreenPreview() {
    HampouchTheme {
        ChangePasswordScreen(email = "hampouch@example.com", onBackClick = {}, onSubmitSuccess = {})
    }
}
