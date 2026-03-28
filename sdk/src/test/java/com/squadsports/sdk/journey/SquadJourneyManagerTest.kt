package com.squadsports.sdk.journey

import org.junit.Test
import org.junit.Assert.*
import org.junit.Before
import kotlinx.coroutines.runBlocking

/**
 * Tests for SquadJourneyManager.
 * Validates milestone triggering, flag management, hydration, and listener callbacks.
 */
class SquadJourneyManagerTest {

    private lateinit var manager: SquadJourneyManager

    @Before
    fun setUp() {
        manager = SquadJourneyManager()
    }

    // --- Milestone tests ---

    @Test
    fun `triggerMilestone returns false on first trigger`() = runBlocking {
        val result = manager.triggerMilestone("first_login")
        assertFalse(result)
    }

    @Test
    fun `triggerMilestone returns true on repeat trigger`() = runBlocking {
        manager.triggerMilestone("first_login")
        val result = manager.triggerMilestone("first_login")
        assertTrue(result)
    }

    @Test
    fun `isMilestoneTriggered returns false for unknown milestone`() {
        assertFalse(manager.isMilestoneTriggered("unknown"))
    }

    @Test
    fun `isMilestoneTriggered returns true after trigger`() = runBlocking {
        manager.triggerMilestone("profile_complete")
        assertTrue(manager.isMilestoneTriggered("profile_complete"))
    }

    // --- Flag tests ---

    @Test
    fun `getFlag returns false for unknown flag`() {
        assertFalse(manager.getFlag("unknown_flag"))
    }

    @Test
    fun `setFlag and getFlag round-trip`() {
        manager.setFlag("dark_mode", true)
        assertTrue(manager.getFlag("dark_mode"))
    }

    @Test
    fun `setFlag can toggle value`() {
        manager.setFlag("feature_x", true)
        assertTrue(manager.getFlag("feature_x"))
        manager.setFlag("feature_x", false)
        assertFalse(manager.getFlag("feature_x"))
    }

    // --- Hydrate tests ---

    @Test
    fun `hydrate populates flags and milestones`() {
        manager.hydrate(
            flags = mapOf("events" to true, "wallet" to false),
            milestones = mapOf("onboarding" to true),
        )
        assertTrue(manager.getFlag("events"))
        assertFalse(manager.getFlag("wallet"))
        assertTrue(manager.isMilestoneTriggered("onboarding"))
    }

    @Test
    fun `hydrate with empty maps does not crash`() {
        manager.hydrate(emptyMap(), emptyMap())
        assertFalse(manager.getFlag("anything"))
        assertFalse(manager.isMilestoneTriggered("anything"))
    }

    // --- Listener tests ---

    @Test
    fun `listener onJourneyReady fires on hydrate`() {
        var readyCalled = false
        val listener = object : SquadJourneyManager.JourneyListener {
            override fun onJourneyReady() { readyCalled = true }
            override fun onMilestoneTriggered(milestone: String) {}
            override fun onFeatureFlagChanged(flag: String) {}
        }
        manager.addListener(listener)
        manager.hydrate(emptyMap(), emptyMap())
        assertTrue(readyCalled)
    }

    @Test
    fun `listener onMilestoneTriggered fires on first trigger`() = runBlocking {
        var triggeredMilestone: String? = null
        val listener = object : SquadJourneyManager.JourneyListener {
            override fun onJourneyReady() {}
            override fun onMilestoneTriggered(milestone: String) { triggeredMilestone = milestone }
            override fun onFeatureFlagChanged(flag: String) {}
        }
        manager.addListener(listener)
        manager.triggerMilestone("first_post")
        assertEquals("first_post", triggeredMilestone)
    }

    @Test
    fun `listener onMilestoneTriggered does not fire on repeat trigger`() = runBlocking {
        var callCount = 0
        val listener = object : SquadJourneyManager.JourneyListener {
            override fun onJourneyReady() {}
            override fun onMilestoneTriggered(milestone: String) { callCount++ }
            override fun onFeatureFlagChanged(flag: String) {}
        }
        manager.addListener(listener)
        manager.triggerMilestone("first_post")
        manager.triggerMilestone("first_post")
        assertEquals(1, callCount)
    }

    @Test
    fun `listener onFeatureFlagChanged fires on setFlag`() {
        var changedFlag: String? = null
        val listener = object : SquadJourneyManager.JourneyListener {
            override fun onJourneyReady() {}
            override fun onMilestoneTriggered(milestone: String) {}
            override fun onFeatureFlagChanged(flag: String) { changedFlag = flag }
        }
        manager.addListener(listener)
        manager.setFlag("polls", true)
        assertEquals("polls", changedFlag)
    }

    @Test
    fun `removeListener stops callbacks`() {
        var callCount = 0
        val listener = object : SquadJourneyManager.JourneyListener {
            override fun onJourneyReady() { callCount++ }
            override fun onMilestoneTriggered(milestone: String) {}
            override fun onFeatureFlagChanged(flag: String) {}
        }
        manager.addListener(listener)
        manager.hydrate(emptyMap(), emptyMap())
        assertEquals(1, callCount)

        manager.removeListener(listener)
        manager.hydrate(emptyMap(), emptyMap())
        assertEquals(1, callCount)
    }

    // --- Milestone sync tests ---

    @Test
    fun `milestone sync callback fires on trigger`() = runBlocking {
        var synced: String? = null
        manager.setMilestoneSync { milestone -> synced = milestone }
        manager.triggerMilestone("first_login")
        assertEquals("first_login", synced)
    }

    @Test
    fun `milestone sync callback does not fire on repeat trigger`() = runBlocking {
        var syncCount = 0
        manager.setMilestoneSync { _ -> syncCount++ }
        manager.triggerMilestone("first_login")
        manager.triggerMilestone("first_login")
        assertEquals(1, syncCount)
    }
}
