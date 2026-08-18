package com.example.hampouch.ui.hamtips

import com.example.hampouch.domain.model.formatTimeAgoLabel
import android.widget.Toast
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.StarBorder
import androidx.compose.material.icons.outlined.BookmarkBorder
import androidx.compose.material.icons.outlined.ChatBubbleOutline
import androidx.compose.material.icons.outlined.FavoriteBorder
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import com.example.hampouch.ui.common.SessionViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.layout.positionInParent
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.hampouch.R
import com.example.hampouch.domain.model.TipComment
import com.example.hampouch.domain.model.TipPost
import com.example.hampouch.domain.model.TipPostType
import com.example.hampouch.domain.model.TipReply
import com.example.hampouch.ui.hamtips.components.HamTipsCommentInputBar
import com.example.hampouch.ui.hamtips.components.HamTipsCommentRow
import com.example.hampouch.ui.hamtips.components.HamTipsMenuSheetItem
import com.example.hampouch.ui.hamtips.components.HamTipsMoreMenuSheet
import com.example.hampouch.ui.hamtips.components.HamTipsPhotoCarousel
import com.example.hampouch.ui.mypage.components.TipCategoryBadge
import com.example.hampouch.ui.theme.HPBlack
import com.example.hampouch.ui.theme.HPGray2
import com.example.hampouch.ui.theme.HPGray4
import com.example.hampouch.ui.theme.HPMain
import com.example.hampouch.ui.theme.HPStar
import com.example.hampouch.ui.theme.HPSub
import com.example.hampouch.ui.theme.HPSub3
import com.example.hampouch.ui.theme.HPSub4
import com.example.hampouch.ui.theme.HPText
import com.example.hampouch.ui.theme.HPWhite
import com.example.hampouch.ui.theme.HampouchTheme
import kotlinx.coroutines.launch

@Composable
fun HamTipsPostDetailTopBar(
    title: String,
    showMoreButton: Boolean,
    onBackClick: () -> Unit,
    onMoreClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .statusBarsPadding()
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
        if (showMoreButton) {
            IconButton(onClick = onMoreClick) {
                Icon(
                    imageVector = Icons.Filled.MoreVert,
                    contentDescription = stringResource(R.string.hamtips_cd_more),
                    tint = HPBlack
                )
            }
        } else {
            Spacer(modifier = Modifier.width(48.dp))
        }
    }
}

@Composable
private fun DashedDivider(modifier: Modifier = Modifier) {
    val dash = PathEffect.dashPathEffect(floatArrayOf(6f, 6f))
    Canvas(modifier = modifier.fillMaxWidth().height(1.dp)) {
        drawLine(HPGray4, Offset.Zero, Offset(size.width, 0f), pathEffect = dash)
    }
}

