package com.example.hampouch.ui.expenseinput

import androidx.lifecycle.SavedStateHandle
import com.example.hampouch.MainDispatcherRule
import com.example.hampouch.domain.model.ActiveChallenge
import com.example.hampouch.domain.model.ChallengeState
import com.example.hampouch.domain.model.ExpenseAnalysisSummary
import com.example.hampouch.domain.model.ExpensePeriodSummary
import com.example.hampouch.domain.model.ExpenseRecord
import com.example.hampouch.domain.model.ExpenseTagAnalysisResult
import com.example.hampouch.domain.model.ExpenseTrendResult
import com.example.hampouch.domain.model.FixedDateChallengeDraft
import com.example.hampouch.domain.model.OnboardingRequest
import com.example.hampouch.domain.repository.ChallengeRepository
import com.example.hampouch.domain.repository.ExpenseRepository
import com.example.hampouch.ui.widget.HomeWidgetRefreshRequester
import java.time.LocalDate
import java.time.YearMonth
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.async
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class ExpenseInputViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

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

    @Test
    fun `챌린지 기간이 아닌 이틀 전 날짜는 첫 단계에서 메시지를 표시한다`() = runTest {
        val viewModel = createViewModel(
            savedStateHandle = SavedStateHandle(),
            challengeRepository = FakeChallengeRepository(ChallengeState())
        )
        viewModel.appendAmountDigit("1")
        viewModel.changeDate(LocalDate.now().minusDays(2))
        val event = async { viewModel.events.first() }

        viewModel.changeStep(2)
        advanceUntilIdle()

        assertEquals(1, viewModel.uiState.value.form.step)
        assertEquals(
            ExpenseInputEvent.ShowMessage(EXPENSE_DATE_LOCKED_MESSAGE),
            event.await()
        )
    }

    @Test
    fun `챌린지가 없어도 오늘 지출은 상세 단계로 이동한다`() {
        val viewModel = createViewModel(
            savedStateHandle = SavedStateHandle(),
            challengeRepository = FakeChallengeRepository(ChallengeState())
        )
        viewModel.appendAmountDigit("1")

        viewModel.changeStep(2)

        assertEquals(2, viewModel.uiState.value.form.step)
    }

    @Test
    fun `잠긴 챌린지 기간의 날짜는 첫 단계에서 메시지를 표시한다`() = runTest {
        val today = LocalDate.now()
        val lockedChallenge = editableChallengeState().challenges.single().copy(
            periodStart = today.minusDays(1),
            periodEnd = today.plusDays(1),
            expenseLockedAt = "2026-08-19T00:00:00"
        )
        val viewModel = createViewModel(
            savedStateHandle = SavedStateHandle(),
            challengeRepository = FakeChallengeRepository(
                ChallengeState(challenges = listOf(lockedChallenge))
            )
        )
        viewModel.appendAmountDigit("1")
        val event = async { viewModel.events.first() }

        viewModel.changeStep(2)
        advanceUntilIdle()

        assertEquals(1, viewModel.uiState.value.form.step)
        assertEquals(
            ExpenseInputEvent.ShowMessage(EXPENSE_DATE_LOCKED_MESSAGE),
            event.await()
        )
    }

    @Test
    fun `선택 날짜의 지출을 변경할 수 있으면 상세 단계로 이동한다`() {
        val viewModel = createViewModel(SavedStateHandle())
        viewModel.appendAmountDigit("1")

        viewModel.changeStep(2)

        assertEquals(2, viewModel.uiState.value.form.step)
    }

    @Test
    fun `지출 저장 성공 시 위젯 동기화를 요청한다`() = runTest {
        val refreshRequester = CountingWidgetRefreshRequester()
        val viewModel = createViewModel(
            savedStateHandle = SavedStateHandle(),
            homeWidgetRefreshRequester = refreshRequester
        )
        viewModel.appendAmountDigit("1")

        viewModel.save()
        advanceUntilIdle()

        assertEquals(1, refreshRequester.requestCount)
    }

    private fun createViewModel(
        savedStateHandle: SavedStateHandle,
        challengeRepository: ChallengeRepository = FakeChallengeRepository(editableChallengeState()),
        homeWidgetRefreshRequester: HomeWidgetRefreshRequester = HomeWidgetRefreshRequester {}
    ) = ExpenseInputViewModel(
        expenseRepository = FakeExpenseRepository(),
        challengeRepository = challengeRepository,
        homeWidgetRefreshRequester = homeWidgetRefreshRequester,
        savedStateHandle = savedStateHandle
    )

    private class CountingWidgetRefreshRequester : HomeWidgetRefreshRequester {
        var requestCount = 0

        override fun refreshAfterExpenseChange() {
            requestCount++
        }
    }

    private fun editableChallengeState(): ChallengeState = ChallengeState(
        challenges = listOf(
            ActiveChallenge(
                id = "editable",
                totalDays = 100_000,
                periodStart = LocalDate.of(1970, 1, 1),
                periodEnd = LocalDate.of(2100, 1, 1),
                dailyLimit = 10_000,
                targetAmount = 1_000_000,
                savedAmount = 0,
                streakDays = 0,
                editCount = 0
            )
        )
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

    private class FakeChallengeRepository(initialState: ChallengeState) : ChallengeRepository {
        override val state: StateFlow<ChallengeState> = MutableStateFlow(initialState)
        override val fixedDateDraft: StateFlow<FixedDateChallengeDraft?> = MutableStateFlow(null)
        override suspend fun loadCurrentChallenge(): Result<Unit> = Result.success(Unit)
        override suspend fun loadHistory(): Result<Unit> = Result.success(Unit)
        override suspend fun loadResult(challengeId: String): Result<Unit> = Result.success(Unit)
        override suspend fun updateFocusCategories(categories: List<String>): Result<List<String>> = Result.success(categories)
        override suspend fun startNewChallenge(request: OnboardingRequest, referenceToday: LocalDate): Result<ActiveChallenge> = error("unused")
        override suspend fun abandonChallenge(referenceToday: LocalDate): Result<Unit> = Result.success(Unit)
        override suspend fun acknowledgeChallengeEnd(): Result<Unit> = Result.success(Unit)
        override suspend fun loadFixedDateDraft(): Result<FixedDateChallengeDraft?> = Result.success(null)
        override suspend fun startFixedDateChallenge(
            sourceChallengeId: Long,
            startDate: LocalDate,
            budgetTotal: Int,
            fixedDay: Int
        ): Result<ActiveChallenge> = error("unused")
        override suspend fun updateTargetAmount(
            newTargetAmount: Int,
            effectiveFrom: LocalDate
        ): Result<Unit> = Result.success(Unit)
        override suspend fun loadRecommendation(): Result<String> = error("unused")
        override fun markNoRecord(date: LocalDate) = Unit
        override fun clearNoRecord(date: LocalDate) = Unit
        override fun markVisitedExpenseEditAfterEnd() = Unit
        override fun resetForAccount(referenceToday: LocalDate) = Unit
        override fun resetEmpty() = Unit
    }
}
