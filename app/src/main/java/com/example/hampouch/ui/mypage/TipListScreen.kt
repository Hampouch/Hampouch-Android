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
import com.example.hampouch.domain.model.TipPost
import com.example.hampouch.ui.mypage.components.MyPageMainTopBar
import com.example.hampouch.ui.mypage.components.TipPostCard
import com.example.hampouch.ui.theme.HPGray2
import com.example.hampouch.ui.theme.HPText
import com.example.hampouch.data.local.AccountMockDataSource
import com.example.hampouch.ui.theme.HampouchTheme

@Composable
fun TipListScreen(
    title: String,
    emptyMessage: String,
    tips: List<TipPost>,
    onBackClick: () -> Unit,
    modifier: Modifier = Modifier,
    onTipClick: (TipPost) -> Unit = {},
    onNotificationClick: () -> Unit = {}
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .background(HPGray2)
    ) {
        MyPageMainTopBar(
            title = title,
            onBackClick = onBackClick,
            onNotificationClick = onNotificationClick,
            modifier = Modifier.padding(start = 4.dp, end = 20.dp)
        )
        if (tips.isEmpty()) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text(
                    text = emptyMessage,
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
                tips.forEach { tip -> TipPostCard(tip = tip, onClick = { onTipClick(tip) }) }
            }
        }
    }
}

@Preview(showBackground = true, name = "21. 내가 쓴 꿀팁 - 목록")
@Composable
private fun MyTipsScreenFilledPreview() {
    HampouchTheme {
        TipListScreen(
            title = stringResource(R.string.mypage_menu_my_tips),
            emptyMessage = stringResource(R.string.my_tips_empty_message),
            tips = MyPageMockData.myTips(AccountMockDataSource.normalUser.id),
            onBackClick = {}
        )
    }
}

@Preview(showBackground = true, name = "20. 내가 쓴 꿀팁 - 비어있음")
@Composable
private fun MyTipsScreenEmptyPreview() {
    HampouchTheme {
        TipListScreen(
            title = stringResource(R.string.mypage_menu_my_tips),
            emptyMessage = stringResource(R.string.my_tips_empty_message),
            tips = MyPageMockData.emptyTips(),
            onBackClick = {}
        )
    }
}

@Preview(showBackground = true, name = "22. 저장한 꿀팁 - 목록")
@Composable
private fun SavedTipsScreenFilledPreview() {
    HampouchTheme {
        TipListScreen(
            title = stringResource(R.string.mypage_menu_saved_tips),
            emptyMessage = stringResource(R.string.saved_tips_empty_message),
            tips = MyPageMockData.savedTips(),
            onBackClick = {}
        )
    }
}

@Preview(showBackground = true, name = "23. 저장한 꿀팁 - 비어있음")
@Composable
private fun SavedTipsScreenEmptyPreview() {
    HampouchTheme {
        TipListScreen(
            title = stringResource(R.string.mypage_menu_saved_tips),
            emptyMessage = stringResource(R.string.saved_tips_empty_message),
            tips = MyPageMockData.emptyTips(),
            onBackClick = {}
        )
    }
}
