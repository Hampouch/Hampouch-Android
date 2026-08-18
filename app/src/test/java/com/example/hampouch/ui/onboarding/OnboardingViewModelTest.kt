package com.example.hampouch.ui.onboarding

import java.time.LocalDate
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class OnboardingViewModelTest {
    @Test
    fun `step event는 UDF 순서로 상태를 전이한다`() {
        val viewModel = OnboardingViewModel()

        viewModel.finishSplash()
        viewModel.next()
        viewModel.next()

        assertEquals(OnboardingStep.GOAL_SETTING, viewModel.uiState.value.step)
    }

    @Test
    fun `필수 draft가 누락되면 제출 모델을 만들지 않는다`() {
        assertNull(OnboardingViewModel().buildRequest())
    }

    @Test
    fun `지난달 식비 입력을 모두 지우면 draft 금액도 초기화한다`() {
        val viewModel = OnboardingViewModel()
        viewModel.changeExpense(99_999_999)

        viewModel.changeExpense(0)

        assertNull(viewModel.uiState.value.draft.lastMonthFoodExpense)
    }

    @Test
    fun `검증된 draft만 non-null 제출 모델로 변환한다`() {
        val viewModel = OnboardingViewModel()
        viewModel.changeDateFixed(true)
        viewModel.changeStartDate(LocalDate.of(2026, 8, 13))
        viewModel.changeExpense(300_000)
        viewModel.changeTotalTarget(140_000)

        val request = requireNotNull(viewModel.buildRequest())

        assertEquals(140_000, request.totalTargetAmount)
        assertEquals(LocalDate.of(2026, 8, 13), request.startDate)
    }
}
