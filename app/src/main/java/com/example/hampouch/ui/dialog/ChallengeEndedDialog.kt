package com.example.hampouch.ui.dialog

import androidx.compose.foundation.background
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
import com.example.hampouch.ui.theme.HPSub4
import com.example.hampouch.ui.theme.HPText
import com.example.hampouch.ui.theme.HPWhite
import com.example.hampouch.ui.theme.HampouchTheme

@Composable
fun ChallengeEndedDialog(
    totalDays: Int,
    hasVisitedExpenseEdit: Boolean,
    onEditExpenseClick: () -> Unit,
    onFinishChallengeClick: () -> Unit
) {
    Dialog(
        onDismissRequest = {},
        properties = DialogProperties(usePlatformDefaultWidth = false, dismissOnBackPress = false, dismissOnClickOutside = false)
    ) {
        ChallengeEndedDialogCard(
            totalDays = totalDays,
            hasVisitedExpenseEdit = hasVisitedExpenseEdit,
            onEditExpenseClick = onEditExpenseClick,
            onFinishChallengeClick = onFinishChallengeClick
        )
    }
}

@Composable
private fun ChallengeEndedDialogCard(
    totalDays: Int,
    hasVisitedExpenseEdit: Boolean,
    onEditExpenseClick: () -> Unit,
    onFinishChallengeClick: () -> Unit
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
                .background(HPSub4)
                .padding(horizontal = 24.dp, vertical = 28.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                stringResource(R.string.home_challenge_ended_title_format, totalDays),
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold,
                color = HPMain,
                textAlign = TextAlign.Center
            )
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                stringResource(R.string.home_challenge_ended_subtitle),
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold,
                color = HPBlack,
                textAlign = TextAlign.Center
            )
            Spacer(modifier = Modifier.height(8.dp))
            if (hasVisitedExpenseEdit) {
                Text(
                    stringResource(R.string.home_challenge_ended_after_edit_line1),
                    style = MaterialTheme.typography.bodySmall,
                    color = HPText,
                    textAlign = TextAlign.Center
                )
                Text(
                    stringResource(R.string.home_challenge_ended_after_edit_line2),
                    style = MaterialTheme.typography.bodySmall,
                    color = HPText,
                    textAlign = TextAlign.Center
                )
            } else {
                Text(
                    stringResource(R.string.home_challenge_ended_initial_line1),
                    style = MaterialTheme.typography.bodySmall,
                    color = HPText,
                    textAlign = TextAlign.Center
                )
                Text(
                    stringResource(R.string.home_challenge_ended_initial_line2),
                    style = MaterialTheme.typography.bodySmall,
                    color = HPText,
                    textAlign = TextAlign.Center
                )
            }

            Spacer(modifier = Modifier.height(24.dp))
            Button(
                onClick = onEditExpenseClick,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                shape = RoundedCornerShape(10.dp),
                colors = ButtonDefaults.buttonColors(containerColor = HPMain)
            ) {
                Text(
                    stringResource(R.string.home_challenge_ended_edit_expense_button),
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.Bold,
                    color = HPWhite
                )
            }

            Spacer(modifier = Modifier.height(10.dp))
            Button(
                onClick = onFinishChallengeClick,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                shape = RoundedCornerShape(10.dp),
                colors = ButtonDefaults.buttonColors(containerColor = HPWhite)
            ) {
                Text(
                    stringResource(R.string.home_challenge_ended_finish_button),
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.Bold,
                    color = HPText
                )
            }
        }
    }
}

@Preview(showBackground = true, backgroundColor = 0xFFB0B0B0, name = "3. 챌린지 종료 - 최초")
@Composable
private fun ChallengeEndedDialogInitialPreview() {
    HampouchTheme {
        ChallengeEndedDialogCard(
            totalDays = 14,
            hasVisitedExpenseEdit = false,
            onEditExpenseClick = {},
            onFinishChallengeClick = {}
        )
    }
}

@Preview(showBackground = true, backgroundColor = 0xFFB0B0B0, name = "5. 챌린지 종료 - 수정 후")
@Composable
private fun ChallengeEndedDialogAfterEditPreview() {
    HampouchTheme {
        ChallengeEndedDialogCard(
            totalDays = 14,
            hasVisitedExpenseEdit = true,
            onEditExpenseClick = {},
            onFinishChallengeClick = {}
        )
    }
}
