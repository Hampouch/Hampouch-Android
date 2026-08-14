package com.example.hampouch.ui.takeabreak

import com.example.hampouch.MainDispatcherRule
import com.example.hampouch.domain.model.BreakDuration
import com.example.hampouch.domain.model.RestState
import com.example.hampouch.domain.model.RestPeriod
import com.example.hampouch.domain.repository.RestRepository
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
    fun `직접 입력은 숫자 세 자리만 남기고 preset 선택을 해제한다`() {
        val viewModel = TakeABreakViewModel(FakeRestRepository())

        viewModel.changeCustomDays("12a34")

        assertEquals("123", viewModel.uiState.value.customDaysInput)
        assertEquals(null, viewModel.uiState.value.selectedDuration)
    }

    @Test
    fun `휴식 시작 실패는 submitting을 해제하고 오류를 UiState에 기록한다`() = runTest {
        val repository = FakeRestRepository(startResult = Result.failure(IllegalStateException("시작 실패")))
        val viewModel = TakeABreakViewModel(repository)

        viewModel.submit(isExtending = false)
        advanceUntilIdle()

        assertFalse(viewModel.uiState.value.isSubmitting)
        assertEquals("시작 실패", viewModel.uiState.value.errorMessage)
        assertEquals(1, repository.startCallCount)
    }

    private class FakeRestRepository(
        private val startResult: Result<Unit> = Result.success(Unit)
    ) : RestRepository {
        override val restState: StateFlow<RestState> = MutableStateFlow(RestState.NotResting)
        var startCallCount: Int = 0

        override suspend fun syncStatus(): Result<Unit> = Result.success(Unit)

        override suspend fun startBreak(period: RestPeriod): Result<Unit> {
            startCallCount++
            return startResult
        }

        override suspend fun extendBreak(period: RestPeriod): Result<Unit> =
            Result.success(Unit)

        override suspend fun resumeNow(): Result<Unit> = Result.success(Unit)

        override suspend fun postponeOneDay(): Result<Unit> = Result.success(Unit)

        override fun resetForAccount() = Unit
    }
}
