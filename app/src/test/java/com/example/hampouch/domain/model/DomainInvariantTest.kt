package com.example.hampouch.domain.model

import java.time.LocalDate
import org.junit.Assert.assertEquals
import org.junit.Test

class DomainInvariantTest {
    @Test(expected = IllegalArgumentException::class)
    fun `무지출을 amount 0인 ExpenseRecord로 만들 수 없다`() {
        ExpenseRecord(id = "1", date = LocalDate.of(2026, 8, 12), amount = 0)
    }

    @Test(expected = IllegalArgumentException::class)
    fun `preset과 custom category를 동시에 만들 수 없다`() {
        ExpenseRecord(
            id = "1",
            date = LocalDate.of(2026, 8, 12),
            amount = 1_000,
            categoryId = "cafe",
            customCategoryName = "직접 입력"
        )
    }

    @Test
    fun `MENU subtype은 필수 상세값을 non-null로 보존한다`() {
        val post = TipPost(
            id = "1",
            category = TipCategory.WHAT_TO_EAT,
            title = "메뉴",
            subtitle = "추천",
            detail = TipPostDetail.Menu(
                menuName = "메뉴",
                place = "식당",
                price = 5_000,
                rating = MenuRatingInfo(taste = 5, costEffectiveness = 4, mood = 3)
            )
        )

        val detail = post.detail as TipPostDetail.Menu
        assertEquals("메뉴", detail.menuName)
        assertEquals(5, detail.rating.taste)
    }

    @Test
    fun `온보딩 제출 모델은 기간과 목표 금액이 항상 존재한다`() {
        val request = OnboardingRequest(
            period = ChallengePeriod.Duration(7),
            dailyTargetAmount = 10_000,
            totalTargetAmount = 70_000
        )

        assertEquals(7, request.customPeriodDays)
        assertEquals(70_000, request.totalTargetAmount)
    }
}