@Composable
private fun MenuInfoRow(label: String, value: String, modifier: Modifier = Modifier) {
    Row(modifier = modifier.fillMaxWidth()) {
        Text(text = label, style = MaterialTheme.typography.bodyMedium, color = HPText)
        Spacer(modifier = Modifier.width(8.dp))
        Text(
            text = value,
            style = MaterialTheme.typography.bodyMedium,
            color = HPMain,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.weight(1f),
            textAlign = TextAlign.End,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
    }
}

@Composable
private fun MenuRatingRow(label: String, rating: Int, modifier: Modifier = Modifier) {
    Row(modifier = modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
        Text(text = label, style = MaterialTheme.typography.bodyMedium, color = HPBlack, modifier = Modifier.width(56.dp))
        Row {
            repeat(5) { index ->
                Icon(
                    imageVector = if (index < rating) Icons.Filled.Star else Icons.Filled.StarBorder,
                    contentDescription = null,
                    tint = if (index < rating) HPStar else HPGray4,
                    modifier = Modifier.padding(end = 2.dp)
                )
            }
        }
        Spacer(modifier = Modifier.weight(1f))
        Text(
            text = stringResource(R.string.hamtips_rating_value_format, rating.toFloat()),
            style = MaterialTheme.typography.bodyMedium,
            color = HPText,
            fontWeight = FontWeight.Bold
        )
    }
}

@Composable
private fun MenuDetailCard(post: TipPost, modifier: Modifier = Modifier) {
    val rating = post.menuRating ?: return
    Column(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(20.dp))
            .background(HPSub3)
            .padding(16.dp)
    ) {
        MenuInfoRow(stringResource(R.string.hamtips_detail_menu_name_row), post.menuName)
        Spacer(modifier = Modifier.height(10.dp))
        DashedDivider()
        Spacer(modifier = Modifier.height(10.dp))
        MenuInfoRow(stringResource(R.string.hamtips_detail_menu_place_row), post.place)
        Spacer(modifier = Modifier.height(10.dp))
        DashedDivider()
        Spacer(modifier = Modifier.height(10.dp))
        MenuInfoRow(
            stringResource(R.string.hamtips_detail_menu_price_row),
            stringResource(R.string.hamtips_detail_menu_price_value_format, "%,d".format(post.price))
        )
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            text = stringResource(R.string.hamtips_detail_menu_rating_section),
            style = MaterialTheme.typography.bodyMedium,
            color = HPText
        )
        Spacer(modifier = Modifier.height(8.dp))
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(16.dp))
                .background(HPWhite)
                .padding(horizontal = 14.dp, vertical = 10.dp)
        ) {
            MenuRatingRow(stringResource(R.string.hamtips_rating_taste), rating.taste)
            Spacer(modifier = Modifier.height(6.dp))
            MenuRatingRow(stringResource(R.string.hamtips_rating_cost_effectiveness), rating.costEffectiveness)
            Spacer(modifier = Modifier.height(6.dp))
            MenuRatingRow(stringResource(R.string.hamtips_rating_mood), rating.mood)
        }
    }
}

@Composable
internal fun HamTipsPostHeader(post: TipPost, modifier: Modifier = Modifier) {
    Column(modifier = modifier.fillMaxWidth()) {
        TipCategoryBadge(category = post.category)
        Spacer(modifier = Modifier.height(10.dp))
        Text(text = post.title, style = MaterialTheme.typography.titleSmall, color = HPBlack)
        Spacer(modifier = Modifier.height(8.dp))
        Row(modifier = Modifier.fillMaxWidth()) {
            Text(
                text = post.authorName,
                style = MaterialTheme.typography.bodySmall,
                color = if (post.isEditorAuthor) HPSub else HPText,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.weight(1f, fill = false),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Text(
                text = stringResource(
                    R.string.hamtips_detail_meta_suffix_format,
                    formatTimeAgoLabel(post.postedMinutesAgo),
                    "%,d".format(post.viewCount)
                ),
                style = MaterialTheme.typography.bodySmall,
                color = HPText
            )
        }
    }
}

@Composable
internal fun HamTipsEngagementRow(
    post: TipPost,
    onLikeClick: () -> Unit,
    onScrapClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Row(modifier = modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
        Icon(
            imageVector = Icons.Outlined.ChatBubbleOutline,
            contentDescription = null,
            tint = HPText,
            modifier = Modifier.height(18.dp)
        )
        Spacer(modifier = Modifier.width(4.dp))
        Text(text = post.commentCount.toString(), style = MaterialTheme.typography.bodySmall, color = HPText)
        Spacer(modifier = Modifier.width(14.dp))
        Icon(
            imageVector = if (post.isLiked) Icons.Filled.Favorite else Icons.Outlined.FavoriteBorder,
            contentDescription = stringResource(R.string.hamtips_cd_like),
            tint = if (post.isLiked) HPMain else HPText,
            modifier = Modifier
                .height(18.dp)
                .clickable(onClick = onLikeClick)
        )
        Spacer(modifier = Modifier.width(4.dp))
        Text(text = post.likeCount.toString(), style = MaterialTheme.typography.bodySmall, color = HPText)
        Spacer(modifier = Modifier.weight(1f))
        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.clickable(onClick = onScrapClick)) {
            Icon(
                imageVector = if (post.isSaved) Icons.Filled.Bookmark else Icons.Outlined.BookmarkBorder,
                contentDescription = stringResource(R.string.hamtips_cd_scrap),
                tint = if (post.isSaved) HPMain else HPText,
                modifier = Modifier.height(18.dp)
            )
            Spacer(modifier = Modifier.width(4.dp))
            Text(
                text = stringResource(if (post.isSaved) R.string.hamtips_cd_scrap_done else R.string.hamtips_cd_scrap),
                style = MaterialTheme.typography.bodySmall,
                color = HPText
            )
        }
    }
}

