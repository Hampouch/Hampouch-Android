package com.example.hampouch.ui.dialog

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.hampouch.R
import com.example.hampouch.ui.theme.HPBlack
import com.example.hampouch.ui.theme.HPMain
import com.example.hampouch.ui.theme.HPSub3
import com.example.hampouch.ui.theme.HPSub4
import com.example.hampouch.ui.theme.HPText
import com.example.hampouch.ui.theme.HPWhite
import com.example.hampouch.ui.theme.HampouchTheme

@Composable
fun MissingExpenseReminderDialog(
    onInputNowClick: () -> Unit,
    onNoSpendingTodayClick: () -> Unit,
    onLaterClick: () -> Unit
) {
    Dialog(
        onDismissRequest = onLaterClick,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        MissingExpenseReminderDialogCard(
            onInputNowClick = onInputNowClick,
            onNoSpendingTodayClick = onNoSpendingTodayClick,
            onLaterClick = onLaterClick
        )
    }
}

@Composable
private fun MissingExpenseReminderDialogCard(
    onInputNowClick: () -> Unit,
    onNoSpendingTodayClick: () -> Unit,
    onLaterClick: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(24.dp))
                .background(HPSub3)
                .padding(horizontal = 24.dp, vertical = 28.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                stringResource(R.string.home_missing_expense_title),
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold,
                color = HPBlack,
                textAlign = TextAlign.Center
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                stringResource(R.string.home_missing_expense_subtitle_line1),
                style = MaterialTheme.typography.bodySmall,
                color = HPText,
                textAlign = TextAlign.Center
            )
            Text(
                stringResource(R.string.home_missing_expense_subtitle_line2),
                style = MaterialTheme.typography.bodySmall,
                color = HPText,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(24.dp))
            Button(
                onClick = onInputNowClick,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                shape = RoundedCornerShape(10.dp),
                colors = ButtonDefaults.buttonColors(containerColor = HPMain)
            ) {
                Text(
                    stringResource(R.string.home_missing_expense_input_now_button),
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.Bold,
                    color = HPWhite
                )
            }

            Spacer(modifier = Modifier.height(10.dp))
            Button(
                onClick = onNoSpendingTodayClick,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                shape = RoundedCornerShape(10.dp),
                colors = ButtonDefaults.buttonColors(containerColor = HPWhite)
            ) {
                Text(
                    stringResource(R.string.home_missing_expense_no_spending_button),
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.Bold,
                    color = HPText
                )
            }

            Spacer(modifier = Modifier.height(14.dp))
            Text(
                stringResource(R.string.home_missing_expense_later_link),
                style = MaterialTheme.typography.bodyMedium,
                color = HPText,
                modifier = Modifier.clickable(onClick = onLaterClick)
            )
        }
    }
}

@Preview(showBackground = true, backgroundColor = 0xFFB0B0B0)
@Composable
private fun MissingExpenseReminderDialogPreview() {
    HampouchTheme {
        MissingExpenseReminderDialogCard(
            onInputNowClick = {},
            onNoSpendingTodayClick = {},
            onLaterClick = {}
        )
    }
}
