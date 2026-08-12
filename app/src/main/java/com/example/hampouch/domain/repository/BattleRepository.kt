package com.example.hampouch.domain.repository

import com.example.hampouch.domain.model.BattleInvitationPreview
import com.example.hampouch.domain.model.BattleState
import com.example.hampouch.domain.model.HamBattleChallenge
import com.example.hampouch.domain.model.HamBattleChallengeRequest
import kotlinx.coroutines.flow.StateFlow

// 햄배틀(/api/battles*) 도메인
interface BattleRepository {

    val state: StateFlow<BattleState>

    /** 참가 중인 햄배틀을 전부 조회해 상태별로 나눠 반영한다. */
    suspend fun loadMyBattles(): Result<Unit>

    suspend fun loadBattleDetail(battleId: Long): Result<Unit>

    /** 초대 코드로 참가 전 미리보기를 조회한다. */
    suspend fun loadInvitationPreview(battleCode: String): Result<BattleInvitationPreview>

    /** 초대 코드로 참가한다. 성공 시 참가한 battleId. */
    suspend fun join(battleCode: String): Result<Long>

    /** 새 햄배틀을 만든다. capacity는 2~10명, durationDays는 3/7/14/31만 허용된다(서버 검증). */
    suspend fun create(request: HamBattleChallengeRequest): Result<HamBattleChallenge>
}