internal fun submitHamTipsComment(
    viewModel: HamTipsDetailViewModel,
    post: TipPost,
    replyTarget: TipComment?,
    content: String
) {
    val target = replyTarget
    if (target != null) {
        viewModel.addReply(post.id, target.id, content)
    } else {
        viewModel.addComment(post.id, content)
    }
}

@Composable
internal fun HamTipsCommentBottomBar(
    replyTarget: TipComment?,
    commentInput: String,
    onCommentInputChange: (String) -> Unit,
    onSubmit: () -> Unit,
    modifier: Modifier = Modifier
) {
    HamTipsCommentInputBar(
        value = commentInput,
        onValueChange = onCommentInputChange,
        onSubmit = onSubmit,
        placeholder = stringResource(
            if (replyTarget != null) R.string.hamtips_reply_input_placeholder else R.string.hamtips_comment_input_placeholder
        ),
        replyTargetName = replyTarget?.authorName,
        modifier = modifier
    )
}

@Composable
internal fun HamTipsCommentListColumn(
    post: TipPost,
    onReplyClick: (TipComment) -> Unit,
    onMoreClick: (TipComment) -> Unit,
    onReplyMoreClick: (TipComment, TipReply) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier.padding(horizontal = 20.dp)) {
        post.comments.forEach { comment ->
            HamTipsCommentRow(
                comment = comment,
                showAuthorTag = comment.authorId == post.authorId && post.authorId.isNotBlank(),
                onReplyClick = { onReplyClick(comment) },
                onMoreClick = { onMoreClick(comment) },
                onReplyMoreClick = { reply -> onReplyMoreClick(comment, reply) }
            )
        }
    }
}

@Composable
internal fun HamTipsCommentMoreMenuHost(
    target: TipComment?,
    canDelete: (TipComment) -> Boolean,
    onDelete: (TipComment) -> Unit,
    onDismiss: () -> Unit
) {
    target?.let { comment ->
        val items = buildList {
            if (canDelete(comment)) {
                add(
                    HamTipsMenuSheetItem(
                        label = stringResource(R.string.hamtips_more_menu_delete),
                        isDestructive = true,
                        onClick = {
                            onDelete(comment)
                            onDismiss()
                        }
                    )
                )
            }
            add(
                HamTipsMenuSheetItem(
                    label = stringResource(R.string.hamtips_more_menu_close),
                    onClick = onDismiss
                )
            )
        }
        HamTipsMoreMenuSheet(items = items, onDismiss = onDismiss)
    }
}

@Composable
internal fun HamTipsReplyMoreMenuHost(
    target: Pair<TipComment, TipReply>?,
    canDelete: (TipReply) -> Boolean,
    onDelete: (TipComment, TipReply) -> Unit,
    onDismiss: () -> Unit
) {
    target?.let { (comment, reply) ->
        val items = buildList {
            if (canDelete(reply)) {
                add(
                    HamTipsMenuSheetItem(
                        label = stringResource(R.string.hamtips_more_menu_delete),
                        isDestructive = true,
                        onClick = {
                            onDelete(comment, reply)
                            onDismiss()
                        }
                    )
                )
            }
            add(
                HamTipsMenuSheetItem(
                    label = stringResource(R.string.hamtips_more_menu_close),
                    onClick = onDismiss
                )
            )
        }
        HamTipsMoreMenuSheet(items = items, onDismiss = onDismiss)
    }
}

