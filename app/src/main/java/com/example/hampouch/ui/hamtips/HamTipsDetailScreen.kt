package com.example.hampouch.ui.hamtips

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
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.hampouch.R
import com.example.hampouch.data.model.TipComment
import com.example.hampouch.data.model.TipPost
import com.example.hampouch.data.model.TipPostType
import com.example.hampouch.data.model.TipReply
import com.example.hampouch.ui.hamtips.components.HamTipsCommentInputBar
import com.example.hampouch.ui.hamtips.components.HamTipsCommentRow
import com.example.hampouch.ui.hamtips.components.HamTipsMenuSheetItem
import com.example.hampouch.ui.hamtips.components.HamTipsMoreMenuSheet
import com.example.hampouch.ui.hamtips.components.HamTipsPhotoCarousel
import com.example.hampouch.ui.mypage.components.TipCategoryBadge
import com.example.hampouch.ui.session.UserSession
import com.example.hampouch.ui.theme.HPBlack
import com.example.hampouch.ui.theme.HPGray2
import com.example.hampouch.ui.theme.HPGray4
import com.example.hampouch.ui.theme.HPMain
import com.example.hampouch.ui.theme.HPStar
import com.example.hampouch.ui.theme.HPSub
import com.example.hampouch.ui.theme.HPSub4
import com.example.hampouch.ui.theme.HPText
import com.example.hampouch.ui.theme.HPWhite
import com.example.hampouch.ui.theme.HampouchTheme

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
            .background(HPSub4)
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

internal fun formatTimeAgoLabel(minutesAgo: Int): String =
    if (minutesAgo < 60) "${minutesAgo}분전" else "${minutesAgo / 60}시간전"

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

internal fun submitHamTipsComment(post: TipPost, replyTarget: TipComment?, content: String) {
    val target = replyTarget
    if (target != null) {
        HamTipsRepository.addReply(post.id, target.id, content)
    } else {
        HamTipsRepository.addComment(post.id, content)
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
    post: TipPost,
    target: TipComment?,
    onDismiss: () -> Unit
) {
    target?.let { comment ->
        val items = buildList {
            if (HamTipsRepository.canDeleteComment(post, comment)) {
                add(
                    HamTipsMenuSheetItem(
                        label = stringResource(R.string.hamtips_more_menu_delete),
                        isDestructive = true,
                        onClick = {
                            HamTipsRepository.deleteComment(post.id, comment.id)
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
    post: TipPost,
    target: Pair<TipComment, TipReply>?,
    onDismiss: () -> Unit
) {
    target?.let { (comment, reply) ->
        val items = buildList {
            if (HamTipsRepository.canDeleteReply(post, reply)) {
                add(
                    HamTipsMenuSheetItem(
                        label = stringResource(R.string.hamtips_more_menu_delete),
                        isDestructive = true,
                        onClick = {
                            HamTipsRepository.deleteReply(post.id, comment.id, reply.id)
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
    modifier: Modifier = Modifier
) {
    var showPostMenu by remember { mutableStateOf(false) }
    var commentMenuTarget by remember { mutableStateOf<TipComment?>(null) }
    var replyMenuTarget by remember { mutableStateOf<Pair<TipComment, TipReply>?>(null) }
    var replyTarget by remember { mutableStateOf<TipComment?>(null) }
    var commentInput by remember { mutableStateOf("") }

    val isAuthor = post.authorId == UserSession.currentUser.id
    val canDeletePost = HamTipsRepository.canDeletePost(post)
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
                    submitHamTipsComment(post, replyTarget, commentInput)
                    commentInput = ""
                    replyTarget = null
                }
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .padding(innerPadding)
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
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
                    onLikeClick = { HamTipsRepository.toggleLike(post.id) },
                    onScrapClick = { HamTipsRepository.toggleSave(post.id) }
                )
                Spacer(modifier = Modifier.height(16.dp))
            }
            HamTipsCommentListColumn(
                post = post,
                onReplyClick = { replyTarget = it },
                onMoreClick = { commentMenuTarget = it },
                onReplyMoreClick = { comment, reply -> replyMenuTarget = comment to reply }
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
                                HamTipsRepository.deletePost(post.id)
                                onDeleted()
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
        post = post,
        target = commentMenuTarget,
        onDismiss = { commentMenuTarget = null }
    )

    HamTipsReplyMoreMenuHost(
        post = post,
        target = replyMenuTarget,
        onDismiss = { replyMenuTarget = null }
    )
}

@Preview(showBackground = true, name = "4. 게시글 상세 - 포치픽")
@Composable
private fun HamTipsDetailScreenPochipickPreview() {
    HampouchTheme {
        HamTipsDetailScreen(
            post = HamTipsMockData.allPosts().first { it.id == "hamtip_1" },
            onBackClick = {},
            onEditClick = {},
            onDeleted = {}
        )
    }
}

@Preview(showBackground = true, name = "3・14. 게시글 상세 - 메뉴 추천")
@Composable
private fun HamTipsDetailScreenMenuPreview() {
    HampouchTheme {
        HamTipsDetailScreen(
            post = HamTipsMockData.allPosts().first { it.id == "hamtip_14" },
            onBackClick = {},
            onEditClick = {},
            onDeleted = {}
        )
    }
}
