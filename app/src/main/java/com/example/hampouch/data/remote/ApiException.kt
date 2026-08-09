package com.example.hampouch.data.remote

class ApiException(
    val code: String,
    override val message: String,
    val fieldErrors: Map<String, String>? = null
) : Exception(message)

fun Throwable.toUserMessage(fallback: String): String {
    val fieldErrors = (this as? ApiException)?.fieldErrors
    return if (!fieldErrors.isNullOrEmpty()) {
        fieldErrors.values.joinToString("\n")
    } else {
        message ?: fallback
    }
}
