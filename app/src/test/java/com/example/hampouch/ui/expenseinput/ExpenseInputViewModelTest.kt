package com.example.hampouch.ui.expenseinput

import androidx.lifecycle.SavedStateHandle
import com.example.hampouch.domain.model.ActiveChallenge
import com.example.hampouch.domain.model.ChallengeState
import com.example.hampouch.domain.model.ExpenseAnalysisSummary
import com.example.hampouch.domain.model.ExpensePeriodSummary
import com.example.hampouch.domain.model.ExpenseRecord
import com.example.hampouch.domain.model.ExpenseTagAnalysisResult
import com.example.hampouch.domain.model.ExpenseTrendResult
import com.example.hampouch.domain.model.OnboardingRequest
import com.example.hampouch.domain.repository.ChallengeRepository
import com.example.hampouch.domain.repository.ExpenseRepository
import java.time.LocalDate
import java.time.YearMonth
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class ExpenseInputViewModelTest {

    @Test
    fun `SavedStateHandle은 작성 중인 작은 폼 입력을 복원한다`() {
        val savedStateHandle = SavedStateHandle(mapOf("initialDateEpochDay" to 20_000L))
        val firstViewModel = createViewModel(savedStateHandle)

        firstViewModel.appendAmountDigit("1")
        firstViewModel.appendAmountDigit("2")
        firstViewModel.changeStep(2)
        firstViewModel.changeDate(LocalDate.of(2026, 8, 13))
        firstViewModel.changeExpenseName("점심")
        firstViewModel.selectCategory("dining_out")
        firstViewModel.changeCustomCategory("직접 입력 카테고리")
        firstViewModel.selectReason("stress")
        firstViewModel.changeCustomReason("직접 입력 이유")
        firstViewModel.changeMemo("동료와 식사")

        val restoredForm = createViewModel(savedStateHandle).uiState.value.form

        assertEquals(2, restoredForm.step)
        assertEquals(LocalDate.of(2026, 8, 13), restoredForm.date)
        assertEquals(12, restoredForm.amount)
        assertEquals("점심", restoredForm.expenseName)
        assertEquals("dining_out", restoredForm.categoryId)
        assertEquals("직접 입력 카테고리", restoredForm.customCategoryText)
        assertEquals("stress", restoredForm.reasonId)
        assertEquals("직접 입력 이유", restoredForm.customReasonText)
        assertEquals("동료와 식사", restoredForm.memo)
    }

    @Test
    fun `사진은 구성 변경 동안 ViewModel 상태로 유지하지만 SavedStateHandle에는 저장하지 않는다`() {
        val savedStateHandle = SavedStateHandle()
        val firstViewModel = createViewModel(savedStateHandle)

        firstViewModel.addPhotos(listOf("content://photo/one"))

        assertEquals(listOf("content://photo/one"), firstViewModel.uiState.value.form.photoUris)
        assertTrue(savedStateHandle.keys().none { it.contains("photo", ignoreCase = true) })
        assertTrue(createViewModel(savedStateHandle).uiState.value.form.photoUris.isEmpty())
    }

    @Test
    fun `초안을 취소하면 SavedStateHandle의 복원값을 정리한다`() {
        val savedStateHandle = SavedStateHandle()
        val viewModel = createViewModel(savedStateHandle)
        viewModel.appendAmountDigit("5")
        viewModel.changeMemo("취소할 메모")

        viewModel.discardDraft()

        val restoredForm = createViewModel(savedStateHandle).uiState.value.form
        assertEquals(0, restoredForm.amount)
        assertTrue(restoredForm.memo.isEmpty())
        assertFalse(savedStateHandle.keys().any { it.startsWith("expenseInput.") })
    }

    private fun createViewModel(savedStateHandle: SavedStateHandle) = ExpenseInputViewModel(
        expenseRepository = FakeExpenseRepository(),
        challengeRepository = FakeChallengeRepository(),
        savedStateHandle = savedStateHandle
    )

    private class FakeExpenseRepository : ExpenseRepository {
        override val records: StateFlow<Map<String, ExpenseRecord>> = MutableStateFlow(emptyMap())
        override val daysWithRecord: StateFlow<Set<LocalDate>> = MutableStateFlow(emptySet())
        override fun recordsForDate(date: LocalDate): List<ExpenseRecord> = emptyList()
        override fun byId(id: String): ExpenseRecord? = null
        override suspend fun createExpense(record: ExpenseRecord): Result<ExpenseRecord> = Result.success(record)
        override suspend fun updateExpense(record: ExpenseRecord): Result<ExpenseRecord> = Result.success(record)
        override suspend fun deleteExpense(id: String): Result<Unit> = Result.success(Unit)
        override suspend fun loadExpenseDetail(id: String): Result<ExpenseRecord> = error("unused")
        override suspend fun loadDay(date: LocalDate): Result<Unit> = Result.success(Unit)
        override suspend fun loadWeekSummary(standardDate: LocalDate): Result<ExpensePeriodSummary> = error("unused")
        override suspend fun loadMonthSummary(standardMonth: YearMonth): Result<ExpensePeriodSummary> = error("unused")
        override suspend fun loadAnalysis(periodStart: LocalDate, periodEnd: LocalDate): Result<ExpenseAnalysisSummary> = error("unused")
        override suspend fun loadCategoryAnalysis(categoryId: String, periodStart: LocalDate, periodEnd: LocalDate): Result<ExpenseTagAnalysisResult> = error("unused")
        override suspend fun loadEmotionAnalysis(reasonId: String, periodStart: LocalDate, periodEnd: LocalDate): Result<ExpenseTagAnalysisResult> = error("unused")
        override suspend fun loadTrend(month: YearMonth): Result<ExpenseTrendResult> = error("unused")
        override suspend fun markNoSpend(date: LocalDate): Result<Unit> = Result.success(Unit)
        override fun resetForAccount() = Unit
    }

    private class FakeChallengeRepository : ChallengeRepository {
        override val state: StateFlow<ChallengeState> = MutableStateFlow(ChallengeState())
        override suspend fun loadCurrentChallenge(): Result<Unit> = Result.success(Unit)
        override suspend fun loadHistory(): Result<Unit> = Result.success(Unit)
        override suspend fun loadResult(challengeId: String): Result<Unit> = Result.success(Unit)
        override suspend fun updateFocusCategories(categories: List<String>): Result<List<String>> = Result.success(categories)
        override suspend fun startNewChallenge(request: OnboardingRequest, referenceToday: LocalDate): Result<ActiveChallenge> = error("unused")
        override suspend fun abandonChallenge(referenceToday: LocalDate): Result<Unit> = Result.success(Unit)
        override suspend fun acknowledgeChallengeEnd(): Result<Unit> = Result.success(Unit)
        override fun updateTargetAmount(newTargetAmount: Int, effectiveFrom: LocalDate) = Unit
        override fun markNoRecord(date: LocalDate) = Unit
        override fun clearNoRecord(date: LocalDate) = Unit
        override fun markVisitedExpenseEditAfterEnd() = Unit
        override fun resetForAccount(referenceToday: LocalDate) = Unit
        override fun resetEmpty() = Unit
    }
}
