package com.example.hampouch.ui.hamtips

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.hampouch.domain.model.MenuRatingInfo
import com.example.hampouch.domain.model.MenuRatingType
import com.example.hampouch.domain.model.TipCategory
import com.example.hampouch.domain.model.TipPost
import com.example.hampouch.domain.model.TipShareCategory
import com.example.hampouch.domain.model.toUserMessage
import com.example.hampouch.domain.repository.HamTipsRepository
import com.example.hampouch.ui.hamtips.components.HamTipsMaxPhotoCount
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

sealed interface HamTipsWriteEvent {
    data object Submitted : HamTipsWriteEvent
    data class ShowMessage(val message: String) : HamTipsWriteEvent
}

data class TipFormState(
    val category: TipShareCategory = TipShareCategory.SHOPPING,
    val title: String = "",
    val content: String = "",
    val photoUris: List<String> = emptyList(),
    val photoKeys: List<String> = emptyList()
)

data class MenuFormState(
    val menuName: String = "",
    val place: String = "",
    val price: Int = 0,
    val taste: Int = 0,
    val costEffectiveness: Int = 0,
    val mood: Int = 0,
    val comment: String = "",
    val photoUris: List<String> = emptyList(),
    val photoKeys: List<String> = emptyList()
)

data class BattleFormState(
    val title: String = "",
    val content: String = "",
    val link: String = ""
)

private const val KeyTipCategory = "hamtips_write_tip_category"
private const val KeyTipTitle = "hamtips_write_tip_title"
private const val KeyTipContent = "hamtips_write_tip_content"

private const val KeyMenuName = "hamtips_write_menu_name"
private const val KeyMenuPlace = "hamtips_write_menu_place"
private const val KeyMenuPrice = "hamtips_write_menu_price"
private const val KeyMenuTaste = "hamtips_write_menu_taste"
private const val KeyMenuCost = "hamtips_write_menu_cost"
private const val KeyMenuMood = "hamtips_write_menu_mood"
private const val KeyMenuComment = "hamtips_write_menu_comment"

private const val KeyBattleTitle = "hamtips_write_battle_title"
private const val KeyBattleContent = "hamtips_write_battle_content"
private const val KeyBattleLink = "hamtips_write_battle_link"

