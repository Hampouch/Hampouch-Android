package com.example.hampouch.data.local

import com.example.hampouch.domain.model.TipPost

interface HamTipsMockDataSource {
    fun allPosts(): List<TipPost>
}
