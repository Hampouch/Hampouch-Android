package com.example.hampouch.domain.repository

import com.example.hampouch.domain.model.OnboardingRequest

interface OnboardingLocalStore {
    fun hasCompletedOnboarding(): Boolean
    fun restorePendingIfNeeded()
    fun captureOnboardingComplete(request: OnboardingRequest)
    fun resetOnboarding()
    fun reserveForNewAccount(email: String)
    fun takeReservedRequest(email: String): OnboardingRequest?
    fun markSkipNextSplash()
    fun consumeSkipNextSplash(): Boolean
}
