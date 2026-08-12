package com.example.hampouch.data.local

import com.example.hampouch.domain.model.ExpenseRecord

interface ExpenseMockDataSource {
    fun initialRecords(): Map<String, ExpenseRecord>
}
