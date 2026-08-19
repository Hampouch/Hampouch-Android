package com.example.hampouch.ui.expenseinput

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SelectableDates
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.hampouch.R
import com.example.hampouch.domain.model.ExpenseRecord
import com.example.hampouch.ui.expensedetail.ChoiceChip
import com.example.hampouch.ui.expensedetail.ChoiceChipIcon
import com.example.hampouch.ui.expensedetail.ExpensePhotoEditSection
import com.example.hampouch.ui.expensedetail.ExpenseReasonCatalog
import com.example.hampouch.ui.expensedetail.ExpenseTextField
import com.example.hampouch.ui.common.ChipGrid
import com.example.hampouch.ui.expensedetail.formatWon
import com.example.hampouch.ui.home.HomeCategoryCatalog
import com.example.hampouch.ui.theme.HPBlack
import com.example.hampouch.ui.theme.HPMain
import com.example.hampouch.ui.theme.HPSub2
import com.example.hampouch.ui.theme.HPSub3
import com.example.hampouch.ui.theme.HPSub4
import com.example.hampouch.ui.theme.HPText
import com.example.hampouch.ui.theme.HPWhite
import com.example.hampouch.ui.theme.HampouchTheme
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneOffset
import java.time.format.DateTimeFormatter

internal const val MaxExpenseAmount = 999_999_999L
private const val ExpenseInputPhotoMaxCount = 5

private val inputDateFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd")

private fun LocalDate.toEpochMillisUtc(): Long =
    atStartOfDay(ZoneOffset.UTC).toInstant().toEpochMilli()

private fun Long.toLocalDateUtc(): LocalDate =
    Instant.ofEpochMilli(this).atZone(ZoneOffset.UTC).toLocalDate()

internal fun isExpenseInputDateSelectable(utcTimeMillis: Long, today: LocalDate): Boolean =
    !utcTimeMillis.toLocalDateUtc().isAfter(today)

internal fun isExpenseAmountValid(amount: Long): Boolean = amount in 0..MaxExpenseAmount

private sealed interface InputCategoryOption {
    data class Preset(val category: HomeCategoryCatalog.Category) : InputCategoryOption
    data object CustomInput : InputCategoryOption
}

private val inputCategoryOptions: List<InputCategoryOption> =
    HomeCategoryCatalog.categories.map { InputCategoryOption.Preset(it) } + InputCategoryOption.CustomInput


