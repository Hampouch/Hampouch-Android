package com.example.hampouch.ui.home

import com.example.hampouch.domain.model.ExpenseEntry
import com.example.hampouch.domain.model.HomeChallenge
import com.example.hampouch.domain.model.HomeWarning
import com.example.hampouch.domain.model.MiniChallengeEntry
import java.time.LocalDate

data class HomeUiState(
    val userName: String,
    val selectedDate: LocalDate,
    val challenge: HomeChallenge?,
    val expenses: List<ExpenseEntry> = emptyList(),
    val miniChallenges: List<MiniChallengeEntry> = emptyList(),
    val warnings: List<HomeWarning> = emptyList()
)
