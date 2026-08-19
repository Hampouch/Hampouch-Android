package com.example.hampouch.ui.onboarding

import com.example.hampouch.domain.model.OnboardingRequest
import com.example.hampouch.domain.repository.OnboardingLocalStore
import java.time.LocalDate
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class OnboardingViewModelTest {

    private fun newViewModel(skipNextSplash: Boolean = false) =
        OnboardingViewModel(FakeOnboardingLocalStore(skipNextSplash))

    @Test
    fun `step event는 UDF 순서로 상태를 전이한다`() {
        val viewModel = newViewModel()

        viewModel.finishSplash()
        viewModel.next()
        viewModel.next()

        assertEquals(OnboardingStep.GOAL_SETTING, viewModel.uiState.value.step)
    }

    @Test
    fun `필수 draft가 누락되면 제출 모델을 만들지 않는다`() {
        assertNull(newViewModel().buildRequest())
    }

    @Test
    fun `지난달 식비 입력을 모두 지우면 draft 금액도 초기화한다`() {
        val viewModel = newViewModel()
        viewModel.changeExpense(99_999_999)

        viewModel.changeExpense(0)

        assertNull(viewModel.uiState.value.draft.lastMonthFoodExpense)
    }

    @Test
    fun `날짜 고정을 선택하면 기존 기간을 초기화한다`() {
        val viewModel = newViewModel()
        viewModel.changePeriodEnabled(true)
        viewModel.changePeriod(30)

        viewModel.changeDateFixed(true)
        viewModel.changeStartDate(LocalDate.of(2026, 8, 20))
        viewModel.changePeriodEnabled(true)

        val draft = viewModel.uiState.value.draft
        assertEquals(true, draft.periodEnabled)
        assertEquals(false, draft.dateFixed)
        assertNull(draft.challengePeriodDays)
        assertNull(draft.startDate)
    }

    @Test
    fun `검증된 draft만 non-null 제출 모델로 변환한다`() {
        val viewModel = newViewModel()
        viewModel.changeDateFixed(true)
        viewModel.changeStartDate(LocalDate.of(2026, 8, 13))
        viewModel.changeExpense(300_000)
        viewModel.changeTotalTarget(140_000)

        val request = requireNotNull(viewModel.buildRequest())

        assertEquals(140_000, request.totalTargetAmount)
        assertEquals(LocalDate.of(2026, 8, 13), request.startDate)
    }

    @Test
    fun `저장소가 스플래시 건너뛰기를 요청하면 진단 단계에서 시작한다`() {
        val viewModel = newViewModel(skipNextSplash = true)

        assertEquals(OnboardingStep.EXPENSE_DIAGNOSIS, viewModel.uiState.value.step)
    }

    private class FakeOnboardingLocalStore(
        private val skipNextSplash: Boolean
    ) : OnboardingLocalStore {
        override fun hasCompletedOnboarding(): Boolean = false
        override fun restorePendingIfNeeded() = Unit
        override fun captureOnboardingComplete(request: OnboardingRequest) = Unit
        override fun resetOnboarding() = Unit
        override fun reserveForNewAccount(email: String) = Unit
        override fun takeReservedRequest(email: String): OnboardingRequest? = null
        override fun markSkipNextSplash() = Unit
        override fun consumeSkipNextSplash(): Boolean = skipNextSplash
    }
}
