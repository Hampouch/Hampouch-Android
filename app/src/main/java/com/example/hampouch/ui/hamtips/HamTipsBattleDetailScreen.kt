package com.example.hampouch.ui.hamtips

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.hampouch.R
import com.example.hampouch.core.config.BattleConfig
import com.example.hampouch.domain.model.HamBattleChallengeRequest
import com.example.hampouch.domain.model.HamBattleChallenge
import com.example.hampouch.domain.model.BattleRecruitInfo
import com.example.hampouch.domain.model.TipComment
import com.example.hampouch.domain.model.TipPost
import com.example.hampouch.domain.model.TipPostDetail
import com.example.hampouch.domain.model.TipReply
import com.example.hampouch.ui.dialog.ChallengeSummaryCard
import com.example.hampouch.ui.dialog.ConfirmActionCard
import com.example.hampouch.ui.dialog.HamBattleRoomFullDialog
import com.example.hampouch.ui.hambattle.HamBattleViewModel
import com.example.hampouch.ui.hamtips.components.HamTipsMenuSheetItem
import com.example.hampouch.ui.hamtips.components.HamTipsMoreMenuSheet
import com.example.hampouch.ui.hamtips.components.HamTipsSubmitButton
import com.example.hampouch.ui.theme.HPBlack
import com.example.hampouch.ui.theme.HPGray2
import com.example.hampouch.ui.theme.HPGray4
import com.example.hampouch.ui.theme.HPMain
import com.example.hampouch.ui.theme.HPSub3
import com.example.hampouch.ui.theme.HPText
import com.example.hampouch.ui.theme.HampouchTheme
import java.time.ZoneOffset
import kotlinx.coroutines.launch

private fun battleChallengeRequestFrom(
    post: TipPost,
    durationLabel: String,
    capacityLabel: String,
    linkedChallenge: HamBattleChallenge? = null
) = HamBattleChallengeRequest(
    challengeName = linkedChallenge?.title ?: post.title,
    participantCount = capacityLabel,
    durationDays = durationLabel,
    startDateMillis = linkedChallenge?.startDate
        ?.atStartOfDay(ZoneOffset.UTC)
        ?.toInstant()
        ?.toEpochMilli() ?: 0L,
    penalty = linkedChallenge?.penalty ?: post.battleInfo?.penalty.orEmpty()
)

