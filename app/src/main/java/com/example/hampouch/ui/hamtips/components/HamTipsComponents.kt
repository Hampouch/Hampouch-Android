package com.example.hampouch.ui.hamtips.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.outlined.ChatBubbleOutline
import androidx.compose.material.icons.outlined.FavoriteBorder
import androidx.compose.material.icons.outlined.Visibility
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.TransformOrigin
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.example.hampouch.R
import com.example.hampouch.ui.common.NotificationBellIcon
import com.example.hampouch.domain.model.HamTipsCategoryTab
import com.example.hampouch.domain.model.HamTipsFabMenuOption
import com.example.hampouch.domain.model.HamTipsSortOrder
import com.example.hampouch.domain.model.TipPost
import com.example.hampouch.ui.mypage.components.SettingsMenuDivider
import com.example.hampouch.ui.mypage.components.TipCategoryBadge
import com.example.hampouch.ui.theme.HPBlack
import com.example.hampouch.ui.theme.HPGray3
import com.example.hampouch.ui.theme.HPGray4
import com.example.hampouch.ui.theme.HPGray5
import com.example.hampouch.ui.theme.HPMain
import com.example.hampouch.ui.theme.HPSub
import com.example.hampouch.ui.theme.HPText
import com.example.hampouch.ui.theme.HPWhite

@Composable
fun HamTipsMainTopBar(onNotificationClick: () -> Unit, modifier: Modifier = Modifier) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .height(64.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Spacer(modifier = Modifier.width(48.dp))
        Text(
            text = stringResource(R.string.hamtips_title),
            style = MaterialTheme.typography.titleSmall,
            color = HPBlack,
            textAlign = TextAlign.Center,
            modifier = Modifier.weight(1f)
        )
        NotificationBellIcon(onClick = onNotificationClick)
    }
}

@Composable
fun HamTipsDetailTopBar(
    title: String,
    onBackClick: () -> Unit,
    onNotificationClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .height(64.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        IconButton(onClick = onBackClick) {
            Icon(
                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                contentDescription = stringResource(R.string.cd_back),
                tint = HPBlack
            )
        }
        Text(
            text = title,
            style = MaterialTheme.typography.titleSmall,
            color = HPBlack,
            textAlign = TextAlign.Center,
            modifier = Modifier.weight(1f)
        )
        NotificationBellIcon(onClick = onNotificationClick)
    }
}

@Composable
fun HamTipsSearchBar(
    query: String,
    onQueryChange: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(50))
            .background(HPGray3)
            .padding(start = 16.dp, end = 8.dp, top = 10.dp, bottom = 10.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = Icons.Filled.Search,
            contentDescription = stringResource(R.string.hamtips_cd_search),
            tint = HPText,
            modifier = Modifier.size(20.dp)
        )
        Spacer(modifier = Modifier.width(8.dp))
        Box(modifier = Modifier.weight(1f)) {
            if (query.isEmpty()) {
                Text(
                    text = stringResource(R.string.hamtips_search_placeholder),
                    style = MaterialTheme.typography.bodyMedium,
                    color = HPText
                )
            }
            BasicTextField(
                value = query,
                onValueChange = onQueryChange,
                singleLine = true,
                textStyle = MaterialTheme.typography.bodyMedium.copy(color = HPBlack),
                cursorBrush = SolidColor(HPMain),
                modifier = Modifier.fillMaxWidth()
            )
        }
        if (query.isNotEmpty()) {
            IconButton(onClick = { onQueryChange("") }, modifier = Modifier.size(32.dp)) {
                Icon(
                    imageVector = Icons.Filled.Close,
                    contentDescription = stringResource(R.string.hamtips_cd_clear_search),
                    tint = HPText,
                    modifier = Modifier.size(18.dp)
                )
            }
        }
    }
}

@Composable
fun HamTipsCategoryTabRow(
    selectedTab: HamTipsCategoryTab,
    onTabSelected: (HamTipsCategoryTab) -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .horizontalScroll(rememberScrollState()),
        horizontalArrangement = Arrangement.spacedBy(20.dp)
    ) {
        HamTipsCategoryTab.entries.forEach { tab ->
            val selected = tab == selectedTab
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier
                    .clickable { onTabSelected(tab) }
                    .padding(horizontal = 5.dp)
            ) {
                Text(
                    text = stringResource(tab.labelResId),
                    style = MaterialTheme.typography.bodyMedium,
                    color = if (selected) HPBlack else HPText,
                    fontWeight = if (selected) FontWeight.Bold else FontWeight.Normal
                )
                Spacer(modifier = Modifier.height(6.dp))
                Box(
                    modifier = Modifier
                        .size(width = 20.dp, height = 3.dp)
                        .background(if (selected) HPMain else Color.Transparent, RoundedCornerShape(50))
                )
            }
        }
    }
}

