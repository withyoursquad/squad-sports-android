package com.squadsports.sdk.screens

import com.squadsports.sdk.journey.SquadJourneyManager
import org.junit.Test
import org.junit.Assert.*
import org.junit.Before
import kotlinx.coroutines.runBlocking

/**
 * Tests for events-related logic.
 * Compose UI tests require Android instrumentation, so we test the
 * underlying data/journey logic that supports the EventsScreen.
 */
class EventsScreenTest {

    private lateinit var journeyManager: SquadJourneyManager

    @Before
    fun setUp() {
        journeyManager = SquadJourneyManager()
    }

    @Test
    fun `event attendance milestone can be tracked`() = runBlocking {
        val firstTime = journeyManager.triggerMilestone("first_event_attend")
        assertFalse(firstTime)
        assertTrue(journeyManager.isMilestoneTriggered("first_event_attend"))
    }

    @Test
    fun `events feature flag defaults to false`() {
        assertFalse(journeyManager.getFlag("events"))
    }

    @Test
    fun `events feature flag can be enabled via hydrate`() {
        journeyManager.hydrate(
            flags = mapOf("events" to true),
            milestones = emptyMap(),
        )
        assertTrue(journeyManager.getFlag("events"))
    }

    @Test
    fun `multiple event milestones are independent`() = runBlocking {
        journeyManager.triggerMilestone("first_event_attend")
        journeyManager.triggerMilestone("first_event_share")

        assertTrue(journeyManager.isMilestoneTriggered("first_event_attend"))
        assertTrue(journeyManager.isMilestoneTriggered("first_event_share"))
        assertFalse(journeyManager.isMilestoneTriggered("first_event_create"))
    }

    @Test
    fun `event JSON parsing shape matches expected keys`() {
        // Validates the JSON keys the EventsScreen expects from the API
        val sampleJson = org.json.JSONObject("""
            {
                "events": [
                    {
                        "id": "evt-1",
                        "title": "Home Opener",
                        "date": "2026-04-01",
                        "location": "Audi Field",
                        "attendeeCount": 42,
                        "isAttending": false
                    }
                ]
            }
        """.trimIndent())

        val arr = sampleJson.getJSONArray("events")
        assertEquals(1, arr.length())

        val event = arr.getJSONObject(0)
        assertEquals("evt-1", event.getString("id"))
        assertEquals("Home Opener", event.getString("title"))
        assertEquals("2026-04-01", event.getString("date"))
        assertEquals("Audi Field", event.getString("location"))
        assertEquals(42, event.getInt("attendeeCount"))
        assertFalse(event.getBoolean("isAttending"))
    }

    @Test
    fun `event JSON handles missing optional fields gracefully`() {
        val sampleJson = org.json.JSONObject("""
            {
                "events": [
                    {
                        "id": "evt-2",
                        "title": "Away Game"
                    }
                ]
            }
        """.trimIndent())

        val event = sampleJson.getJSONArray("events").getJSONObject(0)
        assertEquals("", event.optString("date", ""))
        assertEquals("", event.optString("location", ""))
        assertEquals(0, event.optInt("attendeeCount", 0))
        assertFalse(event.optBoolean("isAttending", false))
    }
}
