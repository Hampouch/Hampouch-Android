package com.example.hampouch.data.repository

import android.content.Context
import android.util.Log
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import com.example.hampouch.core.config.ChallengeConfig
import com.example.hampouch.core.network.NetworkModule
import com.example.hampouch.domain.model.ActiveChallenge
import com.example.hampouch.domain.model.ChallengeProgress
import com.example.hampouch.domain.model.DailyLimitOverride
import com.example.hampouch.domain.model.DailyRecordStatus
import com.example.hampouch.domain.model.OnboardingRequest
import com.example.hampouch.domain.model.ApiException
import com.example.hampouch.data.remote.dto.ApiErrorBody
import com.example.hampouch.data.remote.dto.ChallengeCreateRequest
import com.example.hampouch.data.remote.dto.ChallengeCurrentData
import com.example.hampouch.data.remote.dto.ChallengeFocusCategoriesRequest
import com.example.hampouch.data.remote.dto.ChallengeHistoryItemDto
import com.google.gson.Gson
import java.time.LocalDate
import java.time.temporal.ChronoUnit
import kotlinx.coroutines.CancellationException
import retrofit2.Response

private const val TAG = "ChallengeRepository"

object ChallengeRepository {

    private const val CHALLENGE_TOTAL_DAYS = 14
    private const val CHALLENGE_DAILY_LIMIT = 20_000
    private const val CHALLENGE_SAVED_AMOUNT = 21_400
    private const val CHALLENGE_STREAK_DAYS = 4

    private const val PREVIOUS_CHALLENGE_TOTAL_DAYS = 7
    private const val PREVIOUS_CHALLENGE_DAILY_LIMIT = 20_000

    private const val DEFAULT_ONE_OFF_DAYS = 30

    private val weakCategoryLabels: Map<String, String> = mapOf(
        "delivery" to "배달",
        "dining_out" to "외식",
        "convenience" to "편의점",
        "cafe" to "카페",
        "snack" to "간식",
        "mart" to "장보기",
        "drink" to "술자리"
    )

    private lateinit var appContext: Context
    private val authRepository: AuthRepository get() = AuthRepository.getInstance(appContext)

    fun attach(context: Context) {
        appContext = context.applicationContext
    }

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

    private val challengesState = mutableStateOf(
        if (ChallengeConfig.USE_SERVER_CHALLENGE) emptyList() else buildSeedChallenges(LocalDate.now())
    )

    private var noRecordDates: Set<LocalDate> by mutableStateOf(emptySet())

    fun markNoRecord(date: LocalDate) {
        noRecordDates = noRecordDates + date
    }

    fun clearNoRecord(date: LocalDate) {
        noRecordDates = noRecordDates - date
    }

    val challenges: List<ActiveChallenge>
        get() = challengesState.value

    val activeChallenge: ActiveChallenge?
        get() = challenges.lastOrNull()

    val hasOngoingChallenge: Boolean
        get() = activeChallenge?.let { !LocalDate.now().isAfter(it.effectivePeriodEnd) } ?: false

    var challengeEndAcknowledged: Boolean by mutableStateOf(false)
        private set

    var hasVisitedExpenseEditAfterEnd: Boolean by mutableStateOf(false)
        private set

    fun isChallengeJustEnded(referenceToday: LocalDate): Boolean =
        activeChallenge?.let { !challengeEndAcknowledged && referenceToday.isAfter(it.periodEnd) } ?: false

    fun markVisitedExpenseEditAfterEnd() {
        hasVisitedExpenseEditAfterEnd = true
    }

    /** [id]가 이미 캐시에 있으면 교체하고, 없으면 추가한 뒤 periodStart 오름차순으로 정렬한다(마지막 항목이 곧 [activeChallenge]가 되도록). */
    private fun upsertChallenge(entry: ActiveChallenge) {
        val existingIndex = challenges.indexOfFirst { it.id == entry.id }
        val updated = if (existingIndex >= 0) {
            challenges.toMutableList().also { it[existingIndex] = entry }
        } else {
            challenges + entry
        }
        challengesState.value = updated.sortedBy { it.periodStart }
    }

    /** 서버가 "진행 중인 챌린지 없음"을 알려오면, 로컬에 남아있는 미종료 항목(이전 조회의 잔재)을 지운다. */
    private fun clearActiveChallenge() {
        val today = LocalDate.now()
        challengesState.value = challenges.filterNot { it.abandonedDate == null && !today.isAfter(it.effectivePeriodEnd) }
    }

    private fun weakCategoryLabelFor(id: String): String = weakCategoryLabels[id] ?: id

