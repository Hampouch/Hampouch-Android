package com.example.hampouch.data.repository

import android.content.Context
import android.net.Uri
import com.example.hampouch.core.config.ExpenseConfig
import com.example.hampouch.data.local.ExpenseMockDataSource
import com.example.hampouch.data.remote.ApiService
import com.example.hampouch.data.remote.dto.ExpenseCreateRequest
import com.example.hampouch.data.remote.dto.ExpenseDaySummaryItemData
import com.example.hampouch.data.remote.dto.ExpenseDetailData
import com.example.hampouch.data.remote.dto.ExpensePeriodSummaryData
import com.example.hampouch.data.remote.dto.ExpensePhotoConfirmRequest
import com.example.hampouch.data.remote.dto.ExpensePhotoPresignRequest
import com.example.hampouch.data.remote.dto.ExpenseTagAnalysisItemData
import com.example.hampouch.data.remote.dto.ExpenseUpdateRequest
import com.example.hampouch.data.remote.runCatchingNetwork
import com.example.hampouch.data.remote.toApiException
import com.example.hampouch.data.remote.unauthorized
import com.example.hampouch.domain.model.AmountBreakdownItem
import com.example.hampouch.domain.model.ApiException
import com.example.hampouch.domain.model.DailyAmount
import com.example.hampouch.domain.model.ExpenseAnalysisEtcId
import com.example.hampouch.domain.model.ExpenseAnalysisSummary
import com.example.hampouch.domain.model.ExpenseCategoryIds
import com.example.hampouch.domain.model.ExpensePeriodSummary
import com.example.hampouch.domain.model.ExpenseReasonIds
import com.example.hampouch.domain.model.ExpenseRecord
import com.example.hampouch.domain.model.ExpenseTagAnalysisResult
import com.example.hampouch.domain.model.ExpenseTrendResult
import com.example.hampouch.domain.model.ExpenseWeekdayOrder
import com.example.hampouch.domain.model.MonthlyTotal
import com.example.hampouch.domain.model.WeekdayAmount
import com.example.hampouch.domain.model.categoryBreakdown
import com.example.hampouch.domain.model.inPeriod
import com.example.hampouch.domain.model.monthlyTotals
import com.example.hampouch.domain.model.reasonBreakdown
import com.example.hampouch.domain.model.recordsForCategory
import com.example.hampouch.domain.model.recordsForReason
import com.example.hampouch.domain.model.weekdayBreakdown
import com.example.hampouch.domain.repository.ChallengeRepository
import com.example.hampouch.domain.repository.ExpenseRepository
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.YearMonth
import java.time.temporal.ChronoUnit
import java.util.UUID
import javax.inject.Inject
import javax.inject.Singleton

private const val TAG = "ExpenseRepository"

