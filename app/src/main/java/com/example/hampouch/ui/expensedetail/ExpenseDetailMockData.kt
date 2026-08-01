package com.example.hampouch.ui.expensedetail

import com.example.hampouch.data.model.ExpenseChallengePeriod
import com.example.hampouch.data.model.ExpenseRecord
import java.time.LocalDate

internal const val MockPhotoUri = "drawable://img_hamster_chubby"

object ExpenseDetailMockData {

    fun initialRecords(referenceToday: LocalDate = LocalDate.now()): Map<String, ExpenseRecord> {
        val yesterday = referenceToday.minusDays(1)
        val records = listOf(
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
        return records.associateBy { it.id }
    }

    fun activeChallengePeriod(referenceToday: LocalDate = LocalDate.now()): ExpenseChallengePeriod =
        ExpenseChallengePeriod(
            startDate = referenceToday.minusDays(6),
            endDate = referenceToday.plusDays(7)
        )

    fun endedChallengePeriod(referenceToday: LocalDate = LocalDate.now()): ExpenseChallengePeriod =
        ExpenseChallengePeriod(
            startDate = referenceToday.minusDays(20),
            endDate = referenceToday.minusDays(6)
        )

    private const val MONTHLY_TOTAL = 889_483
    private const val MONTHLY_DAILY_AVERAGE = 29_649
    private const val WEEKLY_TOTAL = 171_400
    private const val WEEKLY_DAILY_AVERAGE = 29_649

    fun monthlyTotal(): Int = MONTHLY_TOTAL
    fun monthlyDailyAverage(): Int = MONTHLY_DAILY_AVERAGE
    fun weeklyTotal(): Int = WEEKLY_TOTAL
    fun weeklyDailyAverage(): Int = WEEKLY_DAILY_AVERAGE
}
