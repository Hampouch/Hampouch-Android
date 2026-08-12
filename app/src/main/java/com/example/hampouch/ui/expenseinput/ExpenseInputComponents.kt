package com.example.hampouch.ui.expenseinput

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.PhotoCamera
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.hampouch.R
import com.example.hampouch.ui.expensedetail.DashedDivider
import com.example.hampouch.ui.expensedetail.ExpenseTextField
import com.example.hampouch.ui.expensedetail.formatWon
import com.example.hampouch.ui.theme.HPBlack
import com.example.hampouch.ui.theme.HPGray3
import com.example.hampouch.ui.theme.HPGray4
import com.example.hampouch.ui.theme.HPGray5
import com.example.hampouch.ui.theme.HPMain
import com.example.hampouch.ui.theme.HPSub
import com.example.hampouch.ui.theme.HPSub4
import com.example.hampouch.ui.theme.HPText
import com.example.hampouch.ui.theme.HPWhite
import com.example.hampouch.ui.theme.HampouchTheme

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ExpenseInputTopBar(
    title: String,
    onBackClick: () -> Unit,
    modifier: Modifier = Modifier,
    containerColor: Color = HPWhite
) {
    CenterAlignedTopAppBar(
        modifier = modifier,
        title = {
            Text(title, style = MaterialTheme.typography.titleSmall, color = HPBlack)
        },
        navigationIcon = {
            IconButton(onClick = onBackClick) {
                Icon(
                    Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = stringResource(R.string.cd_back),
                    tint = HPBlack
                )
            }
        },
        colors = TopAppBarDefaults.topAppBarColors(containerColor = containerColor)
    )
}

@Composable
fun ExpenseInputOptionalBadge(modifier: Modifier = Modifier) {
    Text(
        stringResource(R.string.expenseinput_optional_badge),
        modifier = modifier,
        style = MaterialTheme.typography.labelLarge,
        fontSize = 16.sp,
        fontWeight = FontWeight.Bold,
        color = HPMain
    )
}

@Composable
fun ExpenseInputBalanceCard(
    balance: Int,
    dailyLimit: Int,
    modifier: Modifier = Modifier
) {
    val progress = if (dailyLimit > 0) (balance.toFloat() / dailyLimit).coerceIn(0f, 1f) else 0f
    Column(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(HPWhite)
            .border(width = 1.dp, color = HPMain, shape = RoundedCornerShape(16.dp))
            .padding(horizontal = 20.dp, vertical = 10.dp)
    ) {
        Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
            Text(
                stringResource(R.string.expenseinput_today_balance),
                style = MaterialTheme.typography.bodySmall,
                color = HPBlack
            )
            Spacer(modifier = Modifier.weight(1f))
            Text(
                stringResource(R.string.expenseinput_won_format, formatWon(balance)),
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold,
                color = HPBlack
            )
        }
        Spacer(modifier = Modifier.height(10.dp))
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(16.dp)
                .clip(RoundedCornerShape(50))
                .background(HPGray4)
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth(progress)
                    .height(16.dp)
                    .clip(RoundedCornerShape(50))
                    .background(HPMain)
            )
        }
        Spacer(modifier = Modifier.height(10.dp))
    }
}

@Composable
fun ExpenseInputAmountDisplay(amount: Int, modifier: Modifier = Modifier) {
    val text = if (amount == 0) "0" else formatWon(amount)
    val color = if (amount == 0) HPText else HPBlack
    Row(
        modifier = modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.Center
    ) {
        text.forEachIndexed { index, char ->
            AnimatedContent(
                targetState = char,
                transitionSpec = {
                    (slideInVertically { height -> -height } + fadeIn()) togetherWith
                            (slideOutVertically { height -> height } + fadeOut())
                },
                label = "amountDigit$index"
            ) { animatedChar ->
                Text(
                    text = animatedChar.toString(),
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.Bold,
                    fontSize = 34.sp,
                    color = color
                )
            }
        }
    }
}

private val NumPadKeys = listOf(
    "1", "2", "3",
    "4", "5", "6",
    "7", "8", "9",
    "00", "0", "DEL"
)

private val NumPadRows = NumPadKeys.chunked(3)
private val NumPadKeyMinHeight = 48.dp

val ExpenseAmountNumPadMinHeight = NumPadKeyMinHeight * NumPadRows.size

