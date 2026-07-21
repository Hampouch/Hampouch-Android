package com.example.hampouch.ui.hamtips

import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
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
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.hampouch.R
import com.example.hampouch.data.model.HamTipsCategoryTab
import com.example.hampouch.data.model.HamTipsSortOrder
import com.example.hampouch.data.model.TipCategory
import com.example.hampouch.data.model.TipPost
import com.example.hampouch.navigation.BottomNavBar
import com.example.hampouch.navigation.BottomNavItem
import com.example.hampouch.ui.hamtips.components.HamTipsCategoryTabRow
import com.example.hampouch.ui.hamtips.components.HamTipsCompactPostCard
import com.example.hampouch.ui.hamtips.components.HamTipsDetailTopBar
import com.example.hampouch.ui.hamtips.components.HamTipsFab
import com.example.hampouch.ui.hamtips.components.HamTipsFabMenu
import com.example.hampouch.ui.hamtips.components.HamTipsFeedPostCard
import com.example.hampouch.ui.hamtips.components.HamTipsMainTopBar
import com.example.hampouch.ui.hamtips.components.HamTipsSearchBar
import com.example.hampouch.ui.hamtips.components.HamTipsSectionHeader
import com.example.hampouch.ui.hamtips.components.HamTipsSortDropdown
import com.example.hampouch.ui.theme.HPBlack
import com.example.hampouch.ui.theme.HPGray2
import com.example.hampouch.ui.theme.HampouchTheme

private enum class HamTipsRoute { MAIN, CATEGORY, POPULAR_ALL, POCHIPICK_ALL }

private fun filteredSortedPosts(
    posts: List<TipPost>,
    category: TipCategory?,
    query: String,
    sortOrder: HamTipsSortOrder
): List<TipPost> {
    val filtered = posts.filter { post ->
        (category == null || post.category == category) &&
            (query.isBlank() || post.title.contains(query, ignoreCase = true))
    }
    return when (sortOrder) {
        HamTipsSortOrder.LATEST -> filtered.sortedBy { it.postedMinutesAgo }
        HamTipsSortOrder.POPULAR -> filtered.sortedByDescending { it.likeCount }
        HamTipsSortOrder.VIEWS -> filtered.sortedByDescending { it.viewCount }
    }
}

