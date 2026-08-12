package com.example.hampouch.domain.repository

import com.example.hampouch.domain.model.MiniChallengeState
import com.example.hampouch.domain.model.RecommendedMiniChallenge
import kotlinx.coroutines.flow.StateFlow
import java.time.LocalDate

interface MiniChallengeRepository {

    val state: StateFlow<MiniChallengeState>

    suspend fun loadChallenges(date: LocalDate): Result<Unit>

    suspend fun loadRecommended(durationDays: Int? = null): Result<Unit>

    suspend fun addRecommended(date: LocalDate, recommended: RecommendedMiniChallenge): Result<LocalDate?>

    suspend fun addCustom(date: LocalDate, name: String, totalDays: Int?): Result<LocalDate?>

    suspend fun remove(date: LocalDate, id: String): Result<Unit>

    suspend fun setChecked(date: LocalDate, id: String, checked: Boolean): Result<Unit>
}