private val presetReasons: List<ExpenseReasonCatalog.Reason> = ExpenseReasonCatalog.reasons

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ExpenseInputRoute(
    todayBalance: Int,
    dailyLimit: Int,
    onBackClick: () -> Unit,
    onNoSpendingToday: () -> Unit,
    onComplete: () -> Unit,
    modifier: Modifier = Modifier,
    form: ExpenseInputFormState = ExpenseInputFormState(),
    showMaxAmountError: Boolean = false,
    onDateSelected: (LocalDate) -> Unit = {},
    onAmountDigit: (String) -> Unit = {},
    onAmountDelete: () -> Unit = {},
    onStepChanged: (Int) -> Unit = {},
    onExpenseNameChange: (String) -> Unit = {},
    onCategorySelected: (String) -> Unit = {},
    onCustomCategoryConfirm: () -> Unit = {},
    onCustomCategoryTextChange: (String) -> Unit = {},
    onReasonSelected: (String) -> Unit = {},
    onCustomReasonConfirm: () -> Unit = {},
    onCustomReasonTextChange: (String) -> Unit = {},
    onMemoChange: (String) -> Unit = {},
    onPhotosAdded: (List<String>) -> Unit = {},
    onPhotosRemoved: (Set<Int>) -> Unit = {},
    onPhotoReplaced: (Int, String) -> Unit = { _, _ -> }
) {
    var showDatePicker by remember { mutableStateOf(false) }
    var showSkipPromptDialog by remember { mutableStateOf(false) }
    var showCategoryCustomDialog by remember { mutableStateOf(false) }
    var showReasonCustomDialog by remember { mutableStateOf(false) }
    var showSaveConfirmDialog by remember { mutableStateOf(false) }


    val step = form.step

    BackHandler {
        if (step > 1) onStepChanged(step - 1) else onBackClick()
    }

    Scaffold(
        modifier = modifier.imePadding(),
        topBar = {
            ExpenseInputTopBar(
                title = stringResource(R.string.expenseinput_title),
                onBackClick = { if (step > 1) onStepChanged(step - 1) else onBackClick() },
                containerColor = if (step == 1) HPSub3 else HPWhite
            )
        },
        containerColor = HPWhite
    ) { innerPadding ->
        if (step == 1) {
            ExpenseInputAmountStep(
                modifier = Modifier
                    .padding(innerPadding)
                    .fillMaxSize(),
                date = form.date,
                onDateClick = { showDatePicker = true },
                balance = todayBalance,
                dailyLimit = dailyLimit,
                amount = form.amount,
                onDigit = onAmountDigit,
                onDelete = onAmountDelete,
                showMaxAmountError = showMaxAmountError,
                onNoSpendingToday = { showSkipPromptDialog = true },
                primaryButton = {
                    ExpenseInputPrimaryButton(
                        label = stringResource(R.string.expenseinput_next_button),
                        enabled = form.amount > 0,
                        onClick = { onStepChanged(step + 1) }
                    )
                }
            )
            return@Scaffold
        }

        Box(
            modifier = Modifier
                .padding(innerPadding)
                .fillMaxSize()
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .windowInsetsPadding(WindowInsets.navigationBars)
                    .padding(horizontal = 20.dp)
            ) {
                ExpenseInputDetailStep(
                    expenseName = form.expenseName,
                    onExpenseNameChange = onExpenseNameChange,
                    categoryId = form.categoryId,
                    isCustomCategory = form.isCustomCategory,
                    customCategoryText = form.customCategoryText,
                    onCategorySelected = onCategorySelected,
                    onCustomCategoryClick = { showCategoryCustomDialog = true },
                    reasonId = form.reasonId,
                    isCustomReason = form.isCustomReason,
                    customReasonText = form.customReasonText,
                    onReasonSelected = onReasonSelected,
                    onCustomReasonClick = { showReasonCustomDialog = true },
                    memo = form.memo,
                    onMemoChange = onMemoChange,
                    photoUris = form.photoUris,
                    onPhotosAdded = onPhotosAdded,
                    onPhotosRemoved = onPhotosRemoved,
                    onPhotoReplaced = onPhotoReplaced
                )
                Spacer(modifier = Modifier.height(120.dp))
            }
            Column(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .fillMaxWidth()
                    .background(HPWhite)
                    .windowInsetsPadding(WindowInsets.navigationBars)
                    .padding(horizontal = 20.dp)
            ) {
                ExpenseInputPrimaryButton(
                    label = stringResource(R.string.expenseinput_next_button),
                    // 모든 입력이 선택 사항이라 항상 저장할 수 있다.
                    enabled = true,
                    onClick = { showSaveConfirmDialog = true }
                )
                Spacer(modifier = Modifier.height(20.dp))
            }
        }
    }

    if (showDatePicker) {
        val today = LocalDate.now()
        val datePickerState =
            rememberDatePickerState(
                initialSelectedDateMillis = form.date.coerceAtMost(today).toEpochMillisUtc(),
                selectableDates = object : SelectableDates {
                    override fun isSelectableDate(utcTimeMillis: Long): Boolean =
                        isExpenseInputDateSelectable(utcTimeMillis, today)

                    override fun isSelectableYear(year: Int): Boolean = year <= today.year
                }
            )
        DatePickerDialog(
            onDismissRequest = { showDatePicker = false },
            confirmButton = {
                TextButton(onClick = {
                    datePickerState.selectedDateMillis?.let { millis ->
                        millis.toLocalDateUtc()
                            .takeIf { !it.isAfter(today) }
                            ?.let(onDateSelected)
                    }
                    showDatePicker = false
                }) {
                    Text(stringResource(R.string.common_confirm))
                }
            },
            dismissButton = {
                TextButton(onClick = { showDatePicker = false }) {
                    Text(stringResource(R.string.common_cancel))
                }
            }
        ) {
            DatePicker(state = datePickerState)
        }
    }

    if (showSkipPromptDialog) {
        ExpenseInputSkipPromptDialog(
            question = stringResource(R.string.expenseinput_skip_prompt_title),
            confirmLabel = stringResource(R.string.expenseinput_skip_prompt_confirm),
            onCancel = { showSkipPromptDialog = false },
            onSkip = {
                showSkipPromptDialog = false
                // 0원 지출을 만드는 게 아니라 no_spend_day 기록이라 상세 입력 단계로 가지 않는다.
                onNoSpendingToday()
            }
        )
    }




    if (showCategoryCustomDialog) {
        ExpenseInputTextEntryDialog(
            label = stringResource(R.string.expenseinput_category_label),
            placeholder = stringResource(R.string.expenseinput_category_placeholder),
            value = form.customCategoryText,
            onValueChange = onCustomCategoryTextChange,
            onCancel = { showCategoryCustomDialog = false },
            onConfirm = {
                onCustomCategoryConfirm()
                showCategoryCustomDialog = false
            }
        )
    }

    if (showReasonCustomDialog) {
        ExpenseInputTextEntryDialog(
            label = stringResource(R.string.expenseinput_reason_label),
            placeholder = stringResource(R.string.expenseinput_reason_placeholder),
            value = form.customReasonText,
            onValueChange = onCustomReasonTextChange,
            onCancel = { showReasonCustomDialog = false },
            onConfirm = {
                onCustomReasonConfirm()
                showReasonCustomDialog = false
            }
        )
    }

    if (showSaveConfirmDialog) {
        ExpenseInputSaveConfirmDialog(
            record = ExpenseRecord(
                id = "expense-input-preview",
                date = form.date,
                amount = form.amount,
                categoryId = if (form.isCustomCategory) null else form.categoryId,
                customCategoryName = if (form.isCustomCategory) form.customCategoryText.ifBlank { null } else null,
                expenseName = form.expenseName.ifBlank { null },
                reasonId = if (form.isCustomReason) null else form.reasonId,
                customReason = if (form.isCustomReason) form.customReasonText.ifBlank { null } else null,
                memo = form.memo.ifBlank { null },
                photoUris = form.photoUris
            ),
            onCancel = { showSaveConfirmDialog = false },
            onSave = {
                showSaveConfirmDialog = false
                onComplete()
            }
        )
    }
}

