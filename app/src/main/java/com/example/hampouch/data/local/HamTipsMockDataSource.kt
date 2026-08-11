package com.example.hampouch.data.local

import com.example.hampouch.domain.model.TipPost

/**
 * 목데이터 모드에서 쓰는 초기 커뮤니티 글 목록.
 *
 * [ExpenseMockDataSource]와 같은 이유로 한 겹 끊어 둔다 — 실제 생성 로직은 아직 화면 패키지에 있고,
 * 연결은 [com.example.hampouch.di.MockDataModule]에서 한다.
 */
interface HamTipsMockDataSource {
    fun allPosts(): List<TipPost>
}
