package com.example.hampouch.data.repository

import com.example.hampouch.core.config.ChallengeConfig
import com.example.hampouch.data.remote.ChallengeApi
import com.example.hampouch.data.remote.dto.ChallengeAdjustRequest
import com.example.hampouch.data.remote.dto.ChallengeCalendarData
import com.example.hampouch.data.remote.dto.ChallengeCreateRequest
import com.example.hampouch.data.remote.dto.ChallengeCurrentData
import com.example.hampouch.data.remote.dto.ChallengeEmotionBreakdownDto
import com.example.hampouch.data.remote.dto.ChallengeFixedDateStartRequest
import com.example.hampouch.data.remote.dto.ChallengeFocusCategoriesRequest
import com.example.hampouch.data.remote.dto.ChallengeHistoryItemDto
import com.example.hampouch.data.remote.dto.ChallengeResultSummaryDto
import com.example.hampouch.data.remote.runCatchingNetwork
import com.example.hampouch.data.remote.toApiException
import com.example.hampouch.data.remote.unauthorized
import com.example.hampouch.domain.model.ActiveChallenge
import com.example.hampouch.domain.model.ApiException
import com.example.hampouch.domain.model.ChallengeResultSummary
import com.example.hampouch.domain.model.ChallengeState
import com.example.hampouch.domain.model.DailyLimitOverride
import com.example.hampouch.domain.model.DailyRecordStatus
import com.example.hampouch.domain.model.EmotionStat
import com.example.hampouch.domain.model.FixedDateChallengeDraft
import com.example.hampouch.domain.model.FixedDateDraftState
import com.example.hampouch.domain.model.OnboardingRequest
import com.example.hampouch.domain.model.SpendingEmotion
import com.example.hampouch.domain.repository.ChallengeRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import java.time.LocalDate
import java.time.YearMonth
import java.time.temporal.ChronoUnit
import javax.inject.Inject
import javax.inject.Singleton

private const val TAG = "ChallengeRepository"

