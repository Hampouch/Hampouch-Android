package com.example.hampouch.domain.repository

import com.example.hampouch.domain.model.MiniChallengeState
import com.example.hampouch.domain.model.RecommendedMiniChallenge
import kotlinx.coroutines.flow.StateFlow
import java.time.LocalDate

// 미니 챌린지 도메인
interface MiniChallengeRepository {

    val state: StateFlow<MiniChallengeState>

    /** [date]의 목록·요약을 조회해 반영한다. */
    suspend fun loadChallenges(date: LocalDate): Result<Unit>

    /** 추천 카탈로그를 조회해 반영한다. [durationDays]가 null이면 전체 기간. */
    suspend fun loadRecommended(durationDays: Int? = null): Result<Unit>

    /**
     * 추천 카탈로그의 [recommended]를 내 미니 챌린지로 추가한다.
     *
     * 서버 요청에는 날짜 필드가 없어 항상 서버의 오늘 날짜부터 시작하는 챌린지가 생긴다 —
     * 그래서 실제로 추가된 날짜를 돌려준다. 호출한 화면은 그 날짜로 선택 탭을 옮겨야 방금 추가한
     * 항목이 보인다. 중복 이름 등으로 추가되지 않았으면 null.
     */
    suspend fun addRecommended(date: LocalDate, recommended: RecommendedMiniChallenge): Result<LocalDate?>

    /** 커스텀 미니 챌린지를 만든다. 반환값 의미는 [addRecommended]와 같다. */
    suspend fun addCustom(date: LocalDate, name: String, totalDays: Int?): Result<LocalDate?>

    suspend fun remove(date: LocalDate, id: String): Result<Unit>

    /** [date] 기준으로 [id]의 체크 상태를 [checked]로 바꾼다. */
    suspend fun setChecked(date: LocalDate, id: String, checked: Boolean): Result<Unit>
}
