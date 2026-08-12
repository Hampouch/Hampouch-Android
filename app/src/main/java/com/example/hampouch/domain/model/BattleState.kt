package com.example.hampouch.domain.model

/**
 * 햄배틀 도메인의 서버 연동 상태.
 *
 * 목데이터 모드([com.example.hampouch.core.config.BattleConfig])에서는 채워지지 않는다 —
 * 그때는 화면이 `HamBattleMockData`의 시드 데이터를 쓴다.
 */
data class BattleState(
    val readyBattles: List<HamBattleChallenge> = emptyList(),
    val ongoingBattles: List<HamBattleChallenge> = emptyList(),
    val terminatedBattles: List<HamBattleChallenge> = emptyList(),
    /** battleId별 상세 조회 결과 캐시. */
    val detailByBattleId: Map<Long, HamBattleChallenge> = emptyMap()
) {
    fun detailFor(battleId: Long): HamBattleChallenge? = detailByBattleId[battleId]
}
