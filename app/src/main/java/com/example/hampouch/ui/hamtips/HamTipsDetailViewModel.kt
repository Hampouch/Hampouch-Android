package com.example.hampouch.ui.hamtips

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.hampouch.domain.model.BattleInvitationPreview
import com.example.hampouch.domain.model.TipComment
import com.example.hampouch.domain.model.TipPost
import com.example.hampouch.domain.model.TipReply
import com.example.hampouch.domain.model.ApiException
import com.example.hampouch.domain.model.toUserMessage
import com.example.hampouch.domain.repository.BattleRepository
import com.example.hampouch.domain.repository.HamTipsRepository
import com.example.hampouch.ui.hambattle.extractBattleCodeFromInviteValue
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

sealed interface HamTipsDetailEvent {
    data object PostDeleted : HamTipsDetailEvent

    data class BattleJoined(val battleId: Long) : HamTipsDetailEvent

    data object BattleFull : HamTipsDetailEvent

    data class BattleAlreadyStarted(val message: String) : HamTipsDetailEvent

    data class BattleAlreadyJoined(val message: String) : HamTipsDetailEvent

    data class BattleCancelled(val message: String) : HamTipsDetailEvent

    data class ShowMessage(val message: String) : HamTipsDetailEvent
}

@HiltViewModel
class HamTipsDetailViewModel @Inject constructor(
    private val hamTipsRepository: HamTipsRepository,
    private val battleRepository: BattleRepository
) : ViewModel() {

    val posts: StateFlow<List<TipPost>> = hamTipsRepository.posts
    private val _isRefreshing = MutableStateFlow(false)
    val isRefreshing: StateFlow<Boolean> = _isRefreshing.asStateFlow()

    private val _battleInvitationPreview = MutableStateFlow<BattleInvitationPreview?>(null)
    val battleInvitationPreview: StateFlow<BattleInvitationPreview?> = _battleInvitationPreview.asStateFlow()
    private var requestedBattleCode: String? = null

    private val _events = Channel<HamTipsDetailEvent>(Channel.BUFFERED)
    val events = _events.receiveAsFlow()

    fun canDeletePost(post: TipPost): Boolean = hamTipsRepository.canDeletePost(post)

    fun canDeleteComment(post: TipPost, comment: TipComment): Boolean =
        hamTipsRepository.canDeleteComment(post, comment)

    fun canDeleteReply(post: TipPost, reply: TipReply): Boolean =
        hamTipsRepository.canDeleteReply(post, reply)

    fun loadDetail(postId: String) {
        if (_isRefreshing.value) return
        _isRefreshing.value = true
        viewModelScope.launch {
            try {
                hamTipsRepository.loadPostDetail(postId)
                    .onFailure { notify(it, "글을 불러오지 못했습니다.") }
            } finally {
                _isRefreshing.value = false
            }
        }
    }

    fun loadBattleInvitationPreview(battleUrl: String) {
        val battleCode = extractBattleCode(battleUrl) ?: return
        if (requestedBattleCode == battleCode) return
        requestedBattleCode = battleCode
        _battleInvitationPreview.value = null
        viewModelScope.launch {
            battleRepository.loadInvitationPreview(battleCode)
                .onSuccess { preview ->
                    if (requestedBattleCode == battleCode) {
                        _battleInvitationPreview.value = preview
                    }
                }
                .onFailure { error ->
                    if (requestedBattleCode == battleCode) {
                        notify(error, "햄배틀 정보를 불러오지 못했습니다.")
                    }
                }
        }
    }

    fun toggleLike(postId: String) = run("좋아요 처리에 실패했습니다.") {
        hamTipsRepository.toggleLike(postId)
    }

    fun toggleSave(postId: String) = run("저장 처리에 실패했습니다.") {
        hamTipsRepository.toggleSave(postId)
    }

    fun addComment(postId: String, content: String) = run("댓글 등록에 실패했습니다.") {
        hamTipsRepository.addComment(postId, content)
    }

    fun addReply(postId: String, commentId: String, content: String) = run("답글 등록에 실패했습니다.") {
        hamTipsRepository.addReply(postId, commentId, content)
    }

    fun deleteComment(postId: String, commentId: String) = run("댓글 삭제에 실패했습니다.") {
        hamTipsRepository.deleteComment(postId, commentId)
    }

    fun deleteReply(postId: String, commentId: String, replyId: String) = run("답글 삭제에 실패했습니다.") {
        hamTipsRepository.deleteReply(postId, commentId, replyId)
    }

    fun deletePost(postId: String) {
        viewModelScope.launch {
            hamTipsRepository.deletePost(postId)
                .onSuccess { _events.send(HamTipsDetailEvent.PostDeleted) }
                .onFailure { notify(it, "글 삭제에 실패했습니다.") }
        }
    }

    fun joinBattle(postId: String) = hamTipsRepository.joinBattle(postId)

    fun joinServerBattle(postId: String, battleUrl: String) {
        viewModelScope.launch {
            val battleCode = extractBattleCode(battleUrl)
            if (battleCode == null) {
                notify(
                    ApiException("COMMUNITY_INVALID_BATTLE_URL", "올바르지 않은 햄배틀 URL입니다."),
                    "올바르지 않은 햄배틀 URL입니다."
                )
                return@launch
            }
            battleRepository.join(battleCode)
                .onSuccess { battleId ->
                    hamTipsRepository.joinBattle(postId)
                    _events.send(HamTipsDetailEvent.BattleJoined(battleId))
                }
                .onFailure { error ->
                    _events.send(error.toBattleJoinEvent())
                }
        }
    }

    private fun run(fallback: String, block: suspend () -> Result<Unit>) {
        viewModelScope.launch {
            block().onFailure { notify(it, fallback) }
        }
    }

    private suspend fun notify(error: Throwable, fallback: String) {
        _events.send(HamTipsDetailEvent.ShowMessage(error.toUserMessage(fallback)))
    }
}

internal fun extractBattleCode(battleUrl: String): String? {
    return extractBattleCodeFromInviteValue(battleUrl)
}

internal fun Throwable.toBattleJoinEvent(): HamTipsDetailEvent {
    val message = toUserMessage("햄배틀 참가에 실패했습니다.")
    return when ((this as? ApiException)?.code) {
        "BATTLE_FULL" -> HamTipsDetailEvent.BattleFull
        "BATTLE_ALREADY_STARTED" -> HamTipsDetailEvent.BattleAlreadyStarted(message)
        "ALREADY_JOINED" -> HamTipsDetailEvent.BattleAlreadyJoined(message)
        "BATTLE_CANCELLED" -> HamTipsDetailEvent.BattleCancelled(message)
        else -> HamTipsDetailEvent.ShowMessage(message)
    }
}