@Composable
fun ExpenseAmountNumPad(
    onDigit: (String) -> Unit,
    onDelete: () -> Unit,
    modifier: Modifier = Modifier
) {
    BoxWithConstraints(modifier = modifier.fillMaxWidth()) {
        val rowHeight = (maxHeight / NumPadRows.size).coerceAtLeast(NumPadKeyMinHeight)
        Column(modifier = Modifier.fillMaxWidth()) {
            NumPadRows.forEach { row ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(rowHeight)
                ) {
                    row.forEach { key ->
                        NumPadKey(
                            label = key,
                            onClick = { if (key == "DEL") onDelete() else onDigit(key) },
                            modifier = Modifier
                                .weight(1f)
                                .fillMaxHeight()
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun NumPadKey(label: String, onClick: () -> Unit, modifier: Modifier = Modifier) {
    val isDelete = label == "DEL"
    Column(
        modifier = modifier
            .heightIn(min = NumPadKeyMinHeight)
            .clip(RoundedCornerShape(12.dp))
            .clickable(
                onClickLabel = if (isDelete) stringResource(R.string.cd_expenseinput_delete_digit) else null,
                onClick = onClick
            ),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = if (isDelete) stringResource(R.string.expenseinput_delete_digit) else label,
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.Bold,
            fontSize = 28.sp,
            color = if (isDelete) HPText else HPBlack
        )
    }
}

@Composable
fun ExpenseInputAmountSummaryCard(
    amount: Int,
    balanceAfterExpense: Int,
    modifier: Modifier = Modifier,
    header: @Composable (() -> Unit)? = null
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(30.dp))
            .background(HPSub4)
            .padding(horizontal = 25.dp, vertical = 30.dp)
    ) {
        if (header != null) {
            header()
            Spacer(modifier = Modifier.height(14.dp))
        }
        Row(verticalAlignment = Alignment.Bottom) {
            Text(
                formatWon(amount),
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold,
                color = HPBlack
            )
            Spacer(modifier = Modifier.padding(start = 4.dp))
            Text("원", style = MaterialTheme.typography.bodyLarge, color = HPText)
        }
        Spacer(modifier = Modifier.height(5.dp))
        DashedDivider(color = HPBlack)
        Spacer(modifier = Modifier.height(5.dp))
        Row(modifier = Modifier.fillMaxWidth()) {
            Text(
                stringResource(R.string.expenseinput_balance_after_format),
                style = MaterialTheme.typography.bodyMedium,
                color = HPText
            )
            Spacer(modifier = Modifier.weight(1f))
            Text(
                stringResource(R.string.expenseinput_won_format, formatWon(balanceAfterExpense)),
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Bold,
                color = HPSub
            )
        }
    }
}

@Composable
fun ExpenseInputCategoryBadge(
    iconRes: Int,
    label: String,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .clip(RoundedCornerShape(50))
            .background(HPMain)
            .padding(horizontal = 10.dp, vertical = 5.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Image(
            painter = painterResource(iconRes),
            contentDescription = null,
            modifier = Modifier.size(30.dp)
        )
        Spacer(modifier = Modifier.width(10.dp))
        Text(label, style = MaterialTheme.typography.labelMedium, fontSize = 16.sp, color = HPWhite)
    }
}

@Composable
fun ExpenseInputPrimaryButton(
    label: String,
    enabled: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Button(
        onClick = onClick,
        enabled = enabled,
        modifier = modifier
            .fillMaxWidth()
            .height(56.dp),
        shape = RoundedCornerShape(12.dp),
        colors = ButtonDefaults.buttonColors(
            containerColor = HPMain,
            contentColor = HPWhite,
            disabledContainerColor = HPGray4,
            disabledContentColor = HPGray3
        )
    ) {
        Text(label, style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.Bold)
    }
}

@Composable
fun ExpenseInputSkipRestLink(onClick: () -> Unit, modifier: Modifier = Modifier) {
    Text(
        stringResource(R.string.expenseinput_skip_rest),
        modifier = modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(vertical = 10.dp),
        textAlign = TextAlign.Center,
        style = MaterialTheme.typography.bodySmall,
        color = HPText
    )
}

private val ReasonOptionButtonMinHeight = 64.dp

@Composable
fun ExpenseInputReasonOptionButton(
    label: String,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    /** 기본값은 제한 없음. 사용자가 입력한 문자열을 라벨로 쓰는 버튼에서만 줄 수를 제한한다. */
    maxLines: Int = Int.MAX_VALUE
) {
    Box(
        modifier = modifier
            .fillMaxHeight()
            .heightIn(min = ReasonOptionButtonMinHeight)
            .clip(RoundedCornerShape(14.dp))
            .background(if (selected) HPMain else HPWhite)
            .border(
                width = 1.dp,
                color = if (selected) HPMain else HPGray4,
                shape = RoundedCornerShape(14.dp)
            )
            .clickable(onClick = onClick)
            .padding(horizontal = 14.dp, vertical = 10.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            label,
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = if (selected) FontWeight.Bold else FontWeight.Normal,
            color = if (selected) HPWhite else HPBlack,
            textAlign = TextAlign.Center,
            maxLines = maxLines,
            overflow = TextOverflow.Ellipsis
        )
    }
}

@Composable
fun ExpenseInputPhotoAttachCard(onClick: () -> Unit, modifier: Modifier = Modifier) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(HPSub4)
            .border(width = 1.dp, shape = RoundedCornerShape(16.dp), color = HPMain)
            .clickable(
                onClickLabel = stringResource(R.string.cd_expenseinput_photo_attach),
                onClick = onClick
            )
            .padding(18.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Image(
            painter = painterResource(R.drawable.icon_camera),
            contentDescription = null,
            modifier = Modifier
                .width(51.dp)
                .height(40.dp)
        )
        Spacer(modifier = Modifier.padding(start = 12.dp))
        Column {
            Text(
                stringResource(R.string.expenseinput_photo_attach_title),
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Bold,
                color = HPMain
            )
            Text(
                stringResource(R.string.expenseinput_photo_attach_subtitle),
                style = MaterialTheme.typography.bodySmall,
                fontWeight = FontWeight.Bold,
                color = HPMain
            )
        }
    }
}

@Composable
fun ExpenseInputTextEntryDialog(
    label: String,
    placeholder: String,
    value: String,
    onValueChange: (String) -> Unit,
    onCancel: () -> Unit,
    onConfirm: () -> Unit
) {
    Dialog(
        onDismissRequest = onCancel,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp)
        )
        {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(10.dp))
                    .background(HPWhite)
                    .padding(25.dp)
            ) {
                Text(
                    label,
                    style = MaterialTheme.typography.bodyMedium,
                    color = HPBlack
                )
                Spacer(modifier = Modifier.height(10.dp))
                ExpenseTextField(
                    value = value,
                    onValueChange = onValueChange,
                    placeholder = placeholder,
                    showBorder = false,
                    contentPadding = PaddingValues(0.dp)
                )
                Spacer(modifier = Modifier.height(20.dp))
            }
            Spacer(modifier = Modifier.height(20.dp))
            Row(modifier = Modifier.fillMaxWidth()) {
                Button(
                    onClick = onCancel,
                    modifier = Modifier
                        .weight(1f)
                        .height(64.dp),
                    shape = RoundedCornerShape(10.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = HPGray3)
                ) {
                    Text(
                        stringResource(R.string.common_cancel),
                        style = MaterialTheme.typography.bodyLarge,
                        color = HPText
                    )
                }
                Spacer(modifier = Modifier.padding(start = 10.dp))
                Button(
                    onClick = onConfirm,
                    modifier = Modifier
                        .weight(1f)
                        .height(64.dp),
                    shape = RoundedCornerShape(10.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = HPMain)
                ) {
                    Text(
                        stringResource(R.string.common_confirm),
                        style = MaterialTheme.typography.bodyLarge,
                        fontWeight = FontWeight.Bold,
                        color = HPWhite
                    )
                }
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun ExpenseAmountNumPadPreview() {
    HampouchTheme {
        Column(modifier = Modifier.padding(20.dp)) {
            ExpenseInputBalanceCard(balance = 7_300, dailyLimit = 20_000)
            Spacer(modifier = Modifier.height(20.dp))
            ExpenseInputAmountDisplay(amount = 7_900)
            Spacer(modifier = Modifier.height(24.dp))
            ExpenseAmountNumPad(onDigit = {}, onDelete = {})
        }
    }
}
