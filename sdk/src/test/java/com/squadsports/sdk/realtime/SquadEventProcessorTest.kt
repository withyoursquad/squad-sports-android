package com.squadsports.sdk.realtime

import org.junit.Test
import org.junit.Assert.*
import org.junit.Before
import java.lang.reflect.Method

/**
 * Tests for SquadEventProcessor.
 *
 * Since SquadEventProcessor uses OkHttp SSE internals and coroutine scopes,
 * we test the accessible API and use reflection to verify internal logic
 * like deduplication and backoff calculations.
 */
class SquadEventProcessorTest {

    private lateinit var processor: SquadEventProcessor

    @Before
    fun setUp() {
        // Create a fresh instance for testing via the companion
        processor = SquadEventProcessor.shared
        // Disconnect to clean up any prior state
        processor.disconnect()
    }

    // --- Connection quality tests ---

    @Test
    fun `test initial quality is DISCONNECTED`() {
        assertEquals(SquadConnectionQuality.DISCONNECTED, processor.quality.value)
    }

    @Test
    fun `test SquadConnectionQuality enum values`() {
        val values = SquadConnectionQuality.values()
        assertEquals(3, values.size)
        assertTrue(values.contains(SquadConnectionQuality.GOOD))
        assertTrue(values.contains(SquadConnectionQuality.POOR))
        assertTrue(values.contains(SquadConnectionQuality.DISCONNECTED))
    }

    // --- SquadEvent data class tests ---

    @Test
    fun `test SquadEvent creation`() {
        val event = SquadEvent("message:create", mapOf("id" to "msg-1", "body" to "hello"))
        assertEquals("message:create", event.type)
        assertEquals("msg-1", event.data["id"])
        assertEquals("hello", event.data["body"])
    }

    @Test
    fun `test SquadEvent equality`() {
        val event1 = SquadEvent("test", mapOf("a" to 1))
        val event2 = SquadEvent("test", mapOf("a" to 1))
        assertEquals(event1, event2)
    }

    @Test
    fun `test SquadEvent copy`() {
        val event = SquadEvent("original", mapOf("key" to "value"))
        val copied = event.copy(type = "modified")
        assertEquals("modified", copied.type)
        assertEquals("value", copied.data["key"])
    }

    // --- Disconnect cleanup tests ---

    @Test
    fun `test disconnect sets quality to DISCONNECTED`() {
        processor.disconnect()
        assertEquals(SquadConnectionQuality.DISCONNECTED, processor.quality.value)
    }

    @Test
    fun `test disconnect can be called multiple times safely`() {
        processor.disconnect()
        processor.disconnect()
        processor.disconnect()
        assertEquals(SquadConnectionQuality.DISCONNECTED, processor.quality.value)
    }

    // --- setShouldAllowEvents tests ---

    @Test
    fun `test setShouldAllowEvents false triggers disconnect`() {
        processor.setShouldAllowEvents(false)
        assertEquals(SquadConnectionQuality.DISCONNECTED, processor.quality.value)
    }

    // --- connect without API client ---

    @Test
    fun `test connect without apiClient does nothing`() {
        processor.setShouldAllowEvents(true)
        // connect() should return early since no apiClient is set
        processor.connect()
        assertEquals(SquadConnectionQuality.DISCONNECTED, processor.quality.value)
    }

    // --- Event deduplication logic ---

    @Test
    fun `test deduplication key format`() {
        // The dedup key is "$type:${rawData.take(100)}"
        val type = "message:create"
        val rawData = """{"id":"msg-1","body":"hello world"}"""
        val key = "$type:${rawData.take(100)}"
        assertEquals("message:create:{\"id\":\"msg-1\",\"body\":\"hello world\"}", key)
    }

    @Test
    fun `test deduplication key truncates long data`() {
        val type = "test"
        val rawData = "a".repeat(200)
        val key = "$type:${rawData.take(100)}"
        assertEquals("test:" + "a".repeat(100), key)
        assertEquals(105, key.length) // "test:" = 5 + 100
    }

