package com.example.hampouch.ui.hamtips

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.hampouch.domain.model.MenuRatingInfo
import com.example.hampouch.domain.model.TipCategory
import com.example.hampouch.domain.model.TipPost
import com.example.hampouch.domain.model.toUserMessage
import com.example.hampouch.domain.repository.HamTipsRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

sealed interface HamTipsWriteEvent {
    data object Submitted : HamTipsWriteEvent
    data class ShowMessage(val message: String) : HamTipsWriteEvent
}

@HiltViewModel
class HamTipsWriteViewModel @Inject constructor(
    private val hamTipsRepository: HamTipsRepository
) : ViewModel() {

    private val _isSubmitting = MutableStateFlow(false)
    val isSubmitting: StateFlow<Boolean> = _isSubmitting.asStateFlow()

    private val _events = Channel<HamTipsWriteEvent>(Channel.BUFFERED)
    val events = _events.receiveAsFlow()

    fun submitTip(
        editingPost: TipPost?,
        category: TipCategory,
        title: String,
        content: String,
        photoUris: List<String>,
        photoKeys: List<String>
    ) = submit {
        if (editingPost != null) {
            hamTipsRepository.updateTipPost(editingPost.id, category, title, content, photoUris, photoKeys)
        } else {
            hamTipsRepository.createTipPost(category, title, content, photoUris)
        }
    }

    fun submitMenu(
        editingPost: TipPost?,
        title: String,
        menuName: String,
        place: String,
        price: Int,
        rating: MenuRatingInfo,
        comment: String,
        photoUris: List<String>,
        photoKeys: List<String>
    ) = submit {
        if (editingPost != null) {
            hamTipsRepository.updateMenuPost(
                postId = editingPost.id, title = title, menuName = menuName, place = place,
                price = price, rating = rating, comment = comment,
                imageUris = photoUris, imageKeys = photoKeys
            )
        } else {
            hamTipsRepository.createMenuPost(
                title = title, menuName = menuName, place = place, price = price,
                rating = rating, comment = comment, imageUris = photoUris
            )
        }
    }

    fun submitBattle(editingPost: TipPost?, title: String, content: String, link: String) = submit {
        if (editingPost != null) {
            hamTipsRepository.updateBattlePost(editingPost.id, title, content, link)
        } else {
            hamTipsRepository.createBattlePost(title, content, link)
        }
    }

    private fun submit(block: suspend () -> Result<TipPost>) {
        if (_isSubmitting.value) return
        _isSubmitting.value = true
        viewModelScope.launch {
            block()
                .onSuccess { _events.send(HamTipsWriteEvent.Submitted) }
                .onFailure {
                    _events.send(HamTipsWriteEvent.ShowMessage(it.toUserMessage("글 등록에 실패했습니다.")))
                }
            _isSubmitting.value = false
        }
    }
}
