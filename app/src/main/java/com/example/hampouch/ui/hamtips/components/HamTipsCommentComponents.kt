package com.example.hampouch.ui.hamtips.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.ime
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.union
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.hampouch.R
import com.example.hampouch.data.model.TipComment
import com.example.hampouch.data.model.TipReply
import com.example.hampouch.ui.mypage.components.SettingsMenuDivider
import com.example.hampouch.ui.theme.HPBlack
import com.example.hampouch.ui.theme.HPGray3
import com.example.hampouch.ui.theme.HPGray5
import com.example.hampouch.ui.theme.HPMain
import com.example.hampouch.ui.theme.HPReplyTag
import com.example.hampouch.ui.theme.HPSub4
import com.example.hampouch.ui.theme.HPText
import com.example.hampouch.ui.theme.HPWhite
import com.example.hampouch.ui.theme.HampouchTheme

data class HamTipsMenuSheetItem(
    val label: String,
    val isDestructive: Boolean = false,
    val onClick: () -> Unit
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HamTipsMoreMenuSheet(items: List<HamTipsMenuSheetItem>, onDismiss: () -> Unit) {
    val sheetState = rememberModalBottomSheetState()
    ModalBottomSheet(onDismissRequest = onDismiss, sheetState = sheetState, containerColor = HPWhite) {
        Column(modifier = Modifier.fillMaxWidth().padding(bottom = 12.dp)) {
            items.forEachIndexed { index, item ->
                Text(
                    text = item.label,
                    style = MaterialTheme.typography.bodyMedium,
                    color = if (item.isDestructive) HPMain else HPBlack,
                    fontWeight = FontWeight.Bold,
                    textAlign = TextAlign.Center,
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable(onClick = item.onClick)
                        .padding(vertical = 16.dp)
                )
                if (index != items.lastIndex) SettingsMenuDivider()
            }
        }
    }
}

@Composable
fun HamTipsProfileAvatar(size: androidx.compose.ui.unit.Dp = 36.dp, modifier: Modifier = Modifier) {
    Box(
        modifier = modifier
            .size(size)
            .clip(CircleShape)
            .background(HPGray5),
        contentAlignment = Alignment.Center
    ) {
        Icon(
            imageVector = Icons.Filled.Person,
            contentDescription = null,
            tint = HPWhite,
            modifier = Modifier.size(size / 2)
        )
    }
}

@Composable
fun HamTipsAuthorTagChip(modifier: Modifier = Modifier) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(50))
            .background(HPSub4)
            .padding(horizontal = 8.dp, vertical = 3.dp)
    ) {
        Text(
            text = stringResource(R.string.hamtips_comment_author_tag),
            style = MaterialTheme.typography.labelSmall,
            color = HPMain,
            fontWeight = FontWeight.Bold
        )
    }
}

@Composable
fun HamTipsReplyRow(
    reply: TipReply,
    onMoreClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier.fillMaxWidth().padding(start = 46.dp, top = 10.dp),
        verticalAlignment = Alignment.Top
    ) {
        HamTipsProfileAvatar(size = 28.dp)
        Spacer(modifier = Modifier.width(8.dp))
        Column(modifier = Modifier.weight(1f)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = if (reply.isDeleted) stringResource(R.string.hamtips_comment_deleted_author) else reply.authorName,
                    style = MaterialTheme.typography.bodySmall,
                    color = HPBlack,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.weight(1f, fill = false),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                if (!reply.isDeleted) {
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(text = reply.timeLabel, style = MaterialTheme.typography.labelSmall, color = HPText)
                }
            }
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = if (reply.isDeleted) stringResource(R.string.hamtips_reply_deleted) else reply.content,
                style = MaterialTheme.typography.bodySmall,
                color = if (reply.isDeleted) HPText else HPBlack
            )
        }
        if (!reply.isDeleted) {
            IconButton(onClick = onMoreClick, modifier = Modifier.size(32.dp)) {
                Icon(
                    imageVector = Icons.Filled.MoreVert,
                    contentDescription = stringResource(R.string.hamtips_cd_comment_more),
                    tint = HPText,
                    modifier = Modifier.size(18.dp)
                )
            }
        }
    }
}

@Composable
fun HamTipsCommentRow(
    comment: TipComment,
    showAuthorTag: Boolean,
    onReplyClick: () -> Unit,
    onMoreClick: () -> Unit,
    onReplyMoreClick: (TipReply) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier.fillMaxWidth().padding(top = 16.dp)) {
        Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.Top) {
            HamTipsProfileAvatar(size = 36.dp)
            Spacer(modifier = Modifier.width(10.dp))
            Column(modifier = Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = if (comment.isDeleted) stringResource(R.string.hamtips_comment_deleted_author) else comment.authorName,
                        style = MaterialTheme.typography.bodyMedium,
                        color = HPBlack,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.weight(1f, fill = false),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    if (!comment.isDeleted && showAuthorTag) {
                        Spacer(modifier = Modifier.width(6.dp))
                        HamTipsAuthorTagChip()
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    if (!comment.isDeleted) {
                        Text(text = comment.timeLabel, style = MaterialTheme.typography.labelSmall, color = HPText)
                    }
                }
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = if (comment.isDeleted) stringResource(R.string.hamtips_comment_deleted) else comment.content,
                    style = MaterialTheme.typography.bodyMedium,
                    color = if (comment.isDeleted) HPText else HPBlack
                )
                if (!comment.isDeleted) {
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = stringResource(R.string.hamtips_comment_reply_action),
                        style = MaterialTheme.typography.labelMedium,
                        color = HPText,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.clickable(onClick = onReplyClick)
                    )
                }
            }
            if (!comment.isDeleted) {
                IconButton(onClick = onMoreClick, modifier = Modifier.size(32.dp)) {
                    Icon(
                        imageVector = Icons.Filled.MoreVert,
                        contentDescription = stringResource(R.string.hamtips_cd_comment_more),
                        tint = HPText,
                        modifier = Modifier.size(18.dp)
                    )
                }
            }
        }
        comment.replies.forEach { reply ->
            HamTipsReplyRow(reply = reply, onMoreClick = { onReplyMoreClick(reply) })
        }
    }
}