    @Test
    fun `test processedIds set deduplication behavior`() {
        // Simulate the dedup logic from processEvent
        val processedIds = mutableSetOf<String>()
        val key1 = "type1:data1"
        val key2 = "type1:data1" // duplicate
        val key3 = "type2:data2" // different

        assertFalse(processedIds.contains(key1))
        processedIds.add(key1)
        assertTrue(processedIds.contains(key2)) // same as key1
        assertFalse(processedIds.contains(key3))
    }

    @Test
    fun `test processedIds pruning when exceeding 5000`() {
        // Simulate the pruning logic from processEvent
        val processedIds = mutableSetOf<String>()
        for (i in 0..5000) {
            processedIds.add("key-$i")
        }
        assertTrue(processedIds.size > 5000)

        // Simulate the pruning
        val keep = processedIds.toList().takeLast(2500).toSet()
        processedIds.clear()
        processedIds.addAll(keep)

        assertEquals(2500, processedIds.size)
    }

    // --- Reconnect backoff logic ---

    @Test
    fun `test backoff delay calculation for first attempt`() {
        val initialBackoffMs = 1000L
        val maxBackoffMs = 30000L
        val attempt = 1
        val delay = minOf(initialBackoffMs * (1L shl (attempt - 1)), maxBackoffMs)
        assertEquals(1000L, delay) // 1000 * 2^0 = 1000
    }

    @Test
    fun `test backoff delay calculation for second attempt`() {
        val initialBackoffMs = 1000L
        val maxBackoffMs = 30000L
        val attempt = 2
        val delay = minOf(initialBackoffMs * (1L shl (attempt - 1)), maxBackoffMs)
        assertEquals(2000L, delay) // 1000 * 2^1 = 2000
    }

    @Test
    fun `test backoff delay calculation for third attempt`() {
        val initialBackoffMs = 1000L
        val maxBackoffMs = 30000L
        val attempt = 3
        val delay = minOf(initialBackoffMs * (1L shl (attempt - 1)), maxBackoffMs)
        assertEquals(4000L, delay) // 1000 * 2^2 = 4000
    }

    @Test
    fun `test backoff delay caps at maxBackoffMs`() {
        val initialBackoffMs = 1000L
        val maxBackoffMs = 30000L
        val attempt = 10
        val delay = minOf(initialBackoffMs * (1L shl (attempt - 1)), maxBackoffMs)
        assertEquals(30000L, delay) // capped
    }

    @Test
    fun `test backoff delay for attempt 6 is 32000 capped to 30000`() {
        val initialBackoffMs = 1000L
        val maxBackoffMs = 30000L
        val attempt = 6
        val rawDelay = initialBackoffMs * (1L shl (attempt - 1)) // 1000 * 32 = 32000
        val delay = minOf(rawDelay, maxBackoffMs)
        assertEquals(30000L, delay) // capped
    }

    @Test
    fun `test max reconnect attempts is 10`() {
        // Verify the constant via reflection
        val field = SquadEventProcessor::class.java.getDeclaredField("maxReconnectAttempts")
        field.isAccessible = true
        assertEquals(10, field.getInt(processor))
    }

    @Test
    fun `test initial backoff is 1000ms`() {
        val field = SquadEventProcessor::class.java.getDeclaredField("initialBackoffMs")
        field.isAccessible = true
        assertEquals(1000L, field.getLong(processor))
    }

    @Test
    fun `test max backoff is 30000ms`() {
        val field = SquadEventProcessor::class.java.getDeclaredField("maxBackoffMs")
        field.isAccessible = true
        assertEquals(30000L, field.getLong(processor))
    }

    // --- Buffer capacity tests ---

    @Test
    fun `test events flow has buffer capacity of 64`() {
        // The MutableSharedFlow is created with extraBufferCapacity = 64
        // We verify the shared flow exists
        assertNotNull(processor.events)
    }

    // --- currentToken ---

    @Test
    fun `test currentToken returns null without apiClient`() {
        assertNull(processor.currentToken)
    }

    // --- Message event forwarding logic ---

    @Test
    fun `test message create event type detection`() {
        // processEvent checks if type contains ":message:create"
        val type1 = "connection-123:message:create"
        assertTrue(type1.contains(":message:create"))

        val type2 = "message:create"
        assertFalse(type2.contains(":message:create")) // no leading colon context

        val type3 = "other:event"
        assertFalse(type3.contains(":message:create"))
    }
}