@Composable
fun HamTipsScreen(
    selectedBottomTab: BottomNavItem,
    onItemSelected: (BottomNavItem) -> Unit,
    onAddClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    var route by remember { mutableStateOf(HamTipsRoute.MAIN) }
    var selectedCategoryTab by remember { mutableStateOf(HamTipsCategoryTab.ALL) }
    var searchQuery by remember { mutableStateOf("") }
    var sortOrder by remember { mutableStateOf(HamTipsSortOrder.LATEST) }
    var showFabMenu by remember { mutableStateOf(false) }

    val allPosts = remember { HamTipsMockData.allPosts() }
    val popularPosts = remember { HamTipsMockData.popularPosts() }
    val pochipickPosts = remember { HamTipsMockData.pochipickPosts() }

    val onCategoryTabSelected: (HamTipsCategoryTab) -> Unit = { tab ->
        selectedCategoryTab = tab
        route = if (tab == HamTipsCategoryTab.ALL) HamTipsRoute.MAIN else HamTipsRoute.CATEGORY
    }
    val onBackToMain: () -> Unit = {
        route = HamTipsRoute.MAIN
        selectedCategoryTab = HamTipsCategoryTab.ALL
    }

    Box(modifier = modifier.fillMaxSize()) {
        Scaffold(
            containerColor = HPGray2,
            bottomBar = {
                BottomNavBar(selectedItem = selectedBottomTab, onItemSelected = onItemSelected, onAddClick = onAddClick)
            },
            floatingActionButton = {
                HamTipsFab(onClick = { showFabMenu = true })
            }
        ) { innerPadding ->
            when (route) {
                HamTipsRoute.MAIN -> HamTipsMainContent(
                    query = searchQuery,
                    onQueryChange = { searchQuery = it },
                    selectedCategoryTab = selectedCategoryTab,
                    onCategoryTabSelected = onCategoryTabSelected,
                    popularPosts = popularPosts,
                    pochipickPosts = pochipickPosts,
                    allPosts = filteredSortedPosts(allPosts, null, searchQuery, sortOrder),
                    sortOrder = sortOrder,
                    onSortOrderChange = { sortOrder = it },
                    onNotificationClick = {},
                    onPopularViewAllClick = { route = HamTipsRoute.POPULAR_ALL },
                    onPochipickViewAllClick = { route = HamTipsRoute.POCHIPICK_ALL },
                    modifier = Modifier.padding(innerPadding)
                )

                HamTipsRoute.CATEGORY -> HamTipsFeedRouteContent(
                    title = stringResource(R.string.hamtips_title),
                    query = searchQuery,
                    onQueryChange = { searchQuery = it },
                    selectedCategoryTab = selectedCategoryTab,
                    onCategoryTabSelected = onCategoryTabSelected,
                    posts = filteredSortedPosts(allPosts, selectedCategoryTab.category, searchQuery, sortOrder),
                    sortOrder = sortOrder,
                    onSortOrderChange = { sortOrder = it },
                    onBackClick = onBackToMain,
                    onNotificationClick = {},
                    modifier = Modifier.padding(innerPadding)
                )

                HamTipsRoute.POPULAR_ALL -> HamTipsFeedRouteContent(
                    title = stringResource(R.string.hamtips_popular_title),
                    query = searchQuery,
                    onQueryChange = { searchQuery = it },
                    selectedCategoryTab = selectedCategoryTab,
                    onCategoryTabSelected = onCategoryTabSelected,
                    posts = filteredSortedPosts(popularPosts, selectedCategoryTab.category, searchQuery, sortOrder),
                    sortOrder = sortOrder,
                    onSortOrderChange = { sortOrder = it },
                    onBackClick = onBackToMain,
                    onNotificationClick = {},
                    modifier = Modifier.padding(innerPadding)
                )

                HamTipsRoute.POCHIPICK_ALL -> HamTipsFeedRouteContent(
                    title = stringResource(R.string.hamtips_pochipick_title),
                    query = searchQuery,
                    onQueryChange = { searchQuery = it },
                    selectedCategoryTab = selectedCategoryTab,
                    onCategoryTabSelected = onCategoryTabSelected,
                    posts = filteredSortedPosts(pochipickPosts, selectedCategoryTab.category, searchQuery, sortOrder),
                    sortOrder = sortOrder,
                    onSortOrderChange = { sortOrder = it },
                    onBackClick = onBackToMain,
                    onNotificationClick = {},
                    modifier = Modifier.padding(innerPadding)
                )
            }
        }

        if (showFabMenu) {
            HamTipsFabMenu(
                onOptionClick = { showFabMenu = false },
                onDismiss = { showFabMenu = false }
            )
        }
    }
}

@Composable
private fun HamTipsFeedSection(
    posts: List<TipPost>,
    sortOrder: HamTipsSortOrder,
    onSortOrderChange: (HamTipsSortOrder) -> Unit,
    modifier: Modifier = Modifier,
    initialSortExpanded: Boolean = false
) {
    Column(modifier = modifier.fillMaxWidth()) {
        Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
            Text(
                text = stringResource(R.string.hamtips_section_all_posts),
                style = MaterialTheme.typography.bodyLarge,
                color = HPBlack,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.weight(1f))
            HamTipsSortDropdown(selected = sortOrder, onSelected = onSortOrderChange, initialExpanded = initialSortExpanded)
        }
        Spacer(modifier = Modifier.height(12.dp))
        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
            posts.forEach { post -> HamTipsFeedPostCard(post = post) }
        }
    }
}

