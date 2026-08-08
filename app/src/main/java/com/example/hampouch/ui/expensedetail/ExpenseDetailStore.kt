package com.example.hampouch.ui.expensedetail

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import com.example.hampouch.data.model.ExpenseRecord
import com.example.hampouch.data.repository.ChallengeRepository
import java.time.LocalDate

object ExpenseDetailStore {

    var recordsById: Map<String, ExpenseRecord> by mutableStateOf(ExpenseDetailMockData.initialRecords())
        private set

    fun byId(id: String): ExpenseRecord? = recordsById[id]

    fun recordsForDate(date: LocalDate): List<ExpenseRecord> =
        recordsById.values.filter { it.date == date }.sortedBy { it.id }

    fun upsert(record: ExpenseRecord) {
        recordsById = recordsById + (record.id to record)
        ChallengeRepository.clearNoRecord(record.date)
    }

    fun delete(id: String) {
        recordsById = recordsById - id
    }

    fun resetForAccount() {
        recordsById = ExpenseDetailMockData.initialRecords()
    }
}