@Composable
fun HamTipsSectionHeader(title: String, onViewAllClick: () -> Unit, modifier: Modifier = Modifier) {
    Row(
        modifier = modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(text = title, style = MaterialTheme.typography.bodyLarge, color = HPBlack, fontWeight = FontWeight.Bold)
        Spacer(modifier = Modifier.weight(1f))
        Text(
            text = stringResource(R.string.hamtips_view_all),
            style = MaterialTheme.typography.labelMedium,
            color = HPText,
            modifier = Modifier.clickable(onClick = onViewAllClick)
        )
    }
}

@Composable
private fun formatTimeAgo(minutesAgo: Int): String {
    return if (minutesAgo < 60) {
        stringResource(R.string.hamtips_time_minutes_ago_format, minutesAgo)
    } else {
        stringResource(R.string.hamtips_time_hours_ago_format, minutesAgo / 60)
    }
}

@Composable
private fun HamTipsStatItem(
    icon: ImageVector,
    count: Int,
    contentDescription: String,
    modifier: Modifier = Modifier
) {
    Row(modifier = modifier, verticalAlignment = Alignment.CenterVertically) {
        Icon(imageVector = icon, contentDescription = contentDescription, tint = HPText, modifier = Modifier.size(14.dp))
        Spacer(modifier = Modifier.width(3.dp))
        Text(text = count.toString(), style = MaterialTheme.typography.labelSmall, color = HPText)
    }
}

@Composable
private fun HamTipsStatRow(post: TipPost, modifier: Modifier = Modifier) {
    Row(modifier = modifier, verticalAlignment = Alignment.CenterVertically) {
        HamTipsStatItem(
            icon = Icons.Outlined.Visibility,
            count = post.viewCount,
            contentDescription = stringResource(R.string.hamtips_cd_view_count)
        )
        Spacer(modifier = Modifier.width(10.dp))
        HamTipsStatItem(
            icon = Icons.Outlined.ChatBubbleOutline,
            count = post.commentCount,
            contentDescription = stringResource(R.string.hamtips_cd_comment_count)
        )
        Spacer(modifier = Modifier.width(10.dp))
        HamTipsStatItem(
            icon = Icons.Outlined.FavoriteBorder,
            count = post.likeCount,
            contentDescription = stringResource(R.string.hamtips_cd_like_count)
        )
    }
}

@Composable
fun HamTipsCompactPostCard(post: TipPost, modifier: Modifier = Modifier, onClick: () -> Unit = {}) {
    Column(
        modifier = modifier
            .width(168.dp)
            .clip(RoundedCornerShape(20.dp))
            .background(HPWhite)
            .border(1.dp, HPGray4, RoundedCornerShape(20.dp))
            .clickable(onClick = onClick)
            .padding(12.dp)
    ) {
        TipCategoryBadge(category = post.category)
        Spacer(modifier = Modifier.height(10.dp))
        Text(
            text = post.title,
            style = MaterialTheme.typography.bodySmall,
            color = HPBlack,
            fontWeight = FontWeight.Bold,
            minLines = 2,
            maxLines = 2,
            overflow = TextOverflow.Ellipsis
        )
        Spacer(modifier = Modifier.height(12.dp))
        HamTipsStatRow(post = post)
    }
}