@HiltViewModel
class HamTipsWriteViewModel @Inject constructor(
    private val hamTipsRepository: HamTipsRepository,
    private val savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val _isSubmitting = MutableStateFlow(false)
    val isSubmitting: StateFlow<Boolean> = _isSubmitting.asStateFlow()

    private val _events = Channel<HamTipsWriteEvent>(Channel.BUFFERED)
    val events = _events.receiveAsFlow()

    private var tipFormInitialized = false
    private val _tipForm = MutableStateFlow(TipFormState())
    val tipForm: StateFlow<TipFormState> = _tipForm.asStateFlow()

    private var menuFormInitialized = false
    private val _menuForm = MutableStateFlow(MenuFormState())
    val menuForm: StateFlow<MenuFormState> = _menuForm.asStateFlow()

    private var battleFormInitialized = false
    private val _battleForm = MutableStateFlow(BattleFormState())
    val battleForm: StateFlow<BattleFormState> = _battleForm.asStateFlow()

    fun initTipForm(editingPost: TipPost?) {
        if (tipFormInitialized) return
        tipFormInitialized = true
        val restoredCategory = savedStateHandle.get<String>(KeyTipCategory)
            ?.let { name -> TipShareCategory.entries.find { it.name == name } }
        val restoredTitle = savedStateHandle.get<String>(KeyTipTitle)
        val restoredContent = savedStateHandle.get<String>(KeyTipContent)
        _tipForm.value = TipFormState(
            category = restoredCategory
                ?: TipShareCategory.entries.find { it.category == editingPost?.category }
                ?: TipShareCategory.SHOPPING,
            title = restoredTitle ?: editingPost?.title.orEmpty(),
            content = restoredContent ?: editingPost?.content.orEmpty(),
            photoUris = editingPost?.imageUris ?: emptyList(),
            photoKeys = editingPost?.imageKeys ?: emptyList()
        )
    }

    fun updateTipCategory(category: TipShareCategory) {
        _tipForm.update { it.copy(category = category) }
        savedStateHandle[KeyTipCategory] = category.name
    }

    fun updateTipTitle(title: String) {
        _tipForm.update { it.copy(title = title) }
        savedStateHandle[KeyTipTitle] = title
    }

    fun updateTipContent(content: String) {
        _tipForm.update { it.copy(content = content) }
        savedStateHandle[KeyTipContent] = content
    }

    fun addTipPhotos(added: List<String>) {
        _tipForm.update { state ->
            state.copy(
                photoUris = (state.photoUris + added).take(HamTipsMaxPhotoCount),
                photoKeys = (state.photoKeys + added.map { "" }).take(HamTipsMaxPhotoCount)
            )
        }
    }

    fun removeTipPhoto(uri: String) {
        _tipForm.update { state ->
            val index = state.photoUris.indexOf(uri)
            state.copy(
                photoUris = state.photoUris - uri,
                photoKeys = if (index != -1) state.photoKeys.filterIndexed { i, _ -> i != index } else state.photoKeys
            )
        }
    }

    fun initMenuForm(editingPost: TipPost?) {
        if (menuFormInitialized) return
        menuFormInitialized = true
        _menuForm.value = MenuFormState(
            menuName = savedStateHandle.get<String>(KeyMenuName) ?: editingPost?.menuName.orEmpty(),
            place = savedStateHandle.get<String>(KeyMenuPlace) ?: editingPost?.place.orEmpty(),
            price = savedStateHandle.get<Int>(KeyMenuPrice) ?: (editingPost?.price ?: 0),
            taste = savedStateHandle.get<Int>(KeyMenuTaste) ?: (editingPost?.menuRating?.taste ?: 0),
            costEffectiveness = savedStateHandle.get<Int>(KeyMenuCost)
                ?: (editingPost?.menuRating?.costEffectiveness ?: 0),
            mood = savedStateHandle.get<Int>(KeyMenuMood) ?: (editingPost?.menuRating?.mood ?: 0),
            comment = savedStateHandle.get<String>(KeyMenuComment) ?: editingPost?.content.orEmpty(),
            photoUris = editingPost?.imageUris ?: emptyList(),
            photoKeys = editingPost?.imageKeys ?: emptyList()
        )
    }

    fun updateMenuName(menuName: String) {
        _menuForm.update { it.copy(menuName = menuName) }
        savedStateHandle[KeyMenuName] = menuName
    }

    fun updateMenuPlace(place: String) {
        _menuForm.update { it.copy(place = place) }
        savedStateHandle[KeyMenuPlace] = place
    }

    fun updateMenuPrice(price: Int) {
        _menuForm.update { it.copy(price = price) }
        savedStateHandle[KeyMenuPrice] = price
    }

    fun updateMenuRating(type: MenuRatingType, rating: Int) {
        _menuForm.update { state ->
            when (type) {
                MenuRatingType.TASTE -> state.copy(taste = rating)
                MenuRatingType.COST_EFFECTIVENESS -> state.copy(costEffectiveness = rating)
                MenuRatingType.MOOD -> state.copy(mood = rating)
            }
        }
        when (type) {
            MenuRatingType.TASTE -> savedStateHandle[KeyMenuTaste] = rating
            MenuRatingType.COST_EFFECTIVENESS -> savedStateHandle[KeyMenuCost] = rating
            MenuRatingType.MOOD -> savedStateHandle[KeyMenuMood] = rating
        }
    }

    fun updateMenuComment(comment: String) {
        _menuForm.update { it.copy(comment = comment) }
        savedStateHandle[KeyMenuComment] = comment
    }

    fun addMenuPhotos(added: List<String>) {
        _menuForm.update { state ->
            state.copy(
                photoUris = (state.photoUris + added).take(HamTipsMaxPhotoCount),
                photoKeys = (state.photoKeys + added.map { "" }).take(HamTipsMaxPhotoCount)
            )
        }
    }

    fun removeMenuPhoto(uri: String) {
        _menuForm.update { state ->
            val index = state.photoUris.indexOf(uri)
            state.copy(
                photoUris = state.photoUris - uri,
                photoKeys = if (index != -1) state.photoKeys.filterIndexed { i, _ -> i != index } else state.photoKeys
            )
        }
    }

    fun initBattleForm(editingPost: TipPost?, initialLink: String) {
        if (battleFormInitialized) return
        battleFormInitialized = true
        _battleForm.value = BattleFormState(
            title = savedStateHandle.get<String>(KeyBattleTitle) ?: editingPost?.title.orEmpty(),
            content = savedStateHandle.get<String>(KeyBattleContent) ?: editingPost?.content.orEmpty(),
            link = savedStateHandle.get<String>(KeyBattleLink) ?: (editingPost?.battleInfo?.link ?: initialLink)
        )
    }

    fun updateBattleTitle(title: String) {
        _battleForm.update { it.copy(title = title) }
        savedStateHandle[KeyBattleTitle] = title
    }

    fun updateBattleContent(content: String) {
        _battleForm.update { it.copy(content = content) }
        savedStateHandle[KeyBattleContent] = content
    }

    fun updateBattleLink(link: String) {
        _battleForm.update { it.copy(link = link) }
        savedStateHandle[KeyBattleLink] = link
    }

    fun submitTip(
        editingPost: TipPost?,
        category: TipCategory,
        title: String,
        content: String,
        photoUris: List<String>,
        photoKeys: List<String>
    ) = submit(onSuccess = ::resetTipForm) {
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
    ) = submit(onSuccess = ::resetMenuForm) {
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

    fun submitBattle(editingPost: TipPost?, title: String, content: String, link: String) =
        submit(onSuccess = ::resetBattleForm) {
            if (editingPost != null) {
                hamTipsRepository.updateBattlePost(editingPost.id, title, content, link)
            } else {
                hamTipsRepository.createBattlePost(title, content, link)
            }
        }

    private fun resetTipForm() {
        tipFormInitialized = false
        _tipForm.value = TipFormState()
        savedStateHandle[KeyTipCategory] = null
        savedStateHandle[KeyTipTitle] = null
        savedStateHandle[KeyTipContent] = null
    }

    private fun resetMenuForm() {
        menuFormInitialized = false
        _menuForm.value = MenuFormState()
        savedStateHandle[KeyMenuName] = null
        savedStateHandle[KeyMenuPlace] = null
        savedStateHandle[KeyMenuPrice] = null
        savedStateHandle[KeyMenuTaste] = null
        savedStateHandle[KeyMenuCost] = null
        savedStateHandle[KeyMenuMood] = null
        savedStateHandle[KeyMenuComment] = null
    }

    private fun resetBattleForm() {
        battleFormInitialized = false
        _battleForm.value = BattleFormState()
        savedStateHandle[KeyBattleTitle] = null
        savedStateHandle[KeyBattleContent] = null
        savedStateHandle[KeyBattleLink] = null
    }

    private fun submit(onSuccess: () -> Unit, block: suspend () -> Result<TipPost>) {
        if (_isSubmitting.value) return
        _isSubmitting.value = true
        viewModelScope.launch {
            block()
                .onSuccess {
                    onSuccess()
                    _events.send(HamTipsWriteEvent.Submitted)
                }
                .onFailure {
                    _events.send(HamTipsWriteEvent.ShowMessage(it.toUserMessage("글 등록에 실패했습니다.")))
                }
            _isSubmitting.value = false
        }
    }
}