@Composable
private fun ExpenseInputAmountStep(
    date: LocalDate,
    onDateClick: () -> Unit,
    balance: Int,
    dailyLimit: Int,
    amount: Int,
    onDigit: (String) -> Unit,
    onDelete: () -> Unit,
    showMaxAmountError: Boolean,
    onNoSpendingToday: () -> Unit,
    primaryButton: @Composable () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier.background(HPSub3)) {
        Column(modifier = Modifier.fillMaxWidth()) {
            Spacer(modifier = Modifier.height(20.dp))
            Text(
                date.format(inputDateFormatter),
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(50))
                    .clickable(onClick = onDateClick)
                    .padding(vertical = 6.dp),
                textAlign = TextAlign.Center,
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.Bold,
                color = HPMain
            )
            Spacer(modifier = Modifier.height(5.dp))
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp)
            ) {
                ExpenseInputBalanceCard(balance = balance, dailyLimit = dailyLimit)
            }

            Spacer(modifier = Modifier.height(25.dp))
            Text(
                stringResource(R.string.expenseinput_amount_question),
                modifier = Modifier.fillMaxWidth(),
                textAlign = TextAlign.Center,
                style = MaterialTheme.typography.headlineSmall,
                color = HPText
            )
            Spacer(modifier = Modifier.height(13.dp))
            ExpenseInputAmountDisplay(amount = amount)
            Spacer(modifier = Modifier.height(13.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center
            ) {
                Text(
                    text = "원",
                    style = MaterialTheme.typography.bodyMedium,
                    fontSize = 20.sp,
                    color = HPText
                )
            }
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp)
                    .padding(horizontal = 20.dp),
                contentAlignment = Alignment.Center
            ) {
                if (showMaxAmountError) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(12.dp))
                            .background(MaterialTheme.colorScheme.errorContainer)
                            .padding(horizontal = 16.dp, vertical = 12.dp),
                        horizontalArrangement = Arrangement.Center
                    ) {
                        Text(
                            stringResource(
                                R.string.expenseinput_max_amount_error_format,
                                formatWon(MaxExpenseAmount.toInt())
                            ),
                            textAlign = TextAlign.Center,
                            style = MaterialTheme.typography.bodySmall,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.error
                        )
                    }
                }
            }
            Spacer(modifier = Modifier.height(5.dp))
        }

        Column(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
                .clip(RoundedCornerShape(topStart = 40.dp, topEnd = 40.dp))
                .background(HPWhite)
        ) {
            BoxWithConstraints(
                modifier = Modifier
                    .weight(1f)
                    .padding(horizontal = 30.dp)
            ) {
                val noSpendingTextEstimatedHeight = 24.dp
                val fixedOverheadHeight = 30.dp + 20.dp + noSpendingTextEstimatedHeight
                val hasRoomToFill = maxHeight - fixedOverheadHeight >= ExpenseAmountNumPadMinHeight
                val columnModifier = if (hasRoomToFill) {
                    Modifier.fillMaxSize()
                } else {
                    Modifier.fillMaxWidth().verticalScroll(rememberScrollState())
                }
                Column(modifier = columnModifier) {
                    Spacer(modifier = Modifier.height(15.dp))
                    ExpenseAmountNumPad(
                        onDigit = onDigit,
                        onDelete = onDelete,
                        modifier = if (hasRoomToFill) {
                            Modifier.weight(1f)
                        } else {
                            Modifier.height(ExpenseAmountNumPadMinHeight)
                        }
                    )
                    Spacer(modifier = Modifier.height(20.dp))
                    Text(
                        stringResource(R.string.expenseinput_no_spending_today),
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable(onClick = onNoSpendingToday),
                        textAlign = TextAlign.Center,
                        style = MaterialTheme.typography.bodyMedium,
                        color = HPText
                    )
                    Spacer(modifier = Modifier.height(20.dp))
                }
            }
            Column(
                modifier = Modifier
                    .padding(horizontal = 20.dp)
            ) {
                primaryButton()
                Spacer(modifier = Modifier.height(20.dp))
            }
        }
    }
}

