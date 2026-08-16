package com.example.hampouch.ui.widget

import com.example.hampouch.domain.model.HomeChallenge

/**
 * 피그마의 여유·주의·부족·휴식기와 앱 인증 상태를 함께 표현한다.
 */
sealed interface HomeWidgetState {
    data object Loading : HomeWidgetState

    data object LoggedOut : HomeWidgetState

    data class InProgress(val challenge: HomeChallenge) : HomeWidgetState

    data class Resting(val plannedResumeDateLabel: String) : HomeWidgetState

    data object NoActiveChallenge : HomeWidgetState
}
