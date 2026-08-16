package com.example.hampouch.domain.model

data class BattleState(
    val readyBattles: List<HamBattleChallenge> = emptyList(),
    val ongoingBattles: List<HamBattleChallenge> = emptyList(),
    val terminatedBattles: List<HamBattleChallenge> = emptyList(),
    val detailByBattleId: Map<Long, HamBattleChallenge> = emptyMap()
) {
    fun detailFor(battleId: Long): HamBattleChallenge? = detailByBattleId[battleId]
}
