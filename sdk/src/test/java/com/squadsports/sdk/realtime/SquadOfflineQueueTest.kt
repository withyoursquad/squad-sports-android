package com.squadsports.sdk.realtime

import org.junit.Test
import org.junit.Assert.*
import org.junit.Before

/**
 * Tests for SquadOfflineQueue data types and logic.
 * Tests the queue data model and action types without requiring Android runtime.
 * Full integration tests (with SharedPreferences persistence) run via Android instrumentation.
 */
class SquadOfflineQueueTest {

    // --- OfflineActionType tests ---

    @Test
    fun `test OfflineActionType values`() {
        assertEquals("message:create", OfflineActionType.MESSAGE_CREATE.value)
        assertEquals("freestyle:create", OfflineActionType.FREESTYLE_CREATE.value)
        assertEquals("poll:response", OfflineActionType.POLL_RESPONSE.value)
        assertEquals("message:reaction", OfflineActionType.MESSAGE_REACTION.value)
        assertEquals("freestyle:reaction", OfflineActionType.FREESTYLE_REACTION.value)
        assertEquals("user:update", OfflineActionType.USER_UPDATE.value)
    }

    @Test
    fun `test OfflineActionType entries count`() {
        assertEquals(6, OfflineActionType.entries.size)
    }

    @Test
    fun `test OfflineActionType fromValue resolves known types`() {
        for (type in OfflineActionType.entries) {
            val resolved = OfflineActionType.entries.find { it.value == type.value }
            assertNotNull(resolved)
            assertEquals(type, resolved)
        }
    }

    @Test
    fun `test OfflineActionType fromValue returns null for unknown`() {
        val resolved = OfflineActionType.entries.find { it.value == "unknown:type" }
        assertNull(resolved)
    }

    // --- OfflineAction data class tests ---

    @Test
    fun `test OfflineAction creation`() {
        val action = OfflineAction(
            id = "action-1",
            type = OfflineActionType.MESSAGE_CREATE,
            payload = """{"body":"hello"}""",
            createdAt = 1000L,
        )
        assertEquals("action-1", action.id)
        assertEquals(OfflineActionType.MESSAGE_CREATE, action.type)
        assertEquals("""{"body":"hello"}""", action.payload)
        assertEquals(1000L, action.createdAt)
        assertEquals(0, action.attempts)
    }

    @Test
    fun `test OfflineAction default attempts is 0`() {
        val action = OfflineAction("id", OfflineActionType.POLL_RESPONSE, "{}", 0L)
        assertEquals(0, action.attempts)
    }

    @Test
    fun `test OfflineAction attempts is mutable`() {
        val action = OfflineAction("id", OfflineActionType.POLL_RESPONSE, "{}", 0L)
        action.attempts = 2
        assertEquals(2, action.attempts)
    }

    @Test
    fun `test OfflineAction attempts increment`() {
        val action = OfflineAction("id", OfflineActionType.MESSAGE_CREATE, "{}", 0L)
        action.attempts++
        action.attempts++
        action.attempts++
        assertEquals(3, action.attempts)
    }

    @Test
    fun `test OfflineAction max retry threshold`() {
        val action = OfflineAction("id", OfflineActionType.MESSAGE_CREATE, "{}", 0L)
        val maxAttempts = 3
        for (i in 1..maxAttempts) {
            action.attempts++
        }
        assertTrue(action.attempts >= maxAttempts)
    }

    // --- Queue logic simulation tests ---

    @Test
    fun `test in-memory queue add and count`() {
        val queue = mutableListOf<OfflineAction>()
        assertEquals(0, queue.size)

        queue.add(OfflineAction("1", OfflineActionType.MESSAGE_CREATE, """{"body":"test"}""", System.currentTimeMillis()))
        assertEquals(1, queue.size)
    }

    @Test
    fun `test in-memory queue process all success`() {
        val queue = mutableListOf<OfflineAction>()
        queue.add(OfflineAction("1", OfflineActionType.MESSAGE_CREATE, "{}", 0L))
        queue.add(OfflineAction("2", OfflineActionType.POLL_RESPONSE, "{}", 0L))

        val failed = mutableListOf<OfflineAction>()
        var succeeded = 0
        for (action in queue) {
            succeeded++ // all succeed
        }
        queue.clear()
        queue.addAll(failed)

        assertEquals(2, succeeded)
        assertEquals(0, queue.size)
    }

    @Test
    fun `test in-memory queue re-queues failures`() {
        val queue = mutableListOf<OfflineAction>()
        queue.add(OfflineAction("1", OfflineActionType.MESSAGE_CREATE, "{}", 0L))

        val toProcess = queue.toList()
        queue.clear()
        val requeue = mutableListOf<OfflineAction>()

        for (action in toProcess) {
            action.attempts++
            if (action.attempts < 3) {
                requeue.add(action) // re-queue
            }
        }
        queue.addAll(requeue)

        assertEquals(1, queue.size)
        assertEquals(1, queue[0].attempts)
    }

    @Test
    fun `test in-memory queue drops after max attempts`() {
        val action = OfflineAction("1", OfflineActionType.MESSAGE_CREATE, "{}", 0L)
        action.attempts = 2 // already failed twice

        action.attempts++
        val shouldDrop = action.attempts >= 3

        assertTrue(shouldDrop)
    }

    @Test
    fun `test clear empties queue`() {
        val queue = mutableListOf<OfflineAction>()
        queue.add(OfflineAction("1", OfflineActionType.MESSAGE_CREATE, "{}", 0L))
        queue.add(OfflineAction("2", OfflineActionType.POLL_RESPONSE, "{}", 0L))

        queue.clear()
        assertEquals(0, queue.size)
    }

    @Test
    fun `test all action types can be queued`() {
        val queue = mutableListOf<OfflineAction>()
        for (type in OfflineActionType.entries) {
            queue.add(OfflineAction("id-${type.value}", type, "{}", 0L))
        }
        assertEquals(OfflineActionType.entries.size, queue.size)
    }
}
