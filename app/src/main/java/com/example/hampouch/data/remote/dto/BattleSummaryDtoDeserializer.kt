package com.example.hampouch.data.remote.dto

import com.google.gson.Gson
import com.google.gson.JsonDeserializationContext
import com.google.gson.JsonDeserializer
import com.google.gson.JsonElement
import com.google.gson.JsonParseException
import java.lang.reflect.Type

class BattleSummaryDtoDeserializer : JsonDeserializer<MyBattleSummaryDto> {
    override fun deserialize(
        json: JsonElement,
        typeOfT: Type,
        context: JsonDeserializationContext
    ): MyBattleSummaryDto {
        val status = json.asJsonObject.get("status")?.asString
            ?: throw JsonParseException("battle status가 누락되었습니다.")
        val commonFields = setOf("battleId", "battleCode", "title", "penalty", "startDate", "endDate")
        val (target, statusFields) = when (status) {
            "READY" -> MyBattleSummaryDto.Ready::class.java to setOf("capacity", "joinedCount")
            "ONGOING" -> MyBattleSummaryDto.Ongoing::class.java to setOf("participants")
            "TERMINATED" -> MyBattleSummaryDto.Terminated::class.java to setOf("winnerNickname")
            else -> throw JsonParseException("지원하지 않는 battle status입니다: $status")
        }
        (commonFields + statusFields).forEach { field ->
            if (!json.asJsonObject.has(field) || json.asJsonObject.get(field).isJsonNull) {
                throw JsonParseException("$status battle의 $field 필드가 누락되었습니다.")
            }
        }
        return Gson().fromJson(json, target)
    }
}
