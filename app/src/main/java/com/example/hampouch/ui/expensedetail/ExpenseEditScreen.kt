package com.example.hampouch.ui.expensedetail

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
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
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.hampouch.R
import com.example.hampouch.data.model.ExpenseRecord
import com.example.hampouch.ui.dialog.ExpenseEditConfirmDialog
import com.example.hampouch.ui.home.HomeCategoryCatalog
import com.example.hampouch.ui.theme.HPMain
import com.example.hampouch.ui.theme.HPSub4
import com.example.hampouch.ui.theme.HPWhite
import com.example.hampouch.ui.theme.HampouchTheme
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneOffset
import java.time.format.DateTimeFormatter

private val editDateFormatter = DateTimeFormatter.ofPattern("yyyy.MM.dd")

private fun LocalDate.toEpochMillisUtc(): Long = atStartOfDay(ZoneOffset.UTC).toInstant().toEpochMilli()

private fun Long.toLocalDateUtc(): LocalDate = Instant.ofEpochMilli(this).atZone(ZoneOffset.UTC).toLocalDate()

private sealed interface CategoryOption {
    data class Preset(val category: HomeCategoryCatalog.Category) : CategoryOption
    data object CustomInput : CategoryOption
}

private val categoryOptions: List<CategoryOption> =
    HomeCategoryCatalog.categories.map { CategoryOption.Preset(it) } + CategoryOption.CustomInput

private sealed interface ReasonOption {
    data class Preset(val reason: ExpenseReasonCatalog.Reason) : ReasonOption
    data object CustomInput : ReasonOption
}