@Composable
private fun HamTipsBattleInfoCard(
    summaryRequest: HamBattleChallengeRequest,
    showActionButton: Boolean,
    isFull: Boolean,
    onActionClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(20.dp))
            .background(HPSub3)
            .padding(15.dp)
    ) {
        Text(
            text = "${summaryRequest.durationDays} · ${summaryRequest.participantCount}",
            style = MaterialTheme.typography.bodySmall,
            fontWeight = FontWeight.Bold,
            color = HPText
        )
        Spacer(modifier = Modifier.height(6.dp))
        Text(
            text = summaryRequest.challengeName,
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.Bold,
            color = HPBlack
        )
        Spacer(modifier = Modifier.height(6.dp))
        Row(modifier = Modifier.fillMaxWidth()) {
            Text(
                text = stringResource(R.string.hamtips_battle_penalty_label),
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Bold,
                color = HPText
            )
            Spacer(modifier = Modifier.width(4.dp))
            Text(
                text = summaryRequest.penalty,
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Bold,
                color = HPMain,
                modifier = Modifier.weight(1f),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
        if (showActionButton) {
            Spacer(modifier = Modifier.height(16.dp))
            HamTipsSubmitButton(
                text = stringResource(if (isFull) R.string.hamtips_battle_recruit_done else R.string.hamtips_battle_join),
                enabled = !isFull,
                onClick = onActionClick,
                disabledContainerColor = HPGray4,
                disabledContentColor = HPText
            )
        }
    }
}

@Composable
fun HamTipsBattleDetailScreen(
    post: TipPost,
    onBackClick: () -> Unit,
    onDeleted: () -> Unit,
    onNavigateToBattleLink: (String) -> Unit,
    onNavigateToHamBattleTab: () -> Unit,
    onEditClick: (TipPost) -> Unit,
    modifier: Modifier = Modifier,
    sessionViewModel: SessionViewModel = hiltViewModel(),
    viewModel: HamTipsDetailViewModel = hiltViewModel()
) {
    var showRoomFull by remember { mutableStateOf(false) }
    val context = LocalContext.current

    LaunchedEffect(post.id) { viewModel.loadDetail(post.id) }
    LaunchedEffect(viewModel) {
        viewModel.events.collect { event ->
            when (event) {
                HamTipsDetailEvent.PostDeleted -> onDeleted()
                is HamTipsDetailEvent.BattleJoined -> onNavigateToBattleLink(event.battleId.toString())
                HamTipsDetailEvent.BattleFull -> showRoomFull = true
                is HamTipsDetailEvent.BattleAlreadyStarted ->
                    Toast.makeText(context, event.message, Toast.LENGTH_SHORT).show()
                is HamTipsDetailEvent.BattleAlreadyJoined ->
                    Toast.makeText(context, event.message, Toast.LENGTH_SHORT).show()
                is HamTipsDetailEvent.BattleCancelled ->
                    Toast.makeText(context, event.message, Toast.LENGTH_SHORT).show()
                is HamTipsDetailEvent.ShowMessage ->
                    Toast.makeText(context, event.message, Toast.LENGTH_SHORT).show()
            }
        }
    }

    val currentUser by sessionViewModel.currentUser.collectAsStateWithLifecycle()
    val isAuthor = post.authorId == currentUser.id
    val canDeletePost = viewModel.canDeletePost(post)
    val battleViewModel: HamBattleViewModel = hiltViewModel()
    val battleInfo = post.battleInfo
    val linkedBattleCode = battleInfo?.link?.let(::extractBattleCode)
    val linkedChallenge = linkedBattleCode?.let { battleCode ->
        battleViewModel.mockChallenges.value.find { it.battleCode == battleCode }
    }

    val durationLabel = stringResource(R.string.hamtips_battle_days_format, battleInfo?.durationDays ?: 0)
    val capacityLabel = stringResource(
        R.string.hamtips_battle_capacity_format,
        linkedChallenge?.totalCount ?: battleInfo?.capacity ?: 0
    )
    val summaryRequest = battleChallengeRequestFrom(post, durationLabel, capacityLabel, linkedChallenge)
    val isBattleFull = linkedChallenge?.isFull ?: battleInfo?.isFull ?: false

    HamTipsBattleDetailContent(
        post = post,
        onBackClick = onBackClick,
        onEditClick = onEditClick,
        modifier = modifier,
        isAuthor = isAuthor,
        canDeletePost = canDeletePost,
        battleInfo = battleInfo,
        summaryRequest = summaryRequest,
        isBattleFull = isBattleFull,
        onLikeClick = { viewModel.toggleLike(post.id) },
        onScrapClick = { viewModel.toggleSave(post.id) },
        onDeletePost = { viewModel.deletePost(post.id) },
        onSubmitComment = { replyTarget, content -> submitHamTipsComment(viewModel, post, replyTarget, content) },
        canDeleteComment = { comment -> viewModel.canDeleteComment(post, comment) },
        onDeleteComment = { comment -> viewModel.deleteComment(post.id, comment.id) },
        canDeleteReply = { reply -> viewModel.canDeleteReply(post, reply) },
        onDeleteReply = { comment, reply -> viewModel.deleteReply(post.id, comment.id, reply.id) },
        showRoomFull = showRoomFull,
        onRoomFullDismiss = { showRoomFull = false },
        onJoinConfirm = {
            if (battleInfo != null) {
                if (BattleConfig.USE_SERVER_BATTLE) {
                    viewModel.joinServerBattle(post.id, battleInfo.link)
                } else {
                    val battleCode = extractBattleCode(battleInfo.link)
                    if (battleCode == null) {
                        Toast.makeText(
                            context,
                            "올바르지 않은 햄배틀 링크입니다.",
                            Toast.LENGTH_SHORT
                        ).show()
                    } else {
                        val joinedChallenge = battleViewModel.joinChallengeFromCommunityPost(
                            authorName = post.authorName,
                            title = post.title,
                            penalty = battleInfo.penalty,
                            battleCode = battleCode,
                            totalCount = battleInfo.capacity
                        )
                        if (joinedChallenge == null) {
                            showRoomFull = true
                        } else {
                            viewModel.joinBattle(post.id)
                            if (joinedChallenge.isFull) {
                                onNavigateToHamBattleTab()
                            } else {
                                onNavigateToBattleLink(joinedChallenge.id)
                            }
                        }
                    }
                }
            }
        }
    )
}

@Composable
private fun HamTipsBattleDetailContent(
    post: TipPost,
    onBackClick: () -> Unit,
    onEditClick: (TipPost) -> Unit,
    modifier: Modifier,
    isAuthor: Boolean,
    canDeletePost: Boolean,
    battleInfo: BattleRecruitInfo?,
    summaryRequest: HamBattleChallengeRequest,
    isBattleFull: Boolean,
    onLikeClick: () -> Unit,
    onScrapClick: () -> Unit,
    onDeletePost: () -> Unit,
    onSubmitComment: (TipComment?, String) -> Unit,
    canDeleteComment: (TipComment) -> Boolean,
    onDeleteComment: (TipComment) -> Unit,
    canDeleteReply: (TipReply) -> Boolean,
    onDeleteReply: (TipComment, TipReply) -> Unit,
    showRoomFull: Boolean,
    onRoomFullDismiss: () -> Unit,
    onJoinConfirm: () -> Unit
) {
    var showPostMenu by remember { mutableStateOf(false) }
    var showJoinConfirm by remember { mutableStateOf(false) }
    var commentMenuTarget by remember { mutableStateOf<TipComment?>(null) }
    var replyMenuTarget by remember { mutableStateOf<Pair<TipComment, TipReply>?>(null) }
    var replyTarget by remember { mutableStateOf<TipComment?>(null) }
    var commentInput by remember { mutableStateOf("") }
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
                .verticalScroll(rememberScrollState())
        ) {
            Column(modifier = Modifier.padding(horizontal = 20.dp)) {
                Spacer(modifier = Modifier.height(4.dp))
                HamTipsPostHeader(post = post)
                Spacer(modifier = Modifier.height(16.dp))
                if (post.content.isNotBlank()) {
                    Text(text = post.content, style = MaterialTheme.typography.bodyMedium, color = HPBlack)
                    Spacer(modifier = Modifier.height(16.dp))
                }
                if (battleInfo != null) {
                    HamTipsBattleInfoCard(
                        summaryRequest = summaryRequest,
                        showActionButton = !isAuthor,
                        isFull = isBattleFull,
                        onActionClick = { showJoinConfirm = true }
                    )
                    Spacer(modifier = Modifier.height(20.dp))
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
                onReplyMoreClick = { comment, reply -> replyMenuTarget = comment to reply }
            )
            Spacer(modifier = Modifier.height(24.dp))
        }
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

    if (showJoinConfirm && battleInfo != null) {
        Dialog(onDismissRequest = { showJoinConfirm = false }, properties = DialogProperties(usePlatformDefaultWidth = false)) {
            HamTipsBattleJoinConfirmContent(
                summaryRequest = summaryRequest,
                onCancel = { showJoinConfirm = false },
                onConfirm = {
                    showJoinConfirm = false
                    onJoinConfirm()
                }
            )
        }
    }

    if (showRoomFull) {
        HamBattleRoomFullDialog(
            challengeTitle = summaryRequest.challengeName,
            onConfirmClick = onRoomFullDismiss
        )
    }
}

@Composable
private fun HamTipsBattleJoinConfirmContent(
    summaryRequest: HamBattleChallengeRequest,
    onCancel: () -> Unit,
    onConfirm: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        ChallengeSummaryCard(request = summaryRequest)
        Spacer(modifier = Modifier.height(29.dp))
        ConfirmActionCard(
            question = stringResource(R.string.hamtips_battle_join_confirm_question),
            confirmLabel = stringResource(R.string.hamtips_battle_join),
            onCancel = onCancel,
            onConfirm = onConfirm
        )
    }
}

