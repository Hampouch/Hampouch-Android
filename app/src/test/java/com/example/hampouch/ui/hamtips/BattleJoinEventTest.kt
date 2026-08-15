package com.example.hampouch.ui.hamtips

import com.example.hampouch.domain.model.ApiException
import org.junit.Assert.assertEquals
import org.junit.Test

class BattleJoinEventTest {

    @Test
    fun `BATTLE_FULL은 정원 마감 이벤트로 변환한다`() {
        assertEquals(
            HamTipsDetailEvent.BattleFull,
            ApiException("BATTLE_FULL", "정원이 마감되었습니다.").toBattleJoinEvent()
        )
    }

    @Test
    fun `참가 상태 오류를 code별 이벤트로 변환한다`() {
        assertEquals(
            HamTipsDetailEvent.BattleAlreadyStarted("이미 시작된 햄배틀입니다."),
            ApiException("BATTLE_ALREADY_STARTED", "이미 시작된 햄배틀입니다.").toBattleJoinEvent()
        )
        assertEquals(
            HamTipsDetailEvent.BattleAlreadyJoined("이미 참가한 햄배틀입니다."),
            ApiException("ALREADY_JOINED", "이미 참가한 햄배틀입니다.").toBattleJoinEvent()
        )
        assertEquals(
            HamTipsDetailEvent.BattleCancelled("취소된 햄배틀입니다."),
            ApiException("BATTLE_CANCELLED", "취소된 햄배틀입니다.").toBattleJoinEvent()
        )
    }

    @Test
    fun `알 수 없는 오류는 서버 문구 이벤트로 변환한다`() {
        assertEquals(
            HamTipsDetailEvent.ShowMessage("서버 오류"),
            ApiException("UNKNOWN", "서버 오류").toBattleJoinEvent()
        )
    }
}
