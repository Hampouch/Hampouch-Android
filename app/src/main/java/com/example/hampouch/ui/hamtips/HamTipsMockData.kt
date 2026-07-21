package com.example.hampouch.ui.hamtips

import com.example.hampouch.data.model.TipCategory
import com.example.hampouch.data.model.TipPost

object HamTipsMockData {

    fun allPosts(): List<TipPost> = listOf(
        TipPost(
            id = "hamtip_1",
            category = TipCategory.COOKING,
            title = "배달 끊고 한 달 8만원 아낀 밀프렙 루틴",
            subtitle = "일요일 2시간 투자로 일주일 식비를 반으로 줄였어요",
            authorName = "절약왕 민준",
            isEditorAuthor = true,
            postedMinutesAgo = 2,
            viewCount = 128,
            commentCount = 128,
            likeCount = 128,
            hasImage = true
        ),
        TipPost(
            id = "hamtip_2",
            category = TipCategory.SHOPPING,
            title = "마트 마감 세일 시간표 완벽 정리",
            subtitle = "저녁 7시 이후 정육, 채소 코너를 먼저 가세요 마감세일은 매장마다 다르니 꼭 확인하고 가시길 추천드려요",
            authorName = "알뜰지영",
            isEditorAuthor = false,
            postedMinutesAgo = 180,
            viewCount = 128,
            commentCount = 128,
            likeCount = 128,
            hasImage = false
        ),
        TipPost(
            id = "hamtip_3",
            category = TipCategory.DISCOUNT,
            title = "식비에 진짜 좋은 체크카드 비교 TOP3 알려드릴게요",
            subtitle = "일요일 2시간 투자로 일주일 식비를 반으로 줄였어요",
            authorName = "절약왕 민준",
            isEditorAuthor = true,
            postedMinutesAgo = 2,
            viewCount = 128,
            commentCount = 128,
            likeCount = 128,
            hasImage = true
        ),
        TipPost(
            id = "hamtip_4",
            category = TipCategory.COOKING,
            title = "식비에 진짜 좋은 체크카드 비교 TOP3 알려드릴게요",
            subtitle = "일요일 2시간 투자로 일주일 식비를 반으로 줄였어요",
            authorName = "절약왕 민준",
            isEditorAuthor = true,
            postedMinutesAgo = 2,
            viewCount = 128,
            commentCount = 128,
            likeCount = 128,
            hasImage = true
        ),
        TipPost(
            id = "hamtip_5",
            category = TipCategory.COOKING,
            title = "식비에 진짜 좋은 체크카드 비교 TOP3 알려드릴게요",
            subtitle = "일요일 2시간 투자로 일주일 식비를 반으로 줄였어요",
            authorName = "절약왕 민준",
            isEditorAuthor = true,
            postedMinutesAgo = 2,
            viewCount = 128,
            commentCount = 128,
            likeCount = 128,
            hasImage = true
        ),
        TipPost(
            id = "hamtip_6",
            category = TipCategory.WHAT_TO_EAT,
            title = "야근할 때 사무실에 쟁여두면 좋은 저칼로리 간식 리스트",
            subtitle = "편의점 가는 횟수를 줄여주는 간식 모음이에요",
            authorName = "야근요정",
            isEditorAuthor = false,
            postedMinutesAgo = 45,
            viewCount = 64,
            commentCount = 12,
            likeCount = 30,
            hasImage = false
        ),
        TipPost(
            id = "hamtip_7",
            category = TipCategory.RECRUIT,
            title = "이번 주 햄배틀 같이 하실 분 구해요",
            subtitle = "하루 2만원 한도 챌린지 같이 하면서 서로 인증해요",
            authorName = "배틀모집러",
            isEditorAuthor = false,
            postedMinutesAgo = 20,
            viewCount = 52,
            commentCount = 18,
            likeCount = 9,
            hasImage = false
        )
    )

    fun popularPosts(): List<TipPost> = allPosts().sortedByDescending { it.likeCount }

    fun pochipickPosts(): List<TipPost> = allPosts().filter { it.isEditorAuthor }
}
