package com.example.hampouch.ui.expensedetail

import com.example.hampouch.data.model.ExpenseChallengePeriod
import com.example.hampouch.data.model.ExpenseRecord
import com.example.hampouch.data.repository.ChallengeRepository
import com.example.hampouch.ui.home.HomeCategoryCatalog
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.YearMonth

internal const val MockPhotoUri = "drawable://img_hamster_chubby"

object ExpenseDetailMockData {

    private val historyStoreNamesByCategory: Map<String, List<String>> = mapOf(
        "delivery" to listOf("배달의민족", "요기요", "쿠팡이츠"),
        "dining_out" to listOf("연남동 국밥집", "한신 포차", "역전 우동"),
        "convenience" to listOf("GS25", "CU", "세븐일레븐"),
        "cafe" to listOf("스타벅스", "메가커피", "이디야"),
        "snack" to listOf("올리브영 간식", "다이소 과자", "무인 간식점"),
        "mart" to listOf("이마트24", "홈플러스 익스프레스", "동네 마트"),
        "drink" to listOf("호프집", "포장마차", "이자카야")
    )

    private val historyReasonCycle = ExpenseReasonCatalog.reasons.map { it.id }
    private val historyCategoryCycle = HomeCategoryCatalog.categories.map { it.id }

    fun initialRecords(referenceToday: LocalDate = LocalDate.now()): Map<String, ExpenseRecord> {
        val yesterday = referenceToday.minusDays(1)
        val handcraftedRecords = listOf(
            ExpenseRecord(
                id = "e1",
                date = yesterday,
                amount = 4_500,
                categoryId = "cafe",
                expenseName = "스타벅스",
                reasonId = "stress",
                memo = "식후 커피 어떻게 참아요..",
                photoUris = listOf(MockPhotoUri)
            ),
            ExpenseRecord(
                id = "e2",
                date = yesterday,
                amount = 3_200,
                categoryId = "convenience",
                expenseName = "세븐일레븐"
            ),
            ExpenseRecord(
                id = "e3",
                date = yesterday,
                amount = 4_500
            ),
            ExpenseRecord(
                id = "e4",
                date = yesterday,
                amount = 15_800,
                customCategoryName = "자취요리",
                expenseName = "장보기 재료",
                reasonId = "stress"
            ),
            ExpenseRecord(
                id = "e5",
                date = referenceToday,
                amount = 4_500,
                categoryId = "cafe",
                expenseName = "스타벅스",
                reasonId = "stress"
            ),
            ExpenseRecord(
                id = "e6",
                date = referenceToday,
                amount = 0,
                expenseName = "지출내역",
                customReason = "감정태깅"
            ),
            ExpenseRecord(
                id = "e7",
                date = referenceToday.minusDays(6),
                amount = 163_000,
                categoryId = "mart",
                expenseName = "장보기"
            ),
            ExpenseRecord(
                id = "e8",
                date = referenceToday.minusDays(5),
                amount = 14_000,
                categoryId = "convenience",
                expenseName = "편의점"
            ),
            ExpenseRecord(
                id = "e9",
                date = referenceToday.minusDays(4),
                amount = 12_000,
                categoryId = "delivery",
                expenseName = "배달음식"
            ),
            ExpenseRecord(
                id = "e10",
                date = referenceToday.minusDays(3),
                amount = 124_000,
                categoryId = "dining_out",
                expenseName = "외식"
            )
        )
        // 지출 분석 화면(월별 추이, 카테고리/이유별 통계)이 참고할 6개월치 과거 이력.
        // ExpenseDetailStore가 이 목록으로 시드되므로 지출입력으로 새로 추가한 기록과 함께 지출 분석에도 반영된다.
        val historyRecords = generateHistoryRecords(referenceToday)
        return (historyRecords + handcraftedRecords).associateBy { it.id }
    }

    private fun generateHistoryRecords(referenceToday: LocalDate): List<ExpenseRecord> {
        val startMonth = YearMonth.from(referenceToday).minusMonths(5)
        val result = mutableListOf<ExpenseRecord>()
        var counter = 0

        var month = startMonth
        while (!month.isAfter(YearMonth.from(referenceToday))) {
            var day = 1
            while (day <= month.lengthOfMonth()) {
                val date = month.atDay(day)
                if (!date.isAfter(referenceToday)) {
                    counter++
                    result += buildHistoryRecord(counter, date)
                }
                day += 2
            }
            month = month.plusMonths(1)
        }
        return result
    }

    private fun buildHistoryRecord(counter: Int, date: LocalDate): ExpenseRecord {
        val isWeekendPeak = date.dayOfWeek == DayOfWeek.FRIDAY || date.dayOfWeek == DayOfWeek.SATURDAY
        val baseAmount = 6_000 + (counter % 6) * 3_000
        val amount = if (isWeekendPeak) (baseAmount * 1.8).toInt() else baseAmount

        val useCustomCategory = counter % 9 == 0
        val categoryId = historyCategoryCycle[counter % historyCategoryCycle.size]
        val storeName = historyStoreNamesByCategory[categoryId]?.get(counter % 3)

        val reasonSlot = counter % 6
        val reasonId = if (reasonSlot < historyReasonCycle.size) historyReasonCycle[reasonSlot] else null
        val useCustomReason = reasonId == null && reasonSlot % 2 == 0

        return ExpenseRecord(
            id = "hist_$counter",
            date = date,
            amount = amount,
            categoryId = if (useCustomCategory) null else categoryId,
            customCategoryName = if (useCustomCategory) "자취 생활비" else null,
            expenseName = if (useCustomCategory) null else storeName,
            reasonId = reasonId,
            customReason = if (useCustomReason) "그때 기분" else null
        )
    }

    // 홈/금액조정/지출입력과 동일한 ChallengeRepository를 참조해 챌린지 기간을 일치시킨다.
    fun activeChallengePeriod(): ExpenseChallengePeriod {
        val active = ChallengeRepository.activeChallenge
        return ExpenseChallengePeriod(startDate = active.periodStart, endDate = active.periodEnd)
    }

    fun endedChallengePeriod(referenceToday: LocalDate = LocalDate.now()): ExpenseChallengePeriod =
        ExpenseChallengePeriod(
            startDate = referenceToday.minusDays(20),
            endDate = referenceToday.minusDays(6)
        )

    private const val MONTHLY_TOTAL = 889_483
    private const val MONTHLY_DAILY_AVERAGE = 29_649
    private const val WEEKLY_TOTAL = 171_400
    private const val WEEKLY_DAILY_AVERAGE = 24_486

    fun monthlyTotal(): Int = MONTHLY_TOTAL
    fun monthlyDailyAverage(): Int = MONTHLY_DAILY_AVERAGE
    fun weeklyTotal(): Int = WEEKLY_TOTAL
    fun weeklyDailyAverage(): Int = WEEKLY_DAILY_AVERAGE
}