@Composable
fun HamTipsDetailScreen(
    post: TipPost,
    onBackClick: () -> Unit,
    onEditClick: (TipPost) -> Unit,
    onDeleted: () -> Unit,
    scrollToComments: Boolean = false,
    modifier: Modifier = Modifier,
    sessionViewModel: SessionViewModel = hiltViewModel(),
    viewModel: HamTipsDetailViewModel = hiltViewModel()
) {
    val context = LocalContext.current

    LaunchedEffect(post.id) { viewModel.loadDetail(post.id) }
    LaunchedEffect(viewModel) {
        viewModel.events.collect { event ->
            when (event) {
                HamTipsDetailEvent.PostDeleted -> onDeleted()
                is HamTipsDetailEvent.BattleJoined,
                HamTipsDetailEvent.BattleFull,
                is HamTipsDetailEvent.BattleAlreadyStarted,
                is HamTipsDetailEvent.BattleAlreadyJoined,
                is HamTipsDetailEvent.BattleCancelled -> Unit
                is HamTipsDetailEvent.ShowMessage ->
                    Toast.makeText(context, event.message, Toast.LENGTH_SHORT).show()
            }
        }
    }

    val currentUser by sessionViewModel.currentUser.collectAsStateWithLifecycle()
    val isAuthor = post.authorId == currentUser.id
    val canDeletePost = viewModel.canDeletePost(post)

    HamTipsDetailContent(
        post = post,
        onBackClick = onBackClick,
        onEditClick = onEditClick,
        scrollToComments = scrollToComments,
        modifier = modifier,
        isAuthor = isAuthor,
        canDeletePost = canDeletePost,
        onLikeClick = { viewModel.toggleLike(post.id) },
        onScrapClick = { viewModel.toggleSave(post.id) },
        onDeletePost = { viewModel.deletePost(post.id) },
        onSubmitComment = { replyTarget, content -> submitHamTipsComment(viewModel, post, replyTarget, content) },
        canDeleteComment = { comment -> viewModel.canDeleteComment(post, comment) },
        onDeleteComment = { comment -> viewModel.deleteComment(post.id, comment.id) },
        canDeleteReply = { reply -> viewModel.canDeleteReply(post, reply) },
        onDeleteReply = { comment, reply -> viewModel.deleteReply(post.id, comment.id, reply.id) }
    )
}

