package com.example.hampouch.domain.model

class ApiException(
    val code: String,
    override val message: String,
    val fieldErrors: Map<String, String>? = null,
    val httpStatus: Int? = null
) : Exception(message)

fun Throwable.toUserMessage(fallback: String): String =
    message?.takeIf { it.isNotBlank() } ?: fallback
