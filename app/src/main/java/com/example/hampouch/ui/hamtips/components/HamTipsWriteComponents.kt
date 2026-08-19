package com.example.hampouch.ui.hamtips.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.StarBorder
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.example.hampouch.R
import androidx.compose.ui.tooling.preview.Preview
import com.example.hampouch.domain.model.MenuRatingType
import com.example.hampouch.domain.model.TipShareCategory
import com.example.hampouch.ui.theme.HPBlack
import com.example.hampouch.ui.theme.HPGray5
import com.example.hampouch.ui.theme.HPMain
import com.example.hampouch.ui.theme.HPStar
import com.example.hampouch.ui.theme.HPSub2
import com.example.hampouch.ui.theme.HPSub3
import com.example.hampouch.ui.theme.HPSub4
import com.example.hampouch.ui.theme.HPText
import com.example.hampouch.ui.theme.HPWhite
import com.example.hampouch.ui.theme.HampouchTheme

@Composable
fun HamTipsSimpleTopBar(title: String, onBackClick: () -> Unit, modifier: Modifier = Modifier) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .statusBarsPadding()
            .height(64.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        IconButton(onClick = onBackClick) {
            Icon(
                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                contentDescription = stringResource(R.string.cd_back),
                tint = HPBlack
            )
        }
        Text(
            text = title,
            style = MaterialTheme.typography.titleSmall,
            color = HPBlack,
            textAlign = TextAlign.Center,
            modifier = Modifier.weight(1f)
        )
        Spacer(modifier = Modifier.width(48.dp))
    }
}

@Preview(showBackground = true, name = "햄팁 작성 상단바")
@Composable
private fun HamTipsSimpleTopBarPreview() {
    HampouchTheme {
        HamTipsSimpleTopBar(title = "꿀팁 공유하기", onBackClick = {})
    }
}

@Composable
fun HamTipsWriteHeader(overline: String, heading: String, subheading: String, modifier: Modifier = Modifier) {
    Column(modifier = modifier.fillMaxWidth()) {
        Text(text = overline, style = MaterialTheme.typography.labelLarge, color = HPMain, fontWeight = FontWeight.Bold)
        Spacer(modifier = Modifier.height(8.dp))
        Text(text = heading, style = MaterialTheme.typography.titleMedium, color = HPBlack)
        Spacer(modifier = Modifier.height(4.dp))
        Text(text = subheading, style = MaterialTheme.typography.bodyMedium, color = HPText)
    }
}

@Preview(showBackground = true, name = "햄팁 작성 헤더")
@Composable
private fun HamTipsWriteHeaderPreview() {
    HampouchTheme {
        HamTipsWriteHeader(
            overline = "STEP 1",
            heading = "어떤 꿀팁을 공유할까요?",
            subheading = "다른 사용자에게 도움이 되는 절약 팁을 남겨주세요",
            modifier = Modifier.padding(16.dp)
        )
    }
}

@Composable
fun HamTipsFieldLabel(text: String, modifier: Modifier = Modifier) {
    Text(text = text, style = MaterialTheme.typography.bodyMedium, color = HPBlack, fontWeight = FontWeight.Bold, modifier = modifier)
}

@Preview(showBackground = true, name = "햄팁 필드 라벨")
@Composable
private fun HamTipsFieldLabelPreview() {
    HampouchTheme {
        HamTipsFieldLabel(text = "제목", modifier = Modifier.padding(16.dp))
    }
}

@Composable
fun HamTipsCategoryPickerRow(
    selected: TipShareCategory,
    onSelected: (TipShareCategory) -> Unit,
    modifier: Modifier = Modifier
) {
    Row(modifier = modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        TipShareCategory.entries.forEach { category ->
            val isSelected = category == selected
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(50))
                    .background(if (isSelected) HPMain else HPWhite)
                    .border(1.dp, if (isSelected) HPMain else HPGray5, RoundedCornerShape(50))
                    .clickable { onSelected(category) }
                    .padding(horizontal = 10.dp, vertical = 10.dp)
            ) {
                Text(
                    text = stringResource(category.labelResId),
                    style = MaterialTheme.typography.bodyMedium,
                    color = if (isSelected) HPWhite else HPBlack,
                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                )
            }
        }
    }
}

@Preview(showBackground = true, name = "햄팁 카테고리 선택")
@Composable
private fun HamTipsCategoryPickerRowPreview() {
    HampouchTheme {
        HamTipsCategoryPickerRow(selected = TipShareCategory.SHOPPING, onSelected = {}, modifier = Modifier.padding(16.dp))
    }
}

