package com.example.hampouch.ui.hambattle

import com.example.hampouch.domain.repository.AccountScopedState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import com.example.hampouch.domain.model.HamBattleChallenge

/**
 * 서버(/api/battles*) 연동 결과를 화면에서 읽기 쉬운 형태로 들고 있는 스토어.
 *
 * [com.example.hampouch.core.config.BattleConfig.USE_SERVER_BATTLE]가 true인 동안에는 목데이터를 전혀
 * 섞지 않는다 — [com.example.hampouch.data.repository.BattleRepository]가 서버 응답으로 채워주기 전까지는
 * 빈 목록/캐시로 시작한다([HamBattleMockData]와 달리 시드 데이터가 없다).
 */
object HamBattleStore : AccountScopedState {

    var readyBattles: List<HamBattleChallenge> by mutableStateOf(emptyList())
        private set
    var ongoingBattles: List<HamBattleChallenge> by mutableStateOf(emptyList())
        private set
    var terminatedBattles: List<HamBattleChallenge> by mutableStateOf(emptyList())
        private set

    /** battleId별 상세 조회 결과 캐시. */
    var detailByBattleId: Map<Long, HamBattleChallenge> by mutableStateOf(emptyMap())
        private set

    /**
     * 상세 조회(GET /api/battles/{battleId})는 정원(capacity)을 내려주지 않는다. 목록 조회나 생성 응답에서
     * 알게 된 capacity를 battleId별로 기억해 뒀다가 상세 결과에 합쳐서, 대기중 화면의 빈 슬롯 개수가
     * 어긋나지 않도록 한다.
     */
    private var capacityByBattleId: Map<Long, Int> = emptyMap()

    fun detailFor(battleId: Long): HamBattleChallenge? = detailByBattleId[battleId]

    fun replaceLists(
        ready: List<HamBattleChallenge>,
        ongoing: List<HamBattleChallenge>,
        terminated: List<HamBattleChallenge>
    ) {
        readyBattles = ready
        ongoingBattles = ongoing
        terminatedBattles = terminated
        val readyCapacities = ready.mapNotNull { challenge -> challenge.battleId?.let { it to challenge.totalCount } }
        capacityByBattleId = capacityByBattleId + readyCapacities
    }

    fun rememberCapacity(battleId: Long, capacity: Int) {
        capacityByBattleId = capacityByBattleId + (battleId to capacity)
    }

    fun setDetail(battleId: Long, detail: HamBattleChallenge) {
        val knownCapacity = capacityByBattleId[battleId]
        val merged = if (knownCapacity != null && knownCapacity > detail.totalCount) {
            detail.copy(totalCount = knownCapacity)
        } else {
            detail
        }
        detailByBattleId = detailByBattleId + (battleId to merged)
    }

    override fun resetForAccount() {
        readyBattles = emptyList()
        ongoingBattles = emptyList()
        terminatedBattles = emptyList()
        detailByBattleId = emptyMap()
        capacityByBattleId = emptyMap()
    }
}
