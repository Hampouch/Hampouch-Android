package com.example.hampouch.ui.expenseinput

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.hampouch.domain.model.ChallengeState
import com.example.hampouch.domain.model.ExpenseRecord
import com.example.hampouch.domain.model.toUserMessage
import com.example.hampouch.domain.repository.ChallengeRepository
import com.example.hampouch.domain.repository.ExpenseRepository
import com.example.hampouch.ui.widget.HomeWidgetRefreshRequester
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch
import java.time.LocalDate
import javax.inject.Inject

internal const val EXPENSE_DATE_LOCKED_MESSAGE = "이 날짜의 지출기록은 지금 변경할 수 없습니다."

internal fun ChallengeState.canChangeExpenseOn(
    date: LocalDate,
    referenceToday: LocalDate = LocalDate.now()
): Boolean {
    if (date.isAfter(referenceToday)) return false
    val recordBasedChallenges = challenges.filter { challenge ->
        challenge.abandonedDate == null &&
            !date.isBefore(challenge.periodStart) &&
            !date.isAfter(challenge.periodEnd)
    }
    return when {
        recordBasedChallenges.any { it.expenseLockedAt != null } -> false
        recordBasedChallenges.isNotEmpty() -> date == referenceToday
        else -> true
    }
}

data class ExpenseInputUiState(
    val dailyLimit: Int = 0,
    val todayBalance: Int = 0,
    val form: ExpenseInputFormState = ExpenseInputFormState(),
    val showMaxAmountError: Boolean = false,
    val isSubmitting: Boolean = false
)

data class ExpenseInputFormState(
    val step: Int = 1,
    val date: LocalDate = LocalDate.now(),
    val amount: Int = 0,
    val expenseName: String = "",
    val categoryId: String? = null,
    val isCustomCategory: Boolean = false,
    val customCategoryText: String = "",
    val reasonId: String? = null,
    val isCustomReason: Boolean = false,
    val customReasonText: String = "",
    val memo: String = "",
    val photoUris: List<String> = emptyList()
)

sealed interface ExpenseInputEvent {
    data object Saved : ExpenseInputEvent

    data object NoSpendSaved : ExpenseInputEvent
    data class ShowMessage(val message: String) : ExpenseInputEvent
}

