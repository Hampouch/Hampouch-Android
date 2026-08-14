package com.example.hampouch.data.remote.dto

import com.google.gson.GsonBuilder
import com.google.gson.JsonParseException
import org.junit.Test

class RequiredFieldsDeserializerTest {
    @Test(expected = JsonParseException::class)
    fun `지출 summary의 필수 기간 누락은 계약 오류다`() {
        val gson = GsonBuilder().registerTypeAdapter(
            ExpensePeriodSummaryData::class.java,
            RequiredFieldsDeserializer(
                ExpensePeriodSummaryData::class.java,
                "periodStart", "periodEnd", "totalAmount", "dailyAverage", "dailyBreakdown"
            )
        ).create()

        gson.fromJson(
            """{"periodEnd":"2026-08-12","totalAmount":0,"dailyAverage":0,"dailyBreakdown":[]}""",
            ExpensePeriodSummaryData::class.java
        )
    }

    @Test(expected = JsonParseException::class)
    fun `미니챌린지 duration 누락은 계약 오류다`() {
        val gson = GsonBuilder().registerTypeAdapter(
            MiniChallengeItemDto::class.java,
            RequiredFieldsDeserializer(
                MiniChallengeItemDto::class.java,
                "miniChallengeId", "title", "durationDays", "progressDays", "itemStreak", "checked"
            )
        ).create()

        gson.fromJson(
            """{"miniChallengeId":1,"title":"물 마시기","progressDays":0,"itemStreak":0,"checked":false}""",
            MiniChallengeItemDto::class.java
        )
    }
}
