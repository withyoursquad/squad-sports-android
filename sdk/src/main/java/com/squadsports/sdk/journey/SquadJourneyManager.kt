package com.squadsports.sdk.journey

class SquadJourneyManager {
    companion object {
        val shared = SquadJourneyManager()
    }

    private val milestones = mutableMapOf<String, Boolean>()
    private val flags = mutableMapOf<String, Boolean>()
    private var onMilestoneSync: (suspend (String) -> Unit)? = null

    interface JourneyListener {
        fun onJourneyReady()
        fun onMilestoneTriggered(milestone: String)
        fun onFeatureFlagChanged(flag: String)
    }

    private val listeners = mutableListOf<JourneyListener>()

    fun addListener(listener: JourneyListener) { listeners.add(listener) }
    fun removeListener(listener: JourneyListener) { listeners.remove(listener) }

    fun hydrate(flags: Map<String, Boolean>, milestones: Map<String, Boolean>) {
        this.flags.putAll(flags)
        this.milestones.putAll(milestones)
        listeners.forEach { it.onJourneyReady() }
    }

    suspend fun triggerMilestone(milestone: String): Boolean {
        if (milestones[milestone] == true) return true
        milestones[milestone] = true
        listeners.forEach { it.onMilestoneTriggered(milestone) }
        onMilestoneSync?.invoke(milestone)
        return false
    }

    fun isMilestoneTriggered(milestone: String): Boolean = milestones[milestone] == true
    fun getFlag(flag: String): Boolean = flags[flag] ?: false
    fun setFlag(flag: String, value: Boolean) {
        flags[flag] = value
        listeners.forEach { it.onFeatureFlagChanged(flag) }
    }
    fun setMilestoneSync(sync: suspend (String) -> Unit) { onMilestoneSync = sync }
}
