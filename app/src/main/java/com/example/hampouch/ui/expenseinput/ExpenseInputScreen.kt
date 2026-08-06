package com.example.hampouch.ui.expenseinput

import android.R.attr.top
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
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
import com.example.hampouch.data.model.ExpenseRecord
import com.example.hampouch.ui.expensedetail.ChoiceChip
import com.example.hampouch.ui.expensedetail.ExpensePhotoEditSection
import com.example.hampouch.ui.expensedetail.ExpenseReasonCatalog
import com.example.hampouch.ui.expensedetail.ExpenseTextField
import com.example.hampouch.ui.expensedetail.ThreeColumnChipGrid
import com.example.hampouch.ui.expensedetail.formatWon
import com.example.hampouch.ui.home.HomeCategoryCatalog
import com.example.hampouch.ui.theme.HPBlack
import com.example.hampouch.ui.theme.HPMain
import com.example.hampouch.ui.theme.HPSub4
import com.example.hampouch.ui.theme.HPText
import com.example.hampouch.ui.theme.HPWhite
import com.example.hampouch.ui.theme.HampouchTheme
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneOffset
import java.time.format.DateTimeFormatter
import java.util.UUID

const val ExpenseInputTotalSteps = 4
private const val MaxExpenseAmount = 999_999_999L
private const val ExpenseInputPhotoMaxCount = 5

private val inputDateFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd")

private fun LocalDate.toEpochMillisUtc(): Long =
    atStartOfDay(ZoneOffset.UTC).toInstant().toEpochMilli()

private fun Long.toLocalDateUtc(): LocalDate =
    Instant.ofEpochMilli(this).atZone(ZoneOffset.UTC).toLocalDate()

private sealed interface InputCategoryOption {
    data class Preset(val category: HomeCategoryCatalog.Category) : InputCategoryOption
    data object CustomInput : InputCategoryOption
}

private val inputCategoryOptions: List<InputCategoryOption> =
    HomeCategoryCatalog.categories.map { InputCategoryOption.Preset(it) } + InputCategoryOption.CustomInput

private val categoryChipIconRes: Map<String, Int> = mapOf(
    "delivery" to R.drawable.icon_delivery,
    "dining_out" to R.drawable.icon_eatout,
    "convenience" to R.drawable.icon_conv,
    "cafe" to R.drawable.icon_cafe,
    "snack" to R.drawable.icon_snack,
    "mart" to R.drawable.icon_shopping,
    "drink" to R.drawable.icon_beer,
    "etc" to R.drawable.icon_etc
)

