package com.example.hampouch.ui.mypage

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.hampouch.R
import com.example.hampouch.ui.mypage.components.ChallengeRecordCard
import com.example.hampouch.ui.mypage.components.MyPageDetailTopBar
import com.example.hampouch.ui.theme.HPGray2
import com.example.hampouch.ui.theme.HPText
import com.example.hampouch.ui.theme.HampouchTheme

@Composable
fun ChallengeHistoryScreen(
    records: List<ChallengeRecord>,
    onBackClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val title = if (records.isEmpty()) {
        stringResource(R.string.challenge_history_title_empty)
    } else {
        stringResource(R.string.challenge_history_title_filled)
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(HPGray2)
    ) {
        MyPageDetailTopBar(
            title = title,
            onBackClick = onBackClick,
            showMoreMenu = records.isNotEmpty(),
            modifier = Modifier.padding(horizontal = 8.dp)
        )
        if (records.isEmpty()) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text(
                    text = stringResource(R.string.challenge_history_empty_message),
                    style = MaterialTheme.typography.bodyMedium,
                    color = HPText
                )
            }
        } else {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = 20.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                records.forEach { record -> ChallengeRecordCard(record = record) }
            }
        }
    }
}

@Preview(showBackground = true, name = "9. 지난 챌린지 기록 - 목록")
@Composable
private fun ChallengeHistoryScreenFilledPreview() {
    HampouchTheme {
        ChallengeHistoryScreen(records = MyPageMockData.challengeHistory(), onBackClick = {})
    }
}

@Preview(showBackground = true, name = "8. 지난 챌린지 기록 - 비어있음")
@Composable
private fun ChallengeHistoryScreenEmptyPreview() {
    HampouchTheme {
        ChallengeHistoryScreen(records = MyPageMockData.emptyChallengeHistory(), onBackClick = {})
    }
}
