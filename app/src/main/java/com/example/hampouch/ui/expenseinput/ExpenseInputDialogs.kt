package com.example.hampouch.ui.expenseinput

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.hampouch.R
import com.example.hampouch.domain.model.ExpenseRecord
import com.example.hampouch.ui.dialog.ConfirmActionCard
import com.example.hampouch.ui.expensedetail.DashedDivider
import com.example.hampouch.ui.expensedetail.ExpensePhotoViewRow
import com.example.hampouch.ui.expensedetail.ExpenseSummaryCard
import com.example.hampouch.ui.theme.HPBlack
import com.example.hampouch.ui.theme.HPText
import com.example.hampouch.ui.theme.HPWhite
import com.example.hampouch.ui.theme.HampouchTheme
import java.time.LocalDate

@Composable
fun ExpenseInputSkipPromptDialog(
    question: String,
    confirmLabel: String,
    onCancel: () -> Unit,
    onSkip: () -> Unit
) {
    Dialog(
        onDismissRequest = onCancel,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Column(modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 25.dp, vertical = 30.dp)) {
            ConfirmActionCard(
                question = question,
                confirmLabel = confirmLabel,
                onCancel = onCancel,
                onConfirm = onSkip
            )
        }
    }
}

@Composable
private fun ExpenseInputSaveDetailCard(
    memo: String?,
    photoUris: List<String>,
    modifier: Modifier = Modifier
) {
    if (memo.isNullOrBlank() && photoUris.isEmpty()) return
    Column(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(20.dp))
            .background(HPWhite)
            .padding(20.dp)
    ) {
        if (!memo.isNullOrBlank()) {
            Text(
                stringResource(R.string.expenseinput_memo_label),
                style = MaterialTheme.typography.bodyMedium,
                color = HPText
            )
            Spacer(modifier = Modifier.height(6.dp))
            Text(memo, style = MaterialTheme.typography.bodyMedium, color = HPBlack)
        }
        if (!memo.isNullOrBlank() && photoUris.isNotEmpty()) {
            Spacer(modifier = Modifier.height(10.dp))
            DashedDivider(color = HPBlack)
            Spacer(modifier = Modifier.height(10.dp))
        }
        if (photoUris.isNotEmpty()) {
            Text(
                stringResource(R.string.expenseinput_photo_label),
                style = MaterialTheme.typography.bodyMedium,
                color = HPText
            )
            Spacer(modifier = Modifier.height(10.dp))
            ExpensePhotoViewRow(photoUris = photoUris)
        }
    }
}

@Composable
fun ExpenseInputSaveConfirmDialog(
    record: ExpenseRecord,
    onCancel: () -> Unit,
    onSave: () -> Unit
) {
    Dialog(
        onDismissRequest = onCancel,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Column(modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp)) {
            ExpenseSummaryCard(record = record)
            val hasDetail = !record.memo.isNullOrBlank() || record.photoUris.isNotEmpty()
            if (hasDetail) {
                Spacer(modifier = Modifier.height(12.dp))
                ExpenseInputSaveDetailCard(memo = record.memo, photoUris = record.photoUris)
            }
            Spacer(modifier = Modifier.height(29.dp))
            ConfirmActionCard(
                question = stringResource(R.string.expenseinput_save_question),
                confirmLabel = stringResource(R.string.expenseinput_save_button),
                onCancel = onCancel,
                onConfirm = onSave
            )
        }
    }
}

private val PreviewExpenseRecord = ExpenseRecord(
    id = "preview",
    date = LocalDate.now(),
    amount = 4_500,
    categoryId = "cafe",
    expenseName = "스타벅스",
    reasonId = "reward"
)

private val PreviewExpenseRecordWithMemo = PreviewExpenseRecord.copy(
    memo = "피곤했는데 기분을 풀기 위해 나 자신에게 선물했다."
)

@Preview(showBackground = true, backgroundColor = 0xFFB0B0B0, name = "2. 지출 건너뛰기 안내")
@Composable
private fun ExpenseInputSkipPromptDialogPreview() {
    HampouchTheme {
        ExpenseInputSkipPromptDialog(
            question = stringResource(R.string.expenseinput_skip_prompt_title),
            confirmLabel = stringResource(R.string.expenseinput_skip_prompt_confirm),
            onCancel = {},
            onSkip = {}
        )
    }
}

@Preview(showBackground = true, backgroundColor = 0xFFB0B0B0, name = "저장 확인 - 기본")
@Composable
private fun ExpenseInputSaveConfirmDialogPreview() {
    HampouchTheme {
        ExpenseInputSaveConfirmDialog(record = PreviewExpenseRecord, onCancel = {}, onSave = {})
    }
}

@Preview(showBackground = true, backgroundColor = 0xFFB0B0B0, name = "저장 확인 - 메모 포함")
@Composable
private fun ExpenseInputSaveConfirmDialogWithMemoPreview() {
    HampouchTheme {
        ExpenseInputSaveConfirmDialog(
            record = PreviewExpenseRecordWithMemo,
            onCancel = {},
            onSave = {})
    }
}
