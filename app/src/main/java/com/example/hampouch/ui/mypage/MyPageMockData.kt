package com.example.hampouch.ui.mypage

import com.example.hampouch.data.model.ChallengeRecord
import com.example.hampouch.data.model.ChallengeStatus
import com.example.hampouch.data.model.MyPageProfile
import com.example.hampouch.data.model.TipCategory
import com.example.hampouch.data.model.TipPost

object MyPageMockData {

    fun defaultProfile(): MyPageProfile = MyPageProfile(
        name = "민준",
        handle = "hampochi_minjun",
        email = "hampouch@example.com"
    )

    fun challengeHistory(): List<ChallengeRecord> = listOf(
        ChallengeRecord(
            id = "challenge_ongoing",
            status = ChallengeStatus.IN_PROGRESS,
            totalDays = 14,
            achievedDays = 2,
            startDateLabel = "2026.05.15.",
            endDateLabel = null,
            dailyLimit = 27_000,
            totalSaved = 5_400
        ),
        ChallengeRecord(
            id = "challenge_success",
            status = ChallengeStatus.SUCCESS,
            totalDays = 14,
            achievedDays = 14,
            startDateLabel = "2026.05.01.",
            endDateLabel = "2026.05.14.",
            dailyLimit = 25_000,
            totalSaved = 21_400
        ),
        ChallengeRecord(
            id = "challenge_fail",
            status = ChallengeStatus.FAIL,
            totalDays = 14,
            achievedDays = 3,
            startDateLabel = "2026.04.01.",
            endDateLabel = "2026.04.14.",
            dailyLimit = 30_000,
            totalSaved = 2_400
        )
    )

    fun emptyChallengeHistory(): List<ChallengeRecord> = emptyList()

    fun myTips(): List<TipPost> = listOf(
        TipPost(
            id = "tip_1",
            category = TipCategory.COOKING,
            title = "배달 끊고 한 달 8만원 아낀 밀프렙 루틴",
            subtitle = "일요일 2시간 투자로 일주일 식비를 반으로 줄였어요"
        ),
        TipPost(
            id = "tip_2",
            category = TipCategory.SHOPPING,
            title = "마트 마감 세일 시간표 완벽 정리",
            subtitle = "저녁 7시 이후 정육, 채소 코너를 먼저 가세요 마감세일은 매장마다 다르니 꼭 확인하고 가시길 추천드려요"
        ),
        TipPost(
            id = "tip_3",
            category = TipCategory.DISCOUNT,
            title = "식비에 진짜 좋은 체크카드 비교 TOP3 알려드릴게요",
            subtitle = "일요일 2시간 투자로 일주일 식비를 반으로 줄였어요"
        ),
        TipPost(
            id = "tip_4",
            category = TipCategory.COOKING,
            title = "식비에 진짜 좋은 체크카드 비교 TOP3 알려드릴게요",
            subtitle = "일요일 2시간 투자로 일주일 식비를 반으로 줄였어요"
        ),
        TipPost(
            id = "tip_5",
            category = TipCategory.COOKING,
            title = "식비에 진짜 좋은 체크카드 비교 TOP3 알려드릴게요",
            subtitle = "일요일 2시간 투자로 일주일 식비를 반으로 줄였어요"
        )
    )

    fun savedTips(): List<TipPost> = myTips()

    fun emptyTips(): List<TipPost> = emptyList()
}
