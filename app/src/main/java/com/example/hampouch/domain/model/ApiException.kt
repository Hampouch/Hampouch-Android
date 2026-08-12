package com.example.hampouch.domain.model

class ApiException(
    val code: String,
    override val message: String,
    val fieldErrors: Map<String, String>? = null,
    val httpStatus: Int? = null
) : Exception(message)

/**
 * 사용자에게 보여줄 문구. 서버가 준 [message]를 쓰고 없으면 [fallback].
 *
 * [ApiException.fieldErrors]는 서버 검증 제약 이름(`categoryConsistent` 등)이 섞인 개발자용
 * 정보라 화면에는 쓰지 않는다. 원인 파악은 로그와 OkHttp 응답 로그로 한다.
 */
fun Throwable.toUserMessage(fallback: String): String =
    message?.takeIf { it.isNotBlank() } ?: fallback