private const val CHALLENGE_TOTAL_DAYS = 14
private const val CHALLENGE_DAILY_LIMIT = 20_000
private const val CHALLENGE_SAVED_AMOUNT = 21_400
private const val CHALLENGE_STREAK_DAYS = 4
private const val HTTP_NOT_FOUND = 404

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

    private val _fixedDateDraft = MutableStateFlow<FixedDateChallengeDraft?>(null)
    override val fixedDateDraft: StateFlow<FixedDateChallengeDraft?> = _fixedDateDraft.asStateFlow()

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
                remoteStatus = summary.status,
                warningCodes = data.warningCards.orEmpty().mapNotNull { it.type }
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
                    val calendarDays = fetchCalendarDays(
                        id,
                        LocalDate.parse(data.period.startDate),
                        LocalDate.parse(data.period.endDate)
                    )
                    upsertChallenge(
                        existing.copy(
                            remoteStatus = data.status,
                            expenseLockedAt = data.expenseLockedAt,
                            resultSummary = data.summary.toDomain(),
                            emotionBreakdown = data.emotionBreakdown.map { it.toDomain() },
                            calendarDays = calendarDays
                        )
                    )
                }
                Result.success(Unit)
            } else {
                Result.failure(response.toApiException("챌린지 결과를 불러오지 못했습니다."))
            }
        }
    }

    private suspend fun fetchCalendarDays(
        challengeId: Long,
        periodStart: LocalDate,
        periodEnd: LocalDate
    ): Map<LocalDate, DailyRecordStatus> {
        val months = generateSequence(YearMonth.from(periodStart)) { it.plusMonths(1) }
            .takeWhile { !it.isAfter(YearMonth.from(periodEnd)) }
            .toList()
        val days = mutableMapOf<LocalDate, DailyRecordStatus>()
        for (month in months) {
            val response = apiService.getChallengeCalendar(challengeId, month.year, month.monthValue)
            days.putAll(response.body()?.data.toDailyRecords())
        }
        /** 기록이 없는 날은 0원 지출로 간주해 달력에서도 성공한 날로 계산한다(스펙 명시 규칙). */
        var date = periodStart
        while (!date.isAfter(periodEnd)) {
            days.putIfAbsent(date, DailyRecordStatus.SUCCESS)
            date = date.plusDays(1)
        }
        return days
    }

    private fun ChallengeCalendarData?.toDailyRecords(): Map<LocalDate, DailyRecordStatus> {
        if (this == null) return emptyMap()
        return days.associate { day ->
            LocalDate.parse(day.date) to if (day.status == "SUCCESS") {
                DailyRecordStatus.SUCCESS
            } else {
                DailyRecordStatus.FAIL
            }
        }
    }

    private fun ChallengeResultSummaryDto.toDomain(): ChallengeResultSummary = ChallengeResultSummary(
        successDays = successDays,
        overDays = overDays,
        savedAmount = savedAmount,
        overAmount = overAmount,
        maxStreak = maxStreak,
        budgetTotal = budgetTotal,
        actualSpent = actualSpent
    )

    private fun ChallengeEmotionBreakdownDto.toDomain(): EmotionStat = EmotionStat(
        emotion = runCatching { SpendingEmotion.valueOf(emotion) }.getOrDefault(SpendingEmotion.ETC),
        percent = ratio,
        amount = amount
    )

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
                    durationDays = if (repeatMonthly) null else totalDays,
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
                    totalDays = data.durationDays,
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

    private fun applyLocalAcknowledge(ended: ActiveChallenge) {
        if (ended.repeatMonthly) {
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

    private fun markEndAcknowledged(resetExpenseEditFlag: Boolean) {
        _state.update {
            it.copy(
                endAcknowledged = true,
                hasVisitedExpenseEditAfterEnd = if (resetExpenseEditFlag) false else it.hasVisitedExpenseEditAfterEnd
            )
        }
    }

    override suspend fun loadFixedDateDraft(): Result<FixedDateChallengeDraft?> {
        if (!ChallengeConfig.USE_SERVER_CHALLENGE) {
            _fixedDateDraft.value = null
            return Result.success(null)
        }
        if (authRepository.currentAuthHeader() == null) return Result.failure(unauthorized())
        return runCatchingNetwork(TAG) {
            val draftResponse = apiService.getFixedDateChallengeDraft()
            if (!draftResponse.isSuccessful) {
                return@runCatchingNetwork if (draftResponse.code() == HTTP_NOT_FOUND) {
                    _fixedDateDraft.value = null
                    Result.success(null)
                } else {
                    Result.failure(draftResponse.toApiException("다음 챌린지 초안을 불러오지 못했습니다."))
                }
            }
            val draft = draftResponse.body()?.data
                ?: return@runCatchingNetwork Result.failure(
                    ApiException(code = "EMPTY_RESPONSE", message = "다음 챌린지 초안을 불러오지 못했습니다.")
                )
            val mapped = FixedDateChallengeDraft(
                state = FixedDateDraftState.valueOf(draft.state),
                sourceChallengeId = draft.sourceChallengeId,
                previousStartDate = LocalDate.parse(draft.previousStartDate),
                previousEndDate = LocalDate.parse(draft.previousEndDate),
                fixedDay = draft.fixedDay,
                nextStartDate = LocalDate.parse(draft.nextStartDate),
                nextEndDate = LocalDate.parse(draft.nextEndDate),
                durationDays = draft.durationDays,
                budgetTotal = draft.budgetTotal,
                dailyLimit = draft.dailyLimit
            )
            _fixedDateDraft.value = mapped
            Result.success(mapped)
        }
    }

    override suspend fun startFixedDateChallenge(
        sourceChallengeId: Long,
        startDate: LocalDate,
        budgetTotal: Int,
        fixedDay: Int
    ): Result<ActiveChallenge> {
        if (!ChallengeConfig.USE_SERVER_CHALLENGE) {
            return Result.failure(ApiException(code = "SERVER_DISABLED", message = "날짜 고정 챌린지를 시작할 수 없습니다."))
        }
        if (authRepository.currentAuthHeader() == null) return Result.failure(unauthorized())
        return runCatchingNetwork(TAG) {
            val startResponse = apiService.startFixedDateChallenge(
                ChallengeFixedDateStartRequest(
                    sourceChallengeId = sourceChallengeId,
                    startDate = startDate.toString(),
                    budgetTotal = budgetTotal,
                    fixedDay = fixedDay
                )
            )
            val data = startResponse.body()?.data
            if (startResponse.isSuccessful && data != null) {
                val challenge = ActiveChallenge(
                    id = data.challengeId.toString(),
                    totalDays = data.durationDays,
                    periodStart = LocalDate.parse(data.startDate),
                    periodEnd = LocalDate.parse(data.endDate),
                    dailyLimit = data.dailyLimit,
                    targetAmount = budgetTotal,
                    savedAmount = 0,
                    streakDays = 0,
                    editCount = 0,
                    repeatMonthly = true,
                    remoteStatus = data.status
                )
                upsertChallenge(challenge)
                _fixedDateDraft.value = null
                Result.success(challenge)
            } else {
                Result.failure(startResponse.toApiException("다음 챌린지 시작에 실패했습니다."))
            }
        }
    }

    override suspend fun acknowledgeChallengeEnd(): Result<Unit> {
        val ended = current.activeChallenge ?: return Result.success(Unit)
        if (!ChallengeConfig.USE_SERVER_CHALLENGE) {
            applyLocalAcknowledge(ended)
            return Result.success(Unit)
        }
        val id = ended.id.toLongOrNull()
        if (id != null) {
            if (authRepository.currentAuthHeader() == null) return Result.failure(unauthorized())
            val result = runCatchingNetwork(TAG) {
                val response = apiService.closeChallenge(id)
                val data = response.body()?.data
                if (response.isSuccessful && data != null) {
                    upsertChallenge(ended.copy(remoteStatus = data.status, expenseLockedAt = data.expenseLockedAt))
                    Result.success(Unit)
                } else {
                    Result.failure(response.toApiException("챌린지 종료 처리에 실패했습니다."))
                }
            }
            if (result.isFailure) return result
        }
        markEndAcknowledged(resetExpenseEditFlag = false)
        return Result.success(Unit)
    }

    override suspend fun updateTargetAmount(newTargetAmount: Int, effectiveFrom: LocalDate): Result<Unit> {
        val challenge = current.activeChallenge ?: return Result.success(Unit)
        if (!ChallengeConfig.USE_SERVER_CHALLENGE) {
            applyLocalTargetAmountUpdate(challenge, newTargetAmount, effectiveFrom)
            return Result.success(Unit)
        }
        val id = challenge.id.toLongOrNull() ?: run {
            applyLocalTargetAmountUpdate(challenge, newTargetAmount, effectiveFrom)
            return Result.success(Unit)
        }
        if (authRepository.currentAuthHeader() == null) return Result.failure(unauthorized())
        return runCatchingNetwork(TAG) {
            val response = apiService.adjustChallengeBudget(id, ChallengeAdjustRequest(budgetTotal = newTargetAmount))
            val data = response.body()?.data
            if (response.isSuccessful && data != null) {
                val updated = challenge.copy(
                    targetAmount = data.budgetTotal,
                    dailyLimit = data.dailyLimit,
                    editCount = data.usedCount,
                    dailyLimitOverrides = challenge.dailyLimitOverrides +
                        DailyLimitOverride(effectiveFrom, data.dailyLimit)
                )
                _state.update { it.copy(challenges = it.challenges.dropLast(1) + updated) }
                Result.success(Unit)
            } else {
                Result.failure(response.toApiException("목표 금액 조정에 실패했습니다."))
            }
        }
    }

    private fun applyLocalTargetAmountUpdate(
        challenge: ActiveChallenge,
        newTargetAmount: Int,
        effectiveFrom: LocalDate
    ) {
        val newDailyLimit = (newTargetAmount / challenge.totalDays).coerceAtLeast(0)
        val updated = challenge.copy(
            targetAmount = newTargetAmount,
            dailyLimit = newDailyLimit,
            editCount = challenge.editCount + 1,
            dailyLimitOverrides = challenge.dailyLimitOverrides + DailyLimitOverride(effectiveFrom, newDailyLimit)
        )
        _state.update { it.copy(challenges = it.challenges.dropLast(1) + updated) }
    }

    override suspend fun loadRecommendation(): Result<String> {
        if (!ChallengeConfig.USE_SERVER_CHALLENGE) {
            return Result.failure(
                ApiException(code = "SERVER_DISABLED", message = "추천 정보를 사용할 수 없습니다.")
            )
        }
        if (authRepository.currentAuthHeader() == null) return Result.failure(unauthorized())
        return runCatchingNetwork(TAG) {
            val response = apiService.getChallengeRecommendation()
            val data = response.body()?.data
            if (response.isSuccessful && data != null) {
                Result.success(data.message)
            } else {
                Result.failure(response.toApiException("추천 정보를 불러오지 못했습니다."))
            }
        }
    }

    override fun resetForAccount(referenceToday: LocalDate) {
        _fixedDateDraft.value = null
        _state.value = ChallengeState(
            challenges = if (ChallengeConfig.USE_SERVER_CHALLENGE) emptyList() else buildSeedChallenges(referenceToday)
        )
    }

    override fun resetEmpty() {
        _fixedDateDraft.value = null
        _state.value = ChallengeState()
    }
}
