package com.example.hampouch.ui.widget

import com.example.hampouch.data.model.HomeChallenge

/**
 * 홈 화면 위젯이 그리는 화면 상태.
 * 피그마 "위젯" 섹션(node-id 2627:19676)의 4가지 변형 중
 * - 여유 / 주의 / 부족 은 [InProgress] 하나로 표현되고 [HomeChallenge.characterState]로 갈린다.
 * - 휴식기(진행 중인 챌린지 없음)는 [NoActiveChallenge]로 표현된다.
 */
sealed interface HomeWidgetState {
    data class InProgress(val challenge: HomeChallenge) : HomeWidgetState
    data object NoActiveChallenge : HomeWidgetState
}
