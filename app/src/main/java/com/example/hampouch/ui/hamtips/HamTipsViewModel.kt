package com.example.hampouch.ui.hamtips

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.hampouch.domain.model.HamTipsSortOrder
import com.example.hampouch.domain.model.TipCategory
import com.example.hampouch.domain.model.TipPost
import com.example.hampouch.domain.model.toUserMessage
import com.example.hampouch.domain.repository.HamTipsRepository
import com.example.hampouch.ui.common.LoadState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

/** 커뮤니티 목록 화면(홈·카테고리·인기·포치픽). */
@HiltViewModel
class HamTipsViewModel @Inject constructor(
    private val hamTipsRepository: HamTipsRepository
) : ViewModel() {

    val posts: StateFlow<List<TipPost>> = hamTipsRepository.posts

    private val _loadState = MutableStateFlow<LoadState>(LoadState.Idle)
    val loadState: StateFlow<LoadState> = _loadState.asStateFlow()
    private var retryAction: (() -> Unit)? = null

    fun retry() = retryAction?.invoke()

    private val _messages = Channel<String>(Channel.BUFFERED)
    val messages = _messages.receiveAsFlow()

    fun loadHome(sortOrder: HamTipsSortOrder) = load("커뮤니티 글을 불러오지 못했습니다.") {
        hamTipsRepository.loadHome(sortOrder)
    }

    fun loadCategoryPosts(category: TipCategory, sortOrder: HamTipsSortOrder) =
        load("커뮤니티 글을 불러오지 못했습니다.") {
            hamTipsRepository.loadCategoryPosts(category, sortOrder)
        }

    fun loadPopularPosts(sortOrder: HamTipsSortOrder) = load("인기 글을 불러오지 못했습니다.") {
        hamTipsRepository.loadPopularPosts(sortOrder)
    }

    fun loadPochipickPosts(sortOrder: HamTipsSortOrder) = load("포치픽을 불러오지 못했습니다.") {
        hamTipsRepository.loadPochipickPosts(sortOrder)
    }

    /** 마이페이지의 "내가 쓴 글". */
    fun loadMyPosts(sortOrder: HamTipsSortOrder = HamTipsSortOrder.LATEST) =
        load("내가 쓴 글을 불러오지 못했습니다.") { hamTipsRepository.loadMyPosts(sortOrder) }

    /** 마이페이지의 "저장한 글". */
    fun loadSavedPosts(sortOrder: HamTipsSortOrder = HamTipsSortOrder.LATEST) =
        load("저장한 글을 불러오지 못했습니다.") { hamTipsRepository.loadSavedPosts(sortOrder) }

    private fun load(fallback: String, block: suspend () -> Result<Unit>) {
        retryAction = { load(fallback, block) }
        _loadState.value = LoadState.Loading
        viewModelScope.launch {
            block()
                .onSuccess { _loadState.value = LoadState.Content(posts.value.isEmpty()) }
                .onFailure {
                    val message = it.toUserMessage(fallback)
                    _loadState.value = LoadState.Failure(message)
                    _messages.send(message)
                }
        }
    }
}
