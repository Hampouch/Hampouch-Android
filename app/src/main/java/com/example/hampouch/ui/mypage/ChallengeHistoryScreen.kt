package com.example.hampouch.ui.mypage

import com.example.hampouch.ui.common.previewChallengeState
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.hampouch.R
import com.example.hampouch.domain.model.ChallengeRecord
import com.example.hampouch.ui.mypage.components.ChallengeRecordCard
import com.example.hampouch.ui.mypage.components.MyPageMainTopBar
import com.example.hampouch.ui.theme.HPGray2
import com.example.hampouch.ui.theme.HPText
import com.example.hampouch.ui.theme.HampouchTheme

@Composable
fun ChallengeHistoryScreen(
    records: List<ChallengeRecord>,
    onBackClick: () -> Unit,
    modifier: Modifier = Modifier,
    onRecordClick: (ChallengeRecord) -> Unit,
    onNotificationClick: () -> Unit
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .background(HPGray2)
    ) {
        MyPageMainTopBar(
            title = stringResource(R.string.challenge_history_title),
            onBackClick = onBackClick,
            onNotificationClick = onNotificationClick
        )
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(horizontal = 20.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            if (records.isEmpty()) {
                item(contentType = "empty_state") {
                    Box(modifier = Modifier.fillParentMaxSize(), contentAlignment = Alignment.Center) {
                        Text(
                            text = stringResource(R.string.challenge_history_empty_message),
                            style = MaterialTheme.typography.bodyMedium,
                            color = HPText
                        )
                    }
                }
            } else {
                items(records, key = { it.id }, contentType = { "challenge_record" }) { record ->
                    ChallengeRecordCard(record = record, onClick = { onRecordClick(record) })
                }
            }
        }
    }
}

@Preview(showBackground = true, name = "19. 지난 챌린지 - 목록")
@Composable
private fun ChallengeHistoryScreenFilledPreview() {
    HampouchTheme {
        ChallengeHistoryScreen(records = MyPageMockData.challengeHistory(previewChallengeState(), spentOnDate = { 0 }), onBackClick = {}, onRecordClick = {}, onNotificationClick = {})
    }
}

@Preview(showBackground = true, name = "18. 지난 챌린지 - 비어있음")
@Composable
private fun ChallengeHistoryScreenEmptyPreview() {
    HampouchTheme {
        ChallengeHistoryScreen(records = MyPageMockData.emptyChallengeHistory(), onBackClick = {}, onRecordClick = {}, onNotificationClick = {})
    }
}
