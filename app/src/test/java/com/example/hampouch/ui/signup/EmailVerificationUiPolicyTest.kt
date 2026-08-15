package com.example.hampouch.ui.signup

import com.example.hampouch.domain.model.ApiException
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class EmailVerificationUiPolicyTest {

    @Test
    fun `인증 만료는 회원가입 인증 요청 상태를 초기화한다`() {
        val state = SignUpUiState(
            emailCode = "123456",
            isEmailVerified = false,
            hasSentEmailCode = true,
            emailCodeExpiresAtMillis = 1000L
        )

        val updated = state.emailVerificationFailed(
            ApiException("AUTH_EMAIL_CODE_EXPIRED", "인증번호가 만료되었습니다. 다시 요청해주세요.")
        )

        assertEquals("", updated.emailCode)
        assertFalse(updated.hasSentEmailCode)
        assertNull(updated.emailCodeExpiresAtMillis)
        assertEquals("인증번호가 만료되었습니다. 다시 요청해주세요.", updated.emailVerifyMessage)
    }

    @Test
    fun `인증 시도 초과는 비밀번호 재설정 인증 요청 상태를 초기화한다`() {
        val state = ResetPasswordUiState(
            emailCode = "123456",
            isEmailVerified = false,
            hasSentEmailCode = true,
            emailCodeExpiresAtMillis = 1000L
        )

        val updated = state.emailVerificationFailed(
            ApiException("AUTH_EMAIL_CODE_ATTEMPT_EXCEEDED", "인증번호 확인 횟수를 초과했습니다.")
        )

        assertEquals("", updated.emailCode)
        assertFalse(updated.hasSentEmailCode)
        assertNull(updated.emailCodeExpiresAtMillis)
    }

    @Test
    fun `인증번호 불일치는 기존 입력과 만료 시간을 유지한다`() {
        val state = SignUpUiState(
            emailCode = "123456",
            isEmailVerified = false,
            hasSentEmailCode = true,
            emailCodeExpiresAtMillis = 1000L
        )

        val updated = state.emailVerificationFailed(
            ApiException("AUTH_EMAIL_CODE_MISMATCH", "인증번호를 다시 확인해주세요.")
        )

        assertEquals("123456", updated.emailCode)
        assertTrue(updated.hasSentEmailCode)
        assertEquals(1000L, updated.emailCodeExpiresAtMillis)
    }
}