@Composable
private fun ExpenseInputDetailStep(
    expenseName: String,
    onExpenseNameChange: (String) -> Unit,
    categoryId: String?,
    isCustomCategory: Boolean,
    customCategoryText: String,
    onCategorySelected: (String) -> Unit,
    onCustomCategoryClick: () -> Unit,
    reasonId: String?,
    isCustomReason: Boolean,
    customReasonText: String,
    onReasonSelected: (String) -> Unit,
    onCustomReasonClick: () -> Unit,
    memo: String,
    onMemoChange: (String) -> Unit,
    photoUris: List<String>,
    onPhotosAdded: (List<String>) -> Unit,
    onPhotosRemoved: (Set<Int>) -> Unit,
    onPhotoReplaced: (index: Int, newUri: String) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier.fillMaxWidth()) {
        ExpenseInputOptionalBadge()
        Spacer(modifier = Modifier.height(2.dp))
        Text(
            stringResource(R.string.expenseinput_category_question),
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color = HPBlack
        )
        Spacer(modifier = Modifier.height(2.dp))
        Text(
            stringResource(R.string.expenseinput_detail_description_line1),
            style = MaterialTheme.typography.bodySmall,
            color = HPText
        )
        Text(
            stringResource(R.string.expenseinput_detail_description_line2),
            style = MaterialTheme.typography.bodySmall,
            color = HPText
        )

        Spacer(modifier = Modifier.height(25.dp))
        Text(
            stringResource(R.string.expenseinput_expense_name_label),
            style = MaterialTheme.typography.bodyMedium,
            color = HPBlack
        )
        Spacer(modifier = Modifier.height(10.dp))
        ExpenseTextField(
            value = expenseName,
            onValueChange = onExpenseNameChange,
            placeholder = stringResource(R.string.expensedetail_expense_name_placeholder)
        )

        Spacer(modifier = Modifier.height(25.dp))
        Text(
            stringResource(R.string.expensedetail_field_category),
            style = MaterialTheme.typography.bodyMedium,
            color = HPBlack
        )
        Spacer(modifier = Modifier.height(10.dp))
        ChipGrid(items = inputCategoryOptions, minColumnWidth = 96.dp) { option ->
            when (option) {
                is InputCategoryOption.Preset -> ChoiceChip(
                    label = stringResource(option.category.labelResId),
                    icon = ChoiceChipIcon.Resource(option.category.chipIconResId),
                    iconSize = 20.dp,
                    iconSpacing = 6.dp,
                    selected = !isCustomCategory && categoryId == option.category.id,
                    onClick = { onCategorySelected(option.category.id) }
                )

                InputCategoryOption.CustomInput -> ChoiceChip(
                    label = if (isCustomCategory && customCategoryText.isNotBlank()) {
                        customCategoryText
                    } else {
                        stringResource(R.string.expensedetail_option_custom_input)
                    },
                    maxLines = 2,
                    selected = isCustomCategory,
                    onClick = onCustomCategoryClick
                )
            }
        }

        Spacer(modifier = Modifier.height(25.dp))
        Text(
            stringResource(R.string.expenseinput_reason_label),
            style = MaterialTheme.typography.bodyMedium,
            color = HPBlack
        )
        Spacer(modifier = Modifier.height(10.dp))
        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
            presetReasons.chunked(2).forEach { pair ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(IntrinsicSize.Min),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    pair.forEach { reason ->
                        ExpenseInputReasonOptionButton(
                            label = stringResource(reason.labelResId),
                            selected = !isCustomReason && reasonId == reason.id,
                            onClick = { onReasonSelected(reason.id) },
                            modifier = Modifier.weight(1f)
                        )
                    }
                    if (pair.size == 1) {
                        Spacer(modifier = Modifier.weight(1f))
                    }
                }
            }
            ExpenseInputReasonOptionButton(
                label = if (isCustomReason && customReasonText.isNotBlank()) {
                    customReasonText
                } else {
                    stringResource(R.string.expenseinput_reason_custom_input)
                },
                selected = isCustomReason,
                onClick = onCustomReasonClick,
                maxLines = 2,
                modifier = Modifier.fillMaxWidth()
            )
        }

        Spacer(modifier = Modifier.height(25.dp))
        Text(
            stringResource(R.string.expenseinput_memo_label),
            style = MaterialTheme.typography.bodyMedium,
            color = HPBlack
        )
        Spacer(modifier = Modifier.height(10.dp))
        ExpenseTextField(
            value = memo,
            onValueChange = onMemoChange,
            placeholder = stringResource(R.string.expenseinput_memo_placeholder),
            singleLine = false,
            minLines = 5
        )

        Spacer(modifier = Modifier.height(25.dp))
        ExpensePhotoEditSection(
            photoUris = photoUris,
            onPhotosAdded = onPhotosAdded,
            onPhotosRemoved = onPhotosRemoved,
            onPhotoReplaced = onPhotoReplaced,
            maxCount = ExpenseInputPhotoMaxCount
        )
    }
}

@Preview(showBackground = true, name = "1. 지출 입력 - 금액")
@Composable
private fun ExpenseInputAmountStepPreview() {
    HampouchTheme {
        ExpenseInputRoute(
            todayBalance = 7_300,
            dailyLimit = 20_000,
            onBackClick = {},
            onNoSpendingToday = {},
            onComplete = {},
            form = ExpenseInputFormState(step = 1)
        )
    }
}

@Preview(showBackground = true, name = "2. 지출 입력 - 상세(선택 사항)", heightDp = 1400)
@Composable
private fun ExpenseInputDetailStepPreview() {
    HampouchTheme {
        ExpenseInputRoute(
            todayBalance = 7_300,
            dailyLimit = 20_000,
            onBackClick = {},
            onNoSpendingToday = {},
            onComplete = {},
            form = ExpenseInputFormState(step = 2, amount = 7_900)
        )
    }
}
