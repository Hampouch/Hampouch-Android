package com.example.hampouch.domain.repository

import com.example.hampouch.domain.model.BattleInvitationPreview
import com.example.hampouch.domain.model.BattleState
import com.example.hampouch.domain.model.HamBattleChallenge
import com.example.hampouch.domain.model.HamBattleChallengeRequest
import kotlinx.coroutines.flow.StateFlow

interface BattleRepository {

    val state: StateFlow<BattleState>

    suspend fun loadMyBattles(): Result<Unit>

    suspend fun loadBattleDetail(battleId: Long): Result<Unit>

    suspend fun loadInvitationPreview(battleCode: String): Result<BattleInvitationPreview>

    suspend fun join(battleCode: String): Result<Long>

    suspend fun create(request: HamBattleChallengeRequest): Result<HamBattleChallenge>

    /** 프로필 사진 변경 직후 서버 목록 재조회 전에도 내 참가자 이미지에 반영한다. */
    fun updateMyAvatar(avatarUrl: String?)
}