@Composable
fun HamTipsCommentInputBar(
    value: String,
    onValueChange: (String) -> Unit,
    onSubmit: () -> Unit,
    placeholder: String,
    replyTargetName: String?,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .background(HPWhite)
            .windowInsetsPadding(WindowInsets.navigationBars.union(WindowInsets.ime))
            .padding(horizontal = 16.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(
            modifier = Modifier
                .weight(1f)
                .clip(RoundedCornerShape(50))
                .background(HPGray3)
                .padding(horizontal = 14.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            if (replyTargetName != null) {
                Text(
                    text = stringResource(R.string.hamtips_reply_target_tag_format, replyTargetName),
                    style = MaterialTheme.typography.labelMedium,
                    color = HPReplyTag,
                    fontWeight = FontWeight.Bold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Spacer(modifier = Modifier.width(6.dp))
            }
            Box(modifier = Modifier.weight(1f)) {
                if (value.isEmpty()) {
                    Text(text = placeholder, style = MaterialTheme.typography.bodyMedium, color = HPText)
                }
                BasicTextField(
                    value = value,
                    onValueChange = onValueChange,
                    singleLine = true,
                    textStyle = MaterialTheme.typography.bodyMedium.copy(color = HPBlack),
                    cursorBrush = SolidColor(HPMain),
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }
        Spacer(modifier = Modifier.width(10.dp))
        Text(
            text = stringResource(R.string.hamtips_comment_submit),
            style = MaterialTheme.typography.bodyMedium,
            color = if (value.isNotBlank()) HPMain else HPGray5,
            fontWeight = FontWeight.Bold,
            modifier = Modifier
                .clickable(enabled = value.isNotBlank(), onClick = onSubmit)
                .padding(horizontal = 4.dp, vertical = 6.dp)
        )
    }
}

@Preview(showBackground = true, name = "6. 댓글 더보기")
@Composable
private fun HamTipsCommentMoreMenuSheetPreview() {
    HampouchTheme {
        HamTipsMoreMenuSheet(
            items = listOf(
                HamTipsMenuSheetItem(label = stringResource(R.string.hamtips_more_menu_delete), isDestructive = true, onClick = {}),
                HamTipsMenuSheetItem(label = stringResource(R.string.hamtips_more_menu_close), onClick = {})
            ),
            onDismiss = {}
        )
    }
}

@Preview(showBackground = true, name = "7. 게시글 더보기")
@Composable
private fun HamTipsPostMoreMenuSheetPreview() {
    HampouchTheme {
        HamTipsMoreMenuSheet(
            items = listOf(
                HamTipsMenuSheetItem(label = stringResource(R.string.hamtips_more_menu_delete), isDestructive = true, onClick = {}),
                HamTipsMenuSheetItem(label = stringResource(R.string.hamtips_more_menu_edit), onClick = {}),
                HamTipsMenuSheetItem(label = stringResource(R.string.hamtips_more_menu_close), onClick = {})
            ),
            onDismiss = {}
        )
    }
}

@Preview(showBackground = true, name = "8. 대댓글 입력")
@Composable
private fun HamTipsReplyInputBarPreview() {
    HampouchTheme {
        HamTipsCommentInputBar(
            value = "",
            onValueChange = {},
            onSubmit = {},
            placeholder = stringResource(R.string.hamtips_reply_input_placeholder),
            replyTargetName = "집밥수련생"
        )
    }
}

@Preview(showBackground = true, name = "9. 삭제된 댓글")
@Composable
private fun HamTipsDeletedCommentRowPreview() {
    HampouchTheme {
        HamTipsCommentRow(
            comment = TipComment(
                id = "preview_deleted",
                authorId = "user_jipbap",
                authorName = "집밥수련생",
                content = "소분 냉동꿀팁 감사해요! 저도 알림부터 꺼봐야겠어요.",
                timeLabel = "1분전",
                isDeleted = true,
                replies = listOf(
                    TipReply(id = "preview_reply_1", authorId = "user_altteuli", authorName = "알뜰이", content = "굿굿", timeLabel = "방금"),
                    TipReply(id = "preview_reply_2", authorId = "user_altteuli", authorName = "알뜰이", content = "굿굿", timeLabel = "방금")
                )
            ),
            showAuthorTag = false,
            onReplyClick = {},
            onMoreClick = {},
            onReplyMoreClick = {}
        )
    }
}
