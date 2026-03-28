package com.squadsports.sdk.realtime

import kotlinx.coroutines.*
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONArray
import org.json.JSONObject

/**
 * Analytics tracker for the Squad SDK on Android.
 */
object SquadAnalytics {

    private val events = mutableListOf<Triple<String, Map<String, Any?>, Long>>() // name, props, timestamp
    private var partnerId: String? = null
    private var userId: String? = null
    private var baseUrl: String? = null
    private var enabled = true
    private var flushJob: Job? = null
    private val scope = CoroutineScope(Dispatchers.IO + SupervisorJob())
    private val client = OkHttpClient()

    var customHandler: ((String, Map<String, Any?>) -> Unit)? = null

    fun configure(partnerId: String?, userId: String?, baseUrl: String?, enabled: Boolean = true) {
        this.partnerId = partnerId
        this.userId = userId
        this.baseUrl = baseUrl
        this.enabled = enabled

        // Auto-flush every 30s
        flushJob?.cancel()
        flushJob = scope.launch {
            while (isActive) {
                delay(30_000)
                flush()
            }
        }
    }

    fun track(event: String, properties: Map<String, Any?> = emptyMap()) {
        if (!enabled) return

        val props = properties.toMutableMap().apply {
            put("userId", userId)
            put("partnerId", partnerId)
        }

        synchronized(events) {
            events.add(Triple(event, props, System.currentTimeMillis()))
        }

        customHandler?.invoke(event, props)

        if (events.size >= 20) scope.launch { flush() }
    }

    fun trackScreen(name: String) = track("screen_view", mapOf("screen" to name))

    fun setUserId(id: String) { userId = id }

    suspend fun flush() {
        val batch: List<Triple<String, Map<String, Any?>, Long>>
        synchronized(events) {
            if (events.isEmpty()) return
            batch = events.toList()
            events.clear()
        }

        val pid = partnerId ?: return
        val url = baseUrl ?: return

        try {
            val eventsArray = JSONArray()
            batch.forEach { (name, props, ts) ->
                eventsArray.put(JSONObject().apply {
                    put("name", name)
                    put("properties", JSONObject(props.mapValues { it.value?.toString() ?: "" }))
                    put("timestamp", ts)
                })
            }

            val body = JSONObject().apply {
                put("partnerId", pid)
                put("events", eventsArray)
            }.toString().toRequestBody("application/json".toMediaType())

            val request = Request.Builder()
                .url("$url/v2/partners/$pid/analytics")
                .post(body)
                .build()

            client.newCall(request).execute().close()
        } catch (_: Exception) {
            // Re-queue on failure
            synchronized(events) { events.addAll(0, batch) }
        }
    }
}
