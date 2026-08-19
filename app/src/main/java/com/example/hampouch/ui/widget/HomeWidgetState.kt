package com.example.hampouch.ui.widget

import com.example.hampouch.domain.model.HomeChallenge

sealed interface HomeWidgetState {
    data object Loading : HomeWidgetState

    data object LoggedOut : HomeWidgetState

    data class InProgress(val challenge: HomeChallenge) : HomeWidgetState

    data class Resting(val plannedResumeDateLabel: String) : HomeWidgetState

    data object NoActiveChallenge : HomeWidgetState
}