@Composable
private fun HamTipsMainContent(
    query: String,
    onQueryChange: (String) -> Unit,
    selectedCategoryTab: HamTipsCategoryTab,
    onCategoryTabSelected: (HamTipsCategoryTab) -> Unit,
    popularPosts: List<TipPost>,
    pochipickPosts: List<TipPost>,
    allPosts: List<TipPost>,
    sortOrder: HamTipsSortOrder,
    onSortOrderChange: (HamTipsSortOrder) -> Unit,
    onNotificationClick: () -> Unit,
    onPopularViewAllClick: () -> Unit,
    onPochipickViewAllClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 20.dp)
    ) {
        Spacer(modifier = Modifier.height(12.dp))
        HamTipsMainTopBar(onNotificationClick = onNotificationClick)
        Spacer(modifier = Modifier.height(12.dp))
        HamTipsSearchBar(query = query, onQueryChange = onQueryChange)
        Spacer(modifier = Modifier.height(16.dp))
        HamTipsCategoryTabRow(selectedTab = selectedCategoryTab, onTabSelected = onCategoryTabSelected)
        Spacer(modifier = Modifier.height(24.dp))

        HamTipsSectionHeader(
            title = stringResource(R.string.hamtips_section_popular),
            onViewAllClick = onPopularViewAllClick
        )
        Spacer(modifier = Modifier.height(12.dp))
        Row(
            modifier = Modifier.horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            popularPosts.take(4).forEach { post -> HamTipsCompactPostCard(post = post) }
        }
        Spacer(modifier = Modifier.height(24.dp))

        HamTipsSectionHeader(
            title = stringResource(R.string.hamtips_section_pochipick),
            onViewAllClick = onPochipickViewAllClick
        )
        Spacer(modifier = Modifier.height(12.dp))
        Row(
            modifier = Modifier.horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            pochipickPosts.take(4).forEach { post -> HamTipsCompactPostCard(post = post) }
        }
        Spacer(modifier = Modifier.height(24.dp))

        HamTipsFeedSection(posts = allPosts, sortOrder = sortOrder, onSortOrderChange = onSortOrderChange)
        Spacer(modifier = Modifier.height(96.dp))
    }
}

@Composable
private fun HamTipsFeedRouteContent(
    title: String,
    query: String,
    onQueryChange: (String) -> Unit,
    selectedCategoryTab: HamTipsCategoryTab,
    onCategoryTabSelected: (HamTipsCategoryTab) -> Unit,
    posts: List<TipPost>,
    sortOrder: HamTipsSortOrder,
    onSortOrderChange: (HamTipsSortOrder) -> Unit,
    onBackClick: () -> Unit,
    onNotificationClick: () -> Unit,
    modifier: Modifier = Modifier,
    initialSortExpanded: Boolean = false
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 20.dp)
    ) {
        Spacer(modifier = Modifier.height(4.dp))
        HamTipsDetailTopBar(title = title, onBackClick = onBackClick, onNotificationClick = onNotificationClick)
        Spacer(modifier = Modifier.height(12.dp))
        HamTipsSearchBar(query = query, onQueryChange = onQueryChange)
        Spacer(modifier = Modifier.height(16.dp))
        HamTipsCategoryTabRow(selectedTab = selectedCategoryTab, onTabSelected = onCategoryTabSelected)
        Spacer(modifier = Modifier.height(24.dp))
        HamTipsFeedSection(
            posts = posts,
            sortOrder = sortOrder,
            onSortOrderChange = onSortOrderChange,
            initialSortExpanded = initialSortExpanded
        )
        Spacer(modifier = Modifier.height(96.dp))
    }
}

@Composable
private fun HamTipsScreenPreviewScaffold(content: @Composable (androidx.compose.foundation.layout.PaddingValues) -> Unit) {
    Box(modifier = Modifier.fillMaxSize()) {
        Scaffold(
            containerColor = HPGray2,
            bottomBar = {
                BottomNavBar(selectedItem = BottomNavItem.COMMUNITY, onItemSelected = {}, onAddClick = {})
            },
            floatingActionButton = {
                HamTipsFab(onClick = {})
            }
        ) { innerPadding -> content(innerPadding) }
    }
}

@Preview(showBackground = true, name = "1. 햄꿀팁 메인")
@Composable
private fun HamTipsMainScreenPreview() {
    HampouchTheme {
        HamTipsScreen(selectedBottomTab = BottomNavItem.COMMUNITY, onItemSelected = {}, onAddClick = {})
    }
}