@HiltViewModel
class ExpenseInputViewModel @Inject constructor(
    private val expenseRepository: ExpenseRepository,
    private val challengeRepository: ChallengeRepository,
    private val homeWidgetRefreshRequester: HomeWidgetRefreshRequester,
    private val savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val initialDate: LocalDate = LocalDate.ofEpochDay(
        savedStateHandle.get<Long>("initialDateEpochDay") ?: LocalDate.now().toEpochDay()
    ).coerceAtMost(LocalDate.now())

    private val _uiState = MutableStateFlow(buildInitialState())
    val uiState: StateFlow<ExpenseInputUiState> = _uiState.asStateFlow()

    private val _events = Channel<ExpenseInputEvent>(Channel.BUFFERED)
    val events = _events.receiveAsFlow()

    private fun buildInitialState(): ExpenseInputUiState {
        val dailyLimit = challengeRepository.state.value.activeChallenge?.dailyLimitOn(initialDate) ?: 0
        val alreadySpent = expenseRepository.recordsForDate(initialDate).sumOf { it.amount }
        return ExpenseInputUiState(
            dailyLimit = dailyLimit,
            todayBalance = dailyLimit - alreadySpent,
            form = ExpenseInputFormState(
                step = savedStateHandle[StepKey] ?: 1,
                date = LocalDate.ofEpochDay(savedStateHandle[DateKey] ?: initialDate.toEpochDay())
                    .coerceAtMost(LocalDate.now()),
                amount = savedStateHandle[AmountKey] ?: 0,
                expenseName = savedStateHandle[ExpenseNameKey] ?: "",
                categoryId = savedStateHandle[CategoryIdKey],
                isCustomCategory = savedStateHandle[IsCustomCategoryKey] ?: false,
                customCategoryText = savedStateHandle[CustomCategoryTextKey] ?: "",
                reasonId = savedStateHandle[ReasonIdKey],
                isCustomReason = savedStateHandle[IsCustomReasonKey] ?: false,
                customReasonText = savedStateHandle[CustomReasonTextKey] ?: "",
                memo = savedStateHandle[MemoKey] ?: ""
            )
        )
    }

    fun appendAmountDigit(digit: String) {
        val currentText = _uiState.value.form.amount.takeIf { it != 0 }?.toString().orEmpty()
        val nextAmount = (currentText + digit).trimStart('0').ifEmpty { "0" }.toLongOrNull() ?: return
        if (!isExpenseAmountValid(nextAmount)) {
            _uiState.value = _uiState.value.copy(showMaxAmountError = true)
        } else {
            updateForm { it.copy(amount = nextAmount.toInt()) }
        }
    }

    fun deleteAmountDigit() = updateForm {
        it.copy(amount = it.amount / 10)
    }

    fun changeDate(date: LocalDate) = updateForm { it.copy(date = date.coerceAtMost(LocalDate.now())) }

    fun isDateSelectable(date: LocalDate): Boolean = challengeRepository.state.value.canChangeExpenseOn(date)

    fun changeStep(step: Int) {
        val targetStep = step.coerceAtLeast(1)
        if (_uiState.value.form.step == 1 && targetStep > 1) {
            proceedToDetails()
            return
        }
        setStep(targetStep)
    }

    private fun proceedToDetails() {
        val form = _uiState.value.form
        if (form.amount <= 0) return
        if (!challengeRepository.state.value.canChangeExpenseOn(form.date)) {
            viewModelScope.launch {
                _events.send(ExpenseInputEvent.ShowMessage(EXPENSE_DATE_LOCKED_MESSAGE))
            }
            return
        }
        setStep(2)
    }

    private fun setStep(step: Int) = updateForm { it.copy(step = step) }

    fun changeExpenseName(name: String) = updateForm { it.copy(expenseName = name) }

    fun selectCategory(categoryId: String) = updateForm {
        it.copy(categoryId = categoryId, isCustomCategory = false)
    }

    fun confirmCustomCategory() = updateForm { form ->
        if (form.customCategoryText.isBlank()) form else form.copy(categoryId = null, isCustomCategory = true)
    }

    fun changeCustomCategory(text: String) = updateForm { it.copy(customCategoryText = text) }

    fun selectReason(reasonId: String) = updateForm {
        it.copy(reasonId = reasonId, isCustomReason = false)
    }

    fun confirmCustomReason() = updateForm { form ->
        if (form.customReasonText.isBlank()) form else form.copy(reasonId = null, isCustomReason = true)
    }

    fun changeCustomReason(text: String) = updateForm { it.copy(customReasonText = text) }

    fun changeMemo(memo: String) = updateForm { it.copy(memo = memo) }

    fun addPhotos(uris: List<String>) = updateForm {
        it.copy(photoUris = (it.photoUris + uris).take(MaxPhotoCount))
    }

    fun removePhotos(indices: Set<Int>) = updateForm {
        it.copy(photoUris = it.photoUris.filterIndexed { index, _ -> index !in indices })
    }

    fun replacePhoto(index: Int, uri: String) = updateForm {
        if (index !in it.photoUris.indices) it else it.copy(
            photoUris = it.photoUris.toMutableList().also { photos -> photos[index] = uri }
        )
    }

    fun discardDraft() {
        DraftKeys.forEach { key -> savedStateHandle.remove<Any?>(key) }
        _uiState.value = _uiState.value.copy(
            form = ExpenseInputFormState(date = initialDate),
            showMaxAmountError = false
        )
    }

    private fun updateForm(transform: (ExpenseInputFormState) -> ExpenseInputFormState) {
        val form = transform(_uiState.value.form)
        saveDraft(form)
        _uiState.value = _uiState.value.copy(form = form, showMaxAmountError = false)
    }

    private fun saveDraft(form: ExpenseInputFormState) {
        savedStateHandle[StepKey] = form.step
        savedStateHandle[DateKey] = form.date.toEpochDay()
        savedStateHandle[AmountKey] = form.amount
        savedStateHandle[ExpenseNameKey] = form.expenseName
        savedStateHandle[CategoryIdKey] = form.categoryId
        savedStateHandle[IsCustomCategoryKey] = form.isCustomCategory
        savedStateHandle[CustomCategoryTextKey] = form.customCategoryText
        savedStateHandle[ReasonIdKey] = form.reasonId
        savedStateHandle[IsCustomReasonKey] = form.isCustomReason
        savedStateHandle[CustomReasonTextKey] = form.customReasonText
        savedStateHandle[MemoKey] = form.memo
    }

    fun markNoSpend(date: LocalDate) {
        if (_uiState.value.isSubmitting) return
        _uiState.value = _uiState.value.copy(isSubmitting = true)
        viewModelScope.launch {
            expenseRepository.markNoSpend(date)
                .onSuccess {
                    discardDraft()
                    homeWidgetRefreshRequester.refreshAfterConfirmedStateChange()
                    _events.send(ExpenseInputEvent.NoSpendSaved)
                }
                .onFailure {
                    _events.send(
                        ExpenseInputEvent.ShowMessage(it.toUserMessage("오늘은 안 썼어요 기록에 실패했습니다."))
                    )
                }
            _uiState.value = _uiState.value.copy(isSubmitting = false)
        }
    }

    fun save() {
        if (_uiState.value.isSubmitting) return
        _uiState.value = _uiState.value.copy(isSubmitting = true)
        viewModelScope.launch {
            expenseRepository.createExpense(_uiState.value.form.toExpenseRecord())
                .onSuccess {
                    discardDraft()
                    homeWidgetRefreshRequester.refreshAfterConfirmedStateChange()
                    _events.send(ExpenseInputEvent.Saved)
                }
                .onFailure {
                    _events.send(ExpenseInputEvent.ShowMessage(it.toUserMessage("지출 입력에 실패했습니다.")))
                }
            _uiState.value = _uiState.value.copy(isSubmitting = false)
        }
    }

    companion object {
        private const val MaxPhotoCount = 5
        private const val StepKey = "expenseInput.step"
        private const val DateKey = "expenseInput.date"
        private const val AmountKey = "expenseInput.amount"
        private const val ExpenseNameKey = "expenseInput.expenseName"
        private const val CategoryIdKey = "expenseInput.categoryId"
        private const val IsCustomCategoryKey = "expenseInput.isCustomCategory"
        private const val CustomCategoryTextKey = "expenseInput.customCategoryText"
        private const val ReasonIdKey = "expenseInput.reasonId"
        private const val IsCustomReasonKey = "expenseInput.isCustomReason"
        private const val CustomReasonTextKey = "expenseInput.customReasonText"
        private const val MemoKey = "expenseInput.memo"
        private val DraftKeys = listOf(
            StepKey, DateKey, AmountKey, ExpenseNameKey, CategoryIdKey, IsCustomCategoryKey,
            CustomCategoryTextKey, ReasonIdKey, IsCustomReasonKey, CustomReasonTextKey, MemoKey
        )
    }
}

private fun ExpenseInputFormState.toExpenseRecord() = ExpenseRecord(
    id = java.util.UUID.randomUUID().toString(),
    date = date,
    amount = amount,
    categoryId = if (isCustomCategory) null else categoryId,
    customCategoryName = if (isCustomCategory) customCategoryText.ifBlank { null } else null,
    expenseName = expenseName.ifBlank { null },
    reasonId = if (isCustomReason) null else reasonId,
    customReason = if (isCustomReason) customReasonText.ifBlank { null } else null,
    memo = memo.ifBlank { null },
    photoUris = photoUris
)
