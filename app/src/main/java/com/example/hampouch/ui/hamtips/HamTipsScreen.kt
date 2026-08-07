package com.example.hampouch.ui.hamtips

import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.ScrollState
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.exclude
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.ScaffoldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.layout.positionInRoot
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.hampouch.R
import com.example.hampouch.data.model.HamTipsCategoryTab
import com.example.hampouch.data.model.HamTipsFabMenuOption
import com.example.hampouch.data.model.HamTipsSortOrder
import com.example.hampouch.data.model.TipCategory
import com.example.hampouch.data.model.TipPost
import com.example.hampouch.data.model.TipPostType
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

private enum class HamTipsRoute {
    MAIN, CATEGORY, POPULAR_ALL, POCHIPICK_ALL,
    WRITE_TIP, WRITE_MENU, WRITE_BATTLE, EDIT_TIP,
    DETAIL, BATTLE_DETAIL
}

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
    modifier: Modifier = Modifier,
    onNavigateToHamBattleLink: (String) -> Unit = {},
    onNavigateToHamBattleTab: () -> Unit = {},
    onNotificationClick: () -> Unit = {},
    openWriteBattleOnStart: Boolean = false,
    initialWriteBattleLink: String = "",
    onExitWriteBattle: () -> Unit = {},
    initialPopularPostId: String? = null
) {
    var route by remember {
        mutableStateOf(
            when {
                initialPopularPostId != null -> HamTipsRoute.POPULAR_ALL
                openWriteBattleOnStart -> HamTipsRoute.WRITE_BATTLE
                else -> HamTipsRoute.MAIN
            }
        )
    }
    // True only while the very write-battle screen we were deep-linked into (e.g. from the
    // HamBattle "공유하기" flow) is still showing. Once the user leaves it, back should behave
    // like any other in-app HamTips screen instead of exiting all the way out.
    var isExternalWriteBattleEntry by remember { mutableStateOf(openWriteBattleOnStart) }
    var selectedCategoryTab by remember { mutableStateOf(HamTipsCategoryTab.ALL) }
    var searchQuery by remember { mutableStateOf("") }
    var sortOrder by remember { mutableStateOf(HamTipsSortOrder.LATEST) }
    var showFabMenu by remember { mutableStateOf(false) }
    var selectedPostId by remember { mutableStateOf<String?>(null) }
    var editingPostId by remember { mutableStateOf<String?>(null) }

    val allPosts = HamTipsRepository.allPosts
    val popularPosts = allPosts.filter { it.likeCount >= 10 }.sortedBy { it.postedMinutesAgo }
    val pochipickPosts = allPosts.filter { it.isEditorAuthor }

    val onCategoryTabSelected: (HamTipsCategoryTab) -> Unit = { tab ->
        selectedCategoryTab = tab
        route = if (tab == HamTipsCategoryTab.ALL) HamTipsRoute.MAIN else HamTipsRoute.CATEGORY
    }
    val onBackToMain: () -> Unit = {
        route = HamTipsRoute.MAIN
        selectedCategoryTab = HamTipsCategoryTab.ALL
        isExternalWriteBattleEntry = false
    }
    val onPostClick: (TipPost) -> Unit = { post ->
        selectedPostId = post.id
        route = if (post.type == TipPostType.BATTLE) HamTipsRoute.BATTLE_DETAIL else HamTipsRoute.DETAIL
    }

    AnimatedContent(
        targetState = route,
        transitionSpec = { fadeIn(tween(300)).togetherWith(fadeOut(tween(300))) },
        label = "hamtips_route_transition"
    ) { currentRoute ->
    when (currentRoute) {
        HamTipsRoute.WRITE_TIP -> {
            BackHandler(onBack = onBackToMain)
            HamTipsWriteTipScreen(
                onBackClick = onBackToMain,
                onSubmitted = onBackToMain
            )
        }

        HamTipsRoute.WRITE_MENU -> {
            BackHandler(onBack = onBackToMain)
            HamTipsWriteMenuScreen(
                onBackClick = onBackToMain,
                onSubmitted = onBackToMain
            )
        }

        HamTipsRoute.WRITE_BATTLE -> {
            val onWriteBattleBack = if (isExternalWriteBattleEntry) onExitWriteBattle else onBackToMain
            BackHandler(onBack = onWriteBattleBack)
            HamTipsWriteBattleScreen(
                onBackClick = onWriteBattleBack,
                onSubmitted = onBackToMain,
                initialLink = if (isExternalWriteBattleEntry) initialWriteBattleLink else ""
            )
        }

        HamTipsRoute.EDIT_TIP -> {
            val editingPost = allPosts.find { it.id == editingPostId }
            if (editingPost != null) {
                BackHandler { route = HamTipsRoute.DETAIL }
                HamTipsWriteTipScreen(
                    editingPost = editingPost,
                    onBackClick = { route = HamTipsRoute.DETAIL },
                    onSubmitted = { route = HamTipsRoute.DETAIL }
                )
            }
        }

        HamTipsRoute.DETAIL -> {
            val post = allPosts.find { it.id == selectedPostId }
            if (post != null) {
                BackHandler(onBack = onBackToMain)
                HamTipsDetailScreen(
                    post = post,
                    onBackClick = onBackToMain,
                    onEditClick = { editingPostId = it.id; route = HamTipsRoute.EDIT_TIP },
                    onDeleted = onBackToMain
                )
            }
        }

        HamTipsRoute.BATTLE_DETAIL -> {
            val post = allPosts.find { it.id == selectedPostId }
            if (post != null) {
                BackHandler(onBack = onBackToMain)
                HamTipsBattleDetailScreen(
                    post = post,
                    onBackClick = onBackToMain,
                    onDeleted = onBackToMain,
                    onNavigateToBattleLink = onNavigateToHamBattleLink,
                    onNavigateToHamBattleTab = onNavigateToHamBattleTab
                )
            }
        }

        else -> Box(modifier = modifier.fillMaxSize()) {
            Scaffold(
                containerColor = HPGray2,
                contentWindowInsets = ScaffoldDefaults.contentWindowInsets.exclude(WindowInsets.statusBars),
                bottomBar = {
                    BottomNavBar(selectedItem = selectedBottomTab, onItemSelected = onItemSelected, onAddClick = onAddClick)
                },
                floatingActionButton = {
                    HamTipsFab(onClick = { showFabMenu = true })
                }
            ) { innerPadding ->
                when (currentRoute) {
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
                        onNotificationClick = onNotificationClick,
                        onPopularViewAllClick = { route = HamTipsRoute.POPULAR_ALL },
                        onPochipickViewAllClick = { route = HamTipsRoute.POCHIPICK_ALL },
                        onPostClick = onPostClick,
                        modifier = Modifier.padding(innerPadding)
                    )

                    HamTipsRoute.CATEGORY -> {
                        BackHandler(onBack = onBackToMain)
                        HamTipsFeedRouteContent(
                            title = stringResource(R.string.hamtips_title),
                            query = searchQuery,
                            onQueryChange = { searchQuery = it },
                            selectedCategoryTab = selectedCategoryTab,
                            onCategoryTabSelected = onCategoryTabSelected,
                            posts = filteredSortedPosts(allPosts, selectedCategoryTab.category, searchQuery, sortOrder),
                            sortOrder = sortOrder,
                            onSortOrderChange = { sortOrder = it },
                            onBackClick = onBackToMain,
                            onNotificationClick = onNotificationClick,
                            onPostClick = onPostClick,
                            modifier = Modifier.padding(innerPadding)
                        )
                    }

                    HamTipsRoute.POPULAR_ALL -> {
                        BackHandler(onBack = onBackToMain)
                        HamTipsFeedRouteContent(
                            title = stringResource(R.string.hamtips_popular_title),
                            query = searchQuery,
                            onQueryChange = { searchQuery = it },
                            selectedCategoryTab = selectedCategoryTab,
                            onCategoryTabSelected = onCategoryTabSelected,
                            posts = filteredSortedPosts(popularPosts, selectedCategoryTab.category, searchQuery, sortOrder),
                            sortOrder = sortOrder,
                            onSortOrderChange = { sortOrder = it },
                            onBackClick = onBackToMain,
                            onNotificationClick = onNotificationClick,
                            onPostClick = onPostClick,
                            scrollToPostId = initialPopularPostId,
                            modifier = Modifier.padding(innerPadding)
                        )
                    }

                    HamTipsRoute.POCHIPICK_ALL -> {
                        BackHandler(onBack = onBackToMain)
                        HamTipsFeedRouteContent(
                            title = stringResource(R.string.hamtips_pochipick_title),
                            query = searchQuery,
                            onQueryChange = { searchQuery = it },
                            selectedCategoryTab = selectedCategoryTab,
                            onCategoryTabSelected = onCategoryTabSelected,
                            posts = filteredSortedPosts(pochipickPosts, selectedCategoryTab.category, searchQuery, sortOrder),
                            sortOrder = sortOrder,
                            onSortOrderChange = { sortOrder = it },
                            onBackClick = onBackToMain,
                            onNotificationClick = onNotificationClick,
                            onPostClick = onPostClick,
                            modifier = Modifier.padding(innerPadding)
                        )
                    }

                    else -> Unit
                }
            }

            HamTipsFabMenu(
                visible = showFabMenu,
                onOptionClick = { option ->
                    showFabMenu = false
                    route = when (option) {
                        HamTipsFabMenuOption.SHARE_TIP -> HamTipsRoute.WRITE_TIP
                        HamTipsFabMenuOption.RECRUIT_BATTLE -> HamTipsRoute.WRITE_BATTLE
                        HamTipsFabMenuOption.RECOMMEND_MENU -> HamTipsRoute.WRITE_MENU
                    }
                },
                onDismiss = { showFabMenu = false }
            )
        }
    }
    }
}

