package com.example.hampouch.ui.mypage

import android.widget.Toast
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
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.Saver
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import com.example.hampouch.R
import com.example.hampouch.domain.model.ApiException
import com.example.hampouch.domain.model.toUserMessage
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
import kotlinx.coroutines.launch

private enum class PasswordField { CURRENT, NEW, CONFIRM }

data class ChangePasswordActions(
    val onBackClick: () -> Unit,
    val onSubmitSuccess: () -> Unit,
    val onNotificationClick: () -> Unit
)

private class PasswordFieldState(initialValue: String = "", initialVisible: Boolean = false) {
    var value by mutableStateOf(initialValue)
    var isVisible by mutableStateOf(initialVisible)

    fun toggleVisibility() {
        isVisible = !isVisible
    }
}

private val PasswordFieldStateSaver = Saver<PasswordFieldState, List<Any>>(
    save = { listOf(it.value, it.isVisible) },
    restore = { PasswordFieldState(it[0] as String, it[1] as Boolean) }
)

@Composable
private fun rememberPasswordFieldState(): PasswordFieldState =
    rememberSaveable(saver = PasswordFieldStateSaver) { PasswordFieldState() }

private fun isValidNewPassword(password: String): Boolean =
    password.length >= 8 && password.any { it.isLetter() } && password.any { it.isDigit() }

private fun validatePasswordFields(
    currentPassword: String,
    isNewPasswordValid: Boolean,
    newPassword: String,
    confirmPassword: String
): PasswordField? = when {
    currentPassword.isBlank() -> PasswordField.CURRENT
    !isNewPasswordValid -> PasswordField.NEW
    newPassword != confirmPassword -> PasswordField.CONFIRM
    else -> null
}

@Composable
fun ChangePasswordScreen(
    email: String,
    actions: ChangePasswordActions,
    modifier: Modifier = Modifier,
    viewModel: ChangePasswordViewModel = hiltViewModel()
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .background(HPGray2)
    ) {
        MyPageMainTopBar(
            title = stringResource(R.string.change_password_title),
            onBackClick = actions.onBackClick,
            onNotificationClick = actions.onNotificationClick,
            modifier = Modifier.padding(start = 4.dp, end = 20.dp)
        )
        ChangePasswordForm(email = email, viewModel = viewModel, onSubmitSuccess = actions.onSubmitSuccess)
    }
}

@Composable
private fun ChangePasswordForm(
    email: String,
    viewModel: ChangePasswordViewModel,
    onSubmitSuccess: () -> Unit
) {
    var isSubmitting by rememberSaveable { mutableStateOf(false) }
    var currentPasswordErrorMessage by remember { mutableStateOf<String?>(null) }
    var errorField by rememberSaveable { mutableStateOf<PasswordField?>(null) }
    val currentPasswordField = rememberPasswordFieldState()
    val newPasswordField = rememberPasswordFieldState()
    val confirmPasswordField = rememberPasswordFieldState()
    val isNewPasswordValid = isValidNewPassword(newPasswordField.value)

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .imePadding()
            .padding(horizontal = 20.dp, vertical = 12.dp)
    ) {
        OrDivider(text = stringResource(R.string.change_password_user_info_section))
        Spacer(modifier = Modifier.height(16.dp))
        ChangePasswordEmailField(email)

        Spacer(modifier = Modifier.height(24.dp))
        OrDivider(text = stringResource(R.string.change_password_form_section))
        Spacer(modifier = Modifier.height(16.dp))

        ChangePasswordFieldsSection(
            currentPassword = currentPasswordField,
            newPassword = newPasswordField,
            confirmPassword = confirmPasswordField,
            errorField = errorField,
            currentPasswordErrorMessage = currentPasswordErrorMessage
        )

        Spacer(modifier = Modifier.height(30.dp))
        ChangePasswordSubmitButton(
            values = ChangePasswordFormValues(
                currentPasswordField.value,
                newPasswordField.value,
                confirmPasswordField.value,
                isNewPasswordValid
            ),
            isSubmitting = isSubmitting,
            mutators = ChangePasswordFormMutators(
                onSubmittingChange = { isSubmitting = it },
                onErrorFieldChange = { errorField = it },
                onCurrentPasswordErrorChange = { currentPasswordErrorMessage = it }
            ),
            viewModel = viewModel,
            onSubmitSuccess = onSubmitSuccess
        )
    }
}

