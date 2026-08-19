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

    fun updateMyAvatar(avatarUrl: String?)
}
