package com.example.hampouch.ui.hambattle

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class BattleInviteUrlTest {

    @Test
    fun `builds invite url without overlapping api paths`() {
        assertEquals(
            "https://invite.hampouch.com/battles/invite/ABC1234",
            buildBattleInviteUrl("ABC1234")
        )
    }

    @Test
    fun `extracts battle code from invite url`() {
        assertEquals(
            "ABC1234",
            extractBattleCodeFromInviteUrl(
                "https://invite.hampouch.com/battles/invite/ABC1234?source=kakao#join"
            )
        )
    }

    @Test
    fun `rejects swagger and battle api urls`() {
        assertNull(extractBattleCodeFromInviteUrl("https://api.hampouch.com/v3/api-docs"))
        assertNull(
            extractBattleCodeFromInviteUrl(
                "https://api.hampouch.com/battles/invite/ABC1234"
            )
        )
        assertNull(
            extractBattleCodeFromInviteUrl(
                "https://api.hampouch.com/api/battles/invitations/ABC1234"
            )
        )
    }

    @Test
    fun `rejects root path code to keep server routes separate`() {
        assertNull(extractBattleCodeFromInviteUrl("https://api.hampouch.com/ABC1234"))
    }

    @Test
    fun `normalizes raw code and full url to canonical invite url`() {
        val expected = "https://invite.hampouch.com/battles/invite/ABC1234"

        assertEquals(expected, normalizeBattleInviteUrl("ABC1234"))
        assertEquals(expected, normalizeBattleInviteUrl(expected))
    }
}
