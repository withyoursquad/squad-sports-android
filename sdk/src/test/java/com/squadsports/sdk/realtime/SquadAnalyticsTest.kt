package com.squadsports.sdk.realtime

import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.test.runTest
import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.MockWebServer
import org.junit.Test
import org.junit.Assert.*
import org.junit.After
import org.junit.Before
import java.util.concurrent.TimeUnit

/**
 * Tests for SquadAnalytics.
 */
class SquadAnalyticsTest {

    @Before
    fun setUp() {
        resetAnalyticsState()
    }

    @After
    fun tearDown() {
        resetAnalyticsState()
    }

    /**
     * Resets SquadAnalytics internal state using reflection since it's an object (singleton).
     */
    private fun resetAnalyticsState() {
        val clazz = SquadAnalytics::class.java

        setPrivateField(clazz, "partnerId", null)
        setPrivateField(clazz, "userId", null)
        setPrivateField(clazz, "baseUrl", null)
        setPrivateField(clazz, "enabled", true)

        // Clear events list
        val eventsField = clazz.getDeclaredField("events")
        eventsField.isAccessible = true
        @Suppress("UNCHECKED_CAST")
        val events = eventsField.get(SquadAnalytics) as MutableList<*>
        events.clear()

        // Cancel flush job
        val flushJobField = clazz.getDeclaredField("flushJob")
        flushJobField.isAccessible = true
        val flushJob = flushJobField.get(SquadAnalytics) as? kotlinx.coroutines.Job
        flushJob?.cancel()
        flushJobField.set(SquadAnalytics, null)

        SquadAnalytics.customHandler = null
    }

    private fun setPrivateField(clazz: Class<*>, name: String, value: Any?) {
        val field = clazz.getDeclaredField(name)
        field.isAccessible = true
        field.set(SquadAnalytics, value)
    }

    private fun getEventsSize(): Int {
        val field = SquadAnalytics::class.java.getDeclaredField("events")
        field.isAccessible = true
        @Suppress("UNCHECKED_CAST")
        val events = field.get(SquadAnalytics) as MutableList<*>
        return events.size
    }

    // --- Configuration tests ---

    @Test
    fun `test configure sets partnerId and userId`() {
        SquadAnalytics.configure("partner-123", "user-456", "https://api.test.com", enabled = true)

        val clazz = SquadAnalytics::class.java
        val partnerField = clazz.getDeclaredField("partnerId")
        partnerField.isAccessible = true
        assertEquals("partner-123", partnerField.get(SquadAnalytics))

        val userField = clazz.getDeclaredField("userId")
        userField.isAccessible = true
        assertEquals("user-456", userField.get(SquadAnalytics))
    }

    @Test
    fun `test configure sets baseUrl`() {
        SquadAnalytics.configure("p", "u", "https://custom-url.com")

        val field = SquadAnalytics::class.java.getDeclaredField("baseUrl")
        field.isAccessible = true
        assertEquals("https://custom-url.com", field.get(SquadAnalytics))
    }

    @Test
    fun `test setUserId updates userId`() {
        SquadAnalytics.setUserId("new-user-id")

        val field = SquadAnalytics::class.java.getDeclaredField("userId")
        field.isAccessible = true
        assertEquals("new-user-id", field.get(SquadAnalytics))
    }

    // --- Event tracking tests ---

    @Test
    fun `test track adds event to internal list`() {
        SquadAnalytics.configure("partner", "user", "https://api.test.com")
        assertEquals(0, getEventsSize())

        SquadAnalytics.track("test_event", mapOf("key" to "value"))
        assertEquals(1, getEventsSize())
    }

    @Test
    fun `test track does nothing when disabled`() {
        SquadAnalytics.configure("partner", "user", "https://api.test.com", enabled = false)

        SquadAnalytics.track("test_event")
        assertEquals(0, getEventsSize())
    }

    @Test
    fun `test track adds userId and partnerId to properties`() {
        SquadAnalytics.configure("partner-x", "user-y", "https://api.test.com")

        val capturedProps = mutableMapOf<String, Any?>()
        SquadAnalytics.customHandler = { _, props -> capturedProps.putAll(props) }

        SquadAnalytics.track("test_event", mapOf("custom" to "prop"))

        assertEquals("user-y", capturedProps["userId"])
        assertEquals("partner-x", capturedProps["partnerId"])
        assertEquals("prop", capturedProps["custom"])
    }

    @Test
    fun `test trackScreen tracks screen_view event`() {
        SquadAnalytics.configure("partner", "user", "https://api.test.com")

        var capturedEvent = ""
        var capturedScreen = ""
        SquadAnalytics.customHandler = { event, props ->
            capturedEvent = event
            capturedScreen = props["screen"] as? String ?: ""
        }

        SquadAnalytics.trackScreen("HomeScreen")

        assertEquals("screen_view", capturedEvent)
        assertEquals("HomeScreen", capturedScreen)
    }

