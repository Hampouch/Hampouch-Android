package com.example.hampouch.domain.model

class ApiException(
    val code: String,
    override val message: String,
    val fieldErrors: Map<String, String>? = null,
    val httpStatus: Int? = null
) : Exception(message)

fun Throwable.toUserMessage(fallback: String): String =
    (this as? ApiException)
        ?.fieldErrors
        ?.entries
        ?.filter { (key, value) -> key.isNotBlank() && value.isNotBlank() }
        ?.map { (key, value) -> "$key $value" }
        ?.joinToString(separator = "\n")
        ?.takeIf { it.isNotBlank() }
        ?: message?.takeIf { it.isNotBlank() }
        ?: fallback
