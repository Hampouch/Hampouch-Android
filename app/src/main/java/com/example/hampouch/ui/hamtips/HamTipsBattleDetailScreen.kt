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
    var showPostMenu by remember { mutableStateOf(false) }
    var showJoinConfirm by remember { mutableStateOf(false) }
    var showRoomFull by remember { mutableStateOf(false) }
    var commentMenuTarget by remember { mutableStateOf<TipComment?>(null) }
    var replyMenuTarget by remember { mutableStateOf<Pair<TipComment, TipReply>?>(null) }
    var replyTarget by remember { mutableStateOf<TipComment?>(null) }
    var commentInput by remember { mutableStateOf("") }
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()

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
    val titleRes = if (post.isEditorAuthor) R.string.hamtips_pochipick_title else R.string.hamtips_title
    val battleViewModel: HamBattleViewModel = hiltViewModel()
    val battleInfo = post.battleInfo
    val linkedChallenge = battleInfo?.link?.let { link ->
        battleViewModel.mockChallenges.value.find { it.battleCode == link }
    }

    val durationLabel = stringResource(R.string.hamtips_battle_days_format, battleInfo?.durationDays ?: 0)
    val capacityLabel = stringResource(
        R.string.hamtips_battle_capacity_format,
        linkedChallenge?.totalCount ?: battleInfo?.capacity ?: 0
    )
    val summaryRequest = battleChallengeRequestFrom(post, durationLabel, capacityLabel, linkedChallenge)

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
                    submitHamTipsComment(viewModel, post, submittedReplyTarget, submittedInput)
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
                        isFull = linkedChallenge?.isFull ?: battleInfo.isFull,
                        onActionClick = { showJoinConfirm = true }
                    )
                    Spacer(modifier = Modifier.height(20.dp))
                }
                HamTipsEngagementRow(
                    post = post,
                    onLikeClick = { viewModel.toggleLike(post.id) },
                    onScrapClick = { viewModel.toggleSave(post.id) }
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
        post = post,
        target = commentMenuTarget,
        onDismiss = { commentMenuTarget = null }
    )

    HamTipsReplyMoreMenuHost(
        post = post,
        target = replyMenuTarget,
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
                                viewModel.deletePost(post.id)
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
                    if (BattleConfig.USE_SERVER_BATTLE) {
                        viewModel.joinServerBattle(post.id, battleInfo.link)
                    } else {
                        val joinedChallenge = battleViewModel.joinChallengeFromCommunityPost(
                            authorName = post.authorName,
                            title = post.title,
                            penalty = battleInfo.penalty,
                            battleCode = battleInfo.link,
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
            )
        }
    }

    if (showRoomFull) {
        HamBattleRoomFullDialog(
            challengeTitle = summaryRequest.challengeName,
            onConfirmClick = { showRoomFull = false }
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
        HamTipsBattleDetailScreen(
            post = HamTipsMockData.allPosts().first { it.id == "hamtip_13" },
            onBackClick = {},
            onDeleted = {},
            onNavigateToHamBattleTab = {},
            onEditClick = {},
            onNavigateToBattleLink = {}
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
        HamTipsBattleDetailScreen(
            post = fullPost,
            onBackClick = {},
            onDeleted = {},
            onNavigateToHamBattleTab = {},
            onEditClick = {},
            onNavigateToBattleLink = {}
        )
    }
}
