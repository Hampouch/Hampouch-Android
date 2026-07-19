package com.example.hampouch.ui.common

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import com.example.hampouch.ui.theme.Body16Bold
import com.example.hampouch.ui.theme.HPBlack
import com.example.hampouch.ui.theme.HPGray5
import com.example.hampouch.ui.theme.HPSub
import com.example.hampouch.ui.theme.HPText
import com.example.hampouch.ui.theme.HPWhite

// 화면에서 공통으로 쓰는 컴포저블 모음.

// 로그인 / 회원가입 화면에서 사용
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
    onCheckClick: (() -> Unit)? = null
) {
    val interactionSource = remember { MutableInteractionSource() }
    val colors = OutlinedTextFieldDefaults.colors(
        unfocusedBorderColor = HPGray5,
        unfocusedContainerColor = HPWhite,
        focusedContainerColor = HPWhite,
    )

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
            CheckButton(onClick = onCheckClick)
        }
    }
}

// 햄배틀 화면에서 사용
@Composable
fun CheckButton(onClick: () -> Unit) {
    Button(
        onClick = onClick,
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
