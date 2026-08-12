package com.example.hampouch.ui.notification

import com.example.hampouch.domain.model.NotificationCategory
import com.example.hampouch.domain.model.NotificationItem
import com.example.hampouch.domain.model.NotificationSection
import com.example.hampouch.domain.model.NotificationTarget
import java.time.LocalDate

object NotificationMockData {

    fun populated(): List<NotificationItem> {
        val today = LocalDate.now()
        val yesterday = today.minusDays(1)
        val fiveDaysAgo = today.minusDays(5)
        return listOf(
            NotificationItem(
                id = "1",
                category = NotificationCategory.CHALLENGE,
                section = NotificationSection.TODAY,
                title = "3일째 기록이 없어요. 챌린지가 위험해요",
                message = "지금 기록하지 않으면 실패로 처리돼요",
                timeLabel = "방금",
                isRead = false,
                createdDate = today,
                target = NotificationTarget.ExpenseInput
            ),
            NotificationItem(
                id = "2",
                category = NotificationCategory.CHALLENGE,
                section = NotificationSection.TODAY,
                title = "14일 챌린지 시작",
                message = "포치와 같이 식비를 절약해봐요!",
                timeLabel = "방금",
                isRead = false,
                createdDate = today,
                target = NotificationTarget.Home
            ),
            NotificationItem(
                id = "3",
                category = NotificationCategory.CHALLENGE,
                section = NotificationSection.TODAY,
                title = "벌써 절반! 14일 챌린지 7일차",
                message = "잘하고 있어요! 지금까지 32,400원 절약중",
                timeLabel = "1시간 전",
                isRead = false,
                createdDate = today,
                target = NotificationTarget.ChallengeSummary("challenge_active")
            ),
            NotificationItem(
                id = "4",
                category = NotificationCategory.CHALLENGE,
                section = NotificationSection.TODAY,
                title = "5일 연속 성공! 포치가 기뻐해요",
                message = "이 기세로 완주까지 가봐요",
                timeLabel = "2시간 전",
                isRead = false,
                createdDate = today,
                target = NotificationTarget.Home
            ),
            NotificationItem(
                id = "5",
                category = NotificationCategory.CHALLENGE,
                section = NotificationSection.TODAY,
                title = "오늘 안 하면 연속 기록이 끊겨요",
                message = "지금 지출을 입력하면 기록을 지킬 수 있어요",
                timeLabel = "3시간 전",
                isRead = false,
                createdDate = today,
                target = NotificationTarget.ExpenseInput
            ),
            NotificationItem(
                id = "6",
                category = NotificationCategory.CHALLENGE,
                section = NotificationSection.TODAY,
                title = "D-1, 챌린지 마지막 날이에요",
                message = "오늘까지만 잘 지키면 완주예요",
                timeLabel = "3시간 전",
                isRead = false,
                createdDate = today,
                target = NotificationTarget.ChallengeSummary("challenge_active")
            ),
            NotificationItem(
                id = "7",
                category = NotificationCategory.CHALLENGE,
                section = NotificationSection.TODAY,
                title = "챌린지 성공! 결과를 확인하세요",
                message = "총 60,000원 아꼈어요",
                timeLabel = "3시간 전",
                isRead = false,
                createdDate = today,
                target = NotificationTarget.ChallengeSummary("challenge_previous")
            ),
            NotificationItem(
                id = "8",
                category = NotificationCategory.CHALLENGE,
                section = NotificationSection.TODAY,
                title = "챌린지 실패, 결과를 확인하세요",
                message = "목표를 조정해 다시 도전해봐요",
                timeLabel = "3시간 전",
                isRead = false,
                createdDate = today,
                target = NotificationTarget.ChallengeSummary("challenge_previous")
            ),
            NotificationItem(
                id = "9",
                category = NotificationCategory.HAM_BATTLE,
                section = NotificationSection.YESTERDAY,
                title = "김수진님이 배틀에 참여했어요",
                message = "5월 식비 절약왕 가리기",
                timeLabel = "1일전",
                isRead = true,
                createdDate = yesterday,
                target = NotificationTarget.HamBattleDetail("1")
            ),
            NotificationItem(
                id = "10",
                category = NotificationCategory.HAM_BATTLE,
                section = NotificationSection.YESTERDAY,
                title = "이준혁님에게 역전당했어요!",
                message = "5월 식비 절약왕 가리기",
                timeLabel = "1일전",
                isRead = true,
                createdDate = yesterday,
                target = NotificationTarget.HamBattleDetail("2")
            ),
            NotificationItem(
                id = "11",
                category = NotificationCategory.HAM_BATTLE,
                section = NotificationSection.YESTERDAY,
                title = "배틀이 종료되었어요! 최종 순위를 확인해봐요",
                message = "5월 식비 절약왕 가리기",
                timeLabel = "1일전",
                isRead = true,
                createdDate = yesterday,
                target = NotificationTarget.HamBattleEndedDetail("e1")
            ),
            NotificationItem(
                id = "12",
                category = NotificationCategory.HAM_BATTLE,
                section = NotificationSection.YESTERDAY,
                title = "시작일까지 1자리 남았어요 · D-1",
                message = "5월 식비 절약왕 가리기",
                timeLabel = "1일전",
                isRead = true,
                createdDate = yesterday,
                target = NotificationTarget.HamBattleWaitingDetail("3")
            ),
            NotificationItem(
                id = "13",
                category = NotificationCategory.COMMUNITY,
                section = NotificationSection.LAST_7_DAYS,
                title = "내 꿀팁에 좋아요 10개가 달렸어요",
                message = "배달 끊고 한 달 8만원 아낀 밀프렙 루틴",
                timeLabel = "5일전",
                isRead = true,
                createdDate = fiveDaysAgo,
                target = NotificationTarget.MyTipDetail("hamtip_1")
            ),
            NotificationItem(
                id = "14",
                category = NotificationCategory.COMMUNITY,
                section = NotificationSection.LAST_7_DAYS,
                title = "집밥수련생님이 댓글을 남겼어요",
                message = "소분 냉동 꿀팁 감사해요!",
                timeLabel = "5일전",
                isRead = true,
                createdDate = fiveDaysAgo,
                target = NotificationTarget.MyTipDetail("hamtip_14", scrollToComments = true)
            ),
            NotificationItem(
                id = "15",
                category = NotificationCategory.COMMUNITY,
                section = NotificationSection.LAST_7_DAYS,
                title = "내 글이 이번 주 인기 꿀팁에 올랐어요",
                message = "인기 2위에 선정됐어요",
                timeLabel = "5일전",
                isRead = true,
                createdDate = fiveDaysAgo,
                target = NotificationTarget.CommunityPopularPost("hamtip_1")
            )
        )
    }

    fun empty(): List<NotificationItem> = emptyList()
}
