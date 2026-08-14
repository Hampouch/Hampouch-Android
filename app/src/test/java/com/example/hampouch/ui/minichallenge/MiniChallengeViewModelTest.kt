package com.example.hampouch.ui.minichallenge

import com.example.hampouch.MainDispatcherRule
import com.example.hampouch.domain.model.MiniChallengeDuration
import com.example.hampouch.domain.model.MiniChallengeState
import com.example.hampouch.domain.model.RecommendedMiniChallenge
import com.example.hampouch.domain.repository.MiniChallengeRepository
import com.example.hampouch.ui.common.LoadState
import java.time.LocalDate
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.async
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class MiniChallengeViewModelTest {
    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    @Test
    fun `추가 성공은 반드시 Added event로 귀결된다`() = runTest {
        val date = LocalDate.of(2026, 8, 12)
        val viewModel = MiniChallengeViewModel(FakeMiniChallengeRepository(Result.success(date)))
        val event = async { viewModel.events.first() }

        viewModel.addCustom(date, "물 마시기", MiniChallengeDuration.Today)
        advanceUntilIdle()

        assertEquals(MiniChallengeEvent.Added(date), event.await())
    }

    @Test
    fun `추가 실패는 ShowMessage event로 귀결된다`() = runTest {
        val date = LocalDate.of(2026, 8, 12)
        val viewModel = MiniChallengeViewModel(
            FakeMiniChallengeRepository(Result.failure(IllegalStateException("중복입니다.")))
        )
        val event = async { viewModel.events.first() }

        viewModel.addCustom(date, "물 마시기", MiniChallengeDuration.Today)
        advanceUntilIdle()

        assertEquals(MiniChallengeEvent.ShowMessage("중복입니다."), event.await())
    }

    @Test
    fun `조회 실패와 retry가 LoadState에 명시된다`() = runTest {
        val repository = FakeMiniChallengeRepository(
            addResult = Result.success(LocalDate.of(2026, 8, 12)),
            loadResult = Result.failure(IllegalStateException("조회 실패"))
        )
        val viewModel = MiniChallengeViewModel(repository)

        viewModel.loadChallenges(LocalDate.of(2026, 8, 12))
        advanceUntilIdle()
        assertEquals(LoadState.Failure("조회 실패"), viewModel.loadState.value)

        viewModel.retry()
        advanceUntilIdle()
        assertEquals(2, repository.loadCallCount)
    }

    private class FakeMiniChallengeRepository(
        private val addResult: Result<LocalDate>,
        private val loadResult: Result<Unit> = Result.success(Unit)
    ) : MiniChallengeRepository {
        override val state: StateFlow<MiniChallengeState> = MutableStateFlow(MiniChallengeState())
        var loadCallCount = 0
        override suspend fun loadChallenges(date: LocalDate): Result<Unit> {
            loadCallCount++
            return loadResult
        }
        override suspend fun loadRecommended(durationDays: Int?): Result<Unit> = Result.success(Unit)
        override suspend fun addRecommended(
            date: LocalDate,
            recommended: RecommendedMiniChallenge
        ): Result<LocalDate> = addResult
        override suspend fun addCustom(
            date: LocalDate,
            name: String,
            duration: MiniChallengeDuration
        ): Result<LocalDate> = addResult
        override suspend fun remove(date: LocalDate, id: String): Result<Unit> = Result.success(Unit)
        override suspend fun setChecked(
            date: LocalDate,
            id: String,
            checked: Boolean
        ): Result<Unit> = Result.success(Unit)
    }
}