@Composable
fun HamTipsWriteTextField(
    value: String,
    onValueChange: (String) -> Unit,
    placeholder: String,
    modifier: Modifier = Modifier,
    minHeight: androidx.compose.ui.unit.Dp = 48.dp,
    singleLine: Boolean = true
) {
    val focusRequester = remember { FocusRequester() }
    Box(
        modifier = modifier
            .fillMaxWidth()
            .defaultMinSize(minHeight = minHeight)
            .clip(RoundedCornerShape(12.dp))
            .background(HPWhite)
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null
            ) { focusRequester.requestFocus() }
            .padding(horizontal = 16.dp, vertical = 14.dp)
    ) {
        if (value.isEmpty()) {
            Text(text = placeholder, style = MaterialTheme.typography.bodyMedium, color = HPGray5)
        }
        BasicTextField(
            value = value,
            onValueChange = onValueChange,
            singleLine = singleLine,
            textStyle = MaterialTheme.typography.bodyMedium.copy(color = HPBlack),
            cursorBrush = SolidColor(HPMain),
            modifier = Modifier
                .fillMaxWidth()
                .focusRequester(focusRequester)
        )
    }
}

@Preview(showBackground = true, name = "햄팁 작성 텍스트 필드")
@Composable
private fun HamTipsWriteTextFieldPreview() {
    HampouchTheme {
        HamTipsWriteTextField(
            value = "",
            onValueChange = {},
            placeholder = "제목을 입력해주세요",
            modifier = Modifier.padding(16.dp)
        )
    }
}

private fun Modifier.dashedBorder(
    color: Color,
    cornerRadius: androidx.compose.ui.unit.Dp,
    strokeWidth: androidx.compose.ui.unit.Dp = 1.dp,
    dashWidth: androidx.compose.ui.unit.Dp = 6.dp,
    gapWidth: androidx.compose.ui.unit.Dp = 4.dp
): Modifier = drawWithContent {
    drawContent()
    drawRoundRect(
        color = color,
        cornerRadius = androidx.compose.ui.geometry.CornerRadius(cornerRadius.toPx()),
        style = androidx.compose.ui.graphics.drawscope.Stroke(
            width = strokeWidth.toPx(),
            pathEffect = androidx.compose.ui.graphics.PathEffect.dashPathEffect(
                floatArrayOf(dashWidth.toPx(), gapWidth.toPx())
            )
        )
    )
}

@Composable
fun HamTipsTitlePreviewBox(previewText: String, modifier: Modifier = Modifier) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(HPSub4)
            .dashedBorder(color = HPSub2, cornerRadius = 12.dp)
            .padding(horizontal = 16.dp, vertical = 14.dp)
    ) {
        Text(
            text = stringResource(R.string.hamtips_write_menu_title_preview_label),
            style = MaterialTheme.typography.bodyMedium,
            color = HPBlack,
            fontWeight = FontWeight.Bold
        )
        Spacer(modifier = Modifier.height(8.dp))
        if (previewText.isEmpty()) {
            Text(
                text = stringResource(R.string.hamtips_write_menu_title_preview_placeholder),
                style = MaterialTheme.typography.bodyMedium,
                color = HPText
            )
        } else {
            Text(text = previewText, style = MaterialTheme.typography.bodyMedium, color = HPBlack)
        }
    }
}

@Preview(showBackground = true, name = "햄팁 제목 미리보기 박스")
@Composable
private fun HamTipsTitlePreviewBoxPreview() {
    HampouchTheme {
        HamTipsTitlePreviewBox(previewText = "편의점 1+1 활용 꿀팁", modifier = Modifier.padding(16.dp))
    }
}

@Preview(showBackground = true, name = "햄팁 제목 미리보기 박스 (빈 값)")
@Composable
private fun HamTipsTitlePreviewBoxEmptyPreview() {
    HampouchTheme {
        HamTipsTitlePreviewBox(previewText = "", modifier = Modifier.padding(16.dp))
    }
}