@Preview(showBackground = true, name = "16. 햄배틀 모집 상세 - 참가 가능")
@Composable
private fun HamTipsBattleDetailScreenJoinablePreview() {
    HampouchTheme {
        val previewPost = HamTipsMockData.allPosts().first { it.id == "hamtip_13" }
        val info = requireNotNull(previewPost.battleInfo)
        HamTipsBattleDetailContent(
            post = previewPost,
            onBackClick = {},
            onEditClick = {},
            modifier = Modifier,
            isAuthor = false,
            canDeletePost = false,
            battleInfo = info,
            summaryRequest = battleChallengeRequestFrom(
                previewPost,
                stringResource(R.string.hamtips_battle_days_format, info.durationDays),
                stringResource(R.string.hamtips_battle_capacity_format, info.capacity)
            ),
            isBattleFull = info.isFull,
            onLikeClick = {},
            onScrapClick = {},
            onDeletePost = {},
            onSubmitComment = { _, _ -> },
            canDeleteComment = { false },
            onDeleteComment = {},
            canDeleteReply = { false },
            onDeleteReply = { _, _ -> },
            showRoomFull = false,
            onRoomFullDismiss = {},
            onJoinConfirm = {}
        )
    }
}

@Preview(showBackground = true, backgroundColor = 0xFFB0B0B0, name = "18. 햄배틀 참가 확인")
@Composable
private fun HamTipsBattleJoinConfirmPreview() {
    HampouchTheme {
        val previewPost = HamTipsMockData.allPosts().first { it.id == "hamtip_13" }
        val info = previewPost.battleInfo!!
        HamTipsBattleJoinConfirmContent(
            summaryRequest = battleChallengeRequestFrom(
                previewPost,
                stringResource(R.string.hamtips_battle_days_format, info.durationDays),
                stringResource(R.string.hamtips_battle_capacity_format, info.capacity)
            ),
            onCancel = {},
            onConfirm = {}
        )
    }
}

