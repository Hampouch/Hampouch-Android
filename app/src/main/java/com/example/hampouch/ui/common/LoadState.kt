package com.example.hampouch.ui.common

sealed interface LoadState {
    data object Idle : LoadState
    data object Loading : LoadState
    data class Content(val isEmpty: Boolean) : LoadState
    data class Failure(val message: String) : LoadState
}