@Composable
private fun HamTipsFeedSection(
    posts: List<TipPost>,
    sortOrder: HamTipsSortOrder,
    onSortOrderChange: (HamTipsSortOrder) -> Unit,
    modifier: Modifier = Modifier,
    initialSortExpanded: Boolean = false,
    onPostClick: (TipPost) -> Unit = {},
    scrollToPostId: String? = null,
    scrollState: ScrollState? = null,
    containerRootY: Float? = null
) {
    var targetOffset by remember(scrollToPostId) { mutableStateOf<Int?>(null) }
    var hasScrolled by remember(scrollToPostId) { mutableStateOf(false) }

    LaunchedEffect(targetOffset) {
        val offset = targetOffset
        if (scrollState != null && offset != null && !hasScrolled) {
            hasScrolled = true
            scrollState.animateScrollTo(offset)
        }
    }

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
            posts.forEach { post ->
                val isScrollTarget = scrollState != null && containerRootY != null && post.id == scrollToPostId
                HamTipsFeedPostCard(
                    post = post,
                    onClick = { onPostClick(post) },
                    modifier = if (isScrollTarget) {
                        Modifier.onGloballyPositioned { coordinates ->
                            targetOffset = (coordinates.positionInRoot().y - containerRootY!! + scrollState!!.value).toInt()
                        }
                    } else {
                        Modifier
                    }
                )
            }
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
    modifier: Modifier = Modifier,
    onPostClick: (TipPost) -> Unit = {}
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 20.dp)
    ) {
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
            popularPosts.take(4).forEach { post -> HamTipsCompactPostCard(post = post, onClick = { onPostClick(post) }) }
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
            pochipickPosts.take(4).forEach { post -> HamTipsCompactPostCard(post = post, onClick = { onPostClick(post) }) }
        }
        Spacer(modifier = Modifier.height(24.dp))

        HamTipsFeedSection(posts = allPosts, sortOrder = sortOrder, onSortOrderChange = onSortOrderChange, onPostClick = onPostClick)
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
    initialSortExpanded: Boolean = false,
    onPostClick: (TipPost) -> Unit = {},
    scrollToPostId: String? = null
) {
    val scrollState = rememberScrollState()
    var containerRootY by remember { mutableStateOf<Float?>(null) }
    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(scrollState)
            .padding(horizontal = 20.dp)
            .onGloballyPositioned { coordinates -> containerRootY = coordinates.positionInRoot().y }
    ) {
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
            initialSortExpanded = initialSortExpanded,
            onPostClick = onPostClick,
            scrollToPostId = scrollToPostId,
            scrollState = scrollState,
            containerRootY = containerRootY
        )
        Spacer(modifier = Modifier.height(96.dp))
    }
}

