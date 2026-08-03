package com.example.hampouch.ui.takeabreak

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import java.time.LocalDate

// TODO: 서버팀 챌린지 쉬기 API 연동 시 이 목데이터 대신 서버 응답으로 breakEndDate를 채우도록 교체.
object TakeABreakStore {

    var breakEndDate: LocalDate? by mutableStateOf(null)
        private set

    fun isBreakOver(referenceToday: LocalDate): Boolean {
        val endDate = breakEndDate ?: return false
        return referenceToday.isAfter(endDate)
    }

    fun startBreak(duration: BreakDuration, customDays: Int?, referenceToday: LocalDate = LocalDate.now()) {
        val totalDays = when (duration) {
            BreakDuration.THREE_DAYS -> 3
            BreakDuration.ONE_WEEK -> 7
            BreakDuration.TWO_WEEKS -> 14
            BreakDuration.CUSTOM -> (customDays ?: 7).coerceAtLeast(1)
        }
        breakEndDate = referenceToday.plusDays((totalDays - 1).toLong())
    }

    fun postponeOneDay() {
        breakEndDate = breakEndDate?.plusDays(1)
    }

    fun endBreakNow() {
        breakEndDate = null
    }

    fun resetForAccount() {
        breakEndDate = null
    }
}
