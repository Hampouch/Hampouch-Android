package com.example.hampouch.data.remote.dto

import com.google.gson.GsonBuilder
import com.google.gson.JsonParseException
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class BattleSummaryDtoDeserializerTest {
    private val gson = GsonBuilder()
        .registerTypeAdapter(MyBattleSummaryDto::class.java, BattleSummaryDtoDeserializer())
        .create()

    @Test
    fun `READY fixture를 상태 전용 DTO로 변환한다`() {
        val dto = parse("""{"battleId":1,"battleCode":"A","title":"t","penalty":"p","startDate":"2026-08-12","endDate":"2026-08-18","status":"READY","capacity":4,"joinedCount":2}""")

        assertTrue(dto is MyBattleSummaryDto.Ready)
        assertEquals(4, (dto as MyBattleSummaryDto.Ready).capacity)
    }

    @Test
    fun `ONGOING fixture를 상태 전용 DTO로 변환한다`() {
        val dto = parse("""{"battleId":1,"battleCode":"A","title":"t","penalty":"p","startDate":"2026-08-12","endDate":"2026-08-18","status":"ONGOING","participants":[]}""")

        assertTrue(dto is MyBattleSummaryDto.Ongoing)
    }

    @Test
    fun `TERMINATED fixture를 상태 전용 DTO로 변환한다`() {
        val dto = parse("""{"battleId":1,"battleCode":"A","title":"t","penalty":"p","startDate":"2026-08-12","endDate":"2026-08-18","status":"TERMINATED","winnerNickname":"winner"}""")

        assertTrue(dto is MyBattleSummaryDto.Terminated)
    }

    @Test(expected = JsonParseException::class)
    fun `상태 필수 필드가 누락되면 계약 오류를 던진다`() {
        parse("""{"battleId":1,"battleCode":"A","title":"t","penalty":"p","startDate":"2026-08-12","endDate":"2026-08-18","status":"READY","joinedCount":2}""")
    }

    private fun parse(json: String): MyBattleSummaryDto =
        gson.fromJson(json, MyBattleSummaryDto::class.java)
}