@Composable
fun HamTipsFeedPostCard(post: TipPost, modifier: Modifier = Modifier, onClick: () -> Unit = {}) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(20.dp))
            .background(HPWhite)
            .border(1.dp, HPGray4, RoundedCornerShape(20.dp))
            .clickable(onClick = onClick)
            .padding(16.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            TipCategoryBadge(category = post.category)
            Spacer(modifier = Modifier.weight(1f))
            Text(text = formatTimeAgo(post.postedMinutesAgo), style = MaterialTheme.typography.labelSmall, color = HPText)
        }
        Spacer(modifier = Modifier.height(10.dp))
        Row(modifier = Modifier.heightIn(min = 64.dp), verticalAlignment = Alignment.Top) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = post.title,
                    style = MaterialTheme.typography.bodyMedium,
                    color = HPBlack,
                    fontWeight = FontWeight.Bold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = post.subtitle,
                    style = MaterialTheme.typography.bodySmall,
                    color = HPText,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
            if (post.imageUris.isNotEmpty()) {
                Spacer(modifier = Modifier.width(12.dp))
                HamTipsFeedThumbnail(
                    uriString = post.imageUris.first(),
                    modifier = Modifier.size(64.dp)
                )
            }
        }
        Spacer(modifier = Modifier.height(12.dp))
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(
                text = post.authorName,
                style = MaterialTheme.typography.bodySmall,
                color = if (post.isEditorAuthor) HPSub else HPText,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.weight(1f),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            HamTipsStatRow(post = post)
        }
    }
}

@Composable
fun HamTipsSortDropdown(
    selected: HamTipsSortOrder,
    onSelected: (HamTipsSortOrder) -> Unit,
    modifier: Modifier = Modifier,
    initialExpanded: Boolean = false
) {
    var expanded by remember { mutableStateOf(initialExpanded) }
    Box(modifier = modifier) {
        Row(
            modifier = Modifier.clickable { expanded = true },
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(text = stringResource(selected.labelResId), style = MaterialTheme.typography.labelMedium, color = HPText)
            Icon(
                imageVector = Icons.Filled.KeyboardArrowDown,
                contentDescription = stringResource(R.string.hamtips_cd_sort),
                tint = HPText,
                modifier = Modifier.size(18.dp)
            )
        }
        DropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
            HamTipsSortOrder.entries.forEach { order ->
                DropdownMenuItem(
                    text = { Text(text = stringResource(order.labelResId)) },
                    onClick = {
                        onSelected(order)
                        expanded = false
                    }
                )
            }
        }
    }
}

@Composable
fun HamTipsFab(onClick: () -> Unit, modifier: Modifier = Modifier) {
    Box(
        modifier = modifier
            .size(56.dp)
            .background(HPMain, CircleShape)
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        Icon(
            imageVector = Icons.Filled.Edit,
            contentDescription = stringResource(R.string.hamtips_cd_fab),
            tint = HPWhite,
            modifier = Modifier.size(22.dp)
        )
    }
}

@Composable
fun HamTipsFabMenu(
    visible: Boolean,
    onOptionClick: (HamTipsFabMenuOption) -> Unit,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier
) {
    AnimatedVisibility(
        visible = visible,
        enter = fadeIn(animationSpec = tween(220)),
        exit = fadeOut(animationSpec = tween(180)),
        modifier = modifier
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(HPBlack.copy(alpha = 0.4f))
                .clickable(onClick = onDismiss),
            contentAlignment = Alignment.BottomEnd
        ) {
            AnimatedVisibility(
                visible = visible,
                enter = fadeIn(animationSpec = tween(220)) +
                    scaleIn(animationSpec = tween(220), transformOrigin = TransformOrigin(1f, 1f)) +
                    expandVertically(animationSpec = tween(220), expandFrom = Alignment.Bottom),
                exit = fadeOut(animationSpec = tween(180)) +
                    scaleOut(animationSpec = tween(180), transformOrigin = TransformOrigin(1f, 1f)) +
                    shrinkVertically(animationSpec = tween(180), shrinkTowards = Alignment.Bottom),
                modifier = Modifier.padding(end = 20.dp, bottom = 96.dp)
            ) {
                Column(
                    modifier = Modifier
                        .width(160.dp)
                        .clip(RoundedCornerShape(16.dp))
                        .background(HPWhite)
                ) {
                    HamTipsFabMenuOption.entries.forEachIndexed { index, option ->
                        Text(
                            text = stringResource(option.labelResId),
                            style = MaterialTheme.typography.bodyMedium,
                            color = HPBlack,
                            textAlign = TextAlign.Center,
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { onOptionClick(option) }
                                .padding(vertical = 16.dp)
                        )
                        if (index != HamTipsFabMenuOption.entries.lastIndex) {
                            SettingsMenuDivider()
                        }
                    }
                }
            }
        }
    }
}
