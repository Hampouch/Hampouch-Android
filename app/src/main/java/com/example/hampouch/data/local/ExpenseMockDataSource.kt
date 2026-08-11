package com.example.hampouch.data.local

import com.example.hampouch.domain.model.ExpenseRecord

/**
 * 목데이터 모드([com.example.hampouch.core.config.ExpenseConfig])에서 쓰는 초기 지출 내역 공급자.
 *
 * 실제 생성 로직은 아직 화면 쪽 목데이터 객체에 있어서, data 레이어가 UI를 직접 import 하지 않도록
 * 이 인터페이스로 한 겹 끊는다. 연결은 [com.example.hampouch.di.MockDataModule]에서 한다.
 */
interface ExpenseMockDataSource {
    fun initialRecords(): Map<String, ExpenseRecord>
}