private val presetReasons: List<ExpenseReasonCatalog.Reason> = ExpenseReasonCatalog.reasons

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ExpenseInputRoute(
    todayBalance: Int,
    dailyLimit: Int,
    onBackClick: () -> Unit,
    onNoSpendingToday: () -> Unit,
    onComplete: (ExpenseRecord) -> Unit,
    modifier: Modifier = Modifier,
    initialDate: LocalDate = LocalDate.now(),
    initialStep: Int = 1
) {
    var step by remember { mutableIntStateOf(initialStep) }
    var date by remember { mutableStateOf(initialDate) }
    var amount by remember { mutableStateOf(0) }
    var expenseName by remember { mutableStateOf("") }
    var categoryId by remember { mutableStateOf<String?>(null) }
    var isCustomCategory by remember { mutableStateOf(false) }
    var customCategoryText by remember { mutableStateOf("") }
    var reasonId by remember { mutableStateOf<String?>(null) }
    var isCustomReason by remember { mutableStateOf(false) }
    var customReasonText by remember { mutableStateOf("") }
    var memo by remember { mutableStateOf("") }
    var photoUris by remember { mutableStateOf(listOf<String>()) }

    var showMaxAmountError by remember { mutableStateOf(false) }
    var showDatePicker by remember { mutableStateOf(false) }
    var showSkipPromptDialog by remember { mutableStateOf(false) }
    var showCategorySkipPromptDialog by remember { mutableStateOf(false) }
    var showReasonSkipPromptDialog by remember { mutableStateOf(false) }
    var showReasonNextConfirmDialog by remember { mutableStateOf(false) }
    var showMemoSkipPromptDialog by remember { mutableStateOf(false) }
    var showCategoryCustomDialog by remember { mutableStateOf(false) }
    var showReasonCustomDialog by remember { mutableStateOf(false) }
    var showSaveConfirmDialog by remember { mutableStateOf(false) }

    val balanceAfterExpense = todayBalance - amount
    val isCategoryStepValid = expenseName.isNotBlank() &&
        (categoryId != null || (isCustomCategory && customCategoryText.isNotBlank()))
    val hasReasonSelected = reasonId != null || isCustomReason
    val reasonNoneLabel = stringResource(R.string.expenseinput_reason_none_tag)

    fun handleAmountDigit(digit: String) {
        val currentText = if (amount == 0) "" else amount.toString()
        val nextText = (currentText + digit).trimStart('0').ifEmpty { "0" }
        val parsed = nextText.toLongOrNull() ?: return
        if (parsed > MaxExpenseAmount) {
            showMaxAmountError = true
        } else {
            amount = parsed.toInt()
            showMaxAmountError = false
        }
    }

    fun handleAmountDelete() {
        amount /= 10
        showMaxAmountError = false
    }

    fun buildRecord() = ExpenseRecord(
        id = UUID.randomUUID().toString(),
        date = date,
        amount = amount,
        categoryId = if (isCustomCategory) null else categoryId,
        customCategoryName = if (isCustomCategory) customCategoryText.ifBlank { null } else null,
        expenseName = expenseName.ifBlank { null },
        reasonId = if (isCustomReason) null else reasonId,
        customReason = if (isCustomReason) customReasonText.ifBlank { null } else null,
        memo = memo.ifBlank { null },
        photoUris = photoUris
    )

    Scaffold(
        modifier = modifier,
        topBar = {
            ExpenseInputTopBar(
                title = stringResource(R.string.expenseinput_title),
                onBackClick = { if (step > 1) step-- else onBackClick() },
                containerColor = if (step == 1) HPSub4 else HPWhite
            )
        },
        containerColor = HPWhite
    ) { innerPadding ->
        if (step == 1) {
            ExpenseInputAmountStep(
                modifier = Modifier
                    .padding(innerPadding)
                    .fillMaxSize(),
                date = date,
                onDateClick = { showDatePicker = true },
                balance = todayBalance,
                dailyLimit = dailyLimit,
                amount = amount,
                onDigit = ::handleAmountDigit,
                onDelete = ::handleAmountDelete,
                showMaxAmountError = showMaxAmountError,
                onNoSpendingToday = { showSkipPromptDialog = true },
                primaryButton = {
                    ExpenseInputPrimaryButton(
                        label = stringResource(R.string.expenseinput_next_button),
                        enabled = amount > 0 && !showMaxAmountError,
                        onClick = { step++ }
                    )
                }
            )
            return@Scaffold
        }

        Column(
            modifier = Modifier
                .padding(innerPadding)
                .fillMaxSize()
                .padding(horizontal = 20.dp)
        ) {
            Column(
                modifier = Modifier
                    .weight(1f)
                    .verticalScroll(rememberScrollState())
            ) {
                when (step) {
                    2 -> ExpenseInputCategoryStep(
                        amount = amount,
                        balanceAfterExpense = balanceAfterExpense,
                        expenseName = expenseName,
                        onExpenseNameChange = { expenseName = it },
                        categoryId = categoryId,
                        isCustomCategory = isCustomCategory,
                        customCategoryText = customCategoryText,
                        onCategorySelected = {
                            categoryId = it
                            isCustomCategory = false
                        },
                        onCustomCategoryClick = { showCategoryCustomDialog = true }
                    )

                    3 -> ExpenseInputReasonStep(
                        amount = amount,
                        balanceAfterExpense = balanceAfterExpense,
                        expenseName = expenseName,
                        categoryId = categoryId,
                        isCustomCategory = isCustomCategory,
                        customCategoryText = customCategoryText,
                        reasonId = reasonId,
                        isCustomReason = isCustomReason,
                        customReasonText = customReasonText,
                        onReasonSelected = {
                            reasonId = it
                            isCustomReason = false
                        },
                        onCustomReasonClick = { showReasonCustomDialog = true }
                    )

                    else -> ExpenseInputMemoStep(
                        memo = memo,
                        onMemoChange = { memo = it },
                        photoUris = photoUris,
                        onPhotosAdded = { added ->
                            photoUris = (photoUris + added).take(ExpenseInputPhotoMaxCount)
                        },
                        onPhotosRemoved = { removed ->
                            photoUris = photoUris.filterIndexed { index, _ -> index !in removed }
                        },
                        onPhotoReplaced = { index, newUri ->
                            photoUris = photoUris.toMutableList().also { it[index] = newUri }
                        }
                    )
                }
            }
            ExpenseInputSkipRestLink(
                onClick = {
                    when (step) {
                        2 -> showCategorySkipPromptDialog = true
                        3 -> showReasonSkipPromptDialog = true
                        4 -> showMemoSkipPromptDialog = true
                        else -> showSaveConfirmDialog = true
                    }
                }
            )
            Spacer(modifier = Modifier.height(10.dp))
            ExpenseInputPrimaryButton(
                label = stringResource(R.string.expenseinput_next_button),
                enabled = when (step) {
                    2 -> isCategoryStepValid
                    3 -> hasReasonSelected
                    else -> true
                },
                onClick = {
                    when {
                        step == 3 -> showReasonNextConfirmDialog = true
                        step < ExpenseInputTotalSteps -> step++
                        else -> showSaveConfirmDialog = true
                    }
                }
            )
            Spacer(modifier = Modifier.height(20.dp))
        }
    }

    if (showDatePicker) {
        val datePickerState =
            rememberDatePickerState(initialSelectedDateMillis = date.toEpochMillisUtc())
        DatePickerDialog(
            onDismissRequest = { showDatePicker = false },
            confirmButton = {
                TextButton(onClick = {
                    datePickerState.selectedDateMillis?.let { millis ->
                        date = millis.toLocalDateUtc()
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
                amount = 0
                step = 2
            }
        )
    }

    if (showCategorySkipPromptDialog) {
        ExpenseInputSkipPromptDialog(
            question = stringResource(R.string.expenseinput_category_skip_prompt_title),
            confirmLabel = stringResource(R.string.expenseinput_category_skip_prompt_confirm),
            onCancel = { showCategorySkipPromptDialog = false },
            onSkip = {
                showCategorySkipPromptDialog = false
                expenseName = ""
                categoryId = "etc"
                isCustomCategory = false
                step = 3
            }
        )
    }

    if (showReasonSkipPromptDialog) {
        ExpenseInputSaveConfirmDialog(
            record = buildRecord().copy(customReason = reasonNoneLabel),
            onCancel = { showReasonSkipPromptDialog = false },
            onSave = {
                showReasonSkipPromptDialog = false
                isCustomReason = true
                customReasonText = reasonNoneLabel
                step = 4
            }
        )
    }

    if (showReasonNextConfirmDialog) {
        ExpenseInputSaveConfirmDialog(
            record = buildRecord(),
            onCancel = { showReasonNextConfirmDialog = false },
            onSave = {
                showReasonNextConfirmDialog = false
                step = 4
            }
        )
    }

    if (showMemoSkipPromptDialog) {
        ExpenseInputSaveConfirmDialog(
            record = buildRecord().copy(memo = null, photoUris = emptyList()),
            onCancel = { showMemoSkipPromptDialog = false },
            onSave = {
                showMemoSkipPromptDialog = false
                memo = ""
                photoUris = emptyList()
                onComplete(buildRecord().copy(memo = null, photoUris = emptyList()))
            }
        )
    }

    if (showCategoryCustomDialog) {
        ExpenseInputTextEntryDialog(
            label = stringResource(R.string.expenseinput_category_label),
            placeholder = stringResource(R.string.expenseinput_category_placeholder),
            value = customCategoryText,
            onValueChange = { customCategoryText = it },
            onCancel = { showCategoryCustomDialog = false },
            onConfirm = {
                isCustomCategory = true
                categoryId = null
                showCategoryCustomDialog = false
            }
        )
    }

    if (showReasonCustomDialog) {
        ExpenseInputTextEntryDialog(
            label = stringResource(R.string.expenseinput_reason_label),
            placeholder = stringResource(R.string.expenseinput_reason_placeholder),
            value = customReasonText,
            onValueChange = { customReasonText = it },
            onCancel = { showReasonCustomDialog = false },
            onConfirm = {
                isCustomReason = true
                reasonId = null
                showReasonCustomDialog = false
            }
        )
    }

    if (showSaveConfirmDialog) {
        ExpenseInputSaveConfirmDialog(
            record = buildRecord(),
            onCancel = { showSaveConfirmDialog = false },
            onSave = {
                showSaveConfirmDialog = false
                onComplete(buildRecord())
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
    Column(modifier = modifier.background(HPSub4)) {
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
private fun ExpenseInputCategoryStep(
    amount: Int,
    balanceAfterExpense: Int,
    expenseName: String,
    onExpenseNameChange: (String) -> Unit,
    categoryId: String?,
    isCustomCategory: Boolean,
    customCategoryText: String,
    onCategorySelected: (String) -> Unit,
    onCustomCategoryClick: () -> Unit,
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
            stringResource(R.string.expenseinput_category_description),
            style = MaterialTheme.typography.bodySmall,
            color = HPText
        )
        Spacer(modifier = Modifier.height(30.dp))
        ExpenseInputAmountSummaryCard(amount = amount, balanceAfterExpense = balanceAfterExpense)
        Spacer(modifier = Modifier.height(30.dp))
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
        Spacer(modifier = Modifier.height(30.dp))
        Text(
            stringResource(R.string.expensedetail_field_category),
            style = MaterialTheme.typography.bodyMedium,
            color = HPBlack
        )
        Spacer(modifier = Modifier.height(10.dp))
        ThreeColumnChipGrid(items = inputCategoryOptions) { option ->
            when (option) {
                is InputCategoryOption.Preset -> ChoiceChip(
                    label = stringResource(option.category.labelResId),
                    iconRes = categoryChipIconRes[option.category.id],
                    iconSize = 30.dp,
                    iconSpacing = 10.dp,
                    selected = !isCustomCategory && categoryId == option.category.id,
                    onClick = { onCategorySelected(option.category.id) }
                )

                InputCategoryOption.CustomInput -> ChoiceChip(
                    label = if (isCustomCategory && customCategoryText.isNotBlank()) {
                        customCategoryText
                    } else {
                        stringResource(R.string.expensedetail_option_custom_input)
                    },
                    selected = isCustomCategory,
                    onClick = onCustomCategoryClick
                )
            }
        }
    }
}

@Composable
private fun ExpenseInputReasonStep(
    amount: Int,
    balanceAfterExpense: Int,
    expenseName: String,
    categoryId: String?,
    isCustomCategory: Boolean,
    customCategoryText: String,
    reasonId: String?,
    isCustomReason: Boolean,
    customReasonText: String,
    onReasonSelected: (String) -> Unit,
    onCustomReasonClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val category = HomeCategoryCatalog.byId(categoryId)
    Column(
        modifier = modifier
            .fillMaxWidth()
    ) {
        ExpenseInputOptionalBadge()
        Spacer(modifier = Modifier.height(6.dp))
        Text(
            stringResource(R.string.expenseinput_reason_question),
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color = HPBlack
        )
        Spacer(modifier = Modifier.height(6.dp))
        Text(
            stringResource(R.string.expenseinput_reason_description_line1),
            style = MaterialTheme.typography.bodySmall,
            color = HPText
        )
        Text(
            stringResource(R.string.expenseinput_reason_description_line2),
            style = MaterialTheme.typography.bodySmall,
            color = HPText
        )
        Spacer(modifier = Modifier.height(30.dp))
        val categoryLabel = when {
            isCustomCategory -> customCategoryText.ifBlank { null }
            category != null -> stringResource(category.labelResId)
            else -> null
        }
        ExpenseInputAmountSummaryCard(
            amount = amount,
            balanceAfterExpense = balanceAfterExpense,
            header = if (expenseName.isNotBlank() || categoryLabel != null) {
                {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        if (expenseName.isNotBlank()) {
                            Text(
                                expenseName,
                                style = MaterialTheme.typography.bodyLarge,
                                color = HPText
                            )
                        }
                        Spacer(modifier = Modifier.weight(1f))
                        if (categoryLabel != null) {
                            ExpenseInputCategoryBadge(
                                iconRes = categoryChipIconRes[categoryId] ?: R.drawable.icon_etc,
                                label = categoryLabel
                            )
                        }
                    }
                }
            } else {
                null
            }
        )
        Spacer(modifier = Modifier.height(24.dp))
        Text(
            stringResource(R.string.expenseinput_reason_pick_one),
            style = MaterialTheme.typography.bodyMedium,
            color = HPText
        )
        Spacer(modifier = Modifier.height(12.dp))
        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
            presetReasons.chunked(2).forEach { pair ->
                Row(
                    modifier = Modifier.fillMaxWidth(),
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
                modifier = Modifier.fillMaxWidth()
            )
        }
    }
}

@Composable
private fun ExpenseInputMemoStep(
    memo: String,
    onMemoChange: (String) -> Unit,
    photoUris: List<String>,
    onPhotosAdded: (List<String>) -> Unit,
    onPhotosRemoved: (Set<Int>) -> Unit,
    onPhotoReplaced: (index: Int, newUri: String) -> Unit,
    modifier: Modifier = Modifier
) {
    val pickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickMultipleVisualMedia(maxItems = ExpenseInputPhotoMaxCount)
    ) { uris -> if (uris.isNotEmpty()) onPhotosAdded(uris.map { it.toString() }) }

    Column(
        modifier = modifier
            .fillMaxWidth()
    ) {
        ExpenseInputOptionalBadge()
        Spacer(modifier = Modifier.height(6.dp))
        Text(
            stringResource(R.string.expenseinput_memo_question),
            style = MaterialTheme.typography.titleSmall,
            fontWeight = FontWeight.Bold,
            color = HPBlack
        )
        Spacer(modifier = Modifier.height(6.dp))
        Text(
            stringResource(R.string.expenseinput_memo_description_line1),
            style = MaterialTheme.typography.bodySmall,
            color = HPText
        )
        Text(
            stringResource(R.string.expenseinput_memo_description_line2),
            style = MaterialTheme.typography.bodySmall,
            color = HPText
        )
        Spacer(modifier = Modifier.height(20.dp))
        ExpenseTextField(
            value = memo,
            onValueChange = onMemoChange,
            placeholder = stringResource(R.string.expenseinput_memo_placeholder),
            singleLine = false,
            minLines = 5
        )
        Spacer(modifier = Modifier.height(30.dp))
        if (photoUris.isEmpty()) {
            ExpenseInputPhotoAttachCard(
                onClick = {
                    pickerLauncher.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly))
                }
            )
        } else {
            ExpensePhotoEditSection(
                photoUris = photoUris,
                onPhotosAdded = onPhotosAdded,
                onPhotosRemoved = onPhotosRemoved,
                onPhotoReplaced = onPhotoReplaced,
                maxCount = ExpenseInputPhotoMaxCount
            )
        }
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
            initialStep = 1
        )
    }
}

@Preview(showBackground = true, name = "2. 지출 입력 - 카테고리")
@Composable
private fun ExpenseInputCategoryStepPreview() {
    HampouchTheme {
        ExpenseInputRoute(
            todayBalance = 7_300,
            dailyLimit = 20_000,
            onBackClick = {},
            onNoSpendingToday = {},
            onComplete = {},
            initialStep = 2
        )
    }
}

@Preview(showBackground = true, name = "3. 지출 입력 - 이유")
@Composable
private fun ExpenseInputReasonStepPreview() {
    HampouchTheme {
        ExpenseInputRoute(
            todayBalance = 7_300,
            dailyLimit = 20_000,
            onBackClick = {},
            onNoSpendingToday = {},
            onComplete = {},
            initialStep = 3
        )
    }
}

@Preview(showBackground = true, name = "4. 지출 입력 - 메모/사진")
@Composable
private fun ExpenseInputMemoStepPreview() {
    HampouchTheme {
        ExpenseInputRoute(
            todayBalance = 7_300,
            dailyLimit = 20_000,
            onBackClick = {},
            onNoSpendingToday = {},
            onComplete = {},
            initialStep = 4
        )
    }
}