@Singleton
class ExpenseRepositoryImpl @Inject constructor(
    @ApplicationContext private val context: Context,
    private val apiService: ApiService,
    private val okHttpClient: OkHttpClient,
    private val authRepository: AuthRepository,
    private val challengeRepository: ChallengeRepository,
    private val mockDataSource: ExpenseMockDataSource
) : ExpenseRepository {

    private val _records = MutableStateFlow(
        if (ExpenseConfig.USE_SERVER_EXPENSE) emptyMap() else mockDataSource.initialRecords()
    )
    override val records: StateFlow<Map<String, ExpenseRecord>> = _records.asStateFlow()

    override fun byId(id: String): ExpenseRecord? = _records.value[id]

    override fun recordsForDate(date: LocalDate): List<ExpenseRecord> =
        _records.value.values.filter { it.date == date }.sortedBy { it.id }

    private fun upsert(record: ExpenseRecord) {
        _records.value = _records.value + (record.id to record)
        challengeRepository.clearNoRecord(record.date)
    }

    private fun removeLocal(id: String) {
        _records.value = _records.value - id
    }

    override fun markNoSpending(date: LocalDate) {
        upsert(ExpenseRecord(id = UUID.randomUUID().toString(), date = date, amount = 0))
    }

    override fun resetForAccount() {
        _records.value = if (ExpenseConfig.USE_SERVER_EXPENSE) emptyMap() else mockDataSource.initialRecords()
    }

    // ----- 서버 enum ↔ 앱 내부 id 매핑 -----

    private val localCategoryToServer: Map<String, String> = mapOf(
        "delivery" to "DELIVERY",
        "dining_out" to "DINING_OUT",
        "convenience" to "CONVENIENCE_STORE",
        "cafe" to "CAFE",
        "mart" to "GROCERY",
        "snack" to "DESSERT",
        "drink" to "DRINKING"
    )
    private val serverCategoryToLocal: Map<String, String> =
        localCategoryToServer.entries.associate { (local, server) -> server to local }

    private val localReasonToServer: Map<String, String> = mapOf(
        "stress" to "STRESS",
        "reward" to "COMPENSATION",
        "lazy" to "CONVENIENCE",
        "craving" to "IMPULSE"
    )
    private val localReasonFromServer: Map<String, String> =
        localReasonToServer.entries.associate { (local, server) -> server to local }

    private fun categoryRequestPair(record: ExpenseRecord): Pair<String, String?> {
        val id = record.categoryId
        return when {
            id != null && id != ExpenseAnalysisEtcId -> (localCategoryToServer[id] ?: "ETC") to null
            record.customCategoryName != null -> "ETC" to record.customCategoryName
            else -> "ETC" to null
        }
    }

    private fun emotionRequestPair(record: ExpenseRecord): Pair<String, String?> {
        val id = record.reasonId
        return when {
            id != null -> (localReasonToServer[id] ?: "ETC") to null
            record.customReason != null -> "ETC" to record.customReason
            else -> "ETC" to null
        }
    }

    private fun categoryFieldsFromServer(category: String, customCategory: String?): Pair<String?, String?> =
        if (category == "ETC") null to customCategory else serverCategoryToLocal[category] to null

    private fun reasonFieldsFromServer(emotion: String, customEmotion: String?): Pair<String?, String?> =
        if (emotion == "ETC") null to customEmotion else localReasonFromServer[emotion] to null

    private fun isRemoteUrl(uri: String): Boolean = uri.startsWith("http://") || uri.startsWith("https://")

    private fun sundayOfWeek(date: LocalDate): LocalDate = date.minusDays((date.dayOfWeek.value % 7).toLong())

    // ----- 사진 업로드 -----

    private data class LocalImagePayload(val bytes: ByteArray, val contentType: String)

    private suspend fun readLocalImage(uriString: String): LocalImagePayload = withContext(Dispatchers.IO) {
        val uri = Uri.parse(uriString)
        val resolver = context.contentResolver
        val bytes = resolver.openInputStream(uri)?.use { it.readBytes() }
            ?: throw ApiException("EXPENSE_IMAGE_UPLOAD_FAILED", "이미지를 읽을 수 없습니다.")
        LocalImagePayload(bytes = bytes, contentType = resolver.getType(uri) ?: "image/jpeg")
    }

    private suspend fun uploadNewImage(header: String, expenseId: Long?, localUri: String): Result<String> =
        runCatchingNetwork(TAG) {
            val payload = readLocalImage(localUri)
            val response = apiService.presignExpensePhoto(
                header,
                expenseId,
                ExpensePhotoPresignRequest(contentType = payload.contentType, size = payload.bytes.size.toLong())
            )
            val data = response.body()?.data
            if (!response.isSuccessful || data == null) {
                return@runCatchingNetwork Result.failure(response.toApiException("이미지 업로드에 실패했습니다."))
            }
            withContext(Dispatchers.IO) {
                val body = payload.bytes.toRequestBody(payload.contentType.toMediaTypeOrNull())
                val request = Request.Builder().url(data.uploadUrl).put(body).build()
                okHttpClient.newCall(request).execute().use { httpResponse ->
                    if (!httpResponse.isSuccessful) {
                        throw ApiException("EXPENSE_IMAGE_UPLOAD_FAILED", "이미지 업로드에 실패했습니다.")
                    }
                }
            }
            Result.success(data.imageKey)
        }

    private suspend fun syncPhoto(
        header: String,
        expenseId: Long,
        previousUri: String?,
        newUri: String?
    ): Result<Unit> {
        if (newUri == previousUri) return Result.success(Unit)
        if (newUri == null) {
            return runCatchingNetwork(TAG) {
                val response = apiService.deleteExpensePhoto(header, expenseId)
                if (response.isSuccessful) {
                    Result.success(Unit)
                } else {
                    Result.failure(response.toApiException("사진 삭제에 실패했습니다."))
                }
            }
        }
        if (isRemoteUrl(newUri)) return Result.success(Unit)
        val imageKey = uploadNewImage(header, expenseId, newUri).getOrElse { return Result.failure(it) }
        return runCatchingNetwork(TAG) {
            val response = apiService.confirmExpensePhoto(header, expenseId, ExpensePhotoConfirmRequest(imageKey))
            if (response.isSuccessful) {
                Result.success(Unit)
            } else {
                Result.failure(response.toApiException("사진 반영에 실패했습니다."))
            }
        }
    }

    // ----- DTO → 도메인 매핑 -----

    private fun ExpenseDetailData.toExpenseRecord(): ExpenseRecord {
        val (categoryId, customCategoryName) = categoryFieldsFromServer(category, customCategory)
        val (reasonId, customReason) = reasonFieldsFromServer(emotion, customEmotion)
        return ExpenseRecord(
            id = expenseId.toString(),
            date = LocalDate.parse(date),
            amount = price,
            categoryId = categoryId,
            customCategoryName = customCategoryName,
            expenseName = name.ifBlank { null },
            reasonId = reasonId,
            customReason = customReason,
            memo = memo,
            photoUris = listOfNotNull(imageUrl)
        )
    }

    private fun ExpenseDaySummaryItemData.toExpenseRecord(date: LocalDate): ExpenseRecord {
        val (categoryId, customCategoryName) = categoryFieldsFromServer(category, categoryLabel)
        val (reasonId, customReason) = reasonFieldsFromServer(emotion, emotionLabel)
        return ExpenseRecord(
            id = expenseId.toString(),
            date = date,
            amount = price,
            categoryId = categoryId,
            customCategoryName = customCategoryName,
            expenseName = name.ifBlank { null },
            reasonId = reasonId,
            customReason = customReason
        )
    }

    private fun ExpenseTagAnalysisItemData.toExpenseRecord(): ExpenseRecord {
        val (categoryId, customCategoryName) = categoryFieldsFromServer(category, categoryLabel)
        return ExpenseRecord(
            id = expenseId.toString(),
            date = LocalDate.parse(date),
            amount = price,
            categoryId = categoryId,
            customCategoryName = customCategoryName,
            expenseName = name.ifBlank { null },
            reasonId = null,
            customReason = emotionLabel
        )
    }

    private fun ExpensePeriodSummaryData.toPeriodSummary(
        fallbackStart: LocalDate,
        fallbackEnd: LocalDate
    ): ExpensePeriodSummary = ExpensePeriodSummary(
        periodStart = periodStart?.let { LocalDate.parse(it) } ?: fallbackStart,
        periodEnd = periodEnd?.let { LocalDate.parse(it) } ?: fallbackEnd,
        totalAmount = totalAmount,
        dailyAverage = dailyAverage,
        dailyBreakdown = dailyBreakdown.map { DailyAmount(LocalDate.parse(it.date), it.amount) }
    )

    // ----- 조회/변경 -----

    override suspend fun createExpense(record: ExpenseRecord): Result<ExpenseRecord> {
        if (!ExpenseConfig.USE_SERVER_EXPENSE) {
            upsert(record)
            return Result.success(record)
        }
        val header = authRepository.currentAuthHeader() ?: return Result.failure(unauthorized())
        return runCatchingNetwork(TAG) {
            val localUri = record.photoUris.firstOrNull()
            val imageKey = if (localUri != null && !isRemoteUrl(localUri)) {
                uploadNewImage(header, expenseId = null, localUri = localUri).getOrElse {
                    return@runCatchingNetwork Result.failure(it)
                }
            } else {
                null
            }
            val (category, customCategory) = categoryRequestPair(record)
            val (emotion, customEmotion) = emotionRequestPair(record)
            val response = apiService.createExpense(
                header,
                ExpenseCreateRequest(
                    name = record.expenseName.orEmpty(),
                    price = record.amount,
                    category = category,
                    customCategory = customCategory,
                    emotion = emotion,
                    customEmotion = customEmotion,
                    date = record.date.toString(),
                    memo = record.memo,
                    imageKey = imageKey
                )
            )
            val data = response.body()?.data
            if (response.isSuccessful && data != null) {
                val saved = record.copy(id = data.expenseId.toString())
                upsert(saved)
                Result.success(saved)
            } else {
                Result.failure(response.toApiException("지출 입력에 실패했습니다."))
            }
        }
    }

    override suspend fun updateExpense(record: ExpenseRecord): Result<ExpenseRecord> {
        if (!ExpenseConfig.USE_SERVER_EXPENSE) {
            upsert(record)
            return Result.success(record)
        }
        val id = record.id.toLongOrNull()
            ?: return Result.failure(ApiException("EXPENSE_NOT_FOUND", "지출 내역을 찾을 수 없습니다."))
        val header = authRepository.currentAuthHeader() ?: return Result.failure(unauthorized())
        val previousPhotoUri = _records.value[record.id]?.photoUris?.firstOrNull()
        return runCatchingNetwork(TAG) {
            val (category, customCategory) = categoryRequestPair(record)
            val (emotion, customEmotion) = emotionRequestPair(record)
            val response = apiService.updateExpense(
                header, id,
                ExpenseUpdateRequest(
                    name = record.expenseName.orEmpty(),
                    price = record.amount,
                    category = category,
                    customCategory = customCategory,
                    emotion = emotion,
                    customEmotion = customEmotion,
                    date = record.date.toString(),
                    memo = record.memo
                )
            )
            if (!response.isSuccessful || response.body()?.data == null) {
                return@runCatchingNetwork Result.failure(response.toApiException("지출 내역 수정에 실패했습니다."))
            }
            syncPhoto(header, id, previousUri = previousPhotoUri, newUri = record.photoUris.firstOrNull())
                .onFailure { return@runCatchingNetwork Result.failure(it) }
            upsert(record)
            Result.success(record)
        }
    }

    override suspend fun deleteExpense(id: String): Result<Unit> {
        if (!ExpenseConfig.USE_SERVER_EXPENSE) {
            removeLocal(id)
            return Result.success(Unit)
        }
        val expenseId = id.toLongOrNull()
            ?: return Result.failure(ApiException("EXPENSE_NOT_FOUND", "지출 내역을 찾을 수 없습니다."))
        val header = authRepository.currentAuthHeader() ?: return Result.failure(unauthorized())
        return runCatchingNetwork(TAG) {
            val response = apiService.deleteExpense(header, expenseId)
            if (response.isSuccessful) {
                removeLocal(id)
                Result.success(Unit)
            } else {
                Result.failure(response.toApiException("지출 내역 삭제에 실패했습니다."))
            }
        }
    }

    override suspend fun loadExpenseDetail(id: String): Result<ExpenseRecord> {
        if (!ExpenseConfig.USE_SERVER_EXPENSE) {
            val record = _records.value[id]
                ?: return Result.failure(ApiException("EXPENSE_NOT_FOUND", "지출 내역을 찾을 수 없습니다."))
            return Result.success(record)
        }
        val expenseId = id.toLongOrNull()
            ?: return Result.failure(ApiException("EXPENSE_NOT_FOUND", "지출 내역을 찾을 수 없습니다."))
        val header = authRepository.currentAuthHeader() ?: return Result.failure(unauthorized())
        return runCatchingNetwork(TAG) {
            val response = apiService.getExpenseDetail(header, expenseId)
            val data = response.body()?.data
            if (response.isSuccessful && data != null) {
                val record = data.toExpenseRecord()
                upsert(record)
                Result.success(record)
            } else {
                Result.failure(response.toApiException("지출 내역을 불러오지 못했습니다."))
            }
        }
    }

    override suspend fun loadDay(date: LocalDate): Result<Unit> {
        if (!ExpenseConfig.USE_SERVER_EXPENSE) return Result.success(Unit)
        val header = authRepository.currentAuthHeader() ?: return Result.failure(unauthorized())
        return runCatchingNetwork(TAG) {
            val response = apiService.getExpenseDay(header, date.toString())
            val data = response.body()?.data
            if (response.isSuccessful && data != null) {
                val fetched = data.expenses.associate { it.expenseId.toString() to it.toExpenseRecord(date) }
                val withoutStaleDate = _records.value.filterValues { it.date != date }
                _records.value = withoutStaleDate + fetched
                Result.success(Unit)
            } else {
                Result.failure(response.toApiException("지출 목록을 불러오지 못했습니다."))
            }
        }
    }

    /** 목데이터 모드에서 로컬 캐시로부터 기간 요약을 계산한다. */
    private fun localPeriodSummary(start: LocalDate, end: LocalDate): ExpensePeriodSummary {
        val records = _records.value.values.toList().inPeriod(start, end)
        val days = (ChronoUnit.DAYS.between(start, end).toInt() + 1).coerceAtLeast(1)
        val total = records.sumOf { it.amount }
        val byDate = records.groupBy { it.date }.mapValues { (_, r) -> r.sumOf { it.amount } }
        return ExpensePeriodSummary(
            periodStart = start,
            periodEnd = end,
            totalAmount = total,
            dailyAverage = total / days,
            dailyBreakdown = byDate.map { (date, amount) -> DailyAmount(date, amount) }.sortedBy { it.date }
        )
    }

    override suspend fun loadWeekSummary(standardDate: LocalDate): Result<ExpensePeriodSummary> {
        if (!ExpenseConfig.USE_SERVER_EXPENSE) {
            val weekStart = sundayOfWeek(standardDate)
            return Result.success(localPeriodSummary(weekStart, weekStart.plusDays(6)))
        }
        val header = authRepository.currentAuthHeader() ?: return Result.failure(unauthorized())
        return runCatchingNetwork(TAG) {
            val response = apiService.getExpenseWeekSummary(header, standardDate.toString())
            val data = response.body()?.data
            if (response.isSuccessful && data != null) {
                val weekStart = sundayOfWeek(standardDate)
                Result.success(data.toPeriodSummary(fallbackStart = weekStart, fallbackEnd = weekStart.plusDays(6)))
            } else {
                Result.failure(response.toApiException("주간 기록을 불러오지 못했습니다."))
            }
        }
    }

    override suspend fun loadMonthSummary(standardMonth: YearMonth): Result<ExpensePeriodSummary> {
        if (!ExpenseConfig.USE_SERVER_EXPENSE) {
            return Result.success(
                localPeriodSummary(standardMonth.atDay(1), standardMonth.atEndOfMonth())
            )
        }
        val header = authRepository.currentAuthHeader() ?: return Result.failure(unauthorized())
        return runCatchingNetwork(TAG) {
            val response = apiService.getExpenseMonthSummary(header, standardMonth.toString())
            val data = response.body()?.data
            if (response.isSuccessful && data != null) {
                Result.success(
                    data.toPeriodSummary(
                        fallbackStart = standardMonth.atDay(1),
                        fallbackEnd = standardMonth.atEndOfMonth()
                    )
                )
            } else {
                Result.failure(response.toApiException("월간 기록을 불러오지 못했습니다."))
            }
        }
    }

    override suspend fun loadAnalysis(periodStart: LocalDate, periodEnd: LocalDate): Result<ExpenseAnalysisSummary> {
        if (!ExpenseConfig.USE_SERVER_EXPENSE) {
            val records = _records.value.values.toList().inPeriod(periodStart, periodEnd)
            return Result.success(
                ExpenseAnalysisSummary(
                    periodStart = periodStart,
                    periodEnd = periodEnd,
                    totalAmount = records.sumOf { it.amount },
                    categoryBreakdown = records.categoryBreakdown(),
                    reasonBreakdown = records.reasonBreakdown(),
                    weekdayBreakdown = records.weekdayBreakdown(),
                    weekdayInsight = null,
                    pouchInsight = null
                )
            )
        }
        val header = authRepository.currentAuthHeader() ?: return Result.failure(unauthorized())
        return runCatchingNetwork(TAG) {
            val response = apiService.getExpenseAnalysis(header, periodStart.toString(), periodEnd.toString())
            val data = response.body()?.data
            if (response.isSuccessful && data != null) {
                val categoryByLocalId = data.categoryBreakdown
                    .associateBy { serverCategoryToLocal[it.category] ?: ExpenseAnalysisEtcId }
                val categoryItems = ExpenseCategoryIds.map { id ->
                    val entry = categoryByLocalId[id]
                    AmountBreakdownItem(id, entry?.amount ?: 0, entry?.ratio ?: 0)
                }
                val reasonByLocalId = data.emotionBreakdown
                    .associateBy { localReasonFromServer[it.emotion] ?: ExpenseAnalysisEtcId }
                val reasonItems = ExpenseReasonIds.map { id ->
                    val entry = reasonByLocalId[id]
                    AmountBreakdownItem(id, entry?.amount ?: 0, entry?.ratio ?: 0)
                }
                val weekdayByDay = data.weekdayBreakdown.associateBy { DayOfWeek.valueOf(it.dayOfWeek) }
                val weekdayItems = ExpenseWeekdayOrder.map { day ->
                    WeekdayAmount(day, weekdayByDay[day]?.amount ?: 0)
                }
                Result.success(
                    ExpenseAnalysisSummary(
                        periodStart = LocalDate.parse(data.periodStart),
                        periodEnd = LocalDate.parse(data.periodEnd),
                        totalAmount = data.totalAmount,
                        categoryBreakdown = categoryItems,
                        reasonBreakdown = reasonItems,
                        weekdayBreakdown = weekdayItems,
                        weekdayInsight = data.weekdayInsight,
                        pouchInsight = data.pouchInsight
                    )
                )
            } else {
                Result.failure(response.toApiException("지출 분석을 불러오지 못했습니다."))
            }
        }
    }

    override suspend fun loadCategoryAnalysis(
        categoryId: String,
        periodStart: LocalDate,
        periodEnd: LocalDate
    ): Result<ExpenseTagAnalysisResult> {
        if (!ExpenseConfig.USE_SERVER_EXPENSE) {
            return Result.success(
                localTagResult(categoryId, periodStart, periodEnd) { it.recordsForCategory(categoryId) }
            )
        }
        val serverCategory = if (categoryId == ExpenseAnalysisEtcId) "ETC" else (localCategoryToServer[categoryId] ?: "ETC")
        val header = authRepository.currentAuthHeader() ?: return Result.failure(unauthorized())
        return runCatchingNetwork(TAG) {
            val response = apiService.getExpenseCategoryAnalysis(
                header, serverCategory, periodStart.toString(), periodEnd.toString()
            )
            val data = response.body()?.data
            if (response.isSuccessful && data != null) {
                Result.success(
                    ExpenseTagAnalysisResult(
                        id = categoryId,
                        totalAmount = data.totalAmount,
                        count = data.count,
                        percent = data.ratio,
                        records = data.items.map { it.toExpenseRecord() }
                    )
                )
            } else {
                Result.failure(response.toApiException("카테고리별 지출을 불러오지 못했습니다."))
            }
        }
    }

    override suspend fun loadEmotionAnalysis(
        reasonId: String,
        periodStart: LocalDate,
        periodEnd: LocalDate
    ): Result<ExpenseTagAnalysisResult> {
        if (!ExpenseConfig.USE_SERVER_EXPENSE) {
            return Result.success(
                localTagResult(reasonId, periodStart, periodEnd) { it.recordsForReason(reasonId) }
            )
        }
        val serverEmotion = if (reasonId == ExpenseAnalysisEtcId) "ETC" else (localReasonToServer[reasonId] ?: "ETC")
        val header = authRepository.currentAuthHeader() ?: return Result.failure(unauthorized())
        return runCatchingNetwork(TAG) {
            val response = apiService.getExpenseEmotionAnalysis(
                header, serverEmotion, periodStart.toString(), periodEnd.toString()
            )
            val data = response.body()?.data
            if (response.isSuccessful && data != null) {
                Result.success(
                    ExpenseTagAnalysisResult(
                        id = reasonId,
                        totalAmount = data.totalAmount,
                        count = data.count,
                        percent = data.ratio,
                        records = data.items.map { it.toExpenseRecord() }
                    )
                )
            } else {
                Result.failure(response.toApiException("이유별 지출을 불러오지 못했습니다."))
            }
        }
    }

    /** 목데이터 모드의 카테고리·이유별 집계. [pick]으로 해당 태그의 내역만 걸러낸다. */
    private fun localTagResult(
        id: String,
        periodStart: LocalDate,
        periodEnd: LocalDate,
        pick: (List<ExpenseRecord>) -> List<ExpenseRecord>
    ): ExpenseTagAnalysisResult {
        val periodRecords = _records.value.values.toList().inPeriod(periodStart, periodEnd)
        val picked = pick(periodRecords)
        val periodTotal = periodRecords.sumOf { it.amount }
        val total = picked.sumOf { it.amount }
        return ExpenseTagAnalysisResult(
            id = id,
            totalAmount = total,
            count = picked.size,
            percent = if (periodTotal <= 0) 0 else Math.round(total * 100f / periodTotal),
            records = picked
        )
    }

    override suspend fun loadTrend(month: YearMonth): Result<ExpenseTrendResult> {
        if (!ExpenseConfig.USE_SERVER_EXPENSE) {
            val totals = _records.value.values.toList().monthlyTotals(month.atDay(1))
            val currentTotal = totals.lastOrNull()?.amount ?: 0
            val previousTotal = totals.getOrNull(totals.lastIndex - 1)?.amount ?: 0
            return Result.success(
                ExpenseTrendResult(
                    month = month,
                    totalAmount = currentTotal,
                    // 값이 0인 달도 분모에 포함한다(이관 전 화면 계산과 동일).
                    monthlyAverage = if (totals.isEmpty()) 0 else totals.sumOf { it.amount } / totals.size,
                    diffRateFromLastMonth = if (previousTotal <= 0) {
                        null
                    } else {
                        Math.round((currentTotal - previousTotal) * 100f / previousTotal)
                    },
                    trend = totals,
                    trendInsight = null
                )
            )
        }
        val header = authRepository.currentAuthHeader() ?: return Result.failure(unauthorized())
        return runCatchingNetwork(TAG) {
            val response = apiService.getExpenseTrend(header, month.toString())
            val data = response.body()?.data
            if (response.isSuccessful && data != null) {
                Result.success(
                    ExpenseTrendResult(
                        month = YearMonth.parse(data.month),
                        totalAmount = data.totalAmount,
                        monthlyAverage = data.monthlyAverage,
                        diffRateFromLastMonth = data.diffRateFromLastMonth,
                        trend = data.trend.map { MonthlyTotal(YearMonth.parse(it.month), it.amount) },
                        trendInsight = data.trendInsight
                    )
                )
            } else {
                Result.failure(response.toApiException("월별 추이를 불러오지 못했습니다."))
            }
        }
    }
}
