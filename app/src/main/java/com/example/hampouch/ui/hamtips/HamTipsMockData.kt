package com.example.hampouch.ui.hamtips

import com.example.hampouch.data.model.BattleRecruitInfo
import com.example.hampouch.data.model.MenuRatingInfo
import com.example.hampouch.data.model.TipCategory
import com.example.hampouch.data.model.TipComment
import com.example.hampouch.data.model.TipPost
import com.example.hampouch.data.model.TipPostType
import com.example.hampouch.data.model.TipReply
import com.example.hampouch.data.repository.HamTipsRepository
import com.example.hampouch.ui.login.LoginMockData

object HamTipsMockData {

    private const val HERO_AUTHOR_ID = HamTipsRepository.CURRENT_USER_ID
    private const val HERO_AUTHOR_NAME = HamTipsRepository.CURRENT_USER_NAME
    private val EDITOR_AUTHOR_ID = LoginMockData.editorUser.id
    private val EDITOR_AUTHOR_NAME = LoginMockData.editorUser.name

    fun allPosts(): List<TipPost> = listOf(
        TipPost(
            id = "hamtip_1",
            type = TipPostType.TIP,
            category = TipCategory.COOKING,
            title = "배달 끊고 한 달 8만원 아낀 밀프렙 루틴",
            subtitle = "일요일 2시간 투자로 일주일 식비를 반으로 줄였어요",
            content = "일요일 2시간만 투자해서 일주일치 반찬과 국을 미리 만들어 소분해두면 " +
                "평일 저녁에 배달 앱을 열 일이 확 줄어요. 냉동실에 소분해둔 반찬만 데워도 " +
                "한 끼가 뚝딱이라 야근한 날에도 배달 대신 냉동 반찬을 꺼내게 되더라고요. " +
                "이렇게 한 달을 지내보니 배달비까지 합쳐서 8만원 정도 아낄 수 있었어요.",
            authorId = HERO_AUTHOR_ID,
            authorName = HERO_AUTHOR_NAME,
            isEditorAuthor = false,
            postedMinutesAgo = 5,
            viewCount = 8721,
            commentCount = 5,
            likeCount = 842,
            hasImage = true,
            comments = listOf(
                TipComment(
                    id = "comment_1_1",
                    authorId = "user_jipbap",
                    authorName = "집밥수련생",
                    content = "소분 냉동꿀팁 감사해요! 저도 알림부터 꺼봐야겠어요.",
                    timeLabel = "1분전",
                    replies = listOf(
                        TipReply(
                            id = "reply_1_1_1",
                            authorId = "user_altteuli",
                            authorName = "알뜰이",
                            content = "굿굿",
                            timeLabel = "방금"
                        ),
                        TipReply(
                            id = "reply_1_1_2",
                            authorId = "user_altteuli",
                            authorName = "알뜰이",
                            content = "굿굿",
                            timeLabel = "방금"
                        )
                    )
                ),
                TipComment(
                    id = "comment_1_2",
                    authorId = HERO_AUTHOR_ID,
                    authorName = HERO_AUTHOR_NAME,
                    content = "저도 이번 주부터 다시 소분 밀프렙 시작하려구요!",
                    timeLabel = "방금"
                ),
                TipComment(
                    id = "comment_1_3",
                    authorId = HERO_AUTHOR_ID,
                    authorName = HERO_AUTHOR_NAME,
                    content = "댓글 감사합니다 다들 화이팅이에요!",
                    timeLabel = "방금"
                )
            )
        ),
        TipPost(
            id = "hamtip_2",
            category = TipCategory.SHOPPING,
            title = "마트 마감 세일 시간표 완벽 정리",
            subtitle = "저녁 7시 이후 정육, 채소 코너를 먼저 가세요 마감세일은 매장마다 다르니 꼭 확인하고 가시길 추천드려요",
            authorName = "알뜰지영",
            isEditorAuthor = false,
            postedMinutesAgo = 195,
            viewCount = 3054,
            commentCount = 14,
            likeCount = 297,
            hasImage = false
        ),
        TipPost(
            id = "hamtip_3",
            category = TipCategory.DISCOUNT,
            title = "식비에 진짜 좋은 체크카드 비교 TOP3 알려드릴게요",
            subtitle = "일요일 2시간 투자로 일주일 식비를 반으로 줄였어요",
            authorId = HERO_AUTHOR_ID,
            authorName = HERO_AUTHOR_NAME,
            isEditorAuthor = false,
            postedMinutesAgo = 12,
            viewCount = 7658,
            commentCount = 35,
            likeCount = 921,
            hasImage = true
        ),
        TipPost(
            id = "hamtip_4",
            category = TipCategory.DISCOUNT,
            title = "포치가 직접 써보고 고른 자취생 필수 조미료 3종",
            subtitle = "이 세 개만 있으면 웬만한 집밥 맛은 다 나와요",
            authorId = EDITOR_AUTHOR_ID,
            authorName = EDITOR_AUTHOR_NAME,
            isEditorAuthor = true,
            postedMinutesAgo = 40,
            viewCount = 4432,
            commentCount = 6,
            likeCount = 156,
            hasImage = true
        ),
        TipPost(
            id = "hamtip_5",
            category = TipCategory.COOKING,
            title = "국물 요리 육수 한번에 왕창 내려서 소분하는 법",
            subtitle = "멸치육수 한 번 내릴 때 8팩 소분해두면 한 달이 편해요",
            authorName = "냉장고사수대",
            isEditorAuthor = false,
            postedMinutesAgo = 125,
            viewCount = 512,
            commentCount = 4,
            likeCount = 63,
            hasImage = true
        ),
        TipPost(
            id = "hamtip_6",
            category = TipCategory.ETC,
            title = "야근할 때 사무실에 쟁여두면 좋은 저칼로리 간식 리스트",
            subtitle = "편의점 가는 횟수를 줄여주는 간식 모음이에요",
            authorName = "야근요정",
            isEditorAuthor = false,
            postedMinutesAgo = 70,
            viewCount = 2187,
            commentCount = 19,
            likeCount = 349,
            hasImage = false
        ),
        TipPost(
            id = "hamtip_7",
            type = TipPostType.BATTLE,
            category = TipCategory.RECRUIT,
            title = "이번 주 햄배틀 같이 하실 분 구해요",
            subtitle = "하루 2만원 한도 챌린지 같이 하면서 서로 인증해요",
            content = "하루 2만원 한도 챌린지 같이 하실 분 구해요.",
            authorName = "배틀모집러",
            isEditorAuthor = false,
            postedMinutesAgo = 25,
            viewCount = 1876,
            commentCount = 22,
            likeCount = 88,
            hasImage = false,
            battleInfo = BattleRecruitInfo(
                link = "hamtip_7",
                durationDays = 14,
                capacity = 8,
                penalty = "인증 안하면 벌금 5천원",
                participantIds = listOf("user_a", "user_b", "user_c")
            )
        ),
        TipPost(
            id = "hamtip_8",
            category = TipCategory.COOKING,
            title = "자취 5년차가 정리한 냉장고 파먹기 황금 레시피 10선",
            subtitle = "장 안 봐도 냉장고 속 재료로 일주일 버티는 법이에요",
            authorName = "자취요리사랑",
            isEditorAuthor = false,
            postedMinutesAgo = 8,
            viewCount = 8934,
            commentCount = 38,
            likeCount = 967,
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
            viewCount = 6210,
            commentCount = 21,
            likeCount = 731,
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
            viewCount = 1345,
            commentCount = 9,
            likeCount = 112,
            hasImage = true
        ),
        TipPost(
            id = "hamtip_11",
            category = TipCategory.RECORD,
            title = "가계부 3개월째 이어가는 나만의 기록 습관",
            subtitle = "매일 저녁 5분, 영수증 사진만 찍어도 충분해요",
            authorName = "기록하는하미",
            isEditorAuthor = false,
            postedMinutesAgo = 33,
            viewCount = 3789,
            commentCount = 11,
            likeCount = 204,
            hasImage = false
        ),
        TipPost(
            id = "hamtip_12",
            category = TipCategory.ETC,
            title = "식비 말고도 아낄 수 있는 자잘한 생활비 모음",
            subtitle = "구독 서비스 정리부터 통신비 절감 팁까지 정리해봤어요",
            authorName = "생활절약러",
            isEditorAuthor = false,
            postedMinutesAgo = 150,
            viewCount = 758,
            commentCount = 7,
            likeCount = 47,
            hasImage = false
        ),
        TipPost(
            id = "hamtip_13",
            type = TipPostType.BATTLE,
            category = TipCategory.RECRUIT,
            title = "챌린지 같이 하실 분",
            subtitle = "3명 더 모집해요.",
            content = "3명 더 모집해요.",
            authorId = HERO_AUTHOR_ID,
            authorName = HERO_AUTHOR_NAME,
            isEditorAuthor = false,
            postedMinutesAgo = 2,
            viewCount = 2965,
            commentCount = 0,
            likeCount = 573,
            hasImage = false,
            battleInfo = BattleRecruitInfo(
                link = "hamtip_13",
                durationDays = 7,
                capacity = 5,
                penalty = "커피 쿠폰 쏘기",
                participantIds = listOf("user_a", "user_b")
            )
        ),
        TipPost(
            id = "hamtip_14",
            type = TipPostType.MENU,
            category = TipCategory.WHAT_TO_EAT,
            title = "마라탕 · 홍대 마라공방 · 9,000원",
            subtitle = "진짜 맛있어요. 제발 가보세요.",
            content = "진짜 맛있어요. 제발 가보세요.",
            authorId = HERO_AUTHOR_ID,
            authorName = HERO_AUTHOR_NAME,
            isEditorAuthor = false,
            postedMinutesAgo = 2,
            viewCount = 5127,
            commentCount = 1,
            likeCount = 689,
            hasImage = true,
            menuName = "마라탕 (중)",
            place = "홍대 마라공방",
            price = 9_000,
            menuRating = MenuRatingInfo(taste = 5, costEffectiveness = 5, mood = 5),
            comments = listOf(
                TipComment(
                    id = "comment_14_1",
                    authorId = "user_jipbap",
                    authorName = "집밥수련생",
                    content = "소분 냉동꿀팁 감사해요! 저도 알림부터 꺼봐야겠어요.",
                    timeLabel = "1분전"
                )
            )
        ),
        TipPost(
            id = "hamtip_15",
            type = TipPostType.MENU,
            category = TipCategory.WHAT_TO_EAT,
            title = "치즈불닭볶음면 · 홍대 분식집 · 6,500원",
            subtitle = "맵찔이도 순한맛으로 먹을 수 있어요.",
            content = "맵찔이도 순한맛으로 먹을 수 있어요.",
            authorName = "알뜰지영",
            isEditorAuthor = false,
            postedMinutesAgo = 18,
            viewCount = 3401,
            commentCount = 2,
            likeCount = 258,
            hasImage = false,
            menuName = "치즈불닭볶음면",
            place = "홍대 분식집",
            price = 6_500,
            menuRating = MenuRatingInfo(taste = 4, costEffectiveness = 5, mood = 4)
        ),
        TipPost(
            id = "hamtip_16",
            type = TipPostType.MENU,
            category = TipCategory.WHAT_TO_EAT,
            title = "제육덮밥 · 학교앞 백반집 · 7,000원",
            subtitle = "양도 많고 반찬도 알차서 가성비 최고예요.",
            content = "양도 많고 반찬도 알차서 가성비 최고예요.",
            authorName = "자취요리사랑",
            isEditorAuthor = false,
            postedMinutesAgo = 42,
            viewCount = 1689,
            commentCount = 0,
            likeCount = 19,
            hasImage = false,
            menuName = "제육덮밥",
            place = "학교앞 백반집",
            price = 7_000,
            menuRating = MenuRatingInfo(taste = 4, costEffectiveness = 5, mood = 3)
        ),
        TipPost(
            id = "hamtip_17",
            type = TipPostType.TIP,
            category = TipCategory.RECORD,
            title = "포치가 실제로 3개월 써본 가계부 앱 비교",
            subtitle = "자동 분류 정확도랑 잔소리 강도까지 솔직하게 비교했어요",
            content = "가계부 앱 세 개를 3개월씩 돌려가며 써봤어요. 자동 분류 정확도, " +
                "챌린지 미달 시 알림 강도, 카드사 연동 안정성까지 꼼꼼히 비교했으니 " +
                "본인 소비 습관에 맞는 앱 고르실 때 참고해보세요.",
            authorId = EDITOR_AUTHOR_ID,
            authorName = EDITOR_AUTHOR_NAME,
            isEditorAuthor = true,
            postedMinutesAgo = 15,
            viewCount = 6320,
            commentCount = 18,
            likeCount = 430,
            hasImage = true
        ),
        TipPost(
            id = "hamtip_18",
            type = TipPostType.MENU,
            category = TipCategory.WHAT_TO_EAT,
            title = "김치볶음밥 · 학식당 분식 · 4,500원",
            subtitle = "포치가 직접 먹어보고 인증한 가성비 맛집이에요.",
            content = "포치가 직접 먹어보고 인증한 가성비 맛집이에요.",
            authorId = EDITOR_AUTHOR_ID,
            authorName = EDITOR_AUTHOR_NAME,
            isEditorAuthor = true,
            postedMinutesAgo = 60,
            viewCount = 2890,
            commentCount = 3,
            likeCount = 210,
            hasImage = true,
            menuName = "김치볶음밥",
            place = "학식당 분식",
            price = 4_500,
            menuRating = MenuRatingInfo(taste = 5, costEffectiveness = 5, mood = 4)
        )
    )

    fun popularPosts(): List<TipPost> = allPosts().sortedByDescending { it.likeCount }

    fun pochipickPosts(): List<TipPost> = allPosts().filter { it.isEditorAuthor }
}
