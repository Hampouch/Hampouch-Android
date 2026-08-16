package com.example.hampouch.data.remote.dto

import com.google.gson.Gson
import com.google.gson.JsonDeserializationContext
import com.google.gson.JsonDeserializer
import com.google.gson.JsonElement
import com.google.gson.JsonParseException
import java.lang.reflect.Type

class RequiredFieldsDeserializer<T>(
    private val target: Class<T>,
    private vararg val requiredFields: String
) : JsonDeserializer<T> {
    override fun deserialize(
        json: JsonElement,
        typeOfT: Type,
        context: JsonDeserializationContext
    ): T {
        val objectValue = json.takeIf { it.isJsonObject }?.asJsonObject
            ?: throw JsonParseException("${target.simpleName} 응답이 object가 아닙니다.")
        requiredFields.forEach { field ->
            if (!objectValue.has(field) || objectValue.get(field).isJsonNull) {
                throw JsonParseException("${target.simpleName}의 필수 필드 ${field}가 누락되었습니다.")
            }
        }
        return Gson().fromJson(json, target)
    }
}
