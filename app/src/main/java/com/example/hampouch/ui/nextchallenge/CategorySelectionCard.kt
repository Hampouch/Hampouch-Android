package com.example.hampouch.ui.nextchallenge

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.hampouch.R
import com.example.hampouch.ui.common.ChipGrid
import com.example.hampouch.ui.home.HomeCategoryCatalog
import com.example.hampouch.ui.onboarding.components.OnboardingBulletList
import com.example.hampouch.ui.theme.Body16Bold
import com.example.hampouch.ui.theme.HPBlack
import com.example.hampouch.ui.theme.HPGray5
import com.example.hampouch.ui.theme.HPMain
import com.example.hampouch.ui.theme.HPSub4
import com.example.hampouch.ui.theme.HPText
import com.example.hampouch.ui.theme.HPWhite

/**
 * 챌린지 시작/쉬어가기 화면이 공유하는 카테고리 선택 카드.
 * 라벨은 모두 정해진 문구라 직접입력 항목이 없다.
 */
@Composable
internal fun CategorySelectionCard(
    selectedCategoryIds: Set<String>,
    onToggleCategory: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(20.dp))
            .background(HPSub4)
            .padding(horizontal = 15.dp, vertical = 20.dp)
    ) {
        Text("카테고리", style = Body16Bold, color = HPBlack)
        Spacer(modifier = Modifier.height(6.dp))
        OnboardingBulletList(
            lines = listOf(
                "중복 선택 가능",
                "선택한 카테고리 소비 시 개입이 강해져요."
            )
        )
        Spacer(modifier = Modifier.height(14.dp))
        // 30dp 아이콘이 가로로 붙는 칩이라 한 칸이 더 넓어야 한다.
        ChipGrid(items = HomeCategoryCatalog.categories, minColumnWidth = 96.dp) { category ->
            CategoryIconChip(
                label = stringResource(category.labelResId),
                iconRes = CategoryIconRes[category.id] ?: R.drawable.icon_etc,
                selected = category.id in selectedCategoryIds,
                onClick = { onToggleCategory(category.id) }
            )
        }
    }
}

private val CategoryIconChipMinHeight = 40.dp

@Composable
internal fun CategoryIconChip(
    label: String,
    iconRes: Int,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .fillMaxHeight()
            .heightIn(min = CategoryIconChipMinHeight)
            .background(
                color = if (selected) HPMain else HPWhite,
                shape = RoundedCornerShape(20.dp)
            )
            .border(
                width = 1.dp,
                color = if (selected) HPMain else HPGray5,
                shape = RoundedCornerShape(20.dp)
            )
            .clickable(onClick = onClick)
            .padding(horizontal = 10.dp, vertical = 8.dp),
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Image(
            painter = painterResource(iconRes),
            contentDescription = null,
            modifier = Modifier.size(30.dp)
        )
        Spacer(modifier = Modifier.width(10.dp))
        Text(
            text = label,
            modifier = Modifier.weight(1f, fill = false),
            style = MaterialTheme.typography.labelLarge,
            color = if (selected) HPWhite else HPText,
            fontSize = 16.sp,
            textAlign = TextAlign.Center,
            overflow = TextOverflow.Ellipsis
        )
    }
}
