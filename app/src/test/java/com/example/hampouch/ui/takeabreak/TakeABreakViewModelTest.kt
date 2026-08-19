package com.example.hampouch.ui.takeabreak

import com.example.hampouch.MainDispatcherRule
import com.example.hampouch.domain.model.BreakDuration
import com.example.hampouch.domain.model.RestState
import com.example.hampouch.domain.model.RestPeriod
import com.example.hampouch.domain.repository.RestRepository
import com.example.hampouch.ui.widget.HomeWidgetRefreshRequester
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Rule
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class TakeABreakViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    @Test
    fun `직접 입력은 숫자 네 자리만 남기고 preset 선택을 해제한다`() {
        val viewModel = TakeABreakViewModel(FakeRestRepository(), HomeWidgetRefreshRequester {})

        viewModel.changeCustomDays("12a34")

        assertEquals("1234", viewModel.uiState.value.customDaysInput)
        assertEquals(null, viewModel.uiState.value.selectedDuration)
    }

    @Test
    fun `직접 입력은 Swagger 상한인 3650일까지 허용한다`() = runTest {
        val repository = FakeRestRepository()
        var widgetRefreshCount = 0
        val viewModel = TakeABreakViewModel(
            repository,
            HomeWidgetRefreshRequester { widgetRefreshCount++ }
        )

        viewModel.changeCustomDays("3650")
        viewModel.submit(isExtending = false)
        advanceUntilIdle()

        assertEquals(RestPeriod.Custom(3650), repository.lastStartedPeriod)
        assertEquals(1, repository.startCallCount)
        assertEquals(1, widgetRefreshCount)
    }

    @Test
    fun `직접 입력이 3650일을 넘으면 요청하지 않는다`() = runTest {
        val repository = FakeRestRepository()
        val viewModel = TakeABreakViewModel(repository, HomeWidgetRefreshRequester {})

        viewModel.changeCustomDays("3651")
        viewModel.submit(isExtending = false)
        advanceUntilIdle()

        assertEquals(0, repository.startCallCount)
        assertEquals("휴식 기간은 1일 이상 3650일 이하로 입력해주세요.", viewModel.uiState.value.errorMessage)
    }

    @Test(expected = IllegalArgumentException::class)
    fun `도메인 휴식 기간도 3650일 초과를 허용하지 않는다`() {
        RestPeriod.Custom(3651)
    }

    @Test
    fun `휴식 시작 실패는 submitting을 해제하고 오류를 UiState에 기록한다`() = runTest {
        val repository = FakeRestRepository(startResult = Result.failure(IllegalStateException("시작 실패")))
        var widgetRefreshCount = 0
        val viewModel = TakeABreakViewModel(
            repository,
            HomeWidgetRefreshRequester { widgetRefreshCount++ }
        )

        viewModel.submit(isExtending = false)
        advanceUntilIdle()

        assertFalse(viewModel.uiState.value.isSubmitting)
        assertEquals("시작 실패", viewModel.uiState.value.errorMessage)
        assertEquals(1, repository.startCallCount)
        assertEquals(0, widgetRefreshCount)
    }

    private class FakeRestRepository(
        private val startResult: Result<Unit> = Result.success(Unit)
    ) : RestRepository {
        override val restState: StateFlow<RestState> = MutableStateFlow(RestState.NotResting)
        var startCallCount: Int = 0
        var lastStartedPeriod: RestPeriod? = null

        override suspend fun syncStatus(): Result<Unit> = Result.success(Unit)

        override suspend fun startBreak(period: RestPeriod): Result<Unit> {
            startCallCount++
            lastStartedPeriod = period
            return startResult
        }

        override suspend fun extendBreak(period: RestPeriod): Result<Unit> =
            Result.success(Unit)

        override suspend fun resumeNow(): Result<Unit> = Result.success(Unit)

        override suspend fun postponeOneDay(): Result<Unit> = Result.success(Unit)

        override fun resetForAccount() = Unit
    }
}
