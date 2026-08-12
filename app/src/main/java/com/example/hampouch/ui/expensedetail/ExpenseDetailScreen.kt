package com.example.hampouch.ui.expensedetail

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.background
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
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
import com.example.hampouch.R
import com.example.hampouch.domain.model.ExpenseRecord
import com.example.hampouch.ui.dialog.ExpenseDeleteConfirmDialog
import com.example.hampouch.ui.theme.HPBlack
import com.example.hampouch.ui.theme.HPMain
import com.example.hampouch.ui.theme.HPSub4
import com.example.hampouch.ui.theme.HPText
import com.example.hampouch.ui.theme.HPWhite
import com.example.hampouch.ui.theme.HampouchTheme
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.time.format.TextStyle
import java.util.Locale

private val expenseDateFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd", Locale.KOREA)

private fun formatDetailDate(date: LocalDate): String {
    val dayOfWeek = date.dayOfWeek.getDisplayName(TextStyle.SHORT, Locale.KOREA)
    return "${date.format(expenseDateFormatter)} ($dayOfWeek)"
}

@Composable
fun ExpenseDetailRoute(
    record: ExpenseRecord,
    onBackClick: () -> Unit,
    onEditClick: () -> Unit,
    onDeleted: () -> Unit,
    modifier: Modifier = Modifier
) {
    var showDeleteDialog by remember { mutableStateOf(false) }

    Scaffold(
        modifier = modifier,
        topBar = {
            ExpenseDetailTopBar(
                onBackClick = onBackClick,
                trailingAction = { ExpenseDeleteIconButton(onClick = { showDeleteDialog = true }) }
            )
        },
        containerColor = HPSub4
    ) { innerPadding ->
        ExpenseDetailContent(
            record = record,
            onEditClick = onEditClick,
            modifier = Modifier.padding(innerPadding)
        )
    }

    if (showDeleteDialog) {
        ExpenseDeleteConfirmDialog(
            record = record,
            onCancel = { showDeleteDialog = false },
            onConfirm = {
                showDeleteDialog = false
                onDeleted()
            }
        )
    }
}

@Composable
private fun ExpenseDetailContent(
    record: ExpenseRecord,
    onEditClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val categoryLabel = resolveCategoryLabel(record.categoryId, record.customCategoryName)
    val reasonLabel = resolveReasonLabel(record.reasonId, record.customReason)

    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 20.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(modifier = Modifier.height(12.dp))
        Text(
            formatDetailDate(record.date),
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.Bold,
            color = HPMain
        )
        Spacer(modifier = Modifier.height(10.dp))
        if (reasonLabel != null) {
            ReasonTagPill(label = reasonLabel)
            Spacer(modifier = Modifier.height(20.dp))
        } else {
            Spacer(modifier = Modifier.height(6.dp))
        }
        CategoryIconCircle(categoryId = record.categoryId, customCategoryName = record.customCategoryName)
        Spacer(modifier = Modifier.height(10.dp))
        Text(categoryLabel, style = MaterialTheme.typography.bodySmall, color = HPText)
        Spacer(modifier = Modifier.height(18.dp))
        if (record.expenseName != null) {
            Text(record.expenseName, style = MaterialTheme.typography.bodyLarge, color = HPBlack)
            Spacer(modifier = Modifier.height(6.dp))
        }
        Text(
            stringResource(R.string.expensedetail_amount_won_format, formatWon(record.amount)),
            style = MaterialTheme.typography.titleMedium,
            color = HPBlack
        )
        Spacer(modifier = Modifier.height(28.dp))
        ExpenseDetailInfoCard(record = record, categoryLabel = categoryLabel)
        Spacer(modifier = Modifier.height(24.dp))
        Button(
            onClick = onEditClick,
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp),
            shape = RoundedCornerShape(12.dp),
            colors = ButtonDefaults.buttonColors(containerColor = HPMain, contentColor = HPWhite)
        ) {
            Text(stringResource(R.string.expensedetail_edit_button), style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.Bold)
        }
        Spacer(modifier = Modifier.height(20.dp))
    }
}

@Composable
private fun ExpenseDetailInfoCard(
    record: ExpenseRecord,
    categoryLabel: String,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(20.dp))
            .background(HPWhite)
            .padding(horizontal = 18.dp, vertical = if (record.hasDetail) 4.dp else 40.dp)
    ) {
        if (!record.hasDetail) {
            Text(
                stringResource(R.string.expensedetail_no_detail_message),
                style = MaterialTheme.typography.bodyMedium,
                color = HPText,
                modifier = Modifier.fillMaxWidth(),
                textAlign = TextAlign.Center
            )
            return@Column
        }

        if (record.expenseName != null) {
            ExpenseInfoRow(label = stringResource(R.string.expensedetail_field_expense_name), value = record.expenseName)
            DashedDivider()
        }
        ExpenseInfoRow(label = stringResource(R.string.expensedetail_field_category), value = categoryLabel)

        if (!record.memo.isNullOrBlank()) {
            DashedDivider()
            Column(modifier = Modifier.fillMaxWidth().padding(vertical = 14.dp)) {
                Text(stringResource(R.string.expensedetail_field_memo), style = MaterialTheme.typography.bodyMedium, color = HPText)
                Spacer(modifier = Modifier.height(8.dp))
                Text(record.memo, style = MaterialTheme.typography.bodyMedium, color = HPBlack)
            }
        }

        if (record.photoUris.isNotEmpty()) {
            DashedDivider()
            Column(modifier = Modifier.fillMaxWidth().padding(vertical = 14.dp)) {
                Text(stringResource(R.string.expensedetail_field_photo), style = MaterialTheme.typography.bodyMedium, color = HPText)
                Spacer(modifier = Modifier.height(10.dp))
                ExpensePhotoViewRow(photoUris = record.photoUris)
            }
        }
    }
}

private val PreviewRecordWithPhoto = ExpenseRecord(
    id = "p1",
    date = LocalDate.now(),
    amount = 4_500,
    categoryId = "cafe",
    expenseName = "스타벅스",
    reasonId = "stress",
    photoUris = listOf(MockPhotoUri)
)

private val PreviewRecordWithMemo = PreviewRecordWithPhoto.copy(
    memo = "식후 커피 어떻게 참아요.."
)

private val PreviewRecordMinimal = ExpenseRecord(
    id = "p2",
    date = LocalDate.now(),
    amount = 4_500
)

@Preview(showBackground = true, name = "2. 지출 상세 (사진, 메모 없음)")
@Composable
private fun ExpenseDetailScreenPreview() {
    HampouchTheme {
        ExpenseDetailRoute(record = PreviewRecordWithPhoto, onBackClick = {}, onEditClick = {}, onDeleted = {})
    }
}

@Preview(showBackground = true, name = "3. 지출 상세 (메모 있음)")
@Composable
private fun ExpenseDetailScreenWithMemoPreview() {
    HampouchTheme {
        ExpenseDetailRoute(record = PreviewRecordWithMemo, onBackClick = {}, onEditClick = {}, onDeleted = {})
    }
}

@Preview(showBackground = true, name = "4. 지출 상세 (금액만 입력)")
@Composable
private fun ExpenseDetailScreenMinimalPreview() {
    HampouchTheme {
        ExpenseDetailRoute(record = PreviewRecordMinimal, onBackClick = {}, onEditClick = {}, onDeleted = {})
    }
}