    @Test
    fun `test multiple tracks accumulate events`() {
        SquadAnalytics.configure("partner", "user", "https://api.test.com")

        SquadAnalytics.track("event1")
        SquadAnalytics.track("event2")
        SquadAnalytics.track("event3")

        assertEquals(3, getEventsSize())
    }

    // --- Custom handler tests ---

    @Test
    fun `test customHandler is invoked on track`() {
        SquadAnalytics.configure("partner", "user", "https://api.test.com")

        var handlerCalled = false
        SquadAnalytics.customHandler = { _, _ -> handlerCalled = true }

        SquadAnalytics.track("test_event")
        assertTrue(handlerCalled)
    }

    @Test
    fun `test customHandler receives event name`() {
        SquadAnalytics.configure("partner", "user", "https://api.test.com")

        var receivedEvent = ""
        SquadAnalytics.customHandler = { event, _ -> receivedEvent = event }

        SquadAnalytics.track("button_click")
        assertEquals("button_click", receivedEvent)
    }

    // --- Flush tests ---

    @Test
    fun `test flush with mock server sends events`() = runBlocking {
        val server = MockWebServer()
        server.enqueue(MockResponse().setResponseCode(200))
        server.start()

        val baseUrl = server.url("").toString().trimEnd('/')
        SquadAnalytics.configure("test-partner", "test-user", baseUrl)

        SquadAnalytics.track("test_event", mapOf("action" to "click"))
        SquadAnalytics.flush()

        resetAnalyticsState()

        val request = server.takeRequest(5, TimeUnit.SECONDS)
        assertNotNull("Expected request to MockWebServer but none arrived", request)
        assertEquals("POST", request!!.method)
        assertTrue(request.path!!.contains("/v2/partners/test-partner/analytics"))

        val body = request.body.readUtf8()
        assertTrue(body.contains("\"partnerId\":\"test-partner\"") || body.contains("\"partnerId\": \"test-partner\""))
        assertTrue(body.contains("\"events\""))

        server.shutdown()
    }

    @Test
    fun `test flush with empty events does nothing`() = runBlocking {
        val server = MockWebServer()
        server.start()
        val baseUrl = server.url("").toString().trimEnd('/')

        SquadAnalytics.configure("partner", "user", baseUrl)
        SquadAnalytics.flush()

        resetAnalyticsState()
        assertEquals(0, server.requestCount)
        server.shutdown()
    }

    @Test
    fun `test flush clears events even on server error`() = runBlocking {
        val server = MockWebServer()
        server.enqueue(MockResponse().setResponseCode(500))
        server.start()

        val baseUrl = server.url("").toString().trimEnd('/')
        SquadAnalytics.configure("partner", "user", baseUrl)

        SquadAnalytics.track("event1")
        SquadAnalytics.track("event2")
        assertEquals(2, getEventsSize())

        SquadAnalytics.flush()

        // flush() sends the request regardless of status code — events are cleared
        // (re-queue only happens on network/exception failures, not HTTP errors)
        assertEquals(0, getEventsSize())

        resetAnalyticsState()
        server.shutdown()
    }

    @Test
    fun `test flush without partnerId does not send`() = runBlocking {
        setPrivateField(SquadAnalytics::class.java, "partnerId", null)
        setPrivateField(SquadAnalytics::class.java, "baseUrl", "https://example.com")
        setPrivateField(SquadAnalytics::class.java, "enabled", true)

        // Manually add an event
        val eventsField = SquadAnalytics::class.java.getDeclaredField("events")
        eventsField.isAccessible = true
        @Suppress("UNCHECKED_CAST")
        val events = eventsField.get(SquadAnalytics) as MutableList<Triple<String, Map<String, Any?>, Long>>
        events.add(Triple("test", emptyMap(), System.currentTimeMillis()))

        SquadAnalytics.flush()

        assertEquals(0, getEventsSize())
    }

    @Test
    fun `test flush without baseUrl does not send`() = runBlocking {
        setPrivateField(SquadAnalytics::class.java, "partnerId", "partner")
        setPrivateField(SquadAnalytics::class.java, "baseUrl", null)
        setPrivateField(SquadAnalytics::class.java, "enabled", true)

        val eventsField = SquadAnalytics::class.java.getDeclaredField("events")
        eventsField.isAccessible = true
        @Suppress("UNCHECKED_CAST")
        val events = eventsField.get(SquadAnalytics) as MutableList<Triple<String, Map<String, Any?>, Long>>
        events.add(Triple("test", emptyMap(), System.currentTimeMillis()))

        SquadAnalytics.flush()
        assertEquals(0, getEventsSize())
    }

    // --- Auto-flush threshold test ---

    @Test
    fun `test batch size threshold is 20`() {
        SquadAnalytics.configure("partner", "user", "https://api.test.com")

        // Track 19 events — should NOT trigger auto-flush
        for (i in 1..19) {
            SquadAnalytics.track("event_$i")
        }
        assertEquals(19, getEventsSize())
    }
}
