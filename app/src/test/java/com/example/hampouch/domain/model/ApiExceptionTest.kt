package com.example.hampouch.domain.model

import org.junit.Assert.assertEquals
import org.junit.Test

class ApiExceptionTest {

    @Test
    fun `fieldErrors가 있으면 키와 값을 우선 사용자 메시지로 사용한다`() {
        val error = ApiException(
            code = "VALIDATION_ERROR",
            message = "입력값이 올바르지 않습니다.",
            fieldErrors = linkedMapOf(
                "price" to "금액은 1,000만 원 이하여야 합니다.",
                "memo" to "메모는 300자 이하여야 합니다."
            )
        )

        assertEquals(
            "price 금액은 1,000만 원 이하여야 합니다.\nmemo 메모는 300자 이하여야 합니다.",
            error.toUserMessage("요청에 실패했습니다.")
        )
    }

    @Test
    fun `fieldErrors가 없으면 서버 메시지를 사용한다`() {
        val error = ApiException(
            code = "VALIDATION_ERROR",
            message = "입력값이 올바르지 않습니다."
        )

        assertEquals("입력값이 올바르지 않습니다.", error.toUserMessage("요청에 실패했습니다."))
    }
}