@Preview(showBackground = true, name = "2. 카테고리 선택 - 요리")
@Composable
private fun HamTipsCategoryScreenPreview() {
    HampouchTheme {
        HamTipsScreenPreviewScaffold { innerPadding ->
            HamTipsFeedRouteContent(
                title = stringResource(R.string.hamtips_title),
                query = "",
                onQueryChange = {},
                selectedCategoryTab = HamTipsCategoryTab.COOKING,
                onCategoryTabSelected = {},
                posts = HamTipsMockData.allPosts().filter { it.category == TipCategory.COOKING },
                sortOrder = HamTipsSortOrder.LATEST,
                onSortOrderChange = {},
                onBackClick = {},
                onNotificationClick = {},
                modifier = Modifier.padding(innerPadding)
            )
        }
    }
}

@Preview(showBackground = true, name = "3. 인기 꿀팁 전체보기")
@Composable
private fun HamTipsPopularAllScreenPreview() {
    HampouchTheme {
        HamTipsScreenPreviewScaffold { innerPadding ->
            HamTipsFeedRouteContent(
                title = stringResource(R.string.hamtips_popular_title),
                query = "",
                onQueryChange = {},
                selectedCategoryTab = HamTipsCategoryTab.ALL,
                onCategoryTabSelected = {},
                posts = HamTipsMockData.popularPosts(),
                sortOrder = HamTipsSortOrder.LATEST,
                onSortOrderChange = {},
                onBackClick = {},
                onNotificationClick = {},
                modifier = Modifier.padding(innerPadding)
            )
        }
    }
}

@Preview(showBackground = true, name = "4. 포치픽 전체보기")
@Composable
private fun HamTipsPochipickAllScreenPreview() {
    HampouchTheme {
        HamTipsScreenPreviewScaffold { innerPadding ->
            HamTipsFeedRouteContent(
                title = stringResource(R.string.hamtips_pochipick_title),
                query = "",
                onQueryChange = {},
                selectedCategoryTab = HamTipsCategoryTab.ALL,
                onCategoryTabSelected = {},
                posts = HamTipsMockData.pochipickPosts(),
                sortOrder = HamTipsSortOrder.LATEST,
                onSortOrderChange = {},
                onBackClick = {},
                onNotificationClick = {},
                modifier = Modifier.padding(innerPadding)
            )
        }
    }
}

@Preview(showBackground = true, name = "5. 글쓰기 버튼 클릭 효과")
@Composable
private fun HamTipsFabMenuEffectPreview() {
    HampouchTheme {
        Box(modifier = Modifier.fillMaxSize()) {
            HamTipsScreenPreviewScaffold { innerPadding ->
                HamTipsMainContent(
                    query = "",
                    onQueryChange = {},
                    selectedCategoryTab = HamTipsCategoryTab.ALL,
                    onCategoryTabSelected = {},
                    popularPosts = HamTipsMockData.popularPosts(),
                    pochipickPosts = HamTipsMockData.pochipickPosts(),
                    allPosts = HamTipsMockData.allPosts(),
                    sortOrder = HamTipsSortOrder.LATEST,
                    onSortOrderChange = {},
                    onNotificationClick = {},
                    onPopularViewAllClick = {},
                    onPochipickViewAllClick = {},
                    modifier = Modifier.padding(innerPadding)
                )
            }
            HamTipsFabMenu(onOptionClick = {}, onDismiss = {})
        }
    }
}

@Preview(showBackground = true, name = "6. 글쓰기 메뉴 상세")
@Composable
private fun HamTipsFabMenuDetailPreview() {
    HampouchTheme {
        Box(modifier = Modifier.fillMaxSize()) {
            HamTipsFabMenu(onOptionClick = {}, onDismiss = {})
        }
    }
}

@Preview(showBackground = true, name = "7. 정렬 드롭다운")
@Composable
private fun HamTipsSortDropdownPreview() {
    HampouchTheme {
        HamTipsScreenPreviewScaffold { innerPadding ->
            HamTipsFeedRouteContent(
                title = stringResource(R.string.hamtips_title),
                query = "",
                onQueryChange = {},
                selectedCategoryTab = HamTipsCategoryTab.COOKING,
                onCategoryTabSelected = {},
                posts = HamTipsMockData.allPosts().filter { it.category == TipCategory.COOKING },
                sortOrder = HamTipsSortOrder.LATEST,
                onSortOrderChange = {},
                onBackClick = {},
                onNotificationClick = {},
                modifier = Modifier.padding(innerPadding),
                initialSortExpanded = true
            )
        }
    }
}
