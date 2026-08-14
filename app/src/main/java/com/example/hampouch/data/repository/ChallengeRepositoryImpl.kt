package com.example.hampouch.data.repository

import com.example.hampouch.core.config.ChallengeConfig
import com.example.hampouch.data.remote.ChallengeApi
import com.example.hampouch.data.remote.dto.ChallengeCreateRequest
import com.example.hampouch.data.remote.dto.ChallengeCurrentData
import com.example.hampouch.data.remote.dto.ChallengeFocusCategoriesRequest
import com.example.hampouch.data.remote.dto.ChallengeHistoryItemDto
import com.example.hampouch.data.remote.runCatchingNetwork
import com.example.hampouch.data.remote.toApiException
import com.example.hampouch.data.remote.unauthorized
import com.example.hampouch.domain.model.ActiveChallenge
import com.example.hampouch.domain.model.ApiException
import com.example.hampouch.domain.model.ChallengeState
import com.example.hampouch.domain.model.DailyLimitOverride
import com.example.hampouch.domain.model.OnboardingRequest
import com.example.hampouch.domain.repository.ChallengeRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import java.time.LocalDate
import java.time.temporal.ChronoUnit
import javax.inject.Inject
import javax.inject.Singleton

private const val TAG = "ChallengeRepository"

private const val CHALLENGE_TOTAL_DAYS = 14
private const val CHALLENGE_DAILY_LIMIT = 20_000
private const val CHALLENGE_SAVED_AMOUNT = 21_400
private const val CHALLENGE_STREAK_DAYS = 4

private const val PREVIOUS_CHALLENGE_TOTAL_DAYS = 7
private const val PREVIOUS_CHALLENGE_DAILY_LIMIT = 20_000

private const val DEFAULT_ONE_OFF_DAYS = 30