@Composable
private fun HamTipsScreenPreviewScaffold(content: @Composable (androidx.compose.foundation.layout.PaddingValues) -> Unit) {
    Box(modifier = Modifier.fillMaxSize()) {
        Scaffold(
            containerColor = HPGray2,
            contentWindowInsets = ScaffoldDefaults.contentWindowInsets.exclude(WindowInsets.statusBars),
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

@Preview(showBackground = true, name = "13. 뭐먹지 카테고리 - 메뉴 추천 리스트")
@Composable
private fun HamTipsWhatToEatCategoryScreenPreview() {
    HampouchTheme {
        HamTipsScreenPreviewScaffold { innerPadding ->
            HamTipsFeedRouteContent(
                title = stringResource(R.string.hamtips_title),
                query = "",
                onQueryChange = {},
                selectedCategoryTab = HamTipsCategoryTab.WHAT_TO_EAT,
                onCategoryTabSelected = {},
                posts = HamTipsMockData.allPosts().filter { it.category == TipCategory.WHAT_TO_EAT },
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
            HamTipsFabMenu(visible = true, onOptionClick = {}, onDismiss = {})
        }
    }
}

@Preview(showBackground = true, name = "6. 글쓰기 메뉴 상세")
@Composable
private fun HamTipsFabMenuDetailPreview() {
    HampouchTheme {
        Box(modifier = Modifier.fillMaxSize()) {
            HamTipsFabMenu(visible = true, onOptionClick = {}, onDismiss = {})
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
