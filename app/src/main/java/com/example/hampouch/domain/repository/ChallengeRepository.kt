package com.example.hampouch.domain.repository

import com.example.hampouch.domain.model.ActiveChallenge
import com.example.hampouch.domain.model.ChallengeState
import com.example.hampouch.domain.model.OnboardingRequest
import kotlinx.coroutines.flow.StateFlow
import java.time.LocalDate

/**
 * 챌린지 도메인.
 *
 * 상태는 [state] 하나로 노출되고, 파생 계산(진행 중 챌린지·진척도 등)은 [ChallengeState]에 있다.
 */
interface ChallengeRepository {

    val state: StateFlow<ChallengeState>

    /** GET /api/challenges/current — 진행 중 챌린지를 조회해 캐시에 반영한다. */
    suspend fun loadCurrentChallenge(): Result<Unit>

    /** GET /api/challenges/history — 종료된(SUCCESS/FAIL) 챌린지 목록을 캐시에 반영한다. */
    suspend fun loadHistory(): Result<Unit>

    /** GET /api/challenges/{id}/result — 서버 확정 성패·종료 시각을 캐시에 덧입힌다. */
    suspend fun loadResult(challengeId: String): Result<Unit>

    /** PUT /api/challenges/{id}/focus-categories — 집중 카테고리를 전체 교체한다. */
    suspend fun updateFocusCategories(categories: List<String>): Result<List<String>>

    /** POST /api/challenges — 새 챌린지를 시작한다. */
    suspend fun startNewChallenge(
        request: OnboardingRequest,
        referenceToday: LocalDate = LocalDate.now()
    ): Result<ActiveChallenge>

    /** POST /api/challenges/{id}/give-up — 진행 중 챌린지를 즉시 FAIL로 종료한다. */
    suspend fun abandonChallenge(referenceToday: LocalDate = LocalDate.now()): Result<Unit>

    /** POST /api/challenges/{id}/close — 결과 팝업의 '챌린지 종료'를 서버에 반영하고 상태를 확정한다. */
    suspend fun acknowledgeChallengeEnd(): Result<Unit>

    fun updateTargetAmount(newTargetAmount: Int, effectiveFrom: LocalDate = LocalDate.now())

    /** 해당 날짜를 "미기록"으로 표시한다(한도를 넘지 않았어도 실패로 센다). */
    fun markNoRecord(date: LocalDate)

    fun clearNoRecord(date: LocalDate)

    fun markVisitedExpenseEditAfterEnd()

    fun resetForAccount(referenceToday: LocalDate = LocalDate.now())

    fun resetEmpty()
}