@Singleton
class ChallengeRepositoryImpl @Inject constructor(
    private val apiService: ChallengeApi,
    private val authRepository: AuthRepository
) : ChallengeRepository {

    private val weakCategoryLabels: Map<String, String> = mapOf(
        "delivery" to "배달",
        "dining_out" to "외식",
        "convenience" to "편의점",
        "cafe" to "카페",
        "snack" to "간식",
        "mart" to "장보기",
        "drink" to "술자리"
    )

    private val _state = MutableStateFlow(
        ChallengeState(
            challenges = if (ChallengeConfig.USE_SERVER_CHALLENGE) emptyList() else buildSeedChallenges(LocalDate.now())
        )
    )
    override val state: StateFlow<ChallengeState> = _state.asStateFlow()

    private val current: ChallengeState get() = _state.value

    private fun buildSeedChallenges(referenceToday: LocalDate): List<ActiveChallenge> {
        val activeEnd = referenceToday.minusDays(1)
        val activeStart = activeEnd.minusDays((CHALLENGE_TOTAL_DAYS - 1).toLong())
        val previousEnd = activeStart.minusDays(1)
        val previousStart = previousEnd.minusDays((PREVIOUS_CHALLENGE_TOTAL_DAYS - 1).toLong())

        val previous = ActiveChallenge(
            id = "challenge_previous",
            totalDays = PREVIOUS_CHALLENGE_TOTAL_DAYS,
            periodStart = previousStart,
            periodEnd = previousEnd,
            dailyLimit = PREVIOUS_CHALLENGE_DAILY_LIMIT,
            targetAmount = PREVIOUS_CHALLENGE_DAILY_LIMIT * PREVIOUS_CHALLENGE_TOTAL_DAYS,
            savedAmount = 0,
            streakDays = 0,
            editCount = 0
        )
        val active = ActiveChallenge(
            id = "challenge_active",
            totalDays = CHALLENGE_TOTAL_DAYS,
            periodStart = activeStart,
            periodEnd = activeEnd,
            dailyLimit = CHALLENGE_DAILY_LIMIT,
            targetAmount = CHALLENGE_DAILY_LIMIT * CHALLENGE_TOTAL_DAYS,
            savedAmount = CHALLENGE_SAVED_AMOUNT,
            streakDays = CHALLENGE_STREAK_DAYS,
            editCount = 0
        )
        return listOf(previous, active)
    }

    override fun markNoRecord(date: LocalDate) {
        _state.update { it.copy(noRecordDates = it.noRecordDates + date) }
    }

    override fun clearNoRecord(date: LocalDate) {
        _state.update { it.copy(noRecordDates = it.noRecordDates - date) }
    }

    override fun markVisitedExpenseEditAfterEnd() {
        _state.update { it.copy(hasVisitedExpenseEditAfterEnd = true) }
    }

    private fun upsertChallenge(entry: ActiveChallenge) {
        _state.update { state ->
            val existingIndex = state.challenges.indexOfFirst { it.id == entry.id }
            val updated = if (existingIndex >= 0) {
                state.challenges.toMutableList().also { it[existingIndex] = entry }
            } else {
                state.challenges + entry
            }
            state.copy(challenges = updated.sortedBy { it.periodStart })
        }
    }

    private fun clearActiveChallenge() {
        val today = LocalDate.now()
        _state.update { state ->
            state.copy(
                challenges = state.challenges.filterNot {
                    it.abandonedDate == null && !today.isAfter(it.effectivePeriodEnd)
                }
            )
        }
    }

    private fun weakCategoryLabelFor(id: String): String = weakCategoryLabels[id] ?: id
    private fun resolvePeriod(
        request: OnboardingRequest,
        referenceToday: LocalDate
    ): Triple<LocalDate, LocalDate, Boolean> {
        return if (request.dateFixed) {
            val anchor = request.startDate ?: referenceToday
            val end = if (anchor.isAfter(referenceToday)) {
                anchor.minusDays(1)
            } else {
                referenceToday.plusMonths(1).minusDays(1)
            }
            Triple(referenceToday, end, true)
        } else {
            val days = (request.customPeriodDays ?: DEFAULT_ONE_OFF_DAYS).coerceAtLeast(1)
            Triple(referenceToday, referenceToday.plusDays((days - 1).toLong()), false)
        }
    }

    override suspend fun loadCurrentChallenge(): Result<Unit> {
        if (!ChallengeConfig.USE_SERVER_CHALLENGE) return Result.success(Unit)
        if (authRepository.currentAuthHeader() == null) return Result.failure(unauthorized())
        return runCatchingNetwork(TAG) {
            val response = apiService.getCurrentChallenge()
            if (response.isSuccessful) {
                applyCurrent(response.body()?.data)
                Result.success(Unit)
            } else {
                val error = response.toApiException("진행 중인 챌린지 조회에 실패했습니다.")
                if (error.code == "NO_ACTIVE_CHALLENGE") {
                    clearActiveChallenge()
                    Result.success(Unit)
                } else {
                    Result.failure(error)
                }
            }
        }
    }

    private fun applyCurrent(data: ChallengeCurrentData?) {
        val summary = data?.challenge
        if (summary == null || summary.status == "VOID") {
            clearActiveChallenge()
            return
        }
        upsertChallenge(
            ActiveChallenge(
                id = summary.id.toString(),
                totalDays = summary.durationDays,
                periodStart = LocalDate.parse(summary.startDate),
                periodEnd = LocalDate.parse(summary.endDate),
                dailyLimit = summary.dailyLimit,
                targetAmount = summary.budgetTotal,
                savedAmount = data.progress?.savedAmountSoFar ?: 0,
                streakDays = data.progress?.currentStreak ?: 0,
                editCount = 0,
                remoteStatus = summary.status
            )
        )
    }

    override suspend fun loadHistory(): Result<Unit> {
        if (!ChallengeConfig.USE_SERVER_CHALLENGE) return Result.success(Unit)
        if (authRepository.currentAuthHeader() == null) return Result.failure(unauthorized())
        return runCatchingNetwork(TAG) {
            val response = apiService.getChallengeHistory()
            val data = response.body()?.data
            if (response.isSuccessful && data != null) {
                data.items.forEach { item -> upsertChallenge(item.toActiveChallenge()) }
                Result.success(Unit)
            } else {
                Result.failure(response.toApiException("지난 챌린지 목록을 불러오지 못했습니다."))
            }
        }
    }

    private fun ChallengeHistoryItemDto.toActiveChallenge(): ActiveChallenge = ActiveChallenge(
        id = challengeId.toString(),
        totalDays = durationDays,
        periodStart = LocalDate.parse(startDate),
        periodEnd = LocalDate.parse(endDate),
        dailyLimit = if (durationDays > 0) budgetTotal / durationDays else 0,
        targetAmount = budgetTotal,
        savedAmount = savedAmount,
        streakDays = 0,
        editCount = 0,
        remoteStatus = status
    )

    override suspend fun loadResult(challengeId: String): Result<Unit> {
        if (!ChallengeConfig.USE_SERVER_CHALLENGE) return Result.success(Unit)
        val id = challengeId.toLongOrNull() ?: return Result.success(Unit)
        if (authRepository.currentAuthHeader() == null) return Result.failure(unauthorized())
        return runCatchingNetwork(TAG) {
            val response = apiService.getChallengeResult(id)
            val data = response.body()?.data
            if (response.isSuccessful && data != null) {
                current.challengeById(challengeId)?.let { existing ->
                    upsertChallenge(existing.copy(remoteStatus = data.status, closedAt = data.closedAt))
                }
                Result.success(Unit)
            } else {
                Result.failure(response.toApiException("챌린지 결과를 불러오지 못했습니다."))
            }
        }
    }

    override suspend fun updateFocusCategories(categories: List<String>): Result<List<String>> {
        if (!ChallengeConfig.USE_SERVER_CHALLENGE) return Result.success(categories)
        val id = current.activeChallenge?.id?.toLongOrNull()
            ?: return Result.failure(
                ApiException(code = "CHALLENGE_NOT_IN_PROGRESS", message = "진행 중인 챌린지가 없습니다.")
            )
        if (authRepository.currentAuthHeader() == null) return Result.failure(unauthorized())
        return runCatchingNetwork(TAG) {
            val response = apiService.updateChallengeFocusCategories(
                id, ChallengeFocusCategoriesRequest(categories)
            )
            val data = response.body()?.data
            if (response.isSuccessful && data != null) {
                Result.success(data.categories)
            } else {
                Result.failure(response.toApiException("집중 카테고리 수정에 실패했습니다."))
            }
        }
    }

    private fun applyLocalStartNewChallenge(
        request: OnboardingRequest,
        referenceToday: LocalDate
    ): ActiveChallenge {
        val (periodStart, periodEnd, repeatMonthly) = resolvePeriod(request, referenceToday)
        val totalDays = ChronoUnit.DAYS.between(periodStart, periodEnd).toInt() + 1
        val targetAmount = (request.totalTargetAmount ?: 0).coerceAtLeast(0)
        val dailyLimit = (request.dailyTargetAmount ?: (if (totalDays > 0) targetAmount / totalDays else 0))
            .coerceAtLeast(0)

        val newChallenge = ActiveChallenge(
            id = "challenge_${System.currentTimeMillis()}",
            totalDays = totalDays,
            periodStart = periodStart,
            periodEnd = periodEnd,
            dailyLimit = dailyLimit,
            targetAmount = targetAmount,
            savedAmount = 0,
            streakDays = 0,
            editCount = 0,
            repeatMonthly = repeatMonthly
        )
        _state.update {
            it.copy(
                challenges = it.challenges + newChallenge,
                endAcknowledged = false,
                hasVisitedExpenseEditAfterEnd = false
            )
        }
        return newChallenge
    }

    override suspend fun startNewChallenge(
        request: OnboardingRequest,
        referenceToday: LocalDate
    ): Result<ActiveChallenge> {
        if (!ChallengeConfig.USE_SERVER_CHALLENGE) {
            return Result.success(applyLocalStartNewChallenge(request, referenceToday))
        }
        val (periodStart, periodEnd, repeatMonthly) = resolvePeriod(request, referenceToday)
        val totalDays = ChronoUnit.DAYS.between(periodStart, periodEnd).toInt() + 1
        val budgetTotal = (request.totalTargetAmount ?: 0).coerceAtLeast(0)
        if (authRepository.currentAuthHeader() == null) return Result.failure(unauthorized())
        return runCatchingNetwork(TAG) {
            val response = apiService.createChallenge(
                ChallengeCreateRequest(
                    durationDays = totalDays,
                    budgetTotal = budgetTotal,
                    startDate = periodStart.toString(),
                    resetByPayday = repeatMonthly,
                    paydayDay = if (repeatMonthly) periodEnd.plusDays(1).dayOfMonth else null,
                    weakCategories = request.topSpendingCategoryIds.map { weakCategoryLabelFor(it) }
                )
            )
            val data = response.body()?.data
            if (response.isSuccessful && data != null) {
                val createdStart = LocalDate.parse(data.startDate)
                val createdEnd = LocalDate.parse(data.endDate)
                val newChallenge = ActiveChallenge(
                    id = data.challengeId.toString(),
                    totalDays = ChronoUnit.DAYS.between(createdStart, createdEnd).toInt() + 1,
                    periodStart = createdStart,
                    periodEnd = createdEnd,
                    dailyLimit = data.dailyLimit,
                    targetAmount = budgetTotal,
                    savedAmount = 0,
                    streakDays = 0,
                    editCount = 0,
                    repeatMonthly = repeatMonthly,
                    remoteStatus = data.status
                )
                upsertChallenge(newChallenge)
                _state.update { it.copy(endAcknowledged = false, hasVisitedExpenseEditAfterEnd = false) }
                Result.success(newChallenge)
            } else {
                Result.failure(response.toApiException("챌린지 생성에 실패했습니다."))
            }
        }
    }

    private fun applyAbandon(
        challenge: ActiveChallenge,
        referenceToday: LocalDate,
        remoteStatus: String? = null
    ) {
        val updated = challenge.copy(
            abandonedDate = referenceToday,
            remoteStatus = remoteStatus ?: challenge.remoteStatus
        )
        _state.update {
            it.copy(
                challenges = it.challenges.dropLast(1) + updated,
                endAcknowledged = true,
                hasVisitedExpenseEditAfterEnd = false
            )
        }
    }

    override suspend fun abandonChallenge(referenceToday: LocalDate): Result<Unit> {
        val challenge = current.activeChallenge ?: return Result.success(Unit)
        if (!ChallengeConfig.USE_SERVER_CHALLENGE) {
            applyAbandon(challenge, referenceToday)
            return Result.success(Unit)
        }
        val id = challenge.id.toLongOrNull() ?: run {
            applyAbandon(challenge, referenceToday)
            return Result.success(Unit)
        }
        if (authRepository.currentAuthHeader() == null) return Result.failure(unauthorized())
        return runCatchingNetwork(TAG) {
            val response = apiService.giveUpChallenge(id)
            val data = response.body()?.data
            if (response.isSuccessful && data != null) {
                applyAbandon(challenge, referenceToday, remoteStatus = data.status)
                Result.success(Unit)
            } else {
                Result.failure(response.toApiException("중도 포기 처리에 실패했습니다."))
            }
        }
    }

    private fun applyAcknowledge(ended: ActiveChallenge) {
        if (ended.repeatMonthly && !ChallengeConfig.USE_SERVER_CHALLENGE) {
            val nextStart = ended.periodEnd.plusDays(1)
            val nextEnd = nextStart.plusMonths(1).minusDays(1)
            val nextChallenge = ActiveChallenge(
                id = "challenge_${System.currentTimeMillis()}",
                totalDays = ChronoUnit.DAYS.between(nextStart, nextEnd).toInt() + 1,
                periodStart = nextStart,
                periodEnd = nextEnd,
                dailyLimit = ended.dailyLimit,
                targetAmount = ended.targetAmount,
                savedAmount = 0,
                streakDays = 0,
                editCount = 0,
                repeatMonthly = true
            )
            _state.update {
                it.copy(
                    challenges = it.challenges + nextChallenge,
                    hasVisitedExpenseEditAfterEnd = false
                )
            }
        } else {
            _state.update { it.copy(endAcknowledged = true) }
        }
    }

    override suspend fun acknowledgeChallengeEnd(): Result<Unit> {
        val ended = current.activeChallenge ?: return Result.success(Unit)
        if (ChallengeConfig.USE_SERVER_CHALLENGE) {
            val id = ended.id.toLongOrNull()
            if (id != null) {
                if (authRepository.currentAuthHeader() == null) return Result.failure(unauthorized())
                val result = runCatchingNetwork(TAG) {
                    val response = apiService.closeChallenge(id)
                    val data = response.body()?.data
                    if (response.isSuccessful && data != null) {
                        upsertChallenge(ended.copy(remoteStatus = data.status, closedAt = data.closedAt))
                        Result.success(Unit)
                    } else {
                        Result.failure(response.toApiException("챌린지 종료 처리에 실패했습니다."))
                    }
                }
                if (result.isFailure) return result
            }
        }
        applyAcknowledge(ended)
        return Result.success(Unit)
    }

    override fun updateTargetAmount(newTargetAmount: Int, effectiveFrom: LocalDate) {
        val challenge = current.activeChallenge ?: return
        val newDailyLimit = (newTargetAmount / challenge.totalDays).coerceAtLeast(0)
        val updated = challenge.copy(
            targetAmount = newTargetAmount,
            dailyLimit = newDailyLimit,
            editCount = challenge.editCount + 1,
            dailyLimitOverrides = challenge.dailyLimitOverrides + DailyLimitOverride(effectiveFrom, newDailyLimit)
        )
        _state.update { it.copy(challenges = it.challenges.dropLast(1) + updated) }
    }

    override fun resetForAccount(referenceToday: LocalDate) {
        _state.value = ChallengeState(
            challenges = if (ChallengeConfig.USE_SERVER_CHALLENGE) emptyList() else buildSeedChallenges(referenceToday)
        )
    }

    override fun resetEmpty() {
        _state.value = ChallengeState()
    }
}