@Composable
fun HamTipsPriceInputField(price: Int, onPriceChange: (Int) -> Unit, modifier: Modifier = Modifier) {
    val displayText = if (price > 0) "%,d".format(price) else ""
    Row(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(HPWhite)
            .padding(horizontal = 16.dp, vertical = 14.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(modifier = Modifier.weight(1f)) {
            if (displayText.isEmpty()) {
                Text(
                    text = "0",
                    style = MaterialTheme.typography.bodyMedium,
                    color = HPGray5,
                    textAlign = TextAlign.End,
                    modifier = Modifier.fillMaxWidth()
                )
            }
            BasicTextField(
                value = displayText,
                onValueChange = { input -> onPriceChange(input.filter { it.isDigit() }.take(9).toIntOrNull() ?: 0) },
                singleLine = true,
                textStyle = MaterialTheme.typography.bodyMedium.copy(color = HPBlack, textAlign = TextAlign.End),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                cursorBrush = SolidColor(HPMain),
                modifier = Modifier.fillMaxWidth()
            )
        }
        Spacer(modifier = Modifier.width(6.dp))
        Text(
            text = stringResource(R.string.hamtips_write_menu_price_won_suffix),
            style = MaterialTheme.typography.bodyMedium,
            color = HPBlack,
            fontWeight = FontWeight.Bold
        )
    }
}

@Preview(showBackground = true, name = "햄팁 가격 입력 필드")
@Composable
private fun HamTipsPriceInputFieldPreview() {
    HampouchTheme {
        HamTipsPriceInputField(price = 12_000, onPriceChange = {}, modifier = Modifier.padding(16.dp))
    }
}

@Composable
fun HamTipsStarRatingRow(
    type: MenuRatingType,
    rating: Int,
    onRatingChange: (Int) -> Unit,
    modifier: Modifier = Modifier
) {
    val label = stringResource(type.labelResId)
    Row(modifier = modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
        Text(text = label, style = MaterialTheme.typography.bodyMedium, color = HPBlack, modifier = Modifier.width(56.dp))
        Row {
            (1..5).forEach { starIndex ->
                Icon(
                    imageVector = if (starIndex <= rating) Icons.Filled.Star else Icons.Filled.StarBorder,
                    contentDescription = stringResource(R.string.hamtips_cd_star_rating_format, label, starIndex),
                    tint = if (starIndex <= rating) HPStar else HPGray5,
                    modifier = Modifier
                        .padding(end = 2.dp)
                        .clickable { onRatingChange(starIndex) }
                )
            }
        }
        Spacer(modifier = Modifier.weight(1f))
        Text(
            text = "%.1f".format(rating.toFloat()),
            style = MaterialTheme.typography.bodyMedium,
            color = HPText,
            fontWeight = FontWeight.Bold
        )
    }
}

@Preview(showBackground = true, name = "햄팁 별점 입력")
@Composable
private fun HamTipsStarRatingRowPreview() {
    HampouchTheme {
        HamTipsStarRatingRow(type = MenuRatingType.TASTE, rating = 4, onRatingChange = {}, modifier = Modifier.padding(16.dp))
    }
}

@Composable
fun HamTipsSubmitButton(
    text: String,
    enabled: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    disabledContainerColor: Color = HPMain.copy(alpha = 0.4f),
    disabledContentColor: Color = HPWhite.copy(alpha = 0.8f)
) {
    Button(
        onClick = onClick,
        enabled = enabled,
        modifier = modifier
            .fillMaxWidth()
            .height(56.dp),
        shape = RoundedCornerShape(10.dp),
        colors = ButtonDefaults.buttonColors(
            containerColor = HPMain,
            disabledContainerColor = disabledContainerColor,
            disabledContentColor = disabledContentColor
        )
    ) {
        Text(
            text = text,
            style = MaterialTheme.typography.bodyLarge,
            color = if (enabled) HPWhite else disabledContentColor,
            fontWeight = FontWeight.Bold
        )
    }
}

@Preview(showBackground = true, name = "햄팁 제출 버튼")
@Composable
private fun HamTipsSubmitButtonPreview() {
    HampouchTheme {
        HamTipsSubmitButton(text = "등록하기", enabled = true, onClick = {}, modifier = Modifier.padding(16.dp))
    }
}

@Composable
fun HamTipsFieldCard(modifier: Modifier = Modifier, content: @Composable androidx.compose.foundation.layout.ColumnScope.() -> Unit) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(20.dp))
            .background(HPSub3)
            .padding(horizontal = 16.dp, vertical = 20.dp),
        content = content
    )
}

@Preview(showBackground = true, name = "햄팁 필드 카드")
@Composable
private fun HamTipsFieldCardPreview() {
    HampouchTheme {
        HamTipsFieldCard(modifier = Modifier.padding(16.dp)) {
            HamTipsFieldLabel(text = "메뉴 이름")
            HamTipsWriteTextField(value = "", onValueChange = {}, placeholder = "메뉴 이름을 입력해주세요")
        }
    }
}