@Preview(showBackground = true, name = "17. 햄배틀 모집 상세 - 모집 완료")
@Composable
private fun HamTipsBattleDetailScreenFullPreview() {
    HampouchTheme {
        val fullPost = HamTipsMockData.allPosts().first { it.id == "hamtip_13" }.let { post ->
            val info = requireNotNull(post.battleInfo)
            post.copy(
                detail = TipPostDetail.Battle(
                    info.copy(participantIds = List(info.capacity) { "user_$it" })
                )
            )
        }
        val fullInfo = requireNotNull(fullPost.battleInfo)
        HamTipsBattleDetailContent(
            post = fullPost,
            onBackClick = {},
            onEditClick = {},
            modifier = Modifier,
            isAuthor = false,
            canDeletePost = false,
            battleInfo = fullInfo,
            summaryRequest = battleChallengeRequestFrom(
                fullPost,
                stringResource(R.string.hamtips_battle_days_format, fullInfo.durationDays),
                stringResource(R.string.hamtips_battle_capacity_format, fullInfo.capacity)
            ),
            isBattleFull = fullInfo.isFull,
            onLikeClick = {},
            onScrapClick = {},
            onDeletePost = {},
            onSubmitComment = { _, _ -> },
            canDeleteComment = { false },
            onDeleteComment = {},
            canDeleteReply = { false },
            onDeleteReply = { _, _ -> },
            showRoomFull = false,
            onRoomFullDismiss = {},
            onJoinConfirm = {}
        )
    }
}
