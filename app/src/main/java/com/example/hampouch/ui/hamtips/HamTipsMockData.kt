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
            postedMinutesAgo = 5,
            viewCount = 18234,
            commentCount = 27,
            likeCount = 3120,
            hasImage = true
        ),
        TipPost(
            id = "hamtip_2",
            category = TipCategory.SHOPPING,
            title = "마트 마감 세일 시간표 완벽 정리",
            subtitle = "저녁 7시 이후 정육, 채소 코너를 먼저 가세요 마감세일은 매장마다 다르니 꼭 확인하고 가시길 추천드려요",
            authorName = "알뜰지영",
            isEditorAuthor = false,
            postedMinutesAgo = 195,
            viewCount = 6420,
            commentCount = 14,
            likeCount = 612,
            hasImage = false
        ),
        TipPost(
            id = "hamtip_3",
            category = TipCategory.DISCOUNT,
            title = "식비에 진짜 좋은 체크카드 비교 TOP3 알려드릴게요",
            subtitle = "일요일 2시간 투자로 일주일 식비를 반으로 줄였어요",
            authorName = "절약왕 민준",
            isEditorAuthor = true,
            postedMinutesAgo = 12,
            viewCount = 24980,
            commentCount = 35,
            likeCount = 4890,
            hasImage = true
        ),
        TipPost(
            id = "hamtip_4",
            category = TipCategory.COOKING,
            title = "식비에 진짜 좋은 체크카드 비교 TOP3 알려드릴게요",
            subtitle = "일요일 2시간 투자로 일주일 식비를 반으로 줄였어요",
            authorName = "절약왕 민준",
            isEditorAuthor = true,
            postedMinutesAgo = 40,
            viewCount = 1530,
            commentCount = 6,
            likeCount = 210,
            hasImage = true
        ),
        TipPost(
            id = "hamtip_5",
            category = TipCategory.COOKING,
            title = "식비에 진짜 좋은 체크카드 비교 TOP3 알려드릴게요",
            subtitle = "일요일 2시간 투자로 일주일 식비를 반으로 줄였어요",
            authorName = "절약왕 민준",
            isEditorAuthor = true,
            postedMinutesAgo = 125,
            viewCount = 890,
            commentCount = 4,
            likeCount = 95,
            hasImage = true
        ),
        TipPost(
            id = "hamtip_6",
            category = TipCategory.WHAT_TO_EAT,
            title = "야근할 때 사무실에 쟁여두면 좋은 저칼로리 간식 리스트",
            subtitle = "편의점 가는 횟수를 줄여주는 간식 모음이에요",
            authorName = "야근요정",
            isEditorAuthor = false,
            postedMinutesAgo = 70,
            viewCount = 3320,
            commentCount = 19,
            likeCount = 430,
            hasImage = false
        ),
        TipPost(
            id = "hamtip_7",
            category = TipCategory.RECRUIT,
            title = "이번 주 햄배틀 같이 하실 분 구해요",
            subtitle = "하루 2만원 한도 챌린지 같이 하면서 서로 인증해요",
            authorName = "배틀모집러",
            isEditorAuthor = false,
            postedMinutesAgo = 25,
            viewCount = 980,
            commentCount = 22,
            likeCount = 140,
            hasImage = false
        ),
        TipPost(
            id = "hamtip_8",
            category = TipCategory.COOKING,
            title = "자취 5년차가 정리한 냉장고 파먹기 황금 레시피 10선",
            subtitle = "장 안 봐도 냉장고 속 재료로 일주일 버티는 법이에요",
            authorName = "자취요리사랑",
            isEditorAuthor = false,
            postedMinutesAgo = 8,
            viewCount = 27650,
            commentCount = 38,
            likeCount = 4710,
            hasImage = true
        ),
        TipPost(
            id = "hamtip_9",
            category = TipCategory.COOKING,
            title = "에어프라이어 하나로 끝내는 초간단 저녁 6가지",
            subtitle = "설거지 거의 없이 20분 안에 완성되는 메뉴들이에요",
            authorName = "에어프라이어장인",
            isEditorAuthor = false,
            postedMinutesAgo = 55,
            viewCount = 9840,
            commentCount = 21,
            likeCount = 1580,
            hasImage = false
        ),
        TipPost(
            id = "hamtip_10",
            category = TipCategory.COOKING,
            title = "이번 달 채소값 반토막 낸 제철 반찬 3종",
            subtitle = "제철 재료만 써도 맛도 가격도 만족스러워요",
            authorName = "반찬연구소",
            isEditorAuthor = false,
            postedMinutesAgo = 310,
            viewCount = 2140,
            commentCount = 9,
            likeCount = 260,
            hasImage = true
        )
    )

    fun popularPosts(): List<TipPost> = allPosts().sortedByDescending { it.likeCount }

    fun pochipickPosts(): List<TipPost> = allPosts().filter { it.isEditorAuthor }
}