@Composable
private fun HamTipsDetailContent(
    post: TipPost,
    onBackClick: () -> Unit,
    onEditClick: (TipPost) -> Unit,
    scrollToComments: Boolean,
    modifier: Modifier,
    isAuthor: Boolean,
    canDeletePost: Boolean,
    onLikeClick: () -> Unit,
    onScrapClick: () -> Unit,
    onDeletePost: () -> Unit,
    onSubmitComment: (TipComment?, String) -> Unit,
    canDeleteComment: (TipComment) -> Boolean,
    onDeleteComment: (TipComment) -> Unit,
    canDeleteReply: (TipReply) -> Boolean,
    onDeleteReply: (TipComment, TipReply) -> Unit
) {
    var showPostMenu by remember { mutableStateOf(false) }
    var commentMenuTarget by remember { mutableStateOf<TipComment?>(null) }
    var replyMenuTarget by remember { mutableStateOf<Pair<TipComment, TipReply>?>(null) }
    var replyTarget by remember { mutableStateOf<TipComment?>(null) }
    var commentInput by remember { mutableStateOf("") }
    val scrollState = rememberScrollState()
    var commentsSectionOffset by remember { mutableStateOf<Int?>(null) }
    var hasScrolledToComments by remember { mutableStateOf(false) }

    LaunchedEffect(scrollToComments, commentsSectionOffset) {
        val offset = commentsSectionOffset
        if (scrollToComments && !hasScrolledToComments && offset != null) {
            hasScrolledToComments = true
            scrollState.animateScrollTo(offset)
        }
    }

    val titleRes = if (post.isEditorAuthor) R.string.hamtips_pochipick_title else R.string.hamtips_title

    Scaffold(
        modifier = modifier,
        containerColor = HPGray2,
        topBar = {
            HamTipsPostDetailTopBar(
                title = stringResource(titleRes),
                showMoreButton = canDeletePost,
                onBackClick = onBackClick,
                onMoreClick = { showPostMenu = true }
            )
        },
        bottomBar = {
            HamTipsCommentBottomBar(
                replyTarget = replyTarget,
                commentInput = commentInput,
                onCommentInputChange = { commentInput = it },
                onSubmit = {
                    val submittedInput = commentInput
                    val submittedReplyTarget = replyTarget
                    commentInput = ""
                    replyTarget = null
                    onSubmitComment(submittedReplyTarget, submittedInput)
                }
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .padding(innerPadding)
                .fillMaxSize()
                .verticalScroll(scrollState)
        ) {
            Column(modifier = Modifier.padding(horizontal = 20.dp)) {
                Spacer(modifier = Modifier.height(4.dp))
                HamTipsPostHeader(post = post)
                Spacer(modifier = Modifier.height(16.dp))
                if (post.type == TipPostType.MENU) {
                    MenuDetailCard(post = post)
                    Spacer(modifier = Modifier.height(16.dp))
                }
                if (post.content.isNotBlank()) {
                    Text(text = post.content, style = MaterialTheme.typography.bodyMedium, color = HPBlack)
                    Spacer(modifier = Modifier.height(16.dp))
                }
                if (post.imageUris.isNotEmpty()) {
                    HamTipsPhotoCarousel(photoUris = post.imageUris)
                    Spacer(modifier = Modifier.height(16.dp))
                }
                HamTipsEngagementRow(
                    post = post,
                    onLikeClick = onLikeClick,
                    onScrapClick = onScrapClick
                )
                Spacer(modifier = Modifier.height(16.dp))
            }
            HamTipsCommentListColumn(
                post = post,
                onReplyClick = { replyTarget = it },
                onMoreClick = { commentMenuTarget = it },
                onReplyMoreClick = { comment, reply -> replyMenuTarget = comment to reply },
                modifier = Modifier.onGloballyPositioned { coordinates ->
                    commentsSectionOffset = coordinates.positionInParent().y.toInt()
                }
            )
            Spacer(modifier = Modifier.height(24.dp))
        }
    }

    if (showPostMenu) {
        HamTipsMoreMenuSheet(
            items = buildList {
                if (canDeletePost) {
                    add(
                        HamTipsMenuSheetItem(
                            label = stringResource(R.string.hamtips_more_menu_delete),
                            isDestructive = true,
                            onClick = {
                                showPostMenu = false
                                onDeletePost()
                            }
                        )
                    )
                }
                if (isAuthor) {
                    add(
                        HamTipsMenuSheetItem(
                            label = stringResource(R.string.hamtips_more_menu_edit),
                            onClick = {
                                showPostMenu = false
                                onEditClick(post)
                            }
                        )
                    )
                }
                add(
                    HamTipsMenuSheetItem(
                        label = stringResource(R.string.hamtips_more_menu_close),
                        onClick = { showPostMenu = false }
                    )
                )
            },
            onDismiss = { showPostMenu = false }
        )
    }

    HamTipsCommentMoreMenuHost(
        target = commentMenuTarget,
        canDelete = canDeleteComment,
        onDelete = onDeleteComment,
        onDismiss = { commentMenuTarget = null }
    )

    HamTipsReplyMoreMenuHost(
        target = replyMenuTarget,
        canDelete = canDeleteReply,
        onDelete = onDeleteReply,
        onDismiss = { replyMenuTarget = null }
    )
}

@Preview(showBackground = true, name = "4. 게시글 상세 - 포치픽")
@Composable
private fun HamTipsDetailScreenPochipickPreview() {
    HampouchTheme {
        HamTipsDetailContent(
            post = HamTipsMockData.allPosts().first { it.id == "hamtip_1" },
            onBackClick = {},
            onEditClick = {},
            scrollToComments = false,
            modifier = Modifier,
            isAuthor = false,
            canDeletePost = false,
            onLikeClick = {},
            onScrapClick = {},
            onDeletePost = {},
            onSubmitComment = { _, _ -> },
            canDeleteComment = { false },
            onDeleteComment = {},
            canDeleteReply = { false },
            onDeleteReply = { _, _ -> }
        )
    }
}

@Preview(showBackground = true, name = "3・14. 게시글 상세 - 메뉴 추천")
@Composable
private fun HamTipsDetailScreenMenuPreview() {
    HampouchTheme {
        HamTipsDetailContent(
            post = HamTipsMockData.allPosts().first { it.id == "hamtip_14" },
            onBackClick = {},
            onEditClick = {},
            scrollToComments = false,
            modifier = Modifier,
            isAuthor = false,
            canDeletePost = false,
            onLikeClick = {},
            onScrapClick = {},
            onDeletePost = {},
            onSubmitComment = { _, _ -> },
            canDeleteComment = { false },
            onDeleteComment = {},
            canDeleteReply = { false },
            onDeleteReply = { _, _ -> }
        )
    }
}
