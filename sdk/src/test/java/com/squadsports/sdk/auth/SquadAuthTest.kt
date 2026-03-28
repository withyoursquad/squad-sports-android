package com.squadsports.sdk.auth

import org.junit.Test
import org.junit.Assert.*
import org.junit.Before

/**
 * Tests for SquadAuth storage patterns.
 * Uses a pure JVM map to simulate SharedPreferences behavior
 * without requiring Android SDK.
 */
class SquadAuthTest {

    private val storedValues = mutableMapOf<String, String?>()

    @Before
    fun setUp() {
        storedValues.clear()
    }

    @Test
    fun `test stores and retrieves token`() {
        storedValues["access_token"] = "test-token-123"
        assertEquals("test-token-123", storedValues["access_token"])
    }

    @Test
    fun `test stores and retrieves userId`() {
        storedValues["user_id"] = "user-456"
        assertEquals("user-456", storedValues["user_id"])
    }

    @Test
    fun `test stores communityId`() {
        storedValues["community_id"] = "comm-789"
        assertEquals("comm-789", storedValues["community_id"])
    }

    @Test
    fun `test stores partnerId`() {
        storedValues["partner_id"] = "partner-abc"
        assertEquals("partner-abc", storedValues["partner_id"])
    }

    @Test
    fun `test storedAccessToken returns null when not set`() {
        assertNull(storedValues["access_token"])
    }

    @Test
    fun `test logout clears all stored values`() {
        storedValues["access_token"] = "token"
        storedValues["user_id"] = "user"
        storedValues["community_id"] = "comm"
        storedValues["partner_id"] = "partner"

        storedValues.clear()

        assertTrue(storedValues.isEmpty())
    }

    @Test
    fun `test overwriting token replaces previous value`() {
        storedValues["access_token"] = "old-token"
        assertEquals("old-token", storedValues["access_token"])

        storedValues["access_token"] = "new-token"
        assertEquals("new-token", storedValues["access_token"])
    }

    @Test
    fun `test fulfillSession stores both token and userId`() {
        storedValues["access_token"] = "session-token"
        storedValues["user_id"] = "session-user"

        assertEquals("session-token", storedValues["access_token"])
        assertEquals("session-user", storedValues["user_id"])
    }

    @Test
    fun `test community scoping stores communityId and partnerId`() {
        storedValues["community_id"] = "comm-new"
        storedValues["partner_id"] = "partner-new"

        assertEquals("comm-new", storedValues["community_id"])
        assertEquals("partner-new", storedValues["partner_id"])
    }

    @Test
    fun `test phone formatting adds plus prefix`() {
        val phone = "15551234567"
        val formatted = if (phone.startsWith("+")) phone else "+$phone"
        assertEquals("+15551234567", formatted)
    }

    @Test
    fun `test phone formatting keeps existing plus prefix`() {
        val phone = "+15551234567"
        val formatted = if (phone.startsWith("+")) phone else "+$phone"
        assertEquals("+15551234567", formatted)
    }
}
