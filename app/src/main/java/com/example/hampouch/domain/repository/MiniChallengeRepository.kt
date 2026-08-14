package com.example.hampouch.domain.repository

import com.example.hampouch.domain.model.MiniChallengeState
import com.example.hampouch.domain.model.RecommendedMiniChallenge
import com.example.hampouch.domain.model.MiniChallengeDuration
import kotlinx.coroutines.flow.StateFlow
import java.time.LocalDate

interface MiniChallengeRepository {

    val state: StateFlow<MiniChallengeState>

    suspend fun loadChallenges(date: LocalDate): Result<Unit>

    suspend fun loadRecommended(durationDays: Int? = null): Result<Unit>

    /**
     * 추천 카탈로그의 [recommended]를 내 미니 챌린지로 추가한다.
     *
     * 서버 요청에는 날짜 필드가 없어 항상 서버의 오늘 날짜부터 시작하는 챌린지가 생긴다 —
     * 그래서 실제로 추가된 날짜를 돌려준다. 호출한 화면은 그 날짜로 선택 탭을 옮겨야 방금 추가한
     * 항목이 보인다. 중복 이름 등으로 추가되지 않았으면 null.
     */
    suspend fun addRecommended(date: LocalDate, recommended: RecommendedMiniChallenge): Result<LocalDate>

    /** 커스텀 미니 챌린지를 만든다. 반환값 의미는 [addRecommended]와 같다. */
    suspend fun addCustom(date: LocalDate, name: String, duration: MiniChallengeDuration): Result<LocalDate>

    suspend fun remove(date: LocalDate, id: String): Result<Unit>

    suspend fun setChecked(date: LocalDate, id: String, checked: Boolean): Result<Unit>
}
