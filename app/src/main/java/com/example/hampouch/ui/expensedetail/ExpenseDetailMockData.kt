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
                amount = 3_500,
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
            // 이하 e7~e11: 챌린지 기간(하루 한도 20,000원) 중 2일은 한도 초과, 1일은 정확히
            // 한도와 같음, 나머지는 한도 미만이 되도록 날짜별 합계를 맞춘 기록.
            ExpenseRecord(
                id = "e7",
                date = referenceToday.minusDays(6),
                amount = 15_000,
                categoryId = "mart",
                expenseName = "이마트24"
            ),
            ExpenseRecord(
                id = "e7b",
                date = referenceToday.minusDays(6),
                amount = 8_000,
                categoryId = "cafe",
                expenseName = "이디야",
                reasonId = "reward"
            ),
            ExpenseRecord(
                id = "e8",
                date = referenceToday.minusDays(5),
                amount = 9_000,
                categoryId = "convenience",
                expenseName = "CU"
            ),
            ExpenseRecord(
                id = "e9",
                date = referenceToday.minusDays(4),
                amount = 20_000,
                categoryId = "delivery",
                expenseName = "배달의민족"
            ),
            ExpenseRecord(
                id = "e10",
                date = referenceToday.minusDays(3),
                amount = 18_000,
                categoryId = "dining_out",
                expenseName = "한신 포차"
            ),
            ExpenseRecord(
                id = "e10b",
                date = referenceToday.minusDays(3),
                amount = 7_000,
                categoryId = "drink",
                expenseName = "포장마차",
                reasonId = "lazy"
            ),
            ExpenseRecord(
                id = "e11",
                date = referenceToday.minusDays(2),
                amount = 4_500,
                categoryId = "snack",
                expenseName = "다이소 과자"
            )
        )
        // 지출 분석 화면(월별 추이, 카테고리/이유별 통계)이 참고할 6개월치 과거 이력.
        // ExpenseDetailStore가 이 목록으로 시드되므로 지출입력으로 새로 추가한 기록과 함께 지출 분석에도 반영된다.
        val historyRecords = generateHistoryRecords(referenceToday)
        return (historyRecords + handcraftedRecords).associateBy { it.id }
    }

    private fun generateHistoryRecords(referenceToday: LocalDate): List<ExpenseRecord> {
        val startMonth = YearMonth.from(referenceToday).minusMonths(5)
        // 진행중인 챌린지 기간은 홈/금액조정 화면의 손으로 맞춘 데이터가 대신하므로,
        // 이력 생성은 챌린지 시작일 이전까지만 채워 두 데이터가 겹쳐서 하루 잔액이 어긋나지 않게 한다.
        val activeChallengeStart = ChallengeRepository.activeChallenge.periodStart
        val result = mutableListOf<ExpenseRecord>()
        var counter = 0

        var month = startMonth
        while (!month.isAfter(YearMonth.from(referenceToday))) {
            var day = 1
            while (day <= month.lengthOfMonth()) {
                val date = month.atDay(day)
                if (!date.isAfter(referenceToday) && date.isBefore(activeChallengeStart)) {
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
        val amount = (if (isWeekendPeak) (baseAmount * 1.5).toInt() else baseAmount).coerceAtMost(19_500)

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