private val reasonOptions: List<ReasonOption> =
    ExpenseReasonCatalog.reasons.map { ReasonOption.Preset(it) } + ReasonOption.CustomInput

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ExpenseEditRoute(
    record: ExpenseRecord,
    onBackClick: () -> Unit,
    onSaved: (ExpenseRecord) -> Unit,
    modifier: Modifier = Modifier
) {
    var date by remember(record.id) { mutableStateOf(record.date) }
    var amount by remember(record.id) { mutableStateOf(record.amount) }
    var expenseName by remember(record.id) { mutableStateOf(record.expenseName.orEmpty()) }
    var categoryId by remember(record.id) { mutableStateOf(record.categoryId) }
    var isCustomCategory by remember(record.id) { mutableStateOf(record.customCategoryName != null) }
    var customCategoryText by remember(record.id) { mutableStateOf(record.customCategoryName.orEmpty()) }
    var reasonId by remember(record.id) { mutableStateOf(record.reasonId) }
    var isCustomReason by remember(record.id) { mutableStateOf(record.customReason != null) }
    var customReasonText by remember(record.id) { mutableStateOf(record.customReason.orEmpty()) }
    var memo by remember(record.id) { mutableStateOf(record.memo.orEmpty()) }
    var photoUris by remember(record.id) { mutableStateOf(record.photoUris) }
    var showConfirmDialog by remember { mutableStateOf(false) }
    var showDatePicker by remember { mutableStateOf(false) }

    val pendingRecord = record.copy(
        date = date,
        amount = amount,
        expenseName = expenseName.ifBlank { null },
        categoryId = if (isCustomCategory) null else categoryId,
        customCategoryName = if (isCustomCategory) customCategoryText.ifBlank { null } else null,
        reasonId = if (isCustomReason) null else reasonId,
        customReason = if (isCustomReason) customReasonText.ifBlank { null } else null,
        memo = memo.ifBlank { null },
        photoUris = photoUris
    )

    Scaffold(
        modifier = modifier,
        topBar = { ExpenseDetailTopBar(onBackClick = onBackClick) },
        containerColor = HPSub4
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .padding(innerPadding)
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp)
        ) {
            Spacer(modifier = Modifier.height(16.dp))
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(20.dp))
                    .background(HPWhite)
                    .padding(20.dp),
                verticalArrangement = Arrangement.spacedBy(22.dp)
            ) {
                ExpenseFormSection(label = stringResource(R.string.expensedetail_field_date)) {
                    ExpenseDateField(label = date.format(editDateFormatter), onClick = { showDatePicker = true })
                }
                ExpenseFormSection(label = stringResource(R.string.expensedetail_field_amount)) {
                    AmountTextField(amount = amount, onAmountChange = { amount = it })
                }
                ExpenseFormSection(label = stringResource(R.string.expensedetail_field_expense_name)) {
                    ExpenseTextField(
                        value = expenseName,
                        onValueChange = { expenseName = it },
                        placeholder = stringResource(R.string.expensedetail_expense_name_placeholder)
                    )
                }
                ExpenseFormSection(label = stringResource(R.string.expensedetail_field_category)) {
                    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        ThreeColumnChipGrid(items = categoryOptions) { option ->
                            when (option) {
                                is CategoryOption.Preset -> ChoiceChip(
                                    label = stringResource(option.category.labelResId),
                                    icon = option.category.icon,
                                    iconTint = option.category.accentColor,
                                    selected = !isCustomCategory && categoryId == option.category.id,
                                    onClick = {
                                        categoryId = option.category.id
                                        isCustomCategory = false
                                    }
                                )

                                CategoryOption.CustomInput -> ChoiceChip(
                                    label = stringResource(R.string.expensedetail_option_custom_input),
                                    selected = isCustomCategory,
                                    onClick = {
                                        isCustomCategory = true
                                        categoryId = null
                                    }
                                )
                            }
                        }
                        if (isCustomCategory) {
                            ExpenseTextField(
                                value = customCategoryText,
                                onValueChange = { customCategoryText = it },
                                placeholder = stringResource(R.string.expensedetail_custom_category_placeholder)
                            )
                        }
                    }
                }
                ExpenseFormSection(label = stringResource(R.string.expensedetail_field_reason)) {
                    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        ThreeColumnChipGrid(items = reasonOptions) { option ->
                            when (option) {
                                is ReasonOption.Preset -> ChoiceChip(
                                    label = stringResource(option.reason.labelResId),
                                    selected = !isCustomReason && reasonId == option.reason.id,
                                    onClick = {
                                        reasonId = option.reason.id
                                        isCustomReason = false
                                    }
                                )

                                ReasonOption.CustomInput -> ChoiceChip(
                                    label = stringResource(R.string.expensedetail_option_custom_input),
                                    selected = isCustomReason,
                                    onClick = {
                                        isCustomReason = true
                                        reasonId = null
                                    }
                                )
                            }
                        }
                        if (isCustomReason) {
                            ExpenseTextField(
                                value = customReasonText,
                                onValueChange = { customReasonText = it },
                                placeholder = stringResource(R.string.expensedetail_custom_reason_placeholder)
                            )
                        }
                    }
                }
                ExpenseFormSection(label = stringResource(R.string.expensedetail_field_memo)) {
                    ExpenseTextField(
                        value = memo,
                        onValueChange = { memo = it },
                        placeholder = stringResource(R.string.expensedetail_memo_placeholder),
                        singleLine = false,
                        minLines = 3
                    )
                }
                ExpensePhotoEditSection(
                    photoUris = photoUris,
                    onPhotosAdded = { added -> photoUris = (photoUris + added).take(ExpensePhotoMaxCount) },
                    onPhotosRemoved = { removed ->
                        photoUris = photoUris.filterIndexed { index, _ -> index !in removed }
                    },
                    onPhotoReplaced = { index, newUri ->
                        photoUris = photoUris.toMutableList().also { it[index] = newUri }
                    }
                )
            }
            Spacer(modifier = Modifier.height(24.dp))
            Button(
                onClick = { showConfirmDialog = true },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(containerColor = HPMain, contentColor = HPWhite)
            ) {
                Text(
                    stringResource(R.string.expensedetail_complete_button),
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.Bold
                )
            }
            Spacer(modifier = Modifier.height(20.dp))
        }
    }

    if (showDatePicker) {
        val datePickerState = rememberDatePickerState(initialSelectedDateMillis = date.toEpochMillisUtc())
        DatePickerDialog(
            onDismissRequest = { showDatePicker = false },
            confirmButton = {
                TextButton(onClick = {
                    datePickerState.selectedDateMillis?.let { millis -> date = millis.toLocalDateUtc() }
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

    if (showConfirmDialog) {
        ExpenseEditConfirmDialog(
            record = pendingRecord,
            onCancel = { showConfirmDialog = false },
            onConfirm = {
                showConfirmDialog = false
                onSaved(pendingRecord)
            }
        )
    }
}

private val PreviewFilledRecord = ExpenseRecord(
    id = "p1",
    date = LocalDate.now(),
    amount = 4_500,
    categoryId = "cafe",
    expenseName = "스타벅스",
    reasonId = "reward",
    memo = "식후 커피 어떻게 참아요..",
    photoUris = listOf(MockPhotoUri)
)

private val PreviewEmptyPhotoRecord = ExpenseRecord(
    id = "p2",
    date = LocalDate.now(),
    amount = 4_500,
    categoryId = "cafe",
    expenseName = "스타벅스",
    reasonId = "stress"
)

@Preview(showBackground = true, name = "6. 지출 수정 (기존 데이터)")
@Composable
private fun ExpenseEditScreenFilledPreview() {
    HampouchTheme {
        ExpenseEditRoute(record = PreviewFilledRecord, onBackClick = {}, onSaved = {})
    }
}

@Preview(showBackground = true, name = "7. 지출 수정 (사진 없음)")
@Composable
private fun ExpenseEditScreenEmptyPhotoPreview() {
    HampouchTheme {
        ExpenseEditRoute(record = PreviewEmptyPhotoRecord, onBackClick = {}, onSaved = {})
    }
}
