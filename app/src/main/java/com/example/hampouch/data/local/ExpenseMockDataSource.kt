package com.example.hampouch.data.local

import com.example.hampouch.domain.model.ExpenseRecord

/**
 * 목데이터 모드([com.example.hampouch.core.config.ExpenseConfig])에서 쓰는 초기 지출 내역 공급자.
 * 구현 연결은 [com.example.hampouch.di.MockDataModule]에서 한다.
 */
interface ExpenseMockDataSource {
    fun initialRecords(): Map<String, ExpenseRecord>
}