@Composable
private fun ChangePasswordFieldsSection(
    currentPassword: PasswordFieldState,
    newPassword: PasswordFieldState,
    confirmPassword: PasswordFieldState,
    errorField: PasswordField?,
    currentPasswordErrorMessage: String?
) {
    val errorMessage = stringResource(R.string.change_password_error)

    PasswordInputField(
        spec = PasswordFieldSpec(
            label = stringResource(R.string.change_password_current_label),
            placeholder = stringResource(R.string.change_password_current_placeholder),
            imeAction = ImeAction.Next,
            errorMessage = if (errorField == PasswordField.CURRENT) {
                currentPasswordErrorMessage ?: errorMessage
            } else {
                null
            }
        ),
        state = currentPassword
    )

    Spacer(modifier = Modifier.height(20.dp))
    PasswordInputField(
        spec = PasswordFieldSpec(
            label = stringResource(R.string.change_password_new_label),
            placeholder = stringResource(R.string.change_password_new_placeholder),
            imeAction = ImeAction.Next,
            errorMessage = if (errorField == PasswordField.NEW) errorMessage else null
        ),
        state = newPassword
    )

    Spacer(modifier = Modifier.height(20.dp))
    PasswordInputField(
        spec = PasswordFieldSpec(
            label = stringResource(R.string.change_password_new_confirm_label),
            placeholder = stringResource(R.string.change_password_new_placeholder),
            imeAction = ImeAction.Done,
            errorMessage = if (errorField == PasswordField.CONFIRM) errorMessage else null
        ),
        state = confirmPassword
    )
}

private data class ChangePasswordFormValues(
    val currentPassword: String,
    val newPassword: String,
    val confirmPassword: String,
    val isNewPasswordValid: Boolean
)

private class ChangePasswordFormMutators(
    val onSubmittingChange: (Boolean) -> Unit,
    val onErrorFieldChange: (PasswordField?) -> Unit,
    val onCurrentPasswordErrorChange: (String?) -> Unit
)

@Composable
private fun ChangePasswordSubmitButton(
    values: ChangePasswordFormValues,
    isSubmitting: Boolean,
    mutators: ChangePasswordFormMutators,
    viewModel: ChangePasswordViewModel,
    onSubmitSuccess: () -> Unit
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    val errorMessage = stringResource(R.string.change_password_error)
    val genericFailureMessage = stringResource(R.string.change_password_failed)

    Button(
        onClick = {
            mutators.onCurrentPasswordErrorChange(null)
            val errorField = validatePasswordFields(
                values.currentPassword,
                values.isNewPasswordValid,
                values.newPassword,
                values.confirmPassword
            )
            mutators.onErrorFieldChange(errorField)
            if (errorField == null) {
                mutators.onSubmittingChange(true)
                coroutineScope.launch {
                    viewModel.changePassword(values.currentPassword, values.newPassword)
                        .onSuccess {
                            mutators.onSubmittingChange(false)
                            onSubmitSuccess()
                        }
                        .onFailure { error ->
                            mutators.onSubmittingChange(false)
                            if ((error as? ApiException)?.code == "USER_CURRENT_PASSWORD_MISMATCH") {
                                mutators.onErrorFieldChange(PasswordField.CURRENT)
                                mutators.onCurrentPasswordErrorChange(error.toUserMessage(errorMessage))
                            } else {
                                Toast.makeText(
                                    context,
                                    error.toUserMessage(genericFailureMessage),
                                    Toast.LENGTH_SHORT
                                ).show()
                            }
                        }
                }
            }
        },
        enabled = !isSubmitting,
        modifier = Modifier
            .fillMaxWidth()
            .height(56.dp),
        shape = RoundedCornerShape(10.dp),
        colors = ButtonDefaults.buttonColors(containerColor = HPMain)
    ) {
        Text(stringResource(R.string.change_password_submit), style = MaterialTheme.typography.bodyLarge)
    }
}

@Composable
private fun ChangePasswordEmailField(email: String) {
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
}

private data class PasswordFieldSpec(
    val label: String,
    val placeholder: String,
    val imeAction: ImeAction,
    val errorMessage: String?
)

@Composable
private fun PasswordInputField(spec: PasswordFieldSpec, state: PasswordFieldState) {
    LoginTextField(
        label = spec.label,
        value = state.value,
        onValueChange = { state.value = it },
        placeholder = spec.placeholder,
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password, imeAction = spec.imeAction),
        visualTransformation = if (state.isVisible) VisualTransformation.None else PasswordVisualTransformation(),
        trailingIcon = {
            IconButton(onClick = { state.toggleVisibility() }) {
                Image(
                    modifier = Modifier.height(20.dp),
                    painter = painterResource(
                        id = if (state.isVisible) R.drawable.login_eye else R.drawable.login_no_eye
                    ),
                    contentDescription = null
                )
            }
        }
    )
    spec.errorMessage?.let { FieldMessage(it) }
}

@Preview(showBackground = true, name = "13. 비밀번호 변경")
@Composable
private fun ChangePasswordScreenPreview() {
    HampouchTheme {
        ChangePasswordScreen(
            email = "hampouch@example.com",
            actions = ChangePasswordActions(onBackClick = {}, onSubmitSuccess = {}, onNotificationClick = {})
        )
    }
}
