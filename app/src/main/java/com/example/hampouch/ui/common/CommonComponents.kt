package com.example.hampouch.ui.common

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import com.example.hampouch.R
import com.example.hampouch.ui.notification.NotificationStore
import com.example.hampouch.ui.theme.Body16Bold
import com.example.hampouch.ui.theme.HPBlack
import com.example.hampouch.ui.theme.HPGray4
import com.example.hampouch.ui.theme.HPGray5
import com.example.hampouch.ui.theme.HPStatusInProgressText
import com.example.hampouch.ui.theme.HPSub
import com.example.hampouch.ui.theme.HPText
import com.example.hampouch.ui.theme.HPWhite
import kotlinx.coroutines.delay

@Composable
fun rememberCountdownSeconds(expiresAtMillis: Long?): Int? {
    var remainingSeconds by remember { mutableStateOf<Int?>(null) }
    LaunchedEffect(expiresAtMillis) {
        if (expiresAtMillis == null) {
            remainingSeconds = null
            return@LaunchedEffect
        }
        while (true) {
            val remaining = ((expiresAtMillis - System.currentTimeMillis()) / 1000).toInt()
            remainingSeconds = remaining.coerceAtLeast(0)
            if (remaining <= 0) break
            delay(1000)
        }
    }
    return remainingSeconds
}

fun formatRemainingTime(totalSeconds: Int): String {
    val minutes = totalSeconds / 60
    val seconds = totalSeconds % 60
    return "%d:%02d".format(minutes, seconds)
}

@Composable
fun OrDivider(text: String) {
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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LoginTextField(
    label: String,
    value: String,
    onValueChange: (String) -> Unit,
    placeholder: String,
    keyboardOptions: KeyboardOptions,
    visualTransformation: VisualTransformation = VisualTransformation.None,
    trailingIcon: @Composable (() -> Unit)? = null,
    onCheckClick: (() -> Unit)? = null,
    isCheckEnabled: Boolean = true,
    isValid: Boolean = false
) {
    val interactionSource = remember { MutableInteractionSource() }
    val colors = if (isValid) {
        OutlinedTextFieldDefaults.colors(
            unfocusedBorderColor = HPStatusInProgressText,
            focusedBorderColor = HPStatusInProgressText,
            unfocusedContainerColor = HPWhite,
            focusedContainerColor = HPWhite,
        )
    } else {
        OutlinedTextFieldDefaults.colors(
            unfocusedBorderColor = HPGray5,
            unfocusedContainerColor = HPWhite,
            focusedContainerColor = HPWhite,
        )
    }

    Text(label, style = MaterialTheme.typography.bodyMedium, color = HPText)
    Row(verticalAlignment = Alignment.CenterVertically) {
        BasicTextField(
            value = value,
            onValueChange = onValueChange,
            modifier = Modifier
                .weight(1f)
                .height(48.dp),
            textStyle = MaterialTheme.typography.bodyMedium.copy(color = HPBlack),
            visualTransformation = visualTransformation,
            keyboardOptions = keyboardOptions,
            singleLine = true,
            interactionSource = interactionSource,
            decorationBox = { innerTextField ->
                OutlinedTextFieldDefaults.DecorationBox(
                    value = value,
                    innerTextField = innerTextField,
                    enabled = true,
                    singleLine = true,
                    visualTransformation = visualTransformation,
                    interactionSource = interactionSource,
                    placeholder = {
                        Text(
                            text = placeholder,
                            color = HPGray5,
                            style = MaterialTheme.typography.bodyMedium
                        )
                    },
                    trailingIcon = trailingIcon,
                    colors = colors,
                    contentPadding = OutlinedTextFieldDefaults.contentPadding(
                        start = 12.dp,
                        top = 8.dp,
                        end = 12.dp,
                        bottom = 8.dp
                    ),
                    container = {
                        OutlinedTextFieldDefaults.Container(
                            enabled = true,
                            isError = false,
                            interactionSource = interactionSource,
                            colors = colors,
                            shape = RoundedCornerShape(10.dp)
                        )
                    }
                )
            }
        )
        if (onCheckClick != null) {
            Spacer(modifier = Modifier.size(8.dp))
            CheckButton(onClick = onCheckClick, enabled = isCheckEnabled)
        }
    }
}

@Composable
fun CheckButton(onClick: () -> Unit, enabled: Boolean = true) {
    Button(
        onClick = onClick,
        enabled = enabled,
        modifier = Modifier.height(48.dp),
        shape = RoundedCornerShape(10.dp),
        colors = ButtonDefaults.buttonColors(containerColor = HPWhite),
        contentPadding = PaddingValues(horizontal = 16.dp)
    ) {
        Text(
            "확인",
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.Bold,
            color = HPBlack
        )
    }
}

@Composable
fun FieldMessage(text: String) {
    Spacer(modifier = Modifier.size(8.dp))
    Text(text, style = MaterialTheme.typography.bodyMedium, color = HPSub)
}

@Composable
fun FooterLinkRow(
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

@Composable
fun ReasonTagAndAmountColumn(
    reasonTag: String?,
    amountText: String,
    modifier: Modifier = Modifier
) {
    Column(horizontalAlignment = Alignment.End, modifier = modifier) {
        if (reasonTag != null) {
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(50))
                    .background(HPGray4)
                    .padding(horizontal = 10.dp, vertical = 4.dp)
            ) {
                Text(reasonTag, style = MaterialTheme.typography.labelMedium, color = HPText)
            }
            Spacer(modifier = Modifier.height(4.dp))
        }
        Text(
            amountText,
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.Bold,
            color = HPBlack
        )
    }
}

@Composable
fun NotificationBellIcon(onClick: () -> Unit, modifier: Modifier = Modifier) {
    Box(modifier = modifier) {
        IconButton(onClick = onClick) {
            Icon(
                imageVector = Icons.Filled.Notifications,
                contentDescription = stringResource(R.string.cd_notification),
                tint = HPBlack
            )
        }
        if (NotificationStore.hasUnread) {
            Box(
                modifier = Modifier
                    .padding(top = 8.dp, end = 8.dp)
                    .size(8.dp)
                    .align(Alignment.TopEnd)
                    .background(HPSub, CircleShape)
            )
        }
    }
}
