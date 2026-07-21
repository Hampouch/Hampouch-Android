package com.example.hampouch.ui.hamtips.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.example.hampouch.R
import com.example.hampouch.data.model.MenuRatingType
import com.example.hampouch.data.model.TipShareCategory
import com.example.hampouch.ui.theme.HPBlack
import com.example.hampouch.ui.theme.HPGray5
import com.example.hampouch.ui.theme.HPMain
import com.example.hampouch.ui.theme.HPStar
import com.example.hampouch.ui.theme.HPSub4
import com.example.hampouch.ui.theme.HPText
import com.example.hampouch.ui.theme.HPWhite

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

@Composable
fun HamTipsFieldLabel(text: String, modifier: Modifier = Modifier) {
    Text(text = text, style = MaterialTheme.typography.bodyMedium, color = HPBlack, fontWeight = FontWeight.Bold, modifier = modifier)
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
                    .padding(horizontal = 12.dp, vertical = 10.dp)
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

@Composable
fun HamTipsWriteTextField(
    value: String,
    onValueChange: (String) -> Unit,
    placeholder: String,
    modifier: Modifier = Modifier,
    minHeight: androidx.compose.ui.unit.Dp = 48.dp,
    singleLine: Boolean = true
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .defaultMinSize(minHeight = minHeight)
            .clip(RoundedCornerShape(12.dp))
            .background(HPWhite)
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
            modifier = Modifier.fillMaxWidth()
        )
    }
}

@Composable
fun HamTipsTitlePreviewBox(previewText: String, modifier: Modifier = Modifier) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(HPWhite)
            .border(1.dp, HPMain, RoundedCornerShape(12.dp))
            .padding(horizontal = 16.dp, vertical = 14.dp)
    ) {
        if (previewText.isEmpty()) {
            Text(
                text = stringResource(R.string.hamtips_write_menu_title_preview_placeholder),
                style = MaterialTheme.typography.bodyMedium,
                color = HPGray5
            )
        } else {
            Text(text = previewText, style = MaterialTheme.typography.bodyMedium, color = HPMain, fontWeight = FontWeight.Bold)
        }
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

@Composable
fun HamTipsFieldCard(modifier: Modifier = Modifier, content: @Composable androidx.compose.foundation.layout.ColumnScope.() -> Unit) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(20.dp))
            .background(HPSub4)
            .padding(horizontal = 16.dp, vertical = 20.dp),
        content = content
    )
}