    /**
     * 날짜 고정 모드에서는 지정한 날짜가 "다음 시작점"의 기준일 뿐, 챌린지는 항상 오늘 즉시 시작한다.
     * 기준일이 아직 오지 않았다면 그 전날까지를 첫 주기로 잡고, 이후 acknowledge 시점마다
     * (직전 종료일 + 1일)을 다음 시작일로 삼아 매월 같은 날짜로 반복한다.
     */
    private fun resolvePeriod(request: OnboardingRequest, referenceToday: LocalDate): Triple<LocalDate, LocalDate, Boolean> {
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

    private suspend fun requireAuthHeader(): Result<String> {
        val header = authRepository.currentAuthHeader()
        return if (header != null) {
            Result.success(header)
        } else {
            Result.failure(ApiException(code = "AUTH_UNAUTHORIZED", message = "인증이 필요합니다."))
        }
    }

    private fun errorFrom(response: Response<*>, fallbackMessage: String): ApiException {
        val error = response.errorBody()?.string()?.let {
            runCatching { Gson().fromJson(it, ApiErrorBody::class.java) }.getOrNull()
        }
        return ApiException(code = error?.code ?: "UNKNOWN", message = error?.message ?: fallbackMessage)
    }

    private inline fun <T> runCatchingNetwork(action: () -> Result<T>): Result<T> = try {
        action()
    } catch (e: CancellationException) {
        throw e
    } catch (e: ApiException) {
        Log.e(TAG, "챌린지 API 오류", e)
        Result.failure(e)
    } catch (e: Exception) {
        Log.e(TAG, "챌린지 네트워크 오류", e)
        Result.failure(ApiException(code = "NETWORK_ERROR", message = "인터넷 연결을 확인해주세요."))
    }

    /** GET /api/challenges/current — 진행 중 챌린지(또는 휴식 중이면 없음)를 조회해 로컬 캐시에 반영한다. */
    suspend fun loadCurrentChallenge(): Result<Unit> {
        if (!ChallengeConfig.USE_SERVER_CHALLENGE) return Result.success(Unit)
        val header = requireAuthHeader().getOrElse { return Result.failure(it) }
        return runCatchingNetwork {
            val response = NetworkModule.apiService.getCurrentChallenge(header)
            if (response.isSuccessful) {
                applyCurrent(response.body()?.data)
                Result.success(Unit)
            } else {
                val error = errorFrom(response, "진행 중인 챌린지 조회에 실패했습니다.")
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
        if (summary == null) {
            clearActiveChallenge()
            return
        }
        if (summary.status == "VOID") {
            clearActiveChallenge()
            return
        }
        val periodStart = LocalDate.parse(summary.startDate)
        val periodEnd = LocalDate.parse(summary.endDate)
        upsertChallenge(
            ActiveChallenge(
                id = summary.id.toString(),
                totalDays = summary.durationDays,
                periodStart = periodStart,
                periodEnd = periodEnd,
                dailyLimit = summary.dailyLimit,
                targetAmount = summary.budgetTotal,
                savedAmount = data.progress?.savedAmountSoFar ?: 0,
                streakDays = data.progress?.currentStreak ?: 0,
                editCount = 0,
                remoteStatus = summary.status
            )
        )
    }

    /** GET /api/challenges/history — 종료된(SUCCESS/FAIL) 챌린지 목록을 조회해 로컬 캐시에 반영한다. */
    suspend fun loadHistory(): Result<Unit> {
        if (!ChallengeConfig.USE_SERVER_CHALLENGE) return Result.success(Unit)
        val header = requireAuthHeader().getOrElse { return Result.failure(it) }
        return runCatchingNetwork {
            val response = NetworkModule.apiService.getChallengeHistory(header)
            val data = response.body()?.data
            if (response.isSuccessful && data != null) {
                data.items.forEach { item -> upsertChallenge(item.toActiveChallenge()) }
                Result.success(Unit)
            } else {
                Result.failure(errorFrom(response, "지난 챌린지 목록을 불러오지 못했습니다."))
            }
        }
    }

    private fun ChallengeHistoryItemDto.toActiveChallenge(): ActiveChallenge {
        val start = LocalDate.parse(startDate)
        val end = LocalDate.parse(endDate)
        return ActiveChallenge(
            id = challengeId.toString(),
            totalDays = durationDays,
            periodStart = start,
            periodEnd = end,
            dailyLimit = if (durationDays > 0) budgetTotal / durationDays else 0,
            targetAmount = budgetTotal,
            savedAmount = savedAmount,
            streakDays = 0,
            editCount = 0,
            remoteStatus = status
        )
    }

    /** GET /api/challenges/{id}/result — 종료된 챌린지의 서버 확정 성패(status)·최종 종료 시각(closedAt)을 캐시에 덧입힌다. */
    suspend fun loadResult(challengeId: String): Result<Unit> {
        if (!ChallengeConfig.USE_SERVER_CHALLENGE) return Result.success(Unit)
        val id = challengeId.toLongOrNull() ?: return Result.success(Unit)
        val header = requireAuthHeader().getOrElse { return Result.failure(it) }
        return runCatchingNetwork {
            val response = NetworkModule.apiService.getChallengeResult(header, id)
            val data = response.body()?.data
            if (response.isSuccessful && data != null) {
                challenges.find { it.id == challengeId }?.let { existing ->
                    upsertChallenge(existing.copy(remoteStatus = data.status, closedAt = data.closedAt))
                }
                Result.success(Unit)
            } else {
                Result.failure(errorFrom(response, "챌린지 결과를 불러오지 못했습니다."))
            }
        }
    }

    /** PUT /api/challenges/{id}/focus-categories — 진행 중 챌린지의 집중 카테고리를 전체 교체한다. */
    suspend fun updateFocusCategories(categories: List<String>): Result<List<String>> {
        if (!ChallengeConfig.USE_SERVER_CHALLENGE) return Result.success(categories)
        val id = activeChallenge?.id?.toLongOrNull()
            ?: return Result.failure(ApiException(code = "CHALLENGE_NOT_IN_PROGRESS", message = "진행 중인 챌린지가 없습니다."))
        val header = requireAuthHeader().getOrElse { return Result.failure(it) }
        return runCatchingNetwork {
            val response = NetworkModule.apiService.updateChallengeFocusCategories(
                header, id, ChallengeFocusCategoriesRequest(categories)
            )
            val data = response.body()?.data
            if (response.isSuccessful && data != null) {
                Result.success(data.categories)
            } else {
                Result.failure(errorFrom(response, "집중 카테고리 수정에 실패했습니다."))
            }
        }
    }

    private fun applyLocalStartNewChallenge(request: OnboardingRequest, referenceToday: LocalDate): ActiveChallenge {
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
        challengesState.value = challenges + newChallenge
        challengeEndAcknowledged = false
        hasVisitedExpenseEditAfterEnd = false
        return newChallenge
    }

    /** POST /api/challenges — 새 챌린지를 시작한다. 서버 모드에서는 실제로 생성하고, 목데이터 모드에서는 로컬에만 추가한다. */
    suspend fun startNewChallenge(request: OnboardingRequest, referenceToday: LocalDate = LocalDate.now()): Result<ActiveChallenge> {
        if (!ChallengeConfig.USE_SERVER_CHALLENGE) {
            return Result.success(applyLocalStartNewChallenge(request, referenceToday))
        }
        val (periodStart, periodEnd, repeatMonthly) = resolvePeriod(request, referenceToday)
        val totalDays = ChronoUnit.DAYS.between(periodStart, periodEnd).toInt() + 1
        val budgetTotal = (request.totalTargetAmount ?: 0).coerceAtLeast(0)
        val header = requireAuthHeader().getOrElse { return Result.failure(it) }
        return runCatchingNetwork {
            val response = NetworkModule.apiService.createChallenge(
                header,
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
                challengeEndAcknowledged = false
                hasVisitedExpenseEditAfterEnd = false
                Result.success(newChallenge)
            } else {
                Result.failure(errorFrom(response, "챌린지 생성에 실패했습니다."))
            }
        }
    }

    fun challengeFor(date: LocalDate): ActiveChallenge? =
        challenges.firstOrNull { !date.isBefore(it.periodStart) && !date.isAfter(it.effectivePeriodEnd) }

    private fun applyAbandon(current: ActiveChallenge, referenceToday: LocalDate, remoteStatus: String? = null) {
        val updated = current.copy(abandonedDate = referenceToday, remoteStatus = remoteStatus ?: current.remoteStatus)
        challengesState.value = challenges.dropLast(1) + updated
        challengeEndAcknowledged = true
        hasVisitedExpenseEditAfterEnd = false
    }

    /** POST /api/challenges/{id}/give-up — 진행 중 챌린지를 즉시 FAIL로 종료한다. */
    suspend fun abandonChallenge(referenceToday: LocalDate = LocalDate.now()): Result<Unit> {
        val current = activeChallenge ?: return Result.success(Unit)
        if (!ChallengeConfig.USE_SERVER_CHALLENGE) {
            applyAbandon(current, referenceToday)
            return Result.success(Unit)
        }
        val id = current.id.toLongOrNull() ?: run {
            applyAbandon(current, referenceToday)
            return Result.success(Unit)
        }
        val header = requireAuthHeader().getOrElse { return Result.failure(it) }
        return runCatchingNetwork {
            val response = NetworkModule.apiService.giveUpChallenge(header, id)
            val data = response.body()?.data
            if (response.isSuccessful && data != null) {
                applyAbandon(current, referenceToday, remoteStatus = data.status)
                Result.success(Unit)
            } else {
                Result.failure(errorFrom(response, "중도 포기 처리에 실패했습니다."))
            }
        }
    }

    /** 목데이터 모드에서만: 월간 반복 챌린지는 확인 즉시 다음 달 챌린지를 로컬로 이어 붙인다(서버에 대응 API 없음). */
    private fun applyAcknowledge(ended: ActiveChallenge) {
        if (ended.repeatMonthly && !ChallengeConfig.USE_SERVER_CHALLENGE) {
            val nextStart = ended.periodEnd.plusDays(1)
            val nextEnd = nextStart.plusMonths(1).minusDays(1)
            val nextTotalDays = ChronoUnit.DAYS.between(nextStart, nextEnd).toInt() + 1
            val nextChallenge = ActiveChallenge(
                id = "challenge_${System.currentTimeMillis()}",
                totalDays = nextTotalDays,
                periodStart = nextStart,
                periodEnd = nextEnd,
                dailyLimit = ended.dailyLimit,
                targetAmount = ended.targetAmount,
                savedAmount = 0,
                streakDays = 0,
                editCount = 0,
                repeatMonthly = true
            )
            challengesState.value = challenges + nextChallenge
            hasVisitedExpenseEditAfterEnd = false
        } else {
            challengeEndAcknowledged = true
        }
    }

    /** POST /api/challenges/{id}/close — 결과 팝업의 '챌린지 종료' 선택을 서버에 반영한 뒤 로컬 상태를 확정한다. */
    suspend fun acknowledgeChallengeEnd(): Result<Unit> {
        val ended = activeChallenge ?: return Result.success(Unit)
        if (ChallengeConfig.USE_SERVER_CHALLENGE) {
            val id = ended.id.toLongOrNull()
            if (id != null) {
                val header = requireAuthHeader().getOrElse { return Result.failure(it) }
                val result = runCatchingNetwork {
                    val response = NetworkModule.apiService.closeChallenge(header, id)
                    val data = response.body()?.data
                    if (response.isSuccessful && data != null) {
                        upsertChallenge(ended.copy(remoteStatus = data.status, closedAt = data.closedAt))
                        Result.success(Unit)
                    } else {
                        Result.failure(errorFrom(response, "챌린지 종료 처리에 실패했습니다."))
                    }
                }
                if (result.isFailure) return result
            }
        }
        applyAcknowledge(ended)
        return Result.success(Unit)
    }

    fun updateTargetAmount(newTargetAmount: Int, effectiveFrom: LocalDate = LocalDate.now()) {
        val current = activeChallenge ?: return
        val newDailyLimit = (newTargetAmount / current.totalDays).coerceAtLeast(0)
        val updated = current.copy(
            targetAmount = newTargetAmount,
            dailyLimit = newDailyLimit,
            editCount = current.editCount + 1,
            dailyLimitOverrides = current.dailyLimitOverrides + DailyLimitOverride(effectiveFrom, newDailyLimit)
        )
        challengesState.value = challenges.dropLast(1) + updated
    }

    fun elapsedDays(referenceToday: LocalDate, challenge: ActiveChallenge): List<LocalDate> {
        val trackedEnd = if (referenceToday.isBefore(challenge.effectivePeriodEnd)) referenceToday else challenge.effectivePeriodEnd
        return generateSequence(challenge.periodStart) { it.plusDays(1) }
            .takeWhile { !it.isAfter(trackedEnd) }
            .toList()
    }

    fun computeProgress(
        referenceToday: LocalDate,
        challenge: ActiveChallenge,
        spentOnDate: (LocalDate) -> Int
    ): ChallengeProgress {
        val days = elapsedDays(referenceToday, challenge)
        fun balanceOn(date: LocalDate) = challenge.dailyLimitOn(date) - spentOnDate(date)
        fun isSuccess(date: LocalDate) = date !in noRecordDates && balanceOn(date) >= 0

        val savedAmount = days.sumOf { balanceOn(it) }
        var streakDays = 0
        for (day in days.asReversed()) {
            if (isSuccess(day)) streakDays++ else break
        }
        val dailyRecords = days.associateWith { day ->
            if (isSuccess(day)) DailyRecordStatus.SUCCESS else DailyRecordStatus.FAIL
        }
        return ChallengeProgress(savedAmount = savedAmount, streakDays = streakDays, dailyRecords = dailyRecords)
    }

    fun resetForAccount(referenceToday: LocalDate = LocalDate.now()) {
        challengesState.value = if (ChallengeConfig.USE_SERVER_CHALLENGE) emptyList() else buildSeedChallenges(referenceToday)
        challengeEndAcknowledged = false
        hasVisitedExpenseEditAfterEnd = false
        noRecordDates = emptySet()
    }

    fun resetEmpty() {
        challengesState.value = emptyList()
        challengeEndAcknowledged = false
        hasVisitedExpenseEditAfterEnd = false
        noRecordDates = emptySet()
    }
}
