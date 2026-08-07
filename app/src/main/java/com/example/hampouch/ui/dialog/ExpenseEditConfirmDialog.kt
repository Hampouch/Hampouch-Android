package com.example.hampouch.ui.dialog

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.hampouch.R
import com.example.hampouch.data.model.ExpenseRecord
import com.example.hampouch.ui.expensedetail.ExpenseSummaryCard
import com.example.hampouch.ui.theme.HampouchTheme
import java.time.LocalDate

@Composable
fun ExpenseEditConfirmDialog(
    record: ExpenseRecord,
    onCancel: () -> Unit,
    onConfirm: () -> Unit
) {
    Dialog(
        onDismissRequest = onCancel,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        ExpenseEditConfirmDialogCard(record = record, onCancel = onCancel, onConfirm = onConfirm)
    }
}

@Composable
private fun ExpenseEditConfirmDialogCard(
    record: ExpenseRecord,
    onCancel: () -> Unit,
    onConfirm: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        ExpenseSummaryCard(record = record)
        Spacer(modifier = Modifier.height(29.dp))
        ConfirmActionCard(
            question = stringResource(R.string.expensedetail_edit_question),
            confirmLabel = stringResource(R.string.expensedetail_edit_button),
            onCancel = onCancel,
            onConfirm = onConfirm
        )
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

@Preview(showBackground = true, backgroundColor = 0xFFB0B0B0)
@Composable
private fun ExpenseEditConfirmDialogPreview() {
    HampouchTheme {
        ExpenseEditConfirmDialogCard(
            record = PreviewExpenseRecord,
            onCancel = {},
            onConfirm = {}
        )
    }
}
